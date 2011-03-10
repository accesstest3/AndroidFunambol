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

import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Enumeration;

import com.funambol.syncml.protocol.*;

/**
 * This class represents a formatter for SyncML. A formatter is intented to
 * generate SyncML messages starting from an abstract representation of the same
 * message. In this current implementation the formatter is not yet connected to
 * the abstract implementation and it performs a sort of low level formatting
 * where the bit and pieces of a message are printed together to form the
 * outgoing message.
 * The current implementation supports only plain XML but the class aims at
 * supporting WBXML at some point.
 */
public class SyncMLFormatter {

    public SyncMLFormatter() {
    }

    /**
     * Prepare a SyncML Message header.
     *
     * @param sessionid the session id to use.
     * @param msgid the message id to use.
     * @param src the source uri
     * @param username the username to be used as loc name in the source tag
     * @param tgt the target uri
     * @param tags other SyncML tags to insert in the header.
     *             (e.g. &lt;Cred&gt; or &lt;Meta&gt;).
     */
    public String formatSyncHeader(String sessionid,
                                   String msgid,
                                   String src,
                                   String username,
                                   String tgt,
                                   String tags) {

        StringBuffer ret = new StringBuffer();

        ret.append("<SyncHdr>\n").append("<VerDTD>1.2</VerDTD>\n")
           .append("<VerProto>SyncML/1.2</VerProto>\n").append("<SessionID>")
           .append(sessionid).append("</SessionID>\n").append("<MsgID>")
           .append(msgid).append("</MsgID>\n")
           .append("<Target>")
           .append("<LocURI><![CDATA[").append(tgt).append("]]></LocURI>")
           .append("</Target>\n")
           .append("<Source>")
           .append("<LocURI>").append(src).append("</LocURI>")
           .append("<LocName>").append(username).append("</LocName>")
           .append("</Source>\n");

        if (tags != null) {
            ret.append(tags);
        }
        ret.append("</SyncHdr>\n");
        return ret.toString();
    }

    /**
     * Prepare a status code in response to the server's sync hdr command
     * @param cmdId the command id
     * @param msgIdRef the message id ref
     * @param deviceId the target ref
     * @param serverUrl the source ref
     * @return the SyncML status
     */
    public String formatSyncHdrStatus(SyncMLStatus status) {

        StringBuffer fStatus = new StringBuffer();

        fStatus.append("<Status>\n")
               .append("<CmdID>").append(status.getCmdId()).append("</CmdID>\n")
               .append("<MsgRef>").append(status.getMsgRef()).append("</MsgRef>\n")
               .append("<CmdRef>").append(status.getCmdRef()).append("</CmdRef>\n")
               .append("<Cmd>").append(status.getCmd()).append("</Cmd>\n")
               .append("<TargetRef>").append(status.getTgtRef()).append("</TargetRef>\n")
               .append("<SourceRef>").append(status.getSrcRef()).append("</SourceRef>\n")
               .append("<Data>").append(status.getStatus()).append("</Data>\n")
               .append("</Status>\n");

        return fStatus.toString();
    }

    /**
     * Prepare a status code in response to the server's alert.
     * @param cmdId the command id
     * @param msgIdRef the message id ref
     * @param deviceId the target ref
     * @param serverUrl the source ref
     * @return the SyncML status
     */
    public String formatAlertStatus(SyncMLStatus status, String nextAnchor) {

        StringBuffer fStatus = new StringBuffer();
        fStatus.append("<Status>\n")
               .append("<CmdID>").append(status.getCmdId()).append("</CmdID>\n")
               .append("<MsgRef>").append(status.getMsgRef()).append("</MsgRef>")
               .append("<CmdRef>").append(status.getCmdRef()).append("</CmdRef>")
               .append("<Cmd>Alert</Cmd>\n")
               .append("<TargetRef>").append(status.getTgtRef()).append("</TargetRef>\n")
               .append("<SourceRef>").append(status.getSrcRef()).append("</SourceRef>\n")
               .append("<Data>").append(status.getStatus()).append("</Data>\n")
               .append("<Item>\n").append("<Data>\n")
               .append("<Anchor xmlns=\"syncml:metinf\">")
               .append("<Next>").append(nextAnchor).append("</Next>")
               .append("</Anchor>\n")
               .append("</Data>\n").append("</Item>\n").append("</Status>\n");

        return fStatus.toString();
    }

