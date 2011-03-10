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

package com.funambol.syncml.client;

import com.funambol.util.Log;


/**
 * Class handling properties of an OTA Configuration Message
 * coming from the server
 */
public class OTAConfigMessage {
    // Definitions for the section types sent by the server
    /** The SyncML section id */
    public static final int SYNCML      = 1;
    /** The MAIL section id */
    public static final int MAIL        = 2;
    /** The CONTACT section id */
    public static final int CONTACT     = 3;
    /** The CALENDAR section id */
    public static final int CALENDAR    = 4;
    /** The TASK section id */
    public static final int TASK        = 5;
    /** The NOTE section id */
    public static final int NOTE        = 6;
    /** The BRIEFCASE section id */
    public static final int BRIEFCASE   = 7;
    
    // Number of configurable sources
    public static final int SOURCE_NUMBER = 7;
    
    private String syncUrl;
    private String userName;
    private String password;
    
    private String visibleName;
    private String mailAddress;
    
    private String[] remoteURIs = new String[SOURCE_NUMBER];
    
    /** Creates a new instance of OTAConfigMessage */
    public OTAConfigMessage() {
   }
    
    /** Creates a new instance of OTAConfigMessage */
    public OTAConfigMessage(String userName, String password, String syncUrl,
            String visibleName, String mailAddress, String[] remoteURIs) {
        this.userName = userName;
        this.password = password;
        this.syncUrl = syncUrl;
        this.visibleName = visibleName;
        this.mailAddress = mailAddress;
        this.remoteURIs = remoteURIs;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getSyncUrl() {
        return syncUrl;
    }
    
    public void setSyncUrl(String syncUrl) {
        this.syncUrl = syncUrl;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getVisibleName() {
        return visibleName;
    }
    
    public void setVisibleName(String visibleName) {
        this.visibleName = visibleName;
    }
    
    public String getMailAddress() {
        return mailAddress;
    }
    
    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }
    
    public String[] getRemoteURIs() {
        return remoteURIs;
    }
    
    public void setRemoteURIs(String[] remoteURIs) {
        this.remoteURIs = remoteURIs;
    }
    
    /**
     * Returns the remote URI for the specified source
     *
     * @param sourceId one of the sourceId defined in this class
     */
    public String getRemoteUri(int sourceId) {
        if (sourceId < MAIL || sourceId > SOURCE_NUMBER) {
            Log.error("getRemoteUri: invalid sourceId "+sourceId);
            throw new IllegalArgumentException("sourceId not valid: "+sourceId);
        }
        return remoteURIs[sourceId-2];
    }
}
