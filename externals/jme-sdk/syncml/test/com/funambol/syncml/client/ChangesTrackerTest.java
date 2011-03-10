/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2008 Funambol, Inc.
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

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import com.funambol.util.Log;
import com.funambol.storage.StringKeyValueStore;
import com.funambol.storage.StringKeyValuePair;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.util.ConsoleAppender;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncException;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.protocol.SyncMLStatus;

import junit.framework.*;

public abstract class ChangesTrackerTest extends TestCase {

    protected ChangesTracker tracker;
    protected TestSyncSource source;
    protected int id;

    protected static final int NUM_TESTS = 3;

    private class TestSyncSource extends TrackableSyncSource {

        private Hashtable items;

        public TestSyncSource(SourceConfig config, ChangesTracker tracker) {

            super(config, tracker);
            items = new Hashtable();
        }

        public int addItem(SyncItem item) throws SyncException {
            items.put(item.getKey(), item.getContent());
            tracker.removeItem(item);
            return SyncMLStatus.SUCCESS;
        }

        public int updateItem(SyncItem item) throws SyncException {
            items.put(item.getKey(), item.getContent());
            tracker.removeItem(item);
            return SyncMLStatus.SUCCESS;
        }

        public int deleteItem(String key) throws SyncException {
            items.remove(key);
            SyncItem tempItem = new SyncItem(key);
            tempItem.setState(SyncItem.STATE_DELETED);
            tracker.removeItem(tempItem);
            return SyncMLStatus.SUCCESS;
        }

        protected Enumeration getAllItemsKeys() throws SyncException {
            return items.keys();
        }

        protected SyncItem getItemContent(final SyncItem item) throws SyncException {
            SyncItem res = new SyncItem(item);
            res.setContent((byte[])items.get(item.getKey()));
            return res;
        }

        ///////// Methods used for testing purposes
        public void addItemFromOutside(SyncItem item) {
            items.put(item.getKey(), item.getContent());
        }

        public void updateItemFromOutside(String key, byte[] newContent) {
            items.put(key, newContent);
        }

        public void deleteItemFromOutside(String key) {
            items.remove(key);
        }

        public void deleteAllItems() {
        }
    }

    protected class TestKeyValueStore implements StringKeyValueStore {
        private Hashtable map = new Hashtable();

        public void add(String key, String value) {
            Log.trace("Adding in store: " + key);
            put(key, value);
        }
        
        public void update(String key, String value) {
            Log.trace("Updating in store: " + key);
            put(key, value);
        }
        
        public String put(String key, String value) {
            Log.trace("Putting in store: " + key);
            return (String)map.put(key, value);
        }

        public String get(String key) {
            Log.trace("Getting from store: " + key);
            return (String)map.get(key);
        }

        public Enumeration keys() {
            return map.keys();
        }
        
        public Enumeration keyValuePairs() {
            
            final Enumeration keys   = map.keys();
            final Enumeration values = map.elements();
            
            return new Enumeration () {
                
                boolean last = false;
                
                public Object nextElement() {
                    
                    String key   = (String)keys.nextElement();
                    String value = (String)values.nextElement();
                    
                    return new StringKeyValuePair(key, value);
                }
                
                public boolean hasMoreElements() {
                    return keys.hasMoreElements() && values.hasMoreElements();
                }
            };
        }

        public boolean contains(String key) {
            Object value = map.get(key);
            return value != null;
        }

        public String remove(String key) {
            return (String)map.remove(key);
        }

        public void save() throws IOException {
            // This cannot be saved or loaded
        }

        public void load() throws IOException {
            // This cannot be saved or loaded
        }

