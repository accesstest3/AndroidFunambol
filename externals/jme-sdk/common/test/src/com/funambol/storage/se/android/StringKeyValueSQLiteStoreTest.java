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

import java.util.Enumeration;
import android.test.*;

public class StringKeyValueSQLiteStoreTest extends AndroidTestCase {
    
    
    StringKeyValueStore store = null;
    
    public StringKeyValueSQLiteStoreTest() {
        super();
    }
    
    public StringKeyValueStore createStore() {
        return new StringKeyValueSQLiteStore(getContext(), "funambol.db", "contacts");
    }

    public void setUp() {
        store = createStore();
        try {
            store.load();
        } catch(Exception e) { }
    }
    
    public void tearDown() {
        try {
            store.reset();
            store.save();
        } catch(Exception e) { }
    }
    
    public void testAddGet() throws Exception {
        store.add("1", "value");
        String value = store.get("1");
        assertTrue("value".equals(value));
    }
    
    public void testUpdateGet() throws Exception {
        store.add("3", "value1");
        String value = store.get("3");
        assertTrue("value1".equals(value));
        
        store.update("3", "value2");
        value = store.get("3");
        assertTrue("value2".equals(value));
    }
    
    public void testSaveLoad() throws Exception {
        store.add("0", "value-0");
        store.add("1", "value-1");
        store.save();
        
        // Now create a new instance and load its items
        StringKeyValueStore store2 = createStore();
        store2.load();
        String value0 = store2.get("0");
        String value1 = store2.get("1");
        assertTrue("value-0".equals(value0));
        assertTrue("value-1".equals(value1));
        store2.save();
    }
    
    public void testKeys() throws Exception {
        store.add("0", "value-0");
        store.add("1", "value-1");
        
        boolean value0 = false;
        boolean value1 = false;
        
        Enumeration keys = store.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if ("0".equals(key)) {
                value0 = true;
            } else if ("1".equals(key)) {
                value1 = true;
            } else {
                assertTrue(false);
            }
        }
        assertTrue(value0 && value1);
    }
    
    public void testContains() throws Exception {
        store.add("0", "value-0");
        store.add("1", "value-1");
        
        boolean value0 = false;
        boolean value1 = false;
        
        assertTrue(store.contains("0"));
        assertTrue(store.contains("1"));
        assertTrue(!store.contains("3"));
    }
    
    public void testRemove() throws Exception {
        store.add("0", "value-0");
        store.add("1", "value-1");
        
        assertTrue(store.contains("0"));
        assertTrue(store.contains("1"));
        store.remove("0");
        assertTrue(!store.contains("0"));
        assertTrue(store.contains("1"));
    }
    
    public void testReset() throws Exception {
        store.add("0", "value-0");
        store.add("1", "value-1");
        
        assertTrue(store.contains("0"));
        assertTrue(store.contains("1"));
        store.reset();
        assertTrue(!store.contains("0"));
        assertTrue(!store.contains("1"));
    }
    
}

