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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import junit.framework.*;

import com.funambol.util.Log;


/**
 * Testcase for OTAConfigParser
 */
public class OTAConfigParserTest extends TestCase {
    /** String for testing URL Configuration*/
    public static final String URL = "http://www.funambol.net";
    /** String for testing User Configuration*/
    public static final String USER = "user";
    /** String for testing Password Configuration*/
    public static final String PASSWORD = "password";
    /** String for testing Visible Name Configuration*/
    public static final String VISIBLENAME = "visibleName";
    /** String for testing Mail Address Configuration*/
    public static final String MAILADDRESS = "mailAddress";
    /** String for testing Remote Mail Source URI*/
    public static final String REMOTEMAILURI = "REMOTEMAILURI";
    /** String for testing Remote Contact Source URI*/
    public static final String REMOTECONTACTURI = "REMOTECONTACTURI";
    /** String for testing Remote Calendar Source URI*/
    public static final String REMOTECALENDARURI = "REMOTECALENDARURI";
    /** String for testing Remote task Source URI*/
    public static final String REMOTETASKURI = "REMOTETASKURI";
    /** String for testing Remote Note Source URI*/
    public static final String REMOTENOTEURI = "REMOTENOTEURI";
    /** String for testing Remote Briefcase Source URI*/
    public static final String REMOTEBRIEFCASEURI = "REMOTEBRIEFCASEURI";
    /** String for testing value for Format*/
    public static final String FORMAT = "FORMAT";
    //This constant won't be valorized in V1
    //public static final String LOCALURI = "LOCALURI";
    
    /** Expected sections to be parsed*/
    private static int EXPECTED_SECTION_NUMBER = 7;
    
    private ByteArrayInputStream bais;
    
    private byte[] msg;
    
    public OTAConfigMessageParser ocmp;
    private OTAConfigMessage message;

    public OTAConfigParserTest(String name) {
        super(name);
    }
    
    /**
     * Configure TestCase Environment
     */
    public void setUp() throws Exception {
        Log.setLogLevel(Log.DEBUG);
        ocmp = new OTAConfigMessageParser();
        initParserTest();
    }

    /**
     * Clear TestCase Environment
     */
    public void tearDown() {
    }
    
    /**
     * Test Configuration value after Message parsing
     */
    public void testParseMessageMailSection() throws Exception {
        boolean result = true;
        String msg = "testParseMessageMailSection - Error parsing: \n";
        if(!message.getSyncUrl().equals(URL)){
            result = false;
            msg += ("URL, found: " + message.getSyncUrl() + " instead of: " + URL + "\n");
        }
        if(!message.getUserName().equals(USER)){
            result = false;
            msg += ("Username, found: " + message.getUserName() + " instead of: " + USER + "\n");
        }
        if(!message.getPassword().equals(PASSWORD)){
            result = false;
            msg += ("Password, found: " + message.getPassword() + " instead of: " + PASSWORD +"\n");
        }
        if(!message.getRemoteUri(message.MAIL).equals(REMOTEMAILURI)){
            result = false;
            msg += ("RemoteMailUri, found: " + message.getRemoteUri(message.MAIL) 
                            + " instead of: " + REMOTEMAILURI+"\n");
        }
        
        assertTrue(msg, result);
    }
    
    /**
     * Test REMOTECONTACTURI value after Message parsing
     */
    public void testParseMessageContactSection() throws Exception {
        boolean result = true;
        String msg = "testParseMessageContactSection - Error parsing: \n";
        if(!message.getRemoteUri(message.CONTACT).equals(REMOTECONTACTURI)){
            result = false;
            msg += ("RemoteContactURI, found: " + message.getRemoteUri(message.CONTACT) 
                            + " instead of: " + REMOTECONTACTURI);
        }
        assertTrue(msg, result);
    }
    
     /**
     * Test REMOTECALENDARURI value after Message parsing
     */
    public void testParseMessageCalendarSection() throws Exception {
        boolean result = true;
        String msg = "testParseMessageCalendarSection - Error parsing: \n";
        
        if(!message.getRemoteUri(message.CALENDAR).equals(REMOTECALENDARURI)){
            result = false;
            msg += ("RemoteCalendarURI, found: " + message.getRemoteUri(message.CALENDAR) 
                            + " instead of: " + REMOTECALENDARURI);
        }
        assertTrue(msg, result);
    }
    
