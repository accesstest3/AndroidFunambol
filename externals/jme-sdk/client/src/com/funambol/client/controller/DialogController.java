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

import java.util.Enumeration;
import java.util.Vector;

import com.funambol.client.localization.Localization;
import com.funambol.client.ui.DisplayManager;
import com.funambol.client.ui.Screen;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.AppSyncSourceManager;

import com.funambol.util.Log;

/**
 * Control dialog alert flow on the client using the client's controller and
 * DisplayManager. This class is just a controller. Refer to DisplayManager
 * implementation in order to manage the alert diplaying logic.
 */
public class DialogController {

    /** TAG to be displayed into log messages*/
    public static final String TAG_LOG = "DialogController";

    //--- Local instance fields fed by the constructor
    private Localization localization;
    private AppSyncSourceManager appSyncSourceManager;
    private Controller controller;
    private DisplayManager displayManager;

    //--- Current Alert Status related fields

    //Last selected direction to be used in case of resume of the reset type
    //alert dialog. The
    private int lastSelectedDirection;

    //Last first sync alert status use this field in order to resume itself
    //The field is built in the constructor and is updated whenever a first
    //sync dialog alert is prompted
    FirstSyncDialogState lastFirstSyncDialogState = new FirstSyncDialogState();

    //Last WI-Fi not available alert status use this field in order to resume itself
    //The field is built in the constructor and is updated whenever a first
    //sync dialog alert is prompted
    WifiNotAvailableDialogState lastWifiNotAvailableDialogState = new WifiNotAvailableDialogState();

    /**
     * Public constructor
     * @param displayManager the DisplayManager object to be used.
     * @param controller
     */
    public DialogController(DisplayManager displayManager, Controller controller) {
        this.displayManager = displayManager;
        this.controller = controller;
        this.localization = controller.getLocalization();
        this.appSyncSourceManager = controller.getAppSyncSourceManager();
        this.lastFirstSyncDialogState = new FirstSyncDialogState();
        this.lastWifiNotAvailableDialogState = new WifiNotAvailableDialogState();
        this.lastSelectedDirection = -1;
    }

    /**
     * Prompt a message alert on the screen
     * @param message is the String to be displayed on the screen
     * @return boolean true if the alert was displayed.
     */
    public boolean promptNext(String message) {
        return displayManager.promptNext(message);
    }

    /**
     * Not implemented.
     * @param message the message to be displayed
     * @param defaultyes the default parameter in the selection
     * @return boolean true is the user answered yes, false otherwise
     */
    public boolean askYesNoQuestion(String message, boolean defaultyes) {
        // TODO FIXME: to be implemented
        return false;
    }

    /**
     * Prompt an alert with 2 choices on the screen
     * @param message the message to be displayed
     * @param defaultyes the default parameter in the selection
     * @return boolean true is the user accepted, false otherwise
     */
    public boolean askAcceptDenyQuestion(String message, boolean defaultyes) {
        return displayManager.askAcceptDenyQuestion(message, defaultyes, -1);
    }

    /**
     * Not implemented.
     * @param message the message to be displayed
     * @param defaultyes the default parameter in the selection
     * @param timeToWait the time to wait before dismiss the alert
     * @return boolean true is the user answered yes, false otherwise
     */
    public boolean askYesNoQuestion(String message, boolean defaultyes, int timeToWait) {
        // TODO FIXME: to be implemented
        return false;
    }

    /**
     * Show an alert message on the screen
     * @param screen the dialog alert owner Screen
     * @param language the message to be dispalyed
     */
    public void showMessage(Screen screen, String language) {
        displayManager.showMessage(screen, language);
    }

    /**
     * Show an alert message on the screen for a given amount of time
     * @param screen the dialog alert owner Screen
     * @param language the message to be dispalyed
     * @param delay the duration of the message in milliseconds
     */
    public void showMessage(Screen screen, String language, int delay){
        displayManager.showMessage(screen, language, delay);
    }


