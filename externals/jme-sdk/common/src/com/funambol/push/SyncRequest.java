/**
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

package com.funambol.push;

import java.util.Vector;

/**
 * This class represents a request for synchronization that clients can ask to
 * the SyncScheduler.
 * Usually a client can generate a SyncRequest and pass it to the SyncScheduler
 * that will then enqueu it and trigger a sync when it is more appropriate.
 * A request contains two different information:
 *
 * 1) an opaque information which is passed to the listener once the request
 *    expires. This is completely transparent to the APIs and just passed back
 *    to the client.
 * 2) a time at which the sync shall be started. This is expressed as an
 *    interval from now. 
 *
 * The class is abstract an clients must extend it with the implementation of
 * the method "merge".
 */
public abstract class SyncRequest {
   
    protected Vector contents;
    protected long interval;

    public SyncRequest(Object syncObject, long interval){
        contents = new Vector();
        if(syncObject != null) {
            contents.addElement(syncObject);
        }
        this.interval = interval;
    }
    
    /**
     * @return the time interval (in millisec) at which the sync shall be
     *         started (the interval is relative to "now", the current time
     */
    public long getInterval(){
        return interval;
    }

    /**
     * @return the object represent the thinks to sync
     *
     */
    public Object[] getRequestContent() {
        Object[] contArray = new Object[contents.size()];
        contents.copyInto(contArray);
        return contArray;
    }

    public void addRequestContent(Object requestContent) {
        contents.addElement(requestContent);
    }

    /**
     * This method checks if "this" request can be merged with another one.
     * It is used by the SyncScheduler to verify the possibility to delete the
     * old request and merge the old and the new one into a new request. Such a
     * new request will be rescheduled at the interval of the new request
     * For example if the queue contains a request to sync contacts, and
     * another request for contacts comes in, then the fisrt request is deleted.
     *
     * @param syncReq is the request already in queue
     * @return a merged request if the two requests can be merged, null
     *         otherwise
     */
    public abstract SyncRequest merge(SyncRequest syncReq);
}

