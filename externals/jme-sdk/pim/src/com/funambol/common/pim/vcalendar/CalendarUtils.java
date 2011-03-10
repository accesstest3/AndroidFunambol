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

package com.funambol.common.pim.vcalendar;

import java.util.Vector;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.funambol.util.Log;
import com.funambol.util.StringUtil;

/**
 * A set of Calendar utility methods 
 */
public class CalendarUtils {

    public static final long  SECOND_FACTOR       = 1000;
    public static final long  MINUTE_FACTOR       = 60*SECOND_FACTOR;
    public static final long  HOUR_FACTOR         = 60*MINUTE_FACTOR;
    public static final long  DAY_FACTOR          = 24*HOUR_FACTOR;

    public final static String PATTERN_YYYYMMDD             = "yyyyMMdd";
    public final static int    PATTERN_YYYYMMDD_LENGTH      = 8;

    public final static String PATTERN_YYYY_MM_DD           = "yyyy-MM-dd";
    public final static int    PATTERN_YYYY_MM_DD_LENGTH    = 10;

    public final static String PATTERN_UTC                  = "yyyyMMdd'T'HHmmss'Z'";
    public final static int    PATTERN_UTC_LENGTH           = 16;

    // UTC WOZ = UTC without 'Z'
    public final static String PATTERN_UTC_WOZ              = "yyyyMMdd'T'HHmmss";
    public final static int    PATTERN_UTC_WOZ_LENGTH       = 15;

    public static final long   UNDEFINED_TIME = -1;

    private static TimeZone utc = TimeZone.getTimeZone("UTC");

