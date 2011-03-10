/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2003 - 2007 Funambol, Inc.
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

import java.util.Set;
import java.util.Vector;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Binder;
import android.os.Handler;
import android.accounts.Account;
import android.content.Context;
import android.content.SyncResult;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.AbstractThreadedSyncAdapter;
import android.os.Bundle;

import com.funambol.android.AndroidAppSyncSource;
import com.funambol.android.AppInitializer;
import com.funambol.android.SyncLock;
import com.funambol.android.AndroidConfiguration;
import com.funambol.android.AndroidCustomization;
import com.funambol.android.AndroidAppSyncSourceManager;
import com.funambol.android.controller.AndroidHomeScreenController;
import com.funambol.android.controller.AndroidController;
import com.funambol.android.source.AndroidChangesTracker;

import com.funambol.platform.NetworkStatus;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.controller.HomeScreenController;
import com.funambol.client.controller.Controller;
import com.funambol.client.controller.SynchronizationController;
import com.funambol.client.engine.SyncEngine;
import com.funambol.syncml.client.ChangesTracker;
import com.funambol.syncml.client.TrackableSyncSource;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.syncml.spds.SyncListener;
import com.funambol.util.Log;


/**
 * Represents an abstract SyncAdapterService which can the inherited by every
 * supported authority.
 *
 * If the service is invoked using a android.content.SyncAdapter intent action
 * the oBind method will return the SyncAdapterBinder handled by the native
 * SyncManager. The LocalBinder is returned otherwise, this is the use case of
 * activities that wish to be notified of the sync events.
 */
public abstract class AbstractSyncAdapterService extends Service {

    private final String TAG_LOG = "AbstractSyncAdapterService";

    private SyncAdapterImpl syncAdapter = null;
    private final Object syncAdapterLock = new Object();

    private Handler mHandler;

    protected AndroidAppSyncSourceManager appSyncSourceManager;
    private AndroidConfiguration configuration;
    private SyncSource   source = null;
    private SyncListener listener = null;
    private SyncEngine   syncEngine = null;
    private SyncLock     syncLock   = null;
    private int          syncLockId = SyncLock.FORBIDDEN;

    private Vector<AppSyncSource> syncedSources = new Vector<AppSyncSource>();

    public AbstractSyncAdapterService() {
        super();
    }

    /**
     * Get the SyncSource ids related to the current authority.
     * @return
     */
    protected abstract int[] getAppSyncSoureIds();

    /**
     * LocalBinder used by activities that wish to be notified of the sync events.
     */
    public class LocalBinder extends Binder {
        public void setHandler(Handler h) {
            mHandler = h;
        }

        public void setSyncListener(SyncListener aListener) {
            listener = aListener;
            // Update the SyncSource listener
            if(source != null) {
                source.setListener(listener);
            }
        }
    }

    /**
     * Define a generic SyncAdapter implementation
     */
    private class SyncAdapterImpl extends AbstractThreadedSyncAdapter {

        public SyncAdapterImpl(Context context) {
            super(context, false /* Initialization is performed through the
                                    onPerformSync method */);
        }

        @Override
        public void onPerformSync(Account account, Bundle extras, String authority,
                ContentProviderClient provider, SyncResult syncResult) {

            Log.debug(TAG_LOG, "Performing sync for account: " +
                    account.toString() + ", authority: " + authority);

            Set<String> set = extras.keySet();
            for(String element : set) {
                
                Log.trace(TAG_LOG, "Reading extras: " + element + "=" + extras.get(element));

                // If the SyncManager requests to be initialized we have to make
                // sure that setIsSyncable is called with a value >=0
                if(ContentResolver.SYNC_EXTRAS_INITIALIZE.equals(element)) {
                    boolean initialize = extras.getBoolean(element);
                    if(initialize) {
                        ContentResolver.setIsSyncable(account, authority, 1);
                        ContentResolver.setSyncAutomatically(account, authority, false);
                        return;
                    }
                }
            }

            if(!isValidExtras(extras)) {
                Log.debug(TAG_LOG, "The requested sync is not valid");
                return;
            }

            // Start the given authority
            synchronize(extras, false);
        }
    }

