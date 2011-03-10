/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2003 - 2009 Funambol, Inc.
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

package com.funambol.android;

import android.accounts.Account;
import android.content.ContentResolver;
import com.funambol.android.controller.AndroidController;
import com.funambol.client.controller.AccountScreenController;
import com.funambol.client.test.SyncMonitor;
import com.funambol.client.controller.SynchronizationController;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.test.CheckSyncClient;
import com.funambol.syncml.spds.SyncReport;
import java.util.Enumeration;

/**
 * Redefines the SyncMonitor used in the Android client
 */
public class AndroidSyncMonitor extends SyncMonitor {

    private SynchronizationController sController;

    public AndroidSyncMonitor( SynchronizationController sController ) {
        this.sController = sController;
    }

    @Override
    public boolean isSyncing() {

        if(sController != null) {
            if(sController instanceof AccountScreenController) {
                return sController.isSynchronizing();
            } else {
                boolean result = false;
                AndroidController ac = (AndroidController)sController.getController();
                Enumeration sources = ac.getAppSyncSourceManager().getEnabledAndWorkingSources();
                while(sources.hasMoreElements()) {
                    AndroidAppSyncSource source = (AndroidAppSyncSource)sources.nextElement();
                    String authority = source.getAuthority();
                    if(authority != null) {
                        result |= isSyncing(authority);
                    }
                }
                return result;
            }
        } else if(sManager != null) {
            return sManager.isBusy();
        } else {
            return false;
        }
    }

    public boolean isSyncing(String authority) {
        Account account = AndroidController.getNativeAccount();
        boolean syncing = ContentResolver.isSyncActive(account, authority) &&
              !ContentResolver.isSyncPending(account, authority);
        return syncing;
    }

    @Override
    public SyncReport getSyncReport(String source) {

        int id = 0;
        if(CheckSyncClient.SOURCE_NAME_CONTACTS.equals(source)) {
            id = AppSyncSourceManager.CONTACTS_ID;
        } else if(CheckSyncClient.SOURCE_NAME_CALENDAR.equals(source)) {
            id = AppSyncSourceManager.EVENTS_ID;
        } else if(CheckSyncClient.SOURCE_NAME_PICTURES.equals(source)) {
            id = AppSyncSourceManager.PICTURES_ID;
        }

        if(sController != null) {
            AppSyncSourceManager manager = sController.getController().getAppSyncSourceManager();
            return manager.getSource(id).getUISyncSourceController()
                    .getLastSyncReport();
        } else if(sManager != null) {
            return sManager.getSyncReport();
        } else {
            return null;
        }
    }
}
