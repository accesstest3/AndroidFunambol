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

import java.io.UnsupportedEncodingException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.funambol.util.MD5;
import com.funambol.util.Base64;
import com.funambol.util.StringUtil;
import com.funambol.util.Log;

import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.protocol.SyncMLStatus;
import com.funambol.syncml.protocol.SyncMLCommand;
import com.funambol.syncml.protocol.SyncFilter;
import com.funambol.syncml.protocol.DevInf;
import com.funambol.util.XmlUtil;
import com.funambol.util.XmlException;
import com.funambol.util.ChunkedString;
import com.funambol.util.HttpTransportAgent;
import com.funambol.util.TransportAgent;
import com.funambol.util.CodedException;

/**
 * The SyncManager is the engine of the synchronization process on the
 * client library. It initializes the sync, checks the server responses
 * and communicate with the SyncSource, which is the client-specific
 * source of data.
 * A client developer must prepare a SyncConfig to istantiate a
 * SyncManager, and then can sync its sources calling the sync()
 * method.
 * By default the SyncManager uses an HttpTransportAgent to communicate with the
 * server, but the interface allows clients to specify their own transport
 * agent.
 */
public class SyncManager {

    //------------------------------------------------------------- Private data
    private static final String TAG_LOG = "SyncManager";

    /* Fast sync sending add state*/
    private static final int STATE_SENDING_ADD = 1;
    /* Fast sync sending update state*/
    private static final int STATE_SENDING_REPLACE = 2;
    /* Fast sync sending delete state*/
    private static final int STATE_SENDING_DELETE = 3;
    /* Fast sync modification complete state*/
    private static final int STATE_MODIFICATION_COMPLETED = 4;
    /* The current SyncML message must be flushed */
    private static final int STATE_FLUSHING_MSG = 5;
    /* SyncManager configuration*/
    private SyncConfig config;
    /* SyncSource to sync*/
    protected SyncSource source;
    /* Device ID taken from DeviceConfig*/
    private String deviceId;
    /* Max SyncML Message Size taken from DeviceConfig*/
    private int maxMsgSize;
    /**
     * A flag indicating if the client has to prepare the <DevInf> part of the
     * initialization SyncML message containing the device capabilities. It can
     * be set to <code>true</code> in two falls:
     *
     * a) the <code>serverUrl</code> isn't on the list of the already
     * connected servers
     *
     * b) the device configuration is changed
     */
    private boolean sendDevInf = false;
    /**
     * A flag indicating if the client has to add the device capabilities to the
     * modification message as content of a <Results> element. This occurs when
     * the server has sent a <Get> command request, sollicitating items of type
     * './devinf12'
     */
    private boolean addDevInfResults = false;
    /**
     * String containing the last Url of the server the client was connected to
     */
    private String lastServerUrl;
    /**
     * The value of the <CmdID> element of <Get>, to be used building the
     * <Results> command
     */
    private String cmdIDget = null;
    /**
     * The value of the <MsgID> element of the message in which <Get> is, to be
     * used building the <Results> command
     */
    private String msgIDget = null;
    // state used for fast sync
    int state;
    // The alerts sent by server, indexed by source name, instantiated in
    // checkServerAlerts
    private Hashtable serverAlerts;
    // The alert code for the current source (i.e. the actual sync mode
    // eventually modified by ther server
    protected int alertCode;
    // Server URL modified with session id.
    private String serverUrl;
    private String sessionID = null;
    /**
     * This member stores the LUID/GUID mapping for the items added
     * to the current source during the sync.
     */
    private Hashtable mappings = null;
    /**
     * This table is a helper to handle the hierarchy. It is the reverse table
     * of the mappings as it allows guid -> luid retrieval.
     * It is used when an item has the source parent set, to retrieve the
     * corresponding item in the client representation (luid)
     * There is a specific table because a reverse search in the mappings would
     * be too inefficient.
     */
    private Hashtable hierarchy = null;
    /**
     * This member stores the Status commands to send back to the server
     * in the next message. It is modified at each item received,
     * and is cleared after the status are sent.
     */
    private Vector statusList = null;
    /**
     * This member is used to store the current message ID.
     * It is sent to the server in the MsgID tag.
     */
    private int msgID = 0;
    /**
     * This member is used to store the current command ID.
     * It is sent to the server in the CmdID tag.
     */
    private CmdId cmdID = new CmdId(0);
    /**
     * A single TransportAgent for all the operations
     * performed in this Sync Manager
     */
    private TransportAgent transportAgent;
    private static final int PROTOCOL_OVERHEAD = 3072;
    /**
     * This member is used to indicate if the SyncManager is busy, that is
     * if a sync is on going (SyncManager supports only one synchronization
     * at a time, and requests are queued in the synchronized method sync
     */
    private boolean busy;

    /**
     * Unique instance of a BasicSyncListener which is used when the user does
     * not set up a listener in the SyncSource. In order to avoid the creation
     * of multiple instances of this class we use this static variable
     */
    private static SyncListener basicListener = null;

    /**
     * Mapping manager instance
     */
    private MappingManager mappingManager = null;
    
    /**
     * Useful to test the mapping behavior during acceptance tests
     */
    private boolean isMappingTestDisabled = true;

    /**
     * This is a wrapper to handle incomining large object for sources that do
     * not support it.
     */
    private SyncSourceLOHandler sourceLOHandler = null;

    private String serverNextAnchor = null;

    /**
     * This is the flag used to indicate that the sync shall be cancelled. Users
     * can call the cancel (@see cancel) method to cancel the current sync
     */
    private boolean cancel;

    private SyncMLFormatter formatter = new SyncMLFormatter();

    private SyncMLParser    parser    = new SyncMLParser();

    private SyncReport      syncReport = null;

    /**
     * This variable defines the order in which incoming items are processed by
     * the engine. Clients can change this order by invoking the
     * setCmdProcessingOrder method.
     */
    private String[] cmdProcessingOrder = {SyncML.TAG_ADD,
                                           SyncML.TAG_REPLACE,
                                           SyncML.TAG_DELETE
                                          };

    //------------------------------------------------------------- Constructors
    /**
     * SyncManager constructor
     *
     * @param conf is the configuration data filled by the client
     *
     */
    public SyncManager(SyncConfig conf) {
        this.config = conf;
        this.source = null;

        // Cache device info
        this.deviceId = config.deviceConfig.devID;
        this.maxMsgSize = config.deviceConfig.getMaxMsgSize();

        this.state = 0;
        this.serverAlerts = null;
        this.alertCode = 0;

        // mapping table
        this.mappings = null;

        this.busy = false;

        // status commands
        statusList = null;
        transportAgent =
                new HttpTransportAgent(
                config.syncUrl,
                config.userAgent,
                "UTF-8",
                conf.compress, conf.forceCookies);
        
    }

    //----------------------------------------------------------- Public methods
    /**
     * Synchronizes synchronization source, using the preferred sync
     * mode defined for that SyncSource.
     *
     * @param source the SyncSource to synchronize
     *
     * @throws SyncException
     *                  If an error occurs during synchronization
     *
     */
    public void sync(SyncSource source) throws SyncException {
        sync(source, source.getSyncMode(), false);
    }

    /**
     * Synchronizes synchronization source, using the preferred sync
     * mode defined for that SyncSource.
     *
     * @param source the SyncSource to synchronize
     * @param askServerDevInf forces the sync to query for server device
     * information. The information is returned to the client via the
     * SyncListener (@see SyncListener.startSyncing)
     *
     * @throws SyncException
     *                  If an error occurs during synchronization
     *
     */
    public void sync(SyncSource source, boolean askServerDevInf) throws SyncException {
        sync(source, source.getSyncMode(), askServerDevInf);
    }

    /**
     * Synchronizes synchronization source
     *
     * @param source the SyncSource to synchronize
     * @param syncMode the sync mode
     * @throws SyncException
     *                  If an error occurs during synchronization
     */
    public synchronized void sync(SyncSource src, int syncMode)
            throws SyncException {

        sync(src, syncMode, false);
    }

