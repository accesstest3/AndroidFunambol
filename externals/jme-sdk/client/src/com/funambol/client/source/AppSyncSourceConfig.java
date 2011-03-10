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

package com.funambol.client.source;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import com.funambol.client.configuration.Configuration;
import com.funambol.client.customization.Customization;
import com.funambol.client.source.AppSyncSource;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.syncml.spds.SyncListener;
import com.funambol.syncml.protocol.SyncML;

import com.funambol.util.Log;

/**
 * This class represents the configuration of an AppSyncSource. In particular it
 * holds all the values that are kept across application resets. These values
 * are loaded/saved using a Configuration object.
 */
public class AppSyncSourceConfig {

    private static final String TAG_LOG = "AppSyncSourceConfig";

    protected static final String CONF_KEY_CONFIG_VERSION         = "SYNC_SOURCE_CONFIG_VERSION";
    protected static final String CONF_KEY_SYNC_URI               = "SYNC_SOURCE_URI";
    protected static final String CONF_KEY_SYNC_TYPE              = "SYNC_TYPE";
    protected static final String CONF_KEY_SOURCE_FULL            = "SYNC_SOURCE_FULL";
    protected static final String CONF_KEY_SOURCE_SYNCED          = "SYNC_SOURCE_SYNCED";
    protected static final String CONF_KEY_SOURCE_ACTIVE          = "SYNC_SOURCE_ACTIVE";
    protected static final String CONF_KEY_SOURCE_ENABLED         = "SYNC_SOURCE_ENABLED";
    protected static final String CONF_KEY_SYNC_STATUS            = "SOURCE_STATUS";
    protected static final String CONF_KEY_SOURCE_CONFIG          = "SOURCE_CONFIG";
    protected static final String CONF_KEY_UPLOAD_CONTENT_VIA_HTTP= "UPLOAD_CONTENT_VIA_HTTP";
    protected static final String CONF_KEY_PENDING_SYNC_TYPE      = "PENDING_SYNC_TYPE";
    protected static final String CONF_KEY_PENDING_SYNC_MODE      = "PENDING_SYNC_MODE";
    protected static final String CONF_KEY_SYNC_TIMESTAMP         = "SYNC_SOURCE_TIMESTAMP";

    private static final String VERSION                         = "1";

    protected String uri;
    protected boolean enabled = true;
    protected boolean active;
    protected AppSyncSource appSource;
    protected int syncType = -1;
    protected boolean deviceFullShown = false;
    protected boolean sourceSynced;
    protected int lastSyncStatus = SyncListener.SUCCESS;
    protected long lastSyncTimestamp = 0;
    protected boolean dirty = false;
    protected boolean uploadContentViaHttp = false;
    protected String pendingSyncType = "";
    protected int    pendingSyncMode = -1;
    protected String version;

    protected boolean loaded = false;

    protected Configuration configuration;
    protected Customization customization;

    public AppSyncSourceConfig(AppSyncSource appSource, Customization customization, Configuration configuration) {
        this.appSource     = appSource;
        this.configuration = configuration;
        this.customization = customization;
        appSource.setConfig(this);
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        dirty = true;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        dirty = true;
    }

    public int getLastSyncStatus() {
        return lastSyncStatus;
    }

    public void setLastSyncStatus(int lastSyncStatus) {
        this.lastSyncStatus = lastSyncStatus;
        dirty = true;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
        dirty = true;
    }

    public boolean getDeviceFullShown() {
        return deviceFullShown;
    }

    public void setDeviceFullShown(boolean value) {
        this.deviceFullShown = value;
        dirty = true;
    }

    public void setSynced(boolean sourceSynced) {
        this.sourceSynced = sourceSynced;
        dirty = true;
    }

    public boolean getSynced() {
        return sourceSynced;
    }

    public int getSyncType() {
        return syncType;
    }

    public void setSyncType(int syncType) {
        this.syncType = syncType;
        dirty = true;
    }

    public boolean getUploadContentViaHttp() {
        return uploadContentViaHttp;
    }

    public void setUploadContentViaHttp(boolean value) {
        Log.info(TAG_LOG, "Setting upload content via http to " + value);
        uploadContentViaHttp = value;
        dirty = true;
    }

    public long getLastSyncTimestamp() {
        return lastSyncTimestamp;
    }

