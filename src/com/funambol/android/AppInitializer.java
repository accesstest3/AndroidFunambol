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

package com.funambol.android;

import java.util.Enumeration;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.content.Context;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.Account;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.ContactsContract;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

import com.funambol.android.controller.AndroidHomeScreenController;
import com.funambol.android.controller.AndroidController;
import com.funambol.android.activities.AndroidActivitiesFactory;
import com.funambol.android.activities.AndroidDisplayManager;
import com.funambol.android.controller.AndroidSettingsScreenController;
import com.funambol.android.source.pim.PimTestRecorder;
import com.funambol.android.edit_contact.AndroidEditContact;
import com.funambol.android.source.pim.PimTestRecorder;

import com.funambol.util.FileAppender;
import com.funambol.util.MultipleAppender;
import com.funambol.util.AndroidLogAppender;

import com.funambol.client.controller.Controller;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.localization.Localization;
import com.funambol.client.customization.Customization;
import com.funambol.storage.StringKeyValueSQLiteStore;
import com.funambol.storage.StringKeyValueStore;
import com.funambol.syncml.spds.MappingManager;
import com.funambol.syncml.spds.MappingStoreBuilder;

import com.funambol.platform.NetworkStatus;

import com.funambol.util.Log;

/**
 * This class is used to initialize the entire application. It can be invoked by
 * a starting activity or by a service. Once the application got initialized
 * once, any call to init has no effect. The Singeton instance is realizaed, so
 * this class refrence can be got using the static related getter method.
 */
public class AppInitializer {

    private static final String TAG_LOG = "AppInitializer";

    private Localization localization;
    private AndroidConfiguration configuration;
    private Customization customization;
    private AndroidAppSyncSourceManager appSyncSourceManager;
    private AndroidController controller;
    private Context context;
    private WifiLock wifiLock = null;
    private SyncLock syncLock;

    private static AppInitializer instance = null;
    private static boolean initialized = false;


    /**
     * Private constructor to enforce the singleton pattern realization
     * @param context the application Context object
     */
    private AppInitializer(Context context) {
        this.context = context;
    }

    /**
     * Static getter method to retrieve the singelton instance of this class.
     * @param context the application Context object
     * @return AppInitializer the AppInitializer unique instance
     */
    public static AppInitializer getInstance(Context context) {
        if (instance == null) {
            instance = new AppInitializer(context);
        }
        return instance;
    }

    /**
     * Dispose this object setting its reference to the null object. Once
     * the instance is disposed it must be riinitialized in order to be reused
     * the following times.
     */
    public static void dispose() {
        instance = null;
        initialized = false;
    }

    /**
     * AndroidController instance Getter method
     * @return AndroidController the instance of AndroidController object
     * initialized by this class
     */
    public AndroidController getController() {
        return controller;
    }
    
    /**
     * AndroidLocalization instance Getter method
     * @return AndroidLocalization the instance of AndroidLocalization object
     * used by this class
     */
    public Localization getLocalization() {
        return localization;
    }

    /**
     * AndroidConfiguration instance Getter method
     * @return AndroidConfiguration the instance of AndroidConfiguration object
     * used by this class
     */
    public AndroidConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * AndroidCustomization instance Getter method
     * @return AndroidCustomization the instance of AndroidCustomization object
     * used by this class
     */
    public Customization getCustomization() {
        return customization;
    }

    public SyncLock getSyncLock() {
        return syncLock;
    }

    /**
     * AndroidAppSyncSourceManager instance Getter method
     * @return AndroidAppSyncSourceManager the instance of
     * AndroidAppSyncSourceManager initialized by this class
     */
    public AndroidAppSyncSourceManager getAppSyncSourceManager() {
        return appSyncSourceManager;
    }

    private void initLog() {
        MultipleAppender ma = new MultipleAppender();
        String fileName = "synclog.txt";
        String userDir;
        
        if (isSDCardMounted()) {
            userDir =  Environment.getExternalStorageDirectory().getPath() +
                    System.getProperty("file.separator");
        } else {
            userDir = context.getFilesDir().getAbsolutePath() +
                    System.getProperty("file.separator");
        }
        
        FileAppender fileAppender = new FileAppender(userDir, fileName);
        fileAppender.setLogContentType(!isSDCardMounted());
        fileAppender.setMaxFileSize(256*1024); // Set 256KB log size
        ma.addAppender(fileAppender);

        // If we are running in the emulator, we also use the AndroidLogger
        TelephonyManager tm = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        // must have android.permission.READ_PHONE_STATE
        String deviceId = tm.getDeviceId();
        if ("000000000000000".equals(deviceId) || "debug".equals(BuildInfo.MODE)) {
            // This is an emulator, or a debug build
            AndroidLogAppender androidLogAppender = new AndroidLogAppender("FunambolSync");
            ma.addAppender(androidLogAppender);
        }

        Log.initLog(ma, Log.TRACE);
        
        //for customer who wants to have log locked on specified level
        if(customization.lockLogLevel()){
            Log.lockLogLevel(customization.getLockedLogLevel());
        }
        Log.info("Memory card present: " + isSDCardMounted());
        Log.info(TAG_LOG, "Log file created into: " + userDir + fileName);
    }

