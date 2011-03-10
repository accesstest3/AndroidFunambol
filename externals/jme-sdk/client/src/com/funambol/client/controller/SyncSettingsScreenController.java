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

package com.funambol.client.controller;

import java.util.Vector;
import java.util.Enumeration;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.customization.Customization;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.AppSyncSourceConfig;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.ui.SettingsUIItem;
import com.funambol.client.ui.SyncSettingsScreen;
import com.funambol.client.ui.SettingsUISyncSource;

import com.funambol.util.Log;

public class SyncSettingsScreenController {

    private static final String TAG_LOG = "SyncSettingsScreenController";

    private Vector miscSettingsUIItems = null;
    private Vector sourceSettingsUIItems = null;

    private AppSyncSourceManager appSyncSourceManager = null;

    private Customization customization;
    private Configuration configuration;

    private SyncSettingsScreen ssScreen;
    private Controller controller;

    public SyncSettingsScreenController(Controller controller, SyncSettingsScreen ssScreen) {

        this.controller = controller;

        appSyncSourceManager = controller.getAppSyncSourceManager();
        
        this.customization = controller.getCustomization();
        this.configuration = controller.getConfiguration();
        this.ssScreen = ssScreen;
        controller.setSyncSettingsScreenController(this);

        computeVisibleItems();
    }

    public SyncSettingsScreenController(Controller controller, Customization customization, Configuration configuration, 
                                        AppSyncSourceManager appSyncSourceManager, SyncSettingsScreen ssScreen) {

        this.controller = controller;
        this.appSyncSourceManager = appSyncSourceManager;
        this.customization = customization;
        this.configuration = configuration;
        this.ssScreen = ssScreen;

        computeVisibleItems();
    }


    public Vector getVisibleSourceItems() {
        return sourceSettingsUIItems;
    }

    public void updateListOfSources() {
        sourceSettingsUIItems = null;
        ssScreen.removeAllItems();
        computeVisibleItems();
    }

    public SyncSettingsScreen getSyncSettingsScreen() {
        return ssScreen;
    }

    private void computeVisibleItems() {

        Log.trace(TAG_LOG, "Computing list of visible items");

        // Check if visible items have already been computed
        if(sourceSettingsUIItems == null) {

            miscSettingsUIItems = new Vector();

            // Add sync mode setting if required
            if(customization.showSyncModeInSettingsScreen()) {
                SettingsUIItem item = ssScreen.addSyncModeSetting();
                if (item != null) {
                    miscSettingsUIItems.addElement(item);
                }
                item = ssScreen.addSyncIntervalSetting();
                if (item != null) {
                    miscSettingsUIItems.addElement(item);
                }
            }

            // Add C2S push setting if required
            if(customization.showC2SPushInSettingsScreen()) {
                SettingsUIItem item = ssScreen.addC2SPushSetting();
                if (item != null) {
                    miscSettingsUIItems.addElement(item);
                }
            }
            
            sourceSettingsUIItems = new Vector();

            Vector tempItems = new Vector();
            
            // Add an item for each registered source that has to fit into the 
            // settings screen.
            Enumeration sources = appSyncSourceManager.getRegisteredSources();
            while (sources.hasMoreElements()) {
                AppSyncSource appSource = (AppSyncSource)sources.nextElement();

                if (controller.isVisible(appSource) && appSource.hasSettings()) {
                    // Get the settings item for this source
                    SettingsUISyncSource item = appSource.createSettingsUISyncSource(ssScreen);

                    item.setSource(appSource);

                    // Set the title
                    item.setTitle(appSource.getName());

                    // Set the icons
                    int sourceId = appSource.getId();
                    item.setEnabledIcon(customization.getSourceIcon(sourceId));
                    item.setDisabledIcon(customization.getSourceDisabledIcon(sourceId));

                    // Load settings
                    item.loadSettings(configuration);

                    // The user can change this option if the source is
                    // working and active
                    if (!appSource.getConfig().getActive() || !appSource.isWorking()) {
                        item.setEnabled(false);
                    }
                    item.layout();

                    tempItems.addElement(item);
                }
            }

            sourceSettingsUIItems.setSize(tempItems.size());
            ssScreen.setSettingsUISyncSourceCount(tempItems.size());

            // Now recompute the ui position for all available sources
            int sourcesOrder[] = customization.getSourcesOrder();
            int uiOrder = 0;
            for (int i=0;i<sourcesOrder.length;++i) {
                int sourceId = sourcesOrder[i];
                // If this is a working source, then set its UI position
                AppSyncSource source = appSyncSourceManager.getSource(sourceId);
                if (controller.isVisible(source)) {
                    Log.debug(TAG_LOG, "Setting source " + source.getName() + " at position: " + uiOrder);
                    source.setUiSourceIndex(uiOrder++);
                }
            }

            for(int i=0; i<tempItems.size(); i++) {
                
                SettingsUISyncSource item = (SettingsUISyncSource)tempItems.elementAt(i);
                AppSyncSource source = item.getSource();

                if(source != null) {
                    int index = item.getSource().getUiSourceIndex();
                    sourceSettingsUIItems.setElementAt(item.getSource(), index);
                    ssScreen.setSettingsUISyncSource(item, index);
                }
            }
            tempItems.removeAllElements();
        }
    }


    public boolean hasChanges() {

        boolean result = false;
        if(miscSettingsUIItems != null) {
            Enumeration miscItems = miscSettingsUIItems.elements();
            while(miscItems.hasMoreElements()) {
                SettingsUIItem item = (SettingsUIItem)miscItems.nextElement();
                result |= item.hasChanges();
            }
        }
        if(sourceSettingsUIItems != null) {
            Enumeration items = sourceSettingsUIItems.elements();
            while(items.hasMoreElements()) {
                AppSyncSource item = (AppSyncSource)items.nextElement();
                if(item.isWorking()) {
                    result |= item.getSettingsUISyncSource().hasChanges();
                }
            }
        }
        return result;
    }

    public void saveSettings() {

        Log.debug(TAG_LOG, "Saving sync settings");

        if(miscSettingsUIItems != null) {
            Enumeration miscItems = miscSettingsUIItems.elements();
            while(miscItems.hasMoreElements()) {
                SettingsUIItem item = (SettingsUIItem)miscItems.nextElement();
                if(item.hasChanges()) {
                    item.saveSettings(configuration);
                }
            }
        }

        if(sourceSettingsUIItems != null) {
            // Save SyncSources settings
            Enumeration items = sourceSettingsUIItems.elements();
            while(items.hasMoreElements()) {
                AppSyncSource item = (AppSyncSource)items.nextElement();
                if(item.isWorking()) {
                    SettingsUISyncSource settingsItem = item.getSettingsUISyncSource();
                    AppSyncSourceConfig config = item.getConfig();
                    // Save the settings
                    settingsItem.saveSettings(configuration);
                    config.commit();
                }
            }
        }
        // Save the global configuration
        configuration.save();

        updateListOfSources();
    }
}
