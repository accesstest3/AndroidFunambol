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

package com.funambol.android.source.pim.task;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Hashtable;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentUris;

import android.content.ContentValues;
import android.content.Context;
import android.content.SyncAdapterType;
import android.database.Cursor;
import android.net.Uri;

import com.funambol.android.R;
import com.funambol.android.source.AbstractDataManager;
import com.funambol.android.source.pim.calendar.Calendar;

import com.funambol.common.pim.vcalendar.CalendarUtils;
import com.funambol.common.pim.icalendar.ICalendarSyntaxParser;
import com.funambol.common.pim.model.model.VCalendar;
import com.funambol.common.pim.model.converter.VCalendarConverter;
import com.funambol.common.pim.model.converter.ConverterException;
import com.funambol.common.pim.model.calendar.Event;
import com.funambol.common.pim.model.calendar.Task;
import com.funambol.common.pim.model.icalendar.ICalendarSyntaxParserListenerImpl;
import com.funambol.common.pim.model.calendar.RecurrencePattern;
import com.funambol.common.pim.model.calendar.Reminder;
import com.funambol.common.pim.model.common.Property;
import com.funambol.common.pim.model.common.PropertyWithTimeZone;

import com.funambol.client.source.AppSyncSource;
import com.funambol.common.pim.model.calendar.ExceptionToRecurrenceRule;
import com.funambol.util.Log;
import com.funambol.util.StringUtil;
import com.funambol.util.DateUtil;

public class AstridTaskManager extends AbstractDataManager<Calendar> {

    /** Log entries tag */
    private static final String TAG_LOG = "AstridTaskManager";

    /** Native calendar authority. Value calendar[String] */
    public static final String AUTHORITY = "com.todoroo.astrid";

    private AppSyncSource appSource = null;

    /**
     * Final representation of calendar properties for calendar provider.
     */
    public static final class Tasks {

        public static final Uri CONTENT_URI = Uri.parse("content://com.todoroo.astrid/tasks");

        public static final String _ID                = "_id";
        public static final String TITLE              = "title";
        public static final String IMPORTANCE         = "importance";
        public static final String DUE_DATE           = "dueDate";   // unix time
        public static final String COMPLETED          = "completed"; // unix time
        public static final String NOTES              = "notes";
        public static final String RECURRENCE         = "recurrence";
        public static final String FLAGS              = "flags";

        public static final int IMPORTANCE_DO_OR_DIE = 0;
        public static final int IMPORTANCE_MUST_DO = 1;
        public static final int IMPORTANCE_SHOULD_DO = 2;
        public static final int IMPORTANCE_NONE = 3;

        public static final int FLAG_REPEAT_AFTER_COMPLETION = 1;

        public static final String[] PROJECTION = { _ID,
                                                    TITLE,
                                                    IMPORTANCE,
                                                    DUE_DATE,
                                                    COMPLETED,
                                                    NOTES,
                                                    FLAGS,
                                                    RECURRENCE
                                                  };
    }

    /**
     * Default constructor.
     * @param context the Context object 
     * @param appSource the AppSyncSource object to be related to this manager
     */
    public AstridTaskManager(Context context, AppSyncSource appSource) {
        super(context);
        this.appSource = appSource;
    }

    /**
     * Accessor method: get the calendar authority that manages calendars in the
     * system
     * @return String the String formatted representation of the authority
     */
    protected String getAuthority() {
        return AUTHORITY;
    }

    /**
     * Load a particular calendar entry
     * @param key the long formatted entry key to load
     * @return Calendar the Calendar object related to that entry
     * @throws IOException if anything went wrong accessing the calendar db
     */
    public Calendar load(long key) throws IOException {

        Log.trace(TAG_LOG, "Loading Task: " + key);

        Calendar cal = new Calendar();
        cal.setId(key);

        Uri uri = ContentUris.withAppendedId(Tasks.CONTENT_URI, key);
        Cursor cursor = resolver.query(uri, null, null, null, null);
        try {
            if(cursor != null && cursor.moveToFirst()) {
                loadTaskFields(cursor, cal, key);
            } else {
                // Item not found
                throw new IOException("Cannot find event " + key);
            }
        } finally {
            cursor.close();
        }
        return cal;
    }

