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
import com.funambol.util.Log;

import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.protocol.DataStore;

/**
 * Configuration data for the SyncSource. Can be extended by subclasses
 * of SyncSource to add specific data.
 */
public class SourceConfig {

    private static final String TAG_LOG = "SourceConfig";

    //--------------------------------------------------------------- Constants

    // Definitions for the names of the sources available in a standard
    // Funambol installation. A client can implement different sources too.
    //
    public static final String MAIL      = "mail";
    public static final String CONTACT   = "contact";
    public static final String TASK      = "task";
    public static final String CALENDAR  = "calendar";
    public static final String NOTE      = "note";
    public static final String BRIEFCASE = "briefcase";

    //Specific SyncSource Related Properties
    /**vCard source name definition */
    public static final String VCARD_NAME = "card";
    /** vCard mime type definition*/
    public static final String VCARD_TYPE = "text/x-vcard";
    /** eMail Object source name definition */
    public static final String EMAIL_OBJECT_NAME = "mail";
    /** eMail Object mime type definition */
    public static final String EMAIL_OBJECT_TYPE = "application/vnd.omads-email+xml";
    /** file object mime type definition */
    public static final String FILE_OBJECT_TYPE = "application/vnd.omads-file+xml";
    /** briefcase object mime type definition */
    public static final String BRIEFCASE_TYPE = "application/*";
    
    // This field contains a version number of the configuration data
    protected static final int VERSION_600 = 600 ;
    protected static final int VERSION     = 601 ;
    
    //-------------------------------------------------------------- Attributes
    //Parameter to store the configuration version
    private int version ;

    /** The name of this source. */
    private String name;
    
    /** The mime-type of the items for this source */
    private String type;

    /** 
     * The encoding of the items for this source
     */
    private String encoding;

    /**
     * Sync Mode for this source (it is the initial alert code).
     */
    private int syncMode;

    /** The remote URI on the SyncML server */
    private String remoteUri;

    /** The last anchor for this source */
    private long lastAnchor;
    
    /** The next anchor for this source */
    private long nextAnchor;

    /** Max number of items in a single message during a slow sync */
    private int maxItemsPerMessageInSlowSync = -1;

    /** Shall we flush SyncML message on the last chunk of a LO (only for LO) */
    private boolean breakOnLastChunk = false;

    /** This is the DataStore object describing this source capabilities */
    private DataStore dataStore;

    //------------------------------------------------------------- Constructors
    
    /**
     * Creates a new source configuration. This is the SyncML configuration of
     * the source, so it holds all of its SyncML properties.
     * This implementation builds a configuration suitable for a briefcase
     * source. The remoteUri and the name are set to &quote;briefcase&quote;,
     * and the encoding is set to base64.
     *
     * @param name the source name
     * @param type the source mime type
     * @param remoteUri the remote uri
     */
    public SourceConfig() {
        //Server Auth. params
        version = VERSION;

        name = BRIEFCASE ;   
        type = BRIEFCASE_TYPE;
        encoding = SyncSource.ENCODING_B64;
        syncMode = SyncML.ALERT_CODE_FAST;
        remoteUri = "briefcase";
        lastAnchor = 0;
        nextAnchor = 0;
    }
    
    /**
     * Creates a new source configuration. This is the SyncML configuration of
     * the source, so it holds all of its SyncML properties.
     * By default the source encoding is set to base64, use the setEncoding
     * method to override this property.
     *
     * @param name the source name
     * @param type the source mime type
     * @param remoteUri the remote uri
     */
    public SourceConfig(String name, String type, String remoteUri) {
        this(name, type, remoteUri, null);
    }

    /**
     * Creates a new source configuration. This is the SyncML configuration of
     * the source, so it holds all of its SyncML properties.
     * By default the source encoding is set to base64, use the setEncoding
     * method to override this property.
     *
     * @param name the source name
     * @param type the source mime type
     * @param remoteUri the remote uri
     * @param dataStore the SyncML data store, used to describe the source
     * capabilities in the DevInf. This param can be null and in this case a
     * basic set of capabilities are sent.
     */
    public SourceConfig(String name, String type, String remoteUri, DataStore dataStore) {
        //Server Auth. params
        version = VERSION;

        this.name = name;   
        this.type = type;
        this.encoding = SyncSource.ENCODING_B64;
        this.syncMode = SyncML.ALERT_CODE_FAST;
        this.remoteUri = remoteUri;
        this.dataStore = dataStore;
        lastAnchor = 0;
        nextAnchor = 0;
    }
 
