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
package com.funambol.common.pim.model.calendar;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.SortedSet;
import java.util.TreeSet;

import com.funambol.common.pim.model.common.PropertyWithTimeZone;
import com.funambol.common.pim.model.utility.TimeUtils;

import com.funambol.util.StringUtil;

/**
 * This class represents the recurrence pattern of a calendar item.
 *
 * @version $Id: RecurrencePattern.java,v 1.7 2008-06-24 12:01:27 mauro Exp $
 */
public class RecurrencePattern extends PropertyWithTimeZone {

    // --------------------------------------------------------------- Constants

    //
    // Values for frequency
    //
    public static final byte TYPE_DAYLY     = 0; // deprecated (wrong spelling)
    public static final byte TYPE_DAILY     = 0; // use this
    public static final byte TYPE_WEEKLY    = 1;
    public static final byte TYPE_MONTHLY   = 2;
    public static final byte TYPE_MONTH_NTH = 3;
    public static final byte TYPE_YEARLY    = 5;
    public static final byte TYPE_YEAR_NTH  = 6;

    //
    // Values for dayOfWeekMask
    //
    public static final byte DAY_OF_WEEK_SUNDAY    =  1;
    public static final byte DAY_OF_WEEK_MONDAY    =  2;
    public static final byte DAY_OF_WEEK_TUESDAY   =  4;
    public static final byte DAY_OF_WEEK_WEDNESDAY =  8;
    public static final byte DAY_OF_WEEK_THURSDAY  = 16;
    public static final byte DAY_OF_WEEK_FRIDAY    = 32;
    public static final byte DAY_OF_WEEK_SATURDAY  = 64;
    public static final short ALL_DAYS_MASK        =
                              DAY_OF_WEEK_SUNDAY   +
                              DAY_OF_WEEK_MONDAY   +
                              DAY_OF_WEEK_TUESDAY  +
                              DAY_OF_WEEK_WEDNESDAY +
                              DAY_OF_WEEK_THURSDAY +
                              DAY_OF_WEEK_FRIDAY   +
                              DAY_OF_WEEK_SATURDAY ;

    public static final short UNSPECIFIED = 0;
    
    // -------------------------------------------------------------- Properties

    private short   frequency; // 0 daily
                               // 1 weekly
                               // 2 monthly
                               // 3 month n-th
                               // 5 yearly
                               // 6 year n-th
    private int     interval;
    private short   monthOfYear;
    private short   dayOfMonth;
    private short   dayOfWeekMask;  // A combination (sum) of:
                                    // 1  Sunday
                                    // 2  Monday
                                    // 4  Tuesday
                                    // 8  Wednesday
                                    // 16 Thursday
                                    // 32 Friday
                                    // 64 Saturday
    private short   instance;
    private String  startDatePattern;
    private boolean noEndDate;
    private String  endDatePattern;
    private int     occurrences = -1;  // -1 means no occurrences specified
    private List<ExceptionToRecurrenceRule> exceptions =
            new ArrayList<ExceptionToRecurrenceRule>();
    
    /**
     * Returns the recurrence frequency as a number.
     * 
     * @return value of property frequency
     */
    public short getTypeId() {
        return frequency;
    }
    
    /**
     * Returns the interval.
     * 
     * @return Value of property interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Returns the month of year.
     * 
     * @return Value of property monthOfYear
     */
    public short getMonthOfYear() {
        return monthOfYear;
    }

    /**
     * Returns the day-of-week mask.
     * 
     * @return Value of property dayOfWeekMask
     */
    public short getDayOfWeekMask() {
        return dayOfWeekMask;
    }
    
    /**
     * Returns the day of the month.
     * 
     * @return value of property dayOfMonth
     */
    public short getDayOfMonth() {
        return dayOfMonth;
    }

    /**
     * Returns the instance number.
     * 
     * @return value of property instance
     */
    public short getInstance() {
        return instance;
    }

    /**
     * Returns the start date pattern.
     * 
     * @return value of property startDatePattern
     */
    public String getStartDatePattern() {
        return startDatePattern;
    }

    /**
     * Returns whether the pattern has no end date set.
     * 
     * @return true if the pattern has no end date set, false otherwise
     */
    public boolean isNoEndDate() {
        return noEndDate;
    }

    /**
     * Setter for property noEndDate.
     * 
     * @param noEndDate new value of property noEndDate
     */
    public void setNoEndDate(boolean noEndDate) {
        this.noEndDate = noEndDate;
    }

    /**
     * Returns the end date pattern.
     * 
     * @return value of property endDatePattern
     */
    public String getEndDatePattern() {
        return endDatePattern;
    }

    /**
     * Set the end date pattern.
     *
     * @param date value of property endDatePattern
     */
    public void setEndDatePattern(String date) {
        endDatePattern = date;
    }

    /**
     * Returns the number of occurrences.
     * 
     * @return value of property occurrences
     */
    public int getOccurrences(){
        return occurrences;
    }

    /**
     * Setter for property occurrences.
     * 
     * @param occurrences new value of property occurrences
     */
    public void setOccurrences(int occurrences) {
        this.occurrences = occurrences;
    }
    
   // ------------------------------------------------------------- Private data

    static private Map<String, String> typeDesc = new HashMap<String, String>(6);
    static {
        typeDesc.put(String.valueOf(TYPE_DAILY)    ,"D" );
        typeDesc.put(String.valueOf(TYPE_WEEKLY)   ,"W" );
        typeDesc.put(String.valueOf(TYPE_MONTHLY)  ,"MD");
        typeDesc.put(String.valueOf(TYPE_MONTH_NTH),"MP");
        typeDesc.put(String.valueOf(TYPE_YEARLY)   ,"YM");
        typeDesc.put(String.valueOf(TYPE_YEAR_NTH) ,"YM"); // same as above
    }
    
    // ------------------------------------------------------------ Constructors

