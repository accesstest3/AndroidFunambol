/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2008 Funambol, Inc.
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

package com.funambol.client.customization;

import java.util.Hashtable;
import java.util.Enumeration;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.controller.SignupHandler;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.client.ui.Bitmap;

import com.funambol.util.Log;

public abstract class Customization {

    // Settings customization
    protected final String   SERVER_URI              = "http://my.funambol.com/sync";
    protected final String   USERNAME                = "";
    protected final String   PASSWORD                = "";

    // About customization
    protected final String   ABOUT_COMPANY_NAME      = "Funambol, Inc.";
    protected final String   ABOUT_COPYRIGHT_DEFAULT = "Copyright " + (char)169 + " 2009 - 2011";
    protected final String   ABOUT_SITE_DEFAULT      = "www.funambol.com";
    
    // The application preferred font
    protected final String   PREFERRED_FONT          = "Verdana";
    protected final boolean  USE_DEFAULT_FONT        = true;
    
    // Events sync range customization
    protected final boolean  EVENT_RANGE_LIMITED     = true;
    protected final int      DEFAULT_RANGE_PAST      = 1;
    protected final int      DEFAULT_RANGE_FUTURE    = 6;

    // Log customization
    protected final boolean  LOG_ENABLED             = true;
    protected final boolean  SEND_LOG_ENABLED        = true;
    protected final boolean  LOG_IN_SETTINGS_SCREEN  = true;
    protected final boolean  LOCK_LOG_LEVEL          = false;
    protected final int      LOCKED_LOG_LEVEL        = Log.INFO;

    // Bandwidth Saver customization
    protected final boolean  BANDWIDTH_SAVER_ENABLED = true;

    protected final boolean  USE_BANDWIDTH_SAVER_CONTACTS = false;
    protected final boolean  USE_BANDWIDTH_SAVER_EVENTS = false;
    protected final boolean  USE_BANDWIDTH_SAVER_MEDIA = true;

    // TODO FIXME MARCO
    protected final String   LOG_TITLE               = "Funambol" + " Log";

    protected final boolean  SHOW_SYNC_ICON_ON_SELECTION = true;

    protected final String   LOG_FILE_NAME           = "synclog.txt";
    protected final String   LOG_PATH                = "file:///store/home/user";

    // Updater customization
    protected final boolean  CHECK_FOR_UPDATE        = false; // Used for MailTrust update
    private final boolean    ENABLE_UPDATER_MANAGER  = false; // Used for Funambol update
    protected final long     CHECK_UPDATE_INTERVAL   = (long)24*(long)60*(long)60*(long)1000; // 1 day in milliseconds
    protected final long     REMINDER_UPDATE_INTERVAL= (long)2 *(long)60*(long)60*(long)1000; // 2 hours in seconds

    // Max number of items per SyncML message sent to server during slow sync.
    // This is used to limit the number of items and to avoid client's timeouts
    // To be decreased with slow server/backend installations
    // Set to -1 to disable the limit
    private final int      MAX_ITEMS_PER_MESSAGE_IN_SLOW_SYNC = 20;

    // Set this parameter if a fix apn is to be used from your application
    private final String   FIXED_APN               = null;

    // Define the schedule choices
    private final int[] POLLING_PIM_INTERVAL_CHOICES = {
        5,
        15,
        30,
        60,
        120,
        240,
        480,
        720,
        1440
    };
    private final int  DEFAULT_POLLING_INTERVAL = POLLING_PIM_INTERVAL_CHOICES[1];

    // Specifies if the port must be stripped out of the sync url
    private final boolean  STRIP_PORT_FROM_SYNC_ADDRESS = false;

    // Specifies if the sync url is editable in both the login and setting screens
    private final boolean  SYNC_URI_EDITABLE            = true;

    // Specifies if the user account settigs are editable in both the login and setting screens
    private final boolean  USER_ACCOUNT_EDITABLE        = true;

    // Specifies if the attendees filtering is enabled or disabled
    private final boolean  INVITES_FILTERING            = false;

    // Specifies if slow sync needs to be confirmed by the user
    private final boolean  CONFIRM_SLOW_SYNC            = false;

