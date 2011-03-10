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
import com.funambol.util.Log;
import com.funambol.util.AndroidLogAppender;
import com.funambol.util.StringUtil;

import com.funambol.client.test.BasicScriptRunner;
import com.funambol.client.test.ClientTestException;
import com.funambol.client.test.ContactsCommandRunner;
import com.funambol.client.test.CalendarCommandRunner;

import android.app.Instrumentation;
import android.os.Bundle;

/**
 * Instrumentation class that runs the test suite. Supports both http and file 
 * script streams. Use a BasicCommandRunner to load the test suite scripts and
 * manage the test activities control. Support additional activities to improve
 * the tester experience: the first is an editable text view that allows the
 * user to change the test suite url after the launch before running the script.
 * After the test suite has been executed a second activity displays the report
 * of the test suite for the failed test cases.
 *
 * To run the Instrumentation with adb:
 *
 *  $ adb shell am instrument -e script url [-e stopOnFailure]
 *      -w com.funambol.androidsync/com.funambol.android.AndroidScriptRunner
 */
public class AndroidScriptRunner extends Instrumentation {

    /** Unique ID for Logging reference */
    private static final String TAG_LOG = "AndroidScriptRunner";

    /** The instrumentation arguments loaded in the command line*/
    private static final String EXTRA_SCRIPT_URL      = "script";
    private static final String EXTRA_STOP_ON_FAILURE = "stopOnFailure";

   /** Default test suite url */
    private static final String SCRIPT_URL_DEFAULT =
            "http://d.funambol.com/Test/marco/regression/android/android.txt";

    private String mainScriptUrl = null;

    private String scriptUrlFromCommandLine = null;

    private BasicScriptRunner basicRunner;

    private AndroidCommandRunner acr;

    private static AndroidScriptRunner instance;

    /**
     * When the instrumentation is created it performs the basic configuration
     * actions of initializing the log, getting the test suite script url,
     * initializing the required command runners to be used by the
     * BasicCommandRunner in order to run the test suite. After the
     * initialization the instrumentation is started.
     * @param arguments the Bundle object with the Instrumentation arguments.
     * The script url should be defined here
     */
    @Override
    public void onCreate(Bundle arguments) {
        instance = this;
        super.onCreate(arguments);

        // Init log
        Log.initLog(new AndroidLogAppender(TAG_LOG), Log.TRACE);

        boolean stopOnFailure = false;

        // Get the extra params
        if(arguments != null) {
            mainScriptUrl = arguments.getString(EXTRA_SCRIPT_URL);
            stopOnFailure = arguments.containsKey(EXTRA_STOP_ON_FAILURE);
            scriptUrlFromCommandLine = mainScriptUrl;
        }
        if(StringUtil.isNullOrEmpty(mainScriptUrl)) {
            mainScriptUrl = SCRIPT_URL_DEFAULT;
        }

        AndroidBasicRobot basicRobot = new AndroidBasicRobot(this);
        acr = new AndroidCommandRunner(this, basicRobot);
        basicRunner = new BasicScriptRunner();
        basicRunner.addCommandRunner(acr);
        // Setup contacts script runner
        AndroidContactsRobot aContactRobot = new AndroidContactsRobot(this, basicRobot);
        aContactRobot.setScriptRunner(basicRunner);
        ContactsCommandRunner contactsCommandRunner = new ContactsCommandRunner(aContactRobot);
        basicRunner.addCommandRunner(contactsCommandRunner);
        // Setup contacts script runner
        AndroidCalendarsRobot aCalendarsRobot = new AndroidCalendarsRobot(this, basicRobot);
        aCalendarsRobot.setScriptRunner(basicRunner);
        CalendarCommandRunner calendarCommandRunner = new CalendarCommandRunner(aCalendarsRobot);
        basicRunner.addCommandRunner(calendarCommandRunner);
        // Other properties
        basicRunner.setStopOnFailure(stopOnFailure);
        start();
    }

    /**
     * Accessor method: get the actual script url defined by the tester
     * @return String the String formatted test url
     */
    public String getMainTestUrl() {
        return mainScriptUrl;
    }

    /**
     * Accessor method: Set the main test url
     * @param url the String formatted test url to be set
     */
    public void setMainTestUrl(String url) {
        this.mainScriptUrl = url;
    }

    /**
     * Get the content of the test report based on the BasicScriptRunner
     * Implementation
     * @return String the String formatted test report details
     */
    public String getReportContent() {
        return basicRunner.getResults();
    }

    /**
     * Triggered when the start method is invoked. Uses the AndroidCommandRunner
     * to start the AndroidTestUrl activity - in order the user to define the
     * test suite url -, runs the test suite and finally displays the
     * AndroidTestReport activity to display the results to the tester. If an
     * error is found in any test script it is managed directlyin this method.
     * The low level implementation traps the exception and fill the test report
     * details in order the test suite not to be stopped if an exception occurs.
     */
    @Override
    public void onStart() {
        
        super.onStart();

        // Show the url activity if the user hasn't specified it through the
        // command line
        if(scriptUrlFromCommandLine == null) {
            acr.startUrlActivity();
        }

        try {
            basicRunner.runScriptFile(mainScriptUrl, true);
        } catch(Throwable t) {
            // Notify the error
            Log.error(TAG_LOG, "Error running test: ", t);
            basicRunner.setErrorCode(-1);
        } finally {
            acr.startReportActivity();
        }
    }

    /**
     * Accessor Method to get the BasicScriptRunner instance.
     * @return BasicScriptRunner the BasicScriptRunner object that is defined
     * into the clint API (Low level). This object can be referred to in order
     * to retireve the test details.
     */
    public BasicScriptRunner getBasicScriptRunner() {
        return basicRunner;
    }

    /**
     * Get the current instance of this Instrumentation implementation. This is
     * helpful in order to retrieve the always update instance of the running
     * object
     * @return AndroidScriptRunner this class instance
     */
    public static AndroidScriptRunner getInstance() {
        return instance;
    }
}
