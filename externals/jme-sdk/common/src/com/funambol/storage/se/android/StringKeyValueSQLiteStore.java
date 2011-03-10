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
import java.io.IOException;

import android.content.Context;
import android.content.ContentValues;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

import com.funambol.util.Log;

/**
 * Allows a key-value pairs to be stored to a SQLite database through the
 * Android APIs.
 */
public class StringKeyValueSQLiteStore implements StringKeyValueStore {

    private static final String TAG_LOG = "StringKeyValueSQLiteStore";

    private final static int FUNAMBOL_DATABASE_VERSION = 1;

    protected String tableName;
    
    protected final String KEY_COLUMN_NAME   = "_key";
    protected final String VALUE_COLUMN_NAME = "_value";

    protected final String[] QUERY_KEY_COLUMN       = {KEY_COLUMN_NAME};
    protected final String[] QUERY_VALUE_COLUMN     = {VALUE_COLUMN_NAME};
    protected final String[] QUERY_KEY_VALUE_COLUMN = {KEY_COLUMN_NAME, VALUE_COLUMN_NAME};

    protected SQLiteDatabase dbStore;
    protected DatabaseHelper mDatabaseHelper = null;

    /**
     * Create a new <code>StringKeyValueSQLiteStore</code> given the
     * <code>Context</code> the database file name and the table name.
     *
     * @param c The <code>Context</code>.
     * @param dbName The database file name.
     * @param tableName The table name.
     */
    public StringKeyValueSQLiteStore(Context c, String dbName, String tableName) {
        mDatabaseHelper = new DatabaseHelper(c, dbName, tableName);
        this.tableName = tableName;

        // Create the table containing the key value pairs (if it does not exist
        // already)
        open();
        dbStore.execSQL(getCreateSQLCommand());
        dbStore.close();
        dbStore = null;
    }

