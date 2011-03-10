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

import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.app.Activity;

import com.funambol.android.R;
import com.funambol.client.ui.UISyncSource;
import com.funambol.client.ui.UISyncSourceContainer;
import com.funambol.client.ui.Bitmap;
import com.funambol.client.ui.Font;
import com.funambol.client.source.AppSyncSource;
import com.funambol.util.Log;

/**
 * This class is the basic for defining UI sync source in Android. It is a
 * graphical component that can be extended to display the status of a source.
 */
public class AndroidUISyncSource extends RelativeLayout implements UISyncSource {

    private static final String TAG = "AndroidUISyncSource";

    protected static final int TOP_PADDING = 8;
    protected static final int BOTTOM_PADDING = 8;
    protected static final int TITLE_L_PADDING = 5;
    protected static final int STATUS_L_PADDING = 12;
    protected static final int STATUS_ICON_R_PADDING = 4;
    protected static final int SOURCE_ICON_LEFT_PADDING = 4;
    protected static final int SOURCE_ICON_RIGHT_PADDING = 4;

    private static final int STATUS_FONT_SIZE = 12;
    private static final int TITLE_FONT_SIZE  = 22;

    private static final int ENABLED_TEXT_COLOR  = 0xFF000000;
    private static final int DISABLED_TEXT_COLOR = 0xFF7F7F7F;

    protected String title;
    protected boolean isEnabled;
    protected AppSyncSource appSyncSource;
    protected UISyncSourceContainer container;
    protected Activity activity;

    protected ImageView sourceIconView;
    protected ImageView statusIconView;
    protected TextView  titleTextView;
    protected TextView  statusTextView;

    protected int enabledTitleColor = ENABLED_TEXT_COLOR;
    protected int disabledTitleColor = DISABLED_TEXT_COLOR;
    protected int enabledMessageColor = ENABLED_TEXT_COLOR;
    protected int disabledMessageColor = DISABLED_TEXT_COLOR;

    protected SetStatusIconUIThread setStatusIconUIThread = new SetStatusIconUIThread();
    protected SetIconUIThread setIconUIThread = new SetIconUIThread();
    protected SetStatusStringUIThread setStatusStringUIThread = new SetStatusStringUIThread();
    protected SetEnabledUIThread setEnabledUIThread = new SetEnabledUIThread();
    protected SetSelectedUIThread setSelectedUIThread = new SetSelectedUIThread();
    protected SetTitleUIThread setTitleUIThread = new SetTitleUIThread();
    protected RedrawUIThread redrawUIThread = new RedrawUIThread();

    private android.graphics.Bitmap lastResizedBitmap = null;
    private int lastResId = -1;

    public AndroidUISyncSource(Activity activity) {
        super(activity);

        this.activity = activity;

        // Create the source icon
        sourceIconView = createSourceIcon(activity);

        // Create the title text
        titleTextView = createTitleText(activity);

        // Create the status icon
        statusIconView = createStatusIcon(activity);

        // Create the status text
        statusTextView = createStatusText(activity);
    }

    protected ImageView createSourceIcon(Activity activity) {
        ImageView sourceIconView;

        sourceIconView = new ImageView(activity, null, R.style.sync_icon);
        sourceIconView.setPadding(adaptSizeToDensity(SOURCE_ICON_LEFT_PADDING),
                                  adaptSizeToDensity(TOP_PADDING),
                                  adaptSizeToDensity(SOURCE_ICON_RIGHT_PADDING),
                                  adaptSizeToDensity(BOTTOM_PADDING));
        return sourceIconView;
    }

    protected TextView createTitleText(Activity activity) {
        TextView titleTextView;

        titleTextView  = new TextView(activity, null, R.style.sync_title);
        titleTextView.setTextSize(TITLE_FONT_SIZE);
        titleTextView.setPadding(adaptSizeToDensity(TITLE_L_PADDING), 0, 0, 0);
        titleTextView.setTextColor(ENABLED_TEXT_COLOR);

        return titleTextView;
    }

