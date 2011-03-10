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

package com.funambol.android.controller;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;

import com.funambol.android.AndroidAccountManager;
import com.funambol.android.AndroidAppSyncSource;
import com.funambol.android.AndroidAppSyncSourceManager;
import com.funambol.android.AppInitializer;
import com.funambol.android.source.AndroidChangesTracker;
import com.funambol.client.controller.SynchronizationController;
import com.funambol.client.source.AppSyncSource;
import com.funambol.syncml.client.ChangesTracker;
import com.funambol.syncml.client.TrackableSyncSource;
import com.funambol.util.Log;

import java.util.Enumeration;
import java.util.Vector;


/**
 * <code>AutoSyncSwitcher</code> allows to change the auto sync mode:
 * <li><code>AUTO_SYNC_MODE_NATIVE</code>: leave the auto sync handling to the
 * native sync manager</li>
 * <li><code>AUTO_SYNC_MODE_CUSTOM</code>: start a custom auto sync handling
 * based on <code>ContentObserver</code>s</li>
 *
 * Switching from <code>AUTO_SYNC_MODE_CUSTOM</code> to
 * <code>AUTO_SYNC_MODE_NATIVE</code> will immadiately
 * start a synchronization of the sources which has meen modified during the
 * custom auto sync period.
 */
public class AutoSyncSwitcher {

    private final String TAG_LOG = "AutoSyncSwitcher";

    public static final int AUTO_SYNC_MODE_NATIVE = 0;
    public static final int AUTO_SYNC_MODE_CUSTOM = 1;

    private Context mContext;
    private Handler mHandler;

    private AndroidAppSyncSourceManager appSyncSourceManager;
    private ContentResolver contentResolver;

    private Vector<AppSyncSource>   pendingSources;
    private Vector<String>          authoritiesWithAutoSync;
    private Vector<ContentObserver> observers;

    private AndroidHomeScreenController homeScreenController;

    private int status = AUTO_SYNC_MODE_NATIVE;

    public AutoSyncSwitcher(Context context, Handler handler,
            AndroidHomeScreenController homeScreenController) {

        this.mContext = context;
        this.mHandler = handler;
        this.contentResolver = mContext.getContentResolver();
        this.homeScreenController = homeScreenController;

        AppInitializer initializer = AppInitializer.getInstance(mContext);
        this.appSyncSourceManager = initializer.getAppSyncSourceManager();
    }

    /**
     * Switch to the given auto sync mode.
     * 
     * @param mode
     * @throws IllegalArgumentException
     */
    public void setAutoSyncMode(int mode) throws IllegalArgumentException {
        switch(mode) {
            case AUTO_SYNC_MODE_CUSTOM:
                if(status == AUTO_SYNC_MODE_CUSTOM) {
                    Log.debug(TAG_LOG, "Auto sync is already in custom mode");
                } else {
                    switchToCustomAutoSync();
                    status = AUTO_SYNC_MODE_CUSTOM;
                }
                break;
            case AUTO_SYNC_MODE_NATIVE:
                if(status == AUTO_SYNC_MODE_NATIVE) {
                    Log.debug(TAG_LOG, "Auto sync is already in native mode");
                } else {
                    switchToNativeAutoSync();
                    status = AUTO_SYNC_MODE_NATIVE;
                }
                break;
            default:
                Log.error(TAG_LOG, "Invalid auto sync mode: " + mode);
                throw new IllegalArgumentException("Invalid auto sync mode: " + mode);
        }
    }

    /**
     * @return the current auto sync mode
     */
    public int getAutoSyncMode() {
        return status;
    }

