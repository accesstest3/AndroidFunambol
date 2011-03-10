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

package com.funambol.common.pim.xvcalendar;

import com.funambol.common.pim.vcalendar.CalendarUtils;
import com.funambol.common.pim.icalendar.VAlarm;
import com.funambol.common.pim.vcalendar.BasicVCalendarParserListener;
import com.funambol.common.pim.ParserProperty;
import com.funambol.util.Log;
import com.funambol.util.StringUtil;

import javax.microedition.pim.PIMItem;
import javax.microedition.pim.Event;
import javax.microedition.pim.ToDo;
import javax.microedition.pim.PIMException;

import java.util.Vector;

/**
 * This class implements the <code>XVCalendarSyntaxParserListener</code> 
 * interface in order to listen all the events during the vCalendar parsing
 * process.
 * Depending on the item type (event or task) it will be filled a JSR75 
 * <code>PIMItem</code> (<code>Event</code> or <code>ToDo</code>), provided
 * through the constructor.
 *
 * <code>XVCalendarParserListener</code> inherits from the
 * <code>BasicVCalendarParserListener</code> which handles the vCalendar 
 * (1.0 and 2.0) common properties.
 *
 * The following methods can be overriden in order to handle properties not
 * supported by the JSR75 <code>PIMItem</code> objects:
 * <li>setTZ: set the timezone offset (e.g. +02:00)</li>
 * <li>setDaylight: set the daylight saving properties vector
 * (e.g. TRUE;+02;20090329T020000;20091025T030000;;)</li>
 * <li>setTaskAlarm: set the task alarm property.</li>
 * <li>setAllDay: set the all day property for this item.</li>
 *
 * Note: only the audio alarm (AALARM propery) type is supported, since only one
 * alarm can be set for each PIMItem object.
 */
