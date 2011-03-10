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

import java.io.OutputStream;
import java.util.Vector;
import javax.microedition.pim.Event;
import javax.microedition.pim.ToDo;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMException;

import com.funambol.util.Log;

import com.funambol.common.pim.PimUtils;
import com.funambol.common.pim.vcalendar.CalendarUtils;
import com.funambol.common.pim.vcalendar.BasicVCalendarFormatter;

/**
 * This class implements a vCalendar 1.0 formatter for JSR75 Event or ToDo
 * objects.
 * This class should be extended if you want to format extended fields, not
 * directly included in the basic JSR75 implementation.
 * 
 * In particular the following methods should be implemented:
 * 
 * <li>getTZ(PIMItem pimItem);</li>
 * <li>getDaylight(PIMItem pimItem);</li>
 * 
 * <li>getTaskAlarmInterval(PIMItem pimItem);</li>
 * <li>isAllDay(PIMItem pimItem);</li>
 * <li>formatAttendees(PIMItem pimItem, OutputStream os);</li>
 *
 * Note: the items alarm property is mapped to the AALARM vCalendar property.
 */
public class XVCalendarFormatter extends BasicVCalendarFormatter {

    private final String LOG_TAG = "XVCalendarFormatter";

    protected String getVersion() {
        return XVCalendar.VERSION;
    }

    protected void formatAlarm(PIMItem pimItem, OutputStream os,
            boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatAlarm");
        int alarmInterval = -1;
        if(pimItem instanceof Event) {
            if (isSupported(pimItem, Event.ALARM) && pimItem.countValues(Event.ALARM) > 0) {
                alarmInterval = pimItem.getInt(Event.ALARM, 0);
            }
        } else if(pimItem instanceof ToDo) {
            alarmInterval = getTaskAlarmInterval(pimItem);
        }
        if (alarmInterval >= 0) {
            if(alarm.setAlarmInterval(alarmInterval*1000)) {
                // Format AALARM by default
                PimUtils.println(os, XVCalendar.AALARM + ":" + alarm.getTriggerAbsoluteTime());
            }
        } else if(allFields) {
            // Format empty AALARM 
            PimUtils.println(os, XVCalendar.AALARM + ":");
        }
    }

    protected void formatTimezone(PIMItem pimItem, OutputStream os) throws PIMException {
        Log.trace(LOG_TAG, "formatTimezone");
        String tz = getTZ(pimItem);
        if(tz != null) {
            PimUtils.println(os, XVCalendar.TZ + ":" + tz);
        }
        Vector daylight = getDaylight(pimItem);
        if(daylight != null) {
            for(int i=0; i<daylight.size(); i++) {
                PimUtils.println(os, XVCalendar.DAYLIGHT + ":" + daylight.elementAt(i));
            }
        }
    }

    protected void formatDateTimeField(OutputStream os, PIMItem pimItem, int pimField,
            String vCalField, boolean checkAllDay, boolean checkTimezone, boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatDateTime");
        if(isSupported(pimItem, pimField)) {
            if(pimItem.countValues(pimField) > 0) {
                long millis = pimItem.getDate(pimField, 0);
                boolean allday = checkAllDay ? isAllDay(pimItem) : false;
                String value = CalendarUtils.formatDateTime(millis, allday,
                        getTZ(pimItem), getDaylight(pimItem));
                PimUtils.println(os, vCalField + ":" + value);
                if((pimItem instanceof Event) && pimField == Event.START) {
                    alarm.setCalStartAbsoluteTime(millis);
                }
            } else if(allFields) {
                // Format empty property
                PimUtils.println(os, vCalField + ":");
            }
        }
        Log.trace(LOG_TAG, "Done");
    }

    protected void formatFunambolAllday(PIMItem pimItem, OutputStream os) throws PIMException {
        PimUtils.println(os, XVCalendar.X_FUNAMBOL_ALLDAY + ":" +
                (isAllDay(pimItem) ? "1" : "0"));
    }

    protected String  getTZ(PIMItem pimItem)        { return null; }
    protected Vector  getDaylight(PIMItem pimItem)  { return null; }
}
