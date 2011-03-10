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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Binder;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;

import com.funambol.android.AppInitializer;
import com.funambol.android.AndroidConfiguration;
import com.funambol.android.AndroidAppSyncSourceManager;
import com.funambol.android.controller.AndroidHomeScreenController;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.controller.Controller;
import com.funambol.util.Log;

/**
 * This class is responsible for handling automatic synchronizations. In the
 * client there are different types of automatic syncs:
 *
 * 1) scheduled syncs (periodic)
 * 2) pending syncs waiting for some triggering event (for example syncs pending
 *    on the WiFi availability)
 * 3) syncs pushed from the server
 */
public class AutoSyncService extends Service {

    private final String TAG = "AutoSyncService";

    private Context context;

    private PendingIntent syncIntent;

    private AndroidConfiguration configuration;
    private AndroidAppSyncSourceManager appSyncSourceManager;
    private AndroidHomeScreenController homeScreenController;

    private AlarmManager am;

    public AutoSyncService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.debug(TAG, "Service Created");

        AppInitializer initializer = AppInitializer.getInstance(this);
        initializer.init();

        configuration = initializer.getConfiguration();
        appSyncSourceManager = initializer.getAppSyncSourceManager();
        Controller cont = initializer.getController();
        homeScreenController = (AndroidHomeScreenController)cont.getHomeScreenController();

        context = getApplicationContext();
    }

    @Override
    public IBinder onBind(Intent intent) {
        startForeground(Notification.FLAG_FOREGROUND_SERVICE, new Notification(0, null, System.currentTimeMillis()));
        return new AutoSyncBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.debug(TAG, "Service Started");

        startForeground(Notification.FLAG_FOREGROUND_SERVICE, new Notification(0, null, System.currentTimeMillis()));

        // Program scheduled sync if necessary
        if(!configuration.getCredentialsCheckPending()) {
            programScheduledSync();
        }

        return START_STICKY;

    }

    private void programScheduledSync() {
        // Check in the configuration if a form of auto synchronization is set
        if (configuration.getSyncMode() == Configuration.SYNC_MODE_SCHEDULED) {
            int interval = configuration.getPollingInterval();

            if (interval > 0) {
                // Scheduled sync
                Log.trace(TAG, "Programming scheduled sync at " + interval);
                setInterval(interval);
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.debug(TAG, "Service Stopped");
    }

    /**
     * LocalBinder used by activities that wish to be notified of the sync events.
     */
    public class AutoSyncBinder extends Binder {

        public void updateSyncMode() {
            programScheduledSync();
        }

        public void stop() {
            AutoSyncService.this.stop();
        }
    }

    private void setInterval(int interval) {
        // Program a new pending request.
        int repeatTime = interval * 1000 * 60;

        Intent i = new Intent("com.funambol.android.AUTO_SYNC");

        //IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction(i.getAction());

        //SynchronizationTask st = new SynchronizationTask();
        //registerReceiver(st, intentFilter);

        syncIntent = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + repeatTime, repeatTime, syncIntent);
    }

    private void stop() {
        Log.trace(TAG, "stop");
        if (am != null) {
            am.cancel(syncIntent);
        }
    }
}
