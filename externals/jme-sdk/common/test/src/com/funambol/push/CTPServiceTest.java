/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2003 - 2008 Funambol, Inc.
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


package com.funambol.push;

import com.funambol.util.ConsoleAppender;
import com.funambol.util.Log;
import java.io.IOException;

import junit.framework.*;

/**
 * This tests use a inner class that extends CTPService in order to be used as a 
 * mock object for the tests.
 */
public class CTPServiceTest extends TestCase implements CTPListener {

    CTPServiceTester cst = null;
    
    public CTPServiceTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender(), Log.DEBUG);
    }

    public void setUp() {
        cst = new CTPServiceTester();
        cst.setCTPListener(this);
    }

    public void tearDown() {
        cst = null;
    }

    /**
     * Test the setConfig of the CTP service
     */
    public void testSetConfig() throws Exception {
        Log.debug("[testSetConfig]");
        PushConfig pc1 = new PushConfig();
        PushConfig pc2 = new PushConfig();
        PushConfig pc3 = new PushConfig();
        cst.setConfig(pc1);
        cst.setConfig(pc2);
        cst.setConfig(pc3);
        assertTrue(cst.getConfig().equals(pc3));
    }
    /**
     * Test the getConfig of the CTP service
     */
    public void testGetConfig() throws Exception {
        Log.debug("[testGetConfig]");
        PushConfig pc = new PushConfig();
        cst.setConfig(pc);
        assertTrue(cst.getConfig().equals(pc));
    }
    /**
     * Test the getInstance of the CTP service
     */
    public void testGetInstance() throws Exception {
        Log.debug("[testGetInstance]");
        assertTrue(cst instanceof CTPServiceTester);
    }
    
    /**
     * Test the isRunning of the CTP service
     */
    public void testIsRunning() throws Exception {
        Log.debug("[testIsRunning]");
        boolean init = cst.isRunning()==false;
        
        cst.startService(new PushConfig());
        
        Thread.sleep(2000);
        
        assertTrue(cst.isRunning()&&init);
        cst.stopService();
    }
        
    /**
     * Test the restartService of the CTP service
     */
    public void testRestartService() throws Exception {
        
        Log.debug("[testRestartService]");
        cst.restartService(new PushConfig());
        
        Thread.sleep(2000);
        assertTrue(cst.state==cst.LISTENING);
        cst.stopService();
    }

    /**
     * Test the restartService of the CTP service
     */
    public void testRestartServiceNoConnection() throws Exception {
       
        Log.debug("[testRestartServiceNoConnection]");

        PushConfig pc = new PushConfig();
        cst.setConfig(pc);

        Log.debug("Starting service");
        cst.startService();
        Thread.sleep(2000);
        Log.debug("is running?");
        assertTrue(cst.isRunning());

        Log.debug("Simulate network loss");
        cst.simulateNetworkLoss(10000);
        Log.debug("Restart the service");
        cst.restartService(new PushConfig());

        // Let it run
        Thread.sleep(2000);
        
        assertTrue(cst.state==cst.LISTENING);
        cst.stopService();
    }
        

    /**
     * Test the startService of the CTP service
     */
    public void testStartService() throws Exception {
        Log.debug("[testStartService]");
        cst.startService(new PushConfig());
        try {
            // assertion not really useful due to threads usage
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }
        assertTrue(cst.state==cst.LISTENING);
        cst.stopService();
    }
        
    /**
     * Test the setOfflineMode of the CTP service
     */
    public void testSetOfflineMode() throws Exception {
        Log.debug("[testSetOfflineMode]");
        cst.startService();
        cst.setOfflineMode(true);
        assertTrue(cst.isOfflineMode());
        cst.stopService();
    }
        
    /**
     * Test the isOfflineMode of the CTP service
     */
    public void testIsOfflineMode() throws Exception {
        Log.debug("[testIsOfflineMode]");
        cst.setOfflineMode(false);
        assertTrue(!cst.isOfflineMode());
    }
        
    /**
     * Test the isOfflineMode of the CTP service
     */
    public void testIsNotOfflineMode() throws Exception {
        Log.debug("[testIsNotOfflineMode]");
        cst.setOfflineMode(true);
        assertTrue(cst.isOfflineMode());
    }
        
    /**
     * Test the stopService of the CTP service
     */
    public void testStopService() throws Exception {
        Log.debug("[testStopService]");
        cst.startService(new PushConfig());
        try {
            // assertion not really useful due to threads usage
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }
        cst.stopService();
        assertTrue(cst.state==cst.DISCONNECTED);
    }
       
    /**
     * Test the forceDisconnect of the CTP service
     */
    public void testForceDisconnect() throws Exception {
        Log.debug("[testForceDisconnect]");
        cst.startService(new PushConfig());
        try {
            // assertion not really useful due to threads usage
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }
        cst.stopService();
        cst.forceDisconnect();
        assertTrue(cst.state==cst.DISCONNECTED);
    }
    
    /**
     * Test the getCTPStringState method
     */
    public void testGetCTPStringState() throws AssertionFailedError {
        Log.debug("[testGetCTPStringState]");
        cst.state = cst.DISCONNECTED;
        boolean result = "Disconnected".equals(cst.getCTPStringState());
        cst.state = cst.CONNECTING;
        result = "Connecting...".equals(cst.getCTPStringState());
        cst.state = cst.CONNECTED;
        result = "Connected".equals(cst.getCTPStringState());
        cst.state = cst.AUTHENTICATING;
        result = "Authenticating...".equals(cst.getCTPStringState());
        cst.state = cst.AUTHENTICATED;
        result = "Authenticated".equals(cst.getCTPStringState());
        cst.state = cst.LISTENING;
        result = "SAN Listening...".equals(cst.getCTPStringState());
        assertTrue(result);
    }
    
    /**
     * Test the isPushActive method
     */
    public void testIsPushActive() throws AssertionFailedError {
        Log.debug("[testIsPushActive]");
        boolean init = cst.isPushActive()==false;
        cst.startService(new PushConfig());
        try {
            // assertion not really useful due to threads usage
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }
        boolean result = init && cst.isPushActive(); 
        assertTrue(result);
        cst.stopService();
    }

    public void testRetryPeriod() throws Exception {
        // Start the service and then disable network. The test
        // checks that the service wait an increasing amount of time between
        // successive connection attempts.
        Log.debug("[testRetryPeriod]");

        PushConfig pc = new PushConfig();
        // Hearbeat rate increased to 2 secs for tests purposes
        pc.setCtpReady(2);
        cst.startService(pc);
        Thread.sleep(5000);
        // Wait for six minutes
        cst.simulateNetworkLoss(100);
        Thread.sleep(8 * 60 * 1000);
        // Check if it did not try too many connections
        int connAttempt = cst.getConnAttempt();
        assertTrue(connAttempt == 5);

        cst.stopService();
    }

    public void testLongLatencyError() throws Exception {
        Log.debug("[testLongLatencyError]");

        PushConfig pc = new PushConfig();
        // Hearbeat rate increased to 2 secs for tests purposes
        pc.setCtpReady(2);
        cst.startService(pc);
        Thread.sleep(5000);
        // Simulate a network error with 20 seconds timeout
        cst.simulateNetworkLoss(20 * 1000);
        // Wait for one minutes
        Thread.sleep(2 * 60 * 1000);
        // Check if it did not try too many connections
        int connAttempt = cst.getConnAttempt();
        assertTrue(connAttempt == 3);

        cst.stopService();
    }

    public void testFailingAuthentication1() throws Exception {
        Log.debug("[testFailingAuthentication1]");

        PushConfig pc = new PushConfig();
        cst.simulateAuthenticationResult(cst.UNAUTHORIZED);
        cst.startService(pc);
        Thread.sleep(5000);
        // Check if it did not try too many auth attempts
        int authAttempt = cst.getAuthAttempt();
        assertTrue(authAttempt == 1);
        cst.stopService();
    }

    public void testFailingAuthentication2() throws Exception {
        Log.debug("[testFailingAuthentication2]");

        PushConfig pc = new PushConfig();
        cst.simulateAuthenticationResult(cst.FORBIDDEN);
        cst.startService(pc);
        Thread.sleep(5000);
        // Check if it did not try too many auth attempts
        int authAttempt = cst.getAuthAttempt();
        assertTrue(authAttempt == 1);
        cst.stopService();
    }

    public void testShakyConnection1() throws Exception {
        Log.debug("[testShakyConnection1]");

        PushConfig pc = new PushConfig();
        pc.setCtpReady(2);
        // Hearbeat rate increased to 2 secs for tests purposes
        cst.startService(pc);
        // We should be listening here
        // TODO how do we check that
        for(int i=0;i<5;++i) {
            Thread.sleep(2000);
            cst.simulateNetworkLoss(100);
            Thread.sleep(1000);
            cst.restoreNetwork();
        }
        cst.stopService();
    }
 
    
    public class CTPServiceTester extends CTPService {

        private boolean network           = true;
        private int     timeout           = 0;
        private boolean receiveOk         = false;
        private int     authAttempt       = 0;
        private int     connectionAttempt = 0;
        private boolean closed            = false;
        private int     authResult        = ST_OK;

        public static final int UNAUTHORIZED = 0;
        public static final int FORBIDDEN    = 1;

        private void simulateNetworkError() throws IOException {
            try {
                Log.debug("Simulating failing IO with timeout: " + timeout);
                int t = 0;
                while (t < timeout) {
                    Thread.sleep(100);
                    t += 100;
                    if (closed) {
                        closed = false;
                        throw new IOException("Closed IO channel");
                    }
                }
            } catch(InterruptedException ie) {}

            throw new IOException("Simulated Network IO Error");
        }
        
        /**
         * Overrides the connect method in order to giv e mock object on the 
         * IO stream
         */
        protected void connect(int retry) throws IOException {
            Log.info("[CTPServiceTest]connect");
            connectionAttempt++;
            if (network) {
                Thread.yield();
                state=CONNECTED;
            } else {
                simulateNetworkError();
            }
        }
        
        /**
         * Overrides the connect method in order to giv e mock object on the 
         * IO stream
         */
        protected void disconnect() {
            Log.info("[CTPServiceTest]disconnect");
            Thread.yield();
            state=DISCONNECTED;
        }

        /**
         * Overrides the connect method in order to giv e mock object on the 
         * IO stream
         */
        protected void closeConnection() {
            Log.info("[CTPServiceTest]closeConnection");
            closed=true;
            Thread.yield();
        }

        /**
         * Overrides the connect method in order to giv e mock object on the 
         * IO stream
         */
        protected CTPMessage receiveMessage() throws IOException {
            Log.info("[CTPServiceTest]receivemessage");
            if (network) {
                // Wait until we receive the OK
                while (!receiveOk) {
                    Thread.yield();
                    if (!network || closed) {
                        closed = false;
                        simulateNetworkError();
                    }
                }
                receiveOk = false;
                CTPMessage ctpmsg = new CTPMessage();
                ctpmsg.setCommand(ST_OK);
                return ctpmsg;
            } else {
                simulateNetworkError();
                return null;
            }
        }

        protected void sendMessage(CTPMessage message) throws IOException {
            Log.info("[CTPServiceTest]sendmessage");
            if (network) {
                if (message.getCommand() == CM_READY) {
                    receiveOk = true;
                }
                Thread.yield();
            } else {
                simulateNetworkError();
            }
        }

        protected CTPMessage receiveMessageWithTimeout() throws IOException {
            Log.info("[CTPServiceTest]receivemessagewithTO");
            if (network) {
                Thread.yield();
            } else {
                simulateNetworkError();
            }
            return new CTPMessage();
        }

        public int getAuthAttempt() {
            return authAttempt;
        }

        public int getConnAttempt() {
            return connectionAttempt;
        }

        protected int authenticate() throws IOException {
            authAttempt++;
            Log.info("[CTPServiceTest]authenticate: " + authAttempt);
            if (network) {
                state = AUTHENTICATED;
                Thread.yield();
            } else {
                simulateNetworkError();
            }
            return authResult;
        }

        public void simulateNetworkLoss(int timeout) {
            network = false;
            this.timeout = timeout;
        }

        public void restoreNetwork() {
            network = true;
        }

        public void simulateAuthenticationResult(int authResult) {
            switch (authResult) {
                case UNAUTHORIZED:
                    this.authResult = ST_UNAUTHORIZED;
                    break;
                case FORBIDDEN:
                    this.authResult = ST_FORBIDDEN;
                    break;
                default:
                    this.authResult = authResult;
                    break;
            }
        }
    }

    public void CTPDisconnected() {
        Log.info("[CTPServiceTest-listener]Disconnected");
    }

    public void CTPConnecting() {
        Log.info("[CTPServiceTest-listener]Connecting");
    }

    public void CTPConnected() {
        Log.info("[CTPServiceTest-listener]Connected");
    }

    public void CTPAuthenticating() {
        Log.info("[CTPServiceTest-listener]Authenticating");
    }

    public void CTPAuthenticated() {
        Log.info("[CTPServiceTest-listener]Authenticated");
    }

    public void CTPListening() {
        Log.info("[CTPServiceTest-listener]Listening");
    }
}
