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

package com.funambol.client.engine;

import java.util.Vector;
import java.util.Enumeration;

import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.configuration.Configuration;
import com.funambol.client.customization.Customization;

import com.funambol.platform.NetworkStatus;
import com.funambol.push.SyncSchedulerListener;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.spds.CompressedSyncException;
import com.funambol.syncml.spds.SyncConfig;
import com.funambol.syncml.spds.SyncException;
import com.funambol.syncml.spds.SyncManager;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.syncml.protocol.SyncMLStatus;
import com.funambol.util.BasicConnectionListener;
import com.funambol.util.Log;
import com.funambol.util.StringUtil;
import com.funambol.util.TransportAgent;

/**
 * This class represents an engine for synchronizations. It wraps the APIs and
 * in particular it is built on top of the SyncScheduler. This class has the
 * following main goals:
 *
 * 1) Perform some basic checks before firing a sync. For example it checks if
 *    radio signal is good. These checks are platform dependent and not
 *    performed by the APIs
 * 2) Incapsulate error handling and sync threading. When a sync is requested it
 *    is run in a separate thread, and this thread is monitored by the
 *    SyncEngine which intercepts exceptions and handle them
 * 3) Support compression error recovering. If a sync throws an error because
 *    compression is not supported, then the sync is resumed without compression
 * 4) Add a listener for the entire sync. Each sync source has its own listener
 *    for source specific events, but the SyncEngine has a different listener
 *    that generats events global to the entire synchronization.
 *
 */
public class SyncEngine implements SyncSchedulerListener {

    private static final String TAG_LOG = "SyncEngine";

    private SyncEngineListener              listener = null;
    protected Customization                 customization = null;
    protected AppSyncSourceManager          appSyncSourceManager = null;
    protected Configuration                 configuration = null;
    private boolean                         isSynchronizing = false;
    private AppSyncSource                   currentSource   = null;
    private Vector                          appSourcesRequest = new Vector();
    private SyncThread                      syncThread;
    private NetworkStatus                   networkStatus;
    private boolean                         spawnThread = true;
    private TransportAgent                  customTransportAgent = null;

    public SyncEngine(Customization customization, Configuration configuration,
                      AppSyncSourceManager appSyncSourceManager, NetworkStatus networkStatus)
    {
        this.customization = customization;
        this.configuration = configuration;
        this.appSyncSourceManager = appSyncSourceManager;
        this.networkStatus = networkStatus;
    }

    public void setListener(SyncEngineListener listener) {
        this.listener = listener;
    }

    /**
     * Gets the current listener.
     */
    public SyncEngineListener getListener() {
        return listener;
    }

    public void setSpawnThread(boolean value) {
        spawnThread = value;
    }

    public void setNetworkStatus(NetworkStatus networkStatus) {
        this.networkStatus = networkStatus;
    }

    public void cancelSync() {
        Log.info(TAG_LOG, "Cancelling sync");
        if (isSynchronizing && syncThread != null) {
            Log.debug(TAG_LOG, "Cancelling sync on sync thread");
            syncThread.cancelSync();
        }
    }

    public void setTransportAgent(TransportAgent ta) {
        this.customTransportAgent = ta;
    }

    /**
     * SyncSchedulerListener callback. This method is invoked when a sync
     * programmed in the SyncScheduler shall be fired.
     */
    public void sync(Object[] requestContent){
        appSourcesRequest.removeAllElements();
        for (int i =0; i<requestContent.length; i++){
            appSourcesRequest.addElement(requestContent[i]);
        }
        synchronize(appSourcesRequest);
    }

    /**
     * Returns true iff a sync is in progress
     */
    public boolean isSynchronizing(){
        return isSynchronizing;
    }

    /**
     * Returns the source which is currently being synchronized. This method
     * returns a non null value only if isSynchronizing returns true. The method
     * may return null even during a synchronization. In particular a
     * synchronization is requested and this immediately triggers the
     * "isSynchronizing" but the currentSource is not set until the source
     * really starts synchronizing. Users must be ready to handle null return
     * values.
     */
    public AppSyncSource getCurrentSource() {
        return currentSource;
    }

