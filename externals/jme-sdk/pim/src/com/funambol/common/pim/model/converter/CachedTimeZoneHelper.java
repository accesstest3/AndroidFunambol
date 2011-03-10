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

import com.funambol.common.pim.model.model.Property;
import com.funambol.common.pim.model.model.VTimezone;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.joda.time.DateTimeZone;
import org.w3c.dom.Node;

/**
 * This class is a cached extension of 
 * com.funambol.common.pim.converter.TimeZoneHelper.
 * 
 * @version $Id: CachedTimeZoneHelper.java,v 1.2 2008-04-17 17:04:56 mauro Exp $
 */
public class CachedTimeZoneHelper extends TimeZoneHelper {

    protected boolean transitionsComputed;
    protected long from;
    protected long to;
    
    protected static Map<String, Map<Integer, String>> cache10 =
            new Hashtable<String, Map<Integer, String>>();
    protected static Map<String, List<ICalendarTimeZoneTransition>> cache20 =
            new Hashtable<String, List<ICalendarTimeZoneTransition>>();
    // NB: Synchronization lock on cache20 MUST be acquired before using
    //     cache20From and cache20To
    protected static Map<String, Long> cache20From =
            new Hashtable<String, Long>();
    protected static Map<String, Long> cache20To =
            new Hashtable<String, Long>();
    
    private static final Integer TZ_KEY = Integer.valueOf(0);
    private static final Integer FIXED_KEY = Integer.valueOf(-1);
    
    //------------------------------------------------------------- Constructors
    
    /**
     * Creates a new instance of CachedTimeZoneHelper on the basis of the
     * information extracted from a vCalendar (1.0) item.
     * Transitions are immediately parsed.
     *
     * @param tz the TZ property
     * @param daylightList a List containing all DAYLIGHT properties
     * @throws java.lang.Exception 
     */
    public CachedTimeZoneHelper(Property tz, List<Property> daylightList)
    throws Exception {
        super(tz, daylightList);
        transitionsComputed = true;
    }
    
    /**
     * Creates a new instance of CachedTimeZoneHelper on the basis of the
     * information extracted from an iCalendar (vCalendar 2.0) item.
     * Transitions are immediately computed.
     *
     * @param vTimeZone
     * @param from the start of the relevant time interval for the generation of
     *             transitions (an istant expressed as a long)
     * @param to the end of the relevant time interval for the generation of
     *           transitions (an istant expressed as a long)
     * @throws java.lang.Exception 
     */
    public CachedTimeZoneHelper(VTimezone vTimeZone, long from, long to)
    throws Exception {
        super(vTimeZone, from, to);
        transitionsComputed = true;
    }
    
    /**
     * Creates a new instance of CachedTimeZoneHelper on the basis of a
     * zoneinfo (Olson database) ID.
     * The computation of transitions is delayed until it's eventually needed.
     *
     * @param id the time zone ID according to the zoneinfo (Olson) database
     * @param from the start of the relevant time interval for the generation of
     *             transitions (an istant expressed as a long)
     * @param to the end of the relevant time interval for the generation of
     *           transitions (an istant expressed as a long)
     */
    public CachedTimeZoneHelper(String id, long from, long to) {
        transitionsComputed = false;
        cacheID(id);
        this.from = from;
        this.to = to;
    }
    