    /**
     * Get the local time from DATE/DATE-TIME value, starting from the specified
     * timezone id. If the tzid is not specified (is null) set the timezone to
     * UTC only if the time value is in UTC format (e.g. ends with 'Z'). If the
     * timezone id is specified, try to get the corresponding timezone, if not
     * successfull get the device default timezone.
     * @param value
     * @param tzid
     * @return
     */
    public static long getLocalDateTime(String value, String tzid) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        if(tzid == null) {
            tzid = "UTC";
            if(value.length() == PATTERN_UTC_WOZ_LENGTH) {
                // This is a local time
                tz = TimeZone.getDefault();
                tzid = "";
            }
        } else {
            // If the datetime value contains the Z char it means that the time
            // is expressed in UTC. In this case we force to UTC timezone
            if(value.length() == PATTERN_UTC_LENGTH) {
                // This is a utc time
                tz = TimeZone.getTimeZone("UTC");
            } else {
                tz = TimeZone.getTimeZone(tzid);
                if(!tz.getID().equals(tzid)) {
                    tz = TimeZone.getDefault();
                }
            }
        }
        long utcDate = parseDateTime(value, tz).getTime().getTime();
        return utcDate;
    }

    /**
     * Shift the time from UTC to the local timezone
     * @param time
     * @return
     */
    public static long adjustTimeToDefaultTimezone(long time) {
        return time+getDefaultTimeZoneOffset();
    }

    /**
     * Shift the time from the local timezone do UTC
     * @param time
     * @return
     */
    public static long adjustTimeFromDefaultTimezone(long time) {
        return time-getDefaultTimeZoneOffset();
    }

    /**
      * Get time (a {@code long} value that holds the number of milliseconds
      * since midnight GMT, January 1, 1970) from date in "yyyy-MM-dd" String
      * format
      * @param field Date in "yyyy-MM-dd" String format
      * @return time at 00:00:00 from date
     */
    public static Calendar parseDate(String field, TimeZone tz) {
        int day = 0;
        int month = 0;
        int year = 0;

        Calendar date = null;

        if (field.length() == PATTERN_YYYY_MM_DD_LENGTH) {
            year = Integer.parseInt(field.substring(0, 4));
            month = Integer.parseInt(field.substring(5, 7));
            day = Integer.parseInt(field.substring(8, 10));
        } else {
            year = Integer.parseInt(field.substring(0, 4));
            month = Integer.parseInt(field.substring(4, 6));
            day = Integer.parseInt(field.substring(6, 8));
        }

        date = Calendar.getInstance(tz);

        date.set(Calendar.DAY_OF_MONTH, day);
        date.set(Calendar.MONTH, month - 1);
        if (year < 1970) {
            Log.error("[CalendarUtils.parseDateTime] Date cannot be represented in UTC: " + field);
        }
        date.set(Calendar.YEAR, year);
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        return date;
    }

    /**
     * Get time from date in "yyyyMMddTHHmmssZ" or "yyyyMMdd" or "yyyyMMddTHHmmss"
     *
     * @param data The data to parse
     * @param tz The timezone
     *
     * @return The Calendar object set to the specific date-time
     */
    public static Calendar parseDateTime(String data, TimeZone tz) {

        int day = 0;
        int month = 0;
        int year = 0;
        int hour = 0;
        int minute = 0;
        int second = 0;

        Calendar date = null;
        if (data.length() <= PATTERN_YYYY_MM_DD_LENGTH) {
            return parseDate(data, tz);
        }
        year = Integer.parseInt(data.substring(0, 4));
        if (year < 1970) {
            Log.error("[CalendarUtils.parseDateTime] Date cannot be represented in UTC: " + data);
        }

        month = Integer.parseInt(data.substring(4, 6));
        day = Integer.parseInt(data.substring(6, 8));
        if (data.charAt(8)=='T') {
            hour = Integer.parseInt(data.substring(9, 11));
            minute = Integer.parseInt(data.substring(11, 13));
            second = Integer.parseInt(data.substring(13, 15));
        }
        date = Calendar.getInstance(tz);
        date.set(Calendar.DAY_OF_MONTH, day);
        date.set(Calendar.MONTH, month - 1);
        date.set(Calendar.YEAR, year);
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        date.set(Calendar.SECOND, second);
        date.set(Calendar.MILLISECOND, 0);
        return date;
    }

    /**
     * Get time from date in "yyyyMMddTHHmmssZ" or "yyyyMMdd" or "yyyyMMddTHHmmss"
     * format.
     *
     * @param data The data to parse
     * @param tz The timezone offset String in the ISO 8601 format.
     * @param daylights The Vector containing a list of daylights.
     *
     * @return The Calendar object set to the specific date-time
     */
    public static Calendar parseDateTime(String data, String tz, Vector daylights) {

        Calendar calendar = null;
        
        if(data.length() == PATTERN_UTC_LENGTH) {
            // "yyyyMMddTHHmmssZ"
            calendar = parseDateTime(data, utc);
        } else if(data.length() == PATTERN_UTC_WOZ_LENGTH) {
            // "yyyyMMddTHHmmss"
            if(tz == null) {
                // Set default local timezone if not specified
                calendar = parseDateTime(data, TimeZone.getDefault());
                return calendar;
            } else {
                calendar = parseDateTime(data, utc);
            }
            String daylightOffset = getDaylightSavingOffset(calendar, daylights);
            if(daylightOffset != null) {
                tz = daylightOffset;
            }
            Date date = calendar.getTime();
            long newTime = date.getTime() - getTimezoneOffset(tz);
            date.setTime(newTime);
            calendar.setTime(date);
        } else if(data.length() <= PATTERN_YYYY_MM_DD_LENGTH) {
            // "yyyyMMdd" or "yyyy-MM-dd"
            calendar = parseDate(data, utc);
        }
        return calendar;
    }

    private static String getDaylightSavingOffset(Calendar datetime, Vector daylights) {
        
        for(int i=0; i<daylights.size(); i++) {
            String   dlinfo  = (String)daylights.elementAt(i);
            String[] dlinfos = StringUtil.split(dlinfo, ";");
            if("FALSE".equals(dlinfos[0])) {
                continue;
            }
            Calendar daylightStart  = parseDateTime(dlinfos[2], utc);
            Calendar daylightEnd    = parseDateTime(dlinfos[3], utc);
            if(datetime.after(daylightStart) && datetime.before(daylightEnd)) {
                // We found a corresponfing daylight saving info
                return dlinfos[1];
            }
        }
        return null;
    }

    protected static long getTimezoneOffset(String tzinfo) {
        if(tzinfo == null) {
            return 0;
        }
        String unsigedInfo = tzinfo;
        int sign = 1;
        if (tzinfo.startsWith("+")) {
            unsigedInfo = tzinfo.substring(1);
        } else if (tzinfo.startsWith("-")) {
            unsigedInfo = tzinfo.substring(1);
            sign = -1;
        }
        return getUnsignedTimezoneOffset(unsigedInfo)*sign;
    }

    protected static long getUnsignedTimezoneOffset(String tzinfo) {
        int minutes = 0, hours = 0;
        if(tzinfo.length() > 2) {
            hours = Integer.parseInt(tzinfo.substring(0, 2));
            if(tzinfo.indexOf(":") != -1) {
                minutes = Integer.parseInt(tzinfo.substring(3));
            } else {
                minutes = Integer.parseInt(tzinfo.substring(2));
            }
        } else {
            hours = Integer.parseInt(tzinfo);
        }
        return hours*HOUR_FACTOR + minutes*MINUTE_FACTOR;
    }

    /**
     * Format a DateTime String from the specified time in milliseconds, and the
     * specific timezone offset and daylight. If it's an allday time format as
     * "yyyyMMdd", format as "yyyyMMddTHHmmss(Z)" otherwise.
     * @param milliseconds
     * @param allday
     * @param tz
     * @param daylight
     * @return
     */
    public static String formatDateTime(long milliseconds, boolean allday,
            String tz, Vector daylight) {

        String result = "";

        // Get a calendar instance in UTC time
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(new Date(milliseconds));
        
        String daylightOffset = null;
        if (daylight != null) {
            daylightOffset = getDaylightSavingOffset(calendar, daylight);
        }
        if(daylightOffset != null) {
            tz = daylightOffset;
        }
        if(tz != null) {
            milliseconds += getTimezoneOffset(tz);
            calendar.setTime(new Date(milliseconds));
        }

        // format date "yyyyMMdd"
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        result += getFullInt(year, 4);  //yyyy
        result += getFullInt(month, 2); //MM
        result += getFullInt(day, 2);   //dd

        // add time as "THHmmss(Z)"
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        result += "T";
        result += getFullInt(hour, 2);   //HH
        result += getFullInt(minute, 2); //mm
        result += getFullInt(second, 2); //ss
        if(tz == null) {
            result += "Z";
        }
        return result;
    }
    
    /**
     * Format a DateTime String from the specified time in milliseconds, and the 
     * specific timezone id. If it's an allday time format as "yyyyMMdd", format
     * as "yyyyMMddTHHmmss(Z)" otherwise.
     * @param milliseconds
     * @param allday
     * @param tzid
     * @return
     */
    public static String formatDateTime(long milliseconds, boolean allday, String tzid) {

        String result = "";
        if(tzid == null) {
            tzid = "GMT";
        }
        // Get a calendar instance using the device default timezone
        Calendar date = Calendar.getInstance(TimeZone.getDefault());
        date.setTime(new Date(milliseconds));
        
        TimeZone tz = TimeZone.getTimeZone(tzid);
        if(!tz.getID().equals(tzid)) {
            // If the returned timezone is UTC (e.g. the tzid is unknown) update
            // the timezone id
            tzid = "GMT";
        }
        date.setTimeZone(tz);
        
        // format as "yyyyMMdd"
        int day = date.get(Calendar.DAY_OF_MONTH);
        int month = date.get(Calendar.MONTH) + 1;
        int year = date.get(Calendar.YEAR);
        result += getFullInt(year, 4);  //yyyy
        result += getFullInt(month, 2); //MM
        result += getFullInt(day, 2);   //dd

        if(!allday) {
            // add time as "yyyyMMddTHHmmss(Z)"
            int hour = date.get(Calendar.HOUR_OF_DAY);
            int minute = date.get(Calendar.MINUTE);
            int second = date.get(Calendar.SECOND);
            result += "T";
            result += getFullInt(hour, 2);   //HH
            result += getFullInt(minute, 2); //mm
            result += getFullInt(second, 2); //ss
            if(tzid.equals("GMT") || tzid.equals("UTC")) {
                result += "Z";
            }
        }
        return result;
    }

    /**
     * Fill a number String with '0' chars
     * @param value
     * @param digits
     * @return
     */
    public static String getFullInt(int value, int digits) {
        value = Math.abs(value);
        String result = Integer.toString(value);
        if(value < 10 && digits > 1)    result = "0" + result;
        if(value < 100 && digits > 2)   result = "0" + result;
        if(value < 1000 && digits > 3)  result = "0" + result;
        return result;
    }

    /**
     * Get the offset between GMT and the local timezone
     * @return the offset
     */
    public static long getDefaultTimeZoneOffset() {

        long offset = 0;
        TimeZone zn = TimeZone.getDefault();
        Calendar local = Calendar.getInstance();
        local.setTime(new Date(System.currentTimeMillis()));

        // the offset to add to GMT to get local time, modified in case of
        // daylight savings
        int time = (int)(local.get(Calendar.HOUR_OF_DAY)*HOUR_FACTOR +
                         local.get(Calendar.MINUTE)*MINUTE_FACTOR  +
                         local.get(Calendar.SECOND)*SECOND_FACTOR);
        offset = zn.getOffset(1, // era AD
                              local.get(Calendar.YEAR),
                              local.get(Calendar.MONTH),
                              local.get(Calendar.DAY_OF_MONTH),
                              local.get(Calendar.DAY_OF_WEEK),
                              time);
        Log.trace("[CalendarUtils.getDefaultTimeZoneOffset] Offset: " + offset);
        return offset;
    }

    /**
     * Get the offset between GMT and the specified timezone
     * @return the offset
     */
    public static long getTimeZoneOffset(TimeZone zn) {

        long offset = 0;
        Calendar local = Calendar.getInstance();
        local.setTime(new Date(System.currentTimeMillis()));

        // the offset to add to GMT to get local time, modified in case of
        // daylight savings
        int time = (int)(local.get(Calendar.HOUR_OF_DAY)*HOUR_FACTOR +
                         local.get(Calendar.MINUTE)*MINUTE_FACTOR  +
                         local.get(Calendar.SECOND)*SECOND_FACTOR);
        offset = zn.getOffset(1, // era AD
                              local.get(Calendar.YEAR),
                              local.get(Calendar.MONTH),
                              local.get(Calendar.DAY_OF_MONTH),
                              local.get(Calendar.DAY_OF_WEEK),
                              time);
        Log.trace("[CalendarUtils.getTimeZoneOffset] Offset: " + offset);
        return offset;
    }
}
