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

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore.Images.Media;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;


import com.funambol.android.source.pim.AndroidPIMCacheTracker;
import com.funambol.android.source.pim.contact.ContactSyncSource;
import com.funambol.android.source.pim.contact.ContactExternalAppManager;
import com.funambol.android.source.pim.contact.ContactAppSyncSourceConfig;
import com.funambol.android.source.pim.contact.ContactSettingsUISyncSource;
import com.funambol.android.source.pim.contact.VersionCacheTracker;
import com.funambol.android.source.pim.calendar.CalendarSyncSource;
import com.funambol.android.source.pim.calendar.EventSyncSource;
import com.funambol.android.source.pim.calendar.CalendarManager;
import com.funambol.android.source.pim.calendar.CalendarAppSyncSource;
import com.funambol.android.source.pim.calendar.CalendarChangesTracker;
import com.funambol.android.source.pim.calendar.CalendarChangesTrackerMD5;
import com.funambol.android.source.pim.calendar.CalendarExternalAppManager;
import com.funambol.android.source.pim.calendar.CalendarAppSyncSourceConfig;
import com.funambol.android.source.pim.task.AstridTaskManager;

import com.funambol.android.source.media.MediaAppSyncSource;
import com.funambol.android.source.media.picture.PictureSyncSource;
import com.funambol.android.source.media.picture.TwoPhasesPictureSyncSource;
import com.funambol.android.source.media.picture.PictureAppSyncSourceConfig;
import com.funambol.android.source.media.MediaExternalAppManager;
import com.funambol.android.source.media.picture.PictureTracker;

import com.funambol.android.source.media.video.VideoAppSyncSourceConfig;
import com.funambol.android.source.media.video.TwoPhasesVideoSyncSource;
import com.funambol.android.source.media.video.VideoTracker;

import com.funambol.client.controller.SynchronizationController;
import com.funambol.client.customization.Customization;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.AppSyncSourceConfig;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.localization.Localization;
import com.funambol.storage.StringKeyValueMemoryStore;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.client.ConfigSyncSource;
import com.funambol.syncml.client.CacheTracker;

import com.funambol.util.Log;

/**
 * A manager for the AndroidAppSyncSource instances in use by the Android client
 * The access to this class can be made invoking the static method getInstance()
 * as the pattern was realized as Singleton. Once the instance is no more used
 * by the caller it is suitable to call the dispose() method in order to release
 * this class' resources as soon as possible.
 */
public class AndroidAppSyncSourceManager extends AppSyncSourceManager {

    private static final String TAG = "AndroidAppSyncSourceManager";

    private static AndroidAppSyncSourceManager instance = null;

    private Localization localization;
    private Context      context;

    /**
     * The private constructor that enforce the Singleton implementation
     * @param customization the Customization object to be referred to
     * @param localization the Localization pattern for this manager
     * @param context the Context object to be related to the manager
     */
    private AndroidAppSyncSourceManager(Customization customization,
                                        Localization localization,
                                        Context context) {
        super(customization);
        this.localization = localization;
        this.context = context;
    }

    /**
     * Single instance call
     * @param customization the Customization object to be referred to
     * @param localization the Localization pattern for this manager
     * @param context the Context object to be related to the manager
     * @return the single instance of this class, creating it if it has never
     * been referenced
     */
    public static AndroidAppSyncSourceManager getInstance(Customization customization,
                                                          Localization localization,
                                                          Context context) {
        if (instance == null) {
            instance = new AndroidAppSyncSourceManager(customization, localization, context);
        }
        return instance;
    }

    /**
     * Dispose the single instance referencing it with the null object
     */
    public static void dispose() {
        instance = null;
    }