    public boolean synchronize(Vector sources) {

        Log.info(TAG_LOG, "synchronize");

        isSynchronizing = true;

        if (listener != null) {
            listener.beginSync();
        }

        if (   configuration.getUsername() == null || configuration.getUsername().length() == 0
            || configuration.getPassword() == null || configuration.getPassword().length() == 0) {

            if (listener != null) {
                listener.noCredentials();
            }
            syncEnded();
            return false;
        }

        // Safety check. If there are no ready to use sources the sync is
        // stopped. This case should be captured earlier in the flow, but
        // just in case....
        if (appSyncSourceManager.numberOfEnabledAndWorkingSources() == 0) {
            if (listener != null) {
                listener.noSources();
            }
            syncEnded();
            return false;
        }

        if (networkStatus != null && !networkStatus.isConnected()) {
            if (networkStatus.isRadioOff()) {
                if (listener != null) {
                    listener.noConnection();
                }
            } else {
                if (listener != null) {
                    listener.noSignal();
                }
            }
            syncEnded();
            return false;
        }

        Vector checkSources = new Vector();

        for (int x = 0; x < sources.size(); x++) {
            AppSyncSource appSource = (AppSyncSource) sources.elementAt(x);
            SyncSource    source    = appSource.getSyncSource();
            int mode = source.getConfig().getSyncMode();
            if (   mode != SyncML.ALERT_CODE_REFRESH_FROM_CLIENT
                && mode != SyncML.ALERT_CODE_REFRESH_FROM_SERVER) {
                checkSources.addElement(source);
            }
        }

        if (customization.warnOnDeletes()) {
            Vector sourcesToRemove = checkDeletes(checkSources);
            for (int x = 0; x < sourcesToRemove.size(); x++) {
                sources.removeElement(sourcesToRemove.elementAt(x));
                x--;
            }
        }

        if (sources.size() == 0) {
            if (listener != null) {
                listener.noSources();
            }
            syncEnded();
            return false;
        } else {
            if (listener != null && listener.isCancelled()) {
                syncEnded();
                return false;
            }
            syncThread = new SyncThread(sources);
            if (spawnThread) {
                syncThread.setPriority(Thread.MIN_PRIORITY);
                syncThread.start();
            } else {
                syncThread.sync();
            }
        }// end if

        return true;
    }

    /**
     * Called to warn the user of a large number of deletes
     * 
     * @param sources list of AppSyncSources for which we shall warn the user
     * @returns Whether or not to continue with the sync
     */
    private boolean warnDelete(Enumeration sources) {
        boolean result = true;
        if (listener != null) {
            result = listener.confirmDeletes(sources);
        }
        return result;
    }

    /**
     * Utility method to be invoked at the end of a sync. The method resets all
     * the necessary interal variables and invoke the listener.
     */
    private void syncEnded() {
        isSynchronizing = false;
        currentSource = null;
        if (listener != null) {
            listener.syncEnded();
        }

        // Save the latest authentication config
        if (configuration != null) {
            if (configuration.getSyncConfig() != null) {
                configuration.setClientNonce(configuration.getSyncConfig().clientNonce);
            }
            configuration.save();
        }

        syncThread = null;
    }


    /**
     * Check to see if the user should be warned, and if so, warn them.
     * 
     * @param mask
     */
    private Vector checkDeletes(Vector sources) {

        boolean toWarn = false;
        Vector warnSources = new Vector();

        for (int x = 0; x < sources.size(); x++) {
            AppSyncSource appSource = (AppSyncSource)sources.elementAt(x);
            // TODO FIXME MARCO
            /*
            if (appSource.shouldWarnOnDeletes()) {
                toWarn = true;
                warnSources.addElement(appSource);
            }
            */
        }

        if (toWarn) {
            if (!warnDelete(warnSources.elements())) {
                return sources;
            }
        }

        return new Vector();
    }

    // TODO FIXME: call/reimplement this (for BB)
    protected SyncManager createManager(AppSyncSource source, SyncConfig config) {
        SyncManager sm = new SyncManager(config);
        if(customTransportAgent != null) {
            sm.setTransportAgent(customTransportAgent);
        }
        return sm;
    }

    private class SyncThread extends Thread {

        private final   Vector appSources;
        private boolean compressionRetry;
        private SyncConfig   syncConfig;
        private SyncManager manager;

        public SyncThread(Vector sources) {
            this.appSources       = sources;
            this.compressionRetry = false;
        }

        public SyncConfig getSyncConfig() {
            return syncConfig;
        }

        public void cancelSync() {
            Log.info(TAG_LOG, "Cancelling sync");
            if (manager != null) {
                manager.cancel();
            }
        }

        public void run() {
            sync();
        }

