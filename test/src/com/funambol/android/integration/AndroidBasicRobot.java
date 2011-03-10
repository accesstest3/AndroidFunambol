/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2003 - 2009 Funambol, Inc.
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

import java.util.Hashtable;

import com.funambol.android.activities.AndroidHomeScreen;
import com.funambol.util.Log;

import android.view.KeyEvent;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Context;
import android.accounts.AccountManager;
import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import com.funambol.android.activities.AndroidDisplayManager;
import com.funambol.android.activities.AndroidLoginScreen;
import com.funambol.android.controller.AndroidController;
import com.funambol.android.controller.AndroidHomeScreenController;
import com.funambol.client.configuration.Configuration;
import com.funambol.client.controller.SynchronizationController;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.test.BasicCommandRunner;
import com.funambol.client.test.BasicRobot;
import com.funambol.client.test.BasicScriptRunner;
import com.funambol.client.test.CheckSyncClient;
import com.funambol.client.test.ClientTestException;
import com.funambol.client.test.SyncMonitor;
import com.funambol.client.test.SyncMonitorListener;
import com.funambol.syncml.spds.SyncConfig;
import com.funambol.syncml.spds.SyncListener;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.util.HttpTransportAgent;
import java.util.Enumeration;

public class AndroidBasicRobot extends BasicRobot {
   
    private static final String LOG_TAG = "AndroidBasicRobot";

    private Instrumentation instrumentation = null;

    private AndroidAppSyncSourceManager appSyncSourceManager = null;
    private Configuration configuration = null;

    private Hashtable<String,ActivityMonitor> activityMonitors =
            new Hashtable<String,ActivityMonitor>();

    public AndroidBasicRobot(Instrumentation instrumentation) {

        this.instrumentation = instrumentation;

        // Init monitors
        ActivityMonitor homeScreenMonitor  = instrumentation.addMonitor(AndroidHomeScreen.class.getName(), null, false);
        ActivityMonitor loginScreenMonitor = instrumentation.addMonitor(AndroidLoginScreen.class.getName(), null, false);
        
        activityMonitors.put(AndroidHomeScreen.class.getName(),    homeScreenMonitor);
        activityMonitors.put(AndroidLoginScreen.class.getName(), loginScreenMonitor);
    }
    
    @Override
    public void initialize() {
        AppInitializer initializer = AppInitializer.getInstance(instrumentation.getTargetContext());
        appSyncSourceManager = initializer.getAppSyncSourceManager();
        configuration = initializer.getConfiguration();

        AndroidController.getInstance().getHomeScreenController()
                .getSyncEngine().setTransportAgent(
                BasicScriptRunner.createTestTransportAgent(
                configuration.getSyncConfig()));
    }

    public void waitForActivity(String activityName, int timeout) throws Throwable {

        String activityCompleteName = "com.funambol.android.activities." + activityName;
        ActivityMonitor monitor = getActivityMonitor(activityCompleteName);
        if(monitor == null) {
            throw new ClientTestException("Invalid activityName: " + activityName);
        }

        Activity current = instrumentation.waitForMonitorWithTimeout(monitor, timeout*1000);

        if(current == null) {
            throw new ClientTestException("Timeout waiting activity: " + activityCompleteName);
        } else if(!current.getComponentName().getClassName().equals(activityCompleteName)) {
            throw new ClientTestException("Wrong returned activity: " +
                    current.getComponentName().getClassName() + " Expected: " +
                    activityCompleteName);
        }

        // The monitor was removed after waiting it
        instrumentation.addMonitor(monitor);
    }

