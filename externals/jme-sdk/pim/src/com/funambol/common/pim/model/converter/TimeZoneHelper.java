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
package com.funambol.common.pim.model.converter;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.joda.time.DateTimeZone;

import com.funambol.common.pim.model.calendar.RecurrencePattern;
import com.funambol.common.pim.model.model.Property;
import com.funambol.common.pim.model.model.TzDaylightComponent;
import com.funambol.common.pim.model.model.TzStandardComponent;
import com.funambol.common.pim.model.model.VComponent;
import com.funambol.common.pim.model.model.VTimezone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements the time-zone conversions.
 *
 * @version $Id: TimeZoneHelper.java,v 1.7 2008-08-27 10:58:39 mauro Exp $
 */
public class TimeZoneHelper {

    private boolean cachedID = false;
    protected String id = null;

    private final DateFormat DF =
            new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
    private final DateFormat DF_NO_Z =
            new SimpleDateFormat("yyyyMMdd'T'HHmmss");

    private static final DecimalFormat HH = new DecimalFormat("+00;-00");
    private static final DecimalFormat MM = new DecimalFormat("00");
    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

    private static final long NINE_MONTHS = 23328000000L; // 270 days
    private static final long THREE_MONTHS  = 7776000000L; // 90 days

    private static String[] FAVORITE_TIME_ZONE_IDS = {
        "Etc/UTC",
        "Europe/Berlin",
        "Europe/London",
        "Europe/Moscow",
        "Europe/Istanbul",
        "America/Los_Angeles",
        "America/New_York",
        "America/Phoenix",
        "America/Denver",
        "Africa/Tunis",
        "Africa/Lagos",
        "Africa/Johannesburg",
        "Africa/Nairobi",
        "America/Mexico_City",
        "America/La_Paz",
        "America/Tijuana",
        "America/Buenos_Aires",
        "America/La_Rioja",
        "America/Port-au-Prince",
        "America/Sao_Paulo",
        "Asia/Tel_Aviv",
        "Asia/Bangkok",
        "Asia/Shanghai",
        "Asia/Dacca",
        "Asia/Phnom_Penh",
        "Asia/Riyadh",
        "Asia/Dubai",
        "Asia/Tokyo",
        "Asia/Tashkent",
        "Asia/Vladivostok",
        "Australia/Adelaide",
        "Australia/Brisbane",
        "Australia/Canberra",
        "Australia/Darwin",
        "Australia/Hobart",
        "Australia/Sydney",
        "Antarctica/South_Pole"
    };
    
    private Pattern OLSON_ID_PATTERN = Pattern.compile(
        "(Europe|A((meric)|(si)|(fric)|(ustrali)|(ntarctic))a|Pacific|Atlantic)"
        + "/[A-Z]([A-Z,a-z,_,',\\-])+"
        + "(/[A-Z]([A-Z,a-z,_,',\\-])+)?");

    //--------------------------------------------------------------- Properties

    private String name = null;
    private int basicOffset; // in milliseconds
    private List<TimeZoneTransition> transitions =
            new ArrayList<TimeZoneTransition>();

    private static long referenceTime = -1L;
    private final long REFERENCE_TIME = TimeZoneHelper.getReferenceTime();

    protected String getName() {
        return name;
    }
    
    /**
     * This setter is only for test purposes. Usually, the name is set by 
     * constructors.
     * 
     * @param name the new name to set
     */
    protected void setName(String name) {
        this.name = name;
    }

    protected int getBasicOffset() {
        return basicOffset;
    }

    protected List<TimeZoneTransition> getTransitions() {
        return transitions;
    }

    // No need for setters

    //------------------------------------------------------------- Constructors

    /**
     * Just creates an empty TimeZoneHelper. It's only for usage by subclasses.
     */
    protected TimeZoneHelper() {
        setFormattersToUTC();

        // Does nothing.
    }

