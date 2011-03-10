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

package com.funambol.client.engine;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Date;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.controller.SynchronizationController;
import com.funambol.util.Log;

/**
 * This class can be used to trigger sync requests at periodic intervals.
 * The poller can be enabled/disabled dynamically.
 * SyncRequests are invoked via the SynchronizationController which is passed in
 * the costructor.
 * By default this class invokes a sync for all the sources that are part of the
 * sync all group and are enabled, but the set can be restricted by specifying a 
 * different subset.
 *
 * The Poller can be used in 2 different cases:
 *
 * 1) to trigger scheduled syncs (for the enabled sources)
 * 2) to trigger retry syncs on errors for which a retry attempt must be
 *    performed
 *
 * The parameter handleTimestamp tells if this Poller should handle the
 * timestamp (set into the <code>Configuration</code>) within which the Poller
 * shall be triggered.
 * 
 */
public class Poller extends Thread {

    private static final String TAG_LOG = "Poller";

    private AppSyncSourceManager      appSyncSourceManager = null;
    private Configuration             configuration   = null;
    private SynchronizationController syncController  = null;
    private boolean                   enabled         = false;
    private boolean                   handleTimestamp = false;
    private int                       interval        = 0;
    private Vector                    sources         = null;

    public Poller(SynchronizationController ctrl, int interval, boolean enabled,
                  boolean handleTimestamp)
    {
        this.syncController  = ctrl;
        this.interval        = interval * 1000 * 60;
        this.enabled         = enabled;
        this.handleTimestamp = handleTimestamp;
    }

    public void disable() {
        enabled = false;
        this.interrupt();
    }

    public void enable() {
        enabled = true;
    }

    public int getInterval() {
        return interval/(60*1000);
    }

    public void run() {
        while (enabled) {
            long sleepTime = interval;
            if(handleTimestamp) {
                long timestamp = configuration.getPollingTimestamp();
                long now = System.currentTimeMillis();
                Log.debug(TAG_LOG, "Scheduled sync timestamp: " +
                        new Date(configuration.getPollingTimestamp()));
                if(timestamp == 0) {
                    // First run
                    timestamp = now + interval;
                    configuration.setPollingTimestamp(timestamp);
                    configuration.save();
                    Log.debug(TAG_LOG, "First sync scheduled at: " +
                        new Date(configuration.getPollingTimestamp()));
                }
                if(timestamp <= now) {
                    // timestamp expired
                    sleepTime = (now-timestamp)%interval;
                    configuration.setPollingTimestamp(now + sleepTime);
                    configuration.save();
                    Log.debug("[Poller] Expired sync rescheduled at: " +
                        new Date(configuration.getPollingTimestamp()));
                } else {
                    sleepTime = timestamp-now;
                }
            }
            try {
                Log.debug(TAG_LOG, "Waiting " + sleepTime/1000 + " seconds for the next sync");
                Thread.sleep(sleepTime);
                if(handleTimestamp) {
                    // If the delay ends without interrupts we have to update the
                    // timestamp of the next scheduled sync.
                    configuration.setPollingTimestamp(configuration.getPollingTimestamp() + interval);
                    configuration.save();
                    Log.debug(TAG_LOG, "Next sync scheduled at: " +
                            new Date(configuration.getPollingTimestamp()));
                }
            } catch (final InterruptedException ie) {
                Log.debug(TAG_LOG, "InterruptedException of the sleeping poller thread--\n" + ie.toString());
            }
            Vector sources = new Vector();
            // We must sync all the sources but the config sync source
            Enumeration sourcesIt = appSyncSourceManager.getEnabledAndWorkingSources();
            while (sourcesIt.hasMoreElements()) {
                AppSyncSource source = (AppSyncSource)sourcesIt.nextElement();
                if (source.getId() != AppSyncSourceManager.CONFIG_ID) {
                    sources.addElement(source);
                }
            }
            if (!syncController.isSynchronizing() && enabled) {
                syncController.synchronize(SynchronizationController.SCHEDULED, sources);
            }
        }
    }
}
