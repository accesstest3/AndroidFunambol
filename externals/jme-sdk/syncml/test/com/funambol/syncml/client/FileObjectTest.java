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

package com.funambol.syncml.client;

import java.util.Calendar;
import java.util.Date;
import java.io.ByteArrayInputStream;

import com.funambol.util.ConsoleAppender;
import com.funambol.util.DateUtil;
import com.funambol.util.StringUtil;
import com.funambol.util.Log;

import junit.framework.*;

public class FileObjectTest extends TestCase {

    public FileObjectTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender(), Log.TRACE);
    }

    public void testFormatter1() throws Exception {
        FileObject fo = new FileObject();
        fo.setName("foo.txt");
        Calendar mod = DateUtil.parseDateTime("20070405T103000Z");
        fo.setModified(mod.getTime());
        Calendar cre = DateUtil.parseDateTime("20070405T102000Z");
        fo.setCreated(cre.getTime());
        Calendar acc = DateUtil.parseDateTime("20070405T104000Z");
        fo.setAccessed(acc.getTime());
        fo.setHidden(true);
        fo.setSystem(false);
        fo.setArchived(false);
        fo.setDeleted(false);
        fo.setWritable(false);
        fo.setReadable(true);
        fo.setExecutable(false);
        fo.setSize(100);

        String prologue = fo.formatPrologue();
        String epilogue = fo.formatEpilogue();

        String expectedPrologue = "<FILE>\n" +
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
                                  "<BODY enc=\"base64\">";
        String expectedEpilogue = "</BODY>\n" +
                                   "</FILE>";

        assertTrue(StringUtil.equalsIgnoreCase(expectedPrologue,prologue));
        assertTrue(StringUtil.equalsIgnoreCase(expectedEpilogue,epilogue));
    }

    public void testFormatter2() throws Exception {
        FileObject fo = new FileObject();
        fo.setName("foo.txt");
        Calendar mod = DateUtil.parseDateTime("20070405T103000Z");
        fo.setModified(mod.getTime());
        Calendar cre = DateUtil.parseDateTime("20070405T102000Z");
        fo.setCreated(cre.getTime());
        Calendar acc = DateUtil.parseDateTime("20070405T104000Z");
        fo.setAccessed(acc.getTime());
        fo.setHidden(true);
        fo.setSystem(false);
        fo.setArchived(false);
        fo.setDeleted(false);
        fo.setWritable(false);
        fo.setReadable(true);
        fo.setExecutable(false);
        fo.setSize(100);

        String prologue = fo.formatPrologue(false);
        String epilogue = fo.formatEpilogue(false);

        String expectedPrologue = "<FILE>\n" +
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
                                  "<SIZE>100</SIZE>\n";
        String expectedEpilogue = "</FILE>";

        assertTrue(StringUtil.equalsIgnoreCase(expectedPrologue,prologue));
        assertTrue(StringUtil.equalsIgnoreCase(expectedEpilogue,epilogue));
    }


    public void testPrologue1() throws Exception {
        FileObject fo = new FileObject();

        String prologue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                          "<File>\n" + 
                          "<Name>foo.txt</Name>\n" +
                          "<Modified>20070405T103000Z</Modified>\n" +
                          "<Created>20070405T102000Z</Created>\n" +
                          "<Accessed>20070405T104000Z</Accessed>\n" +
                          "<Attributes>\n" +
                          "<h>true</h>\n" +
                          "<s>false</s>\n" +
                          "<a>false</a>\n" +
                          "<d>false</d>\n" +
                          "<w>false</w>\n" +
                          "<r>true</r>\n" +
                          "<x>false</x>\n" +
                          "</Attributes>\n" +
                          "<size>100</size>\n" +
                          "<body enc=\"base64\">This is the first part";

 
        ByteArrayInputStream bis = new ByteArrayInputStream(prologue.getBytes());
        String body = fo.parsePrologue(bis);

        assertTrue("foo.txt".equals(fo.getName()));
        String mod = DateUtil.formatDateTimeUTC(fo.getModified());
        assertTrue("20070405T103000Z".equals(mod));
        String cre = DateUtil.formatDateTimeUTC(fo.getCreated());
        assertTrue("20070405T102000Z".equals(cre));
        String acc = DateUtil.formatDateTimeUTC(fo.getAccessed());
        assertTrue("20070405T104000Z".equals(acc));
        assertTrue(fo.getHidden());
        assertTrue(!fo.getSystem());
        assertTrue(!fo.getArchived());
        assertTrue(!fo.getDeleted());
        assertTrue(!fo.getWritable());
        assertTrue(fo.getReadable());
        assertTrue(!fo.getExecutable());
        assertTrue(fo.getSize() == 100);
        assertTrue("This is the first part".equals(body));
    }

    public void testPrologue2() throws Exception {
        FileObject fo = new FileObject();

        String prologue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                          "<File>\n" + 
                          "<Name>foo.txt</Name>\n" +
                          "<Modified>20070405T103000Z</Modified>\n" +
                          "<Created>20070405T102000Z</Created>\n" +
                          "<Accessed>20070405T104000Z</Accessed>\n" +
                          "<Attributes>\n" +
                          "<h>true</h><s>false</s>\n" +
                          "<a>false</a> <d>false</d>\n" +
                          "<w>false</w>\n" +
                          "<r>true</r>\n" +
                          "<x>false</x>\n" +
                          "</Attributes><size>100</size>\n" +
                          "<body enc=\"base64\">";

 
        ByteArrayInputStream bis = new ByteArrayInputStream(prologue.getBytes());
        String body = fo.parsePrologue(bis);

        assertTrue("foo.txt".equals(fo.getName()));
        String mod = DateUtil.formatDateTimeUTC(fo.getModified());
        assertTrue("20070405T103000Z".equals(mod));
        String cre = DateUtil.formatDateTimeUTC(fo.getCreated());
        assertTrue("20070405T102000Z".equals(cre));
        String acc = DateUtil.formatDateTimeUTC(fo.getAccessed());
        assertTrue("20070405T104000Z".equals(acc));
        assertTrue(fo.getHidden());
        assertTrue(!fo.getSystem());
        assertTrue(!fo.getArchived());
        assertTrue(!fo.getDeleted());
        assertTrue(!fo.getWritable());
        assertTrue(fo.getReadable());
        assertTrue(!fo.getExecutable());
        assertTrue(fo.getSize() == 100);
        assertTrue(body == null);
    }


    // Test with some invalid inputs
    public void testPrologue3() throws Exception {
        // Case #1
        FileObject fo = new FileObject();
        String prologue = "<File><Name>foo.txt</Name>\n" +
                          "<Modified><Modified><Body>";

        ByteArrayInputStream bis = new ByteArrayInputStream(prologue.getBytes());

        boolean invalid = false;
        try {
            fo.parsePrologue(bis);
        } catch (Exception e) {
            invalid = true;
        }
        assertTrue(invalid);

        // Case #2
        fo = new FileObject();
        prologue = "<File><Name>foo.txt</Name>\n" +
                   "<Modified>20070405T103000Z</Modified>" +
                   "<Created>20070405T102000Z</Created>" +
                   "<Accessed>20070405T104000Z</Accessed>\n" +
                   "<hidden>";

        bis = new ByteArrayInputStream(prologue.getBytes());

        invalid = false;
        try {
            fo.parsePrologue(bis);
        } catch (FileObjectException e) {
            invalid = true;
        }
        assertTrue(invalid);

        // Case #3
        fo = new FileObject();
        prologue = "<File><Name>foo.txt</Name>\n" +
                   "<Modified>20070405T103000Z</Modified>\n" +
                   "<Created>20070405T102000Z</Created>\n" +
                   "<Accessed>20070405T104000Z</Accessed>\n" +
                   "<Attributes></File>";

        bis = new ByteArrayInputStream(prologue.getBytes());

        invalid = false;
        try {
            fo.parsePrologue(bis);
        } catch (FileObjectException e) {
            invalid = true;
        }
        assertTrue(invalid);
    }
}
 