    /**
     * Creates a new instance of TimeZoneHelper on the basis of the
     * information extracted from a vCalendar (1.0) item.
     *
     * @param tz the TZ property
     * @param daylightList a List containing all DAYLIGHT properties
     * @throws java.lang.Exception
     */
    public TimeZoneHelper(Property tz, List<Property> daylightList)
    throws Exception {
        setFormattersToUTC();
        this.name = ""; // vCalendar (1.0) has no time zone identifier because
                        // there's just one time zone per calendar item

        if ((tz == null)
           || (tz.getValue() == null)
           || (tz.getValue().length() == 0)) {
            throw new Exception("No TZ property");
        }

        basicOffset = parseOffset(tz.getValue());

        for (Property transition : daylightList) {

            if (transition.getValue() == null) {
                continue;
            }
            if (transition.getValue().startsWith("TRUE;")) {
                String[] daylight = transition.getValue().split(";");

                String summerOffsetString =
                        daylight[1].replaceAll("[\\+\\-:]", "") + "00";

                int summerOffset = 3600000 *
                        Integer.parseInt(summerOffsetString.substring(0, 2));
                summerOffset += 60000 *
                        Integer.parseInt(summerOffsetString.substring(2, 4));
                if (daylight[1].startsWith("-")) {
                    summerOffset = -summerOffset;
                }

                long summerStart = parseDateTime(daylight[2]);
                String summerTimeName;
                if (daylight.length >= 5) {
                    summerTimeName = daylight[4];
                } else {
                    summerTimeName = "";
                }
                TimeZoneTransition summerTime =
                        new TimeZoneTransition(summerOffset  ,
                                               summerStart   ,
                                               summerTimeName);

                long summerEnd = parseDateTime(daylight[3]);
                String standardTimeName;
                if (daylight.length >= 6) {
                    standardTimeName = daylight[5];
                } else {
                    standardTimeName = "";
                }
                TimeZoneTransition standardTime =
                        new TimeZoneTransition(basicOffset     ,
                                               summerEnd       ,
                                               standardTimeName);

                transitions.add(summerTime);
                transitions.add(standardTime);
            }
        }
    }

    /**
     * Creates a new instance of TimeZoneHelper on the basis of the
     * information extracted from an iCalendar (vCalendar 2.0) item.
     *
     * @param vTimeZone
     * @param from the start of the relevant time interval for the generation of
     *             transitions (an istant expressed as a long)
     * @param to the end of the relevant time interval for the generation of
     *           transitions (an istant expressed as a long)
     * @throws java.lang.Exception
     */
    public TimeZoneHelper(VTimezone vTimeZone, long from, long to)
    throws Exception {
        setFormattersToUTC();
        Property tzID = vTimeZone.getProperty("TZID");
        if (tzID != null) {
            this.name = tzID.getValue();                    
            
            // Try and skip the parsing by using just the TZID:
            String extracted = extractID(name);
            if (extracted != null) {
                cacheID(extracted);
                processID(extracted, from, to);
                return;
            }

            List<VComponent> standardTimeRules =
                    vTimeZone.getComponents("STANDARD");
            List<VComponent> summerTimeRules =
                    vTimeZone.getComponents("DAYLIGHT");

            String standardTimeOffset;
            if (standardTimeRules.isEmpty()) {
                if (summerTimeRules.isEmpty()) {
                    throw new Exception("Empty VTIMEZONE");
                } else {
                    standardTimeOffset = summerTimeRules.get(0)
                                                        .getProperty("TZOFFSETFROM")
                                                        .getValue();
                }
            } else {
                standardTimeOffset = standardTimeRules.get(0)
                                                      .getProperty("TZOFFSETTO")
                                                      .getValue();
            }
            basicOffset = parseOffset(standardTimeOffset);

            for (VComponent standardTimeRule : standardTimeRules) {
                     addTransitions(standardTimeRule, from, to);
                }
            for (VComponent summerTimeRule : summerTimeRules) {
                     addTransitions(summerTimeRule, from, to);
                }
           Collections.sort(transitions);

        } else {
            this.name = ""; // This should not happen!
        }
    }

    /**
     * Creates a new instance of TimeZoneHelper on the basis of a
     * zoneinfo (Olson database) ID.
     *
     * @param id the time zone ID according to the zoneinfo (Olson) database
     * @param from the start of the relevant time interval for the generation of
     *             transitions (an istant expressed as a long)
     * @param to the end of the relevant time interval for the generation of
     *           transitions (an istant expressed as a long)
     */
    public TimeZoneHelper(String id, long from, long to) {
        setFormattersToUTC();
        cacheID(id);
        processID(id, from, to);
    }

