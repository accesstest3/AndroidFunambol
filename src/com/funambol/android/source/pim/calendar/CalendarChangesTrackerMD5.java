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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;

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
import com.funambol.syncml.spds.SyncException;
import com.funambol.syncml.protocol.SyncMLStatus;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.util.Base64;
import com.funambol.util.Log;

/**
 * This interface can be used by TrackableSyncSource to detect changes occourred
 * since the last synchronization. The API provides a basic implementation
 * in CacheTracker which detects changes comparing fingerprints.
 * Client can implement this interface and use it in the TrackableSyncSource if more
 * efficient methods are available.
 */
public class CalendarChangesTrackerMD5 extends CalendarChangesTracker {

    private static final String TAG_LOG = "CalendarChangesTrackerMD5";

    public CalendarChangesTrackerMD5(Context context, StringKeyValueStore status, CalendarManager calendarManager) {
        super(context, status, calendarManager);
    }

    @Override
    public void setItemStatus(String key, int itemStatus) throws TrackerException {
        Log.trace(TAG_LOG, "setItemStatus " + key + "," + itemStatus);

        if(syncMode == SyncML.ALERT_CODE_SLOW ||
           syncMode == SyncML.ALERT_CODE_REFRESH_FROM_CLIENT) {
            try {
                if(status.get(key) != null) {
                    status.update(key, computeFingerprint(key));
                } else {
                    status.add(key, computeFingerprint(key));
                }
            } catch(Exception ex) {
                throw new TrackerException(ex.toString());
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
            // Save the status after each item
            try {
                this.status.save();
            } catch (Exception e) {
                // We try to let this error go trough as we save the status at
                // the end of the sync. Even though it is likely that operation
                // will fail as well and an exception will be thrown
                Log.error(TAG_LOG, "Cannot save tracker status, the status will be written at the end");
            }
        } else {
            // On error we do not change the fp so the change will
            // be reconsidered at the next sync
        }
        Log.trace(TAG_LOG, "status set for item: " + key);
    }

    @Override
    public boolean removeItem(SyncItem item) throws TrackerException {
        // In a cache sync source an item is removed from the cache
        // if it actually part of the cache. In such a case it will not
        // be reported as a new item
        String fp;
        boolean res = true;
        switch (item.getState()) {
            case SyncItem.STATE_NEW:
                try {
                    // We need the item as it has been stored on the device and
                    // not the original one. If we compute the fingerpring on
                    // the original item, we may detect an update at the next
                    // sync if the device does not support/store all fields
                    fp = computeFingerprint(item.getKey());
                    status.add(item.getKey(), fp);
                } catch(Exception ex) {
                    throw new TrackerException(ex.toString());
                }
                break;
            case SyncItem.STATE_UPDATED:
                try {
                    // We need the item as it has been stored on the device and
                    // not the original one. If we compute the fingerpring on
                    // the original item, we may detect an update at the next
                    // sync if the device does not support/store all fields
                    fp = computeFingerprint(item.getKey());
                    status.update(item.getKey(), fp);
                } catch(Exception ex) {
                    throw new TrackerException(ex.toString());
                }
                break;
            case SyncItem.STATE_DELETED:
                status.remove(item.getKey());
                break;
            default:
                Log.error(TAG_LOG, "Cache Tracker cannot remove item");
                res = false;
        }
        return res;
    }

    @Override
    protected boolean isDirty(Cursor snapshot, StringKeyValuePair kvp) throws IOException {
        //Calendar cal = calendarManager.load(snapshot);
        String origMd5 = kvp.getValue();
        String key     = kvp.getKey();

        String currentMd5 = computeFingerprint(key, snapshot);

        //Log.trace(TAG_LOG, "origMd5 = " + origMd5);
        //Log.trace(TAG_LOG, "currentMd5 = " + currentMd5);

        return !currentMd5.equals(origMd5);
    }

    @Override
    protected String computeFingerprint(String key, Cursor cursor) throws IOException {
        // Loading the calendar requires too much time. We use the raw fields
        // and compute their hash value
        try {
            StringBuffer rawEvent = new StringBuffer();

            for(int i=0;i<cursor.getColumnCount();++i) {
                String col = cursor.getString(i);
                rawEvent.append(col);
            }

            // We also need to check the reminders for this event
            int hasRem = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarManager.Events.HAS_ALARM));
            if (hasRem == 1) {
                String fields[] = { CalendarManager.Reminders.MINUTES };
                String whereClause = CalendarManager.Reminders.EVENT_ID + " = ?";
                //String whereClause = CalendarManager.Reminders.EVENT_ID + " = ?" + key;
                String whereArgs[] = { key };
                Cursor rems = resolver.query(CalendarManager.Reminders.CONTENT_URI, fields, whereClause, whereArgs, null);
                if (rems != null && rems.moveToFirst()) {
                    int mins = rems.getInt(rems.getColumnIndexOrThrow(CalendarManager.Reminders.MINUTES));
                    rawEvent.append(mins);
                } else {
                    Log.error(TAG_LOG, "Internal error: cannot find reminder for: " + key);
                }
                if (rems != null) {
                    /*
                    if(rems.moveToNext()) {
                        Log.error(TAG_LOG, "Only one reminder is currently supported, ignoring the others");
                    }
                    */
                    rems.close();
                }
            }
            return computeFingerprint(rawEvent.toString().getBytes());
        } catch (Exception e) {
            Log.error(TAG_LOG, "Cannot compute fingerprint", e);
            throw new IOException(e.toString());
        }
    }

    @Override
    protected String[] getEventCols() {
        // We need all the fields to compute the fingerprint based on the item
        // content
        return null;
    }

    protected String computeFingerprint(String key) throws IOException {

        Log.trace(TAG_LOG, "computeFingerprint");

        long k = Long.parseLong(key);
        Uri uri = ContentUris.withAppendedId(CalendarManager.Events.CONTENT_URI, k);
        Cursor cursor = resolver.query(uri, null, null, null, null);
        try {
            if(cursor.moveToFirst()) {
                return computeFingerprint(key, cursor);
            } else {
                Log.info(TAG_LOG, "Item not found, maybe it was deleted in the meantime");
                throw new IOException("Cannot find item " + key);
            }
        } finally {
            cursor.close();
        }
    }

    protected String computeFingerprint(SyncItem item) {
        Log.trace(TAG_LOG, "computeFingerprint");
        try {
            byte data[] = item.getContent();
            return computeFingerprint(data);
        } catch (Exception e) {
            Log.error(TAG_LOG, "Cannot compute fingerprint", e);
            return "";
        }
    }

    protected String computeFingerprint(byte data[]) throws Exception {

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(data);
        byte[] md5 = md.digest();
        String res = new String(Base64.encode(md5));
        Log.trace(TAG_LOG, "MD5=" + res);
        return res;
    }
}

