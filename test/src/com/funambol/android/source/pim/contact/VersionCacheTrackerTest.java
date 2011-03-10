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

package com.funambol.android.source.pim.contact;

import com.funambol.storage.StringKeyValueSQLiteStore;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.protocol.SyncMLStatus;

import com.funambol.util.Log;
import com.funambol.util.AndroidLogAppender;

import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.content.ContentResolver;

import android.test.AndroidTestCase;
import com.funambol.android.AndroidAppSyncSourceManager;
import com.funambol.android.AppInitializer;
import com.funambol.client.configuration.Configuration;
import com.funambol.client.source.AppSyncSourceManager;

public class VersionCacheTrackerTest extends AndroidTestCase {

    private ContentResolver resolver;

    protected ContactSyncSource source;

    private ContactManager cm;

    private Configuration configuration;
    private AppSyncSourceManager sManager;

    protected int id = 0;

    public void setUp() {

        Log.initLog(new AndroidLogAppender("VersionCacheTrackerTest"), Log.TRACE);

        AppInitializer initializer = AppInitializer.getInstance(getContext());
        initializer.init();

        configuration = initializer.getConfiguration();
        sManager = initializer.getAppSyncSourceManager();
        
        resolver = getContext().getContentResolver();

        cm = new ContactManager(getContext());

        // Init the SyncSource and the tracker
        initSyncSourceTracker();
    }

    public void tearDown() {
        resolver.delete(RawContacts.CONTENT_URI, null, null);
        resolver.delete(ContactsContract.Data.CONTENT_URI, null, null);
    }
    
    private void initSyncSourceTracker() {

        try {
            SourceConfig config = new SourceConfig("contact", "text/x-vcard", "card");

            StringKeyValueSQLiteStore trackerStore =
                    new StringKeyValueSQLiteStore(getContext(), "funambol.test.db", config.getName());

            trackerStore.load();
            trackerStore.reset();
            trackerStore.save();

            resolver.delete(RawContacts.CONTENT_URI, null, null);

            VersionCacheTracker tracker = new VersionCacheTracker(trackerStore, getContext());

            source = new ContactSyncSource(config, tracker, getContext(),
                    configuration, sManager.getSource(AndroidAppSyncSourceManager.CONTACTS_ID));

        } catch(Exception ex) {
            ex.toString();
            Log.error("Exception in initSyncSourceTracker: " + ex.toString());
        }
    }

    public void testSimpleFasts() throws Exception {
        testAddedNewItems();
        testUpdatedItems();
        testDeletedItems();
        testAddCommand();
        testReplaceCommand();
        testDeleteCommand();
    }

    public void testMultipleChanges() throws Exception {

        long lastId  = 0;
        long firstId = 0;

        // Prepopulate the source with six items
        for(int i=0;i<6;++i) {
            lastId = addContactFromOutside("name-" + id++);
            if(i==0) {firstId = lastId; }
        }

        source.beginSync(SyncML.ALERT_CODE_SLOW);
        for(int i=0;i<6;++i) {
            SyncItem nextItem = source.getNextItem();
            assertTrue(nextItem != null);
            source.setItemStatus(nextItem.getKey(), SyncMLStatus.SUCCESS);
        }
        assertTrue(source.getNextItem() == null);
        source.endSync();

        long new1 = addContactFromOutside("name-" + id++);
        long new2 = addContactFromOutside("name-" + id++);
        updateContactFromOutside(firstId, "new name");
        deleteContactFromOutside(lastId);

        source.beginSync(SyncML.ALERT_CODE_FAST);
        SyncItem newItem1 = source.getNextNewItem();
        SyncItem newItem2 = source.getNextNewItem();
        
        assertTrue(newItem1 != null);
        assertTrue(newItem2 != null);

        long key1 = Long.parseLong(newItem1.getKey());
        long key2 = Long.parseLong(newItem2.getKey());

        if(key1 == new1) {
            assertEquals(key2, new2);
        } else if(key1 == new2) {
            assertEquals(key2, new1);
        } else {
            fail();
        }

        source.setItemStatus(Long.toString(key1), SyncMLStatus.SUCCESS);
        source.setItemStatus(Long.toString(key2), SyncMLStatus.SUCCESS);

        SyncItem updated = source.getNextUpdatedItem();
        assertTrue(updated != null);
        assertEquals(Long.parseLong(updated.getKey()), firstId);

        source.setItemStatus(Long.toString(firstId), SyncMLStatus.SUCCESS);

        SyncItem deleted = source.getNextDeletedItem();
        assertTrue(deleted != null);
        assertEquals(Long.parseLong(deleted.getKey()), lastId);

        source.setItemStatus(Long.toString(lastId), SyncMLStatus.SUCCESS);
        
        assertTrue(source.getNextNewItem() == null);
        assertTrue(source.getNextUpdatedItem() == null);
        assertTrue(source.getNextDeletedItem() == null);
        
        source.endSync();
    }

