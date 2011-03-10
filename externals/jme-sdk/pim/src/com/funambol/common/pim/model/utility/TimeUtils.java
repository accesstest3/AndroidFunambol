/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2005 - 2007 Funambol, Inc.
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
package com.funambol.common.pim.model.utility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @version $Id: TimeUtils.java,v 1.9 2008-09-03 09:49:19 mauro Exp $
 */
public class TimeUtils {

    // --------------------------------------------------------------- Constants
    public final static String PATTERN_YYYYMMDD             = "yyyyMMdd";
    public final static int    PATTERN_YYYYMMDD_LENGTH      = 8;

    public final static String PATTERN_YYYY_MM_DD           = "yyyy-MM-dd";
    public final static int    PATTERN_YYYY_MM_DD_LENGTH    = 10;

    public final static String PATTERN_UTC                  = "yyyyMMdd'T'HHmmss'Z'";
    public final static int    PATTERN_UTC_LENGTH           = 16;

    // UTC WOZ = UTC without 'Z'
    public final static String PATTERN_UTC_WOZ              = "yyyyMMdd'T'HHmmss";
    public final static int    PATTERN_UTC_WOZ_LENGTH       = 15;

    // UTC WSEP = UTC with separator
    public final static String PATTERN_UTC_WSEP             = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public final static int    PATTERN_UTC_WSEP_LENGTH      = 20;

    public final static String PATTERN_LOCALTIME            = "dd/MM/yyyy HH:mm:ss";
    public final static int    PATTERN_LOCALTIME_LENGTH     = 19;

    // WOT = without time
    public final static String PATTERN_LOCALTIME_WOT        = "dd/MM/yyyy";
    public final static int    PATTERN_LOCALTIME_WOT_LENGTH = 10;
    
    public final static String PATTERN_YYYY_MM_DD_HH_MM_SS  = "yyyy-MM-dd HH:mm:ss";
    public final static int    PATTERN_YYYY_MM_DD_HH_MM_SS_LENGTH = 19;

    public final static long   SECOND_IN_A_DAY              = 24 * 60 * 60;  // the seconds in a day

    public final static TimeZone TIMEZONE_UTC               = TimeZone.getTimeZone("UTC");
    
    /* public final static PeriodFormatter INTERVAL_FORMATTER = 
            new PeriodFormatterBuilder()
                    .printZeroRarelyLast()
                    .appendLiteral("P")
                    .appendYears()
                    .appendSuffix("Y")
                    .appendMonths()
                    .appendSuffix("M")
                    .appendDays()
                    .appendSuffix("D")
                    .appendSeparatorIfFieldsAfter("T")
                    .appendHours()
                    .appendSuffix("H")
                    .appendMinutes()
                    .appendSuffix("M")
                    .appendSeconds()
                    .appendSuffix("S")
                    .toFormatter(); 
     */
    
    /**
     * It's a costant value for the number of minutes in a day
     */
    private static int MINUTES_IN_A_DAY = 60*24;
    