    /**
     * Synchronizes synchronization source
     *
     * @param source the SyncSource to synchronize
     * @param syncMode the sync mode
     * @param askServerDevInf forces the sync to query for server device
     * information. The information is returned to the client via the
     * SyncListener (@see SyncListener.startSyncing)
     *
     * @throws SyncException
     *                  If an error occurs during synchronization
     */
    public synchronized void sync(SyncSource src, int syncMode,
                                  boolean askServerDevInf)
            throws SyncException {

        busy = true;
        cancel = false;
        serverNextAnchor = null;

        // Initialize the mapping message manager
        Log.debug(TAG_LOG, "Creating Mapping Manager");
        mappingManager = new MappingManager(src.getName());

        // Creates a sync source large object handler
        sourceLOHandler = new SyncSourceLOHandler(src, maxMsgSize, formatter);

        // Initialize a new sync report for this sync
        syncReport = new SyncReport(src);
        syncReport.setRequestedSyncMode(syncMode);
        syncReport.setLocUri(src.getName());
        syncReport.setRemoteUri(src.getSourceUri());

        // By default the sync does not keep track of the hierarchy, we do it
        // only for slow and refresh from server. The hashtable is re-initialized
        // below, once the sync mode has been determined
        hierarchy = null;

        // Initialize the basicSyncListener
        if (basicListener == null) {
            basicListener = new BasicSyncListener();
        }

        // Notifies the listener that a new sync is about to start
        getSyncListenerFromSource(src).startSession();

        if (syncMode == SyncML.ALERT_CODE_NONE) {
            Log.info(TAG_LOG, "Source not active.");
            syncReport.setSyncStatus(SyncListener.SUCCESS);
            getSyncListenerFromSource(src).endSession(syncReport);
            return;
        }

        int syncStatus = SyncListener.SUCCESS;
        Throwable lastException = null;
        try {
            String response = null;

            // Set source attribute
            this.source = src;

            // Set initial state
            nextState(STATE_SENDING_ADD);

            //Set NEXT Anchor referring to current timestamp
            this.source.setNextAnchor(System.currentTimeMillis());

            this.sessionID = String.valueOf(System.currentTimeMillis());
            this.serverUrl = config.syncUrl;

            // init status commands list
            this.statusList = new Vector();

            //deciding if the device capabilities have to be sent
            if (isNewServerUrl(serverUrl)) {
                setFlagSendDevInf();
            }

            // ================================================================
            // Initialization phase
            // ================================================================
            
            DevInf devInf = performInitializationPhase(syncMode, askServerDevInf, syncReport);

            // ================================================================
            // Sync phase
            // ================================================================

            if (isSyncToBeCancelled()) {
                cancelSync();
            }
                    
            //Lookup the storage for mappings that were not sent 
            //when the last sync took place and assign them to the 
            //Mapping object (HT and vector) to send within the first 
            //modification message. If last sync was fine the related 
            //hashtable and status vector are empty, so the sync can normally
            //begin
            // Use a local item map in order to access the store just 1 time
            
            /*
            The mappings are sent in the next sync when the alert code from 
            server is:
            SyncML.ALERT_CODE_FAST
            SyncML.ALERT_CODE_TWO_WAY_BY_SERVER
            SyncML.ALERT_CODE_ONE_WAY_FROM_SERVER_BY_SERVER        
            SyncML.ALERT_CODE_ONE_WAY_FROM_SERVER
            
            No mapping will be sent when the alert code will be different from 
            the ones above:
            SyncML.ALERT_CODE_SLOW
            SyncML.ALERT_CODE_NONE
            SyncML.ALERT_CODE_REFRESH_FROM_SERVER_BY_SERVER
            SyncML.ALERT_CODE_REFRESH_FROM_SERVER        
            SyncML.ALERT_CODE_REFRESH_FROM_CLIENT_BY_SERVER
            SyncML.ALERT_CODE_REFRESH_FROM_CLIENT
            SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_BY_SERVER        
            SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT        
            SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW 
            */
            
            switch (alertCode) {
                case SyncML.ALERT_CODE_FAST:
                case SyncML.ALERT_CODE_TWO_WAY_BY_SERVER:
                case SyncML.ALERT_CODE_ONE_WAY_FROM_SERVER_BY_SERVER:
                case SyncML.ALERT_CODE_ONE_WAY_FROM_SERVER:
                    mappings = mappingManager.getMappings(source.getName());
                    break;
                case SyncML.ALERT_CODE_SLOW:
                case SyncML.ALERT_CODE_NONE:
                case SyncML.ALERT_CODE_REFRESH_FROM_SERVER_BY_SERVER:
                case SyncML.ALERT_CODE_REFRESH_FROM_SERVER:
                case SyncML.ALERT_CODE_REFRESH_FROM_CLIENT_BY_SERVER:
                case SyncML.ALERT_CODE_REFRESH_FROM_CLIENT:
                case SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_BY_SERVER:
                case SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT:
                case SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW:
                    mappings = new Hashtable();
                    mappingManager.resetMappings(source.getName());
                    break;
                default: 
                    break;
            }

            // The hierarchy needs to be initialized for any sync
            switch (alertCode) {
                case SyncML.ALERT_CODE_FAST:
                case SyncML.ALERT_CODE_TWO_WAY_BY_SERVER:
                case SyncML.ALERT_CODE_ONE_WAY_FROM_SERVER_BY_SERVER:
                case SyncML.ALERT_CODE_ONE_WAY_FROM_SERVER:
                    hierarchy = mappingManager.getMappings("hierarchy-" + source.getName());
                    break;
                case SyncML.ALERT_CODE_SLOW:
                case SyncML.ALERT_CODE_NONE:
                case SyncML.ALERT_CODE_REFRESH_FROM_SERVER_BY_SERVER:
                case SyncML.ALERT_CODE_REFRESH_FROM_SERVER:
                case SyncML.ALERT_CODE_REFRESH_FROM_CLIENT_BY_SERVER:
                case SyncML.ALERT_CODE_REFRESH_FROM_CLIENT:
                case SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_BY_SERVER:
                case SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT:
                case SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW:
                    // We start from scratch because we don't support slow sync
                    // resuming. Otherwise we should keep the hierarchy around
                    hierarchy = new Hashtable();
                    mappingManager.resetMappings("hierarchy-" + source.getName());
                    break;
                default: 
                    break;
            }
            // Prepopulate the hierarchy with the root item, whose
            // mapping is itself
            if (hierarchy.get("/") == null) {
                hierarchy.put("/", "/");
            }
            
            // Notifies that the synchronization is going to begin
            boolean ok = getSyncListenerFromSource(src).startSyncing(alertCode, devInf);

            if (!ok) {
                //User Aborts the slow sync request
                Log.info(TAG_LOG, "Sync process aborted by the user");
                syncStatus = SyncListener.CANCELLED;
                return;
            }


            source.beginSync(alertCode);
            getSyncListenerFromSource(src).syncStarted(alertCode);

            boolean done = false;

            // the implementation of the client/server multi-messaging
            // through a do while loop: while </final> tag is reached.
            do {
                getSyncListenerFromSource(src).startSending(source.getClientAddNumber(),
                        source.getClientReplaceNumber(),
                        source.getClientDeleteNumber());
                String modificationsMsg = prepareModificationMessage();
                Log.info(TAG_LOG, "Sending modification");
                Log.debug(TAG_LOG, modificationsMsg);

                if (isSyncToBeCancelled()) {
                    cancelSync();
                }

                response = postRequest(modificationsMsg);
                
                modificationsMsg = null;
                // At this point we are sure mappings were received by the
                // server, so we can clear them
                mappings.clear();

                Log.info(TAG_LOG, "Response received");
                Log.debug(TAG_LOG, response);
                getSyncListenerFromSource(src).endSending();

                //listener.dataReceived(transportAgent.getResponseDate(),
                //                      response.length());

                // The startReceiving(n) is notified from within the
                // processModifications because here we do not know the number
                // of messages to be received
                processModifications(new ChunkedString(response));

                done = ((response.indexOf("<Final/>") >= 0) ||
                        (response.indexOf("</Final>") >= 0));

                response = null;

                getSyncListenerFromSource(src).endReceiving();

                //call the mapping manager to save the current mappings
                //in case of a network error occur. We do not save only in the
                //finally block because if the application exits abruptly we
                //would lose everything
                mappingManager.saveMappings(mappings);

            } while (!done);

            Log.info(TAG_LOG, "Modification session succesfully completed");
            getSyncListenerFromSource(src).endSyncing();

            // ================================================================
            // Mapping phase
            // ================================================================
            if (isSyncToBeCancelled()) {
                cancelSync();
            }

            getSyncListenerFromSource(src).startMapping();
            
            if (!isMappingTestDisabled) {
                //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                //DOESN'T SEND THE MAP COMMAND FOR TESTS
                //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXxXX
                //simulates a broken mapping message in order 
                //it not to be sent because of a connection error on client side
                Log.debug(TAG_LOG, "MAPPING OBJECTS CLEARED FOR TESTING PURPOSES");
                throw new SyncException(WriteRequestException.SERVER_CONNECTION_REQUEST_ERROR,
                                        "Connection blocked for test purposes");
            }

            // Send the map message only if a mapping or a status has to be sent
            // (beware that we always prepare the status to the sync hdr, so we
            // always have at least one status, but this must not be considered
            // here
            if (statusList.size() > 1 || mappings.size() > 0) {
                
                String mapMsg = prepareMappingMessage();
                
                Log.info(TAG_LOG, "Sending Mappings\n");
                Log.debug(TAG_LOG, mapMsg);

                try {
                    response = postRequest(mapMsg);
                    // If we get here, we are sure that mappings were received
                    // by the server and we can clear them. In any other case in
                    // order to be conservative, we preserve them and send them
                    // in the next sync.
                    mappings.clear();
                } catch (ReadResponseException rre) {
                    source.setLastAnchor(source.getNextAnchor());
                    //save last anchors if the mapping message has been sent but
                    //the response has not been received due to network problems
                    Log.info(TAG_LOG, "Last sync message sent - Error reading the response " + rre);
                }
                
                mapMsg = null;

                if (response!=null) {
                    Log.info(TAG_LOG, "Response received");
                    Log.debug(TAG_LOG, response);

                    //listener.dataReceived(
                    //      transportAgent.getResponseDate(), response.length());

                    // Check server response (can throws exception to the caller)
                    checkStatus(new ChunkedString(response), SyncML.TAG_SYNCHDR);

                    response = null;
                } else {
                    Log.info(TAG_LOG, "Response not received, skipping check for status");
                }

                Log.info(TAG_LOG, "Mapping session succesfully completed");

            } else {
                Log.info(TAG_LOG, "No mapping message to send");

            }

           
            // TODO: following code must be run only for succesfull path or error reading inputstream
            //       the other cases must skip the following code
            Log.debug(TAG_LOG, "Notifying listener end mapping");
            getSyncListenerFromSource(src).endMapping();

            Log.debug(TAG_LOG, "Changing anchors");
            // Set the last anchor to the next timestamp for the source
            source.setLastAnchor(source.getNextAnchor());

            Log.debug(TAG_LOG, "Ending session (" + syncStatus + ")");
            // Tell the source that the sync is finished
            Log.debug(TAG_LOG, "Calling source endSync");
            source.endSync();

            //call the mapping manager to save an empty item map
            //every time the sync ended without errors
            if (isMappingTestDisabled) {
                mappingManager.resetMappings(source.getName());
            }

            // If the synchronization terminates with no errors, then we reset
            // the hierarchy, because the sync is really over and it cannot be
            // resumed
            mappingManager.resetMappings("hierarchy-" + source.getName());
            // If we got here without errors, we can clear the mapping hash so
            // that the finally block will not save it
            mappings = null;

            // Create a listener status from the source status
            syncStatus = getListenerStatusFromSourceStatus(source.getStatus());
        } catch (CompressedSyncException compressedSyncException) {
            Log.error(TAG_LOG, "CompressedSyncException: ", compressedSyncException);
            syncStatus = SyncListener.COMPRESSED_RESPONSE_ERROR;
            throw compressedSyncException;
        } catch (SyncException se) {
            Log.error(TAG_LOG, "SyncException", se);
            // Create a listener status from the exception
            syncStatus = getListenerStatusFromSyncException(se);
            lastException = se;
            throw se;
        } catch (Throwable e) {
            // We capture any exception here and transform it into a
            // SyncException. The only runtime exception we allow to go thorugh
            // is the SecurityException because it is related to the users
            // permissions and it is preferable to leave its own semantics
            // intact
            lastException = e;
            if (e instanceof SecurityException) {
                Log.error(TAG_LOG, "Security Exception", e);
                throw (SecurityException)e;
            } else {
                Log.error(TAG_LOG, "Exception", e);
                syncStatus = SyncListener.GENERIC_ERROR;
                throw new SyncException(SyncException.CLIENT_ERROR, e.toString());
            }
        } finally {
            // If we have mappings then we must save them, regardless of how we
            // got here because we haven't sent them to the server
            if (mappings != null && mappings.size() > 0) {
                Log.info(TAG_LOG, "Saving mappings to be sent in the next sync");
                mappingManager.saveMappings(mappings);
            }

            // Notifies the listener that the session is over
            Log.debug(TAG_LOG, "Ending session (" + syncStatus + ")");
            syncReport.setSyncStatus(syncStatus);
            // Notify the listener that the sync is finished
            try {
                getSyncListenerFromSource(src).endSession(syncReport);
            } finally {
                releaseResources();
                sourceLOHandler.releaseResources();
            }
        }
    }


