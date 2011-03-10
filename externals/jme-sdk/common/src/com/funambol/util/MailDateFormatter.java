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
import java.util.Hashtable;

/**
 * A utility class providing methods to convert date information contained in
 * <code>Date</code> objects into RFC2822 and UTC ('Zulu') strings, and to
 * build <code>Date</code> objects starting from string representations of
 * dates in RFC2822 and UTC format.
 * This class does not handle corretly TimeZone in the Java SE env, but it works
 * fine on ME. Therefore it is currently only included in ME based versions.
 */
public class MailDateFormatter {
    
    /** Format date as: MM/DD */
    public static final int FORMAT_MONTH_DAY = 0;
    /** Format date as: MM/DD/YYYY */
    public static final int FORMAT_MONTH_DAY_YEAR = 1;
    /** Format date as: hh:mm */
    public static final int FORMAT_HOURS_MINUTES = 2;
    /** Format date as: hh:mm:ss */
    public static final int FORMAT_HOURS_MINUTES_SECONDS = 3;
    /** Format date as: DD/MM */
    public static final int FORMAT_DAY_MONTH = 4;
    /** Format date as: DD/MM/YYYY */
    public static final int FORMAT_DAY_MONTH_YEAR = 5;
    
    /** Device offset, as string */
    private static String deviceOffset = "+0000";
    /** Device offset, in millis */
    private static long millisDeviceOffset = 0;

    /** Names of the months */
    private static String[] monthNames = new String[] {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };
     
    /**
     * Transforms data contained in a <code>Date</code> object (expressed in
     * UTC) in a string formatted as per RFC2822 in local time (par. 3.3)
     *
     * @return A string representing the date contained in the passed
     *         <code>Date</code> object formatted as per RFC 2822 and in local
     *         time
     */
    public static String dateToRfc2822(Date date) {
        
        Calendar deviceTime = Calendar.getInstance();
        deviceTime.setTime(date);
   
        String dayweek = "";
        int dayOfWeek = deviceTime.get(deviceTime.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case 1 : dayweek = "Sun"; break;
            case 2 : dayweek = "Mon"; break;
            case 3 : dayweek = "Tue"; break;
            case 4 : dayweek = "Wed"; break;
            case 5 : dayweek = "Thu"; break;
            case 6 : dayweek = "Fri"; break;
            case 7 : dayweek = "Sat"; break;
        }
        
        int dayOfMonth = deviceTime.get(deviceTime.DAY_OF_MONTH);
        
        String monthInYear = getMonthName(deviceTime.get(deviceTime.MONTH));
        
        int year = deviceTime.get(deviceTime.YEAR);

        int hourOfDay = deviceTime.get(deviceTime.HOUR_OF_DAY);
        int minutes = deviceTime.get(deviceTime.MINUTE);
        int seconds = deviceTime.get(deviceTime.SECOND);
        
        String rfc = dayweek + ", " +                                   // Tue
                dayOfMonth + " " +                                      // 7
                monthInYear + " " +                                     // Nov
                year + " " +                                            // 2006
                hourOfDay + ":" + minutes + ":" + seconds + " " +       // 14:13:26
                deviceOffset;                                           //+0200  
        return rfc;
    }
    
    
    /**
     * Converts a <code>Date</code> object into a string in 'Zulu' format
     *
     * @param d
     *            A <code>Date</code> object to be converted into a string in
     *            'Zulu' format
     * @return A string representing the date contained in the passed
     *         <code>Date</code> object in 'Zulu' format (e.g.
     *         yyyyMMDDThhmmssZ)
     */
    public static String dateToUTC(Date d) {
        StringBuffer date = new StringBuffer();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        
        date.append(cal.get(Calendar.YEAR));
        
        date.append(printTwoDigits(cal.get(Calendar.MONTH) + 1))
            .append(printTwoDigits(cal.get(Calendar.DATE)))
            .append("T");
        
        date.append(printTwoDigits(cal.get(Calendar.HOUR_OF_DAY)))
            .append(printTwoDigits(cal.get(Calendar.MINUTE)))
            .append(printTwoDigits(cal.get(Calendar.SECOND)))
            .append("Z");
        
        return date.toString();
    }
    
    
    /**
     * A method that returns a string rapresenting a date.
     *
     * @param date the date
     *
     * @param format the format as one of
     * FORMAT_MONTH_DAY,
     * FORMAT_MONTH_DAY_YEAR,
     * FORMAT_HOURS_MINUTES,
     * FORMAT_HOURS_MINUTES_SECONDS
     * FORMAT_DAY_MONTH
     * FORMAT_DAY_MONTH_YEAR
     * constants
     *
     * @param separator the separator to be used
     */
    public static String getFormattedStringFromDate(
            Date date, int format, String separator) {
        
        Calendar cal=Calendar.getInstance();
        cal.setTime(date);
        StringBuffer ret = new StringBuffer();
        
        switch (format) {
            case FORMAT_HOURS_MINUTES:
                //if pm and hour == 0 we want to write 12, not 0
                if (cal.get(Calendar.AM_PM)==Calendar.PM
                        && cal.get(Calendar.HOUR) == 0) {
                    ret.append("12");
                } else {
                    ret.append(cal.get(Calendar.HOUR));
                }
                ret.append(separator)
                    .append(printTwoDigits(cal.get(Calendar.MINUTE)))
                    .append(getAMPM(cal));
                break;
                
            case FORMAT_HOURS_MINUTES_SECONDS:
                //if pm and hour == 0 we want to write 12, not 0
                if (cal.get(Calendar.AM_PM)==Calendar.PM
                        && cal.get(Calendar.HOUR) == 0) {
                    ret.append("12");
                } else {
                    ret.append(cal.get(Calendar.HOUR));
                }
                ret.append(separator)
                    .append(printTwoDigits(cal.get(Calendar.MINUTE)))
                    .append(separator)
                    .append(cal.get(Calendar.SECOND))
                    .append(getAMPM(cal));
                break;
                
            case FORMAT_MONTH_DAY:
                ret.append(cal.get(Calendar.MONTH)+1)
                    .append(separator)
                    .append(cal.get(Calendar.DAY_OF_MONTH));
                break;
                
            case FORMAT_DAY_MONTH:
                ret.append(cal.get(Calendar.DAY_OF_MONTH))
                    .append(separator)
                    .append(cal.get(Calendar.MONTH)+1);
                break;
                
            case FORMAT_MONTH_DAY_YEAR:
                ret.append(cal.get(Calendar.MONTH)+1)
                    .append(separator)
                    .append(cal.get(Calendar.DAY_OF_MONTH))
                    .append(separator)
                    .append(cal.get(Calendar.YEAR));
                break;
                
            case FORMAT_DAY_MONTH_YEAR:
                ret.append(cal.get(Calendar.DAY_OF_MONTH))
                    .append(separator)
                    .append(cal.get(Calendar.MONTH)+1)
                    .append(separator)
                    .append(cal.get(Calendar.YEAR));
                break;
                
            default:
                Log.error("getFormattedStringFromDate: invalid format ("+
                        format+")");
        }
        
        return ret.toString();
    }
    