    // Specifies if the user must be warned on large amount of deletes
    private final boolean  WARN_ON_DELETES              = false;

    // Specifies if photos must be synchronized in contact sync
    private final boolean  SYNC_CONTACT_PHOTO           = true;

    // Specifies if the C2S COP must be enabled in the client
    private final boolean ENABLE_C2S_PUSH               = false;

    // Show non working sources in the home screen
    private final boolean SHOW_NON_WORKING_SOURCES      = false;

    // Sync items type
    private final String DEFAULT_CALENDAR_TYPE = "text/x-vcalendar";
    private final String DEFAULT_TASK_TYPE     = "text/x-vcalendar";
    private final String DEFAULT_CONTACT_TYPE  = "text/x-vcard";

    // Shall we be smarter and check if a memory card is present? In such a
    // case this is where the BB stores pictures by default....
     private final String   DEFAULT_FILE_BROWSER_DIR    = "file:///store/home/user/";
    
    private final boolean  SYNC_ALL_ON_MAIN_SCREEN     = false;

    private final boolean  SYNC_ALL_ACTS_AS_CANCEL_SYNC = true;

    private final boolean  SOURCE_URI_VISIBLE          = true;
    private final boolean  SYNC_DIRECTION_VISIBLE      = true;
    private final int      DEFAULT_C2S_PUSH_DELAY      = 60 * 1000;
    private final int      DEFAULT_SYNC_MODE           = Configuration.SYNC_MODE_MANUAL;

    private final boolean  SYNC_MODE_IN_SETTINGS_SCREEN = true;

    private final int[]    AVAILABLE_SYNC_MODES = {Configuration.SYNC_MODE_PUSH,
                                                   Configuration.SYNC_MODE_MANUAL,
                                                   Configuration.SYNC_MODE_SCHEDULED};

    private final boolean  C2S_PUSH_IN_SETTINGS_SCREEN = true;
    
    private final boolean  DEFAULT_BLOCK_INVITES       = false;
    private final boolean  BLOCK_INVITES_FIELD_VISIBLE = false;
    
    private final boolean  SYNC_COMPANY_DIRECTORY      = false;
    private final boolean  SHOW_TITLE_WITH_VERSION     = false;
    private final boolean  ROLLOVER_ICON_ENABLED       = false;
    private final boolean  GOTO_MENU_ENABLED           = true;
    private final boolean  ENABLE_FILE_LOGGING         = true;
    private final boolean  SHOW_ABOUT_LICENCE          = true;
    private final boolean  SHOW_POWERED_BY             = false;
    private final boolean  REPORT_STATUS_IN_SYNC_ALL   = false;
    private final boolean  ENABLE_REFRESH_COMMAND      = true;

    private final boolean  DEFAULT_ENCODE              = true;
    private final String   DEFAULT_ENCRYPTION_TYPE     = "b64";

    private final String  DEFAULT_AUTH_TYPE = SyncML.AUTH_TYPE_BASIC;

    private final String HTTP_UPLOAD_PREFIX = "sapi/media";

    private final boolean CONTACTS_IMPORT_ENABLED       = true;

    // Mobile Sign Up customizations
    private final boolean MOBILE_SIGNUP_ENABLED         = true;
    private final int     DEFAULT_MSU_VALIDATION_MODE   = SignupHandler.VALIDATION_MODE_CAPTCHA;
    private final boolean SHOW_SIGNUP_SUCCEEDED_MESSAGE = true;
    private final boolean ADD_SHOW_PASSWORD_FIELD       = true;
    private final String  TERMS_AND_CONDITIONS_URL      = "http://my.funambol.com/ui/mobile/jsp/toc.jsp";
    private final String  PRIVACY_POLICY_URL            = "http://my.funambol.com/ui/mobile/jsp/pp.jsp";
    private final boolean PREFILL_PHONE_NUMBER          = false;


    //// ------------------- END OF CUSTOMIZABLE FIELDS --------------------////

