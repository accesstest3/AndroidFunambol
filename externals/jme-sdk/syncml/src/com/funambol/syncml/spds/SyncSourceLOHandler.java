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

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.Hashtable;

import com.funambol.util.XmlException;
import com.funambol.util.ChunkedString;
import com.funambol.util.Base64;
import com.funambol.util.Log;
import com.funambol.util.StringUtil;
import com.funambol.util.XmlUtil;
import com.funambol.syncml.protocol.SyncMLStatus;
import com.funambol.syncml.protocol.SyncML;

/**
 * This class is part of the synchronization engine and it is not visible
 * outside of the package as it is not intended to be used externally.
 * Its purpose is to manage the interaction with the sync sources. In particular
 * everything related to the construction, composition and receiving of
 * SyncItem(s) is processed here.
 * These are the main functionalities of this class:
 *
 * 1) create modification messages by appending items provided by sync sources
 * until no more items are available of until the message reaches the max
 * message size
 *
 * 2) handle large objects for both incoming and outgoing items
 *
 */
class SyncSourceLOHandler {

    private static final String TAG_LOG = "SyncSourceLOHandler";

    public static final int DONE  = 0;
    public static final int FLUSH = 1;
    public static final int MORE  = 2;

    private static final int GET_NEXT_ITEM         = 0;
    private static final int GET_NEXT_NEW_ITEM     = 1;
    private static final int GET_NEXT_UPDATED_ITEM = 2;
 
    private static final int ADD_COMMAND           = 0;
    private static final int REPLACE_COMMAND       = 1;
    private static final int DELETE_COMMAND        = 2;

    private SyncSource             source;
    private int                    maxMsgSize;
    private Chunk                  nextAddChunk       = null;
    private Chunk                  nextReplaceChunk   = null;
    private SyncItem               nextDeleteItem     = null;
    private Chunk                  nextChunk          = null;
    private SyncItem               incomingLo         = null;
    private SyncItem               outgoingItem       = null;
    private byte[]                 previousChunk      = null;
    private ItemReader             outgoingItemReader = null;
    private OutputStream           incomingLoStream   = null;
    private boolean                cancel             = false;
    private SyncMLFormatter        formatter          = null;

    public SyncSourceLOHandler(SyncSource source, int maxMsgSize,
                               SyncMLFormatter formatter)
    {
        this.source     = source;
        this.maxMsgSize = maxMsgSize;
        this.formatter  = formatter;
    }

    /**
     * This method wraps the sync source addItem method. It is meant to handle
     * large objects as it takes a chunk and add it to the current item.
     * Consecutive chunks are all appended to the same output stream to
     * reconstruct the original large object. This method does not perform any
     * decoding as that is handled before we get here.
     * Once an item has been completely received it is handed to the sync source
     * for further processing.
     *
     * @param chunk a single chunk
     * @param creps the client representation for the item added by the sync
     * source
     * @return the SyncML status for this chunk
     */
    public int addItem(Chunk chunk, Object creps[]) throws SyncException {
        return addUpdateItem(chunk, creps, true);
    }

    /**
     * This method wraps the sync source updateItem method. It is meant to handle
     * large objects as it takes a chunk and add it to the current item.
     * Consecutive chunks are all appended to the same output stream to
     * reconstruct the original large object. This method does not perform any
     * decoding as that is handled before we get here.
     * Once an item has been completely received it is handed to the sync source
     * for further processing.
     *
     * @param chunk a single chunk
     * @param creps the client representation for the item added by the sync
     * source
     * @return the SyncML status for this chunk
     */
    public int updateItem(Chunk chunk, Object creps[]) {
        return addUpdateItem(chunk, creps, false);
    }

    public void cancel() {
        cancel = true;
    }

    private void cancelSync() throws SyncException
    {
        Log.info(TAG_LOG, "Cancelling sync for source ["+source.getName()+"]");
        throw new SyncException(SyncException.CANCELLED, "SyncManager sync got cancelled");
    }

    private boolean isSyncToBeCancelled() {
        return cancel;
    }