    /**
     * This method cancels the current sync. The sync is interrupted once the
     * engine reaches the next check point (in other words the termination is
     * not immediate). 
     * When a sync is interrupted, a SyncException with code CANCELLED is
     * thrown. This exception will be thrown by the thread running the
     * synchronization itself, not by this method.
     * This method is non blocking, it marks the sync as to be cancelled and
     * returns, without waiting for the sync to be really cancelled.
     * If this SyncManager is performing more syncs cuncurrently, then all of
     * them are cancelled.
     */
    public void cancel() {
        cancel = true;
        if (sourceLOHandler != null) {
            sourceLOHandler.cancel(); 
        }
    }


    /**
     * To be invoked by every change of the device configuration and if the
     * serverUrl is a new one (i.e., not already on the list
     * <code>lastServerUrl</code>
     */
    public void setFlagSendDevInf() {
        sendDevInf = true;
    }

    /**
     * Checks if the manager is currently busy performing a synchronization.
     * @return true if a sync is currently on going
     */
    public boolean isBusy() {
        return busy;
    }

    /**
     * Sets the transport agent to be used for the next message to be sent. If
     * this method is invoked in the middle of a sync, it changes the connection
     * method for that very sync. This is a possible behavior, but it is very
     * uncommon. Users should make sure no sync is in progress when the
     * transport agent is changed. Typically the transport agent is changed
     * before the first sync and not changed aterward.
     * @param ta the transport agent
     * @throws IllegalArgumentException if the give transport agent is null
     */
    public void setTransportAgent(TransportAgent ta) {
        if (ta != null) {
            transportAgent = ta;
        } else {
            throw new IllegalArgumentException("Transport agent cannot be null");
        }
    }

    /**
     * This method returns the sync report of the last sync performed. This
     * information is valid until a new sync is fired.
     */
    public SyncReport getSyncReport() {
        return syncReport;
    }

    /**
     * Defines the order in which incoming items are processed by the engine.
     * @param newOrder an array of three items whose values must be one of: 
     *        SyncML.TAG_ADD, SyncML.TAG_REPLACE, SyncML.TAG_DELETE
     */
    public void setCmdProcessingOrder(String newOrder[]) {
        if (newOrder == null || newOrder.length != 3) {
            throw new IllegalArgumentException("Three commands expected");
        }
        cmdProcessingOrder = newOrder;
    }

    //---------------------------------------------------------- Private methods

    private DevInf performInitializationPhase(int syncMode, boolean askServerDevInf,
                                            SyncReport syncReport) throws SyncException, XmlException
    {
        // Get ready to try the authentication more than once, because it can
        // fail for invalid nonce or invalid auth method and we must perform
        // different attempts
        boolean md5 = SyncML.AUTH_TYPE_MD5.equals(config.preferredAuthType);
        int md5Attempts = 0;
        boolean retry;
        ChunkedString chunkedResp = null;

        // Reset the msgId here
        resetMsgID();

        getSyncListenerFromSource(source).startConnecting();

        do {
            retry = false;

            Log.info(TAG_LOG, "Sending init message " + md5);
            //Format request message to be sent to the server
            String initMsg = prepareInitializationMessage(syncMode, askServerDevInf, md5);
            Log.debug(TAG_LOG, initMsg);

            if (isSyncToBeCancelled()) {
                cancelSync();
            }

            String response = postRequest(initMsg);
            initMsg = null;

            Log.info(TAG_LOG, "Response received");
            Log.debug(TAG_LOG, "Response: " + response);

            // TODO: today the SyncSource does not need to process this data.
            // When we support large object we may want to change this
            getSyncListenerFromSource(source).dataReceived(transportAgent.getResponseDate(),
                    response.length());

            chunkedResp = new ChunkedString(response);
            // Check server response (can throws exception and break the sync)

            try {
                checkStatus(chunkedResp, SyncML.TAG_SYNCHDR);
            } catch (AuthenticationException ae) {
                // Handle authentication errors and retries
                String authMethod = ae.getAuthMethod();
                String nextNonce  = ae.getNextNonce();
                if (SyncML.AUTH_TYPE_MD5.equals(authMethod)) {
                    // The server required md5 authentication
                    if (config.allowMD5Authentication()) {
                        // If the previous attempt was not md5 or
                        // the previous was md5 but the first try, then
                        // we try again with the new nonce
                        if (!md5 || (md5 && md5Attempts == 0)) {
                            // Try again with the new nonce
                            Log.debug(TAG_LOG, "Setting next nonce to " + nextNonce);
                            retry = true;
                            md5 = true;
                            
                            // must also consider the session sent from server, 
                            // however server start a new sync session
                            if (XmlUtil.getTag(chunkedResp, "RespURI") != -1) {
                                try {
                                    serverUrl = XmlUtil.getTagValue(chunkedResp, "RespURI").toString();
                                } catch (XmlException xe) {
                                    Log.error(TAG_LOG, "Error parsing RespURI from server ", xe);
                                    throw new SyncException(
                                            SyncException.SERVER_ERROR,
                                            "Cannot find the Response URI in server response.");
                                }
                            }
                        }
                    }
                    if (nextNonce != null) {
                        config.clientNonce=nextNonce;
                    }
                } else if (SyncML.AUTH_TYPE_BASIC.equals(authMethod) && md5) {
                    // The previous md5 auth failed and the server required a
                    // basic auth. If the client allows it, we fall back to
                    // basic
                    if (config.allowBasicAuthentication()) {
                        retry = true;
                        md5 = false;
                    }
                }
                if (!retry) {
                    throw new SyncException(SyncException.AUTH_ERROR, "Invalid credentials");
                }
            }
            if (md5) {
                md5Attempts++;
            }
            response = null;
        } while(retry);

        if (chunkedResp == null) {
            throw new SyncException(SyncException.CLIENT_ERROR, "Cannot authenticate");
        }

        checkStatus(chunkedResp, SyncML.TAG_ALERT);

        // client interpretes server alerts and store them into "serverAlerts"
        checkSyncHdr(chunkedResp);
        checkServerAlerts(chunkedResp);

        // save the alert code for the current source
        String name = source.getName();
        Log.debug(TAG_LOG, name);
        alertCode = getSourceAlertCode(name);
        Log.info(TAG_LOG, "Alert code: " + alertCode);
        Log.info(TAG_LOG, "Initialization succesfully completed");
        getSyncListenerFromSource(source).endConnecting(alertCode);
        syncReport.setAlertedSyncMode(alertCode);

        if (alertCode == SyncML.ALERT_CODE_SLOW &&
                syncMode  == SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW) {

            Log.error(TAG_LOG, "Client requested a one way from client "
                    + "no slow, but the server forced a slow sync");
        }

        // if the server has required device capabilities in the response, these
        // are added within the next client request in the <Results> method
        addDevInfResults = isGetCommandFromServer(chunkedResp);

        // Process "Results"
        // If the server put a results in response to our get, we shall
        // process it. At the moment we only expect device info as result
        DevInf devInf = null;
        if (XmlUtil.getTag(chunkedResp, SyncML.TAG_RESULTS) != -1) {
            ChunkedString resultsCommand = null;
            try {
                resultsCommand = XmlUtil.getTagValue(chunkedResp, SyncML.TAG_RESULTS);
                devInf = parser.parseResults(resultsCommand.toString());
                if (devInf != null) {
                    // Prepare a success status for the results command
                    generateStatusForCommand(resultsCommand, SyncML.TAG_RESULTS,
                            SyncMLStatus.SUCCESS, 1);
                } else {
                    // Prepare an error status for the results command
                    generateStatusForCommand(resultsCommand, SyncML.TAG_RESULTS,
                            SyncMLStatus.GENERIC_ERROR, 1);
                }
            } catch (XmlException xe) {
                // There is no Results command, this is not a problem, unless we
                // asked for the server cap
                if (askServerDevInf) {
                    throw new SyncException(SyncException.SERVER_ERROR,
                            "Cannot find server capabilities in server response");
                }
            } catch (SyncMLParserException pe) {
                // We cannot parse the server caps. We continue the sync, but
                // this may result in incorrect clients behavior
                Log.error(TAG_LOG, "Cannot parse server results ", pe);
                // Prepare an error status for the results command
                if (resultsCommand != null) {
                    generateStatusForCommand(resultsCommand, SyncML.TAG_RESULTS,
                            SyncMLStatus.GENERIC_ERROR, 1);
                }
            } catch (Exception e) {
                // Something went wrong processing the results. This is likely
                // to be a bug in the APIs, so we log an error and continue the
                // sync
                Log.error(TAG_LOG, "Cannot parse server results", e);
                // Prepare an error status for the results command
                if (resultsCommand != null) {
                    generateStatusForCommand(resultsCommand, SyncML.TAG_RESULTS,
                            SyncMLStatus.GENERIC_ERROR, 1);
                }
            }
        }
        // Handle the case where the server "put" its capabilities without
        // an explicit get on our side
        if (XmlUtil.getTag(chunkedResp, SyncML.TAG_PUT) != -1) {
            ChunkedString putCommand = null;
            try {
                putCommand = XmlUtil.getTagValue(chunkedResp, SyncML.TAG_PUT);
                if (putCommand != null && putCommand.length() > 0) {
                    devInf = parser.parsePut(putCommand.toString());
                    if (devInf != null) {
                        // Prepare a success status for the results command
                        generateStatusForCommand(putCommand, SyncML.TAG_PUT,
                                SyncMLStatus.SUCCESS, 1);
                    } else {
                        // Prepare an error status for the results command
                        generateStatusForCommand(putCommand, SyncML.TAG_PUT,
                                SyncMLStatus.GENERIC_ERROR, 1);
                    }
                }
            } catch (XmlException xe) {
                // The Xml tag cannot be extracted
                Log.error(TAG_LOG, "Cannot extract put command", xe);
            } catch (SyncMLParserException pe) {
                // The Xml command cannot be parsed
                Log.error(TAG_LOG, "Cannot parse put command ", pe);
                // Prepare an error status for the results command
                if (putCommand != null) {
                    generateStatusForCommand(putCommand, SyncML.TAG_PUT,
                            SyncMLStatus.GENERIC_ERROR, 1);
                }
            } catch (Exception e) {
                // A generic error occurred, ignore this command
                Log.error(TAG_LOG, "Generic error parsing put command", e);
                // Prepare an error status for the results command
                if (putCommand != null) {
                    generateStatusForCommand(putCommand, SyncML.TAG_PUT,
                            SyncMLStatus.GENERIC_ERROR, 1);
                }
            }
        }

        // Get the server URL with the session ID
        if (XmlUtil.getTag(chunkedResp, "RespURI") != -1) {
            try {
                serverUrl = XmlUtil.getTagValue(chunkedResp, "RespURI").toString();
            } catch (XmlException xe) {
                Log.error(TAG_LOG, "Error parsing RespURI from server ", xe);
                throw new SyncException(
                        SyncException.SERVER_ERROR,
                        "Cannot find the Response URI in server response.");
            }
        }

        chunkedResp = null;

        return devInf;
    }


