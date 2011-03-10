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

import android.util.Log;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Default debugger to be used instea of System.out.println(msg);
 */ 
public class AndroidLogAppender implements Appender {
    
    // ---------------------------------------------------------------------------

    private String tag;

    /** Default constructor */
    public AndroidLogAppender(String tag) {
        this.tag = tag;
    }

    //----------------------------------------------------------- Public Methods
    /**
     * ConsoleAppender writes one message on the standard output
     */
    public void writeLogMessage(String level, String msg) {
        if (level.equals("ERROR")) {
            Log.e(tag, msg);
        } else if (level.equals("INFO")) {
            Log.i(tag, msg);
        } else if (level.equals("DEBUG")) {
            Log.d(tag, msg);
        } else {
            Log.v(tag, msg);
        } 
    }
    
    /**
     * ConsoleAppender doesn't implement this method
     */
    public void initLogFile() {
    }

    /**
     * ConsoleAppender doesn't implement this method
     */
    public void openLogFile() {
    }

    /**
     * ConsoleAppender doesn't implement this method
     */
    public void closeLogFile() {
    }

    /**
     * ConsoleAppender doesn't implement this method
     */
    public void deleteLogFile() {
    }

    public void setLogLevel(int i) {}

    public LogContent getLogContent() throws IOException {
        StringBuffer log = new StringBuffer();
        String commandLine = "logcat -d";

        Process process = Runtime.getRuntime().exec(commandLine);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = bufferedReader.readLine()) != null){
            log.append(line);
            log.append(System.getProperty("line.separator"));
        }
        return new LogContent(LogContent.STRING_CONTENT, log.toString());
    }

}
