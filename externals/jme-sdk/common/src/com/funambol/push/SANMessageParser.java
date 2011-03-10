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

import com.funambol.util.Log;


/**
 * Message Parser for messages carring a server
 * alerted notification (SAN)
 */
public class SANMessageParser {
    
    
    private String digest = null;
    
    private String version = null;
    
    private char uiMode = '\0';
    
    private char initiator = '\0';
    
    private int sessionId = 0;
    
    private String serverId = null;
    
    private int numberOfSync = 0;
    
    private SyncInfo[] syncInfo = null;
    
    // -------------------------------------------------------------- Constants
    private static final int DIGEST_SIZE = 128;
    
    private static final int VERSION_SIZE = 10;
    
    private static final int UI_MODE_SIZE = 2;
    
    private static final int INTITIATOR_SIZE = 1;
    
    private static final int FUTURE_USE1_SIZE = 27;
    
    private static final int SESSION_ID_SIZE = 16;
    
    private static final int SERVER_IDENTIFIER_SIZE = 8;
    
    private static final int NUM_SYNCS_SIZE = 4;
    
    private static final int FUTURE_USE2_SIZE = 4;
    
    private static final int SYNC_TYPE_SIZE = 4;
    
    private static final int FUTURE_USE3_SIZE = 4;
    
    private static final int CONTENT_TYPE_SIZE = 24;
    
    private static final int SERVER_URI_SIZE = 8;
    
    private final int ERR_NOT_AUTHENTICATION = 1;
    
    private final int ERR_NOT_PARSER = 2;
    
    private SANMessage message;
    
    /**
     * Creates a new instance of SANMessageParser
     */
    public SANMessageParser() {
        Log.info(this, "SANMessageParser for SAN Message");
        message = new SANMessage();
    }
    
