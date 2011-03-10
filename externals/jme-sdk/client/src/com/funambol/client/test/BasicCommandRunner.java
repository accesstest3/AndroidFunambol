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

package com.funambol.client.test;

import com.funambol.util.StringUtil;
import com.funambol.util.Log;

/**
 * Implementation of the CommandRunner class that define the full commands set
 * available to the tester in order to create automatic test script.
 */
public abstract class BasicCommandRunner extends CommandRunner implements BasicUserCommands {

    private static final String TAG_LOG = "BasicCommandRunner";
    
    // Key events used by KeyPress command
    public static final String DOWN_KEY_NAME  = "KeyDown";
    public static final String UP_KEY_NAME    = "KeyUp";
    public static final String LEFT_KEY_NAME  = "KeyLeft";
    public static final String RIGHT_KEY_NAME = "KeyRight";
    public static final String FIRE_KEY_NAME  = "KeyFire";
    public static final String MENU_KEY_NAME  = "KeyMenu";
    public static final String BACK_KEY_NAME  = "KeyBack";
    public static final String DEL_KEY_NAME   = "KeyDelete";

    public String currentTestName;

    /**
     * Constructor
     * @param robot the BasicRobot object that runs the commands on the given
     * client implementation. This robot should be defined into the high level
     * as it is architecture specific.
     */
    public BasicCommandRunner(BasicRobot robot) {
        super(robot);
    }

    /**
     * Core method of this class. It parses the command line command and
     * arguments to realize the actions defined in the high level provided
     * script
     * @param command the String formatted command to be parsed and given to the
     * robot that is defined to execute it
     * @param pars the command string formatted arguments
     * @return boolean true if the command is valid, false otherwise
     * @throws Throwable if anything goes wrong when the command is run
     */
    public boolean runCommand(String command, String pars) throws Throwable {

        if (WAIT_COMMAND.equals(command)) {
            wait(command, pars);
        } else if (BEGIN_TEST_COMMAND.equals(command)) {
            beginTest(command, pars);
        } else if (END_TEST_COMMAND.equals(command)) {
            endTest(command, pars);
        } else if (KEY_PRESS_COMMAND.equals(command)) {
            keyPress(command, pars);
        } else if (WRITE_STRING_COMMAND.equals(command)) {
            writeString(command, pars);
        } else if (WAIT_FOR_SYNC_TO_COMPLETE_COMMAND.equals(command)) {
            waitForSyncToComplete(command, pars);
        } else if (WAIT_FOR_AUTH_TO_COMPLETE_COMMAND.equals(command)) {
            waitForAuthToComplete(command, pars);
        } else if (CHECK_EXCHANGED_DATA_COMMAND.equals(command)) {
            checkExchangedData(command, pars);
        } else if (CHECK_REQUESTED_SYNC_MODE_COMMAND.equals(command)) {
            checkLastSyncRequestedSyncMode(command, pars);
        } else if (CHECK_ALERTED_SYNC_MODE_COMMAND.equals(command)) {
            checkLastSyncAlertedSyncMode(command, pars);
        } else if (CHECK_REMOTE_URI_COMMAND.equals(command)) {
            checkLastSyncRemoteUri(command, pars);
        } else if (FORCE_SLOW_SYNC_COMMAND.equals(command)) {
            resetSourceAnchor(command, pars);
        } else if (REFRESH_SERVER_COMMAND.equals(command)) {
            refreshServer(command, pars);
        } else if (START_MAIN_APP_COMMAND.equals(command)) {
            startMainApp(command, pars);
        } else if (CHECK_ITEMS_COUNT_COMMAND.equals(command)) {
            checkItemsCount(command, pars);
        } else if (CHECK_ITEMS_COUNT_ON_SERVER_COMMAND.equals(command)) {
            checkItemsCountOnServer(command, pars);
        } else if (INTERRUPT_SYNC_AFTER_PHASE_COMMAND.equals(command)) {
            interruptSyncAfterPhase(command, pars);
        } else {
            return false;
        }
        return true;
    }

