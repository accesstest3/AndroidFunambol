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

package com.funambol.syncml.spds;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

/**
 * This class is a container for items exchanged between the SyncManager
 * and the SyncSources. These items have a content that can be streamed for
 * reading and writing purposes.
 * 
 */
class Chunk {
    
    //-------------------------------------------------------------- Private data

    /** The key of this item.  */
    private String key;
    
    /** The mime-type of this item. Default is text/plain.  */
    private String type;
    
    /** The name of the parent folder of the item (expressed as target parent)  */
    private String parent;

    /** The name of the parent folder of the item (expressed as source parent)  */
    private String sourceParent;
    
    /** True if this is a chunk of data and this is not the last one */
    private boolean hasMoreData;

    /** The chunk number, used when tranferring large objects */
    private int chunkNumber;

    /** The size of the full large object to transfer */
    private long objectSize;

    private byte[] content;

    /** Signals if this is the last chunk of a lo. For single chunk items, this
     * is false
     */
    private boolean lastChunkOfLO;

    //------------------------------------------------------------- Constructors
    
    /**
     * Full contructor. All the item's fields are passed by the caller.
     */
    public Chunk(String key, String type,
                             String parent, byte content[], boolean hasMoreData) {
        this.key = key;
        this.type = type;
        this.parent = parent;
        this.hasMoreData = hasMoreData;
        this.objectSize = -1;
        this.content = content;
    }


    //----------------------------------------------------------- Public methods
    
    /**
     * Get the current key
     */
    public String getKey() {
        return this.key;
    }
    
    /**
     * Set the current key
     */
    public void setKey(String key) {
        this.key = key;
    }
    
    /**
     * Get the item type (this property may be null)
     * A value whose type is null has the type of the SyncSource
     * it belongs to.
     */
    public String getType() {
        return this.type;
    }
    
    /**
     * Set the item type
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Get the item parent expressed as target parent. The value returned may be
     * null if the info is not available. In this case the parent may be
     * expressed as source parent.
     */
    public String getParent() {
        return this.parent;
    }
    
    /**
     * Set the item parent
     */
    public void setParent(String parent) {
        this.parent = parent;
    }

    /**
     * Get the item parent. This is expressed as source ref and it may be null
     * if unknown (or the target parent is available)
     */
    public String getSourceParent() {
        return sourceParent;
    }
    
    /**
     * Set the item parent
     */
    public void setSourceParent(String sourceParent) {
        this.sourceParent = sourceParent;
    }

    /**
     * Returns <code>true</code> if this is not the last chunk. 
     */
    public boolean hasMoreData() {
        return hasMoreData;
    }

    /**
     * Set this item as not the last chunk.
     */
    public void setHasMoreData() {
        hasMoreData = true;
    }

    public void clearHasMoreData() {
        hasMoreData = false;
    }

    public long getObjectSize() {
        return objectSize;
    }

    /**
     * Set the full object size of the item this chunk belongs to.
     */
    public void setObjectSize(long size) {
        this.objectSize = size;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public void setChunkNumber(int chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

    /**
     * Get the content of this chunk.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Returns true if this chunk has a content set to some value
     */
    public boolean hasContent() {
        return content != null;
    }
    
    /**
     * Set the content of this chunk.
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setLastChunkOfLO(boolean value) {
        this.lastChunkOfLO = value;
    }

    public boolean getLastChunkOfLO() {
        return lastChunkOfLO;
    }
}

