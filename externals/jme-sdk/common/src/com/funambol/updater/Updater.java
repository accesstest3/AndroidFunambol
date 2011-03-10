/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2003 - 2007 Funambol, Inc.
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
package com.funambol.updater;

import java.util.Calendar;
import java.util.Date;
import com.funambol.util.TransportAgent;
import com.funambol.util.HttpTransportAgent;
import com.funambol.util.Log;
import com.funambol.util.StringUtil;

public class Updater {

    private static final String TAG_LOG = "Updater";

    public static final String DEFAULT_UPDATER_REQUEST_URI = "/sapi/profile/client?action=get-update-info";
    
    /** Updater url parameters */
    private final String UPDATER_VERSION_PARAM      = "version";
    private final String UPDATER_COMPONENT_PARAM    = "component";
    private final String UPDATER_OS_PARAM           = "os";
    private final String UPDATER_MODEL_PARAM        = "model";
    private final String UPDATER_MANUFACTURER_PARAM = "manufacturer";
    private final String UPDATER_APPINFO_PARAM      = "appinfo";
    private final String UPDATER_CARRIER_PARAM      = "carrier";
    private final String UPDATER_FORMAT_PARAM       = "format";

    /** if empty the server sends back the upd info in JSON format */
    private final String UPDATER_PROPERTIES_FORMAT  = "properties";

    /** upd_info file properties */
    private final String VERSION_TAG         = "version=";
    private final String TYPE_TAG            = "type=";
    private final String URL_TAG             = "url=";
    private final String ACTIVATION_DATE_TAG = "activation-date=";

    private UpdaterConfig config;
    
    private String currentVersion;
    private String component;
    private String os = null;
    private String model = null;
    private String manufacturer = null;
    private String appinfo = null;
    private String carrier = null;

    private TransportAgent userTA = null;
    private UpdaterListener listener  = null;

    /** The updater url */
    private String requestUri        = DEFAULT_UPDATER_REQUEST_URI;
    
    public Updater(UpdaterConfig config, String currentVersion, String component) {
        this.config = config;
        this.currentVersion = currentVersion;
        this.component = component;
    }

    public Updater(UpdaterConfig config, String currentVersion, String component,
            String os) {
        this.config = config;
        this.currentVersion = currentVersion;
        this.component = component;
        this.os = os;
    }

    public Updater(UpdaterConfig config, String currentVersion, String component,
            String os, String model, String manufacturer, String appinfo, String carrier) {
        this.config = config;
        this.currentVersion = currentVersion;
        this.component = component;
        this.os = os;
        this.model = model;
        this.manufacturer = manufacturer;
        this.appinfo = appinfo;
        this.carrier = carrier;
    }

    public Updater(UpdaterConfig config, String currentVersion, TransportAgent userTA) {
        this.config = config;
        this.currentVersion = currentVersion;
        this.userTA = userTA;
    }

    public void setListener(UpdaterListener listener) {
        this.listener = listener;
    }

    public void setTransportAgent(TransportAgent ta) {
        this.userTA = ta;
    }

    public boolean check() {

        // TODO: check if network is available in a platform neutral way

        // Check if updates are available from server
        checkUpdateFromServer();

        if (updateIsReportable() && isNewVersionAvailable()) {
            Log.info(TAG_LOG, "Update available ");
            if(listener != null) {
                if(config.isOptional()) {
                    Log.info(TAG_LOG, "Optional update is available");
                    listener.optionalUpdateAvailable(config.getAvailableVersion());
                } else if(config.isMandatory()) {
                    Log.info(TAG_LOG, "Mandatory update is available");
                    listener.mandatoryUpdateAvailable(config.getAvailableVersion());
                } else {
                    if(!config.isRecommended()) {
                        Log.error(TAG_LOG, "Unknown update type: " + 
                                config.getType() + ", assume it is recommended");
                    }
                    Log.info(TAG_LOG, "Recommended update is available");
                    listener.recommendedUpdateAvailable(config.getAvailableVersion());
                }
            }
            return true;
        }
        return false;
    }

