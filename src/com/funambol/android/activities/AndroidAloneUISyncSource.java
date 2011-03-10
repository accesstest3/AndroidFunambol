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

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.animation.AnimationUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.funambol.android.R;
import com.funambol.client.ui.UISyncSource;
import com.funambol.client.ui.UISyncSourceContainer;
import com.funambol.client.ui.Bitmap;
import com.funambol.client.ui.Font;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.controller.SyncingAnimation;
import com.funambol.client.customization.Customization;
import com.funambol.client.localization.Localization;
import com.funambol.util.Log;

/**
 * This is the UI representation of a source that occupies all the available
 * space. It is used when the source is alone, so that it fills up the space.
 */
public class AndroidAloneUISyncSource extends AndroidUISyncSource {

    private static final int HUGE_TOP_PADDING   = 10;
    private static final int HUGE_LEFT_PADDING  = 10;
    private static final int HUGE_RIGHT_PADDING = 10;

    private static final int TITLE_LEFT_MARGIN  = 40;
    private static final int TITLE_TOP_MARGIN  = 10;
    private static final int TITLE_RIGHT_MARGIN  = 40;
    private static final int TITLE_BOTTOM_MARGIN  = 30;

    private static final String TAG = "AndroidAloneUISyncSource";

    private ImageView arrowsView;
    private int spinningIconIdx;
    private LinearLayout hugeButtonLayout;

    private SetIconUIThread setIconUIThread = new SetIconUIThread();
    private SourceSyncingAnimation animation;
    private Customization customization;
    private Localization localization;
    private String currentTitle;
    private AndroidHomeScreen homeScreen;

    public AndroidAloneUISyncSource(Activity activity) {
        super(activity);

        this.activity = activity;

        // Remove any padding added to the basic components
        statusTextView.setPadding(0,adaptSizeToDensity(5),0,0);
        sourceIconView.setPadding(0,0,0,0);

        // Sets the linear layout
        LinearLayout ll1 = new LinearLayout(activity);
        // All items in ll1 are vertically centered
        ll1.setOrientation(LinearLayout.VERTICAL);
        ll1.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams ll1Params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                                                                            LayoutParams.FILL_PARENT);
        ll1.setLayoutParams(ll1Params);
        // Set the background color to white for the main layout manager
        //ll1.setBackgroundColor(0xFFFFFF);

        // The huge button is contained in its own layout which is heavier so
        // that is occupies all the available space and pushes the other
        // components down
        LinearLayout buttonLayout = new LinearLayout(activity);
        buttonLayout.setPadding(adaptSizeToDensity(40), 0,
                                adaptSizeToDensity(40), 0);

        // Create the huge button component
        hugeButtonLayout = new LinearLayout(activity);
        hugeButtonLayout.setOrientation(LinearLayout.VERTICAL);
        hugeButtonLayout.setBackgroundResource(R.drawable.alone_view_shape);

        // Add the arrows and the title
        arrowsView =  new ImageView(activity);
        arrowsView.setImageResource(R.drawable.icon_sync_154x154_frame01);
        arrowsView.setPadding(0, adaptSizeToDensity(10), 0, 0);
        hugeButtonLayout.addView(arrowsView);

        titleTextView.setGravity(Gravity.CENTER);
        titleTextView.setPadding(adaptSizeToDensity(TITLE_LEFT_MARGIN),
                                 adaptSizeToDensity(TITLE_TOP_MARGIN),
                                 adaptSizeToDensity(TITLE_RIGHT_MARGIN),
                                 adaptSizeToDensity(TITLE_BOTTOM_MARGIN));
        hugeButtonLayout.addView(titleTextView);

        LinearLayout.LayoutParams hugeParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                                                                             LayoutParams.WRAP_CONTENT);
        hugeParams.gravity = Gravity.CENTER;
        buttonLayout.addView(hugeButtonLayout, hugeParams);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                                                                               LayoutParams.WRAP_CONTENT);
        buttonParams.weight = 1.0f;
        buttonParams.gravity = Gravity.CENTER;
        ll1.addView(buttonLayout, buttonParams);

        // Add the source icon view
        ll1.addView(sourceIconView);

        // Finally the status
        statusTextView.setGravity(Gravity.CENTER);
        ll1.addView(statusTextView);

        this.addView(ll1, ll1Params);

        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);
    }

    public void setLocalization(Localization localization) {
        this.localization = localization;
    }

    public void setCustomization(Customization customization) {
        this.customization = customization;
    }

    public void setHomeScreenView(AndroidHomeScreen homeScreen) {
        this.homeScreen = homeScreen;
    }

    @Override
    public void syncStarted() {

        // We use this same button to cancel the sync
        super.setTitle(localization.getLanguage("menu_cancel_sync"));

        Log.trace(TAG, "syncStarted, starting a spinning icon");

        if (animation == null) {
            // Create an object for the animation
            animation = new SourceSyncingAnimation();
        }
        animation.startAnimation();
    }

    @Override
    public void syncEnded() {
        Log.trace(TAG, "syncEnded, stopping any spinning icon");
        animation.stopAnimation();
        super.setTitle(currentTitle);
    }

    @Override
    public void setSelection(boolean selected, boolean fromUi) {
    }

    public void setClickable(boolean clickable) {
        // This button is always clickable
        Log.trace(TAG, "ignoring setClickable");
    }

    public void setOnClickListener(View.OnClickListener listener) {
        // We associate the listener to the huge button only
        Log.trace(TAG, "setOnClickListener");
        hugeButtonLayout.setOnClickListener(listener);
    }

    public void setOnCreateContextMenuListener(View.OnCreateContextMenuListener l) {
        Log.trace(TAG, "setOnCreateContextMenuListener");
        hugeButtonLayout.setOnCreateContextMenuListener(l);
    }

    public boolean hasFocus() {
        // We consider this item as always focused (it is alone in the screen)
        return true;
    }

    @Override
    public void setTitle(String value) {
        if (localization != null) {
            value = localization.getLanguage("home_source_title_prefix") + " " + value;
        }
        currentTitle = value;
        super.setTitle(value);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu) {
        super.onCreateContextMenu(menu);
        homeScreen.createContextMenuForSource(appSyncSource, menu);
    }

    private class SourceSyncingAnimation extends SyncingAnimation {

        public SourceSyncingAnimation() {
            super(customization.getStatusHugeIconsForAnimation());
        }

        protected void showBitmap(Bitmap bitmap) {
            // Update the icon in the central button
            setIconUIThread.setIcon(bitmap);
            activity.runOnUiThread(setIconUIThread);
        }
    }

    private class SetIconUIThread implements Runnable {
        private Bitmap image;

        public SetIconUIThread() {
        }

        public void setIcon(Bitmap image) {
            this.image = image;
        }

        public void run() {
            if (image != null) {
                Integer id = (Integer)image.getOpaqueDescriptor();
                arrowsView.setImageResource(id.intValue());
            } else {
                // Clear the previous image
                arrowsView.setImageResource(0);
            }
        }
    }
}


