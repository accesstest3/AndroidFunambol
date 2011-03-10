/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2009 Funambol, Inc.
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
package com.funambol.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import com.funambol.platform.ConnectionNotFoundException;
import com.funambol.platform.HttpConnectionAdapter;

import com.funambol.util.Log;
import com.funambol.util.StreamReaderFactory;
import com.funambol.util.MD5;

/**
 * An authentication object for http digest authentication.
 */
public class HttpDigestAuthentication implements HttpAuthentication {
    private String realm;
    private String qop;
    private String nonce;
    private String opaque;
    private String username;
    private String password;
    private String uri;
    private String authInfoResponse;
    private boolean retryWithAuth = false;
    private boolean doAuthentication = false;
    private static final char[] hex = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
    };

    // ------------------------------------------------------------ Constructors
    /**
     * Create the digest authentication object with a username, password, and uri.
     * 
     * @param username The username of the client being authenticated
     * @param password The password for the client being authenticated
     * @param uri      The uri to authenticate against
     */
    public HttpDigestAuthentication(final String user, final String pass, final String authUri) {
        username = user;
        password = pass;
        uri = authUri;
        doAuthentication = true;
        retryWithAuth = false;
    }

    /**
     * Extract a specific property from a string of property value pairs separated
     * by '='
     *
     * @param str   The string of properties
     * @param prop  The property to extract
     *
     * @return The value of the property, if it exists
     */
    public static String extractDigestProp(String str, String prop) {
        int start;
        int end;
        char prev = '\0';

        start = str.indexOf(prop + "=\"") + prop.length() + 2;
        end = start;
        for (int i=start; i < str.length(); i++) {
            if (str.charAt(i) == '"' && prev != '\\') {
                end = i;
                break;
            }
            prev = str.charAt(i);
        }
        return str.substring(start, end);
    }

    /**
     * Convert a hash to a hexadecimal number.
     *
     * @param hash The byte array hash to convert
     *
     * @return The byte array in hexadecimal format
     */
    public static final String toHex(byte hash[]) {
        StringBuffer buf = new StringBuffer(hash.length * 2);

        for (int idx=0; idx<hash.length; idx++) {
            buf.append(hex[(hash[idx] >> 4) & 0x0f]).append(
                    hex[hash[idx] & 0x0f]);
        }

        return buf.toString();
    }

    /**
     * Generate an authentication response of digest properties and values.
     *
     * @return An authentication response.
     */
    protected String generateAuthResponseString() {
        return generateAuthResponseString(new Long(new Random().nextLong()));
    }
    
    /**
     * Generate an authentication response of digest properties and values,
     * given a seed to generate a client nonce.
     *
     *
     * @return An authentication response
     */
    protected String generateAuthResponseString(Long seed) {
        StringBuffer buf = new StringBuffer("Digest");
        MD5 md5 = new MD5();
        String nc = "00000001";
        String ha1;
        String ha2;
        String cnonce = "";

        cnonce = toHex(md5.calculateMD5(seed.toString().getBytes()));
        String response;

        buf.append(" username=\"");
        buf.append(username);
        buf.append("\", realm=\"");
        buf.append(realm);
        buf.append("\", nonce=\"");
        buf.append(nonce);
        buf.append("\", uri=\"");
        buf.append(uri);
        buf.append("\", qop=\"");
        buf.append(qop);
        buf.append("\", nc=\"");
        buf.append(nc);
        buf.append("\", cnonce=\"");
        buf.append(cnonce);
        buf.append("\", response=\"");

        ha1 = calculateHa1(username, password, realm);
        ha2 = calculateHa2(uri);
        response = calculateResponse(ha1, ha2, nonce, qop, cnonce, nc);
        buf.append(response);
        buf.append("\", opaque=\"");
        buf.append(opaque + "\"");
        return buf.toString();
    }

    /**
     * Process the given authentication information.
     *
     * @param info The authentication string of digest properties and values
     *
     * @return Whether or not the given info contained digest authentication information
     */
    protected boolean processAuthInfo(String info) {
        String t = info.trim();
        int indexOfRealm;

        if (t.substring(0, 6).equals("Digest")) {
            realm = extractDigestProp(t, "realm");
            qop = extractDigestProp(t, "qop");
            nonce = extractDigestProp(t, "nonce");
            opaque = extractDigestProp(t, "opaque");
            authInfoResponse = generateAuthResponseString();
            return true;
        }
        return false;
    }

    /**
     * Set the authentication property in the given HTTP connection.
     *
     * @param c The connection on which the authentication information is being set
     *
     * @return Whether or not the authentication property was set for the given
     * HTTP connection
     */
    public boolean handleAuthentication(HttpConnectionAdapter c) throws IOException {
        boolean didSetAuthProp = false;
        if (doAuthentication && retryWithAuth) {
            c.setRequestProperty("Authorization", authInfoResponse);
            didSetAuthProp = true;
        }
        return didSetAuthProp;
    }

    /**
     * Check the given connection for a 401 HTTP error and process any authentication
     * information that is present.
     *
     * @param c The connection to check for and process HTTP errors.
     *
     * @return Whether or not an error was processed.
     */
    public boolean processHttpError(HttpConnectionAdapter c) throws IOException {
        boolean processedAuthInfo = false;
        int httpCode = c.getResponseCode();

        if (doAuthentication && httpCode == 401) {
            if (processAuthInfo(c.getHeaderField("WWW-Authenticate"))) {
                retryWithAuth = !retryWithAuth;
                processedAuthInfo = true;
            }
        //cannot handle the given auth info
        } else {
            retryWithAuth = false;
        }
        return processedAuthInfo;
    }

    /**
     * Calculate ha1 given a username, realm, and password.
     *
     * @param username The username of the user being authenticated
     * @param realm The authentication realm
     * @param password The password of the user being authenticated
     *
     * @return A calculated ha1.
     *
     */
    public static String calculateHa1(String username, String password, String realm) {
        MD5 md5 = new MD5();
        return toHex(md5.calculateMD5((username + ":" + realm + ":" + password).getBytes()));
    }

    /**
     * Calculate ha2 given a uri.
     *
     * @param uri The uri to authenticate against.
     *
     * @return A calculated ha2
     */
    public static String calculateHa2(String uri) {
        MD5 md5 = new MD5();
        return toHex(md5.calculateMD5(("POST" + ":" + uri).getBytes()));
    }

    /**
     * Calculate the response given ha1 and ha2.
     *
     * @param ha1 A calculated ha1 value
     * @param ha2 A calculated ha2 value
     * @param cnonce The client nonce
     * @param nc The nonce count
     *
     * @return A calculated response
     */
    public static String calculateResponse(String ha1, String ha2, String nonce, String qop, String cnonce, String nc) {
        MD5 md5 = new MD5(); 
        return toHex(md5.calculateMD5((ha1 + ":" + nonce + ":" + nc +
                                           ":" + cnonce + ":" + qop + ":" +
                                           ha2).getBytes()));
    }

    /**
     * Return whether or not to retry requests with authentication information.
     *
     * @return Whether or not to retry requests
     */
    public boolean getRetryWithAuth() {
        return retryWithAuth;
    }

    /**
     * Set the username of the client being authenticated.
     *
     * @param value The username to authenticate with
     */
    public void setUsername(String value) {
        username = value;
    }

    /**
     * Set the password for the authentication.
     *
     * @param value The password to authenticate with
     */
    public void setPassword(String value) {
        password = value;
    }

    /**
     * Set the URI to authenticate against.
     *
     * @param value The URI to authenticate against
     */
    public void setUri(String value) {
        uri = value;
    }
}