    /**
     * Extract time-zone information from a zoneinfo (Olson database) ID and
     * saves them in the TimeZoneHelper fields.
     *
     * @param id the time zone ID according to the zoneinfo (Olson) database
     * @param from the start of the relevant time interval for the generation of
     *             transitions (an istant expressed as a long)
     * @param to the end of the relevant time interval for the generation of
     *           transitions (an istant expressed as a long)
     */
    protected void processID(String id, long from, long to) {

        DateTimeZone tz = DateTimeZone.forID(id);
        if (name == null) { // The name could have been set already using TZID
                            // and in this case it is important not to change it
            name = id; // The Olson ID is perfect as a unique name
        }
        basicOffset = tz.getStandardOffset(from);
        transitions.clear();

        if (!tz.isFixed()) {

            long oldFrom = from;
            from = fixFrom(tz, basicOffset, oldFrom);
            
            //@todo Consider case when to go beyond last transition (cycle 
            //could become endless)
            while (tz.getStandardOffset(to) != tz.getOffset(to)) {
                to = tz.nextTransition(to);
            }
            
            while ((from <= to) && (oldFrom != from)) {
                transitions.add(
                    new TimeZoneTransition(tz.getOffset(from),
                                           from              ,
                                           id                ));
                oldFrom = from;
                from = tz.nextTransition(oldFrom);
            }
        }
    }

    //----------------------------------------------------------- Public methods

    /**
     * Gets an Olson ID corresponding to the transitions and offsets saved.
     * First it looks for a cached ID. If it is not found, it looks for a
     * matching ID among the favorite ones. Otherwise it looks for it among all
     * available IDs with the same basic offset.
     * 
     * @return a string containing the Olson ID or null if no matching ID is 
     *         found
     */
    public String toID() {

        if (cachedID) {
            return id;
        }

        for (String idGuess : FAVORITE_TIME_ZONE_IDS) {
            if (matchesID(idGuess)) {
                return cacheID(idGuess);
            }
        }

        for (String idGuess : TimeZone.getAvailableIDs(getBasicOffset())) {
            if (matchesID(idGuess)) {
                return cacheID(idGuess);
            }
        }
        return cacheID(null); // No matching time zone found
    }

    /**
     * Gets an Olson ID corresponding to the information saved and a suggestion.
     * First it looks for a cached ID. If it is not found, it checks if the 
     * saved name simply contains an Olson ID. If it does, it will be returned 
     * as the result without further investigation. If that is not the case, the
     * suggested ID is checked against the transitions and offsets saved. If 
     * this also fails, it looks for a matching ID among the favorite ones. 
     * Otherwise it looks for it among all available IDs with the same basic 
     * offset.
     * 
     * @param suggested the suggested ID (as a string)
     * @return a string containing the Olson ID or null if no matching ID is 
     *         found
     */
    public String toID(String suggested) {
        
        if (cachedID) {
            return id;
        }
        String extractedID = extractID(name);
        if (extractedID != null) {
            return cacheID(extractedID);
        }
        if ((suggested != null) && (matchesID(suggested))) {
            return cacheID(suggested);
        }
        return toID();
    }

    /**
     * Gets an Olson ID corresponding to the information saved and a suggestion.
     * First it looks for a cached ID. If it is not found, it checks if the 
     * saved name simply contains an Olson ID. If it does, it will be returned 
     * as the result without further investigation. If that is not the case, the
     * ID of the suggested time zone is checked against the transitions and 
     * offsets saved. If this also fails, it looks for a matching ID among the 
     * favorite ones. Otherwise it looks for it among all available IDs with the
     * same basic offset.
     * 
     * @param suggested the suggested time zone (as a TimeZone object)
     * @return a string containing the Olson ID or null if no matching ID is 
     *         found
     */
    public String toID(TimeZone suggested) {
        
        if (suggested != null) {
            return toID(suggested.getID());
        }
        return toID((String) null);
    }

    public Property getTZ() {
        return new Property("TZ", formatOffset(getBasicOffset()));
    }

    public List<Property> getDaylightList() {

        List<Property> properties =
                new ArrayList<Property>(getTransitions().size() / 2);

        if (getTransitions().size() == 0) {
            properties.add(
                    new Property("DAYLIGHT",
                                 "FALSE")
                    );
        }

        //@todo Check the case with an odd number of transitions
        int previousOffset = getBasicOffset();
        for (int i = 0; i < getTransitions().size() - 1; i += 2) {

            TimeZoneTransition transitionToSummerTime   = getTransitions().get(i);
            TimeZoneTransition transitionToStandardTime = getTransitions().get(i + 1);
            Date forth = new Date(transitionToSummerTime.getTime()
                    + previousOffset);
            Date back = new Date(transitionToStandardTime.getTime()
                    + transitionToSummerTime.getOffset());
            previousOffset = transitionToStandardTime.getOffset();

            StringBuffer buffer = new StringBuffer("TRUE;");
            buffer.append(formatOffset(transitionToSummerTime.getOffset()))
                  .append(';').append(DF_NO_Z.format(forth))
                  .append(';').append(DF_NO_Z.format(back))
                  .append(';').append(transitionToStandardTime.getName())
                  .append(';').append(transitionToSummerTime.getName());
            properties.add(new Property("DAYLIGHT", buffer.toString()));
        }

        return properties;
    }

