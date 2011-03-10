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

package com.funambol.common.pim.icalendar;

import java.io.OutputStream;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.Event;
import javax.microedition.pim.ToDo;

import com.funambol.util.Log;

import com.funambol.common.pim.PimUtils;
import com.funambol.common.pim.vcalendar.CalendarUtils;
import com.funambol.common.pim.vcalendar.BasicVCalendarFormatter;

/**
 * This class implements an iCalendar formatter for JSR75 Event or ToDo objects.
 * This class should be extended if you want to format extended fields, not
 * directly included in the basic JSR75 implementation.
 * 
 * In particular the following methods should be implemented:
 * 
 * <li>getTZID(PIMItem pimItem);</li>
 * <li>getTZOffset(PIMItem pimItem);</li>
 *
 * <li>getTaskAlarmInterval(PIMItem pimItem);</li>
 * <li>isAllDay(PIMItem pimItem);</li>
 * <li>formatAttendees(PIMItem pimItem, OutputStream os);</li>
 * 
 */
public class ICalendarFormatter extends BasicVCalendarFormatter {

    private final String LOG_TAG = "ICalendarFormatter";

    protected String getVersion() {
        return ICalendar.VERSION;
    }

    public ICalendarFormatter(String defaultCharset) {
        super(defaultCharset);
    }

    /**
     * Format the VALARM component
     * @param pimItem the PIMItem to format
     * @param os the output stream
     * @throws javax.microedition.pim.PIMException
     */
    protected void formatAlarm(PIMItem pimItem, OutputStream os, boolean allFields) throws PIMException {
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
                // The VALARM component has to be formatted
                PimUtils.println(os, ICalendar.BEGIN_VALARM);
                formatTrigger(pimItem, alarmInterval, os, allFields);
                // Format AUDIO ACTION type as default
                PimUtils.println(os, ICalendar.ACTION + ":" + ICalendar.ACTION_AUDIO);
                PimUtils.println(os, ICalendar.END_VALARM);
            }
        }
    }
    protected void formatTrigger(PIMItem pimItem, int alarmInterval, OutputStream os,
            boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatTrigger");
        PimUtils.println(os, ICalendar.TRIGGER + ";" +
                    ICalendar.VALUE + "=" + ICalendar.DATE_TIME_VALUE + ":" +
                    alarm.getTriggerAbsoluteTime());
    }

    /**
     * Format the VTIMEZONE component
     * @param pimItem the PIMItem to format
     * @param os the output stream
     * @throws javax.microedition.pim.PIMException
     */
    protected void formatTimezone(PIMItem pimItem, OutputStream os) throws PIMException {
        Log.trace(LOG_TAG, "formatTimezone");
        String tzid = getTZID(pimItem);
        if(tzid != null) {
            // The VTIMEZONE component has to be formatted
            PimUtils.println(os, ICalendar.BEGIN_VTIMEZONE);
            PimUtils.println(os, ICalendar.TZID + ":" + tzid);
            PimUtils.println(os, ICalendar.END_VTIMEZONE);
        }
    }

    protected void formatDateTimeField(OutputStream os, PIMItem pimItem, int pimField,
            String iCalField, boolean checkAllDay, boolean checkTimezone,
            boolean allFields) throws PIMException {

        Log.trace(LOG_TAG, "formatDateTime");
        if(isSupported(pimItem, pimField)) {
            if(pimItem.countValues(pimField) > 0) {
                long millis = pimItem.getDate(pimField, 0);
                boolean allday = checkAllDay ? isAllDay(pimItem) : false;
                if(allday) {
                    iCalField += ";" + ICalendar.VALUE + "=" + ICalendar.DATE_VALUE;
                    if((pimItem instanceof Event) && pimField == Event.END) {
                        millis = fixEndDate(millis);
                    }
                }
                String tzid = "UTC";
                if(checkTimezone && !allday) {
                    tzid = getTZID(pimItem);
                    if(tzid != null) {
                        // Add the TZID param
                        iCalField += ";" + ICalendar.TZID + "=" + tzid;
                        long offset = getTZOffset(pimItem);
                        millis += offset;
                    }
                }
                String dateValue = CalendarUtils.formatDateTime(millis, allday, tzid);
                PimUtils.println(os, iCalField + ":" + dateValue);
                if((pimItem instanceof Event) && pimField == Event.START) {
                    alarm.setCalStartAbsoluteTime(millis);
                }
            } else if(allFields) {
                // Format empty property
                PimUtils.println(os, iCalField + ":");
            }
        }
    }

    protected String   getTZID(PIMItem pimItem)  { return null; }
    protected long getTZOffset(PIMItem pimItem)  { return 0; }
}
