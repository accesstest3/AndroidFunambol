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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Date;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

import com.funambol.android.providers.MediaContentProvider;

import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.spds.SyncException;
import com.funambol.syncml.client.ChangesTracker;
import com.funambol.syncml.client.TrackableSyncSource;
import com.funambol.syncml.client.FileObject;
import com.funambol.syncml.client.FileObjectInputStream;
import com.funambol.syncml.protocol.SyncMLStatus;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.source.AppSyncSource;

import com.funambol.util.Base64;
import com.funambol.util.Log;


public abstract class MediaSyncSource extends TrackableSyncSource {

    private static final String TAG = "MediaSyncSource";
    protected Context context;
    protected Configuration configuration;
    protected AppSyncSource appSource;

    //------------------------------------------------------------- Constructors

    /**
     * MediaSyncSource constructor: initialize source config.
     * 
     * @param config
     * @param tracker
     * @param context
     * @param appSource
     * @param configuration
     */
    public MediaSyncSource(SourceConfig config, ChangesTracker tracker, Context context,
                           AppSyncSource appSource, Configuration configuration) {
        super(config, tracker);
        this.context = context;
        this.appSource = appSource;
        this.configuration = configuration;
    }

    @Override
    public void endSync() throws SyncException {
        super.endSync();
        // We must save the configuration here, because we are sure this code is
        // executed no matter where the sync is started from
        appSource.getConfig().saveSourceSyncConfig();
        appSource.getConfig().commit();
    }

    @Override
    public void setItemStatus(String key, int status) throws SyncException {
        // On device full, we show a proper error to the user and interrupt this
        // sync
        if (status == SyncMLStatus.DEVICE_FULL) {
            // The user reached his quota on the server
            Log.info(TAG, "Server is full");
            throw new SyncException(SyncException.DEVICE_FULL, "Server is full");
        }
        super.setItemStatus(key, status);
    }



    protected Enumeration getAllItemsKeys() throws SyncException {
        Log.trace(TAG, "getAllItemsKeys");

        Cursor cursor = context.getContentResolver().query(getProviderUri(), null, null, null, null);
        if (cursor == null) {
            Log.error(TAG, "Provider not found for: " + getProviderUri());
            throw new SyncException(SyncException.CLIENT_ERROR, "Media library not found, check SDCard.");
        }

        Vector items = new Vector();

        int idIndex = cursor.getColumnIndexOrThrow(getIDColumnName());

        try {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {

                int id = cursor.getInt(idIndex);
                items.addElement(Integer.toString(id));

                cursor.moveToNext();
            }
        } catch (Exception e) {
            Log.error(TAG, "Cannot get all items: ", e);
            throw new SyncException(SyncException.CLIENT_ERROR, "Cannot get all items");
        } finally {
            cursor.close();
        }
        return items.elements();
    }

    /**
     * Add an item to the local store. The item has already been received and
     * the content written into the output stream. The purpose of this method
     * is to simply apply the file object meta data properties to the file used
     * to store the output stream. In particular we set the proper name and
     * modification timestamp.
     *
     * @param item the received item
     * @throws SyncException if an error occurs while applying the file
     * attributes
     * 
     */
    @Override
    public int addItem(SyncItem item) throws SyncException {
        Log.error(TAG, "Not implemented yet");
        return SyncMLStatus.GENERIC_ERROR;
    }

    /**
     * Update an item in the local store. The item has already been received and
     * the content written into the output stream. The purpose of this method
     * is to simply apply the file object meta data properties to the file used
     * to store the output stream. In particular we set the proper name and
     * modification timestamp.
     *
     * @param item the received item
     * @throws SyncException if an error occurs while applying the file
     * attributes
     * 
     */
    @Override
    public int updateItem(SyncItem item) throws SyncException {
        String key = item.getKey();
        Log.error(TAG, "Not implemented yet");
        return SyncMLStatus.GENERIC_ERROR;
    }

    /**
     * Delete an item from the local store.
     * @param key the item key
     * @throws SyncException if the operation fails for any reason
     */
    @Override
    public int deleteItem(String key) throws SyncException {
        Log.error(TAG, "Not implemented yet");
        return SyncMLStatus.GENERIC_ERROR;
    }

