/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2006 - 2007 Funambol, Inc.
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
 * the Free Software Foundation, Inc., 51 Franklin Street
 , Fifth Floor, Boston,
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
package com.funambol.common.pim.model.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.funambol.common.pim.model.calendar.CalendarContent;
import com.funambol.common.pim.model.calendar.RecurrencePatternException;
import com.funambol.common.pim.model.calendar.ExceptionToRecurrenceRule;
import com.funambol.common.pim.model.calendar.Event;
import com.funambol.common.pim.model.calendar.RecurrencePattern;
import com.funambol.common.pim.model.calendar.Reminder;
import com.funambol.common.pim.model.calendar.Task;
import com.funambol.common.pim.model.common.Property;
import com.funambol.common.pim.model.common.PropertyWithTimeZone;
import com.funambol.common.pim.model.model.Parameter;
import com.funambol.common.pim.model.model.VAlarm;
import com.funambol.common.pim.model.model.VCalendarContent;
import com.funambol.common.pim.model.model.VEvent;
import com.funambol.common.pim.model.model.VTodo;
import com.funambol.common.pim.model.utility.TimeUtils;

import java.util.GregorianCalendar;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This object is a converter from CalendarContent (Event or Task) to VCalendar
 * and from VCalendar to CalendarContent.
 *
 * @see Converter
 * @version $Id: VCalendarContentConverter.java,v 1.23 2008-08-29 12:21:02 mauro Exp $
 */
public class VCalendarContentConverter extends VCalendarConverter  {

    private TimeZone dtStartTimeZone;
    private TimeZone dtEndTimeZone;
    private TimeZone reminderTimeZone;

    //---------------------------------------------------------------- Constants

    private static final short SENSITIVITY_NORMAL       = 0             ;
    private static final short SENSITIVITY_PERSONAL     = 1             ;
    private static final short SENSITIVITY_PRIVATE      = 2             ;
    private static final short SENSITIVITY_CONFIDENTIAL = 3             ;
    private static final String CLASS_PUBLIC            = "PUBLIC"      ;
    private static final String CLASS_PRIVATE           = "PRIVATE"     ;
    private static final String CLASS_CONFIDENTIAL      = "CONFIDENTIAL";
    private static final String CLASS_CUSTOM            = "X-PERSONAL"  ;

    private static final short BUSYSTATUS_OLFREE        = 0             ;
    private static final short BUSYSTATUS_OLTENTATIVE   = 1             ;
    private static final short BUSYSTATUS_OLBUSY        = 2             ;
    private static final short BUSYSTATUS_OLOOF         = 3             ;
    private static final String BUSYSTATUS_FREE         = "FREE"        ;
    private static final String BUSYSTATUS_TENTATIVE    = "TENTATIVE"   ;
    private static final String BUSYSTATUS_BUSY         = "BUSY"        ;
    private static final String BUSYSTATUS_OOF          = "OOF"         ;


    private static final String[] WEEK = {
         "SU", // NB: Sunday needs be the first day of the week for the mask
         "MO", //     composition algorithm to work
         "TU",
         "WE",
         "TH",
         "FR",
         "SA"
     };

     private static final String DAILY = "DAILY";
     private static final String WEEKLY = "WEEKLY";
     private static final String MONTHLY = "MONTHLY";
     private static final String YEARLY = "YEARLY";
     private static final String BYMONTH = "BYMONTH";
     private static final String BYDAY = "BYDAY";
     private static final String BYMONTHDAY = "BYMONTHDAY";
     private static final String INTERVAL = "INTERVAL";
     private static final String COUNT = "COUNT";
     private static final String UNTIL = "UNTIL";
     private static final String BYSETPOS = "BYSETPOS";
     private static final String FREQ = "FREQ";

     private final static Parameter DATETIME_PARAMETER =
             new Parameter("VALUE", "DATE-TIME");
     private final static String ZERO = String.valueOf(RecurrencePattern.UNSPECIFIED);
     private final static long ONE_YEAR = 31622400000L; // 366 days
     private final static long ONE_DAY = 86400000L; // 24 hours
     private final long DEFAULT_FROM = TimeZoneHelper.getReferenceTime() - ONE_YEAR;
     // 1 year ago
     private final long DEFAULT_TO = TimeZoneHelper.getReferenceTime() + (ONE_YEAR * 2);
     // 2 years in the future
     private final long DEFAULT_TO_UNLIMITED = DEFAULT_TO + (ONE_YEAR * 2);
     // 4 years in the future

    //-------------------------------------------------------------- Constructor

    /**
     * This constructor is deprecated because to handle the date is need to know
     * timezone but also if the dates must be converted in local time.
     *
     * @param timezone
     * @param charset
     * @deprecated forceClientLocalTime argument should also be specified
     */
    @Deprecated
    public VCalendarContentConverter(TimeZone timezone, String charset) {
        super(timezone, charset);
    }

    /**
     *
     * @param timezone the timezone to use in the conversion
     * @param charset the charset
     * @param forceClientLocalTime true if the date must be converted in the
     *                             client local time, false otherwise.
     */
    public VCalendarContentConverter(TimeZone timezone, String charset, boolean forceClientLocalTime) {
        super(timezone, charset, forceClientLocalTime);
    }

    //----------------------------------------------------------- Public Methods

    public VCalendarContent cc2vcc(CalendarContent cc)
    throws ConverterException {
        return cc2vcc(cc, false); // default: text/calendar (2.0)
    }

