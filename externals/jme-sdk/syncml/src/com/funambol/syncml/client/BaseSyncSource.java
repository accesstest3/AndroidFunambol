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

import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.syncml.spds.SyncException;
import com.funambol.syncml.spds.SyncListener;

import com.funambol.syncml.protocol.SyncFilter;
import com.funambol.syncml.protocol.SyncML;

import com.funambol.util.Log;

/**
 * An abstract implementation of the <i>SyncSource</i> interface, providing
 * the basic framework each SyncSource has to implement. 
 * A developer can choose to extends BaseSyncSource or to implements
 * SyncSource directly if needed.
 *
 * The class BaseSyncSource uses the SyncConfig to store the source 
 * configuration data. With this class is possible to alter the source
 * configuration, which is not permitted by the SyncSource interface.
 */
public abstract class BaseSyncSource implements SyncSource {
    
    //--------------------------------------------------------------- Attributes

    /** SyncSource configuration */
    protected SourceConfig config;

    /** Synchronization filter */
    protected SyncFilter filter;

    /** SyncMode, set by beginSync */
    protected int syncMode;

    // Item lists
    protected SyncItem[] allItems, newItems, updItems, delItems;

    // Lists counters
    protected int allIndex, newIndex, updIndex, delIndex;

    /** The number of items to be sent to the server in the session */
    private int clientItemsNumber;
    
    /** The number of items that the server announced to send in the session */
    private int serverItemsNumber;

    /** The number of new items to be sent to the server in the session */
    private int clientAddItemsNumber;

    /** The number of replaced items to be sent to the server in the session */
    private int clientReplaceItemsNumber;

    /** The number of deleted items to be sent to the server in the session */
    private int clientDeleteItemsNumber;

    /** Status of the sync source summarized in an integer value. See constants
     * defined in SyncSource */
    protected int globalStatus;

    /** Listener of the sync process */
    private SyncListener listener;

    //------------------------------------------------------------- Constructors

    /**
     * BaseSyncSource constructor: initialize source config
     */
    public BaseSyncSource(SourceConfig config) {
        this.config = config;
        
        syncMode = 0;
        
        // Init lists (empty)
        allItems = null;
        newItems = null;
        updItems = null;
        delItems = null;

        // Init counters
        allIndex = newIndex = updIndex = delIndex = 0;

        // Init number of chages counters
        clientItemsNumber = serverItemsNumber = -1;
        clientAddItemsNumber = -1;
        clientReplaceItemsNumber = -1;
        clientDeleteItemsNumber = -1;

        filter = null;
    }

    //----------------------------------------------------------- Public Methods

    /**
     * Returns the config of the source. The client can use this method
     * to obtain the config object and change some parameter. A setConfig()
     * must be called to actually change the source configuration.
     *
     * @return the config of the source
     */
    public SourceConfig getConfig() {
        return config;
    }

    /**
     * Sets the config of the source. The client can use this method
     * to change the config of the source configuration.
     * This operation should not be done while the sync is in progress.
     *
     */
    public void setConfig(SourceConfig config) {
        this.config = config;
    }
    
    //------------------------------------------------ SyncSource implementation

    /**
     * Returns the name of the source
     *
     * @return the name of the source
     */
    public String getName() {
        return config.getName();
    }

    /**
     * Returns the source URI
     *
     * @return the absolute URI of the source
     */
    public String getSourceUri() {
        return config.getRemoteUri();
    }

    /**
     * Returns the type of the source.
     * The types are defined as mime-types, for instance * text/x-vcard).
     * @return the type of the source
     */
    public String getType() {
        return config.getType();
    }

    /**
     * Returns the encoding of the source.
     * The encoding can be 'b64' or 'none' only. The standard defines
     * also 'des' and '3des' but they are not implemented in this version
     * of the APIs.
     *
     * @return the encoding of the source
     */
    public String getEncoding() {
        return config.getEncoding();
    }

    /**
     * Returns the preferred sync mode of the source.
     * The preferred sync mode is the one that the SyncManager sends
     * to the server in the initialization phase. The server can respond
     * with a different alert code, to force, for instance, a slow.
     *
     * @return the preferred sync mode for this source
     */
    public int getSyncMode() {
        return config.getSyncMode();
    }

    /**
     * Returns the current filter for this SyncSource.
     */
    public SyncFilter getFilter() {
        return filter;
    }

    /**
     * Set a new filter for this SyncSource
     */
    public void setFilter(SyncFilter filter) {
        this.filter = filter;
    }

