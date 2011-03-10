/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2010 Funambol, Inc.
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

package com.funambol.client.test;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.funambol.syncml.client.TrackableSyncSource;
import com.funambol.syncml.client.CacheTracker;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.syncml.protocol.SyncMLStatus;
import com.funambol.syncml.spds.SyncException;
import com.funambol.storage.StringKeyValueMemoryStore;
import com.funambol.util.Log;

public class CheckSyncSource extends TrackableSyncSource {

    private static final String TAG_LOG = "CheckSyncSource";

    private static CacheTracker fakeTracker = new CacheTracker(new StringKeyValueMemoryStore());
    private CacheTracker tracker = new CacheTracker(new StringKeyValueMemoryStore());

    private Hashtable allItemsWrapper = new Hashtable();

    private Vector newItemsFromServer = new Vector();
    private Vector updItemsFromServer = new Vector();
    private Vector delItemsFromServer = new Vector();

    public CheckSyncSource(String name, String type, String remoteUri) {
        super(new SourceConfig(name, type, remoteUri), fakeTracker);
        setTracker(tracker);
        config.setEncoding(SyncSource.ENCODING_NONE);
    }

    public void beginSync(int syncMode) throws SyncException {
        super.beginSync(syncMode);

        newItemsFromServer.removeAllElements();
        updItemsFromServer.removeAllElements();
        delItemsFromServer.removeAllElements();
    }

    public int addItem(SyncItem item) throws SyncException {
        Log.info(TAG_LOG, "Added item: " + item.getKey());
        allItemsWrapper.put(item.getKey(), item);
        newItemsFromServer.addElement(item);
        return super.addItem(item);
    }

    public int updateItem(SyncItem item) throws SyncException {
        Log.info(TAG_LOG, "Replaced item: " + item.getKey());
        allItemsWrapper.put(item.getKey(), item);
        updItemsFromServer.addElement(item);
        return super.updateItem(item);
    }

    public int deleteItem(String key) throws SyncException {
        Log.info(TAG_LOG, "Deleted item: " + key);
        allItemsWrapper.remove(key);
        delItemsFromServer.addElement(key);
        return super.deleteItem(key);
    }

    public Enumeration getAllItemsKeys() throws SyncException {
        return allItemsWrapper.keys();
    }

    public SyncItem getItemContent(SyncItem item) throws SyncException {
        SyncItem res = (SyncItem)allItemsWrapper.get(item.getKey());
        Log.trace(TAG_LOG, "Returning item content=" + res);
        return res;
    }

    //------------------- Methods used by automatic tests --------------------//

    public void clear() {
        Log.trace(TAG_LOG, "Clearing check sync source status");
        allItemsWrapper.clear();
        delItemsFromServer.removeAllElements();
        updItemsFromServer.removeAllElements();
        newItemsFromServer.removeAllElements();
    }

    public void addItemFromOutside(SyncItem item) throws SyncException {
        Log.trace(TAG_LOG, "Added item from outside " + item.getKey());
        allItemsWrapper.put(item.getKey(), item);
    }

    public void updateItemFromOutside(SyncItem item) throws SyncException {
        Log.trace(TAG_LOG, "Replaced item from outside " + item.getKey());
        allItemsWrapper.put(item.getKey(), item);
    }

    public void deleteItemFromOutside(String key) throws SyncException {
        Log.trace(TAG_LOG, "Deleted item from outside: " + key);
        allItemsWrapper.remove(key);
    }

    public void deleteAllFromOutside() throws SyncException {
        Log.trace(TAG_LOG, "deleteAllFromOutside");
        allItemsWrapper.clear();
    }

    public Enumeration getAddedItems() {
        return newItemsFromServer.elements();
    }

    public Enumeration getUpdatedItems() {
        return updItemsFromServer.elements();
    }

    public Enumeration getDeletedItems() {
        return delItemsFromServer.elements();
    }

    public Hashtable getAllItems() {
        return allItemsWrapper;
    }

    public int getAllItemsCount() {
        return allItemsWrapper.size();
    }

    protected void deleteAllItems() {
        Log.debug(TAG_LOG, "deleteAllItems");
        allItemsWrapper.clear();
    }
}
