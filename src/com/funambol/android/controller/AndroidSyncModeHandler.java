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

package com.funambol.android.controller;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.os.Binder;

import com.funambol.android.AndroidConfiguration;
import com.funambol.android.services.AutoSyncService;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.controller.Controller;
import com.funambol.client.controller.SyncModeHandler;
import com.funambol.util.Log;

/**
 * This is the class which handles the sync mode set by the user.
 */
public class AndroidSyncModeHandler extends SyncModeHandler {

    private static final String TAG = "AndroidSyncModeHandler";
    private Context context;
    private AutoSyncService.AutoSyncBinder binder = null;
    private boolean stopping = false;
    private AutoSyncServiceConnection serviceConnection;

    public AndroidSyncModeHandler(Context context, Configuration configuration) {
        super(configuration);
        this.context = context;
    }

    /**
     * Handles the given sync mode
     * @param mode
     * @param controller
     */
    public void setSyncMode(Controller controller) {

        int mode = configuration.getSyncMode();

        // TODO: does not support push yet
        if(mode == Configuration.SYNC_MODE_MANUAL) {
            stopAutoSyncService();
        } else if(mode == Configuration.SYNC_MODE_SCHEDULED) {
            startAutoSyncService(controller);
        }
    }

    /*
     * Unbind service to avoid any leak
     */
    public void unbindService() {
        if (serviceConnection != null) {
            context.unbindService(serviceConnection);
        }
    }

    /**
     * Stops the scheduling service
     */
    protected void stopAutoSyncService() {
        if (binder == null) {
            stopping = true;
            bindToService();
        } else {
            Log.debug(TAG, "Stopping scheduling service");
            binder.stop();
        }
    }

    /**
     * Starts a new poller instance
     */
    protected void startAutoSyncService(Controller controller) {
        Log.info(TAG, "Firing an explicit intent to start the auto sync service");

        // In general the auto sync service is triggered at device boot, but
        // before this is true, the device needs to be rebooted
        if (binder != null) {
            binder.updateSyncMode();
        } else {
            stopping = false;
            bindToService();
        }
    }

    protected void bindToService() {

        Intent intent = new Intent(context, AutoSyncService.class);
        int attempts = 0;
        boolean conn = false;
        // We saw cases where binding to the service fails. Therefore we try
        // few times in the hope the problem is just temporary
        do {
            Log.trace(TAG, "Connecting to the auto sync service");
            serviceConnection = new AutoSyncServiceConnection();
            conn = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            if (!conn) {
                Log.error(TAG, "Cannot connect to the AutoSync service");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                }
            }
            attempts++;
        } while(attempts < 3 && !conn);
        if (!conn) {
            // The connection failed, no scheduled sync is available
            // TODO FIXME: shall we warn the user about this?
            Log.error(TAG, "Scheduled sync will not work because the service failed to start");
        }
    }

    private class AutoSyncServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.trace(TAG, "AutoSync service connected");
            binder = (AutoSyncService.AutoSyncBinder) service;
            if (stopping) {
                binder.stop();
            } else {
                binder.updateSyncMode();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }
}
