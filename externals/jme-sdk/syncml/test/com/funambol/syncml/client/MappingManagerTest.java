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

import com.funambol.syncml.spds.MappingManager;
import com.funambol.util.ConsoleAppender;
import junit.framework.*;

import com.funambol.util.Log;
import java.util.Hashtable;

/**
 * Test the MappingManager class, that persists the mapping message into the 
 * store
 */
public class MappingManagerTest extends TestCase {
    private static final String MAPPING_STORE = "SyncMLMappingStore";
    
    private static final String SOURCE_1 = "Dummy_1";
    private static final String SOURCE_2 = "Dummy_2";
    private static final String SOURCE_3 = "Dummy_3";
    
    Hashtable mappingsSample = new Hashtable();
    
    MappingManager mm = null;
    
    public MappingManagerTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.DEBUG);
    }

    /**
     * Set up all of the tests
     */
    public void setUp() {
        mm = new MappingManager(SOURCE_1); 
        mappingsSample.put("first", "1");
        mappingsSample.put("second", "2");
        mappingsSample.put("third", "3");
        Log.info("#########################");
    }

    /**
     * Tear down all of the tests
     */
    public void tearDown() {
        mm = null;
        mappingsSample.clear();
        deleteMappingStore();
    }

    /**
     * Try to store an empty mapping without the store existent on the device
     * @throws Exception
     */
    public void testSaveEmptyMappingsNoStorage() throws Exception {
        Log.info("testSaveEmptyMappingsNoStorage");
        mm.saveMappings(new Hashtable());
        Hashtable ht = mm.getMappings(SOURCE_1);
        assertTrue(ht.isEmpty());
        Log.info("succesfull");
    }
    
    /**
     * Try to get an empty mapping without the store existent on the device
     * @throws Exception
     */
    public void testGetEmptyMappingsNoStorage() throws Exception {
        Log.info("testGetEmptyMappingsNoStorage");
        Hashtable ht = mm.getMappings("NotExistent");
        assertTrue(ht.isEmpty());
        Log.info("succesfull");
    }
    
    /**
     * Try to store an empty mapping with store existent on the device
     * @throws Exception
     */
    public void testSaveEmptyMappingsExistentStorage() throws Exception {
        Log.info("testSaveEmptyMappingsExistentStorage");
        setExistentStorage();
        mm.saveMappings(new Hashtable());
        Hashtable ht = mm.getMappings(SOURCE_1);
        assertTrue(ht.isEmpty());
        Log.info("succesfull");
    }
    
    /**
     * Try to get an empty mapping with an already existent store on the device
     * @throws Exception
     */
    public void testGetEmptyMappingsExistentStorage() throws Exception {
        Log.info("testGetEmptyMappingsExistentStorage");
        setExistentStorage();
        Hashtable ht = mm.getMappings("NotExistent");
        assertTrue(ht.isEmpty());
        Log.info("succesfull");
    }

    /**
     * Save 3 different mappings for 3 different sources
     * @throws java.lang.Throwable
     */
    public void testSave3SourcesMappings() throws Throwable {
        Log.info("testSave3SourcesMappings");
        mm.saveMappings(mappingsSample);
        mm.saveMappings(mappingsSample);
        mm.saveMappings(mappingsSample);
        //rs = RecordStore.openRecordStore(MAPPING_STORE, false);
        //int recordNum = rs.getNumRecords();
        //Log.debug(recordNum + " record/s found");
        //rs.closeRecordStore();
        //assertTrue(recordNum==3);
        Log.info("succesfull");
    }
    
    /**
     * Save 3 different mappings for 3 different and then retrieves them
     * @throws java.lang.Throwable
     */
    public void testGet3SourcesMappings() throws Throwable {
        Log.info("testGet3SourcesMappings");
        String key1 = "key1";
        String value1 = "value1";
        String key2 = "key2";
        String value2 = "value2";
        String key3 = "key3";
        String value3 = "value3";
        mappingsSample.put(key1, value1);
        mm.saveMappings(mappingsSample);
        mappingsSample.put(key2, value2);
        mm.saveMappings(mappingsSample);
        mappingsSample.put(key3, value3);
        mm.saveMappings(mappingsSample);
        
        Hashtable m1 = mm.getMappings(SOURCE_1);
        Hashtable m2 = mm.getMappings(SOURCE_2);
        Hashtable m3 = mm.getMappings(SOURCE_3);
        
        boolean src1 = m1.containsKey(key1)&&m1.contains(value1);
        boolean src2 = m2.containsKey(key2)&&m2.contains(value2);
        boolean src3 = m3.containsKey(key3)&&m3.contains(value3);
    
        assertTrue(src1&&src2&&src3);
        Log.info("succesfull");
    }
    
    /**
     * Save 3 different mappings for 3 different sources
     * and then modify and retrieve them correctly from the store
     * @throws java.lang.Throwable
     */
    public void testModify3SourcesMappings() throws Throwable {
        Log.info("testGet3SourcesMappings");
        String key1 = "key1";
        String value1 = "value1";
        String key2 = "key2";
        String value2 = "value2";
        String key3 = "key3";
        String value3 = "value3";
        mappingsSample.put(key1, value1);
        mm.saveMappings(mappingsSample);
        mappingsSample.put(key2, value2);
        mm.saveMappings(mappingsSample);
        mappingsSample.put(key3, value3);
        mm.saveMappings(mappingsSample);
        
        Hashtable m1 = mm.getMappings(SOURCE_1);
        Hashtable m2 = mm.getMappings(SOURCE_2);
        Hashtable m3 = mm.getMappings(SOURCE_3);
        
        boolean src1 = m1.containsKey(key1)&&m1.contains(value1);
        boolean src2 = m2.containsKey(key2)&&m2.contains(value2);
        boolean src3 = m3.containsKey(key3)&&m3.contains(value3);
    
        //Changes the mapping values for the 3 sources
        mappingsSample.clear();
        mappingsSample.put(key2, value2);
        mm.saveMappings(mappingsSample);

        mappingsSample.clear();
        mappingsSample.put(key3, value3);
        mm.saveMappings(mappingsSample);

        mappingsSample.clear();
        mappingsSample.put(key1, value1);
        mm.saveMappings(mappingsSample);
        
        m1 = mm.getMappings(SOURCE_1);
        m2 = mm.getMappings(SOURCE_2);
        m3 = mm.getMappings(SOURCE_3);

        boolean m1Size = m1.size()==1; 
        boolean m2Size = m2.size()==1; 
        boolean m3Size = m3.size()==1; 
        
        boolean isRightMapsSize = m1Size&&m2Size&&m3Size;
        boolean chSrc1 = m1.containsKey(key2)&&m1.contains(value2);
        boolean chSrc2 = m2.containsKey(key3)&&m2.contains(value3);
        boolean chSrc3 = m3.containsKey(key1)&&m2.contains(value1);
        
        assertTrue(src1&&src2&&src3);
        Log.info("succesfull");
    }

    /**
     * Try to store an empty mapping with store existent on the device
     * @throws Exception
     */
    public void testResetMappings() throws Exception {
        Log.info("testSaveEmptyMappingsExistentStorage");
        setExistentStorage();
        mm.saveMappings(mappingsSample);
        mm.resetMappings(SOURCE_1);
        Hashtable ht = mm.getMappings(SOURCE_1);
        assertTrue(ht.isEmpty());
        Log.info("succesfull");
    }
    

    private void deleteMappingStore() {
    }
    
    private void setExistentStorage() {
    }
}

