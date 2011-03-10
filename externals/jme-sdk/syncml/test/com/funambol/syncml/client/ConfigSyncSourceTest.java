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

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;

import com.funambol.storage.StringKeyValueStore;
import com.funambol.storage.StringKeyValueFileStore;
import com.funambol.storage.StringKeyValuePair;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncException;
import com.funambol.syncml.client.ConfigSyncSource;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.util.ConsoleAppender;
import com.funambol.util.Log;

import junit.framework.*;

public class ConfigSyncSourceTest extends TestCase {

    private StringKeyValueStore store;
    private ConfigSyncSource      source;
    private TestTracker         tracker;
    private String              directory;
    private SourceConfig        config;

    private class TestStore implements StringKeyValueStore {

        private Hashtable data = new Hashtable();

        public TestStore() {
        }

        public void add(String key, String value) {
            put(key, value);
        }
        
        public void update(String key, String value) {
            put(key, value);
        }
        
        public String put(String key, String value) {
            return (String)(data.put(key, value));
        }

        public String get(String key) {
            return (String)data.get(key);
        }

        public Enumeration keys() {
            return data.keys();
        }
        
        public Enumeration keyValuePairs() {
            
            final Enumeration keys   = data.keys();
            final Enumeration values = data.elements();
            
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
            return data.get(key) != null;
        }

        public String remove(String key) {
            return (String)data.remove(key);
        }

        public void save() throws IOException {
        }

        public void load() throws IOException {
        }

        public void reset() throws IOException {
        }
    }

    private class TestTracker implements ChangesTracker {

        private Vector newItems = new Vector();
        private Vector delItems = new Vector();
        private Vector updItems = new Vector();
        private Vector allItems = new Vector();

        public TestTracker() {
        }

        public void setSyncSource(TrackableSyncSource ss) {
        }

        public void begin(int syncMode) throws TrackerException {
        }

        public void end() throws TrackerException {
        }

        public Enumeration getNewItems() throws TrackerException {
            return newItems.elements();
        }

        public int getNewItemsCount() throws TrackerException {
            return newItems.size();
        }

        public Enumeration getUpdatedItems() throws TrackerException {
            return updItems.elements();
        }

        public int getUpdatedItemsCount() throws TrackerException {
            return updItems.size();
        }

        public Enumeration getDeletedItems() throws TrackerException {
            return delItems.elements();
        }

        public int getDeletedItemsCount() throws TrackerException {
            return delItems.size();
        }

        public void setItemStatus(String key, int status) throws TrackerException  {
        }

        public void reset() throws TrackerException {
        }

        public boolean removeItem(SyncItem item) throws TrackerException {
            return false;
        }

        public void addNewItem(String item) {
            newItems.addElement(item);
        }

        public void addUpdItem(String item) {
            updItems.addElement(item);
        }

        public void addDelItem(String item) {
            delItems.addElement(item);
        }

        public void addItem(String item) {
            allItems.addElement(item);
        }

        public void empty() throws TrackerException {

        }
    }

    public ConfigSyncSourceTest(String name) {
        super(name);

        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.TRACE);

        directory = "file:///root1";
    }

    public void setUp() {
    }

    public void testSlowSyncSimple() throws Throwable {

        TestStore store = new TestStore();
        TestTracker tracker = new TestTracker();

        String key0 = "./Email/Address";
        String key1 = "./Email/Display Name";
        String content0 = "test@test.com";
        String content1 = "Test";
        store.put(key0, content0);
        store.put(key1, content1);

        SourceConfig config = new SourceConfig("config", "application/*", "config");
        source = new ConfigSyncSource(config, tracker, store);

        source.beginSync(SyncML.ALERT_CODE_SLOW);

        SyncItem item = source.getNextItem();
        assertTrue(item != null);
        byte itemContent[] = item.getContent();
        assertTrue(itemContent != null);
        String content = new String(itemContent);

        if (key0.equals(item.getKey())) {
            assertTrue(content.equals(content0));
        } else {
            assertTrue(content.equals(content1));
        }

        item = source.getNextItem();
        assertTrue(item != null);
        assertTrue(source.getNextItem() == null);
        itemContent = item.getContent();
        assertTrue(itemContent != null);
        content = new String(itemContent);
        if (key0.equals(item.getKey())) {
            assertTrue(content.equals(content0));
        } else {
            assertTrue(content.equals(content1));
        }
        assertTrue(source.getNextItem() == null);

        source.endSync();
    }

    public void testFastSync1() throws Throwable {

        TestStore store = new TestStore();
        TestTracker tracker = new TestTracker();

        String key0 = "./Email/Address";
        String key1 = "./Email/Display Name";
        String key2 = "./Email/Signature";
        String content0 = "test@test.com";
        String content1 = "Test";
        // Populate the store with two items
        store.put(key0, content0);
        store.put(key1, content1);

        // Simulate one new, one update and one delete
        tracker.addNewItem(key0);
        tracker.addUpdItem(key1);
        tracker.addDelItem(key2);

        SourceConfig config = new SourceConfig("config", "application/*", "config");
        source = new ConfigSyncSource(config, tracker, store);

        source.beginSync(SyncML.ALERT_CODE_FAST);

        SyncItem item = source.getNextNewItem();
        assertTrue(item != null);
        byte itemContent[] = item.getContent();
        assertTrue(itemContent != null);
        String content = new String(itemContent);
        assertTrue(content.equals(content0));
        assertTrue(source.getNextNewItem() == null);

        item = source.getNextUpdatedItem();
        assertTrue(item != null);
        itemContent = item.getContent();
        assertTrue(itemContent != null);
        content = new String(itemContent);
        assertTrue(content.equals(content1));
        assertTrue(source.getNextUpdatedItem() == null);

        item = source.getNextDeletedItem();
        assertTrue(item != null);
        assertTrue(key2.equals(item.getKey()));

        // Now simulates changes from the server
        String key3 = "./Push/config";
        String content3 = "True";
        SyncItem item3 = new SyncItem(key3);
        item3.setContent(content3.getBytes());

        SyncItem item2 = new SyncItem(key1);
        item2.setContent(content0.getBytes());

        source.addItem(item3);
        source.updateItem(item2);

        String temp = (String)store.get(key3);
        assertTrue(content3.equals(temp));

        temp = (String)store.get(key1);
        assertTrue(content0.equals(temp));

        // Terminate the sync
        source.endSync();
    }
}

