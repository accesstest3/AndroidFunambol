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

package com.funambol.android.source.pim;

import java.util.Enumeration;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.ContentResolver;

import com.funambol.android.BuildInfo;
import com.funambol.android.source.AbstractDataManager;

import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.client.TrackableSyncSource;
import com.funambol.syncml.client.ChangesTracker;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncException;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.protocol.SyncMLStatus;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.source.AppSyncSource;

import com.funambol.util.Log;

/**
 */
public abstract class PIMSyncSource<E> extends TrackableSyncSource {
    private static final String TAG = "PIMSyncSource";

    protected Context context;
    protected ContentResolver resolver;
    protected AbstractDataManager<E>  dm = null;
    protected Configuration configuration;
    protected AppSyncSource appSource;

    /**
     * PIMSyncSource constructor: initialize source config.
     * 
     * @param config
     * @param tracker
     * @param context
     * @param configuration
     * @param appSource
     */
    public PIMSyncSource(SourceConfig config, ChangesTracker tracker, Context context,
                             Configuration configuration, AppSyncSource appSource, AbstractDataManager dm) {
        super(config, tracker);
        this.context  = context;
        this.configuration = configuration;
        this.appSource = appSource;
        this.resolver = context.getContentResolver();
        this.dm       = dm;
    }

    /** Delete a SyncItem stored on the related Items list */
    @Override
    public int deleteItem(String key) {
        Log.info(TAG, "Delete from server for item " + key);

        if (syncMode == SyncML.ALERT_CODE_REFRESH_FROM_CLIENT || syncMode == SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT) {
            Log.error(TAG, "Server is trying to delete items for a one way sync! " + "(syncMode: " + syncMode + ")");
            return 500;
        }

        try {
            long id;
            try {
                id = Long.parseLong(key);
            } catch (Exception e) {
                Log.error(TAG, "Invalid item id " + key, e);
                return SyncMLStatus.GENERIC_ERROR;
            }
            dm.delete(id);

            SyncItem tmpItem = new SyncItem(key);
            tmpItem.setState(SyncItem.STATE_DELETED);
            
            // Call super method
            super.deleteItem(key);

            return SyncMLStatus.SUCCESS;
        } catch (Exception e) {
            Log.error(TAG, "Cannot delete item", e);
            return SyncMLStatus.GENERIC_ERROR;
        }
    }

    @Override
    public void beginSync(int syncMode) throws SyncException {
        // Init the data manager account at each sync as it may change at any
        // time
        dm.initAccount();

        super.beginSync(syncMode);

        // For any refresh we reset the anchors so that if this sync does not
        // terminate properly, the next sync will be a slow one
        if(syncMode == SyncML.ALERT_CODE_REFRESH_FROM_SERVER ||
           syncMode == SyncML.ALERT_CODE_REFRESH_FROM_CLIENT)
        {
            setLastAnchor(0);
        }
    }

    @Override
    public void endSync() throws SyncException {
        super.endSync();
        // Save the source configuration. We save the configuration here because
        // this piece of code is always executed on successfull sync, no matter
        // if they are triggered from the native app or our client.
        appSource.getConfig().saveSourceSyncConfig();
        appSource.getConfig().commit();
    }

    @Override
    public Enumeration getAllItemsKeys() throws SyncException {

        Log.info(TAG, "getAllItemsKeys");

        try {
            Enumeration keys = dm.getAllKeys();
            return keys;
        } catch (IOException ioe) {
            Log.error(TAG, "Cannot get all keys", ioe);
            throw new SyncException(SyncException.CLIENT_ERROR, "Cannot get all keys");
        }
    }

    @Override
    protected void deleteAllItems() throws SyncException {
        super.deleteAllItems();
        try {
            dm.deleteAll();
        } catch (IOException ioe) {
            throw new SyncException(SyncException.STORAGE_ERROR, ioe.getMessage());
        }
    }

    /**
     * This method is redefined here only to allow test case recording. In a
     * production environment it is just a pass through
     */
    @Override
    public SyncItem getNextNewItem() throws SyncException {

        SyncItem res = super.getNextNewItem();
        if (BuildInfo.TEST_RECORDING_ENABLED) {
            PimTestRecorder recorder = PimTestRecorder.getInstance();
            // This method is always called at least once because it must
            // returns null before the call realize the items are over. We do
            // not consider the last item (null).
            if (res != null) {
                recorder.saveOutgoingItem(res);
            }
        }
        return res;
    }

    /**
     * This method is redefined here only to allow test case recording. In a
     * production environment it is just a pass through
     */
    @Override
    public int addItem(SyncItem item) throws SyncException {
        int res = super.addItem(item);
        if (BuildInfo.TEST_RECORDING_ENABLED) {
            PimTestRecorder recorder = PimTestRecorder.getInstance();
            recorder.saveIncomingItem(item);
        }
        return res;
    }
}
