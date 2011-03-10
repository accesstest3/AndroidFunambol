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
package com.funambol.updater;

import java.util.Calendar;
import java.util.Date;
import com.funambol.util.Log;
import com.funambol.util.ConsoleAppender;
import com.funambol.util.TransportAgent;
import junit.framework.*;

public class UpdaterTest extends TestCase {
    private Updater updater;
    private TestTransportAgent ta;
    private TestUpdaterConfig  config;
    private final String version = "1.0.0";

    class TestTransportAgent implements TransportAgent {

        private String response = null;

        public String sendMessage(String text, String charset) {
            return sendMessage(text);
        }

        public String sendMessage(String text) {
            return response;
        }

        public void setRetryOnWrite(int retries) {
        }

        public void setResponse(String text) {
            response = text;
        }

        public void setRequestURL(String requestUrl) {
            // Not used in this test
        }

        public String getResponseDate() {
            // Not used in this test
            return null;
        }

    }

    class TestUpdaterConfig extends BasicUpdaterConfig {
        public void save() {
        }

        public void load() {
        }
    }

    class TestUpdaterListener implements UpdaterListener {

        private String newVersion = null;
        private boolean optional  = false;
        private boolean recommended  = false;
        private boolean mandatory = false;

        public void mandatoryUpdateAvailable(String newVersion) {
            this.newVersion = newVersion;
            mandatory = true;
        }

        public void optionalUpdateAvailable(String newVersion) {
            this.newVersion = newVersion;
            optional = true;
        }
        
        public void recommendedUpdateAvailable(String newVersion) {
            this.newVersion = newVersion;
            recommended = true;
        }

        public boolean isOptional() {
            return optional;
        }

        public boolean isMandatory() {
            return mandatory;
        }

        public boolean isRecommended() {
            return recommended;
        }

        public String getNewVersion() {
            return newVersion;
        }

        public void reset() {
            newVersion = null;
            mandatory  = false;
            optional   = false;
            recommended   = false;
        }
    }