    // ---------------------------------------------------------- Public Methods
    /**
     * Set a string date from UTC format (yyyyMMdd'T'HHmmss'Z') into
     * a format dd/MM/yyyy HH:mm:ss according to default local timezone.
     *
     * @param UTCFormat the input date in UTC format
     * @return actualTime the date into default local timezone
     */
    public static String UTCToLocalTime(String UTCFormat, Logger logger) {

        String actualTime = UTCFormat;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(PATTERN_UTC);
            formatter.setLenient(false);
            formatter.setTimeZone(TIMEZONE_UTC);
            Date date = formatter.parse(UTCFormat);
            Calendar rightNow = Calendar.getInstance();
            formatter.setCalendar(rightNow);
            formatter.applyPattern(PATTERN_LOCALTIME);
            actualTime = formatter.format(date);

        } catch (Exception e) {
            if (logger != null) {
                logger.severe("Error into convertion from UTC to Local time");
                logger.throwing("TimeUtils", "UTCToLocalTime", e);
            }
        }
        return actualTime;

    }
    
    /**
     * Gets the day before or after a given argument. Both the argument and the 
     * result are in the "yyyyMMdd" format (the argument can also have dashes).
     * 
     * @param ymd a String representing a date in an all-day format
     * @param after true if the day after is needed, false if it's the day 
     *              before
     * @return a String representing the day before ymd in an all-day
     *         format, or null if ymd was not as expected
     */
    public static String rollOneDay(String ymd, boolean after) {
        String yyyymmdd = ymd.replaceAll("-", "");
        if (yyyymmdd.length() == 8) {
            SimpleDateFormat untimed = new SimpleDateFormat(PATTERN_YYYYMMDD);
            try {
                GregorianCalendar greg = 
                        new GregorianCalendar();
                greg.setTime(untimed.parse(yyyymmdd));
                greg.add(GregorianCalendar.DATE, (after ? +1 : -1));
                return untimed.format(greg.getTime());
            } catch (ParseException pe) {
                return null;
            }
        } else {
            return null;
        }
     }

    /**
     * Set a string date from dd/MM/yyyy HH:mm:ss according to default local
     * timezone into a UTC date pattern yyyyMMdd'T'HHmmss'Z'
     *
     * @param actualTime the date into default local timezone
     * @return UTCFormat the date into UTC format
     */
    public static String localTimeToUTC(String actualTime, Logger logger) {

        String UTCFormat = actualTime;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(PATTERN_LOCALTIME);

            if (actualTime.length() <= 10) {
                formatter.applyPattern(PATTERN_LOCALTIME_WOT);
            }
            Calendar rightNow = Calendar.getInstance();
            formatter.setCalendar(rightNow);
            formatter.setLenient(false);
            Date date = formatter.parse(actualTime);
            TimeZone tz =  TIMEZONE_UTC;
            formatter.setTimeZone(tz);
            formatter.applyPattern(PATTERN_UTC);
            UTCFormat = formatter.format(date);

        } catch (Exception e) {
            if (logger != null) {
                logger.severe("Error into convertion from Local time to UTC");
                logger.throwing("TimeUtils", "localTimeToUTC", e);
            }
        }
        return UTCFormat;
    }

    /**
     * Convert the given date following this roles:
     * <ul>
     * <li>if the given date is in UTC no conversion is required</li>
     * <li>if the given date is not in UTC and the given timezone isn't null,
     * the date is localizated with the timezone and then it's converted in UTC.</li>
     * <li>the returned string is always in UTC</li>
     * <li>if the given timezone is null and the date isn't in UTC, the date is localizated
     * with the default timezone</li>
     * <li>the given date can be in one beetwen the following formats:
     *  <code>yyyyMMdd'T'HHmmss'Z',  yyyyMMdd'T'HHmmss</code></li>
     * </ul>
     * @param sDate the given string date to convert
     * @param timezone TimeZone
     * @return String
     * @throws Exception
     */
    public static String convertLocalDateToUTC(String sDate, TimeZone timezone)
    throws Exception {

        if (sDate == null || sDate.equals("") || isInAllDayFormat(sDate)) {
            return sDate;
        }

        if (sDate.indexOf('Z') != -1) {
            //
            // No conversion is required
            //
            return sDate;
        }
        
        DateFormat zFormatter = new SimpleDateFormat(PATTERN_UTC);
        zFormatter.setTimeZone(TIMEZONE_UTC);

        DateFormat noZFormatter = new SimpleDateFormat(PATTERN_UTC_WOZ);


        Date date = null;
        if (timezone != null) {
            noZFormatter.setTimeZone(timezone);
        } else {
            noZFormatter.setTimeZone(TIMEZONE_UTC);
        }
        date = noZFormatter.parse(sDate);

        return zFormatter.format(date);
    }

    /**
     * Convert the given date following this roles:
     * <ul>
     * <li>if the given timezone isn't null,
     * the date is localizated with the timezone and then reformatted in
     * <code>yyyyMMdd'T'HHmmss'Z'</code>.</li>
     * <li>the returned string is always in this format <code>yyyyMMdd'T'HHmmss'Z'</code></li>
     * <li>if the given timezone is null the date is not changed</li>
     * </ul>
     * @param sDate the given string date to convert
     * @param timezone TimeZone
     * @return String
     * @throws Exception
     */
    public static String convertUTCDateToLocal(String sDate, TimeZone timezone)
    throws Exception {

        if (sDate == null || sDate.equals("") || isInAllDayFormat(sDate)) {
            return sDate;
        }

        if (timezone == null) {
            return sDate;
        }

        if (!sDate.endsWith("Z")) {
            return sDate;
        }

        DateFormat utcFormatter = new SimpleDateFormat(PATTERN_UTC);
        utcFormatter.setTimeZone(TIMEZONE_UTC);

        Date date = null;
        date = utcFormatter.parse(sDate);

        DateFormat utcFormatterWOZ = new SimpleDateFormat(PATTERN_UTC_WOZ);
        utcFormatterWOZ.setTimeZone(timezone);

        return utcFormatterWOZ.format(date);
    }

    /**
     * Convert the given sDate in iso 8601 format.
     * <P>The formats accepted are:
     * <ul>
     * <li>yyyyMMdd'T'HHmmss'Z' (if the date is in this format and the given
     * timezone isn't null and the date doesn't end with 000000Z, the date is localized with
     * the given timezone). We have to check also if the date end with 000000Z because
     * some phones (as Nokia 7650) sends the birthday with the 'Z' but isn't localized</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss'Z' (if the date is in this format and the given
     * timezone isn't null and the date doesn't end with 00:00:00Z, the date is localized with
     * the given timezone).</li>
     * <li>yyyy-MM-dd</li>
     * <li>yyyy/MM/dd</li>
     * <li>yyyyMMdd</li>
     * <li>all formats starts with the previous format (i.e. yyyy-MM-ddTHH:mm:ss.sss)</li>
     * </ul>
     *
     * @param sDate String
     * @return the sDate in iso 8601 format
     */
    public static String normalizeToISO8601(String sDate, TimeZone tz) {

        if (sDate == null || sDate.equals("")) {
            return sDate;
        }

        if (tz != null) {
            //
            // Try to apply the timezone
            //
            SimpleDateFormat utcFormatter = new SimpleDateFormat(PATTERN_UTC);
            utcFormatter.setTimeZone(TIMEZONE_UTC);
            Date date = null;
            try {
                date = utcFormatter.parse(sDate);
                if (!sDate.endsWith("000000Z")) {
                    utcFormatter.setTimeZone(tz);
                    sDate = utcFormatter.format(date);
                }
            } catch (ParseException ex) {
                //
                // Ignore this error. The date isn't in this format.
                //
                // Try with yyyy-MM-dd'T'HH:mm:ss'Z'
                utcFormatter.applyPattern(PATTERN_UTC_WSEP);
                try {
                    date = utcFormatter.parse(sDate);
                    if (!sDate.endsWith("00:00:00Z")) {
                        utcFormatter.setTimeZone(tz);
                        sDate = utcFormatter.format(date);
                    }

                } catch (Exception e) {
                    //
                    // Ignore this error. The date isn't in this format.
                    //
                }
            }
        }
        int year   = -1;
        int month  = -1;
        int day    = -1;
        String tmp = null;
        int last   = 0;

        //
        // The first four digits are the year
        //
        tmp = sDate.substring(0, 4);
        year = Integer.parseInt(tmp);

        //
        // Read the month
        //
        char c = sDate.charAt(4);
        if (c == '/' || c == '-') {
            tmp = sDate.substring(5, 7);
            last = 7;
        } else {
            tmp = sDate.substring(4, 6);
            last = 6;
        }
        month = Integer.parseInt(tmp);

        //
        // Read the day
        //
        c = sDate.charAt(last);
        if (c == '/' || c == '-') {
            tmp = sDate.substring(last + 1, last + 3);
        } else {
            tmp = sDate.substring(last, last + 2);
        }
        day = Integer.parseInt(tmp);

        StringBuffer isoDate = new StringBuffer(10);
        isoDate.append(year).append("-");

        if (month < 10) {
            isoDate.append("0");
        }
        isoDate.append(month).append("-");

        if (day < 10) {
            isoDate.append("0");
        }
        isoDate.append(day);
        return isoDate.toString();
    }

    /**
     * Returns the given Iso 8601 duration in minutes
     * @param iso8601Duration String
     * @return int
     */
    public static int getMinutes(String iso8601Duration) {
        if (iso8601Duration == null ||
            iso8601Duration.equals("") ||
            iso8601Duration.equalsIgnoreCase("null")) {
            return -1;
        }

        Duration d = new Duration();
        d.parse(iso8601Duration);

        long mills = d.getMillis();
        int minutes = (int)(mills / 60L / 1000L);     
        return minutes;
    }

    /**
     * Returns the given minutes in iso 8601 duration format
     * @param minutes String
     * @return String
     */
    public static String getIso8601Duration(String minutes) {

        if (minutes == null || minutes.equals("")) {
            return minutes;
        }

        int min = Integer.parseInt(minutes);

        if (min == -1) {
            return null;
        }

        long mills = min * 60L * 1000L;
        
        Duration d = new Duration();
        d.setMillis(mills);

        return d.format();
    }

    /**
     * If dtStart and duration aren't empty, computes the dtEnd as dtStart + duration
     * and return it. If dtStart or duration are empty, return the given dtEnd unchanges it.
     *
     * The format of the return dtEnd is the same of the dtStart.
     * The format accepted are the following:
     * <lu>
     *  <li>"yyyyMMdd'T'HHmmss'Z'"</li>
     *  <li>"yyyyMMdd'T'HHmmss"</li>
     *  <li>"yyyyMMdd"</li>
     *  <li>"yyyy-MM-dd"</li>
     * </lu>
     *
     * @param dtStart String Date to start the event
     * @param duration String
     * @param dtEnd String
     * @return String
     */
    public static String getDTEnd(String dtStart ,
                                  String duration,
                                  String dtEnd   ,
                                  Logger logger  ) {

        Date dateStart    = null;
        SimpleDateFormat format = null;

        if (duration == null || duration.equals("") ||
            duration.equalsIgnoreCase("null")       ||
            dtStart  == null || dtStart.equals("")  ||
            dtStart.equalsIgnoreCase("null")          ) {

            return dtEnd;
        }
        format = new SimpleDateFormat();

        try {
            format.applyPattern(getDateFormat(dtStart));
            dateStart = format.parse(dtStart);
        } catch (ParseException e) {
            logger.severe("Error into dtStart parsing");
            logger.throwing("TimeUtils", "getDTEnd", e);
            //
            // If we are unable to parse dtStart return dtEnd unchanged
            //
            return dtEnd;
        }

        int minutes = getMinutes(duration);

        Calendar calStart = Calendar.getInstance();
        calStart.setTime(dateStart);

        // In order to avoid issues moving on the end date according to the input
        // period using the number of minutes, since strange behaviours were recognized
        // when the day rapresented by the dtEnd was the end of the daylight time,
        // we check if the amount of minutes is enough to perform the addition
        // as the maximum number of days rapresented by minutes more the rest of
        // the division between minutes and the number of minutes in a day.
        if(minutes>=MINUTES_IN_A_DAY) {
            int day              = getMinutesAsDay(minutes);
            int remainingMinutes = getRemainingMinutes(minutes);
            calStart.add(Calendar.DATE, day);
            calStart.add(Calendar.MINUTE, remainingMinutes);
        } else {
            calStart.add(Calendar.MINUTE, minutes);
        }

        Date dateEnd = calStart.getTime();

        return format.format(dateEnd);
    }

    /**
     * If dtStart and date into Alarm aren't empty, computes the minutes before
     * to start the reminder as dtStart - date alarm = minutes and return it.
     *
     * The format of the return dtEnd is the same of the dtStart.
     * The format accepted are the following:
     * <lu>
     *  <li>"yyyyMMdd'T'HHmmss'Z'"</li>
     *  <li>"yyyyMMdd'T'HHmmss"</li>
     *  <li>"yyyyMMdd"</li>
     *  <li>"yyyy-MM-dd"</li>
     * </lu>
     * However, all times and dates will be interpreted as UTC.
     *
     * @param dtStart String
     * @param dtAlarm String
     *
     * @return int minutes before to start reminder
     */
    public static int getAlarmMinutes(String dtStart,
                                      String dtAlarm,
                                      Logger logger ) {
        Date dateStart = null;
        Date dateAlarm = null;

        if (dtStart == null || dtStart.equals("") ||
            dtAlarm == null || dtAlarm.equals("")) {
            return 0;
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat();
            formatter.setTimeZone(TIMEZONE_UTC);
            
            formatter.applyPattern(getDateFormat(dtStart));
            dateStart = formatter.parse(dtStart);

            formatter.applyPattern(getDateFormat(dtAlarm));
            dateAlarm = formatter.parse(dtAlarm);

        } catch (ParseException e) {
            logger.severe("Error during parser date");
            logger.throwing("TimeUtils", "getAlarmMinutes", e);
            //
            // If we are unable to parse dtStart or dtAlarm return 0
            //
            return 0;
        }

        long minutes = dateStart.getTime() - dateAlarm.getTime();

        return (int) (minutes / 1000 / 60);

    }

    /**
     * Calculate the minutes into int format in the case in which the input
     * is into ISO 8601 format; else return the interval
     *
     * @param interval the interval in which the reminder has to be repeated
     *
     * @return int the interval in minutes format
     */
    public static int getAlarmInterval(String interval) {
        if (interval == null) {
            return 0;
        }
        interval = interval.trim();
        if ("".equals(interval)) {
            return 0;
        }
        
        Duration d = new Duration();
        d.parse(interval);
        long millis = d.getMillis();
        int minutes = (int)(millis / 60000L);
        return minutes;
    }
    
    /**
     * Converts a signed number of minutes into an ISO 8601 interval.
     * 
     * @param minutes
     * @return a string like "-PT15M"
     */
    public static String getAlarmInterval(int minutes) {
        Duration d = new Duration();
        d.setMillis(60000L * minutes);
        return d.format();
    }

    /**
     * Check if the date is into format yyyyMMdd or yyyy-MM-dd.
     * If the date is in one of this format then the event is an AllDay event
     *
     * @param date The date to check (Usually this is the start date event)
     * @return boolean true if the format is right, otherwise false
     */
    public static boolean isInAllDayFormat(String date) {
        String pattern = getDateFormat(date);

        if (pattern == null) {
            return false;
        }

        if (pattern.equals(PATTERN_YYYYMMDD) ||
            pattern.equals(PATTERN_YYYY_MM_DD)) {
            return true;
        }
        return false;
    }

    /**
     * Convert date from yyyy-MM-dd format or from yyyyMMdd format into
     * yyyyMMdd'T'HHmmss format.
     *
     * @param stringDate the date to convert
     * @param hhmmss the hours, minutes, seconds to add
     *
     * @return String the date into proper format
     */
    public static String convertDateFromInDayFormat(String stringDate,
                                                    String hhmmss    )
    throws ParseException {

        if (stringDate == null || stringDate.length() == 0) {
            return "";
        }

        StringBuffer sb = null;

        SimpleDateFormat formatter = new SimpleDateFormat(PATTERN_YYYY_MM_DD);
        Date date;
        try {
            formatter.setLenient(false);
            date = formatter.parse(stringDate);
        } catch (ParseException pe) {
            formatter = new SimpleDateFormat(PATTERN_YYYYMMDD);
            formatter.setLenient(false);
            date = formatter.parse(stringDate);
        }
        formatter.applyPattern(PATTERN_YYYYMMDD);
        sb = new StringBuffer(formatter.format(date));
        sb.append('T').append(hhmmss);
        return sb.toString();
    }

    /**
     * Convert date from yyyy-MM-dd or yyyyMMdd format into format yyyyMMdd'T'HHmmss.
     * If inUtc is true the date is converted in yyyyMMdd'T'HHmmss'Z'
     *
     * @param stringDate the date to convert
     * @param hhmmss the hours, minutes, seconds to add
     *
     * @return String the date into proper format
     */
    public static String convertDateFromInDayFormat(String stringDate,
                                                    String hhmmss,
                                                    boolean inUtc) throws ParseException{

        if (stringDate == null || stringDate.length() == 0) {
            return "";
        }

        StringBuffer sb = null;

        String format = getDateFormat(stringDate);
        SimpleDateFormat formatter = null;
        
        if (format.equals(PATTERN_YYYY_MM_DD)) {
            formatter = new SimpleDateFormat(PATTERN_YYYY_MM_DD);
        } else if (format.equals(PATTERN_YYYYMMDD)) {
            formatter = new SimpleDateFormat(PATTERN_YYYYMMDD);
        } else {
            throw new ParseException("Error, date " + stringDate + 
                    " pattern doesn't match the expected ones, " + 
                    PATTERN_YYYY_MM_DD + " or " + PATTERN_YYYYMMDD, 0);
        }
        formatter.setLenient(false);
        Date date = formatter.parse(stringDate);
        formatter.applyPattern(PATTERN_YYYYMMDD);
        sb = new StringBuffer(formatter.format(date));
        sb.append('T').append(hhmmss);
        if (inUtc) {
            sb.append('Z');
        }
        return sb.toString();
    }

    /**
     * Convert date from the input date format into specificated format.
     *
     *
     * @param patternToUse the pattern to use
     * @param stringDate the date to convert
     * @return String the date into proper format     
     * @throws ParseException
     */
    public static String convertDateFromTo(String stringDate, String patternToUse) throws ParseException {
        if (stringDate == null || stringDate.length() == 0) {
            return "";
        }

        String pattern = getDateFormat(stringDate);
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setLenient(false);
        Date date = formatter.parse(stringDate);
        formatter.applyPattern(patternToUse);
        return formatter.format(date);
    }

    /**
     * Convert date from the input date format into specificated format.
     * <br>In the conversion the following rules are applied:
     * <ul>
     *     <li>if the given timezoneIn is not null, it is applied on the stringDate
     *         conversion
     *     </li>
     *     <li>if the given timezoneOut is not null, it is applied on the output date
     *     </li>
     * </ul>
     * @param stringDate the date to convert
     * @param patternToUse the required pattern for the output date
     * @param timezoneIn the timezone to apply to the given date
     * @param timezoneOut the timezone to apply on the output date
     * @return String the date into proper format
     * @throws ParseException if an error occurs
     */
    public static String convertDateFromTo(String   stringDate,
                                           String   patternToUse,
                                           TimeZone timezoneIn,
                                           TimeZone timezoneOut) throws ParseException {
        if (stringDate == null || stringDate.length() == 0) {
            return "";
        }

        String pattern = getDateFormat(stringDate);
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        if (timezoneIn != null) {
            formatter.setTimeZone(timezoneIn);

        }
        formatter.setLenient(false);
        Date date = formatter.parse(stringDate);
        formatter.applyPattern(patternToUse);
        if (timezoneOut != null) {
            formatter.setTimeZone(timezoneOut);
        }
        return formatter.format(date);
    }

    /**
     * Convert the given date in the given format.

     * @param patternToUse the pattern of the output date
     * @param date the date to convert
     * @return String the date into proper format
     * @throws ParseException if an error occurs
     */
    public static String convertDateTo(Date date, String patternToUse) throws ParseException {
        if (date == null) {
            return null;
        }

        SimpleDateFormat formatter = new SimpleDateFormat();
        formatter.setLenient(false);
        formatter.applyPattern(patternToUse);
        return formatter.format(date);
    }

    /**
     * Convert the given date in the given format.
     *
     *
     * @param timeZone the time zone to use
     * @param patternToUse the pattern of the output date
     * @param date the date to convert
     * @return String the date into proper format     
     * @throws ParseException
     */
    public static String convertDateTo(Date date, TimeZone timeZone, String patternToUse) throws ParseException {
        if (date == null) {
            return null;
        }

        SimpleDateFormat formatter = new SimpleDateFormat();
        if (timeZone != null) {
            formatter.setTimeZone(timeZone);
        }
        formatter.setLenient(false);
        formatter.applyPattern(patternToUse);
        return formatter.format(date);
    }


    /**
     * Get the date pattern
     *
     * @param date the date to get the format
     * @return String the pattern
     */
    public static String getDateFormat(String date) {
        
        if (date == null || date.equals("")) {
            return null;
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        String[] patterns = new String[] {
                            PATTERN_UTC,
                            PATTERN_UTC_WOZ,
                            PATTERN_UTC_WSEP,
                            PATTERN_YYYY_MM_DD,
                            PATTERN_YYYYMMDD,
                            PATTERN_LOCALTIME,
                            PATTERN_LOCALTIME_WOT,
                            PATTERN_YYYY_MM_DD_HH_MM_SS
        };

        int[] patternsLength = new int[] {
                               PATTERN_UTC_LENGTH,
                               PATTERN_UTC_WOZ_LENGTH,
                               PATTERN_UTC_WSEP_LENGTH,
                               PATTERN_YYYY_MM_DD_LENGTH,
                               PATTERN_YYYYMMDD_LENGTH,
                               PATTERN_LOCALTIME_LENGTH,
                               PATTERN_LOCALTIME_WOT_LENGTH,
                               PATTERN_YYYY_MM_DD_HH_MM_SS_LENGTH
        };

        int s = patterns.length; // and also patternsLength.length
        Date d = null;
        for (int i=0; i<s; i++) {
            try {
                dateFormat.applyPattern(patterns[i]);
                dateFormat.setLenient(true);
                d = dateFormat.parse(date);

                if (date.length() == patternsLength[i]) {
                    return patterns[i];
                }

            } catch(ParseException e) {
                continue;
            }
        }
        return null;
    }

    /**
     * Checks if the given dates are relative to an all day event.
     * The dates are relative to an all day event if
     * the start date ends with <code>T000000Z</code> and the end date ends with
     * <code>T235959Z</code> or <code>T235900Z</code> or <code>T240000Z</code>
     * @param dateStart String
     * @param dateEnd String
     * @return true if the given dates are relative to an all day event,
     *         false otherwise
     */
    public static boolean isAllDayEvent(String dateStart, String dateEnd) {
        if (dateStart == null || dateEnd == null) {
            return false;
        }

        if (dateStart.endsWith("T000000Z")) {
            if (dateEnd.endsWith("T235959Z") ||
                dateEnd.endsWith("T235900Z") ||
                dateEnd.endsWith("T240000Z")) {
                //
                // It is an all day event.
                //
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the timestamp of the midnight at a given date (i.e., the midnight 
     * at the beginning of that day). The moment corresponds to midnight in UTC.
     * 
     * @param dateTime a date (and, optionally, a time) in the ISO 8601 format
     * @return the timestamp as a long
     * @throws java.text.ParseException if dateTime has a wrong format
     */
    public static long getMidnightTime(String dateTime) throws ParseException {
        
        while (dateTime.length() < 8) {
            dateTime += "01"; // Quick fix: add default month and/or day
        }
        String yyyyMMdd = dateTime.replaceAll("-", "") // no "yyyy-MM-dd" format
                                  .substring(0, 8); // gets "yyyyMMdd"
                
        SimpleDateFormat JUST_DATE = new SimpleDateFormat("yyyyMMdd");
        JUST_DATE.setTimeZone(TimeZone.getTimeZone("UTC"));
        return JUST_DATE.parse(yyyyMMdd).getTime();
    }

    /**
     * Allows to translate the given number of minutes in
     * the greatest number of day contained in the time rapresent
     * by minutes
     * 
     * @param minutes is the number of minutes we want to traslate in the number
     * of days
     * 
     * @return the greatest number of days contained in the given
     * time expressed by minutes
     */
    private static int getMinutesAsDay(int minutes) {
       int result = 0;
       if(minutes>=MINUTES_IN_A_DAY) {
            result = minutes/MINUTES_IN_A_DAY;
       }
       return result;

    }

    /**
     * return the module between the given number of minutes
     * and the number of minutes in a day
     *
     * @param minutes the number of minutes we want to consider
     *
     * @return the rest you obtain dividing the input values for the number
     * of minutes in a day
     */

    private static int getRemainingMinutes(int minutes) {
        return minutes % MINUTES_IN_A_DAY;
    }

}
