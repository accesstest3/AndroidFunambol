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

package com.funambol.syncml.protocol;

import com.funambol.util.ChunkedString;
import java.util.Vector;

import com.funambol.util.XmlUtil;
import com.funambol.util.Log;

import com.funambol.util.XmlException;

/**
 * This class is a container for SyncML status command.
 * It can be filled from an incominig SyncML fragment using the static
 * method <code>parse</code>, or can be filled with all the info and then
 * used to format an outgoing Status command.
 */
public class SyncMLStatus {
    
    //----------------------------------------------------------------- Constants
    public static final int SUCCESS                     = 200 ;
    public static final int AUTHENTICATION_ACCEPTED     = 212 ;
    public static final int CHUNKED_ITEM_ACCEPTED       = 213 ;
    public static final int INVALID_CREDENTIALS         = 401 ;
    public static final int FORBIDDEN                   = 403 ;
    public static final int NOT_FOUND                   = 404 ;
    public static final int ALREADY_EXISTS              = 418 ;
    public static final int DEVICE_FULL                 = 420 ;
    public static final int GENERIC_ERROR               = 500 ;
    public static final int SERVER_BUSY                 = 503 ;
    public static final int PROCESSING_ERROR            = 506 ;
    public static final int REFRESH_REQUIRED            = 508 ;
    public static final int BACKEND_AUTH_ERROR          = 511 ;
    
    //-------------------------------------------------------------- Private data
    
    /** The id of this command */
    private String cmdId;
    
    /** The message reference */
    private String msgRef;
    
    /** The command reference */
    private String cmdRef;
    
    /** The command name that this status is acknowledging */
    private String cmd;
    
    /** The source reference of the item to acknowledge */
    private String srcRef;
    
    /** The target reference of the item to acknowledge */
    private String tgtRef;
    
    /** Source references for multiple items status */
    private String[] items;

    /** The authentication challenge type */
    private String chalType;

    /** The authentication challenge format */
    private String chalFormat;

    /** The authentication ahallenge next nonce */
    private String chalNextNonce;
    
    /** The status code for the command */
    private int status;

    /** The message from server in case of error status within Item <Data> Tag. */
    private ChunkedString statusDataMessage;
    
    //------------------------------------------------------------- Constructors
    public SyncMLStatus() {
        this("", "", "", "", null, null, 200);
    }
    
    public SyncMLStatus(String cmdId, String msgref, String cmdref,
            String cmd, String src, String tgt, int status) {
        setCmdId(cmdId);
        setMsgRef(msgRef);
        setCmdRef(cmdref);
        setCmd(cmd);
        setSrcRef(src);
        setTgtRef(tgt);
        
        this.items = null;
        this.status = status;
    }
    
    public SyncMLStatus(String cmdId, String msgref, String cmdref,
            String cmd, String[] items, int status) {
        setCmdId(cmdId);
        setMsgRef(msgRef);
        setCmdRef(cmdref);
        setCmd(cmd);
        this.srcRef = null;
        this.tgtRef = null;
        this.items = items;
        this.status = status;
    }
    
    //----------------------------------------------------------- Public methods
    
    /** Get the command id */
    public String getCmdId() {
        return this.cmdId;
    }
    
    /** Set the command id */
    public void setCmdId(String cmdId) {
        this.cmdId = (cmdId != null) ? cmdId : null;
    }
    
    /** Get the message reference */
    public String getMsgRef() {
        return this.msgRef;
    }
    
    /** Set the message reference */
    public void setMsgRef(String msgRef) {
        this.msgRef = (msgRef != null) ? msgRef : null;
    }
    
    /** Get the command reference */
    public String getCmdRef() {
        return this.cmdRef;
    }
    
    /** Set the command reference */
    public void setCmdRef(String cmdRef) {
        this.cmdRef = (cmdRef != null) ? cmdRef : null;
    }
    
    /** Get the command name */
    public String getCmd() {
        return this.cmd;
    }
    
    /** Set the command name */
    public void setCmd(String cmd) {
        this.cmd = (cmdRef != null) ? cmd : null;
    }
    
    /** Get the source reference */
    public String getSrcRef() {
        return this.srcRef;
    }
    
    /** Set the source reference */
    public void setSrcRef(String srcRef) {
        this.srcRef = (srcRef != null) ? new String(srcRef) : null;
    }
    
    /** Get the target reference */
    public String getTgtRef() {
        return this.tgtRef;
    }
    
    /** Set the target reference */
    public void setTgtRef(String tgtRef) {
        this.tgtRef = (tgtRef != null) ? tgtRef : null;
    }
    
    /**
     * Get target reference if set, or source reference otherwise
     */
    public String getRef() {
        return (tgtRef != null ? tgtRef : srcRef);
    }
    
    /** Get the keys of the items acknowledged by this status. */
    public String[] getItemKeys() {
        return this.items;
    }
    
    /** Set the keys of the items acknowledged by this status. */
    public void setItemKeys(String[] items) {
        this.items = items;
    }
    
    /** Get the status code */
    public int getStatus() {
        return this.status;
    }
    
    /** Set the status code */
    public void setStatus(int status) {
        this.status = status;
    }

    public String getChalType() {
        return this.chalType;
    }

    private void setChalType(String type) {
        this.chalType = type;
    }