    //----------------------------------------------------------- Public methods
    @Override
    public List<Property> getXVCalendarProperties() {

        List<Property> properties = new ArrayList<Property>();
        Map<Integer, String> cache;
        String id = toID();

        synchronized (cache10) { // Acquires lock on cache10

            // Checks the cache for elements needed
            cache:
            if (cache10.containsKey(id)) { // cached

                cache = cache10.get(id);

                if (!cache.containsKey(FIXED_KEY)) { // not a fixed time zone
                    DateTimeZone dtz = DateTimeZone.forID(id);
                    int since = TimeZoneHelper.year(
                            fixFrom(dtz, dtz.getStandardOffset(from), from));
                    int until = TimeZoneHelper.year(to);
                    for (int year = since;
                            year <= until;
                            year++) {
                        if (!cache.containsKey(Integer.valueOf(year))) {
                            break cache; // not enough transitions in cache
                        }
                    }
                    // All transitions are in cache: they will just be retrieved
                    properties.add(new Property("TZ",
                            cache.get(TZ_KEY)));
                    for (int year = since;
                            year <= until;
                            year++) {
                        properties.add(
                                new Property("DAYLIGHT",
                                cache.get(Integer.valueOf(year))));
                    }
                    return properties;

                } else { // a fixed time zone
                    properties.add(new Property("TZ",
                            cache.get(TZ_KEY)));
                    properties.add(new Property("DAYLIGHT",
                            "FALSE"));
                    return properties;
                }


            } else { // not in cache, must be created
                cache = new Hashtable<Integer, String>();
                cache10.put(id, cache);
            }

            // Creates elements missing in the cache
            Property tz = getTZ();
            properties.add(tz);
            cache.put(TZ_KEY, tz.getValue());

            List<Property> dayLightList = getDaylightList();
            properties.addAll(dayLightList);
            for (Property dayLight : dayLightList) {
                Integer year = Integer.valueOf(year(dayLight));
                if (!cache.containsKey(year)) {
                    cache.put(year, dayLight.getValue());
                }
            }

        } // Releases lock on cache10
        
        return properties;
    }

    @Override
    public VTimezone getVTimezone() {

        List<ICalendarTimeZoneTransition> transitions;
        String id = toID();
        
        synchronized (cache20) { // Acquires lock on cache20

            if ((cache20.containsKey(id)) &&
                    (from >= cache20From.get(id).intValue()) &&
                    (to <= cache20To.get(id).intValue())) {

                transitions = cache20.get(id);
                for (ListIterator<ICalendarTimeZoneTransition> it =
                        transitions.listIterator(); it.hasNext();) {
                    ICalendarTimeZoneTransition transition = it.next();
                    if ((transition.getTime() < from) ||
                            (transition.getTime() > to)) {
                        it.remove();
                    }
                }
            } else {

                transitions = getICalendarTransitions();
                cache20.put(id, transitions);
                cache20From.put(id, new Long(from));
                cache20To.put(id, new Long(to));
            }

        } // Releases lock on cache20

        return TimeZoneHelper.toVTimezone(transitions, id, getBasicOffset());
    }

    public static synchronized void clearCaches() {
        cache10.clear();
        cache20.clear();
        cache20From.clear();
        cache20To.clear();
    }

    public static synchronized String cacheStatus(String id) {
        StringBuffer sb = new StringBuffer();
        sb.append("text/x-vcalendar:");
        if (cache10.containsKey(id)) {
            sb.append(cache10.get(id).size());
        } else {
            sb.append('0');
        }
        sb.append("\ntext/calendar:");
        if (cache20.containsKey(id)) {
            sb.append(cache20.get(id).size());
        } else {
            sb.append('0');
        }
        return sb.toString();
    }

    @Override    
    protected List<TimeZoneTransition> getTransitions() {
        requireTransitions(id);
        return super.getTransitions();
    }
    
    @Override
    protected int getBasicOffset() {
        requireTransitions(id);
        return super.getBasicOffset();
    }
    
    @Override
    protected String getName() {
        requireTransitions(id);
        return super.getName();
    }
    
//-------------------------------------------------------------- Private methods
    private boolean requireTransitions(String id) {
        if (transitionsComputed) {
            return false;
        }
        if (id == null) {
            transitionsComputed = true;
            return true;
        }
        processID(id, from, to);
        transitionsComputed = true;
        return true;
    }
    
    private static int year(Property dayLight)
            throws NumberFormatException {

        if ((dayLight == null) || dayLight.getValue() == null) {
            throw new NumberFormatException("DAYLIGHT value missing");
        }
        String dayLightValue = dayLight.getValue();
        
        if (dayLightValue.startsWith("F")) { // F is for FALSE
            return FIXED_KEY;
        }

        String[] split = dayLightValue.split(";");
        if (split.length <= 2) {
            throw new NumberFormatException("Wrong DAYLIGHT format: " 
                    + dayLightValue);
        }

        return Integer.parseInt(split[2].substring(0, 4));
    }
}
