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

package com.funambol.android.source.pim.contact;

import java.util.Enumeration;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.ContentResolver;

import com.funambol.android.source.pim.PIMSyncSource;

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
public class ContactSyncSource extends PIMSyncSource<Contact> {
    private static final String TAG = "ContactSyncSource";

    /**
     * ContactSyncSource constructor: initialize source config.
     * 
     * @param config
     * @param tracker
     * @param context
     * @param configuration
     * @param appSource
     */
    public ContactSyncSource(SourceConfig config, ChangesTracker tracker, Context context,
                             Configuration configuration, AppSyncSource appSource) {
        super(config, tracker, context, configuration, appSource, new ContactManager(context));
    }

    /** Logs the new item from the server. */
    @Override
    public int addItem(SyncItem item) {
        Log.info(TAG, "New item " + item.getKey() + " from server.");

        byte[] itemContent = item.getContent();

        if(itemContent.length > 2048) {
            String logContent = new String(itemContent, 0, 2048);
            Log.trace(TAG, logContent);
            Log.trace(TAG, "Item content is too big, logging 2KB only");
        } else {
            String logContent = new String(itemContent);
            Log.trace(TAG, logContent);
        }

        if (syncMode == SyncML.ALERT_CODE_REFRESH_FROM_CLIENT || syncMode == SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT) {
            Log.error(TAG, "Server is trying to update items for a one way sync! " + "(syncMode: " + syncMode + ")");
            return SyncMLStatus.GENERIC_ERROR;
        }

        // Create a new contact
        try {
            Contact c = new Contact();
            c.setVCard(itemContent);
            long id = dm.add(c);
            // Update the LUID for the mapping
            item.setKey(""+id);

            // Call super method
            super.addItem(item);

            return SyncMLStatus.SUCCESS;
        } catch (Throwable t) {
            t.printStackTrace();
            Log.error(TAG, "Cannot save contact", t);
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

        // Create a new contact
        try {
            // If the contact does not exist already, then this is like a new
            long id;
            try {
                id = Long.parseLong(item.getKey());
            } catch (Exception e) {
                Log.error(TAG, "Invalid contact id " + item.getKey(), e);
                return SyncMLStatus.GENERIC_ERROR;
            }

            Contact c = new Contact();
            c.setVCard(item.getContent());
            dm.update(id, c);

            // Call super method
            super.updateItem(item);

            return SyncMLStatus.SUCCESS;
        } catch (Throwable t) {
            Log.error(TAG, "Cannot update contact ", t);
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
                Log.error(TAG, "Invalid contact id " + item.getKey(), e);
                throw new SyncException(SyncException.CLIENT_ERROR, "Invalid item id: " + item.getKey());
            }
            Contact c = dm.load(id);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            c.toVCard(os, true);
            SyncItem res = new SyncItem(item);
            res.setContent(os.toByteArray());
            return res;
        } catch (Throwable t) {
            Log.error(TAG, "Cannot get contact content for " + item.getKey(), t);
            throw new SyncException(SyncException.CLIENT_ERROR, "Cannot get contact content");
        }
    }

    @Override
    public void setItemStatus(String key, int status) throws SyncException {
        super.setItemStatus(key, status);
        if (SyncMLStatus.isSuccess(status) && status != SyncMLStatus.CHUNKED_ITEM_ACCEPTED) {
            long id = Long.parseLong(key);
            ((ContactManager)dm).finalizeCommand(id);
        }
    }

}
