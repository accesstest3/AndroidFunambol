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

import com.funambol.client.test.*;

public class CalendarCommandRunner extends CommandRunner implements EventsUserCommands {

    private static final String TAG_LOG = "CalendarCommandRunner";

    // Event fields used by SetEventField and EmptyEventField commands
    public static final String EVENT_FIELD_SUMMARY      = "Summary";
    public static final String EVENT_FIELD_LOCATION     = "Location";
    public static final String EVENT_FIELD_START        = "Start";
    public static final String EVENT_FIELD_END          = "End";
    public static final String EVENT_FIELD_ALLDAY       = "AllDay";
    public static final String EVENT_FIELD_DESCRIPTION  = "Description";
    public static final String EVENT_FIELD_ATTENDEES    = "Attendees";
    public static final String EVENT_FIELD_TIMEZONE     = "Timezone";
    public static final String EVENT_FIELD_REMINDER     = "Reminder";
    public static final String EVENT_FIELD_DURATION     = "Duration";

    // Event Recurrence fields used by SetEventRecurrenceField command
    public static final String EVENT_REC_FIELD_FREQUENCY      = "Frequency";
    public static final String EVENT_REC_FIELD_INTERVAL       = "Interval";
    public static final String EVENT_REC_FIELD_END_DATE       = "EndDate";
    public static final String EVENT_REC_FIELD_START_DATE     = "StartDate";
    public static final String EVENT_REC_FIELD_DAY_OF_WEEK    = "DayOfWeek";
    public static final String EVENT_REC_FIELD_DAY_OF_MONTH   = "DayOfMonth";
    public static final String EVENT_REC_FIELD_MONTH_OF_YEAR  = "MonthOfYear";
    public static final String EVENT_REC_FIELD_OCCURRENCES    = "Occurrences";
    public static final String EVENT_REC_FIELD_INSTANCE       = "Instance";
    public static final String EVENT_REC_FIELD_EXCEPTIONS     = "Exceptions";
    public static final String EVENT_REC_FIELD_EXCEPTIONS_ADD = "ExceptionsAdd";

    // Recurrence frequency types used by SetEventRecurrenceField(Frequency, ?)
    public static final String FREQUENCY_TYPE_NONE        = "None";
    public static final String FREQUENCY_TYPE_DAILY       = "Daily";
    public static final String FREQUENCY_TYPE_WEEKLY      = "Weekly";
    public static final String FREQUENCY_TYPE_MONTHLY     = "Monthly";
    public static final String FREQUENCY_TYPE_MONTHLY_NTH = "MonthlyNth";
    public static final String FREQUENCY_TYPE_YEARLY      = "Yearly";
    public static final String FREQUENCY_TYPE_YEARLY_NTH  = "YearlyNth";

    // Days of week used by SetEventRecurrenceField(DayOfWeek, ?)
    public static final String DAY_OF_WEEK_SUNDAY    = "SU";
    public static final String DAY_OF_WEEK_MONDAY    = "MO";
    public static final String DAY_OF_WEEK_TUESDAY   = "TU";
    public static final String DAY_OF_WEEK_WEDNESDAY = "WE";
    public static final String DAY_OF_WEEK_THURSDAY  = "TH";
    public static final String DAY_OF_WEEK_FRIDAY    = "FR";
    public static final String DAY_OF_WEEK_SATURDAY  = "SA";

    
    public CalendarCommandRunner(CalendarRobot robot) {
        super(robot);
    }