        public synchronized void sync() {

            Log.info(TAG_LOG, "SyncThread.run");

            syncConfig = configuration.getSyncConfig();
            if (compressionRetry) {
                // We are trying without compression, make sure compression is
                // really disabled
                syncConfig.compress = false;
            }
            if (listener != null) {
                listener.syncStarted(appSources);
            }
            Vector failedSources = new Vector();

            try {
                failedSources = synchronize();
            } catch (CompressedSyncException e) {

                if (!compressionRetry) {
                    // Only retry because of compression once
                    Log.info(TAG_LOG, "Sync failed because compression failed - Retrying");
                    compressionRetry = true;

                    // Recurse
                    this.run();
                    return;
                }
            } catch (Throwable e) {
                // This is unexpected, but we don't want the app to die
                // The finally block will take care of updating the status of
                // the failed sources. Since this case is unexpected we do not
                // rely on the failedSource returned value but we signal all the
                // sources as failed
                failedSources = appSources;
                Log.error(TAG_LOG, "Exception caught during synchronization " + e.toString());
                e.printStackTrace();
            } finally {
                syncEnded();
            }
        }

        /**
         * The main procedure for the sync thread
         * 
         * @throws Exception
         */
        private Vector synchronize() throws Exception {

            Log.info(TAG_LOG, "synchronize");

            Vector failedSources = new Vector();

            for (int x = 0; x < appSources.size(); x++) {

                if (listener != null && listener.isCancelled()) {
                    // If the sync got cancelled then we must exit the loop
                    // Even if the user cancel the sync the SyncSource may be
                    // unable to throw the proper exception if other errors
                    // (such as network error) kick in. So we must recheck here
                    // and stop the sync if necessary
                    break;
                }

                AppSyncSource appSource = (AppSyncSource)appSources.elementAt(x);
                SyncSource source = appSource.getSyncSource();

                Log.info(TAG_LOG, "Firing sync for source " + appSource.getName());

                // We need to create one manager for each source
                manager = createManager(appSource, syncConfig);

                if (listener != null) {
                    listener.sourceStarted(appSource);
                }

                try {
                    // Set this source as the one currently synchronized
                    currentSource = appSource;
                    // TODO not always necessary
                    manager.setFlagSendDevInf();
                    
                    //This sync could have been cancelled when the client was
                    //deleting the device items during a refresh from server operation
                    if (listener == null || (!listener.isCancelled())) {
                        //If we have a credentials check pending, we force to
                        //query for server caps
                        boolean askServerCaps = configuration.getCredentialsCheckPending() ||
                                                configuration.getForceServerCapsRequest();
                        Log.debug(TAG_LOG, "Asking for server caps: " + askServerCaps);
                        fireSync(manager, source, source.getConfig().getSyncMode(), askServerCaps);
                    }

                    currentSource = null;

                    if (source.getStatus() != SyncSource.STATUS_SUCCESS) {
                        failedSources.addElement(appSource);
                    } 

                    // If we get here, it means that the sync was succesfull and
                    // we can update the anchors. A corner case is that a cancel 
                    // operation has been performed when the client was removing
                    // all data during a refresh from server operation. In that 
                    // case the sync was never started but a slow sync must take 
                    // place next time in order not to have intem deleted whitin
                    // a reset operation to be sent to the server on the next 
                    // fast sync
                    if (listener != null) {
                        listener.sourceEnded(appSource);
                    }
                } catch (final Exception e) {

                    boolean compressError = (e instanceof CompressedSyncException);
                    if (compressError) {
                        if (!compressionRetry) {
                            Log.info(TAG_LOG, "Retrying without compression");
                        }

                        throw new CompressedSyncException(e.getMessage());
                    }

                    SyncException se;
                    if (e instanceof SyncException) {
                        se = (SyncException)e;
                    } else {
                        se = new SyncException(SyncException.CLIENT_ERROR, e.toString());
                    }

                    if (listener != null) {
                        listener.sourceFailed(appSource, se);
                    }

                    // After this point, we know the source really
                    // failed

                    failedSources.addElement(appSource);

                    // Depending on the reason why the sync failed, we may
                    // want to abort the other sources as well (e.g. invalid
                    // credentials)
                    if (se.getCode() == SyncException.AUTH_ERROR ||
                        se.getCode() == SyncException.FORBIDDEN_ERROR ||
                        se.getCode() == SyncException.CANCELLED) {

                        break;
                    }
                } finally {
                    source = null;
                    currentSource = null;
                }
            }

            // -- END OF A SYNCHRONIZATION
            if (listener != null) {
                listener.endSync(appSources, failedSources.size() > 0);
            }

            return failedSources;
        }
    }

    protected void fireSync(SyncManager manager, SyncSource source, int syncMode, boolean askServerCaps) {
        manager.sync(source, syncMode, askServerCaps);
    }

}