    /**
     * Performs the CalendarContent-to-VCalendarContent conversion.
     *
     * @param cc the CalendarContent object to be converted
     * @param xv true if the VCalendarContent must be in text/x-vcalendar format
     * @return a VCalendarContent object
     * @throws com.funambol.common.pim.converter.ConverterException
     */
    public VCalendarContent cc2vcc(CalendarContent cc, boolean xv)
    throws ConverterException {

        VCalendarContent vcc;
        VAlarm valarm = null;
        if (cc instanceof Event) {
            vcc = new VEvent();
        } else {
            vcc = new VTodo();
        }

        boolean allDay = cc.isAllDay();

        List<com.funambol.common.pim.model.model.Property> properties =
                new ArrayList<com.funambol.common.pim.model.model.Property>(15);

        properties.add(composeField("UID"        , cc.getUid()        , xv));
        // Shouldn't be necessary: the UID is already known at Calendar level

        properties.add(composeField("SUMMARY"    , cc.getSummary()    , xv));
        properties.add(composeField("DESCRIPTION", cc.getDescription(), xv));
        properties.add(composeField("LOCATION"   , cc.getLocation()   , xv));
        properties.add(composeField("CATEGORIES" , cc.getCategories() , xv));

        Property pAC = cc.getAccessClass();
        if (pAC != null) {
            Object savedPropertyValue = null;
            try {
                savedPropertyValue = Short.parseShort(Property.stringFrom(pAC));

            } catch (NumberFormatException e) {
                savedPropertyValue = new Short("0");
            }

            String accessClass = accessClassFrom03((Short)savedPropertyValue);
            pAC.setPropertyValue(accessClass);
            properties.add(composeField("CLASS", cc.getAccessClass(), xv));
            pAC.setPropertyValue(savedPropertyValue); // Restores the value
        }

        PropertyWithTimeZone dtStart = cc.getDtStart();
        properties.add(composeDateTimeField("DTSTART", dtStart, allDay, xv));
        if (cc instanceof Event) {
            PropertyWithTimeZone pE = cc.getDtEnd();
            Object savedPropertyValue = null;
            if (pE != null) {
                savedPropertyValue = pE.getPropertyValue();
                String end = pE.getPropertyValueAsString();
                if (TimeUtils.isInAllDayFormat(end)) {
                    end = TimeUtils.rollOneDay(end, true); // Rolls on
                    pE.setPropertyValue(end);
                }

                // FIX for Android //////////////////////
                if (pE.getPropertyValue() == null) {
                    Property duration = cc.getDuration();
                    if (duration != null) {
                        String d = duration.getPropertyValueAsString();
                        if (d != null) {
                            end = TimeUtils.getDTEnd(dtStart.getPropertyValueAsString(), d, null, null);
                            if (end != null) {
                                pE.setPropertyValue(end);
                            }
                            // Inherit timezone from dtstart if null
                            if(pE.getTimeZone() == null && dtStart.getTimeZone() != null) {
                                pE.setTimeZone(dtStart.getTimeZone());
                            }
                        }
                    }
                }
                /////////////////////////////////////////
            }
            properties.add(composeDateTimeField("DTEND", pE, allDay, xv));
            if (savedPropertyValue != null) {
                pE.setPropertyValue(savedPropertyValue); // Restores the value
            }
        } else {
            properties.add(composeDateTimeField("DUE", cc.getDtEnd(), allDay, xv));
        }
        // NB: We decided not to store the duration but only Start and End (Due)

        properties.add(composeField("PRIORITY"       , cc.getPriority() , xv));
        properties.add(composeField("CONTACT"        , cc.getContact()  , xv));
        properties.add(composeField("URL"            , cc.getUrl()      , xv));
        properties.add(composeField("SEQUENCE"       , cc.getSequence() , xv));
        properties.add(composeField("PALARM"         , cc.getPAlarm()   , xv));
        properties.add(composeField("DALARM"         , cc.getDAlarm()   , xv));
        properties.add(composeField("ORGANIZER"      , cc.getOrganizer(), xv));
        properties.add(composeDateTimeField("DTSTAMP", cc.getDtStamp(), false, xv));

        if (cc instanceof Event) {
            properties.add(composeField("TRANSP", ((Event) cc).getTransp(), xv));
            properties.add(composeField("STATUS", cc.getStatus()          , xv));

        } else if (cc instanceof Task) {
            properties.add(composeField("PERCENT-COMPLETE",
                    ((Task) cc).getPercentComplete(), xv));
            properties.add(composeDateTimeField("COMPLETED",
                    ((Task) cc).getDateCompleted(), false, xv));

            CalendarStatus calendarStatus = CalendarStatus.mapServerStatus(cc.getStatus());

            properties.add(composeField("STATUS",
                    new Property((calendarStatus != null ? calendarStatus.getVCalICalValue(xv) : null)), xv));
        }

        properties.add(composeField("LAST-MODIFIED", cc.getLastModified(), xv));
        properties.add(composeDateTimeField("DCREATED", cc.getCreated(), false, xv));

        Reminder reminder = cc.getReminder();
        if (reminder != null && reminder.isActive()) {
            if (xv) {
                Object savedPropertyValue = reminder.getPropertyValue(); // null?
                reminder.setPropertyValue(extractAAlarmPropertyValue(dtStart,
                        reminder)); // Temporarily changes the value
                properties.add(composeDateTimeField("AALARM",
                                                    reminder, // A Reminder is
                                                              // a Property too
                                                    allDay  ,
                                                    xv      ,
                                                    false   ));
                reminder.setPropertyValue(savedPropertyValue); // Restores the value
            } else {
                valarm = new VAlarm();

                Object savedPropertyValue = reminder.getPropertyValue(); // null?
                reminder.setPropertyValue(extractReminderTime(dtStart, reminder));
                com.funambol.common.pim.model.model.Property trigger =
                        composeDateTimeField("TRIGGER",
                                             reminder,
                                             allDay,
                                             xv,
                                             false);

                trigger.setParameter(DATETIME_PARAMETER);
                valarm.addProperty(trigger);
                reminder.setPropertyValue(savedPropertyValue); // Restores the value

                valarm.addProperty("REPEAT",
                        String.valueOf(reminder.getRepeatCount()));

                int interval = reminder.getInterval();
                if (interval > 0) {
                    valarm.addProperty("DURATION"                          ,
                                       TimeUtils.getAlarmInterval(interval));
                }

                String soundFile = reminder.getSoundFile();
                if ((soundFile != null) && (soundFile.length() != 0)) {
                    valarm.addProperty("ACTION", "AUDIO");
                    valarm.addProperty("ATTACH", soundFile);
                }
            }
        }

        RecurrencePattern rp = cc.getRecurrencePattern();
        if (rp != null) {
            Object savedPropertyValue = rp.getPropertyValue(); // null?
            rp.setPropertyValue( // Temporarily changes the value
                    extractRRulePropertyValue(rp, xv));
            properties.add(composeDateTimeField(
                    "RRULE" ,
                    rp      , // A RecurrencePattern is also a Property
                    allDay  ,
                    xv      ,
                    true    )); // has a special behaviour
            rp.setPropertyValue(savedPropertyValue); // Restores the value
            properties.add(composeDateTimeField(
                    "EXDATE",
                    new PropertyWithTimeZone(extractExDatePropertyValue(rp, xv),
                                             rp.getTimeZone()                  ),
                    allDay  ,
                    xv      ,
                    true    )); // has a special behaviour
            properties.add(composeDateTimeField(
                    "RDATE" ,
                    new PropertyWithTimeZone(extractRDatePropertyValue(rp, xv),
                                             rp.getTimeZone()                 ),
                    allDay  ,
                    xv      ,
                    true    )); // has a special behaviour
        }

        if ((cc.getLatitude() != null) && (cc.getLongitude() != null)) {
            if ((cc.getLatitude().getPropertyValueAsString() != null) &&
                    (cc.getLongitude().getPropertyValueAsString() != null)) {
                String geo = cc.getLatitude().getPropertyValueAsString() + ";"
                        + cc.getLongitude().getPropertyValueAsString();
                if (geo.length() > 1) { // If it's not just a semicolon
                    Property tmp = cc.getLatitude();
                    Object savedPropertyValue = tmp.getPropertyValue();
                    tmp.setPropertyValue(geo); // Temporarily changes the value
                    properties.add(composeField("GEO", tmp, xv));
                    tmp.setPropertyValue(savedPropertyValue); // Restores the
                }                                                       // value
            }
        }

        properties.add(composeField("X-FUNAMBOL-FOLDER", cc.getFolder(), xv));

        for (int i = 0; i < properties.size(); i++) {
            if (properties.get(i) != null) {
                vcc.addProperty(properties.get(i));
            }
        }

        if (!xv && (valarm != null)) {
            vcc.addComponent(valarm);
        }

        if (xv) {
            try {
                String priority19 = vcc.getProperty("PRIORITY").getValue();
                String priority13 = String.valueOf(
                        importance19To13(Integer.parseInt(priority19)));
                vcc.getProperty("PRIORITY").setValue(priority13);
            } catch(Exception e) { //NumberFormatException, NullPointerException
                // Goes on
            }
        }

        if (cc.getBusyStatus() != null) {
            vcc.addProperty("X-MICROSOFT-CDO-BUSYSTATUS",
                    busyStatusFrom03(cc.getBusyStatus()));
        }

        if (cc.isAllDay()) {
            vcc.addProperty("X-FUNAMBOL-ALLDAY", "1");
        } else {
            vcc.addProperty("X-FUNAMBOL-ALLDAY", "0");
        }

        if (cc.getMeetingStatus() != null) {
            vcc.addProperty("PARTSTAT", cc.getMeetingStatus().toString());
        }

        /*
        // @todo
        List ccentXTag = cc.getXTags();
        for (int i=0; i<ccentXTag.size(); i++){
            //vcc.addProperty(composeFieldXTag(ccentXTag));
        }
        */

        // @todo Add Task-specific properties

        return vcc;

    }

    /**
     * Performs the VCalendarContent-to-CalendarContent conversion.
     *
     * @param vcc the VCalendarContent object to be converted
     * @param xv true if the text/x-vcalendar format must be used while
     *           generating some properties of the VCalendar output object
     * @return a CalendarContent object
     * @throws com.funambol.common.pim.converter.ConverterException
     */
    public CalendarContent vcc2cc(VCalendarContent vcc, boolean xv)
    throws ConverterException {
        return vcc2cc(vcc, xv, null, null, null);
    }