    public void clearTracker() {
        Log.info(TAG, "Emptying the media tracker");
        tracker.empty();
    }

    protected void deleteAllItems()
    {
        Log.error(TAG, "Not implemented yet");
    }


    /**
     * TODO: is this still needed?
     * This is still kind of strange, we don't really need to get the item
     * content any longer but we just need to create a proper item from which
     * the content can be read
     */
    @Override
    protected SyncItem getItemContent(final SyncItem item) throws SyncException {
        // We send the item with the type of the SS
        SourceConfig config = getConfig();
        String type = config.getType();
        Cursor cursor = null;
        try {

            Uri uri = Uri.withAppendedPath(getProviderUri(), item.getKey());
            cursor = context.getContentResolver().query(uri, null, null, null, null);

            if (cursor == null) {
                throw new SyncException(SyncException.CLIENT_ERROR, "Cannot load content for: " + uri);
            }
            if (cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndexOrThrow(MediaContentProvider.DISPLAY_NAME);
                int sizeIdx = cursor.getColumnIndexOrThrow(MediaContentProvider.SIZE);
                int dateIdx = cursor.getColumnIndexOrThrow(MediaContentProvider.DATE_ADDED);

                String displayName = cursor.getString(nameIdx);
                long   size        = cursor.getLong(sizeIdx);
                long   added       = cursor.getLong(dateIdx);

                UriSyncItem usi = new UriSyncItem(uri, item.getKey(), type,
                                                  item.getState(), item.getParent(),
                                                  (int)size, displayName, added);

                return usi;
            } else {
                throw new SyncException(SyncException.CLIENT_ERROR, "Cannot load content for: " + uri);
            }

        } catch (Exception e) {
            throw new SyncException(SyncException.CLIENT_ERROR,
                                    "Cannot create UriSyncItem: " + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    protected class UriSyncItem extends SyncItem {
        private OutputStream os = null;
        private String prologue;
        private String epilogue;
        private Uri uri;
        private int itemSize;

        public UriSyncItem(Uri uri, String key, String type, char state,
                           String parent, int size, String displayName, long date) {

            super(key, type, state, parent);
            this.uri = uri;

            if (SourceConfig.FILE_OBJECT_TYPE.equals(getType())) {
                // Initialize the prologue
                FileObject fo = new FileObject();
                if (displayName != null) {
                    fo.setName(displayName);
                } else {
                    fo.setName(uri.toString());
                }
                //set the long to be multiplied x 1000 because the value is in
                //seconds
                fo.setModified(new Date(date*1000));
                prologue = fo.formatPrologue();
                // Initialize the epilogue
                epilogue = fo.formatEpilogue();
                // Compute the size of the FileObject
                this.itemSize = size;
                int bodySize = Base64.computeEncodedSize(size);
                // Set the size
                setObjectSize(prologue.length() + bodySize + epilogue.length());
            } else {
                setObjectSize(size);
            }

        }

        /**
         * Creates a new output stream to write to. If the item type is
         * FileDataObject, then the output stream takes care of parsing the XML
         * part of the object and it fills a FileObject that can be retrieved
         * later. @see FileObjectOutputStream for more details
         * Note that the output stream is unique, so that is can be reused
         * across different syncml messages.
         */
        @Override
        public OutputStream getOutputStream() throws IOException {
            return null;
        }

        /**
         * Creates a new input stream to read from. If the source is configured
         * to handle File Data Object, then the stream returns the XML
         * description of the file. @see FileObjectInputStream for more details.
         */
        @Override
        public InputStream getInputStream() throws IOException {
            InputStream is = context.getContentResolver().openInputStream(uri);
            // If this item is a file object, we shall use the
            // FileObjectOutputStream
            if (SourceConfig.FILE_OBJECT_TYPE.equals(getType())) {
                is = new FileObjectInputStream(prologue, is, epilogue,
                                               itemSize);
            }
            return is;
        }

        // If we do not reimplement the getContent, it will return a null
        // content, but this is not used in the ss, so there's no need to
        // redefine it
    }

    protected abstract Uri getProviderUri();
    protected abstract String getIDColumnName();
    protected abstract String getNameColumnName();
    protected abstract String getSizeColumnName();
    protected abstract String getDateAddedColumnName();
}