    private int addUpdateItem(Chunk chunk, Object creps[], boolean add) throws SyncException {
        Log.trace(TAG_LOG, "addUpdateItem " + chunk.getKey());

        if (isSyncToBeCancelled()) {
            cancelSync();
        }

        char state = add ? SyncItem.STATE_NEW : SyncItem.STATE_UPDATED;
        SyncItem item = getNextIncomingItem(chunk, state);

        // Grab the sync item output stream and append to it
        try {
            if (incomingLoStream == null) {
                incomingLoStream = item.getOutputStream();
            }

            if (incomingLoStream == null) {
                Log.error(TAG_LOG, "addUpdateItem Cannot write to null output stream");
                return SyncMLStatus.GENERIC_ERROR;
            }
            byte data[] = chunk.getContent();
            incomingLoStream.write(data);
            incomingLoStream.flush();
        } catch (IOException ioe) {
            Log.error(TAG_LOG, "addUpdateItem Cannot write to output stream: " + ioe.toString());

            // Close the output stream and finalize the large object
            incomingLo = null;
            try {
                incomingLoStream.close();
            } catch (IOException ioe2) {
                Log.error(TAG_LOG, "Cannot close output stream: " + ioe2.toString());
            } finally {
                incomingLoStream = null;
            }
            return SyncMLStatus.GENERIC_ERROR;
        }

        if (!chunk.hasMoreData()) {
            // This is the last chunk, close the output stream
            try {
                incomingLoStream.close();
            } catch (IOException ioe) {
                Log.error(TAG_LOG, "addUpdateItem Cannot close output stream " + ioe.toString());
                return SyncMLStatus.GENERIC_ERROR;
            } finally {
                incomingLo = null;
                incomingLoStream = null;
            }
            // Notify the source that an item was added
            int retStatus;
            if (add) {
                retStatus = source.addItem(item);
                // Set the chunk new key, after the source processed it. This way the
                // SyncManager can update the mappings table properly
                chunk.setKey(item.getKey());
            } else {
                retStatus = source.updateItem(item);
            }
            creps[0] = item.getClientRepresentation();
            return retStatus;
        } else {
            // Return the status to proceed
            return SyncMLStatus.CHUNKED_ITEM_ACCEPTED;
        }
    }

    /**
     * This is a utility method that returns the proper item to store the
     * incoming data. For single chunk items or the first chunk of a LO the
     * method asks the sync source to create a SyncItem of the proper type. When
     * a LO is being received this method returns the same item until the LO is
     * being completely received. Other methods (addItem and updateItem) use this
     * utility to fetch the current SyncItem and append the data received from
     * the server.
     *
     * @param chunk is the chunk received from the server (possibly an entire
     * item if not a LO)
     * @param state is the item state (can be NEW or UPDATED here)
     *
     * @return a SyncItem to hold the entire item (the concatenation of all the
     * chunks composing the item)
     */
    private SyncItem getNextIncomingItem(Chunk chunk, char state) {

        String key = chunk.getKey();
        Log.trace(TAG_LOG, "getNextIncomingItem " + key);

        if (incomingLo == null) {
            incomingLo = source.createSyncItem(chunk.getKey(),
                                               chunk.getType(),
                                               state,
                                               chunk.getParent(),
                                               chunk.getObjectSize());
            // Set the source parent if the info is available
            if (chunk.getSourceParent() != null) {
                incomingLo.setSourceParent(chunk.getSourceParent());
            }
        }
        return incomingLo;
    }

    /**
     * This method returns the Add command tag. The tag is composed by
     * concatenating all the necessary items. The method gets an add command at
     * a time until there are items or the message has reached its maximum size.
     * The chunking of large objects is performed by the utility method
     * getNextNewItem which is responsible for returning items of the proper
     * size.
     *
     * @param size is the current size of the message
     * @param listener is the SyncListener
     * @param cmdTag is the string containing syncml tag the method will fill
     * (output parameter)
     * @param cmdId is the CmdId. This is updated after each tag is added
     *
     * @return the status of this message (it can be DONE if no more items are
     * available, FLUSH if the current msg must be flushed or MORE if there are
     * more items but they don't fit in this message). When the method returns
     * FLUSH it is not known if there are new items to be sent. An extra call to
     * getAddCommand is required to check that.
     */
    public int getAddCommand(int size, SyncListener listener,
                             StringBuffer cmdTag, CmdId cmdId) throws SyncException {

        Log.trace(TAG_LOG, "getAddCommand");

        if (isSyncToBeCancelled()) {
            cancelSync();
        }

        Chunk chunk = null;
        if (nextAddChunk == null) {
            chunk = getNextNewItem();
            // No item for this source
            if (chunk == null) {
                return DONE;
            }
        } else {
            chunk = nextAddChunk;
            nextAddChunk = null;
        }

        String itemContent = formatItemAddUpdateTag(chunk);

        cmdTag.append(formatter.formatStartAddCommand());
        cmdTag.append(formatter.formatCmdId(cmdId.next()));

        // We allow a certain degree of flexibility in the message size
        // and do not complain if the item is not bigger than 10% of the
        // maxMsgSize
        if (size + itemContent.length() > ((maxMsgSize * 110)/100)) {
            // If the item does not fit in the max msg size then we shall drop
            // it but for backward compatibility we let it go through

            Log.info(TAG_LOG, source.getName() + 
                   " returned an item that exceeds max msg size and should be dropped");
        }

        int ret = MORE;
        boolean breakMsgOnLastChunk = source.getConfig().getBreakMsgOnLastChunk();
        do {
            cmdTag.append(itemContent);
            // Notify the listener
            notifyListener(listener, ADD_COMMAND, chunk);

            Chunk previousChunk = chunk;
            // Ask the source for next item
            chunk = getNextNewItem();

            // If this is the last chunk of a LO, we may need to flush
            if (breakMsgOnLastChunk && previousChunk.getLastChunkOfLO()) {
                Log.info(TAG_LOG, "Last chunk of a LO, flusing SyncML message");
                ret = FLUSH;
                break;
            }

            // Last new item found
            if (chunk == null) {
                ret = DONE; 
                break;
            }

            itemContent = formatItemAddUpdateTag(chunk);
        } while (size + cmdTag.length() + itemContent.length() < maxMsgSize);

        if (chunk != null) {
            // If we get here then we reached the max msg size, so
            // we store the next msg for the next message
            nextAddChunk = chunk;
        }

        cmdTag.append(formatter.formatEndAddCommand());

        return ret;
    }


