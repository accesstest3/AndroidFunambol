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

package com.funambol.syncml.client;

import java.util.Vector;
import java.util.Date;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.funambol.syncml.spds.SyncException;
import com.funambol.syncml.spds.SyncListener;
import com.funambol.syncml.spds.SyncConfig;
import com.funambol.syncml.spds.DeviceConfig;

import com.funambol.syncml.protocol.SyncMLStatus;

import com.funambol.platform.FileAdapter;
import com.funambol.platform.HttpConnectionAdapter;

import com.funambol.util.ConnectionManager;
import com.funambol.util.StringUtil;
import com.funambol.util.Base64;
import com.funambol.util.Log;

/**
 * This class is a file uploader for a two phases sync source. This class in
 * particular is responsible for uploading an item and notifying a SyncListener
 * about what is going on.
 * It is important to note that this uploader is specific for the sync and it is
 * not a generic uploader.
 * During the upload phase the process can be interrupted, but this class must
 * be derived and uploadCancelled shall be reimplemented to notify when the sync
 * must be stopped.
 */
public class HttpUploader {

    private static final String TAG_LOG = "HttpUploader";

    private static final String JSESSIONID_PARAM   = "jsessionid";
    private static final String ACTION_PARAM       = "action";
    private static final String ACTION_PARAM_VALUE = "content-upload";

    private static final String AUTH_HEADER           = "Authorization";
    private static final String AUTH_BASIC            = "Basic";
    private static final String CONTENT_TYPE_HEADER   = "Content-Type";
    private static final String CONTENT_LENGTH_HEADER = "Content-Length";

    private static final String X_DEVID_HEADER     = "x-funambol-syncdeviceid";
    private static final String X_FILE_SIZE_HEADER = "x-funambol-file-size";
    private static final String X_LUID_HEADER      = "x-funambol-luid";
    
    private static final String JSESSIONID_HEADER = "JSESSIONID";
    private static final String COOKIE_HEADER     = "Set-Cookie";

    private static final int DEFAULT_CHUNK_SIZE = 4096;
    protected int chunkSize = DEFAULT_CHUNK_SIZE;

    // By default we authenticate via http basic authentication in the http
    // request headers
    private boolean authInQueryString = false;

    private String url;
    private SyncListener lis;
    private String uploadUrl;
    private String jsessionId;
    private SyncConfig syncConfig;
    private String remoteUri;
    protected ConnectionManager connectionManager = ConnectionManager.getInstance();

    /**
     * This subclass represents the result of the upload operation.
     */
    public class HttpUploadStatus {
        private int status = SyncMLStatus.GENERIC_ERROR;
        private int uploadedBytes = 0;
        private boolean firstAttempt = true;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getUploadedBytes() {
            return uploadedBytes;
        }

        public void setUploadedBytes(int uploadedBytes) {
            this.uploadedBytes = uploadedBytes;
        }

        public boolean getFirstAttempt() {
            return firstAttempt;
        }

        public void setFirstAttempt(boolean firstAttempt) {
            this.firstAttempt = firstAttempt;
        }

    }

    public HttpUploader(SyncConfig syncConfig, String uploadUrl, String remoteUri, SyncListener listener) {
        this.syncConfig = syncConfig;
        this.lis        = listener;
        this.uploadUrl  = uploadUrl;
        this.remoteUri  = remoteUri;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }


