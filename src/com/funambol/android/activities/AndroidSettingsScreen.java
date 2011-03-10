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

package com.funambol.android.activities;

import android.accounts.AccountAuthenticatorActivity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.widget.TabHost;
import android.widget.Button;

import android.widget.RelativeLayout;
import android.widget.TextView;
import com.funambol.android.AndroidCustomization;
import com.funambol.android.R;
import com.funambol.android.activities.settings.AndroidSettingsTab;
import com.funambol.android.activities.settings.AndroidSyncSettingsTab;
import com.funambol.android.activities.settings.AndroidAdvancedSettingsTab;
import com.funambol.android.activities.settings.SaveSettingsCallback;
import com.funambol.android.controller.AndroidController;
import com.funambol.android.controller.AndroidHomeScreenController;
import com.funambol.android.controller.AndroidSettingsScreenController;

import com.funambol.client.localization.Localization;
import com.funambol.client.ui.Screen;

import com.funambol.util.Log;

import java.util.List;
import java.util.ArrayList;

/**
 * This is the container Activity for all the settings (Sync, Advanced)
 */
public class AndroidSettingsScreen extends AccountAuthenticatorActivity 
        implements Screen {

    private static final String TAG = "AndroidSettingsScreen";

    private TabHost tabs;

    private Localization localization;
    private AndroidCustomization customization;

    private AndroidDisplayManager displayManager;

    private static final String LAST_SELECTED_TAB_ID = "LastSelectedTabId";

    private static final String REFRESH_DIRECTION_ALERT_PENDING = "RefreshDirectionAlert";

    private static final String REFRESH_TYPE_ALERT_PENDING = "RefreshTypeAlert";

    private List<AndroidSettingsTab> settingsTabList = new ArrayList<AndroidSettingsTab>();

    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidController gc = AndroidController.getInstance();
        this.localization = gc.getLocalization();
        this.customization = (AndroidCustomization)gc.getCustomization();
        this.displayManager = (AndroidDisplayManager)gc.getDisplayManager();
        
        setContentView(R.layout.settings);

        setupAllTabs(savedInstanceState);

        // Init button listeners
        Button saveButton = (Button)findViewById(R.id.save_button);
        saveButton.setOnClickListener(new SaveListener());

        Button cancelButton = (Button)findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new CancelListener());

        int directionAlertId = 0;

        int typeAlertId = 0;

        if (savedInstanceState != null) {
            directionAlertId = savedInstanceState.getInt(REFRESH_DIRECTION_ALERT_PENDING);
            typeAlertId = savedInstanceState.getInt(REFRESH_TYPE_ALERT_PENDING);
        }

        AndroidSettingsScreenController settingsScreenController = gc.getSettingsScreenController();
        settingsScreenController.setSettingsScreen(this);

        //Resume the last sync dialog alert if it was displayed before
        //resuming this activity
        if (directionAlertId == AndroidDisplayManager.REFRESH_DIRECTION_DIALOG_ID) {
            savedInstanceState.remove(REFRESH_DIRECTION_ALERT_PENDING);
            gc.getDialogController().showRefreshDirectionDialog(this);
        } else if (typeAlertId == AndroidDisplayManager.REFRESH_TYPE_DIALOG_ID) {
            savedInstanceState.remove(REFRESH_TYPE_ALERT_PENDING);
            gc.getDialogController().resumeLastRefreshTypeDialog(this);
        }
        
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        AndroidController gc = AndroidController.getInstance();

        super.onSaveInstanceState(outState);

        Log.trace(TAG, "onSaveInstanceState");

        // Save the current selected tab
        int id = tabs.getCurrentTab();
        outState.putInt(LAST_SELECTED_TAB_ID, id);

        //Notifies to the Bundle resorce if a selection alert is pending (For example after screen rotation)
        if (displayManager.isAlertPending(AndroidDisplayManager.REFRESH_DIRECTION_DIALOG_ID)) {
            displayManager.dismissSelectionDialog(AndroidDisplayManager.REFRESH_DIRECTION_DIALOG_ID);
            outState.putInt(REFRESH_DIRECTION_ALERT_PENDING, AndroidDisplayManager.REFRESH_DIRECTION_DIALOG_ID);
        } else if (displayManager.isAlertPending(AndroidDisplayManager.REFRESH_TYPE_DIALOG_ID)) {
            displayManager.dismissSelectionDialog(AndroidDisplayManager.REFRESH_TYPE_DIALOG_ID);
            outState.putInt(REFRESH_TYPE_ALERT_PENDING, AndroidDisplayManager.REFRESH_TYPE_DIALOG_ID);
        }
    }

    public void setupAllTabs(Bundle state) {

        tabs = (TabHost)this.findViewById(R.id.tabhost);

        tabs.setup();

        // Set the tab widget background
        tabs.getTabWidget().setBackgroundResource(R.color.black);

        setupSettingsTab(new AndroidSyncSettingsTab(this, state), tabs);
        setupSettingsTab(new AndroidAdvancedSettingsTab(this, state), tabs);

        // Select the last selected tab
        int lastSelectedId = 0;
        if(state != null) {
            lastSelectedId = state.getInt(LAST_SELECTED_TAB_ID);
        }
        tabs.setCurrentTab(lastSelectedId);
    }

    private void setupSettingsTab(AndroidSettingsTab tab, TabHost tabs) {

        Log.debug(TAG, "Setting up tab: " + tab.getIndicatorLabel());

        TabHost.TabSpec syncTabSpec = tabs.newTabSpec(tab.getTag());
        syncTabSpec.setContent(tab);
        syncTabSpec.setIndicator(tab.getIndicatorLabel(), tab.getIndicatorIcon());
        tabs.addTab(syncTabSpec);

        // If the TabWidget contains the separators, it should be doubled
        int tabIndex = tabs.getTabWidget().getTabCount()-1;

        // Define custom background resource for each TabWidget child
        RelativeLayout trl = (RelativeLayout)tabs.getTabWidget().getChildAt(tabIndex);
        trl.setBackgroundResource(R.drawable.tab_indicator_bg);
        
        // Set the tab indicator drawable
        ((TextView)trl.getChildAt(1)).setTextAppearance(this, R.style.tab_widget);

        settingsTabList.add(tab);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && hasChanges()) {
            // Ask to the user if he want to save settings since he did changes
            displayManager.askYesNoQuestion(this, localization.getLanguage(
                    "settings_changed_alert"),
                    new Runnable() {
                        public void run() {
                            save(true, null);
                        }
                    },
                    new Runnable() {
                        public void run() {
                            // Shall we close the settings screen?
                        }
                    }, 0);
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Log.trace(TAG, "onCreateDialog: " + id);
        Dialog result = null;
        if(displayManager != null) {
            result = displayManager.createDialog(id);
        }
        if(result != null) {
            return result;
        } else {
            return super.onCreateDialog(id);
        }
    }

    /**
     * Save settings for all the tabs, and eventually close the screen.
     * 
     * @param close true if the settings screen must be closed after saving
     * @param callback Used to be notified when tab settings are saved
     */
    public void save(boolean close, SaveSettingsCallback callback) {

        Log.info(TAG, "Saving settings");

        AndroidController gc = AndroidController.getInstance();
        AndroidHomeScreenController ahsc = (AndroidHomeScreenController)gc
                .getHomeScreenController();
        if (ahsc.getSyncAll() || ahsc.isSynchronizing() ||
                ahsc.isFirstSyncDialogDisplayed()){
            displayManager.showMessage(this, localization.getLanguage(
                    "sync_in_progress_dialog_save"));
            Log.info(TAG, "Cannot save settings. Synchronization in progress...");
            return;
        }
        
        if(callback == null) {
            callback = new SaveCallback(close, settingsTabList.size());
        } else {
            callback.setCount(settingsTabList.size()); 
        }
        
        // Save the configuration for all the tabs
        for(AndroidSettingsTab tab : settingsTabList) {
            
            Log.info(TAG, "Saving " + tab.getIndicatorLabel() + " settings");

            boolean hasChanges = tab.hasChanges();
            if(!hasChanges) {
                callback.tabSettingsSaved(false);
                continue;
            }
            
            // Save the settings for this tab
            tab.saveSettings(callback);
        }
    }

    private class SaveCallback extends SaveSettingsCallback {

        public SaveCallback(boolean close, int count) {
            super(close, count);
        }

        @Override
        public void tabSettingsSaved(boolean changes) {

            super.tabSettingsSaved(changes); 
            
            if(count <= 0 && result == true) {
                if (changesFound) {
                    displayManager.showMessage(AndroidSettingsScreen.this,
                            localization.getLanguage("settings_saved"));
                }
                if (close) {
                    finish();
                }
            }
        }
    }

    /**
     * @return true if there are changes in settings
     */
    public boolean hasChanges() {
        boolean hasChanges = false;
        for(AndroidSettingsTab tab : settingsTabList) {
            hasChanges |= tab.hasChanges();
        }
        return hasChanges;
    }

    public Object getUiScreen() {
        return this;
    }

    public void cancel() {
        for(AndroidSettingsTab tab : settingsTabList) {
            tab.cancelSettings();
        }
        finish();
    }

    /**
     * A call-back for when the user presses the save button.
     */
    private class SaveListener implements OnClickListener {
        public void onClick(View v) {
            save(true, null);
        }
    }

    /**
     * A call-back for when the user presses the cancel button.
     */
    private class CancelListener implements OnClickListener {
        public void onClick(View v) {
            cancel();
        }
    }
}