    protected Hashtable sourcesUri          = new Hashtable();
    protected Hashtable activeSources       = new Hashtable();
    protected Hashtable sourcesIcon         = new Hashtable();
    protected Hashtable sourcesDisabledIcon = new Hashtable();
    protected Hashtable sourcesSyncModes    = new Hashtable();
    protected Hashtable sourcesSyncMode     = new Hashtable();

    public Customization() {
        initSourcesInfo();
    }

    public abstract String getApplicationFullname();

    public abstract String getApplicationTitle();

    public boolean syncAllOnMainScreenRequired() {
        return SYNC_ALL_ON_MAIN_SCREEN;
    }

    public boolean syncAllActsAsCancelSync() {
        return SYNC_ALL_ACTS_AS_CANCEL_SYNC;
    }
    
    public String getFixedApn() {
        return FIXED_APN;
    }

    public boolean stripPortFromSyncAddress() {
        return STRIP_PORT_FROM_SYNC_ADDRESS;
    }

    public boolean syncUriEditable() {
        return SYNC_URI_EDITABLE;
    }

    public boolean userAccountEditable() {
        return USER_ACCOUNT_EDITABLE;
    }

    public boolean isRangePastVisible() {
        return !EVENT_RANGE_LIMITED;
    }
    
    public boolean isRangeFutureVisible() {
        return !EVENT_RANGE_LIMITED;
    }

    public boolean syncInRange() {
        return EVENT_RANGE_LIMITED;
    }
    
    public boolean isBlockInvitesFieldVisible() {
        return BLOCK_INVITES_FIELD_VISIBLE;
    }
    
    public boolean isSourceUriVisible() {
        return SOURCE_URI_VISIBLE;
    }

    public boolean isSyncDirectionVisible() {
        return SYNC_DIRECTION_VISIBLE;
    }

    public boolean invitesFiltering() {
        return INVITES_FILTERING;
    }

    public boolean isSourceActive(int id) {
        Boolean active = (Boolean)activeSources.get(new Integer(id));
        if (active != null) {
            return active.booleanValue();
        } else {
            return false;
        }
    }

    public boolean isSourceEnabledByDefault(int id) {
        Integer syncMode = (Integer)sourcesSyncMode.get(new Integer(id));
        return (syncMode != null) && (syncMode.intValue() != SyncML.ALERT_CODE_NONE);
    }

    public boolean confirmSlowSync() {
        return CONFIRM_SLOW_SYNC;
    }

    public boolean warnOnDeletes() {
        return WARN_ON_DELETES;
    }

    public boolean syncContactPhoto() {
        return SYNC_CONTACT_PHOTO;
    }

    public String getContactType() {
        return DEFAULT_CONTACT_TYPE;
    }

    public String getCalendarType() {
        return DEFAULT_CALENDAR_TYPE;
    }
    
    public String getTaskType() {
        return DEFAULT_TASK_TYPE;
    }

    public boolean checkForUpdates() {
        return CHECK_FOR_UPDATE;
    }

    public boolean enableUpdaterManager() {
        return ENABLE_UPDATER_MANAGER;
    }

    public boolean syncCompanyDirectory() {
        return SYNC_COMPANY_DIRECTORY;
    }

    public boolean showTitleWithVersion() {
        return SHOW_TITLE_WITH_VERSION;
    }

    public boolean rolloverIcon() {
        return ROLLOVER_ICON_ENABLED;
    }

    public String getDefaultSourceUri(int id) {
        return (String)sourcesUri.get(new Integer(id));
    }

    public int[] getDefaultSourceSyncModes(int id) {
        return (int[])sourcesSyncModes.get(new Integer(id));
    }

    public boolean gotoMenuEnabled() {
        return GOTO_MENU_ENABLED;
    }

    public boolean logEnabled() {
        return LOG_ENABLED;
    }

    public boolean lockLogLevel(){
        return LOCK_LOG_LEVEL;
    }
    
    public int getLockedLogLevel(){
        return LOCKED_LOG_LEVEL;
    }

    public boolean isLogEnabledInSettingsScreen() {
        return LOG_IN_SETTINGS_SCREEN;
    }

    public boolean isBandwidthSaverEnabled() {
        return BANDWIDTH_SAVER_ENABLED;
    }