    /**
     * Creates a new instance of RecurrencePattern. Property occurrences will be
     * kept at -1 (unspecified).
     * Client classes can use <i>getXxxRecurrencePattern()</i> instead of this
     * constructor.
     *
     * @param frequency
     * @param interval
     * @param monthOfYear
     * @param dayOfMonth
     * @param dayOfWeekMask
     * @param instance
     * @param startDatePattern
     * @param endDatePattern
     * @param noEndDate 
     */
    public RecurrencePattern(short  type           ,
                             int    interval        ,
                             short  monthOfYear     ,
                             short  dayOfMonth      ,
                             short  dayOfWeekMask   ,
                             short  instance        ,
                             String startDatePattern,
                             String endDatePattern  ,
                             boolean noEndDate      ) {

         this.frequency             = type            ;
         this.interval         = interval        ;
         this.monthOfYear      = monthOfYear     ;
         this.dayOfMonth       = dayOfMonth      ;
         this.dayOfWeekMask    = dayOfWeekMask   ;
         this.instance         = instance        ;
         this.startDatePattern = startDatePattern;
         this.endDatePattern   = endDatePattern  ;
         this.noEndDate        = noEndDate       ;

         // It's strongly recommended to call fix() after using this constructor
    }

    /**
     * Creates a new instance of RecurrencePattern.
     * Client classes can use <i>getXxxRecurrencePattern()</i> instead of this
     * constructor.
     *
     * @param frequency
     * @param interval
     * @param monthOfYear
     * @param dayOfMonth
     * @param dayOfWeekMask
     * @param instance
     * @param startDatePattern
     * @param endDatePattern
     * @param noEndDate
     * @param occurrences 
     */
    public RecurrencePattern(short   type             ,
                             int     interval         ,
                             short   monthOfYear      ,
                             short   dayOfMonth       ,
                             short   dayOfWeekMask    ,
                             short   instance         ,
                             String  startDatePattern ,
                             String  endDatePattern   ,
                             boolean noEndDate        ,
                             int     occurrences      ) {

        this(type,
             interval,
             monthOfYear,
             dayOfMonth,
             dayOfWeekMask,
             instance,
             startDatePattern,
             endDatePattern,
             noEndDate);

        this.occurrences = occurrences;

        // It's strongly recommended to call fix() after using this constructor
    }

    // ------------------------------------------------------- Daily recurrences

    /**
     * Daily recurrence with start and end dates.
     *
     * @param interval how many days between two recurrences
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getDailyRecurrencePattern(int    interval        ,
                                                              String startDatePattern,
                                                              String endDatePattern  )
    throws RecurrencePatternException {
        validateInterval(interval);
        validateDate(startDatePattern);
        validateDate(endDatePattern);

        return new RecurrencePattern(TYPE_DAILY,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     endDatePattern,
                                     false);
    }

    /**
     * Daily recurrence with start and end dates.
     *
     * @param interval how many days between two recurrences
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @param dayOfWeekMask days when the event or task occurs
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getDailyRecurrencePattern(int    interval        ,
                                                              String startDatePattern,
                                                              String endDatePattern  ,
                                                              short  dayOfWeekMask   )
    throws RecurrencePatternException {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateDate(startDatePattern);
        validateDate(endDatePattern);

        return new RecurrencePattern(TYPE_DAILY,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     endDatePattern,
                                     false);
    }


    /**
     * Daily recurrence with start, end dates and noEndDate.
     *
     * @param interval how many days between two recurrences
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @param noEndDate is the pattern without end date?
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getDailyRecurrencePattern( int     interval        ,
                                                               String  startDatePattern,
                                                               String  endDatePattern  ,
                                                               boolean noEndDate)
    throws RecurrencePatternException {
        validateInterval(interval);
        validateDate(startDatePattern);
        if (!noEndDate) {
            validateDate(endDatePattern);
        }

        return new RecurrencePattern(TYPE_DAILY,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     endDatePattern,
                                     noEndDate);
    }

    /**
     * Daily recurrence with start, end dates and noEndDate.
     *
     * @param interval how many days between two recurrences
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @param noEndDate is the recurrence without end date?
     * @param dayOfWeekMask days when the event or task occurs
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getDailyRecurrencePattern( int     interval        ,
                                                               String  startDatePattern,
                                                               String  endDatePattern  ,
                                                               boolean noEndDate       ,
                                                               short   dayOfWeekMask   )
    throws RecurrencePatternException {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateDate(startDatePattern);
        if (!noEndDate) {
            validateDate(endDatePattern);
        }

        return new RecurrencePattern(TYPE_DAILY,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     endDatePattern,
                                     noEndDate);
    }

    /**
     * Daily recurrence
     *
     * @param interval how many days between two recurrences
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @param noEndDate is the recurrence without end date?
     * @param occurrences number of occurrences
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getDailyRecurrencePattern( int     interval        ,
                                                               String  startDatePattern,
                                                               String  endDatePattern  ,
                                                               boolean noEndDate       ,
                                                               int     occurrences     )
    throws RecurrencePatternException {
        validateInterval(interval);
        validateDate(startDatePattern);
        if (!noEndDate && occurrences < 1) {
            validateDate(endDatePattern);
        }
        validateOccurrences(occurrences);

        return new RecurrencePattern(TYPE_DAILY,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     endDatePattern,
                                     noEndDate,
                                     occurrences);
    }

    /**
     * Daily recurrence.
     *
     * @param interval how many days between two recurrence
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @param noEndDate is the recurrence without end date?
     * @param occurrences number of occurrences
     * @param dayOfWeekMask days when the event or task occurs
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getDailyRecurrencePattern( int     interval        ,
                                                               String  startDatePattern,
                                                               String  endDatePattern  ,
                                                               boolean noEndDate       ,
                                                               int     occurrences     ,
                                                               short   dayOfWeekMask   )
    throws RecurrencePatternException {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateDate(startDatePattern);
        if (!noEndDate && occurrences < 1) {
            validateDate(endDatePattern);
        }
        validateOccurrences(occurrences);

        return new RecurrencePattern(TYPE_DAILY,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     endDatePattern,
                                     noEndDate,
                                     occurrences);
    }

    /**
     * Infinite daily recurrence.
     *
     * @param interval how many days between two recurrences
     * @param startDatePattern start date pattern
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getDailyRecurrencePattern( int     interval         ,
                                                               String  startDatePattern )
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDate(startDatePattern);

        return new RecurrencePattern(TYPE_DAILY,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     null,
                                     false);
    }

    /**
     * Inifinite daily recurrence.
     *
     * @param interval how many days between two recurrences
     * @param startDatePattern start date pattern
     * @param dayOfWeekMask days when the event or task occurs
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     *
     */
    public static RecurrencePattern getDailyRecurrencePattern( int     interval         ,
                                                               String  startDatePattern ,
                                                               short   dayOfWeekMask    )
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateDate(startDatePattern);