     /**
     * Test REMOTETASKURI value after Message parsing
     */
    public void testParseMessageTaskSection() throws Exception {
        boolean result = true;
        String msg = "testParseMessageTaskSection - Error parsing: \n";
        if(!message.getRemoteUri(message.TASK).equals(REMOTETASKURI)){
            result = false;
            msg += ("TASK, found: " + message.getRemoteUri(message.TASK) 
                            + " instead of: " + REMOTETASKURI);
        }
        assertTrue(msg, result);
    }
    
    /**
     * Test REMOTENOTEURI value after Message parsing
     */
    public void testParseMessageNoteUriSection() throws Exception {
        boolean result = true;
        String msg = "testParseMessageNoteUriSection - Error parsing: \n";
        if(!message.getRemoteUri(message.NOTE).equals(REMOTENOTEURI)){
            result = false;
            msg += ("REMOTENOTEURI, found: " + message.getRemoteUri(message.NOTE) 
                            + " instead of: " + REMOTENOTEURI);
        }
        assertTrue(msg, result);
    }
    
    /**
     * Test REMOTEBRIEFCASEURI value after Message parsing 
     */
    public void testParseMessageBriefcaseSection() throws Exception {
        boolean result = true;
        String msg = "testParseMessageBriefcaseSection - Error parsing: \n";
        if(!message.getRemoteUri(message.BRIEFCASE).equals(REMOTEBRIEFCASEURI)){
            result = false;
            msg += ("REMOTEBRIEFCASEURI, found: " + message.getRemoteUri(message.BRIEFCASE) 
                            + " instead of: " + REMOTEBRIEFCASEURI);
        }
        assertTrue(msg, result);
    }
        
    /**
     * Message builder: builds a message in byte[] format and shows its Hex Form
     * Useful to create URL for incoming SMS
     * @param msg is the message to be built
     */
    public void initParserTest() throws Exception {
        msg = buildMessage();
        //Build complete message
        message = ocmp.parseMessage(msg);
        //Print out a string useful for URL creation
        //String out = bytesToHex(msg, '%');
        //Log.debug(out);
    }
    
    /**
     * Build a complete message
     * @return byte[] message representation
     */
    public byte[] buildMessage() throws IOException {
        ByteArrayOutputStream msgByteArray =  new ByteArrayOutputStream();
        DataOutputStream msgData = new DataOutputStream(msgByteArray);
        // write message header %01%06%03%1F%01%B6
        msgData.write(0x01);
        msgData.write(0x06);
        msgData.write(0x03);
        msgData.write(0x1F);
        msgData.write(0x01);
        msgData.write(0xB6);
        
        // write message body
        msgData.write(buildSyncMLSection());
        msgData.write(buildMailSection());
        msgData.write(buildContactSection());
        msgData.write(buildCalendarSection());
        msgData.write(buildTaskSection());
        msgData.write(buildNoteSection());
        msgData.write(buildBriefcaseSection());
        return msgByteArray.toByteArray();
    }
    
    /**
     * Build SyncML Message Section
     * @return byte[] Section representation
     */
    public byte[] buildSyncMLSection() throws IOException {
        ByteArrayOutputStream msgByteArray =  new ByteArrayOutputStream();
        DataOutputStream msgData = new DataOutputStream(msgByteArray);
        //Section Type
        msgData.writeByte(message.SYNCML);
        //First Field length
        msgData.writeByte(URL.getBytes().length);
        //First Field Value
        msgData.write(URL.getBytes());
        //Second Field length
        msgData.writeByte(USER.getBytes().length);
        //Second Field Value
        msgData.write(USER.getBytes());
        //Third Field legth
        msgData.writeByte(PASSWORD.getBytes().length);
        //Third Field Value
        msgData.write(PASSWORD.getBytes());
        return msgByteArray.toByteArray();
    }

    /**
     * Build Mail Message Section
     * @return byte[] Section representation
     */
    public byte[] buildMailSection() throws IOException {
        ByteArrayOutputStream msgByteArray =  new ByteArrayOutputStream();
        DataOutputStream msgData = new DataOutputStream(msgByteArray);
        //Section Type
        msgData.writeByte(message.MAIL);
        //First Field length
        msgData.writeByte(REMOTEMAILURI.getBytes().length);
        //First Field Value
        msgData.write(REMOTEMAILURI.getBytes());
        //Second Field length
        msgData.writeByte(VISIBLENAME.getBytes().length);
        //Second Field Value
        msgData.write(VISIBLENAME.getBytes());
        //Third Field length
        msgData.writeByte(MAILADDRESS.getBytes().length);
        //Third Field Value
        msgData.write(MAILADDRESS.getBytes());
        
        return msgByteArray.toByteArray();
    }

