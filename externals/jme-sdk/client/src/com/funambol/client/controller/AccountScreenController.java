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

import java.util.Vector;

import com.funambol.client.customization.Customization;
import com.funambol.client.configuration.Configuration;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.localization.Localization;
import com.funambol.client.ui.AccountScreen;
import com.funambol.client.ui.DisplayManager;
import com.funambol.util.Log;
import com.funambol.util.StringUtil;
import com.funambol.syncml.spds.SyncException;

/**
 * This class is the controller (in the MVC model) for the AccountScreen.
 */
public class AccountScreenController extends SynchronizationController {

    private static final String TAG_LOG = "AccountScreenController";

    protected AccountScreen        screen = null;

    protected AppSyncSource        configAppSource  = null;
    protected boolean              failed           = false;
    protected boolean              sourceStarted    = false;
    protected Exception            exp              = null;

    protected String               originalUrl      = null;
    protected String               originalUser     = null;
    protected String               originalPassword = null;

    public AccountScreenController(Controller controller, AccountScreen accountScreen) {
        super(controller, accountScreen, null);

        this.screen = accountScreen;
        
        // Save the original values so that we can revert changes at any time
        originalUrl = configuration.getSyncUrl() != null ?  configuration.getSyncUrl() : ""; 
        originalUser = configuration.getUsername() != null ?  configuration.getUsername() : "";
        originalPassword = configuration.getPassword() != null ?  configuration.getPassword() : "";
    }

    /**
     * TODO: Remove once the com.funambol.client.controller package integration is finished
     */
    public AccountScreenController(Controller controller, Customization customization, 
            Configuration configuration, Localization localization,
            AppSyncSourceManager appSyncSourceManager, AccountScreen accountScreen) {

        super(controller, customization, configuration, localization,
                appSyncSourceManager, accountScreen, null);

        this.screen = accountScreen;

        // Save the original values so that we can revert changes at any time
        originalUrl = configuration.getSyncUrl() != null ?  configuration.getSyncUrl() : "";
        originalUser = configuration.getUsername() != null ?  configuration.getUsername() : "";
        originalPassword = configuration.getPassword() != null ?  configuration.getPassword() : "";
    }

    public AccountScreen getAccountScreen() {
        return screen;
    }

    public void saveAndCheck() {
        if(screen != null) {
            saveAndCheck(screen.getSyncUrl(), screen.getUsername(), screen.getPassword());
        } else {
            Log.error(TAG_LOG, "Cannot save, account screen is null");
        }
    }

    public void saveAndCheck(String serverUri, String username, String password) {

        Log.trace(TAG_LOG, "saveAndCheck");

        // Check if a sync is currently running, and in this case warn the user
        // that the account cannot be saved
        if (isSyncInProgress()) {
            showSyncInProgressMessage();
            return;
        }

        // Load all the default settings and overwrite the parameters edited
        // here
        configuration.load();

        // Preliminary check
        if (StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)
            || StringUtil.isNullOrEmpty(serverUri)) {
            showMessage(localization.getLanguage("login_failed_empty_params"));
            return;
        } else if (!StringUtil.isValidProtocol(serverUri)) {
            showMessage(localization.getLanguage("status_invalid_url"));
            return;
        }

