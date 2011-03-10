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

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import com.funambol.platform.FileAdapter;
import com.funambol.platform.HttpConnectionAdapter;
import com.funambol.storage.StringKeyValueStore;
import com.funambol.storage.StringKeyValueFileStore;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncConfig;
import com.funambol.syncml.spds.DeviceConfig;
import com.funambol.syncml.spds.SyncListener;
import com.funambol.syncml.spds.SyncException;
import com.funambol.syncml.protocol.SyncMLStatus;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.util.ConsoleAppender;
import com.funambol.util.ConnectionManager;
import com.funambol.util.Log;

import junit.framework.*;

public class TwoPhasesFileSyncSourceTest extends TestCase {

    public TwoPhasesFileSyncSourceTest(String name) {
        super(name);

        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.TRACE);
    }

    public void setUp() {
    }

    public void testOneUpload() throws Throwable {

        SourceConfig sourceConfig = new SourceConfig(SourceConfig.BRIEFCASE,SourceConfig.FILE_OBJECT_TYPE , "briefcase");
        TestTracker tracker = new TestTracker();
        SyncConfig config = new SyncConfig();
        config.setSyncUrl("http://my.funambol.com:9090/sync");
        config.setUserName("test");
        config.setPassword("test");
        TestTwoPhasesFileSyncSource ss = new TestTwoPhasesFileSyncSource(sourceConfig, tracker,
                                                                         ".", config, "sapi/media");
        ss.beginSync(SyncML.ALERT_CODE_FAST);
        // Simulate a successfull status from the server so that this item will
        // be uploaded in the endSync
        ss.setItemStatus("Pic0.jpg", SyncMLStatus.SUCCESS);
        ss.endSync();

        TestHttpUploader uploader = ss.getHttpUploader();
        TestHttpConnectionAdapter conn = uploader.getHttpConnectionAdapter();
        // Now check if the uploader performed the proper connection/upload
        assertTrue("POST".equals(conn.getRequestedMethod()));
        assertTrue("http://my.funambol.com:9090/sapi/media/briefcase?action=content-upload".equals(conn.getUrl()));
        assertTrue("Content".equals(conn.getWrittenMessage()));
    }

    public void testNoUpload() throws Throwable {

        SourceConfig sourceConfig = new SourceConfig(SourceConfig.BRIEFCASE,SourceConfig.FILE_OBJECT_TYPE , "briefcase");
        TestTracker tracker = new TestTracker();
        SyncConfig config = new SyncConfig();
        config.setSyncUrl("http://my.funambol.com:9090/sync");
        config.setUserName("test");
        config.setPassword("test");
        TestTwoPhasesFileSyncSource ss = new TestTwoPhasesFileSyncSource(sourceConfig, tracker,
                                                                         ".", config, "upload");
        ss.beginSync(SyncML.ALERT_CODE_FAST);
        // Simulate a failed status from the server so that this item will NOT
        // be uploaded in the endSync
        ss.setItemStatus("Pic0.jpg", SyncMLStatus.GENERIC_ERROR);
        ss.endSync();

        TestHttpUploader uploader = ss.getHttpUploader();
        assertTrue(uploader == null);
    }

    public void testTwoUploads() throws Throwable {

        SourceConfig sourceConfig = new SourceConfig(SourceConfig.BRIEFCASE,SourceConfig.FILE_OBJECT_TYPE , "briefcase");
        TestTracker tracker = new TestTracker();
        SyncConfig config = new SyncConfig();
        config.setSyncUrl("http://my.funambol.com:9090/sync");
        config.setUserName("test");
        config.setPassword("test");
        TestTwoPhasesFileSyncSource ss = new TestTwoPhasesFileSyncSource(sourceConfig, tracker,
                                                                         ".", config, "sapi/media");
        ss.beginSync(SyncML.ALERT_CODE_FAST);
        // Simulate a successfull status from the server so that this item will
        // be uploaded in the endSync
        ss.setItemStatus("Pic0.jpg", SyncMLStatus.SUCCESS);
        ss.setItemStatus("Pic1.jpg", SyncMLStatus.SUCCESS);
        ss.endSync();

        TestHttpUploader uploader = ss.getHttpUploader();

        TestConnectionManager connectionManager = uploader.getConnectionManager();
        Vector allConnections = connectionManager.getAllConnections();

        assertTrue(allConnections.size() == 2);
        TestHttpConnectionAdapter conn = (TestHttpConnectionAdapter)allConnections.elementAt(0);
        // Now check if the uploader performed the proper connection/upload
        assertTrue("POST".equals(conn.getRequestedMethod()));
        assertTrue("http://my.funambol.com:9090/sapi/media/briefcase?action=content-upload".equals(conn.getUrl()));
        assertTrue("Content".equals(conn.getWrittenMessage()));

        conn = (TestHttpConnectionAdapter)allConnections.elementAt(1);
        // Now check if the uploader performed the proper connection/upload
        assertTrue("POST".equals(conn.getRequestedMethod()));
        assertTrue("http://my.funambol.com:9090/sapi/media/briefcase?action=content-upload".equals(conn.getUrl()));
        assertTrue("Content".equals(conn.getWrittenMessage()));
    }

    private class TestTwoPhasesFileSyncSource extends TwoPhasesFileSyncSource {

        private TestHttpUploader uploader;

        public TestTwoPhasesFileSyncSource(SourceConfig config, ChangesTracker tracker,
                                           String directory, SyncConfig syncConfig,
                                           String uploadUrl)
        {
            super(config, tracker, directory, syncConfig, uploadUrl);
        }

        protected FileProperties openFile(String fileName) {
            FileProperties res = new FileProperties();
            String content = "Content";
            byte bytes[] = content.getBytes();
            res.stream = new ByteArrayInputStream(bytes);
            res.size   = bytes.length;
            return res;
        }

        protected HttpUploader createUploader(SyncConfig config, String uploadUrl,
                                              String sourceUri, SyncListener listener)
        {
            uploader = new TestHttpUploader(config, uploadUrl, sourceUri, listener);
            TestConnectionManager testConnectionManager = new TestConnectionManager();
            uploader.setConnectionManager(testConnectionManager);
            return uploader;
        }

        public TestHttpUploader getHttpUploader() {
            return uploader;
        }
    }

    private class TestHttpUploader extends HttpUploader {

        private int cancelCounter = 0;
        private int cancelAfter   = -1;

        public TestHttpUploader(SyncConfig syncConfig, String uploadUrl, String remoteUri, SyncListener listener) {
            super(syncConfig, uploadUrl, remoteUri, listener);
        }

        public void setCancelAfter(int count) {
            cancelAfter = count;
        }

        public String getUrl(String sessionId, String key) {
            return createUploadUrl(sessionId, key);
        }

        public TestHttpConnectionAdapter getHttpConnectionAdapter() {
            return ((TestConnectionManager)connectionManager).getHttpConnectionAdapter();
        }

        public TestConnectionManager getConnectionManager() {
            return (TestConnectionManager)connectionManager;
        }

        protected boolean uploadCancelled() {
            if (cancelCounter == cancelAfter) {
                return true;
            } else {
                cancelCounter++;
                return false;
            }
        }
    }

    private class TestConnectionManager extends ConnectionManager {

        private TestHttpConnectionAdapter testHttpConnectionAdapter;
        private Vector allConnections = new Vector();

        protected TestConnectionManager() {
            super();
        }

        public HttpConnectionAdapter openHttpConnection(String url, Object extra) {
            testHttpConnectionAdapter = new TestHttpConnectionAdapter(url);
            allConnections.addElement(testHttpConnectionAdapter);
            return testHttpConnectionAdapter;
        }

        public TestHttpConnectionAdapter getHttpConnectionAdapter() {
            return testHttpConnectionAdapter;
        }

        public Vector getAllConnections() {
            return allConnections;
        }
    }

    private class TestHttpConnectionAdapter extends HttpConnectionAdapter {
        private String response;
        private OutputStream os;
        private int responseCode;
        private String requestedMethod;
        private Hashtable props = new Hashtable();
        private String url;

        public TestHttpConnectionAdapter(String url) {
            super();
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }

        public void addResponseHeader(String key, String value) {
        }

        public Hashtable getRequestProperties() {
            return props;
        }

        public String getWrittenMessage() {
            if (os != null) {
                return os.toString();
            } else {
                return null;
            }
        }

        public String getRequestedMethod() {
            return requestedMethod;
        }

        public InputStream openInputStream() throws IOException {

            if (response != null) {
                return new ByteArrayInputStream(response.getBytes());
            } else {
                throw new IOException("Cannot read response");
            }
        }

        public OutputStream openOutputStream() throws IOException {
            os = new ByteArrayOutputStream();
            return os;
        }

        public int getResponseCode() throws IOException {
            return responseCode;
        }

        public void setRequestMethod(String method) throws IOException {
            requestedMethod = method;
        }

        public void setRequestProperty(String key, String value) throws IOException {
            props.put(key, value);
        }

        public String getHeaderField(String key) throws IOException {
            return null;
        }

        public void setChunkedStreamingMode(int size) throws IOException {
        }

        public void close() throws IOException {
        }
    }

    private class TestTracker implements ChangesTracker {

        private Vector newItems = new Vector();
        private Vector delItems = new Vector();
        private Vector updItems = new Vector();
        private Vector allItems = new Vector();

        public TestTracker() {
        }

        public void setSyncSource(TrackableSyncSource ss) {
        }

        public void begin(int syncMode) throws TrackerException {
        }

        public void end() throws TrackerException {
        }

        public Enumeration getNewItems() throws TrackerException {
            return newItems.elements();
        }

        public int getNewItemsCount() throws TrackerException {
            return newItems.size();
        }

        public Enumeration getUpdatedItems() throws TrackerException {
            return updItems.elements();
        }

        public int getUpdatedItemsCount() throws TrackerException {
            return updItems.size();
        }

        public Enumeration getDeletedItems() throws TrackerException {
            return delItems.elements();
        }

        public int getDeletedItemsCount() throws TrackerException {
            return delItems.size();
        }

        public void setItemStatus(String key, int status) throws TrackerException  {
        }

        public void reset() throws TrackerException {
        }

        public boolean removeItem(SyncItem item) throws TrackerException {
            return false;
        }

        public void addNewItem(SyncItem item) {
            newItems.addElement(item);
        }

        public void addUpdItem(SyncItem item) {
            updItems.addElement(item);
        }

        public void addDelItem(SyncItem item) {
            delItems.addElement(item);
        }

        public void addItem(SyncItem item) {
            allItems.addElement(item);
        }

        public void empty() throws TrackerException {

        }
    }

}