    /**
     * Checks if the current server URL is the same as by the last connection.
     * If not, the current server URL is persisted in a record store on the
     * device
     *
     * @param url
     *            The server URL coming from the SyncConfig
     * @return true if the client wasn't ever connected to the corresponding
     *         server, false elsewhere
     */
    private boolean isNewServerUrl(String url) {

        //retrieve last server URL from the configuration
        lastServerUrl = config.lastServerUrl;

        if (StringUtil.equalsIgnoreCase(lastServerUrl, url)) {
            // the server url is the same as by the last connection, the client
            // may not send the device capabilities
            return false;
        } else {
            // the server url is new, the value has to be stored (this is let to
            // the SyncmlMPIConfig, while the SyncConfig isn't currently stored)
            return true;//the url is different, client can send the device info
        }
    }

    /**
     * Posts the given message to the url specified by <code>serverUrl</code>.
     *
     * @param request the request msg
     * @return the response of the server as a string
     *
     * @throws SyncException in case of network errors (thrown by sendMessage)
     */
    private String postRequest(String request) throws SyncException {
        transportAgent.setRequestURL(serverUrl);
        try {
            return transportAgent.sendMessage(request);
        } catch (CodedException ce) {
            int code;
            switch (ce.getCode()) {
                case CodedException.DATA_NULL:
                    code = SyncException.DATA_NULL;
                    break;
                case CodedException.CONN_NOT_FOUND:
                    code = SyncException.CONN_NOT_FOUND;
                    break;
                case CodedException.ILLEGAL_ARGUMENT:
                    code = SyncException.ILLEGAL_ARGUMENT;
                    break;
                case CodedException.WRITE_SERVER_REQUEST_ERROR:
                    code = SyncException.WRITE_SERVER_REQUEST_ERROR;
                    WriteRequestException wre = new WriteRequestException(code, ce.toString());
                    throw wre;
                case CodedException.ERR_READING_COMPRESSED_DATA:
                    CompressedSyncException cse = new CompressedSyncException(ce.toString());
                    throw cse;
                case CodedException.CONNECTION_BLOCKED_BY_USER:
                    code = SyncException.CONNECTION_BLOCKED_BY_USER;
                    break;
                case CodedException.READ_SERVER_RESPONSE_ERROR:
                    code = SyncException.READ_SERVER_RESPONSE_ERROR;
                    ReadResponseException rre = new ReadResponseException(code, ce.toString());
                    throw rre;
                case CodedException.OPERATION_INTERRUPTED:
                    code = SyncException.CANCELLED;
                    break;
                default:
                    code = SyncException.CLIENT_ERROR;
                    break;
            }
            SyncException se = new SyncException(code, ce.toString());
            throw se;
        }
    }

    /**
     * Checks if the given response message is authenticated by the server
     *
     * @param msg the message to be checked
     * @param statusOf the command of which we want to check the status
     *
     * @throws SyncException in case of other errors
     */
    private void checkStatus(ChunkedString msg, String statusOf)
            throws SyncException {

        Vector statusTags = null;
        try {
            statusTags = XmlUtil.getTagValues(
                    XmlUtil.getTagValues(
                    XmlUtil.getTagValues(msg, SyncML.TAG_SYNCML),
                    SyncML.TAG_SYNCBODY),
                    SyncML.TAG_STATUS);
        } catch (XmlException xe) {
            Log.error(TAG_LOG, "CheckStatus: error parsing server status " + msg, xe);
            return;
        }

        for (int i = 0,  l = statusTags.size(); i < l; i++) {
            ChunkedString tag = (ChunkedString) statusTags.elementAt(i);
            // Parse the status
            SyncMLStatus status = SyncMLStatus.parse(tag);
            if (status != null) {
                // Consider only that status for the requested command
                if (statusOf.equals(status.getCmd())) {

                    switch (status.getStatus()) {

                        case SyncMLStatus.SUCCESS:                      // 200
                            return;
                        case SyncMLStatus.REFRESH_REQUIRED:             // 508
                            Log.info(TAG_LOG, "Refresh required by server.");
                            return;
                        case SyncMLStatus.AUTHENTICATION_ACCEPTED:      // 212
                        {
                            Log.info(TAG_LOG, "Authentication accepted by the server.");
                            // Save the new nonce if the server sent it
                            String nextNonce  = status.getChalNextNonce();
                            if (nextNonce != null) {
                                config.clientNonce = nextNonce;
                            }

                            return;
                        }
                        case SyncMLStatus.INVALID_CREDENTIALS:          // 401
                        {
                            // Grab the authentication chal info and propagate
                            // it
                            String authMethod = status.getChalType();
                            String nonceFormat = status.getChalFormat();
                            String nextNonce  = status.getChalNextNonce();
                            Log.info(TAG_LOG, "Server required authentication " + authMethod + " and nonce: " + nextNonce);

                            AuthenticationException authExc = new AuthenticationException("Authentication failed",
                                                                                          authMethod,
                                                                                          nonceFormat,
                                                                                          nextNonce);
                            throw authExc;
                        }
                        case SyncMLStatus.FORBIDDEN:                    // 403
                            throw new SyncException(
                                    //SyncException.AUTH_ERROR,
                                    SyncException.FORBIDDEN_ERROR,
                                    "User not authorized: " + config.userName + " for source: " + source.getSourceUri());
                        case SyncMLStatus.NOT_FOUND:                    // 404
                            Log.error(TAG_LOG, "Source URI not found on server: " + source.getSourceUri());
                            throw new SyncException(
                                    //SyncException.ACCESS_ERROR,
                                    SyncException.NOT_FOUND_URI_ERROR,
                                    "Source URI not found on server: " + source.getSourceUri());
                        case SyncMLStatus.SERVER_BUSY:                  // 503
                            throw new SyncException(
                                    SyncException.SERVER_BUSY,
                                    "Server busy, another sync in progress for " + source.getSourceUri());
                        case SyncMLStatus.PROCESSING_ERROR:             // 506
                            throw new SyncException(
                                    SyncException.BACKEND_ERROR,
                                    "Error processing source: " + source.getSourceUri() + status.getStatusDataMessage());
                        case SyncMLStatus.BACKEND_AUTH_ERROR:             // 506
                            throw new SyncException(
                                    SyncException.BACKEND_AUTH_ERROR,
                                    "Error processing source: " + source.getSourceUri() + status.getStatusDataMessage());
                        default:
                            // Unhandled status code
                            Log.debug(TAG_LOG, "Unhandled Status Code, throwing exception");
                            throw new SyncException(
                                    SyncException.SERVER_ERROR,
                                    "Error from server: " + status.getStatus());
                    }
                }
            }
        }

        // Should neven happen
        Log.error(TAG_LOG, "checkStatus: can't find Status in " + statusOf + " in server response");
        throw new SyncException(
                SyncException.SERVER_ERROR,
                "Status Tag for " + statusOf + " not found in server response");
    }