    /**
     * Performs the VCalendarContent-to-CalendarContent conversion.
     *
     * @param vcc the VCalendarContent object to be converted
     * @param xv true if the text/x-vcalendar format must be used while
     *           generating some properties of the VCalendar output object
     * @param dtStartTimeZone
     * @param dtEndTimeZone
     * @param reminderTimeZone
     * @return a CalendarContent object
     * @throws com.funambol.common.pim.converter.ConverterException
     */
    protected CalendarContent vcc2cc(VCalendarContent vcc             ,
                                     boolean          xv              ,
                                     TimeZone         dtStartTimeZone ,
                                     TimeZone         dtEndTimeZone   ,
                                     TimeZone         reminderTimeZone)
    throws ConverterException {

        // Sets the three time zones
        this.dtStartTimeZone =
                (dtStartTimeZone == null ) ?
                timezone                   : dtStartTimeZone ;
        this.dtEndTimeZone =
                (dtEndTimeZone == null   ) ?
                timezone                   : dtEndTimeZone   ;
        this.reminderTimeZone =
                (reminderTimeZone == null) ?
                timezone                   : reminderTimeZone;

        CalendarContent cc;
        if (vcc instanceof VEvent) {
            cc = new Event();
        } else {
            cc = new Task();
        }

        cc.setDtStart (decodeDateTimeField(vcc.getProperty("DTSTART" ),
                dtStartTimeZone));
        cc.setDuration(decodeField(vcc.getProperty("DURATION")));

        boolean isAllday = false;

        PropertyWithTimeZone pS;
        PropertyWithTimeZone pE;
        if (vcc instanceof VEvent) {
            pS = decodeDateTimeField(vcc.getProperty("DTSTART"), dtStartTimeZone);
            pE = decodeDateTimeField(vcc.getProperty("DTEND"), dtEndTimeZone);

            //
            // An event is an allday when the dtstart is in the format
            // PATTERN_YYYYMMDD or PATTERN_YYYY_MM_DD and
            // the dtend is null (this means there is the duration)
            // or
            // the dtend is in allday format
            // If the event is already an allday event, it's needed
            // to roll back one day from the end date in order to exclude the
            // last day like required by iCal specification
            //
            if (TimeUtils.isInAllDayFormat(pS.getPropertyValueAsString())) {
                if (pE.getPropertyValueAsString() == null ||
                    TimeUtils.isInAllDayFormat(pE.getPropertyValueAsString())) {

                    isAllday = true;
                }
            }

        } else {
            pE = decodeDateTimeField(vcc.getProperty("DUE"), dtEndTimeZone);
        }
        cc.setDtEnd(pE);

        cc.setDtStamp     (decodeDateTimeField(vcc.getProperty("DTSTAMP"      ),
                dtStartTimeZone));
        cc.setLastModified(decodeDateTimeField(vcc.getProperty("LAST-MODIFIED"),
                dtStartTimeZone));
        cc.setCreated     (decodeDateTimeField(vcc.getProperty("DCREATED"     ),
                dtStartTimeZone));

        if (cc instanceof Task) {
            ((Task) cc).setDateCompleted(
                decodeDateTimeField(vcc.getProperty("COMPLETED"),
                                    dtStartTimeZone));
        }

        try {

            fixGenericDateProperty(cc.getLastModified());
            fixGenericDateProperty(cc.getCreated());

            //
            // The dates must be fixed before the RRULE and the AALARM
            // properties are parsed
            //
            if (cc instanceof Event) {
                fixDates(cc, true);

                //
                // Only if the event is recognized like an allday event in the
                // right allday format the roll back one day it's needed.
                //
                String start = cc.getDtStart().getPropertyValueAsString();
                String end = cc.getDtEnd().getPropertyValueAsString();
                if ((end != null) &&
                    !(end.equals(start)) && // This is for robustness's sake
                    TimeUtils.isInAllDayFormat(end) &&
                    isAllday) {

                    end = TimeUtils.rollOneDay(end, false); // Rolls back
                    cc.getDtEnd().setPropertyValue(end);
                }

            } else if (cc instanceof Task) {
                fixDates(cc, false);
            }

        } catch (Exception e) {
            throw new ConverterException("Error while fixing the dates", e);
        }

        cc.setUid(decodeField(vcc.getProperty("UID")));
        // Shouldn't be necessary: the UID is already known at Calendar level

        if (cc instanceof Event) {
            ((Event) cc).setTransp(decodeField(vcc.getProperty("TRANSP")));
        }

        cc.setSummary    (decodeField(vcc.getProperty("SUMMARY"    )));
        cc.setDescription(decodeField(vcc.getProperty("DESCRIPTION")));
        cc.setLocation   (decodeField(vcc.getProperty("LOCATION"   )));

        if (cc instanceof Task) {
            Property status = new Property();
            CalendarStatus calendarStatus=CalendarStatus.mapVcalIcalStatus(decodeField(vcc.getProperty("STATUS")));
            status.setPropertyValue((calendarStatus!=null?calendarStatus.getServerValue():null));
            cc.setStatus(status);
        }else{
            cc.setStatus(decodeField(vcc.getProperty("STATUS" )));
        }

        cc.setCategories (decodeField(vcc.getProperty("CATEGORIES" )));

        String accessClass;
        com.funambol.common.pim.model.model.Property tmpClass =
                vcc.getProperty("CLASS");
        if (tmpClass == null) {
            accessClass = null;
        } else {
            accessClass = tmpClass.getValue();
        }
        Property accessClassProperty = new Property();
        accessClassProperty.setPropertyValue(
                new Short(accessClassTo03(accessClass)));

        cc.setAccessClass(accessClassProperty);

        cc.setPriority(decodeField(vcc.getProperty("PRIORITY")));
        if (xv) {
            try {
                String priority13 = cc.getPriority().getPropertyValueAsString();
                String priority19 = String.valueOf(
                        importance13To19(Integer.parseInt(priority13)));
                cc.getPriority().setPropertyValue(priority19);
            } catch(Exception e) { //NumberFormatException, NullPointerException
                // Goes on
            }
        }
        cc.setContact  (decodeField(vcc.getProperty("CONTACT"  )));
        cc.setUrl      (decodeField(vcc.getProperty("URL"      )));
        cc.setSequence (decodeField(vcc.getProperty("SEQUENCE" )));
        cc.setPAlarm   (decodeDateTimeField(vcc.getProperty("PALARM"   ),
                reminderTimeZone));
        cc.setDAlarm   (decodeDateTimeField(vcc.getProperty("DALARM"   ),
                reminderTimeZone));
        cc.setOrganizer(decodeField(vcc.getProperty("ORGANIZER")));

        com.funambol.common.pim.model.model.Property msCdoBusyStatus =
                vcc.getProperty("X-MICROSOFT-CDO-BUSYSTATUS");
        if (msCdoBusyStatus != null) {
            String busyStatus = msCdoBusyStatus.getValue();
            if (busyStatus != null) {
                cc.setBusyStatus(busyStatusTo03(busyStatus));
            }
        }

        Short meetingStatus = decodeShortField(vcc.getProperty("PARTSTAT"));
        if (meetingStatus != null) {
            cc.setMeetingStatus(meetingStatus);
        }

        Property geo1 = decodeField(vcc.getProperty("GEO"));
        Property geo2 = decodeField(vcc.getProperty("GEO"));
        if (geo1 != null) {
            StringTokenizer st = new StringTokenizer(
                    geo1.getPropertyValueAsString(), ";");
            if(st.countTokens() == 2) {
                geo1.setPropertyValue(st.nextToken());
                cc.setLatitude(geo1);
                geo2.setPropertyValue(st.nextToken());
                cc.setLongitude(geo2);
            }
        }

        //
        // If the calendar is an all-day event or task but no device timezone
        // is set and there is a start date, the time interval between the start
        // date time and the aalarm time is computed and this difference is
        // applied to the corrected start time of the all-day event or task
        // (ie, midnight UTC) irrespective of any time zone consideration.
        // This modified aalarm time is saved in the DB as a UTC time, but
        // it's interpreted as a local time when it's retrieved (exactly as
        // start and end dates when the all-day flag is on).
        //
        Reminder reminder = null;
        if (xv) { // vCalendar
            Property aalarm = decodeField(vcc.getProperty("AALARM"));
            if (aalarm != null && aalarm.getPropertyValueAsString() != null) {

                Property dtstart = decodeField(vcc.getProperty("DTSTART"));

                if (cc.isAllDay() && reminderTimeZone == null && dtstart != null) {
                    reminder = convertAAlarmToReminderBasedOnMinutes(
                                        dtstart.getPropertyValueAsString(),
                                        cc.getDtStart().getPropertyValueAsString(),
                                        aalarm.getPropertyValueAsString()
                    );

                } else {
                    reminder = convertAAlarmToReminder(
                                                cc.isAllDay(),
                                                cc.getDtStart(),
                                                aalarm.getPropertyValueAsString()
                    );
                }
            }
        } else { // iCalendar
            VAlarm valarm = (VAlarm) vcc.getComponent("VALARM");
            if (valarm != null) {
                reminder = convertVAlarmToReminder(valarm         ,
                                                   cc.isAllDay()  ,
                                                   cc.getDtStart(),
                                                   cc.getDtEnd()  );
            }
        }
        if (reminder != null && reminderTimeZone != null) {
            reminder.setTimeZone(reminderTimeZone.getID());
        }
        cc.setReminder(reminder);

        Property rrule = decodeField(vcc.getProperty("RRULE"));
        if (rrule != null && rrule.getPropertyValueAsString().length() != 0) {
            try {
                cc.setRecurrencePattern(
                    getRecurrencePattern(
                        cc.getDtStart().getPropertyValueAsString(),
                        cc.getDtEnd().getPropertyValueAsString()  ,
                        rrule.getPropertyValueAsString()          ,
                        dtStartTimeZone                           ,
                        xv
                    )
                );

                List<com.funambol.common.pim.model.model.Property> rdates =
                        vcc.getProperties("RDATE");
                for (com.funambol.common.pim.model.model.Property rdateProperty : rdates) {
                    Property rdate = decodeField(rdateProperty);
                    if (rdate != null) {
                        cc.getRecurrencePattern()
                          .getExceptions()
                          .addAll(getRDates(rdate.getPropertyValueAsString(),
                                            cc.isAllDay()                   ));
                    }
                }

                List<com.funambol.common.pim.model.model.Property> exdates =
                        vcc.getProperties("EXDATE");
                for (com.funambol.common.pim.model.model.Property exdateProperty : exdates) {
                    Property exdate = decodeField(exdateProperty);
                    if (exdate != null) {
                        cc.getRecurrencePattern()
                          .getExceptions()
                          .addAll(getExDates(exdate.getPropertyValueAsString(),
                                             cc.isAllDay()                   ));
                    }
                }

            } catch (ConverterException ce) {
                cc.setRecurrencePattern(null); // Ignore parsing errors
            }
        }

        /*
        // @todo
        List ccentXTag = cc.getXTags();
        for (int i=0; i<ccentXTag.size(); i++){
            //vcc.addProperty(composeFieldXTag(ccentXTag));
        }
         */

        if (cc instanceof Task) {
            ((Task) cc).setPercentComplete(
                decodeField(vcc.getProperty("PERCENT-COMPLETE"))
            );

            String status = null;
            if ((cc.getStatus() != null)) {
                status = cc.getStatus().getPropertyValueAsString();
            }
            if ((status != null) && (status.length() != 0)) {
                if ("COMPLETED".equalsIgnoreCase(status)) {
                    ((Task) cc).setComplete(new Property("1"));
                } else {
                    ((Task) cc).setComplete(new Property("0"));
                }
            }
        }

        cc.setFolder(decodeField(vcc.getProperty("X-FUNAMBOL-FOLDER")));

        return cc;
    }

    //---------------------------------------------------------- Private Methods

    /**
     * @return a representation of the field RRULE (version 1.0)
     */
    private String composeFieldRrule(RecurrencePattern rrule) {

        StringBuffer result = new StringBuffer(60); // Estimate 60 is needed

        if (rrule != null) {

            addXParams(result, rrule);

            result.append(rrule.getTypeDesc()).append(rrule.getInterval());

            if (rrule.getInstance() < 0) {
                result.append(" " + (-rrule.getInstance()) + "-");
            } else if (rrule.getInstance() > 0) {
                result.append(" " + rrule.getInstance() + "+");
            } // else, it's zero and nothing's to be done

            for (String day : rrule.getDayOfWeek()) {
                result.append(' ').append(day);
            }
            if (rrule.getDayOfMonth() != 0 && !"YM".equals(rrule.getTypeDesc())) {
                result.append(' ').append(rrule.getDayOfMonth());
            }
            if (rrule.getMonthOfYear() != 0) {
                result.append(' ').append(rrule.getMonthOfYear());
            }
            if (rrule.getOccurrences() != -1 && rrule.isNoEndDate()) {
                result.append(" #").append(rrule.getOccurrences());
            } else {
                if (rrule.isNoEndDate()) {
                    result.append(" #0"); //forever
                }
            }
            if (!rrule.isNoEndDate()               &&
                 rrule.getEndDatePattern() != null &&
                !rrule.getEndDatePattern().equals("")) {

                TimeZone propertyTimeZone;
                String timeZoneID = rrule.getTimeZone();
                if (timeZoneID == null) {
                    propertyTimeZone = timezone;
                } else {
                    propertyTimeZone = TimeZone.getTimeZone(timeZoneID);
                }

                try {
                    result.append(' ');

                    String endDatePattern = rrule.getEndDatePattern();

                    endDatePattern =
                        handleConversionToLocalDate(endDatePattern, propertyTimeZone);

                    if (TimeUtils.isInAllDayFormat(endDatePattern)) {
                        endDatePattern =
                            TimeUtils.convertDateFromInDayFormat(endDatePattern, "000000");
                    }
                    result.append(endDatePattern);

                } catch (ParseException e) {
                    // This should never happen!
                } catch (ConverterException ce) {
                    // This should never happen!
                }

            }
        }
        return result.toString();
    }

