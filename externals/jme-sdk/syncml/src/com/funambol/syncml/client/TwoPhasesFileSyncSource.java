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

import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Date;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.syncml.spds.SyncException;
import com.funambol.syncml.spds.SyncListener;
import com.funambol.syncml.spds.SyncReport;
import com.funambol.syncml.spds.SyncConfig;

import com.funambol.syncml.protocol.SyncFilter;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.protocol.SyncMLStatus;
import com.funambol.syncml.protocol.DevInf;

import com.funambol.platform.FileAdapter;
import com.funambol.platform.HttpConnectionAdapter;

import com.funambol.util.ConnectionManager;
import com.funambol.util.Base64;
import com.funambol.util.DateUtil;
import com.funambol.util.StringUtil;
import com.funambol.util.XmlUtil;
import com.funambol.util.Log;


/**
 * An implementation of TrackableSyncSource, providing
 * the ability to sync briefcases (files). The source can handle both raw files
 * and OMA files (file objects). By default the source formats items according
 * to the OMA file object spec, but it is capable of receiving also raw files,
 * if their MIME type is not OMA file objects.
 */
public class TwoPhasesFileSyncSource extends FileSyncSource {

    private static final String TAG_LOG = "TwoPhasesFileSyncSource";

    private static final int DEFAULT_NUM_RETRIES = 3;

    protected int numRetries = DEFAULT_NUM_RETRIES;

    protected String directory;
    protected Vector itemsToUpload;
    protected String uploadUrl;
    protected ProxySyncListener proxyListener;
    protected SyncConfig syncConfig;
    protected Hashtable itemsToDelete;
    
    //------------------------------------------------------------- Constructors

    /**
     * FileSyncSource constructor: initialize source config.
     * @param config the source configuration
     * @param tracker the changes tracker
     * @param directory the directory containing the files
     * @param syncConfig the SyncConfig. Note that this must be updated with the
     * current credentials of the user. If this is not the case, then the method
     * createUploader shall be redefined so that the HttpUploader is created
     * with an updated SyncConfig
     * @param uploadUrl the upload url suffix (this is added to the sync url)
     */
    public TwoPhasesFileSyncSource(SourceConfig config, ChangesTracker tracker, String directory,
                                   SyncConfig syncConfig, String uploadUrl)
    {
        super(config, tracker, directory);
        this.syncConfig = syncConfig;
        this.uploadUrl  = uploadUrl;
    }

