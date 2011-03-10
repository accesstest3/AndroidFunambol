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

import java.io.InputStream;
import com.funambol.platform.FileAdapter;
import junit.framework.*;
import com.funambol.util.*;
import java.io.OutputStream;

/**
 * Test Class for File Log
 */
public class FileAppenderTest extends TestCase {

    //---------------------------------------------------------------- Constants
    FileAppender appender;

    private String userDir   = null;
    private String filename  = "synclog.txt";

    //------------------------------------------------------------- Constructors
    /**
     * New instance of LogTest class
     */
    public FileAppenderTest(String name) {
        super(name);
    }

    public void setUp() {
        try {
            userDir = Platform.getInstance().getLogFileDir();
            initLogFile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void tearDown() {
    }

    //----------------------------------------------------------- Public Methods
    /**
     * test a single write
     */
    public void testWriteLog() throws  Exception {
        int levels[] = new int[4];
        levels[0] = Log.ERROR;
        levels[1] = Log.INFO;
        levels[2] = Log.DEBUG;
        levels[3] = Log.TRACE;

        String[] expected = new String[levels.length];

        String msg = null;
        for(int i=0;i<levels.length;++i) {

            Log.setLogLevel(levels[i]);
            msg = "LOG MESSAGE level: " + levels[i];
            expected[i] = msg;
            switch (levels[i]) {
                case Log.ERROR:
                    Log.error(msg);
                    break;
                case Log.INFO:
                    Log.info(msg);
                    break;
                case Log.DEBUG:
                    Log.debug(msg);
                    break;
                case Log.TRACE:
                    Log.trace(msg);
                    break;
                default:
                    break;
            }
        }

        LogContent lc = Log.getCurrentLogContent();
        FileAdapter fa = new FileAdapter(lc.getContent());
        InputStream is = fa.openInputStream();

        byte[] buffer = new byte[4096];

        StringBuffer log = new StringBuffer();

        int length = 0;

        do {
            length = is.read(buffer);
            log.append(new String(buffer));
        } while(length > 0);

        for (int i = 0; i < expected.length; i++) {
            System.out.println(expected[i]);
            assertTrue(log.toString().indexOf(expected[i]) > 0);
        }
    }

    public void testGetLogContent() throws Exception {
        LogContent lc = Log.getCurrentLogContent();
        assertTrue(lc.getContent().equals(userDir+"allsynclog.txt"));
        assertTrue(lc.getContentType() == LogContent.FILE_CONTENT);
    }

    /**
     * Create Log File RecordStore
     */
    private void initLogFile() throws Exception {

        appender = new FileAppender(userDir, filename);

        Log.initLog(appender);
    }
}

