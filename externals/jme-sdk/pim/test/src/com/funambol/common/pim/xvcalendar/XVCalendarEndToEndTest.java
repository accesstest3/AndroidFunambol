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

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.funambol.common.pim.ArrayList;
import com.funambol.util.StringUtil;

import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMList;
import javax.microedition.pim.EventList;
import javax.microedition.pim.ToDoList;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.Event;
import javax.microedition.pim.ToDo;

import com.funambol.util.Log;
import com.funambol.util.ConsoleAppender;

import com.funambol.common.pim.vcalendar.CalendarTestUtils;
import com.funambol.common.pim.vcalendar.BasicVCalendar;

import junit.framework.*;

/**
 * This is an end-to-end test for the xvCalendar parsing/formatting process.
 * The input test vCalendar test cases must have the following supported
 * fields, in order to compare them with the formatter output:
 * 
 * Events mandatory fields:
 *  - SUMMARY
 *  - LOCATION
 *  - DTSTART       (in UTC)
 *  - DTEND         (in UTC)
 *  - NOTE
 *  - UID
 *  - LAST-MODIFIED (in UTC)
 *  - CLASS
 * Events optional fields:
 *  - RRULE    
 *
 * Tasks mandatory fields:
 *  - SUMMARY
 *  - STATUS
 *  - NOTE
 *  - UID
 *  - LAST-MODIFIED (in UTC)
 *  - CLASS
 *  - PRIORITY
 * Tasks optional fields:
 *  - DUE           (in UTC)
 *  - COMPLETED     (in UTC)
 *  - RRULE
 *
 * Alarm required fields (alarm component is optional):
 *  - TRIGGER
 *  - ACTION (only AUDIO is supported)
 *
 * Encoding note: in all the text fields if encoding is required (e.g. there are
 *                some special chars), there shall be the ENCODING and CHARSET
 *                params as: ";ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8"
 *                Special chars without encoding are not supported.
 */
public class XVCalendarEndToEndTest extends TestCase {

   private  String taskSuppFields[] = { BasicVCalendar.BEGIN_VCALENDAR,
                                        BasicVCalendar.END_VCALENDAR,
                                        BasicVCalendar.BEGIN_VEVENT,
                                        BasicVCalendar.END_VEVENT,
                                        BasicVCalendar.BEGIN_VTODO,
                                        BasicVCalendar.END_VTODO,
                                        BasicVCalendar.SUMMARY,
                                        BasicVCalendar.LOCATION,
                                        BasicVCalendar.DTEND,
                                        BasicVCalendar.DUE,
                                        BasicVCalendar.DURATION,
                                        BasicVCalendar.DESCRIPTION,
                                        BasicVCalendar.CLASS,
                                        BasicVCalendar.LAST_MODIFIED,
                                        BasicVCalendar.UID,
                                        BasicVCalendar.PRIORITY,
                                        BasicVCalendar.STATUS,
                                        BasicVCalendar.ATTENDEE,
                                        BasicVCalendar.VERSION};

   private  String eventSuppFields[] = { BasicVCalendar.BEGIN_VCALENDAR,
                                         BasicVCalendar.END_VCALENDAR,
                                         BasicVCalendar.BEGIN_VEVENT,
                                         BasicVCalendar.END_VEVENT,
                                         BasicVCalendar.BEGIN_VTODO,
                                         BasicVCalendar.END_VTODO,
                                         BasicVCalendar.SUMMARY,
                                         BasicVCalendar.LOCATION,
                                         BasicVCalendar.DTSTART,
                                         BasicVCalendar.DTEND,
                                         BasicVCalendar.DUE,
                                         BasicVCalendar.DURATION,
                                         BasicVCalendar.DESCRIPTION,
                                         BasicVCalendar.CLASS,
                                         BasicVCalendar.LAST_MODIFIED,
                                         BasicVCalendar.UID,
                                         BasicVCalendar.ATTENDEE,
                                         BasicVCalendar.VERSION};


