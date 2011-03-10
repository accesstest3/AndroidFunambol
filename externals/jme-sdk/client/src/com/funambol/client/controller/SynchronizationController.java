/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2008 Funambol, Inc.
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

package com.funambol.client.controller;

import java.util.Vector;
import java.util.Enumeration;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.engine.SyncEngine;
import com.funambol.client.engine.Poller;
import com.funambol.client.engine.SyncEngineListener;
import com.funambol.client.engine.AppSyncRequest;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.AppSyncSourceConfig;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.customization.Customization;
import com.funambol.client.localization.Localization;
import com.funambol.client.ui.Screen;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.spds.SyncException;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncListener;
import com.funambol.push.SyncScheduler;
import com.funambol.util.ConnectionListener;
import com.funambol.util.StringUtil;
import com.funambol.util.Log;
import com.funambol.platform.NetworkStatus;

/**
 * This class provides a basic controller that can be used by any other
 * controller that needs synchronization support. This controller is just
 * a building block, it does not control any UI component. But it shall be
 * extended by controllers that need synchronization capabilities (e.g.
 * HomeScreenController and AccountScreenController).
 */
public class SynchronizationController implements ConnectionListener, SyncEngineListener {

    private static final String TAG_LOG = "SynchronizationController";

    public static final String MANUAL    = "manual";
    public static final String SCHEDULED = "scheduled";
    public static final String PUSH      = "push";

    public static final int REFRESH_FROM_SERVER = 0;
    public static final int REFRESH_TO_SERVER   = 1;

    protected Controller controller;

    protected Customization customization;

    protected Configuration configuration;

    protected AppSyncSourceManager appSyncSourceManager;

    protected Localization localization;

    protected SyncEngine engine;

    protected boolean    doCancel        = false;

    private String     syncType        = null;

    protected AppSyncSource currentSource = null;

    protected boolean    showTCPAlert;

    protected boolean    logConnectivityError;

    private SyncScheduler  syncScheduler;

    private int            scheduledAttempt = 0;

    private Poller         retryPoller = null;

    protected Screen         screen;

    private AppSyncRequest appSyncRequestArr[] = new AppSyncRequest[1];
    private RequestHandler reqHandler;
    private Vector         deviceFullSources = null;

    private int            RETRY_POLL_TIME = 1;

    private NetworkStatus networkStatus;


    private boolean isUserConfirmationNeeded = false;

    private FirstSyncRequest pendingFirstSyncQuestion = null;

    SynchronizationController() {
        throw new IllegalArgumentException("Invalid");
    }

    SynchronizationController(Controller controller, Screen screen, NetworkStatus networkStatus) {
        Log.trace(TAG_LOG, "Initializing synchronization controller");

        this.controller = controller;
        this.screen = screen;
        this.networkStatus = networkStatus;
        
        localization = controller.getLocalization();
        appSyncSourceManager = controller.getAppSyncSourceManager();
        customization = controller.getCustomization();
        configuration = controller.getConfiguration();

        initSyncScheduler();
    }

    /**
     * TODO: Remove once the com.funambol.client.controller package integration is finished
     */
    SynchronizationController(Controller controller, Customization customization,
            Configuration configuration, Localization localization,
            AppSyncSourceManager appSyncSourceManager, Screen screen,
            NetworkStatus networkStatus) {
        
        Log.trace(TAG_LOG, "Initializing synchronization controller");

        this.controller = controller;
        this.screen = screen;
        this.networkStatus = networkStatus;

        this.localization = localization;
        this.appSyncSourceManager = appSyncSourceManager;
        this.customization = customization;
        this.configuration = configuration;

        initSyncScheduler();
    }

    protected void initSyncScheduler() {
        engine = createSyncEngine();
        syncScheduler = new SyncScheduler(engine);
        // The request handler is a daemon serving external requests
        reqHandler = new RequestHandler();
        reqHandler.start();
    }