    /**
     * This method returns the Replace command tag. The tag is composed by
     * concatenating all the necessary items. The method gets a replace command at
     * a time until there are items or the message has reached its maximum size.
     * The chunking of large objects is performed by the utility method
     * getNextUpdItem which is responsible for returning items of the proper
     * size.
     *
     * @param size is the current size of the message
     * @param listener is the SyncListener
     * @param cmdTag is the string containing syncml tag the method will fill
     * (output parameter)
     * @param cmdId is the CmdId. This is updated after each tag is added
     *
     * @return the status of this message (it can be DONE if no more items are
     * available, FLUSH if the current msg must be flushed or MORE if there are
     * more items but they don't fit in this message). When the method returns
     * FLUSH it is not known if there are new items to be sent. An extra call to
     * getReplaceCommand is required to check that.
     */
    public int getReplaceCommand(int size, SyncListener listener,
                                 StringBuffer cmdTag, CmdId cmdId) throws SyncException {

        Log.trace(TAG_LOG, "getReplaceCommand");

        if (isSyncToBeCancelled()) {
            cancelSync();
        }

        Chunk chunk = null;
        if (nextReplaceChunk == null) {
            chunk = getNextUpdatedItem();
            // No item for this source
            if (chunk == null) {
                return DONE;
            }
        } else {
            chunk = nextReplaceChunk;
            nextReplaceChunk = null;
        }

        String itemContent = formatItemAddUpdateTag(chunk);
        cmdTag.append(formatter.formatStartReplaceCommand());
        cmdTag.append(formatter.formatCmdId(cmdId.next()));

        // We allow a certain degree of flexibility in the message size
        // and do not complain if the item is not bigger than 10% of the
        // maxMsgSize
        if (size + itemContent.length() > ((maxMsgSize * 110)/100)) {
            // If the item does not fit in the max msg size then we shall drop
            // it but for backward compatibility we let it go through

            Log.info(TAG_LOG, source.getName() + 
                     " returned an item that exceeds max msg size and should be dropped");
        }

        int ret = MORE;
        boolean breakMsgOnLastChunk = source.getConfig().getBreakMsgOnLastChunk();
        do {
            cmdTag.append(itemContent);
            // Notify the listener
            notifyListener(listener, REPLACE_COMMAND, chunk);

            Chunk previousChunk = chunk;

            // Ask the source for next item
            chunk = getNextUpdatedItem();

            // If this is the last chunk of a LO, we may need to flush
            if (breakMsgOnLastChunk && previousChunk.getLastChunkOfLO()) {
                Log.info(TAG_LOG, "Last chunk of a LO, flusing SyncML message");
                ret = FLUSH;
                break;
            }

            // Last item found
            if (chunk == null) {
                ret = DONE;
                break;
            }

            itemContent = formatItemAddUpdateTag(chunk);
        } while (size + cmdTag.length() + itemContent.length() < maxMsgSize);

        if (chunk != null) {
            // If we get here then we reached the max msg size, so
            // we store the next msg for the next message
            nextReplaceChunk = chunk;
        }

        cmdTag.append(formatter.formatEndReplaceCommand());

        return ret;
    }

