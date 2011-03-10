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

package com.funambol.android.source.media;

import java.util.Enumeration;

import android.content.Context;
import android.net.Uri;
import android.database.Cursor;
import android.provider.MediaStore.MediaColumns;

import com.funambol.android.source.AndroidChangesTracker;
import com.funambol.android.providers.MediaContentProvider;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.source.AppSyncSource;
import com.funambol.storage.StringKeyValueStore;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.spds.SyncException;
import com.funambol.syncml.client.TrackerException;

import com.funambol.util.Log;

/**
 * An AndroidChangesTracker implementation that aims to keep trace of media
 * files' informations. The main method is used to compute the fingerprint for
 * a given media file.
 */
public abstract class MediaTracker extends AndroidChangesTracker {

    private static final String TAG_LOG = "MediaTracker";
    private AppSyncSource appSource;
    private Configuration configuration;

    /**
     * Default Constructor
     * @param context the Context object related to this tracker
     * @param status the StringKeyValueStore object for this tracker
     */
    public MediaTracker(Context context, StringKeyValueStore status,
                        AppSyncSource appSource, Configuration configuration) {
        super(context, status);
        this.appSource = appSource;
        this.configuration = configuration;
    }

    @Override
    public void begin(int syncMode) throws TrackerException {
        super.begin(syncMode);
        MediaAppSyncSourceConfig config = (MediaAppSyncSourceConfig)appSource.getConfig();
        // Filter outgoing items if the user enabled the filter
        if (! config.getIncludeOlderMedia()) {
            // Remove the old items from the list of changes
            Enumeration adds = newItems.keys();
            while(adds.hasMoreElements()) {
                String key = (String)adds.nextElement();
                Uri itemUri = Uri.withAppendedPath(getProviderUri(), key);
                // Get the item meta information
                Cursor cursor = context.getContentResolver().query(itemUri, null, null, null, null);
                if (cursor == null) {
                    Log.error(TAG_LOG, "Item not found " + itemUri);
                    throw new SyncException(SyncException.CLIENT_ERROR, "Cannot get meta info for item: " + itemUri);
                }

                int added = 0;
                try {
                    int addedIndex = cursor.getColumnIndexOrThrow(MediaColumns.DATE_ADDED);
                    cursor.moveToFirst();
                    if (cursor.getCount() != 1) {
                        Log.error(TAG_LOG, "Item not found, cannot check date for " + itemUri);
                        throw new TrackerException("Cannot get meta info for item: " + itemUri);
                    }
                    added = cursor.getInt(addedIndex);

                    if (added < (configuration.getFirstRunTimestamp()/1000)) {
                        Log.info(TAG_LOG, "Filtering out old items: " + key);
                        newItems.remove(key);
                    }
                } catch (Exception e) {
                    Log.error(TAG_LOG, "Cannot check if item must be filtered out", e);
                } finally {
                    cursor.close();
                }
            }
        }
    }


    /**
     * Calculates the firngerprint of a media item.
     * @param item the SyncItem object that contains the file for which the
     * computation must be done
     * @return String the String formatted fingerprint of the file related to
     * the given SyncItem object
     */
    @Override
    protected String computeFingerprint(SyncItem item) {
        Log.trace(TAG_LOG, "computeFingerprint");

        StringBuffer fp = new StringBuffer();

        Uri uri = Uri.withAppendedPath(getProviderUri(), item.getKey());
        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);

            String size;
            String added;
            if (cursor != null && cursor.moveToFirst()) {
                long s = cursor.getLong(cursor.getColumnIndexOrThrow(MediaContentProvider.SIZE));
                long a = cursor.getLong(cursor.getColumnIndexOrThrow(MediaContentProvider.DATE_ADDED));
                size = "" + s;
                added = "" + a;
            } else {
                Log.error(TAG_LOG, "Cannot compute fingerprint because the item cannot be found");
                size = "";
                added = "";
            }
            fp.append(added).append("-").append(size);

            String ret = fp.toString();
            Log.trace(TAG_LOG, "Fingerprint is: " + ret);
            return ret;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    protected abstract Uri getProviderUri();
}


