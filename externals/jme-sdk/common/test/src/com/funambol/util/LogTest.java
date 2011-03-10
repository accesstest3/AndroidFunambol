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

package com.funambol.util;

import java.util.Vector;

import junit.framework.*;


/**
 * Testing the StringUtil implementation.
 */
public class LogTest extends TestCase {
    
    /** Creates a new instance of ThreadPoolTest */
    public LogTest(String name) {
        super(name);
    }

    public void testInitLog() throws Exception {
        MemoryAppender memoryAppender = new MemoryAppender();
        Log.initLog(memoryAppender, Log.ERROR);

        Log.error("testInitLog", "msg1");
        Log.info("testInitLog",  "msg2");
        Log.debug("testInitLog", "msg3");
        Log.trace("testInitLog", "msg4");

        String logData = memoryAppender.getLogData();
        String items[] = StringUtil.split(logData, "\n");
        assertTrue(items.length == 3);
        assertTrue("[ERROR] [testInitLog] msg1".equals(items[1]));
    }

    public void testSetLogLevel() throws Exception {
        MemoryAppender memoryAppender = new MemoryAppender();
        Log.initLog(memoryAppender, Log.ERROR);
        Log.setLogLevel(Log.DEBUG);

        Log.error("testInitLog", "msg1");
        Log.info("testInitLog",  "msg2");
        Log.debug("testInitLog", "msg3");
        Log.trace("testInitLog", "msg4");

        String logData = memoryAppender.getLogData();
        String items[] = StringUtil.split(logData, "\n");
        assertTrue(items.length == 5);
        assertTrue("[ERROR] [testInitLog] msg1".equals(items[1]));
        assertTrue("[INFO] [testInitLog] msg2".equals(items[2]));
        assertTrue("[DEBUG] [testInitLog] msg3".equals(items[3]));
    }

    public void testLogErrorContext() throws Exception {
        try {
            MemoryAppender memoryAppender = new MemoryAppender();
            Log.initLog(memoryAppender, Log.ERROR);
            Log.enableContextLogging(true);

            Log.info("testInitLog",  "msg2");
            Log.debug("testInitLog", "msg3");
            Log.trace("testInitLog", "msg4");
            // Nothing so far must have been logged

            String logData = memoryAppender.getLogData();
            String items[] = StringUtil.split(logData, "\n");
            assertTrue(items.length == 2);

            Log.error("testInitLog", "error");

            logData = memoryAppender.getLogData();
            items = StringUtil.split(logData, "\n");
            assertTrue(items.length == 8);
        } finally {
            Log.enableContextLogging(false);
        }
    }

    public void testLogErrorContext2() throws Exception {
        try {
            MemoryAppender memoryAppender = new MemoryAppender();
            memoryAppender.setLimit(2048);
            Log.initLog(memoryAppender, Log.ERROR);
            Log.enableContextLogging(true);

            for(int i=0;i<1030;++i) {
                Log.info("testInitLog",  "msg" + i);
            }
            // Nothing so far must have been logged

            String logData = memoryAppender.getLogData();
            String items[] = StringUtil.split(logData, "\n");
            assertTrue(items.length == 2);

            Log.error("testInitLog", "error");

            logData = memoryAppender.getLogData();
            items = StringUtil.split(logData, "\n");
            assertTrue(items.length == 1024 + 4);
        } finally {
            Log.enableContextLogging(false);
        }
    }

}
    
