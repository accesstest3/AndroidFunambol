/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2005 - 2007 Funambol, Inc.
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
 * This class represents the supported parameters of the property object such as
 * parameter name, data type, an array of enumerated value of the content type
 * property and the display name.
 */
public class PropParam {

    // ------------------------------------------------------------ Private data
    private String    paramName  ;
    private String    dataType   ;
    private Vector    valEnums = new Vector();
    private String    displayName;
    // ------------------------------------------------------------ Constructors

    /** For serialization purposes */
    public PropParam() {}

    /**
     * Creates a new PropParam object with the given parameter name, data type,
     * enumerated values and display name
     *
     * @param paramName   The parameter's name - NOT NULL
     * @param dataType    The parameter's data type
     * @param valEnums    An array of enumerated values of the parameter
     * @param displayName The parameter's display name
     */
    public PropParam(final String   paramName  ,
                     final String   dataType   ,
                     final String[] valEnums   ,
                     final String   displayName) {
        this.paramName   = paramName  ;
        this.dataType    = dataType   ;
        setValEnums(valEnums);
        this.displayName = displayName;
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Gets the parameter's name
     *
     * @return the parameter's name
     */
    public String getParamName() {
        return paramName;
    }

    /**
     * Sets the parameter's name
     *
     * @param paramName the parameter's name
     */
    public void setParamName(String paramName) {
        if (paramName == null){
            throw new IllegalArgumentException("paramName cannot be null");
        }
        this.paramName = paramName;
    }

    /**
     * Gets the parameter's data type
     *
     * @return the parameter's data type
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the data type of the parameter
     *
     * @param dataType the data type of the parameter
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * Gets the array of enumerated values of the parameter
     *
     * @return the array of enumerated values of the parameter
     */
    public Vector getValEnums() {
        return this.valEnums;
    }

    /**
     * Sets the array of enumerated values of the parameter
     *
     * @param valEnums the array of enumerated values of the parameter
     */
    public void setValEnums(String[] valEnums) {
        if (valEnums != null) {
            this.valEnums.removeAllElements();
            for(int i=0;i<valEnums.length;++i) {
                this.valEnums.addElement(valEnums[i]);
            }
        } else {
            this.valEnums = null;
        }
    }

    /**
     * Sets the array of enumerated values of the parameter
     *
     * @param valEnums the array of enumerated values of the parameter
     */
    public void setValEnums(Vector valEnums) {
        if (valEnums != null) {
            this.valEnums.removeAllElements();
            for(int i=0;i<valEnums.size();++i) {
                this.valEnums.addElement(valEnums.elementAt(i));
            }
        } else {
            this.valEnums = null;
        }
    }

    public void addValEnum(String val) {
        valEnums.addElement(val);
    }

    /**
     * Gets the parameter's display name
     *
     * @return the parameter's display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the parameter's display name
     *
     * @param displayName the parameter's display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