    public List<Property> getXVCalendarProperties() {
        List<Property> properties = new ArrayList<Property>();
        properties.add(getTZ());
        properties.addAll(getDaylightList());

        return properties;
    }

    public VTimezone getVTimezone() {

        return toVTimezone(getICalendarTransitions(), getName(), getBasicOffset());
    }

    //-------------------------------------------------------- Protected methods

    protected List<ICalendarTimeZoneTransition> getICalendarTransitions() {

        List<ICalendarTimeZoneTransition> iCalendarTransitions;

        if (getTransitions().isEmpty()) {
            iCalendarTransitions =
                new ArrayList<ICalendarTimeZoneTransition>(1);
            iCalendarTransitions.add(
                    new ICalendarTimeZoneTransition(getName(), getBasicOffset()));
            return iCalendarTransitions;
        }

        iCalendarTransitions =
                new ArrayList<ICalendarTimeZoneTransition>(getTransitions().size());

        int previousOffset = getBasicOffset();
        for (TimeZoneTransition transition : getTransitions()) {
            iCalendarTransitions.add(
                    new ICalendarTimeZoneTransition(transition, previousOffset));
            previousOffset = transition.getOffset();
        }

        return iCalendarTransitions;
    }

    protected String cacheID(String id) {
        cachedID = true;
        this.id = id;
        return id;
    }

