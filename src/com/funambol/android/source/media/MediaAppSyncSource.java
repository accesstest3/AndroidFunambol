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

package com.funambol.android.source.media;

import com.funambol.android.AndroidAppSyncSource;

import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.syncml.client.TrackableSyncSource;
import com.funambol.syncml.client.ChangesTracker;
import com.funambol.client.controller.SynchronizationController;
import com.funambol.client.controller.UISyncSourceController;

import com.funambol.util.Log;

public class MediaAppSyncSource extends AndroidAppSyncSource {

    private static final String TAG_LOG = "MediaAppSyncSource";

    protected SyncSource syncMLSource;
    protected SyncSource twoPhasesSyncSource;

    public MediaAppSyncSource(String name, SyncSource source) {
        super(name, source);
        setIsMedia(true);
    }

    public MediaAppSyncSource(String name) {
        this(name, null);
    }

    @Override
    public int prepareRefresh(int direction) {
        int syncMode = 0;
        switch (direction) {
            case SynchronizationController.REFRESH_FROM_SERVER:
                throw new IllegalArgumentException("Invalid refresh direction " + direction);
            case SynchronizationController.REFRESH_TO_SERVER:
                syncMode = SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT_NO_SLOW;
                break;
            default:
                throw new IllegalArgumentException("Invalid refresh direction " + direction);
        }
        // Now clear the tracker so that everything is sent again to the server
        TrackableSyncSource tss = (TrackableSyncSource)getSyncSource();
        ChangesTracker tracker = tss.getTracker();
        tracker.empty();
        return syncMode;
    }

    public void setSyncMLSource(SyncSource source) {
        syncMLSource = source;
    }

    public void setTwoPhasesSyncSource(SyncSource source) {
        twoPhasesSyncSource = source;
    }

    public String getWarningOnFirstSync() {
        MediaAppSyncSourceConfig config = (MediaAppSyncSourceConfig)getConfig();
        if (config.getIncludeOlderMedia()) {
            return warningOnFirstSync;
        } else {
            return null;
        }
    }

    public void setUISyncSourceController(UISyncSourceController controller) {
        super.setUISyncSourceController(controller);
        // We need to make sure the controller is propagated to both sources,
        // and not just the one currently used
        if (twoPhasesSyncSource != null) {
            twoPhasesSyncSource.setListener(controller);
        }
        if (syncMLSource != null) {
            syncMLSource.setListener(controller);
        }
    }

    /**
     * The underlying configuration was changed in a way that requires the
     * AppSyncSource to be refreshed.
     */
    public void reapplyConfiguration() {
        // Check if the upload method got changed and we need to switch sync
        // source
        Log.info(TAG_LOG, "Reapplying media source configuration");
        if (getConfig().getUploadContentViaHttp() && twoPhasesSyncSource != null) {
            Log.info(TAG_LOG, "Switching to two phases sync source");
            setSyncSource(twoPhasesSyncSource);
        } else {
            Log.info(TAG_LOG, "Switching to pure syncml sync source");
            if (syncMLSource != null) {
                setSyncSource(syncMLSource);
            } else {
                Log.error(TAG_LOG, "Invalid syncml source, source will be disabled");
            }
        }
    }
}