    private boolean isValidExtras(Bundle extras) {
        return extras.containsKey(AndroidHomeScreenController.SYNC_TYPE) ||
               extras.containsKey(ContentResolver.SYNC_EXTRAS_UPLOAD) ||
               extras.containsKey(ContentResolver.SYNC_EXTRAS_FORCE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.debug(TAG_LOG, "Service Created");

        AppInitializer initializer = AppInitializer.getInstance(this);
        initializer.init();

        appSyncSourceManager = initializer.getAppSyncSourceManager();
        configuration = initializer.getConfiguration();
        syncLock      = initializer.getSyncLock();

        // Initialize the SyncAdapter implementation
        synchronized (syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new SyncAdapterImpl(getApplicationContext());
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Bind the related SyncAdapter if invoked by the native SyncManager
        if("android.content.SyncAdapter".equals(intent.getAction() )) {
            return syncAdapter.getSyncAdapterBinder();
        } else {
            return new LocalBinder();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.debug(TAG_LOG, "Service Started");
        if(!configuration.getCredentialsCheckPending()) {
            synchronize(null, true);
        }
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        Log.debug(TAG_LOG, "Service Stopped");

        // If a sync is in progress, we shall cancel it
        // Depending on the way the sync is carried on, we need to cancel it in
        // different ways
        try {
            if (syncEngine != null && syncEngine.isSynchronizing()) {
                syncEngine.cancelSync();
            } else if(syncedSources.size() > 0){
                Log.trace(TAG_LOG, "Check if a sync was synchronizing");
                Controller controller = AndroidController.getInstance();
                HomeScreenController homeScreenController = controller.getHomeScreenController();
                for(int i=0; i<syncedSources.size(); i++) {
                    AppSyncSource appSource = syncedSources.get(i);
                    if (appSource == homeScreenController.getCurrentSource()) {
                        if (homeScreenController instanceof AndroidHomeScreenController) {
                            Log.trace(TAG_LOG, "Force sync to be cancelled for source: " + appSource.getName());
                            ((AndroidHomeScreenController)homeScreenController).forceSyncCancel(appSource);
                        }
                    }
                }
            } else if(syncLockId != SyncLock.FORBIDDEN) {
                // if the sync lock has been taken and there were not any synced
                // sources we have to update the home screen.
                Controller controller = AndroidController.getInstance();
                controller.getHomeScreenController().syncEnded();
            }
        } finally {
            if(syncLock != null) {
                syncLock.releaseLock(syncLockId);
            }
        }
    }

    /**
     * Returns the list of sources which needs to be synchronized, given the
     * extras bundle.
     *
     * If the sync request comes from the native sync engine, the extra
     * parameter AUTHORITY_TYPE is not present so we have to find the list of
     * sources to sync by checking the number of changes (through the ChangesTracker)
     *
     * @param extras
     * @return
     */
    private Vector getSourcesToSync(Bundle extras) {

        Log.debug(TAG_LOG, "Retrieving sources to synchronize");

        int[] appSyncSourceIds = getAppSyncSoureIds();
        Vector<AppSyncSource> sources = new Vector<AppSyncSource>();
        String authorityType = extras.getString(AndroidHomeScreenController.AUTHORITY_TYPE);

        if(authorityType == null) {
            //
            // The sync request comes from the native auto sync engine
            // Check which sources needs to be synchronized
            //
            Log.debug(TAG_LOG, "Authority type not found in extras Bundle");
            
            for(int i=0; i<appSyncSourceIds.length; i++) {
                int sourceId = appSyncSourceIds[i];
                AndroidAppSyncSource appSource = (AndroidAppSyncSource)
                appSyncSourceManager.getSource(sourceId);
                TrackableSyncSource tss = (TrackableSyncSource)appSource.getSyncSource();

                ChangesTracker tracker = tss.getTracker();
                if(tracker instanceof AndroidChangesTracker) {
                    AndroidChangesTracker aTracker = (AndroidChangesTracker)tracker;
                    try {
                        if(aTracker.hasChanges()) {
                            Log.debug(TAG_LOG, "Changes found in source: " + appSource.getName());
                            sources.add(appSource);
                        } else {
                            Log.debug(TAG_LOG, "Changes not found in source: " + appSource.getName());
                        }
                    } catch(Exception ex) { 
                        Log.error(TAG_LOG, "Exception while retrieving changes " +
                                "for source: " + appSource.getName(), ex);
                    }
                }
            }
        } else {
            Log.debug(TAG_LOG, "Authority type found in extras Bundle: " + authorityType);
            for(int i=0; i<appSyncSourceIds.length; i++) {
                int sourceId = appSyncSourceIds[i];
                AndroidAppSyncSource appSource = (AndroidAppSyncSource)
                        appSyncSourceManager.getSource(sourceId);
                if(authorityType.equals(appSource.getAuthorityType()) ||
                   authorityType.equals(AndroidAppSyncSource.AUTHORITY_TYPE_ALL)) {
                    Log.debug(TAG_LOG, "Adding source: " + appSource.getName());
                    sources.add(appSource);
                }
            }
        }
        syncedSources = sources;
        return sources;
    }

    /**
     * Perform the synchronization, if the operation is permitted
     *
     * @param extras The extra Bundle
     * @param threaded true if the sync shall be executed in a new thread
     */
    private void synchronize(Bundle extras, boolean threaded) {

        syncLockId = syncLock.acquireLock();
        if (syncLockId == SyncLock.FORBIDDEN) {
            Log.info(TAG_LOG, "Synchronization not permitted because sync is locked");
            return;
        }

        // Init sources to be synchronized
        Vector sources = getSourcesToSync(extras);
        if (sources == null || sources.size() == 0) {
            Log.info(TAG_LOG, "No sources to synchronize");
            return;
        }
        try {
            if(threaded) {
                // This code is executed when an activity connects directly to
                // the service
                NetworkStatus networkStatus = new NetworkStatus(this);
                AndroidCustomization customization = AndroidCustomization.getInstance();
                syncEngine = new SyncEngine(customization, configuration, appSyncSourceManager, networkStatus);
                syncEngine.synchronize(sources);
            } else {
                // This code is executed when an activity syncs via the
                // SyncAdapter
                Controller controller = AndroidController.getInstance();
                AndroidHomeScreenController homeScreenController;
                homeScreenController = (AndroidHomeScreenController)controller.getHomeScreenController();
                
                String syncType = extras.getString(AndroidHomeScreenController.SYNC_TYPE);
                if(syncType == null) {
                    if(extras.containsKey(ContentResolver.SYNC_EXTRAS_UPLOAD)) {
                        Log.debug(TAG_LOG, "Detected push mode");
                        syncType = SynchronizationController.PUSH;
                    } else {
                        Log.error(TAG_LOG, "Undefined sync type, use manual");
                        syncType = SynchronizationController.MANUAL;
                    }
                }
                boolean refresh = extras.getBoolean(AndroidHomeScreenController.REFRESH);
                if (refresh) {
                    int direction = extras.getInt(AndroidHomeScreenController.REFRESH_DIRECTION);
                    homeScreenController.refreshSources(sources, direction);
                } else {
                    homeScreenController.synchronize(syncType, sources);
                }
            }
        } catch (Exception e) {
            // Trap and ignore all these exceptions, as sync exceptions are
            // propagated via the SyncListener. In this thread we just need to
            // terminate
            Log.error(TAG_LOG, "Synchronization thread terminating with exception: ", e);
        } finally {
            syncEngine  = null;
            syncLock.releaseLock(syncLockId);
        }
    }
}