    /**
     * Initialize the application. Call this method to have the main application
     * objects fully initialized.
     * Call this method more than once per instance produces no effect.
     * This realize the same effect of init(Activity activity) but passing a
     * null value and generating the condition to not create the funambol
     * account. Use it carefully.
     */
    public void init() {
        init(null);
    }

    /**
     * Initialize the application. Call this method to have the main application
     * objects fully initialized. Call this method more than once per instance
     * produces no effect.
     * 
     * @param Activity is the Activity that is used to trigger the account 
     * creation if it doesn't exist. Pass null if you don't want to create the
     * account.
     */
    public void init(Activity activity) {

        if (initialized) {
            if(configuration.getCredentialsCheckPending()) {
                // Show the login screen if the credential check is still pending
                initAccount(activity);
            }
            return;
        }

        // Init all the Controller components
        initController();

        // Init account information
        initAccount(activity);

        // Init the SyncLock
        syncLock = new SyncLock();
        
        // Reset the proper log level
        Log.setLogLevel(configuration.getLogLevel());

        // Init the wifi lock if we need one
        initWifiLock();

        // Init the TestRecorder if we are in test recording mode ///////////////
        if (BuildInfo.TEST_RECORDING_ENABLED) {
            PimTestRecorder recorder = PimTestRecorder.getInstance(context, controller);
        }
        ////////////////////////////////////////////////////////////////////////////

        initialized = true;
    }

    /**
     * Initializes the controller of this application. this represents the core
     * of the initialization logic; in particular:
     *  - Init the log system
     *  - Set our own contacts activity as preferred
     *  - Try to create all the necessary sources
     *  - Create the home screen controller
     *  - Set the HomeScreenController reference to the Controller
     */
    public void initController() {

        if(controller == null) {
             customization = AndroidCustomization.getInstance();
            // Init the log system
            initLog();

            // Set our own contacts activity as preferred
            setPreferredContactsActivity();

            localization = AndroidLocalization.getInstance(context);
           
            appSyncSourceManager = AndroidAppSyncSourceManager.getInstance(
                    customization, localization, context);

            configuration = AndroidConfiguration.getInstance(context,
                    customization, appSyncSourceManager);
            configuration.load();
            
            controller = AndroidController.getInstance(context, 
                    new AndroidActivitiesFactory(), configuration, customization,
                    localization, appSyncSourceManager);

            configuration.setController(controller);

            // Try to create all the necessary sources
            Enumeration sources = customization.getAvailableSources();
            while(sources.hasMoreElements()) {
                Integer appSourceId = (Integer)sources.nextElement();

                try {
                    AppSyncSource appSource = appSyncSourceManager.setupSource(appSourceId.intValue(), configuration);
                    appSyncSourceManager.registerSource(appSource);
                } catch (Exception e) {
                    Log.error(TAG_LOG, "Cannot setup source: " + appSourceId, e);
                }
            }

            // We must initialize the mapping store builder so that the
            // MappingManager will use a persistable store (SQLite)
            MappingStoreBuilder mappingStoreBuilder = new AndroidMappingStoreBuilder();
            MappingManager.setStoreBuilder(mappingStoreBuilder);
        }

        // Create the home screen controller
        AndroidActivitiesFactory controllerFactory = new AndroidActivitiesFactory();
        
        controller = AndroidController.getInstance(context,
                                                   controllerFactory,
                                                   configuration,
                                                   customization,
                                                   localization,
                                                   appSyncSourceManager);
        configuration.setController(controller);

        NetworkStatus netStatus = new NetworkStatus(context);
        AndroidHomeScreenController homeScreenController;
        homeScreenController = new AndroidHomeScreenController(context, controller, null, netStatus);
        
        // Set the HomeScreenController reference to the Controller
        controller.setHomeScreenController(homeScreenController);

        AndroidSettingsScreenController settingsScreenController = new AndroidSettingsScreenController(context, controller);
        controller.setSettingsScreenController(settingsScreenController);

        initialized = true;
    }