    public void setLastSyncTimestamp(long ts) {
        this.lastSyncTimestamp = ts;
        dirty = true;
    }

    /**
     * Returns a value indicating if there is a pending sync. A pending sync is
     * a synchronization that could be performed for any reason. The value that
     * is stored here, is the sync type (MANUAL, SCHEDULED or PUSH).
     *
     * @return null if there is no pending sync, a value otherwise (can be
     * MANUAL, SCHEDULED or PUSH)
     */
    public String getPendingSyncType() {
        if (pendingSyncType != null && pendingSyncType.length() == 0) {
            return null;
        } else {
            return pendingSyncType;
        }
    }

    /**
     * Returns a value indicating if there is a pending sync. A pending sync is
     * a synchronization that could be performed for any reason. The value that
     * is stored here, is the sync mode (SyncML alert sync mode).
     *
     * @return -1 if there is no pending sync, a value otherwise (can be any
     * value according to SyncML alert codes)
     */
    public int getPendingSyncMode() {
        return pendingSyncMode;
    }

    /**
     * Sets the last (only one) pending sync. A pending sync is a
     * synchronization that could not be performed for any reason. Two info are
     * stored for this sync:
     *
     * <ul>
     *   <li> how it was triggered (its sync type)</li>
     *   <li> its SyncML sync mode </li>
     * </ul>
     *
     * @param pendingSyncType can be null to cancel any pending sync, or one of
     * the values in MANUAL, SCHEDULED, PUSH
     * @param syncMode the SyncML alert code, or -1 to cancel any pending sync
     */
    public void setPendingSync(String pendingSyncType, int pendingSyncMode) {
        if (pendingSyncType == null) {
            pendingSyncType = "";
        }
        if(!pendingSyncType.equals(this.pendingSyncType)) {
            this.pendingSyncType = pendingSyncType;
            dirty = true;
        }
        if(this.pendingSyncMode != pendingSyncMode) {
            this.pendingSyncMode = pendingSyncMode;
            dirty = true;
        }
    }

    public void saveSourceSyncConfig() {
        int sourceId = appSource.getId();
        Log.debug(TAG_LOG, "Storing SourceConfig for " + appSource.getName());

        SyncSource source = appSource.getSyncSource();
        if (source != null) {
            SourceConfig config = source.getConfig();
            try {
                String storageKey = CONF_KEY_SOURCE_CONFIG + sourceId;
                ByteArrayOutputStream buff = new ByteArrayOutputStream(512);
                DataOutputStream temp = new DataOutputStream(buff);
                config.serialize(temp);
                configuration.saveByteArrayKey(storageKey, buff.toByteArray());
                temp.close();
            } catch (final Exception e) {
                Log.error(TAG_LOG, "Exception while storing SourceConfig [" + config.getName() + "] ", e);
            }
        }
    }

