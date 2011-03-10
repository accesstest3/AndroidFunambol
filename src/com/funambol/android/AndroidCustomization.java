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

package com.funambol.android;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.customization.Customization;
import com.funambol.client.ui.Bitmap;
import com.funambol.util.Log;
import com.funambol.syncml.protocol.SyncML;

/**
 * A container for the customization values. Singleton realization: the instance
 * of this class can be claimed using the super class related getter method
 * invokation. Change this file in order to enable or disable the clien't
 * features
 */
public class AndroidCustomization extends Customization {

    private static final String TAG = "AndroidCustomization";

    private static AndroidCustomization instance = null;

    private static final String VERSION          = null;
    private static final String FULL_NAME        = "Funambol Android Sync Client";
    private static final String APPLICATION_NAME = "Funambol Sync";

    // this is the string used to populate the user agent
    private static final String USER_AGENT_NAME  = "Funambol Android Sync Client";

    // Sync sources customization
    protected final String   CONTACTS_DEFAULT_URI    = "card";
    protected final boolean  CONTACTS_AVAILABLE      = true;
    protected final boolean  CONTACTS_ENABLED        = true;

    protected final String   EVENTS_DEFAULT_URI      = "event";
    protected final boolean  EVENTS_AVAILABLE        = false;
    protected final boolean  EVENTS_ENABLED          = false;

    protected final String   TASKS_DEFAULT_URI       = "task";
    protected final boolean  TASKS_AVAILABLE         = false;
    protected final boolean  TASKS_ENABLED           = false;

    protected final String   PICTURES_DEFAULT_URI    = "picture";
    protected final boolean  PICTURES_AVAILABLE      = true;
    protected final boolean  PICTURES_ENABLED        = true;

    protected final String   VIDEOS_DEFAULT_URI      = "video";
    protected final boolean  VIDEOS_AVAILABLE        = false;
    protected final boolean  VIDEOS_ENABLED          = false;

    protected final String   CONFIG_DEFAULT_URI      = "configuration";
    protected final boolean  CONFIG_AVAILABLE        = true;

    // This is the account screen class name. It can be customized for versions
    // with a different account screen implementation
    protected final String   LOGIN_SCREEN_CLASS_NAME = "com.funambol.android.activities.AndroidLoginScreen";

    // This is the account screen authenticator for the unit tests
    protected final String   UT_ACCOUNT_SCREEN_CLASS_NAME = "com.funambol.android.UnitTestAuthenticator";

    // Defines the classto use while displaying a single sync source in the home screen
    protected final String   ALONE_UI_SYNC_SOURCE_CLASS_NAME = "com.funambol.android.activities.AndroidAloneUISyncSource";

    // Tells wether the account settings tab shall be displayed
    protected final boolean  SHOW_ACCOUNT_SETTINGS   = true;

    // Note: this array must be kept aligned with the list of sources that we
    // register (see initSourcesInfo below)
    private final int SOURCES_ORDER[] = { AndroidAppSyncSourceManager.CONTACTS_ID,
                                          //AndroidAppSyncSourceManager.EVENTS_ID,
                                          //AndroidAppSyncSourceManager.TASKS_ID,
                                          //AndroidAppSyncSourceManager.NOTES_ID,
                                          AndroidAppSyncSourceManager.PICTURES_ID,
                                          //AndroidAppSyncSourceManager.VIDEOS_ID
                                          //AndroidAppSyncSourceManager.MAILS_ID
                                        };

    private final int[] AVAILABLE_SYNC_MODES = {Configuration.SYNC_MODE_MANUAL,
                                                Configuration.SYNC_MODE_SCHEDULED};

    protected final String   SUPPORT_EMAIL_ADDRESS   = "fac_log@funambol.com";

    // Tha name of the SQLite Database used to store application data
    // (e.g. the configuration, sources tracker data)
    protected final String   FUNAMBOL_SQLITE_DB_NAME = "funambol.db";

    /**
     * Private constructor for the singleton pattern enforcement
     */
    private AndroidCustomization() {
        super();
    }

    /**
     * Set this Object instance to null
     */
    public static void dispose() {
        instance = null;
    }

    public static AndroidCustomization getInstance() {
        if (instance == null) {
            instance = new AndroidCustomization();
        }
        return instance;
    }

    public String getFunambolSQLiteDbName() {
        return FUNAMBOL_SQLITE_DB_NAME;
    }

    @Override
    public boolean syncAllOnMainScreenRequired() {
        return true;
    }

    @Override
    public int[] getAvailableSyncModes() {
        return AVAILABLE_SYNC_MODES;
    }

    public String getApplicationFullname() {
        return FULL_NAME;
    }
    
    public String getUserAgentName() {
        return USER_AGENT_NAME;
    }