        return new RecurrencePattern(TYPE_DAILY,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     null,
                                     false);
    }

    /**
     * Infinite daily recurrence.
     *
     * @param interval how many days between two recurrences
     * @param startDatePattern start date pattern
     * @param noEndDate is the recurrence without end date?
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     *
     */
    public static RecurrencePattern getDailyRecurrencePattern( int     interval         ,
                                                               String  startDatePattern ,
                                                               boolean noEndDate)
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDate(startDatePattern);

        return new RecurrencePattern(TYPE_DAILY,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     null,
                                     noEndDate);
    }

    /**
     * Infinite daily recurrence.
     *
     * @param interval how many days between two recurrences
     * @param startDatePattern start date pattern
     * @param noEndDate is the recurrence without end date?
     * @param dayOfWeekMask days when the event or task occurs
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getDailyRecurrencePattern( int     interval         ,
                                                               String  startDatePattern ,
                                                               boolean noEndDate        ,
                                                               short   dayOfWeekMask    )
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateDate(startDatePattern);

        return new RecurrencePattern(TYPE_DAILY,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     null,
                                     noEndDate);
    }

    /**
     * Daily recurrence.
     *
     * @param interval how many days between two recurrences
     * @param startDatePattern start date pattern
     * @param noEndDate is the recurrence without end date?
     * @param occurrences number of occurrences
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     *
     */
    public static RecurrencePattern getDailyRecurrencePattern( int     interval         ,
                                                               String  startDatePattern ,
                                                               boolean noEndDate        ,
                                                               int     occurrences      )
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDate(startDatePattern);
        validateOccurrences(occurrences);

        return new RecurrencePattern(TYPE_DAILY,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     null,
                                     noEndDate,
                                     occurrences);
    }

    /**
     * Daily recurrence.
     *
     * @param interval how many days between two recurrences
     * @param startDatePattern start date pattern
     * @param noEndDate is the recurrence without end date?
     * @param occurrences number of occurrences
     * @param dayOfWeekMask days when the event or task occurs
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     *
     */
    public static RecurrencePattern getDailyRecurrencePattern( int     interval         ,
                                                               String  startDatePattern ,
                                                               boolean noEndDate        ,
                                                               int     occurrences      ,
                                                               short   dayOfWeekMask    )
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateDate(startDatePattern);
        validateOccurrences(occurrences);

        return new RecurrencePattern(TYPE_DAILY,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     null,
                                     noEndDate,
                                     occurrences);
    }


    // ------------------------------------------------------ Weekly recurrences

    /**
     * Weekly recurrence with start and end dates.
     *
     * @param interval how many weeks between two recurrences
     * @param dayOfWeekMask days when the event or task occurs
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getWeeklyRecurrencePattern(int     interval,
                                                               short   dayOfWeekMask,
                                                               String  startDatePattern,
                                                               String  endDatePattern  )
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateDate(startDatePattern);
        validateDate(endDatePattern);

        return new RecurrencePattern(TYPE_WEEKLY,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     endDatePattern,
                                     false);
    }

    /**
     * Weekly recurrence with start, end dates and noEndDate.
     *
     * @param interval how many weeks between two recurrences
     * @param dayOfWeekMask days when the event or task occurs
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @param noEndDate is the recurrence without end date?
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getWeeklyRecurrencePattern(int     interval,
                                                               short   dayOfWeekMask,
                                                               String  startDatePattern,
                                                               String  endDatePattern  ,
                                                               boolean noEndDate)
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateDate(startDatePattern);
        if (!noEndDate) {
            validateDate(endDatePattern);
        }

        return new RecurrencePattern(TYPE_WEEKLY,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     endDatePattern,
                                     noEndDate);
    }


    /**
     * Weekly recurrence.
     *
     * @param interval how many weeks between two recurrences
     * @param dayOfWeekMask days when the event or task occurs
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @param noEndDate is the recurrence without end date?
     * @param occurrences number of occurrences
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getWeeklyRecurrencePattern(int     interval,
                                                               short   dayOfWeekMask,
                                                               String  startDatePattern,
                                                               String  endDatePattern  ,
                                                               boolean noEndDate,
                                                               int     occurrences)
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateDate(startDatePattern);
        if (!noEndDate && occurrences < 1) {
            validateDate(endDatePattern);
        }
        validateOccurrences(occurrences);

        return new RecurrencePattern(TYPE_WEEKLY,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     endDatePattern,
                                     noEndDate,
                                     occurrences);
    }

    /**
     * Infinite weekly recurrence
     *
     * @param interval how many days between two recurrences
     * @param dayOfWeekMask days when the event or task occurs
     * @param startDatePattern start date pattern
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getWeeklyRecurrencePattern(int     interval,
                                                               short   dayOfWeekMask,
                                                               String  startDatePattern)
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateDate(startDatePattern);

        return new RecurrencePattern(TYPE_WEEKLY,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     null,
                                     false);
    }

    /**
     * Infinite weekly recurrence
     *
     * @param interval how many days between two recurrences
     * @param dayOfWeekMask days when the event or task occurs
     * @param startDatePattern start date pattern
     * @param noEndDate is the recurrence without end date?
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getWeeklyRecurrencePattern(int     interval,
                                                               short   dayOfWeekMask,
                                                               String  startDatePattern,
                                                               boolean noEndDate)
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateDate(startDatePattern);

        return new RecurrencePattern(TYPE_WEEKLY,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     null,
                                     noEndDate);
    }

    // ----------------------------------------------------- Monthly recurrences

    /**
     * Monthly recurrence with start and end dates.
     *
     * @param interval how many months between two recurrences
     * @param dayOfMonth day of month (1-31) when the event or task occurs
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getMonthlyRecurrencePattern(int     interval,
                                                                short   dayOfMonth,
                                                                String  startDatePattern,
                                                                String  endDatePattern  )
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfMonth(dayOfMonth);
        validateDate(startDatePattern);
        validateDate(endDatePattern);

        return new RecurrencePattern(TYPE_MONTHLY,
                                     interval,
                                     UNSPECIFIED,
                                     dayOfMonth,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     endDatePattern,
                                     false);
    }

    /**
     * Monthly recurrence.
     *
     * @param interval how many months between two recurrences
     * @param dayOfMonth day of month (1-31) when the event or task occurs
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @param noEndDate is the recurrence without end date?
     * @param occurrences number of occurrences
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getMonthlyRecurrencePattern(int     interval,
                                                                short   dayOfMonth,
                                                                String  startDatePattern,
                                                                String  endDatePattern  ,
                                                                boolean noEndDate,
                                                                int     occurrences)
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfMonth(dayOfMonth);
        validateDate(startDatePattern);
        if (!noEndDate && occurrences < 1) {
            validateDate(endDatePattern);
        }
        validateOccurrences(occurrences);

        return new RecurrencePattern(TYPE_MONTHLY,
                                     interval,
                                     UNSPECIFIED,
                                     dayOfMonth,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     endDatePattern,
                                     noEndDate,
                                     occurrences);
    }

    /**
     * Monthly recurrence with start, end dates and noEndDate.
     *
     * @param interval how many months between two recurrences
     * @param dayOfMonth day of month (1-31) when the event or task occurs
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @param noEndDate is the recurrence without end date?
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getMonthlyRecurrencePattern(int     interval,
                                                                short   dayOfMonth,
                                                                String  startDatePattern,
                                                                String  endDatePattern  ,
                                                                boolean noEndDate)
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfMonth(dayOfMonth);
        validateDate(startDatePattern);
        if (!noEndDate) {
            validateDate(endDatePattern);
        }

        return new RecurrencePattern(TYPE_MONTHLY,
                                     interval,
                                     UNSPECIFIED,
                                     dayOfMonth,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     endDatePattern,
                                     noEndDate);
    }

    /**
     * Infinite monthly recurrence.
     *
     * @param interval how many days between two recurrences
     * @param dayOfMonth day of month (1-31) when the event or task occurs
     * @param startDatePattern start date pattern
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getMonthlyRecurrencePattern(int     interval,
                                                                short   dayOfMonth,
                                                                String  startDatePattern )
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfMonth(dayOfMonth);
        validateDate(startDatePattern);

        return new RecurrencePattern(TYPE_MONTHLY,
                                     interval,
                                     UNSPECIFIED,
                                     dayOfMonth,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     null,
                                     false);
    }

    /**
     * Monthly recurrence.
     *
     * @param interval how many days between two recurrences
     * @param dayOfMonth day of month (1-31) when the event or task occurs
     * @param startDatePattern start date pattern
     * @param noEndDate is the recurrence without end date?
     * @param occurrences number of occurrences
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getMonthlyRecurrencePattern(int     interval,
                                                                short   dayOfMonth,
                                                                String  startDatePattern,
                                                                boolean noEndDate,
                                                                int     occurrences )
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfMonth(dayOfMonth);
        validateDate(startDatePattern);
        validateOccurrences(occurrences);

        return new RecurrencePattern(TYPE_MONTHLY,
                                     interval,
                                     UNSPECIFIED,
                                     dayOfMonth,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     null,
                                     noEndDate,
                                     occurrences);
    }

    /**
     * Infinite monthly recurrence.
     *
     * @param interval how many days between two recurrences
     * @param dayOfMonth day of month (1-31) when the event or task occurs
     * @param startDatePattern start date pattern
     * @param noEndDate is the recurrence without end date?
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getMonthlyRecurrencePattern(int     interval,
                                                                short   dayOfMonth,
                                                                String  startDatePattern,
                                                                boolean noEndDate)
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfMonth(dayOfMonth);
        validateDate(startDatePattern);

        return new RecurrencePattern(TYPE_MONTHLY,
                                     interval,
                                     UNSPECIFIED,
                                     dayOfMonth,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     null,
                                     noEndDate);
    }


    /**
     * Monthly day-of-week-based recurrence with start and end dates.
     *
     * @param interval how many months between two recurrences
     * @param dayOfWeekMask see below
     * @param instance the <i>instance</i> of <i>dayOfWeekMask</i> of every 
     *                 <i>interval</i> months
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wront parameters
     */
    public static RecurrencePattern getMonthNthRecurrencePattern(int     interval  ,
                                                                 short   dayOfWeekMask,
                                                                 short   instance,
                                                                 String  startDatePattern,
                                                                 String  endDatePattern  )
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateInstance(instance);
        validateDate(startDatePattern);
        validateDate(endDatePattern);

        return new RecurrencePattern(TYPE_MONTH_NTH,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     instance,
                                     startDatePattern,
                                     endDatePattern,
                                     false);
    }


    /**
     * Monthly day-of-week-based recurrence with start, end dates and noEndDate.
     *
     * @param interval how many months between two recurrences
     * @param dayOfWeekMask see below
     * @param instance the <i>instance</i> of <i>dayOfWeekMask</i> of every 
     *                 <i>interval</i> months
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @param noEndDate is the recurrence without end date?
     * @param occurrences number of occurrences
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getMonthNthRecurrencePattern(int     interval  ,
                                                                 short   dayOfWeekMask,
                                                                 short   instance,
                                                                 String  startDatePattern,
                                                                 String  endDatePattern  ,
                                                                 boolean noEndDate,
                                                                 int     occurrences)
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateInstance(instance);
        validateDate(startDatePattern);
        if (!noEndDate && occurrences < 1) {
            validateDate(endDatePattern);
        }
        validateOccurrences(occurrences);

        return new RecurrencePattern(TYPE_MONTH_NTH,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     instance,
                                     startDatePattern,
                                     endDatePattern,
                                     noEndDate,
                                     occurrences);
    }

    /**
     * Monthly day-of-week-based recurrence with start and end dates.
     *
     * @param interval how many months between two recurrences
     * @param dayOfWeekMask see below
     * @param instance the <i>instance</i> of <i>dayOfWeekMask</i> of every <i>interval</i> months
     * @param startDatePattern start date pattern
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getMonthNthRecurrencePattern(int     interval  ,
                                                                 short   dayOfWeekMask,
                                                                 short   instance,
                                                                 String  startDatePattern )
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateInstance(instance);
        validateDate(startDatePattern);

        return new RecurrencePattern(TYPE_MONTH_NTH,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     instance,
                                     startDatePattern,
                                     null,
                                     false);
    }

    /**
     * Monthly day-of-week-based recurrence with start and end dates.
     *
     * @param interval how many months between two recurrences
     * @param dayOfWeekMask see below
     * @param instance the <i>instance</i> of <i>dayOfWeekMask</i> of every <i>interval</i> months
     * @param startDatePattern start date pattern
     * @param noEndDate is the recurrence without end date?
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getMonthNthRecurrencePattern(int     interval  ,
                                                                 short   dayOfWeekMask,
                                                                 short   instance,
                                                                 String  startDatePattern,
                                                                 boolean noEndDate )
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateInstance(instance);
        validateDate(startDatePattern);

        return new RecurrencePattern(TYPE_MONTH_NTH,
                                     interval,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     instance,
                                     startDatePattern,
                                     null,
                                     noEndDate);
    }

    // ------------------------------------------------------ Yearly recurrences

    /**
     * Yearly recurrence with start, end dates and noEndDate.
     *
     * @param interval how many years between two recurrences
     * @param dayOfMonth day of month (1-31) when the event or task occurs
     * @param monthOfYear month (1-12) of the year when the event or task occurs
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @param noEndDate is the recurrence without end date?
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getYearlyRecurrencePattern(int     interval,
                                                               short   dayOfMonth,
                                                               short   monthOfYear,
                                                               String  startDatePattern,
                                                               String  endDatePattern  ,
                                                               boolean noEndDate)
    throws RecurrencePatternException {
        validateInterval(interval);
        validateDayOfMonth(dayOfMonth);
        validateMonthOfYear(monthOfYear);
        validateDate(startDatePattern);
        if (!noEndDate) {
            validateDate(endDatePattern);
        }

        return new RecurrencePattern(TYPE_YEARLY,
                                     interval,
                                     monthOfYear,
                                     dayOfMonth,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     endDatePattern,
                                     noEndDate);
    }

    /**
     * Yearly recurrence.
     *
     * @param interval how many years between two recurrences
     * @param dayOfMonth day of month (1-31) when the event or task occurs
     * @param monthOfYear month (1-12) of the year when the event or task occurs
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @param noEndDate is the recurrence without end date?
     * @param occurrences number of occurrences
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getYearlyRecurrencePattern(int     interval,
                                                               short   dayOfMonth,
                                                               short   monthOfYear,
                                                               String  startDatePattern,
                                                               String  endDatePattern  ,
                                                               boolean noEndDate,
                                                               int     occurrences)
    throws RecurrencePatternException {
        validateInterval(interval);
        validateDayOfMonth(dayOfMonth);
        validateMonthOfYear(monthOfYear);
        validateDate(startDatePattern);
        if (!noEndDate && occurrences < 1) {
            validateDate(endDatePattern);
        }
        validateOccurrences(occurrences);

        return new RecurrencePattern(TYPE_YEARLY,
                                     interval,
                                     monthOfYear,
                                     dayOfMonth,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     endDatePattern,
                                     noEndDate,
                                     occurrences);
    }


    /**
     * Yearly recurrence with start, end dates and noEndDate
     *
     * @param interval how many years between two recurrences
     * @param dayOfMonth day of month (1-31) when the event or task occurs
     * @param monthOfYear month (1-12) of the year when the event or task occurs
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getYearlyRecurrencePattern(int    interval,
                                                               short  dayOfMonth,
                                                               short  monthOfYear,
                                                               String startDatePattern,
                                                               String endDatePattern)
    throws RecurrencePatternException {
        validateInterval(interval);
        validateDayOfMonth(dayOfMonth);
        validateMonthOfYear(monthOfYear);
        validateDate(startDatePattern);
        validateDate(endDatePattern);

        return new RecurrencePattern(TYPE_YEARLY,
                                     interval,
                                     monthOfYear,
                                     dayOfMonth,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     endDatePattern,
                                     false);
    }

    /**
     * Infinite yearly recurrence.
     *
     * @param interval how many years between two recurrences
     * @param dayOfMonth day of month (1-31) when the event or task occurs
     * @param monthOfYear month (1-12) of the year when the event or task occurs
     * @param startDatePattern start date pattern
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getYearlyRecurrencePattern(int    interval,
                                                               short  dayOfMonth,
                                                               short  monthOfYear,
                                                               String startDatePattern )
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfMonth(dayOfMonth);
        validateMonthOfYear(monthOfYear);
        validateDate(startDatePattern);

        return new RecurrencePattern(TYPE_YEARLY,
                                     interval,
                                     monthOfYear,
                                     dayOfMonth,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     null,
                                     false);
    }


    /**
     * Infinite yearly recurrence.
     *
     * @param interval how many years between two recurrences
     * @param dayOfMonth day of month (1-31) when the event or task occurs
     * @param monthOfYear month (1-12) of the year when the event or task occurs
     * @param startDatePattern start date pattern
     * @param noEndDate is the recurrence without end date?
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getYearlyRecurrencePattern(int     interval,
                                                               short   dayOfMonth,
                                                               short   monthOfYear,
                                                               String  startDatePattern,
                                                               boolean noEndDate)
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfMonth(dayOfMonth);
        validateMonthOfYear(monthOfYear);
        validateDate(startDatePattern);

        return new RecurrencePattern(TYPE_YEARLY,
                                     interval,
                                     monthOfYear,
                                     dayOfMonth,
                                     UNSPECIFIED,
                                     UNSPECIFIED,
                                     startDatePattern,
                                     null,
                                     noEndDate);
    }


    /**
     * Yearly day-of-week-based recurrence with start, end dates and noEndDate.
     *
     * @param interval how many years between two recurrences
     * @param dayOfWeekMask days when the event or task occurs
     * @param monthOfYear month (1-12) of the year when the event or task occurs
     * @param instance every <i>instance</i> of <i>dayOfWeekMask</i> of 
     *                 <i>monthOfYear</i>
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @param noEndDate is the recurrence without end date?
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getYearNthRecurrencePattern(int     interval,
                                                                short   dayOfWeekMask,
                                                                short   monthOfYear,
                                                                short   instance,
                                                                String  startDatePattern,
                                                                String  endDatePattern  ,
                                                                boolean noEndDate)
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateMonthOfYear(monthOfYear);
        validateInstance(instance);
        validateDate(startDatePattern);
        if (!noEndDate) {
            validateDate(endDatePattern);
        }

        return new RecurrencePattern(TYPE_YEAR_NTH,
                                     interval,
                                     monthOfYear,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     instance,
                                     startDatePattern,
                                     endDatePattern,
                                     noEndDate);
    }

    /**
     * Yearly day-of-week-based recurrence.
     *
     * @param interval how many years between two recurrences
     * @param dayOfWeekMask days when the event or task occurs
     * @param monthOfYear month (1-12) of the year when the event or task occurs
     * @param instance every <i>instance</i> of <i>dayOfWeekMask</i> of 
     *                 <i>monthOfYear</i>
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @param noEndDate is the recurrence without end date?
     * @param occurrences number of occurrences
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getYearNthRecurrencePattern(int     interval,
                                                                short   dayOfWeekMask,
                                                                short   monthOfYear,
                                                                short   instance,
                                                                String  startDatePattern,
                                                                String  endDatePattern  ,
                                                                boolean noEndDate,
                                                                int     occurrences)
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateMonthOfYear(monthOfYear);
        validateInstance(instance);
        validateDate(startDatePattern);
        if (!noEndDate && occurrences < 1) {
            validateDate(endDatePattern);
        }
        validateOccurrences(occurrences);

        return new RecurrencePattern(TYPE_YEAR_NTH,
                                     interval,
                                     monthOfYear,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     instance,
                                     startDatePattern,
                                     endDatePattern,
                                     noEndDate,
                                     occurrences);
    }

    /**
     * Yearly day-of-week-based recurrence with start and end dates.
     *
     * @param interval how many years between two recurrences
     * @param dayOfWeekMask days when the event or task occurs
     * @param monthOfYear month (1-12) of the year when the event or task occurs
     * @param instance every <i>instance</i> of <i>dayOfWeekMask</i> of 
     *                 <i>monthOfYear</i>
     * @param startDatePattern start date pattern
     * @param endDatePattern end date pattern
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getYearNthRecurrencePattern(int    interval,
                                                                short  dayOfWeekMask,
                                                                short  monthOfYear,
                                                                short  instance,
                                                                String startDatePattern,
                                                                String endDatePattern)
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateMonthOfYear(monthOfYear);
        validateInstance(instance);
        validateDate(startDatePattern);
        validateDate(endDatePattern);

        return new RecurrencePattern(TYPE_YEAR_NTH,
                                     interval,
                                     monthOfYear,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     instance,
                                     startDatePattern,
                                     endDatePattern,
                                     false);
    }

    /**
     * Yearly day-of-week-based recurrence with start and end dates.
     *
     * @param interval how many years between two recurrences
     * @param dayOfWeekMask days when the event or task occurs
     * @param monthOfYear month (1-12) of the year when the event or task occurs
     * @param instance every <i>instance</i> of <i>dayOfWeekMask</i> of 
     *                 <i>monthOfYear</i>
     * @param startDatePattern start date pattern
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getYearNthRecurrencePattern(int    interval,
                                                                short  dayOfWeekMask,
                                                                short  monthOfYear,
                                                                short  instance,
                                                                String startDatePattern )
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateMonthOfYear(monthOfYear);
        validateInstance(instance);
        validateDate(startDatePattern);

        return new RecurrencePattern(TYPE_YEAR_NTH,
                                     interval,
                                     monthOfYear,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     instance,
                                     startDatePattern,
                                     null,
                                     false);
    }

    /**
     * Yearly day-of-week-based recurrence with start and end dates.
     *
     * @param interval how many years between two recurrences
     * @param dayOfWeekMask days when the event or task occurs
     * @param monthOfYear month (1-12) of the year when the event or task occurs
     * @param instance every <i>instance</i> of <i>dayOfWeekMask</i> of 
     *                 <i>monthOfYear</i>
     * @param startDatePattern start date pattern
     * @param noEndDate is the recurrence without end date?
     * @return the newly created RecurrencePattern object
     * @throws RecurrencePatternException in case of wrong parameters
     */
    public static RecurrencePattern getYearNthRecurrencePattern(int     interval,
                                                                short   dayOfWeekMask,
                                                                short   monthOfYear,
                                                                short   instance,
                                                                String  startDatePattern,
                                                                boolean noEndDate)
    throws RecurrencePatternException  {
        validateInterval(interval);
        validateDayOfWeekMask(dayOfWeekMask);
        validateMonthOfYear(monthOfYear);
        validateInstance(instance);
        validateDate(startDatePattern);

        return new RecurrencePattern(TYPE_YEAR_NTH,
                                     interval,
                                     monthOfYear,
                                     UNSPECIFIED,
                                     dayOfWeekMask,
                                     instance,
                                     startDatePattern,
                                     null,
                                     noEndDate);
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Gives a vCalendar short form for the recurrence frequency.
     * 
     *@return "D"  for daily recurrence
     *        "W"  for weekly recurrence
     *        "MP" for monthly recurrence (monthly by position)
     *        "MD" for monthly day-of-week-based recurrence (monthly by day)
     *        "YM" for yearly recurrence (yearly by month)
     */
    public String getTypeDesc() {
        return typeDesc.get(String.valueOf(frequency));
    }

    /**
     * Return a list of abbreviations of days of week in which the task or event
     * recurs.
     *
     * @return the list of days of the week ("SU", "MO", etc.)
     */
    public List<String> getDayOfWeek() {
        ArrayList<String> days = new ArrayList<String>();
        if ((dayOfWeekMask & DAY_OF_WEEK_SUNDAY) != 0) {
            days.add("SU");
        }
        if ((dayOfWeekMask & DAY_OF_WEEK_MONDAY) != 0) {
            days.add("MO");
        }
        if ((dayOfWeekMask & DAY_OF_WEEK_TUESDAY) != 0) {
            days.add("TU");
        }
        if ((dayOfWeekMask & DAY_OF_WEEK_WEDNESDAY) != 0) {
            days.add("WE");
        }
        if ((dayOfWeekMask & DAY_OF_WEEK_THURSDAY) != 0) {
            days.add("TH");
        }
        if ((dayOfWeekMask & DAY_OF_WEEK_FRIDAY) != 0) {
            days.add("FR");
        }
        if ((dayOfWeekMask & DAY_OF_WEEK_SATURDAY) != 0) {
            days.add("SA");
        }
        return days;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("<RecurrencePattern [");
        sb.append("DayOfMonth: ").append(dayOfMonth);
        sb.append(", DayOfWeekMask: ").append(dayOfWeekMask);
        sb.append(", EndDatePattern: ").append(endDatePattern);
        sb.append(", Instance: ").append(instance);
        sb.append(", Interval: ").append(interval);
        sb.append(", MonthOfYear: ").append(monthOfYear);
        sb.append(", NoEndDate: ").append(noEndDate);
        sb.append(", Occurrences: ").append(occurrences);
        sb.append(", StartDatePattern: ").append(startDatePattern);
        sb.append(", Type: ").append(frequency);
        sb.append("]>");
        return sb.toString();
    }

    /**
     * Gets the exception list, duplicate-free and ordered.
     * 
     * @return the exceptions as a List collection
     */
    public List<ExceptionToRecurrenceRule> getExceptions() {
        SortedSet<ExceptionToRecurrenceRule> set =  
                new TreeSet<ExceptionToRecurrenceRule>(exceptions);
        exceptions = new ArrayList<ExceptionToRecurrenceRule>(set);
        return exceptions;
    }
    
    /**
     * Sets the exception list.
     * Being a list, there could be duplicates but they will be removed by the 
     * getter in any case.
     * 
     * @param exceptions the exceptions as a List collection
     */
     public void setExceptions(List<ExceptionToRecurrenceRule> exceptions) {
        this.exceptions = exceptions;
    }
    
    /**
     * Sets the exception set. 
     * It is still saved internally as an ArrayList though.
     * 
     * @param exceptions the exceptions as a SortedSet collection
     */
    public void setExceptions(SortedSet<ExceptionToRecurrenceRule> exceptions) {
        this.exceptions = new ArrayList<ExceptionToRecurrenceRule>(exceptions);
    }

    // --------------------------------------------------------- Private methods

    /**
     * Gets the GregorianCalendar object corresponding to the given date.
     *
     * @param date in "YYYYMMDD..." format
     * @return an instance of the GregorianCalendar class
     */
    private GregorianCalendar getGregorianCalendar(String date) {
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(4, 6));
        int day = Integer.parseInt(date.substring(6, 8));
        return new GregorianCalendar(year, month - 1, day);
    }

    /**
     * Checks whether a given day is included in the mask.
     *
     * @param day the day in the RecurrencePattern.DAY_OF_WEEK_*DAY format (as a
     *            byte)
     * @param mask the day-of-week mask
     * @return true only if day is in mask
     */
    private boolean isDayInMask(byte day, int mask) {
        return ((mask % (2 * day)) >=  day);
    }

    /**
     * Gets the week day of a given date.
     *
     * @param date in "YYYYMMDD..." format
     * @return the day in the RecurrencePattern.DAY_OF_WEEK_*DAY format (as a
     *         byte)
     */
    private byte getWeekDay(String date) {

        switch(getGregorianCalendar(date)
                .get(java.util.Calendar.DAY_OF_WEEK)) { // Ugly but safe

            case java.util.Calendar.SUNDAY:
                return DAY_OF_WEEK_SUNDAY;

            case java.util.Calendar.MONDAY:
                return DAY_OF_WEEK_MONDAY;

            case java.util.Calendar.TUESDAY:
                return DAY_OF_WEEK_TUESDAY;

            case java.util.Calendar.WEDNESDAY:
                return DAY_OF_WEEK_WEDNESDAY;

            case java.util.Calendar.THURSDAY:
                return DAY_OF_WEEK_THURSDAY;

            case java.util.Calendar.FRIDAY:
                return DAY_OF_WEEK_FRIDAY;

            case java.util.Calendar.SATURDAY:
                return DAY_OF_WEEK_SATURDAY;
        }
        return 0; // This shouldn't happen!
    }

    /**
     * Computes which instance of its week day a given date is in its month (eg,
     * the 3th Friday of the month).
     *
     * @param date in "YYYYMMDD..." format
     * @return a positive short representing the (forward) ordinal position of
     *         that week day in the month
     */
    private short getWeekDayInstance(String date) {
        short day = Short.parseShort(date.substring(6, 8));
        return (short) ((day + 6) / 7); // Yes, it works
    }

    /**
     * Computes which instance of its week day a given date is in its month,
     * counting in the reverse direction (eg, the 2nd to the last Friday of the
     * month).
     *
     * @param date in "YYYYMMDD..." format
     * @return a negative short representing the (backward) ordinal position of
     *         that week day in the month
     */
    private short getBackwardWeekDayInstance(String date) {
        GregorianCalendar greg = getGregorianCalendar(date);
        return (short) (-1 + greg.get(java.util.Calendar.DAY_OF_WEEK_IN_MONTH) -
                greg.getActualMaximum(java.util.Calendar.DAY_OF_WEEK_IN_MONTH));
    }

    /**
     * Gets the month day of a given date.
     *
     * @param date in "YYYYMMDD..." format
     * @return a positive short representing the (forward) ordinal position of
     *         the day in the month
     */
    private short getMonthDay(String date) {
        return Short.parseShort(date.substring(6, 8));
    }

    /**
     * Gets the backward-counted position of a given date in the month.
     *
     * @param date in "YYYYMMDD..." format
     * @return a negative short representing the backward ordinal position of
     *         the day in the month
     */
    private short getBackwardMonthDay(String date) {
        GregorianCalendar greg = getGregorianCalendar(date);
        return (short) (-1 + greg.get(java.util.Calendar.DAY_OF_MONTH) -
                greg.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
    }

    /**
     * Gets the month of a given date.
     *
     * @param date in "YYYYMMDD..." format
     * @return a short representing the month (1 = January, 2 = February etc.)
     */
    private short getMonth(String date) {
        return Short.parseShort(date.substring(4, 6));
    }

    /**
     * Fixes missing data on the basis of the startDatePattern field, according
     * to the inference rules prescribed by the specifications (missing data
     * must be inferred by the start date).
     *
     * @return true if a fix was needed
     * @throws RecurrencePatternException if no automatic fix was possible
     */
    public boolean fix() throws RecurrencePatternException {

        boolean fixed = false;
        String localStartDate;

        try {
            if (timeZone == null) {
                localStartDate = startDatePattern;
            } else {
                localStartDate =
                    TimeUtils.convertUTCDateToLocal(startDatePattern              ,
                                                    TimeZone.getTimeZone(timeZone));
            }
        } catch (Exception e) {
            throw new RecurrencePatternException(
                    "Conversion of start date pattern to local time failed");
        }
        String yyyyMMddStartDatePattern = localStartDate.replaceAll("-", "");

        try {

            switch(frequency) {

                case TYPE_DAILY:
                    // Does nothing
                    break;

                case TYPE_YEAR_NTH:
                    short month = getMonth(yyyyMMddStartDatePattern);
                    if (month != monthOfYear) {
                        month = monthOfYear;
                        fixed = true;
                    }
                    // Falls through

                case TYPE_MONTH_NTH:
                    if (instance != getBackwardWeekDayInstance(yyyyMMddStartDatePattern)) {
                        short startWeekDayInstance =
                                getWeekDayInstance(yyyyMMddStartDatePattern);
                        if (startWeekDayInstance != instance) {
                            instance = startWeekDayInstance;
                            fixed = true;
                        }
                    }
                    // Falls through

                case TYPE_WEEKLY:
                    byte startWeekDay = getWeekDay(yyyyMMddStartDatePattern);
                    if (!isDayInMask(startWeekDay, dayOfWeekMask)) {
                        dayOfWeekMask += startWeekDay;
                        fixed = true;
                    }
                    break;

                case TYPE_YEARLY:
                    month = getMonth(yyyyMMddStartDatePattern);
                    if (month != monthOfYear) {
                        monthOfYear = month;
                        fixed = true;
                    }
                    // Falls through

                case TYPE_MONTHLY:
                    if (dayOfMonth != getBackwardMonthDay(yyyyMMddStartDatePattern)) {
                        short startMonthDay = getMonthDay(yyyyMMddStartDatePattern);
                        if (startMonthDay != dayOfMonth) {
                            dayOfMonth = startMonthDay;
                            fixed = true;
                        }
                    }
                    break;

                default:
                    throw new RecurrencePatternException("Type " + frequency +
                            "not implemented");
            }

        } catch (Exception e) {
            throw new RecurrencePatternException("RecurrencePattern: Format " +
                    "error found. " + e);
        }

        return fixed;
    }

    // ------------------------------------------------------ Validation methods
    
    /**
     * Validates a day-of-week mask. To be valid it must be >= 0 and less 
     * than ALL_DAYS_MASK.
     *
     * @param dayOfWeekMask the mask
     * @throws RecurrencePatternException if the validation fails
     */
    private static void validateDayOfWeekMask(short dayOfWeekMask)
    throws RecurrencePatternException {
        if (dayOfWeekMask < 0 || dayOfWeekMask > ALL_DAYS_MASK) {
            throw new RecurrencePatternException("dayOfWeekMask outside range [0, ALL_DAYS_MASK]: " + dayOfWeekMask );
        }
    }

    /**
     * Validates the number of occurrences. It must be >= 0.
     *
     * @param occurrences the number of occurrences
     * @throws RecurrencePatternException if the validation fails
     */
    private static void validateOccurrences(int occurrences)
    throws RecurrencePatternException {
        if (occurrences < -1) {
            throw new RecurrencePatternException( "occurrences is not greater or equals than 0: " + occurrences);
        }
    }

    /**
     * Validates a month of the year. It must be in the interval [1, 12].
     *
     * @param monthOfYear the month
     * @throws RecurrencePatternException if the validation fails
     */
    private static void validateMonthOfYear(short monthOfYear)
    throws RecurrencePatternException {
        if (monthOfYear < 1 && monthOfYear > 12) {
            throw new RecurrencePatternException("monthOfYear outside range ([1, 12]: " + monthOfYear);
        }
    }

    /**
     * Validates an instance number. 
     * There are no requirements for instance numbers, therefore it is always
     * validated, but this method is here in any case for consistence's sake.
     * 
     * @param instance the instance number
     * @throws RecurrencePatternException if the validation fails
     */
    private static void validateInstance(int instance)
    throws RecurrencePatternException {
        // Do nothing
    }

    /**
     * Validates the interval number.
     * The interval can be equal to 0 when:
     * <ul>
     *  <li>the recurrence frequency is Daily</li>
     *  <li>the recurrence value is Every Week Day</li>
     * </ul>
     *
     * @param interval the interval of recurrence
     *
     * @throws RecurrencePatternException in case of errors
     */
    private static void validateInterval(int interval)
    throws RecurrencePatternException {
        if (interval < 0) {
            throw new RecurrencePatternException(
                "interval is less than 0: " + interval);
        }
    }

    /**
     * Validates a date pattern. It must not be empty.
     *
     * @param datePattern a start date pattern or an end date pattern
     * @throws RecurrencePatternException if the validation fails
     */
    private static void validateDate(String datePattern)
    throws RecurrencePatternException {
        if (StringUtil.isNullOrEmpty(datePattern)) {
            throw new RecurrencePatternException("The calendar item is recursive but a datePattern is empty");
        }
    }

    /**
     * Validates the day of the month. It must be in the interval [1, 31].
     *
     * @param dayOfMonth the day of the month
     * @throws RecurrencePatternException if the validation fails
     */
    private static void validateDayOfMonth(short dayOfMonth)
    throws RecurrencePatternException {
        if (dayOfMonth < 1 && dayOfMonth > 31) {
            throw new RecurrencePatternException("dayOfMonth outside range [1, 31]: " + dayOfMonth);
        }
    }
}
