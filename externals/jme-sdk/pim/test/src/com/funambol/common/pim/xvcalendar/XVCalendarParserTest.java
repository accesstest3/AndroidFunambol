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

package com.funambol.common.pim.xvcalendar;

import java.io.ByteArrayInputStream;
import javax.microedition.pim.PIM;

import javax.microedition.pim.ToDoList;
import javax.microedition.pim.EventList;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.ToDo;
import javax.microedition.pim.Event;

import com.funambol.util.Log;
import com.funambol.util.ConsoleAppender;

import java.util.*;

import junit.framework.*; 
import com.funambol.common.pim.vcalendar.CalendarUtils;

/**
 * This is a specific test for the vCalendar parser. 
 */
public class XVCalendarParserTest extends TestCase {

    private final String TEST_TODO =  "BEGIN:VCALENDAR\r\n" +
                                      "VERSION:1.0\r\n" +
                                      "TZ:+01\r\n" +
                                      "DAYLIGHT:TRUE;+02;20090329T020000;20091025T030000;;\r\n" +
                                      "DAYLIGHT:TRUE;+02;20100328T020000;20101031T030000;;\r\n" +
                                      "DAYLIGHT:TRUE;+02;20110327T020000;20111030T030000;;\r\n" +
                                      "DAYLIGHT:TRUE;+02;20120325T020000;20121028T030000;;\r\n" +
                                      "DAYLIGHT:TRUE;+02;20130331T020000;20131027T030000;;\r\n" +
                                      "BEGIN:VTODO\r\n" +
                                      "UID:02072009130421122125-0\r\n" +
                                      "SUMMARY:summary\r\n" +
                                      "DUE:20090113T140000Z\r\n" +
                                      "AALARM:20090113T130000Z\r\n" +
                                      "CLASS:PUBLIC\r\n" +
                                      "STATUS:COMPLETED\r\n" +
                                      "LAST-MODIFIED:20090702T131257Z\r\n" +
                                      "ATTENDEE;ROLE=ATTENDEE:John <email@john>\r\n" +
                                      "ATTENDEE;ROLE=ATTENDEE:Jack <email@jack>\r\n" +
                                      "PRIORITY:2\r\n" +
                                      "END:VTODO\r\n" +
                                      "END:VCALENDAR";

    private final String TEST_EVENT = "BEGIN:VCALENDAR\r\n" +
                                      "VERSION:1.0\r\n" +
                                      "BEGIN:VEVENT\r\n" +
                                      "UID:02072009130421122125-0\r\n" +
                                      "SUMMARY:summary\r\n" +
                                      "LOCATION:location\r\n" +
                                      "DTSTART:20090113T140000Z\r\n" +
                                      "DTEND:20090113T160000Z\r\n" +
                                      "AALARM:20090113T130000Z\r\n" +
                                      "CATEGORIES:CAT1;CAT2;CAT3\r\n" +
                                      "ATTENDEE;ROLE=ATTENDEE:John <email@john>\r\n" +
                                      "ATTENDEE;ROLE=ATTENDEE:Jack <email@jack>\r\n" +
                                      "CLASS:PUBLIC\r\n" +
                                      "LAST-MODIFIED:20090702T131257Z\r\n" +
                                      "END:VEVENT\r\n" +
                                      "END:VCALENDAR";

    private final String TEST_EVENT_TZ_1 = "BEGIN:VCALENDAR\r\n" +
                                           "VERSION:1.0\r\n" +
                                           "TZ:+01\r\n" +
                                           "DAYLIGHT:TRUE;+02;20090329T020000;20091025T030000;;\r\n" +
                                           "DAYLIGHT:TRUE;+02;20100328T020000;20101031T030000;;\r\n" +
                                           "DAYLIGHT:TRUE;+02;20110327T020000;20111030T030000;;\r\n" +
                                           "DAYLIGHT:TRUE;+02;20120325T020000;20121028T030000;;\r\n" +
                                           "DAYLIGHT:TRUE;+02;20130331T020000;20131027T030000;;\r\n" +
                                           "BEGIN:VEVENT\r\n" +
                                           "SUMMARY:summary\r\n" +
                                           "DTSTART:20090113T140000\r\n" +
                                           "DTEND:20090113T160000\r\n" +
                                           "AALARM:20090113T130000\r\n" +
                                           "END:VEVENT\r\n" +
                                           "END:VCALENDAR";

