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

package com.funambol.platform;

import java.io.PrintStream;
import java.io.InputStream;
import java.util.Date;

import com.funambol.util.Log;
import com.funambol.util.ConsoleAppender;

import junit.framework.*;


/**
 * Testing the MD5 implementation.
 */
public class FileAdapterTest extends TestCase {
    
    private FileAdapter fa = null; 
    private String directory;
    private String testFileName;

    public FileAdapterTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.DEBUG);
        directory = System.getProperty("java.io.tmpdir");
        if (directory == null) {
            directory = "file:///root1";
        }
        testFileName = directory + "/localfile.txt";
    }
    
    public void testConstructor() throws Exception {
        fa = new FileAdapter(testFileName);
        //TODO: add checks
    }

    public void testClose() throws Exception {
        fa = new FileAdapter(testFileName);
        fa.close();
    }

    /**
     * Create a new file, check existence, delete it and check the existence again.
     */
    public void testCreateExistsDelete() throws Exception {

        // First of all delete the file if it exists
        fa = new FileAdapter(testFileName);
        if (fa.exists()) {
            fa.delete();
        }
        assertTrue(!fa.exists());

        fa = new FileAdapter(testFileName);
        fa.create();
        assertTrue(fa.exists());
        fa.delete();
        assertFalse(fa.exists());
        fa.close();
    }

    public void testOpenOutputStream() throws Exception {
        fa = new FileAdapter(testFileName);
        PrintStream out = new PrintStream(fa.openOutputStream());
        assertTrue(fa.exists());
        out.println("This is a test.");
        out.close();
        fa.close();
    }

    public void testGetSize() throws Exception {
        fa = new FileAdapter(testFileName);
        long size = fa.getSize();
        Log.debug("Size: " + size);
        assertTrue("This is a test.\n".length() == fa.getSize());
    }

    public void testOpenInputStream() throws Exception {
        fa = new FileAdapter(testFileName);
        InputStream in = fa.openInputStream();

        byte[] content = new byte[(int)fa.getSize()];
        in.read(content);
        in.close();
        fa.close();

        String text = new String(content);
        assertTrue(text.equals("This is a test.\n"));
    }

    public void testMkdir() throws Exception {
        FileAdapter dir = new FileAdapter(directory + "/test-dir");
        if (dir.exists()) {
            dir.delete();
        }
        dir.mkdir();
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
        dir.delete();
    }

    public void testGetName() throws Exception {
        FileAdapter file = new FileAdapter(testFileName);
        if (!file.exists()) {
            file.create();
        }
        String name = file.getName();
        assertTrue("localfile.txt".equals(name));
    }

    public void testModified() throws Exception {
        FileAdapter file = new FileAdapter(testFileName);
        if (file.isSetLastModifiedSupported()) {
            if (!file.exists()) {
                file.create();
            }
            file.setLastModified(2000);
            assertTrue(file.lastModified() == 2000);
        }
    }

}

