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

package com.funambol.client.controller;

import java.util.Enumeration;
import java.util.Date;

import com.funambol.client.customization.Customization;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.ui.UISyncSource;
import com.funambol.client.ui.Bitmap;
import com.funambol.client.localization.Localization;
import com.funambol.util.Log;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.syncml.spds.SyncListener;
import com.funambol.syncml.spds.SyncReport;
import com.funambol.syncml.protocol.DevInf;


public class UISyncSourceController implements SyncListener {

    private static final String TAG_LOG = "UISyncSourceController";

    private Localization         localization = null;
    private Customization        customization = null;
    private AppSyncSourceManager appSyncSourceManager = null;
    private Controller           controller = null;
    private UISyncSource         uiSource   = null;
    private AppSyncSource        appSource  = null;

    private int              totalSent;
    private int              totalSending;
    private int              totalReceived;
    private int              totalReceiving;
    private long             currentItemSize = 0;
    private long             currentItemSentSize = 0;

    private Bitmap           statusIcon = null;
    private Bitmap           statusSelectedIcon = null;
    private Bitmap           okIcon = null;
    private Bitmap           errorIcon = null;

    private SyncingAnimation animation = null;
    private SyncReport       lastSyncReport = null;

    private boolean          cancelling = false;
    private boolean          syncing    = false;

    private long             syncStartedTimestamp = 0;

    public UISyncSourceController(Customization customization, Localization localization,
                                  AppSyncSourceManager appSyncSourceManager,
                                  Controller controller, AppSyncSource appSource) {

        this.customization = customization;
        this.localization  = localization;
        this.appSyncSourceManager = appSyncSourceManager;
        this.controller = controller;
        this.appSource = appSource;

        okIcon = customization.getOkIcon();
        errorIcon = customization.getErrorIcon();
        statusSelectedIcon = customization.getStatusSelectedIcon();

        // Create the animation object
        animation = new SourceSyncingAnimation();
    }

    public void setUISyncSource(UISyncSource uiSource) {
        this.uiSource = uiSource;
        if (uiSource != null) {
            String lastStatus;

            if (!appSource.isWorking()) {
                lastStatus = localization.getLanguage("home_not_available");
                uiSource.setEnabled(false);
            } else if (!appSource.getConfig().getEnabled()) {
                lastStatus = localization.getLanguage("home_disabled");
                uiSource.setEnabled(false);
            } else {
                int status = appSource.getConfig().getLastSyncStatus();
                if (status == SyncListener.COMPRESSED_RESPONSE_ERROR) {
                    return;
                }
                lastStatus = getLastSyncStatus(status, null);
                statusIcon   = getLastSyncIcon(status);
                if (statusIcon != null) {
                    uiSource.setStatusIcon(statusIcon);
                }
                uiSource.setEnabled(true);
            }
            uiSource.setStatusString(lastStatus);
            uiSource.redraw();
        }
    }

    public boolean isSyncing() {
        return syncing;
    }

    public void disableStatusAnimation() {
        if(animation != null) {
            animation.stopAnimation();
        }
        animation = null;
    }