    /**
     * Use the localization field to build the reset direction alert dialog.
     * This dialog builds ResetDirectionDialogOption objects to refer to the
     * dialog choiches.
     * @param screen the dialog alert owner Screen
     */
    public void showRefreshDirectionDialog(Screen screen) {
       DialogOption[] opt = new DialogOption[3];

        opt[0] = new ResetDirectionDialogOption(
                screen,
                localization.getLanguage("dialog_refresh_from"),
                SynchronizationController.REFRESH_FROM_SERVER);

        opt[1] = new ResetDirectionDialogOption(
                screen,
                localization.getLanguage("dialog_refresh_to"),
                SynchronizationController.REFRESH_TO_SERVER);

        opt[2] = new ResetDirectionDialogOption(
                screen, 
                localization.getLanguage("dialog_cancel"),
                -1);

        displayManager.promptSelection(screen, localization.getLanguage("dialog_refresh_which") + "\n"
                + localization.getLanguage("dialog_refresh_warn2"), opt, -1, displayManager.REFRESH_DIRECTION_DIALOG_ID);
    }

     /**
     * Use the localization field to build the wifi not available alert dialog.
     * This dialog builds WIFINotAvailableDialogOption objects to refer to the
     * dialog choiches.
     * @param screen the dialog alert owner Screen
     */
    public void showNoWIFIAvailableDialog(Screen screen,
                                          String syncType,
                                          Vector filteredSources,
                                          boolean refresh,
                                          int direction,
                                          int delay,
                                          boolean fromOutside)
    {
        DialogOption[] opt = new DialogOption[2];

        opt[0] = new WIFINotAvailableDialogOption(
                screen,
                localization.getLanguage("dialog_continue"),
                0,
                syncType,
                filteredSources,
                refresh,
                direction,
                delay,
                fromOutside);

        opt[1] = new WIFINotAvailableDialogOption(
                screen,
                localization.getLanguage("dialog_cancel"),
                -1,
                syncType,
                filteredSources,
                refresh,
                direction,
                delay,
                fromOutside);

        lastWifiNotAvailableDialogState.update(syncType, filteredSources,
                                        refresh, direction, delay, fromOutside);
        
        displayManager.promptSelection(screen, localization.getLanguage("dialog_no_wifi_availabale"),
                opt, 0, displayManager.NO_WIFI_AVAILABLE_ID);
    }

    /**
     * Dialog option related to the wifi not available.
     */
    protected class WIFINotAvailableDialogOption extends DialogOption {
        private String syncType;
        private Vector filteredSources;
        private boolean refresh;
        private int direction;
        private int delay;
        private  boolean fromOutside;


        public WIFINotAvailableDialogOption(Screen screen, String description, int value,
                                            String syncType,
                                            Vector filteredSources,
                                            boolean refresh,
                                            int direction,
                                            int delay,
                                            boolean fromOutside) {
            super(screen, description, value);
            this.filteredSources = filteredSources;
            this.refresh = refresh;
            this.direction = direction;
            this.delay = delay;
            this.fromOutside = fromOutside;
            this.syncType = syncType;

            // The default action is to cancel
            if (value == -1) {
                displayManager.addPostDismissSelectionDialogAction(displayManager.NO_WIFI_AVAILABLE_ID, this);
            }
        }

        /**
         * Triggered in threaded like logic when the user selects an option.
         */
        public void run() {
            displayManager.removePostDismissSelectionDialogAction(displayManager.NO_WIFI_AVAILABLE_ID);
            displayManager.dismissSelectionDialog(displayManager.NO_WIFI_AVAILABLE_ID);
            HomeScreenController hsCont = controller.getHomeScreenController();

            if (getValue() == 0) {
                if(!hsCont.isSynchronizing()) {
                    hsCont.changeSyncLabelsOnSyncEnded();
                }

                hsCont.continueSynchronizationAfterBandwithSaverDialog(syncType, filteredSources,
                                                                       refresh, direction,
                                                                       delay, fromOutside, true);
            } else {
                hsCont.continueSynchronizationAfterBandwithSaverDialog(syncType, new Vector(),
                                                                       refresh, direction,
                                                                       delay, fromOutside, false);
            }
        }
    }

