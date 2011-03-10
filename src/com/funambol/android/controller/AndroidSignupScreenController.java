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

import android.accounts.AccountAuthenticatorActivity;
import android.app.Activity;
import android.os.Bundle;

import com.funambol.android.AndroidAccountManager;
import com.funambol.android.AndroidCustomization;
import com.funambol.android.ContactsImporter;
import com.funambol.android.activities.AndroidDisplayManager;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.controller.Controller;
import com.funambol.client.controller.SignupScreenController;
import com.funambol.client.customization.Customization;
import com.funambol.client.localization.Localization;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.ui.Screen;
import com.funambol.client.ui.SignupScreen;
import com.funambol.platform.DeviceInfo;
import com.funambol.platform.DeviceInfoInterface;
import com.funambol.platform.NetworkStatus;
import com.funambol.util.Log;

/**
 * Wrapper of the SignupScreenController for Android
 */
public class AndroidSignupScreenController extends SignupScreenController {

    private static final String TAG_LOG = "AndroidSignupScreenController";

    private final SpinnerTriggerUIThread spinnerTriggerUIThread;
    private final MessageUIThread messageUIThread;

    private AndroidDisplayManager displayManager;

    private static int pDialogId = -1;

    private NetworkStatus networkStatus;

    public AndroidSignupScreenController(Controller controller, Customization customization,
            Configuration configuration, Localization localization,
            AppSyncSourceManager appSyncSourceManager, SignupScreen signupScreen) {
        super(controller, customization, configuration, localization, appSyncSourceManager, signupScreen);
        displayManager = (AndroidDisplayManager)controller.getDisplayManager();
        networkStatus = new NetworkStatus((Activity)signupScreen.getUiScreen());
        engine.setNetworkStatus(networkStatus);
        spinnerTriggerUIThread = new SpinnerTriggerUIThread();
        messageUIThread = new MessageUIThread();
    }

    protected DeviceInfoInterface getDeviceInfo() {
        return new DeviceInfo(((Activity)screen.getUiScreen()));
    }

    @Override
    protected void userAuthenticated() {

        Log.info(TAG_LOG, "User authenticated");

        // Clear the configuration for no pending credentials check
        configuration.setCredentialsCheckPending(false);
        configuration.save();
        
        Activity activity = (Activity)screen.getUiScreen();

        // Add the new account
        AndroidAccountManager.addNewFunambolAccount(getUsername(), 
                (AccountAuthenticatorActivity)activity);

        // We must wait a little bit to be sure the pending initialization
        // syncs are actually performed and removed from the queue
        AndroidHomeScreenController homeScreenController;
        homeScreenController = (AndroidHomeScreenController)controller.getHomeScreenController();
        while (homeScreenController.isAnySourcePending() != null) {
            Log.info(TAG_LOG, "Waiting for adapters to be initialized");
            try {
                Thread.sleep(3000);
            } catch (Exception e) {}
        }
        super.userAuthenticated();

        // If the mobile signup is not enabled we should run the contacts import
        // here. If not it will be triggered as soon as the congrats message is
        // displayed
        if(!customization.getMobileSignupEnabled() ||
           !customization.getShowSignupSuccededMessage() ||
           state == STATE_LOGGED_IN) {
            runContactsImport();
        }
    }

    /**
     * @return the username from the current screen
     */
    protected String getUsername() {
        return screen.getUsername();
    }

    @Override
    protected void showMessage(String msg) {
        messageUIThread.setMessage(msg);
        messageUIThread.setScreen(screen);
        ((Activity)screen.getUiScreen()).runOnUiThread(messageUIThread);
    }

    @Override
    protected void showSignupSucceededMessage(String msg) {
        Screen homeScreen = controller.getHomeScreenController().getHomeScreen();
        messageUIThread.setMessage(msg);
        messageUIThread.setScreen(homeScreen);
        messageUIThread.setOkAction(new Runnable() {
            public void run() {
                // Run contacts import once the congrats message is closed
                runContactsImport();
            }
        });
        ((Activity)homeScreen.getUiScreen()).runOnUiThread(messageUIThread);
    }

    private void runContactsImport() {
        // Check if contacts shall be imported from other accounts
        if(((AndroidCustomization)customization).getContactsImportEnabled()) {
            AndroidAdvancedSettingsScreenController advSettingsController;
            advSettingsController = (AndroidAdvancedSettingsScreenController)
                    controller.getAdvancedSettingsScreenController();
            ContactsImporter importer = new ContactsImporter(Controller.HOME_SCREEN_ID,
                    (Activity)screen.getUiScreen(), advSettingsController);
            importer.importContacts(true);
        }
    }

    @Override
    protected void showProgressDialog(String message) {
        synchronized(spinnerTriggerUIThread) {
            spinnerTriggerUIThread.setValue(true);
            spinnerTriggerUIThread.setMessage(message);
            ((Activity)screen.getUiScreen()).runOnUiThread(spinnerTriggerUIThread);
            try {
                // Wait for the above request to be performed
                spinnerTriggerUIThread.wait(2000);
            } catch(InterruptedException ex) {}
        }
    }

    @Override
    protected void hideProgressDialog() {
        synchronized(spinnerTriggerUIThread) {
            if(pDialogId != -1) {
                spinnerTriggerUIThread.setValue(false);
                ((Activity)screen.getUiScreen()).runOnUiThread(spinnerTriggerUIThread);
                try {
                    // Wait for the above request to be performed
                    spinnerTriggerUIThread.wait(2000);
                } catch(InterruptedException ex) {}
            }
        }
    }

    @Override
    public void switchToLoginScreen() {
        // Build the extras paramater in order to give the current credentials
        // to the login screen
        Bundle extras = new Bundle();
        extras.putString("syncurl", screen.getSyncUrl());
        extras.putString("username", screen.getUsername());
        extras.putString("password", screen.getPassword());
        try {
            ((AndroidDisplayManager)controller.getDisplayManager()).showScreen(
                    (Activity)screen.getUiScreen(), Controller.LOGIN_SCREEN_ID, extras);
            controller.hideScreen(screen);
        } catch(Exception ex) {
            Log.error(TAG_LOG, "Unable to switch to login screen", ex);
        }
    }

    private class MessageUIThread implements Runnable {

        private String message;
        private Screen screen;
        private Runnable okAction;
        private boolean cancelable;

        public void setMessage(String message) {
            this.message = message;
        }

        public void setScreen(Screen screen) {
            this.screen = screen;
        }

        public void setOkAction(Runnable okAction) {
            this.okAction = okAction;
        }

        public void setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
        }

        public void run() {
            // Show alert
            displayManager.showOkDialog( screen, message,
                    localization.getLanguage("dialog_ok"), okAction, cancelable);
            okAction = null;
        }
    }
    
    private class SpinnerTriggerUIThread implements Runnable {

        private boolean value;
        private String message;

        public void setValue(boolean value) {
            this.value = value;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public synchronized void run() {
            if (value) {
                pDialogId = displayManager.showProgressDialog(screen, message);
            } else if(pDialogId != -1) {
                displayManager.dismissProgressDialog(screen, pDialogId);
                pDialogId = -1;
            }
            notifyAll();
        }
    }
}