    /**
     * Add a Calendar item to the db
     * @param item the Calendar object to be added
     * @return long the key given to the added calendar
     * @throws IOException if anything went wrong accessing the calendar db
     */
    public long add(Calendar item) throws IOException {

        Log.trace(TAG_LOG, "Adding Task");

        Task task = item.getTask();

        ContentValues cv = createTaskContentValues(task);
        Uri taskUri = resolver.insert(Tasks.CONTENT_URI, cv);

        long id = Long.parseLong(taskUri.getLastPathSegment());
        Log.trace(TAG_LOG, "The new task has id: " + id);

        //addReminders(item, id);

        return id;
    }

    /**
     * Update a Calendar item to the db
     * @param id the calendar key that represents the calendar to be updated
     * @param newItem the Calendar object taht must replace the existing one
     * @throws IOException if anything went wrong accessing the calendar db
     */
    public void update(long id, Calendar newItem) throws IOException {

        Log.trace(TAG_LOG, "Updating event: " + id);

        // If the contact does not exist, then we perform an add
        if (!exists(id)) {
            Log.info(TAG_LOG, "Tried to update a non existing event. Creating a new one ");
            add(newItem);
            //addReminders(newItem, id);
            return;
        }

        Task task = newItem.getTask();

        ContentValues cv = createTaskContentValues(task);
        Uri uri = ContentUris.withAppendedId(Tasks.CONTENT_URI, id);
        resolver.update(uri, cv, null, null);
        // TODO: update reminders
    }

    /**
     * Delete a Calendar item to the db
     * @param id the calendar key that must be deleted
     * @throws IOException if anything went wrong accessing the calendar db
     */
    public void delete(long itemId) throws IOException {

        Log.trace(TAG_LOG, "Deleting task with id: " + itemId);

        Uri uri = ContentUris.withAppendedId(Tasks.CONTENT_URI, itemId);
        int count = resolver.delete(uri, null, null);

        Log.debug(TAG_LOG, "Deleted task count: " + count);
        if (count < 0) {
            Log.error(TAG_LOG, "Cannot delete task");
            throw new IOException("Cannot delete task");
        }
    }