    /**
     * This method returns the Delete command tag. The tag is composed by
     * concatenating all the necessary items. The method gets a delete command at
     * a time until there are items or the message has reached its maximum size
     *
     * @param size is the current size of the message
     * @param listener is the SyncListener
     * @param cmdTag is the string containing syncml tag the method will fill
     * (output parameter)
     * @param cmdId is the CmdId. This is updated after each tag is added
     *
     * @return true iff there are no more items to send
     */
    public boolean getDeleteCommand(int size, SyncListener listener,
                                    StringBuffer cmdTag, CmdId cmdId)
    throws SyncException {

        Log.trace(TAG_LOG, "getDeleteCommand]");

        if (isSyncToBeCancelled()) {
            cancelSync();
        }

        SyncItem item = null;

        if (nextDeleteItem == null) {
            item = source.getNextDeletedItem();
            // No item for this source
            if (item == null) {
                return true;
            }
        } else {
            item = nextDeleteItem;
            nextDeleteItem = null;
        }

        String itemContent = formatter.formatItemDelete(item.getKey());

        cmdTag.append(formatter.formatStartDeleteCommand());
        cmdTag.append(formatter.formatCmdId(cmdId.next()));

        // We allow a certain degree of flexibility in the message size
        // and do not complain if the item is not bigger than 10% of the
        // maxMsgSize
        if (size + itemContent.length() > ((maxMsgSize * 110)/100)) {
            // If the item does not fit in the max msg size then we shall drop
            // it but for backward compatibility we let it go through

            Log.info(TAG_LOG, source.getName() + 
                   " returned an item that exceeds max msg size and should be dropped");
        }

        // Build Delete command
        boolean done = false;
        do {
            cmdTag.append(itemContent);

            // Notify the listener
            listener.itemDeleteSent(item);

            if (isSyncToBeCancelled()) {
                cancelSync();
            }

            // Ask the source for next item
            item = source.getNextDeletedItem();

            // Last item found
            if (item == null) {
                done = true;
                break;
            }
            itemContent = formatter.formatItemDelete(item.getKey());
        } while (size + cmdTag.length() + itemContent.length() < maxMsgSize);

        if (item != null) {
            // If we get here then we reached the max msg size, so
            // we store the next msg for the next message
            nextDeleteItem = item;
        }

        cmdTag.append(formatter.formatEndDeleteCommand());

        return done;
    }


    /**
     * This method returns the next command tag in a slow sync. The tag is composed by
     * concatenating all the necessary items.
     * The method gets an item at a time and pack it into a "replace" command.
     * The process continues until there are items or the message has reached its maximum size.
     * The chunking of large objects is performed by the utility method
     * getNextItem which is responsible for returning items of the proper
     * size.
     *
     * @param size is the current size of the message
     * @param listener is the SyncListener
     * @param cmdTag is the string containing syncml tag the method will fill
     * (output parameter)
     * @param cmdId is the CmdId. This is updated after each tag is added
     *
     * @return the status of this message (it can be DONE if no more items are
     * available, FLUSH if the current msg must be flushed or MORE if there are
     * more items but they don't fit in this message). When the method returns
     * FLUSH it is not known if there are new items to be sent. An extra call to
     * getNextCommand is required to check that.
     */
    public int getNextCommand(int size, SyncListener listener,
                              StringBuffer cmdTag, CmdId cmdId)
    throws SyncException {

        Chunk chunk = null;

        if (isSyncToBeCancelled()) {
            cancelSync();
        }

        if (nextChunk == null) {
            chunk = getNextItem();
            // No item for this source
            if (chunk == null) {
                return DONE;
            }
        } else {
            chunk = nextChunk;
            nextChunk = null;
        }

        String itemContent = formatItemAddUpdateTag(chunk);
        cmdTag.append(formatter.formatStartReplaceCommand());
        cmdTag.append(formatter.formatCmdId(cmdId.next()));

        // We allow a certain degree of flexibility in the message size
        // and do not complain if the item is not bigger than 10% of the
        // maxMsgSize
        if (size + itemContent.length() > ((maxMsgSize * 110)/100)) {
            Log.info(TAG_LOG, source.getName() + 
                   " returned an item that exceeds max msg size and should be dropped");
        }

        int ret = MORE;
        int itemsCounter = 1;
        int maxItemsPerMessageInSlowSync = source.getConfig().getMaxItemsPerMessageInSlowSync();
        boolean breakMsgOnLastChunk = source.getConfig().getBreakMsgOnLastChunk();
        Log.trace(TAG_LOG, "maxItemsPerMessageInSlowSync=" + maxItemsPerMessageInSlowSync);
        do {
            cmdTag.append(itemContent);
            // Notify the listener
            notifyListener(listener, REPLACE_COMMAND, chunk);

            Chunk previousChunk = chunk;

            // Ask the source for next item
            chunk = getNextItem();

            // If this is the last chunk of a LO, we may need to flush
            if (breakMsgOnLastChunk && previousChunk.getLastChunkOfLO()) {
                Log.info(TAG_LOG, "Last chunk of a LO, flusing SyncML message");
                ret = FLUSH;
                break;
            }

            // Last item found
            if (chunk == null) {
                ret = DONE;
                break;
            }

            // If we reached the max items count, then we are done
            // with this message
            if (maxItemsPerMessageInSlowSync > 0 && itemsCounter >= maxItemsPerMessageInSlowSync) {
                Log.info(TAG_LOG, "Reached max number of items per message in slow sync");
                ret = FLUSH;
                break;
            }

            itemContent = formatItemAddUpdateTag(chunk);
            itemsCounter++;
        } while (size + cmdTag.length() + itemContent.length() < maxMsgSize);

        if (chunk != null) {
            // If we get here then we reached the max msg size, so
            // we store the next msg for the next message
            nextChunk = chunk;
        }

        cmdTag.append(formatter.formatEndReplaceCommand());
        return ret;
    }