    public String getApplicationTitle() {
        return APPLICATION_NAME;
    }

    public int[] getSourcesOrder() {
        return SOURCES_ORDER;
    }

    public Bitmap getImageLogo() {
        return null;
    }

    public Bitmap getSyncAllIcon() {
        return null;
    }

    @Override
    public Bitmap getPoweredByLogo() {
        return new Bitmap(new Integer(R.drawable.powered_by));
    }

    public Bitmap getSyncAllBackground() {
        return new Bitmap(new Integer(R.drawable.icon_sync_all_blue));
    }

    public Bitmap getSyncAllHighlightedBackground() {
        return new Bitmap(new Integer(R.drawable.icon_sync_all_blue));
    }

    public Bitmap getButtonBackground() {
        return null;
    }

    public Bitmap getButtonHighlightedBackground() {
        return null;
    }

    public Bitmap getOkIcon() {
        return new Bitmap(new Integer(R.drawable.icon_complete));
    }

    public Bitmap getErrorIcon() {
        return new Bitmap(new Integer(R.drawable.icon_failed_complete));
    }

    public Bitmap getCancelledIcon() {
        return null;
    }

    public Bitmap getWarningIcon() {
        return null;
    }

    @Override
    public boolean showSyncIconOnSelection() {
        return false;
    }

    public Bitmap getStatusSelectedIcon() {
        return new Bitmap(new Integer(android.R.drawable.ic_popup_sync));
    }

    public Bitmap getFolderIcon() {
        return null;
    }

    public Bitmap[] getStatusIconsForAnimation() {
        Bitmap[] bitmaps = new Bitmap[6];

        bitmaps[0] = new Bitmap(new Integer(R.drawable.icon_progress_sync_1));
        bitmaps[1] = new Bitmap(new Integer(R.drawable.icon_progress_sync_2));
        bitmaps[2] = new Bitmap(new Integer(R.drawable.icon_progress_sync_3));
        bitmaps[3] = new Bitmap(new Integer(R.drawable.icon_progress_sync_4));
        bitmaps[4] = new Bitmap(new Integer(R.drawable.icon_progress_sync_5));
        bitmaps[5] = new Bitmap(new Integer(R.drawable.icon_progress_sync_6));
        
        return bitmaps;
    }

