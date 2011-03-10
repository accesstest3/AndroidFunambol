/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2009 Funambol, Inc.
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

package com.funambol.common.pim.icalendar;

import javax.microedition.pim.PIMItem;
import javax.microedition.pim.Event;
import javax.microedition.pim.ToDo;

import com.funambol.util.Log;
import com.funambol.util.StringUtil;
import com.funambol.common.pim.ParserProperty;
import com.funambol.common.pim.ArrayList;
import com.funambol.common.pim.vcalendar.CalendarUtils;
import com.funambol.common.pim.vcalendar.BasicVCalendarParserListener;

/**
 * This class implements the ICalendarSyntaxParserListener interface in order
 * to listen all the events which happen during the iCalendar parsing process.
 * Depending on the item type (event or task) it will be filled a JSR75 PIMItem
 * (Event or ToDo), provided through the constructor.
 *
 * It includes some methods which should be implemented by a subclass in order
 * to store extended fields, not directly supported by JSR75:
 * <li>setTZID(String value);</li>
 * <li>setTZOffset(long offset);</li>
 * <li>setAllDay(boolean allday);</li>
 * <li>setTaskAlarm(VAlarm value);</li>
 *
 * Common vCalendar properties are parsed by the BasicVCalendarParserListener.
 */
public class ICalendarParserListener extends BasicVCalendarParserListener
        implements ICalendarSyntaxParserListener {

    private final String LOG_TAG = "ICalendarParserListener";
    
    protected String  tzid = null;
    
    protected long eventStartTime = CalendarUtils.UNDEFINED_TIME;

    /**
     * The construtor accepts a PIMItem object that will be populated of all
     * the iCalendar properties
     * @param pimItem the PIMItem object
     */
    public ICalendarParserListener(PIMItem pimItem) {
        super(pimItem);
    }

    /**
     * Methods which should be implemented by a subclass in order to store
     * additional data, not supported by JSR75.
     */
    protected void setTZID(String value) { }
    protected void setTZOffset(long offset) { }
    protected void setTaskAlarm(VAlarm alarm) { }
    protected void setAllDay(boolean allday) {
        if(allday) {
            // In the  standard JSR75 implementation, allday events must have
            // the same start/end datetimes
            if(pimItem.countValues(Event.END) > 0) {
                pimItem.setDate(Event.END, 0, Event.ATTR_NONE, eventStartTime);
            }
        }
    }

    public void addEventProperty(ParserProperty property) throws ParseException {

        Log.trace(LOG_TAG, "addEventProperty: " + property.getName());

        String name  = property.getName();
        String value = getClearValue(property);

        if(pimItem instanceof Event) {
            if (StringUtil.equalsIgnoreCase(ICalendar.DTSTART, name)) {
                updateAllDay(property.getParameters());
                String tz = getParameter(property.getParameters(), ICalendar.TZID);
                updateTZID(tz);
                if (pimList.isSupportedField(Event.START)) {
                    setStart(value, tz);
                }
            } else if (StringUtil.equalsIgnoreCase(ICalendar.DTEND, name)) {
                updateAllDay(property.getParameters());
                String tz = getParameter(property.getParameters(), ICalendar.TZID);
                updateTZID(tz);
                if (pimList.isSupportedField(Event.END)) {
                    setEnd(value, tzid);
                }
            } else if (StringUtil.equalsIgnoreCase(ICalendar.DURATION, name)) {
                Log.error(LOG_TAG, "Duration property not supported, cannot " +
                        "convert ISO 8601 duration");
                throw new ParseException("Duration property not supported");
            } else if (StringUtil.equalsIgnoreCase(ICalendar.X_FUNAMBOL_TZ_OFFSET, name)) {
                try {
                    setTZOffset(Long.parseLong(value));
                } catch(Exception e) {
                    Log.error(LOG_TAG, "Cannot convert timezone offset: " + value);
                }
            } else if (StringUtil.equalsIgnoreCase(ICalendar.RRULE, name)) {
                setRRULE(value);
            } else {
                try {
                    super.addEventProperty(property);
                } catch(Exception e) {
                    // An error has occured in the superclass
                    throw new ParseException(e.getMessage());
                }
            }
        } else {
            String msg = "Found a VTODO property inside a VEVENT item";
            Log.error(LOG_TAG, msg);
            throw new ParseException(msg);
        }
    }

    public void addToDoProperty(ParserProperty property) throws ParseException {
        Log.trace(LOG_TAG, "addToDoProperty: " + property.getName());

        String name  = property.getName();
        String value = getClearValue(property);

        if(pimItem instanceof ToDo) {
            if (StringUtil.equalsIgnoreCase(ICalendar.COMPLETED, name)) {
                String tz = getParameter(property.getParameters(), ICalendar.TZID);
                updateTZID(tz);
                if (pimList.isSupportedField(ToDo.COMPLETION_DATE)) {
                    setCompleted(value);
                }
            } else if (StringUtil.equalsIgnoreCase(ICalendar.DUE, name)) {
                updateAllDay(property.getParameters());
                String tz = getParameter(property.getParameters(), ICalendar.TZID);
                updateTZID(tz);
                if (pimList.isSupportedField(ToDo.DUE)) {
                    setDue(value, tzid);
                }
            } else if (StringUtil.equalsIgnoreCase(ICalendar.DURATION, name)) {
                Log.error(LOG_TAG, "Duration property not supported, cannot " +
                        "convert ISO 8601 duration");
                throw new ParseException("Duration property not supported");
            } else if (StringUtil.equalsIgnoreCase(ICalendar.X_FUNAMBOL_TZ_OFFSET, name)) {
                try {
                    setTZOffset(Long.parseLong(value));
                } catch(Exception e) {
                    Log.error(LOG_TAG, "Cannot convert timezone offset: " + value);
                }
            } else if (StringUtil.equalsIgnoreCase(ICalendar.RRULE, name)) {
                setRRULE(value);
            } else {
                try {
                    super.addToDoProperty(property);
                } catch(Exception e) {
                    // An error has occured in the superclass
                    throw new ParseException(e.getMessage());
                }
            }
        } else {
            String msg = "Found a VEVENT property inside a VTODO item";
            Log.error(LOG_TAG, msg);
            throw new ParseException(msg);
        }
    }

    public void startAlarm() throws ParseException {
        alarm = new VAlarm();
    }
    public void addAlarmProperty(ParserProperty property) throws ParseException {
        Log.trace(LOG_TAG, "addAlarmProperty: " + property.getName());
        String name   = property.getName();
        String value  = property.getValue();
        if (StringUtil.equalsIgnoreCase(ICalendar.TRIGGER, name)) {
            updateTrigger(property);
        } else if(StringUtil.equalsIgnoreCase(ICalendar.ACTION, name)) {
            if(!StringUtil.equalsIgnoreCase(ICalendar.ACTION_AUDIO, value)) {
                // we support only AUDIO action type (as the funambol server does),
                // in order to format the same action for outgoing items
                Log.error(LOG_TAG, "Unsupported alarm action: " + value);
                throw new ParseException("Unsupported alarm action: " + value);
            }
        }else {
            Log.error(LOG_TAG, "Unsupported alarm property: " + name);
        }
    }

    private void updateTrigger(ParserProperty property) throws ParseException {
        // The trigger type could be DURATION (default) or DATE-TIME
        // JSR75 accepts only DURATION alarms in seconds. 
        String type = getParameter(property.getParameters(), ICalendar.VALUE);
        String related = getParameter(property.getParameters(), ICalendar.RELATED);
        alarm.setTriggerRelated(related);
        if(StringUtil.equalsIgnoreCase(type, ICalendar.DATE_TIME_VALUE)) {
            alarm.setTriggerAbsoluteTime(property.getValue());
        } else {
            alarm.setTriggerRelativeTime(property.getValue());
        }
    }

    public void addTimezoneProperty(ParserProperty property) throws ParseException {
        Log.trace(LOG_TAG, "addTimezoneProperty");
        String name  = property.getName();
        String value = property.getValue();
        if (StringUtil.equalsIgnoreCase(ICalendar.TZID, name)) {
            tzid = value;
        }
    }

    public void endEvent() throws ParseException {
        // Set Event additional data
        try {
            super.setEventAlarm(alarm);
        } catch(Exception ex) {
            throw new ParseException(ex.getMessage());
        }
        setAllDay(allDay);
    }

    public void endToDo() throws ParseException {
        // Set Task additional data
        if(alarm != null) {
            setTaskAlarm(alarm);
        }
    }

    public void end() {
        // Set common additional data
        setTZID(tzid);
    }

    /** Unused methods **/
    public void start() { }
    public void startEvent() throws ParseException { }
    public void startToDo() throws ParseException { }
    public void addAlarm() throws ParseException { }
    public void endAlarm() throws ParseException { }
    public void addProperty(ParserProperty property) throws ParseException { }

    public void startTimezone() throws ParseException { }
    public void endTimezone() throws ParseException { }
    public void addTimezoneStandardC() throws ParseException {}
    public void addTimezoneDayLightC() throws ParseException { }
    public void startTimezoneStandardC() throws ParseException { }
    public void endTimezoneStandardC() throws ParseException { }
    public void addStandardCProperty(ParserProperty property) throws ParseException { }
    public void startTimezoneDayLightC() throws ParseException { }
    public void endTimezoneDayLightC() throws ParseException { }
    public void addDayLightCProperty(ParserProperty property) throws ParseException { }

    /************************ End listener methods ****************************/

    protected void setRRULE(String value) throws ParseException {
        // TBD
        Log.error(LOG_TAG, "Not supported yet");
    }
    
    protected void setStart(String value, String tzid) throws ParseException {
        Log.trace(LOG_TAG, "setStart");
        eventStartTime = CalendarUtils.getLocalDateTime(value, tzid);
        alarmStartRelatedTime = eventStartTime;
        pimItem.addDate(Event.START, Event.ATTR_NONE, eventStartTime);
    }
    protected void setEnd(String value, String tzid) throws ParseException {
        Log.trace(LOG_TAG, "setEnd");
        long eventEndTime = CalendarUtils.getLocalDateTime(value, tzid);
        alarmEndRelatedTime = eventEndTime;
        pimItem.addDate(Event.END, Event.ATTR_NONE, eventEndTime);
    }

    protected void setCompleted(String value) throws ParseException {
        Log.trace(LOG_TAG, "setCompleted");
        pimItem.addDate(ToDo.COMPLETION_DATE, ToDo.ATTR_NONE, CalendarUtils.getLocalDateTime(value, "GMT"));
    }
    protected void setDue(String value, String tzid) throws ParseException {
        Log.trace(LOG_TAG, "setDue");
        alarmStartRelatedTime = eventStartTime;
        pimItem.addDate(ToDo.DUE, ToDo.ATTR_NONE, CalendarUtils.getLocalDateTime(value, tzid));
    }

    /**
     * Update the allday property, depending on the value type
     * @param params
     */
    private void updateAllDay(ArrayList params) {
        String valueType = getParameter(params, ICalendar.VALUE);
        allDay = StringUtil.equalsIgnoreCase(valueType, ICalendar.DATE_VALUE);
    }

    /**
     * Update the current tzid property value
     * @param params
     * @throws com.funambol.common.pim.icalendar.ParseException
     */
    private void updateTZID(String newTZID) throws ParseException  {
        if(tzid != null && newTZID != null && !StringUtil.equalsIgnoreCase(tzid, newTZID)) {
            Log.error(LOG_TAG, "There are different TZID values on the same item");
            throw new ParseException("There are different TZID values on the same item");
        }
        tzid = newTZID;
    }
}
