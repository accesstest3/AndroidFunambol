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

package com.funambol.push;

/**
 * A SyncRequest test class used to simulate a sync request
 */
public class TestSyncRequest extends SyncRequest {

    public TestSyncRequest(Object aRequestContent, long aInterval) {
        super(aRequestContent, aInterval);
    }

    public SyncRequest merge(SyncRequest q) {

        // We always merge requests, as we must minimize the number of syncs
        TestSyncRequest merged = new TestSyncRequest(null, this.getInterval());
        // Add al the requests we had in the old one
        Object[] existingss = q.getRequestContent();
        Object[] newss = getRequestContent();
        for(int i=0; i<existingss.length; i++) {
            merged.addRequestContent(existingss[i]);
        }
        // Now add al the requests in the new request, which were not part of the
        // old one
        boolean present;
        for(int j=0; j<newss.length; j++) {
            present = false;
            String ns = (String)newss[j];
            for(int i=0;i<existingss.length;++i) {
                String os = (String)existingss[i];
                if (ns.equals(os)) {
                    present = true;
                    break;
                }
            }
            if(!present) {
                merged.addRequestContent(ns);
            }
        }
        return merged;
    }
}