    public boolean runCommand(String command, String pars) throws Throwable {

        if (CREATE_EMPTY_EVENT_COMMAND.equals(command)) {
            createEmptyEvent(command, pars);
        } else if (LOAD_EVENT_COMMAND.equals(command)) {
            loadEvent(command, pars);
        } else if (SET_EVENT_FIELD_COMMAND.equals(command)) {
            setEventField(command, pars);
        } else if (SET_EVENT_RECURRENCE_FIELD_COMMAND.equals(command)){
            setEventRecurrenceField(command, pars);
        } else if (EMPTY_EVENT_FIELD_COMMAND.equals(command)) {
            emptyEventField(command, pars);
        } else if (SAVE_EVENT_COMMAND.equals(command)) {
            saveEvent(command, pars);
        } else if (DELETE_EVENT_COMMAND.equals(command)) {
            deleteEvent(command, pars);
        } else if (DELETE_ALL_EVENTS_COMMAND.equals(command)) {
            deleteAllEvents(command, pars);
        } else if (CHECK_NEW_EVENT_COMMAND.equals(command)) {
            checkNewEvent(command, pars);
        } else if (CHECK_UPDATED_EVENT_COMMAND.equals(command)) {
            checkUpdatedEvent(command, pars);
        } else if (CHECK_DELETED_EVENT_COMMAND.equals(command)) {
            checkDeletedEvent(command, pars);
        } else if (CHECK_NEW_EVENT_ON_SERVER_COMMAND.equals(command)) {
            checkNewEventOnServer(command, pars);
        } else if (CHECK_UPDATED_EVENT_ON_SERVER_COMMAND.equals(command)) {
            checkUpdatedEventOnServer(command, pars);
        } else if (CHECK_DELETED_EVENT_ON_SERVER_COMMAND.equals(command)) {
            checkDeletedEventOnServer(command, pars);
        } else if (CREATE_EMPTY_EVENT_ON_SERVER_COMMAND.equals(command)) {
            createEmptyEventOnServer(command, pars);
        } else if (LOAD_EVENT_ON_SERVER_COMMAND.equals(command)) {
            loadEventOnServer(command, pars);
        } else if (SAVE_EVENT_ON_SERVER_COMMAND.equals(command)) {
            saveEventOnServer(command, pars);
        } else if (DELETE_EVENT_ON_SERVER_COMMAND.equals(command)) {
            deleteEventOnServer(command, pars);
        } else if (DELETE_ALL_EVENTS_ON_SERVER_COMMAND.equals(command)) {
            deleteAllEventsOnServer(command, pars);
        } else if (SET_EVENT_AS_VCAL_COMMAND.equals(command)){
            setEventAsVCal(command, pars);
        } else if (CHECK_EVENT_RECURRENCE_COMMAND.equals(command)){
            checkEventRecRule(command, pars);
        } else if (CHECK_EVENT_EXCEPTION_COMMAND.equals(command)){
            checkEventExceptions(command, pars);
        } else if (IMPORT_EVENT_ON_SERVER_COMMAND.equals(command)){
            importEventOnServer(command, pars);
        } else if (CHECK_EVENT_EXCEPTION_COMMAND.equals(command)) {
            checkEventExceptions(command, pars);
        } else if (RESET_EVENTS_COMMAND.equals(command)) {
            resetEvents(command, pars);
        } else if (CHECK_EVENT_VCAL_COMMAND.equals(command)) {
            checkEventAsVCal(command, pars);
        } else if (CHECK_EVENT_VCAL_ON_SERVER_COMMAND.equals(command)) {
            checkEventAsVCalOnServer(command, pars);
        } else if (SET_EVENT_FROM_SERVER.equals(command)) {
            setEventFromServer(command, pars);
        } else if (CREATE_EMPTY_RAW_EVENT.equals(command)) {
            createEmptyRawEvent(command, pars);
        } else if (SET_RAW_EVENT_FIELD.equals(command)) {
            setRawEventField(command, pars);
        } else if (SAVE_RAW_EVENT.equals(command)) {
            saveRawEvent(command, pars);
        } else if (CHECK_RAW_EVENT_AS_VCAL.equals(command)) {
            checkRawEventAsVCal(command, pars);
        } else if (SET_RAW_REMINDER_FIELD.equals(command)) {
            setRawReminderField(command, pars);
        } else if (CHECK_RAW_EVENT_FIELD.equals(command)) {
            checkRawEventField(command, pars);
        } else if (CHECK_RAW_REMINDER_FIELD.equals(command)) {
            checkRawReminderField(command, pars);
        } else {
            return false;
        }
        return true;
    }

    private CalendarRobot getCalendarRobot() {
        return (CalendarRobot)robot;
    }

    private void createEmptyEvent(String command, String args) throws Throwable {
        getCalendarRobot().createEmptyEvent();
    }

    private void importEventOnServer(String command, String args) throws Throwable {
        getCalendarRobot().importEventOnServer(getParameter(args, 0));
    }

    private void createEmptyEventOnServer(String command, String args) throws Throwable {
        getCalendarRobot().createEmptyEvent();
    }