    /**
     * Returns true iff a synchronization is in progress
     */
    public boolean isSynchronizing() {
        return engine.isSynchronizing();
    }

    /**
     * Returns the sync source currently being synchronized. If a sync is not
     * in progress, then null is returned. Please note that this method is not
     * completely equivalent to isSynchronizing. At the beginning of a sync,
     * isSynchronizing returns true, but getCurrentSource may return null until
     * the source is prepared for the synchronization.
     */
    public AppSyncSource getCurrentSource() {
        return engine.getCurrentSource();
    }

    /**
     * @return the current <code>SyncEngine</code> instance
     */
    public SyncEngine getSyncEngine() {
        return engine;
    }

    /**
     * Try to cancel the current sync. This works for cooperative sources that
     * check the synchronizationController status.
     */
    public void cancelSync() {
        Log.trace(TAG_LOG, "Cancelling sync " + isSynchronizing() + " currentSource=" + currentSource);
        setCancel(true);

        if (isSynchronizing() && currentSource != null) {
            UISyncSourceController uiSourceController = currentSource.getUISyncSourceController();
            if (uiSourceController != null) {
                uiSourceController.startCancelling();
            }
            engine.cancelSync();
        }
    }

    /**
     * Perform a refresh for a set of sources and a given direction. The method
     * gets blocked until the sync terminates.
     *
     * @param syncMask the set of sources to sync
     * @param direction the refresh direction
     */
    public void refresh(int mask, int direction) {

        Enumeration sources = appSyncSourceManager.getEnabledAndWorkingSources();
        Vector syncSources = new Vector();
        while(sources.hasMoreElements()) {
            AppSyncSource appSource = (AppSyncSource)sources.nextElement();
            if ((appSource.getId() & mask) != 0) {
                syncSources.addElement(appSource);
            }
        }
        refreshSources(syncSources, direction);
    }

    public synchronized void refreshSources(Vector syncSources, int direction) {

        if (isSynchronizing()) {
            return;
        }
        // A refresh is always a manual sync, force the sync type here
        forceSynchronization(MANUAL, syncSources, true, direction, 0, false);
    }

    public void syncEnded() {
        syncType = null;

        // TODO FIXME MARCO
        /*
        if(customization.enableUpdaterManager()){
            UpdaterManager upm = UpdaterManager.getInstance();
            upm.setController(controller);
            upm.check();
        }
        */
    }

    /**
     * Triggers a synchronization for the given syncSources. The caller can
     * specify its type (manual, scheduled, push) to change the error handling
     * behavior
     *
     * @param syncType the caller type (SYNC_TYPE_MANUAL, SYNC_TYPE_SCHEDULED)
     * @param syncSources is a vector of AppSyncSource to be synced
     *
     */
    public synchronized void synchronize(String syncType, Vector syncSources) {
        synchronize(syncType, syncSources, 0);
    }

    /**
     * Schedules a synchronization for the given syncSources. The sync is
     * scheduled in "delay" milliseconds from now. The caller can
     * specify its type (manual, scheduled, push) to change the error handling
     * behavior
     *
     * @param syncType the caller type (SYNC_TYPE_MANUAL, SYNC_TYPE_SCHEDULED)
     * @param syncSources is a vector of AppSyncSource to be synced
     * @param delay the interval at which the sync shall be performed (relative
     *              to now)
     *
     */
    public synchronized void synchronize(String syncType, Vector syncSources, int delay) {
        synchronize(syncType, syncSources, delay, false);
    }