    /**
     * This method parses a byte stream and builds a SANMessage.
     *
     * @param msg is the raw message
     * @param skipDigest this flag specifies if the first 6 bytes must be
     * skipped or not, depending on the message that the server sends. This is
     * temporary and shall be fixed when the server behavior is consinstent in
     * all cases
     *
     * @return SAN Message object handling properties of SAN OTA Message
     */
    public SANMessage parseMessage(byte[] msg, boolean skipDigest)
                                               throws MessageParserException {
         /*
          * To be used with real messages to cut headers
          **/

        int byteToBeParsed;
        byte[] msgToBeParsed;

        if (skipDigest) {
            byteToBeParsed = msg.length-6;
            msgToBeParsed = new byte[byteToBeParsed];
            System.arraycopy(msg, 6, msgToBeParsed, 0, byteToBeParsed);
        } else {
            byteToBeParsed = msg.length;
            msgToBeParsed = new byte[byteToBeParsed];
            System.arraycopy(msg, 0, msgToBeParsed, 0, byteToBeParsed);
        }
        
        try {
            String binaryMessage = "";
            StringBuffer sb = new StringBuffer();
            String temp = "";
            for (int i = 0; i < msgToBeParsed.length; i++) {
                byte c = msgToBeParsed[i];// Get next character
                temp = Integer.toString((c & 0xff) + 0x100, 2 /* radix */).substring(1);
                sb.append(temp);
            }
            binaryMessage = sb.toString();
            
            // actual processing and fill ups
            int arrayPointer = 0;
            digest = binaryMessage.substring(arrayPointer, arrayPointer + DIGEST_SIZE);
            
            message.setDigest(digest);
            
            arrayPointer += DIGEST_SIZE;
            
            // version
            version = binaryMessage.substring(arrayPointer, arrayPointer + VERSION_SIZE); //e.g. 00001100
            int ver = Integer.parseInt(version, 2);//e.g. 12
            version = Integer.toString(ver);
            String first = version.substring(0, 1);//e.g. 1
            String second = version.substring(1);//e.g. 2
            version = first + "." + second;//e.g. 1.2
            message.setVersion(version);
            
            arrayPointer += VERSION_SIZE;
            
            // ui mode;
            String uimode = binaryMessage.substring(arrayPointer, arrayPointer + UI_MODE_SIZE);
            int ui_mode = Integer.parseInt(uimode, 2);
            uiMode = Integer.toString(ui_mode).charAt(0);
            message.setUiMode(uiMode);
            
            arrayPointer += UI_MODE_SIZE;
            
            // initiator
            String initiator_s = binaryMessage.substring(arrayPointer, arrayPointer + INTITIATOR_SIZE);
            int initiator_i = Integer.parseInt(initiator_s, 2);
            initiator = Integer.toString(initiator_i).charAt(0);
            message.setInitiator(initiator);
            
            arrayPointer += INTITIATOR_SIZE;
            
            // future-use
            arrayPointer += FUTURE_USE1_SIZE;
            
            // session-id
            String ses_id = binaryMessage.substring(arrayPointer, arrayPointer + SESSION_ID_SIZE);
            sessionId = Integer.parseInt(ses_id, 2);
            message.setSessionId(sessionId);
            
            arrayPointer += SESSION_ID_SIZE;
            
            // server-identifier-len
            String servidlen = binaryMessage.substring(arrayPointer, arrayPointer + SERVER_IDENTIFIER_SIZE);
            int serverid_len = Integer.parseInt(servidlen, 2);
            arrayPointer += SERVER_IDENTIFIER_SIZE;
            // server-identifier
            // process each-byte
            for (int k = 0; k < serverid_len; k++) {
                temp = binaryMessage.substring(arrayPointer, arrayPointer + 8);
                int oneByte = Integer.parseInt(temp, 2);
                serverId += (char)oneByte;
                arrayPointer += 8;
            }
            message.setServerId(serverId);
            
            // no of syncs
            numberOfSync = Integer.parseInt(binaryMessage.substring(arrayPointer, arrayPointer + NUM_SYNCS_SIZE), 2);
            message.setNumberOfSync(numberOfSync);
            
            arrayPointer += NUM_SYNCS_SIZE;
            
            // future use
            arrayPointer += FUTURE_USE2_SIZE;
            
            // loop now no of syncs time(s)
            syncInfo = new SyncInfo[numberOfSync];
            for (int p = 0; p < numberOfSync; p++) {
                syncInfo[p] = new SyncInfo();
                int syncType = Integer.parseInt(binaryMessage.substring(arrayPointer,
                        arrayPointer + SYNC_TYPE_SIZE), 2);
                syncType += 200;
                syncInfo[p].setSyncType(syncType);
                arrayPointer += SYNC_TYPE_SIZE;
                // future use
                arrayPointer += FUTURE_USE3_SIZE;
                // content type
                int contentTypei;
                String contentType = "";
                for (int o = 0; o < CONTENT_TYPE_SIZE / 8; o++) {
                    contentTypei =
                            Integer.parseInt(binaryMessage.substring(arrayPointer,
                            arrayPointer + 8), 2);
                    contentType = Integer.toString(contentTypei);
                    arrayPointer += 8;
                }
                syncInfo[p].setContentType(contentType);
                
                // server uri-length
                int serverurl_len =
                        Integer.parseInt(binaryMessage.substring(arrayPointer,
                        arrayPointer + SERVER_URI_SIZE), 2);
                arrayPointer += SERVER_URI_SIZE;
                // server uri
                String serverUri = "";
                for (int k = 0; k < serverurl_len; k++) {
                    String byteData =
                            binaryMessage.substring(arrayPointer, arrayPointer + 8);
                    int oneByte = Integer.parseInt(byteData, 2);
                    serverUri += (char)oneByte;
                    arrayPointer += 8;
                }
                syncInfo[p].setServerUri(serverUri);
            }
            message.setSyncInfos(syncInfo);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error("Exception in SANMessageParser:" + e.toString());
            throw new MessageParserException(e.getMessage());
        }
        return message;
    }
    
