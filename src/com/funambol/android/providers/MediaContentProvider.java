/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2003 - 2007 Funambol, Inc.
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

package com.funambol.android.providers;

import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;

import android.os.ParcelFileDescriptor;
import android.net.Uri;
import android.content.ContentProvider;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore.MediaColumns;

import com.funambol.platform.FileAdapter;
import com.funambol.util.Log;

/**
 * This is a private content provider meant to support media synchronization
 * in the Funambol sync client.
 * At the moment the provider is READ ONLY and it is basically a wrapper around
 * the MediaStore.
 */
public class MediaContentProvider extends ContentProvider {

    private static final String TAG = "MediaContentProvider";

    /**
     * These are the columns exported by this provider
     */
    public static final String _ID = BaseColumns._ID;
    public static final String DISPLAY_NAME = MediaColumns.DISPLAY_NAME;
    public static final String SIZE = MediaColumns.SIZE;
    public static final String DATE_ADDED = MediaColumns.DATE_ADDED;
    public static final String MIME_TYPE = MediaColumns.MIME_TYPE;

    protected static final String EXTERNAL_CONTENT_URI_STRING = "/external";
    protected static final String INTERNAL_CONTENT_URI_STRING = "/internal";

    private static final String CAMERA_BUCKET_NAME =
        Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera";

    private static final String CAMERA_BUCKET_NAME_HTC =
        Environment.getExternalStorageDirectory().toString() + "/DCIM/100Media";

    private static final String CAMERA_BUCKET_ID     = getBucketId(CAMERA_BUCKET_NAME);
    private static final String CAMERA_BUCKET_ID_HTC = getBucketId(CAMERA_BUCKET_NAME_HTC);

    private String type;
    private String bucketColumnName;
    private Uri mediaProviderExtUri;
    private Uri mediaProviderIntUri;

    public MediaContentProvider(String contentUri, Uri mediaProviderIntUri, Uri mediaProviderExtUri,
                                String type, String bucketColumnName) {

        this.mediaProviderExtUri = mediaProviderExtUri;
        this.mediaProviderIntUri = mediaProviderIntUri;
        this.type = type;
        this.bucketColumnName = bucketColumnName;
    }

    public boolean onCreate() {
        return true;
    }


    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    public Uri insert(Uri uri, ContentValues values) {
        return uri;
    }

    public String getType(Uri uri) {
        return type;
    }

    protected String getBucketColumn() {
        return bucketColumnName;
    }

    protected String[] getSupportedMimeTypes() {
        return null;
    }

    protected String[] getBucketIds() {
        String buckets[] = { CAMERA_BUCKET_ID, CAMERA_BUCKET_ID_HTC};
        return buckets;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        String allFields[] = {_ID, MIME_TYPE, getBucketColumn(), DATE_ADDED, SIZE, DISPLAY_NAME};

        Context context = getContext();
        Uri mediaUri    = createMediaUri(uri);
        StringBuffer whereClause = new StringBuffer();

        // Filter media in the proper bucket
        String[] buckets = getBucketIds();
        if (buckets != null) {
            whereClause.append("(");
            for(int i=0;i<buckets.length;++i) {
                String bucket = buckets[i];
                if (i>0) {
                    whereClause.append(" OR ");
                }
                whereClause.append(getBucketColumn()).append("='" + bucket + "'");
            }
            whereClause.append(")");
        }

        // Filter media by mime type
        String supportedMimeTypes[] = getSupportedMimeTypes();
        if (supportedMimeTypes != null) {
            whereClause.append(" AND (");
            for(int i=0; i<supportedMimeTypes.length; i++) {
                if(i>0) {
                    whereClause.append(" OR ");
                }
                whereClause.append(MediaColumns.MIME_TYPE).append("='" + supportedMimeTypes[i] + "'");
            }
            whereClause.append(") ");
        }

        if (selection != null) {
            whereClause.append(" AND ").append(selection);
        }
        Cursor cursor = context.getContentResolver().query(mediaUri, allFields, 
                whereClause.toString(), selectionArgs, sortOrder);

        // Filter and fix the required fields
        if (cursor != null && cursor.moveToFirst()) {

            MatrixCursor res = null;
            try {

                int numCols;
                if (projection == null) {
                    res = new MatrixCursor(allFields);
                    numCols = allFields.length;
                } else {
                    res = new MatrixCursor(projection);
                    numCols = projection.length;
                }

                do {

                    Object row[] = new Object[numCols];

                    for(int c=0;c<cursor.getColumnCount();++c) {
                        String columnName = cursor.getColumnName(c);
                        String value = cursor.getString(c);

                        // TODO: we should skip unsupported fields

                        if (SIZE.equals(columnName)) {
                            try {
                                long size = computeSize(cursor);
                                value = "" + size;
                            } catch (Exception e) {
                                Log.error(TAG, "Cannot get item size from file, using db size", e);
                            }
                        }
                        row[c] = value;
                    }
                    res.addRow(row);
                } while(cursor.moveToNext());
            } finally {
                cursor.close();
                cursor = res;
            }
        }

        return cursor;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        Context context = getContext();
        Uri mediaUri    = createMediaUri(uri);
        return context.getContentResolver().openFileDescriptor(mediaUri, mode);
    }

