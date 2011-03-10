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

package com.funambol.android.source.pim.calendar;

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
import android.os.Build;

import android.content.ContentValues;
import android.content.Context;
import android.content.SyncAdapterType;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.Time;

import com.funambol.android.R;
import com.funambol.android.AndroidUtils;
import com.funambol.android.source.AbstractDataManager;

import com.funambol.common.pim.vcalendar.CalendarUtils;
import com.funambol.common.pim.icalendar.ICalendarSyntaxParser;
import com.funambol.common.pim.model.model.VCalendar;
import com.funambol.common.pim.model.converter.VCalendarConverter;
import com.funambol.common.pim.model.converter.ConverterException;
import com.funambol.common.pim.model.calendar.Event;
import com.funambol.common.pim.model.icalendar.ICalendarSyntaxParserListenerImpl;
import com.funambol.common.pim.model.calendar.RecurrencePattern;
import com.funambol.common.pim.model.calendar.Reminder;
import com.funambol.common.pim.model.common.Property;
import com.funambol.common.pim.model.common.PropertyWithTimeZone;

import com.funambol.client.source.AppSyncSource;
import com.funambol.common.pim.model.calendar.ExceptionToRecurrenceRule;
import com.funambol.common.pim.model.utility.TimeUtils;
import com.funambol.util.DateUtil;
import com.funambol.util.Log;
import com.funambol.util.StringUtil;

/**
 * AbstractDataManager for the calendars entries: does the effective events'
 * addition, modifications and deletion.
 */
public class CalendarManager extends AbstractDataManager<Calendar> {

    /** Log entries tag */
    private static final String TAG_LOG = "CalendarManager";

    /** Native calendar authority. Value calendar[String] */
    public static final String CALENDAR_AUTHORITY = "calendar";
    /** Native calendar authority. Value com.android.calendar[String] */
    public static final String CALENDAR_AUTHORITY_2 = "com.android.calendar";

    public static final String CALLER_IS_SYNCADAPTER = "caller_is_syncadapter";

    /** Native Google account: Value com.google[String] */
    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";

    /** Color for native funambol calendar representation: Value  0xFF33bdf0[int]*/
    private static final int FUNAMBOL_CALENDAR_COLOR = 0xFF33bdf0;

    private AppSyncSource appSource = null;

    private static String calendarAuthority = null;

    private Hashtable<Long, AccountInfo> calendarAccountReference = new Hashtable<Long, AccountInfo>();

    private boolean callerIsSyncAdapter = true;
    

    /**
     * Final representation of calendar properties for calendar provider.
     */
    public static final class Calendars {

        public static final Uri CONTENT_URI = Uri.parse("content://" +
                getCalendarAuthority() + "/calendars");

        public static final String _ID                = "_id";
        public static final String _SYNC_ACCOUNT      = "_sync_account";
        public static final String _SYNC_ACCOUNT_TYPE = "_sync_account_type";
        public static final String NAME               = "name";
        public static final String DISPLAY_NAME       = "displayName";
        public static final String TIMEZONE           = "timezone";
        public static final String OWNER_ACCOUNT      = "ownerAccount";
        public static final String COLOR              = "color";
        public static final String SYNC_EVENTS        = "sync_events";
        public static final String ACCESS_LEVEL       = "access_level";

        public static final String[] PROJECTION = { _ID,
                                                    _SYNC_ACCOUNT,
                                                    _SYNC_ACCOUNT_TYPE,
                                                    NAME,
                                                    DISPLAY_NAME,
                                                    OWNER_ACCOUNT };
    }

    /**
     * Final representation of Event properties for calendar provider.
     */
    public static final class Events {

        public static final Uri CONTENT_URI = Uri.parse("content://" +
                getCalendarAuthority() + "/events");

        public static final String _ID                = "_id";
        public static final String _SYNC_ACCOUNT      = "_sync_account";
        public static final String _SYNC_ACCOUNT_TYPE = "_sync_account_type";
        public static final String _SYNC_DIRTY        = "_sync_dirty";
        public static final String _SYNC_VERSION      = "_sync_version";
        public static final String CALENDAR_ID        = "calendar_id";
        public static final String TITLE              = "title";
        public static final String DESCRIPTION        = "description";
        public static final String LOCATION           = "eventLocation";
        public static final String DTSTART            = "dtstart";
        public static final String DTEND              = "dtend";
        public static final String ALL_DAY            = "allDay";
        public static final String DURATION           = "duration";
        public static final String TIMEZONE           = "eventTimezone";
        public static final String HAS_ATTENDEE_DATA  = "hasAttendeeData";
        public static final String VISIBILITY_CLASS   = "visibility";
        public static final String RRULE              = "rrule";
        public static final String EXRULE             = "exrule";
        public static final String EXDATE             = "exdate";
        public static final String RDATE              = "rdate";
        public static final String HAS_ALARM          = "hasAlarm";

        public static final int VISIBILITY_CLASS_PRIVATE = 2;
        public static final int VISIBILITY_CLASS_PUBLIC  = 3;

        public static final String VISIBILITY_CLASS_PRIVATE_S = "PRIVATE";
        public static final String VISIBILITY_CLASS_PUBLIC_S  = "PUBLIC";
    }

    /**
     * Final representation of Reminder properties for calendar provider.
     */
    public static final class Reminders {

