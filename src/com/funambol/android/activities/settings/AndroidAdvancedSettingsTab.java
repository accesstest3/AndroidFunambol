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

import android.view.View;
import android.app.Activity;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.drawable.Drawable;

import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.os.Bundle;

import com.funambol.android.R;
import com.funambol.android.activities.AndroidSettingsScreen;
import com.funambol.android.activities.AndroidDisplayManager;
import com.funambol.client.ui.AdvancedSettingsScreen;

import java.util.Hashtable;
import java.util.Vector;

import com.funambol.util.Log;

import com.funambol.android.controller.AndroidAdvancedSettingsScreenController;
import com.funambol.android.controller.AndroidController;
import com.funambol.client.ui.Screen;

/**
 * Realize the Advanced Settings screen tab into the android sync client. The
 * command items to send and view log and perform reset actions are represented
 * by buttons in this realization. Refer to the related controllers and screen
 * interfaces for further informations.
 */
public class AndroidAdvancedSettingsTab extends AndroidSettingsTab
        implements AdvancedSettingsScreen {

    /** The string that references this class into the log content*/
    private static final String TAB_TAG = "advanced_settings";

    private static final int LAYOUT_ID = R.id.advanced_settings_tab;

    // Log section elements
    private Spinner spin;
    private Button  viewLogBtn;
    private Button  sendLogBtn;

    // Reset section elements
    private Button resetBtn;
    private LinearLayout resetSection;

    // Import section elements
    private Button importBtn;
    private LinearLayout importSection;

    private ArrayAdapter aa;

    private Hashtable<String, Integer> logLevelReference = new Hashtable<String, Integer>();

    private TwoLinesCheckBox saveBandwidthCheckBox;

    private LinearLayout settingsContainer;

    private LinearLayout bandwidthSaverSection;
    private LinearLayout logSection;
    private LinearLayout logButtonsRaw;

    

    private int originalLogLevel;

    private boolean originalBandwidthStatus;

    private AndroidDisplayManager dm;

    private AndroidAdvancedSettingsScreenController screenController;

    /**
     * Default constructor.
     * @param a the Activity which contains this View
     * @param state
     */
    public AndroidAdvancedSettingsTab(Activity a, Bundle state) {
        super(a, state);

        AndroidController cont = AndroidController.getInstance();

        this.dm = (AndroidDisplayManager)cont.getDisplayManager();

        screenController = new AndroidAdvancedSettingsScreenController(cont, this);

        initScreenElements();
        screenController.initialize();
    }

    /**
     * get the tag of this class
     * @return String the TAG that represents this class' name
     */
    public String getTag() {
        return TAB_TAG;
    }

    /**
     * Accessor method to retrieve the layout id for this class in order it to
     * be referenced from external classes
     * @return int the layout id as an int value
     */
    public int getLayoutId() {
        return LAYOUT_ID;
    }

    /**
     * Get the icon related to the AdvancedSettingsScreenTab that is visible
     * on the edge of the tab over the tab title
     * @return Drawable the indicator icon related to this tab
     */
    public Drawable getIndicatorIcon() {
        return getResources().getDrawable(R.drawable.ic_advanced_tab);
    }

    /**
     * Get the title related to the AdvancedSettingsScreenTab that is visible
     * under the tab icon.
     * @return Stirng the title related to this tab
     */
    public String getIndicatorLabel() {
        return localization.getLanguage("settings_advanced_tab");
    }

    /**
     * Save the values contained into this view using the dedicated controller
     */
    public void saveSettings(SaveSettingsCallback callback) {
        //FIX-ME - return true if and only if the save action is successful
        screenController.checkAndSave();
        originalLogLevel = configuration.getLogLevel();
        originalBandwidthStatus = configuration.getBandwidthSaverActivated();
        callback.saveSettingsResult(true);
    }

    public boolean hasChanges() {
        boolean hasChanges = false;

        if((logSection != null) && (originalLogLevel != getViewLogLevel())){
            hasChanges = true;
        }
        
        if((bandwidthSaverSection!= null) && (originalBandwidthStatus != getBandwidthSaver())){
            hasChanges = true;
        }
        
        return hasChanges;
    }

    public void enableResetCommand(boolean enable) {
        resetBtn.setEnabled(enable);
    }

    public void enableSendLogCommand(boolean enable) {
        sendLogBtn.setEnabled(enable);
    }

    public void hideLogsSection() {
        settingsContainer.removeView(logSection);
        logSection = null;
    }

    public void hideImportContactsSection() {
        settingsContainer.removeView(importSection);
        importSection = null;
    }

    public void hideSendLogCommand() {
        logButtonsRaw.removeView(sendLogBtn);
    }

    public void hideViewLogCommand() {
        //removeView(viewLogBtn);
    }

    /**
     * The implementation returns the activity related to this view.
     * @return Object the Activity passed to this view as a constructor
     * parameter
     */
    public Object getUiScreen() {
        return activity;
    }

    public void setViewLogLevel(int logLevel) {
        originalLogLevel = logLevel;
        spin.setSelection(logLevel + 1);
    }

    public int getViewLogLevel() {
        String item = (String) spin.getSelectedItem();
        return logLevelReference.get(item).intValue();
    }

    public void hideBandwidthSaverSection() {
        settingsContainer.removeView(bandwidthSaverSection);
        bandwidthSaverSection = null;
    }

    public void hideResetSection(){
        settingsContainer.removeView(resetSection);
        resetSection = null;
    }

    public void setBandwidthSaver(boolean enable){
        originalBandwidthStatus = enable;
        saveBandwidthCheckBox.setChecked(enable);
    }


    public boolean getBandwidthSaver(){
        return saveBandwidthCheckBox.isChecked();
    }



    private void initScreenElements() {

        logLevelReference.put(localization.getLanguage("advanced_settings_log_level_none"), Log.DISABLED);
        logLevelReference.put(localization.getLanguage("advanced_settings_log_level_error"), Log.ERROR);
        logLevelReference.put(localization.getLanguage("advanced_settings_log_level_info"), Log.INFO);
        logLevelReference.put(localization.getLanguage("advanced_settings_log_level_debug"), Log.DEBUG);
        logLevelReference.put(localization.getLanguage("advanced_settings_log_level_trace"), Log.TRACE);

        saveBandwidthCheckBox = new TwoLinesCheckBox(activity);
        saveBandwidthCheckBox.setText1(localization.getLanguage("conf_save_bandwidth"));
        saveBandwidthCheckBox.setText2(localization.getLanguage("conf_save_bandwidth_description"));
        saveBandwidthCheckBox.setPadding(0, saveBandwidthCheckBox.getPaddingBottom(), saveBandwidthCheckBox.getPaddingRight(),
                saveBandwidthCheckBox.getPaddingBottom());

        View.inflate(activity, R.layout.advanced_settings_view, this);

        importSection = (LinearLayout) findViewById(R.id.advanced_settings_import_section);
        importBtn = (Button) findViewById(R.id.advanced_settings_import_button);
        importBtn.setOnClickListener(new ImportListener());
        addDivider(importSection);
        
        settingsContainer = (LinearLayout) findViewById(R.id.advanced_settings_view);

        bandwidthSaverSection = (LinearLayout) findViewById(R.id.advanced_settings_band_saver_section);
        bandwidthSaverSection.addView(saveBandwidthCheckBox);
        addDivider(bandwidthSaverSection);

        logSection = (LinearLayout) findViewById(R.id.advanced_settings_log_section);
        addDivider(logSection);

        spin = (Spinner) findViewById(R.id.advanced_settings_log_level_spinner);

        aa = new ArrayAdapter<CharSequence>(activity, android.R.layout.simple_spinner_item);

        aa.add(localization.getLanguage("advanced_settings_log_level_none"));
        aa.add(localization.getLanguage("advanced_settings_log_level_error"));
        aa.add(localization.getLanguage("advanced_settings_log_level_info"));
        aa.add(localization.getLanguage("advanced_settings_log_level_debug"));
        aa.add(localization.getLanguage("advanced_settings_log_level_trace"));

        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(aa);

        logButtonsRaw = (LinearLayout) findViewById(R.id.advanced_settings_send_log_button_raw);
        viewLogBtn = (Button) findViewById(R.id.advanced_settings_view_log_button);
        viewLogBtn.setOnClickListener(new ViewLogListener());
        sendLogBtn = (Button) findViewById(R.id.advanced_settings_send_log_button);
        sendLogBtn.setOnClickListener(new SendLogListener());

        resetSection = (LinearLayout) findViewById(R.id.advanced_settings_reset_section);
        resetBtn = (Button) findViewById(R.id.advanced_settings_reset_button);
        resetBtn.setOnClickListener(new ResetListener());
    }

    public void cancelSettings() {
    }

    /**
     * A call-back for when the user presses the reset button.
     */
    private class ResetListener implements OnClickListener {
        public void onClick(View v) {
            AndroidSettingsScreen ass = (AndroidSettingsScreen) getUiScreen();
            //check the changes on other settings tabs before refresh
            if (ass.hasChanges()) {
                    dm.askYesNoQuestion(ass, localization.getLanguage(
                    "settings_changed_alert"),
                    new Runnable() {
                        AndroidSettingsScreen ass = (AndroidSettingsScreen) getUiScreen();
                        public void run() {
                            // Start reset through the SaveCallback
                            ass.save(false, new ResetSaveCallback());
                        }
                    },
                    new Runnable() {
                        public void run() {
                        }
                }, 0);
            } else {
                screenController.reset();
            }
        }
    }

    private class ResetSaveCallback extends SaveSettingsCallback {

        public ResetSaveCallback() {
            super(false, 0);
        }
        
        @Override
        public void tabSettingsSaved(boolean changes) {
            super.tabSettingsSaved(changes);

            if(count == 0 && result == true) {

                controller.getDisplayManager().showMessage((Screen)getUiScreen(),
                            localization.getLanguage("settings_saved"));
                screenController.reset();
            }
        }
    }

    /**
     * A call-back for when the user presses the view log button.
     */
    private class ViewLogListener implements OnClickListener {
        public void onClick(View v) {
            screenController.viewLog();
        }
    }

    /**
     * A call-back for when the user presses the send log button.
     */
    private class SendLogListener implements OnClickListener {
        public void onClick(View v) {
            screenController.sendLog();
        }
    }

    /**
     * A call-back for when the user presses the Import button.
     */
    private class ImportListener implements OnClickListener {
        public void onClick(View v) {
            screenController.importContacts();
        }
    }
}