    public void keyPress(String keyName, int count) throws Throwable {
        int keyCode = 0;
        if (BasicCommandRunner.DOWN_KEY_NAME.equals(keyName)) {
            keyCode = KeyEvent.KEYCODE_DPAD_DOWN;
        } else if (BasicCommandRunner.UP_KEY_NAME.equals(keyName)) {
            keyCode = KeyEvent.KEYCODE_DPAD_UP;
        } else if (BasicCommandRunner.LEFT_KEY_NAME.equals(keyName)) {
            keyCode = KeyEvent.KEYCODE_DPAD_LEFT; 
        } else if (BasicCommandRunner.RIGHT_KEY_NAME.equals(keyName)) {
            keyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
        } else if (BasicCommandRunner.FIRE_KEY_NAME.equals(keyName)) {
            keyCode = KeyEvent.KEYCODE_DPAD_CENTER;
        } else if (BasicCommandRunner.MENU_KEY_NAME.equals(keyName)) {
            keyCode = KeyEvent.KEYCODE_MENU;
        } else if (BasicCommandRunner.BACK_KEY_NAME.equals(keyName)) {
            keyCode = KeyEvent.KEYCODE_BACK;
        } else if (BasicCommandRunner.DEL_KEY_NAME.equals(keyName)) {
            keyCode = KeyEvent.KEYCODE_DEL;
        } else {
            Log.error(LOG_TAG, "Unknown keyName: " + keyName);
            throw new IllegalArgumentException("Unknown keyName: " + keyName);
        }
        for(int i=0; i<count; i++) {
            instrumentation.sendCharacterSync(keyCode);
            delay(500);
        }
    }

    public void writeString(String text) throws Throwable {
        instrumentation.sendStringSync(text);
        delay(500);
    }

    public static void waitDelay(int d) {
        delay(d * 1000);
    }

