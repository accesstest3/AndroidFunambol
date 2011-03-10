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

import java.io.InputStream;
import java.io.IOException;
import com.funambol.util.Log;

/**
 * This class represent a reader for outgoing items, providing methods to get chunks of
 * it of defined size.
 */
class ItemReader {
    // True if it's the last chunk
    private boolean last;
    // Sequence number of the chunk, starting from 0
    private int chunkNumber;
    // Content of the last chunk read
    private byte chunkContent[];
    // Inputstream to the local object to read
    private InputStream is;
    // Is data to be encoded?
    private boolean encoding;

    // the actual msg size is lower than the declared one
    private long computeReverseB64Size(long desiredSize) {

        long actualSize = (desiredSize * 3) / 4;
        for(long i=0;i<3;i++) {
            if ((actualSize + i) % 3 == 0) {
                return actualSize + i;
            }
        }
        // Should never get here
        return -1;
    }

    private long computeActualSize(long size) {
        if (encoding) {
            return computeReverseB64Size(size);
        } else {
            return size;
        }
    }

    /**
     * Constructs the LargeObjectReader, with given chunk size and InputStream.
     *
     * @param maxMsgSize the formal msgSize, which can be reduced to take
     *                   overhead into account
     * @param is the InputStream to read from.
     * @param encoding true if data shall be base64 encoded
     */
    public ItemReader(int maxMsgSize, InputStream is, boolean encoding) {
        this.chunkNumber = -1;
        this.chunkContent = null;
        this.is = is;
        this.encoding = encoding;
        this.chunkContent = new byte[(int)computeActualSize(maxMsgSize)];
        this.last = false;
    }

    /**
     * Read the next chunk. This method always tries to create an item of size
     * maxMsgSize (taking encoding into account). This behavior is justified by
     * the assumption that the engine does not try to split an item on a message
     * partially filled.
     *
     * @return the number of bytes read, or -1 on end of file.
     */
    public int read() throws IOException {

        Log.trace("[ItemReader.read]");

        if (is == null) {
            throw new IOException("Cannot read from null input stream");
        }

        chunkNumber++;

        // Now read chunkSize bytes
        Log.trace("reading " + chunkContent.length);
        int chunkSize = is.read(chunkContent);
        Log.trace("actually read " + chunkSize);
        if(is.available() == 0) {
            last = true;
        }
        return chunkSize;
    }

    /**
     * Returns the sequence number of the last chunk read, starting from 0.
     *
     * @return an integer containing the last chunk sequence number, 
     *         starting from 0.
     */
    public int getChunkNumber() {
        return chunkNumber;
    }

    /**
     * Returns true if the last chunk has been read.
     *
     * @return <code>true</code> if the last chunk has been read.
     */
    public boolean last() {
        return last;
    }

    /**
     * Returns true if the last chunk has been read.
     *
     * @return <code>true</code> if the last chunk has been read.
     */
    public byte[] getChunkContent() {
        return chunkContent;
    }

    /**
     * Close the input stream and finalize the object. After this call it is no
     * longer possible to read data.
     */
    public void close() throws IOException {
        if (is != null) {
            is.close();
            is = null;
        }
    }

}