    /**
     * Schedules a synchronization for the given syncSources. The sync is
     * scheduled in "delay" milliseconds from now. The caller can
     * specify its type (manual, scheduled, push) to change the error handling
     * behavior.
     * The caller can also specify it the sync request is generated outside of
     * the application. In such a case the handling is special and the
     * synchronization is actually performed by the Sync Client process. This
     * calls notifies the SyncClient to schedule a sync at the given interval.
     * This is useful when syncs are triggered on external events, such as
     * modification of PIM (c2s push).
     *
     * @param syncType the caller type (SYNC_TYPE_MANUAL, SYNC_TYPE_SCHEDULED)
     * @param syncSources is a vector of AppSyncSource to be synced
     * @param delay the interval at which the sync shall be performed (relative
     *              to now)
     * @param fromOutside specifies if the request is generated outside of the
     * application
     */
    public synchronized void synchronize(String syncType, Vector syncSources,
                                         int delay, boolean fromOutside) {

        Log.info(TAG_LOG, "synchronize " + syncType);

        if (isSynchronizing()) {
            Log.info(TAG_LOG, "A sync is already in progress");
            return;
        }
        forceSynchronization(syncType, syncSources, false, 0, delay, fromOutside);
    }

    protected synchronized void forceSynchronization(String syncType, Vector syncSources,
                                                     boolean refresh, int direction,
                                                     int delay, boolean fromOutside)
    {
        // Search if at least one of the selected sources has a warning on the
        // first sync
        Vector filteredSources = new Vector();
        Vector sourcesWithQuestion = new Vector();

        // if the syncType is automatic (e.g. not manual), we shall skip all the
        // sources which have a device full error or that are to be synchronized
        // only in WiFi if WiFi is not available
        if (!MANUAL.equals(syncType)) {
            Vector prefilteredSources = new Vector();
            for(int i=0;i<syncSources.size();++i) {
                AppSyncSource appSource = (AppSyncSource)syncSources.elementAt(i);

                // If the source is not in quota error or this is the first sync
                // in this session, then we include the source, otherwise we
                // skip it
                boolean toBeSynced = appSource.getConfig().getLastSyncStatus() != SyncListener.DEVICE_FULL_ERROR ||
                                     !appSource.getSyncedInSession();
                // We still need to check if the source requires to be synced
                // only in WiFi and this connection is available
                if (toBeSynced) {
                    if (configuration.getBandwidthSaverActivated()) {
                        if (appSource.getBandwidthSaverUse() && !networkStatus.isWiFiConnected()) {
                            // Skip this source beacuse of the bandwidth saver.
                            // Remember that we have a pending sync now
                            AppSyncSourceConfig sourceConfig = appSource.getConfig();
                            sourceConfig.setPendingSync(syncType, sourceConfig.getSyncType());
                            configuration.save();
                            toBeSynced = false;
                        }
                    }
                }

                if (toBeSynced) {
                    prefilteredSources.addElement(appSource);
                } else {
                    // The sync for this source is terminated
                    Log.info(TAG_LOG, "Ignoring sync for source: " + appSource.getName());
                    sourceEnded(appSource);
                }
            }
            syncSources = prefilteredSources;
        }


        // Now check if any source to be synchronized requires user confirmation
        // because of the bandwidth saver
        if(MANUAL.equals(syncType)) {
            for(int y=0;y<syncSources.size();++y) {
                AppSyncSource appSource = (AppSyncSource)syncSources.elementAt(y);
                if(appSource.getBandwidthSaverUse() && configuration.getBandwidthSaverActivated()){
                    if(!networkStatus.isWiFiConnected()){
                        sourcesWithQuestion.addElement(appSource);
                    }
                }
            }
        }

        // We cannot ask the question if there is no app visible
        if (screen == null && sourcesWithQuestion.size() > 0) {
            // Remember this so that on the next home screen startup, we will be
            // able to show the dialog. We don't continue the sync here because
            // we need a feedback from the user
            AppSyncSource[] dialogDependentSources = new AppSyncSource[sourcesWithQuestion.size()];
            sourcesWithQuestion.copyInto(dialogDependentSources);
            // TODO FIXME: use the proper question!!!
            /*
            pendingFirstSyncQuestion = new FirstSyncRequest();
            pendingFirstSyncQuestion.dialogDependentSources = dialogDependentSources;
            pendingFirstSyncQuestion.syncType = syncType;
            pendingFirstSyncQuestion.filteredSources = filteredSources;
            pendingFirstSyncQuestion.refresh = refresh;
            pendingFirstSyncQuestion.direction = direction;
            pendingFirstSyncQuestion.delay = delay;
            pendingFirstSyncQuestion.fromOutside = fromOutside;
            pendingFirstSyncQuestion.numSources = dialogDependentSources.length;
            pendingFirstSyncQuestion.sourceIndex = 0;
            */
        } else {
            if (sourcesWithQuestion.size() == 0) {
                Log.debug(TAG_LOG, "Continue sync without prompts");
                //No dialog is prompted for any sources: the sync can begin
                continueSynchronizationAfterBandwithSaverDialog(syncType, syncSources, 
                        refresh, direction, delay, fromOutside, false);
            } else {
                Log.debug(TAG_LOG, "Continue sync displaying bandwith prompt");
                DialogController dialControll = controller.getDialogController();
                dialControll.showNoWIFIAvailableDialog(screen,syncType,
                                                       sourcesWithQuestion, refresh,
                                                       direction, delay,
                                                       fromOutside);

                //The sync request is started when the user has finished to reply
                //all the first sync request dialogs (the last sync request dialog)
                //(calling the continueSynchronizationAfterDialogCheck method)
            }
        }
    }

