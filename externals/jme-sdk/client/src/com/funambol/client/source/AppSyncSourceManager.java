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

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import java.io.DataInputStream;
import java.io.ByteArrayInputStream;

import com.funambol.client.customization.Customization;
import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.syncml.client.ConfigSyncSource;
import com.funambol.util.Log;
import com.funambol.storage.StringKeyValueFileStore;
import com.funambol.syncml.client.CacheTracker;
import com.funambol.syncml.client.FileSyncSource;
import com.funambol.syncml.protocol.SyncML;

/**
 * This class is responsible for handling all the sources at the application
 * level. A source at the application level is a type of data for which a
 * SyncSource exits, but also a sync button on the screen and configuration
 * parameters.
 * This class creates and registers the sources. The list of sources is handled
 * and allows other parts of the application to be completely parametric wrt to
 * this value.
 * For new sources to be added to the application it is necessary to register
 * them in this manager. The only other part of the application that must be
 * changed when new sources are added is the Customization which holds default
 * parameters for each source.
 */
public class AppSyncSourceManager {

    private static final String TAG_LOG = "AppSyncSourceManager";

    public static final int CONTACTS_ID = 1;
    public static final int EVENTS_ID   = 2;
    public static final int TASKS_ID    = 4;
    public static final int NOTES_ID    = 8;
    public static final int PICTURES_ID = 16;
    public static final int MAILS_ID    = 32;
    public static final int CONFIG_ID   = 64;
    public static final int VIDEOS_ID   = 128;
    public static final int FILES_ID    = 256;
    public static final int ALL_ID      = 511;

    // Types
    public static final String  CONTACT_TYPE_VCARD        = SourceConfig.VCARD_TYPE;
    public static final String  CONTACT_TYPE_SIF          = "text/x-s4j-sifc";
    public static final String  CALENDAR_TYPE_SIF         = "text/x-s4j-sife";
    public static final String  CALENDAR_TYPE_ICALENDAR   = "text/calendar";
    public static final String  CALENDAR_TYPE_VCALENDAR   = "text/x-vcalendar";
    public static final String  NOTE_TYPE_SIF             = "text/x-s4j-sifn";
    public static final String  TASK_TYPE_SIF             = "text/x-s4j-sift";
    public static final String  TASK_TYPE_VCALENDAR       = "text/x-vcalendar";
    public static final String  BRIEFCASE_TYPE            = SourceConfig.FILE_OBJECT_TYPE;

    // Source config settings
    protected static final String  NOTE_TYPE              = NOTE_TYPE_SIF;
    protected static final String  PHOTO_TYPE             = SourceConfig.FILE_OBJECT_TYPE;
    protected static final String  VIDEO_TYPE             = SourceConfig.FILE_OBJECT_TYPE;
    protected static final String  CONFIG_TYPE            = SourceConfig.BRIEFCASE_TYPE;
    
    private Hashtable idMap       = null;
    private Vector    sourcesList = null;
    protected Customization customization;

    public AppSyncSourceManager(Customization customization) {
        this.customization = customization;

        idMap       = new Hashtable();
        sourcesList = new Vector();
    }

    public void registerSource(AppSyncSource source) {
        sourcesList.addElement(source);
        idMap.put(new Integer(source.getId()), source);
    }

    public int numberOfRegisteredSources() {
        return idMap.size();
    }

    public int numberOfEnabledSources() {
        int count = 0;
        for(int i=0;i<sourcesList.size();++i) {
            AppSyncSource appSource = (AppSyncSource)sourcesList.elementAt(i);
            if (appSource.isEnabled()) {
                count++;
            }
        }
        return count;
    }

    public int numberOfWorkingSources() {
        int count = 0;
        for(int i=0;i<sourcesList.size();++i) {
            AppSyncSource appSource = (AppSyncSource)sourcesList.elementAt(i);
            if (appSource.isWorking()) {
                count++;
            }
        }
        return count;
    }

