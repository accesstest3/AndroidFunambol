/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2003 - 2007 Funambol, Inc.
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

import java.util.HashMap;
import java.util.Map;

/**
 * This object represents the property of a vCard/vCalendar/iCalendar item,
 * including its value and parameters.
 *
 * @version $Id: Property.java,v 1.6 2008-04-10 10:42:22 mauro Exp $
 */
public class Property {

    //--------------------------------------------------------------- Properties
    
    protected Object propertyValue;
    protected String altrep;
    protected String cn;
    protected String cutype;
    protected String delegatedFrom;
    protected String delegatedTo;
    protected String dir;
    protected String encoding;
    protected String language;
    protected String member;
    protected String partstat;
    protected String related;
    protected String sentby;
    protected String value;
    protected Map<String, String> xParams;
    protected String tag;
    protected String group;
    protected String chrset;
    protected String type;

    /**
     * Returns the value of this property "as it is".
     *
     * @return the value of this property as any Object
     */
    public Object getPropertyValue() {
        return propertyValue;
    }

    /**
     * Returns the value of this property as a String. If the property value
     * is not of type String, String.valueOf() is invoked to translate the
     * object into its string representation.
     *
     * @return the value of this property as a String object
     */
    public String getPropertyValueAsString() {
        if (propertyValue == null) {
            return null;
        }
        if (propertyValue instanceof String) {
            return (String)propertyValue;
        }
        return String.valueOf(propertyValue);
    }

    /**
     * Returns parameter ENCODING that is used to specify an alternate
     * encoding for a value.
     *
     * @return the encoding parameter of this property
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Returns parameter LANGUAGE that is used to identify data in multiple
     * languages.
     *
     * @return the language parameter of this property
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Returns parameter VALUE that is used to identify the value data type and 
     * format of the value.
     *
     * @return the value parameter of this property
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns an alternate text representation for the property value.
     *
     * @return an alternate text representation
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public String getAltrep() {
        return altrep;
    }

    /**
     * Returns the common name that is associated with the calendar user
     * specified by this property.
     *
     * @return a common name
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public String getCn() {
        return cn;
    }

    /**
     * Returns the type of calendar user specified by this property.
     *
     * @return the type of calendar user
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public String getCutype() {
        return cutype;
    }

    /**
     * Returns the calendar users that have delegated their participation to
     * the calendar user specified by this property.
     *
     * @return the delegators
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public String getDelegatedFrom() {
        return delegatedFrom;
    }

    /**
     * Returns the calendar users to whom the calendar user specified by this
     * property has delegated participation.
     *
     * @return the delegatees
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public String getDelegatedTo() {
        return delegatedTo;
    }

    /**
     * Returns the reference to a directory entry associated with the calendar
     * user specified by this property.
     *
     * @return the reference to a directory entry
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public String getDir() {
        return dir;
    }

    /**
     * Returns the group or list membership of the calendar user specified by
     * this property.
     *
     * @return the group or list membership
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public String getMember() {
        return member;
    }

    /**
     * Returns the partecipation status of the calendar user specified by
     * this property.
     *
     * @return the partecipation status
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public String getPartstat() {
        return partstat;
    }

    /**
     * Returns the relationship of the alarm trigger with respect to the start
     * or end of the calendar component.
     *
     * @return the relationship of the alarm trigger
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public String getRelated() {
        return related;
    }

    /**
     * Returns the calendar user that is acting on behalf of the calendar user
     * specified by this property.
     *
     * @return the calendar user that is acting on behalf of the calendar user
     *         specified by this property
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public String getSentby() {
        return sentby;
    }

    /**
     * Returns the group parameter of this property. The group parameter is used
     * to group related attributes together.
     *
     * @return the group parameter of this property
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Returns the default character set used within the body part.
     *
     * @return the charset parameter of this property
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public String getCharset() {
        return chrset;
    }

    /**
     * Returns the type parameter of this property.
     *
     * @return the type parameter of this property
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the custom parameters.
     * 
     * @return a HashMap containing all custom parameters (names and values)
     * 
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public HashMap<String, String> getXParams() {
        return (HashMap) xParams; // Ugly but necessary
    }
    
    /**
     * Sets the custom parameter
     * 
     * @param xParams a Map containing all custom parameters (names and values)
     * 
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setXParams(Map<String, String> xParams) {
        if(this.xParams == null) {
            this.xParams = new HashMap<String, String>();
        }
        this.xParams.clear();
        this.xParams.putAll(xParams);
    }

    /**
     * Returns the TAG of this property.
     * 
     * @return the tag parameter of this property
     * 
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public String getTag() {
        return this.tag;
    }

    /**
     * Sets the TAG of this property.
     * 
     * @param tag the tag to set
     * 
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setTag(String tag){
        this.tag = tag;
    }

    /**
     * Sets the encoding parameter of this property.
     *
     * @param encoding the encoding to set
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets the language parameter of this property.
     *
     * @param language the language to set
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Sets the value parameter of this property.
     *
     * @param value the value to set
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setValue(String value) {
        this.value = value;
    }


    /**
     * Sets the property value. Blank spaces and tabs will be stripped from its
     * end if it is a string.
     *
     * @param propertyValue the property value to set
     */
    public void setPropertyValue(Object propertyValue) {
        
        this.propertyValue = propertyValue;
    }

