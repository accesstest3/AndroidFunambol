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

import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.LinearLayout;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.funambol.android.R;
import com.funambol.android.controller.AndroidController;
import com.funambol.client.controller.Controller;
import com.funambol.client.localization.Localization;
import com.funambol.client.configuration.Configuration;
import com.funambol.client.customization.Customization;

/**
 * Represents a generic settings tab
 */
public abstract class AndroidSettingsTab extends ScrollView implements TabHost.TabContentFactory {

    protected Configuration configuration;
    protected Controller    controller;
    protected Localization  localization;
    protected Customization customization;

    protected Activity activity;
    
    public AndroidSettingsTab(Activity a, Bundle savedInstanceState) {

        super(a);

        this.activity = a;

        // Init the configuration and localization
        controller = AndroidController.getInstance();
        configuration = controller.getConfiguration();
        localization  = controller.getLocalization();
        customization = controller.getCustomization();

        // Set the correct View id
        setId(getLayoutId());
    }

    /**
     * Add a divider View to a ViewGroup object
     * @param vg
     */
    protected void addDivider(ViewGroup vg) {
        ImageView divider = new ImageView(activity);
        divider.setBackgroundResource(R.drawable.divider_shape);
        LinearLayout.LayoutParams dl = new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        vg.addView(divider, dl);
    }

    public View createTabContent(String tag) {
        if(getTag().equals(tag)) {
            return this;
        }
        return null;
    }

    /**
     * @return the Tab specific tag
     */
    @Override
    public abstract String getTag();

    /**
     * @return the layout id for this tab
     */
    public abstract int getLayoutId();

    /**
     * @return true if there is changes on these tab settings
     */
    public abstract boolean hasChanges();
    
    /**
     * Save the tab settings
     * @param callback used to send back the result
     */
    public abstract void saveSettings(SaveSettingsCallback callback);

     /**
     * Cancel the tab settings
     */
    public abstract void cancelSettings();

    /** 
     * @return the tab indicator icon
     */
    public abstract Drawable getIndicatorIcon();

    /**
     * @return the tab indicator string
     */
    public abstract String getIndicatorLabel();

}