    /**
     * Return the name of the test that is executing.
     * @return String the String formatted test name
     */
    protected String getCurrentTestName() {
        return currentTestName;
    }

    /**
     * Accessor method
     * @return BasicRobot the BasicRobot instance reference.
     */
    protected BasicRobot getBasicRobot() {
        return (BasicRobot)robot;
    }

    /**
     * The automatic test common method to start the main application.
     * See implementation for further details.
     * @param command the String formatted command to be executed
     * @param args the command related and String formatted arguments
     * @throws Throwable if anything goes wrong when the application starts.
     */
    protected abstract void startMainApp(String command, String args) throws Throwable;

    /**
     * Uses the SyncMonitor object to wait that for specific sync action and
     * validate it as completed after a given amount of time. Use the BasicRobot
     * to perform the sync action.
     * @param command the String representation of the command
     * @param args the command's related and String formatted arguments.
     * In particular the script commad must contain the sync startup time and
     * the maximum time for the sync to be completed.
     * @throws Throwable if anything went wrong during the sync
     */
    protected void waitForSyncToComplete(String command, String args) throws Throwable {

        String minStart = getParameter(args, 0);
        String maxWait  = getParameter(args, 1);

        checkArgument(minStart, "Missing min start in " + command);
        checkArgument(maxWait, "Missing max wait in " + command);

        checkObject(syncMonitor, "Run StartMainApp before command: " + command);

        int min = Integer.parseInt(minStart)*1000;
        int max = Integer.parseInt(maxWait)*1000;

        getBasicRobot().waitForSyncToComplete(min, max, syncMonitor);
    }

    /**
     * a command that interrupts the current sync action
     * @param command the command related to the cancel sync action
     * @param args the command related and String formatted arguments
     * @throws Throwable if an error occurred
     */
    private void interruptSyncAfterPhase(String command, String args) throws Throwable {

        String phase  = getParameter(args, 0);
        String num    = getParameter(args, 1);
        String reason = getParameter(args, 2);

        checkArgument(phase, "Missing phase name in " + command);
        checkArgument(num, "Missing num in " + command);
        checkArgument(reason, "Missing reason in " + command);

        checkObject(syncMonitor, "Run StartMainApp before command: " + command);

        int n = Integer.parseInt(num);

        getBasicRobot().interruptSyncAfterPhase(phase, n, reason, syncMonitor);
    }

    /**
     * This command is referred to the authentication action to be performed
     * from the client's login screen. Wait that the login operation is
     * correctly performed after a given amount of time. (whatever it means:
     * config sync or simple authentication)
     * @param command the command related to the authentication action
     * @param args the command related and String formatted arguments. In
     * particular the startup action time and the maximum allowed time to
     * complete the operation must be given as parameters to this command by
     * the tester in the input script.
     * @throws Throwable if an error occurred
     */
    private void waitForAuthToComplete(String command, String args) throws Throwable {

        String minStart = getParameter(args, 0);
        String maxWait  = getParameter(args, 1);

        checkArgument(minStart, "Missing min start in " + command);
        checkArgument(maxWait, "Missing max wait in " + command);

        checkObject(authSyncMonitor, "Run StartMainApp before command: " + command);

        int min = Integer.parseInt(minStart)*1000;
        int max = Integer.parseInt(maxWait)*1000;

        getBasicRobot().waitForAuthToComplete(min, max, authSyncMonitor);
    }

    /**
     * Begin test declaration command. Useful to address the current test name
     * @param command the String fomatted command to declare that a test was
     * begun
     * @param args the command String formatted parameter. In particular the
     * tester must provide the test name when declaring the begin of a test
     * @throws Throwable if an error occurred
     */
    private void beginTest(String command, String args) throws Throwable {

        currentTestName = getParameter(args, 0);
        if (currentTestName == null) {
            Log.error(TAG_LOG, "Syntax error in script, missing test name in begin "
                               + BEGIN_TEST_COMMAND);
        } else {
            Log.info(TAG_LOG, "Starting test " + currentTestName);
        }
    }

