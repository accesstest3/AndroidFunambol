/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2008 Funambol, Inc.
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

import java.util.Vector;

import junit.framework.*;


/**
 * Testing the StringUtil implementation.
 */
public class StringUtilTest extends TestCase {
    
    /** Creates a new instance of ThreadPoolTest */
    public StringUtilTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.DEBUG);
    }
    
    public void testReplaceAll() throws Exception {
        String test1 = "Trialxcarriage return";
        String test2 = "\nTrial carriage return";
        String test3 = "\rTrial carriage return";
        String test4 = "\r\nTrial \r\ncarriage return";
        
        String exp1 = test1;
        String exp2 = " Trial carriage return";
        String exp3 = " Trial carriage return";
        String exp4 = "  Trial   carriage return";
        
        boolean t1 = StringUtil.replaceAll(test1, '\r', ' ').equals(exp1);
        boolean t2 = StringUtil.replaceAll(test2, '\n', ' ').equals(exp2);
        boolean t3 = StringUtil.replaceAll(test3, '\r', ' ').equals(exp3);
        test4 = StringUtil.replaceAll(test4, '\r', ' ');
        boolean t4 = StringUtil.replaceAll(test4, '\n', ' ').equals(exp4);

        assertTrue(true==t1==t2==t3==t4);
    }

    public void testReplaceAll2() throws Exception {

        String test = "Hello Funambol";
        String res;
        res = StringUtil.replaceAll(test, "low", "high");
        Log.info("res=" + res);
        assertTrue(test.equals(res));

        res = StringUtil.replaceAll(test, "lo", "hi");
        Log.info("res=" + res);
        assertTrue("Helhi Funambol".equals(res));

        res = StringUtil.replaceAll(test, "Hello", "Good night");
        Log.info("res=" + res);
        assertTrue("Good night Funambol".equals(res));

        res = StringUtil.replaceAll(test, "Funambol", "world!");
        Log.info("res=" + res);
        assertTrue("Hello world!".equals(res));
    }
    
    public void testExtractAddressFromUrl() throws Exception {
        String res;
        res = StringUtil.extractAddressFromUrl("http://pv.funambol.com/sync", "http");
        assertEquals("pv.funambol.com", res);

        res = StringUtil.extractAddressFromUrl("http://pv.funambol.com:8080/sync", "http");
        assertEquals("pv.funambol.com", res);

        res = StringUtil.extractAddressFromUrl("pv.funambol.com/sync", "http");
        assertEquals("pv.funambol.com", res);

        res = StringUtil.extractAddressFromUrl("pv.funambol.com:8080/sync", "http");
        assertEquals("pv.funambol.com", res);

        res = StringUtil.extractAddressFromUrl("http://pv.funambol.com", "http");
        assertEquals("pv.funambol.com", res);

        res = StringUtil.extractAddressFromUrl("pv.funambol.com", "http");
        assertEquals("pv.funambol.com", res);
    }

    public void testExtractAddress() throws Exception {
        String res;
        res = StringUtil.extractAddressFromUrl("http://pv.funambol.com/sync");
        assertEquals("http://pv.funambol.com", res);

        res = StringUtil.extractAddressFromUrl("http://pv.funambol.com:8080/sync");
        assertEquals("http://pv.funambol.com:8080", res);


        res = StringUtil.extractAddressFromUrl("http://pv.funambol.com");
        assertEquals("http://pv.funambol.com", res);


    }

    public void testIsNullOrEmpty() throws Exception {
        boolean res;
        res = StringUtil.isNullOrEmpty("test");
        assertTrue(!res);

        res = StringUtil.isNullOrEmpty("");
        assertTrue(res);

        res = StringUtil.isNullOrEmpty(null);
        assertTrue(res);
    }

    public void testTrim() throws Exception {
        String res;
        res = StringUtil.trim("test", 't');
        assertEquals(res, "es");

        res = StringUtil.trim(" test ", 't');
        assertEquals(res, " test ");

        res = StringUtil.trim("best", 't');
        assertEquals(res, "bes");

        res = StringUtil.trim("tesb", 't');
        assertEquals(res, "esb");

        res = StringUtil.trim("", 't');
        assertEquals(res, "");
    }

    public void testGetBooleanValue() throws Exception {
        boolean res;
        res = StringUtil.getBooleanValue("true");
        assertTrue(res);

        res = StringUtil.getBooleanValue("TrUe");
        assertTrue(res);

        res = StringUtil.getBooleanValue("false");
        assertTrue(!res);

        res = StringUtil.getBooleanValue("FalSe");
        assertTrue(!res);

        res = StringUtil.getBooleanValue("");
        assertTrue(!res);
    }

    public void testEqualsIgnoreCase() throws Exception {
        boolean res;
        res = StringUtil.equalsIgnoreCase(null, null);
        assertTrue(res);

        res = StringUtil.equalsIgnoreCase("test", null);
        assertTrue(!res);

        res = StringUtil.equalsIgnoreCase(null, "test");
        assertTrue(!res);

        res = StringUtil.equalsIgnoreCase("test", "test");
        assertTrue(res);

        res = StringUtil.equalsIgnoreCase("TeSt", "test");
        assertTrue(res);

        res = StringUtil.equalsIgnoreCase("TeSt ", "test");
        assertTrue(!res);

        res = StringUtil.equalsIgnoreCase("test", "TeSt");
        assertTrue(res);
    }

    public void testRemoveBackslashes() throws Exception {
        String res;
        
        res = StringUtil.removeBackslashes("Test");
        assertEquals(res, "Test");

        res = StringUtil.removeBackslashes("");
        assertEquals(res, "");

        res = StringUtil.removeBackslashes("\\");
        assertEquals(res, "");

        res = StringUtil.removeBackslashes("Te\\st");
        assertEquals(res, "Test");

        res = StringUtil.removeBackslashes("\\Te\\st");
        assertEquals(res, "Test");

        res = StringUtil.removeBackslashes("\\Te\\st");
        assertEquals(res, "Test");

        res = StringUtil.removeBackslashes("\\Te\\st\\");
        assertEquals(res, "Test");
    }

    public void testRemoveBlanks() throws Exception {
        String res;

        res = StringUtil.removeBlanks("Test");
        assertEquals(res, "Test");

        res = StringUtil.removeBlanks("");
        assertEquals(res, "");

        res = StringUtil.removeBlanks(" ");
        assertEquals(res, "");

        res = StringUtil.removeBlanks("Te st");
        assertEquals(res, "Test");

        res = StringUtil.removeBlanks(" Te st");
        assertEquals(res, "Test");

        res = StringUtil.removeBlanks(" T e st");
        assertEquals(res, "Test");

        res = StringUtil.removeBlanks(" Te st ");
        assertEquals(res, "Test");
    }

    public void testSplit() throws Exception {

        String[] res;
        res = StringUtil.split("val1\nval2\rval3", "\n\r");
        assertEquals(res.length, 3);
        assertEquals(res[0], "val1");
        assertEquals(res[1], "val2");
        assertEquals(res[2], "val3");
        
       
        char[] newline= {(char)0x0A};
        String nl = new String(newline);
        StringBuffer s = new StringBuffer("val1").append(nl)
                .append("val2").append(nl)
                .append("val3").append(nl)
                .append("val4")
                
                ;
        
        res = StringUtil.split(s.toString(),nl);
        
        assertEquals(res.length, 4);
        assertEquals(res[0], "val1");
        assertEquals(res[1], "val2");
        assertEquals(res[2], "val3");
        assertEquals(res[3], "val4");
        
    }    
    
    
    public void testGetVectorFromArray() throws Exception {
        
        String array[] = new String[3];
        
        array[0] = "A";
        array[1] = "B";
        array[2] = "C";
        
        
        Vector v = StringUtil.getVectorFromArray(array);
        
        for (int i = 0; i < 3; i++) {
            String s = (String) v.elementAt(i);
            assertTrue(s.equals(array[i]));
        }
    }

    public void testRemovePortFromUrl() throws Exception {
        String url = "http://my.funambol.com:8080/sync";
        String res = StringUtil.removePortFromUrl(url, "http");
        assertEquals(res, "http://my.funambol.com/sync");

        Log.error("Removed port = "  + res);

        url = "http://my.funambol.com/sync";
        res = StringUtil.removePortFromUrl(url, "http");
        assertEquals(res, "http://my.funambol.com/sync");

        Log.error("Removed port = "  + res);
    }
    
    public void testGetProtocolFromUrl() throws Exception {
        String url = "http://my.funambol.com:8080/sync";
        String res = StringUtil.getProtocolFromUrl(url);
        assertEquals(res, "http");

        url = "https://my.funambol.com/sync";
        res = StringUtil.getProtocolFromUrl(url);
        assertEquals(res, "https");
    }

    public void testEndsWithIgnoreCase() throws Exception {
        String str = "TEST.TEST";
        String suffix = ".TEST";
        assertTrue(StringUtil.endsWithIgnoreCase(str,suffix));

        str = "test.test";
        suffix = ".test";
        assertTrue(StringUtil.endsWithIgnoreCase(str,suffix));

        str = "test.TEST";
        suffix = "test";
        assertTrue(StringUtil.endsWithIgnoreCase(str,suffix));

        str = "test.test";
        suffix = ".TEST";
        assertTrue(StringUtil.endsWithIgnoreCase(str,suffix));

        str = "test.test";
        suffix = ".TesT";
        assertTrue(StringUtil.endsWithIgnoreCase(str,suffix));

        str = "test.test";
        suffix = ".text";
        assertTrue(!StringUtil.endsWithIgnoreCase(str,suffix));

    }
}

