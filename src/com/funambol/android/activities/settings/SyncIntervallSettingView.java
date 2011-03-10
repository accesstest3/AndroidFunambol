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

package com.funambol.android.activities.settings;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.app.Activity;

import com.funambol.android.R;
import com.funambol.android.controller.AndroidController;
import com.funambol.client.ui.SettingsUIItem;
import com.funambol.client.configuration.Configuration;
import com.funambol.client.localization.Localization;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Implements a LinearLayout to display the sync mode setting (manual, push or
 * scheduled) in Android
 */
public class SyncIntervallSettingView extends LinearLayout implements SettingsUIItem {

    private static final int TOP_PADDING = 0;
    private static final int BOTTOM_PADDING = 12;
    private static final int LEFT_PADDING = 12;
    private static final int RIGHT_PADDING = 12;

    private static final int TITLE_LEFT_PADDING = 0;
    private static final int TITLE_BOTTOM_PADDING = 6;

    private Localization loc;

    private Spinner syncIntervalSpinner;

    private final Vector<String> syncIntervalStrings = new Vector<String>();

    private int[] syncIntervals;
    private int originalSyncInterval;

    private Hashtable<String, Integer> syncIntervalReference = new Hashtable<String, Integer>();

    public SyncIntervallSettingView(Activity activity, int[] syncIntervals) {

        super(activity);

        AndroidController ac = AndroidController.getInstance();
        loc = ac.getLocalization();
        
        this.syncIntervals = syncIntervals;

        // Load the sync interval choices
        loadIntervalChoices(syncIntervals);

        TextView titleTextView  = new TextView(activity, null, R.style.sync_title);
        titleTextView.setPadding(adaptSizeToDensity(TITLE_LEFT_PADDING), 0, 0,
                                 adaptSizeToDensity(TITLE_BOTTOM_PADDING));
        titleTextView.setTextAppearance(activity, android.R.attr.textAppearanceLarge);
        titleTextView.setText(loc.getLanguage("conf_polling_pim_duration"));
 
        syncIntervalSpinner = new Spinner(activity);
        syncIntervalSpinner.setPrompt(loc.getLanguage("conf_polling_pim_duration"));
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(activity,
                android.R.layout.simple_spinner_item);

        // Add the sync modes to the spinner adapter
        for(int i=0; i<syncIntervals.length; i++) {
            adapter.add(getSyncIntervalString(syncIntervals[i]));
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        syncIntervalSpinner.setAdapter(adapter);

        syncIntervalSpinner.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                                                          LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(lp);
        this.setPadding(adaptSizeToDensity(LEFT_PADDING),
                        adaptSizeToDensity(TOP_PADDING),
                        adaptSizeToDensity(RIGHT_PADDING),
                        adaptSizeToDensity(BOTTOM_PADDING));
        this.setOrientation(LinearLayout.VERTICAL);

        this.addView(titleTextView);
        this.addView(syncIntervalSpinner);
    }

    public void setSyncInterval(int interval) {
        for(int i=0; i<syncIntervals.length; i++) {
            if(syncIntervals[i] == interval) {
                originalSyncInterval = interval;
                syncIntervalSpinner.setSelection(i);
                return;
            }
        }
    }

    public int getSyncInterval() {
        String selectedMode = (String)syncIntervalSpinner.getSelectedItem();
        return syncIntervalReference.get(selectedMode);
    }

    public boolean hasChanges() {
        return originalSyncInterval != getSyncInterval();
    }

    public void loadSettings(Configuration configuration) {
        setSyncInterval(configuration.getPollingInterval());
    }
    
    public void saveSettings(Configuration conf) {
        conf.setPollingInterval(getSyncInterval());
    }

    private String getSyncIntervalString(int interval) {
        for(int i=0; i<syncIntervalStrings.size(); i++) {
            if(syncIntervalReference.get(syncIntervalStrings.get(i)) == interval) {
                return syncIntervalStrings.get(i);
            }
        }
        return null;
    }

    private void loadIntervalChoices(int[] minValues) {
        int size = minValues.length;

        String minutes = loc.getLanguage("conf_mins");
        String hour = loc.getLanguage("conf_hour");
        String hours = loc.getLanguage("conf_hours");
        String day = loc.getLanguage("conf_day");
        String days = loc.getLanguage("conf_days");

        String[] intervalChoices = new String[size];

        for (int i = 0; i < size; i++) {
            int hoursValue = minValues[i]/60;

            if (hoursValue == 0) {
                intervalChoices[i] = minValues[i] + " " + minutes;
                syncIntervalStrings.add(intervalChoices[i]);
                syncIntervalReference.put(intervalChoices[i],minValues[i]);
            } else if (hoursValue == 1) {
                intervalChoices[i] = hoursValue + " " + hour;
                syncIntervalStrings.add(intervalChoices[i]);
                syncIntervalReference.put(intervalChoices[i],minValues[i]);
            } else if (hoursValue < 24) {
                intervalChoices[i] = hoursValue + " " + hours;
                syncIntervalStrings.add(intervalChoices[i]);
                syncIntervalReference.put(intervalChoices[i],minValues[i]);
            } else if (hoursValue == 24){
                intervalChoices[i] = hoursValue/24 + " " + day;
                syncIntervalStrings.add(intervalChoices[i]);
                syncIntervalReference.put(intervalChoices[i],minValues[i]);
            } else if (hoursValue > 24){
                intervalChoices[i] = hoursValue/24 + " " + day;
                syncIntervalStrings.add(intervalChoices[i]);
                syncIntervalReference.put(intervalChoices[i],minValues[i]);
            }
        }
    }

    private int adaptSizeToDensity(int size) {
        return (int)(size*getContext().getResources().getDisplayMetrics().density);
    }
}