    /**
     * Saves a syncsource config to storage. Classes that extend the
     * AppSyncSourceConfig shall always save their custom data before invoking
     * this method, because the method before returning notifies the
     * configuration that a source has changed.
     */
    public synchronized void save() {
        int sourceId = appSource.getId();

        // Save the low level sync config
        saveSourceSyncConfig();

        if (!dirty) {
            return;
        }

        // Now save all the high level source parameters
        StringBuffer key = new StringBuffer();

        // Save the version
        key.append(CONF_KEY_CONFIG_VERSION).append("-").append(sourceId);
        configuration.saveStringKey(key.toString(), version);

        // Save the remote URI
        key = new StringBuffer();
        key.append(CONF_KEY_SYNC_URI).append("-").append(sourceId)
            .append("-").append("URI");
        configuration.saveStringKey(key.toString(), getUri());

        // Save the last sync status
        key = new StringBuffer();
        key.append(CONF_KEY_SYNC_STATUS).append("-").append(sourceId);
        configuration.saveIntKey(key.toString(), getLastSyncStatus());

        // Save the last sync timestamp
        key = new StringBuffer();
        key.append(CONF_KEY_SYNC_TIMESTAMP).append("-").append(sourceId);
        configuration.saveLongKey(key.toString(), lastSyncTimestamp);

        // Save the active flag
        key = new StringBuffer();
        key.append(CONF_KEY_SOURCE_ACTIVE).append("-").append(sourceId);
        configuration.saveBooleanKey(key.toString(), getActive());

        // Save the sync type
        key = new StringBuffer();
        key.append(CONF_KEY_SYNC_TYPE).append("-").append(sourceId);
        configuration.saveIntKey(key.toString(), syncType);

        // Save the enabled/flag
        key = new StringBuffer();
        key.append(CONF_KEY_SOURCE_ENABLED).append("-").append(sourceId);
        configuration.saveBooleanKey(key.toString(), enabled);

        // Save if the source showed device full already
        key = new StringBuffer();
        key.append(CONF_KEY_SOURCE_FULL).append("-").append(sourceId);
        configuration.saveBooleanKey(key.toString(), getDeviceFullShown());

        // Save if the source got synced at least once
        key = new StringBuffer();
        key.append(CONF_KEY_SOURCE_SYNCED).append("-").append(sourceId);
        configuration.saveBooleanKey(key.toString(), getSynced());

        // Save if content shall be sent via http
        key = new StringBuffer();
        key.append(CONF_KEY_UPLOAD_CONTENT_VIA_HTTP).append("-").append(sourceId);
        configuration.saveBooleanKey(key.toString(), getUploadContentViaHttp());

        // Save the pending sync type 
        key = new StringBuffer();
        key.append(CONF_KEY_PENDING_SYNC_TYPE).append("-").append(sourceId);
        configuration.saveStringKey(key.toString(), getPendingSyncType());

        // Save the pending sync type 
        key = new StringBuffer();
        key.append(CONF_KEY_PENDING_SYNC_MODE).append("-").append(sourceId);
        configuration.saveIntKey(key.toString(), getPendingSyncMode());

        // Clear the dirty flag
        dirty = false;

        configuration.notifySourceConfigChanged(appSource);
    }

    public void load(SourceConfig config) {

        // We load the config from the storage only once
        if (loaded) {
            return;
        }

        int sourceId = appSource.getId();
        Log.debug(TAG_LOG, "Loading config for " + appSource.getName());

        // Load the version number
        StringBuffer key = new StringBuffer();
        key.append(CONF_KEY_CONFIG_VERSION).append("-").append(sourceId);
        version = configuration.loadStringKey(key.toString(), null);

        // Load the source config
        if (config != null) {
            try {
                String storageKey = CONF_KEY_SOURCE_CONFIG + sourceId;
                byte[] byteArray = configuration.loadByteArrayKey(storageKey, null);
                if (byteArray != null) {
                    Log.debug(TAG_LOG, "Data Found");
                    DataInputStream temp = new DataInputStream(new ByteArrayInputStream(byteArray));
                    config.deserialize(temp);
                    temp.close();
                }
            } catch (final Exception e) {
                Log.error(TAG_LOG, "Exception while initializating (reading) of SourceConfig ["
                        + config.getName() + "] " + e.toString());
            }
        }

        // Now load all the high level source parameters

        // Load the last sync status
        key = new StringBuffer();
        key.append(CONF_KEY_SYNC_STATUS).append("-").append(sourceId);
        lastSyncStatus = configuration.loadIntKey(key.toString(), SyncListener.SUCCESS);

        // Load the last sync timestamp
        key = new StringBuffer();
        key.append(CONF_KEY_SYNC_TIMESTAMP).append("-").append(sourceId);
        lastSyncTimestamp = configuration.loadLongKey(key.toString(), 0);

        // Load the sync type
        key = new StringBuffer();
        key.append(CONF_KEY_SYNC_TYPE).append("-").append(sourceId);
        syncType = configuration.loadIntKey(key.toString(),
                customization.getDefaultSourceSyncMode(sourceId));

        // Load the remote URI
        key = new StringBuffer();
        key.append(CONF_KEY_SYNC_URI).append("-").append(sourceId)
            .append("-").append("URI");
        uri = configuration.loadStringKey(key.toString(),
                customization.getDefaultSourceUri(sourceId));

        // Update the source config
        if (config != null) {
            config.setRemoteUri(uri);
            config.setSyncMode(syncType);
        }

        // Load the enable property
        key = new StringBuffer();
        key.append(CONF_KEY_SOURCE_ENABLED).append("-").append(sourceId);
        enabled = configuration.loadBooleanKey(key.toString(), enabled);

        // Load if the source is active
        key = new StringBuffer();
        key.append(CONF_KEY_SOURCE_ACTIVE).append("-").append(sourceId);
        active = configuration.loadBooleanKey(key.toString(), customization.isSourceActive(sourceId));

        // Load if the source showed device full warning already
        key = new StringBuffer();
        key.append(CONF_KEY_SOURCE_FULL).append("-").append(sourceId);
        deviceFullShown = configuration.loadBooleanKey(key.toString(), deviceFullShown);

        // Load if the source got synced at least once
        key = new StringBuffer();
        key.append(CONF_KEY_SOURCE_SYNCED).append("-").append(sourceId);
        sourceSynced = configuration.loadBooleanKey(key.toString(), sourceSynced);

        // Load if the source shall use http based upload
        key = new StringBuffer();
        key.append(CONF_KEY_UPLOAD_CONTENT_VIA_HTTP).append("-").append(sourceId);
        uploadContentViaHttp = configuration.loadBooleanKey(key.toString(), uploadContentViaHttp);

        // Load the pending sync type
        key = new StringBuffer();
        key.append(CONF_KEY_PENDING_SYNC_TYPE).append("-").append(sourceId);
        pendingSyncType = configuration.loadStringKey(key.toString(), pendingSyncType);

        // Load the pending sync mode
        key = new StringBuffer();
        key.append(CONF_KEY_PENDING_SYNC_MODE).append("-").append(sourceId);
        pendingSyncMode = configuration.loadIntKey(key.toString(), pendingSyncMode);

        // If needed, we migrate the configuration
        if (!VERSION.equals(version)) {
            Log.info(TAG_LOG, "Migrating source config for " + appSource.getName());
            // we need to migrate the config
            migrateConfig(version, VERSION, config);
        }

        loaded = true;
    }