    private void testAddCommand() throws Exception {
        Log.trace("testAddCommand");

        // Begin a new sync
        source.beginSync(SyncML.ALERT_CODE_FAST);

        // Now receives an add command
        SyncItem newItem = new SyncItem(""+(id++));
        newItem.setContent(getSampleVCard("name-" + id, ""));
        source.addItem(newItem);

        // Now check that there are no changes detected
        assertTrue(source.getNextNewItem() == null);
        assertTrue(source.getNextUpdatedItem() == null);
        assertTrue(source.getNextDeletedItem() == null);
        source.endSync();
    }

    private void testAddedNewItems() throws Exception {
        Log.trace("testAddedNewItems");

        // Add a new item from "outside"
        long key = addContactFromOutside("name-" + id++);

        source.beginSync(SyncML.ALERT_CODE_FAST);
        // Now check that there is one new item reported
        SyncItem newItem = source.getNextNewItem();
        assertTrue(newItem != null);

        assertEquals(Long.parseLong(newItem.getKey()), key);

        assertTrue(source.getNextNewItem() == null);
        assertTrue(source.getNextUpdatedItem() == null);
        assertTrue(source.getNextDeletedItem() == null);
        
        source.setItemStatus(""+key, SyncMLStatus.SUCCESS);
        source.endSync();
    }

    private void testUpdatedItems() throws Exception {
        Log.trace("testUpdatedItems");

        long key = addContactFromOutside("name-" + id++);
        source.beginSync(SyncML.ALERT_CODE_FAST);
        source.setItemStatus("" + key, SyncMLStatus.SUCCESS);
        source.endSync();

        // Update item in the store
        updateContactFromOutside(key, "new name");

        // Begin a new sync
        source.beginSync(SyncML.ALERT_CODE_FAST);

        // Now check that there are is one updated item
        SyncItem updItem = source.getNextUpdatedItem();
        assertTrue(updItem != null);
        assertTrue((key+"").equals(updItem.getKey()));

        assertTrue(source.getNextUpdatedItem() == null);
        assertTrue(source.getNextNewItem() == null);
        assertTrue(source.getNextUpdatedItem() == null);
        assertTrue(source.getNextDeletedItem() == null);
        source.setItemStatus(key+"", SyncMLStatus.SUCCESS);
        source.endSync();
    }

    private void testDeletedItems() throws Exception {
        Log.trace("testDeletedItems");

        long key = addContactFromOutside("name-" + id++);
        source.beginSync(SyncML.ALERT_CODE_FAST);
        source.setItemStatus("" + key, SyncMLStatus.SUCCESS);
        source.endSync();
        
        deleteContactFromOutside(key);
        
        // Begin a new sync
        source.beginSync(SyncML.ALERT_CODE_FAST);
        // Now check that there are is one deleted item
        SyncItem delItem = source.getNextDeletedItem();
        assertTrue(delItem != null);
        assertTrue(key == Long.parseLong(delItem.getKey()));
        assertTrue(source.getNextNewItem() == null);
        assertTrue(source.getNextUpdatedItem() == null);
        assertTrue(source.getNextDeletedItem() == null);
        source.setItemStatus(Long.toString(key), SyncMLStatus.SUCCESS);
        source.endSync();
    }

