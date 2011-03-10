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
import javax.microedition.io.SecureConnection;
import javax.microedition.io.SocketConnection;

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
public class HttpConnectionAdapter {

    private static final String TAG_LOG         = "HttpConnectionAdapter";

    public static int HTTP_ACCEPTED             = HttpConnection.HTTP_ACCEPTED;
    public static int HTTP_BAD_GATEWAY          = HttpConnection.HTTP_BAD_GATEWAY;
    public static int HTTP_BAD_METHOD           = HttpConnection.HTTP_BAD_METHOD;
    public static int HTTP_BAD_REQUEST          = HttpConnection.HTTP_BAD_REQUEST;
    public static int HTTP_CLIENT_TIMEOUT       = HttpConnection.HTTP_CLIENT_TIMEOUT;
    public static int HTTP_CONFLICT             = HttpConnection.HTTP_CONFLICT;
    public static int HTTP_CREATED              = HttpConnection.HTTP_CREATED;
    public static int HTTP_ENTITY_TOO_LARGE     = HttpConnection.HTTP_ENTITY_TOO_LARGE;
    public static int HTTP_FORBIDDEN            = HttpConnection.HTTP_FORBIDDEN;
    public static int HTTP_GATEWAY_TIMEOUT      = HttpConnection.HTTP_GATEWAY_TIMEOUT;
    public static int HTTP_GONE                 = HttpConnection.HTTP_GONE;
    public static int HTTP_INTERNAL_ERROR       = HttpConnection.HTTP_INTERNAL_ERROR;
    public static int HTTP_LENGTH_REQUIRED      = HttpConnection.HTTP_LENGTH_REQUIRED;
    public static int HTTP_MOVED_PERM           = HttpConnection.HTTP_MOVED_PERM;
    public static int HTTP_MOVED_TEMP           = HttpConnection.HTTP_MOVED_TEMP;
    public static int HTTP_MULT_CHOICE          = HttpConnection.HTTP_MULT_CHOICE;
    public static int HTTP_NO_CONTENT           = HttpConnection.HTTP_NO_CONTENT;
    public static int HTTP_NOT_ACCEPTABLE       = HttpConnection.HTTP_NOT_ACCEPTABLE;
    public static int HTTP_NOT_AUTHORITATIVE    = HttpConnection.HTTP_NOT_AUTHORITATIVE;
    public static int HTTP_NOT_FOUND            = HttpConnection.HTTP_NOT_FOUND;
    public static int HTTP_NOT_IMPLEMENTED      = HttpConnection.HTTP_NOT_IMPLEMENTED;
    public static int HTTP_NOT_MODIFIED         = HttpConnection.HTTP_NOT_MODIFIED;
    public static int HTTP_OK                   = HttpConnection.HTTP_OK;
    public static int HTTP_PARTIAL              = HttpConnection.HTTP_PARTIAL;
    public static int HTTP_PAYMENT_REQUIRED     = HttpConnection.HTTP_PAYMENT_REQUIRED;
    public static int HTTP_PRECON_FAILED        = HttpConnection.HTTP_PRECON_FAILED;
    public static int HTTP_PROXY_AUTH           = HttpConnection.HTTP_PROXY_AUTH;
    public static int HTTP_REQ_TOO_LONG         = HttpConnection.HTTP_REQ_TOO_LONG;
    public static int HTTP_RESET                = HttpConnection.HTTP_RESET;
    public static int HTTP_SEE_OTHER            = HttpConnection.HTTP_SEE_OTHER;
    public static int HTTP_TEMP_REDIRECT        = HttpConnection.HTTP_TEMP_REDIRECT;
    public static int HTTP_UNAUTHORIZED         = HttpConnection.HTTP_UNAUTHORIZED;
    public static int HTTP_UNAVAILABLE          = HttpConnection.HTTP_UNAVAILABLE;
    public static int HTTP_UNSUPPORTED_TYPE     = HttpConnection.HTTP_UNSUPPORTED_TYPE;
    public static int HTTP_USE_PROXY            = HttpConnection.HTTP_USE_PROXY;
    public static int HTTP_VERSION              = HttpConnection.HTTP_VERSION;
    public static int HTTP_EXPECT_FAILED        = HttpConnection.HTTP_EXPECT_FAILED; 
    public static int HTTP_UNSUPPORTED_RANGE    = HttpConnection.HTTP_UNSUPPORTED_RANGE; 

