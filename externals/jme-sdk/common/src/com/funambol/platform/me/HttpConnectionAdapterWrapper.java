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

package com.funambol.platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.HttpConnection;
import javax.microedition.io.Connector;

import com.funambol.util.ConnectionManager;
import com.funambol.util.Log;
import com.funambol.util.StringUtil;

/**
 * This class is a simple HttpConnection class that wraps the underlying 
 * microedition HttpConnection. Requests/responses can be written/read accessing
 * the corresponding input and output streams.
 *
 * A portable code must use this class only to perform http connections, and must take care
 * of closing the connection when not used anymore.
 * <pre>
 * Example:
 * 
 *   void httpConnectionExample(String url) throws IOException {
 *      HttpConnectionAdapter conn = new HttpConnectionAdapter();
 *
 *      // Open the connection
 *      conn.open(url);
 *
 *      conn.setRequestMethod(HttpConnectionAdapter.POST);
 *      conn.setRequestProperty("CUSTOM-HEADER", "CUSTOM-VALUE");
 *
 *      OutputStream os = conn.openOutputStream();
 *      os.write("TEST");
 *      os.close();
 *
 *      // Suppose the answer is bound to 1KB
 *      byte anwser[] = new byte[1024];
 *      InputStream is = conn.openInputStream();
 *      is.read(answer);
 *      is.close();
 *
 *      // Close the connection
 *      conn.close();
 * </pre>
 */
public class HttpConnectionAdapterWrapper extends HttpConnectionAdapter {

    private static final String TAG_LOG         = "HttpConnectionAdapterWrapper";

    /** This is the underlying connection */
    private HttpConnection conn;

    private String hostname;
    private String port;
    private String resourceName;

    public HttpConnectionAdapterWrapper() {
    }

    /**
     * Open the connection to the given url.
     */
    public void open(String url) throws IOException {

        Log.debug(TAG_LOG, "Opening url: " + url);

        try {
            conn = (HttpConnection)Connector.open(url, Connector.READ_WRITE, false);
        } catch (javax.microedition.io.ConnectionNotFoundException e) {
            // Transform the microedition specific exception into a platform
            // independent one
            throw new ConnectionNotFoundException(e.toString());
        }
    }

    /**
     * This method closes this connection. It does not close the corresponding
     * input and output stream which need to be closed separately (if they were
     * previously opened)
     *
     * @throws IOException if the connection cannot be closed
     */
    public void close() throws IOException {
        if (conn == null) {
            throw new IOException("Connection not opened");
        }
        conn.close();
    }

    /**
     * Open the input stream. The ownership of the stream is transferred to the
     * caller which is responsbile to close and release the resource once it is
     * no longer used. This method shall be called only once per connection.
     *
     * @throws IOException if the input stream cannot be opened or the output
     * stream has not been closed yet.
     */
    public InputStream openInputStream() throws IOException {
        if (conn == null) {
            throw new IOException("Cannot open input stream on non opened connection");
        }
        return conn.openInputStream();
    }

    /**
     * Open the output stream. The ownership of the stream is transferred to the
     * caller which is responsbile to close and release the resource once it is
     * no longer used. This method shall be called only once per connection.
     *
     * @throws IOException if the output stream cannot be opened.
     */
    public OutputStream openOutputStream() throws IOException {
        if (conn == null) {
            throw new IOException("Cannot open output stream on non opened connection");
        }
        return conn.openOutputStream();
    }

    /**
     * Returns the HTTP response status code. It parses responses like:
     *
     * HTTP/1.0 200 OK
     * HTTP/1.0 401 Unauthorized
     * 
     * and extracts the ints 200 and 401 respectively. from the response (i.e., the response is not valid HTTP).
     *
     * Returns:
     *   the HTTP Status-Code or -1 if no status code can be discerned. 
     * Throws:
     *   IOException - if an error occurred connecting to the server.
     */
    public int getResponseCode() throws IOException {
        if (conn == null) {
            throw new IOException("Cannot get response on non opened connection");
        }
        return conn.getResponseCode();
    }

    /**
     * Returns the HTTP response message. It parses responses like:
     *
     * HTTP/1.0 200 OK
     * HTTP/1.0 401 Unauthorized
     * 
     * and extracts the strings OK and Unauthorized respectively. from the response (i.e., the response is not valid HTTP).
     *
     * Returns:
     *   the HTTP Response-Code or null if no status message can be discerned. 
     * Throws:
     *   IOException - if an error occurred connecting to the server.
     */
    public String getResponseMessage() throws IOException {
        if (conn == null) {
            throw new IOException("Cannot get response on non opened connection");
        }
        return conn.getResponseMessage();
    }


    /**
     * Set the method for the URL request, one of:
     * GET
     * POST
     * HEAD 
     * are legal, subject to protocol restrictions. The default method is GET. 
     */
    public void setRequestMethod(String method) throws IOException {
        if (conn == null) {
            throw new IOException("Cannot get response on non opened connection");
        }
        conn.setRequestMethod(method);
    }

    /**
     * Set chunked encoding for the file to be uploaded. This avoid the output
     * stream to buffer all data before transmitting it.
     * This is currently not supported by this implementation and the method has
     * no effect.
     *
     * @param chunkLength the length of the single chunk
     */
    public void setChunkedStreamingMode(int chunkLength) throws IOException {
    }
    
    /**
     * Sets the general request property. If a property with the key already exists,
     * overwrite its value with the new value.
     *
     * NOTE: HTTP requires all request properties which can legally have multiple instances
     * with the same key to use a comma-seperated list syntax which enables multiple
     * properties to be appended into a single property.
     *
     * @param key the keyword by which the request is known (e.g., "accept").
     * @param value the value associated with it.
     */
    public void setRequestProperty(String key, String value) throws IOException {
        if (conn == null) {
            throw new IOException("Cannot get response on non opened connection");
        }
        conn.setRequestProperty(key, value);
    }

    /**
     * Returns the value of the named header field.
     *
     * @param key name of a header field.
     * @return the value of the named header field, or null if there is no such field in the header.
     * @throws IOException if an error occurred connecting to the server.
     */
    public String getHeaderField(String key) throws IOException {
        if (conn == null) {
            throw new IOException("Cannot get response on non opened connection");
        }
        return conn.getHeaderField(key);
    }

    /**
     * Returns the key for the nth header field. Some implementations may treat the
     * 0th header field as special, i.e. as the status line returned by the HTTP server.
     * In this case, getHeaderField(0)  returns the status line, but getHeaderFieldKey(0) returns null.
     */
    public String getHeaderFieldKey(int num) throws IOException {
        if (conn == null) {
            throw new IOException("Cannot open output stream on non opened connection");
        }
        return conn.getHeaderFieldKey(num);
    }


    /**
     * Returns the answer length (excluding headers. This is the content-length
     * field length)
     */
    public int getLength() throws IOException {
        if (conn == null) {
            throw new IOException("Cannot get length on non opened connection");
        }
        return (int)conn.getLength();
    }
}


