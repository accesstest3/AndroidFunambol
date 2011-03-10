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

package com.funambol.android;

import android.app.Activity;
import android.content.Intent;

import com.funambol.android.activities.AndroidHomeScreen;
import com.funambol.android.activities.AndroidActivitiesFactory;
import com.funambol.android.controller.AndroidController;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.test.BasicCommandRunner;
import com.funambol.client.test.BasicScriptRunner;
import com.funambol.client.test.CheckSyncClient;
import com.funambol.client.test.Robot;
import com.funambol.util.Log;

/**
 * Implementation of the client's API BasicCommandRunner object. Manages both
 * the test suite and main additional activities controls. It uses defined
 * intents in order to call the activities that make the tester able to insert
 * the test suite url and display the final report. It is of course responsible
 * to initialize and start the main application that is under test using the
 * App's initilizer agent.
 */
public class AndroidCommandRunner extends BasicCommandRunner implements AndroidUserCommands {

    AndroidTestReport androidTestReport;
    Intent reportIntent;
    Intent urlIntent;

    private static final String TAG_LOG = "AndroidCommandRunner";

    private AndroidScriptRunner scriptRunner;

    /**
     * Constructor
     * @param scriptRunner the AndroidScriptRunner object that represents the
     * Instrumentation to run this test suite
     * @param robot AndroidBasicRobot to interprete all of the Android specific
     * test suite commands
     */
    public AndroidCommandRunner(AndroidScriptRunner scriptRunner,
            AndroidBasicRobot robot) {
        super(robot);
        this.scriptRunner = scriptRunner;
        //androidTestReport = new AndroidTestReport();
        reportIntent = new Intent(Intent.ACTION_MAIN);
        reportIntent.setClass(scriptRunner.getTargetContext(), AndroidTestReport.class);
        reportIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        urlIntent = new Intent(Intent.ACTION_MAIN);
        urlIntent.setClass(scriptRunner.getTargetContext(), AndroidTestUrl.class);
        urlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }


    /**
     * Starts the report activity to display the test suite report after the
     * test suite run. In order this activity to be destroyed when running on
     * auto test system it is auto-destroyed after 15 secs if the tester doesn't
     * manually stop it to see the report details.
     */
    protected void startReportActivity() {
        AndroidTestReport a = (AndroidTestReport) scriptRunner.startActivitySync(reportIntent);

        try {
            Thread.sleep(15000);
        } catch (InterruptedException ex) {
            Log.error(TAG_LOG, "Cannot wait because " + ex);
        }

        if (!a.isTouchActionRunning()) {
            scriptRunner.callActivityOnDestroy(a);
        }
    }