    public String getChalFormat() {
        return this.chalFormat;
    }

    private void setChalFormat(String format) {
        this.chalFormat = format;
    }

    public String getChalNextNonce() {
        return this.chalNextNonce;
    }

    private void setChalNextNonce(String nonce) {
        this.chalNextNonce = nonce;
    }

    
    /**
     * Parse a SyncML fragment and return the first status command
     * found, or null in case of error.
     */
    public static SyncMLStatus parse(ChunkedString xmlCommand) {
        //Log.trace("Entering SyncMLStatus.parse()");
        String tag = null;
        
        // Get command Id
        try {
            Vector items = new Vector();
            SyncMLStatus ret = new SyncMLStatus();
            
            ret.setCmdId(XmlUtil.getTagValue(xmlCommand, SyncML.TAG_CMDID).toString());
            ret.setMsgRef(XmlUtil.getTagValue(xmlCommand, SyncML.TAG_MSGREF).toString());
            ret.setCmdRef(XmlUtil.getTagValue(xmlCommand, SyncML.TAG_CMDREF).toString());
            ret.setCmd(XmlUtil.getTagValue(xmlCommand, SyncML.TAG_CMD).toString());
            
            if(XmlUtil.getTag(xmlCommand, SyncML.TAG_TARGETREF) != -1) {
                tag = XmlUtil.getTagValue(xmlCommand, SyncML.TAG_TARGETREF).toString();
                ret.setTgtRef(tag);
            }
            if(XmlUtil.getTag(xmlCommand, SyncML.TAG_SOURCEREF) != -1) {
                tag=XmlUtil.getTagValue(xmlCommand, SyncML.TAG_SOURCEREF).toString();
                ret.setSrcRef(tag);
            }
            // Save the LocURI of the items acknowledged by the server
            // if present.
            if(XmlUtil.getTag(xmlCommand, SyncML.TAG_ITEM) != -1) {
                Vector tags = XmlUtil.getTagValues(xmlCommand, SyncML.TAG_ITEM);
                
                int tagsSize = tags.size();
                for(int i=0; i<tagsSize; i++) {
                    ChunkedString t = (ChunkedString)tags.elementAt(i);
                    if(XmlUtil.getTag(t, SyncML.TAG_LOC_URI) != -1) {
                        tag=XmlUtil.getTagValue(t, SyncML.TAG_LOC_URI).toString();
                        items.addElement(tag);
                    }
                    // if there is only one item, look for Data tag for
                    //       the detailed message.
                    if(XmlUtil.getTag(t, SyncML.TAG_DATA) != -1) {
                        ret.setStatusDataMessage(XmlUtil.getTagValue(t, SyncML.TAG_DATA));
                    }
                }
            }

            // Parse Challenge information
            if(XmlUtil.getTag(xmlCommand, SyncML.TAG_CHAL) != -1) {

                ChunkedString chalValue = XmlUtil.getTagValue(xmlCommand, SyncML.TAG_CHAL);
                if(XmlUtil.getTag(chalValue, SyncML.TAG_META) != -1) {

                    ChunkedString chalMetaValue = XmlUtil.getTagValue(xmlCommand, SyncML.TAG_CHAL);
                    if(XmlUtil.getTag(chalMetaValue, SyncML.TAG_TYPE) != -1) {
                        ret.setChalType(XmlUtil.getTagValue(chalValue,
                                SyncML.TAG_TYPE).toString());
                    }
                    if(XmlUtil.getTag(chalMetaValue, SyncML.TAG_FORMAT) != -1) {
                        ret.setChalFormat(XmlUtil.getTagValue(chalValue,
                                SyncML.TAG_FORMAT).toString());
                    }
                    if(XmlUtil.getTag(chalMetaValue, SyncML.TAG_NEXT_NONCE) != -1) {
                        ret.setChalNextNonce(XmlUtil.getTagValue(chalValue,
                                SyncML.TAG_NEXT_NONCE).toString());
                    }
                }
            }

            String status = XmlUtil.getTagValue(xmlCommand, SyncML.TAG_DATA).toString();
            ret.setStatus( Integer.parseInt(status) );
            
            // If there is some item, set the items array
            int size = items.size();
            
            if( size > 0 ) {
                String[] itemarray = new String[size];
                
                for (int i=0; i<size; i++) {
                    itemarray[i] = (String)items.elementAt(i);
                }
                ret.setItemKeys(itemarray);
            }

            
            return ret;
        } catch (XmlException e){
            e.printStackTrace();
            Log.error("[status.parse] Error parsing server status " + xmlCommand);
        }
        
        return null;
    }
    
    /**
     * Return true is the status code of this instance is the range 200-299.
     */
    public boolean isSuccess() {
        return isSuccess(this.status);
    }
    
    /**
     * Return true is the given status code is in the range 200-299.
     */
    public static boolean isSuccess(int status) {
        return ((status >= 200  && status < 300) || status == ALREADY_EXISTS);
    }

    public void setStatusDataMessage(ChunkedString statusDataMessage) {
        this.statusDataMessage = statusDataMessage;
    }
    
    public String getStatusDataMessage() {
        if (this.statusDataMessage==null) {
            return "";
        }
        return this.statusDataMessage.toString();
    }
}

