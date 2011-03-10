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

/**
 *
 */
public interface AndroidUserCommands {
    
    /**
     * Removes ALL accounts from the device
     * @example RemoveAccount()
     */
    public static final String REMOVE_ACCOUNT_COMMAND         = "RemoveAccount";

    /**
     * Wait for a given amount of seconds that an activity is displayed on the
     * screen
     * @param activity the name of an activity to be waited for
     * @param integer amount of seconds to wait before the activity is displayed
     * @example WaitForActivity(HomeScreen, 100) the suite waits 100 seconds for
     * the home screen activity to be displayed
     */
    public static final String WAIT_FOR_ACTIVITY_COMMAND      = "WaitForActivity";

    /**
     * Perform a "Cancel Sync" Button pressed event
     * @example CancelSync()
     */
    public static final String CANCEL_SYNC_COMMAND            = "CancelSync";

    /**
     * Check if a sync is pending for a given source.
     * @param sourceName is the source name to be checked
     * @param pending is the optional boolean value for the pending sync.
     * Default set to true
     * @example CheckSyncPending(Pictures) - CheckSyncPending(Pictures, true)
     * checks if a calendar sync is pending. Fails if the sync is not actually
     * pending because the tester put false for the given pending value
     */
    public static final String CHECK_SYNC_PENDING_COMMAND     = "CheckSyncPending";

    /**
     * Change the configuration in order to set the autosync device's setting to
     * the given boolean value.
     * @param enable use the boolean true to set the autosync enabled, false
     * otherwise
     * @example SetAutoSyncEnabled(false) disable the autosync device's setting
     */
    public static final String SET_AUTO_SYNC_COMMAND          = "SetAutoSyncEnabled";

    /**
     * Change the configuration in order to set the autosync device's setting for
     * a particular source to true.
     * @param sourceName the source for which the sutosync setting must be
     * enabled on the device
     * @param enable use the boolean true to enable the autosync for the given
     * source, false otherwise
     * @example SetSourceAutoSyncEnabled(Calendar, true) set the autosync
     * device's parameter to true for the given source
     */
    public static final String SET_SOURCE_AUTO_SYNC_COMMAND   = "SetSourceAutoSyncEnabled";

    /**
     * Check if the device's autosync setting configuration for a particular
     * source is enabled or disabled
     * @param sourceName the source for which the autosync setting must be checked
     * @param enable use the boolean true to check if the sutosync parameter is
     * enabled on the device for the given source, false otherwise
     * @example CheckSourceAutoSyncEnabled("Contacts", false) check the autosync
     * device's parameter. If it is disabled the test is ok, fails otherwise
     */
    public static final String CHECK_SOURCE_AUTO_SYNC_COMMAND = "CheckSourceAutoSyncEnabled";

    /**
     * Check if the last message displayed into an alert is the given one
     * @param alertMessage is the message alert to be checked as last displayed
     * @example CheckLastAlertMessage("This is the message to check") fails if
     * the last message alert is not equal to the given string
     */
    public static final String CHECK_LAST_ALERT_MESSAGE       = "CheckLastAlertMessage";

    /**
     * Cancel the sync on a given sync phase given 2 phase after having
     * @param phase can be set to "Receiving" or "Sending" (String formatted)
     * @param itemNumber the item number tostop the sync if received or sent
     * @example CancelSyncAfterPhase("Receiving", 2) stops the sync after the
     * client has received 2 items
     */
    public static final String CANCEL_SYNC_AFTER_PHASE        = "CancelSyncAfterPhase";

    /**
     * Wait for the given sync phase to be reached
     * @param phase can be set to "Receiving" or "Sending" (String formatted)
     * @param itemNumber the item number tostop the sync if received or sent
     * @param timeout amount of seconds to wait before the sync phase is reached
     * @example WaitForSyncPhase("Sending", 3, 10) waits 10 seconds for the
     * client has sent 3 items
     */
    public static final String WAIT_FOR_SYNC_PHASE            = "WaitForSyncPhase";

    /**
     * Add a given picture to the device store just copying it to the device
     * sync folder
     * @param pictureFileName is the picture file name. It will be added to the
     * default sync directory
     * @example AddPicture(photo.jpg) Add the photo.png picture to the picture
     * sync folder
     */
    public static final String ADD_PICTURE                    = "AddPicture";

    /**
     * Delete a given picture to the device store just copying it to the device
     * sync folder
     * @param pictureFileName is the picture file name. It will be deleted from
     * the default sync directory
     * @example DeletePicture(photo.jpg) Delete the photo.png picture from the
     * picture sync folder if it exists, does anything otherwise
     */
    public static final String DELETE_PICTURE                 = "DeletePicture";
    
}
