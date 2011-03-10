/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2003 - 2010 Funambol, Inc.
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


package com.funambol.android;

import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ArrayList;

import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import com.funambol.android.source.pim.PimTestRecorder;

import com.funambol.android.source.pim.calendar.Calendar;
import com.funambol.android.source.pim.calendar.CalendarAppSyncSourceConfig;
import com.funambol.android.source.pim.calendar.CalendarManager;
import com.funambol.android.source.pim.calendar.CalendarManager.Events;

import com.funambol.common.pim.model.calendar.Attendee;
import com.funambol.common.pim.model.calendar.Event;
import com.funambol.common.pim.model.calendar.Reminder;
import com.funambol.common.pim.model.common.Property;

import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.test.CalendarCommandRunner;
import com.funambol.client.test.CalendarRobot;
import com.funambol.client.test.CheckSyncClient;
import com.funambol.client.test.CheckSyncSource;
import com.funambol.client.test.ClientTestException;
import com.funambol.client.test.BasicRobot;

import com.funambol.common.pim.model.calendar.ExceptionToRecurrenceRule;
import com.funambol.common.pim.model.calendar.RecurrencePattern;
import com.funambol.common.pim.model.common.PropertyWithTimeZone;
import com.funambol.common.pim.model.utility.TimeUtils;
import com.funambol.syncml.client.TrackableSyncSource;

import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.syncml.spds.SyncException;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.util.Log;
import com.funambol.util.StringUtil;
import java.util.TimeZone;

/**
 * This is a robot implementation that owns the logic of the commands to write 
 * vcalendar integration tests
 */
public class AndroidCalendarsRobot extends CalendarRobot{

    private static final String TAG_LOG = "AndroidCalendarsRobot";

    private Instrumentation instrumentation = null;

    private CalendarManager cm = null;
    private Event currentEvent = null;

    private AppInitializer appInitializer;
    
    private AppSyncSource appSyncSource;

    private AndroidAppSyncSourceManager appSyncSourceManager;

    private ContentValues recurrenceValues = new ContentValues();

    private Hashtable supportedValues;

    private ContentValues cv;

    private ContentValues rawEventValues;
    private ContentValues rawReminderValues;
    private long lastRawEventId = -1;
    private String lastCheckedEventId = null;

    
    private static final String[] SUPPORTED_FIELDS = {
        "DTSTART",
        "DTEND", 
        "SUMMARY",
        "DESCRIPTION",
        "LOCATION",
        "X-FUNAMBOL-ALLDAY",
        "ATTENDEE",
        "CLASS",
        "AALARM",
        "DURATION"
    };

    /**
     * Default constructor
     * @param instrumentation the Instrumentation object useful to retrieve both the 
     * context and the resolver for calendars
     */
    public AndroidCalendarsRobot(Instrumentation instrumentation, BasicRobot basicRobot) {

        this.basicRobot = basicRobot;

        this.instrumentation = instrumentation;
        
        appInitializer = AppInitializer.getInstance(instrumentation.getContext());

        appInitializer.init();

        appSyncSourceManager = appInitializer.getAppSyncSourceManager();

        appSyncSource = appSyncSourceManager.getSource(AppSyncSourceManager.EVENTS_ID);

        supportedValues = new Hashtable();
    }

    @Override
    /**
     * Parse the value for events' fields input by the script. 
     * @param field the field to be set
     * @param value the value related to the field to be set
     * @throws Throwable
     */
    public void setEventField(String field, String value) throws Throwable {
        if(currentEvent == null) {
            throw new ClientTestException("You have to inizialize the event before editing it");
        }
        if(CalendarCommandRunner.EVENT_FIELD_START.equals(field)) {
            currentEvent.setDtStart(new Property(value));
        } else if(CalendarCommandRunner.EVENT_FIELD_END.equals(field)) {
            currentEvent.setDtEnd(new Property(value));
        } else if(CalendarCommandRunner.EVENT_FIELD_SUMMARY.equals(field)) {
            currentEvent.setSummary(new Property(value));
        } else if(CalendarCommandRunner.EVENT_FIELD_DESCRIPTION.equals(field)) {
            currentEvent.setDescription(new Property(value));
        } else if(CalendarCommandRunner.EVENT_FIELD_LOCATION.equals(field)) {
            currentEvent.setLocation(new Property(value));
        } else if(CalendarCommandRunner.EVENT_FIELD_ALLDAY.equals(field)) {
            boolean isAllDay = value.equals("1");
            currentEvent.setAllDay(isAllDay);
        } else if(CalendarCommandRunner.EVENT_FIELD_ATTENDEES.equals(field)) {
            Attendee a = new Attendee();
            a.setEmail(value);
            currentEvent.getAttendees().add(a);
        } else if(CalendarCommandRunner.EVENT_FIELD_REMINDER.equals(field)) {
            Reminder r = new Reminder();
            r.setMinutes(Integer.parseInt(value));
            currentEvent.setReminder(r);
        } else if (CalendarCommandRunner.EVENT_FIELD_DURATION.equals(field)) {
            currentEvent.setDuration(new Property(value));
        } else if(CalendarCommandRunner.EVENT_FIELD_TIMEZONE.equals(field)) {
            throw new ClientTestException(CalendarCommandRunner.EVENT_FIELD_TIMEZONE
                    + " field not yet implemented");
        } else {
            throw new ClientTestException("Unknown field: " + field);
        }
    }
    