    public UpdaterTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.TRACE);
    }

    public void setUp() {
        // Set up a basic configuration
        ta      = new TestTransportAgent();
        config  = new TestUpdaterConfig();
        updater = new Updater(config, version, ta);
    }

    // Test a check where no info is available on the server
    // We do not expect any change in the config
    public void testFirstCheck() throws Exception {
        assertTrue(!config.isMandatory());
        assertTrue(!config.isOptional());
        assertTrue(!config.isRecommended());
        // Now prepare the response (simulate an empty one)
        String response = "";
        ta.setResponse(response);
        // Set a listener
        TestUpdaterListener lis = new TestUpdaterListener();
        updater.setListener(lis);
        // Do the update check
        updater.check();
        // We expect the updater to report a no new version of any kind
        assertTrue(!lis.isOptional());
        assertTrue(!lis.isMandatory());
        assertTrue(!lis.isRecommended());
        assertTrue(lis.getNewVersion() == null);
        assertTrue(config.getLastCheck() > 0);
    }

    private String createResponse(String version, String type, String activation,
                                  String size, String url) {

        StringBuffer response = new StringBuffer();
        response.append("version=").append(version).append("\n\r");
        response.append("type=").append(type).append("\n\r");
        if(activation != null) {
            response.append("activation-date=").append(activation).append("\n\r");
        }
        response.append("size=").append(size).append("\n\r");
        response.append("url=").append(url).append("\n\r");
        return response.toString();
    }

    private void resetConfig() {
        config.setLastCheck(0);
        config.setLastReminder(0);
        config.setAvailableVersion(null);
        config.setType(null);
        config.setSkip(false);
        config.setUrl("http://fakeaddress.com");
        config.setCheckInterval((long)(3600 * 1000));
        config.setReminderInterval((long)(60 * 1000));
    }

    // 1) Perform a check on the server
    // 2) we expect an optional update reported to the client
    public void testNewOptional() throws Exception {
        // Now prepare the response (simulate an empty one)
        String version  = new String("1.0.1");
        String type     = new String("optional");
        String size     = new String("467");
        String url      = new String("url=http://fakeaddress.com:9080/bbpim/test.jad");
        String response = createResponse(version, type, null, size, url);
        ta.setResponse(response);
        // Set a listener
        TestUpdaterListener lis = new TestUpdaterListener();
        updater.setListener(lis);
        long now = System.currentTimeMillis();
        // Force the last check timestamp to force check on server
        resetConfig();
        // Do the update check
        updater.check();
        // We expect the updater to report a new optional version
        assertTrue(lis.isOptional());
        assertTrue(!lis.isMandatory());
        assertTrue(!lis.isRecommended());
        long lastRemind = System.currentTimeMillis();
        config.setLastReminder(lastRemind);
        assertTrue(version.equals(lis.getNewVersion()));
        assertTrue(config.getLastCheck() >= now);
        assertTrue(config.getLastReminder() == lastRemind);
        assertTrue(updater.isUpdateAvailable());
    }

    // 1) Perform a check on the server
    // 2) we expect a mandatory update reported to the client
    public void testNewMandatory() throws Exception {
        // Now prepare the response (simulate an empty one)
        String version  = new String("1.0.1");
        String type     = new String("mandatory");
        String activation = new String("20080731");
        String size     = new String("467");
        String url      = new String("url=http://fakeaddress.com:9080/bbpim/test.jad");
        String response = createResponse(version, type, activation, size, url);
        ta.setResponse(response);
        // Set a listener
        TestUpdaterListener lis = new TestUpdaterListener();
        updater.setListener(lis);
        long now = System.currentTimeMillis();
        // Force the last check timestamp to force check on server
        resetConfig();
        // Do the update check
        updater.check();
        // We expect the updater to report a new mandatory version
        assertTrue(!lis.isOptional());
        assertTrue(!lis.isRecommended());
        assertTrue(lis.isMandatory());
        //Check the activation date
        Calendar date = Calendar.getInstance();
        date.setTime(new Date(config.getActivationDate()));

        assertTrue(date.get(Calendar.DAY_OF_MONTH) == 31);
        assertTrue(date.get(Calendar.MONTH) == 6);
        assertTrue(date.get(Calendar.YEAR) == 2008);

        long lastRemind = System.currentTimeMillis();
        config.setLastReminder(lastRemind);
        assertTrue(version.equals(lis.getNewVersion()));
        assertTrue(config.getLastCheck() >= now);
        assertTrue(config.getLastReminder() == lastRemind);
        assertTrue(updater.isUpdateAvailable());
    }

    // 1) Perform a check on the server
    // 2) check again
    // 3) we expect no check on the server and no update reported to the client
    public void testTwoUpdates() throws Exception {
        // Now prepare the response (simulate an empty one)
        String version  = new String("1.0.1");
        String type     = new String("mandatory");
        String activation = new String("20080731");
        String size     = new String("467");
        String url      = new String("url=http://fakeaddress.com:9080/bbpim/test.jad");
        String response = createResponse(version, type, activation, size, url);
        ta.setResponse(response);
        // Set a listener
        TestUpdaterListener lis = new TestUpdaterListener();
        updater.setListener(lis);
        long now = System.currentTimeMillis();
        // Force the last check timestamp to force check on server
        resetConfig();
        // Do the update check
        updater.check();
        // We expect the updater to report a new mandatory version
        assertTrue(!lis.isOptional());
        assertTrue(!lis.isRecommended());
        assertTrue(lis.isMandatory());
        assertTrue(updater.isUpdateAvailable());
        long lastRemind = System.currentTimeMillis();
        config.setLastReminder(lastRemind);
        assertTrue(version.equals(lis.getNewVersion()));
        assertTrue(config.getLastCheck() >= now);
        assertTrue(config.getLastReminder() == lastRemind);
        // Do the update check again
        long lastCheck = config.getLastCheck();
        updater.check();
        assertTrue(config.getLastCheck() == lastCheck);
        assertTrue(updater.isUpdateAvailable());
    }

    // 1) perform a check on the server
    // 2) perform another check
    // 3) since the check interval is 1 millisec we expect a new check on the server
    //    but no version reported to the client (remind interval i 1 minute)
    public void testTwoDistantUpdates() throws Exception {
        // Now prepare the response (simulate an empty one)
        String version  = new String("1.0.1");
        String type     = new String("mandatory");
        String delivery = new String("20080731");
        String size     = new String("467");
        String url      = new String("url=http://fakeaddress.com:9080/bbpim/test.jad");
        String response = createResponse(version, type, delivery, size, url);
        ta.setResponse(response);
        // Set a listener
        TestUpdaterListener lis = new TestUpdaterListener();
        updater.setListener(lis);
        long now = System.currentTimeMillis();
        // Force the last check timestamp to force check on server
        resetConfig();
        long checkInterval = config.getCheckInterval();
        // Set the interval check very low
        config.setCheckInterval(1);
        // Do the update check
        updater.check();
        // We expect the updater to report a new mandatory version
        assertTrue(!lis.isOptional());
        assertTrue(!lis.isRecommended());
        assertTrue(lis.isMandatory());
        assertTrue(updater.isUpdateAvailable());
        long lastRemind = System.currentTimeMillis();
        config.setLastReminder(lastRemind);
        assertTrue(version.equals(lis.getNewVersion()));
        assertTrue(config.getLastCheck() >= now);
        assertTrue(config.getLastReminder() == lastRemind);
        // Do the update check again
        lis = new TestUpdaterListener();
        updater.setListener(lis);
        long lastCheck = config.getLastCheck();
        updater.check();
        // A new server check must have been performed
        // but the available version did not change, so the
        // listener must not have been invoked
        assertTrue(config.getLastCheck() != lastCheck);
        assertTrue(!lis.isOptional());
        assertTrue(!lis.isMandatory());
        assertTrue(!lis.isRecommended());
        assertTrue(version.equals(config.getAvailableVersion()));
        assertTrue(updater.isUpdateAvailable());
    }

    // 1) perform a check on the server
    // 2) perform another check
    // 3) since the intervals are 1 millisec we expect a new check on the server
    //    a new version reported to the client
    public void testTwoDistantUpdatesWithReminder() throws Exception {
        // Now prepare the response (simulate an empty one)
        String version  = new String("1.0.1");
        String type     = new String("recommended");
        String activation = new String("20080731");
        String size     = new String("467");
        String url      = new String("url=http://fakeaddress.com:9080/bbpim/test.jad");
        String response = createResponse(version, type, activation, size, url);
        ta.setResponse(response);
        // Set a listener
        TestUpdaterListener lis = new TestUpdaterListener();
        updater.setListener(lis);
        long now = System.currentTimeMillis();
        // Force the last check timestamp to force check on server
        resetConfig();
        // Set the interval check very low
        config.setCheckInterval(1);
        config.setReminderInterval(1);
        // Do the update check
        updater.check();
        // We expect the updater to report a new recommended version
        assertTrue(!lis.isOptional());
        assertTrue(!lis.isMandatory());
        assertTrue(lis.isRecommended());
        assertTrue(updater.isUpdateAvailable());
        long lastRemind = System.currentTimeMillis();
        config.setLastReminder(lastRemind);
        assertTrue(version.equals(lis.getNewVersion()));
        assertTrue(config.getLastCheck() >= now);
        assertTrue(config.getLastReminder() == lastRemind);
        // Do the update check again
        lis = new TestUpdaterListener();
        updater.setListener(lis);
        long lastCheck = config.getLastCheck();
        updater.check();
        // A new server check must have been performed
        // but the available version did not change, so the
        // listener must not have been invoked
        assertTrue(config.getLastCheck() != lastCheck);
        assertTrue(!lis.isOptional());
        assertTrue(!lis.isMandatory());
        assertTrue(lis.isRecommended());
        assertTrue(version.equals(config.getAvailableVersion()));
        assertTrue(updater.isUpdateAvailable());
    }

    // 1) perform a check on the server
    // 2) set skip
    // 3) perform another check and the server must be queried but no
    //    version reported to the client
    public void testSkip() throws Exception {
        // Now prepare the response (simulate an empty one)
        String version  = new String("1.0.1");
        String type     = new String("optional");
        String activation = new String("20080731");
        String size     = new String("467");
        String url      = new String("url=http://fakeaddress.com:9080/bbpim/test.jad");
        String response = createResponse(version, type, activation, size, url);
        ta.setResponse(response);
        // Set a listener
        TestUpdaterListener lis = new TestUpdaterListener();
        updater.setListener(lis);
        long now = System.currentTimeMillis();
        // Force the last check timestamp to force check on server
        resetConfig();
        // Set the interval check very low
        config.setCheckInterval(1);
        config.setReminderInterval(1);
        // Do the update check
        updater.check();
        // We expect the updater to report a new mandatory version
        assertTrue(lis.isOptional());
        assertTrue(!lis.isRecommended());
        assertTrue(!lis.isMandatory());
        assertTrue(updater.isUpdateAvailable());
        long lastRemind = System.currentTimeMillis();
        config.setLastReminder(lastRemind);
        assertTrue(version.equals(lis.getNewVersion()));
        assertTrue(config.getLastCheck() >= now);
        assertTrue(config.getLastReminder() == lastRemind);
        config.setSkip(true);
        // Do the update check again
        lis = new TestUpdaterListener();
        updater.setListener(lis);
        long lastCheck = config.getLastCheck();
        updater.check();
        // A new server check must have been performed
        // but the available version did not change, so the
        // listener must not have been invoked
        assertTrue(config.getLastCheck() != lastCheck);
        assertTrue(!lis.isOptional());
        assertTrue(!lis.isMandatory());
        assertTrue(!lis.isRecommended());
        assertTrue(version.equals(config.getAvailableVersion()));
        assertTrue(updater.isUpdateAvailable());
    }

    // 1) perform a check on the server
    // 2) set skip
    // 3) perform another check and the server must be queried. A new version is
    //    reported even if the client set the skip flag because yet another
    //    newer version is present
    public void testSkip2() throws Exception {
        // Now prepare the response (simulate an empty one)
        String version  = new String("1.0.1");
        String type     = new String("optional");
        String activation = new String("20080731");
        String size     = new String("467");
        String url      = new String("url=http://fakeaddress.com:9080/bbpim/test.jad");
        String response = createResponse(version, type, activation, size, url);
        ta.setResponse(response);
        // Set a listener
        TestUpdaterListener lis = new TestUpdaterListener();
        updater.setListener(lis);
        long now = System.currentTimeMillis();
        // Force the last check timestamp to force check on server
        resetConfig();
        // Set the interval check very low
        config.setCheckInterval(1);
        config.setReminderInterval(1);
        // Do the update check
        updater.check();
        // We expect the updater to report a new mandatory version
        assertTrue(lis.isOptional());
        assertTrue(!lis.isMandatory());
        assertTrue(!lis.isRecommended());
        long lastRemind = System.currentTimeMillis();
        config.setLastReminder(lastRemind);
        assertTrue(version.equals(lis.getNewVersion()));
        assertTrue(config.getLastCheck() >= now);
        assertTrue(config.getLastReminder() == lastRemind);
        config.setSkip(true);
        // Do the update check again
        String version2  = new String("1.0.2");
        String type2     = new String("mandatory");
        String activation2 = new String("20080731");
        String size2     = new String("467");
        String url2      = new String("url=http://fakeaddress.com:9080/bbpim/test.jad");
        String response2 = createResponse(version2, type2, activation2, size2, url2);
        ta.setResponse(response2);
 
        lis = new TestUpdaterListener();
        updater.setListener(lis);
        long lastCheck = config.getLastCheck();
        updater.check();
        // A new server check must have been performed
        // but the available version did not change, so the
        // listener must not have been invoked
        assertTrue(config.getLastCheck() != lastCheck);
        assertTrue(!lis.isOptional());
        assertTrue(!lis.isRecommended());
        assertTrue(lis.isMandatory());
        assertTrue(version2.equals(config.getAvailableVersion()));
        assertTrue(updater.isUpdateAvailable());
    }

    // 1) perform a check on the server
    // 2) set skip
    // 3) perform another check and the server must be queried. A new version is
    //    reported even if the client set the skip flag because yet another
    //    newer version is present
    public void testSkip3() throws Exception {
        // Now prepare the response (simulate an empty one)
        String version  = new String("1.0.1");
        String type     = new String("optional");
        String activation = new String("20080731");
        String size     = new String("467");
        String url      = new String("url=http://fakeaddress.com:9080/bbpim/test.jad");
        String response = createResponse(version, type, activation, size, url);
        ta.setResponse(response);
        // Set a listener
        TestUpdaterListener lis = new TestUpdaterListener();
        updater.setListener(lis);
        long now = System.currentTimeMillis();
        // Force the last check timestamp to force check on server
        resetConfig();
        // Set the interval check very low
        config.setCheckInterval(1);
        // Do the update check
        updater.check();
        // We expect the updater to report a new mandatory version
        assertTrue(lis.isOptional());
        assertTrue(!lis.isRecommended());
        assertTrue(!lis.isMandatory());
        long lastRemind = System.currentTimeMillis();
        config.setLastReminder(lastRemind);
        assertTrue(version.equals(lis.getNewVersion()));
        assertTrue(config.getLastCheck() >= now);
        assertTrue(config.getLastReminder() == lastRemind);
        assertTrue(updater.isUpdateAvailable());
        config.setSkip(true);
        // Do the update check again
        String version2  = new String("1.0.2");
        String type2     = new String("mandatory");
        String activation2 = new String("20080731");
        String size2     = new String("467");
        String url2      = new String("url=http://fakeaddress.com:9080/bbpim/test.jad");
        String response2 = createResponse(version2, type2, activation2, size2, url2);
        ta.setResponse(response2);
 
        lis = new TestUpdaterListener();
        updater.setListener(lis);
        long lastCheck = config.getLastCheck();
        updater.check();
        // A new server check must have been performed
        // but the available version did not change, so the
        // listener must not have been invoked
        assertTrue(config.getLastCheck() != lastCheck);
        assertTrue(!lis.isOptional());
        assertTrue(!lis.isRecommended());
        assertTrue(!lis.isMandatory());
        assertTrue(version2.equals(config.getAvailableVersion()));
        assertTrue(updater.isUpdateAvailable());
    }

    // 1) perform a check on the server with a version newer than what is
    //    available on the server
    // 2) we expect no notification to the client
    public void testCheckNewerVersion() throws Exception {
        // Now prepare the response (simulate an empty one)
        String version  = new String("0.9.2");
        String type     = new String("optional");
        String activation = new String("20080731");
        String size     = new String("467");
        String url      = new String("url=http://fakeaddress.com:9080/bbpim/test.jad");
        String response = createResponse(version, type, activation, size, url);
        ta.setResponse(response);
        // Set a listener
        TestUpdaterListener lis = new TestUpdaterListener();
        updater.setListener(lis);
        long now = System.currentTimeMillis();
        // Force the last check timestamp to force check on server
        resetConfig();
        // Set the interval check very low
        config.setCheckInterval(1);
        // Do the update check
        updater.check();
        assertTrue(!lis.isOptional());
        assertTrue(!lis.isRecommended());
        assertTrue(!lis.isMandatory());
        long lastRemind = System.currentTimeMillis();
        config.setLastReminder(lastRemind);
        assertTrue(config.getLastCheck() >= now);
        assertTrue(config.getLastReminder() == lastRemind);
        assertTrue(!updater.isUpdateAvailable());
    }
}