    /**
     * <p>Checks response status for the synchronized databases and saves their
     * serverAlerts
     * <p>If this is the first sync for the source, the status code might change
     * according to the value of the PARAM_FIRST_TIME_SYNC_MODE configuration
     * property
     * <p>If firstTimeSyncMode is not set, the alert is left unchanged. If it is
     * set to a value, the specified value is used instead
     *
     * @param msg The message to be checked
     *
     * @throws SyncException In case of errors
     **/
    private void checkServerAlerts(ChunkedString msg) throws SyncException {
        ChunkedString target = null;
        ChunkedString code = null;
        Vector alertTags = null;

        serverAlerts = new Hashtable();

        try {
            alertTags = XmlUtil.getTagValues(
                    XmlUtil.getTagValues(
                    XmlUtil.getTagValues(
                    msg,
                    SyncML.TAG_SYNCML),
                    SyncML.TAG_SYNCBODY),
                    SyncML.TAG_ALERT);

            for (int i = 0,  l = alertTags.size(); i < l; i++) {
                ChunkedString alert = (ChunkedString) alertTags.elementAt(i);
                code = XmlUtil.getTagValue(alert, SyncML.TAG_DATA);
                Vector items = XmlUtil.getTagValues(alert, SyncML.TAG_ITEM);
 
                if (XmlUtil.getTag(alert, "Anchor") != -1) {
                    Log.trace(TAG_LOG, "Server sent its anchor");
                    ChunkedString anchor = XmlUtil.getTagValue(alert, "Anchor");
                    // We expect the Next tag
                    if (XmlUtil.getTag(anchor, "Next") != -1) {
                        ChunkedString next = XmlUtil.getTagValue(anchor, "Next");
                        Log.trace(TAG_LOG, "Server next anchor is: " + next.toString());
                        serverNextAnchor = next.toString();
                    }
                }

                // Get the cmd id
                ChunkedString cmdId = XmlUtil.getTagValue(alert, SyncML.TAG_CMDID);

                for (int j = 0,  m = items.size(); j < m; j++) {
                    ChunkedString targetTag = (ChunkedString) items.elementAt(j);
                    target = XmlUtil.getTagValue(targetTag, SyncML.TAG_TARGET);

                    target = XmlUtil.getTagValue(target, SyncML.TAG_LOC_URI);
                    Log.info(TAG_LOG, "The server alert code for " + target + " is " + code);
                    serverAlerts.put(target.toString(), code.toString());
                }

                // Prepare a SyncMLStatus to be sent in the response
                SyncMLStatus alertStatus = new SyncMLStatus();
                alertStatus.setCmd(SyncML.TAG_ALERT);
                alertStatus.setCmdRef(cmdId.toString());
                alertStatus.setStatus(SyncMLStatus.SUCCESS);
                alertStatus.setMsgRef("" + 1);
                alertStatus.setSrcRef(source.getName());
                alertStatus.setTgtRef(source.getSourceUri());

                // Add this status to the list of items
                statusList.addElement(alertStatus);
            }
        } catch (XmlException xe) {
            Log.error(TAG_LOG, "CheckServerAlerts: error parsing server alert " + msg, xe);
            xe.printStackTrace();
            throw new SyncException(
                    SyncException.SERVER_ERROR,
                    "Invalid alert from server.");
        }
    }

    private void checkSyncHdr(ChunkedString msg) throws SyncException {

        try {
            ChunkedString syncHdr = XmlUtil.getTagValue(msg, SyncML.TAG_SYNCHDR);
            ChunkedString msgId   = XmlUtil.getTagValue(syncHdr, SyncML.TAG_MSGID);

            // Prepare a status for this
            SyncMLStatus syncHdrStatus = new SyncMLStatus();
            syncHdrStatus.setCmd(SyncML.TAG_SYNCHDR);
            syncHdrStatus.setCmdRef("0");
            syncHdrStatus.setStatus(SyncMLStatus.SUCCESS);
            syncHdrStatus.setMsgRef(msgId.toString());
            syncHdrStatus.setTgtRef(deviceId);
            syncHdrStatus.setSrcRef(serverUrl);

            // Add the status to the list
            statusList.addElement(syncHdrStatus);

        } catch (XmlException xe) {
            Log.error(TAG_LOG, "CheckServerAlerts: error parsing server sync header " + msg, xe);
            xe.printStackTrace();
            throw new SyncException(
                    SyncException.SERVER_ERROR,
                    "Invalid sync header from server.");
        }
    }


    /**
     * Prepares inizialization SyncML message
     */
    private String prepareInitializationMessage(int syncMode, boolean requireDevInf, boolean md5Auth)
            throws SyncException {

        String credentials;
        if (md5Auth) {
            // Prepare a MD5 authentication tag
            MD5 md5Computer = new MD5();
            String nonceB64 = config.clientNonce;
            byte nonce[];
            if (nonceB64 == null) {
                nonce = "".getBytes();
            } else {
                // The nonce in the config is b64 encoded
                // (in XML this is a MUST)
                nonce = Base64.decode(nonceB64.getBytes());
            }
            byte md5Token[] = md5Computer.computeMD5Credentials(config.userName,
                                                                config.password,
                                                                nonce);

            credentials = formatter.formatCredentials(new String(md5Token), true);
        } else {
            String login = config.userName + ":" + config.password;
            String b64login = new String(Base64.encode(login.getBytes()));
            credentials = formatter.formatCredentials(b64login, false);
        }

        StringBuffer ret = new StringBuffer();
        ret.append(formatter.formatStartSyncML());

        StringBuffer tags = new StringBuffer(credentials);

        // Meta for the maxmsgsize
        tags.append(formatter.formatMaxMsgSize(maxMsgSize));

        // Add SyncHdr
        String header = formatter.formatSyncHeader(sessionID, getNextMsgID(),
                                                   deviceId, config.userName, serverUrl,
                                                   tags.toString());
        ret.append(header);
        // Add SyncBody
        long nextAnchor = source.getNextAnchor();
        long lastAnchor = source.getLastAnchor();
        int sourceSyncMode = source.getSyncMode();
        String sourceUri = source.getSourceUri();
        SyncFilter filter = source.getFilter();
        String sourceName = source.getName();
        int maxDataSize = maxMsgSize - PROTOCOL_OVERHEAD;
        resetCmdID();
        String alerts = formatter.formatAlerts(getNextCmdID(),
                                               syncMode, nextAnchor,
                                               lastAnchor, sourceSyncMode,
                                               sourceName, sourceUri, filter,
                                               maxDataSize);

        ret.append(formatter.formatStartSyncBody());
        ret.append(alerts);

        // Add DevInf
        if (sendDevInf) {
            ret.append(formatter.formatPutDeviceInfo(getNextCmdID(),
                                                     config.deviceConfig,
                                                     source));
            //reset the flag
            sendDevInf = false;
        }

        // If the user asked for server info, we need to gather them
        if (requireDevInf) {
            ret.append(formatter.formatGetDeviceInfo(getNextCmdID()));
            requireDevInf = false;
        }

        ret.append(formatter.formatFinal());
        ret.append(formatter.formatEndSyncBody());

        ret.append(formatter.formatEndSyncML());

        tags = null;

        return ret.toString();
    }

    /**
     * Process the &lt;Format&gt; tag and return the requested modification
     * in a String array.
     */
    private String[] processFormat(ChunkedString xml) {
        String[] ret = null;

        try {
            if (XmlUtil.getTag(xml, "Format") != -1) {
                ChunkedString format = XmlUtil.getTagValue(xml, "Format");

                if (format != null && !format.equals("")) {
                    ret = StringUtil.split(format.toString(), ";");
                }
            }
        } catch (XmlException e) {
            Log.error(TAG_LOG, "Error parsing format from server: " + xml + ". Ignoring it.", e);
        }
        return ret;
    }



    /**
     * Processes an item in a modification command received from server.
     *
     * @param cmd the cmmand info
     * @param xmlItem the SyncML tag for this item
     * @return the status code for this item
     *
     * @throws SyncException if the command parsing failed
     *
     */
    private SyncMLStatus processSyncItem(SyncMLCommand cmd, ChunkedString xmlItem, String [] formatList)
            throws SyncException {
        int status = 0;
        Chunk chunk = null;
        String guid = null;

        String cmdTag = cmd.getName();

        chunk = sourceLOHandler.getItem(cmd.getType(), xmlItem, formatList, hierarchy);

        if (cmdTag.equals(SyncML.TAG_ADD)) {
            // Save the key sent by server, it will be replaced by the SyncSource
            // with the local UID.
            guid = new String(chunk.getKey());   // Duplicate to avoid leaks!!
            // Preliminary check: don't pass a null item to the SyncSource
            // for add
            if (chunk.hasContent()) {
                Object clientrep[] = new Object[1];
                status = sourceLOHandler.addItem(chunk, clientrep);
                if (SyncMLStatus.isSuccess(status) && !chunk.hasMoreData()) {
                    getSyncListenerFromSource(source).itemReceived(clientrep[0]);
                }
            } else {
                status = SyncMLStatus.GENERIC_ERROR;
            }
            // Update the sync report
            if (!chunk.hasMoreData()) {
                syncReport.addReceivedItem(chunk.getKey(), SyncML.TAG_ADD, status, null);
            }
            if (SyncMLStatus.isSuccess(status) && !chunk.hasMoreData()) {
                mappings.put(new String(chunk.getKey()), guid); // Avoid leaks!!
                if (hierarchy != null) {
                    hierarchy.put(guid, new String(chunk.getKey()));
                }
            }
        } else if (cmdTag.equals(SyncML.TAG_REPLACE)) {
            // Preliminary check: don't pass a null item to the SyncSource
            // for update
            if (chunk.hasContent()) {
                Object clientrep[] = new Object[1];
                status = sourceLOHandler.updateItem(chunk, clientrep);
                if (SyncMLStatus.isSuccess(status) && !chunk.hasMoreData()) {
                    getSyncListenerFromSource(source).itemUpdated(chunk.getKey(), clientrep[0]);
                }
            } else {
                status = SyncMLStatus.GENERIC_ERROR;
            }
            if (!chunk.hasMoreData()) {
                syncReport.addReceivedItem(chunk.getKey(), SyncML.TAG_REPLACE, status, null);
            }
        } else if (cmdTag.equals(SyncML.TAG_DELETE)) {
            status = this.source.deleteItem(chunk.getKey());
            if (SyncMLStatus.isSuccess(status)) {
                getSyncListenerFromSource(source).itemDeleted(chunk.getKey());
            }
            if (!chunk.hasMoreData()) {
                syncReport.addReceivedItem(chunk.getKey(), SyncML.TAG_DELETE, status, null);
            }
        } else {
            Log.error(TAG_LOG, "Invalid command: " + cmd.toString());
        }

        // Init the status object
        SyncMLStatus ret = new SyncMLStatus();
        ret.setCmd(cmdTag);
        ret.setCmdRef(cmd.getCmdId());
        // Save the source ref if present (ADD), otherwise the target ref (UPD & DEL)
        if (guid != null) {
            ret.setSrcRef(guid);
        } else {
            ret.setTgtRef(chunk.getKey());
        }
        ret.setStatus(status);

        return ret;
    }



