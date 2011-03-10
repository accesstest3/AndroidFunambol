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

package com.funambol.client.test;

import java.util.Enumeration;
import java.util.Hashtable;

import com.funambol.syncml.spds.SyncManager;
import com.funambol.syncml.spds.SyncConfig;
import com.funambol.syncml.spds.DeviceConfig;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.util.Log;


/**
 * Represents a sync client used to monitor server side modifications
 */
public class CheckSyncClient extends SyncMonitor {

    private static final String TAG_LOG = "CheckSyncClient";

    public static final String SOURCE_NAME_CONTACTS = "Contacts";
    public static final String SOURCE_NAME_CALENDAR = "Calendar";
    public static final String SOURCE_NAME_PICTURES = "Pictures";

    private Hashtable syncSources = new Hashtable();

    protected String syncUrl;
    protected String userName;
    protected String password;

    public CheckSyncClient(String syncUrl, String userName, String password) {

        // Init sync config
        setSyncConfig(syncUrl, userName, password);

        // Init SyncSource
        syncSources.put(SOURCE_NAME_CONTACTS, new CheckSyncSource(
                SOURCE_NAME_CONTACTS, "text/x-vcard", "card"));
        syncSources.put(SOURCE_NAME_CALENDAR, new CheckSyncSource(
                SOURCE_NAME_CALENDAR, "text/x-vcalendar", "event"));
    }

    public class CheckDeviceConfig extends DeviceConfig {
        public CheckDeviceConfig() {
            man = "Funambol";
            devID = "fnb-check-sync-client";
            utc = true;
            loSupport = true;
            nocSupport = false;
            setMaxMsgSize(64*1024);
            maxObjSize=1024*1024;
        }
    }

    public void sync() throws ClientTestException {
        Log.trace(TAG_LOG, "sync all sources");
        Enumeration sources = syncSources.elements();
        while(sources.hasMoreElements()) {
            sync((CheckSyncSource)sources.nextElement());
        }
    }

    public void sync(String source) throws ClientTestException {
        sync(getSyncSource(source));
    }

    public void sync(CheckSyncSource source) throws ClientTestException {
        reapplySyncConfig();
        Log.trace(TAG_LOG, "sync check source: " + source + "," + sManager);
        if(isSyncing()) {
            Log.error(TAG_LOG, "Cannot sync " + source  + " SyncManager is busy");
            throw new ClientTestException("Cannot sync " + source  + " SyncManager is busy");
        }
        sManager.sync(source);
    }

    public void clear(CheckSyncSource source) throws ClientTestException {
        reapplySyncConfig();
        Log.trace(TAG_LOG, "sync check source: " + source + "," + sManager);
        if(isSyncing()) {
            Log.error(TAG_LOG, "Cannot sync " + source  + " SyncManager is busy");
            throw new ClientTestException("Cannot sync " + source  + " SyncManager is busy");
        }
        source.clear();
        // Now perform a refresh from client to server
        sManager.sync(source, SyncML.ALERT_CODE_REFRESH_FROM_CLIENT);
    }

    public CheckSyncSource getSyncSource(String source) throws ClientTestException {
        CheckSyncSource ss = (CheckSyncSource)syncSources.get(source);
        if(ss != null) {
            return ss;
        } else {
            Log.error(TAG_LOG, "Cannot sync unknown source: " + source);
            throw new ClientTestException("Unknown source: " + source);
        }
    }

    public void setSyncConfig(String syncUrl, String userName, String password) {

        this.syncUrl  = syncUrl;
        this.userName = userName;
        this.password = password;
        
        reapplySyncConfig();
    }

    public int getItemsCount(String source) throws ClientTestException {
        return getSyncSource(source).getAllItemsCount();
    }

    protected void reapplySyncConfig() {

        // Init SyncConfig
        SyncConfig sc = new SyncConfig();
        sc.syncUrl  = this.syncUrl;
        sc.userName = this.userName;
        sc.password = this.password;
        sc.deviceConfig = new CheckDeviceConfig();

        // Init SyncManager
        sManager = new SyncManager(sc);
        sManager.setTransportAgent(BasicScriptRunner.createTestTransportAgent(sc));
    }
}
