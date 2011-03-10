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

package com.funambol.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * Utility class for date manipulation.
 * This class gives a simple interface for common Date, Calendar and Timezone
 * operations.
 * It is possible to apply subsequent transformations to an initial date, and
 * retrieve the changed Date object at any point.
 *
 */
public class DateUtil {
    
    //-------------------------------------------------------------- Attributes
    private Calendar cal;
    
    //------------------------------------------------------------ Constructors
    
    /** Inizialize a new instance with the current date */
    public DateUtil() {
        this(new Date());
    }
    
    /** Inizialize a new instance with the given date */
    public DateUtil(Date d) {
        cal = Calendar.getInstance();
        cal.setTime(d);
    }
    
    //---------------------------------------------------------- Public methods
    
    /** Set a new time */
    public void setTime(Date d) {
        cal.setTime(d);
    }
    
    /** Get the current time */
    public Date getTime() {
        return cal.getTime();
    }
    
    /** Get the current TimeZone */
    public String getTZ() {
        return cal.getTimeZone().getID();
    }
    
    /**
     * Convert the time to the midnight of the currently set date.
     * The internal date is changed after this call.
     *
     * @return a reference to this DateUtil, for concatenation.
     */
    public DateUtil toMidnight() {
        
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND,0);
        
        return this;
    }
    
    /**
     * Make the date go back of the specified amount of days
     * The internal date is changed after this call.
     *
     * @return a reference to this DateUtil, for concatenation.
     */
    public DateUtil removeDays(int days) {
        
        Date d = cal.getTime();
        long time = d.getTime();
        time -= days * 24 * 3600 * 1000;
        d.setTime(time);
        cal.setTime(d);
        
        return this;
    }
    
    /**
     * Make the date go forward of the specified amount of minutes
     * The internal date is changed after this call.
     *
     * @return a reference to this DateUtil, for concatenation.
     */
    public DateUtil addMinutes(int minutes) {
        Date d = cal.getTime();
        long time = d.getTime();
        time += minutes * 60 * 1000;
        d.setTime(time);
        cal.setTime(d);
        
        return this;
    }
    
    /**
     * Convert the date to GMT. The internal date is changed
     *
     * @return a reference to this DateUtil, for concatenation.
     */
    public DateUtil toGMT() {
        return toTZ("GMT");
    }
    
    /**
     * Convert the date to the given timezone. The internal date is changed.
     *
     * @param tz The name of the timezone to set
     *
     * @return a reference to this DateUtil, for concatenation.
     */
    public DateUtil toTZ(String tz) {
        cal.setTimeZone(TimeZone.getTimeZone(tz));
        
        return this;
    }
    
    /**
     * Get the days passed from the specified date up to the date provided 
     * in the constructor
     *
     * @param date The starting date
     *
     * @return number of days within date used in constructor and the provided
     * date
     */
    public int getDaysSince(Date date) {
        long millisecs = date.getTime();
        Date d = cal.getTime();
        long time = d.getTime();
        long daysMillisecs = time - millisecs;
        int days = (int)((((daysMillisecs / 1000)/60)/60)/24);
        return days;
    }
    
    /**
     * Utility method wrapping Calendar.after method
     * Compares the date field parameter with the date provided with the constructor
     * answering the question: date from constructor is after the given param date ?
     *
     * @param date The date to be used for comparison
     *
     * @return true if date from constructor is after given param date
     */
    public boolean isAfter(Date date) {
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);
        return cal.after(cal2);
    }

    /**
     * Format a date-time into UTC format. For example the following string
     * 19980119T070000Z represents 19th Jan 1998 at 07:00:00 in the GMT timezone
     *
     * @param datetime is the date-time to be formatted
     *
     * @return the UTC representation of the give date-time 
     */
    public static String formatDateTimeUTC(Calendar datetime)
    {
        String ret = "";
        int tt = 0;
        ret += datetime.get(Calendar.YEAR);

        tt = datetime.get(Calendar.MONTH)+1;
        ret += tt<10 ? "0"+tt : ""+tt;

        tt = datetime.get(Calendar.DAY_OF_MONTH);
        ret += (tt<10 ? "0"+tt : ""+tt) + "T";

        tt = datetime.get(Calendar.HOUR_OF_DAY);
        ret += tt<10 ? "0"+tt : ""+tt;

        tt = datetime.get(Calendar.MINUTE);
        ret += tt<10 ? "0"+tt : ""+tt;

        tt = datetime.get(Calendar.SECOND);
        ret += (tt<10 ? "0"+tt : ""+tt) + "Z";
        return ret;
    }

    /**
     * Format a date to standard format.
     *
     * @param date is the date to be formatted
     *
     * @return the representation of the give date 
     */
    public static String formatDate(Calendar date)
    {
        StringBuffer ret = new StringBuffer();
        int tt = 0;
        ret.append(date.get(Calendar.YEAR));
        ret.append("-");

        tt = date.get(Calendar.MONTH)+1;
        if (tt < 10) {
            ret.append("0");
        }
        ret.append(tt).append("-");

        tt = date.get(Calendar.DAY_OF_MONTH);
        if (tt < 10) {
            ret.append("0");
        }
        ret.append(tt);
        return ret.toString();
    }


    /**
     * Format a date-time into UTC format. For example the following string
     * 19980119T070000Z represents 19th Jan 1998 at 07:00:00 in the GMT timezone
     *
     * @param date is the date to be formatted. This value will be interpreted
     * as relative to the default timezone 
     *
     * @return the UTC representation of the give date-time 
     */
    public static String formatDateTimeUTC(Date date)
    {
        Calendar datetime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        datetime.setTime(date);
        return formatDateTimeUTC(datetime);
    }

    /**
     * Format a date-time into UTC format. For example the following string
     * 19980119T070000Z represents 19th Jan 1998 at 07:00:00 in the GMT timezone
     *
     * @param date is the date to be formatted. This value will be interpreted
     * as relative to the default timezone 
     *
     * @return the UTC representation of the give date-time 
     */
    public static String formatDateTimeUTC(long date)
    {
        Date d = new Date(date);
        return formatDateTimeUTC(d);
    }

    /**
     * Parse date and time from a string. The method supports multiple formats,
     * in particular:
     * <li>
     *   - yyyyMMddTHHmmssZ
     *   - yyyyMMddTHHmmss
     *   - yyyyMMdd
     *   - yyyy-MM-ddTHH:mm:ssZ
     *   - yyyy-MM-ddTHH:mm:ss
     * @param field date time in one of the supported formats
     * @return a calendar representing the date time
     * @throws IllegalArgumentException if the input date is not valid
     * (according to the acceptable formats)
     *
     * @deprecated use parseDateTime instead
     */
    public static Calendar parseDate(String field)
    {
        return parseDateTime(field);
    }

    /**
     * Parse date and time from a string. The method supports multiple formats,
     * in particular:
     * <li>
     *   - yyyyMMddTHHmmssZ
     *   - yyyyMMddTHHmmss
     *   - yyyyMMdd
     *   - yyyy-MM-ddTHH:mm:ssZ
     *   - yyyy-MM-ddTHH:mm:ss
     * @param field date time in one of the supported formats
     * @return a calendar representing the date time
     * @throws IllegalArgumentException if the input date is not valid
     * (according to the acceptable formats)
     */
    public static Calendar parseDateTime(String field)
    {
        int day = 0;
        int month = 0;
        int year = 0;
        int hour = 0;
        int minute = 0;
        int second = 0;
               
        Calendar date = null;

        if (field == null || field.length() == 0) {
            throw new IllegalArgumentException("Invalid date");
        }

        if (field.charAt(field.length()-1) == 'Z') {
            date = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //GMT
        } else {
            date = Calendar.getInstance(TimeZone.getDefault());
        }
        
        int idx = 0;
        idx = parseDateField(field, idx, 4, '-', Calendar.YEAR, date);
        idx = parseDateField(field, idx, 2, '-', Calendar.MONTH, date);
        idx = parseDateField(field, idx, 2, '-', Calendar.DAY_OF_MONTH, date);

        if (idx < field.length()) {
            if (field.charAt(idx)=='T')
            {
                ++idx;
                idx = parseDateField(field, idx, 2, ':', Calendar.HOUR_OF_DAY, date);
                idx = parseDateField(field, idx, 2, ':', Calendar.MINUTE, date);
                idx = parseDateField(field, idx, 2, ':', Calendar.SECOND, date);
            }
            // else we ignore everything else
        }
        return date;
    }

    /**
     * Get a safe time for all day given a certain calendar. The value returned
     * by this method is guaranteed to be in the given day, even if it is
     * shifted during UTC transformation. This transformation is usually helpful
     * for dates associated to all day events.
     *
     * @param cal a calendar
     * @return the same calendar whose hour is shifted in a way that UTC
     * translation will leave the event in the same day
     */
    public static Calendar getSafeTimeForAllDay(Calendar cal) 
    {
        if (TimeZone.getDefault().getRawOffset() > 0) {
            cal.set(Calendar.HOUR_OF_DAY, 14);
        } else if (TimeZone.getDefault().getRawOffset() <= 0) {
            cal.set(Calendar.HOUR_OF_DAY, 1);
        }
        return cal;
    }
 
 

    private static int parseDateField(String date, int start, int len, char sep,
                                      int field, Calendar cal) {

        if (start + len > date.length()) {
            throw new IllegalArgumentException("Invalid date");
        }
        int value = Integer.parseInt(date.substring(start, start + len));
        start += len;
        if (start < date.length()) {
            // Check if there is a separator to skip
            if (date.charAt(start) == sep) {
                start++;
            }
        }
        if (field == Calendar.MONTH) {
            cal.set(field, value - 1);
        } else {
            cal.set(field, value);
        }
        return start;
    }


}
