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
package com.funambol.common.pim.model.common;

/**
 * This class extends com.funambol.common.pim.common.Property attaching a time
 * zone to it. About the format used for the time zone, please see 
 * <a href="http://java.sun.com/javase/timezones/">
 * http://java.sun.com/javase/timezones/</a>.
 *
 * @version $Id: PropertyWithTimeZone.java,v 1.1 2008-04-10 10:40:56 mauro Exp $
 */
public class PropertyWithTimeZone extends Property {

    //--------------------------------------------------------------- Properties
    
    protected String timeZone;
    
    /**
     * Gets the time zone property.
     * 
     * @return the time zone as its Olson ID
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Sets the time zone property.
     * 
     * @param timeZone the time zone as its Olson ID
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    //------------------------------------------------------------- Constructors
    
    /**
     * Creates a new PropertyWithTimeZone.
     * 
     * @param propertyValue the text value of the property
     * @param timeZone the time zone as its Olson ID
     */
    public PropertyWithTimeZone(String propertyValue, String timeZone) {
        super(propertyValue);
        this.timeZone = timeZone;
    }
    
    /**
     * Creates a new PropertyWithTimeZone with a null time zone.
     * 
     * @param propertyValue the text value of the property
     */    
    public PropertyWithTimeZone(String propertyValue) {
        super(propertyValue);
        this.timeZone = null;
    }
    
    /**
     * Creates a new PropertyWithTimeZone with a null time zone and no value.
     */
    public PropertyWithTimeZone() {
        super();
        this.timeZone = null;
    }
    
    /**
     * Creates a new PropertyWithTimeZone by attaching a time zone to the value
     * of an existing property.
     * 
     * @param property an existing property, whose text value will be recycled
     * @param timezone the time zone as its Olson ID
     */
    public PropertyWithTimeZone(Property property, String timezone) {
        this(property.getPropertyValueAsString(), timezone);
        // Nothing else needs be added
    }  
}
