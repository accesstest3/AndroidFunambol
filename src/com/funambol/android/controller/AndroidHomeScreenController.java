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

import java.util.Enumeration;
import java.util.Vector;

import android.os.Bundle;
import android.content.Context;
import android.content.ContentResolver;
import android.accounts.Account;
import android.app.Activity;
import android.content.SyncStatusObserver;
import android.os.Handler;
import com.funambol.android.AndroidAccountManager;

import com.funambol.android.AndroidAppSyncSource;
import com.funambol.android.activities.AndroidDisplayManager;
import com.funambol.android.activities.AndroidHomeScreen;
import com.funambol.android.source.AndroidChangesTracker;

import com.funambol.platform.NetworkStatus;
import com.funambol.client.controller.HomeScreenController;
import com.funambol.client.controller.Controller;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.ui.HomeScreen;
import com.funambol.client.ui.DisplayManager;
import com.funambol.syncml.client.ChangesTracker;
import com.funambol.syncml.client.TrackableSyncSource;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.syncml.spds.SyncListener;
import com.funambol.syncml.spds.SyncReport;
import com.funambol.syncml.spds.SyncException;
import com.funambol.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class AndroidHomeScreenController extends HomeScreenController {

    private static final String TAG_LOG = "AndroidHomeScreenController";

    public static final String SYNC_TYPE = "SyncType";
    public static final String REFRESH   = "Refresh";
    public static final String REFRESH_DIRECTION = "RefreshDirection";
    public static final String AUTHORITY_TYPE = "AuthorityType";

    private Context context;

    private AndroidDisplayManager dm;

    private boolean syncAll = false;
    private String  syncAllType = MANUAL;
    private Vector  syncAllSources = null;
    private boolean refresh        = false;
    private int     refreshDirection;

    private NetworkStatus networkStatus;

    private AutoSyncSwitcher autoSyncSwitcher;
    private AppSyncSource lastSourceRequested = null;

    private int sourceFinderDialogId = -1;

    private Object statusChangeListenerHandle = null;

    // The Timer used by addSyncStatusListener to handle sync state changes
    private Timer timer = new Timer();
    private SyncStatusTask syncStatusTask = new SyncStatusTask();

    public AndroidHomeScreenController(Context context, Controller controller, HomeScreen homeScreen, NetworkStatus networkStatus) {
        super(controller, homeScreen, networkStatus);
        this.dm = (AndroidDisplayManager)controller.getDisplayManager();
        this.context = context;
        engine.setSpawnThread(false);
        this.networkStatus = networkStatus;
        engine.setNetworkStatus(networkStatus);
        autoSyncSwitcher = new AutoSyncSwitcher(context, new Handler(), this);
    }

    /**
     * This method allows a sync to be cancelled. Cancelling a sync on Android
     * is a rather tricky operation, because there are several entry points and
     * different status in which the sync can be.
     * The following cases need to be addressed:
     *
     * <ul>
     *   <li> the sync is pending (meaning it was requested to the native sync
     *        subsystem, but it has been queued) </li>
     *
     *   <li> the sync is active (the native sync subsystem triggered it) but the
     *        SyncManager has not been invoked yet </li>
     *
     *   <li> the sync is active and the SyncManager has been invoked </li>
     *
     *   <li> the sync is cancelled via the native application </li>
     * </ul>
     *
     * For each case, we must make sure that the following things happen:
     *
     * <ul>
     *   <li> The home screen is properly refreshed </li>
     *   <li> The SyncEngine is unloked and the sync terminated </li>
     * </ul>
     */
    @Override
    public void cancelSync() {
        Log.trace(TAG_LOG, "Cancelling sync " + isSynchronizing() + " currentSource=" + currentSource);
        setCancel(true);

        // If a sync is active, we cancel it
        Account account = AndroidController.getNativeAccount();
        
        AndroidAppSyncSource appSource = isAnySourceSyncing();
        boolean cancelled = false;
        if (appSource != null) {
            String authority = appSource.getAuthority();
            ContentResolver.cancelSync(account, authority);
            cancelled = true;
        }

        // Remove all the sources in the pending state
        Enumeration sources = appSyncSourceManager.getWorkingSources();
        while(sources.hasMoreElements()) {
            appSource = (AndroidAppSyncSource)sources.nextElement();
            String authority = appSource.getAuthority();
            if (authority != null) {
                if (ContentResolver.isSyncPending(account, authority)) {
                    ContentResolver.cancelSync(account, authority);
                    if (!cancelled) {
                        forceSyncCancel(appSource);
                        cancelled = true;
                    }
                }
            }
        }

        if (!cancelled) {
            super.cancelSync();
        }
    }

    @Override
    public boolean isSynchronizing() {
        if (isAnySourcePending() != null) {
            return true;
        } else {
            return super.isSynchronizing();
        }
    }

    @Override
    public synchronized void synchronize(String syncType, Vector syncSources,
                                         int delay, boolean fromOutside) {

        Log.info(TAG_LOG, "synchronize " + syncType);

        if (!PUSH.equals(syncType) && isSynchronizing()) {
            Log.info(TAG_LOG, "A sync is already in progress");
            if(MANUAL.equals(syncType)) {
                showSyncInProgressMessage();
            }
            return;
        }

        if (syncSources.size() > 0) {
            lastSourceRequested = (AndroidAppSyncSource)syncSources.elementAt(0);
        }
        forceSynchronization(syncType, syncSources, false, 0, delay, fromOutside);
    }

    @Override
    public void showConfigurationScreen() {
        if (syncAll) {
            showSyncInProgressMessage();
        } else {
            super.showConfigurationScreen();
        }
    }

    /**
     * This method allows a sync to be cancelled. Cancelling a sync on Android
     * is a rather tricky operation, because there are several entry points and
     * different status in which the sync can be.
     * The following cases need to be addressed:
     *
     * <ul>
     *   <li> the sync is pending (meaning it was requested to the native sync
     *        subsystem, but it has been queued) </li>
     *
     *   <li> the sync is active (the native sync subsystem triggered it) but the
     *        SyncManager has not been invoked yet </li>
     *
     *   <li> the sync is active and the SyncManager has been invoked </li>
     *
     *   <li> the sync is cancelled via the native application </li>
     * </ul>
     *
     * For each case, we must make sure that the following things happen:
     *
     * <ul>
     *   <li> The home screen is properly refreshed </li>
     *   <li> The AndroidSyncEngine is unloked and the sync terminated </li>
     * </ul>
     */
    public void forceSyncCancel(AppSyncSource appSource) {
        if (engine.isSynchronizing()) {
            super.cancelSync();
        } else {
            // SyncManager won't be able to throw any exception and we
            // must simulate a sync termination here
            SyncSource source = appSource.getSyncSource();
            SyncListener listener = source.getListener();
            SyncReport syncReport = new SyncReport(source);
            //syncReport.setRequestedSyncMode(syncMode);
            syncReport.setLocUri(source.getName());
            syncReport.setRemoteUri(source.getSourceUri());
            syncReport.setSyncStatus(SyncListener.CANCELLED);
            listener.endSession(syncReport);
            changeSyncLabelsOnSyncEnded();
            unlockHomeScreen();
        }
    }

    public void syncSingleSource(String syncType, AppSyncSource appSource) {
        syncAll = false;
        syncAllSources = null;

        syncSource(syncType, appSource);
    }

    /**
     * This method is invoked when a sync is triggered from within the
     * application. This means the user clicked on a sync button (either a
     * source or sync all).
     * If the source is configured to use the sync adapter, then a native sync
     * is triggered, otherwise the SynchronizationController gets invoked
     * directly.
     */
    @Override
    protected void syncSource(String syncType, AppSyncSource appSource) {

        if (networkStatus != null && !networkStatus.isConnected()) {
            if (networkStatus.isRadioOff()) {
                noConnection();
            } else {
                noSignal();
            }
            //The sync all button could have been pressed when a running sync
            //wasn't detected. This avoid the user not allowed to enter the
            //settings when the network is down. 
            syncAll = false;
            return;
        }

        // A sync was triggered from our UI, fire a sync via the native sync
        Account account = AndroidController.getNativeAccount();

        Log.debug(TAG_LOG, "syncSource " + appSource.getName());
        AndroidAppSyncSource aAppSource = (AndroidAppSyncSource)appSource;
        lastSourceRequested = aAppSource;

        if (aAppSource.getSyncMethod() == AndroidAppSyncSource.SYNC_ADAPTER) {
            Bundle b = new Bundle();
            b.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
            b.putString(SYNC_TYPE, syncType);
            if(aAppSource.getAuthorityType() != null) {
                b.putString(AUTHORITY_TYPE, aAppSource.getAuthorityType());
            }
            String authority = aAppSource.getAuthority();
            Log.trace(TAG_LOG, "Syncing source with authority " + authority + " and account " + account);
            if (authority != null) {
                ContentResolver.requestSync(account, authority, b);
            } else {
                Log.error(TAG_LOG, "Cannot sync source with unknown authority");
            }
        } else {
            super.syncSource(syncType, aAppSource);
        }
    }

    private void refreshSingleSource(AndroidAppSyncSource source, int direction) {
        Log.trace(TAG_LOG, "Refreshing source: " + source.getName());

        if (networkStatus != null && !networkStatus.isConnected()) {
            if (networkStatus.isRadioOff()) {
                noConnection();
            } else {
                noSignal();
            }
            //The sync all button could have been pressed when a running sync
            //wasn't detected. This avoid the user not allowed to enter the
            //settings when the network is down. 
            syncAll = false;
            return;
        }

        lastSourceRequested = source;

        Account account = AndroidController.getNativeAccount();

        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
        b.putString(SYNC_TYPE, MANUAL);
        b.putBoolean(REFRESH, true);
        b.putInt(REFRESH_DIRECTION, direction);
        if(source.getAuthorityType() != null) {
            b.putString(AUTHORITY_TYPE, source.getAuthorityType());
        }
        String authority = source.getAuthority();
        if (authority != null) {
            ContentResolver.requestSync(account, authority, b);
        } else {
            Log.error(TAG_LOG, "Cannot sync source with unknown authority");
        }
    }

    @Override
    public void refresh(int mask, int direction) {
        // A sync was triggered from our UI, fire a sync via the native sync
        Enumeration sources = appSyncSourceManager.getEnabledAndWorkingSources();
        Vector appSources = new Vector();
        while(sources.hasMoreElements()) {
            AndroidAppSyncSource appSource = (AndroidAppSyncSource)sources.nextElement();
            if ((appSource.getId() & mask) != 0) {
                Log.debug(TAG_LOG, "refreshSource " + appSource.getName());
                appSources.addElement(appSource);
            }
        }

        if (appSources.size() > 0) {

            AndroidAppSyncSource appSource = (AndroidAppSyncSource)appSources.elementAt(0);
            appSources.removeElementAt(0);

            refresh = true;
            refreshDirection = direction;

            if (appSources.size() > 1) {
                syncAllSources = appSources;
                syncAll = true;
            }

            refreshSingleSource(appSource, direction);
        }
    }

    /**
     * Requests a synchronization for multiple sources. The synchronizations
     * will be performed via the native android sync.
     */
    public void syncMultipleSources(String syncType, Vector sources) {

        Log.trace(TAG_LOG, "syncMultipleSources " + syncType);

        syncAll = true;
        refresh = false;
        syncAllType = syncType;
        syncAllSources = sources;

        AppSyncSource firstSource = null;
        if(sources.size() > 0) {
            firstSource = (AppSyncSource)syncAllSources.get(0);
            syncAllSources.removeElementAt(0);
        } else {
            Log.debug(TAG_LOG, "syncMultipleSources: no sources to synchronize");
            syncAll = false;
            return;
        }

        // We schedule only the first source here, because leaving requests in
        // the native queue will trigger the native timeout after 5 minutes.
        // We don't want this to happen, otherwise we may interrupt long syncs.
        // Successive sources will be queued in the sourceEnded method
        syncSource(syncType, firstSource);
    }
    
    /**
     * This method is redefined for Android because we create one request to the
     * native sync subsystem for each source that must be synchronized (similar
     * to what the scheduled sync does).
     */
    @Override
    public void syncAllSources(String syncType) {
        Log.info(TAG_LOG, "syncAllSources " + syncType);
        Vector sources = new Vector();
        for(int i=0;i<items.size();++i) {
            AppSyncSource appSource = (AppSyncSource)items.elementAt(i);
            if (appSource.isEnabled() && appSource.isWorking()) {
                sources.addElement(appSource);
            }
        }
        syncMultipleSources(syncType, sources);
    }

    public boolean isFirstSyncDialogDisplayed() {
        if (dm.isAlertPending(DisplayManager.FIRST_SYNC_DIALOG_ID)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void sourceEnded(AppSyncSource appSource) {
        AndroidAppSyncSource next = null;
        if (syncAll) {
            // If this source was part of a sync all, then we shall trigger the next
            // sync
            if (syncAllSources.size() > 0) {
                next = (AndroidAppSyncSource)syncAllSources.elementAt(0);
                syncAllSources.removeElementAt(0);
            }
        }
        super.sourceEnded(appSource);
        if (next != null) {
            Log.info(TAG_LOG, "Requesting a new sync for: " + next.getName());
            if (refresh) {
                refreshSingleSource(next, refreshDirection);
            } else {
                syncSource(syncAllType, next);
            }
        } else {
            syncAll = false;
            syncAllSources = null;
        }
    }

    /**
     * If the sync all button is pressed and there is not a syncall operation
     * we shall display a "sync in progress" message since there could be a
     * pending sync (e.g. push sync).
     */
    @Override
    public void syncAllPressed() {
        Log.trace(TAG_LOG, "Sync All Button pressed");
        String syncAllText = ((AndroidHomeScreen)homeScreen).getSyncAllText();
        if (syncAllText.equals(localization.getLanguage("home_sync_all")) && isSynchronizing()) {
            showSyncInProgressMessage();
        } else {
            super.syncAllPressed();
        }
    }

    public boolean getSyncAll() {
        return syncAll;
    }

    @Override
    public void sourceFailed(AppSyncSource appSource, SyncException e) {
        syncAll = false;
        super.sourceFailed(appSource, e);
    }

    @Override
    public boolean syncStarted(Vector sources) {
        if(autoSyncSwitcher.getAutoSyncMode() == AutoSyncSwitcher.AUTO_SYNC_MODE_NATIVE) {
            // Switching to custom auto sync mode
            autoSyncSwitcher.setAutoSyncMode(AutoSyncSwitcher.AUTO_SYNC_MODE_CUSTOM);
        }
        return super.syncStarted(sources);
    }

    @Override
    public void syncEnded() {
        if (!syncAll) {
            super.syncEnded();
            syncAll = false;
            // Switching to custom auto sync mode
            autoSyncSwitcher.setAutoSyncMode(AutoSyncSwitcher.AUTO_SYNC_MODE_NATIVE);
        } else {
            if (syncAllSources != null && syncAllSources.size() > 0) {
                syncAll = true;
            } else {
                syncAll = false;
            }
        }
    }

    @Override
    public synchronized void
    continueSynchronizationAfterFirstSyncDialog(String syncType,
                                                Vector filteredSources,
                                                boolean refresh,
                                                int direction,
                                                int delay,
                                                boolean fromOutside,
                                                boolean continueSyncFromDialog)
    {
        // If the sync shall continue after dialog check, the sync must proceed
        // through the native sync application
        if(!continueSyncFromDialog || filteredSources.isEmpty()) {
            super.continueSynchronizationAfterFirstSyncDialog(syncType, filteredSources,
                    refresh, direction, delay, fromOutside, continueSyncFromDialog);
        } else {
            AppSyncSource first = (AppSyncSource)filteredSources.get(0);
            if(filteredSources.size() > 1) {
                this.syncAll = true;
                this.refresh = false;
                this.syncAllType = syncType;
                this.syncAllSources = filteredSources;
            }
            syncSource(syncType, first);
        }
    }

    public AndroidAppSyncSource isAnySourcePending() {

        // On Android a sync can be triggered from the native client app and
        // from within our app. When the sync has actually been fired, we are
        // sure to be always attached, but the sync may be pending. For this
        // case we need an explicit test here

        Enumeration sources = appSyncSourceManager.getWorkingSources();
        Account account = AndroidController.getNativeAccount();
        if(account == null) {
            // No account defined
            return null;
        }
        while(sources.hasMoreElements()) {
            AndroidAppSyncSource appSource = (AndroidAppSyncSource)sources.nextElement();
            String authority = appSource.getAuthority();
            if (authority != null) {
                if (ContentResolver.isSyncPending(account, authority)) {
                    return appSource;
                }
            }
        }
        return null;
    }


    public AndroidAppSyncSource isAnySourceSyncing() {

        Enumeration sources = appSyncSourceManager.getWorkingSources();
        Account account = AndroidController.getNativeAccount();
        if(account == null) {
            // No account defined
            return null;
        }
        AndroidAppSyncSource resSource = null;
        while(sources.hasMoreElements()) {
            AndroidAppSyncSource appSource = (AndroidAppSyncSource)sources.nextElement();
            String authority = appSource.getAuthority();
            if (authority != null) {
                if (ContentResolver.isSyncActive(account, authority) &&
                   !ContentResolver.isSyncPending(account, authority))
                {
                    // Some authorities may have more than one source associated
                    // (e.g. the media one). For this reason if the source is
                    // not the last one that requested a sync, we search more
                    if (appSource == lastSourceRequested) {
                        return appSource;
                    }
                    if (resSource == null) {
                        resSource = appSource;
                    }
                }
            }
        }
        return resSource;
    }

    /**
     * Calls attachToRunningSync method anly if there is a source pending or
     * syncing.
     */
    public void attachToRunningSyncIfAny() {
        Log.debug(TAG_LOG, "Attaching to running sync if any");
        // Iterate over all the sources and check if any of them is currently
        // active or pending (in the native sync queue)
        AndroidAppSyncSource activeSource = isAnySourceSyncing();
        boolean isPending = false;
        if (activeSource == null) {
            isPending = true;
            activeSource = isAnySourcePending();
            // If the pending sync is for media, we don't know exactly the
            // source which is being syncrhonized. Detecting the source may
            // require some time because we must query the trackers. So we show
            // a wait dialog while we do the computation and finally attach to
            // the right source
            if (activeSource != null && activeSource.getIsMedia()) {
                String msg = localization.getLanguage("dialog_wait_for_media_identification");
                sourceFinderDialogId = dm.showProgressDialog(screen, msg, true);
                MediaSourceFinder mediaSourceFinder = new MediaSourceFinder();
                mediaSourceFinder.start();
                activeSource = null;
            }
        }
        if (activeSource != null && !homeScreen.isLocked()) {
            // For pending sync during push, check if the quota is exceeded to cancel sync with
            // correct status. See http://funzilla.funambol.com/bugzilla/show_bug.cgi?id=9711
            if(activeSource.getConfig().getLastSyncStatus() == SyncListener.DEVICE_FULL_ERROR && isPending){
                Account account = AndroidAccountManager.getNativeAccount((Activity)screen.getUiScreen());
                ContentResolver.cancelSync(account, activeSource.getAuthority());
            } else {
                Log.info(TAG_LOG, "A sync is already running on " + activeSource.getName() + ", attaching to it");
                if (sourceFinderDialogId == -1) {
                    attachToRunningSync(activeSource);
                }
            }
        } else if(activeSource == null && homeScreen.isLocked()) {
            // If there is not an active source and the screen is locked, it
            // means that the home screen ui is waiting for a running sync that
            // will never come, in this case we simulate a sync ended.
            syncEnded();
        }
    }

    public void addSyncStatusListener() {
        if(statusChangeListenerHandle == null) {
            statusChangeListenerHandle = ContentResolver.addStatusChangeListener(
            /* SYNC_OBSERVER_TYPE_PENDING */ 2,
            new SyncStatusObserver() {
                public synchronized void onStatusChanged(int i) {
                    Log.trace(TAG_LOG, "Change on sync state detected, "
                            + "scheduling new task");
                    try {
                        // Cancel current timer
                        timer.cancel();
                        syncStatusTask.cancel();

                        // Schedule new task
                        timer = new Timer();
                        syncStatusTask = new SyncStatusTask();
                        timer.schedule(syncStatusTask,
                                SyncStatusTask.DEFAULT_DELAY);
                        
                    } catch(Throwable t) {
                        Log.error(TAG_LOG, "Error while scheduling " +
                                "sync status task", t);
                    }
                }
            });
        }
    }

    private class SyncStatusTask extends TimerTask {
        public static final long DEFAULT_DELAY = 2000;
        public void run() {
            attachToRunningSyncIfAny();
        }
    }

    public void removeSyncStatusListener() {
        if(statusChangeListenerHandle != null) {
            ContentResolver.removeStatusChangeListener(statusChangeListenerHandle);
            statusChangeListenerHandle = null;
        }
    }

    public void logout() {
        // Cannot logout if a sync is in progress
        if (isSynchronizing()) {
            showSyncInProgressMessage();
            return;
        }
        configuration.setCredentialsCheckPending(true);
        configuration.save();
        try {
            controller.getDisplayManager().showScreen(screen, Controller.LOGIN_SCREEN_ID);
        } catch(Exception ex) {
            Log.error(TAG_LOG, "Unable to switch to logout", ex);
        }
    }

    private class MediaSourceFinder extends Thread {
        @Override
        public void run() {
            Log.trace(TAG_LOG, "Identifying the media source that is currently pending");
            try {
                AndroidAppSyncSource activeSource = findActiveMediaSource();
                if (activeSource != null) {
                    if(activeSource.getConfig().getLastSyncStatus() == SyncListener.DEVICE_FULL_ERROR){
                        Log.debug(TAG_LOG, "Cancelling sync for source over quota " + activeSource.getName());
                        Account account = AndroidAccountManager.getNativeAccount((Activity)screen.getUiScreen());
                        ContentResolver.cancelSync(account, activeSource.getAuthority());
                    } else {
                        Log.debug(TAG_LOG, "Attaching to media source: " + activeSource.getName());
                        attachToRunningSync(activeSource);
                    }
                }
            } finally {
                dm.dismissProgressDialog(screen, sourceFinderDialogId);
                sourceFinderDialogId = -1;
            }
        }

        private AndroidAppSyncSource findActiveMediaSource() {
            // Media can correspond to more than one source, we need to
            // figure out which source is being synchronized
            Enumeration sources = appSyncSourceManager.getWorkingSources();
            AndroidAppSyncSource res = null;

            // Order the sources according to the UI order
            while(sources.hasMoreElements()) {
                AndroidAppSyncSource appSource = (AndroidAppSyncSource)sources.nextElement();
                if (appSource.getIsMedia()) {
                    TrackableSyncSource tss = (TrackableSyncSource)appSource.getSyncSource();
                    ChangesTracker tracker = tss.getTracker();
                    if(tracker instanceof AndroidChangesTracker) {
                        AndroidChangesTracker aTracker = (AndroidChangesTracker)tracker;
                        try {
                            // If more than one source has changes, we must get the
                            // first one in the uiOrder
                            if(aTracker.hasChanges()) {
                                if (res == null) {
                                    res = appSource;
                                } else if (appSource.getUiSourceIndex() < res.getUiSourceIndex()) {
                                    res = appSource;
                                }
                            } else {
                                Log.debug(TAG_LOG, "Changes not found in source: " + appSource.getName());
                            }
                        } catch(Exception ex) {
                            Log.error(TAG_LOG, "Exception while retrieving changes " +
                                    "for source: " + appSource.getName(), ex);
                        }
                    }
                }
            }
            return res;
        }
    }
}