    public void acquireWiFiLock() {
        
        if (wifiLock == null) {
            WifiManager wm = (WifiManager) context.getSystemService (Context.WIFI_SERVICE);
            wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "Funambol sync");
            wifiLock.setReferenceCounted(false);
            wifiLock.acquire();
            Log.info(TAG_LOG, "wifiLock=" + wifiLock.toString());
        }
    }

    public void releaseWiFiLock() {
        
        if (wifiLock != null) {
            Log.info(TAG_LOG, "Releasing wifi lock");
            wifiLock.release();
        }
    }

    private void initWifiLock() {
        if (configuration.getBandwidthSaverActivated()) {
            acquireWiFiLock();
        }
    }

    private void initAccount(Activity activity) {

        Account account = AndroidController.getNativeAccount();

        // Do nothing if the request doesn't come from an activity
        if(activity == null) {
            return;
        }

        // Check if there is not funambol account
        if(account == null) {
            Log.debug(TAG_LOG, "Account not found, create a default one");
            // Create the account through our account authenticator
            AccountManager am  = AccountManager.get(context);
            am.addAccount( context.getString(R.string.account_type), null, null, null, activity,
                new AccountManagerCallback<Bundle>() {
                    public void run(AccountManagerFuture<Bundle> result) {
                        try {
                            // Get the authenticator result, it is blocking until the
                            // account authenticator completes
                            result.getResult();
                            Log.debug(TAG_LOG, "Account created");
                        } catch (Exception e) {
                            Log.error(TAG_LOG, "Exception during account creation: ", e);
                        }
                    }
                }, null);
        } else {
            Log.debug(TAG_LOG, "Account already defined");
            // Show the login screen if credentials check is pending and
            // if the init request comes from the main activity
            if(configuration.getCredentialsCheckPending()) {
                try {
                    ((AndroidDisplayManager)controller.getDisplayManager())
                            .showScreen(context, Controller.LOGIN_SCREEN_ID, null);
                } catch(Exception ex) {
                    Log.error(TAG_LOG, "Cannot show login screen", ex);
                }
            }
        }
    }

    private void setPreferredContactsActivity() {
        
        PackageManager pm = context.getPackageManager();

        // Remove the native contacts app as default
        pm.clearPackagePreferredActivities("com.android.contacts");

        Intent edit_intent = new Intent("android.intent.action.EDIT");
        Intent insert_intent = new Intent("android.intent.action.INSERT");

        edit_intent.addCategory("android.intent.category.DEFAULT");
        insert_intent.addCategory("android.intent.category.DEFAULT");

        edit_intent.setData(ContentUris.withAppendedId(
                ContactsContract.RawContacts.CONTENT_URI, 100));
        insert_intent.setData(ContactsContract.RawContacts.CONTENT_URI);

        List<ResolveInfo> editList = pm.queryIntentActivities(
                      edit_intent, PackageManager.MATCH_DEFAULT_ONLY |
                      PackageManager.GET_RESOLVED_FILTER );

        List<ResolveInfo> insertList = pm.queryIntentActivities(
                      insert_intent, PackageManager.MATCH_DEFAULT_ONLY |
                      PackageManager.GET_RESOLVED_FILTER );

        ResolveInfo editRI = null;
        ResolveInfo insertRI = null;

        ComponentName[] editRIS = new ComponentName[editList.size()];
        ComponentName[] insertRIS = new ComponentName[insertList.size()];
        
        if(editList.size() > 0) {
            editRI = editList.get(0);
            for (int i=0; i<editList.size(); i++) {
                ResolveInfo r = editList.get(i);
                editRIS[i] = new ComponentName(r.activityInfo.packageName,
                      r.activityInfo.name);
            }
        }
        if(insertList.size() > 0) {
            insertRI = insertList.get(0);
            for (int i=0; i<insertList.size(); i++) {
                ResolveInfo r = insertList.get(i);
                insertRIS[i] = new ComponentName(r.activityInfo.packageName,
                      r.activityInfo.name);
            }
        }
        
        pm.addPreferredActivity(editRI.filter, editRI.match , editRIS,
                  new ComponentName(BuildInfo.PACKAGE_NAME,
        "com.funambol.android.edit_contact.AndroidEditContact"));
        pm.addPreferredActivity(insertRI.filter, insertRI.match , insertRIS,
                  new ComponentName(BuildInfo.PACKAGE_NAME,
        "com.funambol.android.edit_contact.AndroidEditContact"));
    }

    private boolean isSDCardMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Defines a custom MappingStoreBuilder in order to create use
     * a StringKeyValueSQLiteStore store mapping data.
     *
     * Tables will be created with the "mappings_" prefix for each source.
     */
    private class AndroidMappingStoreBuilder extends MappingStoreBuilder {

        private static final String MAPPING_TABLE_PREFIX = "mappings_";
        
        public AndroidMappingStoreBuilder() { }

        @Override
        public StringKeyValueStore createNewStore(String name) {
            return new StringKeyValueSQLiteStore(context, 
                    ((AndroidCustomization)customization).getFunambolSQLiteDbName(),
                    MAPPING_TABLE_PREFIX + name);
        }
    }
}