    /**
     * Getter to retrieve the CalendarManager instance
     * @return CalendarManager the Singleton instance of the Calendar manager 
     */
    private CalendarManager getCalendarManager() throws SyncException {
        if(cm == null) {
            cm = new CalendarManager(instrumentation.getContext(), appSyncSource, false);
            // At the moment we need to invoke the beginSync method at least once to make sure
            // the config is properly initialized. At the beginning of a sync we
            // check if the calendar is properly set and reset it if necessary
            SyncSource calSource = appSyncSource.getSyncSource();
            calSource.beginSync(SyncML.ALERT_CODE_FAST);
        }
        return cm;
    }

    @Override
    /**
     * Finishes the vcal formatting and saves the event
     * @throws Throwable
     */
    public void saveEvent() throws Throwable {

        Calendar calendar = new Calendar();
        
        // If the event has been set directly with the entire vCal string
        // we simply replace it
        if(eventAsVcal != null) {
            calendar.setVCalendar(eventAsVcal.toString().getBytes());
            currentEvent = calendar.getEvent();
        } else {
            if(recurrenceValues != null && recurrenceValues.size() > 0) {
                short frequency = -1;
                if(recurrenceValues.containsKey(CalendarCommandRunner.EVENT_REC_FIELD_FREQUENCY)) {
                    frequency = recurrenceValues.getAsShort(CalendarCommandRunner.EVENT_REC_FIELD_FREQUENCY);
                }
                int interval = 1;
                if(recurrenceValues.containsKey(CalendarCommandRunner.EVENT_REC_FIELD_INTERVAL)) {
                    interval = recurrenceValues.getAsInteger(CalendarCommandRunner.EVENT_REC_FIELD_INTERVAL);
                }
                int occurences = 0;
                if(recurrenceValues.containsKey(CalendarCommandRunner.EVENT_REC_FIELD_OCCURRENCES)) {
                    occurences = recurrenceValues.getAsInteger(CalendarCommandRunner.EVENT_REC_FIELD_OCCURRENCES);
                }
                short monthOfYear = 0;
                if(recurrenceValues.containsKey(CalendarCommandRunner.EVENT_REC_FIELD_MONTH_OF_YEAR)) {
                    monthOfYear = recurrenceValues.getAsShort(CalendarCommandRunner.EVENT_REC_FIELD_MONTH_OF_YEAR);
                }
                short dayOfMonth = 0;
                if(recurrenceValues.containsKey(CalendarCommandRunner.EVENT_REC_FIELD_DAY_OF_MONTH)) {
                    dayOfMonth = recurrenceValues.getAsShort(CalendarCommandRunner.EVENT_REC_FIELD_DAY_OF_MONTH);
                }
                short dayOfWeek = 0;
                if(recurrenceValues.containsKey(CalendarCommandRunner.EVENT_REC_FIELD_DAY_OF_WEEK)) {
                    dayOfWeek = recurrenceValues.getAsShort(CalendarCommandRunner.EVENT_REC_FIELD_DAY_OF_WEEK);
                }
                short instance = 0;
                if(recurrenceValues.containsKey(CalendarCommandRunner.EVENT_REC_FIELD_INSTANCE)) {
                    instance = recurrenceValues.getAsShort(CalendarCommandRunner.EVENT_REC_FIELD_INSTANCE);
                }
                String startDate = null;
                if(recurrenceValues.containsKey(CalendarCommandRunner.EVENT_REC_FIELD_START_DATE)) {
                    startDate = recurrenceValues.getAsString(CalendarCommandRunner.EVENT_REC_FIELD_START_DATE);
                }
                String endDate = null;
                if(recurrenceValues.containsKey(CalendarCommandRunner.EVENT_REC_FIELD_END_DATE)) {
                    endDate = recurrenceValues.getAsString(CalendarCommandRunner.EVENT_REC_FIELD_END_DATE);
                }

                // Set the event recurrence pattern
                RecurrencePattern rp = new RecurrencePattern(
                        frequency,
                        interval,
                        monthOfYear,
                        dayOfMonth,
                        dayOfWeek,
                        instance,
                        startDate,
                        endDate,
                        endDate == null,
                        occurences);

                // Set recurrence exceptions
                ArrayList<ExceptionToRecurrenceRule> exceptions =
                        new ArrayList<ExceptionToRecurrenceRule>();

                String exdate = recurrenceValues.getAsString(CalendarCommandRunner.EVENT_REC_FIELD_EXCEPTIONS);
                if(exdate != null) {
                    String[] exdates = StringUtil.split(exdate, ";");
                    for(String date : exdates) {
                        exceptions.add(new ExceptionToRecurrenceRule(false, date));
                    }
                }
                String rdate  = recurrenceValues.getAsString(CalendarCommandRunner.EVENT_REC_FIELD_EXCEPTIONS_ADD);
                if(rdate != null) {
                    String[] rdates = StringUtil.split(rdate, ";");
                    for(String date : rdates) {
                        exceptions.add(new ExceptionToRecurrenceRule(true, date));
                    }
                }
                rp.setExceptions(exceptions);
                currentEvent.setRecurrencePattern(rp);
            }

            // If the dtstart/dtend are not set to 0, the provider complains
            // so we set them here
            if (currentEvent.isAllDay()) {
                PropertyWithTimeZone start = currentEvent.getDtStart();
                PropertyWithTimeZone end   = currentEvent.getDtEnd();

                start = toMidnight(start);
                end   = toMidnight(end);

                currentEvent.setDtStart(start);
                currentEvent.setDtEnd(end);
            }

            calendar.setEvent(currentEvent);
        }

        // Check if summary is set
        if(currentEvent.getSummary() == null) {
            throw new ClientTestException("You must set summary before saving the event");
        }

        if(currentEventId != -1) {
            getCalendarManager().update(currentEventId, calendar);
        } else {
            getCalendarManager().add(calendar);
        }

        // Reset current event
        currentEvent = null;
        eventAsVcal = null;
        recurrenceValues = null;
        currentEventId = -1;
    }

