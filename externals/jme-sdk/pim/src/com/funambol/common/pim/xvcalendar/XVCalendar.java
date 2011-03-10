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

package com.funambol.common.pim.xvcalendar;

import com.funambol.common.pim.vcalendar.BasicVCalendar;

/**
 * This class provides the list of specific vCalendar fields/properties
 */
public class XVCalendar extends BasicVCalendar {

    // Fields
    public static final String VERSION            = "VERSION:1.0";

    // vCalendar fields
    public static final String TZ                 = "TZ";
    public static final String DAYLIGHT           = "DAYLIGHT";

    // vEvent/vTodo fields
    public static final String AALARM             = "AALARM";
    public static final String DALARM             = "DALARM";
    public static final String MALARM             = "MALARM";
    public static final String PALARM             = "PALARM";
    public static final String EXDATE             = "EXDATE";
    public static final String XRULE              = "XRULE";
    public static final String RNUM               = "RNUM";
    public static final String RDATE              = "RDATE";

    // Custom fields
    public static final String X_FUNAMBOL_ALLDAY  = "X-FUNAMBOL-ALLDAY";

    // Not supported fields
    public static final String PRODID             = "PRODID";
    public static final String GEO                = "GEO";
    public static final String ATTACHMENT         = "ATTACH";
    public static final String DCREATED           = "DCREATED";
    public static final String RESOURCES          = "RESOURCES";
    public static final String SEQUENCE           = "SEQUENCE";
    public static final String TRANSP             = "TRANSP";
    public static final String RELATED_TO         = "RELATED-TO";
}