    public XVCalendarEndToEndTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.TRACE);
    }
    
    public void setUp() {
        Log.setLogLevel(Log.TRACE);
    }
    
    public void tearDown() {
    }
    
    /**
     * Iterate all the iCalendar test cases
     */
    public void testEndToEnd() throws Throwable {

        Log.debug("--- endToEndTest ---");
        
        InputStream eventsStream = getClass().getResourceAsStream(
                "/res/xvcalendar/vcalendar-events.vcs");
        InputStream todosStream = getClass().getResourceAsStream(
                "/res/xvcalendar/vcalendar-todos.vcs");
        
        EventList eList = (EventList) PIM.getInstance().openPIMList(PIM.EVENT_LIST,PIM.READ_WRITE);
        ToDoList tList = (ToDoList) PIM.getInstance().openPIMList(PIM.TODO_LIST,PIM.READ_WRITE);

        String vcal;
        do {
            vcal = CalendarTestUtils.getNextCalendarItem(eventsStream);
            vcal = removeUnsupportedFields(vcal, eventSuppFields);
            if (vcal.length() > 0) {
                Event item = eList.createEvent();
                singleVCalTest(vcal, item);
            }
        } while (vcal.length() > 0);

        do {
            vcal = CalendarTestUtils.getNextCalendarItem(todosStream);
            vcal = removeUnsupportedFields(vcal, taskSuppFields);
            if (vcal.length() > 0) {
                ToDo item = tList.createToDo();
                singleVCalTest(vcal, item);
            }
        } while (vcal.length() > 0);
    }

    /**
     * Test flow for each test case:
     * 1 - parse iCalendar item -> PIMItem
     * 2 - format PIMItem -> iCalendar item
     * 3 - order iCalendar items fields of the items coming from points 1 and 3
     * 4 - compare the iCalendar strings
     */
    public void singleVCalTest(String originalVcal, PIMItem pimItem) throws Throwable {

        // parsing vCalendar
        ByteArrayInputStream is = new ByteArrayInputStream(originalVcal.getBytes());
        XVCalendarSyntaxParserListener lis = new XVCalendarParserListener(pimItem);
        XVCalendarSyntaxParser parser = new XVCalendarSyntaxParser(is);
        parser.setListener(lis);
        parser.parse();

        // formatting iCalendar 
        XVCalendarFormatter formatter = new XVCalendarFormatter();
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        formatter.format(pimItem, ostream, false);
        String formattedVcal = ostream.toString();
        formattedVcal = removeAllDayAndEmptyFields(formattedVcal, originalVcal);
        originalVcal  = removeEmptyFields(originalVcal);
        // ordering
        formattedVcal = orderXVCalendar(formattedVcal);
        originalVcal = orderXVCalendar(originalVcal);

        // comparing
        if(!formattedVcal.equals(originalVcal)) {
            Log.trace("[XVCalendarEndToEndTest.singleVCalTest] formattedVcal: " + formattedVcal);
            Log.trace("[XVCalendarEndToEndTest.singleVCalTest] originalVcal: " + originalVcal);
        }
        assertTrue(formattedVcal.equals(originalVcal));
    }

    /**
     * Order the iCalendar item fields alphabetically.
     */
    private String orderXVCalendar(String ical) {

        String sep[] = {"\r\n"};
        String lines[] = StringUtil.split(ical, sep);

        ArrayList fields_al = new ArrayList();
        String field = "";
        for(int i=0;i<lines.length;++i) {
            String line = lines[i];
            if(line.length() > 0 && line.charAt(0) == com.funambol.common.pim.Utils.FOLDING_INDENT_CHAR) {
                // this is a multi line field
                field += line.substring(1); // cut the indent char
            } else {
                if(!field.equals("")) {
                    fields_al.add(field);
                }
                field = line;
            }
        }
        // add the latest field
        fields_al.add(field);

        // order the fields array list
        String result = "";
        String[] fields = StringUtil.getStringArray(fields_al);
        for(int i=0; i<fields.length; i++) {
            for(int j=fields.length-1; j>i; j--) {
                if(fields[j].compareTo(fields[j-1])<0) {
                    String temp = fields[j];
                    fields[j] = fields[j-1];
                    fields[j-1] = temp;
                }
            }
            result += fields[i] + "\r\n";
        }
        return result;
    }

    protected String removeUnsupportedFields(String val, String suppFields[]) {

        String sep[] = {"\r\n"};
        String lines[] = StringUtil.split(val, sep);
        StringBuffer vcalOut = new StringBuffer();
        for(int i=0;i<lines.length;++i) {
            String line = lines[i];
            boolean supported = false;
            for(int j=0;j<suppFields.length;++j) {
                String supp = suppFields[j];
                if (line.startsWith(supp)) {
                    supported = true;
                    break;
                }
            }
            if (supported) {
                vcalOut.append(line).append("\r\n");
            }
        }

        return vcalOut.toString();
    }

    protected String removeAllDayAndEmptyFields(String formattedVcal, String originalVcal) {
        int pos = originalVcal.indexOf("X-FUNAMBOL-ALLDAY");
        if (pos != -1) {
            return formattedVcal;
        } else {
            String sep[] = {"\r\n"};
            String lines[] = StringUtil.split(formattedVcal, sep);
            StringBuffer vcalOut = new StringBuffer();
            for(int i=0;i<lines.length;++i) {
                String line = lines[i];
                if (!line.startsWith("X-FUNAMBOL-ALLDAY")) {
                    if (!line.endsWith(":")) {
                        vcalOut.append(line).append("\r\n");
                    }
                }
            }
            return vcalOut.toString();
        }
    }

    protected String removeEmptyFields(String vcal) {
        String sep[] = {"\r\n"};
        String lines[] = StringUtil.split(vcal, sep);
        StringBuffer vcalOut = new StringBuffer();
        for(int i=0;i<lines.length;++i) {
            String line = lines[i];
            if (!line.endsWith(":")) {
                vcalOut.append(line).append("\r\n");
            }
        }
        return vcalOut.toString();
    }
}