    protected static VTimezone toVTimezone(
            List<ICalendarTimeZoneTransition> iCalendarTransitions,
            String id,
            int basicOffset) {

        VTimezone vtz = new VTimezone();
        vtz.addProperty("TZID", id);
        TzDaylightComponent summerTimeRDates = null;
        TzStandardComponent standardTimeRDates = null;
        String standardTimeOffset = formatOffset(basicOffset);

        // Visits all transitions in cronological order
        for (int i = 0; i < iCalendarTransitions.size();) {

            // If it's the last transition, or it's a transition that is not
            // part of the standard/day-light time series, the special case must
            // be separately treated
            if ((i == iCalendarTransitions.size() - 1) ||
                (!areHalfYearFar(iCalendarTransitions.get(i    ).getTime(),
                                 iCalendarTransitions.get(i + 1).getTime()))) {

                // "Burns" components that may be present in the buffer
                if (summerTimeRDates != null) {
                    vtz.addComponent(summerTimeRDates);
                    vtz.addComponent(standardTimeRDates);
                    summerTimeRDates = null;
                    standardTimeRDates = null;
                }

                // Creates a new STANDARD component of the RDATE kind
                TzStandardComponent specialRDate = new TzStandardComponent();
                String specialCaseTime =
                        iCalendarTransitions.get(i).getTimeISO1861();
                specialRDate.addProperty("DTSTART"     , specialCaseTime   );
                specialRDate.addProperty("RDATE"       , specialCaseTime   );
                specialRDate.addProperty("TZOFFSETFROM", standardTimeOffset);
                standardTimeOffset = // It needs be updated
                        formatOffset(iCalendarTransitions.get(i).getOffset());
                specialRDate.addProperty("TZOFFSETTO"  , standardTimeOffset);
                specialRDate.addProperty(
                        "TZNAME",
                        iCalendarTransitions.get(i).getName());
                vtz.addComponent(specialRDate); // Burns it
                i++; // Moves on to the next transition
                continue;
            }

            String lastOffset = standardTimeOffset;
            String summerTimeOffset =
                    formatOffset(iCalendarTransitions.get(i).getOffset());
            standardTimeOffset =
                    formatOffset(iCalendarTransitions.get(i + 1).getOffset());
            String summerTimeStart =
                    iCalendarTransitions.get(i).getTimeISO1861();
            String standardTimeStart =
                    iCalendarTransitions.get(i + 1).getTimeISO1861();
            ICalendarTimeZoneTransition summerTimeClusterStart =
                    iCalendarTransitions.get(i);
            ICalendarTimeZoneTransition standardTimeClusterStart =
                    iCalendarTransitions.get(i + 1);
            int j; // Summer-time starts, backward instance count
            int k; // Summer-time starts, forward instance count
            int l; // Summer-time ends, backward instance count
            int m; // Summer-time ends, forward instance count
            for (j = i + 2; j < iCalendarTransitions.size(); j += 2) {
                ICalendarTimeZoneTransition clusterMember =
                    iCalendarTransitions.get(j);
                if (!summerTimeClusterStart.matchesRecurrence(clusterMember, true)) {
                    break;
                }
            }
            for (k = i + 2; k < iCalendarTransitions.size(); k += 2) {
                ICalendarTimeZoneTransition clusterMember =
                    iCalendarTransitions.get(k);
                if (!summerTimeClusterStart.matchesRecurrence(clusterMember, false)) {
                    break;
                }
            }
            for (l = i + 3; l < iCalendarTransitions.size(); l += 2) {
                ICalendarTimeZoneTransition clusterMember =
                    iCalendarTransitions.get(l);
                if (!standardTimeClusterStart.matchesRecurrence(clusterMember, true)) {
                    break;
                }
            }
            for (m = i + 3; m < iCalendarTransitions.size(); m += 2) {
                ICalendarTimeZoneTransition clusterMember =
                    iCalendarTransitions.get(m);
                if (!standardTimeClusterStart.matchesRecurrence(clusterMember, false)) {
                    break;
                }
            }
            boolean backwardInstanceCountForStarts = true;
            boolean backwardInstanceCountForEnds = true;
            if (k > j) { // counting istances in the forward direction makes a
                                              // longer summer-time-start series
                j = k; // j is now the longest series of summer-time starts
                backwardInstanceCountForStarts = false;
            }
            if (m > l) { // counting istances in the forward direction makes a
                                                // longer summer-time-end series
                l = m; // l is now the longest series of summer-time ends
                backwardInstanceCountForEnds = false;
            }
            j -= 2; // Compensates for the end condition of the for cycle above
            l -= 2; // Compensates for the end condition of the for cycle above
            if (l > j + 1) {
                l = j + 1; // l is now the best acceptable end for a
                                                    // combined start-end series
            } else {
                j = l - 1;
            }
            // At this point, l + 1 = j
            
            if (l > i + 1) { // more than one year: there's a recurrence
                if (summerTimeRDates != null) {
                    vtz.addComponent(summerTimeRDates);
                    vtz.addComponent(standardTimeRDates);
                }

                // Create a new DAYLIGHT component
                summerTimeRDates = new TzDaylightComponent();
                summerTimeRDates.addProperty("DTSTART"     , summerTimeStart           );
                StringBuffer summerTimeRRule = new StringBuffer("FREQ=YEARLY;INTERVAL=1;BYDAY=");
                if (backwardInstanceCountForStarts) {
                    summerTimeRRule.append("-1");
                } else {
                    summerTimeRRule.append('+').append(summerTimeClusterStart.getInstance());
                }
                summerTimeRRule.append(getDayOfWeekAbbreviation(summerTimeClusterStart.getDayOfWeek()))
                               .append(";BYMONTH=")
                               .append(summerTimeClusterStart.getMonth() + 1); // Jan must be 1
                if (j < iCalendarTransitions.size() - 2) {
                    summerTimeRRule.append(";UNTIL=")
                                   .append(iCalendarTransitions.get(j).getTimeISO1861());
                }
                summerTimeRDates.addProperty("RRULE"       , summerTimeRRule.toString());
                summerTimeRDates.addProperty("TZOFFSETFROM", lastOffset                );
                summerTimeRDates.addProperty("TZOFFSETTO"  , summerTimeOffset          );
                summerTimeRDates.addProperty(
                        "TZNAME",
                        iCalendarTransitions.get(i).getName());

                // Create a new STANDARD component
                standardTimeRDates = new TzStandardComponent();
                standardTimeRDates.addProperty("DTSTART"     , standardTimeStart );
                StringBuffer standardTimeRRule = new StringBuffer("FREQ=YEARLY;INTERVAL=1;BYDAY=");
                if (backwardInstanceCountForEnds) {
                    standardTimeRRule.append("-1");
                } else {
                    standardTimeRRule.append('+').append(standardTimeClusterStart.getInstance());
                }
                standardTimeRRule.append(getDayOfWeekAbbreviation(standardTimeClusterStart.getDayOfWeek()))
                                 .append(";BYMONTH=")
                                 .append(standardTimeClusterStart.getMonth() + 1); // Jan must be 1
                if (l < iCalendarTransitions.size() - 1) {
                    standardTimeRRule.append(";UNTIL=")
                                     .append(iCalendarTransitions.get(l).getTimeISO1861());
                }

                standardTimeRDates.addProperty("RRULE"       , standardTimeRRule.toString());
                standardTimeRDates.addProperty("TZOFFSETFROM", summerTimeOffset  );
                standardTimeRDates.addProperty("TZOFFSETTO"  , standardTimeOffset);
                standardTimeRDates.addProperty(
                        "TZNAME",
                        iCalendarTransitions.get(i + 1).getName());

                vtz.addComponent(summerTimeRDates);
                vtz.addComponent(standardTimeRDates);
                summerTimeRDates = null;
                standardTimeRDates = null;
                i = j; // Increases the counter to jump beyond the recurrence
            } else { // just one year: i, i+1 are transitions of the RDATE kind
                if (summerTimeRDates == null) {
                    // Create a new DAYLIGHT component
                    summerTimeRDates = new TzDaylightComponent();
                    summerTimeRDates.addProperty("DTSTART"     , summerTimeStart );
                    summerTimeRDates.addProperty("RDATE"       , summerTimeStart );
                    summerTimeRDates.addProperty("TZOFFSETFROM", lastOffset      );
                    summerTimeRDates.addProperty("TZOFFSETTO"  , summerTimeOffset);
                    summerTimeRDates.addProperty(
                            "TZNAME",
                            iCalendarTransitions.get(i).getName());
                    // Create a new STANDARD component
                    standardTimeRDates = new TzStandardComponent();
                    standardTimeRDates.addProperty("DTSTART"     , standardTimeStart );
                    standardTimeRDates.addProperty("RDATE"       , standardTimeStart );
                    standardTimeRDates.addProperty("TZOFFSETFROM", summerTimeOffset  );
                    standardTimeRDates.addProperty("TZOFFSETTO"  , standardTimeOffset);
                    standardTimeRDates.addProperty(
                            "TZNAME",
                            iCalendarTransitions.get(i + 1).getName());
                } else {
                    Property rdate = summerTimeRDates.getProperty("RDATE");
                    rdate.setValue(rdate.getValue() + ';' + summerTimeStart);
                    summerTimeRDates.setProperty(rdate);
                    rdate = standardTimeRDates.getProperty("RDATE");
                    rdate.setValue(rdate.getValue() + ';' + standardTimeStart);
                    standardTimeRDates.setProperty(rdate);
                }
            }
            i += 2;
        }
        if (summerTimeRDates != null) {
            vtz.addComponent(summerTimeRDates);
            vtz.addComponent(standardTimeRDates);
            summerTimeRDates = null;
            standardTimeRDates = null;
        }
        return vtz;

    }

