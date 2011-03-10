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

package com.funambol.android.controller;

import android.accounts.Account;
import android.content.Context;
import com.funambol.android.AndroidAccountManager;

import com.funambol.platform.NetworkStatus;
import com.funambol.client.localization.Localization;
import com.funambol.client.controller.Controller;
import com.funambol.client.controller.ControllerDataFactory;
import com.funambol.client.configuration.Configuration;
import com.funambol.client.customization.Customization;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.engine.SyncEngine;

public class AndroidController extends Controller {

    private static Context mContext;

    private static AndroidController instance;
    private static SyncEngine engine;

    private AndroidSettingsScreenController settingsScreenController;

    // Constructor
    private AndroidController(Context context, ControllerDataFactory fact,
                              Configuration configuration,
                              Customization customization, Localization localization,
                              AppSyncSourceManager appSyncSourceManager)
    {
        super(fact, configuration, customization, localization, appSyncSourceManager);
        mContext = context;
        // The Android sync mode handler is different from the standard one
        syncModeHandler = new AndroidSyncModeHandler(context, configuration);
    }

    public static AndroidController getInstance(Context context,
                                                ControllerDataFactory fact,
                                                Configuration configuration,
                                                Customization customization,
                                                Localization  localization,
                                                AppSyncSourceManager appSyncSourceManager)
    {
        if (instance == null) {
            instance = new AndroidController(context, fact, configuration, customization,
                                             localization, appSyncSourceManager);
        }
        return instance;
    }

    public static AndroidController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Android controller not yet initialized");
        }
        return instance;
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    public static void dispose() {
        instance = null;
        engine = null;
    }


    public void setSettingsScreenController(AndroidSettingsScreenController settingsScreenController) {
        this.settingsScreenController = settingsScreenController;
    }

    public AndroidSettingsScreenController getSettingsScreenController() {
        return settingsScreenController;
    }

    /**
     * Return the SyncEngine for the activities of this application. Beware that
     * this engine is a singleton and it is important to preserve this
     * semantics. By having a singleton we can guarantee that on activities hot
     * start, we can properly restore the program behavior.
     */
    public SyncEngine createSyncEngine() {
        if (engine == null) {
            NetworkStatus networkStatus = new NetworkStatus(mContext);
            engine = new SyncEngine(customization, configuration,
                                    appSyncSourceManager, networkStatus);
        }
        return engine;
    }

    /**
     * Retrieve the Android account reference from the native account manager.
     * @return
     */
    public static Account getNativeAccount() {
        return getNativeAccount(mContext);
    }

    /**
     * Retrieve the Android account reference from the native account manager.
     *
     * @param context
     * @return
     */
    public static Account getNativeAccount(Context context) {
        if(context == null) {
            return null;
        }
        return AndroidAccountManager.getNativeAccount(context);
    }

}
