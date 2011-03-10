/**
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

package com.funambol.push;

import com.funambol.util.Log;

/**
 * A SyncSchedulerListener test implementation. It's used to simulate a real
 * sync provider running in a separate thread. 
 */
public class TestSyncSchedulerListener implements SyncSchedulerListener {

    private int fails;

    private Object[] syncedContent;

    private boolean  syncStarted;
    private boolean  syncEnded;
    private boolean  syncFailed;
    private boolean  syncAborted;
    private long     duration;

    // the sync thread
    private Thread t;

    public TestSyncSchedulerListener(long syncDuration) {
        syncStarted = false;
        syncEnded = true;
        duration = syncDuration;
    }

    public void sync(Object[] requestContent) throws IllegalArgumentException {
        
        Log.debug("Starting sync...");
        // throw a sync exception if a sync process is already running
        // doesn't handle the retry
        if(!syncEnded) {
            Log.debug("A sync process is already running.");
            throw new IllegalArgumentException("A sync process is already running");
        }
        syncStarted = true;
        syncEnded  = false;

        syncedContent = requestContent;

        t = new Thread() {
            public void run() {
                try {
                    // Simulate a sync of the specified duration
                    Log.debug("Sync started");
                    sleep(duration);
                    Log.debug("Sync ended");

                    syncEnded = true;
                    syncStarted = false;
                } catch (Exception e) {
                    System.out.println("Exception while simulating sync: " + e);
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }
    /** Getters & setters */
    public boolean getSyncStarted() {
        
        //When the caller is notified that the sync is started we can set it
        //to false in order to handle multiple syncs
        if(syncStarted) {
            syncStarted = false;
            return true;
        }
        return syncStarted;
    }
    public boolean getSyncEnded() {
        return syncEnded;
    }
    public Object[] getSyncedContent() {
        return syncedContent;
    }
    public void setDuration(long aDuration) {
        duration = aDuration;
    }
}
