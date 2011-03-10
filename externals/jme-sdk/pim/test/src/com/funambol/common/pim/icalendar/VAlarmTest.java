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


import com.funambol.util.Log;
import com.funambol.util.ConsoleAppender;

import com.funambol.common.pim.vcalendar.CalendarUtils;
import junit.framework.*;

/**
 * This is a VAlarmTest class
 */
public class VAlarmTest extends TestCase {

    private String stringTime = "19701025T020000Z";
    private String relatedTime = "19701025T022000Z";

    private VAlarm vAlarm= null;

    public VAlarmTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.TRACE);
    }
    
    public void setUp() {
        Log.setLogLevel(Log.TRACE);
    }
    
    public void tearDown() {
    }

    public void testSetGetTriggerAbsoluteTime() throws AssertionFailedError {
        Log.debug("--- testSetGetTriggerAbsoluteTime ---");

        vAlarm = new VAlarm();
        vAlarm.setTriggerAbsoluteTime(stringTime);
        String absoluteTime = vAlarm.getTriggerAbsoluteTime();

        assertEquals("testSetGetTriggerAbsoluteTime", stringTime,absoluteTime);

    }

    public void testGetAlarmIntervalUNDEFINED_TIME() throws AssertionFailedError {
        Log.debug("--- testGetAlarmIntervalUNDEFINED_TIME ---");
        vAlarm = new VAlarm();

        long alarm = vAlarm.getAlarmInterval();

        assertEquals("testGetAlarmIntervalUNDEFINED_TIME", CalendarUtils.UNDEFINED_TIME,alarm);
    }

    public void testGetAlarmIntervalRELATED_START() throws AssertionFailedError {
        Log.debug("--- testGetAlarmIntervalRELATED_START ---");
        vAlarm = new VAlarm();
        vAlarm.setTriggerAbsoluteTime(stringTime);
        vAlarm.setCalStartAbsoluteTime(CalendarUtils.getLocalDateTime(relatedTime, "UTC"));
        long expectedAlarm = 1200000;
        long alarm = vAlarm.getAlarmInterval();

        Log.debug("expectedAlarm: " + expectedAlarm);
        Log.debug("alarm: " + alarm);
        assertEquals("testGetAlarmIntervalRELATED_START", expectedAlarm,alarm);
    }

    public void testGetAlarmIntervalRELATED_END() throws AssertionFailedError {
        Log.debug("--- testGetAlarmIntervalRELATED_END ---");
        vAlarm = new VAlarm();
        vAlarm.setTriggerAbsoluteTime(stringTime);
        vAlarm.setTriggerRelated("END");
        vAlarm.setCalEndAbsoluteTime(CalendarUtils.getLocalDateTime(relatedTime, "UTC"));

        long expectedAlarm = 1200000;
        long alarm = vAlarm.getAlarmInterval();

        assertEquals("testGetAlarmIntervalRELATED_END", expectedAlarm,alarm);
    }

    public void testGetAlarmIntervalNOABSOLUTE_TIME() throws AssertionFailedError {
        Log.debug("--- testGetAlarmIntervalNOABSOLUTE_TIME ---");
        vAlarm = new VAlarm();

        vAlarm.setCalStartAbsoluteTime(CalendarUtils.getLocalDateTime(relatedTime, "UTC"));

        long alarm = vAlarm.getAlarmInterval();

        assertEquals("testGetAlarmIntervalNOABSOLUTE_TIME", CalendarUtils.UNDEFINED_TIME,alarm);
    }

    public void testSetAlarmIntervalNORELATED_START_TIME() throws AssertionFailedError {
        Log.debug("--- testSetAlarmIntervalNORELATED_START_TIME ---");
        vAlarm = new VAlarm();
        boolean setted = vAlarm.setAlarmInterval(10000);
        assertTrue("testSetAlarmIntervalNORELATED_START_TIME", !setted);
    }

    public void testSetAlarmIntervalNOINTERVALL() throws AssertionFailedError {
        Log.debug("--- testSetAlarmIntervalNOINTERVALL ---");
        vAlarm = new VAlarm();

        vAlarm.setCalStartAbsoluteTime(CalendarUtils.getLocalDateTime(relatedTime, "UTC"));
        boolean setted = vAlarm.setAlarmInterval(0);
        assertTrue("testSetAlarmIntervalNOINTERVALL", !setted);
    }

    public void testSetAlarmInterval() throws AssertionFailedError {
        Log.debug("--- testSetAlarmInterval ---");
        vAlarm = new VAlarm();
        vAlarm.setCalStartAbsoluteTime(CalendarUtils.getLocalDateTime(relatedTime, "UTC"));
        boolean setted = vAlarm.setAlarmInterval(10000);
        assertTrue("testSetAlarmInterval", setted);

        String absoluteTime = vAlarm.getTriggerAbsoluteTime();
        String expectedTime = "19701025T021950Z";
        assertEquals("testSetAlarmInterval", expectedTime,absoluteTime);
    }
}