    private void switchToCustomAutoSync() {

        Log.debug(TAG_LOG, "Switching to custom auto sync");

        Account account = AndroidAccountManager.getNativeAccount(mContext);
        Enumeration sources = appSyncSourceManager.getEnabledAndWorkingSources();

        observers               = new Vector<ContentObserver>();
        authoritiesWithAutoSync = new Vector<String>();
        pendingSources          = new Vector<AppSyncSource>();

        AndroidAppSyncSource currentSource = (AndroidAppSyncSource)
                homeScreenController.getCurrentSource();
        
        while(sources.hasMoreElements()) {

            AndroidAppSyncSource appSource = (AndroidAppSyncSource)sources.nextElement();
            boolean isCurrentSource = (currentSource == appSource);
            
            String authority = appSource.getAuthority();
            
            if(ContentResolver.isSyncPending(account, authority) && !isCurrentSource) {
                Log.debug(TAG_LOG, "Found pending sync for authority: " + authority);
                pendingSources.add(appSource);

                Log.debug(TAG_LOG, "Cancelling pending sync for authority: " + authority);
                ContentResolver.cancelSync(account, authority);
            }
            if(authority == null) {
                Log.debug(TAG_LOG, "Authority not set for source: " + appSource.getName());
                continue;
            }
            boolean autoSync = ContentResolver.getSyncAutomatically(account, authority);

            Log.debug(TAG_LOG, "Disabling auto sync setting for authority: " + authority);
            ContentResolver.setSyncAutomatically(account, authority, false);

            if(autoSync) {

                Log.debug(TAG_LOG, "Saving auto sync setting for authority: " + authority);
                authoritiesWithAutoSync.add(authority);

                Log.debug(TAG_LOG, "Registering content observer for authority: " + authority);
                ContentObserver obs = new AndroidContentObserver(appSource, pendingSources, mHandler);
                observers.add(obs);

                contentResolver.registerContentObserver(appSource.getProviderUri(), true, obs);
            }
        }
    }

    private void switchToNativeAutoSync() {

        Log.debug(TAG_LOG, "Switching to native auto sync");

        // Remove content observers
        Log.debug(TAG_LOG, "Unregistering content observers");
        for(int i=0; i<observers.size(); i++) {
            contentResolver.unregisterContentObserver(observers.get(i));
        }

        // Resore the native auto-sync settings
        Log.debug(TAG_LOG, "Restoring auto sync settings");
        Account account = AndroidAccountManager.getNativeAccount(mContext);
        for(int i=0; i<authoritiesWithAutoSync.size(); i++) {
            String authority = authoritiesWithAutoSync.get(i);

            Log.debug(TAG_LOG, "Enabling auto sync setting for authority: " + authority);
            ContentResolver.setSyncAutomatically(account, authority, true);
        }

        // Sync the pending sources
        // Perform a synchronization for all the pending sources
        Vector sourcesToSync = getSourcesToSync(pendingSources);
        if(sourcesToSync.size() > 0) {
            Log.debug(TAG_LOG, "Sync pending sources");
            homeScreenController.syncMultipleSources(SynchronizationController.MANUAL,
                    sourcesToSync);
        }
    }

    /**
     * Implements a <code>ContentObserver</code> in order to keep track of
     * changes done on specific sources.
     */
    private class AndroidContentObserver extends ContentObserver {

        private static final String TAG_LOG = "AndroidContentObserver";

        private AppSyncSource currentSource;
        private Vector<AppSyncSource> pendingSources;

        public AndroidContentObserver(AppSyncSource currentSource,
                Vector<AppSyncSource> pendingSources, Handler handler) {
            super(handler);
            this.pendingSources = pendingSources;
            this.currentSource  = currentSource;
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.trace(TAG_LOG, "Detected change for source: " + currentSource.getName());
            if(!pendingSources.contains(currentSource)) {
                Log.trace(TAG_LOG, "Adding source: " + currentSource.getName() +
                        " to pending sources");
                pendingSources.add(currentSource);
            }
        }
    }

    /**
     * Filters the given sources Vector, keep only the ones which really need to
     * be synchronized.
     * 
     * @param sources
     * @return
     */
    private Vector<AppSyncSource> getSourcesToSync(Vector<AppSyncSource> sources) {
        Log.debug(TAG_LOG, "Retrieving sources to synchronize");

        for(int i=0; i<sources.size(); i++) {
            AndroidAppSyncSource appSource = (AndroidAppSyncSource)sources.get(i);
            TrackableSyncSource tss = (TrackableSyncSource)appSource.getSyncSource();
            ChangesTracker tracker = tss.getTracker();

            if(tracker instanceof AndroidChangesTracker) {
                AndroidChangesTracker aTracker = (AndroidChangesTracker)tracker;
                if(aTracker.hasChanges()) {
                    Log.debug(TAG_LOG, "Changes found in source: " + appSource.getName());
                } else {
                    Log.debug(TAG_LOG, "Changes not found in source: " + appSource.getName());
                    sources.remove(appSource);
                }
            }
        }
        return sources;
    }
}
