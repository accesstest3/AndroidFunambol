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

import com.funambol.util.Log;
import com.funambol.util.CodedException;
import com.funambol.util.HttpTransportAgent;
import com.funambol.util.StringUtil;

import com.funambol.syncml.spds.SyncConfig;

import java.util.Vector;

/**
 * The CommandRunner container implementation to run commands that are common to
 * all the clients. This is not only a CommandRunner object itself, but also a
 * container for any other command runner used by the given client test suite.
 * refer to this class using the basic contructor, but remember to add
 * explicitly implemented command runners in order to extend the command pool
 * available to the tester. Use addcommandRunner(CommandRunner) method to
 * achieve this goal. If no commad runner are added, no command will be
 * effective and the run of the test suite will be unuseful.
 * This class uses a CheckSyncClient object, a SyncMonitor and an
 * AuthSyncMonitor to check that the requested sync operations went fine both on
 * the client and the server side.
 */
public class BasicScriptRunner extends CommandRunner {

    private static final String TAG_LOG = "BasicScriptRunner";
    private static final int SUCCESS_STATUS = 0;
    private static final int CLIENT_TEST_EXCEPTION_STATUS = -1;
    // Commands
    private static final String INCLUDE_COMMAND = "Include";
    private static final String ON_COMMAND = "On";
    private static final String ON_OTHERS = "Others";

    private static String baseUrl = null;

    // The list of command runners
    private Vector commandRunners = new Vector();
    private int chainedTestsCounter;
    private String mainTestName = "";
    private StringBuffer report = new StringBuffer();
    protected int errorCode = SUCCESS_STATUS;

    private boolean stopOnFailure = false;

    /**
     * Default constructor
     */
    public BasicScriptRunner() {
        super(null);
    }

    /**
     * Add a specific CommandRunner  implementation to the current
     * BasicScriptRunner instance
     * @param runner th CommandRunner object to be added.
     */
    public void addCommandRunner(CommandRunner runner) {
        commandRunners.addElement(runner);
    }

    /**
     * This method is responsible to interprete the file (being it on the device
     * storage or taken via http), interpreting and running the commands defined
     * by the tester. First of all the it loads the script given the remote or
     * local url. Once the script is loaded it is parsed and the commands given
     * to the CommandRunner array that tries to manage them using the previously
     * added Command runners objects. The actual implementation avoid to stop
     * whenever an error or an exception occurs. In particular: if an exception
     * occurs it can be due to syntax errors contained into the tester script or
     * a client test error. In both cases if the error is detected the script
     * is entirely aborted starting from the line where the error was found. The
     * implementation also supports inner scripts to be invoked: this means that
     * if the error is located into an inner script the system will ignore all
     * of the calling scripts until level 0 is reached. For example if script1
     * needs to include script2 that again needs script3 (chained execution)
     * for its correct execution and an error is detected on script3: script3
     * and script 2 will be aborted (adding this info to the report object) and
     * the tester will see that test case wirtten in script1 failed. This apply
     * for all inner level script in which an error is detected. The first level
     * script is ignored until the latest EndtTest command is found and then
     * when the next BeginTest command is detected the test suite execution
     * restart normally form the next text on. When such those failures occur
     * the global report content is updated and the general error code is set to
     * error.
     * @param scriptUrl the script url String formatted
     * @param mainScript boolean to declare that this is a main test script
     * (not yet in use)
     * @throws Throwable if an error occurred while retrieving a script content.
     * This is the only case in which the test suite is entirely aborted.
     */
    public void runScriptFile(String scriptUrl, boolean mainScript) throws Throwable {
        baseUrl = TestFileManager.getInstance().getBaseUrl(scriptUrl);
        Log.info(TAG_LOG, "Running script at URL = " + scriptUrl);
        if (scriptUrl != null) {
            try {
                String script = TestFileManager.getInstance().getFile(scriptUrl);
                runScript(script, scriptUrl);
            } catch (Exception e) {
                report.append("Cannot load script at:\n" + scriptUrl);
                Log.error(TAG_LOG, "Cannot load script at " + scriptUrl + " because " + e);
                throw new Exception("Cannot load script " + scriptUrl + " because " + e);
            }
        } else {
            Log.error(TAG_LOG, "Cannot load script at " + scriptUrl);
            throw new ClientTestException("The script url is NULL");
        }
    }

