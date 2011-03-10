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

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.app.Activity;
import android.os.Bundle;

import com.funambol.android.AndroidAccountManager;
import com.funambol.android.AndroidCustomization;
import com.funambol.android.ContactsImporter;
import com.funambol.android.activities.AndroidDisplayManager;

import com.funambol.client.controller.Controller;
import com.funambol.client.controller.AccountScreenController;
import com.funambol.client.ui.AccountScreen;

import com.funambol.platform.NetworkStatus;
import com.funambol.util.Log;

import java.util.Vector;

/**
 * Wrapper of the AccountScreenController for Android
 */
public class AndroidLoginScreenController extends AccountScreenController {

    private static final String TAG_LOG = "AndroidLoginScreenController";

    private SpinnerTriggerUIThread spinnerTriggerUIThread;
    private final MessageUIThread messageUIThread;

    private AndroidDisplayManager displayManager;

    private static int pDialogId = -1;
    
    private NetworkStatus networkStatus;

    public AndroidLoginScreenController(Controller controller, AccountScreen accountScreen) {
        super(controller, accountScreen);
        displayManager = (AndroidDisplayManager)controller.getDisplayManager();
        networkStatus = new NetworkStatus((Activity) accountScreen.getUiScreen());
        engine.setNetworkStatus(networkStatus);
        spinnerTriggerUIThread = new SpinnerTriggerUIThread();
        messageUIThread = new MessageUIThread();
    }

    @Override
    protected void userAuthenticated() {

        Log.info(TAG_LOG, "User authenticated");

        // An account has been created. So keep track of it in order to not
        // display the signup screen again
        configuration.setSignupAccountCreated(true);
        configuration.setCredentialsCheckPending(false);
        configuration.save();

        Activity activity = (Activity)screen.getUiScreen();

        Account nativeAccount = AndroidAccountManager.getNativeAccount(
                (Activity)screen.getUiScreen());

        if(hasChanges() || nativeAccount == null) {
            // Add the new account
            AndroidAccountManager.addNewFunambolAccount(getUsername(),
                    (AccountAuthenticatorActivity)activity);
            // We must wait a little bit to be sure the pending initialization
            // syncs are actually performed and removed from the queue
            AndroidHomeScreenController homeScreenController;
            homeScreenController = (AndroidHomeScreenController)controller.getHomeScreenController();
            while (homeScreenController.isAnySourcePending() != null) {
                Log.info(TAG_LOG, "Waiting for adapters to be initialized 2");
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {}
            }
        }

        hideProgressDialog();

        // Notify the activity that the check is terminated
        screen.checkSucceeded();

        // Run contacts import only if the user changed the credentials
        if(hasChanges() || nativeAccount == null) {
            runContactsImport();
        }
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

    /**
     * @return the username from the current screen
     */
    protected String getUsername() {
        return screen.getUsername();
    }
    
    @Override
    protected void showMessage(String msg) {
        messageUIThread.setMessage(msg);
        ((Activity)screen.getUiScreen()).runOnUiThread(messageUIThread);
    }

    public void login() {
        if(hasChanges() && AndroidAccountManager.getNativeAccount(
                (Activity)screen.getUiScreen()) != null) {
            ((Activity)screen.getUiScreen()).runOnUiThread(new AlertUIThread(
                    localization.getLanguage("alert_contacts_reset"),
                    new Runnable() {
                        public void run() {
                            saveAndCheck();
                        }
                    },
                    new Runnable() {
                        public void run() {
                        }
                    }));
        } else {
            saveAndCheck();
        }
    }

    public boolean hasChanges() {
        String serverUri;
        if (customization.syncUriEditable()) {
            serverUri = screen.getSyncUrl();
        } else {
            serverUri = customization.getServerUriDefault();
        }
        return hasChanges(serverUri, screen.getUsername(),
                screen.getPassword());
    }

    @Override
    public synchronized void synchronize(String syncType, Vector syncSources) {
        showProgressDialog(localization.getLanguage("checking_credentials_title"));
        super.synchronize(syncType, syncSources);
    }

    @Override
    public void syncEnded() {
        if (failed) {
            hideProgressDialog();
        }
        super.syncEnded();
    }

    protected void showProgressDialog(String message) {
        spinnerTriggerUIThread.setValue(true);
        spinnerTriggerUIThread.setMessage(message);
        ((Activity)screen.getUiScreen()).runOnUiThread(spinnerTriggerUIThread);
    }

    protected void hideProgressDialog() {
        spinnerTriggerUIThread.setValue(false);
        ((Activity)screen.getUiScreen()).runOnUiThread(spinnerTriggerUIThread);
    }

    @Override
    public void switchToSignupScreen() {
        // Build the extras paramater in order to give the current credentials
        // to the signup screen
        Bundle extras = new Bundle();
        extras.putString("syncurl", screen.getSyncUrl());
        extras.putString("username", screen.getUsername());
        extras.putString("password", screen.getPassword());
        try {
            ((AndroidDisplayManager)controller.getDisplayManager()).showScreen(
                    (Activity)screen.getUiScreen(), Controller.SIGNUP_SCREEN_ID, extras);
            controller.hideScreen(screen);
        } catch(Exception ex) {
            Log.error(TAG_LOG, "Unable to switch to login screen", ex);
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

        public void run() {
            if (value) {
                pDialogId = displayManager.showProgressDialog(screen, message);
            } else if(pDialogId != -1){
                displayManager.dismissProgressDialog(screen, pDialogId);
                pDialogId = -1;
            }
        }
    }

    private class AlertUIThread implements Runnable {

        private String question;
        private Runnable yesAction;
        private Runnable noAction;

        public AlertUIThread(String question,
                             Runnable yesAction,
                             Runnable noAction) {
            this.question = question;
            this.yesAction = yesAction;
            this.noAction = noAction;
        }

        public void run() {
            // Show alert
            controller.getDisplayManager().askYesNoQuestion(screen, question,
                    yesAction, noAction, 0);
        }
    }

    private class MessageUIThread implements Runnable {

        private String message;

        public void setMessage(String message) {
            this.message = message;
        }

        public void run() {
            // Show message
            displayManager.showOkDialog( screen, message,
                    localization.getLanguage("dialog_ok"));
        }
    }
}
