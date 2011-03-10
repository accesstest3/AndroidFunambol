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

package com.funambol.android.services;

import com.funambol.android.AndroidAppSyncSource;
import com.funambol.android.AndroidAppSyncSourceManager;

/**
 * Defines a SyncAdapterService for Media sync
 */
public class MediaSyncAdapterService extends AbstractSyncAdapterService {

    private final int[] APP_SYNC_SOURCE_IDS = { AndroidAppSyncSourceManager.PICTURES_ID,
                                                AndroidAppSyncSourceManager.VIDEOS_ID
                                              };
    private int[] orderedIds;

    // It is important that these ids are returned in the UI order. This is
    // because for media sync we have multiple sources to sync. The sources are
    // synced in the order returned here, so they better be ordered according to
    // the UI
    protected int[] getAppSyncSoureIds() {
        // Order the array based on the sources ui position
        // so that if multiple sources must be synchronized we start from the
        // first one
        if (orderedIds == null) {
            orderedIds = orderSourcesByUiIndex();
        }
        return orderedIds;
    }

    private int[] orderSourcesByUiIndex() {
        // The number of sources can be less according to what is
        // enabled/disabled
        // Count how many sources are really available among the ones listed in
        // the APP_SYNC_SOURCE_IDS
        int count = 0;
        for(int i=0;i<APP_SYNC_SOURCE_IDS.length;++i) {
            int sourceId = APP_SYNC_SOURCE_IDS[i];
            AndroidAppSyncSource appSource = (AndroidAppSyncSource)appSyncSourceManager.getSource(sourceId);
            if (appSource != null) {
                count++;
            }
        }

        int res[] = new int[count];
        for(int i=0,j=0;i<APP_SYNC_SOURCE_IDS.length;++i) {
            int sourceId = APP_SYNC_SOURCE_IDS[i];
            AndroidAppSyncSource appSource = (AndroidAppSyncSource)appSyncSourceManager.getSource(sourceId);
            if (appSource != null) {
                res[j++] = APP_SYNC_SOURCE_IDS[i];
            }
        }

        // Finally order res
        for(int i=0;i<res.length;++i) {
            AndroidAppSyncSource appSource = (AndroidAppSyncSource)appSyncSourceManager.getSource(res[i]);
            int min = appSource.getUiSourceIndex();
            int minIndex = i;
            for(int j=i;j<res.length;++j) {
                AndroidAppSyncSource otherAppSource = (AndroidAppSyncSource)appSyncSourceManager.getSource(res[j]);
                if (otherAppSource.getUiSourceIndex() < appSource.getUiSourceIndex()) {
                    minIndex = j;
                }
            }
            // Now swap i and minIndex
            if (i != minIndex) {
                int tmp = res[i];
                res[i] = res[minIndex];
                res[minIndex] = tmp;
            }
        }

        return res;
    }
}
