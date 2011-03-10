/* 
 * Copyright (c) 2004 Harrie Hazewinkel. All rights reserved.
 */

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
package com.funambol.common.pim.model.model;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;

/**
 * The date and time value type.
 * The purpose is to identify values that contain a calendar date.
 * Notation:
 *    date-time  = date "T" time ;As specified in the date and time
 *                               ;value definitions
 *
 *    date               = date-value
 *    date-value         = date-fullyear date-month date-mday
 *    date-fullyear      = 4DIGIT
 *    date-month         = 2DIGIT        ;01-12
 *    date-mday          = 2DIGIT        ;01-28, 01-29, 01-30, 01-31
 *                                       ;based on month/year
 *    time               = time-hour time-minute time-second [time-utc]
 *    time-hour          = 2DIGIT        ;00-23
 *    time-minute        = 2DIGIT        ;00-59
 *    time-second        = 2DIGIT        ;00-60
 *    ;The "60" value is used to account for "leap" seconds.
 *    time-utc   = "Z"
 *
 * @version $Id: ValueDateTime.java,v 1.2 2007-11-28 11:14:05 nichele Exp $
 */
public class ValueDateTime implements ValueInterface {
    static public SimpleDateFormat fmtDayStart =
                    new SimpleDateFormat("yyyyMMdd'T000000'");
    static public SimpleDateFormat fmtDayEnd =
                    new SimpleDateFormat("yyyyMMdd'T235900'");
    static private SimpleDateFormat fmt =
                    new SimpleDateFormat("yyyyMMdd'T'HHmmss");

    public boolean checkValue(String v) {
        return checkValue(v, fmt);
    }

    static public boolean checkValue(String v, SimpleDateFormat fmt) {
        ParsePosition pos = new ParsePosition(0);
        if (null == fmt.parse(v, pos)) {
             return false;
        }
        if ((v.length() > pos.getIndex()) && (v.charAt(pos.getIndex()) != 'Z')) {
             return false;
        }
        return true;
    }

    static public String FullDay(
                   String start, String end) {
        if (checkValue(start, fmtDayStart) && checkValue(end, fmtDayEnd)
            && start.regionMatches(0, end, 0, 7)) {
            return start.substring(0,6);
        }
        return null;
    }
}