    public Bitmap[] getStatusHugeIconsForAnimation() {
        Bitmap[] bitmaps = new Bitmap[16];

        bitmaps[0] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame01));
        bitmaps[1] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame02));
        bitmaps[2] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame03));
        bitmaps[3] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame04));
        bitmaps[4] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame05));
        bitmaps[5] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame06));
        bitmaps[6] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame07));
        bitmaps[7] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame08));
        bitmaps[8] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame09));
        bitmaps[9] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame10));
        bitmaps[10] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame11));
        bitmaps[11] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame12));
        bitmaps[12] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame13));
        bitmaps[13] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame14));
        bitmaps[14] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame15));
        bitmaps[15] = new Bitmap(new Integer(R.drawable.icon_sync_154x154_frame16));
        
        return bitmaps;
    }

    public boolean isDisplayNameSupported() {
        return false;
    }

    public String getVersion() {
        StringBuffer result = new StringBuffer();
        if (VERSION == null) {
            // Grab the version from the BuildInfo
            result.append(BuildInfo.VERSION);
        } else {
            result.append(VERSION);
        }
        if (!"release".equals(BuildInfo.MODE)) {
            result.append(" (").append(BuildInfo.DATE).append(")");
        }
        return result.toString();
    }

    public String getSupportEmailAddress() {
        return SUPPORT_EMAIL_ADDRESS;
    }

    public String getLoginScreenClassName() {
        if (BuildInfo.UNIT_TEST) {
            return UT_ACCOUNT_SCREEN_CLASS_NAME;
        } else {
            return LOGIN_SCREEN_CLASS_NAME;
        }
    }

    public String getAloneUISyncSourceClassName() {
        return ALONE_UI_SYNC_SOURCE_CLASS_NAME;
    }
        
    public boolean getShowAccountSettings() {
        return SHOW_ACCOUNT_SETTINGS;
    }

    protected void initSourcesInfo() {

        Log.info(TAG, "Initializing sources info");

        // Initialize the sources available in this application
        if(CONTACTS_AVAILABLE) {
            int id = AndroidAppSyncSourceManager.CONTACTS_ID;
            Log.debug(TAG, "Initializing source: " + id);
            sourcesUri.put(new Integer(id), CONTACTS_DEFAULT_URI);
            activeSources.put(new Integer(id), new Boolean(CONTACTS_AVAILABLE));
            sourcesIcon.put(new Integer(id), new Bitmap(R.drawable.icon_contacts));
            sourcesDisabledIcon.put(new Integer(id), new Bitmap(R.drawable.icon_contacts_grey));
            sourcesSyncMode.put(new Integer(id), new Integer(CONTACTS_ENABLED ?
                SyncML.ALERT_CODE_FAST : SyncML.ALERT_CODE_NONE));
            sourcesSyncModes.put(new Integer(id), getDefaultSourceSyncModes());
        }
        if(EVENTS_AVAILABLE) {
            int id = AndroidAppSyncSourceManager.EVENTS_ID;
            Log.debug(TAG, "Initializing source: " + id);
            sourcesUri.put(new Integer(id), EVENTS_DEFAULT_URI);
            activeSources.put(new Integer(id), new Boolean(EVENTS_AVAILABLE));
            sourcesIcon.put(new Integer(id), new Bitmap(R.drawable.icon_calendar));
            sourcesDisabledIcon.put(new Integer(id), new Bitmap(R.drawable.icon_calendar_grey));
            sourcesSyncMode.put(new Integer(id), new Integer(EVENTS_ENABLED ?
                SyncML.ALERT_CODE_FAST : SyncML.ALERT_CODE_NONE));
            sourcesSyncModes.put(new Integer(id), getDefaultSourceSyncModes());
        }
        if(TASKS_AVAILABLE) {
            int id = AndroidAppSyncSourceManager.TASKS_ID;
            Log.debug(TAG, "Initializing source: " + id);
            sourcesUri.put(new Integer(id), TASKS_DEFAULT_URI);
            activeSources.put(new Integer(id), new Boolean(TASKS_AVAILABLE));
            sourcesIcon.put(new Integer(id), new Bitmap(R.drawable.icon_tasks));
            sourcesDisabledIcon.put(new Integer(id), new Bitmap(R.drawable.icon_tasks_grey));
            sourcesSyncMode.put(new Integer(id), new Integer(TASKS_ENABLED ?
                SyncML.ALERT_CODE_FAST : SyncML.ALERT_CODE_NONE));
            sourcesSyncModes.put(new Integer(id), getDefaultSourceSyncModes());
        }
        if(PICTURES_AVAILABLE) {
            int id = AndroidAppSyncSourceManager.PICTURES_ID;
            Log.debug(TAG, "Initializing source: " + id);
            sourcesUri.put(new Integer(id), PICTURES_DEFAULT_URI);
            activeSources.put(new Integer(id), new Boolean(PICTURES_AVAILABLE));
            sourcesIcon.put(new Integer(id), new Bitmap(R.drawable.icon_photo));
            sourcesDisabledIcon.put(new Integer(id), new Bitmap(R.drawable.icon_photo_grey));
            sourcesSyncMode.put(new Integer(id), new Integer(PICTURES_ENABLED ?
                SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW : SyncML.ALERT_CODE_NONE));
            sourcesSyncModes.put(new Integer(id), getPicturesSourceSyncModes());
        }
        if(VIDEOS_AVAILABLE) {
            int id = AndroidAppSyncSourceManager.VIDEOS_ID;
            Log.debug(TAG, "Initializing source: " + id);
            sourcesUri.put(new Integer(id), VIDEOS_DEFAULT_URI);
            activeSources.put(new Integer(id), new Boolean(VIDEOS_AVAILABLE));
            sourcesIcon.put(new Integer(id), new Bitmap(R.drawable.icon_video));
            sourcesDisabledIcon.put(new Integer(id), new Bitmap(R.drawable.icon_video_grey));
            sourcesSyncMode.put(new Integer(id), new Integer(VIDEOS_ENABLED ?
                SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW : SyncML.ALERT_CODE_NONE));
            sourcesSyncModes.put(new Integer(id), getVideosSourceSyncModes());
        }
        if(CONFIG_AVAILABLE) {
            int id = AndroidAppSyncSourceManager.CONFIG_ID;
            Log.debug(TAG, "Initializing source: " + id);
            sourcesUri.put(new Integer(id), CONFIG_DEFAULT_URI);
            activeSources.put(new Integer(id), new Boolean(CONFIG_AVAILABLE));
            sourcesSyncMode.put(new Integer(id), new Integer(SyncML.ALERT_CODE_FAST));
        }
    }

    private int[] getDefaultSourceSyncModes() {

        return new int[] { SyncML.ALERT_CODE_NONE,
                           SyncML.ALERT_CODE_FAST};
    }

    private int[] getPicturesSourceSyncModes() {

        return new int[] { SyncML.ALERT_CODE_NONE,
                           SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW};
    }

    private int[] getVideosSourceSyncModes() {

        return new int[] { SyncML.ALERT_CODE_NONE,
                           SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW};
    }

}