        if(!originalUrl.equals(serverUri) || !originalUser.equals(username)
                || !originalPassword.equals(password)
                || configuration.getCredentialsCheckPending()){

            // Okay, save the configuration
            configuration.setSyncUrl(serverUri);
            configuration.setUsername(username);
            configuration.setPassword(password);

            if (configuration.save() != Configuration.CONF_OK) {
                showMessage(localization.getLanguage("message_config_error") + ": " +
                        localization.getLanguage("message_config_error_save"));
                return;
            }

            // Now we must perform a sync of the configuration to authenticate and
            // verify the credentials
            configAppSource = appSyncSourceManager.getSource(
                                                AppSyncSourceManager.CONFIG_ID);
            if (configAppSource == null) {
                Log.error(TAG_LOG, "No suitable ConfigSyncSource, cannot verify credentials");
            } else {
                // Disable the save command
                failed = false;
                sourceStarted = false;
                exp = null;
                screen.disableSave();

                Vector sources = new Vector();
                sources.addElement(configAppSource);

                // We want to perform this sync at log level debug because if
                // this is the first sync the user has no means to set a log
                // level and we may need to check what's going on in case of
                // errors
                try {
                    configuration.setTempLogLevel(Log.TRACE);
                    controller.reapplyMiscConfiguration();
                    synchronize(SynchronizationController.MANUAL, sources);
                } catch (Exception e) {
                    Log.error(TAG_LOG, "Config sync failed ", e);
                    failed = true;
                    syncEnded();
                } finally {
                    // Restore the original log level
                    configuration.restoreLogLevel();
                    controller.reapplyMiscConfiguration();
                }
            }
        } else {
            // There where no need to authenticate
            Log.debug(TAG_LOG, "No need to authenticate");
        }
    }

    protected boolean isSyncInProgress() {
        HomeScreenController homeScreenController = controller.getHomeScreenController();
        return homeScreenController.isSynchronizing();
    }

    public void sourceFailed(AppSyncSource appSource, SyncException e) {
        super.sourceFailed(appSource, e);
        // In order to guarantee compatibility with servers without a
        // ConfigSyncSource, we must allow users to access the home screen even
        // if the server sends a 404 status for this sync
        if (e.getCode() != SyncException.NOT_FOUND_URI_ERROR) {
            exp = e;
            failed = true;
        } else {
            Log.info(TAG_LOG, "Server does not have a config source, most likely not a Funambol server");
            // Apply the default server configuration as the server did not
            // provide its capabilities. This has the side effect of hiding the
            // picture source
            controller.reapplyServerCaps(null);
        }
    }

    public void sourceStarted(AppSyncSource appSource) {
        super.sourceStarted(appSource);
        sourceStarted = true;
    }

    public void syncEnded() {
        super.syncEnded();

        screen.enableSave();
        if (failed && sourceStarted) {
            Log.info(TAG_LOG, "Cannot access home screen");
            screen.checkFailed();
            // Clear the configuration for no pending credentials check
            configuration.setCredentialsCheckPending(true);
            configuration.save();
            // Now we show an error to the user, depending on the error we got
            String msg;
            if (exp instanceof SyncException) {
                msg = getMessageFromSyncException((SyncException)exp);
                if(msg == null) {
                    return;
                }
            } else {
                msg = localization.getLanguage("status_generic_error");
            }
            // We should never fall into this case, unless we miss some strings
            // in the language table
            if (msg == null) {
                msg = localization.getLanguage("status_generic_error");
            }
            // Show an error to the user
            showMessage(msg);

        } else if (!sourceStarted) {
            // Sync the source did not start, an appropriate error was displayed
            // by the syncrhonizationController
            Log.info(TAG_LOG, "Cannot access home screen");
            // Clear the configuration for no pending credentials check
            configuration.setCredentialsCheckPending(true);
            configuration.save();
            screen.checkFailed();
        } else {
            // The user is authenticated, hide the login and open the main view
            // screen
            userAuthenticated();

            originalUrl = screen.getSyncUrl();
            originalUser = screen.getUsername();
            originalPassword = screen.getPassword();
        }
    }

    protected String getMessageFromSyncException(SyncException ex) {
        String msg;
        switch (ex.getCode()) {
            case SyncException.AUTH_ERROR:
                msg = localization.getLanguage("status_invalid_credentials");
                break;
            case SyncException.FORBIDDEN_ERROR:
                msg = localization.getLanguage("status_forbidden_error");
                break;
            case SyncException.DATA_NULL:
            case SyncException.CONN_NOT_FOUND:
                msg = localization.getLanguage("status_invalid_url");
                break;
            case SyncException.READ_SERVER_RESPONSE_ERROR:
            case SyncException.WRITE_SERVER_REQUEST_ERROR:
            case SyncException.SERVER_CONNECTION_REQUEST_ERROR:
                msg = localization.getLanguage("status_network_error");
                break;
            case SyncException.CONNECTION_BLOCKED_BY_USER:
                msg = localization.getLanguage("status_connection_blocked");
                break;
            case SyncException.CANCELLED:
                // In this case we shall simply go back to the account
                // screen, so we just return from this method
                msg = null;
            default:
                msg = localization.getLanguage("status_generic_error");
                break;
        }
        return msg;
    }

    public boolean hasChanges(String serverUri, String username, String password) {
        if (customization.syncUriEditable()) {
            if (!originalUrl.equals(serverUri)) {
                return true;
            }
        }
        if (!originalUser.equals(username) ||
            !originalPassword.equals(password)) {
            return true;
        }
        return false;
    }

    public void resetValues() {
        if (customization.syncUriEditable()) {
            screen.setSyncUrl(originalUrl);
            configuration.setSyncUrl(originalUrl);
        }
        screen.setUsername(originalUser);
        screen.setPassword(originalPassword);
        configuration.setUsername(originalUser);
        configuration.setPassword(originalPassword);
        configuration.save();
    }

    public void endSync(Vector sources, boolean hadErrors) {
        // Errors are handled in the syncEnded method
        setCancel(false);
    }

    public void hide() {
        controller.toBackground();
    }

    public void initScreen() {
        Configuration config = controller.getConfiguration();
        String url, usr, pwd;
        if (config.load() == Configuration.CONF_OK) {
            url = config.getSyncUrl();
            usr = config.getUsername();
            pwd = config.getPassword();
        } else {
            Log.error(TAG_LOG, "Error loading the configuration, using default values");
            url = customization.getServerUriDefault();
            usr = customization.getUserDefault();
            pwd = customization.getPasswordDefault();
        }
        initScreen(url, usr, pwd); 
    }

    public void initScreen(String url, String usr, String pwd) {
        if(screen != null) {
            screen.setSyncUrl(url);  originalUrl = url;
            screen.setUsername(usr); originalUser = usr;
            screen.setPassword(pwd); originalPassword = pwd;
        }
    }

    protected void showSyncInProgressMessage() {
        // If the home screen is not displayed, we cannot show any warning and
        // just ignore this event
        if (screen != null) {
            DisplayManager dm = controller.getDisplayManager();
            String msg = localization.getLanguage("message_sync_running_wait");
            dm.showMessage(screen, msg);
        }
    }

    protected void userAuthenticated() {
        Log.info(TAG_LOG, "Opening home screen");

        // An account has been created. So keep track of it in order to not 
        // display the signup screen again
        configuration.setSignupAccountCreated(true);
        configuration.setCredentialsCheckPending(false);
        configuration.save();

        screen.checkSucceeded();
    }

    public void switchToSignupScreen() {
        try {
            controller.getDisplayManager().showScreen(screen, Controller.SIGNUP_SCREEN_ID);
            controller.getDisplayManager().hideScreen(screen);
        } catch(Exception ex) {
            Log.error(TAG_LOG, "Unable to switch to login screen", ex);
        }
    }
}
