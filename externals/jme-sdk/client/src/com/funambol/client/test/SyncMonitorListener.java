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

import com.funambol.syncml.spds.SyncReport;
import com.funambol.syncml.spds.SyncListener;
import com.funambol.syncml.spds.BasicSyncListener;
import com.funambol.syncml.protocol.DevInf;

import com.funambol.util.Log;

public class SyncMonitorListener extends BasicSyncListener {

    private static final String TAG_LOG = "SyncMonitorListener";

    protected int receivingPhaseCounter = 0;
    protected int sendingPhaseCounter   = 0;

    protected SyncListener lis;

    protected String interruptOnPhase = null;
    protected int    interruptOnPhaseNumber = -1;
    protected String interruptReason = null;

    public SyncMonitorListener(SyncListener lis) {
        this.lis = lis;
    }

    public void interruptAfterPhase(String phaseName, int num, String reason) {
        Log.trace(TAG_LOG, "interrupt after phase: " + phaseName + "," + num);
        interruptOnPhase = phaseName;
        interruptOnPhaseNumber = num;
        interruptReason  = reason;
    }

    public void startSession() {
        lis.startSession();
        receivingPhaseCounter = 0;
        sendingPhaseCounter    = 0;
    }

    public void itemReceived(Object item) {
        lis.itemReceived(item);
        ++receivingPhaseCounter;
        Log.trace(TAG_LOG, "endReceiving: " + interruptOnPhase);
        Log.trace(TAG_LOG, "receivingPhaseCounter: " + receivingPhaseCounter);
        Log.trace(TAG_LOG, "interruptOnPhaseNumber: " + interruptOnPhaseNumber);
        if ("Receiving".equals(interruptOnPhase) && receivingPhaseCounter == interruptOnPhaseNumber) {
            interruptSync(interruptReason);
        }
    }

    public void itemDeleted(Object item) {
        lis.itemDeleted(item);
    }

    public void itemUpdated(Object item, Object update) {
        lis.itemUpdated(item, update);
    }

    public void itemUpdated(Object item) {
        lis.itemUpdated(item);
    }

    public void itemAddSendingEnded(String key, String parent, int size) {
        ++sendingPhaseCounter;
        if ("Sending".equals(interruptOnPhase) && sendingPhaseCounter == interruptOnPhaseNumber) {
            interruptSync(interruptReason);
        }
        lis.itemAddSendingEnded(key, parent, size);
    }

    public void itemReplaceSendingEnded(String key, String parent, int size) {
        lis.itemReplaceSendingEnded(key, parent, size);
    }

    public void itemDeleteSent(Object item) {
        lis.itemDeleteSent(item);
    }

    public void endSession(SyncReport report) {
        lis.endSession(report);
    }

    public void startConnecting() {
        lis.startConnecting();
    }

    public void endConnecting(int action) {
        lis.endConnecting(action);
    }


    public void syncStarted(int alertCode) {
        lis.syncStarted(alertCode);
    }

    public void endSyncing() {
        lis.endSyncing();
    }

    public void startReceiving(int number) {
        lis.startReceiving(number);
    }

    public void endReceiving() {
        lis.endReceiving();
    }

    public void dataReceived(String date, int size) {
        lis.dataReceived(date, size);
    }

    public void startSending(int numNewItems, int numUpdItems, int numDelItems) {
        lis.startSending(numNewItems, numUpdItems, numDelItems);
    }

    public void itemAddSendingStarted(String key, String parent, int size) {
        lis.itemAddSendingStarted(key, parent, size);
    }

    public void itemAddChunkSent(String key, String parent, int size) {
        lis.itemAddChunkSent(key, parent, size);
    }

    public void itemReplaceSendingStarted(String key, String parent, int size) {
        lis.itemReplaceSendingStarted(key, parent, size);
    }

    public void itemReplaceChunkSent(String key, String parent, int size) {
        lis.itemReplaceChunkSent(key, parent, size);
    }

    public void endSending() {
        lis.endSending();
    }

    public void startMapping() {
        lis.startMapping();
    }

    public void endMapping() {
        lis.endMapping();
    }

    public boolean startSyncing(int alertCode, DevInf devInf) {
        return lis.startSyncing(alertCode, devInf);
    }

    protected void interruptSync(String reason) {
        interruptOnPhase = null;
        interruptOnPhaseNumber = -1;
        interruptReason = null;

        throw new IllegalArgumentException("Simulating sync error " + reason);
    }
}

