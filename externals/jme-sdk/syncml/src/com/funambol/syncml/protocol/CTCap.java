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
package com.funambol.syncml.protocol;

import java.util.Vector;

/**
 * This class corresponds to the &lt;CTCap&gt; tag in the SyncML devinf DTD v1.2
 */
public class CTCap {

    // ------------------------------------------------------------ Private data
    private CTInfo    ctInfo    ;
    private Boolean   fieldLevel;
    private Vector    properties = new Vector();

    // ------------------------------------------------------------ Constructors
    /**
     * In order to expose the server configuration like WS this constructor
     * must be public
     */
    public CTCap() {}

    /**
     * Creates a new CTCap object with the given type, version, field-level and
     * an array of properties
     *
     * @param ctInfo The type and version of a supported content type - NOT NULL
     * @param fieldLevel The capability to apply field-level replaces for the
     *                   corresponding CTType
     * @param properties The array of supported properties of a given content
     *                   type - NOT NULL
     *
     */
    public CTCap(final CTInfo  ctInfo,
                 final boolean fieldLevel    ,
                 final Property[] properties ) {
        setCTInfo(ctInfo);
        this.fieldLevel = (fieldLevel) ? new Boolean(fieldLevel) : null;
        setProperties(properties);
    }
    // ---------------------------------------------------------- Public methods

    /**
     * Get a CTInfo object
     *
     * @return a CTInfo object
     */
    public CTInfo getCTInfo() {
        return this.ctInfo;
    }

    /**
     * Sets a CTInfo object
     *
     * @param ctInfo The CTInfo object
     */
    public void setCTInfo(CTInfo ctInfo) {
        if (ctInfo == null) {
            throw new IllegalArgumentException("ctInfo cannot be null");
        }
        this.ctInfo = ctInfo;
    }

    /**
     * Sets the field-level
     *
     * @param fieldLevel the Boolean value of FieldLevel property
     */
    public void setFieldLevel(Boolean fieldLevel) {
        this.fieldLevel = (fieldLevel.booleanValue()) ? fieldLevel : null;
    }

    /**
     * Gets the value of FieldLevel property
     *
     * @return true if the sender is able to apply field-level replaces for the
     *         corresponding CTType property, otherwise false
     */
    public boolean isFieldLevel() {
        return (fieldLevel != null);
    }

    /**
     * Gets the value of FieldLevel property
     *
     * @return true if the sender is able to apply field-level replaces for the
     *         corresponding CTType property, otherwise false
     */
    public Boolean getFieldLevel() {
        if (fieldLevel == null || !fieldLevel.booleanValue()) {
            return null;
        }
        return fieldLevel;
    }

    /**
     * Get an array of supported properties of a given content type
     *
     * @return an array of supported properties
     */
    public Vector getProperties() {
        return this.properties;
    }

    /**
     * Sets an array of supported properties of a given content type
     *
     * @param properties an array of supported properties
     */
    public void setProperties(Property[] properties) {

        if (properties == null || properties.length == 0) {
            throw new IllegalArgumentException("properties cannot be null");
        }
        this.properties.removeAllElements();
        for(int i=0;i<properties.length;++i) {
            this.properties.addElement(properties[i]);
        }
    }

    /**
     * Sets an array of supported properties of a given content type
     *
     * @param properties an array of supported properties
     */
    public void setProperties(Vector properties) {

        if (properties == null || properties.size() == 0) {
            throw new IllegalArgumentException("properties cannot be null");
        }
        this.properties.removeAllElements();
        for(int i=0;i<properties.size();++i) {
            this.properties.addElement(properties.elementAt(i));
        }
    }

    public void addProperty(Property property) {
        if (property == null) {
            throw new IllegalArgumentException("property cannot be null");
        }
        properties.addElement(property);
    }
}