    /**
     * Delete all calendars from the calendar db
     * @throws IOException if anything went wrong accessing the calendar db
     */
    public void deleteAll() throws IOException {
        Log.trace(TAG_LOG, "Deleting all tasks");
        Enumeration keys = getAllKeys();
        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            long k = Long.parseLong(key);
            delete(k);
            // Delete all reminders associated to this item
            //deleteRemindersForEvent(k, false);
        }
    }

    /**
     * Check if a calendar with the given id exists in the calendar db
     * @param id the id which existence is to be checked
     * @return true if the given id exists in the db false otherwise
     */
    public boolean exists(long id) {
        Uri uri = ContentUris.withAppendedId(Tasks.CONTENT_URI, id);
        Cursor cur = resolver.query(uri, null, null, null, null);
        if(cur == null) {
            return false;
        }
        boolean found = cur.getCount() > 0;
        cur.close();
        return found;
    }

    /**
     * Get all of the calendar keys that exist into the DB
     * @return Enumeration the enumeration object that contains alll of the
     * calendar keys
     * @throws IOException if anything went wrong accessing the calendar db
     */
    public Enumeration getAllKeys() throws IOException {

        String cols[] = {Tasks._ID};
        Cursor cursor = resolver.query(Tasks.CONTENT_URI, cols, null, null, null);
        try {
            int size = cursor.getCount();
            Vector<String> itemKeys = new Vector<String>(size);
            if (!cursor.moveToFirst()) {
                return itemKeys.elements();
            }
            for (int i = 0; i < size; i++) {
                String key = cursor.getString(0);
                Log.trace(TAG_LOG, "Found item with key: " + key);
                itemKeys.addElement(key);
                cursor.moveToNext();
            }
            return itemKeys.elements();
        } catch (Exception e) {
            Log.error(TAG_LOG, "Cannot get all items keys: ", e);
            throw new IOException("Cannot get all items keys");
        } finally {
            cursor.close();
        }
    }

    private void loadTaskFields(Cursor cursor, Calendar cal, long key) {

        Task task = new Task();

        // Load TITLE
        String name = cursor.getString(cursor.getColumnIndex(Tasks.TITLE));
        if(name != null) {
            task.setSummary(new Property(name));
        }

        // Load COMPLETED
        long completed = cursor.getLong(cursor.getColumnIndex(Tasks.COMPLETED));
        if (completed > 0) {
            Property completedProp = new Property();
            completedProp.setPropertyValue(Boolean.TRUE);
            task.setComplete(completedProp);
            
            // Set also the completed date
            String d = DateUtil.formatDateTimeUTC(completed);
            Property completedDate = new Property(d);
            task.setDateCompleted(completedDate);
        }

        // Load NOTES (mapped to description)
        String notes = cursor.getString(cursor.getColumnIndex(Tasks.NOTES));
        if(notes != null) {
            task.setDescription(new Property(notes));
        }

        // Load DUE DATE (mapped to DtEnd)
        long due = cursor.getLong(cursor.getColumnIndex(Tasks.DUE_DATE));
        String dtEnd = null;
        if (due > 0) {
            dtEnd = DateUtil.formatDateTimeUTC(due);
            task.setDtEnd(new PropertyWithTimeZone(dtEnd, "GMT"));
        }

        // Load importance
        long importance = cursor.getLong(cursor.getColumnIndex(Tasks.IMPORTANCE));

        // Load recurrence
        String recurrence = cursor.getString(cursor.getColumnIndex(Tasks.RECURRENCE));
        if (!StringUtil.isNullOrEmpty(recurrence)) {
            try {
                if (recurrence.startsWith("RRULE:")) {
                    recurrence = recurrence.substring(6);
                    recurrence = recurrence.trim();
                }
                // We need to understand if the task repeats after the due date
                // or the completion one
                int flags = cursor.getInt(cursor.getColumnIndexOrThrow(Tasks.FLAGS));
                if ((flags & Tasks.FLAG_REPEAT_AFTER_COMPLETION) != 0) {
                    // How do we represent this in VCalendar??? Not sure we
                    // can...
                    Log.error(TAG_LOG, "Unsupported repeat rule based on completion date which is unknown");
                } else if (dtEnd == null) {
                    Log.error(TAG_LOG, "Unsupported repeat rule based on unknown due date");
                } else {
                    RecurrencePattern rp = createRecurrencePattern(recurrence, dtEnd);
                    if (rp == null) {
                        Log.error(TAG_LOG, "Cannot load recurrence");
                    } else {
                        task.setRecurrencePattern(rp);
                    }
                }
            } catch (Exception e) {
                Log.error(TAG_LOG, "Cannot load recurrence", e);
            }
        }
        

        /*
        // Load REMINDER
        int hasRem = cursor.getInt(cursor.getColumnIndexOrThrow(Events.HAS_ALARM));
        if (hasRem == 1) {
            Log.trace(TAG_LOG, "This event has an alarm associated");
            String fields[] = { Reminders.MINUTES };
            String whereClause = Reminders.EVENT_ID + " = " + key;
            Cursor rems = resolver.query(Reminders.CONTENT_URI,
                                         fields, whereClause, null, null);
            if (rems != null && rems.moveToFirst()) {
                int mins = rems.getInt(rems.getColumnIndexOrThrow(Reminders.MINUTES));
                Reminder rem = new Reminder();
                rem.setMinutes(mins);
                rem.setActive(true);
                event.setReminder(rem);
            } else {
                Log.error(TAG_LOG, "Internal error: cannot find reminder for: " + key);
            }
            if (rems != null) {
                if(rems.moveToNext()) {
                    Log.error(TAG_LOG, "Only one reminder is currently supported, ignoring the others");
                }
                rems.close();
            }
        }
        */

        cal.setTask(task);
    }

    /**
     * Fills a new ContentValues objects with all the given Event's properties
     * @param event the event to be used to fill the ContentValue object
     * @return ContentValues the filled ContenValues object.
     * @throws IOException if anything went wrong accessing the calendar db
     */
    private ContentValues createTaskContentValues(Task task) throws IOException {

        ContentValues cv = new ContentValues();

        // Put title property
        putStringProperty(Tasks.TITLE, task.getSummary(), cv);

        // We must set the complete date
        PropertyWithTimeZone completedDate = task.getDateCompleted();
        if (completedDate != null) {
            putDateTimeProperty(Tasks.COMPLETED, completedDate, false, cv);
        }

        // Notes
        putStringProperty(Tasks.NOTES, task.getDescription(), cv);

        // DUE DATE (mapped to DtEnd)
        PropertyWithTimeZone dueDate = task.getDtEnd();
        if (dueDate != null) {
            putDateTimeProperty(Tasks.DUE_DATE, dueDate, false, cv);
        }

        // Recurrence
        try {
            putRecurrence(task, cv);
        } catch (Exception e) {
            Log.error(TAG_LOG, "Cannot convert recurrence rule", e);
            throw new IOException("Cannot write recurrence rule");
        }

        /*
        putStringProperty(Events.DESCRIPTION, event.getDescription(), cv);
        putStringProperty(Events.LOCATION,    event.getLocation(), cv);

        // Put date properties
        PropertyWithTimeZone start = event.getDtStart();
        PropertyWithTimeZone end = event.getDtEnd();
        boolean allDay = false;
        if(putAllDay(event, cv)) {
            start.setTimeZone("UTC");
            putDateTimeProperty(Events.DTSTART, start, false, cv);
            end.setTimeZone("UTC");
            allDay = true;
        } else {
            putDateTimeProperty(Events.DTSTART, start, false, cv);
        }

        // Android requires that we set DURATION or DTEND for all events
        Property duration = event.getDuration();
        if (!Property.isEmptyProperty(duration)) {
            putStringProperty(Events.DURATION, duration, cv);
        } else if (!Property.isEmptyProperty(end)) {
            if (allDay) {
                // Android dislike events all day with a date whose hour/min/sec
                // are not zero (the calendar app crashes). For this reason we
                // save all day events with their duration
                // TODO FIXME: handle multi days all day events
                putStringProperty(Events.DURATION, new Property("P1D"), cv);
            } else {
                putDateTimeProperty(Events.DTEND, end, false, cv);
            }
        } else {
            // Use a default DURATION of 1 in this case
            putStringProperty(Events.DURATION, new Property("1"), cv);
        }

        // Put Timezone
        putTimeZone(event.getDtStart(), cv);

        // Put visibility class property
        putVisibilityClass(event.getAccessClass(), cv);

        // Put constant values
        cv.put(Events.HAS_ATTENDEE_DATA, 1);

        // Put Account reference
        AccountInfo account = getCalendarAccount(calendarId);
        cv.put(Events._SYNC_ACCOUNT, account.name);
        cv.put(Events._SYNC_ACCOUNT_TYPE, account.type);

        // Set the hasAlarm property
        Reminder rem = event.getReminder();
        cv.put(Events.HAS_ALARM, rem != null);

        try {
            putRecurrence(event, cv);
        } catch (Exception e) {
            Log.error(TAG_LOG, "Cannot convert recurrence rule", e);
            throw new IOException("Cannot write recurrence rule");
        }

        // Put Calendar references
        cv.put(Events.CALENDAR_ID, calendarId);
        */

        return cv;
    }

    /**
     * Put a String property to the given ContentValues.
     * @param column the culumn to be written
     * @param property the property to be written into the column
     * @param cv the content values related to the property
     */
    private void putStringProperty(String column,
            Property property, ContentValues cv) {
        if(property != null) {
            String value = property.getPropertyValueAsString();
            if(value != null) {
                value = StringUtil.replaceAll(value, "\r\n", "\n");
                value = StringUtil.replaceAll(value, "\r",   "\n");
                cv.put(column, value);
            }
        }
    }

    /**
     * Put a date time property to the given ContentValues.
     * @param column the culumn to be written
     * @param property the property to be written into the column
     * @param cv the content values related to the property
     */
    private void putDateTimeProperty(String column, PropertyWithTimeZone property,
            boolean addOneDay, ContentValues cv) {
        if(property != null) {
            String value = property.getPropertyValueAsString();
            if(value != null) {
                long time = CalendarUtils.getLocalDateTime(value, property.getTimeZone());
                if(addOneDay) {
                    time += CalendarUtils.DAY_FACTOR;
                }
                cv.put(column, time);
            }
        }
    }

    /**
     * Put the allday property to the given ContentValues.
     * @param event the event that contains the all day property
     * @param cv the content values related to the event
     */
    private boolean putAllDay(Event event, ContentValues cv) {
        //int allday = event.isAllDay() ? 1 : 0;
        //cv.put(Events.ALL_DAY, allday);
        //return event.isAllDay();
        return false;
    }

    /**
     * Put the timezone property to the given ContentValues.
     * @param property the TZ to be set
     * @param cv the contentValues where to put the TZ
     */
    /*
    private void putTimeZone(PropertyWithTimeZone property, ContentValues cv) {
        if(property != null) {
            String tz = property.getTimeZone();
            if(!StringUtil.isNullOrEmpty(tz)) {
                cv.put(Events.TIMEZONE, tz);
            }
        }
    }
    */

    /**
     * Put the visibility class property to the given ContentValues.
     * @param property the visibility property container
     * @param cv the Content value to be updated
     */
    /*
    private void putVisibilityClass(Property property, ContentValues cv) {
        if(property != null) {
            String vclass = property.getPropertyValueAsString();
            if(!StringUtil.isNullOrEmpty(vclass)) {
                if(Events.VISIBILITY_CLASS_PRIVATE_S.equals(vclass)) {
                    cv.put(Events.VISIBILITY_CLASS, Events.VISIBILITY_CLASS_PRIVATE);
                } else if(Events.VISIBILITY_CLASS_PUBLIC_S.equals(vclass)) {
                    cv.put(Events.VISIBILITY_CLASS, Events.VISIBILITY_CLASS_PUBLIC);
                }
            }
        }
    }
    */

    private void putRecurrence(Task task, ContentValues cv) throws ConverterException {
        // We basically need to transform a vCal rec rule into an iCal rec rule
        // This can be done in different ways. One possibility was to used the
        // VCalendarConverter and the VComponentWriter, but this would not give
        // us anu ability to modify the generated RRULE to fix issues. For this
        // reason that method has been discarded even if implementation wise it
        // would have been simpler.
        Log.trace(TAG_LOG, "Saving recurrence");
        RecurrencePattern rp = task.getRecurrencePattern();
        if (rp != null) {
            StringBuffer result = new StringBuffer(60); // Estimate 60 is needed
            String typeDesc = rp.getTypeDesc();

            if (typeDesc != null) {
                result.append("FREQ=");
                if ("D".equals(typeDesc)) {
                    result.append("DAILY");
                } else if ("W".equals(typeDesc)) {
                    result.append("WEEKLY");
                } else if ("YM".equals(typeDesc)) {
                    result.append("YEARLY");
                } else if ("D".equals(typeDesc)) {
                    result.append("YEARLY");
                } else if ("YD".equals(typeDesc)) {
                    result.append("YEARLY");
                } else if ("MP".equals(typeDesc) || "MD".equals(typeDesc)) {
                    // This ia by position recurrence
                    result.append("MONTHLY");
                }
                result.append(";INTERVAL=" + rp.getInterval());
            }
            if (rp.getOccurrences() != -1 && rp.isNoEndDate()) {
                result.append(";COUNT=" + rp.getOccurrences());
            }
            if (!rp.isNoEndDate()               &&
                 rp.getEndDatePattern() != null &&
                !rp.getEndDatePattern().equals("")) {

                result.append(";UNTIL=" + rp.getEndDatePattern());
            }

            if ("W".equals(typeDesc)) {
                StringBuffer days = new StringBuffer();
                for (int i=0; i<rp.getDayOfWeek().size(); i++) {
                    if (days.length() > 0) {
                        days.append(",");
                    }
                    days.append(rp.getDayOfWeek().get(i));
                }
                if (days.length() > 0) {
                    result.append(";BYDAY=").append(days.toString());
                }
            } else if ("MD".equals(typeDesc)) {
                Log.trace(TAG_LOG, "getDayOfMonth=" + rp.getDayOfMonth());
                result.append(";BYMONTHDAY=" + rp.getDayOfMonth());
            } else if ("MP".equals(typeDesc)) {
                int instance = rp.getInstance();
                short mask = rp.getDayOfWeekMask();
                StringBuffer daysOfWeek = new StringBuffer();
                if ((mask & RecurrencePattern.DAY_OF_WEEK_SUNDAY) != 0) {
                    addDayOfWeek(daysOfWeek, instance, "SU");
                } 
                if ((mask & RecurrencePattern.DAY_OF_WEEK_MONDAY) != 0) {
                    addDayOfWeek(daysOfWeek, instance, "MO");
                }
                if ((mask & RecurrencePattern.DAY_OF_WEEK_TUESDAY) != 0) {
                    addDayOfWeek(daysOfWeek, instance, "TU");
                }
                if ((mask & RecurrencePattern.DAY_OF_WEEK_WEDNESDAY) != 0) {
                    addDayOfWeek(daysOfWeek, instance, "WE");
                }
                if ((mask & RecurrencePattern.DAY_OF_WEEK_THURSDAY) != 0) {
                    addDayOfWeek(daysOfWeek, instance, "TH");
                }
                if ((mask & RecurrencePattern.DAY_OF_WEEK_FRIDAY) != 0) {
                    addDayOfWeek(daysOfWeek, instance, "FR");
                }
                if ((mask & RecurrencePattern.DAY_OF_WEEK_SATURDAY) != 0) {
                    addDayOfWeek(daysOfWeek, instance, "SA");
                }
                if (daysOfWeek.length() > 0) {
                    result.append(";BYDAY=").append(daysOfWeek.toString());
                }
            } else if ("YM".equals(typeDesc)) {
                short monthOfYear = rp.getMonthOfYear();
                if (monthOfYear > 0) {
                    result.append(";BYMONTH=" + monthOfYear);
                }
            } else if ("YD".equals(typeDesc)) {
                // This is not supported by the calendar model
            }

            // Add the RRULE field
            String rule = result.toString();
            Log.info(TAG_LOG, "Setting rrule in task to: " + rule);
            cv.put(Tasks.RECURRENCE, rule);
        }
    }

    private void addDayOfWeek(StringBuffer dayOfWeek, int instance, String day) {
        if (dayOfWeek.length() > 0) {
            dayOfWeek.append(",");
        }
        if (instance != 1 && instance != 0) {
            dayOfWeek.append(instance);
        }
        dayOfWeek.append(day);
    }


    private RecurrencePattern createRecurrencePattern(String rrule, String dueDate) throws Exception {

        // We must parse an ICalendar recurrence
        StringBuffer todo = new StringBuffer();
        todo.append("BEGIN:VCALENDAR\r\n")
            .append("VERSION:2.0\r\n")
            .append("BEGIN:VTODO\r\n")
            .append("SUMMARY:Conversion todo\r\n")
            .append("DUE:").append(dueDate).append("\r\n")
            .append("RRULE:").append(rrule).append("\r\n")
            .append("END:VTODO\r\n")
            .append("END:VCALENDAR\r\n");

        ByteArrayInputStream buffer = new ByteArrayInputStream(todo.toString().getBytes());

        VCalendar vcalendar = new VCalendar();
        ICalendarSyntaxParserListenerImpl listener = new ICalendarSyntaxParserListenerImpl(vcalendar);
        ICalendarSyntaxParser parser = new ICalendarSyntaxParser(buffer);
        parser.setListener(listener);
        parser.parse();

        vcalendar.addProperty("VERSION", "2.0");
        VCalendarConverter vcf =
                 new VCalendarConverter(TimeZone.getDefault(), "UTF-8", false);
        Task t = vcf.vcalendar2calendar(vcalendar).getTask();
        return t.getRecurrencePattern();
    }
}

