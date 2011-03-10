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
 * The ParameterSemantics class provides the limitations of the
 * parameters.
 *
 * @version $Id: ParameterSemantics.java,v 1.2 2007-11-28 11:14:05 nichele Exp $
 */
public class ParameterSemantics {
    public String name;
    private com.funambol.common.pim.model.model.ValueInterface value;
    private boolean multiple;

    /**
     * The constructor for the semantics of a parameter
     * @param name the name of the parameter(String).
     * @param value the value class of the paramter (ValueInterface)
     * @param registry The quick lookup to register the parameter semantics.
     */
    public ParameterSemantics(String name, com.funambol.common.pim.model.model.ValueInterface value,
                              Hashtable registry) {
        this(name, value, false, registry);
    }
    /**
     * The constructor for the semantics of a parameter
     * @param name the name of the parameter(String).
     * @param value the value class of the paramter (ValueInterface)
     * @param multiple the boolean indicator whether multiple values are allowed
     * @param registry The quick lookup to register the parameter semantics.
     */
    public ParameterSemantics(String name, com.funambol.common.pim.model.model.ValueInterface value,
                              boolean multiple,
                              Hashtable registry) {
        this.name = name;
        this.value = value;
        this.multiple = multiple;
        registry.put(name, this);
    }

    /**
     * Perform a check for the value of the parameter if it is according to
     * the standard.
     * @param v The String based value.
     * @return True if the value is allowed according to the specification.
     */
    public boolean checkValue(String v) {
        return value.checkValue(v);
    }
}