    /**
     * End test declaration command.
     * @param command the end test related Stirng formatted representation
     * @param args the command's related String arguments. Not required for this
     * command
     * @throws Throwable if an error occurred
     */
    private void endTest(String command, String args) throws Throwable {
        currentTestName = null;
    }

    /**
     * checks the final data exchanged after a sync
     * @param command the check data command related Stirng formatted
     * representation
     * @param args the command's related String arguments.
     * @throws Throwable if an error occurred
     */
    private void checkExchangedData(String command, String args) throws Throwable {

        String source          = getParameter(args, 0);
        String sentAdd         = getParameter(args, 1);
        String sentReplace     = getParameter(args, 2);
        String sentDelete      = getParameter(args, 3);
        String receivedAdd     = getParameter(args, 4);
        String receivedReplace = getParameter(args, 5);
        String receivedDelete  = getParameter(args, 6);

        checkArgument(source, "Missing source name in " + command);
        checkArgument(sentAdd, "Missing sentAdd in " + command);
        checkArgument(sentReplace, "Missing sentReplace in " + command);
        checkArgument(sentDelete, "Missing sentDelete in " + command);
        checkArgument(receivedAdd, "Missing receivedAdd in " + command);
        checkArgument(receivedReplace, "Missing receivedReplace in " + command);
        checkArgument(receivedDelete, "Missing receivedDelete in " + command);

        checkObject(syncMonitor, "Run StartMainApp before command: " + command);

        getBasicRobot().checkLastSyncExchangedData(source, Integer.parseInt(sentAdd), Integer.parseInt(sentReplace),
                                         Integer.parseInt(sentDelete), Integer.parseInt(receivedAdd),
                                         Integer.parseInt(receivedReplace), Integer.parseInt(receivedDelete),
                                         syncMonitor);
    }

    /**
     * Check that the latest sync request was requested with a specific sync
     * mode
     * @param command the check sync mode command related String formatted
     * representation
     * @param args the command's related String arguments.
     * @throws Throwable if an error occurred
     */
    private void checkLastSyncRequestedSyncMode(String command, String args) throws Throwable {

        String source = getParameter(args, 0);
        String mode = getParameter(args, 1);

        checkArgument(source, "Missing source in " + command);
        checkArgument(mode, "Missing mode in " + command);
        int modeValue = Integer.parseInt(mode);

        checkObject(syncMonitor, "Run StartMainApp before command: " + command);

        getBasicRobot().checkLastSyncRequestedSyncMode(source, modeValue, syncMonitor);
    }

    /**
     * Check that the latest sync request was requested with a specific sync
     * alert mode
     * @param command the check sync alert command related String formatted
     * representation
     * @param args the command's related String arguments.
     * @throws Throwable if an error occurred
     */
    private void checkLastSyncAlertedSyncMode(String command, String args) throws Throwable {

        String source = getParameter(args, 0);
        String mode = getParameter(args, 1);

        checkArgument(source, "Missing source in " + command);
        checkArgument(mode, "Missing mode in " + command);
        int modeValue = Integer.parseInt(mode);

        checkObject(syncMonitor, "Run StartMainApp before command: " + command);

        getBasicRobot().checkLastSyncAlertedSyncMode(source, modeValue, syncMonitor);
    }

    /**
     * Check that the latest sync request was requested with a specific sync
     * uri
     * @param command the check sync uri command related String formatted
     * representation
     * @param args the command's related String arguments.
     * @throws Throwable if an error occurred
     */
    private void checkLastSyncRemoteUri(String command, String args) throws Throwable {

        String source = getParameter(args, 0);
        String remoteUri = getParameter(args, 1);

        checkArgument(source, "Missing source in " + command);
        checkArgument(remoteUri, "Missing remoteUri in " + command);

        checkObject(syncMonitor, "Run StartMainApp before command: " + command);

        getBasicRobot().checkLastSyncRemoteUri(source, remoteUri, syncMonitor);
    }