    /**
     * Processes a modification command received from server,
     * returning the command parts in an Hashtable
     *
     * @param msgRef The messageId tag of the message containing this command
     * @param cmdName the command name
     * @param command the body of the command
     *
     * @return the number of modifications made
     *
     * @throws SyncException if the command parsing failed
     *
     */
    private int processCommand(ChunkedString msgRef, String cmdName, ChunkedString command)
            throws SyncException {

        ChunkedString cmdId = null;
        int i = 0;

        // Get command Id
        try {
            cmdId = XmlUtil.getTagValue(command, SyncML.TAG_CMDID);
        } catch (XmlException e) {
            Log.error(TAG_LOG, "Invalid command Id from server: " + command, e);
            e.printStackTrace();
            throw new SyncException(
                    SyncException.SERVER_ERROR,
                    "Invalid command from server.");
        }

        SyncMLCommand cmd = new SyncMLCommand(cmdName, cmdId.toString());

        try {
            // Get the type of the items for this command, if present
            // otherwise use the type defined for this source.
            int pos = XmlUtil.getTag(command, SyncML.TAG_TYPE);
            String itemType = null;

            if (pos != -1) {
                try {
                    itemType = XmlUtil.getTagValue(command, SyncML.TAG_TYPE).toString();
                //int begin = command.indexOf(">", pos);
                //int end = command.indexOf("</"++">", begin);
                //if(begin != -1 && end != -1) {
                //    itemType = command.substring(begin+1, end).toString();
                //}
                } catch (XmlException xe) {
                    Log.error(TAG_LOG, "Error parsing item type, using default for source.", xe);
                }
            }
            // Set the command type or use the source one
            if (itemType != null) {
                cmd.setType(itemType);
            } else {
                cmd.setType(source.getType());
            }

            // Process format tag (encryption and encoding)
            String[] formatList = this.processFormat(command);

            Vector itemTags = XmlUtil.getTagValues(command, SyncML.TAG_ITEM);
            int len = itemTags.size();

            // Process items
            SyncMLStatus status = null;
            for (i = 0; i < len; i++) {
                status = this.processSyncItem(cmd, (ChunkedString) itemTags.elementAt(i), formatList);
                status.setMsgRef(msgRef.toString());
                statusList.addElement(status);
            }

        } catch (XmlException xe) {
            Log.error(TAG_LOG, "Parse error", xe);
            throw new SyncException(
                    SyncException.SERVER_ERROR,
                    "Error processing command:" + cmdName + " in message " + msgRef);
        }


        return i;
    }

    /**
     * Processes the modifications from the received response from server
     *
     * @param modifications The modification message from server
     * @return true if a response message is required, false otherwise
     * @throws SyncException
     */
    protected boolean processModifications(ChunkedString modifications)
            throws SyncException {
        boolean ret = false;

        ChunkedString msgId = null;
        ChunkedString bodyTag = null;

        try {
            // Check the SyncML tag
            if (XmlUtil.getTag(modifications, SyncML.TAG_SYNCML) == -1) {
                Log.error(TAG_LOG, "Invalid message from server.");
                throw new SyncException(
                        SyncException.SERVER_ERROR,
                        "Invalid message from server.");
            }
            // Process header
            ChunkedString syncHdr = XmlUtil.getTagValue(modifications,
                    SyncML.TAG_SYNCHDR);
            checkSyncHdr(modifications);
            // Get message id
            msgId = XmlUtil.getTagValue(syncHdr, SyncML.TAG_MSGID);

            // Get message body
            bodyTag = XmlUtil.getTagValue(modifications, SyncML.TAG_SYNCBODY);

        } catch (XmlException e) {
            Log.error(TAG_LOG, "Error parsing message: ", e);
            throw new SyncException(
                    SyncException.SERVER_ERROR,
                    "Error parsing message: " + e.getMessage());
        }

        // Process body
        Vector cmdTags = null;
        Vector xmlBody = new Vector(1);
        xmlBody.addElement(bodyTag);

        // Ignore incoming modifications for one way from client modes
        if (alertCode != SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT &&
            alertCode != SyncML.ALERT_CODE_REFRESH_FROM_CLIENT &&
            alertCode != SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW) {

            try {

                // If the server sends a <Sync> tag, the message contains
                // modifications, otherwise it contains only status.
                if (XmlUtil.getTag(bodyTag, SyncML.TAG_SYNC) != - 1) {

                    ChunkedString syncTag = XmlUtil.getTagValue(bodyTag, SyncML.TAG_SYNC);

                    ret = true;
                    processSyncCommand(msgId, syncTag);
                    bodyTag = null;

                    // Process commands, one kind at a time, in the order
                    // defined above
                    for (int c = 0; c < cmdProcessingOrder.length; c++) {
                        int count = 0;
                        Log.debug(TAG_LOG, "Processing " + cmdProcessingOrder[c] + " commands");
                        cmdTags = XmlUtil.getTagValues(xmlBody, cmdProcessingOrder[c]);

                        for (int i = 0,  l = cmdTags.size(); i < l; i++) {
                            ChunkedString command = (ChunkedString) cmdTags.elementAt(i);
                            count += processCommand(msgId, cmdProcessingOrder[c], command);

                            command = null;

                        }
                        cmdTags = null;

                        Log.info(TAG_LOG, cmdProcessingOrder[c] + ": " + count + " items processed");
                    }

                }
            } catch (XmlException e) {
                Log.error(TAG_LOG, "Error parsing command: ", e);
            }
        }

        try {
            // Process status commands
            cmdTags = XmlUtil.getTagValues(xmlBody, SyncML.TAG_STATUS);
            SyncMLStatus status = null;
            for (int i = 0,  l = cmdTags.size(); i < l; i++) {
                status = SyncMLStatus.parse((ChunkedString) cmdTags.elementAt(i));
                if (status != null) {
                    String cmd = status.getCmd();
                    Log.debug(TAG_LOG, "Processing Status for <" + cmd + "> command.");

                    // Check sync status
                    if (isSyncCommand(cmd)) {

                        // In case of error we throw a SyncException
                        if (!SyncMLStatus.isSuccess(status.getStatus())) {
                            String msg = "Server responded " + status.getStatus() +
                                         " to command " + cmd + " [" +
                                         status.getStatusDataMessage() + "]";
                            Log.error(TAG_LOG, msg);
                            SyncException exc;

                            switch(status.getStatus()) {
                                case SyncMLStatus.SERVER_BUSY:
                                    // 503
                                    exc = new SyncException(SyncException.SERVER_BUSY, msg);
                                    break;
                                case SyncMLStatus.PROCESSING_ERROR:
                                    // 506
                                    exc = new SyncException(SyncException.BACKEND_ERROR, msg);
                                    break;
                                case SyncMLStatus.BACKEND_AUTH_ERROR:
                                    // 511
                                    exc = new SyncException(SyncException.BACKEND_AUTH_ERROR, msg);
                                    break;
                                default:
                                    // All error codes should be trapped by the above
                                    // cases, but to be conservative we leave this
                                    // fallback
                                    exc = new SyncException(SyncException.SERVER_ERROR, msg);
                                    break;
                            }
                            throw exc;
                        }
                    } else if (isMappingCommand(cmd)) {
                        //
                        // The status of Map commands is ignored
                        //
                    } else if (isPutCommand(cmd)) {
                        //
                        // The status of Put commands is ignored
                        //
                    } else {
                        // Otherwise, pass it to the source
                        String[] items = status.getItemKeys();
                        int code = status.getStatus();
                        // Check if it's a multi-item response
                        if (items != null) {
                            for (int j = 0,  n = items.length; j < n; j++) {
                                if (SyncML.TAG_ADD.equals(cmd) || SyncML.TAG_REPLACE.equals(cmd) ||
                                    SyncML.TAG_DELETE.equals(cmd))
                                {
                                    // The sync source is unware of chunks, it
                                    // is only interested at items
                                    if (code != SyncMLStatus.CHUNKED_ITEM_ACCEPTED) {
                                        source.setItemStatus(items[j], code);
                                        syncReport.addSentItem(items[j], cmd, code, null);
                                    }
                                } else {
                                    source.setItemStatus(items[j], code);
                                }
                            }
                        } else {
                            // The chunk accepted status (213) is not
                            // propagated to the source, because the source
                            // has no knowledge/visibility of the individual
                            // chunks
                            if (SyncML.TAG_ADD.equals(cmd) || SyncML.TAG_REPLACE.equals(cmd) ||
                                    SyncML.TAG_DELETE.equals(cmd))
                            {
                                if (code != SyncMLStatus.CHUNKED_ITEM_ACCEPTED) {
                                    source.setItemStatus(status.getRef(), code);
                                    syncReport.addSentItem(status.getRef(), cmd, code, null);
                                }
                            } else {
                                source.setItemStatus(status.getRef(), code);
                            }
                        }
                    }

                    status = null;

                } else {
                    Log.error(TAG_LOG, "Error in Status command.");
                }
            }
        } catch (XmlException e) {
            Log.error(TAG_LOG, "Error parsing status: ", e);
        }
        modifications = null;
        return ret;
    }

    private boolean isSyncCommand(String cmd) {
        return cmd.equals(SyncML.TAG_SYNCHDR) || cmd.equals(SyncML.TAG_SYNC);
    }

    private boolean isMappingCommand(String cmd) {
        return cmd.equals(SyncML.TAG_MAP);
    }

    private boolean isPutCommand(String cmd) {
        return cmd.equals(SyncML.TAG_PUT);
    }


    /**
     * Prepares the modification message in SyncML.
     *
     * @return the formatted message
     */
    private String prepareModificationMessage() throws SyncException {

        // Reset the cmd id for this message
        resetCmdID();

        StringBuffer modMsg = new StringBuffer();
        modMsg.append(formatter.formatStartSyncML());

        // Meta
        String meta = formatter.formatMaxMsgSize(maxMsgSize);

        // Sync header
        String syncHdr = formatter.formatSyncHeader(sessionID, getNextMsgID(),
                                                    deviceId, config.userName, serverUrl,
                                                    meta);
        modMsg.append(syncHdr);

        // Sync Body
        modMsg.append(formatter.formatStartSyncBody());

        int msgIdRef = msgID - 1;

        // Add status commands, if any
        appendStatusTags(modMsg);

        // Add mappings if necessary.
        appendMapTag(modMsg);

        if (this.state != STATE_MODIFICATION_COMPLETED) {
            modMsg.append(prepareSyncTag(modMsg.length()));
        }

        //Adding the device capabilities as response to the <Get> command
        //TODO: check if the response from server is the best place to set this
        if (addDevInfResults) {
            String nextCmdId = getNextCmdID();
            String results = formatter.formatResultsDeviceInfo(nextCmdId,
                                                               msgIDget,
                                                               cmdIDget,
                                                               source,
                                                               config.deviceConfig);
            modMsg.append(results);
            //reset the flag
            addDevInfResults = false;
        }

        if (this.state == STATE_MODIFICATION_COMPLETED) {
            Log.info(TAG_LOG, "Modification done, sending <final> tag.");
            modMsg.append(formatter.formatFinal());
        }

        modMsg.append(formatter.formatEndSyncBody());
        modMsg.append(formatter.formatEndSyncML());

        return modMsg.toString();
    }