    public boolean isNewVersionAvailable() {

        final String VER_SEP = ".";
        boolean possibleUpdate = false;

        String version = currentVersion;

        Log.info(TAG_LOG, "Current version : " + currentVersion);
        String fversion = config.getAvailableVersion();
        if (fversion == null || " ".equals(fversion)) {
            fversion = currentVersion;
        }
        Log.info(TAG_LOG, "Available version : " + fversion);

        int vpos = 0;
        int vfpos = 0;
        do {
            vpos = version.indexOf(VER_SEP, 0);
            vfpos = fversion.indexOf(VER_SEP, 0);
            if (vpos < 0 && version.length() > 0) {
                vpos = version.length();
            }
            if (vfpos < 0 && fversion.length() > 0) {
                vfpos = fversion.length();
            }

            if (vpos > 0 && vfpos > 0) {
                int val = Integer.parseInt(version.substring(0, vpos));
                int fval = Integer.parseInt(fversion.substring(0, vfpos));
                if (val < fval) {
                    Log.debug(TAG_LOG, "Current version is old");
                    possibleUpdate = true;
                     break;
                } else if (val > fval) {
                    Log.debug(TAG_LOG, "Current version isn't old");
                    break;
                }
                if (vpos < version.length()) {
                    version = version.substring(vpos + 1);
                } else {
                    vpos = -1;
                }
                if (vfpos < fversion.length()) {
                    fversion = fversion.substring(vfpos + 1);
                } else {
                    vfpos = -1;
                }
            }
        } while (vpos > 0 && vfpos > 0);

        return possibleUpdate;
    }

    private String getValueTag(String string, String tag) {
        int index = string.indexOf(tag);
        if (index < 0) {
            return null;
        }
        String tmp = string.substring(index);
        int end = tmp.indexOf("\n");
        if (end < 0) {
            end = tmp.indexOf("\r");
            if (end < 0) {
                return null;
            }
        }

        String value = tmp.substring(tag.length(), end);
        return value;
    }

    private boolean isTimeToRefresh() {
        long now = System.currentTimeMillis();
        boolean refresh = false;

        long lastCheck = config.getLastCheck();
        Log.info(TAG_LOG, "isTimeToRefresh - Now Date is: " + new Date(now) +
                 " Last Check Date was " + new Date(lastCheck));
        if (((now - lastCheck) >= config.getCheckInterval())) {
            Log.info(TAG_LOG, "isTimeToRefresh - Update info need to be refreshed. " +
                     "Last Check was " + new Date(lastCheck));
            refresh = true;
            config.save();
        }
        return refresh;
    }

    private void checkUpdateFromServer() {

        if (isTimeToRefresh()) {

            String url = getUpdaterUrl();
            Log.info(TAG_LOG, "checkUpdateFromServer - update url: " + url);

            TransportAgent ta;
            if (userTA != null) {
                ta = userTA;
            } else {
                ta = new HttpTransportAgent(url, false, false);
            }

            try {
                String updateProperties = ta.sendMessage("");
                String version = getValueTag(updateProperties, VERSION_TAG);
                if (version != null) {
                    if (!version.equals(config.getAvailableVersion())) {
                        // There is a new version on the server, even if the
                        // user decided to skip the current version, we must
                        // inform about the new one. Superseed user decision
                        // in this case and change the config
                        config.setSkip(false);
                    }
                    config.setAvailableVersion(version);
                }
                String type = getValueTag(updateProperties, TYPE_TAG);
                if (type != null) {
                    config.setType(type);
                }
                String downloadUrl = getValueTag(updateProperties, URL_TAG);
                if (downloadUrl != null) {
                    config.setDownloadUrl(downloadUrl);
                }
                String activationDate = getValueTag(updateProperties,
                        ACTIVATION_DATE_TAG);
                if (activationDate != null) {
                    config.setActivationDate(parseDate(activationDate));
                }
                config.setLastCheck(System.currentTimeMillis());
                config.save();

                // Log updater infos
                Log.info(TAG_LOG, "availableVersion: " + config.getAvailableVersion());
                Log.info(TAG_LOG, "updateType: " + config.getType());
                Log.info(TAG_LOG, "updateURL: " + config.getUrl());
                Log.info(TAG_LOG, "activationDate: " + config.getActivationDate());
                Log.info(TAG_LOG, "lastUpdateCheck: " + config.getLastCheck());
            } catch (Throwable t) {
                Log.error(TAG_LOG, "checkUpdateFromServer - " + t.toString());
            }
        } else {
            Log.info(TAG_LOG, "No refresh update info from server needs");
        }
    }

