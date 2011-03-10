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

package com.funambol.syncml.spds;

import java.util.Vector;
import java.util.Enumeration;

import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.protocol.SyncMLStatus;

public class SyncReport {

    private Vector     receivedItems   = new Vector();
    private Vector     sentItems       = new Vector();
    private SyncSource source;
    private int        syncStatus;
    private int        requestedSyncMode;
    private int        alertedSyncMode;
    private String     locUri;
    private String     remoteUri;
    private Throwable  se;

    public SyncReport(SyncSource source) {
        this.source = source;
    }

    public int getReceivedAddNumber() {
        return getItemsNumber(receivedItems, SyncML.TAG_ADD);
    }

    public int getReceivedReplaceNumber() {
        return getItemsNumber(receivedItems, SyncML.TAG_REPLACE);
    }

    public int getReceivedDeleteNumber() {
        return getItemsNumber(receivedItems, SyncML.TAG_DELETE);
    }

    public int getSentAddNumber() {
        return getItemsNumber(sentItems, SyncML.TAG_ADD);
    }

    public int getSentReplaceNumber() {
        return getItemsNumber(sentItems, SyncML.TAG_REPLACE);
    }

    public int getSentDeleteNumber() {
        return getItemsNumber(sentItems, SyncML.TAG_DELETE);
    }

    public Enumeration getReceivedItems() {
        return receivedItems.elements();
    }

    public Enumeration getSentItems() {
        return sentItems.elements();
    }

    /**
     * @return an indication if the session was error free or if
     *         one or more errors were encountered. The value is encoded as a
     *         bit mask according to the value of the STATUS_* constants defined
     *         in SyncListener. If no error was encountered then status has the value
     *         SUCCESS.
     */
    public int getSyncStatus() {
        return syncStatus;
    }

    /**
     * @param status gives an indication if the session was error free or if
     *        one or more errors were encountered. The value is encoded as a
     *        bit mask according to the value of the STATUS_* constants defined
     *        in SyncListener. If no error was encountered then status has the value
     *        SUCCESS.
     */
    public void setSyncStatus(int status) {
        this.syncStatus = status;
    }

    public int getNumberOfReceivedItemsWithError() {
        return getNumberOfItemsWithError(receivedItems);
    }

    public int getNumberOfSentItemsWithError() {
        return getNumberOfItemsWithError(sentItems);
    }

    public int getNumberOfReceivedItems() {
        return getReceivedAddNumber() + getReceivedReplaceNumber() + getReceivedDeleteNumber();
    }

    public int getNumberOfSentItems() {
        return getSentAddNumber() + getSentReplaceNumber() + getSentDeleteNumber();
    }

    public int getRequestedSyncMode() {
        return requestedSyncMode;
    }

    public int getAlertedSyncMode() {
        return alertedSyncMode;
    }

    public String getLocUri() {
        return locUri;
    }

    public String getRemoteUri() {
        return remoteUri;
    }

    
    public String toString() {
        StringBuffer res = new StringBuffer();

        res.append("\n");
        res.append("==================================================================\n");
        res.append("| Syncrhonization report for\n");
        res.append("| Local URI: ").append(locUri).append(" - Remote URI:").append(remoteUri).append("\n");
        res.append("| Requested sync mode: ").append(requestedSyncMode)
           .append(" - Alerted sync mode:").append(alertedSyncMode).append("\n");
        res.append("|-----------------------------------------------------------------\n");
        res.append("| Changes received from server\n");
        res.append("|-----------------------------------------------------------------\n");
        res.append("| Add: ").append(getReceivedAddNumber()).append("\n");
        res.append("| Replace: ").append(getReceivedReplaceNumber()).append("\n");
        res.append("| Delete: ").append(getReceivedDeleteNumber()).append("\n");
        res.append("| Total errors: ").append(getNumberOfReceivedItemsWithError()).append("\n");
        res.append("|-----------------------------------------------------------------\n");
        res.append("| Changes sent to server\n");
        res.append("|-----------------------------------------------------------------\n");
        res.append("| Add: ").append(getSentAddNumber()).append("\n");
        res.append("| Replace: ").append(getSentReplaceNumber()).append("\n");
        res.append("| Delete: ").append(getSentDeleteNumber()).append("\n");
        res.append("| Total errors: ").append(getNumberOfSentItemsWithError()).append("\n");
        res.append("|-----------------------------------------------------------------\n");
        res.append("| Global sync status: ").append(getSyncStatus()).append("\n");
        res.append("==================================================================\n");

        return res.toString();
    }

    public void setSyncException(Throwable exc) {
        se = exc;
    }

    public Throwable getSyncException() {
        return se;
    }


    void addReceivedItem(String key, String cmd, int status, String msgStatus) {
        ItemReport itemReport = new ItemReport(key, cmd, status, msgStatus);
        receivedItems.addElement(itemReport);
    }

    void addSentItem(String key, String cmd, int status, String msgStatus) {
        ItemReport itemReport = new ItemReport(key, cmd, status, msgStatus);
        sentItems.addElement(itemReport);
    }

    public void setRequestedSyncMode(int mode) {
        requestedSyncMode = mode;
    }

    void setAlertedSyncMode(int mode) {
        alertedSyncMode = mode;
    }

    public void setLocUri(String locUri) {
        this.locUri = locUri;
    }

    public void setRemoteUri(String remoteUri) {
        this.remoteUri = remoteUri;
    }

    private int getItemsNumber(Vector items, String cmd) {
        int count = 0;
        for(int i=0;i<items.size();++i) {
            ItemReport ir = (ItemReport)items.elementAt(i);
            if (cmd.equals(ir.getCmd())) {
                count++;
            }
        }
        return count;
    }

    private int getNumberOfItemsWithError(Vector items) {
        int count = 0;
        for(int i=0;i<items.size();++i) {
            ItemReport ir = (ItemReport)items.elementAt(i);
            if (!SyncMLStatus.isSuccess(ir.getStatusCode())) {
                count++;
            }
        }
        return count;
    }
}