    /**
     * Build Contact Message Section
     * @return byte[] Section representation
     */
    public byte[] buildContactSection() throws IOException {
        ByteArrayOutputStream msgByteArray =  new ByteArrayOutputStream();
        DataOutputStream msgData = new DataOutputStream(msgByteArray);
        //Section Type
        msgData.writeByte(message.CONTACT);
        //First Field length
        msgData.writeByte(REMOTECONTACTURI.getBytes().length);
        //First Field Value
        msgData.write(REMOTECONTACTURI.getBytes());
        //Second Field length
        msgData.writeByte(FORMAT.getBytes().length);
        //Second Field Value
        msgData.write(FORMAT.getBytes());
        
        return msgByteArray.toByteArray();
    }
    
    /**
     * Build Calendar Message Section
     * @return byte[] Section representation
     */
    public byte[] buildCalendarSection() throws IOException {
        ByteArrayOutputStream msgByteArray =  new ByteArrayOutputStream();
        DataOutputStream msgData = new DataOutputStream(msgByteArray);
        //Section Type
        msgData.writeByte(message.CALENDAR);
        //First Field length
        msgData.writeByte(REMOTECALENDARURI.getBytes().length);
        //First Field Value
        msgData.write(REMOTECALENDARURI.getBytes());
        //Second Field length
        msgData.writeByte(FORMAT.getBytes().length);
        //Second Field Value
        msgData.write(FORMAT.getBytes());
        
        return msgByteArray.toByteArray();
    }
    
    /**
     * Build Task Message Section
     * @return byte[] Section representation
     */
    public byte[] buildTaskSection() throws IOException {
        ByteArrayOutputStream msgByteArray =  new ByteArrayOutputStream();
        DataOutputStream msgData = new DataOutputStream(msgByteArray);
        //Section Type
        msgData.writeByte(message.TASK);
        //First Field length
        msgData.writeByte(REMOTETASKURI.getBytes().length);
        //First Field Value
        msgData.write(REMOTETASKURI.getBytes());
        //Second Field length
        msgData.writeByte(FORMAT.getBytes().length);
        //Second Field Value
        msgData.write(FORMAT.getBytes());
        
        return msgByteArray.toByteArray();
    }
    
    /**
     * Build Note Message Section
     * @return byte[] Section representation
     */
    public byte[] buildNoteSection() throws IOException {
        ByteArrayOutputStream msgByteArray =  new ByteArrayOutputStream();
        DataOutputStream msgData = new DataOutputStream(msgByteArray);
        //Section Type
        msgData.writeByte(message.NOTE);
        //First Field length
        msgData.writeByte(REMOTENOTEURI.getBytes().length);
        //First Field Value
        msgData.write(REMOTENOTEURI.getBytes());
        //OPEN POINT: LocalURI is unuseful for eMail Client Configuration
        //Second Field length
        //msgData.writeByte(LOCALURI.getBytes().length);
        //Second Field Value
        //msgData.write(LOCALURI.getBytes());
        
        return msgByteArray.toByteArray();
    }
    
    /**
     * Build Briefcase Message Section
     * @return byte[] Section representation
     */
    public byte[] buildBriefcaseSection() throws IOException {
        ByteArrayOutputStream msgByteArray =  new ByteArrayOutputStream();
        DataOutputStream msgData = new DataOutputStream(msgByteArray);
        //Section Type
        msgData.writeByte(message.BRIEFCASE);
        //First Field length
        msgData.writeByte(REMOTEBRIEFCASEURI.getBytes().length);
        //First Field Value
        msgData.write(REMOTEBRIEFCASEURI.getBytes());
        //OPEN POINT: LocalURI is unuseful for eMail Client Configuration
        //Second Field length
        //msgData.writeByte(LOCALURI.getBytes().length);
        //Second Field Value
        //msgData.write(LOCALURI.getBytes());
        
        return msgByteArray.toByteArray();
    }
    
    /**
     * Useful method to translate translate a byte array in HEX form
     * @param byte to be translate
     * @param delimitator is the output string separator to be used
     * @return String byte translated 
     */ 
    private static String bytesToHex(byte [] b, char delimitator) {
        StringBuffer buf = new StringBuffer("");
        for (int i=0; i< b.length;i++) {
            if (i != 0) {
                if (delimitator != 0) {
                    buf.append(delimitator);
                }
            }   
            buf.append(byteToHex(b[i]));

        }
        return buf.toString();
    }
    
    /**
     * Translate byte in HEX form
     * @param byte to be translate
     * @return String byte translated 
     */
    private static String byteToHex(byte  b) {
        // Returns hex String representation of byte b
        char hexDigit[] = {
            '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };
        char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
        return new String(array);
    }
}