    // These are the constants that can be specified in the setRequestMethod
    public static final String GET              = HttpConnection.GET;
    public static final String POST             = HttpConnection.POST;
    public static final String HEAD             = HttpConnection.HEAD;

    private static final String HTTP_PROTOCOL    = "HTTP/1.1";
    private static final String HTTP_HEADER_HOST = "Host";
    private static final String CRLF             = "\r\n";

    private static final int KEEP_ALIVE         = 1;
    private static final int SEND_BUFFER_SIZE   = 40960; //40KB

    private static final String HTTP_DEFAULT_PORT  = "80";
    private static final String HTTPS_DEFAULT_PORT = "443";

    private Hashtable requestHeaders;
    private Hashtable responseHeaders;
    private Hashtable responseHeadersId;
    
    private String requestMethod = GET;
    private int responseCode = 500;
    private String responseMessage = "";

    private OutputStream socketOs;
    private InputStream  socketIs;

    /** This is the underlying connection */
    private SocketConnection conn;

    private String hostname;
    private String port;
    private String resourceName;

    public HttpConnectionAdapter() {
    }

    /**
     * Open the connection to the given url.
     */
    public void open(String url) throws IOException {

        Log.debug(TAG_LOG, "Opening url: " + url);
        retrieveUrlComponents(url);

        url = getSocketLevelUrl(url);

        try {
            conn = (SocketConnection)ConnectionManager.getInstance().open(url);
        } catch (javax.microedition.io.ConnectionNotFoundException e) {
            // Transform the microedition specific exception into a platform
            // independent one
            throw new ConnectionNotFoundException(e.toString());
        }

        // Setup SocketConnection options
        conn.setSocketOption(SocketConnection.KEEPALIVE, KEEP_ALIVE);

        requestHeaders = new Hashtable();
        requestHeaders.put(HTTP_HEADER_HOST, hostname);
    }

    /**
     * This method closes this connection. It does not close the corresponding
     * input and output stream which need to be closed separately (if they were
     * previously opened)
     *
     * @throws IOException if the connection cannot be closed
     */
    public void close() throws IOException {
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
        if (socketIs == null) {
            throw new IOException("Before opening the input stream the output one must be closed");
        }
        return socketIs;
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
        socketOs = conn.openOutputStream();
        return new ConnectionOutputStream();
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
        return responseCode;
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
        return responseMessage;
    }


    /**
     * Set the method for the URL request, one of:
     * GET
     * POST
     * HEAD 
     * are legal, subject to protocol restrictions. The default method is GET. 
     */
    public void setRequestMethod(String method) throws IOException {
        requestMethod = method;
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
        requestHeaders.put(key, value);
    }

    /**
     * Returns the value of the named header field.
     *
     * @param key name of a header field.
     * @return the value of the named header field, or null if there is no such field in the header.
     * @throws IOException if an error occurred connecting to the server.
     */
    public String getHeaderField(String key) throws IOException {
        if (responseHeaders == null) {
            return null;
        } else {
            return (String)responseHeaders.get(key);
        }
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
        if (responseHeadersId == null) {
            return null;
        } else {
            return (String)responseHeadersId.get(new Integer(num));
        }
    }