    //---------------------------------------------------------- Private methods

    private boolean matchesID(String idToCheck) {

        DateTimeZone tz;

        try {
            tz = DateTimeZone.forID(idToCheck);
        } catch (IllegalArgumentException e) { // the ID is not recognized
            return false;
        }

        if (getTransitions().size() == 0) { // No transitions
            if (tz.getStandardOffset(REFERENCE_TIME) != basicOffset) {
                    return false; // Offsets don't match: wrong guess
            }
            if (tz.isFixed() ||
               (REFERENCE_TIME == tz.nextTransition(REFERENCE_TIME))) {
                return true; // A right fixed or currently-fixed time zone
                             // has been found
            }
            return false; // Wrong guess
        }

        long t = getTransitions().get(0).getTime() - 1;
        if (tz.getStandardOffset(t) != basicOffset) {
                return false; // Wrong guess
        }

        for (TimeZoneTransition transition : getTransitions()) {
            t = tz.nextTransition(t);
            if (!isClose(t, transition.getTime())) {
                return false; // Wrong guess
            }
            if (tz.getOffset(t) != transition.getOffset()) {
                return false; // Wrong guess
            }
        }
        return true; // A right non-fixed time zone has been found

    }

    private static String formatOffset(int offset) {

        int offsetHours =   offset / 3600000;
        int offsetMinutes = (offset / 60000) % 60;

        return (HH.format(offsetHours) + MM.format(offsetMinutes));
    }

    /**
     * Parses offset string value that could be in the format prefix + or -, or
     * with semicolon +03:00
     *
     * @param text the offset string value
     * @return the offset int value
     */
    private static int parseOffset(String text)
    throws Exception {
        int offset;
        try {
            String offsetString = text.replaceAll("[\\+\\-\\:]", "") + "00";
            offset = 3600000 * Integer.parseInt(offsetString.substring(0, 2));
            offset += 60000 * Integer.parseInt(offsetString.substring(2, 4));
            if (text.startsWith("-")) {
                return -offset;
            }
            return offset;
        } catch (Exception e) {
            throw new Exception("Wrong offset format");
        }
    }

