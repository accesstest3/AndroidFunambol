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
import java.io.OutputStream;
import java.io.ByteArrayInputStream;

import com.funambol.util.Log;
import com.funambol.util.Base64;
import com.funambol.util.StringUtil;


/**
 * This class represents a stream into which a file object can be written
 * directly. It is responsibility of this stream to separate the meta
 * information from the real file content. The user can simply stream bytes as
 * they arrive from a file object source into this output stream. When the "body"
 * of the item starts, then its content is written into an output stream that
 * must be provided to this class. After the stream is closed, the user can ask
 * for a FileObject which contains all the file object meta data.
 */
public class FileObjectOutputStream extends OutputStream {

    private static final int B64BUFFER_SIZE = 4;

    private StringBuffer currentToken = null;
    private StringBuffer buffer = new StringBuffer();
    private OutputStream os;
    private boolean content = false;
    private byte[] b64Buffer = new byte[B64BUFFER_SIZE];
    private int b64Idx = 0;
    private FileObject fo;

    private boolean prologue = true;
    private boolean body     = false;
    private boolean epilogue = false;
    private boolean invalidObject = false;

    /**
     * Builds the output stream.
     * @param fo is the file object which is filled with the meta information
     * which comes into the stream
     * @param os is the output stream into which the file object body is written
     * into
     */
    public FileObjectOutputStream(FileObject fo, OutputStream os) {
        this.os = os;
        this.fo = fo;
    }

    /**
     * Writes a single byte into the stream. If the body tag has not been found
     * yet, then the info is kept as meta information. Once the body is
     * encountered, then all the subsequent bytes are written into the real
     * output stream. If the content is base64 encoded, then the method performs
     * also decoding.
     * The body tag is recognized by the FileObject parser, which parses the
     * information received so far and it extracts the body content. This makes
     * the parsing mechanism safe.
     *
     * @param b the byte write
     * @throws IOException if the operation cannot be performed. This can be due
     * to several reasons, including a failure of the underlying output stream
     * or an invalid file object content that cannot be parsed.
     */
    public void write(int b) throws IOException {
        if (prologue) {
            buffer.append((char)b);
            if (buffer.length() == 1024) {
                ByteArrayInputStream bis = new ByteArrayInputStream(buffer.toString().getBytes());
                try {
                    String bod = fo.parsePrologue(bis);
                    // Note that if the body cannot be found, or it contains
                    // part of the epilogue, then we do not parse anything here
                    // because the item is small enough to be parsed entirely in
                    // the close method
                    if (bod != null) {
                        // We shall write the first part of the body
                        // but first we must decode it (if the body is b64)
                        if (fo.isBodyBase64()) {
                            int bodyBytesSize = (bod.length() / 4) * 4;
                            byte bodyBytes[] = bod.getBytes();
                            byte tBuf[] = new byte[bodyBytesSize];
                            for(int i=0;i<bodyBytesSize;++i) {
                                tBuf[i] = bodyBytes[i];
                            }
                            byte tBuf2[] = Base64.decode(tBuf);
                            os.write(tBuf2);
                            b64Idx = 0;
                            // The remainder must be copied into the b64 buffer
                            for(int i=0;i<bod.length() % 4;++i) {
                                b64Buffer[b64Idx++] = bodyBytes[bodyBytesSize + i];
                            }
                        } else {
                            os.write(bod.getBytes());
                        }
                        body = true;
                        prologue = false;
                    }
                } catch (Exception e) {
                    invalidObject = true;
                    Log.error("Error parsing file object prologue: "+e.toString());
                    throw new IOException("Error parsing file object epilogue: " + e.toString());
                }
            }
        } else if (body) {
            if (b == '<') {
                // If we finished the body we may have few bytes left behind
                if (b64Idx > 0) {
                    byte tBuf[] = new byte[b64Idx];
                    for(int i=0;i<b64Idx;++i) {
                        tBuf[i] = b64Buffer[i];
                    }
                    tBuf = Base64.decode(tBuf);
                    os.write(tBuf);
                    b64Idx = 0;
                }
                body = false;
                epilogue = true;
                buffer = new StringBuffer();
                buffer.append((char)b);
            } else {
                // We must make decodable chunks (size must be multiple of 4)
                // if the body is base64
                if (fo.isBodyBase64()) {
                    b64Buffer[b64Idx++] = (byte)b;
                    if (b64Idx == B64BUFFER_SIZE) {
                        byte tBuf[] = Base64.decode(b64Buffer);
                        os.write(tBuf);
                        b64Idx = 0;
                    }
                } else {
                    os.write(b);
                }
            }
        } else {
            buffer.append((char)b);
        }
    }

    /**
     * Close the output stream. When the stream is closed, the method makes sure
     * that no pending bytes are present. If there are, then the body and the
     * FileObject may get updated. After the stream has been closed, it is safe
     * to read the FileObject.
     *
     * @throws IOException if the operation cannot be performed. Either because
     * the underlying output stream has a failure or because the the meta
     * information cannot be parsed properly.
     */
    public void close() throws IOException {
        if (epilogue) {
            os.close();
            if (!invalidObject) {
                try {
                    fo.parseEpilogue(buffer.toString());
                } catch (Exception e) {
                    Log.error("Error parsing file object epilogue: " + e.toString());
                    throw new IOException("Error parsing file object epilogue: " + e.toString());
                }
            }
        } else if (prologue) {
            // It is possible the entire file object fits in 1024 bytes,
            // so we can try to parse everything
            try {
                if (!invalidObject) {
                    String obj = buffer.toString();
                    ByteArrayInputStream bis = new ByteArrayInputStream(obj.getBytes());
                    String bod = fo.parse(bis);
                    if (bod != null) {
                        if (fo.isBodyBase64()) {
                            byte tBuf[] = Base64.decode(bod.getBytes());
                            os.write(tBuf);
                        } else {
                            os.write(bod.getBytes());
                        }
                        os.close();
                    } else {
                        // The file has an empty content
                        Log.debug("Received an emtpy file");
                    }
                } else {
                    os.close();
                }
            } catch (Exception e) {
                Log.error("Error while parsing file object: " + e.toString());
                throw new IOException("Error parsing file object epilogue: " + e.toString());
            }
        } else {
            os.close();
            // The stream is interrupted in the middle of the
            // body or the prologue. We shall throw an exception to signal that this
            // item is invalid
            throw new IOException("Incomplete file object item");
        } 
    }

    /**
     * Flushes the underlying stream.
     */
    public void flush() throws IOException {
        os.flush();
    }

    /**
     * Get the file object descrption (meta information). It is safe to invoke
     * this method only after the stream was closed. If writing or closing
     * generated an exception, then the content of the returned object may be
     * inaccurate.
     */
    public FileObject getFileObject() {
        return fo;
    }
}

