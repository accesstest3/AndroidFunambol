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

import com.funambol.common.pim.model.utility.TimeUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * This class is just instrumental to the output of time-zone information in the
 * iCalendar format.
 */
public class ICalendarTimeZoneTransition extends TimeZoneTransition {

    private int year;
    private int month;
    private int dayOfWeek;
    private int instance;
    private boolean lastInstance;
    private String timeISO1861;

    public ICalendarTimeZoneTransition(TimeZoneTransition transition,
                                       int previousOffset           ) {
        
        super(transition.getOffset(), 
              transition.getTime()  , 
              transition.getName()  );
        
        TimeZone fixed = TimeZone.getTimeZone("UTC");
        fixed.setRawOffset(previousOffset);
        Calendar finder = new GregorianCalendar(fixed);
        
        finder.setTimeInMillis(transition.getTime());
        year = finder.get(Calendar.YEAR);
        month = finder.get(Calendar.MONTH);
        dayOfWeek = finder.get(Calendar.DAY_OF_WEEK);
        instance = finder.get(Calendar.DAY_OF_WEEK_IN_MONTH);
        lastInstance =
                (finder.getActualMaximum(Calendar.DAY_OF_WEEK_IN_MONTH)
                 ==
                 instance);
        DateFormat localTimeFormat = 
                new SimpleDateFormat(TimeUtils.PATTERN_UTC_WOZ);
        localTimeFormat.setTimeZone(fixed);
        timeISO1861 = localTimeFormat.format(finder.getTime());
    }
    
    public ICalendarTimeZoneTransition(String name, int offset) {
        super(offset, 
              0     , // age start
              name  );
        year         = 1970;
        month        = 1;
        dayOfWeek    = 5; // It was a Thursday
        instance     = 1;
        lastInstance = false;
        timeISO1861  = "19700101T000000";
    }
    
    public boolean matchesRecurrence(ICalendarTimeZoneTransition other      ,
                                     boolean                     lastInMonth) {
        if (getMonth() != other.getMonth()) {
            return false;
        }
        if (getDayOfWeek() != other.getDayOfWeek()) {
            return false;
        }
        if (lastInMonth) {
            if (!lastInstance || !(other.lastInstance)) {
               return false;
            }         
            return true;
        }
        return (getInstance() == other.getInstance());
    }
    
    public String getTimeISO1861() {
        return timeISO1861;
    }

    public int getMonth() {
        return month;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getInstance() {
        return instance;
    }

    public int getYear() {
        return year;
    }
}