    /**
     * Contructs the alerts for the given source.
     * @param src SyncSource
     * @param syncMode
     * @return the SyncML Alert commands
     */
    public String formatAlerts(String cmdId, int syncMode, long nextAnchor,
                               long lastAnchor, int sourceSyncMode,
                               String sourceName,
                               String sourceUri,
                               SyncFilter filter,
                               int maxDataSize) {

        StringBuffer sb = new StringBuffer();

        // XXX CHECK IT OUT XXX
        // the Last overwrite the Next?????????????????
        String timestamp = "<Next>" + nextAnchor + "</Next>\n";

        if (lastAnchor != 0l) {
            timestamp = "<Last>" + lastAnchor + "</Last>\n" + timestamp;
        }

        sb.append("<Alert>\n");
        sb.append("<CmdID>" + cmdId + "</CmdID>\n");
        sb.append("<Data>");

        // First, use the syncMode passed as argument,
        // if not valid, use the default for the source
        // as last chance, check the anchor.
        if (syncMode != 0) {
            sb.append(syncMode);
        } else if (sourceSyncMode != 0) {
            sb.append(SyncML.ALERT_CODE_SLOW);
        } else if (lastAnchor != 0) {
            sb.append(SyncML.ALERT_CODE_FAST);
        } else {
            sb.append(sourceSyncMode);
        }

        sb.append("</Data>\n");
        sb.append("<Item>\n");
        sb.append("<Target><LocURI>");
        sb.append(sourceUri);
        sb.append("</LocURI>\n");
        // Apply source filter with a default limit to maxMsgSize.
        // TODO: change it to maxObjSize when the Large Object will be
        // implemented.
        if (filter != null) {
            sb.append(filter.toSyncML(maxDataSize));
        }
        sb.append("</Target>\n");
        sb.append("<Source><LocURI>");
        sb.append(sourceName);
        sb.append("</LocURI></Source>\n");
        sb.append("<Meta>\n");
        sb.append("<Anchor xmlns=\"syncml:metinf\">\n");
        sb.append(timestamp);
        sb.append("</Anchor>\n");
        sb.append("</Meta>\n");
        sb.append("</Item>\n");
        sb.append("</Alert>");
        sb.append("\n");

        return sb.toString();
    }

    /**
     * Format credentials into a &lt;Cred&gt; tag
     * @param token the authentication token
     * @param md5 true if the credentials are md5
     * @return the XML credential tag
     */
    public String formatCredentials(String token, boolean md5) {
        // Add <Cred> and <Meta> to the syncHdr
        StringBuffer cred = new StringBuffer("<Cred>\n");
        cred.append("<Meta>");
        if (md5) {
            cred.append("<Type xmlns=\"syncml:metinf\">").append(SyncML.AUTH_TYPE_MD5).append("</Type>\n");
        } else {
            cred.append("<Type xmlns=\"syncml:metinf\">").append(SyncML.AUTH_TYPE_BASIC).append("</Type>\n");
            cred.append("<Format xmlns=\"syncml:metinf\">b64</Format>\n");
        }
        cred.append("</Meta>\n")
            .append("<Data>").append(token).append("</Data>")
            .append("</Cred>\n");
        return cred.toString();
    }

    /**
     * Format the max message size
     * @param maxMsgSize the max message size
     * @return a Meta tag containing the max message size
     */
    public String formatMaxMsgSize(int maxMsgSize) {
        StringBuffer ret = new StringBuffer();
        ret.append("<Meta><MaxMsgSize>").append(maxMsgSize).append("</MaxMsgSize></Meta>\n");
        return ret.toString();
    }

    /**
     * Format the syncml start tag
     * @return the SyncML start tag
     */
    public String formatStartSyncML() {
        return "<SyncML>\n";
    }

    /**
     * Format the syncml start sync body
     * @return the SyncML start sync body
     */
    public String formatStartSyncBody() {
        return "<SyncBody>\n";
    }

    /**
     * Format the syncml final tag
     * @return the SyncML final tag
     */
    public String formatFinal() {
        return "<Final/>";
    }

    /**
     * Format the syncml end sync body
     * @return the SyncML end sync body
     */
    public String formatEndSyncBody() {
        return "</SyncBody>\n";
    }