    public void commit() {
        // We commit the whole configuration
        Log.trace(TAG_LOG, "Committing config for: " + appSource.getName());
        if (dirty) {
            save();
        }
        configuration.commit();
    }

    public boolean isDirty() {
        return dirty;
    }

    protected void migrateConfig(String from, String to, SourceConfig config) {

        // the version in the app sync source config was introduced in v9. In
        // this case the from is null because not available in previous version
        if (from == null) {
            // Migrates from v8.7 to v9.0

            // The enabled flag is now properly handled, before it was based on the
            // syncType
            if (syncType == SyncML.ALERT_CODE_NONE) {
                enabled = false;
            }

            // In v9 we also introduced the lastSyncTimestamp property in the
            // configuration. We shall read the value stored in the last anchor
            // and copy it over
            if (config != null) {
                lastSyncTimestamp = config.getLastAnchor();
            }

            // In v9 we removed 1way syncs from the product, so it is safe to
            // execute this piece of code here
            migrateSupportedSyncModes(config);
        }
        // Finally we update the version to its current value
        version = VERSION;
        // Now persist everything
        dirty = true;
        commit();
    }

    /**
     * This method checks if the sync type currently set for this source is
     * still supported. If not, then the source is disabled and its anchor
     * reset. This method shall be invoked any time a client removes a sync
     * type for a source.
     */
    protected void migrateSupportedSyncModes(SourceConfig config) {
        Log.debug(TAG_LOG, "Migrating supported sync modes for " + appSource.getName());
        // Check if the current sync type is supported
        int sourceModes[] = customization.getDefaultSourceSyncModes(appSource.getId());

        Log.debug(TAG_LOG, "sourceModes = " + sourceModes);

        if (sourceModes == null) {
            return;
        }

        boolean found = false;
        for(int i=0;i<sourceModes.length;++i) {
            if (sourceModes[i] == syncType) {
                Log.debug(TAG_LOG, "Found sourceModes[i]=" + sourceModes[i] + ",syncType=" + syncType);
                found = true;
            }
        }
        if (!found) {
            // The sync type currently set is no longer supported for this
            // source
            Log.info(TAG_LOG, "Sync mode: " + syncType + " is no longer supported for: " + appSource.getName());
            setEnabled(false);
            // Resets the anchors in order to force a slow sync at the next sync
            if (config != null) {
                config.setLastAnchor(0);
                config.setNextAnchor(0);
            }
            // Revert to the default sync mode
            syncType = customization.getDefaultSourceSyncMode(appSource.getId());
            save();
            // Persist the fact that we changed the configuration so the UI can
            // show appropriate messages
            configuration.setPimSourceSyncTypeChanged(true);
            configuration.save();
        }
    }
}