    /**
     * Returns the answer length (excluding headers. This is the content-length
     * field length)
     */
    public int getLength() throws IOException {
        if (conn == null) {
            throw new IOException("Cannot get length on non opened connection");
        }
        String len = (String)responseHeaders.get("content-length");
        if (len == null) {
            len = (String)responseHeaders.get("Content-Length");
        }
        if (len != null) {
            try {
                return Integer.parseInt(len);
            } catch (Exception e) {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * @return true if the current connection is secure
     */
    private boolean isSecureConnection() {
        return (conn instanceof SecureConnection);
    }

    private String getSocketLevelUrl(String httpUrl) throws IOException {
        String protocol = StringUtil.getProtocolFromUrl(httpUrl);
        StringBuffer socketUrl = new StringBuffer();
        if(protocol != null && protocol.equals("http")) {
            socketUrl.append("socket://");
        } else if(protocol != null && protocol.equals("https")) {
            socketUrl.append("ssl://");
        } else {
            throw new IOException("Unsupported protocol " + protocol);
        }
        socketUrl.append(hostname);
        if(port != null) {
            socketUrl.append(":").append(port); 
        }
        return socketUrl.toString();
    }

    /**
     * Retrieves url components: hostname, port and resource name
     * @param url
     */
    private void retrieveUrlComponents(String url) {

        Log.trace(TAG_LOG, "Retrieving url components");
        int protocolEndIndex = url.indexOf("://");
        int addressStartIndex = protocolEndIndex+3;

        // Save the original url to be used in case it does not contain the port
        String completeUrl = url;
        // Remove the protocol part
        url = url.substring(addressStartIndex);

        String address = url.substring(0, url.indexOf('/'));
        if(url.indexOf(';') >= 0) {
            resourceName = url.substring(url.indexOf('/'), url.indexOf(';'));
        } else {
            resourceName = url.substring(url.indexOf('/'));
        }
        Log.trace(TAG_LOG, "Found resource name: " + resourceName);
        
        if(address.indexOf(':') > 0) {
            hostname = address.substring(0, address.indexOf(':'));
            port = address.substring(address.indexOf(':')+1);
        } else {
            hostname = address;
            String protocol = StringUtil.getProtocolFromUrl(completeUrl);
            if ("http".equals(protocol)) {
                port = HTTP_DEFAULT_PORT;
            } else if ("https".equals(protocol)) {
                port = HTTPS_DEFAULT_PORT;
            } else {
                Log.error(TAG_LOG, "Unspecified port, this will probably result in a failure during socket opening");
            }
        }
        Log.trace(TAG_LOG, "Found host name: " + hostname);
        Log.trace(TAG_LOG, "Found port number: " + port);
    }

    private class ConnectionOutputStream extends OutputStream {

        private boolean hdrSent = false;

        private ByteArrayOutputStream bufferOs = new ByteArrayOutputStream();

        public ConnectionOutputStream() {
        }

        public void write(int b) throws IOException {
            if(!hdrSent) {
                writeHeader();
            }
            bufferOs.write(b);
            if(bufferOs.size() >= SEND_BUFFER_SIZE) {
                writeBufferOsToSocket();
            }
        }

        private void writeBufferOsToSocket() throws IOException {
            Log.trace(TAG_LOG, "Writing " + bufferOs.size() + " bytes to " +
                    "socket OutputStream");
            byte[] bufferContent = bufferOs.toByteArray();
            // In secure connections the buffer content shall be written byte 
            // per byte. A bug into the secure connection implementation doesn't
            // allow the server to read the stream properly.
            if(isSecureConnection()) {
                for(int i=0; i<bufferContent.length; i++) {
                    socketOs.write(bufferContent[i]);
                }
            } else {
                socketOs.write(bufferContent);
            }
            bufferOs.reset();
        }

        public void flush() throws IOException {
            if(!hdrSent) {
                writeHeader();
            }
            Log.trace(TAG_LOG, "Flushing output stream");
            flushSocketOutputStream();
            initSocketInputStream();
        }

        public void close() throws IOException {
            Log.trace(TAG_LOG, "Closing output stream");
            try {
                flushSocketOutputStream();
                initSocketInputStream();
            } finally {
                if (socketOs != null) {
                    socketOs.close();
                }
            }
        }

        private void flushSocketOutputStream() throws IOException {
            Log.trace(TAG_LOG, "Flushing socket output stream");
            if(bufferOs.size() > 0) {
                writeBufferOsToSocket();
            }
            socketOs.flush();
        }

        private void initSocketInputStream() throws IOException {
            // Open the input stream to get the response header (so that
            // the response code is available)
            socketIs = conn.openInputStream();
            readResponseHdr();
        }

        /**
         * Reads response headers and retrieves the status code from the status
         * line.
         * @throws IOException
         */
        private void readResponseHdr() throws IOException {
            Log.trace(TAG_LOG, "Reading response header");
            // Read everything until we find an empty line
            StringBuffer line = new StringBuffer();
            String statusLine = null;
            responseHeaders = new Hashtable();
            responseHeadersId = new Hashtable();
            int lastCh = -1;
            do {
                int ch = socketIs.read();
                if (ch == -1) {
                    break;
                }
                line.append((char)ch);
                if (lastCh == '\r' && ch == '\n') {
                    if (line.length() > 2) {
                        String header = line.toString().trim();
                        Log.trace(TAG_LOG, "Found header line: " + header);
                        if (statusLine == null) {
                            statusLine = header;
                        } else {
                            int seppos = header.indexOf(':');
                            if(seppos > 0) {
                                String headerKey = header.substring(0, seppos).trim();
                                String headerValue = header.substring(seppos+1).trim();
                                responseHeaders.put(headerKey, headerValue);
                                responseHeadersId.put(new Integer(responseHeadersId.size()), headerValue);
                            } else {
                                Log.error(TAG_LOG, "Invalid header from server: " + header);
                            }
                        }
                    } else {
                        // we are done in this case
                        break;
                    }
                    line = new StringBuffer();
                }
                lastCh = ch;
            } while(true);

            if (statusLine != null) {
                Log.debug("Status received from server: " + statusLine);
                String comps[] = StringUtil.split(statusLine, " ");
                // Get the response code
                if(comps != null && comps.length == 3) {
                    responseCode = Integer.parseInt(comps[1]);
                    responseMessage = comps[2];
                } else {
                    Log.error(TAG_LOG, "Invalid status line from server: " + statusLine);
                }
            } else {
                Log.error(TAG_LOG, "Status line not found in server response");
            }
            String encoding = (String)responseHeaders.get("Transfer-Encoding");
            if(encoding == null) {
                encoding = (String)responseHeaders.get("transfer-encoding");
            }
            if(encoding != null && "chunked".equals(encoding)) {
                Log.debug(TAG_LOG, "Using ChunkedInputStream");
                socketIs = new ChunkedInputStream(socketIs);
            }
        }

        /**
         * Write the http headers to the OutputStream
         * @return
         * @throws IOException
         */
        private void writeHeader() throws IOException {

            StringBuffer requestLine = new StringBuffer();
            requestLine.append(requestMethod).append(" ").append(resourceName)
                    .append(" ").append(HTTP_PROTOCOL).append(CRLF);
            // For the headers
            Enumeration keys = requestHeaders.keys();
            StringBuffer hdr = new StringBuffer();
            while(keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                String value = (String)requestHeaders.get(key);
                hdr.append(key).append(": ").append(value).append(CRLF);
            }
            StringBuffer allHdr = new StringBuffer(requestLine.toString());
            allHdr.append(hdr.toString()).append(CRLF);

            Log.trace(TAG_LOG, "Writing headers: " + allHdr.toString());
            bufferOs.write(allHdr.toString().getBytes());
            hdrSent = true;
        }
    }

    /**
     * Wrap a chunked input stream to a standard input stream.
     * 
     * NOTE: there is no support for chunk extensions and trailers
     *       http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.6.1
     */
    public static class ChunkedInputStream extends InputStream {

        private static final int CR = '\r';
        private static final int LF = '\n';

        private InputStream stream = null;
        private int chunkSize = 0;

        private boolean endReached = false;
        private boolean isFirst = true;
        
        public ChunkedInputStream(InputStream aStream) throws IOException {
            this.stream = aStream;
        }

        public int read() throws IOException {
            if(endReached) {
                return -1;
            }
            refreshChunkSize();
            if(chunkSize > 0) {
                chunkSize--;
                return stream.read();
            } else {
                endReached = true;
                return -1;
            }
        }

        private void readCRLF(InputStream aStream) throws IOException {
            readCR(aStream);
            readLF(aStream);
        }

        private void readCR(InputStream aStream) throws IOException {
            int cr = aStream.read();
            if(cr != CR) {
                throw new IOException("Expected CR char in InputStream");
            }
        }

        private void readLF(InputStream aStream) throws IOException {
            int lf = aStream.read();
            if(lf != LF) {
                throw new IOException("Expected LF char in InputStream");
            }
        }

        private void refreshChunkSize() throws IOException {
            if(endReached) { return; }
            if(chunkSize == 0) {
                chunkSize = readChunkSize(stream);
            }
        }

        private int readChunkSize(InputStream aStream) throws IOException {
            if(!isFirst) {
                readCRLF(aStream);
            } else {
                isFirst = false;
            }
            StringBuffer chunkSizeString = new StringBuffer();
            int ch;
            while((ch = aStream.read()) != CR) {
                chunkSizeString.append((char)ch);
            }
            readLF(aStream);
            return Integer.valueOf(chunkSizeString.toString(), 16).intValue();
        }

        public void close() throws IOException {
            stream.close();
        }

        public int available() throws IOException {
            refreshChunkSize();
            return chunkSize;
        }

        public boolean markSupported() {
            return false;
        }
    }
}


