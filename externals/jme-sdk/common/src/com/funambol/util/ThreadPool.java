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

import java.util.Vector;

/**
 * ThreadPool manages threads which are created within the pool. The pool
 * provides two main features:
 *
 * 1) threads created by the pool can be monitored for runtime exceptions. If an
 *    exception is not handled by the client code then the exception raises and
 *    reaches the ThreadPool which will handle it through a ThreadPoolMonitor.
 *    The client can provide its own monitor and handle the exception, or a
 *    default monitor will be used (@see ThreadPoolMonitor). The default monitor
 *    simply logs the exception.
 * 
 * 2) checks if the number of concurrent threads (active) exceeds the threshold
 *    which is set at ThreadPool construction time. The ThreadPool does not
 *    enforce a restriction on the number of active threads. If the JVM cannot
 *    start a new thread the behavior is undefined. The ThreadPool logs the
 *    cases when the number of threads exceed the threshold. Then it tries to
 *    start the thread anyway. 
 */
public class ThreadPool {

    private final ThreadPoolMonitor monitor;
    private Vector threads;
    private int numberOfThreads;

    /**
     * This class represent all Threads (all but the main) which are created by
     * the application through the ThreadPool. This class is a Thread that in
     * its run method executes the run method of a Runnable object and waits for
     * it to finish. Its main purpose is to provide a poin where we can
     * intercept all runtime exceptions and invoke the proper ThreadPoolMonitor.
     */
    private class MonitorThread extends Thread {

        private Runnable task;

        private MonitorThread() {}

        public MonitorThread(Runnable task) {
            this.task = task;
        }

        public final void run() {
            try {
                // This is a blocking call. If an exception is thrown
                // by the executed code, we can catch it and handle it
                task.run();
            } catch (Throwable t) {
                Log.error(this, "throwable catched in run()");
                monitor.handleThrowable(this.getClass(), task, t);
            }
        }

        public String toString() {
            return "MonitorThread " + task;
        }
    }

    /**
     * Constructs a ThreadPool with the given monitor and the given maximum
     * number of concurrent threads
     *
     * @param monitor invoked in case of runtime exception
     * @param numberOfThreads maximum number of threads
     */
    public ThreadPool(ThreadPoolMonitor monitor, int numberOfThreads) {
        this.monitor = monitor;
        this.numberOfThreads = numberOfThreads;
        threads = new Vector();
    }
    
    /**
     * Constructs a ThreadPool with a default monitor (@see ThreadPoolMonitor)
     * and the given maximun number of concurrent threads
     *
     * @param numberOfThreads maximum number of threads
     */
    public ThreadPool(int numberOfThreads) {
        this(new ThreadPoolMonitor(), numberOfThreads);
    }

    /**
     * Returns the number of threads belonging to this pool that are currently
     * running (still active).
     * As a side effect the method also clears the references to objects which
     * are no longer active and returns an available slot in the Vector
     * containing references to threads running. Such as a slot may not exit and
     * in this case -1 is returned.
     *
     * @param availableIdx this is a return value, indicating an empty slot
     * where a new thread reference can be safely stored. -1 indicates such a
     * slot is not available
     * @return the number of currently active threads
     */
    private int getRunnableCount(int availableIdx[]) {
        int numRunning = 0;
        int idx = -1;
        for(int i=0;i<threads.size();++i) {
            MonitorThread tr = (MonitorThread)threads.elementAt(i);
            if (tr != null) {
                if (!tr.isAlive()) {
                    threads.setElementAt(null, i);
                    idx = i;
                } else {
                    Log.debug(tr.toString());
                    numRunning++;
                }
            }
        }
        availableIdx[0] = idx;
        return numRunning;
    }

    /**
     * Returns the number of threads belonging to this pool that are currently
     * running (still active)
     */
    public int getRunnableCount() {
        int availableIdx[] = new int[1];
        return getRunnableCount(availableIdx);
    }


    /**
     * Start a new task in a separate thread which is monitored by the object
     * monitor set in the constructor.
     * If there are no more threads available we just log an error and try to
     * create a new thread anyway. We could easily implement a waiting strategy
     * to wait for at least one running thread to finish, but this is not safe
     * as we may end up in some deadlocks.
     * As of today we mainly check that we never exceed the maximum number of
     * councurrent threads, this is why we simply log if we exceed this value
     * and then try to create the new thread anyway.
     *
     * @param task is the Runnable object to be executed in a new thread
     * @return the new thread that has been spawned
     */
    public synchronized Thread startThread(Runnable task) {
        MonitorThread thread = new MonitorThread(task);
        // Check if we are about to exceed the maximum number of threads
        // and nullify the references to threads that are no longer alive
        int availableIdx[] = new int[1];
        int numRunning = getRunnableCount(availableIdx);
        int idx = availableIdx[0];
        if (idx == -1) {
            threads.addElement(thread);
        } else {
            threads.setElementAt(thread, idx);
        }
        Log.debug("Number of running threads: " + numRunning);
        if (numRunning > numberOfThreads) {
            Log.error(numRunning + "/" + numberOfThreads + " running threads. " +
                    "About to exceed the max.");
        }
        // Finally start a thread
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        return thread;
    }
}
