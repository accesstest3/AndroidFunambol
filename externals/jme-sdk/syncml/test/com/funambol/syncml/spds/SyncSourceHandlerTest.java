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

package com.funambol.syncml.spds;

import java.util.Vector;
import java.util.Hashtable;

import com.funambol.util.Log;
import com.funambol.util.ConsoleAppender;
import com.funambol.util.Base64;
import com.funambol.util.ChunkedString;
import com.funambol.util.ConsoleAppender;
import com.funambol.syncml.protocol.SyncMLStatus;
import com.funambol.storage.StringKeyValueStore;
import com.funambol.storage.StringKeyValueFileStore;
import com.funambol.syncml.client.BaseSyncSource;

import junit.framework.*;

public class SyncSourceHandlerTest extends TestCase {

    private class TestSyncListener extends BasicSyncListener {

        private int numAddSent     = 0;
        private int numReplaceSent = 0;
        private int numDeleteSent  = 0;

        public void itemAddSendingEnded(String key, String parent, int size) {
            ++numAddSent;
        }

        public void itemReplaceSendingEnded(String key, String parent, int size) {
            ++numReplaceSent;
        }

        public void itemDeleteSent(Object item) {
            ++numDeleteSent;
        }

        public int getAddSent() {
            return numAddSent;
        }

        public int getReplaceSent() {
            return numReplaceSent;
        }

        public int getDeleteSent() {
            return numDeleteSent;
        }

    }

    private class TestSyncSource extends BaseSyncSource {
        private SyncItem lastAdded     = null;
        private SyncItem lastUpdated   = null;
        private String   lastDeleted   = null;
        private Vector   nextNewItems  = new Vector();
        private Vector   nextUpdItems  = new Vector();
        private Vector   nextDelItems  = new Vector();
        private Vector   nextItems     = new Vector();
        private int      nextNewItemId = 0;
        private int      nextUpdItemId = 0;
        private int      nextDelItemId = 0;
        private int      nextItemId    = 0;

        public TestSyncSource(SourceConfig config) {
            super(config);
        }

        public int addItem(SyncItem item) throws SyncException {
            lastAdded = item;
            return SyncMLStatus.SUCCESS;
        }

        public int updateItem(SyncItem item) throws SyncException {
            lastUpdated = item;
            return SyncMLStatus.SUCCESS;
        }
    
        public int deleteItem(String key) throws SyncException {
            lastDeleted = key;
            return SyncMLStatus.SUCCESS;
        }

        public SyncItem getNextNewItem() throws SyncException {
            if (nextNewItemId < nextNewItems.size()) {
                return (SyncItem) nextNewItems.elementAt(nextNewItemId++);
            } else {
                return null;
            }
        }

        public SyncItem getNextUpdatedItem() throws SyncException {
            if (nextUpdItemId < nextUpdItems.size()) {
                return (SyncItem) nextUpdItems.elementAt(nextUpdItemId++);
            } else {
                return null;
            }
        }

        public SyncItem getNextDeletedItem() throws SyncException {
            if (nextDelItemId < nextDelItems.size()) {
                return (SyncItem) nextDelItems.elementAt(nextDelItemId++);
            } else {
                return null;
            }
        }

         public SyncItem getNextItem() throws SyncException {
            if (nextItemId < nextItems.size()) {
                return (SyncItem) nextItems.elementAt(nextItemId++);
            } else {
                return null;
            }
        }

        protected void initAllItems() throws SyncException {
        }

        protected void initNewItems() throws SyncException {
        }

        protected void initUpdItems() throws SyncException {
        }

        protected void initDelItems() throws SyncException {
        }

        protected SyncItem getItemContent(final SyncItem item) throws SyncException {
            return null;
        }

        public SyncItem lastItemAdded() {
            return lastAdded;
        }

        public SyncItem lastItemUpdated() {
            return lastUpdated;
        }

        public void setNextNewItems(Vector items) {
            nextNewItems = items;
        }

        public void setNextUpdItems(Vector items) {
            nextUpdItems = items;
        }

        public void setNextDelItems(Vector items) {
            nextDelItems = items;
        }

        public void setNextItems(Vector items) {
            nextItems = items;
        }
    }

    private final String SYNCML_FOLDER_DATA = "<Data>" +
                                              "<![CDATA[" +
                                              "<Folder>" +
                                              "<name>Inbox</name>" +
                                              "<created>20090428T162654Z&lt;/created>" +
                                              "<role>inbox</role>" +
                                              "</Folder>" +
                                              "]]>" +
                                              "</Data>";

