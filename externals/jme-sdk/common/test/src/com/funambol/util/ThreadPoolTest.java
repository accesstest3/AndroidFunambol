/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2003 - 2007 Funambol, Inc.
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

package com.funambol.util;

import junit.framework.*;


/**
 * Testing the ThreadPool implementation.
 */
public class ThreadPoolTest extends TestCase {
    
    private int TEST_NUM_THREADS = 10;
    private int THREAD_MAX_SIZE = 5;
    private ThreadPool threadPool;
    
    public ThreadPoolTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.TRACE);
        threadPool = new ThreadPool(THREAD_MAX_SIZE);
    }
    
    /**
     * Try to start concurrently 10 threads while the max
     * number of threads of the pool (queue) has been configured to 5.
     * As soon as a thread ends a new thread starts from the queue.
     */
    public void testThreadPool() throws AssertionFailedError {
        
        for (int i=0; i<TEST_NUM_THREADS; i++) {
            Log.debug("Request starting thread n. " + i);
            threadPool.startThread(new MyRunnableClass());
        }
        
        // assertion not really useful due to threads usage
        assertTrue(true);
    }

    /**
     * Start a new thread and check that the number of running threads
     * is properly updated.
     */
    public void testSingleThread() throws AssertionFailedError {

        Log.debug("Test:::Test Single Thread");
        int initialRunning = threadPool.getRunnableCount();
        Log.debug("Test:::Initial number of threads = " + initialRunning);
        Thread tr = threadPool.startThread(new MyRunnableClass());
        int running = threadPool.getRunnableCount();
        Log.debug("Test:::Number of threads running = " + running);
        try {
            tr.join();
        } catch (InterruptedException ex) {
        }
        int finalRunning = threadPool.getRunnableCount();
        Log.debug("Test:::Final number of threads = " + finalRunning);

        assertTrue(initialRunning == finalRunning);
        assertTrue(initialRunning + 1 == running);
        
    }
    
    /**
     * Example of runnable class to be added to the queue
     * and later started from the queue
     */
    public class MyRunnableClass extends Thread {
        private long waitPeriod = 10000;
        
        MyRunnableClass() {
        }
        
        /**
         * Simple work for this sample thread: just waiting 10 seconds
         */
        public void run() {
            Log.debug("Thread " + this.currentThread() + " runs");
            try {
                Thread.sleep(waitPeriod);
            } catch(Exception e) {
                
            }
            Log.debug("Thread " + this.currentThread() + " ends");
        }
        
    }
}
