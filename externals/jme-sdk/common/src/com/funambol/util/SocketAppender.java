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
import java.io.OutputStream;
import java.io.IOException;
import java.util.Date;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

/**
 * Default debugger to be used instea of System.out.println(msg);
 */
public class SocketAppender implements Appender {

    private String serverUrl = "socket://localhost:7456";
    private SocketConnection sc;
    private OutputStream os;

    /**
     * Default constructor
     */
    public SocketAppender(String url) {
        System.out.println("Setting URL to: " + url);
        if (url != null) {
           //TODO: fix this using bbhelper
            serverUrl = "socket://" + url ;
            
        }
        sc = null;
        os = null;
    }

    //----------------------------------------------------------- Public Methods
    /**
     * ConsoleAppender writes one message on the standard output
     */
    public void writeLogMessage(String level, String msg) {
        //System.out.print(MailDateFormatter.dateToUTC(new Date()));
        String levelMsg = " [" + level + "] ";
        try {
            if (sc != null && os != null) {
                synchronized (os) {
                    os.write(MailDateFormatter.dateToUTC(new Date()).getBytes());
                    os.write(levelMsg.getBytes());
                    os.write(msg.getBytes());
                    os.write("\r\n".getBytes());
                    os.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ConsoleAppender doesn't implement this method
     */
    public void initLogFile() {
        
        try {
            sc = (SocketConnection) ConnectionManager.getInstance().open(serverUrl);
            sc.setSocketOption(SocketConnection.LINGER, 5);
            os = sc.openOutputStream();
        } catch (Exception e) {
            System.out.println("Cannot open socket at: " + serverUrl);
            e.printStackTrace();
        }
    }

    /**
     * ConsoleAppender doesn't implement this method
     */
    public void openLogFile() {
    }

    /**
     * Perform additional actions needed when setting a new level.
     * SocketAppender doesn't implement this method
     */
    public void setLogLevel(int level) {
    }

    /**
     * Close connection and streams
     */
    public void closeLogFile() {

        try {
            if (os != null) {
                os.close();
            }
            if (sc != null) {
                sc.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * SocketAppender doesn't implement this method
     */
    public void deleteLogFile() {
    }

    public LogContent getLogContent() throws IOException {
        throw new IOException("Cannot get log content");
    }
}