    public void releaseResources() {
        if (outgoingItemReader != null) {
            try {
                outgoingItemReader.close();
            } catch (IOException ioe) {
                Log.error(TAG_LOG, "Cannot close item reader " + ioe.toString());
            }
        }
        if (incomingLoStream != null) {
            try {
                incomingLoStream.close();
            } catch (IOException ioe) {
                Log.error(TAG_LOG, "Cannot close output stream " + ioe.toString());
            }
        }
    }

    private void notifyListener(SyncListener listener, int command, Chunk chunk) {

        Log.trace(TAG_LOG, "notifying listener");
        Log.trace(TAG_LOG, "key=" + chunk.getKey());
        Log.trace(TAG_LOG, "chunk number = " + chunk.getChunkNumber());
        Log.trace(TAG_LOG, "has more data = " + chunk.hasMoreData());

        if (chunk.getChunkNumber() == 0) {
            // A new chunk is about to begin
            long size = chunk.getObjectSize();
            switch (command) {
                case ADD_COMMAND:
                    listener.itemAddSendingStarted(chunk.getKey(), chunk.getParent(),
                                                   (int)size);
                    break;
                case REPLACE_COMMAND:
                    listener.itemReplaceSendingStarted(chunk.getKey(), chunk.getParent(),
                                                       (int)size);
                    break;
                default:
                    Log.error(TAG_LOG, "Unknown command type " + command);
                    break;
            }
        }
        
        if (chunk.hasMoreData()) {

            // This is an individual chunk
            switch (command) {
                case ADD_COMMAND:
                    listener.itemAddChunkSent(chunk.getKey(), chunk.getParent(),
                                              chunk.getContent().length);
                    break;
                case REPLACE_COMMAND:
                    listener.itemReplaceChunkSent(chunk.getKey(), chunk.getParent(),
                                                  chunk.getContent().length);
                    break;
                default:
                    Log.error(TAG_LOG, "Unexpected chunked item in delete command");
                    break;
            }
        } else {
            // This is the last chunk of a multi or single chunked item
            switch (command) {
                case ADD_COMMAND:
                    listener.itemAddSendingEnded(chunk.getKey(), chunk.getParent(),
                                                 chunk.getContent().length);
                    break;
                case REPLACE_COMMAND:
                    listener.itemReplaceSendingEnded(chunk.getKey(), chunk.getParent(),
                                                     chunk.getContent().length);
                    break;
                default:
                    Log.error(TAG_LOG, "Unknown command type " + command);
                    break;
            }
        }
    }

    private Chunk getNextItem() throws SyncException {
        return getNextItemHelper(GET_NEXT_ITEM);
    }

    private Chunk getNextNewItem() throws SyncException {
        return getNextItemHelper(GET_NEXT_NEW_ITEM);
    }

    private Chunk getNextUpdatedItem() throws SyncException {
        return getNextItemHelper(GET_NEXT_UPDATED_ITEM);
    }

