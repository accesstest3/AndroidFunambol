/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2007 Funambol, Inc.
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

import com.funambol.common.pim.model.utility.TimeUtils;
import java.text.ParseException;

/**
 * This class represents the removal, the addition or the change of a
 * particular occurrence in the context of a recurrence rule.
 *
 * @version $Id: ExceptionToRecurrenceRule.java,v 1.4 2008-06-24 11:43:34 mauro Exp $
 */
public class ExceptionToRecurrenceRule 
implements Comparable<ExceptionToRecurrenceRule> {
    
    //--------------------------------------------------------------- Properties
    
    private String date; // in "yyyyMMdd'T'HHmmssZ" or 
                         // "yyyyMMdd'T'HHmmss" format
    private boolean addition; // true if it's an RDATE, false if it's an EXDATE
    
    /**
     * Gets the date value, which means the deleted occurrence if isAddition()
     * returns false, the added occurrence otherwise.
     *
     * @return date as a String in "yyyyMMdd'T'HHmmssZ", "yyyyMMdd'T'HHmmss" 
     *              or "yyyy-MM-dd" format
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the date value, which means the deleted or the added occurrence. The
     * date format is turned into one of the three accepted formats in the 
     * process ("yyyyMMdd'T'HHmmssZ", "yyyyMMdd'T'HHmmss" or "yyyy-MM-dd").
     *
     * @param date as a String in any ISO 8601 format
     * @throws ParseException if date could not be interpreted
     */
    public void setDate(String date) throws ParseException {
        String format = TimeUtils.getDateFormat(date.replaceAll("[Z:\\-]", ""));
        if (format == null) {
            throw new ParseException("Unknown format for recurrence exception \"" 
                    + date + '\"', -1);
        } else if (TimeUtils.PATTERN_YYYYMMDD.equals(format)) {
            this.date = TimeUtils.convertDateFromTo(date, TimeUtils.PATTERN_YYYY_MM_DD);
        } else if (TimeUtils.PATTERN_UTC_WOZ.equals(format)) {
            this.date = date.replaceAll("[:\\-]", ""); // NB: 'Z' is preserved!
        } else {
            throw new ParseException("Invalid format for recurrence exception: " 
                    + TimeUtils.getDateFormat(date), -1);
        }
    }
   
    /**
     * Returns whether the exception is an addition or a removal.
     * 
     * @return true if it is an addition ("positive exception"), false if it is
     *         a removal ("negative exception")
     */
    public boolean isAddition() {
        return addition;
    }

    /**
     * Setter for property addition.
     * 
     * @param addition new value of property addition (true if it is a positive
     *                 exception, false if it is a negative one)
     */
    public void setAddition(boolean addition) {
        this.addition = addition;
    }
    
    //-------------------------------------------------------------- Constructor
    
    /** 
     * Creates a new instance of ExceptionToRecurrenceRule. 
     * 
     * @param rdate true if it's an addition to the recurrence rule, false if
     *              it's a deletion
     * @param date as a String in "yyyyMMdd'T'HHmmssZ", "yyyyMMdd'T'HHmmss",
     *             "yyyy-MM-dd" or "yyMMdd" format
     * @throws ParseException if date could not be interpreted
     */
    public ExceptionToRecurrenceRule(boolean rdate, String date) 
    throws ParseException {
        this.setAddition(rdate);
        this.setDate(date);
    }
    
    //----------------------------------------------------------- Public methods
    
    @Override
    public boolean equals(Object object) {
        if (object instanceof ExceptionToRecurrenceRule) {
            ExceptionToRecurrenceRule other = (ExceptionToRecurrenceRule)object;
            if (this.addition != other.addition) {
                return false;
            }
            if (this.date == null) {
                return (other.date == null);
            }
            if (!this.date.equals(((ExceptionToRecurrenceRule)object).date)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = (this.date != null ? this.date.hashCode() : 0);
        if (this.addition) {
            return hash;
        }
        return -hash;
    }
    
    public int compareTo(ExceptionToRecurrenceRule e) {
        
        if (this.date == null) {
            return -1000;
        }
        
        int dateCompare = this.date.compareTo(e.date);        
        if (dateCompare == 0) {
            if (this.addition == e.addition) {
                return 0;
            }
            return (this.addition ? 1 : -1);
        }
        return (dateCompare * 2);
    }
}