    public void showPendingFirstSyncQuestion() {
        if (pendingFirstSyncQuestion != null) {
            DialogController dialControll = controller.getDialogController();
            dialControll.showFirstSyncDialog(screen,
                                             pendingFirstSyncQuestion.dialogDependentSources,
                                             pendingFirstSyncQuestion.syncType,
                                             pendingFirstSyncQuestion.filteredSources,
                                             pendingFirstSyncQuestion.refresh,
                                             pendingFirstSyncQuestion.direction,
                                             pendingFirstSyncQuestion.delay,
                                             pendingFirstSyncQuestion.fromOutside,
                                             pendingFirstSyncQuestion.numSources,
                                             pendingFirstSyncQuestion.sourceIndex);
            pendingFirstSyncQuestion = null;
        }
    }

    public synchronized void
    continueSynchronizationAfterBandwithSaverDialog(String syncType,
                                                    Vector syncSources,
                                                    boolean refresh,
                                                    int direction,
                                                    int delay,
                                                    boolean fromOutside,
                                                    boolean continueSyncFromDialog)
    {
        Vector filteredSources = new Vector();
        Vector sourcesWithQuestion = new Vector();
        //Select the sources to be synchronized without displaying
        //fisrt sync question to the user and count the other ones
        for(int i=0;i<syncSources.size();++i) {
            AppSyncSource appSource = (AppSyncSource)syncSources.elementAt(i);
            String warning = appSource.getWarningOnFirstSync();
            boolean synced = appSource.getConfig().getSynced();
            if (synced || warning == null) {
                //Sources that don't need the first sync question
                filteredSources.addElement(appSource);
                appSource.getConfig().commit();
                appSource.setSyncedInSession(true);
            } else if (!synced && warning != null) {
                //Sources that need the first sync question
                sourcesWithQuestion.addElement(appSource);
            }
        }

        // We cannot ask the question if there is no app visible
        if (screen == null && sourcesWithQuestion.size() > 0) {
            // Remember this so that on the next home screen startup, we will be
            // able to show the dialog. We don't continue the sync here because
            // we need a feedback from the user
            AppSyncSource[] dialogDependentSources = new AppSyncSource[sourcesWithQuestion.size()];
            sourcesWithQuestion.copyInto(dialogDependentSources);
            pendingFirstSyncQuestion = new FirstSyncRequest();
            pendingFirstSyncQuestion.dialogDependentSources = dialogDependentSources;
            pendingFirstSyncQuestion.syncType = syncType;
            pendingFirstSyncQuestion.filteredSources = filteredSources;
            pendingFirstSyncQuestion.refresh = refresh;
            pendingFirstSyncQuestion.direction = direction;
            pendingFirstSyncQuestion.delay = delay;
            pendingFirstSyncQuestion.fromOutside = fromOutside;
            pendingFirstSyncQuestion.numSources = dialogDependentSources.length;
            pendingFirstSyncQuestion.sourceIndex = 0;
        } else {
            if (sourcesWithQuestion.size() == 0) {
                Log.debug(TAG_LOG, "Continue sync without alerts");
                //No dialog is prompted for any sources: the sync can begin
                continueSynchronizationAfterFirstSyncDialog(syncType, filteredSources, 
                        refresh, direction, delay, fromOutside, false);
            } else {
                Log.debug(TAG_LOG, "Continue sync displaying alerts");
                AppSyncSource[] dialogDependentSources = new AppSyncSource[sourcesWithQuestion.size()];
                sourcesWithQuestion.copyInto(dialogDependentSources);
                DialogController dialControll = controller.getDialogController();
                dialControll.showFirstSyncDialog(screen, dialogDependentSources, syncType, filteredSources,
                        refresh, direction, delay, fromOutside, dialogDependentSources.length, 0);
                //The sync request is started when the user has finished to reply
                //all the first sync request dialogs (the last sync request dialog)
                //(calling the continueSynchronizationAfterDialogCheck method)
            }
        }

    }
 

