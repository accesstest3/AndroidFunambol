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
 * This class represents the filtering capabilities
 */
public class FilterCap {

    // ------------------------------------------------------------ Private data
    private CTInfo       ctInfo                          ;
    private Vector       filterKeywords = new Vector();
    private Vector       propNames      = new Vector();

    // ------------------------------------------------------------ Constructors
    /**
     * In order to expose the server configuration like WS this constructor
     * must be public
     */
    public FilterCap() {}

    /**
     * Creates a new FilterCap object with the given input information
     *
     * @param ctInfo The type and version of a supported content type - NOT NULL
     * @param filterKeyword The record level filter keyword
     * @param propName      The name of a supported property
     */
    public FilterCap(final CTInfo   ctInfo        ,
                     final String[]        filterKeywords,
                     final String[]        propNames     ) {
        setCTInfo(ctInfo);
        setFilterKeywords(filterKeywords);
        setPropNames(propNames);
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
     * Get the record level filter keyword
     *
     * @return the record level filter keyword
     */
    public Vector getFilterKeywords() {
        return this.filterKeywords;
    }

    /**
     * Sets the record level filter keyword
     *
     * @param filterKeywords The record level filter keyword
     */
    public void setFilterKeywords(String[] filterKeywords) {
        if (filterKeywords != null) {
            this.filterKeywords.removeAllElements();
            for(int i=0;i<filterKeywords.length;++i) {
                this.filterKeywords.addElement(filterKeywords[i]);
            }
        } else {
            this.filterKeywords = null;
        }
    }

    /**
     * Sets the record level filter keyword
     *
     * @param filterKeywords The record level filter keyword
     */
    public void setFilterKeywords(Vector filterKeywords) {
        if (filterKeywords != null) {
            this.filterKeywords.removeAllElements();
            for(int i=0;i<filterKeywords.size();++i) {
                this.filterKeywords.addElement(filterKeywords.elementAt(i));
            }
        } else {
            this.filterKeywords = null;
        }
    }

    /**
     * Get the name of a supported property
     *
     * @return the name of a supported property
     */
    public Vector getPropNames() {
        return this.propNames;
    }

    /**
     * Sets the name of a supported property
     *
     * @param propNames The name of a supported property
     */
    public void setPropNames(String[] propNames) {
        if (propNames != null) {
            this.propNames.removeAllElements();
            for(int i=0;i<propNames.length;++i) {
                this.propNames.addElement(propNames[i]);
            }
        } else {
            this.propNames = null;
        }
    }

    /**
     * Sets the name of a supported property
     *
     * @param propNames The name of a supported property
     */
    public void setPropNames(Vector propNames) {
        if (propNames != null) {
            this.propNames.removeAllElements();
            for(int i=0;i<propNames.size();++i) {
                this.propNames.addElement(propNames.elementAt(i));
            }
        } else {
            this.propNames = null;
        }
    }
}
