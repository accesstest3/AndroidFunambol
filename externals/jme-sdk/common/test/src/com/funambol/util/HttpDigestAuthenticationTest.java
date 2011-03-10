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

import junit.framework.*;

/**
 * Test the HttpDigestAuthentication
 *
 */
public class HttpDigestAuthenticationTest extends TestCase {

    protected static class MockAuth extends HttpDigestAuthentication {
        public MockAuth() {
            super("user", "pass", "http://funambol.org/auth/");
        }

        public String generateAuthResponseString(Long seed) {
            return super.generateAuthResponseString(seed);
        }

        public boolean processAuthInfo(String info) {
            return super.processAuthInfo(info);
        }
    }

    private static String properties = "Digest: " +
        "username=\"user\", " +
        "realm=\"sync-user\", " +
        "nonce=\"111111\", " +
        "uri=\"http://funambol.org/auth/\", " +
        "qop=\"1234567890\", " +
        "opaque=\"abcdefghij\", " +
        "cnonce=\"000002\", ";
    
    /**
     * Create a new instance of DigestAuthenticationTest
     *
     */
    public HttpDigestAuthenticationTest(String name) {
        super(name);
    }

    public void testExtractDigestProperty() throws Exception {
        String prop;

        prop = HttpDigestAuthentication.extractDigestProp(properties, "username");
        assertEquals("username: got " + prop + "; expected 'user'", prop, "user");

        prop = HttpDigestAuthentication.extractDigestProp(properties, "realm");
        assertEquals("realm: got " + prop + "; expected 'sync-user'", prop, "sync-user");

        prop = HttpDigestAuthentication.extractDigestProp(properties, "qop");
        assertEquals("qop: got " + prop + "; expected '1234567890'", prop, "1234567890");

        prop = HttpDigestAuthentication.extractDigestProp(properties, "nonce");
        assertEquals("nonce: got " + prop + "; expected '111111'", prop, "111111");

        prop = HttpDigestAuthentication.extractDigestProp(properties, "uri");
        assertEquals("uri: got " + prop + "; expected 'http://funambol.org/auth/'", prop, "http://funambol.org/auth/");

        prop = HttpDigestAuthentication.extractDigestProp(properties, "opaque");
        assertEquals("opaque: got " + prop + "; expected 'abcdefghij'", prop, "abcdefghij");

        prop = HttpDigestAuthentication.extractDigestProp(properties, "cnonce");
        assertEquals("cnonce: got " + prop + "; expected '000002'", prop, "000002");
    }
    
    public void testGenerateAuthResponseString() throws Exception {
        String authResponse;
        MockAuth auth = new MockAuth();

        if (auth.processAuthInfo(properties) == false) {
            throw new Exception("Unable to process auth info.");
        }

        authResponse = auth.generateAuthResponseString(new Long(1));

        assertTrue("username not found", authResponse.indexOf("username=\"user\"") > -1);
        assertTrue("realm not found in " + authResponse, authResponse.indexOf("realm=\"sync-user\"") > -1);
        assertTrue("nonce not found", authResponse.indexOf("nonce=\"111111\"") > -1);
        assertTrue("uri not found", authResponse.indexOf("uri=\"http://funambol.org/auth/\"") > -1);
        assertTrue("qop not found", authResponse.indexOf("qop=\"1234567890\"") > -1);
        assertTrue("nc not found", authResponse.indexOf("nc=\"00000001\"") > -1);
    }

    public void testCalculateResponse() throws Exception {
        // md5("1:2:3:4:5:6") = 2c3284a6c3a61fa999423be6b1463580
        String expected = "2c3284a6c3a61fa999423be6b1463580";
        String actual = HttpDigestAuthentication.calculateResponse("1", "6", "2", "5", "4", "3");
        assertEquals("Got:\n" + actual + "\nExpected:\n" + expected, expected, actual);
    }

    public void testCalculateHa1() throws Exception {
        // md5("1:2:3") = 7b6e2994f12a7e000c01190edec1921e
        String expected = "7b6e2994f12a7e000c01190edec1921e";
        String actual = HttpDigestAuthentication.calculateHa1("1", "3", "2");
        assertEquals("Got:\n" + actual + "\nExpected:\n" + expected, expected, actual);
    }

    public void testCalculateHa2() throws Exception {
        // md5("POST:http://funambol.org/auth/") = 079bb1feff85f964480822f0f384a46a
        String expected = "079bb1feff85f964480822f0f384a46a";
        String actual = HttpDigestAuthentication.calculateHa2("http://funambol.org/auth/");
        assertEquals("Got:\n" + actual + "\nExpected:\n" + expected, expected, actual);
    }
}