    public synchronized void
    continueSynchronizationAfterFirstSyncDialog(String syncType,
                                                Vector filteredSources,
                                                boolean refresh,
                                                int direction,
                                                int delay,
                                                boolean fromOutside,
                                                boolean continueSyncFromDialog)
    {
        // If no sources left, we simply return and do not update/change
        // anything
        if (filteredSources.size() == 0) {
            syncEnded();
            return;
        }

        // We register as listeners for the sync
        engine.setListener(this);

        this.syncType = syncType;
        int sourceSyncType = 0;
        AppSyncRequest appSyncRequest = new AppSyncRequest(null, delay);
        Enumeration sources = filteredSources.elements();
        while(sources.hasMoreElements()) {
            AppSyncSource appSource = (AppSyncSource) sources.nextElement();
            SyncSource source = appSource.getSyncSource();

            if (refresh) {
                int syncMode = appSource.prepareRefresh(direction);
                source.getConfig().setSyncMode(syncMode);
            } else {
                sourceSyncType = appSource.getConfig().getSyncType();
                // If this source has no config set, then we cannot force a sync
                // mode, but this is a logical error
                if (source.getConfig() != null) {
                    source.getConfig().setSyncMode(sourceSyncType);
                } else {
                    Log.error(TAG_LOG, "Source has no config, cannot set sync mode");
                }
            }

            // Clear any pending sync information here, because we are about to
            // start a sync
            AppSyncSourceConfig sourceConfig = appSource.getConfig();
            sourceConfig.setPendingSync("", -1);
            configuration.save();
            // Add the request for this synchronization
            appSyncRequest.addRequestContent(appSource);
        }

        if (fromOutside) {
            synchronized(appSyncRequestArr) {
                appSyncRequestArr[0] = appSyncRequest;
                appSyncRequestArr.notify();
            }
        } else {
            syncScheduler.addRequest(appSyncRequest);
        }
    }

    public boolean confirmDeletes(Enumeration sourceNameList) {

        String sourceNames = getListOfSourceNames(sourceNameList).toLowerCase();

        Log.info(TAG_LOG, "Prompting user for delete confirmation");

        String message = localization.getLanguage("dialog_delete1") + " " + sourceNames
                + localization.getLanguage("dialog_delete2");

        boolean result = controller.getDialogController().askYesNoQuestion(message, false);

        if (result) {
            Log.info(TAG_LOG, "Continuing with sync");
        } else {
            Log.info(TAG_LOG, "User opted to cancel sync - " + sourceNames);
        }

        return result;
    }