    /**
     * Add a new SyncItem to this source backend.
     * The item key after a successful add must contain the local UID,
     * that is used by the engine to send the mappings to the server.
     * The source must then change the item key accordingly before return.
     *
     * @param item the SyncItem to add, with the GUID sent by the server.
     *             The source is resposible to set it to the LUID before
     *             returning a successful status code.
     *
     * @return the status code of the operation. It will be returned to
     *         the server in the response for this item.
     *
     * @throws SyncException if an unrecoverable error occur, to stop the sync
     */
    public abstract int addItem(SyncItem item) throws SyncException ;
    
    /**
     * Update a given SyncItem stored in the source backend.
     *
     * @param item the SyncItem to update. The key of the item is already
     *             the LUID.
     *
     * @return the status code of the operation. It will be returned to
     *         the server in the response for this item.
     *
     * @throws SyncException if an unrecoverable error occur, to stop the sync
     */
    public abstract int updateItem(SyncItem item) throws SyncException ;
    
    /**
     * Delete a SyncItem stored in the source backend.
     *
     * @param key The key of the item to delete.
     *
     * @return the status code of the operation. It will be returned to
     *         the server in the response for this item.
     *
     * @throws SyncException if an unrecoverable error occur, to stop the sync
     */
    public abstract int deleteItem(String key) throws SyncException ;
    
    /** 
     * Returns the next item of the store.
     **/
    public SyncItem getNextItem() throws SyncException {
        if (allItems == null) {
            Log.info("Source "+getName()+": no items to send for slow sync");
            return null;
        }
        
        if (allIndex<allItems.length) {
            Log.info("Source "+getName()+": sending item "
                     +allItems[allIndex].getKey());
            SyncItem ret = getItemContent(allItems[allIndex]);
            allIndex++;
            return ret;
        }
        else {
            Log.info("Source "+getName()+": no more items to send for slow sync");
            // All Items sent, we can free memory
            allItems = null;
            allIndex = 0;
            return null;
        }
    }
    
    /**
     * Returns the next new item of the store
     * (not yet sent to the server)
     */
    public SyncItem getNextNewItem() throws SyncException {
        if (newItems == null) {
            Log.info("Source "+getName()+": no new items to send");
            return null;
        }
        
        if (newIndex<newItems.length) {
            Log.info("Source "+getName()+": sending item "
                     +newItems[newIndex].getKey());
            SyncItem ret = getItemContent(newItems[newIndex]);
            // Move to the next item only if this one has been sent completely.
            newIndex++;
            return ret;
            
        }
        else {
            Log.info("Source "+getName()+": no more new items to send");
            // All Items sent, we can free memory
            newItems = null;
            newIndex = 0;
            return null;
        }
    }
    
    
    /**
     * Returns the first/next updated item of the store
     * (changed from the last sync)
     */
    public SyncItem getNextUpdatedItem() throws SyncException {
        if (updItems == null) {
            Log.info("Source "+getName()+": no updated items to send");
            return null;
        }
        
        if (updIndex<updItems.length) {
            Log.info("Source "+getName()+": sending item "
                     +updItems[updIndex].getKey());
            SyncItem ret = getItemContent(updItems[updIndex]);
            // Move to the next item only if this one has been sent completely.
            updIndex++;
            return ret;
            
        }
        else {
            Log.info("Source "+getName()+": no more updated items to send");
            // All Items sent, we can free memory
            updItems = null;
            updIndex = 0;
            return null;
        }
    }
    
    /** 
     * Returns a SyncItem containing the key of the first/next
     * deleted item of the store (locally removed after the last sync,
     * but not yet deleted on server)
     */
    public SyncItem getNextDeletedItem() throws SyncException {
        if (delItems == null) {
            Log.info("Source "+getName()+": no deleted items to send");
            return null;
        }
        
        if (delIndex<delItems.length) {
            Log.info("Source "+getName()+": sending item "
                     +delItems[delIndex].getKey());
            // No need to get the content here
            return delItems[delIndex++];
        }
        else {
            Log.info("Source "+getName()+": no more deletetd items to send");
            // All Items sent, we can free memory
            delItems = null;
            delIndex = 0;
            return null;
        }
    } 
    
    /**
     * Tell the SyncSource the status returned by the server 
     * for an Item previously sent.
     * This is a dummy implementation that just logs the status.
     * A concrete implementation can override this method to perform
     * some checks on the received status.
     *
     * @param key the key of the item
     * @param status the status code received for that item
     *
     * @throws SyncException if the SyncSource wants to stop the sync
     */
    public void setItemStatus(String key, int status)
    throws SyncException {
        Log.info("Status " + status + "for item " + key + "from server.");
    }