    /**
     * Use the localization field to build the reset type alert dialog that use
     * the DisplayManager client implementation to ask the user whose sources
     * must be refreshed. This dialog builds ResetTypeDialogOption objects to
     * refer to the dialog choiches. Actual client implementation requires this
     * alert to be diaplayed after the showRefreshDirectionDialog call in order
     * to have full information abaout the direction.
     * @param screen the dialog alert owner Screen
     * @param int the refresh direction for the selected sources.
     */
    public void showRefreshTypeDialog(Screen screen, int direction) {
        ResetTypeDialogOption[] options = null;
        // Count the number of enabled and refreshable sources
        Vector opts = new Vector();

        int numEnabledSources = -1;
        Enumeration enabledSources = appSyncSourceManager.getEnabledAndWorkingSources();

        int allId = 0;
        while(enabledSources.hasMoreElements()) {
            AppSyncSource source = (AppSyncSource)enabledSources.nextElement();
            if (source.isRefreshSupported(direction)&&source.isVisible()&& source.getConfig().getActive() && source.isWorking()) {
                Log.debug(TAG_LOG, "Source: " + source.getName() + " direction " + direction +
                                   " supported " + source.isRefreshSupported(direction));
                opts.addElement(new ResetTypeDialogOption(screen, source.getName(), source.getId(), direction));
                numEnabledSources++;
                allId |= source.getId();
            }
        }

        if ((numEnabledSources + 1) > 1) {
            opts.addElement(new ResetTypeDialogOption(
                    screen,
                    localization.getLanguage("type_all_enabled"),
                    allId,
                    direction));
        }

        opts.addElement(new ResetTypeDialogOption(
                    screen,
                    localization.getLanguage("dialog_cancel"),
                    0,
                    direction));

        options = new ResetTypeDialogOption[opts.size()];

        opts.copyInto(options);

        displayManager.promptSelection(screen, localization.getLanguage("dialog_refresh")
                + " "
                + (direction == SynchronizationController.REFRESH_FROM_SERVER ? localization
                        .getLanguage("dialog_refresh_from").toLowerCase() : localization
                        .getLanguage("dialog_refresh_to")).toLowerCase(), options, -1, displayManager.REFRESH_TYPE_DIALOG_ID);
    }

    /**
     * Dialog option related to the refresh direction to be used.
     */
    protected class ResetDirectionDialogOption extends DialogOption {

        public ResetDirectionDialogOption(Screen screen, String description, int value) {
            super(screen, description, value);
        }

        /**
         * Triggered in threaded like logic when the user selects an option.
         */
        public void run() {
            //Dismiss the currect dialog
            displayManager.dismissSelectionDialog(displayManager.REFRESH_DIRECTION_DIALOG_ID);
            lastSelectedDirection = value;
            //if the user selected a direction the refresh type dialog is shown
            //with the message related to that sync direction
            if (!this.getDescription().equals(localization.getLanguage("dialog_cancel"))) {
                showRefreshTypeDialog(screen, value);
            }
        }
    }

    /**
     * Resumes the last refresh type alert dialog if it was paused before the
     * user answered. This could be due to any pausing event on the client side
     * (screen rotation, incoming calls ...).
     * This method works if and only if a previous call to the
     * method was performed because it needs the last saved state of the field
     * lastSelectedDirection that is updated by the RefreshDirectionDialog class
     * when the user chose the reset direction
     * @param screen the dialog alert owner Screen
     */
    public void resumeLastRefreshTypeDialog(Screen screen) {
        showRefreshTypeDialog(screen, lastSelectedDirection);
    }

    /**
     * Contains the state of the last first sync alert dialog request.
     */
    class FirstSyncDialogState {
        protected AppSyncSource[] appSourceList;
        protected String syncType;
        protected Vector filteredSources;
        protected int delay;
        protected boolean refresh;
        protected int direction;
        protected boolean fromOutside;
        protected int questionCounter;
        protected int sourceIndex;