    private Reminder convertVAlarmToReminder(VAlarm valarm               ,
                                             boolean allDay              ,
                                             PropertyWithTimeZone dtStart,
                                             PropertyWithTimeZone dtEnd  ) {
        Reminder reminder = new Reminder();

        try {
            com.funambol.common.pim.model.model.Property triggerProperty =
                    valarm.getProperty("TRIGGER");
            String trigger = triggerProperty.getValue();
            if (trigger.startsWith("-P") || trigger.startsWith("P")) { // it's an interval
                int minutes = -TimeUtils.getAlarmInterval(trigger);
                String related = triggerProperty.getParameter("RELATED").value;
                PropertyWithTimeZone relatedProperty;
                if ("END".equals(related)) {
                    relatedProperty = dtEnd;
                } else {
                    relatedProperty = dtStart;
                    reminder.setMinutes(minutes);
                }
                setAlarmTimeBasedOnMinutes(reminder, minutes, relatedProperty, allDay);
            } else {
                setAlarmTimeAndMinutes(reminder, trigger, dtStart, allDay);
            }

            com.funambol.common.pim.model.model.Property repeatProperty =
                    valarm.getProperty("REPEAT");
            if (repeatProperty != null) {
                try {
                    int repeatCount = Integer.parseInt(repeatProperty.getValue());
                    reminder.setRepeatCount(repeatCount);
                } catch (NumberFormatException e) {
                    // Does nothing
                }
            }

            com.funambol.common.pim.model.model.Property durationProperty =
                    valarm.getProperty("DURATION");
            if (durationProperty != null) {
                int interval =
                        TimeUtils.getAlarmInterval(durationProperty.getValue());
                reminder.setInterval(interval);
            }

            com.funambol.common.pim.model.model.Property actionProperty =
                    valarm.getProperty("ACTION");
            if ((actionProperty != null) &&
                ("AUDIO".equals(actionProperty.getValue()))) {
                com.funambol.common.pim.model.model.Property attachProperty =
                    valarm.getProperty("ATTACH");
                if (attachProperty != null) {
                    reminder.setSoundFile(attachProperty.getValue());
                }
            }

            return reminder;

        } catch (Exception e) {
            return null;
        }
    }

    private void setAlarmTimeBasedOnMinutes(Reminder reminder                   ,
                                            int minutes                         ,
                                            PropertyWithTimeZone relatedProperty,
                                            boolean allDay                      )
    throws ConverterException {

        String relatedTime;
        SimpleDateFormat formatter;
        if (!allDay) {
            String relatedPropertyValue = relatedProperty.getPropertyValueAsString();
            if (relatedPropertyValue == null) {
                throw new ConverterException("RELATED parameter refers to non-existing property");
            }
            String relatedTimeZoneID = relatedProperty.getTimeZone();
            TimeZone relatedTimeZone = (relatedTimeZoneID == null              ?
                                        null                                   :
                                        TimeZone.getTimeZone(relatedTimeZoneID));
            relatedTime =
                    handleConversionToUTCDate(relatedPropertyValue,
                                              relatedTimeZone     );
            formatter = new SimpleDateFormat(TimeUtils.PATTERN_UTC);
        } else {
            relatedTime = (relatedProperty.getPropertyValueAsString() + "T000000")
                          .replaceAll("-", "");
            formatter = new SimpleDateFormat(TimeUtils.PATTERN_UTC_WOZ);
        }
        formatter.setTimeZone(TimeUtils.TIMEZONE_UTC);
        Date relatedDate;
        try {
            relatedDate = formatter.parse(relatedTime);
        } catch (ParseException e) {
            throw new ConverterException(e);
        }
        GregorianCalendar greg = new GregorianCalendar();
        greg.setTime(relatedDate);
        greg.add(GregorianCalendar.MINUTE, -minutes);
        reminder.setTime(formatter.format(greg.getTime()));

        reminder.setActive(true);
    }

    private void setAlarmTimeAndMinutes(Reminder reminder,
                                        String time      ,
                                        Property dtStart ,
                                        boolean allDay   )
    throws ConverterException {

        String alarmStart;

       // If the calendar is an all day event (or task) then
       // the aalarm time is considered as a local time
       // information consistently with the way start and end
       // dates are processed.
        if (allDay) {

            // Converts aalarm in local date and time to preserve
            // the distance from aalarm time to midnigth of the
            // start date.
            alarmStart =
                handleConversionToLocalDate(time, reminderTimeZone);

        } else {

            // Converts aalarm in UTC date and time to preserve
            // the absolute moment of the aalarm.
            alarmStart =
                handleConversionToUTCDate(time, reminderTimeZone);
        }
        reminder.setTime(alarmStart);

        if (dtStart != null) {
                reminder.setMinutes(
                    TimeUtils.getAlarmMinutes(
                        dtStart.getPropertyValueAsString(),
                        reminder.getTime(),
                        null
                    )
            );
        } else {
            reminder.setMinutes(0);
        }
        reminder.setActive(true);
    }

    /**
     * Returns the reminder time calculated using the given dtstart and the
     * minutes set in the given Reminder in case of the in this object the time
     * is null.
     */
    private String extractReminderTime(PropertyWithTimeZone dtStart ,
                                       Reminder             reminder)
    throws ConverterException {

        java.util.Date dateStart = null;
        SimpleDateFormat formatter = new SimpleDateFormat();

        String dtStartVal=(String)dtStart.getPropertyValue();
        String dtAlarmVal = reminder.getTime();
        if (dtAlarmVal == null || dtAlarmVal.length() == 0) {
            if (dtStartVal == null || dtStartVal.length() == 0) {
                return null;
            }
            try {
                TimeZone propertyTimeZone;
                String timeZoneID = dtStart.getTimeZone();
                if (timeZoneID == null) {
                    propertyTimeZone = timezone;
                } else {
                    propertyTimeZone = TimeZone.getTimeZone(timeZoneID);
                }
                dtStartVal =
                    handleConversionToUTCDate(dtStartVal, propertyTimeZone);
                formatter.applyPattern(TimeUtils.getDateFormat(dtStartVal));
                dateStart = formatter.parse(dtStartVal);
            } catch (Exception e) {
                throw new ConverterException("Error while parsing start date "
                        + "during reminder time calculation", e);
            }

            java.util.Calendar calAlarm = java.util.Calendar.getInstance();
            calAlarm.setTime(dateStart);
            calAlarm.add(java.util.Calendar.MINUTE, -reminder.getMinutes());

            Date dtAlarm = calAlarm.getTime();
            formatter.applyPattern("yyyyMMdd'T'HHmmss'Z'");
            dtAlarmVal = formatter.format(dtAlarm);
        }

        TimeZone propertyTimeZone;
        String timeZoneID = reminder.getTimeZone();
        if (timeZoneID == null) {
            propertyTimeZone = timezone;
        } else {
            propertyTimeZone = TimeZone.getTimeZone(timeZoneID);
        }

        if (forceClientLocalTime) {
            dtAlarmVal = handleConversionToLocalDate(dtAlarmVal, propertyTimeZone);
        }
        return dtAlarmVal;
    }

    /**
     * @return a representation of the event field AALARM
     */
    private String extractAAlarmPropertyValue(PropertyWithTimeZone dtStart ,
                                              Reminder             reminder)
    throws ConverterException {

        StringBuffer result = new StringBuffer(60); // 60 has been estimated OK

        String dtAlarmVal = extractReminderTime(dtStart, reminder);
        result.append(dtAlarmVal).append(';');

        if (reminder.getInterval() != 0) {
            result.append(TimeUtils.getIso8601Duration(String.valueOf(reminder.getInterval())));
        }
        result.append(';').append(reminder.getRepeatCount());

        result.append(';');
        if (reminder.getSoundFile() != null) {
            result.append(reminder.getSoundFile());
        }

        return result.toString();
    }

    private String extractRRulePropertyValue(RecurrencePattern rp, boolean xv) {

        if (xv) {
            return composeFieldRrule(rp);
        }

        StringBuffer result = new StringBuffer(99); // 99 has been estimated OK

        String type = null;
        switch(rp.getTypeId()) {
            case RecurrencePattern.TYPE_DAILY:
                type = DAILY;
                break;
            case RecurrencePattern.TYPE_WEEKLY:
                type = WEEKLY;
                break;
            case RecurrencePattern.TYPE_MONTHLY:
            case RecurrencePattern.TYPE_MONTH_NTH:
                type = MONTHLY;
                break;
            case RecurrencePattern.TYPE_YEARLY:
            case RecurrencePattern.TYPE_YEAR_NTH:
                type = YEARLY;
                break;
            default:
                return null;
        }
        appendToStringBuffer(result, FREQ, type);

        appendToStringBuffer(result, INTERVAL, String.valueOf(rp.getInterval()));

        appendToStringBuffer(result, BYMONTH,
                String.valueOf(rp.getMonthOfYear()));

        appendToStringBuffer(result, BYMONTHDAY,
                String.valueOf(rp.getDayOfMonth()));

        appendToStringBuffer(result, BYSETPOS,
                String.valueOf(rp.getInstance()));

        int occurrences = rp.getOccurrences();
        if (occurrences != -1) { // means unspecified: see RecurrencePattern
            appendToStringBuffer(result, COUNT, String.valueOf(occurrences));
        } else {
            if (!rp.isNoEndDate()) {
                try {

                    TimeZone propertyTimeZone;
                    String timeZoneID = rp.getTimeZone();
                    if (timeZoneID == null) {
                        propertyTimeZone = timezone;
                    } else {
                        propertyTimeZone = TimeZone.getTimeZone(timeZoneID);
                    }
                    appendToStringBuffer(result, UNTIL,
                            handleConversionToLocalDate(
                                String.valueOf(rp.getEndDatePattern()),
                                propertyTimeZone
                            )
                    );
                } catch (ConverterException ce) {
                    // This should never happen!
                }
            }
        }

        String weekDays = "";
        for(
                short mask = rp.getDayOfWeekMask(), j = 0;
                mask > 0; // Until the mask has been eaten up
                mask /= 2, j++) { // Shifts the mask, looks for the next weekday

            if (mask % 2 == 1) { // Bingo!
                weekDays += "," + WEEK[j]; // Adds the correct weekday symbol
            }
        }
        if (weekDays.length() > 0) {
            appendToStringBuffer(result, BYDAY,
                    weekDays.substring(1)); // Discards the first ","
        }

        return result.toString();

    }

