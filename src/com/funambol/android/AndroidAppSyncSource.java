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

package com.funambol.android;

import android.accounts.Account;
import java.lang.reflect.Constructor;

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;

import com.funambol.android.activities.AndroidUISyncSource;
import com.funambol.android.activities.settings.AndroidSettingsUISyncSource;
import com.funambol.android.activities.AndroidButtonUISyncSource;
import com.funambol.android.controller.AndroidController;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.ui.SettingsUISyncSource;
import com.funambol.client.ui.Screen;
import com.funambol.client.ui.UISyncSource;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.util.Log;
import com.funambol.util.StringUtil;

/**
 * Implementation of the AppSyncSource for the Android client. Define the
 * generic AppSyncSource for this particular client
 */
public class AndroidAppSyncSource extends AppSyncSource {

    /** The Log String */
    private static final String TAG_LOG = "AndroidAppSyncSource";

    /** Direct method value = 0 */
    public static final int DIRECT = 0;

    /** Service method value = 1 */
    public static final int SERVICE = 1;

    /** SyncAdapter method value = 2 */
    public static final int SYNC_ADAPTER = 2;

    public static final String AUTHORITY_CONTACTS = "com.android.contacts";
    public static final String AUTHORITY_MEDIA = "media";
    public static final String AUTHORITY_MEDIA_TYPE_PICTURES = "images";
    public static final String AUTHORITY_MEDIA_TYPE_VIDEOS = "videos";

    public static final String AUTHORITY_TYPE_ALL = "all";

    private String authority;
    private String authorityType;
    private int    syncMethod;
    private boolean isMedia = false;

    private Uri providerUri;

    /**
     * Constructor
     * @param name is the String formatted name of the AndroidAppSyncSource to
     * be built
     * @param source is the SyncSource object to be wrapped by this object
     */
    public AndroidAppSyncSource(String name, SyncSource source) {
        super(name, source);
        syncMethod = SYNC_ADAPTER;
        authorityType = AUTHORITY_TYPE_ALL;
    }

    /**
     * Constructor
     * @param name is the String formatted name of the AndroidAppSyncSource to
     * be built
     */
    public AndroidAppSyncSource(String name) {
        this(name, null);
    }

    /**
     * Get the String representation of this instance related authority
     * @return String the String formatted representation of the authority
     * related to this AppSyncSource
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * Set the String representation of this instance related authority
     * @param String is the authority to be set
     */
    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getAuthorityType() {
        return authorityType;
    }

    public void setAuthorityType(String authorityType) {
        this.authorityType = authorityType;
    }

    public boolean getIsMedia() {
        return isMedia;
    }

    public void setIsMedia(boolean value) {
        isMedia = value;
    }

    /**
     * Get the sync Method related to this AppSyncSource
     * @return int the int value related to the sync method defined for this
     * AndroidAppSyncSource
     */
    public int getSyncMethod() {
        return syncMethod;
    }

    /**
     * Set the sync Method related to this AppSyncSource: valid values are
     * defined as constants in this class - SERVICE, DIRECT, SYNC_ADAPTER -
     * @param int the int value related to the sync method defined for this
     * AndroidAppSyncSource
     */
    public void setSyncMethod(int syncMethod) {
        this.syncMethod = syncMethod;
    }

    public Uri getProviderUri() {
        return providerUri;
    }

    public void setProviderUri(Uri providerUri) {
        this.providerUri = providerUri;
    }

    /**
     * Create the SettingsUISyncSource object valid for this
     * AndroidAppSyncSource using the given screen to retrieve the related
     * activity.
     * @param screen the Screen object
     * @return the created SettingsUISyncSource instance
     */
    @Override
    public SettingsUISyncSource createSettingsUISyncSource(Screen screen) {

        if (settingsClass != null) {
            Activity activity = (Activity)screen.getUiScreen();
            // Invoke the constructor
            try {
                Constructor c = settingsClass.getConstructor(new Class[]{Class.forName("android.app.Activity")});
                settingsUISource = (AndroidSettingsUISyncSource)c.newInstance(activity);
            } catch (Exception e) {
                Log.error(TAG_LOG, "Cannot create settings UI view", e);
            }
        } else {
            Log.error(TAG_LOG, "Cannot create instance for source settings");
        }

        if (settingsUISource == null) {
            throw new IllegalStateException("Cannot create settings for source");
        }
        return settingsUISource;
    }

    /**
     * Create the UISyncSource representation for this object. The Android
     * client use buttons representation.
     * @param screen the screen object
     * @return UISyncSource the AndroidAppSyncSource UI representation object:
     * AndroidButtonUISyncSource
     */
    @Override
    public UISyncSource createButtonUISyncSource(Screen screen) {

        if (buttonClass != null) {
            Activity activity = (Activity)screen.getUiScreen();
            // Invoke the constructor
            try {
                Constructor c = buttonClass.getConstructor(new Class[] {
                    Class.forName("android.app.Activity") } );
                uiSource = (AndroidUISyncSource)c.newInstance(activity);
            } catch (Exception e) {
                Log.error(TAG_LOG, "Cannot create button UI view for: " + getName(), e);
            }
        } else {
            Log.error(TAG_LOG, "Cannot create instance for button UI view for: " + getName());
        }

        if (uiSource == null) {
            throw new IllegalStateException("Cannot create representation for source");
        }
        return uiSource;
    }

    /**
     * Create the UISyncSource representation for this object when it is alone
     * in the screen. The Android client use buttons representation.
     * @param screen the screen object
     * @return UISyncSource the AndroidAppSyncSource UI representation object:
     * AndroidButtonUISyncSource or null if it cannot be created
     */
    @Override
    public UISyncSource createAloneUISyncSource(Screen screen) {

        if (aloneClass != null) {
            Activity activity = (Activity)screen.getUiScreen();
            // Invoke the constructor
            try {
                Constructor c = aloneClass.getConstructor(new Class[] {
                    Class.forName("android.app.Activity") } );
                uiSource = (AndroidUISyncSource)c.newInstance(activity);
            } catch (Exception e) {
                Log.error(TAG_LOG, "Cannot create button UI view for: " + getName(), e);
            }
        }

        return uiSource;
    }


    /**
     * Re-Apply the source configuration to this object in order to re-define
     * it: if a NATIVE Account exists it just define if it is syncable or not
     * using the related content resolver.
     */
    @Override
    public void reapplyConfiguration() {

        if(StringUtil.isNullOrEmpty(authority)) {
            return;
        }
        
        int syncable = config.getActive() && config.getEnabled() ? 1 : 0;

        Account account = AndroidController.getNativeAccount();
        if(account != null) {
            ContentResolver.setIsSyncable(account, authority, syncable);
        }
    }
}
