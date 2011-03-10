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

package com.funambol.android;

import java.util.Enumeration;
import java.util.Vector;

import android.accounts.Account;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.funambol.android.controller.AndroidController;
import com.funambol.android.controller.AndroidHomeScreenController;

import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.source.AppSyncSourceConfig;
import com.funambol.client.configuration.Configuration;
import com.funambol.client.controller.SynchronizationController;
import com.funambol.util.Log;

/**
 * BroadcastReceiver implementation for the Wifi status.
 */
public class ConnectivityIntentReceiver extends BroadcastReceiver {
    private static final String TAG = "ConnectivityIntentReceiver";

    private Context context;
    private Object lock = new Object();
    
    /**
     * Defines what to do when the BroadcastReceiver is triggered.
     * @param context the application Context
     * @param intent the Intent to be launched to start the service - Not used
     * use a custom defined intent with a predefined service action
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        final String action = intent.getAction();
        Log.info(TAG, "onReceiveConnectionChange " + action);

        synchronized(lock) {
            NetworkInfo netInfo = (NetworkInfo)intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (netInfo != null) {
                if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (netInfo.isConnected()) {
                        Log.info(TAG, "WiFi connected");

                        // Grab the list of sources and trigger a sync for the ones
                        // that have a pending sync
                        AppInitializer appInitializer = AppInitializer.getInstance(context);
                        appInitializer.init();
                        Configuration configuration = appInitializer.getConfiguration();
                        AndroidController controller = appInitializer.getController();
                        AppSyncSourceManager appSourceManager = appInitializer.getAppSyncSourceManager();
                        Enumeration appSources = appSourceManager.getEnabledAndWorkingSources();
                        Log.info(TAG, "WiFi connected");

                        Vector sources = null;
                        AndroidHomeScreenController hsc;
                        hsc = (AndroidHomeScreenController)controller.getHomeScreenController();
                        if (hsc != null) {
                            sources = new Vector();
                        }
                        while(appSources.hasMoreElements()) {
                            AndroidAppSyncSource appSource = (AndroidAppSyncSource)appSources.nextElement();
                            // If this source authority is already being
                            // synchronized (or pending) then there is no need
                            // to check. We need this extra check because the
                            // platform may generate several Intents when WiFi
                            // gets connected
                            String authority = appSource.getAuthority();
                            Account account = AndroidController.getNativeAccount();

                            if (authority != null &&
                                !ContentResolver.isSyncActive(account, authority) &&
                                !ContentResolver.isSyncPending(account, authority))
                            {
                                AppSyncSourceConfig  appSourceConfig = appSource.getConfig();
                                Log.trace(TAG, "Checking if source " + appSource.getName() + " must be synced");
                                if (appSourceConfig.getPendingSyncType() != null &&
                                    appSource.getBandwidthSaverUse() &&
                                    configuration.getBandwidthSaverActivated())
                                {
                                    Log.trace(TAG, "Yes, a sync is pending");
                                    if (sources != null) {
                                        sources.addElement(appSource);
                                    } else {
                                        Log.debug(TAG, "HomeScreenController not available, sync via the resolver");
                                        Bundle b = new Bundle();
                                        b.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
                                        b.putString(AndroidHomeScreenController.SYNC_TYPE, appSourceConfig.getPendingSyncType());
                                        if(appSource.getAuthorityType() != null) {
                                            b.putString(AndroidHomeScreenController.AUTHORITY_TYPE, appSource.getAuthorityType());
                                        }
                                        Log.info(TAG, "Requesting sync for authority " + authority);
                                        ContentResolver.requestSync(account, authority, b);
                                    }
                                }
                            }
                        }
                        if (sources != null) {
                            Log.debug(TAG, "HomeScreenController available, use it to sync");
                            if (sources.size() > 1) {
                                hsc.syncMultipleSources(SynchronizationController.PUSH, sources);
                            } else if (sources.size() == 1) {
                                AndroidAppSyncSource s = (AndroidAppSyncSource)sources.elementAt(0);
                                hsc.syncSingleSource(SynchronizationController.PUSH, s);
                            }
                        }
                    } else {
                        Log.info(TAG, "WiFi disconnected");
                    }
                }
            }
        }
    }
}