    private String getListOfSourceNames(Enumeration sourceNameList) {
        StringBuffer sourceNames = new StringBuffer();

        int x = 0;
        AppSyncSource appSource = (AppSyncSource)sourceNameList.nextElement();

        while(appSource != null) {

            String name = appSource.getName();
            appSource = (AppSyncSource)sourceNameList.nextElement();

            if (x > 0) {
                sourceNames.append(", ");
                if (appSource == null) {
                    sourceNames.append(localization.getLanguage("dialog_and").toLowerCase());
                }
            }

            sourceNames.append(name);
        }

        return sourceNames.toString();
    }

    protected void showMessage(String msg) {
        controller.getDialogController().showMessage(screen, msg);
    }

    protected void showMessage(String msg, int delay) {
        controller.getDialogController().showMessage(screen, msg, delay);
    }

    // ConnectionListener implementation
    
    public boolean isConnectionConfigurationAllowed(final String apn) { 
        String message = localization.getLanguage("message_APN_question_1") + " " + apn + " "
                + localization.getLanguage("message_APN_question_2");
        return controller.getDialogController().askAcceptDenyQuestion(message, true);
    }

    public void noCredentials() {
        showMessage(localization.getLanguage("message_login_required"));

    }

    public void noSources() {
        showMessage(localization.getLanguage("message_nothing_to_sync"));

    }

    public void noConnection() {
        showMessage(localization.getLanguage("message_radio_off"));

    }

    public void noSignal() {
        showMessage(localization.getLanguage("message_no_signal"));

    }

    public void setCancel(boolean value) {
        doCancel = value;
    }

    /**
     * Check if the current sync should be cancelled
     * 
     * @return
     */
    public boolean isCancelled() {
        return doCancel;
    }

    // TODO FIXME MARCO: call this method
    public void setIsUserConfirmationNeeded(boolean value) {
        isUserConfirmationNeeded = value;
    }

    public void beginSync() {
        clearErrors();
        if (isUserConfirmationNeeded) {
            Log.debug(TAG_LOG, "Setting connection listener for this application");
            // TODO FIXME MARCO
            //ConnectionManager.getInstance().setConnectionListener(this);
        } else {
            Log.debug(TAG_LOG, "Using Default BasicConnectionListener");
        }
        setCancel(false);
    }


    public boolean syncStarted(Vector sources) {
        Log.trace(TAG_LOG, "syncStarted");
        if (customization.checkForUpdates()) {
            boolean isRequired = controller.checkForUpdate();
            if (isRequired) {
                return false;
            }
        }

        return true;

    }