    public long computeSize(Cursor cursor) throws IOException {
        try {
            int sizeIndex = cursor.getColumnIndexOrThrow(MediaColumns.SIZE);
            int fileNameIndex = cursor.getColumnIndexOrThrow(MediaColumns.DISPLAY_NAME);
            int bucketIndex = cursor.getColumnIndexOrThrow(bucketColumnName);

            //Retrieving the file size provided by the MediaStore could produce an
            //error on some devices. The implementation forces the usage of the File
            //System to understand the property related to the file. Here it just
            //stores the value provided by the media store for debugging purposes
            long mediaStoreSize  = cursor.getLong(sizeIndex);

            //As additional control use the FileSystem to check the real size
            //of the media file.
            String fileName = cursor.getString(fileNameIndex);
            String bucket = cursor.getString(bucketIndex);
            String fullName = null;

            if (CAMERA_BUCKET_ID.equals(bucket)) {
                fullName = CAMERA_BUCKET_NAME + File.separator + fileName;
            } else if (CAMERA_BUCKET_ID_HTC.equals(bucket)) {
                fullName = CAMERA_BUCKET_NAME_HTC + File.separator + fileName;
            } else {
                Log.info(TAG, "Unknown bucket, use the db size");
            }

            long size;
            long fsSize = -1;
            if (fullName != null) {

                fsSize = getFileSize(fullName);

                if (mediaStoreSize != fsSize) {
                    Log.debug(TAG, "File size reported from provider is different than the one on the FS");
                }
                size = fsSize;
            } else {
                size = mediaStoreSize;
            }

            return size;

        } catch (Exception e) {
            Log.error(TAG, "Cannot create media descriptor: ", e);
            throw new IOException("Cannot get file size");
        }
    }

    private Uri createMediaUri(Uri inputUri) {

        String path = inputUri.getPath();
        Uri outputUri = null;

        if (path != null) {
            if (path.startsWith(INTERNAL_CONTENT_URI_STRING)) {
                outputUri = mediaProviderIntUri;
            } else if (path.startsWith(EXTERNAL_CONTENT_URI_STRING)) {
                outputUri = mediaProviderExtUri;
            }
        }

        if (outputUri == null) {
            throw new IllegalArgumentException("Unknown URI " + inputUri);
        }
        String lastPathSegment = inputUri.getLastPathSegment();
        if (!EXTERNAL_CONTENT_URI_STRING.equals("/" + lastPathSegment) &&
            !INTERNAL_CONTENT_URI_STRING.equals("/" + lastPathSegment))
        {
            outputUri = Uri.withAppendedPath(outputUri, lastPathSegment);
        }
        return outputUri;
    }

    /**
     * Check the size of a file on the file system
     * @param fullName the file name with path
     * @return long the size on the file long formatted
     * @throws IOException if the FileAdapter pointing to the file encounters a
     * problem
     */
    private long getFileSize(String fullName) throws IOException {
        //Manages to find the device's file using the FS
        long pictureSize;

        FileAdapter fa = new FileAdapter(fullName);
        pictureSize = fa.getSize();
        fa = null;
        return pictureSize;
    }


    protected static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }
}
