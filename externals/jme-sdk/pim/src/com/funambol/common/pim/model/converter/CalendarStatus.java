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
 * You can event Funambol, Inc. headquarters at 643 Bair Island Road, Suite
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

import java.util.HashMap;
import java.util.Map;

import com.funambol.common.pim.model.common.Property;

/**
 *
 * CalendarStatus enum. Holds all possible status values for the status property of
 * the task. Each enum value holds the value stored server side, the corresponding
 * sif value and the label used in vcal/ical rapresentation.
 *
 * @version $Id: CalendarStatus.java 32831 2009-11-23 10:48:47Z filmac $
 */
public enum CalendarStatus {

    

    // ENUM NAME    SERVER VALUE    SIF VALUE       VCAL LABEL        ICAL LABEL
    ACCEPTED(       (short)0,       (short)0,       "ACCEPTED"),
    SENT(           (short)1,       (short)0,       "SENT" ,          null),
    TENTATIVE(      (short)2,       (short)0,       "TENTATIVE"),
    IN_PROCESS(     (short)3,       (short)1,       "CONFIRMED",      "IN-PROCESS"),
    CONFIRMED(      (short)4,       (short)1,       "CONFIRMED",      "IN-PROCESS"),
    COMPLETED(      (short)5,       (short)2,       "COMPLETED"),
    NEEDS_ACTION(   (short)6,       (short)3,       "NEEDS ACTION",   "NEEDS-ACTION"),
    DELEGATED(      (short)7,       (short)3,       "DELEGATED"),
    DECLINED(       (short)8,       (short)4,       "DECLINED");


   // static dictionaries used to recognize a CalendarStatus from a giving

    private final static CalendarStatus[]           serverMappings
                                  = new CalendarStatus[CalendarStatus.values().length];
    private final static CalendarStatus[]           sifMappings
                                  = new CalendarStatus[5];
    private final static Map<String,CalendarStatus> vcalIcalMappings
                                    = new HashMap<String, CalendarStatus>();

    static {
        // Filling the mapping for the sif status property
        sifMappings[0] = CalendarStatus.ACCEPTED;
        sifMappings[1] = CalendarStatus.CONFIRMED;
        sifMappings[2] = CalendarStatus.COMPLETED;
        sifMappings[3] = CalendarStatus.NEEDS_ACTION;
        sifMappings[4] = CalendarStatus.DECLINED;

        // Filling the mappings for the server 
        for(CalendarStatus t:CalendarStatus.values()) {
            serverMappings[t.serverValue] = t;
        }

        // Filling the mappings for the VCal/ICal values
        vcalIcalMappings.put(ACCEPTED.vcalIcalValue,ACCEPTED);
        vcalIcalMappings.put(TENTATIVE.vcalIcalValue,TENTATIVE);
        vcalIcalMappings.put(COMPLETED.vcalIcalValue,COMPLETED);
        vcalIcalMappings.put(DELEGATED.vcalIcalValue,DELEGATED);
        vcalIcalMappings.put(DECLINED.vcalIcalValue,DECLINED);

        // exceptions
        vcalIcalMappings.put(SENT.vcalValue,SENT); // has only ICal 2.0 values
         
        // the two following mappings have the same value for VCal 1.0 and
        // ICal 2.0
        vcalIcalMappings.put(CONFIRMED.vcalValue,CONFIRMED);
        vcalIcalMappings.put(IN_PROCESS.icalValue,IN_PROCESS);

        // NEEDS ACTION has a different spelling according to the VCal/ICal output
        // version
        vcalIcalMappings.put(NEEDS_ACTION.vcalValue,NEEDS_ACTION);
        vcalIcalMappings.put(NEEDS_ACTION.icalValue,NEEDS_ACTION);

    }


    //------------------------------------------------------------- Constructors

    /**
     * Constructs a CalendarStatus item with the given values.
     * @param serverValue is the short value that will be stored server side
     * @param sifValue is the value used to fill the SIF-T rapresentation of the 
     * task
     * @param vcalIcalValue is the value used to fill the Vcal/Ical rapresentation
     * of the Task.
     */
    CalendarStatus(int serverValue,
                   int sifValue,
                   String vcalValue,
                   String icalValue) {
       this.serverValue         = (short) serverValue;
       this.sifValue            = (short) sifValue;
       this.vcalValue           = vcalValue;
       this.icalValue           = icalValue;
       this.vcalIcalValue       = null;
    }


    CalendarStatus(int serverValue,
                   int sifValue,
                   String vcalIcalValue) {
       this.serverValue         = (short) serverValue;
       this.sifValue            = (short) sifValue;
       this.vcalIcalValue       = vcalIcalValue;
       this.vcalValue           = null;
       this.icalValue           = null;
    }


    //------------------------------------------------------------ Instance data
    public final short serverValue;
    public final short sifValue;
    private final String vcalValue;
    private final String icalValue;
    private final String vcalIcalValue;

    
    //----------------------------------------------------------- Public methods

    /**
     * 
     * @return the sif value bound to this CalendarStatus object as string
     */
    public String getSifValue() {
        return Short.toString(sifValue);
    }

    /**
     *
     * @return the value stored on the server database if you want to store such
     * a CalendarStatus
     */
    public String getServerValue() {
        return Short.toString(serverValue);
    }
    
