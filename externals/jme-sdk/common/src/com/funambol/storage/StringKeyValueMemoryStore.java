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
import java.util.Hashtable;
import java.io.IOException;

import com.funambol.util.Log;

/**
 *
 */
public class StringKeyValueMemoryStore implements StringKeyValueStore {

    protected Hashtable store;

    public StringKeyValueMemoryStore() {
        store = new Hashtable();
    }

    public void add(String key, String value) {
        put(key, value);
    }

    public void update(String key, String value) {
        put(key, value);
    }

    public String put(String key, String value) {
        return (String)store.put(key, value);
    }

    public String get(String key) {
        return (String)store.get(key);
    }

    public Enumeration keys() {
        return store.keys();
    }

    public Enumeration keyValuePairs() {

        final Enumeration keys   = store.keys();
        final Enumeration values = store.elements();

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
        return store.get(key) != null;
    }

    public String remove(String key) {
        return (String)store.remove(key);
    }

    public void save() throws IOException {
    }

    public void load() throws IOException {
    }

    public void reset() throws IOException {
        store = new Hashtable();
    }
}

