/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2009 Funambol, Inc.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
 * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Funambol" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Funambol".
 */

package com.funambol.android.source.pim.calendar;

import java.util.Enumeration;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.ContentResolver;

import com.funambol.android.source.pim.PIMSyncSource;
import com.funambol.android.source.AbstractDataManager;

import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.client.TrackableSyncSource;
import com.funambol.syncml.client.ChangesTracker;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncException;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.protocol.SyncMLStatus;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.source.AppSyncSource;

import com.funambol.util.Log;

/**
 */
public class CalendarSyncSource extends PIMSyncSource<Calendar> {
    private static final String TAG = "CalendarSyncSource";

    /**
     * CalendarSyncSource constructor: initialize source config.
     * 
     * @param config
     * @param tracker
     * @param context
     * @param configuration
     * @param appSource
     */
    public CalendarSyncSource(SourceConfig config, ChangesTracker tracker, Context context,
                              Configuration configuration, AppSyncSource appSource, AbstractDataManager dm) {
        super(config, tracker, context, configuration, appSource, dm);
    }

    public void beginSync(int syncMode) throws SyncException {
        // If the calendar is not defined or no longer exists, then we revert to
        // the default one
        if (dm instanceof CalendarManager) {
            CalendarManager cm = (CalendarManager)dm;
            CalendarAppSyncSourceConfig asc = (CalendarAppSyncSourceConfig)appSource.getConfig();
            if (asc.getCalendarId() == -1 || cm.getCalendarDescription(asc.getCalendarId()) == null) {
                Log.info(TAG, "No calendar defined, set the default one");
                CalendarManager.CalendarDescriptor defaultCal = cm.getDefaultCalendar();
                if (defaultCal != null) {
                    Log.info(TAG, "Found a default calendar, setting it in the config " + defaultCal.getName());
                    asc.setCalendarId(defaultCal.getId());
                    asc.save();
                } else {
                    // This should never happen, but just in case...
                    Log.error(TAG, "No default calendar, disabling calendar sync");
                    throw new SyncException(SyncException.CLIENT_ERROR, "Undefined calendar");
                }
            }
        }
        super.beginSync(syncMode);
    }

    /** Logs the new item from the server. */
    @Override
    public int addItem(SyncItem item) {
        Log.info(TAG, "New item " + item.getKey() + " from server.");
        Log.trace(TAG, new String(item.getContent()));

        if (syncMode == SyncML.ALERT_CODE_REFRESH_FROM_CLIENT || syncMode == SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT) {
            Log.error(TAG, "Server is trying to update items for a one way sync! " + "(syncMode: " + syncMode + ")");
            return SyncMLStatus.GENERIC_ERROR;
        }

        // Create a new calendar
        try {
            Calendar cal = new Calendar();
            cal.setVCalendar(item.getContent());
            long id = dm.add(cal);
            // Update the LUID for the mapping
            item.setKey(""+id);

            // Call super method
            super.addItem(item);

            return SyncMLStatus.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(TAG, "Cannot save calendar", e);
            return SyncMLStatus.GENERIC_ERROR;
        }
    }

    /** Update a given SyncItem stored on the source backend */
    @Override
    public int updateItem(SyncItem item) {
        Log.info(TAG, "Updated item " + item.getKey() + " from server.");

        if (syncMode == SyncML.ALERT_CODE_REFRESH_FROM_CLIENT || syncMode == SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT) {
            Log.error(TAG, "Server is trying to update items for a one way sync! " + "(syncMode: " + syncMode + ")");
            return SyncMLStatus.GENERIC_ERROR;
        }

        // Create a new calendar
        try {
            // If the calendar does not exist already, then this is like a new
            long id;
            try {
                id = Long.parseLong(item.getKey());
            } catch (Exception e) {
                Log.error(TAG, "Invalid calendar id " + item.getKey(), e);
                return SyncMLStatus.GENERIC_ERROR;
            }

            Calendar c = new Calendar();
            c.setVCalendar(item.getContent());
            dm.update(id, c);

            // Call super method
            super.updateItem(item);

            return SyncMLStatus.SUCCESS;
        } catch (Exception e) {
            Log.error(TAG, "Cannot update calendar ", e);
            return SyncMLStatus.GENERIC_ERROR;
        }
    }

    @Override
    protected SyncItem getItemContent(final SyncItem item) throws SyncException {
        try {
            // Load all the item content
            long id;
            try {
                id = Long.parseLong(item.getKey());
            } catch (Exception e) {
                Log.error(TAG, "Invalid calendar id " + item.getKey(), e);
                throw new SyncException(SyncException.CLIENT_ERROR, "Invalid item id: " + item.getKey());
            }
            Calendar c = dm.load(id);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            c.toVCalendar(os, true);
            SyncItem res = new SyncItem(item);
            res.setContent(os.toByteArray());
            return res;
        } catch (Exception e) {
            Log.error(TAG, "Cannot get calendar content for " + item.getKey(), e);
            throw new SyncException(SyncException.CLIENT_ERROR, "Cannot get calendar content");
        }
    }
}