    /**
     * @see extractExceptionsAsString(RecurrencePattern, boolean, boolean)
     */
    private String extractExDatePropertyValue(RecurrencePattern rp, boolean xv) {
        return extractExceptionsAsString(rp, false, xv);
    }

    /**
     * @see extractExceptionsAsString(RecurrencePattern, boolean, boolean)
     */
    private String extractRDatePropertyValue(RecurrencePattern rp, boolean xv) {
        return extractExceptionsAsString(rp, true, xv);
    }

    /**
     * Extracts the recurrence exceptions from a given RecurrencePattern object
     * and returns them as a string of colon- or semicolon-separated-values.
     *
     * @param rp the recurrence pattern that contains the exception list
     * @param rdate true if only the addition exceptions are to be extracted,
     *              false if only the deletion exceptions are to be extracted
     * @param xv true if the format to be used is vCalendar (1.0),
     *           false if it is iCalendar (vCalendar 2.0).
     *           The difference is about the usage of ';' or ',' as a separator
     * @return the list in the proper vCalendar/iCalendar format
     */
    private String extractExceptionsAsString(RecurrencePattern rp   ,
                                             boolean           rdate,
                                             boolean           xv   ) {

        List<ExceptionToRecurrenceRule> exceptions = rp.getExceptions();
        StringBuilder result = new StringBuilder();
        char separator = (xv ? ';' : ','); // as of specifications

        TimeZone propertyTimeZone;
        String timeZoneID = rp.getTimeZone();
        if (timeZoneID == null) {
            propertyTimeZone = timezone;
        } else {
            propertyTimeZone = TimeZone.getTimeZone(timeZoneID);
        }
        for (ExceptionToRecurrenceRule etrr : exceptions) {
            if (etrr.isAddition() == rdate) {
                if (result.length() > 0) { // always but the first time
                    result.append(separator);
                }
                try {
                    result.append(
                        handleConversionToLocalDate(etrr.getDate()  ,
                                                    propertyTimeZone)
                    );
                } catch (ConverterException ce) {
                    result.append(etrr.getDate()); // Keeps it as it is
                }
            }
        }

        if (result.length() == 0) {
            return null;
        }
        return result.toString();
    }

    private void appendToStringBuffer(StringBuffer sb   ,
                                      String       key  ,
                                      String       value) {

        if (value != null && !ZERO.equals(value)) {
            if (!key.equals(FREQ)) { // FREQ is the first piece of the RRULE
                sb.append(';');
            }
            sb.append(key).append('=').append(value);
        }
    }

    private static boolean isEndDateOrDuration(String token) { // Just for RRULE 1.0
        if (token.startsWith("#")) { // it's a duration
            return true;
        }
        if (token.length() >= 8) { // it's an end date
            return true;
        }
        return false;
    }

    private static short instanceModifierToInt(String modifier) { // Irrespective of the
        if (modifier.indexOf("-") != -1) {                                 // version
            return (short) - Short.parseShort(modifier.replaceAll("\\-", ""));
        }
        return Short.parseShort(modifier.replaceAll("\\+", ""));
    }

    private static boolean beginsWith1To9(String s) {
        switch (s.charAt(0)) {
            case '1' :
            case '2' :
            case '3' :
            case '4' :
            case '5' :
            case '6' :
            case '7' :
            case '8' :
            case '9' :
                return true;
            default:
                return false;
        }
    }