   /**
    * Format the syncml end tag
    * @return the SyncML end tag
    */
   public String formatEndSyncML() {
        return "</SyncML>\n";
    }

    /**
     * Constructs the <Put> section of a SyncML initialization message used to
     * carry the device capabilities with the <DevInf> element
     *
     * @param devInf
     *            A reference to the current device configuration (<code>DeviceConfig</code>)
     * @return a String to be added to the initialization SyncML message
     */
    public String formatPutDeviceInfo(String cmdId, DeviceConfig devInf, SyncSource source) {
        StringBuffer sb = new StringBuffer();

        //TODO: retrieve most values from the passed DeviceConfig object
        sb.append("<Put>\n")
          .append("<CmdID>").append(cmdId).append("</CmdID>\n")
          .append("<Meta>\n")
          .append("<Type xmlns='syncml:metinf'>application/vnd.syncml-devinf+xml</Type>\n")
          .append("</Meta>\n").append("<Item>\n")
          .append("<Source><LocURI>./devinf12</LocURI></Source>\n")
          .append("<Data>\n").append(createDevInf(devInf, source)) //closing all tags
          .append("</Data>\n").append("</Item>\n").append("</Put>\n");

        return sb.toString();
    }

    /**
     * Used to build the part of the SyncML modification message containing the
     * device sync capabilities (<Results>) when requested by the server with
     * the command <Get>
     *
     * @param devInf
     *            A reference to the current device configuration (<code>DeviceConfig</code>)
     * @return the string containing the device capabilities part of the SyncML
     *         message sent to the server
     */
    public String formatResultsDeviceInfo(String nextCmdId,
                                           String msgIDget,
                                           String cmdIDget,
                                           SyncSource source,
                                           DeviceConfig devInf) {
        StringBuffer sb = new StringBuffer();

        sb.append("<Results>\n").append("<CmdID>" + nextCmdId + "</CmdID>\n")
          .append("<MsgRef>" + msgIDget + "</MsgRef>\n")
          .append("<CmdRef>" + cmdIDget + "</CmdRef>\n").append("<Meta>\n")
          .append("<Type xmlns='syncml:metinf'>application/vnd.syncml-devinf+xml</Type>\n")
          .append("</Meta>\n").append("<Item>\n")
          .append("<Source><LocURI>./devinf12</LocURI></Source>\n")
          .append("<Data>\n").append(createDevInf(devInf, source)) //closing all tags
          .append("</Data>\n").append("</Item>\n").append("</Results>");

        return sb.toString();
    }



    /**
     * Format a mapping message. Mappings allow server and client to have a
     * different representation for item's keys.
     * @param nextCmdId the next command id
     * @param sourceName is the source name
     * @param sourceUri is the source URI
     * @param mappings is an hashtable of strings where the key is the LUID and
     * the value is the GUID
     * @return a SyncML message containing the mapping
     */
    public String formatMappings(String nextCmdId,
                                 String sourceName,
                                 String sourceUri,
                                 Hashtable mappings) {

        if (mappings.size() == 0) {
            // No mappings to add
            return "";
        }

        Enumeration e = mappings.keys();
        StringBuffer out = new StringBuffer();

        out.append("<Map>\n").append("<CmdID>" + nextCmdId + "</CmdID>\n")
           .append("<Target>\n").append("<LocURI>" + sourceUri + "</LocURI>\n")
           .append("</Target>\n").append("<Source>\n")
           .append("<LocURI>" + sourceName + "</LocURI>\n").append("</Source>\n");

        while (e.hasMoreElements()) {

            String sourceRef = (String) e.nextElement();
            String targetRef = (String) mappings.get(sourceRef);

            out.append("<MapItem>\n").append("<Target>\n")
               .append("<LocURI>" + targetRef + "</LocURI>\n")
               .append("</Target>\n").append("<Source>\n")
               .append("<LocURI>" + sourceRef + "</LocURI>\n")
               .append("</Source>\n").append("</MapItem>\n");
        }
        out.append("</Map>\n");
        return out.toString();
    }

