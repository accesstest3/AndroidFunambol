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

package com.funambol.client.test;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import com.funambol.client.test.*;

import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.util.StringUtil;
import com.funambol.util.Log;

public abstract class CalendarRobot extends Robot {
   
    private static final String TAG_LOG = "CalendarRobot";

    protected static final char FOLDING_INDENT_CHAR = ' ';

    protected long currentEventId = -1;

    protected long incrementalServerItemkey = 10000000;

    protected String eventAsVcal = null;

    protected BasicRobot basicRobot;

    protected AppSyncSourceManager appSourceManager;

    public CalendarRobot(BasicRobot basicRobot, AppSyncSourceManager appSourceManager) {
        this.basicRobot = basicRobot;
        this.appSourceManager = appSourceManager;
    }

    public CalendarRobot() {
    }

    public void importEventOnServer(String filename) throws Throwable {
        this.eventAsVcal = TestFileManager.getInstance().getFile(BasicScriptRunner.getBaseUrl() + "/" + filename);
    }

    public void saveEventOnServer(CheckSyncClient client) throws Throwable {
        CheckSyncSource source = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CALENDAR);
        SyncItem item = new SyncItem(Long.toString(incrementalServerItemkey++));
        item.setContent(getCurrentEventVCal().getBytes());
        
        if(currentEventId != -1) {
            item.setKey(Long.toString(currentEventId));
            source.updateItemFromOutside(item);
        } else {
            source.addItemFromOutside(item);
        }