public class XVCalendarParserListener extends BasicVCalendarParserListener
        implements XVCalendarSyntaxParserListener {

    private final String LOG_TAG = "XVCalendarParserListener";

    /**
     * Specifies the tz property (e.g. +02:00)
     */
    private String tz = null;

    /**
     * Specifies the daylight saving properties vector
     * (e.g. TRUE;+02;20090329T020000;20091025T030000;;)
     */
    private Vector daylights = new Vector();

    /** 
     * Specifies if an alarm is already set for the current item. Note that a
     * JSR75 PIMItem can have only one alarm setted.
     */
    private boolean alarmSet = false;

    /**
     * Specifies tha start time of this item (used while setting allday property)
     */
    private long itemStartTime = CalendarUtils.UNDEFINED_TIME;
    
    /**
     * The construtor accepts a PIMItem object that will be populated of all
     * the vCalendar properties
     * @param pimItem the PIMItem object
     */
    public XVCalendarParserListener(PIMItem pimItem) {
        super(pimItem);
    }

    /**
     * Methods which should be implemented by a subclass in order to store
     * additional data, not supported by JSR75.
     */
    protected void setTZ(String value) { }
    protected void setDaylight(Vector daylights) { }
    protected void setTaskAlarm(String alarm) { }
    protected void setAllDay(boolean allday) {
        // In the  standard JSR75 implementation, allday events must have
        // the same start/end datetimes
        if(allday && (pimItem instanceof Event) && pimItem.countValues(Event.END) > 0) {
            pimItem.setDate(Event.END, 0, Event.ATTR_NONE, itemStartTime);
        }
    }
    
    public void addProperty(ParserProperty property) throws ParseException {
        Log.trace(LOG_TAG, "addEventProperty: " + property.getName());

        String name  = property.getName();
        String value = getClearValue(property);

        if (StringUtil.equalsIgnoreCase(XVCalendar.TZ, name)) {
            tz = value;
        } else if (StringUtil.equalsIgnoreCase(XVCalendar.DAYLIGHT, name)) {
            daylights.addElement(value);
        }
    }
    
    public void addEventProperty(ParserProperty property) throws ParseException { 
        Log.trace(LOG_TAG, "addEventProperty: " + property.getName());

        String name  = property.getName();
        String value = getClearValue(property);

        if(pimItem instanceof Event) {
            if (StringUtil.equalsIgnoreCase(XVCalendar.DTSTART, name)) {
                if (pimList.isSupportedField(Event.START)) {
                    setStart(value);
                }
            } else if (StringUtil.equalsIgnoreCase(XVCalendar.DTEND, name)) {
                if (pimList.isSupportedField(Event.END)) {
                    setEnd(value);
                }
            } else if (StringUtil.equalsIgnoreCase(XVCalendar.CATEGORIES, name)) {
                setCategories(value);
            } else if (StringUtil.equalsIgnoreCase(XVCalendar.AALARM, name)) {
                if (pimList.isSupportedField(Event.ALARM)) {
                    setAlarm(value);
                }
            } else if (StringUtil.equalsIgnoreCase(XVCalendar.RRULE, name)) {
                setRRULE(value);
            } else if (StringUtil.equalsIgnoreCase(XVCalendar.X_FUNAMBOL_ALLDAY, name)) {
                if("1".equals(value)) {
                    allDay = true;
                }
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
            if (StringUtil.equalsIgnoreCase(XVCalendar.DUE, name)) {
                if (pimList.isSupportedField(ToDo.DUE)) {
                    setDue(value);
                }
            } else if (StringUtil.equalsIgnoreCase(XVCalendar.RRULE, name)) {
                setRRULE(value);
            } else if (StringUtil.equalsIgnoreCase(XVCalendar.X_FUNAMBOL_ALLDAY, name)) {
                if("1".equals(value)) {
                    allDay = true;
                }
            } else if (StringUtil.equalsIgnoreCase(XVCalendar.AALARM, name)) {
                setTaskAlarm(value);
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

    public void start() {
        alarmSet = false;
    }
    
    public void end() {
        setTZ(tz);
        setDaylight(daylights);
        setAllDay(allDay);
    }

    public void endEvent() throws ParseException {
        try {
            super.setEventAlarm(alarm);
        } catch(Exception ex) {
            throw new ParseException(ex.getMessage());
        }
    }
    
    public void startEvent() throws ParseException { }
    public void startToDo() throws ParseException { }
    public void endToDo() throws ParseException { }

    /************************ End listener methods ****************************/

    protected void setStart(String value) throws ParseException {
        Log.trace(LOG_TAG, "setStart");
        long startTime = CalendarUtils.parseDateTime(value, tz, daylights).getTime().getTime();
        alarmStartRelatedTime = startTime;
        itemStartTime = startTime;
        pimItem.addDate(Event.START, Event.ATTR_NONE, startTime);
    }
    protected void setEnd(String value) throws ParseException {
        Log.trace(LOG_TAG, "setEnd");
        long endTime = CalendarUtils.parseDateTime(value, tz, daylights).getTime().getTime();
        alarmEndRelatedTime = endTime;
        pimItem.addDate(Event.END, Event.ATTR_NONE, endTime);
    }
    protected void setDue(String value) throws ParseException {
        Log.trace(LOG_TAG, "setDue");
        long dueTime = CalendarUtils.parseDateTime(value, tz, daylights).getTime().getTime();
        alarmStartRelatedTime = dueTime;
        itemStartTime = dueTime;
        pimItem.addDate(ToDo.DUE, ToDo.ATTR_NONE, dueTime);
    }
    protected void setCategories(String value) throws ParseException {
        Log.trace(LOG_TAG, "setCategories");

        String[] categories = StringUtil.split(value, ";");
        int maxCategories = pimList.maxCategories();
        
        for(int i=0, count=0; i<categories.length &&
                (maxCategories == -1 || count<maxCategories); i++) {
            try {
                if(!pimList.isCategory(categories[i])) {
                    pimList.addCategory(categories[i]);
                }
                pimItem.addToCategory(categories[i]);
                count++;
            } catch(PIMException ex) {
                Log.error(LOG_TAG, "Exception while setting item categories: " + ex);
            }
        }
    }
    protected void setAlarm(String value) throws ParseException {
        Log.trace(LOG_TAG, "setAlarm");
        if(!alarmSet) {
            int dateIndex = value.indexOf(";");
            if(dateIndex != -1) {
                value = value.substring(0, dateIndex);
            }
            long alarmTime = CalendarUtils.parseDateTime(value, tz, daylights).getTime().getTime();
            alarm = new VAlarm();
            alarm.setTriggerAbsoluteTime(alarmTime);
            alarmSet = true;
        } else {
            Log.error(LOG_TAG, "The item alarm has been already set");
        }
        
    }
    protected void setRRULE(String value) throws ParseException {
        // TBD
        Log.error(LOG_TAG, "Not supported yet");
    }
}
