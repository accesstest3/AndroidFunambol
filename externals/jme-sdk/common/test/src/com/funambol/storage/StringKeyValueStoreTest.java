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

package com.funambol.storage;

import com.funambol.util.Log;
import com.funambol.util.ConsoleAppender;

import java.util.Enumeration;
import junit.framework.*;

public abstract class StringKeyValueStoreTest extends TestCase {
    
    private int storeId = 0;

    public StringKeyValueStoreTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender(), Log.TRACE);
    }

    public abstract StringKeyValueStore createStore(String name);

    private StringKeyValueStore createNewStore() {
        return createStore("Test_" + storeId++);
    }

    public void setUp() {
    }

    public void tearDown() {
    }

    public void testPutGet() throws Exception  {
        StringKeyValueStore store = createNewStore();
        store.put("key", "value");

        String value = store.get("key");
        assertTrue("value".equals(value));
    }

    public void testSaveLoad() throws Exception  {
        StringKeyValueStore store = createStore("Test_saveLoadTest");

        store.put("key-0", "value-0");
        store.put("key-1", "value-1");
        store.save();

        // Now create a new instance and load its items
        StringKeyValueStore store2 = createStore("Test_saveLoadTest");
        store2.load();
        String value0 = store2.get("key-0");
        String value1 = store2.get("key-1");
        assertTrue("value-0".equals(value0));
        assertTrue("value-1".equals(value1));
    }

    public void testKeys() throws Exception  {
        StringKeyValueStore store = createNewStore();

        store.put("key-0", "value-0");
        store.put("key-1", "value-1");
 
        boolean value0 = false;
        boolean value1 = false;

        Enumeration keys = store.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if ("key-0".equals(key)) {
                value0 = true;
            } else if ("key-1".equals(key)) {
                value1 = true;
            } else {
                assertTrue(false);
            }
        }
        assertTrue(value0 && value1);
    }

    public void testContains() throws Exception  {
        StringKeyValueStore store = createNewStore();

        store.put("key-0", "value-0");
        store.put("key-1", "value-1");
 
        boolean value0 = false;
        boolean value1 = false;

        assertTrue(store.contains("key-0"));
        assertTrue(store.contains("key-1"));
        assertTrue(!store.contains("key"));
    }

    public void testRemove() throws Exception  {
        StringKeyValueStore store = createNewStore();

        store.put("key-0", "value-0");
        store.put("key-1", "value-1");
 
        assertTrue(store.contains("key-0"));
        assertTrue(store.contains("key-1"));
        store.remove("key-0");
        assertTrue(!store.contains("key-0"));
        assertTrue(store.contains("key-1"));
    }

}

