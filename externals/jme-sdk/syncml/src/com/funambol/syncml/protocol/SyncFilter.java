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
 * The interface <i>SyncFilter</i> is used by the SyncManager to apply a
 * filter defined for a SyncSource to the outgoing request.
 * <p>
 * An implementation of SyncSource can contain an implementation of SyncFilter,
 * that must be able to provide a SyncML 1.2 filter when the method toString()
 * is called.
 */
public interface SyncFilter {
    
    //------------------------------------------------------------ Attributes
    
    //--------------------------------------------------------------- Methods

    /**
     * Format the Filter in SyncML syntax, according to the parameter
     * contained in the SyncFilter implementation.
     *
     * @return the SyncML filter to add to the Alert command.
     */
    public String toSyncML();

    /**
     * Format the Filter in SyncML syntax. This is done according to the
     * parameter contained in the SyncFilter implementation, but with a size
     * limit forced by the sync engine. This limit is usually maxMsgSize
     * or MaxObjSize.
     *
     * @return the SyncML filter to add to the Alert command.
     */
    public String toSyncML(int maxSize);

}