    private final String TEST_EVENT_TZ_2 = "BEGIN:VCALENDAR\r\n" +
                                           "VERSION:1.0\r\n" +
                                           "TZ:+01\r\n" +
                                           "DAYLIGHT:TRUE;+02;20090329T020000;20091025T030000;;\r\n" +
                                           "DAYLIGHT:TRUE;+02;20100328T020000;20101031T030000;;\r\n" +
                                           "DAYLIGHT:TRUE;+02;20110327T020000;20111030T030000;;\r\n" +
                                           "DAYLIGHT:TRUE;+02;20120325T020000;20121028T030000;;\r\n" +
                                           "DAYLIGHT:TRUE;+02;20130331T020000;20131027T030000;;\r\n" +
                                           "BEGIN:VEVENT\r\n" +
                                           "SUMMARY:summary\r\n" +
                                           "DTSTART:20090813T140000\r\n" +
                                           "DTEND:20090813T160000\r\n" +
                                           "AALARM:20090813T100000Z\r\n" +
                                           "END:VEVENT\r\n" +
                                           "END:VCALENDAR";

    private final String TEST_ALL_DAY_EVENT = "BEGIN:VCALENDAR\r\n" +
                                              "VERSION:1.0\r\n" +
                                              "BEGIN:VEVENT\r\n" +
                                              "SUMMARY:summary\r\n" +
                                              "DTSTART:20090113T000000Z\r\n" +
                                              "DTEND:20090113T235959Z\r\n" +
                                              "X-FUNAMBOL-ALLDAY:1\r\n" +
                                              "END:VEVENT\r\n" +
                                              "END:VCALENDAR";
            