    public void beginSync(int syncMode) throws SyncException {
        super.beginSync(syncMode);
        itemsToUpload = new Vector();
        itemsToDelete = new Hashtable();
        // Replace the listener
        if (getListener() != null) {
            proxyListener = new ProxySyncListener(getListener());
            setListener(proxyListener);
        }
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
    public int addItem(SyncItem item) throws SyncException {
        Log.error(TAG_LOG, "addItem not implemented");
        throw new SyncException(SyncException.CLIENT_ERROR, "addItem not implemented");
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
    public int updateItem(SyncItem item) throws SyncException {

        Log.error(TAG_LOG, "updateItem not implemented");
        throw new SyncException(SyncException.CLIENT_ERROR, "updateItem not implemented");
    }

    public void setItemStatus(String key, int status) throws SyncException {
        Log.info(TAG_LOG, "setItemStatus for " + key + " status " + status);
        boolean deleted = itemsToDelete.get(key) != null;
        if (deleted) {
            Log.trace(TAG_LOG, "item was deleted");
            super.setItemStatus(key, status);
        } else {
            if (SyncMLStatus.isSuccess(status)) {
                // We can upload the item in the 2nd phase
                itemsToUpload.addElement(key);
            } else {
                Log.error(TAG_LOG, "Server refused item: " + key); 
            }
        }
    }

    public SyncItem getNextDeletedItem() throws SyncException {
        SyncItem nextDelete = super.getNextDeletedItem();
        if (nextDelete != null) {
            Log.trace(TAG_LOG, "next item to delete: " + nextDelete.getKey());
            itemsToDelete.put(nextDelete.getKey(), nextDelete);
        }
        return nextDelete;
    }



    public void endSync() throws SyncException {
        uploadItems();
        super.endSync();
    }

    protected void uploadItems() throws SyncException {
        // This is the beginning of the 2nd phase. Start to upload/download
        // items
        if (itemsToUpload.size() > 0) {
            Log.info(TAG_LOG, "Beginning upload/download phase");

            if (proxyListener != null) {
                proxyListener.beginSecondPhase();
            }

            HttpUploader uploader = createUploader(syncConfig, uploadUrl, getSourceUri(), proxyListener);

            for(int i=0;i<itemsToUpload.size();++i) {
                String fileName = (String)itemsToUpload.elementAt(i);
                boolean uploaded = false;
                IOException lastIoe = null;
                int lastHttpError = -1;

                HttpUploader.HttpUploadStatus uploadRes = uploader.new HttpUploadStatus();
                for(int r=0;r<numRetries;++r) {

                    FileProperties fProp = null;
                    try {
                        fProp = openFile(fileName);
                    } catch (IOException ioe) {
                        Log.error(TAG_LOG, "Cannot upload file: " + fileName, ioe);
                    }

                    if (fProp != null) {
                        try {
                            // TODO: we should allow the client to customize the
                            // content type
                            uploader.upload(fileName, fProp.stream, fProp.size, "application/octet-stream", uploadRes);
                            super.setItemStatus(fileName, uploadRes.getStatus());
                            // If we get here, the item was transferred successfully
                            uploaded = true;
                            break;
                        } catch (HttpUploaderException fue) {
                            if (fue.cancelled()) {
                                throw new SyncException(SyncException.CANCELLED, "Upload cancelled by the user");
                            }
                            lastIoe = fue.getIOException();
                            if (lastIoe != null) {
                                Log.error(TAG_LOG, "Network error uploading file", lastIoe);
                            } else {
                                Log.error(TAG_LOG, "Network error uploading file");
                            }
                            if (fue.isHttpError()) {
                                lastHttpError = fue.getHttpErrorCode();
                            }
                        } catch (Throwable e) {
                            Log.error(TAG_LOG, "Internal error uploading file", e);
                            throw new SyncException(SyncException.CLIENT_ERROR, e.toString());
                        } finally {
                            // Close everthing related to the input stream.
                            // Note that the closing sequence must follow a
                            // particular order to work properly on BlackBerry.
                            // See here for more details:
                            // http://www.blackberry.com/knowledgecenterpublic/livelink.exe/fetch/2000/348583/800451/800563/How_To_-_Close_connections.html?nodeid=1261294&vernum=0
                            closeFileProperties(fProp);
                        }
                    }
                }

                if (!uploaded) {
                    if (lastHttpError != -1) {
                        // Generate a proper sync exception, depending on the last
                        // error
                        SyncException syncException;
                        if (lastHttpError == HttpConnectionAdapter.HTTP_FORBIDDEN) {
                            syncException = new SyncException(SyncException.FORBIDDEN_ERROR, "User not authorized");
                        } else {
                            syncException = new SyncException(SyncException.WRITE_SERVER_REQUEST_ERROR, "Error uploading item");
                        }
                        throw syncException;
                    } else {
                        // If we could not open the connection at all, this is a
                        // network error
                        throw new SyncException(SyncException.CONN_NOT_FOUND, "Cannot establish connection to server");
                    }

                    // If this is not the last attempt, we may wait a bit
                }
            }
        }
        Log.info(TAG_LOG, "Upload/download phase completed");
    }

    /**
     * This method opens a file and store its input stream and size into a
     * FileProperties object.
     *
     * @param fileName the file name
     * @return a descriptor of the file (stream to read content and file size)
     */
    protected FileProperties openFile(String fileName) throws IOException {
        FileAdapter fa = new FileAdapter(fileName, true);
        FileProperties res = new FileProperties();
        res.adapter = fa;
        // If we were able to open the connection but not the stream, we shall
        // make sure the connection gets closed before returning
        try {
            res.stream = fa.openInputStream();
            res.size   = (int)fa.getSize();
            return res;
        } catch (IOException ioe) {
            closeFileProperties(res);
            throw ioe;
        }
    }

    protected void closeFileProperties(FileProperties fProp) {
        if (fProp != null) {
            if (fProp.stream != null) {
                try {
                    fProp.stream.close();
                } catch (IOException e) {}
                fProp.stream = null;
            }
            if (fProp.adapter != null) {
                try {
                    fProp.adapter.close();
                } catch (IOException e) {}
                fProp.adapter = null;
            }
        }
    }

    protected HttpUploader createUploader(SyncConfig config, String uploadUrl,
                                          String sourceUri, SyncListener listener)
    {
        HttpUploader uploader = new HttpUploader(config, uploadUrl, sourceUri, listener);
        return uploader;
    }

    protected class FileProperties {

        public InputStream stream  = null;
        public int size            = 0;
        public FileAdapter adapter = null;

        public FileProperties() {
        }
    }

    protected class ProxySyncListener implements SyncListener {

        private SyncListener lis;
        private boolean secondPhase = false;

        public ProxySyncListener(SyncListener lis) {
            this.lis = lis;
        }

        public void beginSecondPhase() {
            secondPhase = true;
        }

        public void startSession() {
            lis.startSession();
        }

        public void endSession(SyncReport report) {
            // Restore the original listener because we are done
            TwoPhasesFileSyncSource.this.setListener(lis);
            // Propagate the event
            lis.endSession(report);
        }

        public void startConnecting() {
            lis.startConnecting();
        }

        public void endConnecting(int action) {
            lis.endConnecting(action);
        }

        public void syncStarted(int alertCode) {
            lis.syncStarted(alertCode);
        }

        public void endSyncing() {
            //lis.endSyncing();
        }

        public void startMapping() {
            //lis.startMapping();
        }

        public void endMapping() {
            //lis.endMapping();
        }

        public void startReceiving(int number) {
            lis.startReceiving(number);
        }

        public void endReceiving() {
            lis.endReceiving();
        }

        public void itemReceived(Object item) {
            lis.itemReceived(item);
        }

        public void itemDeleted(Object item) {
            lis.itemDeleted(item);
        }

        public void itemUpdated(Object item, Object update) {
            lis.itemUpdated(item, update);
        }

        public void itemUpdated(Object item) {
            lis.itemUpdated(item);
        }

        public void dataReceived(String date, int size) {
            lis.dataReceived(date, size);
        }

        public void startSending(int numNewItems, int numUpdItems, int numDelItems) {
            lis.startSending(numNewItems, numUpdItems, numDelItems);
        }

        public void itemAddSendingStarted(String key, String parent, int size) {
            // During the SyncML phase, we filter it out
            if (secondPhase) {
                lis.itemAddSendingStarted(key, parent, size);
            }
        }

        public void itemAddSendingEnded(String key, String parent, int size) {
            if (secondPhase) {
                lis.itemAddSendingEnded(key, parent, size);
            }
        }

        public void itemAddChunkSent(String key, String parent, int size) {
            if (secondPhase) {
                lis.itemAddChunkSent(key, parent, size);
            }
        }

        public void itemReplaceSendingStarted(String key, String parent, int size) {
            if (secondPhase) {
                lis.itemReplaceSendingStarted(key, parent, size);
            }
        }

        public void itemReplaceSendingEnded(String key, String parent, int size) {
            if (secondPhase) {
                lis.itemReplaceSendingEnded(key, parent, size);
            }
        }

        public void itemReplaceChunkSent(String key, String parent, int size) {
            if (secondPhase) {
                lis.itemReplaceChunkSent(key, parent, size);
            }
        }

        public void itemDeleteSent(Object item) {
            lis.itemDeleteSent(item);
        }

        public void endSending() {
            lis.endSending();
        }

        public boolean startSyncing(int alertCode, DevInf devInf) {
            return lis.startSyncing(alertCode, devInf);
        }
    }


    /**
     * TODO: is this still needed?
     * This is still kind of strange, we don't really need to get the item
     * content any longer but we just need to create a proper item from which
     * the content can be read
     */
    protected SyncItem getItemContent(final SyncItem item) throws SyncException {
        Log.trace(TAG_LOG, "getItemContent for: " + item.getKey());
        // We send the item with the type of the SS
        SourceConfig config = getConfig();
        String type = config.getType();
        String fileName = item.getKey();
        try {
            EmptyFileSyncItem fsi = new EmptyFileSyncItem(fileName, item.getKey(), type, item.getState(),
                                                          item.getParent());
            return fsi;
        } catch (IOException ioe) {
            throw new SyncException(SyncException.CLIENT_ERROR,
                                    "Cannot create EmptyFileSyncItem: " + ioe.toString());
        }
    }

    protected class EmptyFileSyncItem extends SyncItem {
        private String fileName;
        private OutputStream os = null;
        private String content;

        public EmptyFileSyncItem(String fileName, String key) throws IOException {
            this(fileName, key, null, SyncItem.STATE_NEW, null);
        }

        public EmptyFileSyncItem(String fileName, String key, String type, char state,
                                 String parent) throws IOException {

            super(key, type, state, parent);
            this.fileName = fileName;
            FileAdapter file = new FileAdapter(fileName);

            // Initialize the prologue
            FileObject fo = new FileObject();
            fo.setName(file.getName());
            fo.setModified(new Date(file.lastModified()));
            fo.setSize((int)file.getSize());
            // Print all the item without body
            content = fo.formatPrologue(false);
            content = content + fo.formatEpilogue(false);
            // Set the size
            setObjectSize(content.length());

            // Release the file object
            file.close();
        }

        /**
         * Creates a new input stream to read from. If the source is configured
         * to handle File Data Object, then the stream returns the XML
         * description of the file. @see FileObjectInputStream for more details.
         */
        public InputStream getInputStream() throws IOException {
            ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes());
            return is;
        }

        public String getFileName() {
            return fileName;
        }

        // If we do not reimplement the getContent, it will return a null
        // content, but this is not used in the ss, so there's no need to
        // redefine it
    }



}

