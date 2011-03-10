/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2003 - 2007 Funambol, Inc.
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
 * Testing the MD5 implementation.
 */
public class MD5Test extends TestCase {
    
    private MD5 md5;
    
    /** Creates a new instance of ThreadPoolTest */
    public MD5Test(String name) {
        super(name);
        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.TRACE);
        md5 = new MD5();
    }
   
    public void testAuth() throws AssertionFailedError {
        
        // Set config parameters
        String username = "user";
        String password = "password";
        String nonce = "nonce";
        
        byte[] credentials = md5.computeMD5Credentials(username, password,
                nonce.getBytes());
        System.out.println("credentials in byte: " + byteAsHex(credentials));
      //  String expectedCred = "6yCj39RcoU31wnz3zOo0bQ==";
          String expectedCred = new String(new Base64().encode(getMD5()));
        byte[] expectedBytes = expectedCred.getBytes();
        
        System.out.println("Expected: (hex) " + byteAsHex(expectedCred.getBytes()));
        System.out.println("Expected: " + expectedCred);
        System.out.println("Actual: " + new String(credentials));
        
        
        boolean equal = true;
        if (credentials.length != expectedBytes.length) {
            equal = false;
        } else {
            for(int i=0;i<credentials.length;++i) {
                if (credentials[i] != expectedBytes[i]) {
                    equal = false;
                    break;
                }
            }
        }
        
        if (equal) {
            Log.info("Autentication test passed!");
        } else {
            Log.error("Autentication test failed!");
        }
        
        assertTrue(equal);
    }
    
    private String byteAsHex(byte[] b) {
        StringBuffer buf = new StringBuffer();
        
        for (int i = 0; i < b.length; i++) {
            buf.append(Integer.toHexString((int)b[i] & 0xFF));
        }
        
        return buf.toString();
        
        
    }
    
    /**
     * test md5 encoding
     */
    public void testMD5() throws Exception {
        Log.info("--- Test MD5 ----------------------------------------------");
        String toHash = "md5";
        String expectedBytesString = "1bc29b36f623ba82aaf6724fd3b16718";
        assertEquals(expectedBytesString, byteAsHex(md5.calculateMD5(toHash.getBytes())));
        Log.info("-----------------------------------------------------[ OK ]");
    }
    
      
    private byte[] getMD5() {
        
        byte b[] = new byte[16];
        b[0] = (byte)Integer.parseInt("c6", 16);
        b[1] = (byte)Integer.parseInt("4b", 16);
        b[2] = (byte)Integer.parseInt("19", 16);
        b[3] = (byte)Integer.parseInt("ce", 16);
        b[4] = (byte)Integer.parseInt("f9", 16);
        b[5] = (byte)Integer.parseInt("b5", 16);
        b[6] = (byte)Integer.parseInt("90", 16);
        b[7] = (byte)Integer.parseInt("ea", 16);
        b[8] = (byte)Integer.parseInt("40", 16);
        b[9] = (byte)Integer.parseInt("f2", 16);
        b[10] = (byte)Integer.parseInt("87", 16);
        b[11] = (byte)Integer.parseInt("1d", 16);
        b[12] = (byte)Integer.parseInt("5c", 16);
        b[13] = (byte)Integer.parseInt("16", 16);
        b[14] = (byte)Integer.parseInt("fa", 16);
        b[15] = (byte)Integer.parseInt("4e", 16);
        return b;
        
    }
    
    // not used now, but can be useful for testing purposes and generate methods
    // like getMd5 above
    private void printByteDec(byte[] b) {
        
        StringBuffer print = new StringBuffer();
        
        for (int i = 0; i < b.length; i++) {
            print.append("b[" + i + "] = Byte.parseByte(\"" + Integer.toHexString((int)b[i] & 0xFF)  +
                    "\", 16);\n" );
        }
        
        System.out.print(print);
        
        
        
    }
}
