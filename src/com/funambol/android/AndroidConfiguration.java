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

import android.os.Build;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.content.Context;

import com.funambol.syncml.spds.DeviceConfig;
import com.funambol.client.configuration.Configuration;
import com.funambol.client.customization.Customization;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.util.Base64;
import com.funambol.util.Log;

/**
 * Container for the main client client configuration information. Relized using
 * the singleton pattern. Access this class using the getInstance() metod
 * invocation
 */
public class AndroidConfiguration extends Configuration {

    private static final String TAG_LOG = "AndroidConfiguration";

    public static final String KEY_FUNAMBOL_PREFERENCES = "fnblPref";
    
    private static AndroidConfiguration instance = null;
    private        Context context;
    protected      SharedPreferences settings;
    protected      SharedPreferences.Editor editor;
    private        DeviceConfig devconf;

    /**
     * Private contructor to enforce the Singleton implementation
     * @param context the application Context
     * @param customization the Customization object passed by the getInstance
     * call
     * @param appSyncSourceManager the AppSyncSourceManager object. Better to
     * use an AndroidAppSyncSourceManager or an extension of its super class
     */
    private AndroidConfiguration(Context context,
                                 Customization customization,
                                 AppSyncSourceManager appSyncSourceManager)
    {
        super(customization, appSyncSourceManager);
        this.context = context;
        settings = context.getSharedPreferences(KEY_FUNAMBOL_PREFERENCES, 0);
        editor = settings.edit();
    }

    /**
     * Static method that returns the AndroidConfiguration unique instance
     * @param context the application Context object
     * @param customization the AndoidCustomization object used in this client
     * @param appSyncSourceManager the AppSyncSourceManager object. Better to
     * use an AndroidAppSyncSourceManager or an extension of its super class
     * @return AndroidConfiguration an AndroidConfiguration unique instance
     */
    public static AndroidConfiguration getInstance(Context context,
                                                   Customization customization,
                                                   AppSyncSourceManager appSyncSourceManager)
    {
        if (instance == null) {
            instance = new AndroidConfiguration(context, customization, appSyncSourceManager);
        }
        return instance;
    }

    /**
     * Dispose this object referencing it with the null object
     */
    public static void dispose() {
        instance = null;
    }

    /**
     * Load the value referred to the configuration given the key
     * @param key the String formatted key representing the value to be loaded
     * @return String String formatted vlaue related to the give key
     */
    protected String loadKey(String key) {
        return settings.getString(key, null);
    }

    /**
     * Save the loaded twin key-value using the android context package
     * SharedPreferences.Editor instance
     * @param key the key to be saved
     * @param value the value related to the key String formatted
     */
    protected void saveKey(String key, String value) {
        editor.putString(key, value);
    }

    /**
     * Save the loaded twin key-value using the android context package
     * SharedPreferences.Editor instance
     * @param key the key to be saved
     * @param value the value related to the key byte[] formatted
     */
    public void saveByteArrayKey(String key, byte[] value) {
        String b64 = new String(Base64.encode(value));
        saveKey(key, b64);
    }

    /**
     * Load the value referred to the configuration given the key and the
     * default value
     * @param key the String formatted key representing the value to be loaded
     * @param defaultValue the default byte[] formatted value related to the
     * given key
     * @return byte[] String formatted vlaue related to the give key byte[]
     * formatted
     */
    public byte[] loadByteArrayKey(String key, byte[] defaultValue) {
        String b64 = loadKey(key);
        if (b64 != null) {
            return Base64.decode(b64);
        } else {
            return defaultValue;
        }
    }

    /**
     * Commit the changes
     * @return true if new values were correctly written into the persistent
     * storage
     */
    public boolean commit() {
        return editor.commit();
    }

    /**
     * Get the device id related to this client. Useful when doing syncml
     * requests
     * @return String the device id that is formatted as the string "fac-" plus
     * the information of the deviceId field got by the TelephonyManager service
     */
    protected String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // must have android.permission.READ_PHONE_STATE
        String deviceId = tm.getDeviceId();
        return "fac-" + deviceId;
    }

    /**
     * Get the device related configuration
     * @return DeviceConfig the DeviceConfig object related to this device
     */
    protected DeviceConfig getDeviceConfig() {
        if (devconf != null) {
            return devconf;
        }
        devconf = new DeviceConfig();
        devconf.man = Build.MANUFACTURER;
        devconf.mod = Build.MODEL;
        // See here for possible values of SDK_INT
        // http://developer.android.com/reference/android/os/Build.VERSION_CODES.html
        devconf.swv = Build.VERSION.CODENAME + "(" + Build.VERSION.SDK_INT + ")";
        devconf.hwv = Build.FINGERPRINT;
        devconf.devID = getDeviceId();
        devconf.setMaxMsgSize(64 * 1024);
        devconf.loSupport = true;
        devconf.utc = true;
        devconf.nocSupport = true;
        return devconf;
    }

    /**
     * Get the user agent id related to this client. Useful when doing syncml
     * requests
     * @return String the user agent that is formatted as the string
     * "Funambol Android Sync Client " plus the version of the client
     */
    protected String getUserAgent() {
        StringBuffer ua = new StringBuffer(
                ((AndroidCustomization)customization).getUserAgentName());
        ua.append(" ");
        ua.append(BuildInfo.VERSION);
        return ua.toString();
    }

    /**
     * Migrate the configuration (anything specific to the client)
     */
    @Override
    protected void migrateConfig() {

        // From 6 to 7 means from Diablo to Gallardo, where we introduced a new
        // mechanism for picture sync. We need to check what the server supports
        // to switch to the new method.
        if ("6".equals(version)) {
            setForceServerCapsRequest(true);
        }

        // Now migrate the basic configuration (this will update version)
        super.migrateConfig();
    }

}
