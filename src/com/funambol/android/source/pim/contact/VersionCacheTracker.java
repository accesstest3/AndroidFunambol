/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2008 Funambol, Inc.
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

import android.accounts.Account;
import java.util.Hashtable;
import java.util.Enumeration;

import com.funambol.util.Log;
import com.funambol.storage.StringKeyValueStore;
import com.funambol.storage.StringKeyValuePair;
import com.funambol.syncml.client.TrackerException;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.protocol.SyncMLStatus;
import com.funambol.syncml.protocol.SyncML;

import android.content.Context;
import android.provider.ContactsContract.RawContacts;
import android.database.Cursor;
import android.content.ContentResolver;
import com.funambol.android.controller.AndroidController;
import com.funambol.android.source.AndroidChangesTracker;
import java.io.IOException;

/**
 * <code>VersionCacheTracker</code> extends the <code>CacheTracker</code>
 * implementation and overloads the changes retrieving and the fingerprint
 * computing algorithms.
 *
 * The fingerprint used to retrieve changes is the contact version.
 */
public class VersionCacheTracker extends AndroidChangesTracker {

    private final String LOG_TAG = "VersionCacheTracker";

    private ContentResolver resolver;

    /**
     * Creates a VersionCacheTracker. The constructor detects changes so that
     * the method to get the changes can be used right away
     *
     * @param status is the key value store with stored data
     * @param context the application Context
     */
    public VersionCacheTracker(StringKeyValueStore status, Context context) {
        super(context, status);
        resolver = context.getContentResolver();
    }

    /**
     * Implements the changes tracking logic. It retrieves changes based to the
     * cache of the items version (the status).
     * @throws TrackerException
     */
    @Override
    public void begin(int syncMode) throws TrackerException {

        Log.trace(LOG_TAG, "begin");

        // Init account info
        Account account = AndroidController.getNativeAccount();
        String  accountType = null;
        String  accountName = null;
        
        if(account != null) {
            accountType = account.type;
            accountName = account.name;
        }

        this.syncMode = syncMode;
        
        newItems      = new Hashtable();
        updatedItems  = new Hashtable();
        deletedItems  = new Hashtable();

        // Initialize the status
        try {
            this.status.load();
        } catch (Exception ex) {
            Log.debug(LOG_TAG, "Cannot load tracker status: " + ex.toString());
            throw new TrackerException("Cannot load tracker status");
        }

        if(syncMode == SyncML.ALERT_CODE_FAST ||
           syncMode == SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT ||
           syncMode == SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW) {

            // Initialize the items snapshot
            String cols[] = {RawContacts._ID, RawContacts.VERSION, RawContacts.DELETED};

            StringBuffer whereClause = new StringBuffer();
            if(accountName != null && accountType != null) {
                whereClause.append(RawContacts.ACCOUNT_NAME).append("='").append(accountName).append("'");
                whereClause.append(" AND ");
                whereClause.append(RawContacts.ACCOUNT_TYPE).append("='").append(accountType).append("'");
            }
            Cursor snapshot = resolver.query(RawContacts.CONTENT_URI, cols,
                    whereClause.toString(), null, RawContacts._ID + " ASC");
            try {
                // Get the snapshot column indexes
                int keyColumnIndex     = snapshot.getColumnIndexOrThrow(RawContacts._ID);
                int valueColumnIndex   = snapshot.getColumnIndexOrThrow(RawContacts.VERSION);
                int deletedColumnIndex = snapshot.getColumnIndexOrThrow(RawContacts.DELETED);

                // Get the status key/value pairs
                Enumeration statusKVPs = status.keyValuePairs();

                snapshot.moveToFirst();

                StringKeyValuePair statusKVP = null;
                String statusKey       = null;
                String statusVersion   = null;
                String snapshotKey     = null;
                String snapshotVersion = null;

                // Iterate on the status elements
                while(statusKVPs.hasMoreElements()) {

                    // Get the status key/value
                    statusKVP = (StringKeyValuePair)statusKVPs.nextElement();
                    statusKey     = statusKVP.getKey();
                    statusVersion = statusKVP.getValue();

                    boolean found = false;

                    // Look for the same element in the snapshot
                    while(!snapshot.isAfterLast() && !found) {

                        // Get the snapshot key/value
                        snapshotKey     = snapshot.getString(keyColumnIndex);
                        snapshotVersion = snapshot.getString(valueColumnIndex);
                        int snapshotDeleted = snapshot.getInt(deletedColumnIndex);

                        if(snapshotKey.equals(statusKey)) {
                            found = true;
                            if(snapshotDeleted == 1) {
                                Log.debug(LOG_TAG, "Found a deleted item with key: " + statusKey);
                                deletedItems.put(statusKey, statusVersion);
                            } else if(!statusVersion.equals(snapshotVersion)) {
                                Log.debug(LOG_TAG, "Found an updated item with key: " + snapshotKey);
                                updatedItems.put(snapshotKey, snapshotVersion);
                            }
                        } else if(!(snapshotDeleted == 1)) {
                            Log.debug(LOG_TAG, "Found a new item with key: " + snapshotKey);
                            newItems.put(snapshotKey, snapshotVersion);
                        }
                        snapshot.moveToNext();
                    }
                    if(!found) {
                        Log.debug(LOG_TAG, "Found a deleted item with key: " + statusKey);
                        deletedItems.put(statusKey, statusVersion);
                    }
                }
                while(!snapshot.isAfterLast()) {

                    snapshotKey     = snapshot.getString(keyColumnIndex);
                    snapshotVersion = snapshot.getString(valueColumnIndex);
                    int snapshotDeleted = snapshot.getInt(deletedColumnIndex);

                    if(!(snapshotDeleted == 1)) {
                        Log.debug(LOG_TAG, "Found a new item with key: " + snapshotKey);
                        newItems.put(snapshotKey, snapshotVersion);
                    }
                    snapshot.moveToNext();
                }
            } finally {
                if(snapshot != null) {
                    snapshot.close();
                }
            }
        } else if(syncMode == SyncML.ALERT_CODE_SLOW ||
                  syncMode == SyncML.ALERT_CODE_REFRESH_FROM_CLIENT ||
                  syncMode == SyncML.ALERT_CODE_REFRESH_FROM_SERVER) {
            // Reset the status when performing a slow sync
            try {
                status.reset();
            } catch(IOException ex) {
                Log.error(LOG_TAG, "Cannot reset status", ex);
                throw new TrackerException("Cannot reset status");
            }
        }
    }

