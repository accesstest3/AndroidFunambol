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

package com.funambol.syncml.client;

import java.util.Calendar;
import java.util.Date;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.funambol.util.ConsoleAppender;
import com.funambol.util.DateUtil;
import com.funambol.util.Base64;
import com.funambol.util.StringUtil;
import com.funambol.util.Log;

import junit.framework.*;

public class FileObjectOutputStreamTest extends TestCase {

    public FileObjectOutputStreamTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender(), Log.TRACE);
    }

    public void testSmallObject() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileObject fo = new FileObject();
        FileObjectOutputStream fos = new FileObjectOutputStream(fo, bos);

        String body = "Hello world!";

        String item = "<FILE>\n" +
                      "<NAME>foo.txt</NAME>\n" +
                      "<MODIFIED>20070405T103000Z</MODIFIED>\n" +
                      "<CREATED>20070405T102000Z</CREATED>\n" +
                      "<ACCESSED>20070405T104000Z</ACCESSED>\n" +
                      "<ATTRIBUTES>\n" +
                      "<H>TRUE</H>\n" +
                      "<S>FALSE</S>\n" +
                      "<A>FALSE</A>\n" +
                      "<D>FALSE</D>\n" +
                      "<W>FALSE</W>\n" +
                      "<R>TRUE</R>\n" +
                      "<X>FALSE</X>\n" +
                      "</ATTRIBUTES>\n" +
                      "<SIZE>100</SIZE>\n" +
                      "<BODY enc=\"base64\">"  +
                      new String (Base64.encode(body.getBytes())) +
                      "</BODY>\n" +
                      "</FILE>\n";


        byte itemBytes[] = item.getBytes();
        for(int i = 0;i<itemBytes.length;++i) {
            byte b = itemBytes[i];
            fos.write(b);
        }
        fos.close();
        // Now check the content and all properties
        byte decodedBodyBytes[] = bos.toByteArray();
        String decodedBody = new String(decodedBodyBytes);
        assertTrue(body.equals(decodedBody));
        assertTrue("foo.txt".equals(fo.getName()));
    }

    public void testSmallObject2() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileObject fo = new FileObject();
        FileObjectOutputStream fos = new FileObjectOutputStream(fo, bos);

        String body = "Hello world!";

        String item = "<FILE>\n" +
                      "<BODY enc=\"base64\">"  +
                      new String (Base64.encode(body.getBytes())) +
                      "</BODY>\n" +
                      "<NAME>foo.txt</NAME>\n" +
                      "<MODIFIED>20070405T103000Z</MODIFIED>\n" +
                      "<CREATED>20070405T102000Z</CREATED>\n" +
                      "<ACCESSED>20070405T104000Z</ACCESSED>\n" +
                      "<ATTRIBUTES>\n" +
                      "<H>TRUE</H>\n" +
                      "<S>FALSE</S>\n" +
                      "<A>FALSE</A>\n" +
                      "<D>FALSE</D>\n" +
                      "<W>FALSE</W>\n" +
                      "<R>TRUE</R>\n" +
                      "<X>FALSE</X>\n" +
                      "</ATTRIBUTES>\n" +
                      "<SIZE>100</SIZE>\n" +
                      "</FILE>\n";


        byte itemBytes[] = item.getBytes();
        for(int i = 0;i<itemBytes.length;++i) {
            byte b = itemBytes[i];
            fos.write(b);
        }
        fos.close();
        // Now check the content and all properties
        byte decodedBodyBytes[] = bos.toByteArray();
        String decodedBody = new String(decodedBodyBytes);
        assertTrue(body.equals(decodedBody));
        assertTrue("foo.txt".equals(fo.getName()));
    }

    public void testMediumObject() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileObject fo = new FileObject();
        FileObjectOutputStream fos = new FileObjectOutputStream(fo, bos);

        String prologue = "<FILE>\n" +
                          "<NAME>foo.txt</NAME>\n" +
                          "<MODIFIED>20070405T103000Z</MODIFIED>\n" +
                          "<CREATED>20070405T102000Z</CREATED>\n" +
                          "<ACCESSED>20070405T104000Z</ACCESSED>\n" +
                          "<ATTRIBUTES>\n" +
                          "<H>TRUE</H>\n" +
                          "<S>FALSE</S>\n" +
                          "<A>FALSE</A>\n" +
                          "<D>FALSE</D>\n" +
                          "<W>FALSE</W>\n" +
                          "<R>TRUE</R>\n" +
                          "<X>FALSE</X>\n" +
                          "</ATTRIBUTES>\n" +
                          "<SIZE>100</SIZE>\n" +
                          "<BODY>";

        // In this test we want the first 1024 bytes to contain all the body and
        // part of the body closure tag, but not the entire item
        byte bodyBytes[] = new byte[1024 - prologue.length() - 1];
        for(int i=0;i<bodyBytes.length - 1;++i) {
            bodyBytes[i] = 'a';
        }
        bodyBytes[bodyBytes.length - 1] = '<';
        String body = new String(bodyBytes, 0, bodyBytes.length - 1);

        String epilogue = "/BODY>\n" +
                          "</FILE>\n";

        String item = prologue + new String(bodyBytes) + epilogue;

        byte itemBytes[] = item.getBytes();
        for(int i = 0;i<itemBytes.length;++i) {
            byte b = itemBytes[i];
            fos.write(b);
        }
        fos.close();
        // Now check the content and all properties
        byte decodedBodyBytes[] = bos.toByteArray();
        String decodedBody = new String(decodedBodyBytes);
        assertTrue(body.equals(decodedBody));
        assertTrue("foo.txt".equals(fo.getName()));
    }

    public void testMediumObject2() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileObject fo = new FileObject();
        FileObjectOutputStream fos = new FileObjectOutputStream(fo, bos);

        String prologue = "<FILE>\n" +
                          "<BODY>";

        // In this test we want the first 1024 bytes to contain all the body and
        // part of the body closure tag, but not the entire item
        byte bodyBytes[] = new byte[1024 - prologue.length() - 1];
        for(int i=0;i<bodyBytes.length - 1;++i) {
            bodyBytes[i] = 'a';
        }
        bodyBytes[bodyBytes.length - 1] = '<';
        String body = new String(bodyBytes, 0, bodyBytes.length - 1);

        String epilogue = "/BODY>\n" +
                          "<NAME>foo.txt</NAME>\n" +
                          "<MODIFIED>20070405T103000Z</MODIFIED>\n" +
                          "<CREATED>20070405T102000Z</CREATED>\n" +
                          "<ACCESSED>20070405T104000Z</ACCESSED>\n" +
                          "<ATTRIBUTES>\n" +
                          "<H>TRUE</H>\n" +
                          "<S>FALSE</S>\n" +
                          "<A>FALSE</A>\n" +
                          "<D>FALSE</D>\n" +
                          "<W>FALSE</W>\n" +
                          "<R>TRUE</R>\n" +
                          "<X>FALSE</X>\n" +
                          "</ATTRIBUTES>\n" +
                          "<SIZE>100</SIZE>\n" +
                          "</FILE>\n";

        String item = prologue + new String(bodyBytes) + epilogue;

        byte itemBytes[] = item.getBytes();
        for(int i = 0;i<itemBytes.length;++i) {
            byte b = itemBytes[i];
            fos.write(b);
        }
        fos.close();
        // Now check the content and all properties
        byte decodedBodyBytes[] = bos.toByteArray();
        String decodedBody = new String(decodedBodyBytes);
        assertTrue(body.equals(decodedBody));
        assertTrue("foo.txt".equals(fo.getName()));
    }

    public void testBigObject() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileObject fo = new FileObject();
        FileObjectOutputStream fos = new FileObjectOutputStream(fo, bos);

        String prologue = "<FILE>\n" +
                          "<NAME>foo.txt</NAME>\n" +
                          "<MODIFIED>20070405T103000Z</MODIFIED>\n" +
                          "<CREATED>20070405T102000Z</CREATED>\n" +
                          "<ACCESSED>20070405T104000Z</ACCESSED>\n" +
                          "<ATTRIBUTES>\n" +
                          "<H>TRUE</H>\n" +
                          "<S>FALSE</S>\n" +
                          "<A>FALSE</A>\n" +
                          "<D>FALSE</D>\n" +
                          "<W>FALSE</W>\n" +
                          "<R>TRUE</R>\n" +
                          "<X>FALSE</X>\n" +
                          "</ATTRIBUTES>\n" +
                          "<SIZE>100</SIZE>\n" +
                          "<BODY>";

        byte bodyBytes[] = new byte[36 * 1024];
        for(int i=0;i<bodyBytes.length;++i) {
            bodyBytes[i] = 'a';
        }
        String body = new String(bodyBytes);

        String epilogue = "</BODY>\n" +
                          "</FILE>\n";

        String item = prologue + new String(bodyBytes) + epilogue;

        byte itemBytes[] = item.getBytes();
        for(int i = 0;i<itemBytes.length;++i) {
            byte b = itemBytes[i];
            fos.write(b);
        }
        fos.close();
        // Now check the content and all properties
        byte decodedBodyBytes[] = bos.toByteArray();
        String decodedBody = new String(decodedBodyBytes);
        assertTrue(body.equals(decodedBody));
        assertTrue("foo.txt".equals(fo.getName()));
    }

    public void testBigObject2() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileObject fo = new FileObject();
        FileObjectOutputStream fos = new FileObjectOutputStream(fo, bos);

        String prologue = "<FILE>\n" +
                          "<BODY>";

        // In this test we want the first 1024 bytes to contain all the body and
        // part of the body closure tag, but not the entire item
        byte bodyBytes[] = new byte[36 * 1024];
        for(int i=0;i<bodyBytes.length;++i) {
            bodyBytes[i] = 'a';
        }
        String body = new String(bodyBytes);

        String epilogue = "</BODY>\n" +
                          "<NAME>foo.txt</NAME>\n" +
                          "<MODIFIED>20070405T103000Z</MODIFIED>\n" +
                          "<CREATED>20070405T102000Z</CREATED>\n" +
                          "<ACCESSED>20070405T104000Z</ACCESSED>\n" +
                          "<ATTRIBUTES>\n" +
                          "<H>TRUE</H>\n" +
                          "<S>FALSE</S>\n" +
                          "<A>FALSE</A>\n" +
                          "<D>FALSE</D>\n" +
                          "<W>FALSE</W>\n" +
                          "<R>TRUE</R>\n" +
                          "<X>FALSE</X>\n" +
                          "</ATTRIBUTES>\n" +
                          "<SIZE>100</SIZE>\n" +
                          "</FILE>\n";

        String item = prologue + new String(bodyBytes) + epilogue;

        byte itemBytes[] = item.getBytes();
        for(int i = 0;i<itemBytes.length;++i) {
            byte b = itemBytes[i];
            fos.write(b);
        }
        fos.close();
        // Now check the content and all properties
        byte decodedBodyBytes[] = bos.toByteArray();
        String decodedBody = new String(decodedBodyBytes);
        assertTrue(body.equals(decodedBody));
        assertTrue("foo.txt".equals(fo.getName()));
    }
}


