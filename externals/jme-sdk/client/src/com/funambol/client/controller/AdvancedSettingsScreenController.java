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
package com.funambol.client.controller;

import java.util.Enumeration;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.customization.Customization;
import com.funambol.client.localization.Localization;
import com.funambol.client.ui.AdvancedSettingsScreen;
import com.funambol.client.ui.Screen;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.source.AppSyncSource;
import com.funambol.util.Log;

/**
 * Realize the MVC control section of the view elements' in all of the
 * AdvancedSettingsScreen interface implementation. It uses the screen in order
 * to set and get the log level and correctly initialize and dinamically manage
 * its view elements. This class is abstract as it contains generic methods to
 * send and view log; such those methods are implementation dependent as their
 * realization is different on different platforms.
 */
public abstract class AdvancedSettingsScreenController {

    /** The tag to be wirtten into the log*/
    public static final String TAG_LOG = "AdvancedSettingsScreenController";

    protected AdvancedSettingsScreen screen;

    protected Controller controller;

    protected Configuration configuration;
    protected Customization customization;
    protected Localization localization;
    protected AppSyncSourceManager appSyncSourceManager;

    /**
     * public constructor: uses the simple controller object and the controlled
     * screen because on one side it must be compliant with the customization,
     * localization and configuration related to the screen and on the other
     * side it must control the behavior of the screen elements referring to the
     * AdvancedSettingsScreen interface.
     * @param controller the object that contains the customization,
     * localization and configuration instances.
     * @param screen the AdvancedSettingsScreen implementation to be controlled
     * by this class.
     */
    public AdvancedSettingsScreenController(Controller controller, AdvancedSettingsScreen screen) {
        this.controller = controller;
        this.screen = screen;
        this.configuration = controller.getConfiguration();
        this.customization = controller.getCustomization();
        this.localization = controller.getLocalization();
        this.appSyncSourceManager = controller.getAppSyncSourceManager();
        controller.setAdvancedSettingsScreenController(this);
    }

    public AdvancedSettingsScreenController(Controller controller,
            Customization customization, Configuration configuration,
            Localization localization, AppSyncSourceManager appSyncSourceManager,
            AdvancedSettingsScreen screen) {
        this.controller = controller;
        this.screen = screen;
        this.configuration = configuration;
        this.customization = customization;
        this.localization = localization;
        this.appSyncSourceManager = appSyncSourceManager;
    }

    /**
     * Check the view parameters and save the configuration related to the
     * AdvancedSettingsScreen implementation that uses this class.
     */
    public void checkAndSave() {
        configuration.setBandwidthSaver(screen.getBandwidthSaver());
        Log.debug(TAG_LOG, "Old Log level set to: " + configuration.getLogLevel());
        Log.debug(TAG_LOG, "New Log level set to: " + screen.getViewLogLevel());
        configuration.setLogLevel(screen.getViewLogLevel());
        configuration.save();
    }

    /**
     * Realize the "Reset" action. Implement the client dependent reset feature
     * and it is common to every high level AdvancedSettingsScreen controllers.
     */
    public void reset() {
        DialogController dc = controller.getDialogController();
        if (controller.getHomeScreenController().isSynchronizing()){
            dc.showMessage(screen, localization.getLanguage("sync_in_progress_dialog"));
        } else {
            dc.showRefreshDirectionDialog(screen);
        }
    }

    /**
     * Initialize the config parameter on the screen, in particular the log
     * level. Implement the client dependent screen's view population referring
     * to the customization object retrieved by the Controller object that was
     * passed as parameter of the consturctor. Due to this reference this method
     * can hide the screen's commands (buttons, view items, or every other
     * command implementation such as menu items). The initialization covers
     * also the dinamyc aspect: if a sync is in progress and the
     * AdvancedSettingsScreen is required to be shown, the send log and reset
     * commands should appear disabled in most cases (if not hidden by the
     * customization implementation). This method must be overridden if the
     * application must behave differently from the above description.
     */
    public void initialize() {

        // Show/Hide bandwidth section
        boolean showBandwidthSaver = customization.isBandwidthSaverEnabled();
        // Check if we have at least one media source visible
        Enumeration appSources = appSyncSourceManager.getWorkingSources();
        int mediaCount = 0;
        while(appSources.hasMoreElements()) {
            AppSyncSource appSource = (AppSyncSource)appSources.nextElement();
            if (appSource.getIsMedia() && controller.isVisible(appSource)) {
                mediaCount++;
            }
        }
        // We show the media bandwidth saver only if we have at least one media source
        showBandwidthSaver &= (mediaCount > 0);

        if (showBandwidthSaver) {
            configuration.load();
            screen.setBandwidthSaver(configuration.getBandwidthSaverActivated());
        } else {
            screen.hideBandwidthSaverSection();
        }

        // Show/Hide send log section
        if (customization.isLogEnabledInSettingsScreen()) {
            configuration.load();
            screen.setViewLogLevel(configuration.getLogLevel());
        } else {
            screen.hideLogsSection();
        }

        // Show/Hide send log command
        if (!customization.sendLogEnabled()) {
            screen.hideSendLogCommand();
        }

        //To be decided: Enable or disable the send log and the reset command when a
        //sync or a connection is in progress.
        /*if (engine.isSynchronizing()) {
            screen.enableResetCommand(false);
            screen.enableSendLogCommand(false);
        }*/

        // Show/Hide reset section
        if (!customization.enableRefreshCommand()) {
            screen.hideResetSection();
        }

        // Show/Hide import contacts
        if (!customization.getContactsImportEnabled()) {
            screen.hideImportContactsSection();
        }
    }

    public Screen getAdvancedSettingsScreen() {
        return screen;
    }

    /**
     * Realize the Send log action. As the send log feature is platform
     * dependent this method is left abstract and must be implemented on the
     * high level client controller.
     */
    public abstract void sendLog();

    /**
     * Realize the View log action. As the view log feature is platform
     * dependent this method is left abstract and must be implemented on the
     * high level client controller.
     */
    public abstract void viewLog();

    /**
     * Run the Import Contacts process
     */
    public abstract void importContacts();
}