    private long parseDateTime(String dateTime)
    throws ParseException {

        if (!dateTime.endsWith("Z")) {
            return DF_NO_Z.parse(dateTime).getTime() - getBasicOffset();
        }
        return DF.parse(dateTime).getTime();
    }

    private static boolean isClose(long t1, long t2) {
        if (t1 == t2) {
            return true;
        }
        long difference = t2 - t1;
        if ((difference <= 3600000) && (difference >= -3600000)) {
            return true;
        }
        return false;
    }

    private void addTransitions(VComponent timeRule, long from, long to)
    throws Exception {

        int offset;
        int previousOffset;
        String start;
        long startTime;
        long time;
        String transitionName;

        Property tzName       = timeRule.getProperty("TZNAME");
        Property tzOffsetFrom = timeRule.getProperty("TZOFFSETFROM");
        Property tzOffsetTo   = timeRule.getProperty("TZOFFSETTO");
        Property tzDtStart    = timeRule.getProperty("DTSTART");
        Property tzRRule      = timeRule.getProperty("RRULE");
        Property tzRDate      = timeRule.getProperty("RDATE");

        if (tzDtStart != null) {
            start = tzDtStart.getValue();
            startTime = parseDateTime(start);
        } else {
            throw new Exception("Required property DTSTART (of a time zone) is missing");
        }
        if (tzOffsetTo != null) {
            offset = parseOffset(tzOffsetTo.getValue());
        } else {
            throw new Exception("Required property TZOFFSETTO is missing");
        }
        if (tzOffsetFrom != null) {
            previousOffset = parseOffset(tzOffsetFrom.getValue());
        } else {
            throw new Exception("Required property TZOFFSETFROM is missing");
        }
        if (tzName != null) {
            transitionName = tzName.getValue();
        } else {
            transitionName = "";
        }

        if (tzRDate != null) {
            String[] rDates = tzRDate.getValue().split(",");
            for (String rDate : rDates) {
                time = parseDateTime(rDate);
                transitions.add(new TimeZoneTransition(offset        ,
                                                       time          ,
                                                       transitionName));
            }
        }

        if (tzRRule != null) {
            RecurrencePattern rrule =
                    VCalendarContentConverter.getRecurrencePattern(start,
                                                                   null,
                                                                   tzRRule.getValue(),
                                                                   null, // as of specs
                                                                   false); // iCalendar
            if (((rrule.getTypeId() == RecurrencePattern.TYPE_MONTH_NTH) &&
                 (rrule.getInterval() == 12))
                 ||
                ((rrule.getTypeId() == RecurrencePattern.TYPE_YEAR_NTH) &&
                 (rrule.getInterval() == 1))
               ) { // yearly

                int dayOfWeek = getDayOfWeekFromMask(rrule.getDayOfWeekMask());
                if (dayOfWeek > 0) { // one day
                    TimeZone fixed = TimeZone.getTimeZone("UTC");
                    fixed.setRawOffset(previousOffset);
                    Calendar finder = new GregorianCalendar(fixed);
                    finder.setTimeInMillis(startTime); // Sets hour and minutes
                    int hh = finder.get(Calendar.HOUR_OF_DAY);
                    int mm = finder.get(Calendar.MINUTE);
                    int m = rrule.getMonthOfYear() - 1; // Yes, it works
                    int yearStart = year(startTime);
                    int yearFrom = (startTime > from) ?
                                   yearStart          :
                                   year(from)         ;
                    int yearTo = year(to);
                    if (rrule.isNoEndDate()) {
                        int count = rrule.getOccurrences();
                        int yearRecurrenceEnd;
                        if (count != -1) {
                            yearRecurrenceEnd = yearStart + count - 1;
                            if (yearRecurrenceEnd < yearTo) {
                               yearTo = yearRecurrenceEnd;
                            }
                        }
                    } else {
                        try {
                            int yearRecurrenceEnd =
                                    year(rrule.getEndDatePattern());
                            if (yearRecurrenceEnd < yearTo) {
                               yearTo = yearRecurrenceEnd;
                            }
                        } catch (ParseException e) {
                        // Ignores the UNTIL part
                        }
                    }
                    for (int y = yearFrom; y <= yearTo; y++) {
                        finder.clear();
                        finder.set(Calendar.YEAR, y);
                        finder.set(Calendar.MONTH, m);
                        finder.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                        finder.set(Calendar.DAY_OF_WEEK_IN_MONTH,
                                rrule.getInstance());
                        finder.set(Calendar.HOUR_OF_DAY, hh);
                        finder.set(Calendar.MINUTE, mm);
                        long transitionTime = finder.getTimeInMillis()
                                - (previousOffset - getBasicOffset());
                        transitions.add(
                                new TimeZoneTransition(offset        ,
                                                       transitionTime,
                                                       transitionName));
                    }
                }
            }
        }
    }

