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
 * The interactive user interfaces in modified sourceName and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Funambol" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Funambol".
 */

package com.funambol.syncml.spds;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.IOException;

import com.funambol.storage.StringKeyValueStore;
import com.funambol.util.Log;

/**
 * A class that saves and retrieves the mapping information from the store
 */
public class MappingManager {
    
    //private static final String MAPPING_STORE = "SyncMLMappingStore";
    private static final String TAG_LOG = "MappingManager";

    private static MappingStoreBuilder builder = null;
    private String sourceName;
    private StringKeyValueStore store;

    static public void setStoreBuilder(MappingStoreBuilder builder) {
        MappingManager.builder = builder; 
    }
    
    public MappingManager(String sourceName) {
        this.sourceName = sourceName;
        if (builder == null) {
            builder = new MappingStoreBuilder();
        }
        store = builder.createNewStore(sourceName);
    }

    /**
     * Returns the ItemMap related to the given name
     * 
     * @param sourceName the name of the source to be retrieved
     * @return ItemMap of the given source
     */
    public Hashtable getMappings(String sourceName) {

        Hashtable mappings = new Hashtable();
        try {
            //Create or open the Mapping storage
            store.load();
            Enumeration keys = store.keys();

            while(keys.hasMoreElements()) {
                String key   = (String)keys.nextElement();
                String value = store.get(key);

                mappings.put(key, value);
            }
        } catch (Exception ex) {
            Log.error(TAG_LOG, "Exception caught reading the mapping stores", ex);
        }
        
        return mappings;
    }

    /**
     * Replace the current mappings with the new one and persist the info
     *
     * @param mappings the mapping hshtable
     */
    public void saveMappings(Hashtable mappings) {
        
        try {
            store.reset();
            Enumeration keys = mappings.keys();
            while(keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                String value = (String)mappings.get(key);

                store.add(key, value);
            }
            store.save();
            // Append the mappings
        } catch (Exception ex) {
            Log.error(TAG_LOG, "Exception caught writing the mapping store", ex);
        }
    }

    /**
     * Reset the mappings for the given source
     * @param sourceName is the name of the source to be reset
     */
    public void resetMappings(String sourceName) {
        try {
            store.reset();
        } catch (IOException ioe) {
            Log.error(TAG_LOG, "Cannot reset store ", ioe);
        }
    }
}
