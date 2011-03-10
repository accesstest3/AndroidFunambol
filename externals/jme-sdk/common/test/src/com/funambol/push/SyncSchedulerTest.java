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

import junit.framework.*;

/**
 * This is a test for the SyncScheduler class.
 */
public class SyncSchedulerTest extends TestCase {

    //Change the following if you want to speed up the tests
    public static final long  ONE_SECOND = 100; // in milliseconds

    private final long ONE_MINUTE = ONE_SECOND*60;

    //Sync wait delay
    private final long WAIT_DELAY = ONE_SECOND/10;
    private final long[] RETRY_DELAY = {ONE_MINUTE, 
                                        ONE_MINUTE*5,
                                        ONE_MINUTE*15};

    //Specify the tolerable delay of a sync start
    private final long TOLERATED_DELAY = ONE_SECOND*30;

    private final long UI_CALLER_INTERVAL        = 0;
    private final long LISTENER_CALLER_INTERVAL  = ONE_MINUTE;
    private final long SCHEDULER_CALLER_INTERVAL = 0;

    private final int CONTACT_SS = 0;
    private final int EVENT_SS = 1;
    private final int TASK_SS = 2;
    private final int NOTE_SS = 3;
    private final int PHOTO_SS = 4;

    private SyncScheduler scheduler;
    private TestSyncSchedulerListener slistener;
    private TestSyncRequest request;
    
    private boolean assertingSync = false;