        /**
         * Update this class fields with the current FirstSyncDialogOption state
         * @param appSourceList the list of appSyncSources
         * @param syncType the String representation for the sync type
         * (manual...)
         * @param filteredSources the sources Vector to be updated in case the
         * user select to sync now
         * @param delay request the sync scheduler to be initiate the sync after
         * the given amount of milliseconds
         * @param fromOutside used by the sync scheduler to manage the incoming
         * sync request from outside if true
         * @param questionCounter the number of question to be displayed to the
         * user. This number depends by the number of sources that have a
         * warning messgae to be displayed at the first sync
         * @param sourceIndex the sync source index source which this dialog
         * alert is related.
         */
        protected void update(AppSyncSource[] appSourceList, String syncType,
                              Vector filteredSources, boolean refresh,
                              int direction, int delay, boolean fromOutside,
                              int questionCounter, int sourceIndex) {
            this.appSourceList = appSourceList;
            this.syncType = syncType;
            this.filteredSources = filteredSources;
            this.refresh = refresh;
            this.direction = direction;
            this.delay = delay;
            this.fromOutside = fromOutside;
            this.questionCounter = questionCounter;
            this.sourceIndex = sourceIndex;
        }
    }

    /**
     * Resumes the last fisrt sync alert dialog if it was paused before the
     * user answered. This could be due to any pausing event on the client side
     * (screen rotation, incoming calls ...).
     * This method works if and only if a previous call to the showFirstSyncDialog
     * method was performed because it needs the last saved state of the field
     * lastFirstSyncDialogState that is updated by the showFirstSyncDialog method
     * when called.
     * @param screen the dialog alert owner Screen
     */
    public void resumeLastFirstSyncDialog(Screen screen) {
        showFirstSyncDialog(screen, lastFirstSyncDialogState.appSourceList,
                            lastFirstSyncDialogState.syncType,
                            lastFirstSyncDialogState.filteredSources,
                            lastFirstSyncDialogState.refresh,
                            lastFirstSyncDialogState.direction,
                            lastFirstSyncDialogState.delay,
                            lastFirstSyncDialogState.fromOutside,
                            lastFirstSyncDialogState.questionCounter,
                            lastFirstSyncDialogState.sourceIndex);
    }

     /**
     * Contains the state of the last WI-Fi not available alert dialog request.
     */
    class WifiNotAvailableDialogState {
        protected String syncType;
        protected Vector filteredSources;
        protected int delay;
        protected boolean refresh;
        protected int direction;
        protected boolean fromOutside;

        /**
         * Update this class fields with the current WIFINotAvailableDialogOption state
         * @param appSourceList the list of appSyncSources
         * @param syncType the String representation for the sync type
         * (manual...)
         * @param filteredSources the sources Vector to be updated in case the
         * user select to sync now
         * @param delay request the sync scheduler to be initiate the sync after
         * the given amount of milliseconds
         * @param fromOutside used by the sync scheduler to manage the incoming
         * sync request from outside if true
         * @param questionCounter the number of question to be displayed to the
         * user. This number depends by the number of sources that have a
         * warning messgae to be displayed at the first sync
         * @param sourceIndex the sync source index source which this dialog
         * alert is related.
         */
        protected void update(String syncType,
                              Vector filteredSources, boolean refresh,
                              int direction, int delay, boolean fromOutside) {
            this.syncType = syncType;
            this.filteredSources = filteredSources;
            this.refresh = refresh;
            this.direction = direction;
            this.delay = delay;
            this.fromOutside = fromOutside;
        }
    }

     /**
     * Resumes the last WI-FI not available alert dialog if it was paused before the
     * user answered. This could be due to any pausing event on the client side
     * (screen rotation, incoming calls ...).
     * This method works if and only if a previous call to the showNoWIFIAvailableDialog
     * method was performed because it needs the last saved state of the field
     * lastWifiNotAvailableDialogState that is updated by the showNoWIFIAvailableDialog method
     * when called.
     * @param screen the dialog alert owner Screen
     */
    public void resumeWifiNotAvailableDialog(Screen screen) {
        showNoWIFIAvailableDialog(screen,
                                    lastWifiNotAvailableDialogState.syncType,
                                    lastWifiNotAvailableDialogState.filteredSources,
                                    lastWifiNotAvailableDialogState.refresh,
                                    lastWifiNotAvailableDialogState.direction,
                                    lastWifiNotAvailableDialogState.delay,
                                    lastWifiNotAvailableDialogState.fromOutside);
    }

