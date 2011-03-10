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

package com.funambol.push;



/**
 * Class handling properties of a Server Alert Notification
 * Message coming from the server
 */
public class SANMessage {
    
    private String digest;
    
    private String version;
    
    private char uiMode;
    
    private char initiator;
    
    private int sessionId;
    
    private String serverId;
    
    private int numberOfSync;
    
    private SyncInfo[] syncInfo;
    
    public SANMessage() {
    }
    
    /** Creates a new instance of SANMessage */
    public SANMessage(String digest, String version, char uiMode, char initiator,
            int sessionId, String serverId, int numberOfSync, SyncInfo[] syncInfo) {
        this.digest = digest;
        this.version = version;
        this.uiMode = uiMode;
        this.initiator = initiator;
        this.sessionId = sessionId;
        this.serverId = serverId;
        this.numberOfSync = numberOfSync;
        this.syncInfo = syncInfo;
    }
    
    public String getDigest() {
        return digest;
    }
    
    public void setDigest(String digest) {
        this.digest = digest;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public char getUiMode() {
        return uiMode;
    }
    
    public void setUiMode(char uiMode) {
        this.uiMode = uiMode;
    }
    
    public char getInitiator() {
        return initiator;
    }
    
    public void setInitiator(char initiator) {
        this.initiator = initiator;
    }
    
    public int getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getServerId() {
        return serverId;
    }
    
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
    
    public int getNumberOfSync() {
        return numberOfSync;
    }
    
    public void setNumberOfSync(int numberOfSync) {
        this.numberOfSync = numberOfSync;
    }
    
    public SyncInfo[] getSyncInfos() {
        return syncInfo;
    }
    
    public void setSyncInfos(SyncInfo[] syncInfo) {
        this.syncInfo = syncInfo;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("SANMessage:\n Digest:" + this.digest + "\n");
        sb.append("version: " + this.version + "\n");
        sb.append("uiMode: " + this.uiMode + "\n");
        sb.append("initiator: " + this.initiator + "\n");
        sb.append("sessionId: " + this.sessionId + "\n");
        sb.append("serverId: " + this.serverId + "\n");
        sb.append("numberOfSync: " + this.numberOfSync + "\n");
        sb.append("syncInfo; " + this.syncInfo + "\n");
        return sb.toString();
    }
}
