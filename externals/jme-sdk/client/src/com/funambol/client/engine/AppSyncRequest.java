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

package com.funambol.client.engine;

import java.util.Vector;

import com.funambol.push.SyncRequest;
import com.funambol.client.source.AppSyncSource;
import com.funambol.util.Log;

/**
 * A SyncRequest for the Sync Client
 */
public class AppSyncRequest extends SyncRequest {

    public AppSyncRequest(AppSyncSource appSource, long interval) {
        super(appSource, interval);
    }

    public SyncRequest merge(SyncRequest r) {
        Log.trace("[AppSyncRequest.merge]");
        // We always merge requests, as we must minimize the number of syncs
        StringBuffer msg = new StringBuffer();
        AppSyncRequest merged = new AppSyncRequest(null, this.getInterval());
        // Add al the requests we had in the old one
        Object[] existingss = r.getRequestContent();
        Object[] newss = getRequestContent();
        for(int i=0; i<existingss.length; i++) {
            AppSyncSource os = (AppSyncSource)existingss[i];
            merged.addRequestContent(os);
            msg.append(" ").append(os.getName());
       }
       // Now add al the requests in the new request, which were not part of the
       // old one
       boolean present;
       for(int j=0; j<newss.length; j++) {
           present = false;
           AppSyncSource ns = (AppSyncSource)newss[j];
           for(int i=0;i<existingss.length;++i) {
               AppSyncSource os = (AppSyncSource)existingss[i];
                if (ns.getId() == os.getId()) {
                    present = true;
                    break;
                }
           }
           if(!present) {
               merged.addRequestContent(ns);
               msg.append(" ").append(ns.getName());
           }
       }
       // Order the sources according to the UI order
       Object content[] = merged.getRequestContent();
       boolean changed = false;
       for(int i=0;i<content.length;++i) {
           int min = i;
           AppSyncSource first = (AppSyncSource)content[i];

           for(int j=i+1;j<content.length;++j) {
               AppSyncSource second = (AppSyncSource)content[j];

               if (second.getUiSourceIndex() < first.getUiSourceIndex()) {
                   first = second;
                   min = j;
                   changed = true;
               }
           }
           // Now swap min and i (if different)
           if (i != min) {
               AppSyncSource tmp = (AppSyncSource)content[i];
               content[i] = first;
               content[min] = tmp;
           }
       }
       if (changed) {
           // Create a new request with ordered items
           merged = new AppSyncRequest(null, this.getInterval());
           for(int i=0;i<content.length;++i) {
               AppSyncSource appSource = (AppSyncSource)content[i];
               merged.addRequestContent(appSource);
           }
       }

       Log.info("[AppSyncRequest] Request merged with an existing one [" + msg.toString() + "]");
       return merged;
    }
}

