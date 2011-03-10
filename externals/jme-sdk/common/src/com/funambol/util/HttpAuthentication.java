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
package com.funambol.util;

import java.io.IOException;

import com.funambol.platform.HttpConnectionAdapter;

/**
 * This interface represents a generic mechanism used by the HttpTransportAgent
 * to get authenticated. Various authentication mechanism can be implemented,
 * and each one must implement this interface which is then used by the
 * transport agent.
 */
public interface HttpAuthentication {

    /**
     * Handle the authentication by adding authentication headers to the given HttpConnection.
     *
     * @param c The HttpConnection to add authentication headers to.
     *
     * @return Whether or not authentication headers were added to the connection.
     */
    public boolean handleAuthentication(HttpConnectionAdapter c) throws IOException;

    /**
     * Process any errors that the given HttpConnection has that are related to authentication.
     *
     * @param c The HttpConnection to check for errors.
     *
     * @return Whether any errors were processed.
     */
    public boolean processHttpError(HttpConnectionAdapter c) throws IOException;


    /**
     * Determines whether or not to retry sending a message, this time with authentication information.
     *
     * @return Whether or not to retry with authentication.
     */
    public boolean getRetryWithAuth();

    /**
     * Set the username to the given string.
     *
     * @param user The new username.
     */
    public void setUsername(String user);

    /**
     * Set the password to the given string.
     *
     * @param pass The new password.
     */
    public void setPassword(String pass);

    /**
     * Set the authentication URI to the given string.
     *
     * @param uri The new uri.
     */
    public void setUri(String uri);
}

