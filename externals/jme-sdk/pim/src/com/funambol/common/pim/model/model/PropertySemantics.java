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

import java.util.Hashtable;

/**
 * The PropertySemantics class provides the limitations of the
 * properties.
 *
 * @version $Id: PropertySemantics.java,v 1.2 2007-11-28 11:14:05 nichele Exp $
 */
public class PropertySemantics {
    public String name;
    private ValueInterface value;
    private ParameterSemantics[] parameters;
    private int min;
    private int max;

    /**
     * The constructor for the semantics of a property.
     * @param name the name of the property (String).
     * @param value the value class of the property (ValueInterface)
     * @param registry The quick lookup to register the property semantics.
     */
    public PropertySemantics(String name, ValueInterface value,
                             Hashtable registry) {
        this(name, value, new ParameterSemantics[0], 0, 1, registry);
    }

    /**
     * The constructor for the semantics of a property.
     * Upon creation the property is also registered in the lookup table.
     * @param name the name of the property (String).
     * @param value the value class of the property (ValueInterface)
     * @param parameters the parameters allowed for the property (ParameterSematics[]).
     * @param min the minimum amount of occurences
     * @param max the maximum amount of occurences
     * @param registry The quick lookup to register the property semantics.
     */
    public PropertySemantics(String name, ValueInterface value,
                             ParameterSemantics[] parameters,
                             int min, int max, Hashtable registry) {
        this.name = name;
        this.value = value;
        this.parameters = parameters;
        this.min = min;
        this.max = max;
        if (registry != null) {
            registry.put(this.name, this);
        }
    }

    /**
     * Perform a check for the value of the property if it is according to
     * the standard.
     * @param v The String based value.
     * @return True if the value is allowed according to the specification.
     */
    public boolean checkValue(String v) {
        return value.checkValue(v);
    }
    /**
     * Perform a check if the parameter (name and value) of the property
     * are allowed according to the standard.
     * @param pn The name of the parameter.
     * @param pv The String based value of the parameter.
     * @return True if the parameter is allowed according to the specification.
     */
    public boolean checkParameter(String pn, String pv) {
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                if (pn.equals(parameters[i].name)) {
                    return parameters[i].checkValue(pv);
                }
            }
        }
        return false;
    }
    public int getMaxOccurrences() {
        return max;
    }
}