    private void setSyncInfoArray(int arrayPointer, final String binaryMessage) throws NumberFormatException {
        
        // loop now no of syncs time(s)
        syncInfo = new SyncInfo[numberOfSync];
        for (int p = 0; p < numberOfSync; p++) {
            syncInfo[p] = new SyncInfo();
            int syncType = Integer.parseInt(binaryMessage.substring(arrayPointer,
                    arrayPointer + SYNC_TYPE_SIZE), 2);
            syncType += 200;
            syncInfo[p].setSyncType(syncType);
            
            arrayPointer += SYNC_TYPE_SIZE;
            // future use
            arrayPointer += FUTURE_USE3_SIZE;
            
            // content type
            int contentTypei;
            String contentType = "";
            for (int o = 0; o < CONTENT_TYPE_SIZE / 8; o++) {
                contentTypei = Integer.parseInt(binaryMessage.substring(arrayPointer,
                        arrayPointer + 8), 2);
                contentType = Integer.toString(contentTypei);
                arrayPointer += 8;
            }
            syncInfo[p].setContentType(contentType);
            
            // server uri-length
            int serverurl_len = Integer.parseInt(binaryMessage.substring(arrayPointer,
                    arrayPointer + SERVER_URI_SIZE), 2);
            
            arrayPointer += SERVER_URI_SIZE;
            
            // server uri
            String serverUri = "";
            for (int k = 0; k < serverurl_len; k++) {
                String byteData = binaryMessage.substring(arrayPointer, arrayPointer + 8);
                int oneByte = Integer.parseInt(byteData, 2);
                serverUri += (char)oneByte;
                arrayPointer += 8;
            }
            syncInfo[p].setServerUri(serverUri);
        }
    }
    
    private int setServerId(String temp, int arrayPointer, final String binaryMessage) throws NumberFormatException {
        int serverid_len = getServerIdLen(arrayPointer, binaryMessage);
        
        arrayPointer += SERVER_IDENTIFIER_SIZE;
        
        // server-identifier
        // process each-byte
        for (int k = 0; k < serverid_len; k++) {
            temp = binaryMessage.substring(arrayPointer, arrayPointer + 8);
            int oneByte = Integer.parseInt(temp, 2);
            serverId += (char)oneByte;
            arrayPointer += 8;
        }
        message.setServerId(serverId);
        //Log.debug("ServerId: " + serverId);
        return arrayPointer;
    }
    
    private int getServerIdLen(final int arrayPointer, final String binaryMessage) throws NumberFormatException {
        
        // server-identifier-len
        String servidlen = binaryMessage.substring(arrayPointer, arrayPointer
                + SERVER_IDENTIFIER_SIZE);
        int serverid_len = Integer.parseInt(servidlen, 2);
        return serverid_len;
    }
    
    private void setSessionId(final String binaryMessage, final int arrayPointer) throws NumberFormatException {
        
        // session-id
        String ses_id = binaryMessage.substring(arrayPointer, arrayPointer + SESSION_ID_SIZE);
        sessionId = Integer.parseInt(ses_id, 2);
        message.setSessionId(sessionId);
        //Log.debug("SessionId: " + sessionId);
    }
    
    private void setInitiator(final int arrayPointer, final String binaryMessage) throws NumberFormatException {
        
        // initiator
        String initiator_s = binaryMessage.substring(arrayPointer, arrayPointer
                + INTITIATOR_SIZE);
        int initiator_i = Integer.parseInt(initiator_s, 2);
        initiator = Integer.toString(initiator_i).charAt(0);
        message.setInitiator(initiator);
        //Log.debug("Initiator: " + initiator);
    }
    
    private void setUiMode(final int arrayPointer, final String binaryMessage) throws NumberFormatException {
        
        // ui mode;
        String uimode = binaryMessage.substring(arrayPointer, arrayPointer
                + UI_MODE_SIZE);
        int ui_mode = Integer.parseInt(uimode, 2);
        uiMode = Integer.toString(ui_mode).charAt(0);
        message.setUiMode(uiMode);
        //Log.debug("UiMode: " + uiMode);
    }
    
    private void setVersion(final int arrayPointer, final String binaryMessage) throws NumberFormatException {
        
        // version
        version = binaryMessage.substring(arrayPointer, arrayPointer
                + VERSION_SIZE); //e.g. 00001100
        int ver = Integer.parseInt(version, 2);//e.g. 12
        version = Integer.toString(ver);
        String first = version.substring(0, 1);//e.g. 1
        String second = version.substring(1);//e.g. 2
        version = first + "." + second;//e.g. 1.2
        message.setVersion(version);
        //Log.debug("Version: " + version);
    }
    
    public String getDigest() {
        return this.digest;
    }
    
    public String getVersion() {
        return this.version;
    }
    
    public char getUiMode() {
        return this.uiMode;
    }
    
    public int getInititator() {
        return this.initiator;
    }
    
    public int getSessionId() {
        return this.sessionId;
    }
    
    public String getServerId() {
        return this.serverId;
    }
    public int getNumberOfSync() {
        return this.numberOfSync;
    }
    
    public SyncInfo[] getSyncInfoArray() {
        return this.syncInfo;
    }
    
}