    /**
     * Return the number of changes that the client will send during the
     * session. This method, after the beginSync() call, should return
     * the number of items to be sent to the server.
     *
     * The number of changes is computed by initXXXItems() during beginSync().
     *
     * @return number of items to sent, or -1 if unknown
     */
    public int getClientItemsNumber() {
        return clientItemsNumber;
    }

    /**
     * Return the number of new items (add) that the client will send during the
     * session. This method, after the beginSync() call, should return
     * the number of new items to be sent to the server.
     *
     * The number of changes is computed by initXXXItems() during beginSync().
     *
     * @return number of items to sent, or -1 if unknown
     */
    public int getClientAddNumber() {
        return clientAddItemsNumber;
    }

    /**
     * Return the number of replaced items that the client will send during the
     * session. This method, after the beginSync() call, should return
     * the number of replaced items to be sent to the server.
     *
     * The number of changes is computed by initXXXItems() during beginSync().
     *
     * @return number of items to sent, or -1 if unknown
     */
    public int getClientReplaceNumber() {
        return clientReplaceItemsNumber;
    }

    /**
     * Return the number of deleted items that the client will send during the
     * session. This method, after the beginSync() call, should return
     * the number of delted items to be sent to the server.
     *
     * The number of changes is computed by initXXXItems() during beginSync().
     *
     * @return number of items to sent, or -1 if unknown
     */
    public int getClientDeleteNumber() {
        return clientDeleteItemsNumber;
    }


    /**
     * Return the number of changes that the server will send during the
     * session. This method, after the beginSync() call, should return
     * the number of items to be sent to the server.
     *
     * @return number of changes from the server, or -1 if not announced.
     */
    public int getServerItemsNumber() {
        return serverItemsNumber;
    }

    /**
     * Set the number of changes that the server will send during the
     * session. This method is called by the engine to notify the Source
     * of the number of changes announced by the server. If the server
     * does not announce the number of changes, the engine will call
     * this method with parameter -1.
     *
     * @param number of changes from the server, or -1 if not announced.
     */
    public void setServerItemsNumber(int number) {
        serverItemsNumber = number;
    }

    /**
     * Default implementation for
     */
    public void dataReceived(String date, int size) {
        Log.info("Received " + size + "bytes.");
    }


    /** 
     * Return the Last Anchor for this source
     */
    public long getLastAnchor() {
        return config.getLastAnchor();
    }
    
    /** 
     * Set the value of the Last Anchor for this source
     */
    public void setLastAnchor(long time) {
        config.setLastAnchor(time);
    }
    
    /** 
     * Return the Next Anchor for this source
     */
    public long getNextAnchor() {
        return config.getNextAnchor();
    }
    
    /** 
     * Set the value of the Next Anchor for this source
     */
    public void setNextAnchor(long time) {
        config.setNextAnchor(time);
    }

    /**
     * Called after SyncManager preparation and initialization just before start
     * the synchronization of the SyncSource.
     *
     * @param syncMode the synchronization type: one of the values in
     *                 sync4j.framework.core.AlertCode
     *
     * @throws SyncException in case of error. This will stop the sync process
     */
    public void beginSync(int syncMode) throws SyncException {
        Log.info("Begin sync for source '" + getName() +
                 "' with mode " + syncMode);

        // Init lists
        switch(syncMode) {
            case SyncML.ALERT_CODE_SLOW:
            case SyncML.ALERT_CODE_REFRESH_FROM_CLIENT:
                // A refresh from client is like a slow here
                initAllItems();
                allIndex = 0;
                // Init number of changes counter
                clientItemsNumber = (allItems != null) ? allItems.length : 0 ;
                clientAddItemsNumber =
                                    (newItems != null) ? newItems.length : 0 ;
                clientReplaceItemsNumber =
                                    (updItems != null) ? updItems.length : 0 ;
                clientDeleteItemsNumber =
                                    (delItems != null) ? delItems.length : 0 ;
                break;
            case SyncML.ALERT_CODE_FAST:
            case SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT:
            case SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW: 
                // A one way from client is like a fast here
                initNewItems();
                initUpdItems();
                initDelItems();
                newIndex = updIndex = delIndex = 0;
                // Init number of changes counter
                clientAddItemsNumber =
                                    (newItems != null) ? newItems.length : 0 ;
                clientReplaceItemsNumber =
                                    (updItems != null) ? updItems.length : 0 ;
                clientDeleteItemsNumber =
                                    (delItems != null) ? delItems.length : 0 ;

                clientItemsNumber = clientAddItemsNumber +
                                    clientReplaceItemsNumber +
                                    clientDeleteItemsNumber;
                break;
            case SyncML.ALERT_CODE_ONE_WAY_FROM_SERVER:
                // No modifications to send (it's not
                // strictly necessary to reset the lists,
                // because the engine will not ask items to
                // the SyncSource, but it's good to do it)
                newItems = null;
                updItems = null;
                delItems = null;
                newIndex = updIndex = delIndex = 0;
                // Init number of changes counter
                clientItemsNumber = 0;
                clientAddItemsNumber = 0;
                clientReplaceItemsNumber = 0;
                clientDeleteItemsNumber = 0;
                break;
            case SyncML.ALERT_CODE_REFRESH_FROM_SERVER:
                // In this case, the SyncSource should
                // delete all the items in the database
                // (possibly asking the user before that)
                // No modifications to send.
                newItems = null;
                updItems = null;
                delItems = null;
                newIndex = updIndex = delIndex = 0;
                // Init number of changes counter
                clientItemsNumber = 0;
                clientAddItemsNumber = 0;
                clientReplaceItemsNumber = 0;
                clientDeleteItemsNumber = 0;
                break;
            default:
                throw new SyncException(SyncException.SERVER_ERROR,
                                        "SyncSource "+getName()+
                                        ": invalid sync mode "+getSyncMode());
        }

        this.syncMode = syncMode;
    }