    public static void removeAccount(Context context) throws Throwable {
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(context.getString(R.string.account_type));
        for(int i=0; i<accounts.length; i++) {
            am.removeAccount(accounts[i], null, null);
        }
        SharedPreferences settings = context.getSharedPreferences(
                AndroidConfiguration.KEY_FUNAMBOL_PREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }

    private ActivityMonitor getActivityMonitor(String activity) {
        return activityMonitors.get(activity);
    }

    protected void saveSourceConfig(SyncSource source) {
        // Not implemented
    }

    protected void reapplySyncConfig(CheckSyncClient client) {
        if(configuration != null) {
            String syncUrl  = configuration.getSyncUrl();
            String userName = configuration.getUsername();
            String password = configuration.getPassword();
            client.setSyncConfig(syncUrl, userName, password);
        }
    }

    protected AppSyncSource getAppSyncSource(String sourceName) throws Exception {
        AppSyncSource source = null;
        if(CheckSyncClient.SOURCE_NAME_CONTACTS.equals(sourceName)) {
            source = appSyncSourceManager.getSource(AndroidAppSyncSourceManager.CONTACTS_ID);
        } else if(CheckSyncClient.SOURCE_NAME_CALENDAR.equals(sourceName)) {
            source = appSyncSourceManager.getSource(AndroidAppSyncSourceManager.EVENTS_ID);
        } else if(CheckSyncClient.SOURCE_NAME_PICTURES.equals(sourceName)) {
            source = appSyncSourceManager.getSource(AndroidAppSyncSourceManager.PICTURES_ID);
        } else {
            Log.error(LOG_TAG, "Unknown source: " + sourceName);
            throw new IllegalArgumentException("Unknown source: " + sourceName);
        }
        return source;
    }

    protected SyncSource getSyncSource(String sourceName) throws Exception {
        return getAppSyncSource(sourceName).getSyncSource();
    }

    public void waitForSyncToComplete(String sourceName, int minStart, int max,
            AndroidSyncMonitor syncMonitor) throws Throwable {

        Log.debug(LOG_TAG, "waiting for sync to complete for source: " + sourceName);
        
        String authority = ((AndroidAppSyncSource)getAppSyncSource(sourceName))
                .getAuthority();

        // We wait no more than minStart for sync client to start
        while(!syncMonitor.isSyncing(authority)) {
            Thread.sleep(WAIT_DELAY);
            minStart -= WAIT_DELAY;
            if (minStart < 0) {
                throw new ClientTestException("Sync did not start within time limit");
            }
        }

        // Now wait until the busy is in progress for a max amount of time
        while(syncMonitor.isSyncing(authority)) {
            Thread.sleep(WAIT_DELAY);
            max -= WAIT_DELAY;
            if (max < 0) {
                throw new ClientTestException("Sync did not complete before timeout");
            }
        }
    }

    public void waitForAuthToComplete(int minStart, int max,
            SyncMonitor syncMonitor) throws Throwable {

        AndroidController ac = AndroidController.getInstance();

        // Get the AccountScreenController from te global controller
        super.waitForSyncToComplete(minStart, max,
                new AndroidSyncMonitor(ac.getLoginScreenController()));

        // Update the sync config
        AndroidController.getInstance().getHomeScreenController()
                .getSyncEngine().setTransportAgent(
                BasicScriptRunner.createTestTransportAgent(
                configuration.getSyncConfig()));
    }

    public void checkSyncPending(String sourceName, boolean checkPending) throws Throwable {
        AndroidAppSyncSource source = (AndroidAppSyncSource)getAppSyncSource(sourceName);
        String authority = source.getAuthority();

        boolean pending = ContentResolver.isSyncPending(AndroidController.getNativeAccount(), authority);
        boolean active = ContentResolver.isSyncActive(AndroidController.getNativeAccount(), authority);

        Log.debug(LOG_TAG, "Checking pending sync for authority: " + authority);
        Log.debug(LOG_TAG, "Pending sync: " + pending);
        Log.debug(LOG_TAG, "Active sync: " + active);

        boolean isSyncPending = pending;
        if(isSyncPending != checkPending) {
            if(checkPending) {
                throw new ClientTestException("Cannot find pending sync for source: " + sourceName +
                    " with authority: " + authority);
            } else {
                throw new ClientTestException("Found pending sync for source: " + sourceName +
                    " with authority: " + authority);
            }
         }
    }

    public void cancelSync() throws Throwable {
        AndroidHomeScreenController c = (AndroidHomeScreenController)
                AndroidController.getInstance().getHomeScreenController();
        c.cancelMenuSelected();
    }

    public void setAutoSyncEnabled(boolean enabled) throws Throwable {
        ContentResolver.setMasterSyncAutomatically(enabled); 
    }

    public void setSourceAutoSyncEnabled(String sourceName, boolean enabled) throws Throwable {
        AndroidAppSyncSource source = (AndroidAppSyncSource)getAppSyncSource(sourceName);
        String authority = source.getAuthority();
        ContentResolver.setSyncAutomatically(AndroidAccountManager.getNativeAccount(
                instrumentation.getTargetContext()), authority, enabled);
    }

    public void checkSourceAutoSyncEnabled(String sourceName, boolean enabled) throws Throwable {
        AndroidAppSyncSource source = (AndroidAppSyncSource)getAppSyncSource(sourceName);
        String authority = source.getAuthority();
        boolean autoSync = ContentResolver.getSyncAutomatically(AndroidAccountManager.getNativeAccount(
                instrumentation.getTargetContext()), authority);
        if(autoSync != enabled) {
            if(autoSync) {
                throw new ClientTestException("Auto sync is enabled for source: " + sourceName +
                    " with authority: " + authority);
            } else {
                throw new ClientTestException("Auto sync is disabled for source: " + sourceName +
                    " with authority: " + authority);
            }
        }
    }

    public void cancelSyncAfterPhase(String phaseName, int num, SyncMonitor syncMonitor) throws Throwable {
        Log.debug(LOG_TAG, "Preparing to interrupt sync after phase " + phaseName + "," + num);

        // Register the listeners to monitor the sync execution
        Enumeration workingSources = appSyncSourceManager.getWorkingSources();
        syncMonitor.cleanListeners();
        while(workingSources.hasMoreElements()) {
            AppSyncSource appSource = (AppSyncSource)workingSources.nextElement();
            SyncSource    source    = appSource.getSyncSource();
            SyncListener  lis       = source.getListener();
            Log.info(LOG_TAG, "Registering monitoring listener for source " + source.getName());
            if (lis != null) {
                // Replace the listener with a monitoring one
                AndroidSyncMonitorListener monLis = new AndroidSyncMonitorListener(lis,
                        AndroidController.getInstance().getHomeScreenController());
                source.setListener(monLis);
                syncMonitor.addListener(monLis);
                Log.info(LOG_TAG, "Monitoring listener registered");
            }
        }
        syncMonitor.interruptSyncAfterPhase(phaseName, num, "Cancel sync");
    }

    public void waitForSyncPhase(String phaseName, int num, int timeout,
            SyncMonitor syncMonitor) throws Throwable {
        Log.debug(LOG_TAG, "Waiting for sync phase " + phaseName + "," + num);

        final Object syncInterruptedMonitor = new Object();

        synchronized(syncInterruptedMonitor) {
            // Register the listeners to monitor the sync execution
            Enumeration workingSources = appSyncSourceManager.getWorkingSources();
            syncMonitor.cleanListeners();
            while(workingSources.hasMoreElements()) {
                AppSyncSource appSource = (AppSyncSource)workingSources.nextElement();
                SyncSource    source    = appSource.getSyncSource();
                SyncListener  lis       = source.getListener();
                Log.info(LOG_TAG, "Registering monitoring listener for source " + source.getName());
                if (lis != null) {
                    // Replace the listener with a monitoring one
                    SyncMonitorListener monLis = new SyncMonitorListener(lis) {
                        @Override
                        protected void interruptSync(String reason) {
                            synchronized(syncInterruptedMonitor) {
                                syncInterruptedMonitor.notifyAll();
                            }
                        }
                    };
                    source.setListener(monLis);
                    syncMonitor.addListener(monLis);
                    Log.info(LOG_TAG, "Monitoring listener registered");
                }
            }
            syncMonitor.interruptSyncAfterPhase(phaseName, num, null);

            // Wait for sync phase to be reached
            try {
                syncInterruptedMonitor.wait(timeout);
            }catch(Exception ex) {
                Log.error(LOG_TAG, "Ex: ", ex);
                ex.printStackTrace();
            }
        }
    }

    public void checkLastAlertMessage(String message) throws Throwable {
        AndroidDisplayManager dm = (AndroidDisplayManager)
                AndroidController.getInstance().getDisplayManager();
        String lastMessage = dm.readAndResetLastMessage();
        assertTrue(lastMessage, message, "Last alert message mismatch");
    }

    public void addPicture(String path) throws Throwable {

        SyncConfig config = new SyncConfig();
        config.syncUrl = path;
        config.userAgent = null;
        config.compress = false;
        config.forceCookies = false;
        HttpTransportAgent ta = BasicScriptRunner.createTestTransportAgent(config);

        byte[] response = ta.sendMessage("".getBytes());

        String filename = path.substring(path.lastIndexOf('/')+1);

        String uri = MediaStore.Images.Media.insertImage(instrumentation.getContext().getContentResolver(),
                BitmapFactory.decodeByteArray(response, 0, response.length), filename, filename);

        // Update the picture size in the db
        ContentResolver cr = instrumentation.getTargetContext().getContentResolver();
        int size = cr.openInputStream(Uri.parse(uri)).available();
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.SIZE, size);
        cr.update(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv, 
                MediaStore.Images.Media.TITLE+"='"+filename+"'", null);
    }

    public void deletePicture(String filename) {
        ContentResolver cr = instrumentation.getTargetContext().getContentResolver();
        cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
                MediaStore.Images.Media.TITLE+"='"+filename+"'", null);
    }

    private static void delay(int msec) {
        try {
            Thread.sleep(msec);
        } catch (Exception e) {
            Log.error(LOG_TAG, "Exception Occurred while sleeping", e);
        }
    }



    private class AndroidSyncMonitorListener extends SyncMonitorListener {

        private SynchronizationController scontroller;
        
        public AndroidSyncMonitorListener(SyncListener lis, SynchronizationController scontroller) {
            super(lis);
            this.scontroller = scontroller;
        }

        /**
         * Override default implementation in order to cancel the synchronization
         * instead of throw an Exception.
         * 
         * @param reason
         */
        @Override
        protected void interruptSync(String reason) {
            interruptOnPhase = null;
            interruptOnPhaseNumber = -1;
            interruptReason = null;
            scontroller.cancelSync();
        }
    }
}
