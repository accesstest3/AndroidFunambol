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

package com.funambol.common.pim.icalendar;

import com.funambol.common.pim.vcalendar.BasicVCalendar;

/**
 * This class provides the list of specific iCalendar fields/properties
 */
public class ICalendar extends BasicVCalendar{

    // Fields
    public static final String BEGIN_VALARM       = "BEGIN:VALARM";
    public static final String END_VALARM         = "END:VALARM";
    public static final String BEGIN_VTIMEZONE    = "BEGIN:VTIMEZONE";
    public static final String END_VTIMEZONE      = "END:VTIMEZONE";
    public static final String VERSION            = "VERSION:2.0";
    public static final String VERSION_NAME       = "VERSION";
    public static final String VERSION_VALUE      = "2.0";

    public static final String TRIGGER            = "TRIGGER";
    public static final String ACTION             = "ACTION";
    public static final String ACTION_AUDIO       = "AUDIO";
    public static final String FREEBUSY           = "FREEBUSY";
    public static final String PRODID             = "PRODID";
    public static final String CALSCALE           = "CALSCALE";
    public static final String METHOD             = "METHOD";

    public static final String TZID               = "TZID";
    public static final String RELATED            = "RELATED";
    public static final String VALUE              = "VALUE";
    public static final String DATE_VALUE         = "DATE";
    public static final String DATE_TIME_VALUE    = "DATE-TIME";
    public static final String DURATION_VALUE     = "DURATION";
    public static final String FBTYPE             = "FBTYPE";
    public static final String FBTYPE_FREE        = "FREE";
    public static final String FBTYPE_BUSY        = "BUSY";
    public static final String FBTYPE_UNAVAILABLE = "BUSY-UNAVAILABLE";
    public static final String FBTYPE_TENTATIVE   = "BUSY-TENTATIVE";
    public static final String ATTENDEE_EMAIL_URI = "MAILTO";

    // Custom fields
    public static final String X_FUNAMBOL_TZ_OFFSET = "X-FUNAMBOL-TZ-OFFSET";
}