    /**
     * Execute the given script by interpreting it
     */
    public void runScript(String script, String scriptUrl) throws Throwable {
        
        int idx = 0;
        int lineNumber = 0;
        int nextLine = -1;
        boolean onExecuted = false;

        //If an exception is catched the remaining lines of this script are
        //ignored
        boolean ignoreCurrentScript = false;

        String syntaxError = null;

        boolean eol;
        do {
            try {
                // The end of the command is recognized with the following
                // sequence ;( *)\n
                StringBuffer l = new StringBuffer();
                eol = false;
                String line = null;
                while(idx<script.length()) {
                    char ch = script.charAt(idx);
                    l.append(ch);
                    if (ch == ';') {
                        eol = true;
                        if (idx < script.length() - 1) {
                            // This may be the end of line
                            while(idx<script.length()) {
                                char nextCh = script.charAt(++idx);
                                if (nextCh == '\n') {
                                    break;
                                } else if (nextCh == ' ' || nextCh == '\r') {
                                    // Keep searching
                                    l.append(nextCh);
                                } else {
                                    // This is not the end of line
                                    l.append(nextCh);
                                    eol = false;
                                    break;
                                }
                            }
                        } else {
                            // This is the last char
                            ++idx;
                        }
                    } else if (ch == '#' && l.length() == 1) {
                        Log.trace(TAG_LOG, "Found a comment, consuming line");
                        // This line is a comment, skip everything until an EOL
                        // is found
                        ++idx;
                        for(;idx<script.length();++idx) {
                            char nextChar = script.charAt(idx);
                            if (nextChar == '\n') {
                                break;
                            } else {
                                l.append(nextChar);
                            }
                        }
                        eol = true;
                    } else if (ch == '\n') {
                        // We found a EOL without the ;
                        // This maybe an empty line that we just ignore
                        String currentLine = l.toString().trim();
                        if (currentLine.length() == 0) {
                            l = new StringBuffer();
                        } else {
                            // If otherwise this is an EOL in the middle of a
                            // command, we just ignore it (EOL shall be represented
                            // as \n)
                            l.deleteCharAt(l.length() - 1);
                        }
                    }
                    if (eol) {
                        // Remove trailing end of line (everything after the ;)
                        while(l.length() > 0 && l.charAt(l.length() - 1) != ';') {
                            l.deleteCharAt(l.length() - 1);
                        }
                        line = l.toString();
                        break;
                    }
                    ++idx;
                }

                Log.trace(TAG_LOG, "Executing line: " + line);

                if (line == null) {
                    return;
                }

                lineNumber++;

                syntaxError = null;

                line = line.trim();
                if (line.length() > 0 && !line.startsWith("#")) {
                    if (line.startsWith(ON_COMMAND + " ")) {
                        // This is a conditional statement. Check if it must be
                        // executed
                        boolean exec = false;
                        try {
                            exec = checkCandidateStatement(line, onExecuted);
                        } catch (Throwable t) {
                            errorCode = CLIENT_TEST_EXCEPTION_STATUS;
                            ignoreCurrentScript = true;
                            exec = false;
                        }
                        if (exec) {
                            onExecuted = true;
                            // Get the real command
                            int colPos = line.indexOf(":");
                            if (colPos == -1) {
                                String msg = "Syntax error in script, missing ':' in: "
                                        + line + " at line " + lineNumber;
                                Log.error(TAG_LOG, msg);
                                report.append(msg);
                                errorCode = CLIENT_TEST_EXCEPTION_STATUS;
                                ignoreCurrentScript = true;
                                //throw new ClientTestException("Script syntax error");
                            }
                            if (colPos + 1 >= line.length()) {
                                String msg = "Syntax error in script, missing command in: "
                                        + line + " at line " + lineNumber;
                                Log.error(TAG_LOG, msg);
                                report.append(msg);
                                errorCode = CLIENT_TEST_EXCEPTION_STATUS;
                                ignoreCurrentScript = true;
                                //throw new ClientTestException("Script syntax error");
                            }
                            line = line.substring(colPos + 1);
                            line = line.trim();
                        } else {
                            // skip the rest
                            Log.info(TAG_LOG, "Skipping conditional statement");
                            continue;
                        }
                    } else {
                        // Reset the conditional statement status
                        onExecuted = false;
                    }

                    int parPos = line.indexOf('(');
                    if (parPos == -1) {
                        syntaxError = "Syntax error in script "
                                + scriptUrl
                                + "\nmissing '(' in: "
                                + line + " at line " + lineNumber;
                        Log.error(syntaxError);

                        errorCode = CLIENT_TEST_EXCEPTION_STATUS;
                        ignoreCurrentScript = true;
                        // Force this script to be terminated
                        idx = script.length();
                    }

                    String command = line.substring(0, parPos);
                    command = command.trim();
                    String pars;
                    Log.trace(TAG_LOG, "line=" + line);
                    Log.trace(TAG_LOG, "parPos = " + parPos);
                    if (line.endsWith(";")) {
                        pars = line.substring(parPos, line.length() - 1);
                    } else {
                        pars = line.substring(parPos);
                    }

                    //Increments the test counter to
                    if (BasicCommandRunner.BEGIN_TEST_COMMAND.equals(command)) {
                        chainedTestsCounter++;
                        if (chainedTestsCounter == 1) {
                            mainTestName = pars;
                        }
                    } else if (BasicCommandRunner.END_TEST_COMMAND.equals(command)) {
                        chainedTestsCounter--;
                        if (chainedTestsCounter == 0) {
                            ignoreCurrentScript = false;
                        }
                    }

                    if (!ignoreCurrentScript) {
                        runCommand(command, pars);
                    }
                }
            } catch (Throwable t) {
                errorCode = CLIENT_TEST_EXCEPTION_STATUS;

                StringBuffer msg = new StringBuffer();
                msg.append("\nTEST FAILED: " + mainTestName);
                msg.append("\n\tException: " + t);
                msg.append(syntaxError != null ? "\n\t" + syntaxError : "");
                msg.append("\n\t(" + scriptUrl + ": " + lineNumber + ")");

                Log.error(msg.toString());
                Log.error(TAG_LOG, "Exception details", t);
                report.append(msg);

                //tell the scriptrunner to ignore all of the chained tests
                //commands
                ignoreCurrentScript = true;

                if(stopOnFailure) {
                    throw new ClientTestException("TEST FAILED");
                }
            }
        } while (true);
    }