    /**
      * Get time (a {@code long} value that holds the number of milliseconds
      * since midnight GMT, January 1, 1970) from date in "yyyyMMdd" String
      * format
      * @param field Date in "yyyyMMdd" String format
      * @return the Date timestamp
     */
    private static long parseDate(String field) {
        int day = 0;
        int month = 0;
        int year = 0;

        Calendar date = Calendar.getInstance();

        year = Integer.parseInt(field.substring(0, 4));
        month = Integer.parseInt(field.substring(4, 6));
        day = Integer.parseInt(field.substring(6, 8));

        date.set(Calendar.DAY_OF_MONTH, day);
        date.set(Calendar.MONTH, month - 1);
        date.set(Calendar.YEAR, year);
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        return date.getTime().getTime();
    }

    private String getUpdaterUrl() {
        StringBuffer urlParams = new StringBuffer();

        // Set the url path and the component
        urlParams.append(requestUri);
        
        // Append component param
        urlParams.append("&" + UPDATER_COMPONENT_PARAM + "=" + component);

        // Append version param
        urlParams.append("&" + UPDATER_VERSION_PARAM + "=" + currentVersion);

        // Append os param if needed
        if(!StringUtil.isNullOrEmpty(os)) {
            urlParams.append("&" + UPDATER_OS_PARAM + "=" + os);
        }
        // Append model param if needed
        if(!StringUtil.isNullOrEmpty(model)) {
            urlParams.append("&" + UPDATER_MODEL_PARAM + "=" + model);
        }
        // Append manufacturer param if needed
        if(!StringUtil.isNullOrEmpty(manufacturer)) {
            urlParams.append("&" + UPDATER_MANUFACTURER_PARAM + "=" + manufacturer);
        }
        // Append appinfo param if needed
        if(!StringUtil.isNullOrEmpty(appinfo)) {
            urlParams.append("&" + UPDATER_APPINFO_PARAM + "=" + appinfo);
        }
        // Append carrier param if needed
        if(!StringUtil.isNullOrEmpty(carrier)) {
            urlParams.append("&" + UPDATER_CARRIER_PARAM + "=" + carrier);
        }
        // Append format param if needed
        if(!StringUtil.isNullOrEmpty(UPDATER_PROPERTIES_FORMAT)) {
            urlParams.append("&" + UPDATER_FORMAT_PARAM + "=" + UPDATER_PROPERTIES_FORMAT);
        }
        return config.getUrl() + urlParams.toString();
    }

    private boolean updateIsReportable() {
        if (config.getSkip()) {
            return false;
        }
        long now = System.currentTimeMillis();
        Log.info(TAG_LOG, "Now: " + new Date(now));
        Date next = new Date(config.getLastReminder() + config.getReminderInterval());
        Log.info(TAG_LOG, "Next update remind: " + next);
        if ((config.getLastReminder() + config.getReminderInterval()) > now) {
            return false;
        }
        return true;
    }

    public void setLastReminder(long time) {
        config.setLastReminder(time);
        config.save();
    }

    public void setSkip() {
        config.setSkip(true);
        config.save();
    }

    public boolean isUpdateAvailable() {
        try {
            String availableVersion = config.getAvailableVersion();
            return (availableVersion != null && isNewVersionAvailable());
        } catch (Exception e) {
            Log.error(TAG_LOG, "Cannot detect if a new version is available", e);
            return false;
        }
    }

    /**
     * @param uri the requestUri to set (null will reset to default)
     */
    public void setRequestUri(String uri) {
        if (uri != null) {
            this.requestUri = uri;
        } else {
            this.requestUri = DEFAULT_UPDATER_REQUEST_URI;
        }
    }

}
