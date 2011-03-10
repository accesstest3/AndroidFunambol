/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2010 Funambol, Inc.
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

import com.funambol.platform.HttpConnectionAdapter;
import com.funambol.platform.SocketAdapter;

/**
 * Controls HTTP and HTTPS connections requested by the API implementations. 
 */
public class ConnectionManager {

    private static final String TAG_LOG = "ConnectionManager";

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
            Log.debug(TAG_LOG, "Creating new connection manager");
            instance = new ConnectionManager();
            return instance;
        } else {
            Log.trace(TAG_LOG, "Returning the existing connection manager instance");
            return instance;
        }
    }

    public ConnectionListener getConnectionListener() {
        return cl;
    }

    /**
     * Open an http connection to the given URL
     * @param url is the url (in the form of "http://..." or "https://...")
     * @param extra is some extra information that can be specified to specific
     * implementations
     * @throws IOException if the connection cannot be established
     */
    public HttpConnectionAdapter openHttpConnection(String url, Object extra) throws IOException {
        HttpConnectionAdapter res = new HttpConnectionAdapter();
        res.open(url);
        return res;
    }

    public SocketAdapter openSocketConnection(String addr, int port, int mode, boolean timeout) throws IOException {
        SocketAdapter res = new SocketAdapter(addr, port, mode, timeout);
        return res;
    }
}

