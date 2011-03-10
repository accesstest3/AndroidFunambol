/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2003 - 2008 Funambol, Inc.
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

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.Connection;

import com.funambol.platform.HttpConnectionAdapter;
import com.funambol.platform.SocketAdapter;
import com.funambol.platform.HttpConnectionAdapterWrapper;

/**
 * Controls all of the connections requested by the API implementations. 
 * It is strongly recommended to use this class instead of the direct call to 
 * the Connector.open(String url) method. This class is based on the singleton 
 * pattern: it has private constructor and just one instance to be referenced 
 * calling the method ConnectionManager.getInstance() by other classes. 
 */
public class ConnectionManager {

    /**
     * This class implements the singleton pattern only this instance can be 
     * used by other classes
     */
    private static ConnectionManager instance =  null;

    /**
     * The listsener associated to this ConnectionManager
     */
    private ConnectionListener cl = null;
    
    /**
     * Private constructor - Use getInstance() method
     */
    protected ConnectionManager() {
    }

    /**
     * Singleton implementation:
     * @return the current instance of this class or a new instance if it the 
     * current instance is null
     */
    public static ConnectionManager getInstance() {
        if (instance == null) {
            Log.debug("[ConnectionManager.getInstance]Creating new connection manager");
            instance = new ConnectionManager();
            instance.setConnectionListener(new BasicConnectionListener());
            return instance;
        } else {
            Log.debug("[ConnectionManager.getInstance]Returning the existing connection manager insatnce");
            return instance;
        }
    }

    /**
     * Open up a connection to the give url
     * @param url The URL for the connection
     * @return the connection url with the given parameters
     * @throws java.io.IOException
     */
    public synchronized Connection open(String url) throws IOException {
        return open(url, Connector.READ_WRITE, false);
    }

    /**
     * Open up a connection to the given url with the given access mode and 
     * @param url The URL for the connection
     * @param mode the access mode that can be READ, WRITE, READ_WRITE
     * @param b A flag to indicate that the called wants timeout exceptions
     * @return Connection related to the given parameters
     * @throws java.io.IOException
     */
    public synchronized Connection open(String url,int mode, boolean b) throws IOException {
        return Connector.open(url, mode, b);
    }
    
    /**
     * Accessor method to set the connection listener 
     * @param cl the connection listener to be set
     */
    public void setConnectionListener(ConnectionListener cl) {
        this.cl = cl;
    }


    /**
     * Accessor method to get the current connection listener 
     * @return ConnectionListener related to this ConnectionManager instance
     */
    public ConnectionListener getConnectionListener() {
        return cl;
    }

    /**
     * Open an http connection to the given URL
     * @param url is the url (in the form of "http://..." or "https://...")
     * @param extra is some extra information that can be specified to specific
     * implementations. In this implementation the supported values are:
     * * "wrapper" which indicates an HttpConnectionAdapterWrapper shall be used
     *
     * @throws IOException if the connection cannot be established
     */
    public HttpConnectionAdapter openHttpConnection(String url, Object extra) throws IOException {
        HttpConnectionAdapter res = null;
        if (extra instanceof String) {
            String e = (String)extra;
            if ("wrapper".equals(e)) {
                res = new HttpConnectionAdapterWrapper();
            }
        }
        if (res == null) {
            res = new HttpConnectionAdapter();
        }
        res.open(url);
        return res;
    }

    /**
     * Open a socket connection to the given URL
     * @param addr is the server address
     * @param port the port
     * @param mode can be READ_WRITE
     * @param timeout enable timeout on IO operations
     */
    public SocketAdapter openSocketConnection(String addr, int port, int mode, boolean timeout) throws IOException {
        SocketAdapter res = new SocketAdapter(addr, port, mode, timeout);
        return res;
    }
}