    protected ImageView createStatusIcon(Activity activity) {
        ImageView statusIconView;

        statusIconView = new ImageView(activity);
        statusIconView.setAdjustViewBounds(true);
        statusIconView.setMaxHeight(adaptSizeToDensity(32));
        statusIconView.setMaxWidth(adaptSizeToDensity(32));
        statusIconView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                                                                  LayoutParams.WRAP_CONTENT));
        return statusIconView;
    }

    protected TextView createStatusText(Activity activity) {
        TextView statusTextView;

        statusTextView = new TextView(activity, null, R.style.sync_message);
        statusTextView.setPadding(adaptSizeToDensity(STATUS_L_PADDING), 0, 0, 0);
        statusTextView.setTextSize(STATUS_FONT_SIZE);
        statusTextView.setTextColor(ENABLED_TEXT_COLOR);

        return statusTextView;
    }


    /**
     * Set the title
     */
    public void setTitle(String value) {
        setTitleUIThread.setTitle(value);
        activity.runOnUiThread(setTitleUIThread);
    }

    /**
     * Set the title color when the item is enabled
     */
    public void setTitleEnabledColor(int color) {
        enabledTitleColor = color;
    }

    /**
     * Set the title color when the item is disabled
     */
    public void setTitleDisabledColor(int color) {
        disabledTitleColor = color;
    }

    /**
     * Check if the item is enabled/disabled
     */
    public boolean isDisabled() {
        return !isEnabled;
    }

    public void setIcon(Bitmap image) {
        setIconUIThread.setIcon(image);
        activity.runOnUiThread(setIconUIThread);
    }

    /**
     * Set the status icon to be used when the item is not selected
     */
    public void setStatusIcon(Bitmap image) {
        // Now set the new icon
        setStatusIconUIThread.setIcon(image);
        activity.runOnUiThread(setStatusIconUIThread);
    }

    /**
     * Set the status string
     */
    public void setStatusString(String value) {
        setStatusStringUIThread.setText(value);
        activity.runOnUiThread(setStatusStringUIThread);
    }

    @Override
    public void setEnabled(boolean flag) {
        setEnabledUIThread.setEnabled(flag);
        activity.runOnUiThread(setEnabledUIThread);
    }

    public void setSelection(boolean selected, boolean fromUi) {
        setSelectedUIThread.setSelection(selected);
        activity.runOnUiThread(setSelectedUIThread);
    }

    /**
     * Forces the current item to be re-drawn
     */
    public void redraw() {
        activity.runOnUiThread(redrawUIThread);
    }

    /**
     * Returns the AppSyncSource this item represents
     */
    public AppSyncSource getSource() {
        return appSyncSource;
    }

    /**
     * Set the AppSyncSource this item represents
     */
    public void setSource(AppSyncSource source) {
        appSyncSource = source;
    }

    /**
     * Set the container this item belongs to
     */
    public void setContainer(UISyncSourceContainer container) {
        this.container = container;
    }

    /**
     * Set the item font used for the title and the status
     */
    public void setFont(Font font) {
    }

    public void syncStarted() {
        Log.trace(TAG, "syncStarted");
    }

    public void syncEnded() {
        Log.trace(TAG, "syncEnded");
    }

    protected int adaptSizeToDensity(int size) {
        return (int)(size*getContext().getResources().getDisplayMetrics().density);
    }

    private class RedrawUIThread implements Runnable {
        public RedrawUIThread() {
        }

        public void run() {
            invalidate();
        }
    }

    private class SetStatusStringUIThread implements Runnable {
        private String text;

        public SetStatusStringUIThread() {
        }

        public void setText(String value) {
            this.text = value;
        }

        public void run() {
            statusTextView.setText(text);
        }
    }

    protected class SetIconUIThread implements Runnable {
        private Bitmap image;

        public SetIconUIThread() {
        }

        public void setIcon(Bitmap image) {
            this.image = image;
        }

        public void run() {
            if (image != null) {
                int resId = ((Integer)image.getOpaqueDescriptor()).intValue();
                sourceIconView.setImageResource(resId);
            } else {
                // Clear the previous image
                sourceIconView.setImageResource(0);
            }
        }
    }

    private class SetStatusIconUIThread implements Runnable {
        private Bitmap image;

        public SetStatusIconUIThread() {
        }

        public void setIcon(Bitmap image) {
            this.image = image;
        }

        public void run() {
            if (image != null) {
                Integer id = (Integer)image.getOpaqueDescriptor();
                statusIconView.setImageResource(id.intValue());
            } else {
                // Clear the previous image
                statusIconView.setImageResource(0);
            }
        }
    }

    private class SetSelectedUIThread implements Runnable {

        private boolean selected;

        public SetSelectedUIThread() {
        }

        public void setSelection(boolean selected) {
            this.selected = selected;
        }

        public void run() {
            setSelected(selected);
            if (selected) {
                setBackgroundResource(R.drawable.sync_shape_over);
                requestFocus();
            } else {
                setBackgroundResource(R.drawable.sync_shape);
            }
        }
    }


    private class SetEnabledUIThread implements Runnable {
        private boolean enabled;

        public SetEnabledUIThread() {
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void run() {

            if (enabled) {
                titleTextView.setTextColor(enabledTitleColor);
                statusTextView.setTextColor(enabledMessageColor);
            } else {
                titleTextView.setTextColor(disabledTitleColor);
                statusTextView.setTextColor(disabledMessageColor);
            }

            setFocusable(enabled);
            setFocusableInTouchMode(enabled);
            setClickable(enabled);

            isEnabled = enabled;
        }
    }

    private class SetTitleUIThread implements Runnable {
        private String title;

        public SetTitleUIThread() {
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void run() {
            titleTextView.setText(title);
        }
    }

}