    private String prepareMappingMessage() {
        return prepareMappingMessage(true);
    }
    
    /**
     * Prepare mapping message
     *
     **/
    private String prepareMappingMessage(boolean isAddStatusEnabled) {

        int i = 0;

        StringBuffer ret = new StringBuffer(formatter.formatStartSyncML());

        // Add SyncHdr
        String syncHdr = formatter.formatSyncHeader(sessionID, getNextMsgID(),
                                                    deviceId, config.userName, serverUrl, null);
        ret.append(syncHdr);

        // Add SyncBody
        ret.append(formatter.formatStartSyncBody());

        // This is used to address the correct MsgIdRef on the outgoing message
        int msgIdRef = msgID - 1;
        // Add Status
        resetCmdID();

        SyncMLStatus syncHdrStatus = new SyncMLStatus();
        syncHdrStatus.setCmdId(getNextCmdID());
        syncHdrStatus.setMsgRef(""+msgIdRef);
        syncHdrStatus.setSrcRef(config.syncUrl);
        syncHdrStatus.setTgtRef(deviceId);
        syncHdrStatus.setCmdRef("0");
        syncHdrStatus.setStatus(SyncMLStatus.SUCCESS);

        String alertStatus = formatter.formatSyncHdrStatus(syncHdrStatus);
        ret.append(alertStatus);

        if (isAddStatusEnabled) {
            appendStatusTags(ret);
        }
        
        appendMapTag(ret);

        ret.append(formatter.formatFinal());
        ret.append(formatter.formatEndSyncBody());
        ret.append(formatter.formatEndSyncML());

        return ret.toString();
    }

    /**
     * Prepares the Map tag if there is some mappings to send, and append
     * it to the given StringBuffer.
     * It cleans up the mapping table at the end.
     *
     * @param out the StringBuffer to append the Map tag to.
     * @return none.
     */
    private void appendMapTag(StringBuffer out) {
        if (mappings.size() == 0) {
            // No mappings to add
            return;
        }

        String mapTag = formatter.formatMappings(getNextCmdID(),
                                                 source.getName(),
                                                 source.getSourceUri(),
                                                 mappings);
        out.append(mapTag);
    }

    /**
     * Prepares the Status tags if there is some status commands to send,
     * and append it to the given StringBuffer.
     * It cleans up the status lost at the end.
     *
     * @param out the StringBuffer to append the Status tags to.
     * @return none.
     */
    private void appendStatusTags(StringBuffer out) {

        int l = statusList.size();
        if (l == 0) {
            // Nothing to send
            return;
        }

        SyncMLStatus status = null;

        // Build status commands...
        for (int idx = 0; idx < l; idx++) {
            status = (SyncMLStatus) statusList.elementAt(idx);
            String cmdId = getNextCmdID();
            status.setCmdId(cmdId);    // update the command id

            // There are different kind of status, we need the right formatting
            // method
            if (SyncML.TAG_ALERT.equals(status.getCmd())) {
                String n;

                if (serverNextAnchor != null) {
                    n = serverNextAnchor;
                } else {
                    n = "" + source.getNextAnchor();
                }
                out.append(formatter.formatAlertStatus(status, n));
            } else if (SyncML.TAG_SYNCHDR.equals(status.getCmd())) {
                out.append(formatter.formatSyncHdrStatus(status));
            } else {
                out.append(formatter.formatItemStatus(status));
            }
        }
        // ...and cleanup the status vector
        statusList.removeAllElements();
    }

