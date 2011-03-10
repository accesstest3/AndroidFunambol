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

package com.funambol.storage;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.IOException;

/**
 * This interface defines a generic data store for strings where data is orgnized as
 * key/value pair. 
 * The store is persistable and each implementation is free to choose where and
 * how data is persisted.
 * Note that the lack of serializable concept in jme prevents this store to be
 * more generic. The Funambol APIs have the notion of Serializable but that
 * interface is not implemented by String, so it would be coumbersome to have a
 * simple KeyValueStore for strings.
 */
public interface StringKeyValueStore {

    /**
     * Add a new item into the store. The item is not persisted but it is
     * cached. Clients should invoke the save method to persist changes.
     *
     * @param key the unique key for this item (cannot be null)
     * @param value the value to be stored
     */
    public void add(String key, String value);

    /**
     * Update and existing item into the store. The item is not persisted but it
     * is cached. Clients should invoke the save method to persist changes.
     *
     * @param key the unique key of the existing item
     * @param value the value to be stored
     */
    public void update(String key, String value);

    /**
     * Add a new item into the store. The item is not persisted but it is
     * cached. Clients should invoke the save method to persist changes.
     * If an element with the same key exists, then it is replaced.
     *
     * @param key the unique key for this item (cannot be null)
     * @param value the value to be stored
     *
     * @return the old value associated to this key (null if it was not present)
     * @throws Exception if the operation can't be performed
     *
     * @deprecated Use add and update methods instead.
     */
    public String put(String key, String value) throws Exception;

    /**
     * Returns the value associated to the given key or null if not present.
     *
     * @param key is the key (cannot be null)
     * @return the value in the store or null if not present
     */
    public String get(String key);

    /**
     * Returns an enumeration with all the keys in the store.
     *
     * The elements type is <code>String</code>.
     *
     * @return the keys
     */
    public Enumeration keys();

    /**
     * Returns an enumeration of all the key/value pairs in the store.
     *
     * The elements type is <code>KeyValuePair</code>.
     *
     * @return the key/value pairs
     */
    public Enumeration keyValuePairs();

    /**
     * Returns true iff key is contained in this store.
     */
    public boolean contains(String key);

    /**
     * Removes an entry from the store
     * @param key the item key
     * @return the value associated to the item being deleted or null if the
     * item is not in the store
     */
    public String remove(String key);

    /**
     * Persist this store.
     *
     * @throws IOException if the operation cannot be performed
     */
    public void save() throws IOException;

    /**
     * Load this store into memory.
     *
     * @throws IOException if the operation cannot be performed
     */
    public void load() throws IOException;

    /**
     * Resets this data store. All data is lost after this call.
     *
     * @throws IOException if the operation fails
     */
    public void reset() throws IOException;

}