    /**
     * Format a status in response to a server command.
     * @param status is the status representation
     * @return a SyncML representation of the status
     */
    public String formatItemStatus(SyncMLStatus status) {
        StringBuffer ret = new StringBuffer("<Status>");
       
        ret.append("<CmdID>").append(status.getCmdId()).append("</CmdID>\n").
            append("<MsgRef>").append(status.getMsgRef()).append("</MsgRef>\n").
            append("<CmdRef>").append(status.getCmdRef()).append("</CmdRef>\n").
            append("<Cmd>").append(status.getCmd()).append("</Cmd>\n");
        
        String srcRef = status.getSrcRef();
        String tgtRef = status.getTgtRef();
        if (srcRef != null) {
            ret.append("<SourceRef>").append(srcRef).append("</SourceRef>\n");
        }
        if (tgtRef != null) {
            ret.append("<TargetRef>").append(tgtRef).append("</TargetRef>\n");
        }
        String items[] = status.getItemKeys();
        if (items != null) {
            for(int i=0, l=items.length; i<l; i++) {
                ret.append("<Item><Source><LocURI>").append(items[i])
                .append("</LogURI></Source></Item>");
            }
        }
        
        ret.append("<Data>").append(status.getStatus()).append("</Data>\n")
           .append("</Status>\n");
        
        return ret.toString();
    }

    /**
     * Format the sync tag preamble. This preamble is essentially the
     * information about source and target sync source
     * @param nextCmdId is the next command id
     * @param sourceName is the local source uri
     * @param sourceUri  is the remote source uri
     * @return the SyncML preamble
     */
    public String formatSyncTagPreamble(String nextCmdId,
                                        String sourceName,
                                        String sourceUri)
    {

        StringBuffer syncTag = new StringBuffer();

        syncTag.append("<CmdID>").append(nextCmdId)
               .append("</CmdID>\n")
               .append("<Target><LocURI>")
               .append(sourceUri)
               .append("</LocURI></Target>\n")
               .append("<Source><LocURI>")
               .append(sourceName)
               .append("</LocURI></Source>\n");

        return syncTag.toString();
    }

    /**
     * Format the syncml sync start tag
     * @return the SyncML sync start tag
     */
    public String formatStartSync() {
        return "<Sync>\n";
    }

   /**
    * Format the syncml sync end tag
    * @return the SyncML sync end tag
    */
   public String formatEndSync() {
        return "</Sync>\n";
    }

    /**
     * This method formats an Item tag for delete commands
     * @param key is the item key
     * @return the SyncML item
     */
    public String formatItemDelete(String key) {

        StringBuffer ret = new StringBuffer();
        
        ret.append("<Item>\n")
           .append("<Source><LocURI>")
           .append(key)
           .append("</LocURI></Source>\n")
           .append("</Item>\n");

        return ret.toString();
    }

    /**
     * Format the syncml add command (start)
     * @return the SyncML add command (start)
     */
    public String formatStartAddCommand() {
        return "<Add>\n";
    }

    /**
     * Format the syncml add command (end)
     * @return the SyncML add command (end)
     */
    public String formatEndAddCommand() {
        return "</Add>\n";
    }

    /**
     * Format the syncml replace command (start)
     * @return the SyncML replace command (start)
     */
    public String formatStartReplaceCommand() {
        return "<Replace>\n";
    }

    /**
     * Format the syncml replace command (end)
     * @return the SyncML replace command (end)
     */
    public String formatEndReplaceCommand() {
        return "</Replace>\n";
    }

    /**
     * Format the syncml delete command (start)
     * @return the SyncML delete command (start)
     */
    public String formatStartDeleteCommand() {
        return "<Delete>\n";
    }

    /**
     * Format the syncml delete command (end)
     * @return the SyncML delete command (end)
     */
    public String formatEndDeleteCommand() {
        return "</Delete>\n";
    }

    /**
     * Format the CmdID tag
     * @param cmId the cmdID value
     * @return the SyncML CmdID element
     */
    public String formatCmdId(String cmdId) {
        StringBuffer cmd = new StringBuffer();
        cmd.append("<CmdID>" + cmdId + "</CmdID>\n");
        return cmd.toString();
    }

    /**
     * Format the CmdID tag
     * @param cmId the cmdID value
     * @return the SyncML CmdID element
     */
    public String formatCmdId(int cmdId) {
        StringBuffer cmd = new StringBuffer();
        cmd.append("<CmdID>" + cmdId + "</CmdID>\n");
        return cmd.toString();
    }

