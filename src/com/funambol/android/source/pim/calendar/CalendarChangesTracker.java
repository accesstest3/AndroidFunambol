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
import java.util.Hashtable;
import java.io.IOException;

import android.database.Cursor;
import android.content.Context;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContentUris;
import android.net.Uri;

import com.funambol.android.source.AndroidChangesTracker;
import com.funambol.android.controller.AndroidController;

import com.funambol.storage.StringKeyValueStore;
import com.funambol.storage.StringKeyValuePair;
import com.funambol.syncml.client.CacheTracker;
import com.funambol.syncml.client.TrackerException;
import com.funambol.syncml.client.TrackableSyncSource;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.protocol.SyncMLStatus;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.util.Log;

/**
 * This interface can be used by TrackableSyncSource to detect changes occourred
 * since the last synchronization. The API provides a basic implementation
 * in CacheTracker which detects changes comparing fingerprints.
 * Client can implement this interface and use it in the TrackableSyncSource if more
 * efficient methods are available.
 */
public class CalendarChangesTracker extends AndroidChangesTracker {

    private static final String TAG_LOG = "CalendarChangesTracker";

    protected ContentResolver resolver;

    protected CalendarManager calendarManager;


    public CalendarChangesTracker(Context context, StringKeyValueStore status, CalendarManager calendarManager) {
        super(context, status);
        this.resolver = context.getContentResolver();
        this.calendarManager = calendarManager;
    }


    @Override
    public void begin(int syncMode) throws TrackerException {

        Log.trace(TAG_LOG, "beginning changes computation");

        CalendarManager.CalendarDescriptor calendar = calendarManager.getDefaultCalendar();
        if (calendar == null) {
            throw new TrackerException("Cannot track undefined calendar");
        }
        long calendarId = calendar.getId();

        this.syncMode = syncMode;
    
        newItems      = new Hashtable();
        updatedItems  = new Hashtable();
        deletedItems  = new Hashtable();

        // Initialize the status
        try {
            this.status.load();
        } catch (Exception ex) {
            Log.debug(TAG_LOG, "Cannot load tracker status: " + ex.toString());
            throw new TrackerException("Cannot load tracker status");
        }

        if(syncMode == SyncML.ALERT_CODE_FAST ||
           syncMode == SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT ||
           syncMode == SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW) {

            // Initialize the items snapshot
            String cols[] = getEventCols();

            // Grab only the rows which are sync dirty and belong to the
            // calendar being synchronized
            StringBuffer whereClause = new StringBuffer();
            whereClause.append(CalendarManager.Events.CALENDAR_ID).append("='").append(calendarId).append("'");

            // Get all the items belonging to the calendar being
            // synchronized in ascending order
            Cursor snapshot = resolver.query(CalendarManager.Events.CONTENT_URI, cols,
                    whereClause.toString(), null, CalendarManager.Events._ID + " ASC");

            // Get the snapshot column indexes
            int keyColumnIndex     = snapshot.getColumnIndexOrThrow(CalendarManager.Events._ID);

            Enumeration statusPairs = status.keyValuePairs();

            // We have two ordered sets to compare
            try {
                boolean snapshotDone = !snapshot.moveToFirst();
                boolean statusDone   = !statusPairs.hasMoreElements();
                String statusIdStr   = null;
                long statusId = -1;
                long snapshotId = -1;
                StringKeyValuePair kvp = null;
                do {
                    Log.trace(TAG_LOG, "snapshotDone = " + snapshotDone);
                    Log.trace(TAG_LOG, "statusDone = " + statusDone);

                    String snapshotIdStr = null;

                    if (!snapshotDone) {
                        // Get the item id in the snapshot
                        snapshotIdStr = snapshot.getString(keyColumnIndex);
                        snapshotId = Long.parseLong(snapshotIdStr);
                    } else {
                        snapshotId = -1;
                    }

                    statusDone = !statusPairs.hasMoreElements();
                    if (statusIdStr == null && !statusDone) {
                        kvp = (StringKeyValuePair)statusPairs.nextElement();
                        statusIdStr = kvp.getKey();
                        statusId = Long.parseLong(statusIdStr);
                    }

                    Log.trace(TAG_LOG, "snapshotId = " + snapshotId);
                    Log.trace(TAG_LOG, "statusId = " + statusId);

                    if (!statusDone || !snapshotDone) {
                        if (snapshotId == statusId) {
                            // Check if the item is updated. Note that on
                            // Android LUIDs can be reused, therefore it is
                            // possible that if the user removes the last item
                            // and add a new one, we detect a replace instead of
                            // a pair delete/add
                            Log.trace(TAG_LOG, "Same id: " + statusId);

                            if (isDirty(snapshot, kvp)) {
                                Log.trace(TAG_LOG, "Found updated item: " + snapshotId);
                                updatedItems.put(snapshotIdStr, computeFingerprint(snapshotIdStr, snapshot));
                            }
                            // Advance both pointers
                            snapshotDone = !snapshot.moveToNext();
                            statusIdStr = null;
                        } else if ((snapshotId < statusId && snapshotId != -1) || statusDone) {
                            Log.trace(TAG_LOG, "Found new item: " + snapshotId);
                            // This item was added
                            newItems.put(snapshotIdStr, computeFingerprint(snapshotIdStr, snapshot));
                            // Move only the snapshot pointer
                            snapshotDone = !snapshot.moveToNext();
                        } else {
                            Log.trace(TAG_LOG, "Found deleted item: " + statusId);
                            // The item was deleted
                            deletedItems.put(statusIdStr, "1");
                            // Move only the status pointer
                            statusIdStr = null;
                        }
                    }
                } while(!statusDone || !snapshotDone);
            } catch (Exception e) {
                Log.error(TAG_LOG, "Cannot compute changes", e);
                throw new TrackerException(e.toString());
            } finally {
                snapshot.close();
            }
        } else if(syncMode == SyncML.ALERT_CODE_SLOW ||
                  syncMode == SyncML.ALERT_CODE_REFRESH_FROM_CLIENT ||
                  syncMode == SyncML.ALERT_CODE_REFRESH_FROM_SERVER) {
            // Reset the status when performing a slow sync
            try {
                status.reset();
            } catch(IOException ex) {
                Log.error(TAG_LOG, "Cannot reset status", ex);
                throw new TrackerException("Cannot reset status");
            }
        }
    }