    private PropertyWithTimeZone toMidnight(PropertyWithTimeZone dateTime) {
        String value = dateTime.getPropertyValueAsString();
        if(value != null) {
            int tPos = value.indexOf("T");
            if (tPos != -1) {
                String newValue = value.substring(0, tPos);
                Log.trace(TAG_LOG, "toMidnight returning: " + newValue);
                PropertyWithTimeZone res = new PropertyWithTimeZone(newValue, dateTime.getTimeZone());
                return res;
            }
        }
        return dateTime;
    }

    @Override
    /**
     * Delete all events from the device
     * @throws Throwable
     */
    public void deleteAllEvents() throws Throwable {

        // Reset the tracker
        AppSyncSource appSource = appSyncSourceManager.getSource(AppSyncSourceManager.EVENTS_ID);
        TrackableSyncSource source = (TrackableSyncSource)appSource.getSyncSource();
        source.getTracker().empty();

        // Phisically delete all the items from the store
        ContentResolver cr = instrumentation.getTargetContext().getContentResolver();
        Enumeration keys = getCalendarManager().getAllKeys();
        while(keys.hasMoreElements()) {
            long itemId = Long.parseLong((String) keys.nextElement());
            Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, itemId);
            cr.delete(uri, null, null);
        }
    }

    @Override
    /**
     * Delete one events from the device
     * @param summary the summary of the event
     * @throws Throwable
     */
    public void deleteEvent(String summary) throws Throwable {
        ContentResolver cr = instrumentation.getTargetContext().getContentResolver();
        long itemId = findEventKey(summary);
        Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, itemId);
        cr.delete(uri, null, null);
    }


    @Override
    /**
     * Check that an event has been deleted on the device
     * @param summary the summary of the event to be deleted
     * @param client the CheckSyncClient object
     * @throws Throwable
     */
    public void checkDeletedEvent(String summary, CheckSyncClient client)
            throws Throwable {
        try {
            findEventKey(summary);
            throw new ClientTestException("Deleted item found: " + summary);
        } catch(ClientTestException ex) {
            // OK Item not found
        }
    }

    @Override
    /**
     * checks that an event was deleted on the server
     * @param summary the summary of the event to be deleted
     * @param client the CheckSyncClient object
     * @throws Throwable
     */
    public void checkDeletedEventOnServer(String summary, CheckSyncClient client)
            throws Throwable {
        try {
            findEventKeyOnServer(summary, client);
            throw new ClientTestException("Deleted item found on server: " + summary);
        } catch(ClientTestException ex) {
            // OK Item not found
        }        
    }

    @Override
    /**
     * checks that an event was created on the device
     * @param summary the summary of the event to be created
     * @param client the CheckSyncClient object
     * @param checkContent the boolean that allows the deep event's content check
     * @throws Throwable
     */
    public void checkNewEvent(String summary, CheckSyncClient client,
            boolean checkContent) throws Throwable {
        CheckSyncSource source = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CALENDAR);

        String key = findEventKeyOnServer(summary, client);

        Calendar remote = new Calendar();

        SyncItem item = new SyncItem(key);
        item = source.getItemContent(item);
        remote.setVCalendar(item.getContent());

        Calendar local = getCalendarManager().load(findEventKey(summary));

        checkEvent(local, remote, checkContent);
    }
    
    @Override
    /**
     * checks that an event was created on the server
     * @param summary the summary of the event to be created
     * @param client the CheckSyncClient object
     * @param checkContent the boolean that allows the deep event's content check
     * @throws Throwable
     */
    public void checkNewEventOnServer(String summary, CheckSyncClient client,
            boolean checkContent) throws Throwable {
        Calendar local = getCalendarManager().load(findEventKey(summary));
        Enumeration items = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CALENDAR).getAddedItems();

        checkEventOnServer(summary, checkContent, local, items);
        
    }

    @Override
    /**
     * checks that an event was updated on the device
     * @param summary the summary of the event to be created
     * @param client the CheckSyncClient object
     * @param checkContent the boolean that allows the deep event's content check
     * @throws Throwable
     */
    public void checkUpdatedEvent(String summary, CheckSyncClient client,
            boolean checkContent) throws Throwable {
        checkNewEvent(summary, client, checkContent);
    }

    @Override
    /**
     * checks that an event was updated on the server
     * @param summary the summary of the event to be created
     * @param client the CheckSyncClient object
     * @param checkContent the boolean that allows the deep event's content check
     * @throws Throwable
     */
    public void checkUpdatedEventOnServer(String summary,
            CheckSyncClient client, boolean checkContent) throws Throwable {
        Calendar local = getCalendarManager().load(findEventKey(summary));
        Enumeration items = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CALENDAR).getUpdatedItems();

        checkEventOnServer(summary, checkContent, local, items);
    }

    @Override
    /**
     * Create an empty event filling the event vcal
     */
    public void createEmptyEvent() throws Throwable {
        currentEvent = new Event();
    }

    @Override
    protected AppSyncSourceManager getAppSyncSourceManager() {
        if (appSourceManager == null) {
            AppInitializer appInitializer = AppInitializer.getInstance(instrumentation.getContext());
            appSourceManager = appInitializer.getAppSyncSourceManager();
        }
        return appSourceManager;
    }

    @Override
    /**
     * Find a key related to an event on the server
     * @param summary the summary of the event to be found
     * @param client the CheckSyncClient object
     * @throws Throwable
     */
    protected String findEventKeyOnServer(String summary, CheckSyncClient client)
            throws Throwable {
        Hashtable<String,SyncItem> allItems = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CALENDAR).getAllItems();

        Enumeration<SyncItem> allElements = allItems.elements();
        
        while(allElements.hasMoreElements()) {
            SyncItem item = allElements.nextElement();
            Calendar c = new Calendar();
            c.setVCalendar(item.getContent());

            Log.trace(TAG_LOG, "Found event with summary: " + c.getEvent().getSummary().getPropertyValueAsString());

            if(c.getEvent().getSummary().getPropertyValueAsString().equals(summary)) {
               return item.getKey();
            }
        }
        throw new ClientTestException("Can't find event on server: " + summary);
    }

    @Override
    /**
     * Load an event on the device
     * @param summary the summary of the event to be loaded
     * @throws Throwable
     */
    public void loadEvent(String summary) throws Throwable {
        currentEventId = findEventKey(summary);
        currentEvent = getCalendarManager().load(currentEventId).getEvent();
    }

    @Override
    /**
     * Load an event on the server
     * @param summary the summary of the event to be found
     * @param client the CheckSyncClient object
     * @throws Throwable
     */
    public void loadEventOnServer(String summary, CheckSyncClient client)
            throws Throwable {

        CheckSyncSource source = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CALENDAR);

        String key = findEventKeyOnServer(summary, client);
        currentEventId = Long.parseLong(key);
        Calendar c = new Calendar();

        SyncItem item = new SyncItem(key);
        item = source.getItemContent(item);
        c.setVCalendar(item.getContent());

        currentEvent = c.getEvent();
    }


    @Override
    /**
     * Set a Event recurrence field:
     *
     *  Field          | Type    | Value
     * -------------------------------------------------------------------------
     *  Frequency      | String  | None Daily Weekly Monthly MonthlyNth YearlyNth
     *  Interval       | Integer | -
     *  StartDate      | String  | Date/Time
     *  EndDate        | String  | Date/Time
     *  DayOfWeek      | Short   | SU MO TU WE TH FR SA
     *  DayOfMonth     | Short   | 1-31
     *  MonthOfYear    | Short   | 1-12
     *  Instance       | Short   | -
     *  Occurrences    | Integer | -
     *  Exceptions     | String  | Date/Time
     *  ExceptionsAdd  | String  | Date/Time
     *
     */
    public void setEventRecurrenceField(String recField, String value)
            throws Throwable {
        if(recurrenceValues == null) {
            recurrenceValues = new ContentValues();
        }
        if (CalendarCommandRunner.EVENT_REC_FIELD_FREQUENCY.equals(recField)) {
            short type;
            if(StringUtil.equalsIgnoreCase(value, CalendarCommandRunner.FREQUENCY_TYPE_DAILY)) {
                type = 0;
            } else if(StringUtil.equalsIgnoreCase(value, CalendarCommandRunner.FREQUENCY_TYPE_WEEKLY)) {
                type = 1;
            } else if(StringUtil.equalsIgnoreCase(value, CalendarCommandRunner.FREQUENCY_TYPE_MONTHLY)) {
                type = 2;
            } else if(StringUtil.equalsIgnoreCase(value, CalendarCommandRunner.FREQUENCY_TYPE_MONTHLY_NTH)) {
                type = 3;
            } else if(StringUtil.equalsIgnoreCase(value, CalendarCommandRunner.FREQUENCY_TYPE_YEARLY)) {
                type = 5;
            } else if(StringUtil.equalsIgnoreCase(value, CalendarCommandRunner.FREQUENCY_TYPE_YEARLY_NTH)) {
                type = 6;
            } else {
                throw new ClientTestException("Unsupported Frequency " + value);
            }
            recurrenceValues.put(CalendarCommandRunner.EVENT_REC_FIELD_FREQUENCY, type);
        } else if (CalendarCommandRunner.EVENT_REC_FIELD_INTERVAL.equals(recField)) {
            recurrenceValues.put(CalendarCommandRunner.EVENT_REC_FIELD_INTERVAL, Integer.parseInt(value));
        } else if (CalendarCommandRunner.EVENT_REC_FIELD_DAY_OF_WEEK.equals(recField)) {
            short dayOfWeekMask = 0;
            String[] days = StringUtil.split(value, " ");
            for(String day : days) {
                if(CalendarCommandRunner.DAY_OF_WEEK_SUNDAY.equals(day)) {
                    dayOfWeekMask += RecurrencePattern.DAY_OF_WEEK_SUNDAY;
                } else if(CalendarCommandRunner.DAY_OF_WEEK_MONDAY.equals(day)) {
                    dayOfWeekMask += RecurrencePattern.DAY_OF_WEEK_MONDAY;
                } else if(CalendarCommandRunner.DAY_OF_WEEK_TUESDAY.equals(day)) {
                    dayOfWeekMask += RecurrencePattern.DAY_OF_WEEK_TUESDAY;
                } else if(CalendarCommandRunner.DAY_OF_WEEK_WEDNESDAY.equals(day)) {
                    dayOfWeekMask += RecurrencePattern.DAY_OF_WEEK_WEDNESDAY;
                } else if(CalendarCommandRunner.DAY_OF_WEEK_THURSDAY.equals(day)) {
                    dayOfWeekMask += RecurrencePattern.DAY_OF_WEEK_THURSDAY;
                } else if(CalendarCommandRunner.DAY_OF_WEEK_FRIDAY.equals(day)) {
                    dayOfWeekMask += RecurrencePattern.DAY_OF_WEEK_FRIDAY;
                } else if(CalendarCommandRunner.DAY_OF_WEEK_SATURDAY.equals(day)) {
                    dayOfWeekMask += RecurrencePattern.DAY_OF_WEEK_SATURDAY;
                }
            }
            recurrenceValues.put(CalendarCommandRunner.EVENT_REC_FIELD_DAY_OF_WEEK, dayOfWeekMask);
        } else if (CalendarCommandRunner.EVENT_REC_FIELD_DAY_OF_MONTH.equals(recField)) {
            recurrenceValues.put(CalendarCommandRunner.EVENT_REC_FIELD_DAY_OF_MONTH, Short.parseShort(value));
        } else if (CalendarCommandRunner.EVENT_REC_FIELD_MONTH_OF_YEAR.equals(recField)) {
            recurrenceValues.put(CalendarCommandRunner.EVENT_REC_FIELD_MONTH_OF_YEAR, Short.parseShort(value));
        } else if (CalendarCommandRunner.EVENT_REC_FIELD_INSTANCE.equals(recField)) {
            recurrenceValues.put(CalendarCommandRunner.EVENT_REC_FIELD_INSTANCE, Short.parseShort(value));
        } else if (CalendarCommandRunner.EVENT_REC_FIELD_END_DATE.equals(recField)) {
            recurrenceValues.put(CalendarCommandRunner.EVENT_REC_FIELD_END_DATE, value);
        } else if (CalendarCommandRunner.EVENT_REC_FIELD_START_DATE.equals(recField)) {
            recurrenceValues.put(CalendarCommandRunner.EVENT_REC_FIELD_START_DATE, value);
        } else if (CalendarCommandRunner.EVENT_REC_FIELD_OCCURRENCES.equals(recField)) {
            recurrenceValues.put(CalendarCommandRunner.EVENT_REC_FIELD_OCCURRENCES, Integer.parseInt(value));
        } else if (CalendarCommandRunner.EVENT_REC_FIELD_EXCEPTIONS.equals(recField)) {
            recurrenceValues.put(CalendarCommandRunner.EVENT_REC_FIELD_EXCEPTIONS, value);
        } else if (CalendarCommandRunner.EVENT_REC_FIELD_EXCEPTIONS_ADD.equals(recField)) {
            recurrenceValues.put(CalendarCommandRunner.EVENT_REC_FIELD_EXCEPTIONS_ADD, value);
        } else {
             throw new ClientTestException("Unsupported recurrence field " + recField);
        }
    }

    @Override
    /**
     * Returns a string representation of the current event
     * @return String the vCal formatted String
     */
    protected String getCurrentEventVCal() throws Throwable {
        if (eventAsVcal != null) {
            return eventAsVcal;
        } else {
            Calendar c = new Calendar();
            c.setEvent(currentEvent);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            c.toVCalendar(baos, true);

            return new String(baos.toByteArray());
        }
    }

    private void assertEquals(Calendar c1, Calendar c2, String msg) throws ClientTestException {

        String expectedStr = null;
        String resultStr = null;

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            c1.toVCalendar(os, true);
            byte[] c1Ba = os.toByteArray();

            os = new ByteArrayOutputStream();
            c2.toVCalendar(os, true);
            byte[] c2Ba = os.toByteArray();

            expectedStr = orderVCal(new String(c1Ba), SUPPORTED_FIELDS, supportedValues);
            resultStr = orderVCal(new String(c2Ba), SUPPORTED_FIELDS, supportedValues);

            assertTrue(resultStr.equals(expectedStr), msg);

        } catch(ClientTestException ex) {
            Log.error(TAG_LOG, "Expected: " + expectedStr + " -- Found: " + resultStr);
            throw new ClientTestException(msg);
        } catch(Exception ex) {
            throw new ClientTestException(msg);
        }
    }    
    
    private void checkEventOnServer(String summary, boolean checkContent, Calendar local, Enumeration items) throws Throwable {

        Log.trace(TAG_LOG, "Checking event on server");

        while(items.hasMoreElements()) {

            SyncItem syncItem = (SyncItem)items.nextElement();
            byte[] remote = syncItem.getContent();

            Calendar remoteCalendar = new Calendar();

            remoteCalendar.setVCalendar(remote);
            
            Log.trace(TAG_LOG, "Found remote event: " + remoteCalendar.getEvent().getSummary().getPropertyValueAsString());

            if(remoteCalendar.getEvent().getSummary().getPropertyValueAsString().equals(summary)) {
                if(checkContent) {

                    //ByteArrayOutputStream os = new ByteArrayOutputStream();
                    //remoteCalendar.toVCalendar(os, true);
                    //Log.trace(TAG_LOG, "Remote calendar");
                    //Log.trace(TAG_LOG, os.toString());
                    Log.trace(TAG_LOG, "Remote is");
                    Log.trace(TAG_LOG, new String(remote));

                    assertEquals(local, remoteCalendar, "Events mismatch");
                }
                return;
            }
        }
        throw new ClientTestException("Can't find event on server: " + summary);
    }

    private void checkEvent(Calendar local, Calendar remote,
            boolean checkContent) throws Throwable {
        if(checkContent) {
            assertEquals(local, remote, "Events mismatch");
        }
    }

    private long findEventKey(String summary) throws Throwable {
        Enumeration allkeys = getCalendarManager().getAllKeys();
        while(allkeys.hasMoreElements()) {
            long key = Long.parseLong((String)allkeys.nextElement());
            Calendar calendar = getCalendarManager().load(key);
            if(calendar.getEvent().getSummary().getPropertyValueAsString().equals(summary)) {
                return key;
            }
        }
        throw new ClientTestException("Can't find event: " + summary);
    }

    @Override
    public void checkEventRecRule(String summary, String rrule) throws Throwable{
        Calendar cal = findEventBySummary(summary);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        cal.toVCalendar(os, true);
        byte[] calBa = os.toByteArray();

        String[] fields = new String[]{"RRULE"};
        String resultStr = orderVCal(new String(calBa), fields, supportedValues);
        resultStr = resultStr.trim();
        resultStr = resultStr.substring(resultStr.lastIndexOf(':')+1);

        assertTrue(resultStr, rrule, "Recurrence fields mismatch");
    }

    public void checkEventAsVCal(String summary, String vcal) throws Throwable{
        Calendar cal = findEventBySummary(summary);

        // Unescape the user string
        vcal = StringUtil.replaceAll(vcal, "\\r\\n","\r\n");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        cal.toVCalendar(os, true);

        assertTrue(vcal, os.toString(), "VCal comparison mismatch");
    }

    public void checkEventExceptions(String summary, String exc) throws Throwable {
        throw new ClientTestException("CheckEventExceptions not implemented");
    }

    public void createEmptyRawEvent() throws Throwable {
        rawEventValues = new ContentValues();
        rawReminderValues = new ContentValues();
    }

    public void setRawEventField(String fieldName, String fieldValue) throws Throwable {

        boolean skip = false;

        // There are some fields that some providers do not allow to write. We
        // shall skip them
        if (!PimTestRecorder.isFieldAllowed(fieldName))  {
            Log.trace(TAG_LOG, "Skipping raw field " + fieldName);
            skip = true;
        }
        if (!skip) {
            rawEventValues.put(fieldName, fieldValue);
        }
    }

    public void setRawReminderField(String fieldName, String fieldValue) throws Throwable {

        boolean skip = false;

        // There are some fields that the provider does not allow to write. We
        // shall skip them
        if (fieldName.equals(CalendarManager.Reminders._ID) ||
            fieldName.equals(CalendarManager.Reminders.EVENT_ID))
        {
            skip = true;
            Log.trace(TAG_LOG, "Skipping raw field " + fieldName);
        }

        if (!skip) {
            rawReminderValues.put(fieldName, fieldValue);
        }
    }

    public void saveRawEvent() throws Throwable {
        ContentResolver resolver = instrumentation.getContext().getContentResolver();

        // Last think to do before saving the event is to set the related calendar_id field
        AppSyncSource appSource = appSyncSourceManager.getSource(AppSyncSourceManager.EVENTS_ID);
        CalendarAppSyncSourceConfig config = (CalendarAppSyncSourceConfig)appSource.getConfig();
        rawEventValues.put("calendar_id", config.getCalendarId());

        Uri eventUri = resolver.insert(CalendarManager.Events.CONTENT_URI, rawEventValues);
        lastRawEventId = Long.parseLong(eventUri.getLastPathSegment());

        // Set the event id
        rawReminderValues.put(CalendarManager.Reminders.EVENT_ID, "" + lastRawEventId);

        // Now save the reminder
        resolver.insert(CalendarManager.Reminders.CONTENT_URI, rawReminderValues);
    }

    public void checkRawEventAsVCal(String vcal) throws Throwable {
        vcal = StringUtil.replaceAll(vcal, "\\r\\n", "\r\n");

        Calendar cal = getCalendarManager().load(lastRawEventId);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        cal.toVCalendar(os, true);

        assertTrue(os.toString(), vcal, "VCalendar mismatch");
    }

    public void checkRawEventField(String fieldName, String fieldValue) throws Throwable {
        // We must load the values in the DB and compare them to the given
        // representation
        ContentResolver cr = instrumentation.getTargetContext().getContentResolver();
        AppSyncSource appSource = appSyncSourceManager.getSource(AppSyncSourceManager.EVENTS_ID);
        CalendarAppSyncSourceConfig config = (CalendarAppSyncSourceConfig)appSource.getConfig();

        Cursor cursor = cr.query(CalendarManager.Events.CONTENT_URI, null,
                CalendarManager.Events.CALENDAR_ID + "='" + config.getCalendarId() + "'", null, null);
        try {
            if(cursor != null && cursor.moveToFirst()) {
                if(PimTestRecorder.isFieldAllowed(fieldName)) {
                    String localValue = cursor.getString(
                            cursor.getColumnIndexOrThrow(fieldName));
                    if (fieldName.equals("_id")) {
                        lastCheckedEventId = localValue;
                    }
                    // Unescape commas
                    fieldValue = StringUtil.replaceAll(fieldValue, "?-?", ",");
                    // Unescape CRLF
                    fieldValue = StringUtil.replaceAll(fieldValue, "\\r", "\r");
                    fieldValue = StringUtil.replaceAll(fieldValue, "\\n", "\n");

                    // Reserve a special behaviour for some fields
                    if(fieldName.equals("eventTimezone")) {
                        TimeZone timezone = TimeZone.getTimeZone(fieldValue);
                        TimeZone localTimezone = TimeZone.getTimeZone(
                                localValue);
                        assertTrue(localTimezone.hasSameRules(timezone),
                            "Raw field mismatch: " + fieldName);
                    } else if(fieldName.equals("duration") &&
                            Integer.parseInt(Build.VERSION.SDK) < 8 &&
                            isAllDay(cursor)) {
                        // In Android 2.2 the duration for allday events is 
                        // converted in P<days>D format. Other versions keeps
                        // the duration in seconds.
                        long minutes = TimeUtils.getAlarmInterval(localValue);
                        minutes++;
                        int days = (int)((minutes / 60) / 24);
                        if(days == 0) {
                            days = 1;
                        }
                        StringBuffer duration = new StringBuffer(10);
                        duration.append("P");
                        duration.append(days);
                        duration.append("D");
                        assertTrue(fieldValue, duration.toString(),
                            "Raw field mismatch: " + fieldName);
                    } else {
                        assertTrue(fieldValue, localValue,
                            "Raw field mismatch: " + fieldName);
                    }
                } else {
                    Log.debug(TAG_LOG, "Skipping unsupported field: " + fieldName);
                }
            } else {
                // Item not found
                throw new IllegalStateException("No events in the db");
            }
        } finally {
            cursor.close();
        }
    }

    private boolean isAllDay(Cursor cursor) {
        String allday = cursor.getString(cursor.getColumnIndexOrThrow("allDay"));
        return "1".equals(allday);
    }

    public void checkRawReminderField(String fieldName, String fieldValue) throws Throwable {
        // We must load the values in the DB and compare them to the given
        // representation
        ContentResolver cr = instrumentation.getTargetContext().getContentResolver();

        Cursor cursor = cr.query(CalendarManager.Reminders.CONTENT_URI, null,
                CalendarManager.Reminders.EVENT_ID + "='" + lastCheckedEventId + "'", null, null);
        try {
            if(cursor != null && cursor.moveToFirst()) {
                if(PimTestRecorder.isFieldAllowed(fieldName)) {
                    String localValue = cursor.getString(
                            cursor.getColumnIndexOrThrow(fieldName));
                    if (fieldName.equals("_id")) {
                        lastCheckedEventId = localValue;
                    }
                    // Unescape commas
                    fieldValue = StringUtil.replaceAll(fieldValue, "?-?", ",");
                    // Unescape CRLF
                    fieldValue = StringUtil.replaceAll(fieldValue, "\\r", "\r");
                    fieldValue = StringUtil.replaceAll(fieldValue, "\\n", "\n");
                    assertTrue(fieldValue, localValue.toString(), 
                            "Reminder raw field mismatch: " + fieldName);
                } else {
                    Log.debug(TAG_LOG, "Skipping unsupported field: " + fieldName);
                }
            } else {
                // Item not found
                throw new IllegalStateException("No reminders in the db");
            }
        } finally {
            cursor.close();
        }
    }

    private Calendar findEventBySummary(String summary) throws Throwable {
        return getCalendarManager().load(findEventKey(summary));
    }

}