    private Chunk getNextItemHelper(int syncSourceMethod) throws SyncException {
        Log.trace(TAG_LOG, "getNewItemHelper");

        if (isSyncToBeCancelled()) {
            cancelSync();
        }

        try {
            SyncItem newItem;
            boolean multiChunks = false;
            if (outgoingItemReader == null) {

                if (syncSourceMethod == GET_NEXT_ITEM) {
                    newItem = source.getNextItem();
                } else if (syncSourceMethod == GET_NEXT_NEW_ITEM) {
                    newItem = source.getNextNewItem();
                } else if (syncSourceMethod == GET_NEXT_UPDATED_ITEM) {
                    newItem = source.getNextUpdatedItem();
                } else {
                    // This is an internal error
                    throw new SyncException(SyncException.CLIENT_ERROR, "Unknown sync source method");
                }

                // If there are no more items to send, just return
                if (newItem == null) {
                    return null;
                }

                InputStream is = newItem.getInputStream();
                outgoingItemReader = new ItemReader(maxMsgSize, is,
                                                    source.getEncoding() == SyncSource.ENCODING_B64);
                outgoingItem = newItem;
            } else {
                multiChunks = true;
            }
            int size = outgoingItemReader.read();
            if (size <= 0) {
                throw new SyncException(SyncException.CLIENT_ERROR, "Internal error: size is zero");
            }

            // TODO: this can be removed once we can operate on sub-arrays
            byte actualContent[] = new byte[size];
            byte content[]       = outgoingItemReader.getChunkContent();
            for(int i=0;i<size;++i) {
                actualContent[i] = content[i];
            }
            ////////

            Chunk chunk = new Chunk(outgoingItem.getKey(), outgoingItem.getType(),
                    outgoingItem.getParent(),
                    actualContent,
                    !outgoingItemReader.last());
            chunk.setObjectSize(outgoingItem.getObjectSize());
            chunk.setChunkNumber(outgoingItemReader.getChunkNumber());

            if (outgoingItemReader.last()) {
                if (multiChunks) {
                    chunk.setLastChunkOfLO(true);
                }
                try {
                    outgoingItemReader.close();
                } catch (IOException ioe) {
                    Log.error(TAG_LOG, "Cannot close input stream " + ioe.toString());
                }
                outgoingItemReader = null;
            }
            return chunk;
        } catch (SyncException se) {
            throw se;
        } catch (Exception e) {
            throw new SyncException(SyncException.CLIENT_ERROR, e.toString());
        }
    }

    /**
     * Encode the item data according to the format specified by the SyncSource.
     *
     * @param formats the list of requested encodings (des, 3des, b64)
     * @param data the byte array of data to encode
     * @return the encoded byte array, or <code>null</code> in case of error
     */
    private byte[] encodeItemData(String[] formats, byte[] data) {

        if (formats != null && data != null) {
            // If ecryption types are specified, apply them
            for (int count = formats.length - 1; count >= 0; count--) {

                String encoding = formats[count];

                if (encoding.equals("b64")) {
                    data = Base64.encode(data);
                }
            /*
            else if (encoding.equals("des")) {
            // DES not supported now, ignore SyncSource encoding
            }
            else if (currentDecodeType.equals("3des")) {
            // 3DES not supported now, ignore SyncSource encoding
            }
             */
            }
        }
        return data;
    }

