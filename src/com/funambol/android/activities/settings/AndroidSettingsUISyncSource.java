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
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.view.Gravity;
import android.app.Activity;
import android.content.Context;

import com.funambol.android.R;
import com.funambol.android.controller.AndroidController;
import com.funambol.client.ui.Bitmap;
import com.funambol.client.ui.SettingsUISyncSource;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.configuration.Configuration;
import com.funambol.client.localization.Localization;
import com.funambol.client.source.AppSyncSourceConfig;
import com.funambol.util.Log;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncSource;
import java.util.Enumeration;

import java.util.Hashtable;

public class AndroidSettingsUISyncSource extends RelativeLayout 
        implements SettingsUISyncSource {

    private static final String TAG = "AndroidSettingsUISyncSource";

    private static final int ENABLED_TEXT_COLOR  = 0xFF000000;
    private static final int DISABLED_TEXT_COLOR = 0xFFCCCCCC;

    protected AppSyncSource appSyncSource;

    private ImageView sourceIconView;
    private TextView  titleTextView;
    private Spinner   syncModeSpinner;

    private boolean syncModeSet   = false;

    private int originalSyncMode;
    private String originalRemoteUri;

    private static final int TOP_PADDING = 12;
    private static final int BOTTOM_PADDING = 12;
    private static final int LEFT_PADDING = 12;
    private static final int RIGHT_PADDING = 12;

    private static final int ICON_TOP_PADDING = 0;
    private static final int ICON_BOTTOM_PADDING = 6;
    private static final int ICON_LEFT_PADDING = 0;
    private static final int ICON_RIGHT_PADDING = 0;

    private static final int TITLE_LEFT_PADDING = 12;
    private static final int TITLE_BOTTOM_PADDING = 6;

    private int enabledTextColor  = ENABLED_TEXT_COLOR;
    private int disabledTextColor = DISABLED_TEXT_COLOR;

    private Bitmap enabledIcon;
    private Bitmap disabledIcon;

    private int[] availableSyncModes = new int[0];
    
    private Hashtable<Integer, String> sourceSyncModesReference = new Hashtable<Integer, String>();

    protected LinearLayout mainLayout;

    protected Localization loc;

    public AndroidSettingsUISyncSource(Activity activity) {

        super(activity);

        AndroidController ac = AndroidController.getInstance();
        loc = ac.getLocalization();

        // Init the sourceSyncModes reference
        sourceSyncModesReference.put(SyncML.ALERT_CODE_NONE,
                loc.getLanguage("sync_direction_do_nothing"));
        sourceSyncModesReference.put(SyncML.ALERT_CODE_FAST,
                loc.getLanguage("sync_direction_two_way"));
        sourceSyncModesReference.put(SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT,
                loc.getLanguage("sync_direction_upload_only"));
        sourceSyncModesReference.put(SyncML.ALERT_CODE_ONE_WAY_FROM_SERVER,
                loc.getLanguage("sync_direction_download_only"));
        sourceSyncModesReference.put(SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW,
                loc.getLanguage("sync_direction_upload_only"));

        sourceIconView = new ImageView(activity, null, R.style.sync_icon);
        sourceIconView.setPadding(adaptSizeToDensity(ICON_LEFT_PADDING),
                                  adaptSizeToDensity(ICON_TOP_PADDING),
                                  adaptSizeToDensity(ICON_RIGHT_PADDING),
                                  adaptSizeToDensity(ICON_BOTTOM_PADDING));
        sourceIconView.setAdjustViewBounds(true);
        sourceIconView.setScaleType(ImageView.ScaleType.FIT_XY);

        titleTextView  = new TextView(activity, null, R.style.sync_title);
        titleTextView.setPadding(adaptSizeToDensity(TITLE_LEFT_PADDING), 0, 0,
                adaptSizeToDensity(TITLE_BOTTOM_PADDING));
        titleTextView.setTextAppearance(activity, R.style.funambol_title);

        syncModeSpinner = new Spinner(activity);
        syncModeSpinner.setPrompt(loc.getLanguage("sync_direction_prompt"));
        syncModeSpinner.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public void setEnabledIcon(Bitmap image) {
        enabledIcon = image;
    }

    public void setDisabledIcon(Bitmap image) {
        disabledIcon = image;
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setAvailableSyncModes(int[] modes) {

        availableSyncModes = modes;
        
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                getContext(), android.R.layout.simple_spinner_item);

        // Add the sync modes to the spinner sadapter
        for(int i=0; i<modes.length; i++) {
            String mode = sourceSyncModesReference.get(modes[i]);
            if(mode != null) {
                adapter.add(mode);
            } else {
                Log.error(TAG, "Unknown sync mode: " + modes[i]);
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        syncModeSpinner.setAdapter(adapter);
    }

    public void setSyncMode(int mode) {
        for(int i=0; i<availableSyncModes.length; i++) {
            if(availableSyncModes[i] == mode) {
                syncModeSpinner.setSelection(i);
                originalSyncMode = mode;
                syncModeSet = true;
                return;
            } 
        }
    }

    public int getSyncMode() {
        int position = syncModeSpinner.getSelectedItemPosition();
        return availableSyncModes[position];
    }

    public void setRemoteUri(String remoteUri) {
        originalRemoteUri = remoteUri;
    }

    public String getRemoteUri() {
        return originalRemoteUri;
    }

    public boolean hasChanges() {
        return (syncModeSet && (getSyncMode() != originalSyncMode));
    }

    public void loadSettings(Configuration configuration) {

        boolean isEnabled = appSyncSource.isWorking() &&
                appSyncSource.getConfig().getEnabled();

        Hashtable settings = appSyncSource.getSettings();
        if (settings != null) {
            Enumeration settingKeys = settings.keys();

            while(settingKeys.hasMoreElements()) {

                Integer setting = (Integer)settingKeys.nextElement();
                switch(setting.intValue()) {

                    // Sync mode setting
                    case AppSyncSource.SYNC_MODE_SETTING:

                        int syncMode = isEnabled ?
                            appSyncSource.getConfig().getSyncType():
                            SyncML.ALERT_CODE_NONE;

                        setAvailableSyncModes((int[])settings
                                .get(setting));
                        setSyncMode(syncMode);

                        break;
                }
            }
        }
    }

    public void saveSettings(Configuration conf) {
        if(syncModeSet) {
            int syncMode = getSyncMode();

            AppSyncSourceConfig config = appSyncSource.getConfig();
            SyncSource source = appSyncSource.getSyncSource();

            SourceConfig sc = source.getConfig();
            sc.setSyncMode(syncMode);

            boolean enabled = syncMode != SyncML.ALERT_CODE_NONE;
            config.setEnabled(enabled);
            config.setSyncType(syncMode);
        }
    }

    /**
     * @return the AppSyncSource this item represents
     */
    public AppSyncSource getSource() {
        return appSyncSource;
    }

    /**
     * Set the AppSyncSource this item represents
     *
     * @param source
     */
    public void setSource(AppSyncSource source) {
        appSyncSource = source;
    }

    @Override
    public void setEnabled(boolean enabled) {
        int id;
        int color;
        if(enabled) {
            id = (Integer)enabledIcon.getOpaqueDescriptor();
            color = enabledTextColor;
        } else {
            id = (Integer)disabledIcon.getOpaqueDescriptor();
            color = disabledTextColor;
        }
        sourceIconView.setImageResource(id);
        titleTextView.setTextColor(color);
        syncModeSpinner.setEnabled(enabled);
        syncModeSpinner.setFocusable(enabled);
    }

    public void layout() {

        // Sets the linear layout for the icon and the title
        TitleLayout ll1 = new TitleLayout(getContext(), sourceIconView, titleTextView);

        // All items in ll1 are vertically centered
        ll1.setGravity(Gravity.CENTER_VERTICAL);
        ll1.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                    LayoutParams.WRAP_CONTENT));
        ll1.addView(sourceIconView);
        ll1.addView(titleTextView);

        // Container layout for all the items
        if (mainLayout == null) {
            mainLayout = new LinearLayout(getContext());
            mainLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                        LayoutParams.WRAP_CONTENT));
            mainLayout.setPadding(adaptSizeToDensity(LEFT_PADDING),
                                  adaptSizeToDensity(TOP_PADDING),
                                  adaptSizeToDensity(RIGHT_PADDING),
                                  adaptSizeToDensity(BOTTOM_PADDING));
            mainLayout.setOrientation(LinearLayout.VERTICAL);
        }

        mainLayout.addView(ll1);

        if(syncModeSet) {
            mainLayout.addView(syncModeSpinner);
        }

        this.addView(mainLayout);
    }

    private int adaptSizeToDensity(int size) {
        return (int)(size*getContext().getResources().getDisplayMetrics().density);
    }

    /**
     * This is the layout manager for the button. It works under the assumtpion
     * that 2 views are added. The first one being an ImageView (source icon),
     * the second one being a TextView (the title).
     * The main purpose of this component, is to limit the size of
     * the source icon, according to the size of the text field. In
     * particular the height of this field is used to bound the maxSize for the
     * icon.
     */
    private class TitleLayout extends LinearLayout {

        private boolean first = true;

        private ImageView imageView;
        private TextView  textView;

        public TitleLayout(Context context, ImageView imageView, TextView textView) {
            super(context);
            this.imageView = imageView;
            this.textView = textView;
        }


        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            // First of all measure the text part
            textView.measure(widthMeasureSpec, heightMeasureSpec);

            // Now limit the max size for the icon and then compute its measure
            int height = textView.getMeasuredHeight();
            // We allow 120% of the text size
            height = (12 * height) / 10;
            imageView.setMaxWidth(height);
            imageView.setMaxHeight(height);
            imageView.measure(widthMeasureSpec, heightMeasureSpec);

            // Now compute this item measure
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}