        // Reset current event
        currentEventId = -1;
        eventAsVcal = null;
    }

    public void deleteEventOnServer(String summary, CheckSyncClient client) throws Throwable {
        CheckSyncSource source = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CALENDAR);
        String itemKey = findEventKeyOnServer(summary, client);
        source.deleteItemFromOutside(itemKey);
    }

    public void deleteAllEventsOnServer(CheckSyncClient client) throws Throwable {
        CheckSyncSource source = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CALENDAR);
        source.deleteAllFromOutside();
    }

    public void resetEvents(CheckSyncClient client) throws Throwable {
        CheckSyncSource source = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CALENDAR);

        // Remove everything locally
        deleteAllEvents();
        // Clean the sync client and perform a refresh from client to server
        basicRobot.reapplySyncConfig(client);
        client.clear(source);
    }

    public void setEventAsVCal(String vCal) throws Throwable{
        String[] sep = new String[]{"\\r\\n"};
        String[] parts = StringUtil.split(vCal, sep);

        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        for (int i=0;i<parts.length;i++){
            ostream.write(parts[i].getBytes());
            ostream.write("\r\n".getBytes());
        }
        eventAsVcal = ostream.toString();
        ostream.close();
    }

    public void setEventFromServer(String vCal) throws Throwable {

        vCal = StringUtil.replaceAll(vCal, "\\r\\n", "\r\n");

        Enumeration sources = getAppSyncSourceManager().getWorkingSources();
        AppSyncSource appSource = null;

        while(sources.hasMoreElements()) {
            appSource = (AppSyncSource)sources.nextElement();
            if (appSource.getId() == AppSyncSourceManager.EVENTS_ID) {
                break;
            }
        }

        // We add an item via the SyncSource
        SyncSource source = appSource.getSyncSource();
        SyncItem item = new SyncItem("guid", "text/x-vcalendar", SyncItem.STATE_NEW, null);
        item.setContent(vCal.getBytes("UTF-8"));

        source.addItem(item);
    }

    protected AppSyncSourceManager getAppSyncSourceManager() {
        return appSourceManager;
    }

    public abstract void createEmptyEvent() throws Throwable;
    public abstract void setEventField(String field, String value) throws Throwable;

    public abstract void setEventRecurrenceField(String recField, String value) throws Throwable;

    public abstract void loadEvent(String summary) throws Throwable;

    /** TODO FIXME */
    public abstract void saveEvent() throws Throwable;
    public void saveEvent(boolean save) throws Throwable { }
    
    public abstract void deleteEvent(String summary) throws Throwable;
    public abstract void deleteAllEvents() throws Throwable;

    public abstract void checkNewEvent(String summary,
            CheckSyncClient client, boolean checkContent) throws Throwable;

    public abstract void checkUpdatedEvent(String summary,
            CheckSyncClient client, boolean checkContent) throws Throwable;

    public abstract void checkDeletedEvent(String summary,
            CheckSyncClient client) throws Throwable;

    public abstract void checkNewEventOnServer(String summary,
            CheckSyncClient client, boolean checkContent) throws Throwable;

    public abstract void checkUpdatedEventOnServer(String summary,
            CheckSyncClient client, boolean checkContent) throws Throwable;

    public abstract void checkDeletedEventOnServer(String summary,
            CheckSyncClient client) throws Throwable;

    public abstract void checkEventRecRule(String summary, String rrule) throws Throwable;
    public abstract void checkEventAsVCal(String summary, String rrule) throws Throwable;
    public abstract void checkEventExceptions(String summary, String exceptions) throws Throwable;

    public abstract void loadEventOnServer(String summary,
            CheckSyncClient client) throws Throwable;

    public abstract void createEmptyRawEvent() throws Throwable;

    public abstract void setRawEventField(String fieldName, String fieldValue) throws Throwable;

    public abstract void setRawReminderField(String fieldName, String fieldValue) throws Throwable;

    public abstract void saveRawEvent() throws Throwable;

    public abstract void checkRawEventField(String fieldName, String fieldValue) throws Throwable;

    public abstract void checkRawReminderField(String fieldName, String fieldValue) throws Throwable;
    
    public abstract void checkRawEventAsVCal(String vcal) throws Throwable;

    protected abstract String findEventKeyOnServer(String summary,
            CheckSyncClient client) throws Throwable;

    protected abstract String getCurrentEventVCal() throws Throwable;

    private String cleanField(String fieldName, String value, Hashtable supportedValues) {
        String filter = (String)supportedValues.get(fieldName); 
        if (filter != null) {
            Log.trace(TAG_LOG, "Found filter for field: " + fieldName + "," + filter);
            String values[] = StringUtil.split(value, ";");
            String filters[] = StringUtil.split(filter, ";");
            String res = "";

            for(int i=0;i<values.length;++i) {
                String v = values[i];
                boolean include;
                if (i<filters.length) {
                    String f = filters[i];
                    if (f.length() > 0) {
                        include = true;
                    } else {
                        include = false;
                    }
                } else {
                    include = true;
                }

                if (include) {
                    res = res + v;
                }
                if (i != values.length - 1) {
                    res = res + ";";
                }
            }
            return res;

        } else {
            return value;
        }
    }

    private Vector getFieldsVector(String vcard) {

        String sep[] = {"\r\n"};
        String lines[] = StringUtil.split(new String(vcard), sep);

        Vector fieldsAl = new Vector();
        String field = "";
        for(int i=0;i<lines.length;++i) {
            String line = lines[i];
            if(line.length() > 0 && line.charAt(0) == FOLDING_INDENT_CHAR) {
                // this is a multi line field
                field += line.substring(1); // cut the indent char
            } else {
                if(!field.equals("")) {
                    fieldsAl.addElement(field);
                }
                field = line;
            }
        }
        // add the latest field
        fieldsAl.addElement(field);

        return fieldsAl;
    }


    protected String orderVCal(String vcal, String supportedFields[], Hashtable supportedValues) {

        Log.trace(TAG_LOG, "Ordering vcal: " + vcal);
        Vector fieldsAl = getFieldsVector(vcal);

        // order the fields array list
        String result = "";
        String[] fields = StringUtil.getStringArray(fieldsAl);
        for(int i=0; i<fields.length; i++) {
            for(int j=fields.length-1; j>i; j--) {
                if(fields[j].compareTo(fields[j-1])<0) {
                    String temp = fields[j];
                    fields[j] = fields[j-1];
                    fields[j-1] = temp;
                }
            }

            // Trim any leading/trailing white space
            fields[i] = fields[i].trim();

            // Exclude last occurrence of ";" from all fields
            while (fields[i].endsWith(";")) {
                fields[i] = new String(fields[i].substring(0, fields[i].length()-1));
            }
            
            // Order ENCODING and CHARSET parameters
            int index = fields[i].indexOf("ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8");
            int length = "ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8".length();
            if(index != -1) {
                StringBuffer field = new StringBuffer();
                field.append(fields[i].substring(0, index));
                field.append("CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE");
                field.append(fields[i].substring(index+length));
                fields[i] = field.toString();
            }
            
            // Exclude empty fields and fields which are not supported by the
            // device
            if(!fields[i].endsWith(":")) {
                if (supportedFields != null) {
                    int fieldNameIdx = fields[i].indexOf(":");
                    if (fieldNameIdx != -1) {
                        String fieldName = fields[i].substring(0, fieldNameIdx);

                        for(int j=0;j<supportedFields.length;++j) {
                            if (fieldName.equals(supportedFields[j])) {

                                if (fieldNameIdx + 1 < fields[i].length()) {
                                    String value = fields[i].substring(fieldNameIdx + 1);
                                    value = cleanField(fieldName, value, supportedValues);

                                    // Exclude last occurrence of ";" from all fields
                                    while (value.endsWith(";")) {
                                        value = new String(value.substring(0, value.length()-1));
                                    }

                                    result += fieldName + ":" + value + "\r\n";
                                } else {
                                    result += fields[i] + "\r\n";
                                }
                                break;
                            }
                        }
                    } else {
                        result += fields[i] + "\r\n";
                    }
                } else {
                    result += fields[i] + "\r\n";
                }
            }
        }
        Log.trace(TAG_LOG, "Ordered vcal: " + result);
        return result;
    }
}
