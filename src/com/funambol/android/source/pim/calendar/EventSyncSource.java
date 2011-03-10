/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2010 Funambol, Inc.
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
public class EventSyncSource extends CalendarSyncSource {
    private static final String TAG = "EventSyncSource";

    public EventSyncSource(SourceConfig config, ChangesTracker tracker, Context context,
                           Configuration configuration, AppSyncSource appSource, AbstractDataManager dm) {
        super(config, tracker, context, configuration, appSource, dm);
    }

    public void beginSync(int syncMode) {
        // If the calendar is not defined or no longer exists, then we revert to
        // the default one
        CalendarManager cm = (CalendarManager)dm;
        CalendarAppSyncSourceConfig asc = (CalendarAppSyncSourceConfig)appSource.getConfig();
        if (asc.getCalendarId() == -1 || cm.getCalendarDescription(asc.getCalendarId()) == null) {
            Log.info(TAG, "No calendar defined, set the default one");
            CalendarManager.CalendarDescriptor defaultCal = cm.getDefaultCalendar();
            if (defaultCal != null) {
                Log.info(TAG, "Found a default calendar, setting it in the config " + defaultCal.getName());
                asc.setCalendarId(defaultCal.getId());
            } else {
                // There are no default calendar, disable the source
                Log.error(TAG, "No default calendar, disabling calendar sync");
                // Warn the user that the sync cannot be performed
                //asc.setEnabled(false);
            }
        }
        // Now begin the real sync
        super.beginSync(syncMode);
    }
}

