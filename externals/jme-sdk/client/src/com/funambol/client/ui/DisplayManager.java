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

package com.funambol.client.ui;

import com.funambol.client.controller.DialogOption;

/**
 * Interface to manage the display of application screens and alert dialogs.
 * To be implemented on cient side. The calls to this class instance are usually
 * made by the DialogController Class.
 */
public interface DisplayManager {

    /** Refresh direction alert dialog id*/
    public static final int REFRESH_DIRECTION_DIALOG_ID = 1;
    /** Refresh type alert dialog id*/
    public static final int REFRESH_TYPE_DIALOG_ID = 2;
    /** first sync alert dialog id*/
    public static final int FIRST_SYNC_DIALOG_ID = 3;
    /** wi-fi not available alert dialog id*/
    public static final int NO_WIFI_AVAILABLE_ID = 4;


    /**
     * int value related to the infinitive time to wait before dismissing a
     * screen or a dialog
     */
    public static final long NO_LIMIT = -1;

    /**
     * Hide a screen pulling it to the background
     * @param screen The screen to be hidden
     */
    public void hideScreen(Screen screen) throws Exception;

    /**
     * Shows a screen putting it in foreground
     * @param screen the Screen to be shown
     * @param screenId the screen id related to the Screen to be shown
     * @throws Exception if an error occurred
     */
    public void showScreen(Screen screen, int screenId) throws Exception;

    /**
     * Shows a screen putting it in foreground
     * @param screen the Screen to be shown
     * @param screenId the screen id related to the Screen to be shown
     * @param donotwait if true the screen must be shown immediately
     * @throws Exception if an error occurred
     */
    public void showScreen(Screen screen, int screenId, boolean donotwait) throws Exception;
    
    /**
     * Helper function to prompt the user for a yes/no answer
     * @param question the question to be displayed
     * @param defaultyes the default otpion
     * @param timeToWait time to wait before dismissing the dialog in milliseconds
     */
    public void askYesNoQuestion(Screen screen, String question, Runnable yesAction,
                                 Runnable noAction, long timeToWait);

    /**
     * Helper function to prompt the user for an accept/deny answer
     * Helper function to prompt the user for a yes/no answer
     * @param question the question to be displayed
     * @param defaultyes the default otpion
     * @param timeToWait time to wait before dismissing the dialog in milliseconds
     * @return True on accept
     */
    public boolean askAcceptDenyQuestion(String question, boolean defaultyes, long timeToWait);

    /**
     * Shows a progress dialog with the given text prompt
     * @param screen the alert dialog owner Screen
     * @param prompt the message to be prompted
     * @return the id associated to the given dialog (use it when calling
     *         dismissProgressDialog)
     */
    public int showProgressDialog(Screen screen, String prompt);

    /**
     * Dismisses a progress dialog given its id
     * @param screen the alert dialog owner Screen
     * @param id the dialog id related to the dialog to be dismissed
     */
    public void dismissProgressDialog(Screen screen, int id);

    /**
     * Dismiss a previously shown selection dialog given its id
     * @param id the int id of the dialog to be dismissed
     */
    public void dismissSelectionDialog(int id);

    /**
     * Record the action to be executed after dismissing the alert
     * @param id the int id of the dialog to be dismissed
     * @param dismissAction the Runnable that represents what to do after the
     * alert is dismissed
     */
    public void addPostDismissSelectionDialogAction(int id, Runnable dismissAction);

    /**
     * Remove the action to be executed after dismissing a given alert
     * @param id the int id of the dialog for wich the action is pending
     */
    public void removePostDismissSelectionDialogAction(int id);

    /**
     * Prompt a message to continue or cancel some pending process
     * @param message the message to be prompted
     * @return boolean true if the user selects to continue, false otherwise
     */
    public boolean promptNext(String message);

    /**
     * Prompt a message to the user
     * @param screen the screen where to prompt the message
     * @param message the String formatted message to display
     */
    public void showMessage(Screen screen, String message);

    /**
     * Prompt a message to the user for a given amount of time
     * @param screen the screen where to prompt the message
     * @param message the String formatted message to display
     * @param delay the message delay
     */
    public void showMessage(Screen screen, String message, int delay);

    /**
     * Prompt a selection of different options to the user
     * @param screen the alert dialog owner Screen
     * @param question the message that describe the selection
     * @param options An array of DialogOption objects
     * @param defaultValue The default value for this selection
     * @param dialogId the dialog id related to the type of scelection dialog
     * to be prompted
     */
    public void promptSelection(Screen screen,
            String question,
            DialogOption[] options,
            int defaultValue,
            int dialogId);

    /**
     * Put the application in foreground (Active satus)
     */
    public void toForeground();

    /**
     * Put the application in background (unactive state)
     */
    public void toBackground();

    /**
     * Load the browser to the given url
     * To be implemented.
     * @param url the url to be set on the browser
     */
    public void loadBrowser(String url);

}