    /** Creates a new instance of SyncSchedulerTest */
    public SyncSchedulerTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender(), Log.TRACE);
    }

    public void setUp() {
        slistener = new TestSyncSchedulerListener(5*ONE_SECOND);
        scheduler = new SyncScheduler(slistener);
    }

    public void tearDown() {
        scheduler = null;
    }

    /** Test a single SyncRequest */
    public void testSingleRequest(Object item, long interval) throws Exception {

        Log.debug("==== Running testSingleRequest test. Interval="
                + interval + " ====");
                

        Object[] content  = {item};

        // setting up a sync request
        request = new TestSyncRequest(content[0], interval);

        scheduler.addRequest(request);
        Log.debug("Sync Request added");

        assertSync(content, interval);
    }

    /** Test multiple SyncRequests of the same type, from the same caller */
    public void testMultipleRequests(Object item, int num, long delay, long interval)
                   throws Exception {

        Log.debug("==== Running testMultipleRequests test. Interval="
                + interval + " ====");
                
        Object[] content = {item};

        // setting up a sync request
        request = new TestSyncRequest(content[0], interval);

        for(int i=0; i<num; i++) {
            Thread.sleep(delay);
            scheduler.addRequest(request);
            Log.debug("Sync Request added");
        }
        assertSync(content, interval);
    }

    /** Test two delayed (e.g. delay > 5min) SyncRequests */
    public void testLongDelayedRequests(Object item, long delay, long interval)
                   throws Exception {

        Log.debug("==== Running testLongDelayedRequests test. Interval="
                + interval + " ====");

        Object[] content = {item};

        // setting up a sync request
        request = new TestSyncRequest(content[0], interval);

        scheduler.addRequest(request);
        Log.debug("Sync Request added");
        assertSyncWithThread(content, interval);

        Log.debug("Waiting " + delay/1000 + " seconds...");
        Thread.sleep(delay);
        
        scheduler.addRequest(request);
        Log.debug("Sync Request added");
        assertSync(content, interval);
    }
    
    /** Test two short delayed (e.g. delay = 30 sec) SyncRequests */
    public void testShortDelayedRequests(Object item, long delay, long interval)
                   throws Exception {

        Log.debug("==== Running testShortDelayedRequests test. Interval="
                + interval + " ====");

        Object[] content = {item};

        // setting up a sync request
        request = new TestSyncRequest(content[0], interval);

        scheduler.addRequest(request);
        Log.debug("Sync Request added");
        Thread.sleep(delay);
        scheduler.addRequest(request);
        Log.debug("Sync Request added");
        assertSync(content, interval);
    }

    /**
     * Test mixed SyncRequests case #1
     *  - test a sync request of all the sources from UI
     */
    public void testMixedRequests1() throws Exception {

        Log.debug("==== Running testMixedRequests1 test ====");

        // set a SyncSchedulerListener with 0 failures and 5 seconds duration

        Object[] content = {getTestSyncSource(CONTACT_SS),
                            getTestSyncSource(EVENT_SS),
                            getTestSyncSource(TASK_SS),
                            getTestSyncSource(NOTE_SS),
                            getTestSyncSource(PHOTO_SS)};

        request = new TestSyncRequest(content[0], UI_CALLER_INTERVAL);
        for(int i=1; i<content.length; i++) {
            request.addRequestContent(content[i]);
        }
        scheduler.addRequest(request);
        Log.debug("Sync Request added");
        assertSync(content, UI_CALLER_INTERVAL);
    }

    /**
     * Test mixed SyncRequests case #2
     *  - 5 mergeable sync requests from listeners (3 contacts, 2 notes)
     *    with 1 second delay.
     */
    public void testMixedRequests2() throws Exception {

        Log.debug("==== Running testMixedRequests2 test ====");

        Object contact_ss = getTestSyncSource(CONTACT_SS);
        Object note_ss = getTestSyncSource(NOTE_SS);

        slistener.setDuration(ONE_SECOND);

        Object[] content = {contact_ss,
                            contact_ss,
                            contact_ss,
                            note_ss,
                            note_ss};

        Object[] expcontent = {contact_ss, note_ss};

        for(int i=0; i<content.length; i++) {
            // setting up a sync request
            //
            request = new TestSyncRequest(content[i], LISTENER_CALLER_INTERVAL);
            Thread.sleep(ONE_SECOND);
            scheduler.addRequest(request);
            Log.debug("Sync Request added");
        }
        assertSync(expcontent, LISTENER_CALLER_INTERVAL );
    }

    /**
     * Test mixed SyncRequests case #3
     *  - 3 mergeable sync requests of contacts
     *  - 10min delay
     *  - 2 mergeable sync requests of notes
     */
    public void testMixedRequests3() throws Exception {

        Log.debug("==== Running testMixedRequests3 test ====");

        Object contact_ss = getTestSyncSource(CONTACT_SS);
        Object note_ss = getTestSyncSource(NOTE_SS);

        Object[] content1 = {contact_ss,
                             contact_ss,
                             contact_ss};

        Object[] expcontent1 = {contact_ss};

        Object[] content2 = {note_ss,
                             note_ss};

        Object[] expcontent2 = {note_ss};

        for(int i=0; i<content1.length; i++) {
            // setting up a sync request
            request = new TestSyncRequest(content1[i], LISTENER_CALLER_INTERVAL);
            Thread.sleep(ONE_SECOND);
            scheduler.addRequest(request);
            Log.debug("Sync Request added");
        }
        assertSyncWithThread(expcontent1, LISTENER_CALLER_INTERVAL);
        
        Log.debug("Waiting " + ONE_MINUTE*2/1000 + " seconds...");
        Thread.sleep(ONE_MINUTE*2);
        
        for(int i=0; i<content2.length; i++) {
            // setting up a sync request
            request = new TestSyncRequest(content2[i], LISTENER_CALLER_INTERVAL);
            Thread.sleep(ONE_SECOND);
            scheduler.addRequest(request);
            Log.debug("Sync Request added");
        }
        assertSync(expcontent2, LISTENER_CALLER_INTERVAL);
    }

    /**
     * Test mixed SyncRequests case #4
     *  - 3 mergeable sync requests of contacts
     *  - 60sec delay
     *  - 1 (mergeable) sync request of event
     *  - 1 (mergeable) sync request of photo
     */
    public void testMixedRequests4() throws Exception {

        Log.debug("==== Running testMixedRequests4 test ====");

        Object contact_ss = getTestSyncSource(CONTACT_SS);
        Object event_ss = getTestSyncSource(EVENT_SS);
        
        Object[] content1 = {contact_ss,
                             contact_ss,
                             contact_ss};

        Object[] content2 = {event_ss};

        Object[] expcontentNotMergeable1 = {contact_ss};
        Object[] expcontentNotMergeable2 = {event_ss};

        for(int i=0; i<content1.length; i++) {
            // setting up a sync request
            request = new TestSyncRequest(content1[i], LISTENER_CALLER_INTERVAL);
            Thread.sleep(ONE_SECOND);
            scheduler.addRequest(request);
            Log.debug("Sync Request added");
        }
        assertSyncWithThread(expcontentNotMergeable1, LISTENER_CALLER_INTERVAL);
        
        Log.debug("Waiting " + ONE_SECOND*60/1000 + " seconds...");
        Thread.sleep(ONE_SECOND*60);
        
        for(int i=0; i<content2.length; i++) {
            // setting up a sync request
            request = new TestSyncRequest(content2[i], LISTENER_CALLER_INTERVAL);
            Thread.sleep(ONE_SECOND);
            scheduler.addRequest(request);
            Log.debug("Sync Request added");
        }
        assertSync(expcontentNotMergeable2, LISTENER_CALLER_INTERVAL);
    }

    /**
     * Test mixed SyncRequests case #5
     *  - 1 sync request of contacts
     *  - 30sec delay
     *  - sync all sources from UI
     */
    public void testMixedRequests5() throws Exception {

        Log.debug("==== Running testMixedRequests5 test ====");

        Object contact_ss = getTestSyncSource(CONTACT_SS);

        Object[] content1 = {contact_ss};
     
        Object[] content2 = {contact_ss,
                            getTestSyncSource(EVENT_SS),
                            getTestSyncSource(TASK_SS),
                            getTestSyncSource(NOTE_SS),
                            getTestSyncSource(PHOTO_SS)};

        request = new TestSyncRequest(content1[0], LISTENER_CALLER_INTERVAL);
        scheduler.addRequest(request);
        Log.debug("Sync Request added");
        
        Log.debug("Waiting " + ONE_SECOND*3/100 + " seconds...");
        Thread.sleep(ONE_SECOND*30);
        
        request = new TestSyncRequest(content2[0], UI_CALLER_INTERVAL);
        for(int i=1; i<content2.length; i++) {
            request.addRequestContent(content2[i]);
        }
        scheduler.addRequest(request);
        Log.debug("Sync Request added");
        
        assertSync(content2, UI_CALLER_INTERVAL);
    }
    
    /**
     * Test mixed SyncRequests case #6
     *  - 1 sync request of contacts (sync after 1 minute delay)
     *  - 10sec delay
     *  - 1 not mergeable sync request of events (sync after 2 minutes delay)
     */
    public void testMixedRequests6() throws Exception {

        Log.debug("==== Running testMixedRequests6 test ====");

        Object contact_ss = getTestSyncSource(CONTACT_SS);
        Object event_ss = getTestSyncSource(EVENT_SS);

        Object[] content = {contact_ss, event_ss};
     
        request = new TestSyncRequest(content[0], ONE_MINUTE*1);
        scheduler.addRequest(request);
        Log.debug("Sync Request added");
        
        Log.debug("Waiting " + ONE_SECOND/100 + " seconds...");
        
        Thread.sleep(ONE_SECOND*10);
        
        request = new TestSyncRequest(content[1], ONE_MINUTE*2);
        scheduler.addRequest(request);
        Log.debug("Sync Request added");
        
        assertSync(content, ONE_MINUTE*2);
    }
    
    /**
     * Test conflict SyncRequests case #1
     *  - mergeable sync requests of events/tasks from UI (2min duration)
     *  - 30sec delay
     *  - mergaeble sync request of contacts from listener (start after 1min)
     *  - 1min delay
     *  - contacts sync from listener fails
     *  - retry after 5min ok
     */
    public void testConflictRequests1() throws Exception {

        Log.debug("==== Running testConflictRequests1 test ====");

        slistener.setDuration(5*ONE_SECOND);

        Object[] uiContent = {getTestSyncSource(EVENT_SS),
                              getTestSyncSource(TASK_SS)};

        Object[] listenerContent = {getTestSyncSource(CONTACT_SS)};

        // setting up a sync request
        request = new TestSyncRequest(uiContent[0], UI_CALLER_INTERVAL);
        request.addRequestContent(uiContent[1]);
        scheduler.addRequest(request);
        Log.debug("Sync Request added");

        assertSyncWithThread(uiContent, UI_CALLER_INTERVAL);
        
        Log.debug("Waiting " + ONE_SECOND*3/100 + " seconds...");
        Thread.sleep(ONE_SECOND*30);
        
        slistener.setDuration(5*ONE_SECOND);
        for(int i=0; i<listenerContent.length; i++) {
            // setting up a sync request
            request = new TestSyncRequest(listenerContent[i], LISTENER_CALLER_INTERVAL);
            Thread.sleep(ONE_SECOND);
            scheduler.addRequest(request);
            Log.debug("Sync Request added");
        }
        assertSync(listenerContent, LISTENER_CALLER_INTERVAL);
    }

    /**
     * It's expected that the SyncSchedulerListener syncs the expected content
     * within the expected delay.
     */
    private void assertSync(Object[] expectedContent, long expectedDelay)
                 throws Exception {
        
        long startTime = System.currentTimeMillis();

        // wait untill another assertSync is running
        while(assertingSync) {
            try {
                Thread.sleep(WAIT_DELAY);
            } catch(Exception e) {
                Log.error("Exception during thread sleep: " + e.toString());
            }
        }
        assertingSync = true;
        
        // wait untill the sync starts
        while(!slistener.getSyncStarted()) {
            try {
                Thread.sleep(WAIT_DELAY);
            } catch(Exception e) {
                Log.error("Exception during thread sleep: " + e.toString());
            }
        }

        // syncdelay represents the delay between the sync request and the effective
        // sync start
        long syncDelay = System.currentTimeMillis() - startTime;

        // verify the sync delay
        Log.debug("Verifying the sync delay=" + syncDelay +" (" + expectedDelay + " is expected)");
        assertTrue((syncDelay >= expectedDelay - TOLERATED_DELAY) &&
                   (syncDelay <= expectedDelay + TOLERATED_DELAY));

        // wait untill the sync ends
        while(!slistener.getSyncEnded()) {
            try {
                Thread.sleep(WAIT_DELAY);
            } catch(Exception e) {
                Log.error("Exception during thread sleep: " + e.toString());
            }
        }

        // check if the synced contents are the same of what is expected
        Log.debug("Verifying the synced content");
        Object[] syncedContent = slistener.getSyncedContent();
        int verifiedContentCount = 0;
        assertEquals(expectedContent.length, syncedContent.length);
        for(int i=0; i<expectedContent.length; i++) {
            for(int j=0; j<syncedContent.length; j++) {
                if(expectedContent[i].equals(syncedContent[j])) {
                    verifiedContentCount++;
                    break;
                }
            }
        }
        assertEquals(expectedContent.length, verifiedContentCount);
        Log.debug("Synced content verified.");
        assertingSync = false;
    }

    /** Start a new Thread for the assertSync check */
    private void assertSyncWithThread(final Object[] expectedContent, 
              final long expectedDelay) throws Exception {

        Thread t = new Thread() {
            public void run() {
                try {
                    assertSync(expectedContent, expectedDelay );
                } catch (Exception e) {
                }

            }
        };
        t.start();
    }

    /** Get a test SyncSource */
    private String getTestSyncSource(int ssid) {
        String res = "SyncSource_" + ssid;
        return res;
    }
}

