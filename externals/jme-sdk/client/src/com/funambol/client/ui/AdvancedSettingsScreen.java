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

package com.funambol.client.ui;

/**
 * Advanced Settings Screen interface. Useful to realize the view on the high
 * level client. The complete view is composed by two sections:
 * <li>
 * Logs section: contains the description, a selector view with log levels
 * (can be a spinner, a checkbox or another multiple choice item) and two
 * commands to send log or view log (they can be buttons or just menu commands).
 * </li>
 * <li>
 * Reset section: contains a description of the reset feature and a command to
 * start the reset action (can be a button or a menu command or more in general
 * a selection item)
 * </li>
 *
 * <p>
 * The log section can be completely hidden or shown depending by the
 * customization implementation.
 * The send log and the reset commands can be dinamically enabled or disabled if
 * another sync or connection is in progress. The send log and view log commands
 * can also be hidden separately.
 * </p>
 */
public interface AdvancedSettingsScreen extends Screen {

    /**
     * Hide the entire log section. The section is removed by the settings
     * screen during the initialization depending by customization
     * implementation. No parameters are passed to this method for this reason
     */
    public void hideLogsSection();

    /**
     * Hide the send log button. It can be requested by the customization
     * implementation.
     */
    public void hideSendLogCommand();

    /**
     * Hide the view log button. It can be requested by the customization
     * implementation.
     */
    public void hideViewLogCommand();

    /**
     * Hide the import contacts section
     */
    public void hideImportContactsSection();


    /**
     * Disable the item related to the reset command. Useful not to have
     * concurrent sync or connections.
     * @param hidden true if hidden, false otherwise
     */
    public void enableResetCommand(boolean enable);

    /**
     * Disable the item related to the Send Log command. Useful not to have
     * concurrent sync or connections.
     * @param hidden true if hidden, false otherwise
     */
    public void enableSendLogCommand(boolean enable);

    /**
     * Accessor method to set the log level on the view selector item
     * @param logLevel the log level to be set
     */
    public void setViewLogLevel(int logLevel);

    /**
     * Accessor method to get the log level set on the view selector item
     * @param logLevel the log level to be set
     * @return int the log level set by the user on the view
     */
    public int getViewLogLevel();

    /**
     * Accessor method to enable/disable Bandwidth Saver
     * @param bandwidthSaverChecked true to enable, false to disable Bandwidth Saver
     */
    public void setBandwidthSaver(boolean enable);

     /**
     * Accessor method to get value fore Bandwith Saver status
     * @return boolean status enabled/disabled for Bandwidth Saver
     */
    public boolean getBandwidthSaver();


     /**
     * Hide the entire Bandwidth Saver section. The section is removed by the settings
     * screen during the initialization depending by customization
     * implementation. No parameters are passed to this method for this reason
     */
    public void hideBandwidthSaverSection();
    
    /**
     * Hide the entire reset section. The section is removed by the settings
     * screen during the initialization depending by customization
     * implementation. No parameters are passed to this method for this reason
     */
    public void hideResetSection();
}