    /**
     * @see StringKeyValueStore#add(java.lang.String, java.lang.String) 
     */
    public void add(String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_COLUMN_NAME, key);
        cv.put(VALUE_COLUMN_NAME, value);
        if (dbStore == null) {
            open();
        }
        if(dbStore.insert(tableName, null, cv) != -1) {
            Log.debug(TAG_LOG, "Insert new record. Key: " + key + " value: " +
                    value);
        } else {
            Log.error(TAG_LOG, "Error while insert new record. Key: " + key +
                    " value: " + value);
        }
    }

    /**
     * @see StringKeyValueStore#update(java.lang.String, java.lang.String) 
     */
    public void update(String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_COLUMN_NAME, key);
        cv.put(VALUE_COLUMN_NAME, value);
        if (dbStore == null) {
            open();
        }
        StringBuffer where = new StringBuffer(KEY_COLUMN_NAME);
        where.append("='").append(key).append("'");
        if(dbStore.update(tableName, cv, where.toString(), null) != -1) {
            Log.debug(TAG_LOG, "Update record. Key: " + key + " value: " + value);
        } else {
            Log.error(TAG_LOG, "Error while update record. Key: " + key +
                    " value: " + value);
        }
    }

    /**
     * Deprecated method. Throws always an Exception.
     */
    public String put(String key, String value) throws Exception {
        throw new Exception("Operation not allowed in StringKeyValueSQLiteStore");
    }

    /**
     * @see StringKeyValueStore#get(java.lang.String)
     */
    public String get(String key) {

        String result = null;
        if (dbStore == null) {
            open();
        }
        StringBuffer where = new StringBuffer(KEY_COLUMN_NAME);
        where.append("='").append(key).append("'");
        Cursor resultCursor = dbStore.query(true, tableName, QUERY_VALUE_COLUMN,
                where.toString(), null, null, null, null, null);

        if(resultCursor.getCount() > 0) {
            int colIndex = resultCursor.getColumnIndexOrThrow(VALUE_COLUMN_NAME);
            resultCursor.moveToFirst();
            result = resultCursor.getString(colIndex);
        }
        resultCursor.close();
        return result;
    }

    /**
     * @see StringKeyValueStore#keys()
     */
    public Enumeration keys() {

        if (dbStore == null) {
            open();
        }

        final Cursor result = dbStore.query(true, tableName, QUERY_KEY_COLUMN,
                null, null, null, null, KEY_COLUMN_NAME + " ASC", null);

        final int keyColumnIndex = result.getColumnIndexOrThrow(KEY_COLUMN_NAME);

        // Move Cursor to the first element
        result.moveToFirst();
        
        return new Enumeration () {

            boolean last = false;

            public Object nextElement() {

                // Get the Current value
                String value = result.getString(keyColumnIndex);

                // Move Cursor to the next element
                result.moveToNext();
                
                return value;
            }

            public boolean hasMoreElements() {
                if(last) {
                    return false; 
                }
                last = result.isAfterLast();
                if(last) {
                    result.close();
                }
                return !last;
            }
            
        };
    }

    /**
     * @see StringKeyValueStore#keyValuePairs()
     */
    public Enumeration keyValuePairs() {

        if (dbStore == null) {
            open();
        }

        final Cursor result = dbStore.query(true, tableName, QUERY_KEY_VALUE_COLUMN,
                null, null, null, null, KEY_COLUMN_NAME + " ASC", null);

        final int keyColumnIndex   = result.getColumnIndexOrThrow(KEY_COLUMN_NAME);
        final int valueColumnIndex = result.getColumnIndexOrThrow(VALUE_COLUMN_NAME);

        // Move Cursor to the first element
        result.moveToFirst();

        return new Enumeration () {

            boolean last = false;

            public Object nextElement() {

                // Get the Current value
                String key   = result.getString(keyColumnIndex);
                String value = result.getString(valueColumnIndex);

                // Move Cursor to the next element
                result.moveToNext();

                return new StringKeyValuePair(key, value);
            }

            public boolean hasMoreElements() {
                if(last) {
                    return false;
                }
                last = result.isAfterLast();
                if(last) {
                    result.close();
                }
                return !last;
            }

        };
    }

    /**
     * @see StringKeyValueStore#contains(java.lang.String)
     */
    public boolean contains(String key) {
        if (dbStore == null) {
            open();
        }
        StringBuffer where = new StringBuffer(KEY_COLUMN_NAME);
        where.append("='").append(key).append("'");
        Cursor result = dbStore.query(true, tableName, QUERY_VALUE_COLUMN,
                where.toString(), null, null, null, null, null);
        int count = result.getCount();
        result.close();
        return count > 0;
    }

    /**
     * @see StringKeyValueStore#remove(java.lang.String)
     */
    public String remove(String key) {
        if (dbStore == null) {
            open();
        }
        StringBuffer where = new StringBuffer(KEY_COLUMN_NAME);
        where.append("='").append(key).append("'");
        dbStore.delete(tableName, where.toString(), null);
        return null;
    }

    /**
     * Save the current store. Ends a successfull transaction.
     * @see StringKeyValueStore#save()
     */
    public void save() throws IOException {
        if (dbStore != null) {
            dbStore.close();
            dbStore = null;
        }
    }

    /**
     * @see StringKeyValueStore#load()
     */
    public void load() throws IOException {
        dbStore = mDatabaseHelper.getWritableDatabase();
    }

    /**
     * @see StringKeyValueStore#reset() 
     */
    public void reset() throws IOException {
        // Delete all the rows from the current table
        if (dbStore == null) {
            open();
        }
        dbStore.delete(tableName, null, null);
        dbStore.close();
        dbStore = null;
    }

    protected String getCreateSQLCommand() {
        return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
               + KEY_COLUMN_NAME + " varchar[50],"
               + VALUE_COLUMN_NAME + " varchar[50]);";
    }

    private void open() {
        dbStore = mDatabaseHelper.getWritableDatabase();
    }

    /**
     * Helps on creating and upgrading the SQLite db.
     */
    public class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String dbName, String tableName) {
            super(context, dbName, null, FUNAMBOL_DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}