    /**
     * Command to reset a specific source anchors in order to provoke a slow
     * sync next time
     * @param command the String formatted command to break the anchors
     * @param args the command's related String arguments.
     * @throws Throwable if an error occurred
     */
    private void resetSourceAnchor(String command, String args) throws Throwable {

        String source = getParameter(args, 0);

        checkArgument(source, "Missing source in " + command);

        getBasicRobot().resetSourceAnchor(source);
    }

    /**
     * Command to request a refresh from server action from a given source
     * @param command the String formatted command to refresh the server
     * @param args the command's related String arguments.
     * @throws Throwable if an error occurred
     */
    private void refreshServer(String command, String args) throws Throwable {

        String source = getParameter(args, 0);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getBasicRobot().refreshServer(source, checkSyncClient);
    }

    /**
     * Command to simulate a keypress
     * @param command the String formatted command to simualte the key press
     * @param args the command's related String arguments.
     * @throws Throwable if an error occurred
     */
    private void keyPress(String command, String args) throws Throwable {

        String keyName = getParameter(args, 0);
        String count = getParameter(args, 1);

        checkArgument(keyName, "Missing key name in " + command);

        int pressCount = 1;
        if(count != null) {
            try {
                pressCount = Integer.parseInt(count);
            } catch(NumberFormatException ex) {
                throw new ClientTestException("Invalid count: " + count
                         + " in command: " + command);
            }
        }
        getBasicRobot().keyPress(keyName, pressCount);
    }

    /**
     * Command to simulate a input text action into an editable text field
     * @param command the String formatted command to edit text
     * @param args the command's arguments string formatted
     * @throws Throwable if anything goes wrong
     */
    private void writeString(String command, String args) throws Throwable {

        String text = getParameter(args, 0);

        checkArgument(text, "Missing string in " + command);

        getBasicRobot().writeString(text);
    }

    /**
     * A generic wait request command. Useful if the tester must wait for a
     * limited amount of time before continuing the test execution.
     * @param command the String formatted command to tell tes runner to wait
     * @param args the command's arguments string formatted
     * @throws Throwable if anything goes wrong
     */
    private void wait(String command, String args) throws Throwable {

        String delay = getParameter(args, 0);

        checkArgument(delay, "Missing delay in " + command);

        int d = Integer.parseInt(delay);

        if(d > 0) {
            Robot.waitDelay(d);
        } else {
            // Wait forever
            Robot.waitDelay(10000);
        }
    }

    /**
     * Command to check the items count on the device
     * @param command the String formatted command to check the client's items
     * count
     * @param args the command's String formatted arguments
     * @throws Throwable if anything went wrong
     */
    public void checkItemsCount(String command, String args) throws Throwable {

        String source = getParameter(args, 0);
        String count =  getParameter(args, 1);

        checkArgument(source, "Missing source name in " + command);
        checkArgument(count, "Missing count in " + command);

        getBasicRobot().checkItemsCount(source, Integer.parseInt(count));
    }

    /**
     * Command to check the items count on server
     * @param command the String formatted command to check the server's items
     * count
     * @param args the command's String formatted arguments
     * @throws Throwable if anything went wrong
     */
    public void checkItemsCountOnServer(String command, String args) throws Throwable {

        String source = getParameter(args, 0);
        String count =  getParameter(args, 1);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);
        checkArgument(source, "Missing source name in " + command);
        checkArgument(count, "Missing count in " + command);

        getBasicRobot().checkItemsCountOnServer(source, checkSyncClient,
                Integer.parseInt(count));
    }
}
 