    /**
     * Sets an alternative text representation for the property value.
     *
     * @param altrep an alternative text representation for the property value
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setAltrep(String altrep) {
        this.altrep = altrep;
    }

    /**
     * Sets a common name.
     *
     * @param cn a common name
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setCn(String cn) {
        this.cn = cn;
    }

    /**
     * Sets the type of calendar user.
     *
     * @param cutype the type of calendar user
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setCutype(String cutype) {
        this.cutype = cutype;
    }

    /**
     * Sets the delegated to partecipate at the event.
     *
     * @param delegatedFrom the delegated to partecipate at the event
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setDelegatedFrom(String delegatedFrom) {
        this.delegatedFrom = delegatedFrom;
    }

    /**
     * Sets the delegate.
     *
     * @param delegatedTo the delegate
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setDelegatedTo(String delegatedTo) {
        this.delegatedTo = delegatedTo;
    }

    /**
     * Sets the directory entry.
     *
     * @param dir the directory entry
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setDir(String dir) {
        this.dir = dir;
    }

    /**
     * Sets the group or list membership.
     *
     * @param member the group or list membership
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setMember(String member) {
        this.member = member;
    }

    /**
     * Sets the partecipation status.
     *
     * @param partstat the partecipation status
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setPartstat(String partstat) {
        this.partstat = partstat;
    }

    /**
     * Sets the relationship of the alarm trigger.
     *
     * @param related the relationship of the alarm trigger
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setRelated(String related) {
        this.related = related;
    }

    /**
     * Sets the calendar user that is acting on behalf of the calendar user
     * specified by the property.
     *
     * @param sentby the calendar user that is acting on behalf of the calendar
     *               user specified by the property
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setSentby(String sentby) {
        this.sentby = sentby;
    }

    /**
     * Sets the group parameter of this property.
     *
     * @param group the group to set
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Sets the charset parameter of this property.
     *
     * @param chrset the charset to set
     *
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setCharset(String chrset) {
        this.chrset = chrset;
    }

    /**
     * Sets the type parameter of this property.
     *
     * @param type the type to set
     * 
     * @deprecated Since version 6.5, a Property should not contain anything but
     *             the propertyValue.
     */
    public void setType(String type) {
        this.type = type;
    }

    //------------------------------------------------------------- Constructors
    
    /**
     * Creates an empty property.
     */
    public Property() {
        this(null);
    }

    /**
     * Creates property without parameters but with the specified value.
     * 
     * @param propertyValue the property value
     */
    public Property(String propertyValue) {
        this.altrep = null;
        this.cn = null;
        this.cutype = null;
        this.delegatedFrom = null;
        this.delegatedTo = null;
        this.dir = null;
        this.encoding = null;
        this.language = null;
        this.member = null;
        this.partstat = null;
        this.related = null;
        this.sentby = null;
        this.value = null;
        this.xParams = new HashMap<String, String>();

        this.tag = null;
        setPropertyValue(propertyValue);

        this.group = null;
        this.chrset = null;
        this.type = null;
    }

    //----------------------------------------------------------- Public methods

    /**
     * Compares this Property with another one. Only the property value in its
     * string form is considered, since all other properties are deprecated.
     * 
     * @param prop the Property object to compare with
     * 
     * @return true if the objects are to be considered equal, false otherwise.
     */
    public boolean compare(Property prop) {

        if (prop == null) {
            return false;
        }

        String val1 = getPropertyValueAsString();
        String val2 = prop.getPropertyValueAsString();
        if (val1 != null) {
            return val1.equals(val2);
        } else if (val2 != null) {
            return false;
        } else {
            return true;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Property) {
            return compare((Property) o);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (this.propertyValue != null ? this.propertyValue.hashCode() : 0);
    }

    //---------------------------------------------------- Public static methods

    /**
     * Checks (safely) whether the property is unset or set to an empty string.
     *
     * @param property may be null
     * @return false only if the property value is a non-null non-empty string
     *
     * @see PIMEntityDAO#stringFrom(Property, boolean)
     */
    public static boolean isEmptyProperty(Property property) {
        
        if (property == null) {
            return true;
        }
        String string = property.getPropertyValueAsString();
        if (string == null || string.length() == 0) {
            return true;
        }
        return false;
    }
    
    /**
     * Extract a string from a property in a safe way. An empty string ("") is
     * considered as an acceptable value for the property: in such a case, an
     * empty String object will be returned.
     *
     * @param property may be null
     * @return if existing, the property value will be returned as a String
     *         object
     */
    public static String stringFrom(Property property) {
        
        if (property == null) {
            return null;
        }
        return property.getPropertyValueAsString();
    }
    
    /**
     * Extract a string from a property in a safe way. This method is not
     * currently used, but it could be useful in the future for determining the
     * behaviour of the connector when dealing with empty properties. A field
     * whose value is extracted with stringFrom(..., true) will not be updated
     * in case its value is set to ""; a field whose value is extracted with
     * stringFrom(..., false) will be considered as explicitly kept blank if its
     * value is "". This means that single field deletions can be made tunable.
     *
     * @param property may be null
     * @param emptyImpliesNull if true, an empty string ("") will be treated as
     *                         if it were null; otherwise, in such a case an
     *                         empty String object will be returned
     *
     * @return if existing (and not empty if emptyImpliesNull is true), the
     *         property value will be returned as a String object
     */
    public static String stringFrom(
            Property property, boolean emptyImpliesNull) {
        
        if (property == null) {
            return null;
        }
        String string = property.getPropertyValueAsString();
        if (string == null || !emptyImpliesNull) {
            return string;
        }
        if (string.length() == 0) {
            return null;
        }
        return string;
    }

}