    private void testReplaceCommand() throws Exception {
        Log.trace("testReplaceCommand");

        long key = addContactFromOutside("name-" + id++);
        source.beginSync(SyncML.ALERT_CODE_FAST);
        source.setItemStatus(Long.toString(key), SyncMLStatus.SUCCESS);
        source.endSync();
        
        source.beginSync(SyncML.ALERT_CODE_FAST);
        SyncItem item = new SyncItem(Long.toString(key));
        item.setState(SyncItem.STATE_UPDATED);
        item.setContent(getSampleVCard("updated", "000000"));
        source.updateItem(item);
        
        // Now check that there are no changes detected
        assertTrue(source.getNextNewItem() == null);
        assertTrue(source.getNextUpdatedItem() == null);
        assertTrue(source.getNextDeletedItem() == null);
        source.endSync();
    }

    private void testDeleteCommand() throws Exception {
        Log.trace("testDeleteCommand");

        long key = addContactFromOutside("name-" + id++);
        source.beginSync(SyncML.ALERT_CODE_FAST);
        source.setItemStatus("" + key, SyncMLStatus.SUCCESS);
        source.endSync();
        
        // Begin a new sync
        source.beginSync(SyncML.ALERT_CODE_FAST);
        // Now receives a delete command
        source.deleteItem(Long.toString(key));

        // Now check that there are no changes detected
        assertTrue(source.getNextNewItem() == null);
        assertTrue(source.getNextUpdatedItem() == null);
        assertTrue(source.getNextDeletedItem() == null);
        source.endSync();
    }

    public void testChangesDuringSync1() throws Exception {

        Log.debug("Test Changes During Sync #1");

        // Prepopulate the source with six items
        for(int i=0;i<6;++i) {
            Log.trace("Add item " + i);
            addContactFromOutside("name-" + id++);
        }

        Log.trace("Perform slow sync");
        // Begin the sync and exchange all items
        source.beginSync(SyncML.ALERT_CODE_SLOW);

        for(int i=0;i<6;++i) {
            Log.trace("Check item " + i);
            SyncItem nextItem = source.getNextItem();
            assertTrue(nextItem != null);
            source.setItemStatus(nextItem.getKey(), SyncMLStatus.SUCCESS);
        }
        Log.trace("Simulate user changes in the middle of the sync");
        // Simulate user changes in the middle of the sync
        long id1 = addContactFromOutside("name-" + id++);
        long id2 = addContactFromOutside("name-" + id++);
        
        // Now check that no items are reported as new items
        assertTrue(source.getNextItem() == null);
        source.endSync();

        // Start a fast sync and check that changed made during the previous
        // sync are detected
        source.beginSync(SyncML.ALERT_CODE_FAST);

        SyncItem added1 = source.getNextNewItem();
        SyncItem added2 = source.getNextNewItem();

        assertTrue(added1 != null);
        assertTrue(added2 != null);

        long key1 = Long.parseLong(added1.getKey());
        long key2 = Long.parseLong(added2.getKey());

        if(key1 == id1) {
            assertEquals(key2, id2);
        } else if(key1 == id2) {
            assertEquals(key2, id1);
        } else {
            fail();
        }
        source.endSync();
    }

    private byte[] getSampleVCard(String name, String phone) {
        return ("BEGIN:VCARD\r\n" +
                "VERSION:2.1\r\n" +
                "N:" + name + ";;;;\r\n" +
                "TEL;CELL:" + phone + "\r\n" +
                "TEL;FAX:123456\r\n" +
                "END:VCARD").getBytes();
    }

    private long addContactFromOutside(String name) {
        try {
            Contact c = new Contact();
            c.setVCard(getSampleVCard(name, ""));
            return cm.add(c);
        } catch(Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    private void updateContactFromOutside(long key, String newName) {
        try {
            Contact c = new Contact();
            c.setVCard(getSampleVCard(newName, "000000000"));
            cm.update(key, c);
        } catch(Exception ex) { }
    }

    private void deleteContactFromOutside(long key) {
        try {
            cm.delete(key);
        } catch(Exception ex) { }
    }
}