    public XVCalendarParserTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.TRACE);
    }
    
    public void setUp() { Log.setLogLevel(Log.TRACE); }
    
    public void tearDown() { }

    public void testParserListener_toDo() throws Throwable {

        ToDoList todolist = (ToDoList) PIM.getInstance().openPIMList(PIM.TODO_LIST, PIM.READ_WRITE);
        ToDo todo  = todolist.createToDo();
        
        ByteArrayInputStream is = new ByteArrayInputStream(TEST_TODO.getBytes());
        TestXVCalendarParserListener lis = new TestXVCalendarParserListener(todo);
        XVCalendarSyntaxParser parser = new XVCalendarSyntaxParser(is);
        parser.setListener(lis);
        parser.parse();
        lis.checkParser();

        // Check DUE
        assertEquals(todo.getDate(ToDo.DUE, 0),
                CalendarUtils.parseDateTime("20090113T140000Z",
                TimeZone.getTimeZone("UTC")).getTime().getTime());

        // Check STATUS
        assertTrue(todo.getBoolean(ToDo.COMPLETED, 0) == true);

        // Check PRIORITY
        assertEquals(todo.getInt(ToDo.PRIORITY, 0), 2);
    }

    public void testParserListener_event() throws Throwable {

        EventList eventlist = (EventList) PIM.getInstance().openPIMList(PIM.EVENT_LIST, PIM.READ_WRITE);
        Event event = eventlist.createEvent();

        ByteArrayInputStream is = new ByteArrayInputStream(TEST_EVENT.getBytes());
        TestXVCalendarParserListener lis = new TestXVCalendarParserListener(event);
        XVCalendarSyntaxParser parser = new XVCalendarSyntaxParser(is);
        parser.setListener(lis);
        parser.parse();
        event.commit();

        // Check SUMMARY
        assertEquals(event.getString(Event.SUMMARY, 0), "summary");

        // Check LOCATION
        assertEquals(event.getString(Event.LOCATION, 0), "location");

        // Check DTSTART
        assertEquals(event.getDate(Event.START, 0),
                CalendarUtils.parseDateTime("20090113T140000Z",
                TimeZone.getTimeZone("UTC")).getTime().getTime());

        // Check DTEND
        assertEquals(event.getDate(Event.END, 0),
                CalendarUtils.parseDateTime("20090113T160000Z",
                TimeZone.getTimeZone("UTC")).getTime().getTime());

        // Check CLASS
        assertEquals(event.getInt(Event.CLASS, 0), Event.CLASS_PUBLIC);

        // Check ALARM (1 hour)
        assertEquals(event.getInt(Event.ALARM, 0), 3600);

        // Check categories
        String[] categories = event.getCategories();
        assertEquals(categories[0], "CAT1");
        assertEquals(categories[1], "CAT2");
        assertEquals(categories[2], "CAT3");
    }

    public void testParserListener_event_tz1() throws Throwable {

        EventList eventlist = (EventList) PIM.getInstance().openPIMList(PIM.EVENT_LIST, PIM.READ_WRITE);
        Event event = eventlist.createEvent();

        ByteArrayInputStream is = new ByteArrayInputStream(TEST_EVENT_TZ_1.getBytes());
        TestXVCalendarParserListener lis = new TestXVCalendarParserListener(event);
        XVCalendarSyntaxParser parser = new XVCalendarSyntaxParser(is);
        parser.setListener(lis);
        parser.parse();
        event.commit();

        // Check DTSTART
        assertEquals(event.getDate(Event.START, 0),
                CalendarUtils.parseDateTime("20090113T130000Z",
                TimeZone.getTimeZone("UTC")).getTime().getTime());

        // Check DTEND
        assertEquals(event.getDate(Event.END, 0),
                CalendarUtils.parseDateTime("20090113T150000Z",
                TimeZone.getTimeZone("UTC")).getTime().getTime());

        // Check ALARM (1 hour)
        assertEquals(event.getInt(Event.ALARM, 0), 3600);
    }

    public void testParserListener_event_tz2() throws Throwable {

        EventList eventlist = (EventList) PIM.getInstance().openPIMList(PIM.EVENT_LIST, PIM.READ_WRITE);
        Event event = eventlist.createEvent();

        ByteArrayInputStream is = new ByteArrayInputStream(TEST_EVENT_TZ_2.getBytes());
        TestXVCalendarParserListener lis = new TestXVCalendarParserListener(event);
        XVCalendarSyntaxParser parser = new XVCalendarSyntaxParser(is);
        parser.setListener(lis);
        parser.parse();
        event.commit();

        // Check DTSTART
        assertEquals(event.getDate(Event.START, 0),
                CalendarUtils.parseDateTime("20090813T120000Z",
                TimeZone.getTimeZone("UTC")).getTime().getTime());

        // Check DTEND
        assertEquals(event.getDate(Event.END, 0),
                CalendarUtils.parseDateTime("20090813T140000Z",
                TimeZone.getTimeZone("UTC")).getTime().getTime());

        // Check ALARM (2 hour)
        assertEquals(event.getInt(Event.ALARM, 0), 7200);
    }

    public void testParserListener_event_allday() throws Throwable {

        EventList eventlist = (EventList) PIM.getInstance().openPIMList(PIM.EVENT_LIST, PIM.READ_WRITE);
        Event event = eventlist.createEvent();

        ByteArrayInputStream is = new ByteArrayInputStream(TEST_ALL_DAY_EVENT.getBytes());
        TestXVCalendarParserListener lis = new TestXVCalendarParserListener(event);
        XVCalendarSyntaxParser parser = new XVCalendarSyntaxParser(is);
        parser.setListener(lis);
        parser.parse();
        event.commit();

        // Check DTSTART
        assertEquals(event.getDate(Event.START, 0),
                CalendarUtils.parseDateTime("20090113T000000Z",
                TimeZone.getTimeZone("UTC")).getTime().getTime());

        // Check DTEND
        assertEquals(event.getDate(Event.END, 0),
                CalendarUtils.parseDateTime("20090113T000000Z",
                TimeZone.getTimeZone("UTC")).getTime().getTime());
    }

    private class TestXVCalendarParserListener extends XVCalendarParserListener {

        private String tz        = null;
        private String alarm     = null;
        private Vector daylights = null;
        private Vector attendees = new Vector();

        public TestXVCalendarParserListener(PIMItem item) {
            super(item);
        }

        protected void setTZ(String value) {
            Log.debug("tz: " + value);
            this.tz = value;
        }
        protected void setDaylight(Vector daylights) {
            for(int i=0; i<daylights.size(); i++) {
                Log.debug("daylight: " + (String)daylights.elementAt(i));
            }
            this.daylights = daylights;
        }
        protected void setTaskAlarm(String alarm) {
            Log.debug("alarm: " + alarm);
            this.alarm = alarm;
        }

        protected void addAttendee(String value) {
            Log.debug("attendee: " + value);
            attendees.addElement(value);
        }

        public void checkParser() throws Throwable {
            assertTrue(tz != null);
            assertTrue(tz.equals("+01"));
            assertTrue(alarm != null);
            assertTrue(alarm.equals("20090113T130000Z"));
            assertTrue(daylights != null);
            assertTrue(((String)daylights.elementAt(0)).equals("TRUE;+02;20090329T020000;20091025T030000;;"));
            assertTrue(((String)daylights.elementAt(1)).equals("TRUE;+02;20100328T020000;20101031T030000;;"));
            assertTrue(((String)daylights.elementAt(2)).equals("TRUE;+02;20110327T020000;20111030T030000;;"));
            assertTrue(((String)daylights.elementAt(3)).equals("TRUE;+02;20120325T020000;20121028T030000;;"));
            assertTrue(((String)daylights.elementAt(4)).equals("TRUE;+02;20130331T020000;20131027T030000;;"));
            assertTrue(((String)attendees.elementAt(0)).equals("John <email@john>"));
            assertTrue(((String)attendees.elementAt(1)).equals("Jack <email@jack>"));
        }
    }
}
