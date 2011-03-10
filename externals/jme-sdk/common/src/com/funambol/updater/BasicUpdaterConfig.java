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
package com.funambol.updater;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import com.funambol.util.Log;

public abstract class BasicUpdaterConfig implements UpdaterConfig {

    public static final String UPDATE_TYPE_OPTIONAL    = "optional";
    public static final String UPDATE_TYPE_RECOMMENDED = "recommended";
    public static final String UPDATE_TYPE_MANDATORY   = "mandatory";

    public static final long ACTIVATION_DATE_NONE = 0;
    
    private String  url;
    private String  downloadUrl;
    private String  type;
    private String  availableVersion;
    private long    lastCheck;
    private long    checkInterval;
    private long    reminderInterval;
    private long    lastReminder;
    private long    activationDate = ACTIVATION_DATE_NONE;
    private boolean skip;

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public boolean isOptional() {
        return UPDATE_TYPE_OPTIONAL.equals(type);
    }

    public boolean isRecommended() {
        return UPDATE_TYPE_RECOMMENDED.equals(type);
    }

    public boolean isMandatory() {
        return UPDATE_TYPE_MANDATORY.equals(type);
    }

    public String getAvailableVersion() {
        return availableVersion;
    }

    public void setAvailableVersion(String availableVersion) {
        this.availableVersion = availableVersion;
    }

    public long getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(long time) {
        lastCheck = time;
    }

    public long getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    public long getReminderInterval() {
        return reminderInterval;
    }

    public void setReminderInterval(long reminderInterval) {
        this.reminderInterval = reminderInterval;
    }

    public long getActivationDate() {
        return activationDate;
    }
    
    public void setActivationDate(long time) {
        activationDate = time;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getLastReminder() {
        return lastReminder;
    }

    public void setLastReminder(long lastReminder) {
        this.lastReminder = lastReminder;
    }

    public boolean getSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public void serialize(DataOutputStream out) throws IOException {
        if (url != null) {
            out.writeUTF(url);
        } else {
            out.writeUTF("");
        }
        if (downloadUrl != null) {
            out.writeUTF(downloadUrl);
        } else {
            out.writeUTF("");
        }
        if (type != null) {
            out.writeUTF(type);
        } else {
            out.writeUTF("");
        }
        if (availableVersion != null) {
            out.writeUTF(availableVersion);
        } else {
            out.writeUTF("");
        }
        out.writeLong(activationDate);
        
        out.writeLong(lastCheck);
        out.writeLong(checkInterval);
        out.writeLong(lastReminder);
        out.writeLong(reminderInterval);
        out.writeBoolean(skip);
    }

    public void deserialize(DataInputStream in) throws IOException {
        url              = in.readUTF();
        downloadUrl      = in.readUTF();
        type             = in.readUTF();
        availableVersion = in.readUTF();
        activationDate   = in.readLong();
        lastCheck        = in.readLong();
        checkInterval    = in.readLong();
        lastReminder     = in.readLong();
        reminderInterval = in.readLong();
        skip             = in.readBoolean();
    }

    public abstract void save();
    public abstract void load();
}

