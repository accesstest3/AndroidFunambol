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

package com.funambol.common.pim.model.icalendar;

import java.util.ArrayList;

import com.funambol.common.pim.model.model.VEvent;
import com.funambol.common.pim.model.model.VTodo;
import com.funambol.common.pim.model.model.VAlarm;
import com.funambol.common.pim.model.model.VTimezone;
import com.funambol.common.pim.model.model.TzStandardComponent;
import com.funambol.common.pim.model.model.TzDaylightComponent;
import com.funambol.common.pim.model.model.Property;
import com.funambol.common.pim.model.model.Parameter;
import com.funambol.common.pim.model.model.VCalendar;
import com.funambol.common.pim.ParserParam;
import com.funambol.common.pim.ParserProperty;
import com.funambol.common.pim.icalendar.ParseException;
import com.funambol.common.pim.model.model.VCalendarContent;
import com.funambol.common.pim.icalendar.ICalendarSyntaxParserListener;

public class ICalendarSyntaxParserListenerImpl implements ICalendarSyntaxParserListener {

    private VCalendar           calendar  = null;
    private VEvent              event     = null;
    private VTodo               todo      = null;
    private VTimezone           timezone  = null;
    private VAlarm              alarm     = null;
    private TzStandardComponent standardc = null;
    private TzDaylightComponent daylightc = null;
    private VCalendarContent    current   = null;

    public ICalendarSyntaxParserListenerImpl(VCalendar calendar) {
        this.calendar = calendar;
    }

    public void start() {
    }

    public void end() {
    }

    public void addProperty(ParserProperty property) throws ParseException {
        calendar.addProperty(buildProperty(property));
    }

    public void startEvent() throws ParseException {
        event   = new VEvent();
        current = event;
    }

    public void endEvent() throws ParseException {
        calendar.addEvent(event);
        current = null;
    }

    public void addEventProperty(ParserProperty property) throws ParseException {
        event.addProperty(buildProperty(property));
    }

    public void startToDo() throws ParseException {
        todo = new VTodo();
        current = todo;
    }

    public void endToDo() throws ParseException {
        calendar.addTodo(todo);
        current = null;
    }

    public void addToDoProperty(ParserProperty property) throws ParseException {
        todo.addProperty(buildProperty(property));
    } 

    public void startAlarm() throws ParseException {
        alarm = new VAlarm();
    }

    public void endAlarm() throws ParseException {
    }

    public void addAlarmProperty(ParserProperty property) throws ParseException {
        alarm.addProperty(buildProperty(property));
    }

    public void addAlarm() throws ParseException {

        if (event != null && current == event) {
            event.addComponent(alarm);
        } else if (todo != null && current == todo) {
            todo.addComponent(alarm);
        } else {
            throw new ParseException("Cannot add alarm to unknwon component");
        }
    }

    public void startTimezone() throws ParseException {
        timezone      = new VTimezone();
    }

    public void endTimezone() throws ParseException {
        calendar.addTimezone(timezone);
    }

    public void addTimezoneProperty(ParserProperty property) throws ParseException {
        timezone.addProperty(buildProperty(property));
    }

    public void addTimezoneStandardC() throws ParseException {
        if (timezone != null && standardc != null) {
            timezone.addComponent(standardc);
        }
    }

    public void addTimezoneDayLightC() throws ParseException {
        if (timezone != null && daylightc != null) {
            timezone.addComponent(daylightc);
        }
    }


    public void startTimezoneStandardC() throws ParseException {
        standardc = new TzStandardComponent();
    }

    public void endTimezoneStandardC() throws ParseException {
    }

    public void addStandardCProperty(ParserProperty property) throws ParseException {
        standardc.addProperty(buildProperty(property));
    }

    public void startTimezoneDayLightC() throws ParseException {
        daylightc   = new TzDaylightComponent ();
    }

    public void endTimezoneDayLightC() throws ParseException {
    }

    public void addDayLightCProperty(ParserProperty property) throws ParseException {
        daylightc.addProperty(buildProperty(property));
    }

    private Property buildProperty(ParserProperty pproperty) {

        ArrayList params = pproperty.getParameters();
        ArrayList newParams = new ArrayList();
        if (params != null) {
            // Each param is a ParserParameter and we need to tranform it into a
            // Parameter
            for(int i=0;i<params.size();++i) {
                ParserParam p = (ParserParam)params.get(i);
                Parameter newP    = new Parameter(p.getName(), p.getValue());
                newParams.add(newP);
            }
        }

        Property p = new Property(pproperty.getName(),
                                  pproperty.getCustom(),
                                  newParams,
                                  pproperty.getValue());
        return p;
    }
}