    public int numberOfEnabledAndWorkingSources() {
        int count = 0;
        for(int i=0;i<sourcesList.size();++i) {
            AppSyncSource appSource = (AppSyncSource)sourcesList.elementAt(i);
            if (appSource.isEnabled() && appSource.isWorking()) {
                count++;
            }
        }
        return count;
    }

    public AppSyncSource getSource(int id) {
        AppSyncSource source = (AppSyncSource)idMap.get(new Integer(id));
        return source;
    }

    public AppSyncSource getSource(SyncSource source) {
        if (source == null) {
            return null;
        }
        for(int i=0;i<sourcesList.size();++i) {
            AppSyncSource appSource = (AppSyncSource)sourcesList.elementAt(i);
            SyncSource ss = appSource.getSyncSource();
            if (ss == source) {
                return appSource;
            }
        }
        return null;
    }

    public Enumeration getEnabledSources() {

        Log.trace(TAG_LOG, "getEnabledSources");
        Vector result = new Vector();
        for(int i=0;i<sourcesList.size();++i) {
            AppSyncSource appSource = (AppSyncSource)sourcesList.elementAt(i);
            if (appSource.isEnabled()) {
                result.addElement(appSource);
            }
        }
        return new SortedSourcesEnumeration(result);
    }

    public Enumeration getWorkingSources() {

        Log.trace(TAG_LOG, "getWorkingSources");
        Vector result = new Vector();
        for(int i=0;i<sourcesList.size();++i) {
            AppSyncSource appSource = (AppSyncSource)sourcesList.elementAt(i);
            if (appSource.isWorking()) {
                result.addElement(appSource);
            }
        }
        return new SortedSourcesEnumeration(result);
    }

    public Enumeration getEnabledAndWorkingSources() {
        Log.trace(TAG_LOG, "getEnabledAndWorkingSources");
        Vector result = new Vector();
        for(int i=0;i<sourcesList.size();++i) {
            AppSyncSource appSource = (AppSyncSource)sourcesList.elementAt(i);
            if (appSource.isEnabled() && appSource.isWorking()) {
                result.addElement(appSource);
            }
        }
        return new SortedSourcesEnumeration(result);
    }

    public class SortedSourcesEnumeration implements Enumeration {

        private Vector sources;
        private Vector result;
        private int index;

        private SortedSourcesEnumeration() {
        }
        
        public SortedSourcesEnumeration(Vector sources) {
            this.sources = sources;
            this.index = sources.size() - 1;
            result = new Vector();
            bubbleSortResult();
        }

        private void bubbleSortResult() {
            Log.trace(TAG_LOG, "Sorting Sources");
            int n = sources.size();

            for (int i = 0; i < sources.size(); i++) {
                result.addElement(sources.elementAt(i));
            }

            for (int pass = 1; pass < n; pass++) {
                // count how many times
                // This next loop becomes shorter and shorter

                for (int i = 0; i < n - pass; i++) {
                    AppSyncSource ass1 = (AppSyncSource) result.elementAt(i);
                    AppSyncSource ass2 = (AppSyncSource) result.elementAt(i + 1);
                    int id1 = ass1.getId();
                    int id2 = ass2.getId();
                    if (id1 < id2) {
                        // exchange elements
                        Object temp = result.elementAt(i);
                        result.setElementAt(result.elementAt(i + 1), i);
                        result.setElementAt(temp, i + 1);
                    }
                }
            }
            Log.trace(TAG_LOG, "Returning sorted Sources: " + result.size());
        }

        public boolean hasMoreElements() {
            return index >= 0;
        }

        public Object nextElement() {
            Object o = result.elementAt(index);
            index--;
            return o;
        }

    }

    public Enumeration getRegisteredSources() {
        Log.trace(TAG_LOG, "getRegisteredSources");
        
        Vector result = new Vector();
        for(int i=0;i<sourcesList.size();++i) {
            AppSyncSource appSource = (AppSyncSource)sourcesList.elementAt(i);
            result.addElement(appSource);
        }
        return result.elements();
    }

    protected int getSourcePosition(int id) {
        int order[] = customization.getSourcesOrder();
        int pos = 0;
        while(order[pos] != id && pos < order.length) {
            pos++;
        }
        return pos;
    }
}