    protected static int year(long time) {
        final Calendar FINDER = new GregorianCalendar(UTC_TIME_ZONE);
        FINDER.setTimeInMillis(time);
        return FINDER.get(Calendar.YEAR);
    }

    protected static int year(String time)
    throws ParseException {

        String year = time.substring(0, 4);
        return Integer.parseInt(year);
    }

    private static int getDayOfWeekFromMask(short mask) {
        switch (mask) {
            case RecurrencePattern.DAY_OF_WEEK_SUNDAY:
                return Calendar.SUNDAY;
            case RecurrencePattern.DAY_OF_WEEK_MONDAY:
                return Calendar.MONDAY;
            case RecurrencePattern.DAY_OF_WEEK_TUESDAY:
                return Calendar.TUESDAY;
            case RecurrencePattern.DAY_OF_WEEK_WEDNESDAY:
                return Calendar.WEDNESDAY;
            case RecurrencePattern.DAY_OF_WEEK_THURSDAY:
                return Calendar.THURSDAY;
            case RecurrencePattern.DAY_OF_WEEK_FRIDAY:
                return Calendar.FRIDAY;
            case RecurrencePattern.DAY_OF_WEEK_SATURDAY:
                return Calendar.SATURDAY;
            case 0: // empty mask
                return 0;
            default: // several days
                return -1;
        }

    }

    private static String getDayOfWeekAbbreviation(int day) {
        switch (day) {
            case java.util.Calendar.SUNDAY:
                return "SU";
            case java.util.Calendar.SATURDAY:
                return "SA";
            case java.util.Calendar.FRIDAY:
                return "FR";
            case java.util.Calendar.THURSDAY:
                return "TH";
            case java.util.Calendar.WEDNESDAY:
                return "WE";
            case java.util.Calendar.TUESDAY:
                return "TU";
            case java.util.Calendar.MONDAY:
                return "MO";
            default: // empty mask or several days
                return null;
        }
    }

    public void clearCachedID() {
        cachedID = false;
    }

    public static long getReferenceTime() {
        if (referenceTime >= 0) {
            return referenceTime;
        } else {
            return System.currentTimeMillis();
        }
    }

    public static synchronized void setReferenceTime(long time) {
        if (time < 0) {
            referenceTime = -1;
        } else {
            referenceTime = time;
        }
    }

    private static boolean areHalfYearFar(long time0, long time1) {
        return  ((time1 - time0 < NINE_MONTHS ) &&
                 (time1 - time0 > THREE_MONTHS));
    }

    private void setFormattersToUTC() {
        DF.setTimeZone(TimeZone.getTimeZone("UTC"));
        DF_NO_Z.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Appends a opening XML tag.
     */
    private StringBuffer openXMLTag(StringBuffer buffer, String tag) {
        return buffer.append('<').append(tag).append('>');
    }

    /**
     * Appends a closing XML tag.
     */
    private StringBuffer closeXMLTag(StringBuffer buffer, String tag) {
        return buffer.append("</").append(tag).append('>');
    }

    /**
     * Appends an XML node with the given content.
     */
    private StringBuffer addXMLNode(StringBuffer buffer, String tag, String content) {

        if (content == null || "null".equals(content)) {
            return buffer.append('<').append(tag).append("/>");
        }
        return closeXMLTag(openXMLTag(buffer, tag).append(content), tag);
    }
    
    /**
     * Looks for a substring that corresponds to an Olson ID.
     * 
     * @param label the string to search through
     * @return the substring that represents an Olson ID
     */
    private String extractID(String label) {
        Matcher matcher = OLSON_ID_PATTERN.matcher(label);
        if (matcher.find()) {
            String id = matcher.group();
            try {
                DateTimeZone.forID(id); // just to check whether it exists
            } catch (IllegalArgumentException e) { // not found
                return null;
            }
            return id;
        }
        return null;
    }

    protected long fixFrom(DateTimeZone tz, int standardOffset, long from) {
        
        if (standardOffset != tz.getOffset(from)) { // NB: this must NOT be
             // a call to getBasicOffset(), because that method may be
             // overriden by the cached implementation(s) of this class.
            do {
                from = tz.previousTransition(from) + 1;
            } while ((from != 0) && (standardOffset == tz.getOffset(from)));
        } else {
            from = tz.nextTransition(from);
        }
        return from;
    }
}