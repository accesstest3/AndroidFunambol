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
 * This objects represents a list of vCard fields. The list is based
 * on the informations contained in a list of parser tokens
 *
 * @version $Id: FieldsList.java,v 1.2 2007-11-28 11:14:04 nichele Exp $
 */
public class FieldsList extends ArrayList {

    /**
     * Constructs a list of tokens starting from head and ending at tail, parses
     * the list and extracts informations about fields
     */
    public FieldsList () {
        super();
    }

    /**
     * Parse the list of values separated by ; and add the single values
     * at the vector.
     *
     * @param listValues the list of values specificated in the token
     */
    public void addValue(String listValues) {

        if (listValues.indexOf(";") == -1) {
           this.add(listValues);
           return;
        }

        //
        // This is for reading the last element too
        //
        listValues = listValues + ';';

        StringBuffer value = new StringBuffer();
        int length = listValues.length();
        boolean foundSlash = false;
        for (int i=0; i<length; i++) {
            char ch = listValues.charAt(i);

            //Unescape of backslash and semicolon
            switch (ch) {
                case '\\':
                    if (foundSlash) {
                        foundSlash = false;
                        value.append('\\');
                    } else {
                        foundSlash = true;
                    }
                    break;
                case ';':
                    if (foundSlash) {
                        value.append(';');
                        foundSlash = false;
                    } else {
                        this.add(value.toString());
                        value = new StringBuffer();
                    }
                    break;
                default:
                    if (foundSlash) {
                        foundSlash = false;
                        value.append('\\');
                    }
                    value.append(ch);
                    break;
            }
        }
        if (value.length() != 0) {
            this.add(value.toString());
        }
    }

    /**
     * Method used to insert all the listValues separated by semicolons
     * as a string. It's needed i.e. when a tag like NOTE is parsed. Its argument
     * value is to be consider as a unique string with no token
     *
     * @param listValues the list of values contains into token
     */
    public void addValueAsString(String listValues) {
        if (listValues != null) {
            this.add(listValues);
        } else {
            this.add("");
        }
    }

    /**
     * Return the element contains at the specificated index
     *
     * @param index the index of the element
     * @return String the element at the specifated index
     */
    public String getElementAt(int index) {
        return (String)get(index);
    }

    public String getTheOnlyElement() {
        if (get(0) != null)
            return (String)get(0);
        else
            return "";
    }
}