    private void setEventField(String command, String args) throws Throwable {

        String field = getParameter(args, 0);
        String value = getParameter(args, 1);

        checkArgument(field, "Missing field name in " + command);
        checkArgument(value, "Missing value in " + command);

        getCalendarRobot().setEventField(field, value);
    }
    private void setEventRecurrenceField(String command, String args) throws Throwable {

        String field = getParameter(args, 0);
        String value = getParameter(args, 1);

        checkArgument(field, "Missing recurrence field name in " + command);
        checkArgument(value, "Missing recurrence value in " + command);

        getCalendarRobot().setEventRecurrenceField(field, value);
    }

    private void emptyEventField(String command, String args) throws Throwable {

        String field = getParameter(args, 0);

        checkArgument(field, "Missing field in " + command);

        getCalendarRobot().setEventField(field, "");
    }

    private void loadEvent(String command, String args) throws Throwable {

        String summary = getParameter(args, 0);

        checkArgument(summary, "Missing summary in " + command);

        getCalendarRobot().loadEvent(summary);
    }

    private void saveEvent(String command, String args) throws Throwable {

        String useNativeApp = getParameter(args, 0);

        if("true".equals(useNativeApp)) {
            getCalendarRobot().saveEvent(true);
        } else {
            getCalendarRobot().saveEvent();
        }
    }

    private void deleteEvent(String command, String args) throws Throwable {

        String summary = getParameter(args, 0);
        checkArgument(summary, "Missing summary in " + command);

        getCalendarRobot().deleteEvent(summary);
    }

    private void deleteAllEvents(String command, String args) throws Throwable {
        getCalendarRobot().deleteAllEvents();
    }


    private void checkNewEvent(String command, String args) throws Throwable {

        String summary = getParameter(args, 0);
        String checkContent  = getParameter(args, 1);

        checkArgument(summary, "Missing summary in " + command);
        checkArgument(checkContent, "Missing checkContent in " + command);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getCalendarRobot().checkNewEvent(summary, checkSyncClient,
                parseBoolean(checkContent));
    }

    private void checkUpdatedEvent(String command, String args) throws Throwable {

        String summary = getParameter(args, 0);
        String checkContent  = getParameter(args, 1);

        checkArgument(summary, "Missing summary in " + command);
        checkArgument(checkContent, "Missing checkContent in " + command);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getCalendarRobot().checkUpdatedEvent(summary, checkSyncClient,
                parseBoolean(checkContent));
    }

    private void checkDeletedEvent(String command, String args) throws Throwable {

        String summary = getParameter(args, 0);

        checkArgument(summary, "Missing summary in " + command);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getCalendarRobot().checkDeletedEvent(summary, checkSyncClient);
    }


    private void checkNewEventOnServer(String command, String args) throws Throwable {

        String summary      = getParameter(args, 0);
        String checkContent  = getParameter(args, 1);

        checkArgument(summary, "Missing summary in " + command);
        checkArgument(checkContent, "Missing checkContent in " + command);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getCalendarRobot().checkNewEventOnServer(summary, checkSyncClient,
                parseBoolean(checkContent));
    }

    private void checkUpdatedEventOnServer(String command, String args) throws Throwable {

        String summary      = getParameter(args, 0);
        String checkContent = getParameter(args, 1);

        checkArgument(summary, "Missing summary in " + command);
        checkArgument(checkContent, "Missing checkContent in " + command);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getCalendarRobot().checkUpdatedEventOnServer(summary, checkSyncClient,
                parseBoolean(checkContent));
    }

    private void checkDeletedEventOnServer(String command, String args) throws Throwable {

        String summary = getParameter(args, 0);

        checkArgument(summary, "Missing summary in " + command);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getCalendarRobot().checkDeletedEventOnServer(summary, checkSyncClient);
    }

    public void loadEventOnServer(String command, String args) throws Throwable {

        String summary = getParameter(args, 0);

        checkArgument(summary, "Missing summary in " + command);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getCalendarRobot().loadEventOnServer(summary, checkSyncClient);
    }

    public void saveEventOnServer(String command, String args) throws Throwable {

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getCalendarRobot().saveEventOnServer(checkSyncClient);
    }