    @Override
    public void setItemStatus(String key, int itemStatus) throws TrackerException {
        if(syncMode == SyncML.ALERT_CODE_SLOW ||
           syncMode == SyncML.ALERT_CODE_REFRESH_FROM_CLIENT) {
            if(status.get(key) == null) {
                status.add(key, "1");
            }
        } else if (isSuccess(itemStatus) && itemStatus != SyncMLStatus.CHUNKED_ITEM_ACCEPTED) {
            // We must update the fingerprint store with the value of the
            // fingerprint at the last sync
            if (newItems.get(key) != null) {
                // Update the fingerprint
                status.add(key, "1");
            } else if (deletedItems.get(key) != null) {
                // Update the fingerprint
                status.remove(key);
            }
        }
        // If the item was succesfully synchronized, then we clear the dirty
        // flag
        if (isSuccess(itemStatus) && itemStatus != SyncMLStatus.CHUNKED_ITEM_ACCEPTED) {
            clearSyncDirty(key);
        }
    }

    @Override
    public boolean removeItem(SyncItem item) throws TrackerException {

        Log.trace(TAG_LOG, "Removing item " + item.getKey());

        if (item.getState() == SyncItem.STATE_DELETED) {
            status.remove(item.getKey());
        } else {
            Log.trace(TAG_LOG, "Updating status");
            if (item.getState() == SyncItem.STATE_NEW) {
                status.add(item.getKey(), "1");
            }
            Log.trace(TAG_LOG, "Updating events table");
            // Removing item from the list of changes
            String key = item.getKey();
            clearSyncDirty(key);
        }
        return true;
    }

    protected boolean isDirty(Cursor snapshot, StringKeyValuePair kvp) throws IOException {
        int syncDirtyColIdx = snapshot.getColumnIndexOrThrow(CalendarManager.Events._SYNC_DIRTY);
        String dirty = snapshot.getString(syncDirtyColIdx);
        return "1".equals(dirty);
    }

    protected String computeFingerprint(String key, Cursor cursor) throws IOException {
        return "1";
    }

    protected String[] getEventCols() {
        String cols[] = {CalendarManager.Events._ID, CalendarManager.Events._SYNC_DIRTY};
        return cols;
    }

    private void clearSyncDirty(String key) {
        // This item was succesfully synced, mark it as such
        long id = Long.parseLong(key);
        Log.trace(TAG_LOG, "Updating sync dirty flag for " + id);
        ContentValues values = new ContentValues();
        values.put(CalendarManager.Events._SYNC_DIRTY, "0");
        Uri uri = addCallerIsSyncAdapterFlag(CalendarManager.Events.CONTENT_URI);
        uri = ContentUris.withAppendedId(uri, id);
        int numUpdates = resolver.update(uri, values, null, null);
        Log.trace(TAG_LOG, "Number of updated rows = " + numUpdates);
    }

    private Uri addCallerIsSyncAdapterFlag(Uri uri) {
        Uri.Builder b = uri.buildUpon();
        b.appendQueryParameter(CalendarManager.CALLER_IS_SYNCADAPTER, "true");
        return b.build();
    }
}