    public boolean useBandwidthSaverContacts(){
        return USE_BANDWIDTH_SAVER_CONTACTS;
    }

    public boolean useBandwidthSaverEvents(){
        return USE_BANDWIDTH_SAVER_EVENTS;
    }

    public boolean useBandwidthSaverMedia(){
        return USE_BANDWIDTH_SAVER_MEDIA;
    }
    
    public boolean sendLogEnabled() {
        return SEND_LOG_ENABLED;
    }

    public String getServerUriDefault() {
        return SERVER_URI;
    }

    public String getUserDefault() {
        return USERNAME;
    }

    public String getPasswordDefault() {
        return PASSWORD;
    }

    public long getCheckUpdtIntervalDefault(){
        return CHECK_UPDATE_INTERVAL;
    }

    public long getReminderUpdtIntervalDefault(){
        return REMINDER_UPDATE_INTERVAL;
    }

    public String getLogAppTitle() {
        return LOG_TITLE;
    }

    public boolean enableFileLogging() {
        return ENABLE_FILE_LOGGING;
    }

    public String getLogFileDirectory() {
        return LOG_PATH;
    }

    public String getLogFileName() {
        return LOG_FILE_NAME;
    }

    public boolean useDefaultFont() {
        return USE_DEFAULT_FONT;
    }

    public String getPreferredFont() {
        return PREFERRED_FONT;
    }

    public String getCompanyName() {
        return ABOUT_COMPANY_NAME;
    }
    
    public String getAboutCopyright() {
        return ABOUT_COPYRIGHT_DEFAULT;
    }

    public String getAboutSite() {
        return ABOUT_SITE_DEFAULT;
    }

    public boolean showAboutLicence() {
        return SHOW_ABOUT_LICENCE;
    }

    public boolean showPoweredBy() {
        return SHOW_POWERED_BY;
    }


    public boolean reportStatusInSyncAll() {
        return REPORT_STATUS_IN_SYNC_ALL;
    }

    public boolean enableRefreshCommand() {
        return ENABLE_REFRESH_COMMAND;
    }

    public String getFileBrowserDir() {
        return DEFAULT_FILE_BROWSER_DIR;
    }

    public int getDefaultPollingInterval() {
        return DEFAULT_POLLING_INTERVAL;
    }

    public String getSourceUri(int id) {
        return (String)sourcesUri.get(new Integer(id));
    }

    public boolean isC2SPushEnabled() {
        return ENABLE_C2S_PUSH;
    }
    
    public int getC2SPushDelay() {
        return DEFAULT_C2S_PUSH_DELAY;
    }

    public int getDefaultSyncMode() {
        return DEFAULT_SYNC_MODE;
    }
    
    public int[] getPollingPimIntervalChoices() {
        return POLLING_PIM_INTERVAL_CHOICES;
    }

    public boolean showSyncModeInSettingsScreen() {
        return SYNC_MODE_IN_SETTINGS_SCREEN;
    }

    public boolean showC2SPushInSettingsScreen() {
        return C2S_PUSH_IN_SETTINGS_SCREEN;
    }

    public int[] getAvailableSyncModes() {
        return AVAILABLE_SYNC_MODES;
    }
    
    public int getMaxItemsPerMessageInSlowSync() {
        return MAX_ITEMS_PER_MESSAGE_IN_SLOW_SYNC;
    }
    
    public boolean getDefaultEncode() {
        return DEFAULT_ENCODE;
    }

    public String getDefaultEncryptionType() {
        return DEFAULT_ENCRYPTION_TYPE;
    }

    public int getDefaultSourceSyncMode(int id) {
        return ((Integer)sourcesSyncMode.get(new Integer(id))).intValue();
    }

    public int getDefaultRangePast() {
        return DEFAULT_RANGE_PAST;
    }

    public int getDefaultRangeFuture() {
        return DEFAULT_RANGE_FUTURE;
    }

    public boolean getDefaultBlockInvites() {
        return DEFAULT_BLOCK_INVITES;
    }

    public boolean showSyncIconOnSelection() {
        return SHOW_SYNC_ICON_ON_SELECTION;
    }

