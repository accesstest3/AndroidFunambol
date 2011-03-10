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

import java.io.IOException;
import java.util.Vector;

import com.funambol.util.Appender;

public class MemoryAppender implements Appender {

    Vector  logData = null;

    private int limit   = 250;

    public MemoryAppender() {
        initLogFile();
    }

    public void closeLogFile() {
    }

    public void deleteLogFile() {
        initLogFile();
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void initLogFile() {
        logData = new Vector();
        logData.ensureCapacity(limit + 1);
    }

    public void openLogFile() {
    }

    public void writeLogMessage(String level, String msg) throws IOException {
        StringBuffer message = new StringBuffer();
        message.append("[").append(level).append("] ").append(msg);
        msg = message.toString();
        
        logData.addElement(msg);
        if (logData.size() > limit) {
            logData.removeElementAt(0);
        }
    }
    
    public String getLogData() {
        StringBuffer result = new StringBuffer();
        for (int x = 0; x < logData.size(); x++) {
            result.append(logData.elementAt(x)).append("\n");
        }
        return result.toString();
    }

    /**
     * Perform additional actions needed when setting a new level.
     * MemoryAppender doesn't implement this method
     */
    public void setLogLevel(int level) {
    }

    public LogContent getLogContent() throws IOException {
        return new LogContent(LogContent.STRING_CONTENT, getLogData());
    }

}
