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

package com.funambol.android.services;

import android.content.Context;
import java.util.Enumeration;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Binder;
import android.os.Handler;
import android.accounts.Account;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.net.wifi.WifiManager;

import com.funambol.android.R;
import com.funambol.android.AppInitializer;
import com.funambol.android.AndroidConfiguration;
import com.funambol.android.AndroidAppSyncSourceManager;
import com.funambol.android.AndroidAppSyncSource;
import com.funambol.android.controller.AndroidController;
import com.funambol.android.controller.AndroidHomeScreenController;

import com.funambol.platform.NetworkStatus;
import com.funambol.client.configuration.Configuration;
import com.funambol.client.controller.SynchronizationController;
import com.funambol.client.controller.Controller;
import com.funambol.util.Log;

public class SynchronizationTask extends BroadcastReceiver {

    private static final String TAG = "SynchronizationTask";

    private AndroidAppSyncSourceManager appSyncSourceManager;
    private AndroidHomeScreenController homeScreenController;
    private Context context;

    public SynchronizationTask()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.info(TAG, "Intent Received - Checking if sync can be run");

        // We may need to re-initialize the application here in case it got
        // destroyed
        this.context = context;
        AppInitializer initializer = AppInitializer.getInstance(context);
        initializer.init();

        appSyncSourceManager = initializer.getAppSyncSourceManager();
        Controller cont = initializer.getController();
        homeScreenController = (AndroidHomeScreenController)cont.getHomeScreenController();

        if (homeScreenController.isFirstSyncDialogDisplayed()) {
            Log.info(TAG, "Skipping scheduled sync because a sync all is already in progress");
            return;
        }

        // If the WiFi is down, we may need to bring it up
        NetworkStatus ns = new NetworkStatus(context);
        if (!ns.isConnected()) {
            Log.info(TAG, "There is no network coverage for scheduled sync, trying to bring up WiFi");
            //bringUpWiFi();
        }

        // We schedule a new sync only if there are no other syncs already
        // pending or in progress
        Enumeration sources = appSyncSourceManager.getWorkingSources();
        Account account = AndroidController.getNativeAccount(context);

        while(sources.hasMoreElements()) {
            AndroidAppSyncSource appSource = (AndroidAppSyncSource)sources.nextElement();
            String authority = appSource.getAuthority();
            if (authority != null) {
                if (ContentResolver.isSyncPending(account, authority) ||
                    ContentResolver.isSyncActive(account, authority))
                {
                    Log.info(TAG, "Skipping scheduled sync because a sync is already in progress or scheduled");
                    return;
                }
            }
        }

        // Perform a sync via the SyncAdapter for all the enabled and
        // working sources
        Log.info(TAG, "Running a scheduled sync");
        homeScreenController.syncAllSources(SynchronizationController.SCHEDULED);
    }

    private void bringUpWiFi() {

        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        Log.info(TAG, "Enabling WiFi");
        boolean enabled = wifiManager.setWifiEnabled(true);
        Log.info(TAG, "Enabled = " + enabled);

        try {
            Thread.sleep(5000);
        } catch (Exception e) {
        }
        Log.info(TAG, "Starting WiFi scan");
        boolean initiated = wifiManager.startScan();
        Log.info(TAG, "Scan initiated = " + initiated);

        try {
            Thread.sleep(10000);
        } catch (Exception e) {
        }

        boolean reconnected = wifiManager.reconnect();
        if (reconnected) {
            Log.info(TAG, "Reconnected to WiFi");
        } else {
            Log.info(TAG, "Failed to reconnect to WiFi");
        }
    }
}