    /**
     * Show the first sync alert dialogs for all the sources that are listed
     * into the given appSourceList array.
     * @param appSourceList the list of appSyncSources
     * @param syncType the String representation for the sync type
     * (manual...)
     * @param filteredSources the sources Vector to be updated in case the
     * user select to sync now
     * @param refresh specifies if this sync is a refresh
     * @param direction in case of refresh, this is the direction (client to
     * server or server to client)
     * @param delay request the sync scheduler to be initiate the sync after
     * the given amount of milliseconds
     * @param fromOutside used by the sync scheduler to manage the incoming
     * sync request from outside if true
     * @param questionCounter the number of question to be displayed to the
     * user. This number depends by the number of sources that have a
     * warning messgae to be displayed at the first sync
     * @param sourceIndex the sync source index source which this dialog
     * alert is related.
     */
    public void showFirstSyncDialog(Screen screen, AppSyncSource[] appSourceList, String syncType,
                                    Vector filteredSources, boolean refresh, int direction,
                                    int delay, boolean fromOutside,
                                    int questionCounter, int sourceIndex)
    {
        // This is just a safety check
        if (sourceIndex >= appSourceList.length) {
            Log.error(TAG_LOG, "Invalid source id, cannot show first sync dialog");
            return;
        }

        //Set the last variables to be used in case of unexpected resume 
        //(for example a device screen rotation or a incoming call or in general
        //an event that pauses the application and require a resume action)
        String warning = appSourceList[sourceIndex].getWarningOnFirstSync();
        FirstSyncDialogOption[] options = new FirstSyncDialogOption[2];
        options[0] = new FirstSyncDialogOption(screen, localization.getLanguage("dialog_sync_now"), 0,
                                               appSourceList, syncType, filteredSources, refresh,
                                               direction, delay, fromOutside,
                                               questionCounter, sourceIndex);
        options[1] = new FirstSyncDialogOption(screen, localization.getLanguage("dialog_try_later"), 1,
                                               appSourceList, syncType, filteredSources, refresh,
                                               direction, delay, fromOutside,
                                               questionCounter, sourceIndex);
        lastFirstSyncDialogState.update(appSourceList, syncType, filteredSources,
                                        refresh, direction, delay, fromOutside,
                                        questionCounter, sourceIndex);
        displayManager.promptSelection(screen, warning, options, 0, displayManager.FIRST_SYNC_DIALOG_ID);
    }

    /**
     * Container for the first sync dialog options
     */
    protected class FirstSyncDialogOption extends DialogOption {
        private AppSyncSource[] appSourceList;
        private String syncType;
        private Vector filteredSources;
        private boolean refresh;
        private int direction;
        private int delay;
        private  boolean fromOutside;
        private int questionCounter;
        private int sourceIndex;

        /**
         * Public copy-constructor
         * @param screen the dialog alert owner Screen
         * @param description the dialog option description (describe the
         * current option to the user)
         * @param value the current option returned value
         * @param appSourceList the list of appSyncSources
         * @param syncType the String representation for the sync type
         * (manual...)
         * @param filteredSources the sources Vector to be updated in case the
         * user select to sync now
         * @param refresh specifies if this sync is for a refresh
         * @param direction in case of refresh sync, this is the direction
         * (client to server or server to client)
         * @param delay request the sync scheduler to be initiate the sync after
         * the given amount of milliseconds
         * @param fromOutside used by the sync scheduler to manage the incoming
         * sync request from outside if true
         * @param questionCounter the number of question to be displayed to the
         * user. This number depends by the number of sources that have a
         * warning messgae to be displayed at the first sync
         * @param sourceIndex the sync source index source which this dialog
         * alert is related.
         */
        public FirstSyncDialogOption(
                Screen screen,
                String description,
                int value,
                AppSyncSource[] appSourceList,
                String syncType,
                Vector filteredSources,
                boolean refresh,
                int direction,
                int delay,
                boolean fromOutside,
                int questionCounter,
                int sourceIndex) {
            super(screen, description, value);
            this.appSourceList = appSourceList;
            this.filteredSources = filteredSources;
            this.refresh = refresh;
            this.direction = direction;
            this.delay = delay;
            this.fromOutside = fromOutside;
            this.syncType = syncType;
            this.questionCounter = questionCounter;
            this.sourceIndex = sourceIndex;

            //Post the runnable action for the latest alert
            Runnable cancelAction = null;
            if (sourceIndex >= questionCounter - 1) {
                displayManager.addPostDismissSelectionDialogAction(displayManager.FIRST_SYNC_DIALOG_ID, this);
            }
        }