        public static final Uri CONTENT_URI = Uri.parse("content://" +
                getCalendarAuthority() + "/reminders");

        public static final String _ID          = "_id";
        public static final String EVENT_ID     = "event_id";
        public static final String MINUTES      = "minutes";
        public static final String METHOD       = "method";

        public static final int DEFAULT_METHOD  = 1;
    }

    /**
     * Final representation of Attendees properties for calendar provider.
     */
    public static final class Attendees {

        public static final Uri CONTENT_URI = Uri.parse("content://" +
                getCalendarAuthority() + "/attendees");

        public static final String _ID          = "_id";
        // TODO: ...
    }

    /**
     * Default constructor.
     * @param context the Context object 
     * @param appSource the AppSyncSource object to be related to this manager
     */
    public CalendarManager(Context context, AppSyncSource appSource) {
        super(context);
        this.appSource = appSource;
        this.callerIsSyncAdapter = true;
    }

    public CalendarManager(Context context, AppSyncSource appSource, boolean callerIsSyncAdapter) {
        super(context);
        this.appSource = appSource;
        this.callerIsSyncAdapter = callerIsSyncAdapter;
    }

    /**
     * Accessor method: get the calendar authority that manages calendars in the
     * system
     * @return String the String formatted representation of the authority
     */
    protected String getAuthority() {
        return getCalendarAuthority();
    }

    /**
     * Accessor method
     * @return the calendar authority used by the current CalendarProvider
     */
    public static String getCalendarAuthority() {
        if(calendarAuthority == null) {
            SyncAdapterType[] types = ContentResolver.getSyncAdapterTypes();

            //select the right calendar authority to manage calendars on the
            //current system
            for(SyncAdapterType type: types) {

                String accountType = type.accountType;
                String authority = type.authority;

                if(GOOGLE_ACCOUNT_TYPE.equals(accountType)) {
                    Log.trace(TAG_LOG, "Found google account");
                    if(CALENDAR_AUTHORITY.equals(authority)) {
                        Log.trace(TAG_LOG, "Found calendar authority: " +
                                CALENDAR_AUTHORITY);
                        calendarAuthority = CALENDAR_AUTHORITY;
                    } else if(CALENDAR_AUTHORITY_2.equals(authority)) {
                        Log.trace(TAG_LOG, "Found calendar authority: " +
                                CALENDAR_AUTHORITY_2);
                        calendarAuthority = CALENDAR_AUTHORITY_2;
                    } else {
                        Log.info(TAG_LOG, "Found Google account for authority" + authority);
                    }
                }
            }
            if(calendarAuthority == null) {
                // Fall to default authority based on the OS version
                if (Integer.parseInt(Build.VERSION.SDK) == 8 ) {
                    calendarAuthority = CALENDAR_AUTHORITY_2;
                } else {
                    calendarAuthority = CALENDAR_AUTHORITY;
                }
            } 
        }
        return calendarAuthority;
    }