    public boolean showNonWorkingSources() {
        return SHOW_NON_WORKING_SOURCES;
    }

    public String getDefaultAuthType() {
        return DEFAULT_AUTH_TYPE;
    }

    public boolean getContactsImportEnabled() {
        return CONTACTS_IMPORT_ENABLED;
    }

    public boolean getMobileSignupEnabled() {
        return MOBILE_SIGNUP_ENABLED;
    }

    public int getDefaultMSUValidationMode() {
        return DEFAULT_MSU_VALIDATION_MODE;
    }
    
    public boolean getShowSignupSuccededMessage() {
        return SHOW_SIGNUP_SUCCEEDED_MESSAGE;
    }
    
    public boolean getAddShowPasswordField() {
        return ADD_SHOW_PASSWORD_FIELD;
    }

    public String getTermsAndConditionsUrl() {
        return TERMS_AND_CONDITIONS_URL;
    }

    public String getPrivacyPolicyUrl() {
        return PRIVACY_POLICY_URL;
    }

    public boolean getPrefillPhoneNumber() {
        return PREFILL_PHONE_NUMBER;
    }
    
    /**
     * Returns an Enumeration of Integer where each item represents the id of an
     * available source. The source is not ready yet to be used, but it is
     * available in this client version. To check if a source is really working
     * and enabled, use the corresponding AppSyncSource methods.
     *
     * @return an enumeration of Integer
     */
    public Enumeration getAvailableSources() {
        Enumeration keys = activeSources.keys();
        return keys;
    }

    public Bitmap getSourceIcon(int id) {
        Bitmap icon = (Bitmap)sourcesIcon.get(new Integer(id));
        return icon;
    }

    public Bitmap getSourceDisabledIcon(int id) {
        Bitmap icon = (Bitmap)sourcesDisabledIcon.get(new Integer(id));
        return icon;
    }

    public String getPoweredBy() {
        return null;
    }

    public Bitmap getPoweredByLogo() {
        return null;
    }

    // Note that this is hardcoded here because it cannot be translated
    public String getLicense() {
        StringBuffer license = new StringBuffer();
        
        license.append("This program is provided AS IS, without warranty licensed under AGPLV3. The ")
               .append("Program is free software; you can redistribute it and/or modify it under the ")
               .append("terms of the GNU Affero General Public License version 3 as published by the Free ")
               .append("Software Foundation including the additional permission set forth source code ")
               .append("file header.\n\n")
               .append("The interactive user interfaces in modified source and object code versions of ")
               .append("this program must display Appropriate Legal Notices, as required under Section 5 ")
               .append("of the GNU Affero General Public License version 3.\n\n ")
               .append("In accordance with Section 7(b) of the GNU Affero General Public License version 3, ")
               .append("these Appropriate Legal Notices must retain the display of the \"Powered by ")
               .append("Funambol\" logo. If the display of the logo is not reasonably feasible for ")
               .append("technical reasons, the Appropriate Legal Notices must display the words \"Powered ")
               .append("by Funambol\". Funambol is a trademark of Funambol, Inc.");
        return license.toString();
    }

    public String getHttpUploadPrefix() {
        return HTTP_UPLOAD_PREFIX;
    }

    public abstract int[] getSourcesOrder();
    public abstract Bitmap getImageLogo();
    public abstract Bitmap getSyncAllIcon();
    public abstract Bitmap getSyncAllBackground();
    public abstract Bitmap getSyncAllHighlightedBackground();
    public abstract Bitmap getButtonBackground();
    public abstract Bitmap getButtonHighlightedBackground();
    public abstract Bitmap getOkIcon();
    public abstract Bitmap getErrorIcon();
    public abstract Bitmap getCancelledIcon();
    public abstract Bitmap getWarningIcon();
    public abstract Bitmap getStatusSelectedIcon();
    public abstract Bitmap getFolderIcon();
    public abstract Bitmap[] getStatusIconsForAnimation();
    public abstract Bitmap[] getStatusHugeIconsForAnimation();
    public abstract String getVersion();
    public abstract String getSupportEmailAddress();

    protected abstract void initSourcesInfo();
}