    /**
     * Checks if in the response from server a <Get> command is present and that
     * the information required by the server with this command is the device
     * capabilities
     *
     * @param response
     *            The SyncML message received from server
     * @return <code>true</code> if the <Get> tag is present in the message
     *         and the required information is the device capabilities
     */
    private boolean isGetCommandFromServer(ChunkedString response) {
        ChunkedString get = null;
        ChunkedString item = null;
        ChunkedString target = null;
        ChunkedString locUri = null;
        ChunkedString syncHdr = null;

        if (XmlUtil.getTag(response, "Get") == -1) {
            Log.debug(TAG_LOG, "No <Get> command.");
            return false;
        }

        try {
            get = XmlUtil.getTagValue(response, "Get");
            if (get != null) {
                item = XmlUtil.getTagValue(get, "Item");//mandatory if Get present
                target = XmlUtil.getTagValue(item, "Target");
                locUri = XmlUtil.getTagValue(target, "LocURI");
                this.cmdIDget = XmlUtil.getTagValue(get, "CmdID").toString();
                syncHdr = XmlUtil.getTagValue(response, "SyncHdr");
                this.msgIDget = XmlUtil.getTagValue(syncHdr, "MsgID").toString();
            }
        } catch (XmlException e1) {
            Log.error(TAG_LOG, "Invalid get command from server.", e1);
            // TODO: return an error status to the server.
            return false;
        }

        //TODO: check if backward compatibility is required (./devinf11)
        if ("./devinf12".equals(locUri.toString())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method returns the Add command tag.
     */
    private String getAddCommand(int size) throws SyncException {

        StringBuffer cmdTag = new StringBuffer();
        int status = sourceLOHandler.getAddCommand(size, 
                getSyncListenerFromSource(source), cmdTag, cmdID);

        if (status == SyncSourceLOHandler.DONE) {
            nextState(STATE_SENDING_REPLACE);
        } else if (status == SyncSourceLOHandler.FLUSH) {
            nextState(STATE_FLUSHING_MSG);
        }

        String res = cmdTag.toString();
        if (res.length() == 0) {
            return null;
        } else {
            return cmdTag.toString();
        }
    }

    /**
     * This method returns the Replace command tag.
     */
    private String getReplaceCommand(int size) throws SyncException {

        StringBuffer cmdTag = new StringBuffer();
        int status = sourceLOHandler.getReplaceCommand(size, 
                getSyncListenerFromSource(source), cmdTag, cmdID);

        // No item for this source
        if (status == SyncSourceLOHandler.DONE) {
            nextState(STATE_SENDING_DELETE);
        } else if (status == SyncSourceLOHandler.FLUSH) {
            nextState(STATE_FLUSHING_MSG);
        }

        String res = cmdTag.toString();
        if (res.length() == 0) {
            return null;
        } else {
            return cmdTag.toString();
        }
    }

    /**
     * This method returns the Delete command tag.
     */
    private String getDeleteCommand(int size) throws SyncException {

        StringBuffer cmdTag = new StringBuffer();
        boolean done = sourceLOHandler.getDeleteCommand(size, 
                getSyncListenerFromSource(source), cmdTag, cmdID);

        // No item for this source
        if (done) {
            // All new items are donw, go to the next state.
            nextState(STATE_MODIFICATION_COMPLETED);
        }
        String res = cmdTag.toString();
        if (res.length() == 0) {
            return null;
        } else {
            return cmdTag.toString();
        }
    }

    /**
     *  Get the next command tag, with all the items that can be contained
     *  in defined the message size.
     *
     *  @param size
     *
     *  @return the command tag of null if no item to send.
     */
    private String getNextCmdTag(int size) throws SyncException {

        StringBuffer cmdTag = new StringBuffer();
        String uri = source.getSourceUri();

        switch (alertCode) {

            case SyncML.ALERT_CODE_SLOW:
            case SyncML.ALERT_CODE_REFRESH_FROM_CLIENT:
                int status = sourceLOHandler.getNextCommand(size,
                        getSyncListenerFromSource(source), cmdTag, cmdID);
                if (status == SyncSourceLOHandler.DONE) {
                    nextState(STATE_MODIFICATION_COMPLETED);
                } else if (status == SyncSourceLOHandler.FLUSH) {
                    nextState(STATE_FLUSHING_MSG);
                }
                // Check if there are no items, then we signal the end of the
                // sync
                if (cmdTag.toString().length() == 0) {
                    return null;
                }
                break;

            case SyncML.ALERT_CODE_REFRESH_FROM_SERVER:
            case SyncML.ALERT_CODE_ONE_WAY_FROM_SERVER:
                nextState(STATE_MODIFICATION_COMPLETED);
                return null; // no items sent for refresh from server

            case SyncML.ALERT_CODE_FAST:
            case SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT:
            case SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW:
                //
                // Fast Sync or One way from client.
                //
                String command = null;
                switch (state) {
                    case STATE_SENDING_ADD:
                        command = getAddCommand(size);
                        break;
                    case STATE_SENDING_REPLACE:
                        command = getReplaceCommand(size);
                        break;
                    case STATE_SENDING_DELETE:
                        command = getDeleteCommand(size);
                        break;
                    default:
                        return null;
                }
                if (command != null) {
                    cmdTag.append(command);
                }
                break;

            default:
                Log.error(TAG_LOG, "Invalid alert code: " + alertCode);
                throw new SyncException(
                        SyncException.SERVER_ERROR,
                        "Invalid alert code: " + alertCode);
        }

        return cmdTag.toString();
    }

    /**
     * return Sync tag about sourceUri
     *
     * @param records records to sync
     * @param sourceURI source uri
     * @return sync tag value
     */
    private String prepareSyncTag(int size) throws SyncException {

        StringBuffer syncTag = new StringBuffer();
        String cmdTag = null;

        syncTag.append(formatter.formatStartSync());
        syncTag.append(formatter.formatSyncTagPreamble(getNextCmdID(),
                                                       source.getName(),
                                                       source.getSourceUri()));

        do {
            int oldState = state;
            cmdTag = getNextCmdTag(size + syncTag.length());

            // Last command?
            if (cmdTag == null) {
                Log.debug(TAG_LOG, "No more commands to send");
                break;
            }

            // append command tag
            syncTag.append(cmdTag);

            // If the message must be flushed here, we do it, but we restore the
            // previous state so that we can continue afterward
            if (state == STATE_FLUSHING_MSG) {
                Log.info(TAG_LOG, "SyncML msg flushed");
                nextState(oldState);
                break;
            }
        } while (size + syncTag.length() < maxMsgSize);

        syncTag.append(formatter.formatEndSync());

        return syncTag.toString();
    }

    /**
     * Process the Sync command (check the source uri, save the
     * number of changes).
     *
     * @param msgRef message reference
     * @param command xml command to parse
     * @return none
     */
    private void processSyncCommand(ChunkedString msgRef, ChunkedString command)
            throws SyncException {

        String cmdId = null;
        String locuri = null;

        try {
            cmdId = XmlUtil.getTagValue(command, SyncML.TAG_CMDID).toString();
            locuri = XmlUtil.getTagValue(
                    XmlUtil.getTagValue(command, SyncML.TAG_TARGET),
                    SyncML.TAG_LOC_URI).toString();

        } catch (XmlException e) {
            Log.error(TAG_LOG, "Invalid Sync command: ", e);
            e.printStackTrace();
            throw new SyncException(
                    SyncException.SERVER_ERROR,
                    "Invalid Sync command from server.");
        }

        // If this sync is not for this source, throw an exception
        if (!locuri.equals(source.getName())) {
            Log.error(TAG_LOG, "Invalid uri: '" + locuri + "' for source: '" + source.getName() + "'");
            throw new SyncException(
                    SyncException.SERVER_ERROR,
                    "Invalid source to sync: " + locuri);
        }

        int nc = -1;

        // If the Sync contains the number of chages, pass them to
        // the SyncSource, otherwise pass -1
        if (XmlUtil.getTag(command, "NumberOfChanges") != -1) {
            try {
                ChunkedString ncVal = XmlUtil.getTagValue(command, "NumberOfChanges");
                nc = Integer.parseInt(ncVal.toString());
                Log.info(TAG_LOG, "Number of changes from server: " + nc);
            } catch (XmlException xe) {
                Log.error(TAG_LOG, "Error parsing NumberOfChanges, ignoring it.", xe);
            }
        }
        // This is the very first moment we know how many message we're about
        // to receive. This is when we notify the listener about it, even though
        // the receiving phase has already begun.
        getSyncListenerFromSource(source).startReceiving(nc);
        source.setServerItemsNumber(nc);

        // Build the status to the Sync command
        SyncMLStatus status = new SyncMLStatus();
        status.setMsgRef(msgRef.toString());
        status.setCmdRef(cmdId.toString());
        status.setCmd(SyncML.TAG_SYNC);
        status.setTgtRef(source.getName());
        status.setSrcRef(source.getSourceUri());

        statusList.addElement(status);
    }

    /**
     * Returns the server alert code for the given source
     *
     * @param sourceURI the source
     *
     * @return the server alert code for the given source or -1 if it is not
     *         found/parsable
     */
    private int getSourceAlertCode(String sourceURI) {

        try {
            String alert = (String) serverAlerts.get(sourceURI);
            return Integer.parseInt(alert);
        } catch (Throwable t) {
            Log.error(TAG_LOG, "ERROR: unrecognized server alert code ("
                      + serverAlerts.get(sourceURI) + ") for " + sourceURI.toString(), t);
        }

        return -1;
    }

    // Reset the message ID counter.
    private void resetMsgID() {
        msgID = 0;
    }

    // Return the next message ID to use.
    private String getNextMsgID() {
        return String.valueOf(++msgID);
    }

    // Reset the command ID counter.
    private void resetCmdID() {
        cmdID.setValue(0);
    }

    // Return the next message ID to use.
    public String getNextCmdID() {
        return String.valueOf(cmdID.next());
    }

    private void nextState(int state) {
        this.state = state;
        String msg = null;

        if (Log.getLogLevel() >= Log.DEBUG) {
            switch (state) {
                case STATE_SENDING_ADD:
                    msg = "state=>STATE_SENDING_ADD";
                    break;
                case STATE_SENDING_REPLACE:
                    msg = "state=>STATE_SENDING_REPLACE";
                    break;
                case STATE_SENDING_DELETE:
                    msg = "state=>STATE_SENDING_DELETE";
                    break;
                case STATE_MODIFICATION_COMPLETED:
                    msg = "state=>STATE_MODIFICATION_COMPLETED";
                    break;
                case STATE_FLUSHING_MSG:
                    msg = "state=>STATE_FLUSHING_MSG";
                    break;
                default:
                    msg = "UNKNOWN STATE!";
            }
            Log.debug(TAG_LOG, msg);
        }
    }

    private void cancelSync() throws SyncException
    {
        Log.info(TAG_LOG, "Cancelling sync for source ["+source.getName()+"]");
        throw new SyncException(SyncException.CANCELLED, "SyncManager sync got cancelled");
    }

    private boolean isSyncToBeCancelled() {
        return cancel;
    }

    private SyncListener getSyncListenerFromSource(SyncSource source) {
        SyncListener slistener = source.getListener();
        
        if(slistener != null) {
            return slistener;
        } else {
            return basicListener;
        }
    }

    private int getListenerStatusFromSourceStatus(int status) {
        int syncStatus;
        switch(status) {
            case SyncSource.STATUS_SUCCESS:
                syncStatus = SyncListener.SUCCESS;
                break;
            case SyncSource.STATUS_SEND_ERROR:
                syncStatus = SyncListener.ERROR_SENDING_ITEMS;
                break;
            case SyncSource.STATUS_RECV_ERROR:
                syncStatus = SyncListener.ERROR_RECEIVING_ITEMS;
                break;
            case SyncSource.STATUS_SERVER_ERROR:
            case SyncSource.STATUS_CONNECTION_ERROR:
            default:
                syncStatus = SyncListener.GENERIC_ERROR;
        }
        return syncStatus;
    }

    private int getListenerStatusFromSyncException(SyncException se) {
        Log.trace(TAG_LOG, "getting listener status for " + se.getCode());
        int syncStatus;
        switch (se.getCode()) {
            case SyncException.AUTH_ERROR:
                syncStatus = SyncListener.INVALID_CREDENTIALS;
                break;
            case SyncException.FORBIDDEN_ERROR:
                syncStatus = SyncListener.FORBIDDEN_ERROR;
                break;
            case SyncException.CONN_NOT_FOUND:
                syncStatus = SyncListener.CONN_NOT_FOUND;
                break;
            case SyncException.READ_SERVER_RESPONSE_ERROR:
                syncStatus = SyncListener.READ_SERVER_RESPONSE_ERROR;
                break;
            case SyncException.WRITE_SERVER_REQUEST_ERROR:
                syncStatus = SyncListener.WRITE_SERVER_REQUEST_ERROR;
                break;
            case SyncException.SERVER_CONNECTION_REQUEST_ERROR:
                syncStatus = SyncListener.SERVER_CONNECTION_REQUEST_ERROR;
                break;
            case SyncException.BACKEND_AUTH_ERROR:
                syncStatus = SyncListener.BACKEND_AUTH_ERROR;
                break;
            case SyncException.NOT_FOUND_URI_ERROR:
                syncStatus = SyncListener.URI_NOT_FOUND_ERROR;
                break;
            case SyncException.CONNECTION_BLOCKED_BY_USER:
                syncStatus = SyncListener.CONNECTION_BLOCKED_BY_USER;
                break;
            case SyncException.SMART_SLOW_SYNC_UNSUPPORTED:
                syncStatus = SyncListener.SMART_SLOW_SYNC_UNSUPPORTED;
                break;
            case SyncException.CLIENT_ERROR:
                syncStatus = SyncListener.CLIENT_ERROR;
                break;
            case SyncException.ACCESS_ERROR:
                syncStatus = SyncListener.ACCESS_ERROR;
                break;
            case SyncException.DATA_NULL:
                syncStatus = SyncListener.DATA_NULL;
                break;
            case SyncException.ILLEGAL_ARGUMENT:
                syncStatus = SyncListener.ILLEGAL_ARGUMENT;
                break;
            case SyncException.SERVER_ERROR:
                syncStatus = SyncListener.SERVER_ERROR;
                break;
            case SyncException.SERVER_BUSY:
                syncStatus = SyncListener.SERVER_BUSY;
                break;
            case SyncException.BACKEND_ERROR:
                syncStatus = SyncListener.BACKEND_ERROR;
                break;
            case SyncException.CANCELLED:
                syncStatus = SyncListener.CANCELLED;
                break;
            case SyncException.ERR_READING_COMPRESSED_DATA:
                syncStatus = SyncListener.COMPRESSED_RESPONSE_ERROR;
                break;
            case SyncException.DEVICE_FULL:
                syncStatus = SyncListener.DEVICE_FULL_ERROR;
                break;
            default:
                syncStatus = SyncListener.GENERIC_ERROR;
                break;
        }
        return syncStatus;
    }

    private void releaseResources() {
        // Release resources
        this.mappings = null;
        this.hierarchy = null;
        this.statusList = null;

        this.source = null;
        this.sessionID = null;
        this.serverUrl = null;

        this.busy = false;
    }

    private void generateStatusForCommand(ChunkedString xmlCommand, String command,
                                          int code, int msgRef) throws XmlException
    {
        if (! hasNoResp(xmlCommand)) {
            SyncMLStatus status = new SyncMLStatus();
            status.setCmd(command);
            ChunkedString cmdId = XmlUtil.getTagValue(xmlCommand, SyncML.TAG_CMDID);
            if (cmdId != null) {
                status.setCmdRef(cmdId.toString());
            }
            status.setStatus(code);
            status.setMsgRef("" + msgRef);

            ChunkedString source = XmlUtil.getTagValue(xmlCommand, SyncML.TAG_SOURCE);
            if (source != null) {
                ChunkedString locURI = XmlUtil.getTagValue(source, SyncML.TAG_LOC_URI);
                if (locURI != null) {
                    status.setSrcRef(locURI.toString());
                }
            }

            // Add this status to the list of items
            statusList.addElement(status);
        }
    }

    private boolean hasNoResp(ChunkedString command) {
        return XmlUtil.getTag(command, SyncML.TAG_NORESP) != -1;
    }


    
    /**
     * Enable or disable the mappings for test purposes only
     */
    public void enableMappingTest(boolean isMappingTestDisabled) {
        this.isMappingTestDisabled=isMappingTestDisabled;
    }
}

