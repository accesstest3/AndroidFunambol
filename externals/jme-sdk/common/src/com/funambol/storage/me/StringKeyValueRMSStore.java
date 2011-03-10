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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.rms.RecordStore;

import com.funambol.util.Log;

/**
 *
 */
public class StringKeyValueRMSStore implements StringKeyValueStore {

    private static final String TAG_LOG = "StringKeyValueRMSStore";

    protected Hashtable store;
    private String      storeName;

    public StringKeyValueRMSStore(String storeName) {
        store = new Hashtable();
        this.storeName = storeName;
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
        // Persist the hashtable into the store
        // Get the output stream from the open store (create if necessary)
        Log.trace(TAG_LOG, "Saving store " + storeName);

        RecordStore rmsStore = null;
        try {
            rmsStore = RecordStore.openRecordStore(storeName, true);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteStream);

            serializeHashTable(out, store);

            byte[] data = byteStream.toByteArray();
            if (rmsStore.getNumRecords() == 0) {
                Log.trace(TAG_LOG, "Adding new record");
                rmsStore.addRecord(data, 0, data.length);
            } else {
                Log.trace(TAG_LOG, "Setting existing record");
                rmsStore.setRecord(1, data, 0, data.length);
            }
        } catch (Exception e) {
            Log.error(TAG_LOG, "Cannot save RMS store", e);
            throw new IOException("Cannot create RMS store");
        } finally {
            if (rmsStore != null) {
                try {
                    rmsStore.closeRecordStore();
                } catch (Exception e1) {
                    Log.error("Cannot close RMS store " + e1.toString());
                }
            }
        }
    }

    public void load() throws IOException {
        // Persist the hashtable into the store
        // Get the output stream from the open store (create if necessary)
        Log.trace(TAG_LOG, "Loading store " + storeName);
        RecordStore rmsStore = null;
        try {
            rmsStore = RecordStore.openRecordStore(storeName, true);
            if (rmsStore.getNumRecords() > 0) {
                byte data[] = rmsStore.getRecord(1);

                if (data != null) {
                    ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
                    DataInputStream in = new DataInputStream(dataStream);

                    store = deserializeHashTable(in);
                }
            }
        } catch (Exception e) {
            Log.error(TAG_LOG, "Error loading RMS store", e);
            throw new IOException("Cannot load RMS store " + e.toString());
        } finally {
            if (rmsStore != null) {
                try {
                    rmsStore.closeRecordStore();
                } catch (Exception e1) {
                    Log.error("Cannot close RMS store " + e1.toString());
                }
            }
        }
    }

    public void reset() throws IOException {
        store = new Hashtable();
        try {
            String stores[] = RecordStore.listRecordStores();
            if (stores != null) {
                for(int i=0;i<stores.length;++i) {
                    String store = stores[i];
                    if (storeName.equals(store)) {
                        RecordStore.deleteRecordStore(storeName);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.error("Cannot delete mapping record store " + e.toString());
            throw new IOException("Cannot delete mapping record store " + e.toString());
        }
    }

    private void serializeHashTable(DataOutputStream dout, Hashtable ht) throws IOException {
        // For backward compatibilty we must write a marker byte. See
        // ComplexSerializer in the JavaEmailClient code for more details.
        dout.writeByte(5);
        // Store size
        dout.writeInt(ht.size());
        // Iterate through keys
        for(Enumeration e = ht.keys(); e.hasMoreElements(); ){
            String key = (String)e.nextElement();
            String val = (String)ht.get(key);
            dout.writeUTF(key);
            dout.writeUTF(val);
        }
    }
    
    
    /**
     * A helper method to deserialize a <code>Hashtable</code> <p>
     *
     * @param din
     *            The stream to write data from
     * @return
     *            The Hashtable deserialzed
     */
    private Hashtable deserializeHashTable(DataInputStream din) throws IOException {
        // For backward compatibilty we must read a marker byte. See
        // ComplexSerializer in the JavaEmailClient code for more details.
        din.readByte();
        // Retrieve size
        int size = din.readInt();
        Hashtable ht = new Hashtable();
        
        for(int i=0; i<size; i++){
            String key = din.readUTF();
            String val = din.readUTF();
            ht.put(key, val);
        }
        return ht;
    }
 
}