    /**
     * Setup the SyncSource identified by its sourceId index
     * @param sourceId the int that identifies the source
     * @param configuration the AndroidConfiguration object used to setup the
     * source
     * @return AppSyncSource the instance of the setup AppSyncSource
     * @throws Exception
     */
    public AppSyncSource setupSource(int sourceId, AndroidConfiguration configuration) throws Exception {
        Log.info(TAG, "Setting up source: " + sourceId);
        AppSyncSource appSource;
        switch(sourceId) {
            case CONTACTS_ID:
            {
                appSource = setupContactsSource(configuration);
                break;
            }
            case EVENTS_ID:
            {
                appSource = setupEventsSource(configuration);
                break;
            }
            case TASKS_ID:
            {
                appSource = setupTasksSource(configuration);
                break;
            }
            case CONFIG_ID:
            {
                appSource = setupConfigSource(configuration);
                break;
            }
            case PICTURES_ID:
            {
                appSource = setupPicturesSource(configuration);
                break;
            }
            case VIDEOS_ID:
            {
                appSource = setupVideosSource(configuration);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown source: " + sourceId);
        }
        return appSource;
    }

    /**
     * Setup the source for contacts
     * @param configuration the AndroidConfiguration to be used to setup the
     * source
     * @return AppSyncSource related to contacts
     * @throws Exception
     */
    protected AppSyncSource setupContactsSource(AndroidConfiguration configuration) throws Exception {

        int id = CONTACTS_ID;
        String name = localization.getLanguage("type_contacts");

        AndroidAppSyncSource appSyncSource = new AndroidAppSyncSource(name);
        appSyncSource.setId(id);

        // On the 2.0.x simulator there is no support for native sync, so in
        // this case we revert to sync service method
        if (AndroidUtils.isSimulator(context) && Build.VERSION.SDK_INT < 7) {
            appSyncSource.setSyncMethod(AndroidAppSyncSource.SERVICE);
        } else {
            appSyncSource.setSyncMethod(AndroidAppSyncSource.SYNC_ADAPTER);
        }
        appSyncSource.setAuthority(AndroidAppSyncSource.AUTHORITY_CONTACTS);
        appSyncSource.setProviderUri(ContactsContract.Contacts.CONTENT_URI);

        // Create the proper settings component for this source
        Class basicSettings = Class.forName("com.funambol.android.source.pim.contact.ContactSettingsUISyncSource");
        appSyncSource.setSettingsUIClass(basicSettings);

        Class buttonView = Class.forName("com.funambol.android.activities.AndroidButtonUISyncSource");
        appSyncSource.setButtonUIClass(buttonView);

        Class aloneView = Class.forName(((AndroidCustomization)customization)
                .getAloneUISyncSourceClassName());
        appSyncSource.setAloneUIClass(aloneView);
        
        int order = getSourcePosition(id);
        appSyncSource.setUiSourceIndex(order);

        appSyncSource.setHasSetting(AppSyncSource.SYNC_MODE_SETTING,
                customization.isSyncDirectionVisible(),
                customization.getDefaultSourceSyncModes(id));
        appSyncSource.setHasSetting(
                ContactSettingsUISyncSource.DEFAULT_ADDRESS_BOOK_SETTING,
                true, "");

        appSyncSource.setBandwidthSaverUse(customization.useBandwidthSaverContacts());

        SourceConfig sc = null;
        if (SourceConfig.VCARD_TYPE.equals(customization.getContactType())) {
            // This is vcard format
            String defaultUri = customization.getDefaultSourceUri(id);
            sc = new SourceConfig("contacts", SourceConfig.VCARD_TYPE, defaultUri);
            sc.setEncoding(SyncSource.ENCODING_NONE);
            sc.setSyncMode(customization.getDefaultSourceSyncMode(id));
        }

        if (sc != null) {
            // Load the source config from the configuration
            ContactAppSyncSourceConfig asc = new ContactAppSyncSourceConfig(appSyncSource, customization, configuration);
            asc.load(sc);
            appSyncSource.setConfig(asc);

            // Create the sync source
            IntKeyValueSQLiteStore trackerStore =
                new IntKeyValueSQLiteStore(context, 
                ((AndroidCustomization)customization).getFunambolSQLiteDbName(),
                sc.getName());

            VersionCacheTracker tracker = new VersionCacheTracker(trackerStore, context);
            ContactSyncSource src = new ContactSyncSource(sc, tracker, context, configuration, appSyncSource);
            appSyncSource.setSyncSource(src);

            // Setup the external app manager
            ContactExternalAppManager appManager = new ContactExternalAppManager(context, appSyncSource);
            appSyncSource.setAppManager(appManager);
        } else {
            Log.error(TAG, "The contact sync source does not support the type: " + customization.getContactType());
            Log.error(TAG, "Contact source will be disabled as not working");
        }
        
        return appSyncSource;
    }

    /**
     * Setup the source for tasks
     * @param configuration the AndroidConfiguration to be used to setup the
     * source
     * @return AppSyncSource related to events
     * @throws Exception
     */
    protected AppSyncSource setupTasksSource(AndroidConfiguration configuration) throws Exception {

        int id = TASKS_ID;
        String name = localization.getLanguage("type_tasks");

        AndroidAppSyncSource appSyncSource = new AndroidAppSyncSource(name);
        appSyncSource.setId(id);


        appSyncSource.setSyncMethod(AndroidAppSyncSource.SYNC_ADAPTER);
        appSyncSource.setAuthority(AstridTaskManager.AUTHORITY);
        appSyncSource.setProviderUri(AstridTaskManager.Tasks.CONTENT_URI);

        // Create the proper settings component for this source
        Class calendarSettings = Class.forName("com.funambol.android.source.pim.calendar.CalendarSettingsUISyncSource");
        appSyncSource.setSettingsUIClass(calendarSettings);

        Class buttonView = Class.forName("com.funambol.android.activities.AndroidButtonUISyncSource");
        appSyncSource.setButtonUIClass(buttonView);

        Class aloneView = Class.forName("com.funambol.android.activities.AndroidAloneUISyncSource");
        appSyncSource.setAloneUIClass(aloneView);

        int order = getSourcePosition(id);
        appSyncSource.setUiSourceIndex(order);

        appSyncSource.setHasSetting(AppSyncSource.SYNC_MODE_SETTING,
                customization.isSyncDirectionVisible(),
                customization.getDefaultSourceSyncModes(id));

        SourceConfig sc = null;
        String defaultUri = customization.getDefaultSourceUri(id);
        sc = new SourceConfig("task", customization.getCalendarType(), defaultUri);
        sc.setEncoding(SyncSource.ENCODING_NONE);
        sc.setSyncMode(customization.getDefaultSourceSyncMode(id));

        CalendarAppSyncSourceConfig asc = new CalendarAppSyncSourceConfig(appSyncSource, customization, configuration);
        asc.load(sc);

        appSyncSource.setConfig(asc);

        // We need a third party app to sync task. At the moment we only support
        // Astrid, so we check for its availability and version
        PackageManager pm = context.getPackageManager();
        try {
            Log.debug(TAG, "Checking if Astrid is available");
            ProviderInfo info = pm.resolveContentProvider("com.todoroo.astrid", 0);
            if (info != null) {
                Log.info(TAG, "Astrid provider found, enable task source");
            } else {
                Log.info(TAG, "Astrid provider not found, disable task source");
                asc.setActive(false);
                return appSyncSource;
            }
        } catch (Exception e) {
            Log.error(TAG, "Error detecting Astrid", e);
            asc.setActive(false);
            return appSyncSource;
        }

        // Create the sync source
        IntKeyValueSQLiteStore trackerStore =
            new IntKeyValueSQLiteStore(context, 
            ((AndroidCustomization)customization).getFunambolSQLiteDbName(),
            sc.getName());

        // Since we sync calendars that our app does not owe, we cannot use sync
        // fields and calendars do not have revisions, so we are forced to use a
        // CacheTracker here (based on MD5)
        AndroidPIMCacheTracker tracker = new AndroidPIMCacheTracker(context, trackerStore);

        AstridTaskManager dm = new AstridTaskManager(context, appSyncSource);
        CalendarSyncSource src = new CalendarSyncSource(sc, tracker, context, configuration, appSyncSource, dm);
        appSyncSource.setSyncSource(src);

        // Setup the external app manager
        //CalendarExternalAppManager appManager = new CalendarExternalAppManager(context, appSyncSource);
        //appSyncSource.setAppManager(appManager);

        return appSyncSource;
    }


    /**
     * Setup the source for events
     * @param configuration the AndroidConfiguration to be used to setup the
     * source
     * @return AppSyncSource related to events
     * @throws Exception
     */
    protected AppSyncSource setupEventsSource(AndroidConfiguration configuration) throws Exception {

        int id = EVENTS_ID;
        String name = localization.getLanguage("type_calendar");

        CalendarAppSyncSource appSyncSource = new CalendarAppSyncSource(name);
        appSyncSource.setId(id);

        if (AndroidUtils.isSimulator(context) && Build.VERSION.SDK_INT < 7) {
            appSyncSource.setSyncMethod(AndroidAppSyncSource.SERVICE);
        } else {
            appSyncSource.setSyncMethod(AndroidAppSyncSource.SYNC_ADAPTER);
        }
        appSyncSource.setAuthority(CalendarManager.getCalendarAuthority());
        appSyncSource.setProviderUri(CalendarManager.Events.CONTENT_URI);

        // Create the proper settings component for this source
        Class calendarSettings = Class.forName("com.funambol.android.source.pim.calendar.CalendarSettingsUISyncSource");
        appSyncSource.setSettingsUIClass(calendarSettings);

        Class buttonView = Class.forName("com.funambol.android.activities.AndroidButtonUISyncSource");
        appSyncSource.setButtonUIClass(buttonView);

        Class aloneView = Class.forName(((AndroidCustomization)customization)
                .getAloneUISyncSourceClassName());
        appSyncSource.setAloneUIClass(aloneView);

        int order = getSourcePosition(id);
        appSyncSource.setUiSourceIndex(order);

        appSyncSource.setHasSetting(AppSyncSource.SYNC_MODE_SETTING,
                customization.isSyncDirectionVisible(),
                customization.getDefaultSourceSyncModes(id));

        SourceConfig sc = null;
        String defaultUri = customization.getDefaultSourceUri(id);
        sc = new SourceConfig("calendar", customization.getCalendarType(), defaultUri);
        sc.setEncoding(SyncSource.ENCODING_NONE);
        sc.setSyncMode(customization.getDefaultSourceSyncMode(id));

        CalendarAppSyncSourceConfig asc = new CalendarAppSyncSourceConfig(appSyncSource, customization, configuration);
        asc.load(sc);

        appSyncSource.setConfig(asc);

        appSyncSource.setBandwidthSaverUse(customization.useBandwidthSaverEvents());

        // Create the sync source
        IntKeyValueSQLiteStore trackerStore =
            new IntKeyValueSQLiteStore(context, 
            ((AndroidCustomization)customization).getFunambolSQLiteDbName(),
            sc.getName());

        CalendarManager dm = new CalendarManager(context, appSyncSource);

        // This source needs the calendar manager to decide if a valid calendar
        // is available
        appSyncSource.setCalendarManager(dm);

        // There are two different trackers depending on the OS version, because
        // OS < 2.2 does not allow the sync_dirty to be handled properly
        CalendarChangesTracker tracker;
        if (AndroidUtils.isSimulator(context) || Build.VERSION.SDK_INT >= 8) {
            tracker = new CalendarChangesTracker(context, trackerStore, dm);
        } else {
            tracker = new CalendarChangesTrackerMD5(context, trackerStore, dm);
        }

        EventSyncSource src = new EventSyncSource(sc, tracker, context, configuration, appSyncSource, dm);
        appSyncSource.setSyncSource(src);

        // Setup the external app manager
        CalendarExternalAppManager appManager = new CalendarExternalAppManager(context, appSyncSource);
        appSyncSource.setAppManager(appManager);

        return appSyncSource;
    }

    /**
     * Setup the source for pictures
     * @param configuration the AndroidConfiguration to be used to setup the
     * source
     * @return AppSyncSource related to pictures
     * @throws Exception
     */
    protected AppSyncSource setupPicturesSource(AndroidConfiguration configuration) throws Exception {

        int id = PICTURES_ID;
        String name = localization.getLanguage("type_photos");

        MediaAppSyncSource appSyncSource = new MediaAppSyncSource(name);
        appSyncSource.setId(id);
        appSyncSource.setSyncMethod(AndroidAppSyncSource.SYNC_ADAPTER);
        appSyncSource.setAuthority(AndroidAppSyncSource.AUTHORITY_MEDIA);
        appSyncSource.setAuthorityType(AndroidAppSyncSource.AUTHORITY_MEDIA_TYPE_PICTURES);
        appSyncSource.setWarningOnFirstSync(localization.getLanguage("dialog_first_picture_sync"));
        appSyncSource.setIsRefreshSupported(SynchronizationController.REFRESH_FROM_SERVER, false);
        appSyncSource.setIsRefreshSupported(SynchronizationController.REFRESH_TO_SERVER, true);
        int order = getSourcePosition(id);
        appSyncSource.setUiSourceIndex(order);
        appSyncSource.setProviderUri(Media.EXTERNAL_CONTENT_URI);

        appSyncSource.setBandwidthSaverUse(customization.useBandwidthSaverMedia());

        // Create the proper settings component for this source
        Class pictureSettings = Class.forName("com.funambol.android.source.media.picture.PictureSettingsUISyncSource");
        appSyncSource.setSettingsUIClass(pictureSettings);

        Class buttonView = Class.forName("com.funambol.android.activities.AndroidButtonUISyncSource");
        appSyncSource.setButtonUIClass(buttonView);

        Class aloneView = Class.forName(((AndroidCustomization)customization)
                .getAloneUISyncSourceClassName());
        appSyncSource.setAloneUIClass(aloneView);

        appSyncSource.setHasSetting(AppSyncSource.SYNC_MODE_SETTING,
                customization.isSyncDirectionVisible(),
                customization.getDefaultSourceSyncModes(id));
        appSyncSource.setHasSetting(AppSyncSource.SYNC_FOLDER_SETTING, true,
                localization.getLanguage("sync_pictures_folder_label"));

        SourceConfig sc = null;
        String defaultUri = customization.getDefaultSourceUri(id);
        sc = new SourceConfig("pictures", SourceConfig.FILE_OBJECT_TYPE, defaultUri);
        sc.setEncoding(SyncSource.ENCODING_NONE);
        sc.setSyncMode(customization.getDefaultSourceSyncMode(id));

        PictureAppSyncSourceConfig asc = new PictureAppSyncSourceConfig(appSyncSource, customization, configuration);
        asc.load(sc);

        appSyncSource.setConfig(asc);

        // Create the sync source
        IntKeyValueSQLiteStore trackerStore =
                new IntKeyValueSQLiteStore(context, 
                ((AndroidCustomization)customization).getFunambolSQLiteDbName(),
                sc.getName());
        PictureTracker tracker = new PictureTracker(context, trackerStore, appSyncSource, configuration);

        PictureSyncSource syncMLSrc = new PictureSyncSource(sc, tracker, context, appSyncSource, configuration);
        
        String httpUploadPrefix = customization.getHttpUploadPrefix();
        TwoPhasesPictureSyncSource src = new TwoPhasesPictureSyncSource(sc, tracker, context,
                                                                        appSyncSource, configuration, httpUploadPrefix);
        appSyncSource.setSyncMLSource(syncMLSrc);
        appSyncSource.setTwoPhasesSyncSource(src);
        // Allow the source to use the proper source
        appSyncSource.reapplyConfiguration();

        // Setup the external app manager
        MediaExternalAppManager appManager = new MediaExternalAppManager(context, appSyncSource);
        appSyncSource.setAppManager(appManager);

        return appSyncSource;
    }


    /**
     * Setup the source for videos
     * @param configuration the AndroidConfiguration to be used to setup the
     * source
     * @return AppSyncSource related to pictures
     * @throws Exception
     */
    protected AppSyncSource setupVideosSource(AndroidConfiguration configuration) throws Exception {

        int id = VIDEOS_ID;
        String name = localization.getLanguage("type_videos");

        MediaAppSyncSource appSyncSource = new MediaAppSyncSource(name);
        appSyncSource.setId(id);
        appSyncSource.setSyncMethod(AndroidAppSyncSource.SYNC_ADAPTER);
        appSyncSource.setAuthority(AndroidAppSyncSource.AUTHORITY_MEDIA);
        appSyncSource.setAuthorityType(AndroidAppSyncSource.AUTHORITY_MEDIA_TYPE_VIDEOS);
        appSyncSource.setWarningOnFirstSync(localization.getLanguage("dialog_first_video_sync"));
        appSyncSource.setIsRefreshSupported(SynchronizationController.REFRESH_FROM_SERVER, false);
        appSyncSource.setIsRefreshSupported(SynchronizationController.REFRESH_TO_SERVER, true);
        int order = getSourcePosition(id);
        appSyncSource.setUiSourceIndex(order);
        appSyncSource.setProviderUri(Media.EXTERNAL_CONTENT_URI);

        appSyncSource.setBandwidthSaverUse(customization.useBandwidthSaverMedia());

        // Create the proper settings component for this source
        Class pictureSettings = Class.forName("com.funambol.android.source.media.video.VideoSettingsUISyncSource");
        appSyncSource.setSettingsUIClass(pictureSettings);

        Class buttonView = Class.forName("com.funambol.android.activities.AndroidButtonUISyncSource");
        appSyncSource.setButtonUIClass(buttonView);

        Class aloneView = Class.forName("com.funambol.android.activities.AndroidAloneUISyncSource");
        appSyncSource.setAloneUIClass(aloneView);

        appSyncSource.setHasSetting(AppSyncSource.SYNC_MODE_SETTING,
                customization.isSyncDirectionVisible(),
                customization.getDefaultSourceSyncModes(id));
        appSyncSource.setHasSetting(AppSyncSource.SYNC_FOLDER_SETTING, true,
                localization.getLanguage("sync_videos_folder_label"));

        SourceConfig sc = null;
        String defaultUri = customization.getDefaultSourceUri(id);
        sc = new SourceConfig("videos", SourceConfig.FILE_OBJECT_TYPE, defaultUri);
        sc.setEncoding(SyncSource.ENCODING_NONE);
        sc.setSyncMode(customization.getDefaultSourceSyncMode(id));

        VideoAppSyncSourceConfig asc = new VideoAppSyncSourceConfig(appSyncSource, customization, configuration);
        asc.load(sc);

        appSyncSource.setConfig(asc);

        // Create the sync source
        IntKeyValueSQLiteStore trackerStore =
                new IntKeyValueSQLiteStore(context, 
                ((AndroidCustomization)customization).getFunambolSQLiteDbName(),
                sc.getName());
        VideoTracker tracker = new VideoTracker(context, trackerStore, appSyncSource, configuration);

        String httpUploadPrefix = customization.getHttpUploadPrefix();
        TwoPhasesVideoSyncSource src = new TwoPhasesVideoSyncSource(sc, tracker, context,
                                                                    appSyncSource, configuration, httpUploadPrefix);
        appSyncSource.setSyncSource(src);
        // Allow the source to use the proper source
        appSyncSource.reapplyConfiguration();

        // Setup the external app manager
        MediaExternalAppManager appManager = new MediaExternalAppManager(context, appSyncSource);
        appSyncSource.setAppManager(appManager);

        return appSyncSource;
    }


    /**
     * Setup the source for config
     * @param configuration the AndroidConfiguration to be used to setup the
     * source
     * @return AppSyncSource related to config
     * @throws Exception
     */
    private AppSyncSource setupConfigSource(AndroidConfiguration configuration) throws Exception {
        int id = CONFIG_ID;
        // This source is invisible, don't care about the name and its
        // localization
        String name = "config";
        AndroidAppSyncSource configSource = new AndroidAppSyncSource(name);
        configSource.setId(id);
        configSource.setEnabledLabel(null);
        configSource.setDisabledLabel(null);
        configSource.setIconName(null);
        configSource.setDisabledIconName(null);
        configSource.setUiSourceIndex(0);
        configSource.setIsRefreshSupported(false);
        configSource.setIsVisible(false);
        configSource.setSyncMethod(AndroidAppSyncSource.DIRECT);
        registerSource(configSource);

        SourceConfig sc = new SourceConfig("config", SourceConfig.BRIEFCASE_TYPE,
                                           customization.getDefaultSourceUri(id));
        sc.setEncoding(SyncSource.ENCODING_NONE);
        sc.setSyncMode(customization.getDefaultSourceSyncMode(id));

        AppSyncSourceConfig asc = new AppSyncSourceConfig(configSource, customization, configuration);
        asc.load(sc);
        configSource.setConfig(asc);

        StringKeyValueMemoryStore configStore = new StringKeyValueMemoryStore();
        StringKeyValueMemoryStore cacheStore  = new StringKeyValueMemoryStore();
        CacheTracker tracker = new CacheTracker(cacheStore);
 
        SyncSource syncSource = new ConfigSyncSource(sc, tracker, configStore);

        // Reset the underlying sync source
        configSource.setSyncSource(syncSource);

        return configSource;
    }
}
