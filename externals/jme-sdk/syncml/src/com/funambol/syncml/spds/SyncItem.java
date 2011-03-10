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
public class SyncItem {
    
    //----------------------------------------------------------------- Constants
    public static final char STATE_NEW = 'N';
    public static final char STATE_UPDATED = 'U';
    public static final char STATE_DELETED = 'D';
    public static final char STATE_UNDEF = ' ';

    //-------------------------------------------------------------- Private data

    /** The key of this item.  */
    private String key;
    
    /** The mime-type of this item. Default is text/plain.  */
    private String type;
    
    /** The state of this item ([N]ew, [U]pdated, [D]eleted) */
    private char state;

    /** The name of the parent folder of the item (target ref).  */
    private String parent;

    /** The name of the parent folder of the item (source ref).  */
    private String sourceParent;
    
    /** The client representation of this item (may be null) */
    private Object clientRepresentation;

    /** The size of the full large object to transfer */
    private long objectSize;

    /** The output stream */
    protected ByteArrayOutputStream os;

    
    //------------------------------------------------------------- Constructors
    
    /**
     * Basic constructor. Only the key is required, the others
     * are set to a default and can be set later.
     */
    public SyncItem(String key) {
        this(key, null, STATE_NEW, null, null);
    }
    
    /**
     * Full contructor. All the item's fields are passed by the caller.
     */
    public SyncItem(String key, String type, char state,
                    String parent) {
        this(key, type, state, parent, null);
    }

    /**
     * Full contructor. All the item's fields are passed by the caller.
     */
    public SyncItem(String key, String type, char state,
                             String parent, byte content[]) {
        this.key = key;
        this.type = type;
        this.state = state;
        this.parent = parent;
        this.clientRepresentation = null;
        this.objectSize = -1;
        this.os = new ByteArrayOutputStream();
        setContent(content);
    }



    /**
     * Copy constructor. The item is created using the values from another
     * one.
     */
    public SyncItem(SyncItem that) {
        this.key = that.key;
        this.type = that.type;
        this.state = that.state;
        this.parent = that.parent;
        this.clientRepresentation = that.clientRepresentation;
        this.objectSize = that.objectSize;
        this.os = that.os;
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
     * Get the item state
     */
    public char getState() {
        return this.state;
    }
    
    /**
     * Set the item state
     */
    public void setState(char state) {
        this.state = state;
    }
    
    /**
     * Get the item parent. This is expressed as target ref and it may be null
     * if it is unknown.
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
     * Get the client representation of this item (maybe null)
     */
    public Object getClientRepresentation() {
        return this.clientRepresentation;
    }

    /**
     * Set the client representation of this item (maybe null)
     */
    public void setClientRepresentation(Object clientRepresentation) {
        this.clientRepresentation = clientRepresentation;
    }

    public long getObjectSize() {
        return objectSize;
    }

    /**
     * Set the full object size of the Large Object (mandatory if the item is chunked)
     */
    public void setObjectSize(long size) {
        this.objectSize = size;
    }

    /**
     * Returns an OutputStream to write data to. In this default implementation
     * a ByteArrayOutputStream is used to store the content. SyncSource that
     * need to manipulate bug items shall redefine this method to use a non
     * memory based sync item.
     */
    public OutputStream getOutputStream() throws IOException {
        return os;
    }

    /**
     * Returns an InputStream to read data from. Such an InputStream contains
     * what has been written so far in the OutputStream. If the content is
     * changed after this method is invoked, then the changes are not visible in
     * the InputStream, unless this method is re-invoked.
     * This default implementation is based on the default OutputStream, but it
     * does not work properly if the OutputStream is redefined. If the class is
     * extended and the OutputStream redefined, then this method *MUST* be
     * reimplemented to return the proper InputStream.
     *
     * @return an InputStream if the item contains at least one byte, null
     * otherwise
     */
    public InputStream getInputStream() throws IOException {
        if (os == null) {
            return null;
        } else {
            byte buf[] = os.toByteArray();
            ByteArrayInputStream is = new ByteArrayInputStream(buf);
            return is;
        }
    }

    /**
     * Get the content of this item. This method shall be used only when it is
     * known that the item can be safely loaded in memory completely. For big
     * items this method may generate OutOfMemoryError or performance
     * degradation. In all these cases it is much preferable to use the
     * getInputStream method and work on the stream.
     */
    public byte[] getContent() {
        if (os != null) {
            return os.toByteArray();
        } else {
            return null;
        }
    }

    /**
     * Returns true iff this item has a content set to some value
     */
    public boolean hasContent() {
        return os != null;
    }
    
    /**
     * Set the content of this item. The previous content is removed and
     * replaced by this one, unless the specified content is null. In such a
     * case the content is left unchanged.
     */
    public void setContent(byte[] content) {
        // Write all the content into the source OutputStream
        if (content != null) {
            try {
                OutputStream sos = getOutputStream();
                sos.write(content);
                objectSize = content.length;
            } catch (IOException ioe) {
                // We should never fall into this case, as the default
                // implementation of the StreamingSyncItem writes in memory
                // but if this happens, we treat this as an OutOfMemoryError
                throw new OutOfMemoryError("Cannot write item content");
            }
        }
    }
}