    /**
     * Called just before committing the synchronization process by the
     * SyncManager. The SyncSource can stop the commit phase raising an
     * exception here.
     *
     * @throws SyncException in case of error, to stop the commit.
     */
    public void endSync() throws SyncException  {
        Log.info("End sync for source " + getName());
        // Release resources
        allItems = newItems = updItems = delItems = null;
        allIndex = newIndex = updIndex = delIndex = 0;
    }

    /**
     * Set a sync listener.
     *
     * @param listener the listener or null to remove it
     */
    public void setListener(SyncListener listener) {

        this.listener = listener;
    }

    /**
     * Returns the current listener (or null if not set)
     */
    public SyncListener getListener() {

        return listener;
    }

    /**
     * Returns the status of the sync source. The status is encoded as a bit
     * mask of the STATUS_* values
     */
    public int getStatus() {

        return globalStatus;
    }


    /**
     * Creates a new SyncItem for the engine to store incoming items
     */
    public SyncItem createSyncItem(String key, String type, char state,
                                   String parent, long size) {

        SyncItem item = new SyncItem(key, type, state, parent);
        return item;
    }


    /* ----------------------------------------------------------------------
     * The following methods must be implemented by the concrete 
     * implementation of BaseSyncSource to perform the real modification
     * detection, based on the source type.
     */

    /**
     * In a concrete implementation, this function should search the database
     * for all the items present and store their keys.
     *
     * @throws SyncException implementation can throw a SyncException 
     *                       to stop the sync on fatal errors.
     */
    protected abstract void initAllItems() throws SyncException;

    /**
     * In a concrete implementation, this function should search the database
     * for the new items present and store their keys.
     *
     * @throws SyncException implementation can throw a SyncException 
     *                       to stop the sync on fatal errors.
     */
    protected abstract void initNewItems() throws SyncException;

    /**
     * In a real implementation, this function should search the database
     * for the modified items present and store their keys.
     * The policy to detect a change can vary from one source to another:
     * from generating a CRC to keep the status in a field of the item in
     * the backend database.
     *
     * @throws SyncException implementation can throw a SyncException 
     *                       to stop the sync on fatal errors.
     */
    protected abstract void initUpdItems() throws SyncException ;

    /**
     * In a real implementation, this function should search the database
     * for the deleted items present and store their keys.
     * The policy to detect a deleted item can vary from one source to another:
     * from keeping a list of items after the last sync to keep the items with
     * a deleted flag and then remove them after the successful deletion on
     * the server.
     *
     * @throws SyncException implementation can throw a SyncException 
     *                       to stop the sync on fatal errors.
     */
    protected abstract void initDelItems() throws SyncException ;

    /**
     * This function gets the item content in the backend database and
     * returns a complete item. The parameter item is marked final because
     * should not be used for the filled item: it is a reference to the
     * array entry, and filling it would cause the array to keep all the
     * filled items at the end (the gc will not dispose them). <p>
     * The content of the item depends also from the encoding of this
     * SyncSource:
     * <li> if the encoding is <i>none</i>, it must be a String, converted
     *      with getBytes(), so the engine will send it unchanged.
     * <li> if the encoding is <i>b64</i>, the content can be binary, and the 
     *      type should be set accordingly, so that the receiving source
     *      can handle it. In this way, the binary content is transferred
     *      encoded in the SyncML message. This encoding can be applied to
     *      a test item too, to avoid problems with charset or other, like
     *      what is done with the SIF format.
     */
    protected abstract SyncItem getItemContent(final SyncItem item)
    throws SyncException ;

}