    /**
     * Returns a localized string representation of Date.
     */
    public static String formatLocalTime(Date d) {
        int dateFormat = FORMAT_MONTH_DAY_YEAR;
        int timeFormat = FORMAT_HOURS_MINUTES;
        
        if(!System.getProperty("microedition.locale").equals("en")) {
            dateFormat = FORMAT_DAY_MONTH_YEAR;
        }
        return getFormattedStringFromDate(d,FORMAT_MONTH_DAY_YEAR,"/")
                +" "+getFormattedStringFromDate(d,FORMAT_HOURS_MINUTES,":");
    }
    
    
    /**
     * Parses the string in RFC 2822 format and return a <code>Date</code>
     * object. <p>
     * Parse strings like:
     * Thu, 03 May 2007 14:45:38 GMT
     * Thu, 03 May 2007 14:45:38 GMT+0200
     * Thu,  1 Feb 2007 03:57:01 -0800
     * Fri, 04 May 2007 13:40:17 PDT
     * 
     * @param d the date representation to parse
     * @return a date, if valid, or null on error
     *
     */
    public static Date parseRfc2822Date(String stringDate) {
        if (stringDate == null) {
            return null;
        }
        
        long hourOffset=0;
        long minOffset=0;
        Calendar cal = Calendar.getInstance();
        
        try {
            
            Log.debug("[MailDateFormatter] Date original: " + stringDate);
            // We use the ' ' as separator and we expect only one space. We
            // clean the string to remove extra spaces
            StringBuffer cleanedDate = new StringBuffer();
            char previous = 'a';
            for(int i=0;i<stringDate.length();++i) {
                char ch = stringDate.charAt(i);
                if (ch != ' ' || previous != ' ') {
                    cleanedDate.append(ch);
                }
                previous = ch;
            }
            stringDate = cleanedDate.toString();
            Log.debug("Cleaned date: " + stringDate);
            
            // Just skip the weekday if present
            int start = stringDate.indexOf(',');
            //put start after ", "
            start = (start == -1) ? 0 : start + 2;
            
            stringDate = stringDate.substring(start).trim();
            start = 0;
            
            // Get day of month
            int end = stringDate.indexOf(' ', start);
           
            //4  Nov 2008 10:30:05 -0400
            
            int day =1;
            try {
               day = Integer.parseInt(stringDate.substring(start, end)); 
            } catch (NumberFormatException ex) {
                Log.error("[MailDateFormatter]Invalid date with day: " + stringDate.substring(start, end));
                // some phones (Nokia 6111) have a invalid date format, 
                // something like Tue10 Jul... instead of Tue, 10 
                // so we try to strip (again) the weekday
                Log.debug("[MailDateFormatter] Applying Nokia 6111 patch");
                day = Integer.parseInt(stringDate.substring(start+3, end));
            }
            
            cal.set(Calendar.DAY_OF_MONTH,day);
            
            // Get month
            start = end + 1;
            end = stringDate.indexOf(' ', start);
            cal.set(Calendar.MONTH, getMonthNumber(stringDate.substring(start, end)));

            // Get year
            start = end + 1;
            end = stringDate.indexOf(' ', start);
            cal.set(Calendar.YEAR,
                    Integer.parseInt(stringDate.substring(start, end)));

            // Get hour
            start = end + 1;
            end = stringDate.indexOf(':', start);
            cal.set(Calendar.HOUR_OF_DAY,
                    Integer.parseInt(stringDate.substring(start, end).trim()));

            // Get min
            start = end + 1;
            end = stringDate.indexOf(':', start);
            cal.set(Calendar.MINUTE,
                    Integer.parseInt(stringDate.substring(start, end)));

            // Get sec
            start = end + 1;
            end = stringDate.indexOf(' ', start);
            cal.set(Calendar.SECOND,
                    Integer.parseInt(stringDate.substring(start, end)));

            // Get OFFSET
            start = end +1;
            end = stringDate.indexOf('\r', start);
            
            // Process Timezone, checking first for the actual RFC2822 format,
            // and then for nthe obsolete syntax.

            char sign = '+';
            String hourDiff = "0";
            String minDiff = "0";

            String offset = stringDate.substring(start).trim();
            if (offset.startsWith("+") || offset.startsWith("-")) {
                if(offset.length() >= 5 ){
                    sign = offset.charAt(0);
                    hourDiff = offset.substring(1,3);
                    minDiff = offset.substring(3,5);
                }
                else if(offset.length() == 3){
                    sign = offset.charAt(0);
                    hourDiff = offset.substring(1);
                    minDiff = "00";
                }
                // Convert offset to int
                hourOffset = Long.parseLong(hourDiff);
                minOffset = Long.parseLong(minDiff);
                if(sign == '-') {
                    hourOffset = -hourOffset;
                }
            }
            else if(offset.equals("EDT")){
                hourOffset = -4;
            }
            else if(offset.equals("EST") || offset.equals("CDT")){
                hourOffset = -5;
            }
            else if(offset.equals("CST") || offset.equals("MDT")){
                hourOffset = -6;
            }
            else if(offset.equals("PDT") || offset.equals("MST")){
                hourOffset = -7;
            }
            else if(offset.equals("PST")){
                hourOffset = -8;
            }
            else if(offset.equals("GMT") || offset.equals("UT")){
                hourOffset = 0;
            }
            else if (offset.substring(0,3).equals("GMT") && offset.length() > 3){
                sign = offset.charAt(3);
                hourDiff = offset.substring(4,6);
                minDiff = offset.substring(6,8);                
            }

            long millisOffset = (hourOffset * 3600000) + (minOffset * 60000);
     
            Date gmtDate = cal.getTime();
            long millisDate = gmtDate.getTime();
            
            millisDate -= millisOffset;
            
            gmtDate.setTime(millisDate);
            return gmtDate;
            
        } catch (Exception e) {
            Log.error("Exception in parseRfc2822Date: " + e.toString() +
                      " parsing " + stringDate);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Convert the given date (GMT) into the local date.
     * NOTE: changes the original date too!
     * Should we change it to a void toLocalDate(Date) that changes the
     * input date only?
     */
    public static Date getDeviceLocalDate (Date gmtDate){
        if (null != gmtDate){
            /*long dateInMillis = gmtDate.getTime();
            Date deviceDate = new Date();
            deviceDate.setTime(dateInMillis+millisDeviceOffset);
            return deviceDate;
             **/
            gmtDate.setTime(gmtDate.getTime()+millisDeviceOffset);
            return gmtDate;
        }
        else {
            return null;
        }
    }
    
    
    /**
     * Gets a <code>Date</code> object from a string representing a date in
     * 'Zulu' format (yyyyMMddTHHmmssZ)
     *
     * @param utc
     *            date in 'Zulu' format (yyyyMMddTHHmmssZ)
     * @return A <code>Date</code> object obtained starting from a time in
     *         milliseconds from the Epoch
     */
    public static Date parseUTCDate(String utc) {
        
        int day = 0;
        int month = 0;
        int year = 0;
        int hour = 0;
        int minute = 0;
        int second = 0;
        Calendar calendar = null;
        
        day = Integer.parseInt(utc.substring(6, 8));
        month = Integer.parseInt(utc.substring(4, 6));
        year = Integer.parseInt(utc.substring(0, 4));
        hour = Integer.parseInt(utc.substring(9, 11));
        minute = Integer.parseInt(utc.substring(11, 13));
        second = Integer.parseInt(utc.substring(13, 15));
        
        
        calendar = Calendar.getInstance();
        
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        
        
        Date date = calendar.getTime();
        long dateInMillis = date.getTime();
            
        date.setTime(dateInMillis+millisDeviceOffset);
            
        return date;
    }
    
    public static void setTimeZone(String timeZone){
        
        if (timeZone == null || timeZone.length() < 5) {
            Log.error("setTimeZone: invalid timezone " + timeZone);
        }

        try {
            deviceOffset = timeZone;
            String hstmz = deviceOffset.substring(1, 3);
            String mstmz = deviceOffset.substring(3, 5);
            
            long hhtmz = Long.parseLong(hstmz);
            long mmtmz = Long.parseLong(mstmz);
            millisDeviceOffset = (hhtmz * 3600000) + (mmtmz * 60000);
            if(deviceOffset.charAt(0)=='-') {
                millisDeviceOffset *= -1;
            }   

        } catch(Exception e) {
            Log.error("setTimeZone: " + e.toString());
            e.printStackTrace();
        }
    }
    
    /**
     * returns a date with string representation of the month
     * @param date input date in the format MM/DD/YYYY HH:MMp/a
     * @return a representation of the date in the format <MonthName> DD, YYYY HH:MM
     */
    public static String getReplyDateString(String date) {
        StringBuffer ret = new StringBuffer();
        //Replace the month number with the month name
        String monthName = getMonthName(
                Integer.parseInt(date.substring(0, date.indexOf('/')))-1
                );
        String day = date.substring(date.indexOf('/')+1, date.lastIndexOf('/'));
        String yearAndTime = date.substring(date.lastIndexOf('/')+1);
        
        ret.append(monthName).append(" ").append(day).append(", ").append(yearAndTime);
        //Replace the slash char between DD and YYYY with ", "
        return ret.toString();
    } 
    
    //------------------------------------------------------------- Private methods

    /**
     * Get the number of the month, given the name.
     */
    private static int getMonthNumber(String name) {
        for(int i=0, l=monthNames.length; i<l; i++) {
            if(monthNames[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the name of the month, given the number.
     */
    private static String getMonthName(int number) {
        if(number>=0 && number<monthNames.length) {
            return monthNames[number];
        }
        else return null;
    }

    private static String getAMPM(Calendar cal) {
        return (cal.get(Calendar.AM_PM)==Calendar.AM)?"a":"p";
    }
    
    /**
     * Returns a string representation of number with at least 2 digits
     */
    private static String printTwoDigits(int number) {
        if (number>9) {
            return String.valueOf(number);
        } else {
            return "0"+number;
        }
    }
}

