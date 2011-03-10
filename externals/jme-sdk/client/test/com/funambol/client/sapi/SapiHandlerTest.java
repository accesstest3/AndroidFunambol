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

package com.funambol.client.sapi;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Vector;
import java.util.Hashtable;

import org.json.me.JSONObject;

import com.funambol.platform.FileAdapter;
import com.funambol.platform.HttpConnectionAdapter;
import com.funambol.storage.StringKeyValueStore;
import com.funambol.storage.StringKeyValueFileStore;
import com.funambol.util.ConsoleAppender;
import com.funambol.util.ConnectionManager;
import com.funambol.util.Log;

import junit.framework.*;

public class SapiHandlerTest extends TestCase {

    private static final String TAG_LOG = "SapiHandlerTest";

    public SapiHandlerTest(String name) {
        super(name);

        System.out.println("SapiHandlerTest!!!!!");

        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.TRACE);
    }

    public void setUp() {
    }

    public void testAuthentication1() throws Throwable {
        TestSapiHandler handler = new TestSapiHandler("http://my.funambol.com:9090", "test", "test");
        handler.setAuthenticationMethod(SapiHandler.AUTH_IN_QUERY_STRING);
        String url = handler.getUrl("mobile", "signup", null);
        Log.trace(TAG_LOG, "url=" + url);
        assertTrue("http://my.funambol.com:9090/sapi/mobile?action=signup&login=test&password=test".equals(url));
    }

    public void testAuthentication2() throws Throwable {
        TestSapiHandler handler = new TestSapiHandler("http://my.funambol.com:9090", "test", "test");
        handler.setAuthenticationMethod(SapiHandler.AUTH_IN_HTTP_HEADER);
        String url = handler.getUrl("mobile", "signup", null);
        Log.trace(TAG_LOG, "url=" + url);
        assertTrue("http://my.funambol.com:9090/sapi/mobile?action=signup".equals(url));
        ConnectionManagerMock testConnectionManager = new ConnectionManagerMock();
        handler.setConnectionManager(testConnectionManager);
        HttpConnectionAdapterMock adapter = testConnectionManager.getHttpConnectionAdapter();
        adapter.setResponseCode(200);
        adapter.setResponse("{\"data\":{ \"user\":{ \"active\":true } } }");

        JSONObject res = handler.query("mobile", "signup", null, null, (JSONObject)null);
        Hashtable headers = adapter.getRequestProperties();
        // We expect username and pwd in the headers
        String authToken = (String)headers.get("Authorization");
        assertTrue("Basic dGVzdDp0ZXN0".equals(authToken));
    }

    public void testAuthentication3() throws Throwable {
        TestSapiHandler handler = new TestSapiHandler("http://my.funambol.com:9090", "test &$", "test &$");
        handler.setAuthenticationMethod(SapiHandler.AUTH_IN_QUERY_STRING);
        String url = handler.getUrl("mobile", "signup", null);
        Log.trace(TAG_LOG, "url=" + url);
        assertTrue("http://my.funambol.com:9090/sapi/mobile?action=signup&login=test+%26%24&password=test+%26%24".equals(url));
    }

    public void testSapi1() throws Throwable {
        Log.trace(TAG_LOG, "testSapi1");
        TestSapiHandler sapiHandler = new TestSapiHandler("http://my.funambol.com:9090", "test", "test");
        ConnectionManagerMock testConnectionManager = new ConnectionManagerMock();
        sapiHandler.setConnectionManager(testConnectionManager);

        String content = "File content";
        byte contentArr[] = content.getBytes();
        ByteArrayInputStream is = new ByteArrayInputStream(contentArr);

        HttpConnectionAdapterMock conn = sapiHandler.getHttpConnectionAdapterMock();
        conn.setResponseCode(200);
        conn.setResponse("{\"data\":{ \"user\":{ \"active\":true } } }");

        JSONObject request = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject phoneNumber = new JSONObject();
        phoneNumber.put("phonenumber", "+390523444444");
        data.put("user", phoneNumber);
        request.put("data", data);

        sapiHandler.query("mobile", "signup", null, null, request);
        String body = conn.getWrittenMessage();
        assertTrue("{\"data\":{\"user\":{\"phonenumber\":\"+390523444444\"}}}".equals(body));
        assertTrue("POST".equals(conn.getRequestedMethod()));
    }

    private class TestSapiHandler extends SapiHandler {

        public TestSapiHandler(String url, String user, String pwd) {
            super(url, user, pwd);
        }

        public String getUrl(String sessionId, String key, Vector params) {
            return createUrl(sessionId, key, params);
        }

        public HttpConnectionAdapterMock getHttpConnectionAdapterMock() {
            return ((ConnectionManagerMock)connectionManager).getHttpConnectionAdapter();
        }
    }
}

