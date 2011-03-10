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

package com.funambol.util;

import javax.microedition.midlet.MIDlet;

/**
 * This class provides a mechanism to read attributes stored in the jad file.
 * The BB implementation uses the RIM apis so it can be used by both BB native
 * applications and MIDlets.
 * The class provides a list of basic properties, but it can be used to get any
 * custom property, not just the ones listed as constants.
 */
public class AppProperties {
    public static final String MIDLET_VERSION           = "MIDlet-Version";
    public static final String SH_FORMAT                = "shformat";
    public static final String LIVE_SERVER_ATTR         = "AdsLiveServer";
    public static final String LOG_APPENDER             = "LogAppender";
    public static final String LOG_SOCKET_URL           = "LogSocketUrl";
    public static final String LOG_FILE_DIR_URL         = "LogFileDirUrl";
    public static final String EMBEDDED_ADS             = "EmbeddedAds";
    public static final String REL                      = "rel";
    public static final String CTP                      = "CTP";
    public static final String CTP_SERVER               = "CTPServer";
    public static final String MIDLET_PUSH_1            = "MIDlet-Push-1";
    public static final String SAN_PORT                 = "SAN-Port";
    public static final String MIDLET_NAME              = "MIDlet-Name";
    public static final String MIDLET_VENDOR            = "MIDlet-Vendor";
    public static final String LOGLEVEL_ATTR            = "LogLevel";
    public static final String ENABLE_SCHEDULER_ATTR    = "EnableScheduler";
    public static final String ENABLE_SMS_LISTENER_ATTR = "EnableSmsListener";
    public static final String USER_ATTR                = "User";
    public static final String URL_ATTR                 = "Url";
    public static final String PASSWORD_ATTR            = "Password";
    public static final String ADDRESS_ATTR             = "Address";
    public static final String NAME_ATTR                = "Name";
    public static final String REMOTEURI_ATTR           = "RemoteUri";
    public static final String DEVID_ATTR               = "Device-ID";
    public static final String PIM_REMOTEURI_ATTR       = "vCardRemoteURI";
    public static final String POLL_INTERVAL_ATTR       = "PollInterval";
    public static final String ENABLE_COMPRESS          = "Compress";
    public static final String ENABLE_DATE_FILTER       = "DateFilter";
    public static final String APP_INFO_ATTR            = "AppInfo";
    public static final String ENABLE_DELETE_PROPAGATION = "EnableDeletePropagation";
    public static final String AUTOUPDATE_URL           = "AutoUpdateUrl";
    public static final String AUTOUPDATE_CHECK_INTERVAL= "AutoUpdateCheckInterval";
    public static final String AUTOUPDATE_REMINDER_INTERVAL= "AutoUpdateReminderInterval";
    public static final String SINGLE_BUNDLE            = "singleBundle";

    
    private MIDlet midlet;

    /**
     * Construct an AppProperties
     *
     * @param app is the application. In this implementation this must be the
     * MIDlet object
     */
    public AppProperties(Object app) {
        this.midlet =  (MIDlet)app;
    }
    
    public String get(String property) {
        return midlet.getAppProperty(property);
    }

    /**
     *  return true if body field in compose message screen should have
     *  empty title. Currently is true only for BB
     */
    public boolean isNoCaptionInBodyField() {
        return false;
    }
    
    
}
