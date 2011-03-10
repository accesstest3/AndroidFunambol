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

/**
 * Represent a Property of a particular type, for usage for typified contact 
 * fields.
 */
public class TypifiedProperty extends Property {


    protected String propertyType;

    /**
     * Creates an empty typified property with a null property type.
     */
    public TypifiedProperty() {
        super();
        propertyType = null;
    }
    /**
     * Creates a TypifiedProperty with the provided property value and a null
     * property type
     * 
     * @param value the property value as a string
     */
    public TypifiedProperty(String value) {
        super(value);
        propertyType = null;
    }

    /**
     * Returns the property type.
     *
     * @return the value of propertyType
     */
    public String getPropertyType() {
       return propertyType;
    }

    /**
     * Sets the property type.
     *
     * @param propertyType the new value of propertyType
     */
    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null) &&
               (o instanceof TypifiedProperty) &&
               (propertyType != null) &&
               propertyType.equalsIgnoreCase(((TypifiedProperty)o).getPropertyType());
    }

    @Override
    public int hashCode() {
        
        int hash = super.hashCode();
        
        if (propertyType != null) {
            hash += propertyType.hashCode();
        }

        return hash;
    }

    /**
     * Compares this TypifiedProperty with another one.
     * 
     * @param prop the other TypifiedProperty
     * @return true if the two objects have the same contents
     */
    public boolean compare(TypifiedProperty prop) {

        if (prop == null) {
            return false;
        }

        String val1 = propertyType;
        String val2 = prop.getPropertyType();

        if (val1 != null && !val1.equals(val2)) {
            return false;
        } else if (val1 == null && val2 != null) {
            return false;
        }

        if (!super.compare(prop)) {
            return false;
        }

        return true;
    }

}
