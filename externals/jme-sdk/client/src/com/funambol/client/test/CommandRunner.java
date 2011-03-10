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

package com.funambol.client.test;

import com.funambol.util.Log;
import com.funambol.util.StringUtil;


public abstract class CommandRunner {

    private static final String TAG_LOG = "CommandRunner";

    protected Robot robot;

    protected CheckSyncClient checkSyncClient = null;
    protected SyncMonitor     syncMonitor     = null;
    protected SyncMonitor     authSyncMonitor = null;
    
    public CommandRunner(Robot robot) {
        this.robot = robot;
    }
    
    public abstract boolean runCommand(String command, String args) throws Throwable;

    public void setCheckSyncClient(CheckSyncClient client) {
        this.checkSyncClient = client;
    }

    public void setSyncMonitor(SyncMonitor monitor) {
        this.syncMonitor = monitor;
    }

    public void setAuthSyncMonitor(SyncMonitor monitor) {
        this.authSyncMonitor = monitor;
    }

    protected String getParameter(String allPars, int index) {
        // Remove the paranthesis if necessary
        if (allPars.startsWith("(")) {
            allPars = allPars.substring(1);
        }
        if (allPars.endsWith(")")) {
            allPars = allPars.substring(0, allPars.length() - 1);
        }
        String args[] = StringUtil.split(allPars, ",");

        if (index < args.length) {
            String value = args[index];
            value=value.trim();
            if (value.startsWith("\"")) {
                value = value.substring(1);
            }
            if (value.endsWith("\"")) {
                value = value.substring(0, value.length() - 1);
            }

            // Commas are encoded, we must decode them
            // The rrule needs to be encoded because it can contain commas
            value = StringUtil.replaceAll(value, "?-?", ",");
            Log.trace(TAG_LOG, "field after decoding: " + value);
            return value;
        } else {
            return null;
        }
    }

    protected int getParametersCount(String allPars) {
        String args[] = StringUtil.split(allPars, ",");
        return args.length;
    }

    protected void checkArgument(String value, String errorMsg) {
        if (value == null) {
            Log.error(TAG_LOG, "Syntax error in script, invalid argument");
            Log.error(TAG_LOG, errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    protected void checkObject(Object obj, String errorMsg) throws ClientTestException {
        if (obj == null) {
            Log.error(TAG_LOG, "Error in script");
            Log.error(TAG_LOG, errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    protected boolean parseBoolean(String value) {
        boolean v = false;
        value = value.toLowerCase();
        if ("true".equals(value)) {
            v = true;
        }
        return v;
    }
}
