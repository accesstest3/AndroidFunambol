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

/**
 * This class represents each offset transition in a time zone contained in a 
 * vCalendar/iCalendar item.
 *
 * @version $Id: TimeZoneTransition.java,v 1.1 2008-04-10 11:00:21 mauro Exp $
 */
public class TimeZoneTransition implements Comparable<TimeZoneTransition> {
    
    private int offset;
    private long time;
    private String name;
    
    /** Creates a new instance of TZTransition.
     * @param offset
     * @param time
     * @param name 
     */
    public TimeZoneTransition(int offset, long time, String name) {
        this.offset = offset;
        this.time = time;
        this.name = name;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object instanceof TimeZoneTransition) {
            TimeZoneTransition tzt = (TimeZoneTransition) object;
            if (this.offset != tzt.offset) {
                return false;
            }
            if (this.time != tzt.time) {
                return false;
            }
            return true; // The name is not relevant
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return (int) (time % 1000000000) + (offset / 1000); // The name is not 
                                                                     // relevant
        
    }
    
    public int compareTo(TimeZoneTransition tzt) {
        long timeDiff = this.time - tzt.time;
        if (timeDiff != 0) {
            return (int) (timeDiff % 1000000000);
        }
        return this.offset - tzt.offset;
        // The name is not relevant
    }
}