        /**
         * Triggered in threaded like logic when the user selects an option.
         */
        public void run() {
            //Whenever an alert choice is selected by the user the related
            //action related to the dismiss alert must be removed and must be
            //redefined run through this thread
            displayManager.removePostDismissSelectionDialogAction(displayManager.FIRST_SYNC_DIALOG_ID);

            //Dismiss the first sync selection dialog passing the command to
            //reset the sync all button on the home screen if and only if the
            //dialog is the latest to be shown to the user. The dismiss action
            //must rely on the DisplayManager implementation.
            displayManager.dismissSelectionDialog(displayManager.FIRST_SYNC_DIALOG_ID);
            HomeScreenController hsCont = controller.getHomeScreenController();

            // We don't want to show the alert twice. Even if the user postpones
            // the sync, we consider the source as synced just when nan active
            // response has been given to the alert (yes/no). In case the alert
            // is dismissed for any reason the client will display the alert
            // next time the sync is requested.
            appSourceList[sourceIndex].getConfig().setSynced(true);
            appSourceList[sourceIndex].getConfig().commit();

            if (getValue() == 0) {
                //User says "Sync Now"
                //add the given app source to the next sync request vector
                filteredSources.addElement(appSourceList[sourceIndex]);
            }
            hsCont.redraw();

            if (sourceIndex >= questionCounter - 1) {
                if(!hsCont.isSynchronizing()) {
                    hsCont.changeSyncLabelsOnSyncEnded();
                }
                //Last sync question reached. Synchronization must start now. If
                //the filteredSources param is empty, then the sync is
                //terminated
                hsCont.continueSynchronizationAfterFirstSyncDialog(syncType, filteredSources,
                                                                   refresh, direction,
                                                                   delay, fromOutside, true);
            } else {
                //There are more sources that require this alert to be displayed
                sourceIndex++;
                showFirstSyncDialog(screen, appSourceList, syncType, filteredSources,
                                    refresh, direction, delay, fromOutside,
                                    questionCounter, sourceIndex);
            }
        }
    }

    /**
     * Container for the reset type dialog option
     */
    protected class ResetTypeDialogOption extends DialogOption {
        int direction;
        /**
         * Public copy-constructor
         * @param screen the dialog alert owner Screen
         * @param description the dialog option description (describe the
         * current option to the user)
         * @param value the current option returned value
         * @param direction the related sync direction value
         */
        public ResetTypeDialogOption(Screen screen, String description, int value, int direction) {
            super(screen, description, value);
            this.direction = direction;
        }

        /**
         * Accessor method to get the sync direction value
         * @return int the sync direction
         */
        public int getDirection() {
            return this.direction;
        }

        /**
         * Triggered in threaded like logic when the user selects an option.
         */
        public void run() {
            Log.debug(TAG_LOG, "TYPE - Selected: " + this.getDescription() + " - code " +
                      this.getValue() + " - direction " + getDirection());

            //dismiss the progress dialog
            displayManager.dismissSelectionDialog(displayManager.REFRESH_TYPE_DIALOG_ID);
            if (!this.getDescription().equals(localization.getLanguage("dialog_cancel"))) {
                //User selected the sync sources to be refreshed
                try {
                    displayManager.hideScreen(screen);
                    //starts the sync
                    controller.getHomeScreenController().refresh(value, direction);
                    controller.getHomeScreenController().redraw();
                } catch (Exception ex) {
                    Log.error("Exception accessing home screen: " + ex);
                }
            } else {
                //User selected the cancel option
                controller.getHomeScreenController().redraw();
            }
        }
    }
}
