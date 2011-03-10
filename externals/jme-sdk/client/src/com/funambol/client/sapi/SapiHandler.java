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

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;

import org.json.me.JSONObject;
import org.json.me.JSONException;

import com.funambol.platform.HttpConnectionAdapter;
import com.funambol.util.Base64;
import com.funambol.util.StringUtil;
import com.funambol.util.ConnectionManager;
import com.funambol.util.Log;

/**
 * This class is a utility to perform SAPI requests. It provides some basic
 * mechanism to authentication, and url encoding.
 */
public class SapiHandler {

    private static final String TAG_LOG = "SapiHandler";

    public static final int AUTH_NONE = -1;
    public static final int AUTH_IN_QUERY_STRING = 0;
    public static final int AUTH_IN_HTTP_HEADER  = 1;

    private static final String JSESSIONID_PARAM   = "jsessionid";
    private static final String ACTION_PARAM       = "action";

    private static final String AUTH_HEADER           = "Authorization";
    private static final String AUTH_BASIC            = "Basic";
    private static final String CONTENT_TYPE_HEADER   = "Content-Type";
    private static final String CONTENT_LENGTH_HEADER = "Content-Length";

    private static final String JSESSIONID_HEADER = "JSESSIONID";
    private static final String COOKIE_HEADER     = "Set-Cookie";

    private String baseUrl;
    private String user;
    private String pwd;

    private int authMethod = AUTH_IN_QUERY_STRING;
    private boolean jsessionAuthEnabled = false;

    private String  jsessionId = null;
    protected ConnectionManager connectionManager = ConnectionManager.getInstance();

    public SapiHandler(String baseUrl, String user, String pwd) {
        this.baseUrl = baseUrl;
        this.user    = user;
        this.pwd     = pwd;
    }

    public SapiHandler(String baseUrl) {
        this(baseUrl, null, null);
        setAuthenticationMethod(AUTH_NONE);
    }

    public void setAuthenticationMethod(int authMethod) {
        this.authMethod = authMethod;
    }

    public void enableJSessionAuthentication(boolean value) {
        this.jsessionAuthEnabled = value;
    }

