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

package com.funambol.android;

import android.content.Context;
import com.funambol.storage.StringKeyValueSQLiteStore;

import com.funambol.util.Log;

/**
 * Allows a key-value pairs to be stored to a SQLite database through the
 * Android APIs. The DB schema is constrained to key of type integer. This may
 * be useful to increase performance.
 */
public class IntKeyValueSQLiteStore extends StringKeyValueSQLiteStore {

    private static final String TAG_LOG = "IntKeyValueSQLiteStore";

    /**
     * Create a new <code>IntKeyValueSQLiteStore</code> given the
     * <code>Context</code> the database file name and the table name.
     *
     * @param c The <code>Context</code>.
     * @param dbName The database file name.
     * @param tableName The table name.
     */
    public IntKeyValueSQLiteStore(Context c, String dbName, String tableName) {
        super(c, dbName, tableName);
    }

    /**
     * Add an entry into the db. If the key is not an integer value, then
     * an IllegalArgumentException is thrown.
     *
     * @see StringKeyValueStore#add(java.lang.String, java.lang.String)
     */
    @Override
    public void add(String key, String value) {
        checkKey(key);
        super.add(key, value);
    }

    /**
     * Update an existing entry in the db. If the key is not an integer value, then
     * an IllegalArgumentException is thrown.
     *
     * @see StringKeyValueStore#update(java.lang.String, java.lang.String)
     */
    @Override
    public void update(String key, String value) {
        checkKey(key);
        super.update(key, value);
    }

    /**
     * Get the value of an existing key in the db. If the key is not an integer value, then
     * an IllegalArgumentException is thrown.
     *
     * @see StringKeyValueStore#get(java.lang.String)
     */
    @Override
    public String get(String key) {
        checkKey(key);
        return super.get(key);
    }

    /**
     * Check if a given key exists in the db. If the key is not an integer value, then 
     * an IllegalArgumentException is thrown.
     *
     * @see StringKeyValueStore#contains(java.lang.String)
     */
    @Override
    public boolean contains(String key) {
        checkKey(key);
        return super.contains(key);
    }

    /**
     * Remove an entry from the db. If the key is not an integer value, then 
     * an IllegalArgumentException is thrown.
     *
     * @see StringKeyValueStore#remove(java.lang.String)
     */
    @Override
    public String remove(String key) {
        checkKey(key);
        return super.remove(key);
    }

    @Override
    protected String getCreateSQLCommand() {
        return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                        + KEY_COLUMN_NAME + " INTEGER PRIMARY KEY,"
                        + VALUE_COLUMN_NAME + " varchar[50]);";
    }

    private int checkKey(String key) {
        try {
            int intKey = Integer.parseInt(key);
            return intKey;
        } catch(NumberFormatException ex) {
            Log.error(TAG_LOG, "Invalid key " + key);
            throw new IllegalArgumentException("key must be an integer value");
        }
    }
}

