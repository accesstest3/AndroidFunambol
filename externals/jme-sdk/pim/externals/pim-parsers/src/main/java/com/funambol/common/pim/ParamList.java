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
package com.funambol.common.pim;

import java.util.*;

/**
 * This objects represents a list of vCard property parameters.
 * The list is based on the informations contained in a list of parser tokens.
 *
 *
 * @version $Id: ParamList.java,v 1.3 2007-11-28 11:14:04 nichele Exp $
 */
public class ParamList {

    protected String encoding;
    protected String chrset;
    protected String language;
    protected String value;
    protected ArrayList typelist;  // A list of Strings containing the types

    protected String altrep;
    protected String cn;
    protected String cutype;
    protected String delegatedFrom;
    protected String delegatedTo;
    protected String dir;
    protected String member;
    protected String partstat;
    protected String related;
    protected String sentby;
    protected ArrayList xProps;

    protected HashMap hash;
    protected HashMap xHash;

    protected final static String ENCODING    = "ENCODING"    ;
    protected final static String CHARSET     = "CHARSET"     ;
    protected final static String LANGUAGE    = "LANGUAGE"    ;
    protected final static String VALUE       = "VALUE"       ;

    public ParamList () {
        hash  = new HashMap();
        xHash = new HashMap();
    }

    public boolean containsKey(String key) {
        return hash.containsKey(key);
    }

    public String getValue(String key) {
        return (String) hash.get(key);
    }

    public int getSize() {
        return hash.size();
    }

    public String getEncoding() {
        if (hash != null && hash.containsKey(ENCODING))
            return (String) hash.get((String)ENCODING);
        else
            return null;
    }

    public String getCharset() {
         if (hash != null && hash.containsKey(CHARSET))
            return (String) hash.get((String)CHARSET);
        else
            return null;
    }

    public String getLanguage() {
         if (hash != null && hash.containsKey(LANGUAGE))
            return (String) hash.get((String)LANGUAGE);
        else
            return null;
    }

    public String getValue() {
         if (hash != null && hash.containsKey(VALUE))
            return (String) hash.get((String)VALUE);
        else
            return null;
    }

    public HashMap getXParams() {
        return xHash;
    }

    public void add(String paramName, String paramValue) {
        //
        // to manage a thing like TEL;TYPE=HOME;TYPE=VOICE:123456
        // Consider like TEL;HOME;VOICE=123456
        // BTW, we keep also at least one TYPE since it is needed
        // handling the PHOTO
        //
        if (paramName.equals("TYPE")) {
            hash.put(paramValue, null);
            //
            // In this way we have at least a TYPE 
            // (this is required to handle the PHOTO)
            //
            hash.put("TYPE", paramValue);
        }
        //
        // to manager thing like N;BASE64:john;defoe;;;
        // Consider like N;ENCODING=BASE64
        //
        else if (paramName.equals("7BIT"            ) |
                 paramName.equals("8BIT"            ) |
                 paramName.equals("QUOTED-PRINTABLE") |
                 paramName.equals("BASE64"          )
                ) {
            hash.put(ENCODING, paramName);
        }
        //
        // to manager thing like N;INLINE:john;defoe;;;
        // Consider like N;VALUE=INLINE:john;defoe;;;
        //
        else if (paramName.equals("URL"        ) |
                 paramName.equals("INLINE"     ) |
                 paramName.equals("CONTENT-ID" ) |
                 paramName.equals("CID"        )
                ) {
            hash.put(VALUE, paramName);
        }
        else if (paramName.startsWith("X-")) {
            xHash.put(paramName, paramValue);
        }
        else
            hash.put(paramName, paramValue);

    }

    /**
     * Returns the size of the list of TYPE parameters.
     *
     * @return the size of the list of TYPE parameters
     */
    public int getTypeListSize() {
        return typelist.size();
    }

    /**
     * Returns the list of TYPE parameters formatted in a comma-separated format.
     *
     * @return a formatted list of TYPE parameters
     */
    public String getFormattedTypeList() {
        String result = "";
        for (int i = 0; i < typelist.size(); i++) {
            result += typelist.get(i)+" ";
        }
        return result.trim();
    }

    /**
     * Checks if the type list contains the specified item
     *
     * @param item
     * @return true if the item is contained in the list, false otherwise
     */
    public boolean typeListContains(String item) {
        for (int i = 0; i < typelist.size(); i++) {
            if (((String) typelist.get(i)).toUpperCase().equals(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value of the ALTREP parameter.
     *
     * @return the value of the ALTREP parameter.
     */
    public String getAltrep() {
        return altrep;
    }

    /**
     * Returns the value of the CN parameter.
     *
     * @return the value of the CN parameter.
     */
    public String getCn() {
        return cn;
    }

    /**
     * Returns the value of the CUTYPE parameter.
     *
     * @return the value of the CUTYPE parameter.
     */
    public String getCutype() {
        return cutype;
    }

    /**
     * Returns the value of the DELEGATED-FROM parameter.
     *
     * @return the value of the DELEGATED-FROM parameter.
     */
    public String getDelegatedFrom() {
        return delegatedFrom;
    }

    /**
     * Returns the value of the DELEGATED-TO parameter.
     *
     * @return the value of the DELEGATED-TO parameter.
     */
    public String getDelegatedTo() {
        return delegatedTo;
    }

    /**
     * Returns the value of the DIR parameter.
     *
     * @return the value of the DIR parameter.
     */
    public String getDir() {
        return dir;
    }

    /**
     * Returns the value of the MEMBER parameter.
     *
     * @return the value of the MEMBER parameter.
     */
    public String getMember() {
        return member;
    }

    /**
     * Returns the value of the PARTSTAT parameter.
     *
     * @return the value of the PARTSTAT parameter.
     */
    public String getPartstat() {
        return partstat;
    }

    /**
     * Returns the value of the RELATED parameter.
     *
     * @return the value of the RELATED parameter.
     */
    public String getRelated() {
        return related;
    }

    /**
     * Returns the value of the SENT-BY parameter.
     *
     * @return the value of the SENT-BY parameter.
     */
    public String getSentby() {
        return sentby;
    }

    /**
     * Returns the list of X-PROP parameters.
     *
     * @return the the list of X-PROP parameters
     */
    public ArrayList getXProps() {
        return xProps;
    }

    /**
     * Returns the size of the list of X-PROP parameters.
     *
     * @return the size of the list of X-PROP parameters
     */
    public int getXPropsSize() {
        return xProps.size();
    }

    /**
     * Returns the list of X-PRO parameters formatted in a comma-separated format.
     *
     * @return a formatted list of X-PRO parameters
     */
    public String getFormattedXProps() {
        String result = "";
        for (int i = 0; i < xProps.size(); i++) {
            result += xProps.get(i)+" ";
        }
        return result.trim();
    }

    /**
     * Checks if the xProps list contains the specified item
     *
     * @param item
     * @return true if the item is contained in the list, false otherwise
     */
    public boolean xPropsContains(String item) {
        for (int i = 0; i < xProps.size(); i++) {
            if (((String) xProps.get(i)).toUpperCase().equals(item)) {
                return true;
            }
        }
        return false;
    }
}
