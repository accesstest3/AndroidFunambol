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
package com.funambol.syncml.protocol;


/**
 * This class corresponds to &lt;SyncType&gt; tag in SyncML devinfo DTD
 */
public class SyncType {
    // --------------------------------------------------------------- Constants

    public static final SyncType TWO_WAY             = new SyncType(1);
    public static final SyncType SLOW                = new SyncType(2);
    public static final SyncType ONE_WAY_FROM_CLIENT = new SyncType(3);
    public static final SyncType REFRESH_FROM_CLIENT = new SyncType(4);
    public static final SyncType ONE_WAY_FROM_SERVER = new SyncType(5);
    public static final SyncType REFRESH_FROM_SERVER = new SyncType(6);
    public static final SyncType SERVER_ALERTED      = new SyncType(7);

    public static final SyncType[] ALL_SYNC_TYPES = new SyncType[] {
        TWO_WAY, SLOW, ONE_WAY_FROM_CLIENT, REFRESH_FROM_CLIENT,
        ONE_WAY_FROM_SERVER, REFRESH_FROM_SERVER, SERVER_ALERTED
    };

    // ------------------------------------------------------------ Private data
    private int syncType;

    // ------------------------------------------------------------ Constructors
    /**
     * In order to expose the server configuration like WS this constructor
     * must be public
     */
    public SyncType() {}

    /**
     * Creates a new SyncType object with syncType value
     *
     * @param syncType the value of SyncType - NOT NULL
     *
     */
    public SyncType(final int syncType) {
        setType(syncType);
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Gets the synchronization type
     *
     * @return syncType the synchronization type
     */
    public int getType() {
        return syncType;
    }

    /**
     * Sets the synchronization type
     *
     * @param syncType the synchronization type
     */
    public void setType(int syncType) {
        this.syncType = syncType;
    }

    /**
     * Gets the instance of synchronization type
     *
     * @return syncType the synchronization type
     */
    public static final SyncType getInstance(final int syncType) {
        if ((syncType < 0) || (syncType >= ALL_SYNC_TYPES.length)) {
            throw new IllegalArgumentException("unknown syncType: " + syncType);
        }
        return ALL_SYNC_TYPES[syncType-1];
    }
}