    //----------------------------------------------------------- Public Methods
    
    /** Return the name of this source */
    public String getName() {
        return name;
    }

    /** Set the name of this source */
    public void setName(String name) {
        this.name = name;
    }

    /** Return the mime-type of this source */
    public String getType() {
        return type;
    }

    /** Set the mime-type of this source */
    public void setType(String type) {
        this.type = type;
    }

    /** Return the encoding of this source */
    public String getEncoding() {
        return encoding;
    }

    /** Set the encoding of this source */
    public void setEncoding(String enc) {
        this.encoding = enc;
    }

    /** Return the sync mode of this source */
    public int getSyncMode() {
        return syncMode;
    }

    /** Set the sync mode of this source */
    public void setSyncMode(int syncMode) {
        this.syncMode = syncMode;
    }
    
    /** Return the remote URI of this source */
    public String getRemoteUri() {
        return remoteUri;
    }

    /** Set the remote URI of this source */
    public void setRemoteUri(String remoteUri) {
        this.remoteUri = remoteUri;
    }

    /** Return the last anchor of this source */
    public long getLastAnchor() {
        return lastAnchor;
    }

    /** Set the last anchor of this source */
    public void setLastAnchor(long anchor) {
        Log.debug("[Sourceconfig] [ANCHOR]" + name + " setting last anchor to " + anchor);
        this.lastAnchor = anchor;
    }

    /** Return the next anchor of this source */
    public long getNextAnchor() {
        return nextAnchor;
    }

    /** Set the next anchor of this source */
    public void setNextAnchor(long anchor) {
        Log.debug("[Sourceconfig] [ANCHOR] "+ name +" setting next anchor to " + anchor);
        this.nextAnchor = anchor;
    }

    /**
     * Set the maximum number of items to be placed in a single SyncML message
     * during a slow sync. This value is part of the source config because we
     * want to allow clients to heuristically determine the best value for this
     * parameter and save it in the configuration.
     */
    public void setMaxItemsPerMessageInSlowSync(int value) {
        this.maxItemsPerMessageInSlowSync = value;
    }

    /**
     * Get the maximum number of items to be placed in a single SyncML message
     * during a slow sync
     */
    public int getMaxItemsPerMessageInSlowSync() {
        return maxItemsPerMessageInSlowSync;
    }

    public boolean getBreakMsgOnLastChunk() {
        return breakOnLastChunk;
    }

    public void setBreakMsgOnLastChunk(boolean value) {
        breakOnLastChunk = value;
    }

    /**
     * Write object fields to the output stream.
     * @param out Output stream
     * @throws IOException
     */
    public void serialize(DataOutputStream out) throws IOException {
        Log.trace(TAG_LOG, "Serializing " + name + 
                           " LastAnchor = " + lastAnchor + 
                           " NextAnchor = " + nextAnchor );

        out.writeInt(version);
        out.writeUTF(name);
        out.writeUTF(type);
        out.writeUTF(encoding);
        out.writeInt(syncMode);
        out.writeUTF(remoteUri);
        out.writeLong(lastAnchor);
        out.writeLong(nextAnchor);
        out.writeInt(maxItemsPerMessageInSlowSync);
    }

    /**
     * Read object field from the input stream.
     * @param in Input stream
     * @throws IOException
     */
    public void deserialize(DataInputStream in) throws IOException {
        int savedVer = in.readInt();
        version = savedVer;
        name = in.readUTF();
        type = in.readUTF();
        encoding = in.readUTF();
        syncMode = in.readInt();
        remoteUri = in.readUTF();
        lastAnchor = in.readLong();
        nextAnchor = in.readLong();
        if (savedVer >= VERSION) {
            maxItemsPerMessageInSlowSync = in.readInt();
        }
        Log.trace(TAG_LOG, "Deserializing " + name +
                           " LastAnchor = " + lastAnchor +
                           " NextAnchor = " + nextAnchor);
    }


    /**
     * This method returns the source DataStore. This store describe the source
     * capabilities from a SyncML standpoint and this info is used to build the
     * client device capabilities. If no DataStore is returned, the API will
     * build a default basic devinf (@see SyncMLFormatter)
     */
    public DataStore getDataStore() {
        return dataStore;
    }
}

