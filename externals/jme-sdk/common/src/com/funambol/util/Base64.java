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

package com.funambol.util;

import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class Base64 {
    private static final byte[] encodingTable =
        { (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F',
                (byte)'G', (byte)'H', (byte)'I', (byte)'J', (byte)'K',
                (byte)'L', (byte)'M', (byte)'N', (byte)'O', (byte)'P',
                (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U',
                (byte)'V', (byte)'W', (byte)'X', (byte)'Y', (byte)'Z',
                (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e',
                (byte)'f', (byte)'g', (byte)'h', (byte)'i', (byte)'j',
                (byte)'k', (byte)'l', (byte)'m', (byte)'n', (byte)'o',
                (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t',
                (byte)'u', (byte)'v', (byte)'w', (byte)'x', (byte)'y',
                (byte)'z', (byte)'0', (byte)'1', (byte)'2', (byte)'3',
                (byte)'4', (byte)'5', (byte)'6', (byte)'7', (byte)'8',
                (byte)'9', (byte)'+', (byte)'/' };

    /**
     * encode the input stream and write the base64 encoded output into the output stream
     * @throws IOException if the input/output streams cannot be read/written
     * 
     */
    public static void encode(InputStream is, OutputStream os, int breakLen,
                              String breakStr) throws IOException {

        // The BUF SIZE must be a multiple of 3 to avoid b64 intermeditate padding
        int BUF_SIZE = 16380;
        byte buf[] = new byte[BUF_SIZE];
        int size = is.read(buf);
        int col = 0;
        while (size > 0) {
            // Encode this chunk
            byte encoded[];
            if (size != BUF_SIZE) {
                byte tmp[] = new byte[size];
                System.arraycopy(buf, 0, tmp, 0, size);
                encoded = encode(tmp);
                Log.debug("Done");
            } else {
                encoded = encode(buf);
                Log.debug("Done");
            }
            // Now print it to the OS, breaking if required
            for(int i=0;i<encoded.length;++i) {
                os.write((char)encoded[i]);
                col++;
                if (col == breakLen - breakStr.length()) {
                    os.write(breakStr.getBytes());
                    col = 0;
                }
            }
            size = is.read(buf);
        }
        Log.debug("Encoding done");
    }


    /**
     * encode the input data producong a base 64 encoded byte array.
     * 
     * @return a byte array containing the base 64 encoded data.
     */
    public static byte[] encode(byte[] data) {
        byte[] bytes;

        int modulus = data.length % 3;
        if (modulus == 0) {
            bytes = new byte[4 * data.length / 3];
        } else {
            bytes = new byte[4 * ((data.length / 3) + 1)];
        }

        int dataLength = (data.length - modulus);
        int a1, a2, a3;
        for (int i = 0, j = 0; i < dataLength; i += 3, j += 4) {
            a1 = data[i] & 0xff;
            a2 = data[i + 1] & 0xff;
            a3 = data[i + 2] & 0xff;

            bytes[j] = encodingTable[(a1 >>> 2) & 0x3f];
            bytes[j + 1] = encodingTable[((a1 << 4) | (a2 >>> 4)) & 0x3f];
            bytes[j + 2] = encodingTable[((a2 << 2) | (a3 >>> 6)) & 0x3f];
            bytes[j + 3] = encodingTable[a3 & 0x3f];
        }

        /*
         * process the tail end.
         */
        int b1, b2, b3;
        int d1, d2;

        switch (modulus) {
            case 0: /* nothing left to do */
                break;
            case 1:
                d1 = data[data.length - 1] & 0xff;
                b1 = (d1 >>> 2) & 0x3f;
                b2 = (d1 << 4) & 0x3f;

                bytes[bytes.length - 4] = encodingTable[b1];
                bytes[bytes.length - 3] = encodingTable[b2];
                bytes[bytes.length - 2] = (byte)'=';
                bytes[bytes.length - 1] = (byte)'=';
                break;
            case 2:
                d1 = data[data.length - 2] & 0xff;
                d2 = data[data.length - 1] & 0xff;

                b1 = (d1 >>> 2) & 0x3f;
                b2 = ((d1 << 4) | (d2 >>> 4)) & 0x3f;
                b3 = (d2 << 2) & 0x3f;

                bytes[bytes.length - 4] = encodingTable[b1];
                bytes[bytes.length - 3] = encodingTable[b2];
                bytes[bytes.length - 2] = encodingTable[b3];
                bytes[bytes.length - 1] = (byte)'=';
                break;
        }

        return bytes;
    }

    /*
     * set up the decoding table.
     */
    private static final byte[] decodingTable;

    static {
        decodingTable = new byte[128];

        for (int i = 0; i < 128; i++) {
            decodingTable[i] = (byte)-1;
        }

        for (int i = 'A'; i <= 'Z'; i++) {
            decodingTable[i] = (byte)(i - 'A');
        }

        for (int i = 'a'; i <= 'z'; i++) {
            decodingTable[i] = (byte)(i - 'a' + 26);
        }

        for (int i = '0'; i <= '9'; i++) {
            decodingTable[i] = (byte)(i - '0' + 52);
        }

        decodingTable['+'] = 62;
        decodingTable['/'] = 63;
    }


    /**
     * decode the base 64 encoded input data.
     * 
     * @return a byte array representing the decoded data.
     */
    public static byte[] decode(byte[] data) {

        byte[] bytes;
        byte b1, b2, b3, b4;

        data = discardNonBase64Bytes(data);

        if (data[data.length - 2] == '=') {
            bytes = new byte[(((data.length / 4) - 1) * 3) + 1];
        } else if (data[data.length - 1] == '=') {
            bytes = new byte[(((data.length / 4) - 1) * 3) + 2];
        } else {
            bytes = new byte[((data.length / 4) * 3)];
        }

        for (int i = 0, j = 0; i < data.length - 4; i += 4, j += 3) {
            b1 = decodingTable[data[i]];
            b2 = decodingTable[data[i + 1]];
            b3 = decodingTable[data[i + 2]];
            b4 = decodingTable[data[i + 3]];

            bytes[j] = (byte)((b1 << 2) | (b2 >> 4));
            bytes[j + 1] = (byte)((b2 << 4) | (b3 >> 2));
            bytes[j + 2] = (byte)((b3 << 6) | b4);
        }

        if (data[data.length - 2] == '=') {
            b1 = decodingTable[data[data.length - 4]];
            b2 = decodingTable[data[data.length - 3]];

            bytes[bytes.length - 1] = (byte)((b1 << 2) | (b2 >> 4));
        } else if (data[data.length - 1] == '=') {
            b1 = decodingTable[data[data.length - 4]];
            b2 = decodingTable[data[data.length - 3]];
            b3 = decodingTable[data[data.length - 2]];

            bytes[bytes.length - 2] = (byte)((b1 << 2) | (b2 >> 4));
            bytes[bytes.length - 1] = (byte)((b2 << 4) | (b3 >> 2));
        } else {
            b1 = decodingTable[data[data.length - 4]];
            b2 = decodingTable[data[data.length - 3]];
            b3 = decodingTable[data[data.length - 2]];
            b4 = decodingTable[data[data.length - 1]];

            bytes[bytes.length - 3] = (byte)((b1 << 2) | (b2 >> 4));
            bytes[bytes.length - 2] = (byte)((b2 << 4) | (b3 >> 2));
            bytes[bytes.length - 1] = (byte)((b3 << 6) | b4);
        }

        return bytes;
    }


    /**
     * decode the base 64 encoded String data.
     * 
     * TODO: Use the byte version to avoid duplication?
     * 
     * @return a byte array representing the decoded data.
     */
    public static byte[] decode(String data) {
        byte[] bytes;
        byte b1, b2, b3, b4;

        data = discardNonBase64Chars(data);

        if (data.charAt(data.length() - 2) == '=') {
            bytes = new byte[(((data.length() / 4) - 1) * 3) + 1];
        } else if (data.charAt(data.length() - 1) == '=') {
            bytes = new byte[(((data.length() / 4) - 1) * 3) + 2];
        } else {
            bytes = new byte[((data.length() / 4) * 3)];
        }

        for (int i = 0, j = 0; i < data.length() - 4; i += 4, j += 3) {
            b1 = decodingTable[data.charAt(i)];
            b2 = decodingTable[data.charAt(i + 1)];
            b3 = decodingTable[data.charAt(i + 2)];
            b4 = decodingTable[data.charAt(i + 3)];

            bytes[j] = (byte)((b1 << 2) | (b2 >> 4));
            bytes[j + 1] = (byte)((b2 << 4) | (b3 >> 2));
            bytes[j + 2] = (byte)((b3 << 6) | b4);
        }

        if (data.charAt(data.length() - 2) == '=') {
            b1 = decodingTable[data.charAt(data.length() - 4)];
            b2 = decodingTable[data.charAt(data.length() - 3)];

            bytes[bytes.length - 1] = (byte)((b1 << 2) | (b2 >> 4));
        } else if (data.charAt(data.length() - 1) == '=') {
            b1 = decodingTable[data.charAt(data.length() - 4)];
            b2 = decodingTable[data.charAt(data.length() - 3)];
            b3 = decodingTable[data.charAt(data.length() - 2)];

            bytes[bytes.length - 2] = (byte)((b1 << 2) | (b2 >> 4));
            bytes[bytes.length - 1] = (byte)((b2 << 4) | (b3 >> 2));
        } else {
            b1 = decodingTable[data.charAt(data.length() - 4)];
            b2 = decodingTable[data.charAt(data.length() - 3)];
            b3 = decodingTable[data.charAt(data.length() - 2)];
            b4 = decodingTable[data.charAt(data.length() - 1)];

            bytes[bytes.length - 3] = (byte)((b1 << 2) | (b2 >> 4));
            bytes[bytes.length - 2] = (byte)((b2 << 4) | (b3 >> 2));
            bytes[bytes.length - 1] = (byte)((b3 << 6) | b4);
        }

        return bytes;
    }
 
    /**
     * Decode the string and convert back the decoded value into a string
     * using the specified charset. 
     * Use default encoding if charset is null or invalid.
     */
    public static String decode(String data, String charset) {
        if (charset == null){
            // use default
            return new String(Base64.decode(data));            
        }

        try {
            return new String(Base64.decode(data), charset);

        } catch (UnsupportedEncodingException ex) {
            Log.error("Charset: "+charset+" not supported. Using default.");
            return new String(Base64.decode(data));
        }
    }

    /**
     * Decode the string and convert back the decoded value into a string
     * using the specified charset. 
     * Use default encoding if charset is null or invalid.
     */
    public static String decode(byte[] data, String charset) {
        if (charset == null){
            // use default
            return new String(Base64.decode(data));            
        }

        try {
            return new String(Base64.decode(data), charset);

        } catch (UnsupportedEncodingException ex) {
            Log.error("Charset: "+charset+" not supported. Using default.");
            return new String(Base64.decode(data));
        }
    }

    /**
     * Compute the base64 encoded size for a stream whose size is specified in
     * the incoming parameter
     *
     * @param size the original size (>=0)
     * @return the size of the encoded data
     */
    public static int computeEncodedSize(int size) {
        int encodedSize;
        int modulus = (int)(size % 3);
        if (modulus == 0) {
            encodedSize = (int)(4 * size / 3);
        } else {
            encodedSize = (int)(4 * ((size / 3) + 1));
        }
        return encodedSize;
    }


    // --------------------------------------------------------- Private Methods

    /**
     * Discards any characters outside of the base64 alphabet (see page 25 of
     * RFC 2045) "Any characters outside of the base64 alphabet are to be
     * ignored in base64 encoded data."
     * 
     * @param data
     *            the base64 encoded data
     * @return the data, less non-base64 characters.
     */
    private static byte[] discardNonBase64Bytes(byte[] data) {
        byte temp[] = new byte[data.length];
        int bytesCopied = 0;

        for (int i = 0; i < data.length; i++) {
            if (isValidBase64Byte(data[i])) {
                temp[bytesCopied++] = data[i];
            }
        }

        byte newData[] = new byte[bytesCopied];

        System.arraycopy(temp, 0, newData, 0, bytesCopied);

        return newData;
    }


    /**
     * Discards any characters outside of the base64 alphabet (see page 25 of
     * RFC 2045) "Any characters outside of the base64 alphabet are to be
     * ignored in base64 encoded data."
     * 
     * @param data
     *            the base64 encoded data
     * @return the data, less non-base64 characters.
     */
    private static String discardNonBase64Chars(String data) {

        StringBuffer sb = new StringBuffer();

        int length = data.length();

        for (int i = 0; i < length; i++) {
            if (isValidBase64Byte((byte)(data.charAt(i)))) {
                sb.append(data.charAt(i));
            }
        }

        return sb.toString();
    }


    /**
     * Checks is the given byte is in base64 alphabet
     * 
     * @param b
     *            the byte to check
     * @return boolean true if the byte is in base64 alphabet
     */
    private static boolean isValidBase64Byte(byte b) {
        if (b == '=') {
            return true;
        } else if (b < 0 || b >= 128) {
            return false;
        } else if (decodingTable[b] == -1) {
            return false;
        }
        return true;
    }
}