    /**
     * Accessor Method to get the base url related to the tests location
     * @return String the String formatted url to be used as the base url for
     * tests. Example: be the main script url
     * "http://url.somewhere.com/folder1/folder2/Test.txt", this method will
     * return be "http://url.somewhere.com/folder1/folder2". This method just
     * return the url after the computation. Can return null if the baseUrl
     * has not yet been calculated.
     */
    public static String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Tells if the script runner shall be interrupted at the first failure
     *
     * @param  stop
     */
    public void setStopOnFailure(boolean stop) {
        stopOnFailure = stop;
    }

    /**
     * Checks the given command runner in order to execute the given command
     * with the given arguments. If no runner is defined for a given command the
     * error is detected returning false.
     * @param command the String formatted command to be passed to the runners
     * array
     * @param pars the command's parameter.
     * @return true if a CommandRunner defined for this object can manage the
     * given command with the given parameters, false otherwise.
     * @throws Throwable if an error occurred executing the command.
     */
    public boolean runCommand(String command, String pars) throws Throwable {

        Log.trace(TAG_LOG, "command=" + command);
        Log.trace(TAG_LOG, "pars=" + pars);

        if (INCLUDE_COMMAND.equals(command)) {
            includeScript(command, pars);
        } else {
            boolean ok = false;
            for (int i = 0; i < commandRunners.size(); i++) {
                CommandRunner runner = (CommandRunner) commandRunners.elementAt(i);
                if (runner.runCommand(command, pars)) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                throw new IllegalArgumentException("Unknown command " + command);
            }
        }
        return true;
    }

    /**
     * Includes a given script in the current executing one.This avoid testers
     * to write a huge amount of lines to reach the same goal (for example
     * initialize an atomic test set).
     * @param command the String formatted command to include a script
     * @param args the command's related String formatted arguments
     * @throws Throwable if an error occurs
     */
    private void includeScript(String command, String args) throws Throwable {
        String scriptUrl = getParameter(args, 0);

        checkArgument(scriptUrl, "Missing script url in " + command);

        if (!scriptUrl.startsWith("http") && !scriptUrl.startsWith("file")) {
            int upCount = 0;
            while(scriptUrl.startsWith("..")) {
                scriptUrl = scriptUrl.substring("../".length());
                upCount++;
            }
            if (baseUrl != null) {
                String newBaseUrl = baseUrl;
                for(int i=0; i<upCount; i++) {
                    int index = newBaseUrl.lastIndexOf('/');
                    newBaseUrl = newBaseUrl.substring(0, index);
                }
                scriptUrl = newBaseUrl + "/" + scriptUrl;
            }
        }
        String tmpBaseUrl = baseUrl; 
        runScriptFile(scriptUrl, false);
        baseUrl = tmpBaseUrl;
    }

    /**
     * Accessor method to retrieve the global status of the entire test suite.
     * @return int the value related to this test failure or success.
     */
    public int getErrorCode() {
        return this.errorCode;
    }

    /**
     * Accessor method to set the global suite error code from outside (external 
     * syntax error) 
     * @param errorCode the int representation of the error code
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Accessor method to retrieve the details of the failed tests
     * @return String the String formatted failed test report content.
     */
    public String getResults() {
        return report.toString();
    }