    public void enableStatusAnimation() {
        if(animation == null) {
            animation = new SourceSyncingAnimation();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#dataReceived(java.lang.String, int)
     */
    public void dataReceived(String date, int size) {
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#endConnecting(int)
     */
    public void endConnecting(int action) {
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#endMapping()
     */
    public void endMapping() {
        if (uiSource != null) {
            if (!cancelling) {
                uiSource.setStatusString(localization.getLanguage("status_mapping_done"));
                uiSource.redraw();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#endReceiving()
     */
    public void endReceiving() {
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#endSending()
     */
    public void endSending() {
    }

    public void disable() {
        if (uiSource != null) {
            String status;
            if (!appSource.isWorking()) {
                status = localization.getLanguage("home_not_available");
            } else {
                status = localization.getLanguage("home_disabled");
            }
            uiSource.setStatusString(status);
            Bitmap sourceIcon = customization.getSourceDisabledIcon(appSource.getId());
            if (sourceIcon != null) {
                uiSource.setIcon(sourceIcon);
            }
            uiSource.setStatusIcon(null);
            uiSource.setEnabled(false);
            uiSource.redraw();
        }
    }

    public void enable() {
        if (uiSource != null) {
            AppSyncSource appSource = uiSource.getSource();
            int status = appSource.getConfig().getLastSyncStatus();
            if (status==SyncListener.COMPRESSED_RESPONSE_ERROR) {
                return;
            }
            uiSource.setStatusString(getLastSyncStatus(status, null));
            Bitmap sourceIcon = customization.getSourceIcon(appSource.getId());
            if (sourceIcon != null) {
                uiSource.setIcon(sourceIcon);
            }
            statusIcon = getLastSyncIcon(status);
            if (statusIcon != null) {
                uiSource.setStatusIcon(statusIcon);
            }
            uiSource.setEnabled(true);
            uiSource.redraw();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#endSession(int)
     */
    public void endSession(SyncReport report) {

        if (!syncing) {
            return;
        }

        Log.trace(TAG_LOG, "endSession");

        lastSyncReport = report;
        int status = report.getSyncStatus();

        if (Log.getLogLevel() >= Log.INFO) {
            Log.info(report.toString());
        }

        // Stop any animation in progress
        if (animation != null) {
            animation.stopAnimation();
        }
        
        //The following condition is made to trap the compression error when a 
        //wap compression error occur. 
        //Notice that this change introduce a dependency on the class SyncEngine 
        //and it can happen that the status is not correctly update the http 
        //compression is disabled.
        if (status==SyncListener.COMPRESSED_RESPONSE_ERROR) {
            //This error is the result for a problem reading the compressed 
            //stream. In this case the sync client retries to send
            //an uncompressed request
            Log.error(TAG_LOG, "Compressed Header Error");
            return;
        }

        // This source sync is over, set the proper status
        if (uiSource != null) {
            String statusMsg = getLastSyncStatus(status, report);
            statusIcon   = getLastSyncIcon(status);
            uiSource.setStatusString(statusMsg);
            if (statusIcon != null) {
                uiSource.setStatusIcon(statusIcon);
            }
            uiSource.syncEnded();
        }
        // set the status into the app source
        appSource.getConfig().setLastSyncStatus(status);
        appSource.getConfig().setLastSyncTimestamp(syncStartedTimestamp);
        appSource.getConfig().commit();
        
        if (uiSource != null) {
            uiSource.redraw();
        }
        cancelling = false;
        syncing = false;
    }
    
    /**
     * Resets the current status
     */
    public void resetStatus() {

        // Stop any animation in progress
        if (animation != null) {
            animation.stopAnimation();
        }
        int status = appSource.getConfig().getLastSyncStatus();
        String lastStatus = getLastSyncStatus(status, null);
        statusIcon = getLastSyncIcon(status);
        uiSource.setStatusIcon(statusIcon);
        uiSource.setStatusString(lastStatus);
        uiSource.redraw();

        cancelling = false;
        syncing = false;
    }

    public void setSelected(boolean value, boolean fromUi) {
        if (uiSource != null && !cancelling) {
            // Sets the proper icon (if the source is enabled)
            if (appSource.getConfig().getEnabled()) {
                if (customization.showSyncIconOnSelection()) {
                    if (value) {
                        uiSource.setStatusIcon(statusSelectedIcon);
                    } else {
                        uiSource.setStatusIcon(statusIcon);
                    }
                } else {
                    uiSource.setStatusIcon(statusIcon);
                }
            }
            uiSource.setSelection(value, fromUi);
            uiSource.redraw();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#endSyncing()
     */
    public void endSyncing() {
    }

    /*
     * (non-Javadoc)
     */
    public void itemAddSendingEnded(String key, String parent, int size) {
    }

    public void itemAddSendingStarted(String key, String parent, int size) {
        startSending(key, size);
    }

    public void itemAddChunkSent(String key, String parent, int size) {
        chunkSent(key, size);
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#itemDeleteSent(java.lang.Object)
     */
    public void itemDeleteSent(Object syncItem) {
        startSending(((SyncItem)syncItem).getKey(), 0);
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#itemDeleted(java.lang.Object)
     */
    public void itemDeleted(Object itemId) {
        receivedChange();

    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#itemReceived(java.lang.Object)
     */
    public void itemReceived(Object arg0) {
        receivedChange();

    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#itemReplaceSent(java.lang.Object)
     */
    public void itemReplaceSendingStarted(String key, String parent, int size) {
        startSending(key, size);
    }

    public void itemReplaceSendingEnded(String key, String parent, int size) {
    }

    public void itemReplaceChunkSent(String key, String parent, int size) {
        chunkSent(key, size);
    }


    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#itemUpdated(java.lang.Object)
     */
    public void itemUpdated(Object arg0) {
        receivedChange();
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#itemUpdated(java.lang.Object,
     * java.lang.Object)
     */
    public void itemUpdated(Object item, Object update) {
        receivedChange();
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#startConnecting()
     */
    public void startConnecting() {
        if (uiSource != null) {
            if (animation != null && !animation.isRunning()) {
                animation.startAnimation();
            }
            if (!cancelling) {
                uiSource.setStatusString(localization.getLanguage("status_connecting"));
                uiSource.redraw();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#startMapping()
     */
    public void startMapping() {
        if (uiSource != null) {
            if (!cancelling) {
                uiSource.setStatusString(localization.getLanguage("status_mapping"));
                uiSource.redraw();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#startReceiving(int)
     */
    public void startReceiving(int numItems) {
        if (totalReceiving == ITEMS_NUMBER_UNKNOWN) {
            totalReceiving = numItems;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#startSending(int, int, int)
     */
    public void startSending(int numNewItems, int numUpdItems, int numDelItems) {
        totalSending = numNewItems + numUpdItems + numDelItems;
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#startSession()
     */
    public void startSession() {
        Log.debug(TAG_LOG, "startSession");
        totalReceiving = ITEMS_NUMBER_UNKNOWN;
        totalReceived = 0;
        totalSent = 0;
        totalSending = 0;
        cancelling = false;
        // It is possible that this method gets invoked more than once. This is
        // the case because it is invoked by SyncManager but also by the
        // HomeScreenController.
        if (uiSource != null && !syncing) {
            uiSource.syncStarted();
        }
        syncStartedTimestamp = new Date().getTime();
        syncing = true;
    }

    public void attachToSession() {
        Log.info(TAG_LOG, "Attaching to session");
        syncing = true;
        if (uiSource != null) {
            uiSource.syncStarted();
            String text = localization.getLanguage("status_connecting");
            uiSource.setStatusString(text);
            if (animation != null && !animation.isRunning()) {
                animation.startAnimation();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#startSyncing(int, DevInf)
     */
    public boolean startSyncing(int mode, DevInf devInf) {
        if (mode == com.funambol.syncml.protocol.SyncML.ALERT_CODE_SLOW) {
            if (customization.confirmSlowSync()) {
                String text = localization.getLanguage("status_confirm_slow");
                if (uiSource != null) {
                    uiSource.setStatusString(text);
                    uiSource.redraw();
                }
                if (!confirmSlowSync()) {
                    abortSlow();
                    return false;
                }
            }
        }

        // If the server sends its capabilities, we must decode them and update
        // the configuration accordingly
        if (devInf != null) {
            Log.info(TAG_LOG, "Server sent its capabilities");
            controller.reapplyServerCaps(devInf);
        }

        return true;
    }

    public void startCancelling() {
        if (uiSource != null) {
            uiSource.setStatusString(localization.getLanguage("status_cancelling"));
            uiSource.redraw();
        }
        cancelling = true;
    }

    /*
     * (non-Javadoc)
     * @see com.funambol.util.SyncListener#syncStarted(int)
     */
    public void syncStarted(int arg0) {
    }

    public void removingAllData() {
        if (uiSource != null) {
            if (animation != null && !animation.isRunning()) {
                animation.startAnimation();
            }
            uiSource.setStatusString(localization.getLanguage("status_recover"));
            uiSource.redraw();
        }
    }

    public void itemRemoved(int current, int size) {
        StringBuffer sb = new StringBuffer(localization.getLanguage("status_removing_item"));
        sb.append(" ").append(current);
        sb.append("/").append(size);
        Log.trace(TAG_LOG, "notifyRemoved " + sb.toString());
        if (uiSource != null) {
            if (!cancelling) {
                uiSource.setStatusString(sb.toString());
                uiSource.redraw();
            }
        }
    }
    
    public Controller getController() {
        return controller;
    }

    public SyncReport getLastSyncReport() {
        return lastSyncReport;
    }

    public void setAnimationIcons(Bitmap[] icons) {
        if (animation != null) {
            animation.setAnimationIcons(icons);
        }
    }

    private void receivedChange() {
        Log.trace(TAG_LOG, "receivedChange");

        totalReceived++;
        StringBuffer sb = new StringBuffer(localization.getLanguage("status_receiving_item"));
        sb.append(" ").append(totalReceived);
        if (totalReceiving > 0) {
            sb.append("/").append(totalReceiving);
        }
        if (uiSource != null) {
            if (!cancelling) {
                uiSource.setStatusString(sb.toString());
                uiSource.redraw();
            }
        }
    }

    private void chunkSent(String key, int size) {
        if (size != currentItemSize) {

            StringBuffer sb = new StringBuffer(localization.getLanguage("status_sending_item"));
            sb.append(" ").append(totalSent);

            if (totalSending > 0) {
                sb.append("/").append(totalSending);
            }

            // This is a LO
            // Compute the percentage of what we have sent so far
            long perc = (currentItemSentSize * 100) / currentItemSize;
            if (perc > 100) {
                perc = 100;
            }
            sb.append(" (").append(perc).append("%)");
            currentItemSentSize += size;

            if (uiSource != null) {
                if (!cancelling) {
                    uiSource.setStatusString(sb.toString());
                    uiSource.redraw();
                }
            }
        }
    }

    private void startSending(String key, int size) {
        totalSent++;
        currentItemSize = size;
        currentItemSentSize = 0;

        StringBuffer sb = new StringBuffer(localization.getLanguage("status_sending_item"));
        sb.append(" ").append(totalSent);

        if (totalSending > 0) {
            sb.append("/").append(totalSending);
        }

        if (uiSource != null) {
            if (!cancelling) {
                uiSource.setStatusString(sb.toString());
                uiSource.redraw();
            }
        }
    }


    private void changeSent(String key) {
    }

    private String createLastSyncedString(long anchor) {
    
        StringBuffer sb = new StringBuffer();
        long now = System.currentTimeMillis();
        long aday = 24 * 60 * 60 * 1000;
        long yesterday = now - aday;

        String todayDate = localization.getDate(now);
        String yesterdayDate = localization.getDate(yesterday);
        String anchorDate = localization.getDate(anchor);

        if (anchorDate.equals(todayDate)) {
            sb.append(localization.getLanguage("word_today"));
        } else if (anchorDate.equals(yesterdayDate)) {
            sb.append(localization.getLanguage("word_yesterday"));
        } else {
            sb.append(anchorDate);
        }
    
        sb.append(" ").append(localization.getLanguage("word_at")).append(" ");
    
        String time = localization.getTime(anchor);
        sb.append(time);
    
        return sb.toString();
    }

    private void abortSlow() {
    }

    private boolean confirmSlowSync() {

        Enumeration sources = appSyncSourceManager.getEnabledAndWorkingSources();
        String sourceNames = getListOfSourceNames(sources).toLowerCase();

        StringBuffer question = new StringBuffer();
        question.append(localization.getLanguage("dialog_slow_text1")).append(" ").append(
                sourceNames).append(localization.getLanguage("dialog_slow_text2"));

        return controller.getDialogController().askYesNoQuestion(question.toString(), true, 20000);
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

    private String getLastSyncStatus(int status, SyncReport report) {

        String res;
        switch (status) {
            case SyncListener.SUCCESS:
            {
                SyncSource source = appSource.getSyncSource();
                long lastSyncTS;
                if (source != null) {
                    lastSyncTS = appSource.getConfig().getLastSyncTimestamp();
                } else {
                    lastSyncTS = 0;
                }

                if (lastSyncTS > 0) {
                    res = localization.getLanguage("home_last_sync") + " "
                          + createLastSyncedString(lastSyncTS);
                } else {
                    res = localization.getLanguage("home_unsynchronized");
                }
                break;
            }
            case SyncListener.INVALID_CREDENTIALS:
                res = localization.getLanguage("status_invalid_credentials");
                break;
            case SyncListener.FORBIDDEN_ERROR:
                res = localization.getLanguage("status_forbidden_error");
                break;
            case SyncListener.READ_SERVER_RESPONSE_ERROR:
            case SyncListener.WRITE_SERVER_REQUEST_ERROR:
            case SyncListener.CONN_NOT_FOUND:
                if (report != null && (report.getNumberOfReceivedItems() > 0 || report.getNumberOfSentItems() > 0)) {
                    res = localization.getLanguage("status_partial_failure");
                } else {
                    res = localization.getLanguage("status_network_error");
                }
                break;
            case SyncListener.CONNECTION_BLOCKED_BY_USER:
                res = localization.getLanguage("status_connection_blocked");
                break;
            case SyncListener.CANCELLED:
                res = localization.getLanguage("status_cancelled");
                break;
            case SyncListener.DEVICE_FULL_ERROR:
                res = localization.getLanguage("status_quota_exceeded");

                /*
                if (report != null && (report.getNumberOfReceivedItems() > 0 || report.getNumberOfSentItems() > 0)) {
                    res = localization.getLanguage("status_partial_failure");
                } else {
                    res = localization.getLanguage("status_complete_failure");
                }*/
                break;
            default:
                if (report != null && (report.getNumberOfReceivedItems() > 0 || report.getNumberOfSentItems() > 0)) {
                    res = localization.getLanguage("status_partial_failure");
                } else {
                    res = localization.getLanguage("status_complete_failure");
                }
                break;
        }
        Log.debug(TAG_LOG, "getLastSyncStatus " + res);
        return res;
    }

    private Bitmap getLastSyncIcon(int status) {

        Bitmap res;
        if (status == SyncListener.SUCCESS) {
            SyncSource source = appSource.getSyncSource();
            long lastSyncTS = appSource.getConfig().getLastSyncTimestamp();

            if (lastSyncTS > 0) {
                res = okIcon;
            } else {
                res = null;
            }
        } else {
            res = errorIcon;
        }
        return res;
    }

    private class SourceSyncingAnimation extends SyncingAnimation {

        public SourceSyncingAnimation() {
            super(customization.getStatusIconsForAnimation());
        }

        protected void showBitmap(Bitmap bitmap) {
            uiSource.setStatusIcon(bitmap);
            uiSource.redraw();
        }
    }
}