    public String getVCalValue() {
        if(vcalIcalValue!=null) {
            return vcalIcalValue;
        }
        return vcalValue;
    }

    public String getICalValue() {
        if(vcalIcalValue!=null) {
            return vcalIcalValue;
        }
        return icalValue;
    }

    public String getVCalICalValue(boolean isVcal) {
        if(vcalIcalValue!=null) {
            return vcalIcalValue;
        } else if(isVcal) {
            return vcalValue;
        }
        return icalValue;
    }



    /**
     * retrieves the CalendarStatus bound to the given property.
     * If the given property is null or it doesn't contain any meaningful information,
     * the ACCEPTED item is returned.
     *
     * @param property is the property of a Task object build server side and
     * it contains a value from 0 to 8.
     *
     * @return the CalendarStatus item bound to the given property
     */
    public static CalendarStatus mapServerStatus(Property property) {
       if(property!=null) {
           String propertyValue = property.getPropertyValueAsString();
           int index = -1;
           try {
               index = Short.parseShort(propertyValue);
           } catch(NumberFormatException e) {

           }
           if(index>=0 && index < serverMappings.length) {
               return serverMappings[index];
           }
       }
       return null;
    }

    /**
     *
     * retrieves the CalendarStatus bound to the given string, rapresenting the
     * status of the task contained in a SIF-T rapresentation.
     * If the given property is null or it doesn't contain any meaningful information,
     * the ACCEPTED item is returned.
     *
     * @param status is the value of the status tag contained in the SIF-T rapresentation
     * and we expect a value between 0 and 4.
     *
     * @return the CalendarStatus item bound to the given SIF-T status
     */

    public static CalendarStatus mapSifStatus(String status) {
       if(status!=null) {
           int index = -1;
           try {
               index = Short.parseShort(status);
           } catch(NumberFormatException e) {

           }
           if(index>=0 && index < sifMappings.length) {
               return sifMappings[index];
           }
       }
       return null;
    }


    /**
     * retrieves the CalendarStatus bound to the given Vcal/Ical status.
     * If the given property is null or it doesn't contain any meaningful information,
     * the ACCEPTED item is returned.
     *
     * @param status is the string value contained in the Vcal/Ical rapresentation
     * and we expected one of the following values:
     *      ACCEPTED      (vCal 1.0 & 2.0)
     *      SENT          (vCal 1.0)
     *      TENTATIVE     (vCal 1.0 & 2.0)
     *      IN-PROCESS    (vCal 2.0)
     *      CONFIRMED     (vCal 1.0)
     *      COMPLETED     (vCal 1.0 & 2.0)
     *      NEEDS-ACTION  (vCal 1.0 & 2.0)
     *      DELEGATED     (vCal 1.0 & 2.0)
     *      DECLINED      (vCal 1.0 & 2.0)
     *
     * @return the CalendarStatus item bound to the given property
     */

    public static CalendarStatus mapVcalIcalStatus(String status) {
       if(status!=null) {
           if(vcalIcalMappings!=null && vcalIcalMappings.containsKey(status)) {
                return vcalIcalMappings.get(status);
            }
       }
       return null;
    }

    /**
     * retrieves the CalendarStatus bound to the given Vcal/Ical status property.
     * If the given property is null or it doesn't contain any meaningful information,
     * the ACCEPTED item is returned.
     *
     * @param statusProperty is the property rapresenting the status value contained
     * in the Vcal/Ical rapresentation and we expected one of the following values:
     *      ACCEPTED      (vCal 1.0 & 2.0)
     *      SENT          (vCal 1.0)
     *      TENTATIVE     (vCal 1.0 & 2.0)
     *      IN-PROCESS    (vCal 2.0)
     *      CONFIRMED     (vCal 1.0)
     *      COMPLETED     (vCal 1.0 & 2.0)
     *      NEEDS-ACTION  (vCal 1.0 & 2.0)
     *      DELEGATED     (vCal 1.0 & 2.0)
     *      DECLINED      (vCal 1.0 & 2.0)
     *
     * @return the CalendarStatus item bound to the given property
     */

    public static CalendarStatus mapVcalIcalStatus(Property statusProperty) {
        if (statusProperty != null) {
            return mapVcalIcalStatus(statusProperty.getPropertyValueAsString());
        }
        return null;
    }

    /**
     * Retrieves the server value bound to the given status contained in a
     * SIF-T rapresentation.
     * If the given status is null or we're not able to map it, the server value for
     * ACCEPTED is returned.
     *
     * @param status is the value of the status tag contained in the SIF-T rapresentation
     * and we expect a value between 0 and 4.
     *
     * @return the valued that rapresents server side the given SIF-T status
     */
    
    public static String getServerValueFromSifStatus(String status) {
        CalendarStatus calendarStatus = mapSifStatus(status);
        if(calendarStatus!=null) {
            return calendarStatus.getServerValue();
        }
        return null;
    }

    /**
     * Retrieves the SIF status bound to the given status property in a Task object.
     *
     * @param property is the status property of a Task object and we expected it
     * may contain a value from 0 to 8 (String).
     *
     * @return the string rapresenting the value of the status tag in the SIF-T
     * rapresentation of the corresponding task object.
     */
    public static String getSifStatusFromServerValue(Property property) {
        CalendarStatus calendarStatus = mapServerStatus(property);
        if(calendarStatus!=null) {
            return calendarStatus.getSifValue();
        }
        return null;
    }
}