    /**
     * Format the syncml start of an item element
     * @return the SyncML start of an item element
     */
    public String formatStartItem() {
        return "<Item>\n";
    }

    /**
     * Format the syncml end of an item element
     * @return the SyncML end of an item element
     */
    public String formatEndItem() {
        return "</Item>\n";
    }

    /**
     * Format an item type. This is the type used in modification commands.
     * @param type the item's type
     * @return the SyncML representation of the item's type
     */
    public String formatItemType(String type) {
        StringBuffer ret = new StringBuffer();
        ret.append("<Type xmlns=\"syncml:metinf\">")
            .append(type)
            .append("</Type>");
        return ret.toString();
    }

    /**
     * Format an item size.
     * @param size is the item's size
     * @return the SyncML representation of the item's size
     */
    public String formatItemSize(long size) {
        StringBuffer ret = new StringBuffer();
        ret.append("<Size>").append(size).append("</Size>");
        return ret.toString();
    }

    /**
     * Format the syncml start of a meta element
     * @return the SyncML start of a meta element
     */
    public String formatStartMeta() {
        return "<Meta>\n";
    }

    /**
     * Format the syncml end of a meta element
     * @return the SyncML end of a meta element
     */
    public String formatEndMeta() {
        return "</Meta>\n";
    }

    /**
     * Format an item luid.
     * @return the SyncML luid of an item
     */
    public String formatItemLuid(String luid) {
        StringBuffer ret = new StringBuffer();
        ret.append("<Source><LocURI>" + luid + "</LocURI></Source>\n");
        return ret.toString();
    }

    /**
     * Format an item parent.
     * @return the SyncML parent of an item
     */
    public String formatItemParent(String parent) {
        StringBuffer ret = new StringBuffer();
        ret.append("<SourceParent><LocURI>")
           .append(parent)
           .append("</LocURI></SourceParent>\n");
           return ret.toString();
    }

    /**
     * Format an item data.
     * @return the SyncML data element
     */
    public String formatItemData(String data) {
        StringBuffer ret = new StringBuffer();
        ret.append("<Data>").append(data).append("</Data>\n");
        return ret.toString();
    }

    /**
     * Format a "more data" element
     * @return the SyncML "more data" element
     */
    public String formatMoreData() {
        StringBuffer ret = new StringBuffer();
        ret.append("<").append(SyncML.TAG_MORE_DATA).append("/>\n");
        return ret.toString();
    }

    /**
     * Get the format string to add to the outgoing message.
     *
     * @return the Format string, according to the source encoding
     */
    public String formatItemFormat(String format) {
        // Get the Format tag from the SyncSource encoding.
        StringBuffer ret = new StringBuffer();
        ret.append("<Format xmlns=\'syncml:metinf\'>")
           .append(format)
           .append("</Format>\n");
        return ret.toString();
    }

    /**
     * Format a request to the server for its device capabilities
     * @param cmdId is the command id
     * @return the SyncML representation of the device capabilities request
     */
    public String formatGetDeviceInfo(String cmdId) {
        StringBuffer req = new StringBuffer();
        req.append("<Get>\n")
           .append("<CmdID>").append(cmdId).append("</CmdID>\n")
           .append("<Meta><Type xmlns='syncml:metinf'>application/vnd.syncml-devinf+xml</Type></Meta>\n")
           .append("<Item>")
           .append("<Target><LocURI>").append(SyncML.DEVINF12).append("</LocURI></Target>")
           .append("</Item>\n")
           .append("</Get>\n");
        return req.toString();
    }

