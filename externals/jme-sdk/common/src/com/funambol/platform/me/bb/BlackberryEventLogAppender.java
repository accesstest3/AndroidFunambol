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

import java.io.IOException;
import net.rim.device.api.system.EventLogger;

import com.funambol.util.Appender;
import com.funambol.util.LogContent;

public class BlackberryEventLogAppender implements Appender {

    private final static long guid = 0x948b87e2ee064ebeL;
    
    private String appName;
    private String appVersion;

    public BlackberryEventLogAppender(String appName, String appVersion) {
        this.appName = appName;
        this.appVersion = appVersion;
    }

    public void closeLogFile() {

    }

    public void deleteLogFile() {
    }

    public void initLogFile() {

        try {
            EventLogger.register(guid, appName + " " + appVersion,
                    EventLogger.VIEWER_STRING);
        } catch (final Throwable t) {
            System.out.println(">>> Exception by registering the event logger:\n"
                    + ">>> Error message: " + t.getMessage() + "\n" + ">>> Short description: "
                    + t.toString() + "\n");
            t.printStackTrace();
        }
    }

    public void openLogFile() {
    }

    public void setLogLevel(int level) {
    }

    public void writeLogMessage(String level, String msg) throws IOException {
        StringBuffer message = new StringBuffer();
        message.append("[").append(level).append("] ").append(msg);
        msg = message.toString();

        try {
            EventLogger.logEvent(guid, msg.getBytes());
        } catch (final Throwable tt) {
            System.out.println(">>> Exception by registering the event logger:\n"
                    + ">>> Error message: " + tt.getMessage() + "\n"
                    + ">>> Short description: " + tt.toString() + "\n");
            tt.printStackTrace();
        }
    }

    public LogContent getLogContent() throws IOException {
        throw new IOException("Cannot get log content for BlackBerryEventLogAppender");
    }

}