    public SyncSourceHandlerTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender(), Log.TRACE);
    }

    public void testAddItem() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 2 * 1024,
                                                              formatter);

        // Add items and checks that the sync source addItem is invoked for each
        // small object, while large object are assmbled in memory and passed to
        // the sync source once completed

        Object creps[] = new Object[1];

        // Simulate a complete item
        Chunk item1 = new Chunk("0", null, null, null, false);
        byte content1[] = new byte[500];
        fillContent(content1, 'A');
        item1.setContent(content1);
        handler.addItem(item1, creps);
        assertTrue(ss.lastItemAdded() != null);
        assertTrue(ss.lastItemAdded().getKey().equals(item1.getKey()));

        // Simulate a partial item
        Chunk item2 = new Chunk("1", null, null, null, true);
        byte content2[] = new byte[2001];
        fillContent(content2, 'A');
        item2.setContent(content2);
        handler.addItem(item2, creps);
        assertTrue(ss.lastItemAdded() != null);
        assertTrue(ss.lastItemAdded().getKey().equals(item1.getKey()));

        // And now finalize it 
        Chunk item3 = new Chunk("2", null, null, null, false);
        byte content3[] = new byte[48];
        fillContent(content3, 'B');
        item3.setContent(content3);
        handler.addItem(item3, creps);
        assertTrue(ss.lastItemAdded() != null);
        assertTrue(ss.lastItemAdded().getKey().equals(item2.getKey()));
        assertTrue(ss.lastItemAdded().getContent().length == content2.length + content3.length);

        // Now perform the same test with a sync source using b64 encoding
        ss.getConfig().setEncoding(SyncSource.ENCODING_B64);

        // An encoded ss must be given encoded items
        item1.setContent(Base64.encode(item1.getContent()));
        handler.addItem(item1, creps);
        assertTrue(ss.lastItemAdded() != null);
        assertTrue(ss.lastItemAdded().getKey().equals(item1.getKey()));

        item2.setContent(Base64.encode(item2.getContent()));
        handler.addItem(item2, creps);
        assertTrue(ss.lastItemAdded() != null);
        assertTrue(ss.lastItemAdded().getKey().equals(item1.getKey()));

        item3.setContent(Base64.encode(item3.getContent()));
        handler.addItem(item3, creps);
        assertTrue(ss.lastItemAdded() != null);
        assertTrue(ss.lastItemAdded().getKey().equals(item2.getKey()));
        byte content[] = ss.lastItemAdded().getContent();
        content = Base64.decode(content);
        assertTrue(content.length == content2.length + content3.length);
    }

    public void testUpdateItem() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 2 * 1024,
                                                              formatter);

        // Add items and checks that the sync source addItem is invoked for each
        // small object, while large object are assmbled in memory and passed to
        // the sync source once completed

        // Simulate a complete item
        Chunk item1 = new Chunk("0", null, null, null, false);
        byte content1[] = new byte[500];
        fillContent(content1, 'A');
        item1.setContent(content1);
        Object creps[] = new Object[1];
        handler.updateItem(item1, creps);
        assertTrue(ss.lastItemUpdated() != null);
        assertTrue(ss.lastItemUpdated().getKey().equals(item1.getKey()));

        // Simulate a partial item
        Chunk item2 = new Chunk("1", null, null, null, true);
        byte content2[] = new byte[2001];
        fillContent(content2, 'A');
        item2.setContent(content2);
        item2.setHasMoreData();
        handler.updateItem(item2, creps);
        assertTrue(ss.lastItemUpdated() != null);
        assertTrue(ss.lastItemUpdated().getKey().equals(item1.getKey()));

        // And now finalize it 
        Chunk item3 = new Chunk("2", null, null, null, false);
        byte content3[] = new byte[48];
        fillContent(content3, 'B');
        item3.setContent(content3);
        handler.updateItem(item3, creps);
        assertTrue(ss.lastItemUpdated() != null);
        assertTrue(ss.lastItemUpdated().getKey().equals(item2.getKey()));
        assertTrue(ss.lastItemUpdated().getContent().length == content2.length + content3.length);

        // Now perform the same test with a sync source using b64 encoding
        ss.getConfig().setEncoding(SyncSource.ENCODING_B64);

        // An encoded ss must be given encoded items
        item1.setContent(Base64.encode(item1.getContent()));
        handler.updateItem(item1, creps);
        assertTrue(ss.lastItemUpdated() != null);
        assertTrue(ss.lastItemUpdated().getKey().equals(item1.getKey()));

        item2.setContent(Base64.encode(item2.getContent()));
        handler.updateItem(item2, creps);
        assertTrue(ss.lastItemUpdated() != null);
        assertTrue(ss.lastItemUpdated().getKey().equals(item1.getKey()));

        item3.setContent(Base64.encode(item3.getContent()));
        handler.updateItem(item3, creps);
        assertTrue(ss.lastItemUpdated() != null);
        assertTrue(ss.lastItemUpdated().getKey().equals(item2.getKey()));
        byte content[] = ss.lastItemUpdated().getContent();
        content = Base64.decode(content);
        assertTrue(content.length == content2.length + content3.length);
    }

    public void testGetAddCommand() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        Vector newItems = new Vector(3);
        SyncItem item0 = new SyncItem("0", null, SyncItem.STATE_NEW, null);
        byte content0[] = new byte[320];
        fillContent(content0, 'A');
        item0.setContent(content0);

        SyncItem item1 = new SyncItem("1", null, SyncItem.STATE_NEW, null);
        byte content1[] = new byte[180];
        fillContent(content1, 'A');
        item1.setContent(content1);

        SyncItem item2 = new SyncItem("2", null, SyncItem.STATE_NEW, null);
        byte content2[] = new byte[10];
        fillContent(content2, 'A');
        item2.setContent(content2);

        newItems.addElement(item0);
        newItems.addElement(item1);
        newItems.addElement(item2);

        ss.setNextNewItems(newItems);

        TestSyncListener listener = new TestSyncListener();
        StringBuffer cmdTag = new StringBuffer();
        CmdId cmdId = new CmdId(0);

        // The first message shall contain only the first item
        int status = handler.getAddCommand(0, listener, cmdTag, cmdId);
        assertTrue(status == SyncSourceLOHandler.MORE);
        assertTrue(cmdId.getValue() == 1);
        assertTrue(listener.getAddSent() == 1);

        // The second message shall contain the other two items
        cmdTag = new StringBuffer();
        status = handler.getAddCommand(0, listener, cmdTag, cmdId);
        assertTrue(status == SyncSourceLOHandler.DONE);
        assertTrue(cmdId.getValue() == 2);
        assertTrue(listener.getAddSent() == 3);
    }

    public void testGetReplaceCommand() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        Vector updItems = new Vector(3);
        SyncItem item0 = new SyncItem("0", null, SyncItem.STATE_UPDATED, null);
        byte content0[] = new byte[320];
        fillContent(content0, 'A');
        item0.setContent(content0);

        SyncItem item1 = new SyncItem("1", null, SyncItem.STATE_UPDATED, null);
        byte content1[] = new byte[180];
        fillContent(content1, 'A');
        item1.setContent(content1);

        SyncItem item2 = new SyncItem("2", null, SyncItem.STATE_UPDATED, null);
        byte content2[] = new byte[10];
        fillContent(content2, 'A');
        item2.setContent(content2);

        updItems.addElement(item0);
        updItems.addElement(item1);
        updItems.addElement(item2);

        ss.setNextUpdItems(updItems);

        TestSyncListener listener = new TestSyncListener();
        StringBuffer cmdTag = new StringBuffer();
        CmdId cmdId = new CmdId(0);

        // The first message shall contain only the first item
        int status = handler.getReplaceCommand(0, listener, cmdTag, cmdId);
        assertTrue(status == SyncSourceLOHandler.MORE);
        assertTrue(cmdId.getValue() == 1);
        assertTrue(listener.getReplaceSent() == 1);

        // The second message shall contain the other two items
        cmdTag = new StringBuffer();
        status = handler.getReplaceCommand(0, listener, cmdTag, cmdId);
        assertTrue(status == SyncSourceLOHandler.DONE);
        assertTrue(cmdId.getValue() == 2);
        assertTrue(listener.getReplaceSent() == 3);
    }

    public void testGetDeleteCommand() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 200, formatter);
        Vector delItems = new Vector(3);
        SyncItem item0 = new SyncItem("WeNeedAVeryLongKeySoTheItemIsTheOnlyOne" +
                                      "ThatFitsInASingleMessageBecauseInDeletes" +
                                      "TheContentIsNotTransmitted", null, SyncItem.STATE_DELETED, null);

        SyncItem item1 = new SyncItem("1", null, SyncItem.STATE_DELETED, null);

        SyncItem item2 = new SyncItem("2", null, SyncItem.STATE_DELETED, null);

        delItems.addElement(item0);
        delItems.addElement(item1);
        delItems.addElement(item2);

        ss.setNextDelItems(delItems);

        TestSyncListener listener = new TestSyncListener();
        StringBuffer cmdTag = new StringBuffer();
        CmdId cmdId = new CmdId(0);

        // The first message shall contain only the first item
        boolean done = handler.getDeleteCommand(0, listener, cmdTag, cmdId);
        assertTrue(!done);
        assertTrue(cmdId.getValue() == 1);
        assertTrue(listener.getDeleteSent() == 1);

        // The second message shall contain the other two items
        cmdTag = new StringBuffer();
        done = handler.getDeleteCommand(0, listener, cmdTag, cmdId);
        assertTrue(done);
        assertTrue(cmdId.getValue() == 2);
        assertTrue(listener.getDeleteSent() == 3);
    }

    public void testGetNextCommand() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        Vector items = new Vector();
        SyncItem item0 = new SyncItem("0", null, SyncItem.STATE_UPDATED, null);
        byte content0[] = new byte[320];
        fillContent(content0, 'A');
        item0.setContent(content0);

        SyncItem item1 = new SyncItem("1", null, SyncItem.STATE_UPDATED, null);
        byte content1[] = new byte[150];
        fillContent(content1, 'A');
        item1.setContent(content1);

        SyncItem item2 = new SyncItem("2", null, SyncItem.STATE_UPDATED, null);
        byte content2[] = new byte[50];
        fillContent(content2, 'A');
        item2.setContent(content2);

        items.addElement(item0);
        items.addElement(item1);
        items.addElement(item2);

        ss.setNextItems(items);

        TestSyncListener listener = new TestSyncListener();
        StringBuffer cmdTag = new StringBuffer();
        CmdId cmdId = new CmdId(0);

        // The first message shall contain only the first item
        int done = handler.getNextCommand(0, listener, cmdTag, cmdId);
        Log.debug(cmdTag.toString());
        assertTrue(done == SyncSourceLOHandler.MORE);
        assertTrue(cmdId.getValue() == 1);
        assertTrue(listener.getReplaceSent() == 1);

        // The second message shall contain the other two items
        cmdTag = new StringBuffer();
        done = handler.getNextCommand(0, listener, cmdTag, cmdId);
        Log.debug(cmdTag.toString());
        assertTrue(done == SyncSourceLOHandler.DONE);
        assertTrue(cmdId.getValue() == 2);
        assertTrue(listener.getReplaceSent() == 3);
    }

    /**
     * This method check if chunks decoding works properly.
     */
    public void testChunkdecoding() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "text/plain", "briefcase");
        config.setEncoding(SyncSource.ENCODING_B64);
        TestSyncSource ss = new TestSyncSource(config);

        byte content0Bytes[] = new byte[100];
        for(int i=0;i<content0Bytes.length;++i) {
            content0Bytes[i] = 'A';
        }
        String encodedContent0 = new String(Base64.encode(content0Bytes));

        String chunk0Content = encodedContent0.substring(0, 59);
        String chunk1Content = encodedContent0.substring(59);

        // Create an item with 100 bytes or so
        String chunk0 = "<Add> <CmdID>1</CmdID> <Item> <Meta> <Type xmlns=\"syncml:metinf\">text/plain</Type></Meta> " +
                        "<Source><LocURI>0</LocURI></Source> " +
                        "<Data>" + chunk0Content +
                        "</Data> <MoreData/> </Item> </Add>";

        String chunk1 = "<Add> <CmdID>1</CmdID> <Item> <Meta> <Type xmlns=\"syncml:metinf\">text/plain</Type></Meta> " +
                        "<Source><LocURI>0</LocURI></Source> " +
                        "<Data>" + chunk1Content +
                        "</Data> </Item> </Add>";

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        TestSyncListener listener = new TestSyncListener();

        String formats[] = { "b64" };
        Chunk firstChunk = handler.getItem("text/plain", new ChunkedString(chunk0), formats, null);
        Chunk secondChunk = handler.getItem("text/plain", new ChunkedString(chunk1), formats, null);

        String decodedItem = new String(firstChunk.getContent()) + new String(secondChunk.getContent());
        String compare     = new String(content0Bytes);
        assertTrue(decodedItem.equals(compare));
    }

    /**
     * Check if the TargetParent property is correctly read from a SyncML command
     * @throws java.lang.Throwable
     */
    public void testTargetParent() throws Throwable {

        String xmlCommand = "<Item>" +
                            "<Source><LocURI>101</LocURI></Source>" +
                            "<TargetParent><LocURI>xyz01</LocURI></TargetParent>" +
                            "<Meta><Type>application/vnd.omads-folder+xml</Type></Meta>" +
                            SYNCML_FOLDER_DATA +
                            "</Item>";

        simpleSyncItemTest(xmlCommand, "101", "xyz01", null);
    }

    /**
     * Check if the TargetParent and Source uri properties are correctly read
     * from a SyncML command, in the case the TargetParent is specified before
     * the Source uri.
     * @throws java.lang.Throwable
     */
    public void testTargetParentBeforeSourceUri() throws Throwable {

        String xmlCommand = "<Item>" +
                            "<TargetParent><LocURI>xyz01</LocURI></TargetParent>" +
                            "<Source><LocURI>101</LocURI></Source>" +
                            "<Meta><Type>application/vnd.omads-folder+xml</Type></Meta>" +
                            SYNCML_FOLDER_DATA +
                            "</Item>";

        simpleSyncItemTest(xmlCommand, "101", "xyz01", null);
    }

    /**
     * Check if the SourceParent is correctly read and translated to the local
     * parent key.
     * @throws java.lang.Throwable
     */
    public void testSourceParent() throws Throwable {

        String xmlCommand = "<Item>" +
                            "<Source><LocURI>101</LocURI></Source>" +
                            "<SourceParent><LocURI>100</LocURI></SourceParent>" +
                            "<Meta><Type>application/vnd.omads-folder+xml</Type></Meta>" +
                            SYNCML_FOLDER_DATA +
                            "</Item>";

        Hashtable hierarchy = new Hashtable();
        hierarchy.put("222", "2222");
        hierarchy.put("333", "3333");
        hierarchy.put("100", "xyz01"); // the parent mapping
        hierarchy.put("999", "9999");
        simpleSyncItemTest(xmlCommand, "101", "xyz01", hierarchy);
    }

    /**
     * Check if the SourceParent is correctly read and translated to the local
     * parent key. A SyncException must be thrown if the local parent key is not
     * found.
     * @throws java.lang.Throwable
     */
    public void testWrongSourceParent() throws Throwable {

        String xmlCommand = "<Item>" +
                            "<Source><LocURI>101</LocURI></Source>" +
                            "<SourceParent><LocURI>100</LocURI></SourceParent>" +
                            "<Meta><Type>application/vnd.omads-folder+xml</Type></Meta>" +
                            SYNCML_FOLDER_DATA +
                            "</Item>";

        Hashtable hierarchy = new Hashtable();
        hierarchy.put("222", "2222");
        hierarchy.put("333", "3333");
        hierarchy.put("999", "9999");

        // The parent key is not mapped, the item must have a source parent set
        simpleSyncItemTest(xmlCommand, "101", null, "100", hierarchy);
    }

    public void testCancelAddItem1() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 2 * 1024, formatter);

        Object creps[] = new Object[1];

        // Simulate a complete item
        Chunk item1 = new Chunk("0", null, null, null, false);
        byte content1[] = new byte[500];
        fillContent(content1, 'A');
        item1.setContent(content1);

        handler.cancel();
        boolean interrupted = false;
        try {
            handler.addItem(item1, creps);
        } catch (SyncException se) {
            if (se.getCode() == SyncException.CANCELLED) {
                interrupted = true;
            }
        }
        assertTrue(interrupted);
    }

    public void testCancelAddItem2() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 2 * 1024, formatter);

        Object creps[] = new Object[1];

        // Simulate a complete item
        Chunk item1 = new Chunk("0", null, null, null, false);
        byte content1[] = new byte[500];
        fillContent(content1, 'A');
        item1.setContent(content1);

        Chunk item2 = new Chunk("1", null, null, null, false);
        byte content2[] = new byte[500];
        fillContent(content2, 'B');
        item2.setContent(content2);

        // Add the first item before interrupting the sync
        handler.addItem(item1, creps);
        handler.cancel();
        boolean interrupted = false;
        try {
            handler.addItem(item1, creps);
        } catch (SyncException se) {
            if (se.getCode() == SyncException.CANCELLED) {
                interrupted = true;
            }
        }
        assertTrue(interrupted);
    }

    public void testCancelUpdateItem1() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 2 * 1024, formatter);

        Chunk item1 = new Chunk("0", null, null, null, false);
        byte content1[] = new byte[500];
        fillContent(content1, 'A');
        item1.setContent(content1);
        Object creps[] = new Object[1];
        boolean interrupted = false;
        handler.cancel();
        try {
            handler.updateItem(item1, creps);
        } catch (SyncException se) {
            if (se.getCode() == SyncException.CANCELLED) {
                interrupted = true;
            }
        }
        assertTrue(interrupted);
    }

    public void testCancelUpdateItem2() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 2 * 1024, formatter);

        Chunk item1 = new Chunk("0", null, null, null, false);
        byte content1[] = new byte[500];
        fillContent(content1, 'A');
        item1.setContent(content1);
        Object creps[] = new Object[1];
        handler.updateItem(item1, creps);

        Chunk item2 = new Chunk("1", null, null, null, false);
        byte content2[] = new byte[500];
        fillContent(content2, 'B');
        item2.setContent(content2);
        handler.cancel();

        boolean interrupted = false;
        try {
            handler.updateItem(item1, creps);
        } catch (SyncException se) {
            if (se.getCode() == SyncException.CANCELLED) {
                interrupted = true;
            }
        }
        assertTrue(interrupted);
    }

    public void testCancelGetAddCommand() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        Vector newItems = new Vector(3);
        SyncItem item0 = new SyncItem("0", null, SyncItem.STATE_NEW, null);
        byte content0[] = new byte[10];
        fillContent(content0, 'A');
        item0.setContent(content0);

        newItems.addElement(item0);
        ss.setNextNewItems(newItems);

        TestSyncListener listener = new TestSyncListener();
        StringBuffer cmdTag = new StringBuffer();
        CmdId cmdId = new CmdId(0);

        handler.cancel();

        boolean interrupted = false;
        try {
            handler.getAddCommand(0, listener, cmdTag, cmdId);
        } catch (SyncException se) {
            if (se.getCode() == SyncException.CANCELLED) {
                interrupted = true;
            }
        }
        assertTrue(interrupted);
    }

    public void testCancelGetAddCommand2() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        Vector newItems = new Vector(3);
        SyncItem item0 = new SyncItem("0", null, SyncItem.STATE_NEW, null);
        byte content0[] = new byte[10];
        fillContent(content0, 'A');
        item0.setContent(content0);

        SyncItem item1 = new SyncItem("1", null, SyncItem.STATE_NEW, null);
        byte content1[] = new byte[10];
        fillContent(content1, 'A');
        item1.setContent(content1);

        newItems.addElement(item0);
        newItems.addElement(item1);

        ss.setNextNewItems(newItems);

        TestSyncListener listener = new TestSyncListener();
        StringBuffer cmdTag = new StringBuffer();
        CmdId cmdId = new CmdId(0);

        // The first message shall contain only the first item
        int status = handler.getAddCommand(0, listener, cmdTag, cmdId);
        handler.cancel();

        boolean interrupted = false;
        try {
            handler.getAddCommand(0, listener, cmdTag, cmdId);
        } catch (SyncException se) {
            if (se.getCode() == SyncException.CANCELLED) {
                interrupted = true;
            }
        }
        assertTrue(interrupted);
    }

    public void testCancelGetUpdateCommand() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        Vector updItems = new Vector(3);
        SyncItem item0 = new SyncItem("0", null, SyncItem.STATE_NEW, null);
        byte content0[] = new byte[10];
        fillContent(content0, 'A');
        item0.setContent(content0);

        updItems.addElement(item0);
        ss.setNextUpdItems(updItems);

        TestSyncListener listener = new TestSyncListener();
        StringBuffer cmdTag = new StringBuffer();
        CmdId cmdId = new CmdId(0);

        handler.cancel();

        boolean interrupted = false;
        try {
            handler.getReplaceCommand(0, listener, cmdTag, cmdId);
        } catch (SyncException se) {
            if (se.getCode() == SyncException.CANCELLED) {
                interrupted = true;
            }
        }
        assertTrue(interrupted);
    }

    public void testCancelGetUpdateCommand2() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        Vector updItems = new Vector(3);
        SyncItem item0 = new SyncItem("0", null, SyncItem.STATE_NEW, null);
        byte content0[] = new byte[10];
        fillContent(content0, 'A');
        item0.setContent(content0);

        SyncItem item1 = new SyncItem("1", null, SyncItem.STATE_NEW, null);
        byte content1[] = new byte[10];
        fillContent(content1, 'A');
        item1.setContent(content1);

        updItems.addElement(item0);
        updItems.addElement(item1);

        ss.setNextUpdItems(updItems);

        TestSyncListener listener = new TestSyncListener();
        StringBuffer cmdTag = new StringBuffer();
        CmdId cmdId = new CmdId(0);

        // The first message shall contain only the first item
        int status = handler.getReplaceCommand(0, listener, cmdTag, cmdId);
        handler.cancel();

        boolean interrupted = false;
        try {
            handler.getReplaceCommand(0, listener, cmdTag, cmdId);
        } catch (SyncException se) {
            if (se.getCode() == SyncException.CANCELLED) {
                interrupted = true;
            }
        }
        assertTrue(interrupted);
    }

    public void testCancelGetDeleteCommand() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        Vector delItems = new Vector(3);
        SyncItem item0 = new SyncItem("0", null, SyncItem.STATE_DELETED, null);

        delItems.addElement(item0);
        ss.setNextDelItems(delItems);

        TestSyncListener listener = new TestSyncListener();
        StringBuffer cmdTag = new StringBuffer();
        CmdId cmdId = new CmdId(0);

        handler.cancel();

        boolean interrupted = false;
        try {
            handler.getDeleteCommand(0, listener, cmdTag, cmdId);
        } catch (SyncException se) {
            if (se.getCode() == SyncException.CANCELLED) {
                interrupted = true;
            }
        }
        assertTrue(interrupted);
    }

    public void testCancelGetDeleteCommand2() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        Vector delItems = new Vector(3);
        SyncItem item0 = new SyncItem("0", null, SyncItem.STATE_DELETED, null);

        SyncItem item1 = new SyncItem("1", null, SyncItem.STATE_NEW, null);

        delItems.addElement(item0);
        delItems.addElement(item1);

        ss.setNextDelItems(delItems);

        TestSyncListener listener = new TestSyncListener();
        StringBuffer cmdTag = new StringBuffer();
        CmdId cmdId = new CmdId(0);

        // The first message shall contain only the first item
        boolean done = handler.getDeleteCommand(0, listener, cmdTag, cmdId);
        handler.cancel();

        boolean interrupted = false;
        try {
            handler.getDeleteCommand(0, listener, cmdTag, cmdId);
        } catch (SyncException se) {
            if (se.getCode() == SyncException.CANCELLED) {
                interrupted = true;
            }
        }
        assertTrue(interrupted);
    }

    public void testCancelGetNextCommand() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        Vector updItems = new Vector(3);
        SyncItem item0 = new SyncItem("0", null, SyncItem.STATE_UPDATED, null);
        byte content0[] = new byte[10];
        fillContent(content0, 'A');
        item0.setContent(content0);

        updItems.addElement(item0);
        ss.setNextItems(updItems);

        TestSyncListener listener = new TestSyncListener();
        StringBuffer cmdTag = new StringBuffer();
        CmdId cmdId = new CmdId(0);

        handler.cancel();

        boolean interrupted = false;
        try {
            handler.getNextCommand(0, listener, cmdTag, cmdId);
        } catch (SyncException se) {
            if (se.getCode() == SyncException.CANCELLED) {
                interrupted = true;
            }
        }
        assertTrue(interrupted);
    }

    public void testCancelGetNextCommand2() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        Vector updItems = new Vector(3);
        SyncItem item0 = new SyncItem("0", null, SyncItem.STATE_UPDATED, null);
        byte content0[] = new byte[10];
        fillContent(content0, 'A');
        item0.setContent(content0);

        SyncItem item1 = new SyncItem("1", null, SyncItem.STATE_UPDATED, null);
        byte content1[] = new byte[10];
        fillContent(content1, 'B');
        item1.setContent(content1);

        updItems.addElement(item0);
        updItems.addElement(item1);

        ss.setNextItems(updItems);

        TestSyncListener listener = new TestSyncListener();
        StringBuffer cmdTag = new StringBuffer();
        CmdId cmdId = new CmdId(0);

        // The first message shall contain only the first item
        int done = handler.getNextCommand(0, listener, cmdTag, cmdId);
        handler.cancel();

        boolean interrupted = false;
        try {
            handler.getNextCommand(0, listener, cmdTag, cmdId);
        } catch (SyncException se) {
            if (se.getCode() == SyncException.CANCELLED) {
                interrupted = true;
            }
        }
        assertTrue(interrupted);
    }

    public void testGetNextCommandWithItemsLimit1() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        // Simulate a max number of items per sync of 1
        config.setMaxItemsPerMessageInSlowSync(1);
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        Vector items = new Vector();
        SyncItem item0 = new SyncItem("0", null, SyncItem.STATE_UPDATED, null);
        byte content0[] = new byte[32];
        fillContent(content0, 'A');
        item0.setContent(content0);

        SyncItem item1 = new SyncItem("1", null, SyncItem.STATE_UPDATED, null);
        byte content1[] = new byte[16];
        fillContent(content1, 'A');
        item1.setContent(content1);

        SyncItem item2 = new SyncItem("2", null, SyncItem.STATE_UPDATED, null);
        byte content2[] = new byte[16];
        fillContent(content2, 'A');
        item2.setContent(content2);

        items.addElement(item0);
        items.addElement(item1);
        items.addElement(item2);

        ss.setNextItems(items);

        TestSyncListener listener = new TestSyncListener();
        StringBuffer cmdTag = new StringBuffer();
        CmdId cmdId = new CmdId(0);

        // The first message shall contain only the first item
        int done = handler.getNextCommand(0, listener, cmdTag, cmdId);
        Log.debug(cmdTag.toString());
        assertTrue(done == SyncSourceLOHandler.FLUSH);
        assertTrue(cmdId.getValue() == 1);
        assertTrue(listener.getReplaceSent() == 1);

        // The second message shall contain another item
        cmdTag = new StringBuffer();
        done = handler.getNextCommand(0, listener, cmdTag, cmdId);
        Log.debug(cmdTag.toString());
        assertTrue(done == SyncSourceLOHandler.FLUSH);
        assertTrue(cmdId.getValue() == 2);
        assertTrue(listener.getReplaceSent() == 2);

        // The third message shall contain the last item item
        cmdTag = new StringBuffer();
        done = handler.getNextCommand(0, listener, cmdTag, cmdId);
        Log.debug(cmdTag.toString());
        assertTrue(done == SyncSourceLOHandler.DONE);
        assertTrue(cmdId.getValue() == 3);
        assertTrue(listener.getReplaceSent() == 3);
    }

    public void testGetNextCommandWithItemsLimit2() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        // Simulate a max number of items per sync of 2
        config.setMaxItemsPerMessageInSlowSync(2);
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        Vector items = new Vector();
        SyncItem item0 = new SyncItem("0", null, SyncItem.STATE_UPDATED, null);
        byte content0[] = new byte[32];
        fillContent(content0, 'A');
        item0.setContent(content0);

        SyncItem item1 = new SyncItem("1", null, SyncItem.STATE_UPDATED, null);
        byte content1[] = new byte[16];
        fillContent(content1, 'A');
        item1.setContent(content1);

        SyncItem item2 = new SyncItem("2", null, SyncItem.STATE_UPDATED, null);
        byte content2[] = new byte[16];
        fillContent(content2, 'A');
        item2.setContent(content2);

        items.addElement(item0);
        items.addElement(item1);
        items.addElement(item2);

        ss.setNextItems(items);

        TestSyncListener listener = new TestSyncListener();
        StringBuffer cmdTag = new StringBuffer();
        CmdId cmdId = new CmdId(0);

        // The first message shall contain only the first two items
        int done = handler.getNextCommand(0, listener, cmdTag, cmdId);
        Log.debug(cmdTag.toString());
        assertTrue(done == SyncSourceLOHandler.FLUSH);
        assertTrue(cmdId.getValue() == 1);
        assertTrue(listener.getReplaceSent() == 2);

        // The third message shall contain the last item item
        cmdTag = new StringBuffer();
        done = handler.getNextCommand(0, listener, cmdTag, cmdId);
        Log.debug(cmdTag.toString());
        assertTrue(done == SyncSourceLOHandler.DONE);
        assertTrue(cmdId.getValue() == 2);
        assertTrue(listener.getReplaceSent() == 3);
    }

    public void testGetNextCommandWithItemsLimit3() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        // Simulate a max number of items per sync of 3
        config.setMaxItemsPerMessageInSlowSync(3);
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        Vector items = new Vector();
        SyncItem item0 = new SyncItem("0", null, SyncItem.STATE_UPDATED, null);
        byte content0[] = new byte[32];
        fillContent(content0, 'A');
        item0.setContent(content0);

        SyncItem item1 = new SyncItem("1", null, SyncItem.STATE_UPDATED, null);
        byte content1[] = new byte[16];
        fillContent(content1, 'A');
        item1.setContent(content1);

        SyncItem item2 = new SyncItem("2", null, SyncItem.STATE_UPDATED, null);
        byte content2[] = new byte[16];
        fillContent(content2, 'A');
        item2.setContent(content2);

        items.addElement(item0);
        items.addElement(item1);
        items.addElement(item2);

        ss.setNextItems(items);

        TestSyncListener listener = new TestSyncListener();
        StringBuffer cmdTag = new StringBuffer();
        CmdId cmdId = new CmdId(0);

        // The first message shall contain all three items
        int done = handler.getNextCommand(0, listener, cmdTag, cmdId);
        Log.debug(cmdTag.toString());
        assertTrue(done == SyncSourceLOHandler.DONE);
        assertTrue(cmdId.getValue() == 1);
        assertTrue(listener.getReplaceSent() == 3);
    }


    public void testGetNextCommandWithItemsLimit4() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        // Simulate a max number of items per sync of 1
        config.setBreakMsgOnLastChunk(true);
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        Vector items = new Vector();
        SyncItem item0 = new SyncItem("0", null, SyncItem.STATE_UPDATED, null);
        byte content0[] = new byte[600];
        fillContent(content0, 'A');
        item0.setContent(content0);

        SyncItem item1 = new SyncItem("1", null, SyncItem.STATE_UPDATED, null);
        byte content1[] = new byte[16];
        fillContent(content1, 'A');
        item1.setContent(content1);

        items.addElement(item0);
        items.addElement(item1);

        ss.setNextItems(items);

        TestSyncListener listener = new TestSyncListener();
        StringBuffer cmdTag = new StringBuffer();
        CmdId cmdId = new CmdId(0);

        // The first message shall contain only the first item chunk
        int done = handler.getNextCommand(0, listener, cmdTag, cmdId);
        Log.debug(cmdTag.toString());
        assertTrue(done == SyncSourceLOHandler.MORE);
        assertTrue(cmdId.getValue() == 1);
        assertTrue(listener.getReplaceSent() == 0);

        // The second message shall contain the last chunk and nothing else
        cmdTag = new StringBuffer();
        done = handler.getNextCommand(0, listener, cmdTag, cmdId);
        Log.debug(cmdTag.toString());
        assertTrue(done == SyncSourceLOHandler.FLUSH);
        assertTrue(cmdId.getValue() == 2);
        assertTrue(listener.getReplaceSent() == 1);

        // The third message shall contain the last item
        cmdTag = new StringBuffer();
        done = handler.getNextCommand(0, listener, cmdTag, cmdId);
        Log.debug(cmdTag.toString());
        assertTrue(done == SyncSourceLOHandler.DONE);
        assertTrue(cmdId.getValue() == 3);
        assertTrue(listener.getReplaceSent() == 2);
    }




    public void testGetNextCommandWithItemsLimit5() throws Throwable {
        SourceConfig config = new SourceConfig("Test", "application/*", "briefcase");
        // Simulate a max number of items per sync of 1
        config.setBreakMsgOnLastChunk(true);
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        Vector newItems = new Vector();
        Vector updItems = new Vector();
        SyncItem item0 = new SyncItem("0", null, SyncItem.STATE_NEW, null);
        byte content0[] = new byte[600];
        fillContent(content0, 'A');
        item0.setContent(content0);

        SyncItem item1 = new SyncItem("1", null, SyncItem.STATE_UPDATED, null);
        byte content1[] = new byte[16];
        fillContent(content1, 'A');
        item1.setContent(content1);

        newItems.addElement(item0);
        updItems.addElement(item1);

        ss.setNextNewItems(newItems);
        ss.setNextUpdItems(updItems);

        TestSyncListener listener = new TestSyncListener();
        StringBuffer cmdTag = new StringBuffer();
        CmdId cmdId = new CmdId(0);

        // The first message shall contain only the first item chunk
        int done = handler.getAddCommand(0, listener, cmdTag, cmdId);
        Log.debug(cmdTag.toString());
        assertTrue(done == SyncSourceLOHandler.MORE);
        assertTrue(cmdId.getValue() == 1);
        assertTrue(listener.getAddSent() == 0);

        // The second message shall contain the last chunk and nothing else
        cmdTag = new StringBuffer();
        done = handler.getAddCommand(0, listener, cmdTag, cmdId);
        Log.debug(cmdTag.toString());
        assertTrue(done == SyncSourceLOHandler.FLUSH);
        assertTrue(cmdId.getValue() == 2);
        assertTrue(listener.getAddSent() == 1);

        // The third message shall contain the last item
        cmdTag = new StringBuffer();
        done = handler.getReplaceCommand(0, listener, cmdTag, cmdId);
        Log.debug(cmdTag.toString());
        assertTrue(done == SyncSourceLOHandler.DONE);
        assertTrue(cmdId.getValue() == 3);
        assertTrue(listener.getReplaceSent() == 1);
    }

    private void simpleSyncItemTest (String xmlCommand, String expectedKey,
            String expectedParent, Hashtable hierarchy) throws Throwable {

        simpleSyncItemTest(xmlCommand, expectedKey, expectedParent, null, hierarchy);
    }

    /**
     * Simple SyncItem test. Verify if the provided xml command is related to
     * the correct item key and parent key.
     * @param xmlCommand
     * @param expectedKey
     * @param expectedParent
     * @param hierarchy
     * @throws java.lang.Throwable
     */
    private void simpleSyncItemTest (String xmlCommand, String expectedKey,
            String expectedParent, String expectedSourceParent,
            Hashtable hierarchy) throws Throwable {

        SourceConfig config = new SourceConfig(SourceConfig.MAIL, SourceConfig.EMAIL_OBJECT_TYPE, SourceConfig.MAIL);
        config.setEncoding(SyncSource.ENCODING_NONE);
        TestSyncSource ss = new TestSyncSource(config);

        SyncMLFormatter formatter = new SyncMLFormatter();
        SyncSourceLOHandler handler = new SyncSourceLOHandler(ss, 512, formatter);
        Chunk item = handler.getItem("application/vnd.omads-folder+xml",
                                     new ChunkedString(xmlCommand), null, hierarchy);

        assertTrue(item.getType().equals("application/vnd.omads-folder+xml"));
        assertTrue(item.getKey().equals(expectedKey));
        if (expectedParent != null) {
            assertTrue(item.getParent().equals(expectedParent));
        }
        if (expectedSourceParent != null) {
            assertTrue(item.getSourceParent().equals(expectedSourceParent));
        }
    }

    private void fillContent(byte content[], char filler) {
        for(int i=0;i<content.length;++i) {
            content[i] = (byte)filler;
        }
    }
}