    ////////////////////////////// SyncML parser ////////////////////////////
    /**
     * Get an item from the SyncML tag.
     *
     * @param type the mime type of the item
     * @param xmlItem the SyncML tag for this item
     * @param formatList a list of encoding formats
     * @param hierarchy the current inverse mapping table, used to retrieve the SyncItem
     *                  parent, when the SourceParent is specified in the SyncML
     *                  command.
     *
     * @return a Chunk instance corresponding to the SyncML item
     *
     * @throws SyncException if the command parsing failed
     *
     */
    public Chunk getItem(String type, ChunkedString xmlItem,
                         String[] formatList, Hashtable hierarchy) throws SyncException {

        String key = null;
        String data = null;
        String parent = null;
        String sourceParent = null;
        byte[] content = null;

        if (isSyncToBeCancelled()) {
            cancelSync();
        }

        // Get item key
        try {
            ChunkedString itemLocUriTag = null;
            // Search for the item key from the <Target> or <Source> tags
            if (XmlUtil.getTag(xmlItem, SyncML.TAG_TARGET) >= 0) {
               itemLocUriTag = XmlUtil.getTagValue(xmlItem, SyncML.TAG_TARGET);
            }
            else if (XmlUtil.getTag(xmlItem, SyncML.TAG_SOURCE) >= 0) {
               itemLocUriTag = XmlUtil.getTagValue(xmlItem, SyncML.TAG_SOURCE);
            }
            else {
                Log.error(TAG_LOG, "Invalid item key from server: " + xmlItem);
                throw new SyncException(
                    SyncException.SERVER_ERROR,
                    "Invalid item key from server.");
            }
            key = XmlUtil.getTagValue(itemLocUriTag, SyncML.TAG_LOC_URI).toString();
        } catch (XmlException e) {
            Log.error(TAG_LOG, "Invalid item key from server: " + xmlItem);
            e.printStackTrace();
            throw new SyncException(
                    SyncException.SERVER_ERROR,
                    "Invalid item key from server.");
        }

        //
        // Get item parent, if present
        //
        // First of all search for the TargetParent tag, or the SourceParent if
        // it's present. In the latter case the correct item parent key must be
        // found from the current mapping table, throws a SyncException
        // otherwise.
        //
        if (XmlUtil.getTag(xmlItem, SyncML.TAG_TARGET_PARENT) >= 0) {
            try {
                parent = XmlUtil.getTagValue(
                        XmlUtil.getTagValue(
                        xmlItem, SyncML.TAG_TARGET_PARENT),
                        SyncML.TAG_LOC_URI).toString();
            } catch (XmlException e) {
                Log.error(TAG_LOG, "Invalid item target parent from server: " + e.toString());
                throw new SyncException(
                        SyncException.SERVER_ERROR,
                        "Invalid item target parent from server.");
            }
        } else if (XmlUtil.getTag(xmlItem, SyncML.TAG_SOURCE_PARENT) >= 0) {
            try {
                sourceParent =
                        XmlUtil.getTagValue(
                            XmlUtil.getTagValue(
                                xmlItem, SyncML.TAG_SOURCE_PARENT),
                            SyncML.TAG_LOC_URI).toString();

                // Lookup the parent key from the mapping table
                if(hierarchy != null) {
                    parent = (String) hierarchy.get(sourceParent);
                } else {
                    Log.error(TAG_LOG, "Invalid item parent from server, " +
                            "the current mapping table is empty.");
                    throw new SyncException(
                            SyncException.SERVER_ERROR,
                            "Invalid item parent from server.");
                }
                if(parent == null) {
                    Log.info(TAG_LOG, "Received an item without target parent and source parent: "
                                      + sourceParent);
                }
            } catch (XmlException e) {
                Log.error(TAG_LOG, "Invalid item parent from server: " + e.toString());
                throw new SyncException(
                        SyncException.SERVER_ERROR,
                        "Invalid item source parent from server.");
            }
        }

        // Check if the item has the MoreData
        boolean hasMoreData = false;
        if (XmlUtil.getTag(xmlItem, SyncML.TAG_MORE_DATA) != -1) {
            hasMoreData = true;
        }

        // Get the item data, if present
        if (XmlUtil.getTag(xmlItem, SyncML.TAG_DATA) != -1) {
            try {
                // Get item data
                data = XmlUtil.getTagValue(xmlItem, SyncML.TAG_DATA).toString();
                if (formatList != null) {
                    // Format tag from server
                    content = decodeItemData(hasMoreData, formatList, data.getBytes());
                } else {
                    content = data.getBytes("UTF-8");
                }
            } catch (UnsupportedEncodingException uee) {
                uee.printStackTrace();
                Log.error(TAG_LOG, "Can't decode content for item: " + key);
                // in case of error, the content is null
                // and this will be reported as an error to the server
                content = null;
            } catch (XmlException xe) {
                xe.printStackTrace();
                Log.error(TAG_LOG, "Can't parse data tag for item: " + key);
                // in case of error, the content is null
                // and this will be reported as an error to the server
                content = null;
            }
        }
        // Create an item in memory. We don't use the sync source item type
        // here, as this is a single chunk. The item handler will ask the source
        // to create the item if necessary
        Chunk chunk = new Chunk(key, type, parent, content, hasMoreData);
        if (parent == null && sourceParent != null) {
            chunk.setSourceParent(sourceParent);
        }
        return chunk;
    }

    /**
     * Process the &lt;Format&gt; tag and return the requested modification
     * in a String array.
     */
    private String[] processFormat(ChunkedString xml) {
        String[] ret = null;

        try {
            if (XmlUtil.getTag(xml, "Format") != -1) {
                ChunkedString format = XmlUtil.getTagValue(xml, "Format");

                if (format != null && !format.equals("")) {
                    ret = StringUtil.split(format.toString(), ";");
                }
            }
        } catch (XmlException e) {
            Log.error(TAG_LOG, "Error parsing format from server: " + xml + ". Ignoring it.");
            e.printStackTrace();
        }
        return ret;
    }

