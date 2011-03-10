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

package com.funambol.client.controller;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.engine.Poller;

/**
 * This is the class which handles the sync mode set by the user.
 */
public class SyncModeHandler {

    /** The Poller used to schedule periodic syncs */
    private Poller poller = null;

    protected Configuration configuration;

    public SyncModeHandler(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Handles the given sync mode
     * @param mode
     * @param controller
     */
    public void setSyncMode(Controller controller) {

        int mode = configuration.getSyncMode();

        if(mode == Configuration.SYNC_MODE_MANUAL ||
           mode == Configuration.SYNC_MODE_PUSH) {
            // Nothing to do, just stop the current poller if running
            stopPoller();
        } else if(mode == Configuration.SYNC_MODE_SCHEDULED) {
            if (poller != null && poller.isAlive()) {
                if(poller.getInterval() != configuration.getPollingInterval()) {
                    restartPoller(controller);
                }
            } else {
                startPoller(controller);
            }
        }
    }

    /**
     * Handles the current sync mode
     * @param controller
     */
    //public void handleCurrentSyncMode(Controller controller) {
    //    handleSyncMode(getCurrentSyncMode(), controller);
    //}

    /**
     * Get the current sync mode from the configuration
     * @return
     */
    //public int getCurrentSyncMode() {
    //    return configuration.getSyncMode();
    //}

    /**
     * Set the current sync mode
     * @param mode
     */
    //public void setCurrentSyncMode(int mode) {
    //    configuration.setSyncMode(mode);
    //}

    /**
     * Stops the current poller instance if running
     */
    private void stopPoller() {
        if (poller != null && poller.isAlive()) {
            poller.disable();
            poller = null;
        }
        // Reset the polling timestamp
        //configuration.setPollingTimestamp(0);
        //configuration.save();
    }

    /**
     * Starts a new poller instance
     */
    private void startPoller(Controller controller) {
        if (poller != null && poller.isAlive()) {
            stopPoller();
        }
        poller = new Poller(controller.getHomeScreenController(),
                            configuration.getPollingInterval(),
                            true, true);
        poller.start();
    }

    /**
     * Restarts the current poller instance
     */
    private void restartPoller(Controller controller) {
        stopPoller();
        startPoller(controller);
    }
}
