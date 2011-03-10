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

package com.funambol.android.activities.settings;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.os.Bundle;

import com.funambol.android.AndroidAccountManager;
import com.funambol.android.AndroidAppSyncSource;
import com.funambol.android.R;
import com.funambol.client.configuration.Configuration;
import com.funambol.client.ui.SettingsUISyncSource;
import com.funambol.client.ui.SyncSettingsScreen;
import com.funambol.client.ui.SettingsUIItem;
import com.funambol.client.controller.SyncSettingsScreenController;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Represents the Android Sync Settings tab
 */
public class AndroidSyncSettingsTab extends AndroidSettingsTab
        implements SyncSettingsScreen, OnItemSelectedListener {

    private static final String TAB_TAG = "sync_settings";
    
    private static final int LAYOUT_ID = R.id.sync_settings_tab;

    private SyncModeSettingView syncModeView;
    private SyncIntervallSettingView syncIntervalView;
    private C2SPushSettingView c2sPushView;

    private boolean syncIntervalViewShown = false;
    
    private Vector<AndroidSettingsUISyncSource> sourceItems =
            new Vector<AndroidSettingsUISyncSource>();

    private SyncSettingsScreenController screenController;

    private LinearLayout ll;
    
    public AndroidSyncSettingsTab(Activity a, Bundle state) {
        super(a, state);
        screenController = new SyncSettingsScreenController(controller, this);
        initialize();
    }

    private void initialize() {
        // Prepare container layout
        ll = new LinearLayout(activity);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                                                         LayoutParams.WRAP_CONTENT));

        // Add the global sync mode setting
        if(syncModeView != null) {
            ll.addView(syncModeView);
        }

        if(syncIntervalView != null && !syncIntervalViewShown){
            ll.addView(syncIntervalView);
            syncIntervalViewShown = true;
        }

        if(c2sPushView != null) {
            if(syncModeView != null) {
                addDivider(ll);
            }
            ll.addView(c2sPushView);
        }

        // Add all the source settings
        boolean first = true;
        for(AndroidSettingsUISyncSource item : sourceItems) {
            if(item == null) {
                continue;
            }
            if(!first) {
                addDivider(ll);
            } else {
                first = false;
                if(syncModeView != null) {
                    addBigDivider(ll);
                }
            }
            ll.addView(item);
        }

        this.addView(ll);
    }

    /**
     * Add a divider View to a ViewGroup object
     * @param vg
     */
    private void addBigDivider(ViewGroup vg) {
        ImageView divider = new ImageView(activity);
        divider.setBackgroundResource(R.drawable.divider_big_shape);
        LinearLayout.LayoutParams dl = new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        vg.addView(divider, dl);
    }

    //---------------------------------------- AndroidSettingsTab implementation
    
    public String getTag() {
        return TAB_TAG;
    }

    public int getLayoutId() {
        return LAYOUT_ID;
    }

    public void saveSettings(SaveSettingsCallback callback) {
        screenController.saveSettings();
        callback.saveSettingsResult(true);
    }

    public boolean hasChanges() {
        return screenController.hasChanges();
    }

    public Drawable getIndicatorIcon() {
        return getResources().getDrawable(R.drawable.ic_sync_tab);
    }

    public String getIndicatorLabel() {
        return localization.getLanguage("settings_sync_tab");
    }

    //---------------------------------------- SyncSettingsScreen implementation

    public SettingsUISyncSource createSettingsUISyncSource() {
        return new AndroidSettingsUISyncSource(activity);
    }

    public void setSettingsUISyncSource(SettingsUISyncSource item, int index) {
        sourceItems.setElementAt((AndroidSettingsUISyncSource)item, index);
    }

    public void setSettingsUISyncSourceCount(int count) {
        sourceItems.setSize(count);
    }
    
    public SettingsUIItem addSyncModeSetting() {
        // Setup SyncMode setting View
        int[] modes = customization.getAvailableSyncModes();
        if(modes.length > 1) {
            syncModeView = new SyncModeSettingView(activity, modes);
            syncModeView.setSelectedItemListener(this);
            syncModeView.loadSettings(configuration);
        }
        return syncModeView;
    }

    public SettingsUIItem addSyncIntervalSetting() {
        // Setup SyncInterval setting View
        int[] intervals = customization.getPollingPimIntervalChoices();
        syncIntervalView = new SyncIntervallSettingView(activity, intervals);
        syncIntervalView.loadSettings(configuration);
        return syncIntervalView;
    }

    public SettingsUIItem addC2SPushSetting() {
        c2sPushView = new C2SPushSettingView(activity);

        Account account = AndroidAccountManager.getNativeAccount(activity);
        boolean checked = false;
        
        // Get the auto-sync setting for each source
        Enumeration sources = controller.getAppSyncSourceManager().getWorkingSources();
        while(sources.hasMoreElements()) {
            AndroidAppSyncSource source = (AndroidAppSyncSource)sources.nextElement();
            String authority = source.getAuthority();
            if(authority != null) {
                checked |= ContentResolver.getSyncAutomatically(account, authority);
            }
        }
        c2sPushView.setChecked(checked);

        // Get global auto-sync setting
        boolean autoSyncEnabled = ContentResolver.getMasterSyncAutomatically();

        // Get the background data setting
        ConnectivityManager cm = (ConnectivityManager)getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean backgroundDataEnabled = cm.getBackgroundDataSetting();
        
        c2sPushView.setEnabled(autoSyncEnabled && backgroundDataEnabled);

        return c2sPushView;
    }

    public void removeAllItems() {
        syncModeView = null;
        syncIntervalView = null;
        c2sPushView = null;
        sourceItems.clear();
    }

    public Object getUiScreen() {
        return activity;
    }

    public void cancelSettings() {
    }

    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if(syncModeView.getSyncMode() == Configuration.SYNC_MODE_SCHEDULED) {
            if(syncIntervalView != null && !syncIntervalView.isShown() &&
                    !syncIntervalViewShown) {
                // This is the position of the interval
                ll.addView(syncIntervalView, 1);
                syncIntervalViewShown = true;
            }
        } else {
            if(syncIntervalView!= null && syncIntervalView.isShown() &&
                    syncIntervalViewShown) {
                ll.removeView(syncIntervalView);
                syncIntervalViewShown = false;
            }
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) {
    }
}