    /**
     * Accessor method to retrieve the OS version
     * @return the empty String. Other values are implementation specific.
     */
    protected String getOsVersion() {
        return "";
    }

    private String[] createVersionArray(String version[]) {
        String tmp[] = new String[3];
        for (int i = 0; i < 3; ++i) {
            if (i < version.length) {
                tmp[i] = version[i];
            } else {
                tmp[i] = "0";
            }
        }
        return tmp;
    }

    /**
     * Check the candidate statement to be executed given a conditional command
     * @param command the conditional statement command
     * @param onExecuted tries to predict the condition set in the command
     * @return true if the command is validand must be executed, false otherwise
     * @throws Throwable if an error occurs.
     */
    protected boolean checkCandidateStatement(String command, boolean onExecuted) throws Throwable {
        String currentOs = getOsVersion();
        String commandTokens[] = StringUtil.split(command, " ");
        boolean onOther = false;

        if (commandTokens.length < 3) {
            // This can only be the on others case
            if (commandTokens.length == 2) {
                String tmp = commandTokens[1].trim();
                if (ON_OTHERS.equals(tmp)) {
                    onOther = true;
                }
            }

            if (!onOther) {
                String msg = "Syntax error in On command, missing os versions" + command;
                Log.error(TAG_LOG, msg);
                report.append(msg);
                throw new ClientTestException("Script syntax error");
            }
        }

        if (!onOther) {
            String minOS = commandTokens[1];
            String maxOS = commandTokens[2];

            String min[] = StringUtil.split(minOS.trim(), ".");
            String max[] = StringUtil.split(maxOS.trim(), ".");
            String os[] = StringUtil.split(currentOs.trim(), ".");

            min = createVersionArray(min);
            max = createVersionArray(max);
            os = createVersionArray(os);

            int low = Integer.parseInt(min[0]) * 100
                    + Integer.parseInt(min[1]) * 10
                    + Integer.parseInt(min[0]);

            int high = Integer.parseInt(max[0]) * 100
                    + Integer.parseInt(max[1]) * 10
                    + Integer.parseInt(max[0]);

            int value = Integer.parseInt(os[0]) * 100
                    + Integer.parseInt(os[1]) * 10
                    + Integer.parseInt(os[0]);

            Log.info(TAG_LOG, "current version =" + value + ", min=" + min + ", max=" + max);
            if (value >= low && value <= high) {
                // The on condition matches
                if (onExecuted) {
                    String msg = "Syntax error in On command, more conditions matching" + command;
                    Log.error(TAG_LOG, msg);
                    report.append(msg);
                    throw new ClientTestException("Script syntax error");
                }
                return true;
            } else {
                return false;
            }
        } else {
            boolean res = !onExecuted;
            return res;
        }
    }

    /**
     * Set the SyncMonitor object for this CommandRunner container
     * @param monitor the SyncMonitor to be set
     */
    public void setSyncMonitor(SyncMonitor monitor) {
        super.setSyncMonitor(monitor);
        for (int i = 0; i < commandRunners.size(); i++) {
            CommandRunner runner = (CommandRunner) commandRunners.elementAt(i);
            runner.setSyncMonitor(monitor);
        }
    }

    /**
     * Set the AuthSyncMonitor object for this CommandRunner container
     * @param monitor the AuthSyncMonitor to be set
     */
    public void setAuthSyncMonitor(SyncMonitor monitor) {
        super.setAuthSyncMonitor(monitor);
        for (int i = 0; i < commandRunners.size(); i++) {
            CommandRunner runner = (CommandRunner) commandRunners.elementAt(i);
            runner.setAuthSyncMonitor(monitor);
        }
    }

    /**
     * Set the CheckSyncClient object for this CommandRunner container
     * @param client the CheckSyncClient to be set
     */
    public void setCheckSyncClient(CheckSyncClient client) {
        super.setCheckSyncClient(client);
        for (int i = 0; i < commandRunners.size(); i++) {
            CommandRunner runner = (CommandRunner) commandRunners.elementAt(i);
            runner.setCheckSyncClient(client);
        }
    }

    /**
     * Create a transport agent useful for the tests framework
     * @param config the SyncConfig used to configure the TransportAgent
     * @return HttpTransportAgent the instance of the HTTPTransportAgent correctly configured
     */
    public static HttpTransportAgent createTestTransportAgent(SyncConfig config) {
        HttpTransportAgent ta = new HttpTransportAgent(
                config.syncUrl,
                config.userAgent,
                "UTF-8",
                config.compress, config.forceCookies);
        // Force messages to be resent in case of errors
        ta.setResendMessageOnErrors(true);
        return ta;
    }
}

