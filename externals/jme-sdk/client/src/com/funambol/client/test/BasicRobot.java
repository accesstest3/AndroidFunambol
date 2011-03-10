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

import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.spds.SyncReport;
import com.funambol.util.StringUtil;
import com.funambol.util.Log;

import com.funambol.syncml.spds.SyncSource;

public abstract class BasicRobot extends Robot {
   
    private static final String TAG_LOG = "BasicRobot";

    public void waitForSyncToComplete(int minStart, int max,
            SyncMonitor syncMonitor) throws Throwable {
        
        Log.debug(TAG_LOG, "waiting for sync to complete");

        // We wait no more than minStart for sync client to start
        while(!syncMonitor.isSyncing()) {
            Thread.sleep(WAIT_DELAY);
            minStart -= WAIT_DELAY;
            if (minStart < 0) {
                throw new ClientTestException("Sync did not start within time limit");
            }
        }

        // Now wait until the busy is in progress for a max amount of time
        while(syncMonitor.isSyncing()) {
            Thread.sleep(WAIT_DELAY);
            max -= WAIT_DELAY;
            if (max < 0) {
                throw new ClientTestException("Sync did not complete before timeout");
            }
        }
    }

    public void interruptSyncAfterPhase(String phase, int num, String reason, SyncMonitor syncMonitor) throws Throwable {
        Log.debug(TAG_LOG, "Preparing to interrupt after phase " + phase + "," + num);
        syncMonitor.interruptSyncAfterPhase(phase, num, reason);
    }

    public void checkLastSyncRequestedSyncMode(String source, int mode,
            SyncMonitor syncMonitor) throws Throwable {
        Log.debug(TAG_LOG, "check last sync requested sync mode");

        SyncReport sr = syncMonitor.getSyncReport(source);
        assertTrue(sr != null, "source has no report associated");
        assertTrue(sr.getRequestedSyncMode() == mode, "Requested sync mode mismatch");
    }

    public void checkLastSyncAlertedSyncMode(String source, int mode,
            SyncMonitor syncMonitor) throws Throwable {
        Log.debug(TAG_LOG, "check last sync alerted sync mode");

        SyncReport sr = syncMonitor.getSyncReport(source);
        assertTrue(sr != null, "source has no report associated");
        assertTrue(sr.getAlertedSyncMode() == mode, "Alerted sync mode mismatch");
    }

    public void checkLastSyncRemoteUri(String source, String uri,
            SyncMonitor syncMonitor) throws Throwable {
        Log.debug(TAG_LOG, "check last sync remote URI");

        SyncReport sr = syncMonitor.getSyncReport(source);
        assertTrue(sr != null, "source has no report associated");
        assertTrue(sr.getRemoteUri(), uri, "Requested remote URI mismatch");
    }

    public void checkLastSyncExchangedData(String source,
            int sentAdd, int sentReplace, int sentDelete,
            int receivedAdd, int receivedReplace, int receivedDelete,
            SyncMonitor syncMonitor) throws Throwable
    {
        Log.debug(TAG_LOG, "check last sync exchanged data");

        SyncReport sr = syncMonitor.getSyncReport(source);
        assertTrue(sr != null, "source has no report associated");

        assertTrue(receivedAdd, sr.getReceivedAddNumber(),
                "Received add mismatch");
        assertTrue(receivedReplace, sr.getReceivedReplaceNumber(),
                "Received replace mismatch");
        assertTrue(receivedDelete, sr.getReceivedDeleteNumber(),
                "Received delete mismatch");
        assertTrue(sentAdd, sr.getSentAddNumber(),
                "Sent add mismatch");
        assertTrue(sentReplace, sr.getSentReplaceNumber(),
                "Sent replace mismatch");
        assertTrue(sentDelete, sr.getSentDeleteNumber(),
                "Sent delete mismatch");
    }

    public void resetSourceAnchor(String sourceName) throws Throwable {
        Log.debug(TAG_LOG, "resetting source anchor");

        SyncSource source = getSyncSource(sourceName);
        source.getConfig().setLastAnchor(0);
        source.getConfig().setNextAnchor(0);
        saveSourceConfig(source);
    }

    public void refreshServer(String source, CheckSyncClient client) throws Throwable {

        Log.trace(TAG_LOG, "refreshServer " + source + "," + client);

        reapplySyncConfig(client);

        if(StringUtil.isNullOrEmpty(source)) {
            client.sync();
        } else {
            client.sync(source);
        } 
    }

    public void checkItemsCount(String sourceName, int count) throws Throwable {

        SyncSource source = getSyncSource(sourceName);

        source.beginSync(SyncML.ALERT_CODE_SLOW); // Resets the tracker status
        int itemsCount = 0;
        SyncItem item = source.getNextItem();
        while(item != null) {
            itemsCount++;
            source.setItemStatus(item.getKey(), 200); // Restore the item status
            item = source.getNextItem();
        }
        assertTrue(count, itemsCount, "Items count mismatch for source: " + sourceName);
    }

    public void checkItemsCountOnServer(String sourceName,
            CheckSyncClient client, int count) throws Throwable {
        assertTrue(count, client.getItemsCount(sourceName),
                "Server items count mismatch for source: " + sourceName);
    }

    public abstract void waitForAuthToComplete(int minStart, int max, SyncMonitor syncMonitor) throws Throwable;

    public abstract void keyPress(String keyName, int count) throws Throwable;
    public abstract void writeString(String text) throws Throwable;

    protected abstract SyncSource getSyncSource(String sourceName) throws Exception;
    protected abstract void saveSourceConfig(SyncSource source);

    protected abstract void reapplySyncConfig(CheckSyncClient client) throws Throwable;
    
}
