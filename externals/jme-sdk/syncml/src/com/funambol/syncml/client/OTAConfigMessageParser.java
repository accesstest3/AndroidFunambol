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
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;



/**
 * Message Listener for messages carring configuration messages (Config)
 */
public class OTAConfigMessageParser {
    
    // Definitions for the section types sent by the server
    OTAConfigMessage message = new OTAConfigMessage();

    private String[] remoteURIs;
    // Number of configurable sources
        
    /**
     * Creates a new instance of OTAConfigMessageParser
     */
    public OTAConfigMessageParser() {
        Log.info(this, "OTAConfigMessageParser for OTA Configuration Binary Message");
        remoteURIs = new String[OTAConfigMessage.SOURCE_NUMBER];
    }
    
    /**
     * This is the main parse method. It reads the incoming message
     * and store the informations in the instance attributes.
     * @return OTAConfigMessage Object handling properties from 
     * OTA Configuration message
     */
    public OTAConfigMessage parseMessage(byte[] msg) throws IOException {
        /*
         * To be used with real messages to cut headers
         **/
        int byteToBeParsed = msg.length-6;
        byte[] msgToBeParsed = new byte[byteToBeParsed];
        System.arraycopy(msg, 6, msgToBeParsed, 0, byteToBeParsed);
        
        //parse message and write configuration parameters
        ByteArrayInputStream bin = new ByteArrayInputStream(msgToBeParsed);
        
        //ByteArrayInputStream bin = new ByteArrayInputStream(msg);
        DataInputStream din = new DataInputStream(bin);
        int sections = 0;
        
        while (din.available() != 0) {
            parseSection(din);
            sections++;
        }
        message.setRemoteURIs(this.remoteURIs);
        return message;
    }
    
    /**
     * Parse Message Sections.
     * Sets the instance attributes accordingly.
     */
    private int parseSection(DataInputStream din) throws IOException {
        String localUri = null;
        int type = getSectionType(din);

        String remoteUri;
        String format;
        
        switch(type) {
            case OTAConfigMessage.SYNCML:
                message.setSyncUrl(getSectionField(din));
                message.setUserName(getSectionField(din));
                message.setPassword(getSectionField(din));
                break;
            case OTAConfigMessage.MAIL:
                remoteURIs[OTAConfigMessage.MAIL-2] = getSectionField(din);
                message.setVisibleName(getSectionField(din));
                message.setMailAddress(getSectionField(din));
                break;
            case OTAConfigMessage.CONTACT:
                remoteURIs[OTAConfigMessage.CONTACT-2] = getSectionField(din);
                format = getSectionField(din);      // Not used on j2me clients
                break;
            case OTAConfigMessage.CALENDAR:
                remoteURIs[OTAConfigMessage.CALENDAR-2] = getSectionField(din);
                format = getSectionField(din);      // Not used on j2me clients
                break;
            case OTAConfigMessage.TASK:
                remoteURIs[OTAConfigMessage.TASK-2] = getSectionField(din);
                format = getSectionField(din);      // Not used on j2me clients
                break;
            case OTAConfigMessage.NOTE:
                remoteURIs[OTAConfigMessage.NOTE-2] = getSectionField(din);
                //Open Point: localUri may not be assigned by server
                //localUri = getSectionField(din);
                break;
            case OTAConfigMessage.BRIEFCASE:
                remoteURIs[OTAConfigMessage.BRIEFCASE-2] = getSectionField(din);
                //Open Point: localUri may not be assigned by server
                //localUri = getSectionField(din);
                break;
            default:
                throw new IOException("Cannot read incoming message");
        }
        return type;
    }
    
    /**
     * Retrieve Section Type.
     * @return  Section Type as an int
     */
    private int getSectionType(DataInputStream din) throws IOException {
        int type = din.readByte()&0xff;
        return type;
    }
    
    /**
     * Retrieve Field value.
     * @return  Field value as a String
     */
    private String getSectionField(DataInputStream din) throws IOException{
        //Get field Length
        int length = din.readByte()&0xff;
        
        if (length == 0) {
            return null;
        }
        
        byte[] tmp = new byte[length];
        din.read(tmp, 0, length);
        
        return new String(tmp);
    }


    
    
    
}