    /**
     * Load a particular calendar entry
     * @param key the long formatted entry key to load
     * @return Calendar the Calendar object related to that entry
     * @throws IOException if anything went wrong accessing the calendar db
     */
    public Calendar load(long key) throws IOException {

        Log.trace(TAG_LOG, "Loading Event: " + key);

        Calendar cal = new Calendar();
        cal.setId(key);

        Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, key);
        Cursor cursor = resolver.query(uri, null, null, null, null);
        try {
            if(cursor != null && cursor.moveToFirst()) {
                loadCalendarFields(cursor, cal, key);
            } else {
                // Item not found
                throw new IOException("Cannot find event " + key);
            }
        } finally {
            cursor.close();
        }
        return cal;
    }

    public Calendar load(Cursor cursor) throws IOException {
        Log.trace(TAG_LOG, "Loading event from cursor");

        Calendar cal = new Calendar();
        String key = cursor.getString(cursor.getColumnIndexOrThrow(Events._ID));
        Long k = Long.parseLong(key);
        loadCalendarFields(cursor, cal, k);
        return cal;
    }

    /**
     * Add a Calendar item to the db
     * @param item the Calendar object to be added
     * @return long the key given to the added calendar
     * @throws IOException if anything went wrong accessing the calendar db
     */
    public long add(Calendar item) throws IOException {

        Log.trace(TAG_LOG, "Adding Event");

        Event event = item.getEvent();

        ContentValues cv = createEventContentValues(event);
        Uri uri = addCallerIsSyncAdapterFlag(Events.CONTENT_URI);
        Uri eventUri = resolver.insert(uri, cv);

        long id = Long.parseLong(eventUri.getLastPathSegment());
        Log.trace(TAG_LOG, "The new event has id: " + id);

        addReminders(item, id);

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
            addReminders(newItem, id);
            return;
        }

        Event event = newItem.getEvent();

        ContentValues cv = createEventContentValues(event);
        Uri uri = addCallerIsSyncAdapterFlag(Events.CONTENT_URI);
        Uri eventUri = ContentUris.withAppendedId(uri, id);
        resolver.update(eventUri, cv, null, null);

        // For reminders we remove the old ones and add the new one
        deleteRemindersForEvent(id, false);
        addReminders(newItem, id);
    }

    /**
     * Delete a Calendar item to the db
     * @param id the calendar key that must be deleted
     * @throws IOException if anything went wrong accessing the calendar db
     */
    public void delete(long itemId) throws IOException {

        Log.trace(TAG_LOG, "Deleting event with id: " + itemId);

        Uri uri = addCallerIsSyncAdapterFlag(Events.CONTENT_URI);
        Uri eventUri = ContentUris.withAppendedId(uri, itemId);
        int count = resolver.delete(eventUri, null, null);

        Log.debug(TAG_LOG, "Deleted events count: " + count);
        if (count <= 0) {
            Log.error(TAG_LOG, "Cannot delete event");
            throw new IOException("Cannot delete event");
        } else {
            Log.debug(TAG_LOG, "deleting event reminder");
            long id = Long.parseLong(eventUri.getLastPathSegment());
            deleteRemindersForEvent(id, false);
        }
    }

    /**
     * Delete all calendars from the calendar db
     * @throws IOException if anything went wrong accessing the calendar db
     */
    public void deleteAll() throws IOException {
        Log.trace(TAG_LOG, "Deleting all events");
        Enumeration keys = getAllKeys();
        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            long k = Long.parseLong(key);
            delete(k);
            // Delete all reminders associated to this item
            deleteRemindersForEvent(k, false);
        }
    }

    /**
     * Check if a calendar with the given id exists in the calendar db
     * @param id the id which existence is to be checked
     * @return true if the given id exists in the db false otherwise
     */
    public boolean exists(long id) {
        Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, id);
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

        CalendarAppSyncSourceConfig config = (CalendarAppSyncSourceConfig)appSource.getConfig();
        if (config.getCalendarId() == -1) {
            throw new IOException("Cannot access undefined calendar");
        }

        String cols[] = {Events._ID};
        Cursor cursor = resolver.query(Events.CONTENT_URI, cols,
                Events.CALENDAR_ID + "='" + config.getCalendarId() + "'", null, null);
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


    /**
     * Retrive the whole Calendar objects list
     * @returns ArrayList<CalendarDescriptor> a list of available calendars.
     * Each calendar is described by a CalendarDescriptor entry with all of its
     * properties. If no calendars are available the method returns an empty
     * list (not null).
     */
    public ArrayList<CalendarDescriptor> getCalendars() {

        Log.trace(TAG_LOG, "Retrieving list of calendars");

        String fields[] = { Calendars._ID, Calendars.DISPLAY_NAME };
        Cursor cals = resolver.query(Calendars.CONTENT_URI,
                                     fields, null, null, null);

        ArrayList<CalendarDescriptor> res = new ArrayList<CalendarDescriptor>();
        
        if (cals != null) {
            int count = cals.getCount();
            try {
                for (int i = 0; i < count; i++) {
                    if (cals.moveToNext()) {
                        String calName = cals.getString(cals.getColumnIndexOrThrow(Calendars.DISPLAY_NAME));
                        int    calId   = cals.getInt(cals.getColumnIndexOrThrow(Calendars._ID));
                        Log.debug(TAG_LOG, "Found calendar " + calName);

                        CalendarDescriptor calDesc = new CalendarDescriptor(calId, calName);
                        res.add(calDesc);
                    }
                }
            } finally {
                cals.close();
            }
        }
        return res;
    }

    /**
     * Gets a calendar description from its id. If the
     * calendar is no longer available, then the method returns null
     *
     * @param id the calendar id
     * @return the calendar description or null if the calendar is no longer
     *         available
     */
    public CalendarDescriptor getCalendarDescription(long id) {
        // Check if this calendar is still available
        String whereClause = Calendars._ID + " = " + id;
        Cursor cals = resolver.query(Calendars.CONTENT_URI,
                                     Calendars.PROJECTION, whereClause, null, null);
        if (cals == null) {
            return null;
        }
        try {
            if (cals.moveToFirst()) {
                // The calendar is available
                String calendarName = cals.getString(cals.getColumnIndexOrThrow(Calendars.DISPLAY_NAME));
                CalendarDescriptor calDesc = new CalendarDescriptor(id, calendarName);
                return calDesc;
            }
        } finally {
            cals.close();
        }
        return null;
    }

    /**
     * Gets the default calendar to sync. The default calendar
     * is chosen with the following criterion:
     *
     * 1) native calendar first (e.g. Personal calendar on HTC devices)
     * 2) main google calendar (if a Google account is defined)
     * 3) Funambol calendar
     *
     * If no available calendar calendar is found, this method tries to create a
     * Funambol calendar and return it.
     *
     * @return a calendar descriptor if a calendar was identified as the default
     * one
     */
    public CalendarDescriptor getDefaultCalendar() {

        Log.trace(TAG_LOG, "getting default calendar");

        // On Android 2.2 and onward we always create the Funambol calendar to
        // sync. On older versions, because of a bug in Android, we try to use a
        // calendar that gets not removed once a google sync kicks in
        CalendarDescriptor res = null;
        if (Build.VERSION.SDK_INT >= 8 || AndroidUtils.isSimulator(context)) {
            Log.info(TAG_LOG, "On this version of Android, we create a separate calendar");
            res = createFunambolCalendar();
        } else {
            // On older devices we sync the calendar only if we know there is a
            // local calendar suitable for sync
            res = findNativeCalendar();
        }
        return res;
    }

    /**
     * This class represent a calendar, with all its relevant properties
     */
    public class CalendarDescriptor {
        private long id;
        private String name;

        public CalendarDescriptor(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    private void loadCalendarFields(Cursor cursor, Calendar cal, long key) {

        Event event = new Event();

        // Load SUMMARY
        String summary = cursor.getString(cursor.getColumnIndex(Events.TITLE));
        if(summary != null) {
            event.setSummary(new Property(summary));
        }
        // Load DESCRIPTION
        String description = cursor.getString(cursor.getColumnIndex(Events.DESCRIPTION));
        if(description != null) {
            event.setDescription(new Property(description));
        }
        // Load LOCATION
        String location = cursor.getString(cursor.getColumnIndex(Events.LOCATION));
        if(location != null) {
            event.setLocation(new Property(location));
        }

        // Load TIMEZONE
        String tz = cursor.getString(cursor.getColumnIndexOrThrow(Events.TIMEZONE));

        // Load ALL_DAY
        boolean allday = cursor.getInt(cursor.getColumnIndex(Events.ALL_DAY)) == 1;
        if (allday) {
            event.setAllDay(allday);
        }

        // Load DTSTART
        String dtstart = cursor.getString(cursor.getColumnIndex(Events.DTSTART));
        if(!StringUtil.isNullOrEmpty(dtstart)) {
            // The dstart was shifted to UTC because expressed as msecs
            dtstart = CalendarUtils.formatDateTime(Long.parseLong(dtstart), allday, tz);
            event.setDtStart(new PropertyWithTimeZone(dtstart, tz));
        }
        // Load DTEND
        String dtend = cursor.getString(cursor.getColumnIndex(Events.DTEND));
        if(!StringUtil.isNullOrEmpty(dtend)) {
            // Substract a day if this is an all day event
            long endMillis = Long.parseLong(dtend);
            if(allday) {
                endMillis -= CalendarUtils.DAY_FACTOR;
            }
            dtend = CalendarUtils.formatDateTime(endMillis, allday, tz);
            event.setDtEnd(new PropertyWithTimeZone(dtend, tz));
        }
        // Load DURATION
        String duration = cursor.getString(cursor.getColumnIndex(Events.DURATION));
        if (duration != null) {
            event.setDuration(new Property(duration));
        }

        // Load VISIBILITY_CLASS
        int vclass  = cursor.getInt(cursor.getColumnIndex(Events.VISIBILITY_CLASS));
        if(vclass > 0) {
            if(vclass == Events.VISIBILITY_CLASS_PRIVATE) {
                event.setAccessClass(new Property(Events.VISIBILITY_CLASS_PRIVATE_S));
            } else if(vclass == Events.VISIBILITY_CLASS_PUBLIC) {
                event.setAccessClass(new Property(Events.VISIBILITY_CLASS_PUBLIC_S));
            }
        }

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

        // Load recurrence
        String rrule  = cursor.getString(cursor.getColumnIndexOrThrow(Events.RRULE));
        if (rrule != null && rrule.length() > 0) {
            try {
                String exdate  = cursor.getString(cursor.getColumnIndexOrThrow(Events.EXDATE));
                String exrule  = cursor.getString(cursor.getColumnIndexOrThrow(Events.EXRULE));
                String rdate   = cursor.getString(cursor.getColumnIndexOrThrow(Events.RDATE));
        
                RecurrencePattern rp = createRecurrencePattern(dtstart, rrule, 
                        exdate, exrule, rdate, tz, allday);
                if (rp == null) {
                    Log.error(TAG_LOG, "Cannot load recurrence");
                } else {
                    event.setRecurrencePattern(rp);
                }
            } catch (Exception e) {
                Log.error(TAG_LOG, "Cannot load recurrence", e);
            }
        }

        cal.setEvent(event);
    }

    /**
     * Fills a new ContentValues objects with all the given Event's properties
     * @param event the event to be used to fill the ContentValue object
     * @return ContentValues the filled ContenValues object.
     * @throws IOException if anything went wrong accessing the calendar db
     */
    private ContentValues createEventContentValues(Event event) throws IOException {

        CalendarAppSyncSourceConfig config = (CalendarAppSyncSourceConfig)appSource.getConfig();
        long calendarId = config.getCalendarId();
        if (calendarId == -1) {
            throw new IOException("Cannot use undefined calendar");
        }

        ContentValues cv = new ContentValues();

        // Put String properties
        putStringProperty(Events.TITLE,       event.getSummary(), cv);
        putStringProperty(Events.DESCRIPTION, event.getDescription(), cv);
        putStringProperty(Events.LOCATION,    event.getLocation(), cv);

        // Put date properties
        PropertyWithTimeZone start = event.getDtStart();
        PropertyWithTimeZone end = event.getDtEnd();

        boolean allDay = false;
        if(putAllDay(event, cv)) {
            // We must take the event TZ into account for this to work
            allDay = true;
        }

        putDateTimeProperty(Events.DTSTART, start, cv, allDay, false);

        // Android requires that we set DURATION or DTEND for all events
        Property duration = event.getDuration();
        if (!Property.isEmptyProperty(duration)) {
            putStringProperty(Events.DURATION, duration, cv);
        } else if (!Property.isEmptyProperty(end)) {
            // For rucurring events we must save the duration instead of the dtend
            if(event.getRecurrencePattern() != null) {
                // This piece of code computes the duration given a dtstart and
                // dtend. Probably redundant, do do not set it.
                if (!Property.isEmptyProperty(start)) {

                    java.util.Calendar startCal = DateUtil.parseDateTime(
                            start.getPropertyValueAsString());
                    java.util.Calendar endCal = DateUtil.parseDateTime(
                            end.getPropertyValueAsString());

                    long endMillis = endCal.getTimeInMillis();
                    long startMillis = startCal.getTimeInMillis();
                    long d = endMillis - startMillis;
                    int seconds = (int)(d / (1000));

                    // Duration should be formatted as 'P<seconds>S'
                    StringBuffer newDuration = new StringBuffer(10);
                    newDuration.append("P");
                    newDuration.append(seconds);
                    newDuration.append("S");

                    Log.trace(TAG_LOG, "Setting duration to: " + newDuration);
                    putStringProperty(Events.DURATION, new Property(
                            newDuration.toString()), cv);
                } else {
                    // Should never happen
                    // Use a default DURATION of 1 in this case
                    putStringProperty(Events.DURATION, new Property("P1D"), cv);
                }
            } else {
                putDateTimeProperty(Events.DTEND, end, cv, allDay, true);
            }
        } else {
            // Use a default DURATION of 1 in this case
            putStringProperty(Events.DURATION, new Property("P1D"), cv);
        }

        // Put Timezone
        putTimeZone(event.getDtStart(), cv, allDay);

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
     * @param allday tells that the given date is an all day
     * @param addOneDay add one day in milliseconds to the given date
     */
    private void putDateTimeProperty(String column, PropertyWithTimeZone property,
                                     ContentValues cv, boolean allDay, boolean addOneDay)
    {
        if (property != null) {
            if (allDay) {
                String date = property.getPropertyValueAsString();
                try {
                    TimeZone tz = null;
                    if (property.getTimeZone() != null) {
                        tz = TimeZone.getTimeZone(property.getTimeZone());
                    }
                    date = TimeUtils.convertUTCDateToLocal(date, tz);
                } catch(Exception ex) {
                    Log.error(TAG_LOG, "Cannot convert to local time", ex);
                }

                // Android dislike events all day with a date whose hour/min/sec
                // are not zero (the calendar app crashes). For this reason we
                // remove any time info
                int tIdx = date.indexOf("T");
                if (tIdx != -1) {
                    date = date.substring(0, tIdx);
                }
                // TimeZone for all day properties must be UTC
                property = new PropertyWithTimeZone(date, "UTC");
            }

            String value = property.getPropertyValueAsString();
            if(value != null) {
                long time = CalendarUtils.getLocalDateTime(value, property.getTimeZone());
                if(allDay && addOneDay) {
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
        int allday = event.isAllDay() ? 1 : 0;
        cv.put(Events.ALL_DAY, allday);
        return event.isAllDay();
    }

    /**
     * Put the timezone property to the given ContentValues.
     * @param property the TZ to be set
     * @param cv the contentValues where to put the TZ
     */
    private void putTimeZone(PropertyWithTimeZone property, ContentValues cv,
            boolean allDay) {
        if(property != null) {
            String tz = allDay ? "UTC" : property.getTimeZone();
            if(!StringUtil.isNullOrEmpty(tz)) {
                cv.put(Events.TIMEZONE, tz);
            }
        }
    }

    /**
     * Put the visibility class property to the given ContentValues.
     * @param property the visibility property container
     * @param cv the Content value to be updated
     */
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

    private void addReminders(Calendar item, long itemId) throws IOException {
        // Create a new entry in the Reminders table if necessary
        Reminder rem = item.getEvent().getReminder();
        if (rem != null) {
            int mins = -1;
            if (rem.getMinutes() > 0) {
                mins = rem.getMinutes();
            } else if (rem.getTime() != null) {
                Log.error(TAG_LOG, "Reminder as absloute value not implemented yet");
                /* TODO FIXME
                String time = rem.getTime();
                Log.info(TAG_LOG, "Reminder time: " + time);
                try {
                    Date remDate = DateFormat.getDateInstance().parse(time);
                    Log.info(TAG_LOG, "remDate=" + remDate.toString());
                } catch (Exception e) {
                    Log.error(TAG_LOG, "Cannot parse reminder date, ignoring reminder");
                }
                */
            }

            if (mins != -1) {
                // We need to specify the time as minutes before the start
                ContentValues cv = new ContentValues();
                cv.put(Reminders.EVENT_ID,      itemId);
                cv.put(Reminders.MINUTES,       mins);
                cv.put(Reminders.METHOD,        Reminders.DEFAULT_METHOD);

                // Add the reminder
                Uri calUri = resolver.insert(Reminders.CONTENT_URI, cv);
            }
        }
    }

    private void deleteRemindersForEvent(long itemId, boolean resetHasAlarm) throws IOException {

        Log.trace(TAG_LOG, "Deleting reminders for item: " + itemId);

        // TODO FIXME: if we don't sync all of the reminders, we shall remove
        // only the first one, which is the one we sync...
        String whereClause = Reminders.EVENT_ID + " = " + itemId;
        int count = resolver.delete(Reminders.CONTENT_URI, whereClause, null);

        Log.debug(TAG_LOG, "Deleted reminders count: " + count);
        if (resetHasAlarm) {
            // TODO: reset the has alarm in the original item
        }
    }

    private void putRecurrence(Event event, ContentValues cv) throws ConverterException {
        // We basically need to transform a vCal rec rule into an iCal rec rule
        // This can be done in different ways. One possibility was to used the
        // VCalendarConverter and the VComponentWriter, but this would not give
        // us any ability to modify the generated RRULE to fix issues. For this
        // reason that method has been discarded even if implementation wise it
        // would have been simpler.
        Log.trace(TAG_LOG, "Saving recurrence");
        RecurrencePattern rp = event.getRecurrencePattern();
        if (rp != null) {
            StringBuffer result = new StringBuffer(60); // Estimate 60 is needed
            String typeDesc = rp.getTypeDesc();

            if (typeDesc != null) {
                result.append("FREQ=");
                if ("D".equals(typeDesc)) {
                    result.append("DAILY");
                } else if ("W".equals(typeDesc)) {
                    result.append("WEEKLY");
                } else if ("YM".equals(typeDesc) || "YD".equals(typeDesc)) {
                    result.append("YEARLY");
                } else if ("MP".equals(typeDesc) || "MD".equals(typeDesc)) {
                    // This ia by position recurrence
                    result.append("MONTHLY");
                }
                int interval = rp.getInterval();
                if(interval > 1) {
                    result.append(";INTERVAL=" + interval);
                }
            }
            int count = rp.getOccurrences();
            if (count > 0 && rp.isNoEndDate()) {
                result.append(";COUNT=" + count);
            }
            if (!rp.isNoEndDate()               &&
                 rp.getEndDatePattern() != null &&
                !rp.getEndDatePattern().equals("")) {

                rp = fixEndDatePattern(rp, true);
                String enddate = rp.getEndDatePattern();
                
                result.append(";UNTIL=" + enddate);
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
                addDaysOfWeek(result, instance, mask);
            } else if ("YM".equals(typeDesc)) {
                short monthOfYear = rp.getMonthOfYear();
                if (monthOfYear > 0) {
                    result.append(";BYMONTH=" + monthOfYear);
                }
                int instance = rp.getInstance();
                short mask = rp.getDayOfWeekMask();
                addDaysOfWeek(result, instance, mask);
            } else if ("YD".equals(typeDesc)) {
                // This is not supported by the calendar model
            }

            // Add the RRULE field
            String rule = result.toString();
            Log.info(TAG_LOG, "Setting rrule in event to: " + rule);
            cv.put(Events.RRULE, rule);

            // Add the EXDATE and RDATE fields
            List<ExceptionToRecurrenceRule> exceptions = rp.getExceptions();
            StringBuffer exdate = new StringBuffer();
            StringBuffer rdate = new StringBuffer();
            for(ExceptionToRecurrenceRule ex : exceptions) {
                String date = ex.getDate();
                if(ex.isAddition()) {
                    if(rdate.length() > 0) {
                        rdate.append(',');
                    }
                    rdate.append(date);
                } else {
                    if(exdate.length() > 0) {
                        exdate.append(',');
                    }
                    exdate.append(date);
                }
            }
            Log.info(TAG_LOG, "Setting exdate in event to: " + exdate.toString());
            cv.put(Events.EXDATE, exdate.toString());
            Log.info(TAG_LOG, "Setting rdate in event to: " + rdate.toString());
            cv.put(Events.RDATE, rdate.toString());
        }
    }

    private void addDaysOfWeek(StringBuffer result, int instance, short mask) {
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
    }

    private void addDayOfWeek(StringBuffer dayOfWeek, int instance, String day) {
        if (dayOfWeek.length() > 0) {
            dayOfWeek.append(",");
        }
        if (instance != 0) {
            dayOfWeek.append(instance);
        }
        dayOfWeek.append(day);
    }


    private RecurrencePattern createRecurrencePattern(String dtstart, String rrule,
            String exdate, String exrule, String rdate, String tz, boolean allday)
            throws Exception {

        // We must parse an ICalendar recurrence
        StringBuffer event = new StringBuffer();
        event.append("BEGIN:VCALENDAR\r\n")
             .append("VERSION:2.0\r\n")
             .append("BEGIN:VEVENT\r\n")
             .append("DTSTART:").append(dtstart).append("\r\n");

        event.append("RRULE:").append(rrule).append("\r\n");
        if (exdate != null) {
            event.append("EXDATE:").append(exdate).append("\r\n");
        }
        if (exrule != null) {
            event.append("EXRULE:").append(exrule).append("\r\n");
        }
        if (rdate != null) {
            event.append("RDATE:").append(rdate).append("\r\n");
        }

        event.append("END:VEVENT\r\n")
             .append("END:VCALENDAR\r\n");

        ByteArrayInputStream buffer = new ByteArrayInputStream(event.toString().getBytes());

        VCalendar vcalendar = new VCalendar();
        ICalendarSyntaxParserListenerImpl listener = new ICalendarSyntaxParserListenerImpl(vcalendar);
        ICalendarSyntaxParser parser = new ICalendarSyntaxParser(buffer);
        parser.setListener(listener);
        parser.parse();

        vcalendar.addProperty("VERSION", "2.0");
        // In the iCalendar event we synthetized, we did not specify any
        // vtimezone. In this case the converter will consider the event in the
        // timezone supplied in the constructor. For this reason we create the
        // converter in the timezone of the vcal event
        TimeZone tZone;
        if (tz != null) {
            tZone = TimeZone.getTimeZone(tz);
        } else {
            // Use the device default TZ in this case
            tZone = TimeZone.getDefault();
        }
        VCalendarConverter vcf = getConverter(tZone, allday);
        Event e = vcf.vcalendar2calendar(vcalendar).getEvent();

        RecurrencePattern rp = e.getRecurrencePattern();

        return fixEndDatePattern(rp, false);
    }

    /**
     * Fix the end date in the recurrence pattern.
     * Android saves the end date pattern as the first occurence to exclude
     * minus 1 second. According to the vCalendar specification the end date
     * "Controls when a repeating event terminates. The enddate is the last
     * time an event can occur."
     * So if the end date we read from the database is in the form:
     * 20101010T105959Z we should retrieve the last occurence previous to
     * that one.
     */
    private RecurrencePattern fixEndDatePattern(RecurrencePattern rp, boolean incoming) {
        if(rp != null) {
            String enddate = rp.getEndDatePattern();
            if((enddate != null) && 
                ((!incoming && (enddate.endsWith("59") || enddate.endsWith("59Z"))) ||
                   incoming)) {

                Log.debug(TAG_LOG, "Fixing end date in recurrence field");
                Log.debug(TAG_LOG, "Old end date: " + enddate);

                String tz = rp.getTimeZone();
                Time endDateTime = new Time(tz != null ? tz : "UTC");
                endDateTime.parse(enddate);
                if(incoming) {
                    endDateTime.second--;
                } else {
                    endDateTime.second++;
                }
                switch(rp.getTypeId()) {
                    case RecurrencePattern.TYPE_MONTHLY:
                        if(incoming) {
                            endDateTime.month++;
                        } else {
                            endDateTime.month--;
                        }
                        break;
                    case RecurrencePattern.TYPE_YEARLY:
                        if(incoming) {
                            endDateTime.year++;
                        } else {
                            endDateTime.year--;
                        }
                        break;
                    case RecurrencePattern.TYPE_DAILY:
                    default:
                        if(incoming) {
                            endDateTime.monthDay++;
                        } else {
                            endDateTime.monthDay--;
                        }
                        break;
                }
                endDateTime.normalize(false); // Do not ignore DST
                enddate = endDateTime.format2445();
                Log.debug(TAG_LOG, "New end date: " + enddate);
                rp.setEndDatePattern(enddate);
            }
        }
        return rp;
    }

    private VCalendarConverter getConverter(TimeZone tZone, boolean allday) {
        if (allday) {
            // For all day events we should use the device's default timezone in
            // order to convert the pattern end date to the related local time.
            return new VCalendarConverter(TimeZone.getDefault(), "UTF-8", false);
        } else {
            return new VCalendarConverter(tZone, "UTF-8", false);
        }
    }


    /**
     * Find the default native calendar if any.
     *
     * @return a calendar descriptor if the calendar was found
     */
    private CalendarDescriptor findNativeCalendar() {

        Log.trace(TAG_LOG, "Searching for native calendars");
        Cursor dcals = resolver.query(Calendars.CONTENT_URI, null, null, null, null);

        if (dcals == null) {
            return null;
        }

        CalendarDescriptor res = null;
        try {

            if (dcals.moveToFirst()) {
                do {
                    long calendarId = dcals.getLong(dcals.getColumnIndexOrThrow(Calendars._ID));
                    String calendarName = dcals.getString(dcals.getColumnIndexOrThrow(Calendars.DISPLAY_NAME));
                    String syncAccountType = dcals.getString(dcals.getColumnIndexOrThrow(Calendars._SYNC_ACCOUNT_TYPE));
                    String syncAccount = dcals.getString(dcals.getColumnIndexOrThrow(Calendars._SYNC_ACCOUNT));
                    String ownerAccount = dcals.getString(dcals.getColumnIndexOrThrow(Calendars.OWNER_ACCOUNT));

                    // There is some blackmagic here, because each manufacturer
                    // creates local calendars with different properties.
                    if (ownerAccount != null && ownerAccount.indexOf("default@") != -1) {
                        // This the personal calendar on HTC devices
                        res = new CalendarDescriptor(calendarId, calendarName);
                    } else if ("local".equals(syncAccount) && "local".equals(syncAccountType)) {
                        // This is the personal calendar on Samsung devices
                        res = new CalendarDescriptor(calendarId, calendarName);
                    } else {
                        Log.debug(TAG_LOG, "Calendar found with id: " + calendarId + " name: " + calendarName);
                        Log.debug(TAG_LOG, "Calendar found with sync account: " + syncAccount);
                        Log.debug(TAG_LOG, "Calendar found with sync account type: " + syncAccountType);
                        Log.debug(TAG_LOG, "Calendar found with owner account: " + ownerAccount);
                    }

                    if (res != null) {
                        break;
                    }
                } while(dcals.moveToNext());
            }
        } finally {
            dcals.close();
        }
        return res;
    }

    /**
     * Find an existing google calendar and returns its description.
     *
     * @return a CalendarDescriptor if the calendar was found
     */
    private CalendarDescriptor findGoogleCalendar() {

        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(GOOGLE_ACCOUNT_TYPE);

        if(accounts.length > 0) {
            String name = accounts[0].name;

            String whereClause = Calendars._SYNC_ACCOUNT_TYPE + "='" +
                    GOOGLE_ACCOUNT_TYPE + "' AND " + Calendars.NAME + "='" + name + "'";

            Cursor gcals = resolver.query(Calendars.CONTENT_URI,
                Calendars.PROJECTION, whereClause, null, null);

            if (gcals == null) {
                return null;
            }

            CalendarDescriptor res = null;
            try {
                if(gcals.moveToFirst()) {
                    long calendarId = gcals.getLong(gcals.getColumnIndexOrThrow(Calendars._ID));
                    String calendarName = gcals.getString(gcals.getColumnIndexOrThrow(Calendars.DISPLAY_NAME));
                    Log.debug(TAG_LOG, "Google calendar found with id: " + calendarId
                            + " name: " + name);
                    res = new CalendarDescriptor(calendarId, calendarName);
                } else {
                    Log.error(TAG_LOG, "Cannot find a Google Calendar with the same " +
                            "name of the account");
                }
            } finally {
                gcals.close();
            }
            return res;
        } else {
            Log.info(TAG_LOG, "Cannot find Google Calendars");
            return null;
        }
    }

    /**
     * Create a Funambol calendar
     */
    private CalendarDescriptor createFunambolCalendar() {

        Log.trace(TAG_LOG, "Creating Funambol Calendar");

        // Init the funambol account if not yet initialized
        if(accountName == null || accountType == null) {
            initAccount();
        }

        // Query all the calendars belonging to a funambol account
        Cursor cals = resolver.query(Calendars.CONTENT_URI,
                Calendars.PROJECTION, Calendars._SYNC_ACCOUNT_TYPE+"='"
                +accountType+"'", null, null);

        CalendarDescriptor res = null;

        try {
            if(cals == null || !cals.moveToFirst()) {
                // The funambol calendar doesn't exist -> create it if the account
                // information is correctly loaded
                Log.debug(TAG_LOG, "No Funambol calendar defined, create one (" + accountName + "," + accountType);
                if(accountName != null && accountType != null) {

                    String calDisplayName = context.getString(R.string.account_label);

                    ContentValues cv = new ContentValues();
                    cv.put(Calendars._SYNC_ACCOUNT,      accountName);
                    cv.put(Calendars._SYNC_ACCOUNT_TYPE, accountType);
                    cv.put(Calendars.DISPLAY_NAME,       calDisplayName);
                    cv.put(Calendars.OWNER_ACCOUNT,      accountName);
                    cv.put(Calendars.SYNC_EVENTS,        1);
                    cv.put(Calendars.ACCESS_LEVEL,       700);
                    cv.put(Calendars.COLOR,              FUNAMBOL_CALENDAR_COLOR);

                    Uri calUri = resolver.insert(Calendars.CONTENT_URI, cv);
                    long calendarId = Long.parseLong(calUri.getLastPathSegment());
                    String calendarName = calDisplayName;
                    res = new CalendarDescriptor(calendarId, calendarName);
                } else {
                    Log.error(TAG_LOG, "Cannot initialize calendar since there is " +
                            "no accounts defined.");
                }
            } else {
                long calendarId = cals.getLong(cals.getColumnIndexOrThrow(Calendars._ID));
                String calendarName = cals.getString(cals.getColumnIndexOrThrow(Calendars.DISPLAY_NAME));
                res = new CalendarDescriptor(calendarId, calendarName);
                Log.debug(TAG_LOG, "Calendar found with id: " + calendarId);
            }
        } finally {
            if (cals != null) {
                cals.close();
            }
        }
        return res;
    }

    /**
     * Get the Account information related to the given calendar id
     * 
     * @param calendarId
     * @return
     */
    private AccountInfo getCalendarAccount(long calendarId) throws IOException {

        AccountInfo account = calendarAccountReference.get(calendarId);
        if(account != null) {
            return account;
        }

        // Retrieve account info from the db
        String[] projection = { Calendars._ID, Calendars._SYNC_ACCOUNT,
            Calendars._SYNC_ACCOUNT_TYPE };

        Cursor cal = resolver.query(Calendars.CONTENT_URI, projection,
                Calendars._ID+"="+calendarId, null, null);

        try {
            if(cal != null && cal.moveToFirst()) {
                String name = cal.getString(1); // _SYNC_ACCOUNT
                String type = cal.getString(2); // _SYNC_ACCOUNT_TYPE
                account = new AccountInfo(name, type);
                calendarAccountReference.put(calendarId, account);
                return account;
            } else {
                Log.error(TAG_LOG, "Cannot find calendar with id: " + calendarId);
                throw new IOException("Cannot find calendar with id: " + calendarId);
            }
        } finally {
            cal.close();
        }
    }

    private Uri addCallerIsSyncAdapterFlag(Uri uri) {
        if(callerIsSyncAdapter) {
            Uri.Builder b = uri.buildUpon();
            b.appendQueryParameter(CALLER_IS_SYNCADAPTER, "true");
            return b.build();
        } else {
            return uri;
        }
    }

    private class AccountInfo {
        public String name = null;
        public String type = null;
        public AccountInfo(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

}