    public static RecurrencePattern getRecurrencePattern(String   startDate   ,
                                                         String   endDate     ,
                                                         String   rrule       ,
                                                         TimeZone recurrenceTZ,
                                                         boolean xv           )
    throws ConverterException {

        if (rrule == null || rrule.length() == 0) {
            return null;
        }

        String startDatePattern;

        if (startDate == null || startDate.length() == 0) {
            if (endDate == null) {
                return null;
            }
            startDatePattern = endDate;
        } else {
            startDatePattern = startDate;
        }

        Map<String, List<String>> map =
                new HashMap<String, List<String>>(7);
        List<String> week = null;
        short type = -1;
        int occurrences = -1; // means unspecified: see RecurrencePattern
        short instance = 0;
        int interval;
        short dayOfWeekMask = 0;
        short dayOfMonth = 0;
        short monthOfYear = 0;
        String endDatePattern = null;

        if (xv) {
            try {
                StringTokenizer st = new StringTokenizer(rrule, " ");
                String frequencyInterval = st.nextToken();
                String durationOrEndDate = null;
                int c = 2; // Default: frequency is 2-character long

                if (frequencyInterval.startsWith("D")) {
                    type = RecurrencePattern.TYPE_DAILY;
                    c = 1; // Shorter than default
                    // No modifier... moves on to end date or duration

                } else if (frequencyInterval.startsWith("W")) {
                    type = RecurrencePattern.TYPE_WEEKLY;
                    c = 1; // Shorter than default
                    week = new ArrayList<String>(7); // Big enough!
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        if (isEndDateOrDuration(token)) {
                            durationOrEndDate = token;
                            break;
                        } else {
                            week.add(token);
                        }
                    }

                } else if (frequencyInterval.startsWith("MD")) {
                    type = RecurrencePattern.TYPE_MONTHLY;
                    String monthDay = null;
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        if (isEndDateOrDuration(token)) {
                            durationOrEndDate = token;
                            break;
                        } else if (monthDay == null) { // Just the 1st one found
                            monthDay = token;
                        }
                    }
                    if (monthDay != null) {
                        dayOfMonth = instanceModifierToInt(monthDay);
                    }

                } else if (frequencyInterval.startsWith("MP")) {
                    type = RecurrencePattern.TYPE_MONTH_NTH;
                    week = new ArrayList<String>(7); // Big enough!
                    String instanceModifier = "";
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        if (isEndDateOrDuration(token)) {
                            durationOrEndDate = token;
                            break;
                        } else if (beginsWith1To9(token)) {
                            instanceModifier = token;
                        } else {
                            week.add(new String(instanceModifier + token));
                        }
                    }

                } else if (frequencyInterval.startsWith("YD")) {
                    type = RecurrencePattern.TYPE_YEAR_NTH;
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        if (isEndDateOrDuration(token)) {
                            durationOrEndDate = token;
                            break;
                        }
                    }
                    // Month etc. are ignored and set below with rp.fix()

                } else if (frequencyInterval.startsWith("YM")) {
                    type = RecurrencePattern.TYPE_YEARLY;
                    week = new ArrayList<String>(7); // Big enough!
                    String instanceModifier = "";
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        if (isEndDateOrDuration(token)) {
                            durationOrEndDate = token;
                            break;
                        } else if (beginsWith1To9(token)) {
                            instanceModifier = token;
                        } else {
                            week.add(new String(instanceModifier + token));
                        }
                    }
                } else {
                    throw new ConverterException("Error while parsing RRULE " +
                            "(1.0): frequency not recognized");
                }
                interval = Short.parseShort(
                        frequencyInterval.substring(c));

                if (durationOrEndDate == null) { // If it's not been retricced,
                    if (!st.hasMoreTokens()) {                            // yet
                        durationOrEndDate = "#2"; // Default: repeat twice
                    } else {
                        durationOrEndDate = st.nextToken();
                    }
                }
                if (durationOrEndDate.startsWith("#")) { // it's a duration
                    occurrences = Short.parseShort(
                            durationOrEndDate.substring(1));
                    if (st.hasMoreTokens()) {
                        // This is possible in theory. We ignore this case but
                        // the specification prescribes to use whichever
                        // requirement is stricter among the duration and the
                        // end date. We just use the first one we find.
                    }
                } else { // it's an end date
                    endDatePattern = handleConversionToUTCDate(
                            durationOrEndDate,
                            recurrenceTZ);
                     if (st.hasMoreTokens()) {
                        // This is possible in theory. We ignore this case but
                        // the specification prescribes to use whichever
                        // requirement is stricter among the duration and the
                        // end date. We just use the first one we find.
                    }
                }

            } catch (Exception e) {
                throw new ConverterException("Error while parsing RRULE (1.0): "
                        + e);
            }

        } else {
            try {
                StringTokenizer stSemiColon =
                        new StringTokenizer(rrule, ";");
                while(stSemiColon.hasMoreTokens()) {
                    StringTokenizer stEquals =
                            new StringTokenizer(stSemiColon.nextToken(), "=");
                    String key = stEquals.nextToken();
                    StringTokenizer stComma =
                            new StringTokenizer(stEquals.nextToken(), ",");
                    List<String> list = new ArrayList<String>();
                    while (stComma.hasMoreTokens()) {
                        list.add(stComma.nextToken());
                    }
                    map.put(key, list);
                }
            } catch (Exception e) {
                throw new ConverterException("Error while parsing RRULE (2.0): "
                        + e);
            }

            interval = Short.parseShort(find(map, INTERVAL, true));
            if (interval == 0) {
                interval = 1;
            }
            monthOfYear = Short.parseShort(find(map, BYMONTH, true));
            dayOfMonth = Short.parseShort(find(map, BYMONTHDAY, true));
            instance = Short.parseShort(find(map, BYSETPOS, true));
            occurrences = Short.parseShort(find(map, COUNT, true));
            endDatePattern = find(map, UNTIL, false);
            try {
                endDatePattern = handleConversionToUTCDate(endDatePattern,
                        recurrenceTZ);
            } catch (Exception e) {
                throw new ConverterException("Error while parsing RRULE (2.0):"
                         + " timezone-based conversion failed.");
            }
            List<String> obj = map.get(BYDAY);
            if (obj != null) {
                week = obj;
            }

            String freq = find(map, FREQ, false);
            if (freq == null) {
                 throw new ConverterException("Error while parsing RRULE (2.0):"
                         + " frequency not found");
            }
            if (freq.equals(DAILY)) {
                type = RecurrencePattern.TYPE_DAILY;

            } else if (freq.equals(WEEKLY)) {
                type = RecurrencePattern.TYPE_WEEKLY;

            } else if (freq.equals(MONTHLY)) {
                if (week == null) {
                    type = RecurrencePattern.TYPE_MONTHLY;

                } else {
                    type = RecurrencePattern.TYPE_MONTH_NTH;
                }

            } else if (freq.equals(YEARLY)) {
                if (week == null) {
                    type = RecurrencePattern.TYPE_YEARLY;
                } else {
                    type = RecurrencePattern.TYPE_YEAR_NTH;
                }
            }
            if (type == -1) { // is the default value
                 throw new ConverterException("Error while parsing RRULE (2.0):"
                         + " frequency not found");
            }
        }

        if (occurrences == 0) {
            occurrences = -1; // means unspecified: see RecurrencePattern
        }

        boolean noEndDate = (endDatePattern == null);

        // Duplicates will be counted just once
        if (week != null) {
            short maskElement = 1;
            for (
                    int j = 0;
                    j < 7;           // 7, because a week has 7 days
                    j++,
                    maskElement *= 2 // Sunday = 1, Monday = 2, Tuesday = 4 etc.
                    ) {
                for (int i = 0; i < week.size(); i++) {
                    int pos = week.get(i).indexOf(WEEK[j]);
                    if (pos != -1) { // Bingo!
                        dayOfWeekMask += maskElement;
                        if (pos > 0) { // There's an instance marker, too
                            if (instance == 0) { // it's not been set yet
                                try { // Takes just the 1st modifier it finds
                                    instance = instanceModifierToInt(
                                            week.get(i).substring(0, pos));
                                } catch (Exception e) {
                                    throw new ConverterException("Error while "
                                            + "parsing RRULE's instance "
                                            + "modifier.", e);
                                }
                            } else {
                                // Currently, multi-instance recurrence patterns
                                // are not supported.
                            }
                        }
                        week.remove(i); // It won't be used any more
                        break; // Time to look for the next week day
                    }
                }
            }
        }

        RecurrencePattern rp = new RecurrencePattern(
                type,
                interval,
                monthOfYear,
                dayOfMonth,
                dayOfWeekMask,
                instance,
                startDatePattern,
                endDatePattern,
                noEndDate,
                occurrences);
        if (recurrenceTZ != null) {
            rp.setTimeZone(recurrenceTZ.getID());
        }

        try {
            rp.fix(); // If it lacks some information, it's extracted from the
                      // start date
        } catch (RecurrencePatternException rpe) {
            throw new ConverterException("Error while fixing a newly parsed " +
                    "recurrence pattern that lacks some information.", rpe);
        }

        return rp;
    }

    /**
     * @see getExceptionsAsSet(String,boolean)
     */
    private SortedSet<ExceptionToRecurrenceRule> getRDates(String rdate, boolean isAllDay) {
        return getExceptionsAsSet(rdate, true, isAllDay);
    }

    /**
     * @see getExceptionsAsSet(String,boolean)
     */
    private SortedSet<ExceptionToRecurrenceRule> getExDates(String exdate, boolean isAllDay) {
        return getExceptionsAsSet(exdate, false, isAllDay);
    }

    /**
     * Parses a string of semicolon- or comma-separated values taken from the
     * content of an EXDATE or RDATE vCalendar/iCalendar property and uses the
     * parsed values to fill a set of ExceptionToRecurrenceRule istances.
     *
     * @param scsv the property value, as a String object
     * @param rdate true if the string comes from an RDATE property, false if
     *              it comes from an EXDATE property
     * @param isAllDay true if the calendar is an allday
     *
     * @return a SortedSet of ExceptionToRecurrenceRule objects
     */
    private SortedSet<ExceptionToRecurrenceRule> getExceptionsAsSet(String scsv, boolean rdate, boolean isAllDay) {

        SortedSet<ExceptionToRecurrenceRule> exceptions =
                new TreeSet<ExceptionToRecurrenceRule>();

        if (scsv == null || scsv.length() == 0) {
            return exceptions;
        }

        String[] tokens = scsv.split("[;,]");
        for (String token : tokens) {
            try {
                //
                // Trimming the given date because some devices send it with a
                // space at the beginning
                //
                if (isAllDay) {
                    exceptions.add(
                        new ExceptionToRecurrenceRule(
                                rdate,
                                handleConversionToLocalDate(token.trim(), dtStartTimeZone)
                        )
                    );
                } else {
                    exceptions.add(
                        new ExceptionToRecurrenceRule(
                                rdate,
                                handleConversionToUTCDate(token.trim(), dtStartTimeZone)
                        )
                    );
                }
            } catch (ConverterException e) {
                // Skips this one
            } catch (ParseException e) {
                // Skips this one
            }
        }
        return exceptions;
    }

    private static String find(Map where, String what, boolean zeroIfTrouble) {
        try {
            return (String) ((List) where.get(what)).get(0);
        } catch (Exception e) {
            if (zeroIfTrouble) {
                return ZERO;
            }
            else {
                return null;
            }
        }
    }

    /**
     * It uses the DTSTART's time zone.
     */
    private void fixGenericDateProperty(Property property) throws Exception {
        if (property == null ||
            property.getPropertyValue() == null ||
            "".equals(property.getPropertyValueAsString()) ) {
            return ;
        }
        String value = property.getPropertyValueAsString();
        property.setPropertyValue(handleConversionToUTCDate(value, dtStartTimeZone));
    }

    private void fixDates(CalendarContent cc, boolean isEvent) throws Exception {

        if (isEvent) {
            fixGenericDateProperty(((Event)cc).getReplyTime());
        } else {
            fixGenericDateProperty(((Task)cc).getDateCompleted());
        }

        if (cc.getDtStart() == null) {
            cc.setDtStart(new PropertyWithTimeZone());
        }
        if (cc.getDtEnd() == null) {
            cc.setDtEnd(new PropertyWithTimeZone());
        }
        if (cc.getDuration() == null) {
            cc.setDuration(new Property());
        }

        String dtstart   = cc.getDtStart().getPropertyValueAsString() ;
        String dtend     = cc.getDtEnd().getPropertyValueAsString()   ;
        String duration  = cc.getDuration().getPropertyValueAsString();

        try{
            cc.setAllDay(Boolean.FALSE);

            //
            // Check if the event is an AllDay event
            // (yyyyMMdd or yyyy-MM-dd)
            //
            if (TimeUtils.isInAllDayFormat(dtstart)) {
                try{
                    dtstart = TimeUtils.convertDateFromTo(dtstart, TimeUtils.PATTERN_YYYY_MM_DD);
                } catch (java.text.ParseException e) {
                    throw new ConverterException("Error parsing date: " + e.getMessage());
                }
                cc.getDtStart().setPropertyValue(dtstart);
                cc.setAllDay(Boolean.TRUE);
            } else {
                dtstart = handleConversionToUTCDate(dtstart, dtStartTimeZone);
                cc.getDtStart().setPropertyValue(dtstart);
            }

            //
            // Check if the event is an AllDay event
            //
            if (TimeUtils.isInAllDayFormat(dtend)) {
                dtend = TimeUtils.convertDateFromTo(dtend, TimeUtils.PATTERN_YYYY_MM_DD);
                cc.setAllDay(Boolean.TRUE);
            } else {
                dtend = handleConversionToUTCDate(dtend, dtEndTimeZone);
            }

            //
            // Compute End Date by Start Date and Duration
            //
            dtend = TimeUtils.getDTEnd(dtstart, duration, dtend, null);
            cc.getDtEnd().setPropertyValue(dtend);

            //
            // If the event is an all day check if there is the end date:
            // 1) if end date is null then set it with start date value.
            // 2) if end date is not into yyyy-MM-dd or yyyyMMdd format then
            //    normalize it in yyyy-MM-dd format.
            //
            boolean startAllDay = TimeUtils.isInAllDayFormat(dtstart);
            boolean endAllDay   = TimeUtils.isInAllDayFormat(dtend);

            if (startAllDay) {
                if (dtend == null) {
                    dtend = dtstart;
                } else {
                    if (!endAllDay) {
                        try{
                            dtend = TimeUtils.convertDateFromTo(dtend, TimeUtils.PATTERN_YYYY_MM_DD);
                        } catch (java.text.ParseException e) {
                            throw new ConverterException("Error parsing date: " + e.getMessage());
                        }
                    }
                }
            }

            //
            //Note: with task the start date could be null. In this case, the
            //task should be handled like an allday. Pay attention because also
            //the due date could be null: in this case the task should not be
            //handled like an allday.
            //
            if (dtstart == null && dtend != null) {
                if (TimeUtils.isInAllDayFormat(dtend) ||
                    (dtend.endsWith("T000000" ) || dtend.endsWith("T235900")) ||
                    (dtend.endsWith("T000000Z") || dtend.endsWith("T235900Z"))) {

                    cc.setAllDay(Boolean.TRUE);
                }
            }

            //
            // We have to check if the dates are not in the DayFormat but are however
            // relative to an all day event.
            //
            if (!cc.isAllDay()) {
                boolean isAllDayEvent = false;

                //
                // Before to check the dates, we have to convert them in local format
                // in order to have 00:00:00 as time for the middle night
                //
                String tmpDateStart = TimeUtils.convertUTCDateToLocal(dtstart, dtStartTimeZone);
                String tmpDateEnd   = TimeUtils.convertUTCDateToLocal(dtend, dtEndTimeZone);

                //Android Sync Client can manage the X-FUNAMBOL-ALL-DAY field,
                //so, this check is only performed if this calendar is a vTask
                //If problem are encountered during vTask migration
                //it is possible that this check is responsible of transforming
                //the vTask to all day
                isAllDayEvent = !isEvent && TimeUtils.isAllDayEvent(
                        tmpDateStart,
                        tmpDateEnd
                );

                if (isAllDayEvent) {

                    //
                    // Convert the dates in DayFormat
                    //
                    try{
                        dtstart =
                            TimeUtils.convertDateFromTo(tmpDateStart, TimeUtils.PATTERN_YYYY_MM_DD);

                        dtend =
                            TimeUtils.convertDateFromTo(tmpDateEnd, TimeUtils.PATTERN_YYYY_MM_DD);
                    } catch (java.text.ParseException e) {
                        throw new ParseException("Error parsing date: " + e.getMessage(), e.getErrorOffset());
                    }

                    cc.getDtStart().setPropertyValue(dtstart);
                    cc.getDtEnd().setPropertyValue(dtend);

                    cc.setAllDay(Boolean.TRUE);
                } else {
                    //Android Sync Client can manage the X-FUNAMBOL-ALL-DAY field,
                    //so, this check is only performed if this calendar is a vTask
                    //If problems are encountered during vTask migration
                    //it is possible that this check is responsible of transforming
                    //the vTask to all day
                    if (!isEvent) {
                        isAllDayCheckingDuration(cc);
                    } else {
                        return;
                    }
                }
            }
        } catch (java.text.ParseException e) {
            throw new ConverterException("Error parsing date: " + e.getMessage());
        }
    }

    /**
     * Checks if the given dates are of an all day event checking if the duration
     * is a multiple of 24 hour.
     * The main problem is the end time is something like 23:59:59 so the difference
     * is not 24 hour but 24 hour - 1 second.
     * Another problem is about the day of the event because the date can be shifted
     * because we may not have the timezone of the device. BTW, in order to find
     * the day, we don't need the timezone but we need to know just if the timezone
     * is with an offset positive or negative. And to know it, we check the time
     * of the dtEnd.
     * <br/>If an all day event is detected, the properties are set in the given
     * calendar.
     *
     * KNOW ISSUE: this method fails with the timezone with an offset greater than
     * 12 hours
     *
     * @param cc
     * @throws Exception if an error occurs
     */
    public void isAllDayCheckingDuration(CalendarContent cc) throws Exception {

        String dtStart = cc.getDtStart().getPropertyValueAsString() ;
        String dtEnd   = cc.getDtEnd().getPropertyValueAsString()   ;

        if (dtStart == null || dtStart.length() == 0) {
            cc.setAllDay(Boolean.FALSE);
            return;

        }

        if (dtEnd == null || dtEnd.length() == 0) {
            cc.setAllDay(Boolean.FALSE);
            return;

        }
        //
        // We replace end time 5900Z with 5959Z in order to check just 5959Z
        //
        dtEnd = dtEnd.replaceAll("5900Z", "5959Z");

        //
        // The date start must end with 00Z
        //
        if (!dtStart.endsWith("00Z")) {
            cc.setAllDay(Boolean.FALSE);
            return;
        }

        SimpleDateFormat formatter = new SimpleDateFormat(TimeUtils.PATTERN_UTC);
        TimeZone tz = TimeZone.getTimeZone("UTC");
        formatter.setLenient(false);
        formatter.setTimeZone(tz);

        Date dateStart = formatter.parse(dtStart);
        Date dateEnd   = formatter.parse(dtEnd);

        long timeStart = dateStart.getTime();
        long timeEnd   = dateEnd.getTime();

        if (dtEnd.endsWith("5959Z")) {
            timeEnd = timeEnd + 1000; // we add a second because
                                      // we'll check
                                      // if the difference is 24H
                                      // (we have already checked if
                                      // the end time finished with
                                      // 5959Z)
        }

        long diff = timeEnd - timeStart;

        long sec = diff / 1000L;

        if (sec == 0) {
            cc.setAllDay(Boolean.FALSE);
            return;
        }

        if (sec % TimeUtils.SECOND_IN_A_DAY != 0) {
            cc.setAllDay(Boolean.FALSE);
            return;
        }

        cc.setAllDay(Boolean.TRUE);

        boolean isGMTPositive = false;

        java.util.Calendar calStart =
            java.util.Calendar.getInstance(TimeUtils.TIMEZONE_UTC);
        calStart.setTime(dateStart);
        calStart.setTimeZone(TimeUtils.TIMEZONE_UTC);

        java.util.Calendar calEnd =
            java.util.Calendar.getInstance(TimeUtils.TIMEZONE_UTC);
        calEnd.setTime(dateEnd);
        calEnd.setTimeZone(TimeUtils.TIMEZONE_UTC);

        int hourEnd   = calEnd.get(java.util.Calendar.HOUR_OF_DAY);
        int minuteEnd = calEnd.get(java.util.Calendar.MINUTE);

        int hourMinuteEnd = Integer.parseInt(String.valueOf(hourEnd) +
                                             String.valueOf(minuteEnd));

        if (hourMinuteEnd >= 1200 && hourMinuteEnd <= 2350) {
            //
            // Positive
            //
            isGMTPositive = true;
        } else {
            //
            // Negative
            //
            isGMTPositive = false;
        }

        String allDayStart = null;
        String allDayEnd = null;

        if (isGMTPositive) {
            //
            // If the gmt is with an offset positive, the dtStart of the event is
            // a day before so we have to add 1 day
            //
            calStart.add(java.util.Calendar.DATE, 1);

            allDayStart = TimeUtils.convertDateTo(calStart.getTime(),
                                                  TimeUtils.TIMEZONE_UTC,
                                                  TimeUtils.PATTERN_YYYY_MM_DD);
            allDayEnd = TimeUtils.convertDateTo(calEnd.getTime(),
                                                TimeUtils.TIMEZONE_UTC,
                                                TimeUtils.PATTERN_YYYY_MM_DD);

        } else {
            //
            // If the gmt is with an offset negative, the end of the event is
            // a day after so we have to subtract 1 day
            //
            calEnd.add(java.util.Calendar.DATE, -1);
            //
            // We have also to add 1 minute otherwise with the timezones with
            // offset = 0 we fail
            //
            calEnd.add(java.util.Calendar.MINUTE, 1);

            allDayStart = TimeUtils.convertDateTo(calStart.getTime(),
                                                  TimeUtils.TIMEZONE_UTC,
                                                  TimeUtils.PATTERN_YYYY_MM_DD);
            allDayEnd = TimeUtils.convertDateTo(calEnd.getTime(),
                                                TimeUtils.TIMEZONE_UTC,
                                                TimeUtils.PATTERN_YYYY_MM_DD);
        }

        String dtStartTimeZoneID;
        if (dtStartTimeZone == null) {
            dtStartTimeZoneID = null;
        } else {
            dtStartTimeZoneID = dtStartTimeZone.getID();
        }
        cc.setDtStart(new PropertyWithTimeZone(allDayStart, dtStartTimeZoneID));

        String dtEndTimeZoneID;
        if (dtEndTimeZone == null) {
            dtEndTimeZoneID = null;
        } else {
            dtEndTimeZoneID = dtEndTimeZone.getID();
        }
        cc.setDtEnd(new PropertyWithTimeZone(allDayEnd, dtEndTimeZoneID));
    }

    /**
     * Converts the importance in the vCalendar scale (one to three, where
     * three is the lowest priority) to the iCalendar-like scale (one to nine,
     * where nine is the lowest priority) according to RFC 2445.
     * NB: 3 is the lowest priority in many implementations of the vCalendar
     * standard, that in itself does not prescibe a fixed value for the lowest
     * priority level.
     *
     * @param oneToThree an int being 1 or 2 or 3
     * @return an int in the [1; 9] range
     * @throws NumberFormatException if the argument is out of range
     */
    private int importance13To19(int oneToThree) throws NumberFormatException {
        switch (oneToThree) {
            case 1:
                return 1;
            case 2:
                return 5;
            case 3:
                return 9;
            default:
                throw new NumberFormatException(); // will be caught
        }
    }

    /**
     * Converts the importance in the iCalendar scale (one to nine, where
     * nine is the lowest priority) to the vCalendar scale (one to three,
     * where three is the lowest priority) according to RFC 2445.
     * NB: 3 is the lowest priority in many implementations of the vCalendar
     * standard, that in itself does not prescibe a fixed value for the lowest
     * priority level.
     *
     * @param oneToNine an int in the [1; 9] range
     * @return an int being 1 or 2 or 3
     * @throws NumberFormatException if the argument is out of range
     */
    private int importance19To13(int oneToNine) throws NumberFormatException {
        switch (oneToNine) {
            case 1:
            case 2:
            case 3:
            case 4:
                return 1;
            case 5:
                return 2;
            case 6:
            case 7:
            case 8:
            case 9:
                return 3;
            default:
                throw new NumberFormatException(); // will be caught
        }
    }

    private Short busyStatusTo03(String busyStatus) {
        if (busyStatus == null) {
            return null;
        }

        if (BUSYSTATUS_FREE.equals(busyStatus)) {
            return BUSYSTATUS_OLFREE;
        }

        if (BUSYSTATUS_TENTATIVE.equals(busyStatus)) {
            return BUSYSTATUS_OLTENTATIVE;
        }

        if (BUSYSTATUS_BUSY.equals(busyStatus)) {
            return BUSYSTATUS_OLBUSY;
        }

        if (BUSYSTATUS_OOF.equals(busyStatus)) { // out of office
            return BUSYSTATUS_OLOOF;
        }

        return null;
    }

    private String busyStatusFrom03(Short zeroToThree) {
        if (zeroToThree == null) {
            return null; // undefined busy-status
        }

        switch(zeroToThree.shortValue()) {
            case BUSYSTATUS_OLFREE:
                return BUSYSTATUS_FREE;

            case BUSYSTATUS_OLTENTATIVE:
                return BUSYSTATUS_TENTATIVE;

            case BUSYSTATUS_OLBUSY:
                return BUSYSTATUS_BUSY;

            case BUSYSTATUS_OLOOF:
                return BUSYSTATUS_OOF;

            default:
                return null;
        }
    }

    private short accessClassTo03(String accessClass) {
        if (accessClass == null) {
            return SENSITIVITY_NORMAL; // default
        }

        if (accessClass.equals(CLASS_PUBLIC)) {
            return SENSITIVITY_NORMAL;
        }

        if (accessClass.equals(CLASS_PRIVATE)) {
            return SENSITIVITY_PRIVATE;
        }

        if (accessClass.equals(CLASS_CONFIDENTIAL)) {
            return SENSITIVITY_CONFIDENTIAL;
        }

        return SENSITIVITY_PERSONAL; // custom value
    }

    private String accessClassFrom03(Short zeroToThree) {

        if (zeroToThree == null) {
            return CLASS_PUBLIC;
        }

        switch(zeroToThree.shortValue()) {
            case SENSITIVITY_PRIVATE:
                return CLASS_PRIVATE;

            case SENSITIVITY_CONFIDENTIAL:
                return CLASS_CONFIDENTIAL;

            case SENSITIVITY_PERSONAL:
                return CLASS_CUSTOM;

            case SENSITIVITY_NORMAL:
            default:
                return CLASS_PUBLIC;
        }
    }

    /**
     * Converts the given aalarm string in a Reminder object
     * @param dtStart the event's start date
     * @param aalarm the aalarm string
     * @return the Reminder object built according to the given params
     */
    private Reminder convertAAlarmToReminder(boolean isAllday,
                                             Property dtStart,
                                             String aalarm) {

        if (aalarm == null) {
            return null;
        }
        Reminder reminder = new Reminder();
        reminder.setActive(false);

        //
        // Splits aalarm considering the eventual spaces before or after ';'
        // like part of the token to search: this because some phones send the
        // values of the AALARM property with space at the beginning of the
        // value.
        // For example
        // AALARM;TYPE=WAVE;VALUE=URL:20070415T235900; ; ; file:///mmedia/taps.wav
        //
        String[] values = aalarm.split("( )*;( )*");
        int cont = 0;
        for (String value: values) {

            switch (cont++) {
                case 0:
                    //
                    // The first token is the date
                    //
                    if (value == null || "".equals(value)) {
                        // The date is empty
                        break;
                    }
                    try {

                        setAlarmTimeAndMinutes(reminder,
                                               value   ,
                                               dtStart ,
                                               isAllday);

                    } catch (Exception e) {
                        //
                        // Something went wrong
                        //
                        reminder.setActive(false);
                        return reminder;
                    }
                    break;

                case 1:
                    //
                    // The second token is the duration
                    //
                    if (value == null || "".equals(value)) {
                        // The duration is empty
                        break;
                    }
                    reminder.setInterval(TimeUtils.getAlarmInterval(value));

                    break;

                case 2:
                    //
                    // The third token is the repeat count
                    //
                    if (value == null || "".equals(value)) {
                        // The repeat count is empty
                        break;
                    }
                    reminder.setRepeatCount(Integer.parseInt(value));

                    break;
                case 3:
                    //
                    // The fourth token is the sound file
                    //
                    if (value == null || "".equals(value)) {
                        // The sound file is empty
                        break;
                    }
                    reminder.setSoundFile(value);

                    break;
                default:
                    return reminder;
            }
        }
        return reminder;
    }


    /**
     * Convert AALARM to Reminder object when the calendar is an all day event
     * and Timezone is not set and the start date is not null.
     *
     * @param originalDtstart start date before convertion
     * @param convertedDtstart start date after convertion
     * @param aalarm the aalarm string
     */
    private Reminder convertAAlarmToReminderBasedOnMinutes(
                                             String originalDtstart,
                                             String convertedDtstart,
                                             String aalarm) {

        if (aalarm == null) {
            return null;
        }
        Reminder reminder = new Reminder();
        reminder.setActive(false);

        //
        // Splits aalarm considering the eventual spaces before or after ';'
        // like part of the token to search: this because some phones send the
        // values of the AALARM property with space at the beginning of the
        // value.
        // For example
        // AALARM;TYPE=WAVE;VALUE=URL:20070415T235900; ; ; file:///mmedia/taps.wav
        //
        String[] values = aalarm.split("( )*;( )*");
        int cont = 0;
        for (String value: values) {

            switch (cont++) {
                case 0:
                    //
                    // The first token is the date
                    //
                    if (value == null || "".equals(value)) {
                        // The date is empty
                        break;
                    }
                    try {

                        //
                        // Uses start date converted to local time to calculate
                        // minutes
                        //
                        reminder.setMinutes(TimeUtils.getAlarmMinutes(
                                    originalDtstart,
                                    value,
                                    null)
                        );

                        //
                        // Uses original start date minus reminder minutes
                        // (calculated previously) to set the reminder time.
                        // In this way, the reminder time is calculated like
                        // relative moment to the timezone.
                        //

                        convertedDtstart =
                            TimeUtils.convertDateFromTo(convertedDtstart, TimeUtils.PATTERN_YYYY_MM_DD);

                        java.util.Calendar calAlarm =
                            java.util.Calendar.getInstance();
                        SimpleDateFormat formatter =
                            new SimpleDateFormat(TimeUtils.PATTERN_YYYY_MM_DD_HH_MM_SS);
                        calAlarm.setTime(
                            formatter.parse(convertedDtstart + " 00:00:00"));
                        calAlarm.add(java.util.Calendar.MINUTE,
                                     -reminder.getMinutes());

                        formatter =
                            new SimpleDateFormat(TimeUtils.PATTERN_UTC_WOZ);
                        Date dtAlarm = calAlarm.getTime();
                        reminder.setTime(formatter.format(dtAlarm));

                        reminder.setActive(true);

                    } catch (Exception e) {
                        //
                        // Something went wrong
                        //
                        reminder.setActive(false);
                        return reminder;
                    }
                    break;

                case 1:
                    //
                    // The second token is the duration
                    //
                    if (value == null || "".equals(value)) {
                        // The duration is empty
                        break;
                    }
                    reminder.setInterval(TimeUtils.getAlarmInterval(value));

                    break;

                case 2:
                    //
                    // The third token is the repeat count
                    //
                    if (value == null || "".equals(value)) {
                        // The repeat count is empty
                        break;
                    }
                    reminder.setRepeatCount(Integer.parseInt(value));

                    break;
                case 3:
                    //
                    // The fourth token is the sound file
                    //
                    if (value == null || "".equals(value)) {
                        // The sound file is empty
                        break;
                    }
                    reminder.setSoundFile(value);

                    break;
                default:
                    return reminder;
            }
        }
        return reminder;
    }

    /**
     * Extracts roughly a time interval large enough to contain the whole
     * event/task and, in case it's a recurrent one, all its occurrences.
     * To be used just with iCalendar (vCalendar 2.0) items.
     *
     * @param vcc the VCalendarContent object to be quickly parsed
     * @return an array of long integers, the first one being the lower end of
     *         the interval and the other one the upper end
     */
    public long[] extractInterval(VCalendarContent vcc) {

        String low = null;
        if (vcc.getProperty("DTSTART") != null) {
            low = vcc.getProperty("DTSTART").getValue();
        }
        if ((low == null) || ("".equals(low))) {
            if (vcc.getProperty("DTEND") != null) {
                low = vcc.getProperty("DTEND").getValue();
            }
        }
        if ((low == null) || ("".equals(low))) {
            if (vcc.getProperty("DUE") != null) {
                low = vcc.getProperty("DUE").getValue();
            }
        }
        long from, to;
        if ((low == null) || ("".equals(low))) {
            from = DEFAULT_FROM;
        } else {
            try {
                from = TimeUtils.getMidnightTime(low);
            } catch (ParseException e) {
                from = DEFAULT_FROM;
            }
        }

        String rrule;
        if (vcc.getProperty("RRULE") != null) {
            rrule = vcc.getProperty("RRULE").getValue();
            Pattern pattern = Pattern.compile(
                    "UNTIL=([0-9]{4}([\\-])?[0-1][0-9]([\\-])?[0-3][0-9])");
            Matcher matcher = pattern.matcher(rrule);
            if (matcher.find()) { // has an end date
                String high = matcher.group(1);
                try {
                    to = TimeUtils.getMidnightTime(high) + ONE_DAY;
                } catch (ParseException e) {
                    to = DEFAULT_TO;
                }
            } else { // has no end date
                pattern = Pattern.compile("COUNT=([0-9]+)");
                matcher = pattern.matcher(rrule);
                if (matcher.find()) { // has an occurrence limit
                    int count = Integer.parseInt(matcher.group(1));
                    pattern = Pattern.compile("FREQ=([A-Z]+LY)");
                    matcher = pattern.matcher(rrule);
                    if (matcher.find()) { // has a frequency
                        String freq = matcher.group(1);
                        int period = 0;
                        if (DAILY.equalsIgnoreCase(freq)) {
                            period = 1;
                        } else if (WEEKLY.equalsIgnoreCase(freq)) {
                            period = 7;
                        } else if (MONTHLY.equalsIgnoreCase(freq)) {
                            period = 31;
                        } else if (YEARLY.equalsIgnoreCase(freq)) {
                            period = 366;
                        }
                        if (period != 0) { // frequency was recognized
                            pattern = Pattern.compile("INTERVAL=([0-9]+)");
                            matcher = pattern.matcher(rrule);
                            if (matcher.find()) { // has an interval
                                period *= Integer.parseInt(matcher.group(1));
                            } // else, no problem
                            to = from + (period * count * ONE_DAY);
                        } else { // frequency was not recognized
                            to = DEFAULT_TO;
                        }
                    } else { // has no frequency
                        to = DEFAULT_TO;
                    }
                } else { // is unlimited
                    to = DEFAULT_TO_UNLIMITED;
                }
            }
            List<com.funambol.common.pim.model.model.Property> rdates =
                    vcc.getProperties("RDATE");
            for (com.funambol.common.pim.model.model.Property property : rdates) {
                String rdate = property.getValue();
                try {
                    long extra = TimeUtils.getMidnightTime(rdate) + ONE_DAY;
                    if (extra > to) {
                        to = extra;
                    }
                } catch (ParseException e) {
                    // Ignores this RDATE
                }
            }
        } else {
            to = DEFAULT_TO;
        }

        if (from > DEFAULT_FROM) {
           from = DEFAULT_FROM;
        }
        if (to < DEFAULT_TO) {
            to = DEFAULT_TO;
        }
        return new long[]{from, to};
    }
}