    /**
     * Used to build the &lt;DevInf&gt; element as part of a SyncML message's
     * &lt;Put&gt; or &lt;Results&gt; section
     *
     * @param devInf
     *            A reference to the current device configuration (<code>DeviceConfig</code>)
     * @return the string containing the device capabilities part of the SyncML
     *         message sent to the server
     */
    private String createDevInf(DeviceConfig devInf, SyncSource source) {
        DataStore dataStore = source.getConfig().getDataStore();
        String sourceName = source.getName();
        String sourceType = source.getType();


        StringBuffer sb = new StringBuffer();

        if (devInf.man == null) {
            devInf.man = "";
        }

        if (devInf.mod == null) {
            devInf.mod = "";
        }

        if (devInf.oem == null) {
            devInf.oem = "";
        }

        if (devInf.fwv == null) {
            devInf.fwv = "";
        }

        if (devInf.swv == null) {
            devInf.swv = "";
        }

        if (devInf.hwv == null) {
            devInf.hwv = "";
        }

        if (devInf.devID == null) {
            devInf.devID = "";
        }

        if (devInf.devType == null) {
            devInf.devType = "";
        }

        sb.append("<DevInf xmlns='syncml:devinf'>\n").append("<VerDTD>1.2</VerDTD>\n")//mandatory
          .append("<Man>" + devInf.man + "</Man>\n")//mandatory: name of the manufacturer of the device
          .append("<Mod>" + devInf.mod + "</Mod>\n")//mandatory: model name or model number of the device
          .append("<OEM>" + devInf.oem + "</OEM>\n")//optional: Original Equipment Manufacturer
          .append("<FwV>" + devInf.fwv + "</FwV>\n")//mandatory: firmware version of the device or a date
          .append("<SwV>" + devInf.swv + "</SwV>\n")//mandatory: software version of the device or a date
          .append("<HwV>" + devInf.hwv + "</HwV>\n")//mandatory: hardware version of the device or a date
          .append("<DevID>" + devInf.devID + "</DevID>\n")//mandatory: identifier of the source synchronization device
          .append("<DevTyp>" + devInf.devType + "</DevTyp>\n");//mandatory: type of the source synchronization device (see OMA table)

        //optional flag (if present, the server SHOULD send time in UTC form)
        if (devInf.utc) {
            sb.append("<UTC/>\n");
        }
        //optional (if present, it specifies that the device supports receiving
        //large objects)
        if (devInf.loSupport) {
            sb.append("<SupportLargeObjs/>\n");
        }
        //optional: server MUST NOT send <NumberOfChanges> if the client has
        //not specified this flag
        if (devInf.nocSupport) {
            sb.append("<SupportNumberOfChanges/>\n");
        }

        //<DataStore> one for each of the local datastores
        if (null != dataStore) {
            formatDataStore(sb, dataStore);
        } else {
            sb.append("<DataStore>\n")//
                .append("<SourceRef>" + sourceName + "</SourceRef>\n") //required for each specified datastore
                .append("<Rx-Pref>\n").append("<CTType>").append(sourceType)
                .append("</CTType>\n").append("<VerCT></VerCT>\n")
                .append("</Rx-Pref>\n") //required for each specified datastore
                .append("<Tx-Pref>\n").append("<CTType>").append(sourceType)
                .append("</CTType>\n").append("<VerCT></VerCT>\n").append("</Tx-Pref>\n") //SyncCap
                .append("<SyncCap>\n")//mandatory
                .append("<SyncType>1</SyncType>\n")//Support of 'two-way sync'
                .append("<SyncType>2</SyncType>\n")//Support of 'slow two-way sync'
                //TODO: add support of one way?
                .append("<SyncType>7</SyncType>\n")//Support of 'server alerted sync'
                .append("</SyncCap>\n").append("</DataStore>\n");
        }
        sb.append("</DevInf>");
        return sb.toString();
    }

    private String startTag(String tag) {
        StringBuffer res = new StringBuffer();
        res.append("<").append(tag).append(">");
        return res.toString();
    }
    
    private String endTag(String tag) {
        StringBuffer res = new StringBuffer();
        res.append("</").append(tag).append(">");
        return res.toString();
    }
    
    private String tag(String name, String value) {
        return startTag(name) + value + endTag(name);
    }
    
    private void formatCTInfo(StringBuffer sb, CTInfo ctInfo) {
        sb.append(tag(SyncML.TAG_CTTYPE, ctInfo.getCTType()))
          .append(tag(SyncML.TAG_VERCT, ctInfo.getVerCT()));
    }
    
    private void formatCTInfo(StringBuffer sb, String tag, CTInfo ctInfo) {
        sb.append(startTag(tag)).append("\n");
        formatCTInfo(sb, ctInfo);
        sb.append(endTag(tag));
    }

    private boolean notEmpty(String s) {
        return null != s && s.length() > 0;
    }
    
    private void appendIfNotEmpty(StringBuffer sb, String tag, String value) {
        if (notEmpty(value)) {
            sb.append(tag(tag, value));
        }
    }

