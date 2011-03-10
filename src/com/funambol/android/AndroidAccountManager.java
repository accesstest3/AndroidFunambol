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

package com.funambol.android;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.BroadcastReceiver;
import android.content.ContentValues;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;

import com.funambol.android.activities.AndroidDisplayManager;
import com.funambol.android.controller.AndroidController;
import com.funambol.client.configuration.Configuration;
import com.funambol.client.controller.AboutScreenController;
import com.funambol.client.controller.AccountScreenController;
import com.funambol.client.controller.HomeScreenController;
import com.funambol.client.controller.SignupScreenController;
import com.funambol.client.controller.SyncSettingsScreenController;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.ui.Screen;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncListener;
import com.funambol.util.Log;

import java.util.Enumeration;

/**
 * A BroadcastReceiver implementation sensible to account changes
 */
public class AndroidAccountManager extends BroadcastReceiver {

    private static final String TAG_LOG = "AndroidAccountManager";
    
    private AndroidDisplayManager dm = new AndroidDisplayManager();

    private static boolean ignoreAccountChanges = false;

    /**
     * Implementation of the super class abstract method: define what tpo do
     * when this BroadcastReceiver is invoked.
     * @param context the Context object
     * @param intent the Intent action to be triggered
     */
    public void onReceive(Context context, Intent intent) {

        //Does anything if another operation is pending
        if(ignoreAccountChanges) {
            return;
        }

        //Get the native account reference
        Account account = AndroidController.getNativeAccount();

        if(account == null && AndroidController.isInitialized()) {
            AndroidController controller = AndroidController.getInstance();

            AboutScreenController aboutController = controller.getAboutScreenController();
            if(aboutController != null) {
                hideScreen(aboutController.getAboutScreen());
            }

            SyncSettingsScreenController settingsController = controller.getSyncSettingsScreenController();
            if(settingsController != null) {
                hideScreen(settingsController.getSyncSettingsScreen());
            }

            HomeScreenController homeController = controller.getHomeScreenController();
            if(homeController != null) {
                hideScreen(homeController.getHomeScreen());
            }

            AccountScreenController loginController = controller.getLoginScreenController();
            if(loginController != null) {
                hideScreen(loginController.getAccountScreen());
            }

            Configuration configuration = controller.getConfiguration();
            configuration.setCredentialsCheckPending(true);
            configuration.save();

            // Dispose all the singletons
            AppInitializer             .dispose();
            AndroidController          .dispose();
            AndroidConfiguration       .dispose();
            AndroidCustomization       .dispose();
            AndroidLocalization        .dispose();
            AndroidAppSyncSourceManager.dispose();
        }
    }

    /**
     * Use the DisplayManager to hide the given screen
     * @param s is the Screen to be hidden
     */
    private void hideScreen(Screen s) {
        try {
            if(s != null) dm.hideScreen(s);
        } catch(Exception ex) {
            android.util.Log.e(TAG_LOG, "Error while closing screen: " + ex);
        }
    }

    /**
     * When an operation has begun on an account advise the receiver to ignore
     * any other concurrent change
     */
    public static void beginAccountOperation() {
        ignoreAccountChanges = true;
    }

    /**
     * When an operation has ended on an account advise the receiver to listen
     * for any further account change
     */
    public static void endAccountOperation() {
        ignoreAccountChanges = false;
    }

    /**
     * Retrieve the Android account reference from the native account manager.
     * @param context the Context object
     * @return Account the NATIVE android.accounts.Account reference - the first
     * element of the account array that matches the account type for the
     * application
     */
    public static Account getNativeAccount(Context context) {

        //Needs a reference object
        if(context == null) {
            return null;
        }

        //Retrieve the AccountManager
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(context.getString(
                R.string.account_type));
        if(accounts != null && accounts.length > 0) {
            //Return the first element of the account array that matches the
            //account type for the application
            return accounts[0];
        } else {
            return null;
        }
    }

    /**
     * Adds a new Funambol account given the username. If any account already
     * exist, it will be removed.
     * 
     * @param accountName
     * @param screen
     */
    public static void addNewFunambolAccount(String accountName,
            AccountAuthenticatorActivity screen) {
        
        AccountManager am = AccountManager.get(screen);
        Account existingAccount = AndroidController.getNativeAccount();

        if (existingAccount != null) {
            beginAccountOperation();
            try {
                AccountManagerFuture<Boolean> result = am.removeAccount(
                        existingAccount, null, null);
                result.getResult();

                // Reset source anchors
                Log.debug(TAG_LOG, "Reset source anchors");
                AndroidController gc = AndroidController.getInstance();
                Enumeration sources = gc.getAppSyncSourceManager()
                        .getWorkingSources();

                while(sources.hasMoreElements()) {
                    AppSyncSource ass = (AppSyncSource)sources.nextElement();
                    ass.getConfig().setLastSyncStatus(SyncListener.SUCCESS);
                    if (ass.getUISyncSource() != null) {
                        ass.getUISyncSource().setStatusIcon(null);
                    }

                    Log.debug(TAG_LOG, "Reset anchors for source: " + ass.getName());
                    SourceConfig sc = ass.getSyncSource().getConfig();
                    sc.setLastAnchor(0);
                    sc.setNextAnchor(0);

                    // Reset the last timestamp so that the source is shown as
                    // not synchronized
                    ass.getConfig().setLastSyncTimestamp(0);
                    ass.getConfig().saveSourceSyncConfig();
                    ass.getConfig().save();
                }
            } catch(Exception ex) {
                Log.error(TAG_LOG, "Failed to remove existing account", ex);
            }
            endAccountOperation();
        }

        // Add the account if necessary
        // If the account is not available, we need to create it
        Log.info(TAG_LOG, "Adding account explicitly");

        //Set the account type
        String accountType = screen.getString(R.string.account_type);

        // Create the new account
        Account account = new Account(accountName, accountType);

        if (am.addAccountExplicitly(account, null, null)) {

            Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
            screen.setAccountAuthenticatorResult(result);

            // Set the new created account contacts to be displayed in the addressbook
            ContentValues cv = new ContentValues();
            cv.put(ContactsContract.Settings.ACCOUNT_NAME, accountName);
            cv.put(ContactsContract.Settings.ACCOUNT_TYPE, accountType);
            cv.put(ContactsContract.Settings.UNGROUPED_VISIBLE, 1);
            screen.getContentResolver().insert(ContactsContract.Settings.CONTENT_URI, cv);
        }

        AndroidController controller = AndroidController.getInstance();
        HomeScreenController homeScreenController = controller.getHomeScreenController();
        // The existance of the account may impact on the availability of
        // some sources. Therefore we inform the home screen controller when
        // the account gets created
        if(homeScreenController != null) {
            homeScreenController.updateAvailableSources();
            homeScreenController.redraw();
        }
    }
}
