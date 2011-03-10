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

package com.funambol.platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import com.funambol.util.ConnectionManager;

/**
 * This class is a proxy to javax.microedition.io.SocketConnection to
 * provide a common access to a socket resource on all mobile platforms.
 *
 * A portable code must use this class only to use sockets, and must take care
 * of closing the SocketAdapter when not used anymore.
 * <pre>
 * Example:
 * 
 *   void socketAccessExample(String host, int port) {
 *      SocketAdapter sa = new SocketAdapter(host, port); // opens the SocketConnection
 *      InputStream is = sa.openInputStream();  // opens the InputStream
 *      while( (char c = is.read()) != -1) {    // read till the end of the file
 *         System.out.print(c);
 *      }
 *      is.close();                             // closes the InputStream
 *      sa.close();                             // closes the SocketConnection
 * </pre>
 */
public class SocketAdapter {

    public static final byte DELAY     = SocketConnection.DELAY;
    public static final byte KEEPALIVE = SocketConnection.KEEPALIVE;
    public static final byte LINGER    = SocketConnection.LINGER;
    public static final byte RCVBUF    = SocketConnection.RCVBUF;
    public static final byte SNDBUF    = SocketConnection.SNDBUF;
    public static final int READ_WRITE = Connector.READ_WRITE;

    /** The underlying SocketConnection */
    private SocketConnection socket;
    
    //------------------------------------------------------------- Constructors

    /**
     * Creates a FileAdapter instance, opening the underlying SocketConnection.
     * @param server is the remote server (without protocol specification)
     * @param port is the remote port
     *
     * @throws IOException if the connection cannot be established
     */
    public SocketAdapter(String host, int port, int mode, boolean timeout) throws IOException {
        StringBuffer uri = new StringBuffer();
        uri.append("socket://").append(host).append(":").append(port);
        // We cannot open the socket directly, because we need the
        // ConnectionManager to add all the required parameters
        socket = (SocketConnection) ConnectionManager.getInstance().open(uri.toString(), mode, timeout);
    }

    /**
     * Open and return an input stream for this Socket.
     */
    public InputStream openInputStream() throws IOException {
        if (socket == null) {
            throw new IOException("Socket not available");
        }
        return socket.openInputStream();
    }

    /**
     * Open and return an output stream for this Socket.
     */
    public OutputStream openOutputStream() throws IOException {
        if (socket == null) {
            throw new IOException("Socket not available");
        }
        return socket.openOutputStream();
    }

    /**
     * Close this Socket
     */
    public void close() throws IOException {
        if (socket == null) {
            throw new IOException("Socket not available");
        }
        socket.close();
        socket = null;
    }

    public void setSocketOption(byte option, int value) throws IOException {
        if (socket == null) {
            throw new IOException("Socket not available");
        }
        socket.setSocketOption(option, value);
    }
}