    ////////////////////////////// SyncML formatter ////////////////////////////
    /**
     * This method formats the Item tag for adds/updates that operate on chunks
     */
    private String formatItemAddUpdateTag(Chunk chunk) throws SyncException {

        StringBuffer ret = new StringBuffer();
       
        ret.append(formatter.formatStartItem());
         
        Log.info(TAG_LOG, "The encoding method is [" + source.getEncoding() + "]");
        String encodedData = null;

        if (!chunk.hasContent()) {
            Log.error(TAG_LOG, "Empty content from SyncSource for chunk:" +
                    chunk.getKey());
            encodedData = "";
        } else if (!source.getEncoding().equals(source.ENCODING_NONE)) {
            String[] formatList = StringUtil.split(
                    source.getEncoding(), ";");
            byte[] data = encodeItemData(formatList, chunk.getContent());

            encodedData = new String(data);
        } else {
            // Else, the data is text/plain,
            // and the XML special chars are escaped.
            String content = new String(chunk.getContent());
            encodedData = XmlUtil.escapeXml(content);
        }

        // Meta information
        ret.append(formatter.formatStartMeta());

        // type
        String theType = chunk.getType() == null ? source.getType() : chunk.getType();

        ret.append(formatter.formatItemType(theType));

        if (!source.getEncoding().equals(source.ENCODING_NONE)) {
            ret.append(formatter.formatItemFormat(source.getEncoding()));
        }

        // If this is the first chunk of a Large Object, and if this
        // item has a declared size then we must specify it in the meta
        // data. If the item must be encoded, the size must reflect
        // that.
        Log.trace(TAG_LOG, "objsize: "+ chunk.getObjectSize()
                + " chunk: "+ chunk.getChunkNumber()
                + " moredata: " + chunk.hasMoreData());

        if (chunk.getChunkNumber() == 0 && chunk.hasMoreData()) {
            if (chunk.getObjectSize() != -1) {
                long realObjSize = getRealSize(chunk.getObjectSize());
                ret.append(formatter.formatItemSize(realObjSize));
            } else {
                Log.error(TAG_LOG, "Cannot format a LO with unknown size");
                throw new SyncException(SyncException.CLIENT_ERROR, "LO with unknwon size");
            }
        }

        ret.append(formatter.formatEndMeta());

        // source
        ret.append(formatter.formatItemLuid(chunk.getKey()));
        //parent
        if (chunk.getParent() != null) {
            ret.append(formatter.formatItemParent(chunk.getParent()));
        }
        //item data
        Log.debug(TAG_LOG, "EncodedDataSize: " + encodedData.length());
        ret.append(formatter.formatItemData(encodedData));

        // More data flag
        if (chunk.hasMoreData()) {
            ret.append(formatter.formatMoreData());
        }
        ret.append(formatter.formatEndItem());

        return ret.toString();
    }
    ///////////////////////////////////////////////////////////////////////////////////

    /*
     * If the source has b64 encoding, compute the size of the item that will be sent.
     * B64 size is 4/3 of the original size.
     */
    private long getRealSize(long origSize) {
        if(source.getEncoding() == SyncSource.ENCODING_B64) {
            long rem  = origSize % 3;
            long size;
            if (rem == 0) {
                size = 4 * (origSize / 3);
            } else {
                size = 4 * ((origSize / 3) + 1);
            }
            return size;
        }
        else {
            return origSize;
        }
    }

    private byte[] decodeChunk(boolean hasMoreData, byte[] content) {

        int extra = 0;
        if (previousChunk != null) {
            extra = previousChunk.length;
        }
        int size = content.length + extra;
        int rem  = (3 * size) % 4;
        byte data[];

        if (rem != 0 && hasMoreData) {
            // We have a remainder, so we must truncate and keep the
            // extra bytes for the next chunk, unless this is the last
            // one
            int chunkableSize = (size / 4) * 4;
            rem = extra + content.length - chunkableSize;
            data = new byte[chunkableSize];
            int i;
            for(i=0;i<extra;++i) {
                data[i] = previousChunk[i];
            }
            for(i=0;i<content.length - rem;++i) {
                data[i+extra] = content[i];
            }
            previousChunk = new byte[rem];
            for(int j=0;j<rem;j++) {
                previousChunk[j] = content[i+j];
            }
        } else {
            int realSize = extra + content.length;
            // Copy everything
            data = new byte[realSize];
            int i;
            for(i=0;i<extra;++i) {
                data[i] = previousChunk[i];
            }
            for(i=0;i<content.length;++i) {
                data[i+extra] = content[i];
            }
            previousChunk = null;
        }
        if (data.length > 0) {
            data = Base64.decode(data);
        }
        return data;
    }

    /**
     * Decode the item data according to the format specified by the server.
     *
     * @param formats the list of requested decodings (des, 3des, b64)
     * @param data the byte array of data to decode
     * @return the decode byte array, or <code>null</code> in case of error
     *
     * @throws UnsupportedEncodingException
     */
    private byte[] decodeItemData(boolean hasMoreData, String[] formats, byte[] data)
            throws UnsupportedEncodingException {

        if (formats != null && data != null) {
            // If ecryption types are specified, apply them
            for (int count = formats.length - 1; count >= 0; count--) {

                String currentDecodeType = formats[count];

                if (currentDecodeType.equals("b64")) {
                    data = decodeChunk(hasMoreData, data);
                } else if (currentDecodeType.equals("des")) {
                    // Error, DES not supported now, send error to the server
                    return null;
                /*
                desCrypto = new Sync4jDesCrypto(Base64.encode(login.getBytes()));
                data = desCrypto.decryptData(data);
                 */
                } else if (currentDecodeType.equals("3des")) {
                    // Error, 3DES not supported now, send error to the server
                    return null;
                /*
                sync3desCrypto = new Sync4j3DesCrypto(Base64.encode(login.getBytes()));
                data = sync3desCrypto.decryptData(data);
                 */
                }
            }
        }
        return data;
    }
}