        public void reset() throws IOException {
            map = new Hashtable();
        }
    }

    
    public ChangesTrackerTest(String name) {

        super(name);
        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.TRACE);
    }

    /**
     * This method must be implemented by derived classes and it is used to
     * instantiate a ChangesTracker used by the TrackableSyncSource
     * implementation.
     */
    public abstract ChangesTracker createTracker();


    public void setUp() {
    }
    
    public void tearDown() {
    }

    public void testSimpleFasts() throws Exception {

        // Init the SyncSource and the tracker
        id = 0;
        try {
            SourceConfig config = new SourceConfig("TestSyncSource", "application/*", "briefcase");
            tracker = createTracker();
            source = new TestSyncSource(config, tracker);
        } catch (Exception e) {
            assertTrue(false);
        }

        // Simulate an initial slow sync. After this "unique-0" is in the store
        testReceivedNewItems();
        // User adds an item. unique-0 and unique-1 in the store
        testAddedNewItems();
        // User updates an item. unique-0 and unique-1 in the store
        testUpdatedItems();
        // User deletes unique-0. unique-1 in the store
        testDeletedItems();
        // Sync creates a new item. unique-1 and unique-2 in the store
        testAddCommand();
        // Sync replaces an item. unique-1 and unique-2 in the store
        testReplaceCommand();
        // Sync deletes an item. unique-2 is in the store
        testDeleteCommand();
    }

    //public void testReceivedNewItems() throws AssertionFailedException {
    public void testReceivedNewItems() throws Exception {
        Log.trace("testReceivedNewItems");
        // Begin a new sync
        source.beginSync(SyncML.ALERT_CODE_SLOW);
        // Add a new item
        SyncItem item = new SyncItem("unique-" + id++);
        item.setContent("Content".getBytes());
        source.addItem(item);
        source.endSync();
    }

    public void testAddedNewItems() throws Exception {
        Log.trace("testAddedNewItems");
        // Add a new item from "outside"
        String key = "unique-" + id++;
        SyncItem item = new SyncItem(key);
        item.setContent("Content".getBytes());
        source.addItemFromOutside(item);
        // Begin a new sync
        source.beginSync(SyncML.ALERT_CODE_FAST);
        // Now check that there is one new item reported
        SyncItem newItem = source.getNextNewItem();
        assertTrue(newItem != null);
        assertTrue(key.equals(newItem.getKey()));
        assertTrue(source.getNextNewItem() == null);
        assertTrue(source.getNextUpdatedItem() == null);
        assertTrue(source.getNextDeletedItem() == null);
        source.setItemStatus(key, SyncMLStatus.SUCCESS);
        source.endSync();
    }

    public void testUpdatedItems() throws Exception {
        Log.trace("testUpdatedItems");
        // Update item0 in the store
        String key = "unique-0";
        source.updateItemFromOutside(key, "NewContent".getBytes());
        // Begin a new sync
        source.beginSync(SyncML.ALERT_CODE_FAST);
        // Now check that there are is one updated item
        SyncItem updItem = source.getNextUpdatedItem();
        assertTrue(updItem != null);
        assertTrue(key.equals(updItem.getKey()));
        assertTrue(source.getNextUpdatedItem() == null);
        assertTrue(source.getNextNewItem() == null);
        assertTrue(source.getNextUpdatedItem() == null);
        assertTrue(source.getNextDeletedItem() == null);
        source.setItemStatus(key, SyncMLStatus.SUCCESS);
        source.endSync();
    }

    public void testDeletedItems() throws Exception {
        Log.trace("testDeletedItems");
        // Delete item0 in the store
        String key = "unique-0";
        source.deleteItemFromOutside(key);
        // Begin a new sync
        source.beginSync(SyncML.ALERT_CODE_FAST);
        // Now check that there are is one deleted item
        SyncItem delItem = source.getNextDeletedItem();
        assertTrue(delItem != null);
        assertTrue(key.equals(delItem.getKey()));
        assertTrue(source.getNextNewItem() == null);
        assertTrue(source.getNextUpdatedItem() == null);
        assertTrue(source.getNextDeletedItem() == null);
        source.setItemStatus(key, SyncMLStatus.SUCCESS);
        source.endSync();
    }

    public void testAddCommand() throws Exception {
        Log.trace("testAddCommand");
        // Begin a new sync
        source.beginSync(SyncML.ALERT_CODE_FAST);
        // Now receives an add command
        SyncItem newItem = new SyncItem("unique-" + id++);
        newItem.setContent("Content".getBytes());
        source.addItem(newItem);
        // Now check that there are no changes detected
        assertTrue(source.getNextNewItem() == null);
        assertTrue(source.getNextUpdatedItem() == null);
        assertTrue(source.getNextDeletedItem() == null);
        source.endSync();
    }

    public void testReplaceCommand() throws Exception {
        Log.trace("testReplaceCommand");
        // Begin a new sync
        source.beginSync(SyncML.ALERT_CODE_FAST);
        // Now receives a replace command
        String key = "unique-1";
        SyncItem item = new SyncItem(key);
        item.setContent("NewContent".getBytes());
        source.updateItem(item);
        // Now check that there are no changes detected
        assertTrue(source.getNextNewItem() == null);
        assertTrue(source.getNextUpdatedItem() == null);
        assertTrue(source.getNextDeletedItem() == null);
        source.endSync();
    }

    public void testDeleteCommand() throws Exception {
        Log.trace("testDeleteCommand");
        // Begin a new sync
        source.beginSync(SyncML.ALERT_CODE_FAST);
        // Now receives a delete command
        String key = "unique-1";
        source.deleteItem(key);
        // Now check that there are no changes detected
        assertTrue(source.getNextNewItem() == null);
        assertTrue(source.getNextUpdatedItem() == null);
        assertTrue(source.getNextDeletedItem() == null);
        source.endSync();
    }

    public void testSlowSync1() throws Exception {

        Log.trace("testSlowSync1");
        
        // Init the SyncSource and the tracker
        id = 0;
        try {
            SourceConfig config = new SourceConfig("TestSyncSource", "application/*", "briefcase");
            tracker = createTracker();
            source = new TestSyncSource(config, tracker);
        } catch (Exception e) {
            assertTrue(false);
        }

        // Prepopulate the source with three items
        for(int i=0;i<3;++i) {
            SyncItem item = new SyncItem("unique-" + id++);
            item.setContent("Content".getBytes());
            source.addItemFromOutside(item);
        }

        // Begin the sync and exchange all items
        source.beginSync(SyncML.ALERT_CODE_SLOW);
        for(int i=0;i<3;++i) {
            assertTrue(source.getNextItem() != null);
        }
        // Now simulate some new items from the server
        for(int i=0;i<3;++i) {
            SyncItem item = new SyncItem("unique-" + id++);
            item.setContent("Content".getBytes());
            source.addItem(item);
        }
        // Now check that no items are reported as new items
        assertTrue(source.getNextItem() == null);
        source.endSync();
    }

    public void testChangesDuringSync1() throws Exception {

        Log.debug("Test Changes During Sync #1");
        // Init the SyncSource and the tracker
        id = 0;
        try {
            SourceConfig config = new SourceConfig("TestSyncSource", "application/*", "briefcase");
            tracker = createTracker();
            source = new TestSyncSource(config, tracker);
        } catch (Exception e) {
            assertTrue(false);
        }

        Log.trace("Populate the source with 6 items");
        // Prepopulate the source with six items
        for(int i=0;i<6;++i) {
            SyncItem item = new SyncItem("unique-" + id++);
            item.setContent("Content".getBytes());
            source.addItemFromOutside(item);
        }

        Log.trace("Perform slow sync");
        // Begin the sync and exchange all items
        source.beginSync(SyncML.ALERT_CODE_SLOW);
        Log.trace("Send items to the server");
        for(int i=0;i<6;++i) {
            SyncItem nextItem = source.getNextItem();
            assertTrue(nextItem != null);
            source.setItemStatus(nextItem.getKey(), SyncMLStatus.SUCCESS);
        }
        Log.trace("Simulate user changes in the middle of the sync");
        // Simulate user changes in the middle of the sync
        SyncItem item = new SyncItem("unique-" + id++);
        item.setContent("Content".getBytes());
        source.addItemFromOutside(item);
        source.updateItemFromOutside("unique-0", "NewContent".getBytes());
        source.deleteItemFromOutside("unique-1");
        Log.trace("Receive items from the server");
        // Now simulate some new items from the server
        for(int i=0;i<3;++i) {
            item = new SyncItem("unique-" + id++);
            item.setContent("Content".getBytes());
            source.addItem(item);
        }
        
        source.endSync();
        // Start a fast sync and check that changed made during the previous
        // sync are detected
        // We expect: one new item (unique-6), one updated (unique-0) and one
        // deleted (unique-1)
        Log.trace("Perform fast sync");
        source.beginSync(SyncML.ALERT_CODE_FAST);
        Log.trace("Check new items");
        SyncItem added = source.getNextNewItem();
        assertTrue(added != null);
        assertTrue(added.getKey().equals("unique-6"));
        assertTrue(source.getNextNewItem() == null);
        Log.trace("Check updated items");
        SyncItem updated = source.getNextUpdatedItem();
        assertTrue(updated != null);
        assertTrue(updated.getKey().equals("unique-0"));
        assertTrue(source.getNextUpdatedItem() == null);
        Log.trace("Check deleted items");
        SyncItem deleted = source.getNextDeletedItem();
        assertTrue(deleted != null);
        assertTrue(deleted.getKey().equals("unique-1"));
        assertTrue(source.getNextDeletedItem() == null);
        source.endSync();
    }

}


