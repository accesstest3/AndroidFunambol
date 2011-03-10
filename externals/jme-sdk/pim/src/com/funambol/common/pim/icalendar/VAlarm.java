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
import com.funambol.util.StringUtil;

import com.funambol.common.pim.vcalendar.CalendarUtils;

/**
 * This class represents a VAlarm object which handles only trigger information
 * Once you've set the correct absolute/relative trigger time and the trigger
 * related time (start/end time), you can get the alarm interval in milliseconds
 */
public class VAlarm {

    private final String LOG_TAG = "VAlarm";
    
    private static final String RELATED_START      = "START";
    private static final String RELATED_END        = "END";

    private long alarmAbsoluteTime = CalendarUtils.UNDEFINED_TIME;
    private long relatedStartTime  = CalendarUtils.UNDEFINED_TIME;
    private long relatedEndTime    = CalendarUtils.UNDEFINED_TIME;

    private String related = RELATED_START;

    public VAlarm() { }

    /**
     * Set the alarm trigger absolute time.
     * @param time the alarm absolute time in String format.
     */
    public void setTriggerAbsoluteTime(String time) {
        alarmAbsoluteTime = CalendarUtils.getLocalDateTime(time, "GMT");
    }

    /**
     * Set the alarm trigger absolute time.
     * @param time the alarm absolute time in milliseconds.
     */
    public void setTriggerAbsoluteTime(long time) {
        alarmAbsoluteTime = time;
    }

    /**
     * Get the alarm trigger absolute time
     * @return the alarm absolute time in String format
     */
    public String getTriggerAbsoluteTime() {
        return CalendarUtils.formatDateTime(alarmAbsoluteTime, false, "GMT");
    }

    /**
     * Set the alarm trigger relative time (the interval). Up to now it's not
     * supported yet.
     * @param time the relative time in String ISO 8601 format
     * @throws com.funambol.common.pim.icalendar.ParseException
     */
    public void setTriggerRelativeTime(String time) throws ParseException {
        // We cannot convert ISO 8601 duration
        Log.error(LOG_TAG, "Cannot convert ISO 8601 duration");
        throw new ParseException("Cannot convert ISO 8601 duration");
    }

    /**
     * Set the related start absolute time
     * @param time the absolute time in milliseconds
     */
    public void setCalStartAbsoluteTime(long time) {
        relatedStartTime = time;
    }

    /**
     * Set the related end absolute time
     * @param time the absolute time
     */
    public void setCalEndAbsoluteTime(long time) {
        relatedEndTime = time;
    }

    /**
     * Set the alarm related property: START or END
     * @param related the related property
     */
    public void setTriggerRelated(String related) {
        if(related != null) {
            this.related = related;
        }
    }

    /**
     * Get the alarm interval in milliseconds
     * @return the alarm interval in milliseconds
     */
    public long getAlarmInterval() {
        if(alarmAbsoluteTime == CalendarUtils.UNDEFINED_TIME) {
            Log.error(LOG_TAG, "The alarm absolute time hasn't been set yet");
            return CalendarUtils.UNDEFINED_TIME;
        }
        long date;
        if(StringUtil.equalsIgnoreCase(related, RELATED_START)) {
            date = relatedStartTime;
        } else {
            date = relatedEndTime;
        }
        if(date == CalendarUtils.UNDEFINED_TIME) {
            Log.error(LOG_TAG, "The alarm related absolute time hasn't been set yet");
            return CalendarUtils.UNDEFINED_TIME;
        }
        return Math.abs(date-alarmAbsoluteTime);
    }

    /**
     * Set the alarm interval in milliseconds.
     * @param interval the alarm interval in milliseconds
     * @return true if the interval is successfully updated
     */
    public boolean setAlarmInterval(long interval) {
        if(relatedStartTime == CalendarUtils.UNDEFINED_TIME) {
            Log.error(LOG_TAG, "The alarm start time hasn't been set yet");
            return false;
        }
        if(interval>0) {
            alarmAbsoluteTime = relatedStartTime-interval;
            return true;
        } else {
            Log.error(LOG_TAG, "Invalid interval: " + interval);
            return false;
        }
    }
}