    public void endSync(Vector sources, boolean hadErrors) {

        setCancel(false);

        // Disable the retry poller if not null
        if(retryPoller != null) {
            retryPoller.disable();
            retryPoller = null;
        }

        // If we had a CONNECTION BLOCKED BY THE USER error (user does not allow
        // any network configuration) then we show an error because the user
        // had already interacted with the app for this sync
        if (hadErrors && showTCPAlert) {
            controller.toForeground();
            Log.debug(TAG_LOG, "showing tcp settings alert!");
            showMessage(localization.getLanguage("message_enter_TCP_settings"));
        }

        // if we had a connectivity error, we bring the app to foreground
        // iff the user started the sync by hand
        if (logConnectivityError) {
            // TODO FIXME MARCO
            /*
            if (configuration.SYNC_MODE_MANUAL.equals(syncType)) {
                Log.error(TAG_LOG, "Manual sync: Connection attempts failed.");
                controller.toForeground();
            } else {
                if (!retry(sources)) {
                    // Do not show any error to the user, just log the fact
                    Log.error(TAG_LOG, "Automatic sync(" + syncType + ") failed after retrying");
                }
            }
            */
        }

        // if we had at least one device full error, we must show a popup error
        if (deviceFullSources != null) {
            StringBuffer sourceNames = new StringBuffer("");
            for(int i=0;i<deviceFullSources.size();++i) {
                AppSyncSource appSource = (AppSyncSource)deviceFullSources.elementAt(i);
                Log.error(TAG_LOG, "Server full for source " + appSource.getName());

                boolean shown = appSource.getConfig().getDeviceFullShown();
                // The popup is always shown on manual syncs and only the first
                // time if the sync is automatic
                if (!shown || MANUAL.equals(syncType)) {
                    if (sourceNames.length() > 0) {
                        sourceNames.append(",");
                    }
                    sourceNames.append(appSource.getName().toLowerCase());
                    // Remember that we have shown the error to the user. We
                    // won't show this popup again until a sync succeeds
                    appSource.getConfig().setDeviceFullShown(true);
                    appSource.getConfig().commit();
                }
            }
            if (sourceNames.length() > 0) {
                controller.toForeground();
                String msg = localization.getLanguage("dialog_server_full");
                msg = StringUtil.replaceAll(msg, "__source__", sourceNames.toString());
                showMessage(msg, 10000);
            }
        }

        // We reset these errors because this sync is over (if we are retrying,
        // we must consider the new one with no errors)
        logConnectivityError = false;
        showTCPAlert = false;
    }

    public void sourceStarted(AppSyncSource appSource) {
        Log.trace(TAG_LOG, "sourceStarted " + appSource.getName());
        currentSource = appSource;
        UISyncSourceController sourceController = appSource.getUISyncSourceController();
        if (sourceController != null) {
            sourceController.setSelected(true, false);
        }
        
        if (currentSource.getSyncSource().getConfig().getSyncMode()==SyncML.ALERT_CODE_REFRESH_FROM_SERVER) {
            refreshClientData(appSource, sourceController);
        }
        Log.trace(TAG_LOG, "sourceStarted " + currentSource);
    }

    public void sourceEnded(AppSyncSource appSource) {
        Log.trace(TAG_LOG, "sourceEnded " + appSource.getName());
        currentSource = null;

        // Set synced source
        appSource.getConfig().setSynced(true);
        
        // If a source sync ends successfully we reset the device full property
        appSource.getConfig().setDeviceFullShown(false);
        
        saveSourceConfig(appSource);
        
        UISyncSourceController sourceController = appSource.getUISyncSourceController();
        if (sourceController != null) {
            sourceController.setSelected(false, false);
        }
    }

    public void sourceFailed(AppSyncSource appSource, SyncException e) {

        Log.trace(TAG_LOG, "sourceFailed");

        int code = e.getCode();
        if (   code == SyncException.READ_SERVER_RESPONSE_ERROR
            || code == SyncException.WRITE_SERVER_REQUEST_ERROR
            || code == SyncException.CONN_NOT_FOUND) {

            logConnectivityError = true;
        } else if (code == SyncException.CONNECTION_BLOCKED_BY_USER) {
            showTCPAlert = true;
        } else if (code == SyncException.DEVICE_FULL) {
            if (deviceFullSources == null) {
                deviceFullSources = new Vector();
            }
            deviceFullSources.addElement(appSource);
        }
    }

    public String getRemoteUri(AppSyncSource appSource) {
        SourceConfig config = appSource.getSyncSource().getConfig();
        return config.getRemoteUri();
    }

    public void serverOperationFailed() {
        showMessage(localization.getLanguage("message_not_send_to_server"));
    }

    public Controller getController() {
        return controller;
    }

    public void clearErrors() {
        // TODO FIXME MARCO
        //controller.clearErrors();
        showTCPAlert = false;
        logConnectivityError = false;
        deviceFullSources = null;
    }

    public void connectionOpened() {
        Log.debug(TAG_LOG, "Connection opened");
    }

    public void requestWritten() {
        Log.debug(TAG_LOG, "Request written");
    }

