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
import com.funambol.util.ConsoleAppender;
import com.funambol.util.ConnectionManager;
import com.funambol.util.Log;

import junit.framework.*;

public class HttpUploaderTest extends TestCase {

    public HttpUploaderTest(String name) {
        super(name);

        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.TRACE);
    }

    public void setUp() {
    }

    public void testCreateUploadUrl1() throws Throwable {
        SyncConfig config = new SyncConfig();
        config.setSyncUrl("http://my.funambol.com:9090/sync");
        config.setUserName("test");
        config.setPassword("test");
        TestHttpUploader uploader = new TestHttpUploader(config, "sapi/media", "picture", null);
        String url = uploader.getUrl(null, "Picture0");
        assertTrue("http://my.funambol.com:9090/sapi/media/picture?action=content-upload".equals(url));
    }

    public void testUpload1() throws Throwable {
        SyncConfig config = new SyncConfig();
        config.setSyncUrl("http://my.funambol.com:9090/sync");
        config.setUserName("test");
        config.setPassword("test");

        DeviceConfig dc = new DeviceConfig();
        dc.devID = "test-device-id";
        config.deviceConfig = dc;

        TestHttpUploader uploader = new TestHttpUploader(config, "sapi/media", "picture", null);
        TestConnectionManager testConnectionManager = new TestConnectionManager();
        uploader.setConnectionManager(testConnectionManager);

        String content = "File content";
        byte contentArr[] = content.getBytes();
        ByteArrayInputStream is = new ByteArrayInputStream(contentArr);

        TestHttpConnectionAdapter conn = uploader.getHttpConnectionAdapter();
        conn.setResponseCode(200);

        HttpUploader.HttpUploadStatus status = uploader.new HttpUploadStatus();
        uploader.upload("Pic0.jpg", is, contentArr.length, null, status);

        assertTrue("POST".equals(conn.getRequestedMethod()));

        Hashtable headers = conn.getRequestProperties();

        String auth = (String)headers.get("Authorization");    
        assertTrue("Basic dGVzdDp0ZXN0".equals(auth));
        
        String len  = (String)headers.get("Content-Length");
        int l = Integer.parseInt(len);
        assertTrue(l == contentArr.length);
        
        String filesize  = (String)headers.get("x-funambol-file-size");
        l = Integer.parseInt(filesize);
        assertTrue(l == contentArr.length);
        
        String devId = (String)headers.get("x-funambol-syncdeviceid");
        assertTrue("test-device-id".equals(devId));
        
        String luid = (String)headers.get("x-funambol-luid");
        assertTrue("Pic0.jpg".equals(luid));

        String msg = conn.getWrittenMessage();
        assertTrue("File content".equals(msg));
        
        // Since the response is positive, we expect a successfull status
        assertTrue(status.getStatus() == SyncMLStatus.SUCCESS);
    }

    public void testUpload2() throws Throwable {
        SyncConfig config = new SyncConfig();
        config.setSyncUrl("http://my.funambol.com:9090/sync");
        config.setUserName("test");
        config.setPassword("test");

        DeviceConfig dc = new DeviceConfig();
        dc.devID = "test-device-id";
        config.deviceConfig = dc;

        TestHttpUploader uploader = new TestHttpUploader(config, "sapi/media", "picture", null);
        TestConnectionManager testConnectionManager = new TestConnectionManager();
        uploader.setConnectionManager(testConnectionManager);
        uploader.setChunkSize(4);
        uploader.setCancelAfter(1);

        String content = "File content";
        byte contentArr[] = content.getBytes();
        ByteArrayInputStream is = new ByteArrayInputStream(contentArr);

        boolean cancelled = false;
        try {
            HttpUploader.HttpUploadStatus status = uploader.new HttpUploadStatus();
            uploader.upload("Pic0.jpg", is, contentArr.length, null, status);
        } catch (HttpUploaderException fue) {
            if (fue.cancelled()) {
                cancelled = true;
            }
        }
        assertTrue(cancelled);
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

        private TestHttpConnectionAdapter testHttpConnectionAdapter = new TestHttpConnectionAdapter();

        protected TestConnectionManager() {
            super();
        }

        public HttpConnectionAdapter openHttpConnection(String url, Object extra) {
            return testHttpConnectionAdapter;
        }

        public TestHttpConnectionAdapter getHttpConnectionAdapter() {
            return testHttpConnectionAdapter;
        }
    }

    private class TestHttpConnectionAdapter extends HttpConnectionAdapter {
        private String response;
        private OutputStream os;
        private int responseCode;
        private String requestedMethod;
        private Hashtable props = new Hashtable();

        public TestHttpConnectionAdapter() {
            super();
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
}

