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

package com.funambol.syncml.spds;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.util.Log;

/**
 * Configuration data for the SyncManager: account, polling time etc.
 *
 */
public class SyncConfig {

    //---------------------------------------------------------------- Constants

    public static final String NAME = "SyncConfig" ;

    protected static final int VERSION_600 = 600;
    protected static final int VERSION_850 = 850;
    
    /** This field contains the version of the configuration data */
    protected static final int VERSION = VERSION_850 ;
    
    //--------------------------------------------------------------- Attributes

    /** Parameter to store the configuration version */
    private long version ;
    
    /** Account Connection params */
    public String syncUrl;
    public String userName;
    public String password;

    public String userAgent;

    public boolean forceCookies;

    /** Devinfo */
    public DeviceConfig deviceConfig;

    /** to contain the value of lastSyncUrl from SyncmlMPIConfig */
    public String lastServerUrl;
    
    /** Communications with server compressed or not */
    public boolean compress;

    /** The nonce used for client authentication */
    public String clientNonce = null;

    /** The list of supported authentication types */
    public String[] supportedAuthTypes = { SyncML.AUTH_TYPE_BASIC,
                                           SyncML.AUTH_TYPE_MD5 };

    public String preferredAuthType = SyncML.AUTH_TYPE_BASIC;

    /**
     * Creates a new instance of SyncConfig:
     * Sets default configuration values 
     */
    public SyncConfig() {

        version = VERSION;
        
        syncUrl = "http://<host>:<port>/funambol/ds";
        userName = "guest";
        password = "guest";
        userAgent = null;

        // Use default device config
        deviceConfig = new DeviceConfig();
        
        lastServerUrl = syncUrl;     
        compress = true;
    }

    /** 
     * @return the configuration version
     */
    public long getVersion() {
        return version;
    }

    /**
     * Write object fields to the output stream.
     * @param out Output stream
     * @throws IOException
     */
    public void serialize(DataOutputStream out) throws IOException {
        
        out.writeInt(VERSION);

        out.writeUTF(syncUrl);
        out.writeUTF(userName);
        out.writeUTF(password);

        out.writeUTF(lastServerUrl);

        out.writeUTF(clientNonce);
    }

    /**
     * Read object field from the input stream.
     * @param in Input stream
     * @throws IOException
     */
    public void deserialize(DataInputStream in) throws IOException {
        int savedVer = in.readInt();
        if (savedVer != VERSION) {
            if(savedVer < VERSION_850) {
                // Nothing to do
            } else {
                Log.error("Config version mismatch: use default.");
                return;
            }
        }

        version = savedVer;
        syncUrl = in.readUTF();
        userName = in.readUTF();
        password = in.readUTF();

        // Use default device config (FIXME)
        deviceConfig = new DeviceConfig();
        
        lastServerUrl = in.readUTF();

        if(savedVer >= VERSION_850) {
            clientNonce = in.readUTF();
        }
    }

    public boolean allowBasicAuthentication() {
        for(int i=0;i<supportedAuthTypes.length;++i) {

            if (SyncML.AUTH_TYPE_BASIC.equals(supportedAuthTypes[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean allowMD5Authentication() {
        for(int i=0;i<supportedAuthTypes.length;++i) {

            if (SyncML.AUTH_TYPE_MD5.equals(supportedAuthTypes[i])) {
                return true;
            }
        }
        return false;
    }

    public String getSyncUrl() {
        return syncUrl;
    }

    public void setSyncUrl(String syncUrl) {
        this.syncUrl = syncUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DeviceConfig getDeviceConfig() {
        return deviceConfig;
    }

}