    public void responseReceived() {
        Log.debug(TAG_LOG, "Response received");
    }

    public void connectionClosed() {
        Log.debug(TAG_LOG, "Connection closed");
    }

    public void connectionConfigurationChanged() {
        Log.debug(TAG_LOG, "Configuration changed");
    }

    protected void setScreen(Screen screen) {
        this.screen = screen;
    }

    protected SyncEngine createSyncEngine() {
        return new SyncEngine(customization, configuration, appSyncSourceManager, null);
    }

    private void saveSourceConfig(AppSyncSource appSource) {
        appSource.getConfig().saveSourceSyncConfig();
        appSource.getConfig().commit();
    }

    private boolean retry(Vector sources) {

        boolean willRetry = false;

        if (retryPoller != null) {
            retryPoller.disable();
        }

        if (scheduledAttempt < 3) {
            scheduledAttempt++;
            Log.error(TAG_LOG, "Scheduled sync: Connection attempt failed. " + "Try again in "
                    + RETRY_POLL_TIME + " minutes");

            retryPoller = new Poller(this, RETRY_POLL_TIME, true, false);
            retryPoller.start();
            willRetry = true;
        } else {
            retryPoller = null;
            scheduledAttempt = 0;
        }
        return willRetry;
    }

    private class RequestHandler extends Thread {

        private boolean stop = false;

        public RequestHandler() {
        }

        public void run() {
            Log.info(TAG_LOG, "Starting request handler");
            while (!stop) {
                try {
                    synchronized (appSyncRequestArr) {
                        appSyncRequestArr.wait();
                        syncScheduler.addRequest(appSyncRequestArr[0]);
                    }
                } catch (Exception e) {
                    // All handled exceptions are trapped below, this is just a
                    // safety net for runtime exception because we don't want
                    // this thread to die.
                    Log.error(TAG_LOG, "Exception while performing a programmed sync " + e.toString());
                }
            }
        }
    }

    private class FirstSyncRequest {
        public AppSyncSource dialogDependentSources[];
        public String syncType;
        public Vector filteredSources;
        public boolean refresh;
        public int direction;
        public int delay;
        public boolean fromOutside;
        public int numSources;
        public int sourceIndex;
    }


    private void refreshClientData(AppSyncSource appSource, UISyncSourceController controller) {
        // TODO FIXME: MARCO (delete items and notify the UI)
        /*
        if (appSource.getSyncSource() instanceof BBPIMSyncSource) {
            try {
                BBPIMSyncSource bpss = (BBPIMSyncSource) appSource.getSyncSource();

                PIMItemHelper pih = bpss.getHelper();

                Enumeration items = pih.getItemsList();

                //Notify the ui that items are being deleted from the client
                controller.removingAllData();

                //count the PIMList elements
                int size = 0;
                while (items.hasMoreElements()) {
                    PIMItem item = (PIMItem) items.nextElement();
                    size++;
                }

                //remove the PIMList elements
                items = pih.getItemsList();
                int i = 0;
                while (items.hasMoreElements()) {
                    if (doCancel) {
                        // The sync has not started yet, so we must synthetize a
                        // report here
                        SyncReport report = new SyncReport(bpss);
                        report.setSyncStatus(SyncListener.CANCELLED);
                        controller.endSession(report);
                        
                        //Reset anchors if a cancel action was performed
                        //after at least 1 item was cancelled
                        if (i>0) {
                            bpss.getConfig().setLastAnchor(0);
                            bpss.getConfig().setLastAnchor(0);
                            bpss.resetTrackingData();
                        }

                        return;
                    }
                    i++;
                    PIMItem item = (PIMItem) items.nextElement();
                    pih.deleteItem(item);

                    //Notify the UI
                    controller.itemRemoved(i, size);
                }
            } catch (PIMException ex) {
                Log.error(TAG_LOG, "[refreshClientData]Cannot delete device item" + ex);
            }
        }
        */
    }
}