    // <!ELEMENT PropParam (ParamName, DataType?, ValEnum*, DisplayName?)>
    private void formatPropParam(StringBuffer sb, PropParam param) {
       sb.append(startTag(SyncML.TAG_PROPPARAM));
       sb.append(tag(SyncML.TAG_PARAMNAME, param.getParamName()));
       appendIfNotEmpty(sb, SyncML.TAG_DATATYPE, param.getDataType());
       for (int i = 0; i < param.getValEnums().size(); i++) {
           String v = (String)param.getValEnums().elementAt(i);
           sb.append(tag(SyncML.TAG_VALENUM, v));
       } 
       appendIfNotEmpty(sb, SyncML.TAG_DISPLAYNAME, param.getDisplayName());
       sb.append(endTag(SyncML.TAG_PROPPARAM));
    }

    // <!ELEMENT Property (PropName, DataType?, MaxOccur?, MaxSize?, NoTruncate?, ValEnum*, DisplayName?, PropParam*)>
    private void formatProperty(StringBuffer sb, Property property) {
        sb.append(startTag(SyncML.TAG_PROPERTY));
        sb.append(tag(SyncML.TAG_PROPNAME, property.getPropName()));
        appendIfNotEmpty(sb, SyncML.TAG_DATATYPE, property.getDataType());
        // TODO: maxOccur, maxSize, noTruncate
        for (int i = 0; i < property.getValEnums().size(); i++) {
            String v = (String)property.getValEnums().elementAt(i);
            sb.append(tag(SyncML.TAG_VALENUM, v));
        }
        appendIfNotEmpty(sb, SyncML.TAG_DISPLAYNAME, property.getDisplayName());
        for (int i = 0; i < property.getPropParams().size(); i++) {
            PropParam v = (PropParam)property.getPropParams().elementAt(i);
            formatPropParam(sb, v);
        }
        sb.append(endTag(SyncML.TAG_PROPERTY));
    }
    
    private void formatCTCap(StringBuffer sb, CTCap ctCap) {
        sb.append(startTag(SyncML.TAG_CTCAP));
        formatCTInfo(sb, ctCap.getCTInfo());
        for (int i = 0; i < ctCap.getProperties().size(); i++) {
            Property v = (Property)ctCap.getProperties().elementAt(i);
            formatProperty(sb, v);
        }
        sb.append(endTag(SyncML.TAG_CTCAP));
    }
    
    private void formatSyncType(StringBuffer sb, SyncType syncType) {
        sb.append(tag(SyncML.TAG_SYNCTYPE, Integer.toString(syncType.getType())));
    }
    
    private void formatSyncCap(StringBuffer sb, SyncCap syncCap) {
        sb.append(startTag(SyncML.TAG_SYNCCAP));
        for (int i = 0; i < syncCap.getSyncType().size(); i++) {
            SyncType v = (SyncType)syncCap.getSyncType().elementAt(i);
            formatSyncType(sb, v);
        }
        sb.append(endTag(SyncML.TAG_SYNCCAP));
    }
    
    private void formatDataStore(StringBuffer sb, DataStore dataStore) {
        //<DataStore> one for each of the local datastores
        sb.append(startTag(SyncML.TAG_DEVINFDATASTORE));
        sb.append(tag(SyncML.TAG_SOURCEREF, dataStore.getSourceRef().getValue()));
        formatCTInfo(sb, SyncML.TAG_RXPREF, dataStore.getRxPref());
        for (int i = 0; i < dataStore.getRxs().size(); i++) {
            CTInfo v = (CTInfo)dataStore.getRxs().elementAt(i);
            formatCTInfo(sb, SyncML.TAG_RX, v);
        }
        formatCTInfo(sb, SyncML.TAG_TXPREF, dataStore.getTxPref());
        for (int i = 0; i < dataStore.getTxs().size(); i++) {
            CTInfo v = (CTInfo)dataStore.getTxs().elementAt(i);
            formatCTInfo(sb, SyncML.TAG_TX, v);
        }
        for (int i = 0; i < dataStore.getCTCaps().size(); i++) {
            CTCap v = (CTCap)dataStore.getCTCaps().elementAt(i);
            formatCTCap(sb, v);
        }
        formatSyncCap(sb, dataStore.getSyncCap());
        sb.append(endTag(SyncML.TAG_DEVINFDATASTORE));
    }



}

 