    public void forceJSessionId(String jsessionId) {
        this.jsessionId = jsessionId;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public JSONObject query(String name, String action, Vector params,
            Hashtable headers, JSONObject request) throws IOException, JSONException {
        ByteArrayInputStream s = null;
        int contentLength = 0;
        if (request != null) {
            byte[] r = request.toString().getBytes("UTF-8");
            contentLength = r.length;
            s = new ByteArrayInputStream(r);
        }
        return query(name, action, params, headers, s, contentLength);
    }


    public JSONObject query(String name, String action, Vector params, 
            Hashtable headers, InputStream requestIs, int contentLength)
            throws IOException, JSONException {
        String url = createUrl(name, action, params);
        HttpConnectionAdapter conn;
        
        try {
            // Open the connection with a given size to prevent the output
            // stream from buffering all data
            Log.info(TAG_LOG, "Requesting url: " + url);
            conn = connectionManager.openHttpConnection(url, null);
            conn.setRequestMethod(HttpConnectionAdapter.POST);
            //conn.setChunkedStreamingMode(4096);
            if(contentLength > 0) {
                Log.debug(TAG_LOG, "Setting content type to application/octet-stream");
                conn.setRequestProperty(CONTENT_TYPE_HEADER, "application/octet-stream");
            }
            Log.debug(TAG_LOG, "Setting content length to " + contentLength);
            conn.setRequestProperty(CONTENT_LENGTH_HEADER, String.valueOf(contentLength));

            // Set the authentication if we have no jsessionid
            if (jsessionId != null && jsessionAuthEnabled) {
                Log.debug(TAG_LOG, "Authorization is specified via jsessionid");
                conn.setRequestProperty("Cookie", jsessionId);
            } else if (authMethod == AUTH_IN_HTTP_HEADER) {
                String token = user + ":" + pwd;
                String authToken = new String(Base64.encode(token.getBytes()));

                String authParam = AUTH_BASIC + " " + authToken;
                Log.debug(TAG_LOG, "Setting auth header to: " + authParam);
                conn.setRequestProperty(AUTH_HEADER, authParam);
            }

            // Add custom headers
            if (headers != null) {
                Enumeration keys = headers.keys();
                while(keys.hasMoreElements()) {
                    String key = (String)keys.nextElement();
                    String value = (String)headers.get(key);
                    conn.setRequestProperty(key, value);
                }
            }
            //DeviceConfig devConf = syncConfig.getDeviceConfig();
            //Log.debug(TAG_LOG, "Setting device id header to: " + devConf.devID);
            //conn.setRequestProperty(X_DEVID_HEADER, devConf.devID);
        } catch (IOException ioe) {
            Log.error(TAG_LOG, "Cannot open http connection", ioe);
            throw ioe;
        }

        OutputStream os = null;
        InputStream  is = null;

        try {
            os = conn.openOutputStream();
            // In case of SAPI that require a body, this must be written here
            // Note that the length is not handled here because we don't know
            // the length of the stream. Callers shall put it in the custom
            // headers if it is required.
            if (requestIs != null) {
                int read = 0;
                do {
                    byte chunk[] = new byte[4096];
                    read = requestIs.read(chunk);
                    if (read > 0) {
                        Log.trace(TAG_LOG, "Writing chunk: " + (new String(chunk)));
                        os.write(chunk, 0, read);
                    }
                } while(read == 4096);
            }
            os.flush();

            Log.trace(TAG_LOG, "Response is: " + conn.getResponseCode());

            // Now check the HTTP response, in case of success we set the item
            // status to OK
            StringBuffer response = new StringBuffer();
            if (conn.getResponseCode() == HttpConnectionAdapter.HTTP_OK) {
                // Open the input stream and read the response
                is = conn.openInputStream();
                // Read until we have data
                int responseLength = conn.getLength();
                if(responseLength > 0) {
                    // Read the input stream according to the response
                    // content-length header
                    int b;
                    do {
                        b = is.read();
                        responseLength--;
                        if (b != -1) {
                            response.append((char)b);
                        }
                    } while(b != -1 && responseLength > 0);
                } else if(responseLength < 0 && is.available() > 0) {
                    // The content-length header was not found in the http
                    // response. It could be a chunked encoded response
                    int b;
                    do {
                        b = is.read();
                        if (b != -1) {
                            response.append((char)b);
                        }
                    } while(b != -1 && is.available() > 0);
                } else if(responseLength < 0) {
                    // The is.available() always returns 0, we should try to read
                    // until the read byte is -1
                    try {
                        int b;
                        do {
                            b = is.read();
                            if (b != -1) {
                                response.append((char)b);
                            }
                        } while(b != -1);
                    } catch(IOException ex) {
                        // The end of the stream is reached, ignore exception
                    }
                }
            } else {
                // The request failed
                Log.error(TAG_LOG, "SAPI query error: " + conn.getResponseCode());
                throw new IOException("HTTP error code: " + conn.getResponseCode());
            }

            // This code handles JSESSION ID authentication but it is currently
            // disabled because we don't hanlde auth errors at the moment
            String cookies = conn.getHeaderField(COOKIE_HEADER);
            if (cookies != null) {
                int jsidx = cookies.indexOf(JSESSIONID_HEADER);
                if (jsidx >= 0) {
                    jsessionId = cookies.substring(jsidx);
                    int idx = jsessionId.indexOf(";");
                    if (idx >= 0) {
                        Log.debug(TAG_LOG, "Found jsessionid = " + jsessionId);
                        jsessionId = jsessionId.substring(0, idx);
                    }
                }
            }
            String r = response.toString();
            Log.trace(TAG_LOG, "response is:" + r);
            // Prepare the response
            JSONObject res = new JSONObject(r);
            return res;
        } catch (IOException ioe) {
            // If we get a non authorized error and we used a jsession id, then
            // we invalidate the jsessionId and throw the exception
            Log.error(TAG_LOG, "Error while uploading", ioe);

            if (conn != null) {
                try {
                    if (jsessionId != null && conn.getResponseCode() ==
                            HttpConnectionAdapter.HTTP_FORBIDDEN) {
                        Log.info(TAG_LOG, "Invalidating jsession id");
                        jsessionId = null;
                    }
                } catch (IOException ioe2) {}
            }
            throw ioe;
        } finally {
            // Release all resources
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {}
                os = null;
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
                is = null;
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (IOException ioe) {}
                conn = null;
            }
        }
    }

    protected String encodeURLString(String s) {
        if (s != null) {
            StringBuffer tmp = new StringBuffer();
            try {
                for(int i=0;i<s.length();++i) {
                    int b = (int)s.charAt(i);
                    if ((b>=0x30 && b<=0x39) || (b>=0x41 && b<=0x5A) || (b>=0x61 && b<=0x7A)) {
                        tmp.append((char)b);
                    } else if (b == 32) {
                        tmp.append("+");
                    } else {
                        tmp.append("%");
                        if (b <= 0xf) tmp.append("0");
                        tmp.append(Integer.toHexString(b));
                    }
                }
            } catch (Exception e) {
                Log.error(TAG_LOG, "Cannot encode URL " + s, e);
            }
            return tmp.toString();
        }
        return null;
    }


    protected String createUrl(String name, String action, Vector params) {
        // Prepare the URL
        StringBuffer url = new StringBuffer(StringUtil.extractAddressFromUrl(baseUrl));
        url.append("/").append("sapi/").append(name /* no need to encode the SAPI name */);
        // Append the Params
        url.append("?").append(ACTION_PARAM).append("=").append(encodeURLString(action));
        // Credentials in the query string
        if (authMethod == AUTH_IN_QUERY_STRING) {
            url.append("&login=").append(encodeURLString(user))
               .append("&password=").append(encodeURLString(pwd));
        }
        if (params != null) {
            for(int i=0;i<params.size();++i) {
                String param = (String)params.elementAt(i);
                int eqIndex = param.indexOf('=');
                if(eqIndex > 0) {
                    String paramName  = param.substring(0, eqIndex);
                    String paramValue = param.substring(eqIndex + 1);
                    url.append("&").append(encodeURLString(paramName))
                       .append("=").append(encodeURLString(paramValue));
                } else {
                    url.append("&").append(encodeURLString(param));
                }
            }
        }
        return url.toString();
    }

}