    /**
     * Upload an item to the HTTP server. The item is identified by a key and
     * its content is made available via the given stream. Note that the stream
     * is not closed at the end of the transfer, so it is up to the caller to
     * close it properly.
     *
     * @param key the item identifier (the LUID used during the sync)
     * @param fileStream the item stream
     * @param size the item size
     * @param contentType is the content type of the item to upload (if a null
     * value is given, a default application/octect-stream type is used)
     * @param previousStatus this is the status of the previous upload for this same
     * item (it is null on the first attempt and has a value for successive
     * ones)
     *
     */
    public void upload(String key, InputStream fileStream, int size, String contentType,
                       HttpUploadStatus status)
    throws HttpUploaderException {

        String url = createUploadUrl(jsessionId, key);

        Log.debug(TAG_LOG, "Uploading to url: " + url.toString());

        HttpConnectionAdapter conn;
        
        try {
            // Open the connection with a given size to prevent the output
            // stream from buffering all data
            conn = connectionManager.openHttpConnection(url.toString(), null);
            conn.setRequestMethod(HttpConnectionAdapter.POST);
            conn.setChunkedStreamingMode(4096);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            Log.debug(TAG_LOG, "Setting content type to: " + contentType);
            conn.setRequestProperty(CONTENT_TYPE_HEADER, contentType);

            // Set the authentication if we have no jsessionid
            if (jsessionId == null) {
                String user = syncConfig.getUserName();
                String pwd  = syncConfig.getPassword();
                String token = user + ":" + pwd;
                String authToken = new String(Base64.encode(token.getBytes()));

                String authParam = AUTH_BASIC + " " + authToken;
                Log.debug(TAG_LOG, "Setting auth header to: " + authParam);
                conn.setRequestProperty(AUTH_HEADER, authParam);
            } else {
                Log.debug(TAG_LOG, "Authorization is specified via jsessionid");
            }
            DeviceConfig devConf = syncConfig.getDeviceConfig();
            Log.debug(TAG_LOG, "Setting device id header to: " + devConf.devID);
            conn.setRequestProperty(X_DEVID_HEADER, devConf.devID);

            Log.debug(TAG_LOG, "Setting luid to: " + key);
            conn.setRequestProperty(X_LUID_HEADER, key);
        } catch (IOException ioe) {
            Log.error(TAG_LOG, "Cannot open http connection", ioe);
            HttpUploaderException fue = new HttpUploaderException();
            fue.setIOException(ioe);
            throw fue;
        }

        // The content length header is appended below once the file is
        // available

        OutputStream os = null;

        try {
            Log.info(TAG_LOG, "Uploading file: " + key);

            Log.debug(TAG_LOG, "Setting file size: " + size);
            conn.setRequestProperty(X_FILE_SIZE_HEADER, "" + size);

            Log.debug(TAG_LOG, "Setting content length: " + size);
            conn.setRequestProperty(CONTENT_LENGTH_HEADER, "" + size);

            os = conn.openOutputStream();

            Log.trace(TAG_LOG, "Output stream opened");

            byte chunk[] = new byte[chunkSize];

            if (status.getFirstAttempt()) {
                if (lis != null && !uploadCancelled()) {
                    lis.itemAddSendingStarted(key, null, size);
                }
                status.setFirstAttempt(false);
            }

            int total = 0;
            do {
                size = fileStream.read(chunk);
                if (size > 0) {
                    Log.trace(TAG_LOG, "Writing chunk of data: " + size);
                    os.write(chunk, 0, size);
                    total += size;
                    if (lis != null) {
                        if (total > status.getUploadedBytes() && !uploadCancelled()) {
                            lis.itemAddChunkSent(key, null, size);
                        }
                    }
                    status.setUploadedBytes(total);
                }
                if (uploadCancelled()) {
                    HttpUploaderException cancelExp = new HttpUploaderException();
                    cancelExp.setCancelled(true);
                    throw cancelExp;
                }
            } while(size > 0);

            if (lis != null && !uploadCancelled()) {
                lis.itemAddSendingEnded(key, null, size);
            }

            Log.trace(TAG_LOG, "Content written");
            os.close();
            os = null;

            Log.trace(TAG_LOG, "Response is: " + conn.getResponseCode());

            // Now check the HTTP response, in case of success we set the item
            // status to OK
            if (conn.getResponseCode() == HttpConnectionAdapter.HTTP_OK) {
                status.setStatus(SyncMLStatus.SUCCESS);
            }

            // This code handles JSESSION ID authentication but it is currently
            // disabled because we don't hanlde auth errors at the moment
            /*
            String cookies = conn.getHeaderField(COOKIE_HEADER);
            if (cookies != null) {
                int jsidx = cookies.indexOf(JSESSIONID_HEADER);
                if (jsidx >= 0) {
                    jsessionId = cookies.substring(jsidx);
                    int idx = jsessionId.indexOf(";");
                    if (idx >= 0) {
                        jsessionId = jsessionId.substring(0, idx);
                    }
                }
            }
            */
        } catch (IOException ioe) {
            // If we get a non authorized error and we used a jsession id, then
            // we invalidate the jsessionId and throw the exception
            Log.error(TAG_LOG, "Error while uploading", ioe);
            HttpUploaderException e = new HttpUploaderException();

            if (conn != null) {
                try {
                    if (jsessionId != null && conn.getResponseCode() == HttpConnectionAdapter.HTTP_FORBIDDEN) {
                        Log.info(TAG_LOG, "Invalidating jsession id");
                        jsessionId = null;
                    }
                    e.setHttpErrorCode(conn.getResponseCode());
                } catch (IOException ioe2) {}
            }
            e.setIOException(ioe);
            throw e;
        } finally {
            // Release all resources
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {}
                os = null;
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (IOException ioe) {}
                conn = null;
            }
        }
    }

    protected String createUploadUrl(String sessionId, String key) {
        // Prepare the URL
        String syncUrl = syncConfig.getSyncUrl();
        StringBuffer url = new StringBuffer(StringUtil.extractAddressFromUrl(syncUrl));
        url.append("/").append(uploadUrl).append("/").append(remoteUri);
        // Append the LUID
        url.append("?").append(ACTION_PARAM).append("=").append(ACTION_PARAM_VALUE);
        // Credentials in the query string
        if (authInQueryString) {
            String user = syncConfig.getUserName();
            String pwd  = syncConfig.getPassword();
            url.append("&login=").append(user).append("&password=").append(pwd);
        }

        // If we have a jsessionid we can specify it in the URL to speed up the
        // server work. But before enabling this piece of code we need to handle
        // authentication failures due to expired sessions
        //if (sessionId != null) {
        //    url.append(";").append(sessionId);
        //}
        return url.toString();
    }

    protected boolean uploadCancelled() {
        return false;
    }

}


