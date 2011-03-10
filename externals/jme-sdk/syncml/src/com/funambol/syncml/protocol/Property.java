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

import java.util.*;

/**
 * This class represents a supported property of a given content type.
 * The parameters to specify a property are name, data type, maximum number of
 * occurences of a property, maximum size in UTF-8 characters, truncation,
 * enumerated value, display name and supported parameter.
 */
public class Property {

    // ------------------------------------------------------------ Private data
    private String    propName   ;
    private String    dataType   ;
    private int       maxOccur   ;
    private int       maxSize    ;
    private Boolean   noTruncate ;
    private Vector    valEnums   = new Vector();
    private String    displayName;
    private Vector    propParams = new Vector();

    // ------------------------------------------------------------ Constructors

    /**
     * In order to expose the server configuration like WS this constructor
     * must be public
     */
    public Property() {}

    /**
     * Creates a new Propety object
     *
     * @param propName The name of a supported property - NOT NULL
     * @param dataType The datatype of a supported property
     * @param maxOccur The maximum number of occurrences of a property of the
     *                 same type supported within a single object
     * @param maxSize  The maximum size for a given datastore (in bytes)
     * @param noTruncate Specify if the property value can exceed the maximum
     *                   size as specified by the MaxSize tag
     * @param valEnums The supported enumerated value of a given CTType property
     * @param displayName The display name of a property
     * @param propParams  The array of supported parameters of a given property
     */
    public Property(final String      propName   ,
                    final String      dataType   ,
                    final int         maxOccur   ,
                    final int         maxSize    ,
                    final boolean     noTruncate ,
                    final String[]    valEnums   ,
                    final String      displayName,
                    final PropParam[] propParams ) {
        setPropName(propName);
        this.dataType    = dataType;
        this.maxOccur    = maxOccur;
        this.maxSize     = maxSize ;
        this.noTruncate  = (noTruncate) ? new Boolean(noTruncate) : null;
        setValEnums(valEnums);
        this.displayName = displayName;
        setPropParams(propParams);
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Gets the property name
     *
     * @return the property name
     */
    public String getPropName() {
        return propName;
    }

    /**
     * Sets the property name
     *
     * @param propName the property name
     */
    public void setPropName(String propName) {
        if (propName == null){
            throw new IllegalArgumentException("propName cannot be null");
        }
        this.propName = propName;
    }

    /**
     * Gets the data type of the property
     *
     * @return the data type of the property
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the data type of the property
     *
     * @param dataType the data type of the property
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * Gets the maximum number of occurrences of the property of the same type
     * supported within a single object
     *
     * @return the maximum number of occurrences
     */
    public int getMaxOccur() {
        return maxOccur;
    }

    /**
     * Sets the maximum number of occurrences of the property of the same type
     * supported within a single object
     *
     * @param maxOccur the maximum number of occurrences of the property
     */
    public void setMaxOccur(int maxOccur) {
        this.maxOccur = maxOccur;
    }

    /**
     * Gets the maximum size for a given datastore (in bytes)
     *
     * @return the maximum size for a given datastore (in bytes)
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Sets the maximum size for a given datastore (in bytes)
     *
     * @param size the maximum size
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Specify if the property value can exceed the maximum size as specified
     * by the MaxSize
     *
     * @param noTruncate the Boolean value of NoTruncate property
     */
    public void setNoTruncate(Boolean noTruncate) {
        this.noTruncate = (noTruncate.booleanValue()) ? noTruncate : null;
    }

    /**
     * Gets the value of NoTruncate property
     *
     * @return true if the property value can exceed the maximum size as
     *              specified by the MaxSize
     */
    public boolean isNoTruncate() {
        return (noTruncate != null);
    }

    /**
     * Gets the value of NoTruncate property
     *
     * @return true if the property value can exceed the maximum size as
     *              specified by the MaxSize
     */
    public Boolean getNoTruncate() {
        if (noTruncate == null || !noTruncate.booleanValue()) {
            return null;
        }
        return noTruncate;
    }

    /**
     * Gets the array of supported enumerated value of the CTType property
     *
     * @return the array of supported enumerated value of the CTType property
     */
    public Vector getValEnums() {
        return this.valEnums;
    }

    /**
     * Sets the array of supported enumerated value of the CTType property
     *
     * @param valEnums the array of supported enumerated value
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
     * Sets the array of supported enumerated value of the CTType property
     *
     * @param valEnums the array of supported enumerated value
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
     * Gets the display name
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name of the property
     *
     * @param displayName the display name of the property
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the array of supported parameters of the property
     *
     * @return the array of supported parameters of the property
     */
    public Vector getPropParams() {
        return this.propParams;
    }

    /**
     * Sets an array of supported parameters of the property
     *
     * @param propParams the array of supported parameters of the property
     */
    public void setPropParams(PropParam[] propParams) {
        if (propParams != null) {
            this.propParams.removeAllElements();
            for(int i=0;i<propParams.length;++i) {
                this.propParams.addElement(propParams[i]);
            }
        } else {
            this.propParams = null;
        }
    }

    /**
     * Sets an array of supported parameters of the property
     *
     * @param propParams the array of supported parameters of the property
     */
    public void setPropParams(Vector propParams) {
        if (propParams != null) {
            this.propParams.removeAllElements();
            for(int i=0;i<propParams.size();++i) {
                this.propParams.addElement(propParams.elementAt(i));
            }
        } else {
            this.propParams = null;
        }
    }

    public void addPropParam(PropParam propParam) {
        propParams.addElement(propParam);
    }
    
    /**
     * @param paramName ParamName's value
     */
    public void addParamName(String paramName) {
        if (propParams == null) {
            propParams = new Vector();
        }
        
        if (propParams.isEmpty()) {
            PropParam p = new PropParam();
            p.setDataType("UNKNOWN");
            p.setParamName(this.propName);
            
            propParams.addElement(p);
        }
        
        ((PropParam)(propParams.elementAt(0))).getValEnums().addElement(paramName);
    }
}
