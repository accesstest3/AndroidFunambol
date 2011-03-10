/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2008 Funambol, Inc.
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

import java.util.Hashtable;

/**
 * This object is a replacement of J2SE HashMap, but it does not have the same
 * semantics, so it cannot be used outside of this package. Even within the
 * package it must be used with caution. It is meant to be used by ParamList
 * and it conforms to the J2SE semantics only for what is used by ParamList.
 */

class HashMap extends Hashtable {
    private static final String NULL = "__NULL__";

    public Object put(Object key, Object value) {
        if (key == null) {
            key = NULL;
        }
        if (value == null) {
            value = NULL;
        }
        return super.put(key,value);
    }

    public Object get(Object key) {
        Object res = super.get(key);
        if (res == NULL) {
            return null;
        }
        return res;
    }

    public boolean containsKey(String key) {
        if (key == null) {
            return super.containsKey(NULL);
        } else {
            return super.containsKey(key);
        }
    }
}