    public void deleteEventOnServer(String command, String args) throws Throwable {

        String summary = getParameter(args, 0);

        checkArgument(summary, "Missing summary in " + command);

        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getCalendarRobot().deleteEventOnServer(summary, checkSyncClient);
    }

    public void deleteAllEventsOnServer(String command, String args) throws Throwable {
        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);

        getCalendarRobot().deleteAllEventsOnServer(checkSyncClient);
    }

    public void resetEvents(String command, String args) throws Throwable {
        checkObject(checkSyncClient, "Run StartMainApp before command: " + command);
        getCalendarRobot().resetEvents(checkSyncClient);
    }

    private void setEventAsVCal(String command, String args) throws Throwable {

        String VCal = getParameter(args, 0);

        checkArgument(VCal, "Missing field name in " + command);

        getCalendarRobot().setEventAsVCal(VCal);
    }

    private void checkEventRecRule(String command, String args) throws Throwable {

        String summary = getParameter(args, 0);
        String rrule = getParameter(args,1);

        checkArgument(summary, "Missing field name in " + command);
        checkArgument(rrule, "Missing rrule in " + command);

        getCalendarRobot().checkEventRecRule(summary,rrule);
    }

    private void checkEventAsVCal(String command, String args) throws Throwable {

        String summary = getParameter(args, 0);
        String vcal    = getParameter(args,1);

        checkArgument(summary, "Missing field name in " + command);
        checkArgument(vcal, "Missing vcal in " + command);

        getCalendarRobot().checkEventAsVCal(summary,vcal);
    }

    private void checkEventAsVCalOnServer(String command, String args) throws Throwable {

        String summary = getParameter(args, 0);
        String vcal    = getParameter(args,1);

        throw new ClientTestException("Not implemented");
    }

    private void checkEventExceptions(String command, String args) throws Throwable {
        String summary = getParameter(args, 0);
        String exceptions = getParameter(args,1);

        checkArgument(summary, "Missing field name in " + command);
        checkArgument(exceptions, "Missing exceptions in " + command);

        getCalendarRobot().checkEventExceptions(summary,exceptions);
    }

    private void setEventFromServer(String command, String args) throws Throwable {

        String vcal = getParameter(args, 0);
        checkArgument(vcal, "Missing vcal in " + command);
        getCalendarRobot().setEventFromServer(vcal);
    }

    private void createEmptyRawEvent(String command, String args) throws Throwable {
        getCalendarRobot().createEmptyRawEvent();
    }

    private void setRawEventField(String command, String args) throws Throwable {
        String fieldName  = getParameter(args, 0);
        String fieldValue = getParameter(args, 1);
        checkArgument(fieldName,  "Missing field name in " + command);
        checkArgument(fieldValue, "Missing field value in " + command);
        getCalendarRobot().setRawEventField(fieldName, fieldValue);
    }

    private void setRawReminderField(String command, String args) throws Throwable {
        String fieldName  = getParameter(args, 0);
        String fieldValue = getParameter(args, 1);
        checkArgument(fieldName,  "Missing field name in " + command);
        checkArgument(fieldValue, "Missing field value in " + command);
        getCalendarRobot().setRawReminderField(fieldName, fieldValue);
    }

    private void saveRawEvent(String command, String args) throws Throwable {
        getCalendarRobot().saveRawEvent();
    }

    private void checkRawEventAsVCal(String command, String args) throws Throwable {
        String vcal = getParameter(args, 0);
        checkArgument(vcal, "Missing vcal in " + command);
        getCalendarRobot().checkRawEventAsVCal(vcal);
    }

    private void checkRawReminderField(String command, String args) throws Throwable {
        String fieldName  = getParameter(args, 0);
        String fieldValue = getParameter(args, 1);
        checkArgument(fieldName,  "Missing field name in " + command);
        checkArgument(fieldValue, "Missing field value in " + command);
        getCalendarRobot().checkRawReminderField(fieldName, fieldValue);
    }

    private void checkRawEventField(String command, String args) throws Throwable {
        String fieldName  = getParameter(args, 0);
        String fieldValue = getParameter(args, 1);
        checkArgument(fieldName,  "Missing field name in " + command);
        checkArgument(fieldValue, "Missing field value in " + command);
        getCalendarRobot().checkRawEventField(fieldName, fieldValue);
    }


}
 
