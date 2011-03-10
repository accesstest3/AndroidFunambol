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

import com.funambol.common.pim.model.common.Visitor;
import com.funambol.common.pim.model.common.VisitorException;
import com.funambol.common.pim.model.common.VisitorInterface;
import com.funambol.common.pim.model.common.VisitorObject;

import java.util.ListIterator;

/**
 *
 * @version $Id: VCalendar.java,v 1.2 2007-11-28 11:14:05 nichele Exp $
 */
public class VCalendar extends VComponent implements VisitorInterface {
    /**
     * Version 2.0 is iCalendar as specified by RFC-2445
     * */
    public static final String VCALENDAR_VERSION_20 = "2.0";

    /**
     * Version 1.0 is older vCalendar standard:
     * <a href="http://www.imc.org/pdi/vcal-10.rtf">http://www.imc.org/pdi/vcal-10.rtf</a>
     * */
    public static final String VCALENDAR_VERSION_10 = "1.0";

    private static final String COMPONENT_NAME = "VCALENDAR";

    public String getVComponentName () {
        return COMPONENT_NAME;
    }

    public String getSifType () {
        return null;
    }

    public VCalendar () {
        super ();
    }

    public void addEvent ( VEvent event ) {
        addComponent ( event );
    }

    public void addTodo ( VTodo todo ) {
        addComponent ( todo );
    }

    public void addTimezone ( VTimezone timezone ) {
        addComponent ( timezone );
    }

    
    
    public VCalendarContent getVCalendarContent() {
        VEvent ve = getFirstVEvent();
        if (ve != null) {
            return ve;
        }
        return getFirstVTodo();
    }
    
    public VEvent getFirstVEvent () {
        ListIterator listIterator = getAllComponents ().listIterator ();
        while ( listIterator.hasNext () ) {
            Object obj = listIterator.next ();
            if ( obj instanceof VEvent )
                return (VEvent) obj;
        }

        return null;
    }

    public VTodo getFirstVTodo () {
        ListIterator listIterator = getAllComponents ().listIterator ();
        while ( listIterator.hasNext () ) {
            Object obj = listIterator.next ();
            if ( obj instanceof VTodo )
                return (VTodo) obj;
        }

        return null;
    }

    /**
     * visitor.
     */
    public void accept ( Visitor v ) throws VisitorException {
        v.visitVCalendar ( this );
    }

    /**
     * visitor with arguments.
     */
    public Object accept ( VisitorObject v, Object argu ) throws VisitorException {
        return v.visitVCalendar ( this, argu );
    }

    public String toString () {
        StringBuffer buffer = new StringBuffer ();
        buffer.append ( "BEGIN:VCALENDAR\r\n" );
        toStringBuffer ( buffer );
        buffer.append ( "END:VCALENDAR\r\n" );
        return buffer.toString ();
    }
}