    /**
     * Start the url editing activity that allows the tester to change the url
     * of the test suite to be run. The tester can usa both an http or a file
     * url as they are both supported. After the launch of the activity the
     * current url is shown to the tester. In order to be consistent with an
     * eventual automatic build systestem if no user interaction occurred after
     * 15 secs the test url is automatically run, otherwise the start command
     * must be pressed by the tester in order to start the test suite once the
     * url is finally defined.
     */
    protected void startUrlActivity() {
        AndroidTestUrl a = (AndroidTestUrl) scriptRunner.startActivitySync(urlIntent);

        try {
            Thread.sleep(15000);
        } catch (InterruptedException ex) {
            Log.error(TAG_LOG, "Cannot wait because " + ex);
        }

        if (!a.isTouchActionRunning()) {
            scriptRunner.callActivityOnDestroy(a);
        } else {

            while(!a.isEditComplete()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Log.error(TAG_LOG, "Cannot wait because " + ex);
                }
            }
            Log.error(TAG_LOG, "Edit complete");
            scriptRunner.setMainTestUrl(a.getTestUrl());
            scriptRunner.callActivityOnDestroy(a);
        }
    }

    /**
     * Starts the  main test suite on the client using the related intent.
     * @param command the String that represents the command to run (Actually
     * not used in this implementation)
     * @param args the String formatted command arguments
     * @throws Throwable if an unrecoverable error occurs during the test suite
     * run (for example the unavailability to initialize the application for any
     * reason).
     */
    protected void startMainApp(String command, String args) throws Throwable {

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(scriptRunner.getTargetContext(), AndroidHomeScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Activity activity = scriptRunner.startActivitySync(intent);

        android.util.Log.i("getComponentName", activity.getComponentName().getShortClassName());

        if(activity.getComponentName().getShortClassName().equals(
                "com.funambol.android.activities.AndroidHomeScreen")) {

            // Now initialize everything
            AppInitializer initializer = AppInitializer.getInstance(scriptRunner.getTargetContext());
            AndroidController cont = AndroidController.getInstance(scriptRunner.getTargetContext(),
                    new AndroidActivitiesFactory(),
                    initializer.getConfiguration(),
                    initializer.getCustomization(),
                    initializer.getLocalization(),
                    initializer.getAppSyncSourceManager());

            scriptRunner.getBasicScriptRunner().setSyncMonitor(
                    new AndroidSyncMonitor(cont.getHomeScreenController()));

            Configuration configuration = initializer.getConfiguration();

            // Wait that the login screen is shown
            // FIXME: do it in a smarter way
            Robot.waitDelay(3);
            scriptRunner.getBasicScriptRunner().setAuthSyncMonitor(
                    new AndroidSyncMonitor(cont.getLoginScreenController()));

            // Get the sync config
            String syncUrl  = configuration.getSyncUrl();
            String userName = configuration.getUsername();
            String password = configuration.getPassword();

            scriptRunner.getBasicScriptRunner().setCheckSyncClient(
                    new CheckSyncClient(syncUrl, userName, password));

            // Init robot
            robot.initialize();
        }
    }

    /**
     * Run the command given in the input script by the tester. Relies upon the
     * low level BasicCommandRunner implementation for the main commands
     * definition while his extension just manages the case of waiting for a
     * specific ativity or removing an addressed account (they are architecture
     * specific implementations)
     * @param command the String representation of the input command
     * @param args the String representation of the command arguments
     * @return boolean true if the command is valid, false otherwise
     * @throws Throwable if a command thrown an exception when it was run
     */
    @Override
    public boolean runCommand(String command, String args) throws Throwable {

        if(!super.runCommand(command, args)) {
            if (WAIT_FOR_ACTIVITY_COMMAND.equals(command)) {
                waitForActivity(command, args);
            } else if (REMOVE_ACCOUNT_COMMAND.equals(command)) {
                removeAccount(command, args);
            } else if (CANCEL_SYNC_COMMAND.equals(command)) {
                cancelSync(command, args);
            } else if (CHECK_SYNC_PENDING_COMMAND.equals(command)) {
                checkSyncPending(command, args);
            } else if (SET_AUTO_SYNC_COMMAND.equals(command)) {
                setAutoSyncEnabled(command, args);
            } else if (SET_SOURCE_AUTO_SYNC_COMMAND.equals(command)) {
                setSourceAutoSyncEnabled(command, args);
            } else if (CHECK_SOURCE_AUTO_SYNC_COMMAND.equals(command)) {
                checkSourceAutoSyncEnabled(command, args);
            } else if (CHECK_LAST_ALERT_MESSAGE.equals(command)) {
                checkLastAlertMessage(command, args);
            } else if (CANCEL_SYNC_AFTER_PHASE.equals(command)) {
                cancelSyncAfterPhase(command, args);
            } else if (WAIT_FOR_SYNC_PHASE.equals(command)) {
                waitForSyncPhase(command, args);
            } else if (ADD_PICTURE.equals(command)) {
                addPicture(command, args);
            } else if (DELETE_PICTURE.equals(command)) {
                deletePicture(command, args);
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void waitForSyncToComplete(String command, String args) throws Throwable {

        int paramsCount = getParametersCount(args);
        if(paramsCount == 2) {
            super.waitForSyncToComplete(command, args);
        } else {
            String sourceName = getParameter(args, 0);
            String minStart   = getParameter(args, 1);
            String maxWait    = getParameter(args, 2);

            checkArgument(sourceName, "Missing sourceName in " + command);
            checkArgument(minStart, "Missing min start in " + command);
            checkArgument(maxWait, "Missing max wait in " + command);

            checkObject(syncMonitor, "Run StartMainApp before command: " + command);

            int min = Integer.parseInt(minStart)*1000;
            int max = Integer.parseInt(maxWait)*1000;

            ((AndroidBasicRobot)getBasicRobot()).waitForSyncToComplete(
                    sourceName, min, max, (AndroidSyncMonitor)syncMonitor);
        }
    }

    /**
     * Wait for a specified amount of time that a particular activity is
     * displayed on the screen. The parameters are set into the script command
     * arguments.
     * @param command the String representation that match the "WaitForActivity"
     * command
     * @param args the "WaitForActivity" command arguments: the name of the
     * expected activity and the time interval to wait until is it displayed to
     * the tester on the screen.
     * @throws Throwable if anything went wrong with the command execution
     */
    private void waitForActivity(String command, String args) throws Throwable {

        String activityName = getParameter(args, 0);
        String timeout      = getParameter(args, 1);

        checkArgument(activityName, "Missing activity name in " + command);
        checkArgument(timeout, "Missing timeout in " + command);

        int t = Integer.parseInt(timeout);
        ((AndroidBasicRobot)robot).waitForActivity(activityName, t);
    }

    /**
     * Android specific account removal command execution
     * @param command the String representation of the action to "Remove" an
     * account
     * @param args the String arguments to be passed to the AndroidBasicRobot
     * in order to have a particular account deleted
     * @throws Throwable is something went wrong while executing the command
     */
    private void removeAccount(String command, String args) throws Throwable {
        AndroidBasicRobot.removeAccount(scriptRunner.getTargetContext());
    }

    private void cancelSync(String command, String args) throws Throwable {
        ((AndroidBasicRobot)getBasicRobot()).cancelSync();
    }

    private void checkSyncPending(String command, String args) throws Throwable {

        String sourceName = getParameter(args, 0);

        checkArgument(sourceName, "Missing source name in " + command);

        String pending = getParameter(args, 1);
        boolean checkPending = true;
        if(pending != null) {
            checkPending = Boolean.parseBoolean(pending);
        }
        ((AndroidBasicRobot)robot).checkSyncPending(sourceName, checkPending);
    }

    private void setAutoSyncEnabled(String command, String args) throws Throwable {

        String enabled = getParameter(args, 0);

        checkArgument(enabled, "Missing enabled param in " + command);

        ((AndroidBasicRobot)robot).setAutoSyncEnabled(Boolean.parseBoolean(enabled));
    }

    private void setSourceAutoSyncEnabled(String command, String args) throws Throwable {

        String sourceName = getParameter(args, 0);
        String enabled = getParameter(args, 1);

        checkArgument(enabled, "Missing source name param in " + command);
        checkArgument(enabled, "Missing enabled param in " + command);

        ((AndroidBasicRobot)robot).setSourceAutoSyncEnabled(sourceName,
                Boolean.parseBoolean(enabled));
    }

    private void checkSourceAutoSyncEnabled(String command, String args) throws Throwable {

        String sourceName = getParameter(args, 0);
        String enabled = getParameter(args, 1);

        checkArgument(enabled, "Missing source name param in " + command);
        checkArgument(enabled, "Missing enabled param in " + command);

        ((AndroidBasicRobot)robot).checkSourceAutoSyncEnabled(sourceName,
                Boolean.parseBoolean(enabled));
    }

    private void checkLastAlertMessage(String command, String args) throws Throwable {

        String message = getParameter(args, 0);

        checkArgument(message, "Missing message param in " + command);

        ((AndroidBasicRobot)robot).checkLastAlertMessage(message);
    }

    private void cancelSyncAfterPhase(String command, String args) throws Throwable {

        String phase  = getParameter(args, 0);
        String num    = getParameter(args, 1);

        checkArgument(phase, "Missing phase name param in " + command);
        checkArgument(num, "Missing num param in " + command);

        checkObject(syncMonitor, "Run StartMainApp before command: " + command);

        ((AndroidBasicRobot)robot).cancelSyncAfterPhase(phase,
                Integer.parseInt(num), syncMonitor);
    }
    
    private void waitForSyncPhase(String command, String args) throws Throwable {

        String phase   = getParameter(args, 0);
        String num     = getParameter(args, 1);
        String timeout = getParameter(args, 2);

        checkArgument(phase, "Missing phase name param in " + command);
        checkArgument(num, "Missing num param in " + command);
        checkArgument(timeout, "Missing timeout param in " + command);

        checkObject(syncMonitor, "Run StartMainApp before command: " + command);

        ((AndroidBasicRobot)robot).waitForSyncPhase(phase,
                Integer.parseInt(num), Integer.parseInt(timeout)*1000, syncMonitor);
    }

    private void addPicture(String command, String args) throws Throwable {

        String filename = getParameter(args, 0);

        checkArgument(filename, "Missing filename param in " + command);

        ((AndroidBasicRobot)robot).addPicture(BasicScriptRunner.getBaseUrl() +
                "/" + filename);
    }

    private void deletePicture(String command, String args) throws Throwable {

        String filename = getParameter(args, 0);

        checkArgument(filename, "Missing filename param in " + command);

        ((AndroidBasicRobot)robot).deletePicture(filename);
    }

}
 
