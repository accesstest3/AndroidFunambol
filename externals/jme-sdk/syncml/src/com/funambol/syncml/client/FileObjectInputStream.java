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

package com.funambol.syncml.client;

import java.io.IOException;
import java.io.InputStream;

import com.funambol.util.Base64;
import com.funambol.util.Log;

/**
 * This class represents an input stream associated to a file object. A file
 * object is an XML representation of a file, as defined by OMA (see OMA File
 * Data Object Specification for more details).
 * The purpose of this class is to "decorate" a given input stream with a
 * prologue and an epilogue. The given input stream represents a file content,
 * while the prologue and epilogue represent the File Object meta information,
 * which is around the item content (see the FileObject definition for more
 * info). In order to obtain a proper prologue and epilogue, the user can use
 * the FileObject (@see FileObject) to create the prologue and epilogue via the
 * formatting methods.
 * This class is also responsible for encoding the content. The current
 * implementation always performs base64 encoding, so it expects FileObject
 * to generate a prologue with a base64 encoding for the body.
 */
public class FileObjectInputStream extends InputStream {
    private static final int B64BUFFER_SIZE = 3;

    private InputStream is         = null;
    private String prologue        = null;
    private String epilogue        = null;
    private int    idx             = 0;
    private int    dataSize        = 0;
    private byte   encodedValues[] = null;
    private int    encodedIdx      = 0;
    private int    totalSize       = 0;
    private int    totalIdx        = 0;

    /**
     * Constructs a FileObjectInputStream for the given input stream. The
     * prologue and epilogue contains the file object meta data.
     *
     * @param prologue is the FileObject prologue (the body must be reported as
     * base64 encoded)
     * @param @is is the data input stream
     * @param epilogue is the FileObject epilogue
     * @param dataSize is the size of raw data to be read
     */
    public FileObjectInputStream(String prologue, InputStream is,
                                 String epilogue, int dataSize)
    {
        this.is = is;
        this.prologue = prologue;
        this.epilogue = epilogue;
        this.dataSize = dataSize;
        int bodySize = Base64.computeEncodedSize(dataSize);
        // Set the size
        totalSize = prologue.length() + bodySize + epilogue.length();
    }

    /**
     * Reads the next byte from the input stream. Initially the bytes returned
     * are the ones from the prologue, then the body content (which is base64
     * encoded) and finally the epilogue bytes.
     *
     * @return the next byte or -1 on end of stream
     * @throws IOException if the underlying stream throws such an exception
     */
    public int read() throws IOException {
        // First of all we shall return the FileObject
        int ch;
        if (idx < prologue.length()) {
            // Return the next element in the prologue
            ch = (int) prologue.charAt(idx);
            idx++;
        } else if (idx < prologue.length() + dataSize ||
                   (encodedValues != null && encodedIdx < encodedValues.length))
        {
            // We must read and encode in base64. We read three bytes at a
            // time and encode them
            if (encodedValues != null && encodedIdx < encodedValues.length) {
                ch = (int) encodedValues[encodedIdx++];
            } else {
                // Try to read B64BUFFER_SIZE bytes
                // Do not rely on is.available as it is not always reliable
                byte values[] = new byte[B64BUFFER_SIZE];
                int size = 0;
                for(int i=0;i<B64BUFFER_SIZE;++i) {
                    int value = is.read();
                    if (value == -1) {
                        break;
                    }
                    values[i] = (byte)value;
                    size++;
                }
                if (size < B64BUFFER_SIZE) {
                    byte newValues[] = new byte[size];
                    for(int i=0;i<size;++i) {
                        newValues[i] = values[i];
                    }
                    values = newValues;
                }
                encodedValues = Base64.encode(values);
                encodedIdx = 0;
                ch = (int) encodedValues[encodedIdx++];
                idx += size;
            }
        } else if (idx < prologue.length() + dataSize + epilogue.length()) {
            ch = (int) epilogue.charAt(idx - prologue.length() - dataSize);
            idx++;
        } else {
            // We reached the end of this stream
            ch = -1;
        }

        if (ch != -1) {
            totalIdx++;
        }
        // Safety check
        if (totalIdx > totalSize) {
            // This should never happen, and if it does it is a bug in the code
            throw new IOException("FileObjectInputStream internal error, exceeded file size limit");
        }
        return ch;
    }

    /**
     * Returns the number of available bytes.
     */
    public int available() throws IOException {
        return (totalSize - totalIdx);
    }

    public void close() throws IOException {
        if (is != null) {
            is.close();
        }
    }
}


