/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2003-2007 Funambol, Inc.
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

import java.util.Hashtable;

import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.spds.SyncException;

import com.funambol.util.Log;

/**
 * A test implementation of the <i>SyncSource</i> interface, with dummy
 * items exchanged with the server.
 *
 * This implementation shows how a SyncSource can keep the lists of items
 * to exchange during the session without actually have all the items in
 * memory.
 * This policy is useful when the items can be very big, like emails or
 * files.
 */
public class TestSyncSource extends BaseSyncSource {
    
    //--------------------------------------------------------------- Attributes

    private Hashtable status;

    public int ITEMS_NUMBER = 300;
    public int counter;

    //------------------------------------------------------------- Constructors

    /**
     * BaseSyncSource constructor: initialize source config and
     * init all the rest to null. The real initialization is done by
     * the beginSync method.
     */
    public TestSyncSource(SourceConfig config) {
        super(config);

        // Initialized in beginSync
        status = null;
    }

    //----------------------------------------------------------- Public Methods

    public int getCounter() {
      return counter;
    }
    /**
     * Logs the new item from the server.
     */
    public int addItem(SyncItem item) {
        Log.info("New item " + item.getKey() + " from server.");

        // Create a fake LUID and set it in the item
        String luid = item.getKey() + "_luid";
        item.setKey(luid);
        return 200;
    }
    
    /** Update a given SyncItem stored on the source backend */
    public int updateItem(SyncItem item) {
        Log.info("Updated item " + item.getKey() + " from server.");

        if(syncMode == SyncML.ALERT_CODE_REFRESH_FROM_CLIENT ||
           syncMode == SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT ) {
            Log.error("Server is trying to update items for a one way sync! "+
                      "(syncMode: "+syncMode+")");
            return 500;
        }

        return 200;
    }
    
    /** Delete a SyncItem stored on the related Items list */
    public int deleteItem(String key) {
        Log.info("Delete from server for item " + key);
        
        if(syncMode == SyncML.ALERT_CODE_REFRESH_FROM_CLIENT ||
           syncMode == SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT ) {
            Log.error("Server is trying to delete items for a one way sync! "+
                      "(syncMode: "+syncMode+")");
            return 500;
        }


        return 200;
    }
    
    /**
     * Tell the SyncSource the status returned by the server 
     * for an Item previously sent.
     *
     * @param key the key of the item
     * @param status the status code received for that item
     *
     * @throws SyncException if the SyncSource wants to stop the sync
     */
    public void setItemStatus(String key, int status)
    throws SyncException {
        Log.info("Status " + status + " for item " + key + " from server.");
        // Here we can do something in case the status is okay, or
        // in case of error. In this test we only keep the status codes.
        counter++;
        this.status.put(key, new Integer(status));
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
        super.beginSync(syncMode);
        this.status = new Hashtable();
    }
    

    //-------------------------------------------------------- Private methods

    /**
     * In a real implementation, this function should search the database
     * for all the items present and store their keys.
     */
    protected void initAllItems() {
        int allTotal = ITEMS_NUMBER;

        allItems = new SyncItem[allTotal];

        for(int i=0; i<allTotal; i++) {
            allItems[i] = new SyncItem("Item"+i);
        }
    }

    /**
     * In a real implementation, this function should search the database
     * for the new items present and store their keys.
     */
    protected void initNewItems() {
        int newTotal = 5;

        newItems = new SyncItem[newTotal];

        // Items are created with a key different from the all list.
        // This simulates the add of 5 items to the 10 present at the
        // previous sync
        for(int i=0; i<newTotal; i++) {
            String name="Item"+(i+11);
            newItems[i] = new SyncItem(name, getType(),
                                       SyncItem.STATE_NEW, null, null);
        }
    }

    /**
     * In a real implementation, this function should search the database
     * for the modified items present and store their keys.
     * The policy to detect a change can vary from one source to another:
     * from generating a CRC to keep the status in a field of the item in
     * the backend database.
     */
    protected void initUpdItems() {
        int updTotal = 3;

        updItems = new SyncItem[updTotal];

        // This simulates that 3 of the initial 10 items (4,5,6) are changed.
        for(int i=0; i<updTotal; i++) {
            String name="Item"+(i+4);
            updItems[i]= new SyncItem(name, getType(),
                                      SyncItem.STATE_UPDATED, null, null);
        }
    }

    /**
     * In a real implementation, this function should search the database
     * for the deleted items present and store their keys.
     * The policy to detect a deleted item can vary from one source to another:
     * from keeping a list of items after the last sync to keep the items with
     * a deleted flag and then remove them after the successful deletion on
     * the server.
     */
    protected void initDelItems() {
        int delTotal = 3;

        delItems = new SyncItem[delTotal];

        // This simulates that 3 of the initial 10 items (8,9,10) are deleted.
        for(int i=0; i<delTotal; i++) {
            String name="Item"+(i+8);
            delItems[i] = new SyncItem(name, getType(),
                                       SyncItem.STATE_DELETED, null, null);
        }
    }

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
    protected SyncItem getItemContent(final SyncItem item) {
        SyncItem ret = new SyncItem(item);
        ret.setContent(
            new String("This is the content of item: "+item.getKey()).getBytes()
            );
        return ret;
    }
    
    public SyncItem getNextItem() throws SyncException {
      SyncItem ret = super.getNextItem();
      if (ret!=null) {
      //counter++;
      
      }
      return ret;
    }
    
}

