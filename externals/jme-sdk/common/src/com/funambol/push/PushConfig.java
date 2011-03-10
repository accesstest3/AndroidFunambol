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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This class represents the system push subsystem configuration. The push
 * subsystem is responsible for notifying the client on events that require a
 * synchronization.
 * As of today the JAM supports two push mechanisms:
 *
 * 1) OTA via SMS
 * 2) CTP via socket connection
 *
 * At the moment the configuration is mainly for CTP, but it is intended to
 * incorporate also OTA and STP parameters.
 * The configuration is described in the CTP design document, and this
 * implementation is like that.
 *
 */
public class PushConfig {

    /** Bitmask describing which push are enabled */
    private int     push         = 0;

    /** Polling interval (seconds) */
    private int     polling      = 0;

    /** Should requests be queued */
    private boolean queuePush    = false;

    /** Device id. This must be consistent with the SyncClientConfig */
    private String  deviceId     = "";

    private int ctpRetry         = 60;

    /** Maximum total retry time (secs) to set up the connection */
    private int ctpMaxRetry      = 1200;

    /** IO operations timeout (secs) */
    private int ctpCmdTimeout    = 60;

    private int ctpConnTimeout   = 0;

    private int ctpReady         = 5 * 60;  //5 minutes
    
    
    /** CTP nonce */
    private byte[] ctpNonce      = new byte[0];

    private int ctpNotifyTimeout = 120;

    /** CTP server name */
    private String ctpServer     = "";

    /** CTP server port */
    private int ctpPort          = 4745;

    /** CTP username */
    private String ctpUsername   = "";

    /** CTP password */
    private String ctpPassword   = "";

    public PushConfig() {
    }

    /**
     * Serialize this object to the given output stream.
     *
     * @param dout the data output stream
     */
    public void serialize(DataOutputStream dout) throws IOException {

        // Write global information
        dout.writeInt(push);
        dout.writeInt(polling);
        dout.writeBoolean(queuePush);
        dout.writeUTF(deviceId);

        // Write CTP config
        dout.writeInt(ctpRetry);
        dout.writeInt(ctpMaxRetry);
        dout.writeInt(ctpCmdTimeout);
        dout.writeInt(ctpConnTimeout);
        dout.writeInt(ctpPort);
        dout.writeInt(ctpReady);
     
        // Write the CTP nonce
        dout.writeInt(ctpNonce.length);
        dout.write(ctpNonce, 0, ctpNonce.length);

        dout.writeInt(ctpNotifyTimeout);

        dout.writeUTF(ctpUsername);
        dout.writeUTF(ctpPassword);
    }

    /**
     * Deserialize this object from the given input stream
     *
     * @param in the data input stream
     */
    public void deserialize(DataInputStream in) throws IOException {

        // Read global information
        push             = in.readInt();
        polling          = in.readInt();
        queuePush        = in.readBoolean();
        deviceId         = in.readUTF();

        // Read CTP config
        ctpRetry         = in.readInt();
        ctpMaxRetry      = in.readInt();
        ctpCmdTimeout    = in.readInt();
        ctpConnTimeout   = in.readInt();
        ctpPort          = in.readInt();
        ctpReady         = in.readInt();

        // Read the CTP nonce
        int ctpNonceLen  = in.readInt();
        ctpNonce = new byte[ctpNonceLen];
        in.read(ctpNonce, 0, ctpNonceLen);

        ctpNotifyTimeout = in.readInt();

        ctpUsername      = in.readUTF();
        ctpPassword      = in.readUTF();
    }
    
    /**
     * Returns the queuePush parameter
     */
    public boolean getQueuePush() {
        return queuePush;
    }

    /**
     * Set the queuePush parameter
     */
    public void setQueuePush(boolean v) {
        queuePush = v;
    }
    
    /**
     * Returns the ctp retry parameter
     */
    public int getCtpRetry() {
        return ctpRetry;
    }

    /**
     * Set the ctp retry parameter
     */
    public void setCtpRetry(int v) {
        ctpRetry = v;
    }
    
    /**
     * Returns the ctp max retry parameter
     */
    public int getCtpMaxRetry() {
        return ctpMaxRetry;
    }

    /**
     * Set the ctp max retry parameter
     */
    public void setCtpMaxRetry(int v) {
        ctpMaxRetry = v;
    }
    
    /**
     * Returns the ctp command timeout parameter
     */
    public int getCtpCmdTimeout() {
        return ctpCmdTimeout;
    }

    /**
     * Sets the ctp command timeout parameter
     */
    public void setCtpCmdTimeout(int v) {
        ctpCmdTimeout = v;
    }
    
    /**
     * Returns the ctp connection timeout parameter
     */
    public int getCtpConnTimeout() {
        return ctpConnTimeout;
    }

    /**
     * Sets the ctp connection timeout parameter
     */
    public void setCtpConnTimeout(int v) {
        ctpConnTimeout = v;
    }
    
    /**
     * Returns the ctp server port
     */
    public int getCtpPort() {
        return ctpPort;
    }

    /**
     * Sets the ctp server port
     */
    public void setCtpPort(int v) {
        ctpPort = v;
    }
    
    /**
     * Returns the ctp ready period
     */
    public int getCtpReady() {
        return ctpReady;
    }

    /**
     * Sets the ctp ready period
     */
    public void setCtpReady(int v) {
        ctpReady = v;
    }
    
    /**
     * Returns the ctp nonce
     */
    public byte[] getCtpNonce() {
        return ctpNonce;
    }

    /**
     * Sets the ctp nonce
     */
    public void setCtpNonce(byte[] ctpNonce) {
        this.ctpNonce = ctpNonce;
    }

    /**
     * Returns the ctp notify timeout
     */
    public int getCtpNotifyTimeout() {
        return ctpNotifyTimeout;
    }

    /**
     * Sets the ctp notify timeout
     */
    public void setCtpNotifyTimeout(int v) {
        ctpNotifyTimeout = v;
    }

    /**
     * Returns the ctp user name
     */
    public String getCtpUsername() {
        return ctpUsername;
    }

    /**
     * Sets the ctp user name
     */
    public void setCtpUsername(String username) {
        ctpUsername = username;
    }

    /**
     * Returns the ctp password
     */
    public String getCtpPassword() {
        return ctpPassword;
    }

    /**
     * Sets the ctp password
     */
    public void setCtpPassword(String password) {
        ctpPassword = password;
    }
    
    /**
     * Returns the device id
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the device id
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Returns the CTP server name
     */
    public String getCtpServer() {
        return ctpServer;
    }

    /**
     * Sets the CTP server name
     */
    public void setCtpServer(String server) {
        ctpServer = server;
    }
    
    /**
     * Returns a string containing the config details
     */
    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append("CTP Server: " + getCtpServer() + "\n");
        out.append("CTP Port: " + getCtpPort() + "\n");
        out.append("CTP Username: " + getCtpUsername() + "\n");
        out.append("CTP Notify Timeout: " + getCtpNotifyTimeout() + "\n");
        out.append("CTP Cmd Timeout: " + getCtpCmdTimeout() + "\n");
        out.append("CTP HB Timeout: " + getCtpReady() + "\n");
        return out.toString();
        
    }
}