    /**
     * Computes the item fingerprint using the Andoid Contact Version. The
     * Version attribute is incremented everytime a Contact is modified.
     * 
     * @param item The SyncItem object.
     * @return The item version.
     */
    @Override
    protected String computeFingerprint(SyncItem item) {

        Log.trace(LOG_TAG, "computeFingerprint");

        String fp = "1";
        
        String cols[] = {RawContacts.VERSION};
        Cursor versionCursor = resolver.query(RawContacts.CONTENT_URI, cols,
                RawContacts._ID + " = \"" + item.getKey() + "\"", null, null);
       
        if(versionCursor.getCount() > 0) {
            versionCursor.moveToFirst();
            fp = versionCursor.getString(0);
        } 
        versionCursor.close();
        return fp;
    }

    @Override
    public void setItemStatus(String key, int itemStatus) throws TrackerException {
        Log.trace("[CacheTracker.setItemStatus] " + key + "," + itemStatus);

        if(syncMode == SyncML.ALERT_CODE_SLOW ||
           syncMode == SyncML.ALERT_CODE_REFRESH_FROM_CLIENT) {
            SyncItem item = new SyncItem(key);
            if(status.get(key) != null) {
                status.update(key, computeFingerprint(item));
            } else {
                status.add(key, computeFingerprint(item));
            }
        } else if (isSuccess(itemStatus) && itemStatus != SyncMLStatus.CHUNKED_ITEM_ACCEPTED) {
            // We must update the fingerprint store with the value of the
            // fingerprint at the last sync
            if (newItems.get(key) != null) {
                // This is a new item
                String itemFP = (String)newItems.get(key);
                // Update the fingerprint
                status.add(key, itemFP);
            } else if (updatedItems.get(key) != null) {
                // This is a new item
                String itemFP = (String)updatedItems.get(key);
                // Update the fingerprint
                status.update(key, itemFP);
            } else if (deletedItems.get(key) != null) {
                // Update the fingerprint
                status.remove(key);
            }
        }
    }
}
