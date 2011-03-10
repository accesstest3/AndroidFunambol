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
package com.funambol.common.pim.model.common;

import java.util.Iterator;
import com.funambol.common.pim.model.model.Property;
import com.funambol.common.pim.model.model.VCalendar;
import com.funambol.common.pim.model.model.VEvent;
import com.funambol.common.pim.model.model.VTodo;
import com.funambol.common.pim.model.model.VNote;
import com.funambol.common.pim.model.model.VTimezone;
import com.funambol.common.pim.model.model.Parameter;

/**
 * This class represents a "walk" through a VisitorObject instance.
 * 
 * @version $Id: VisitorObjectWalk.java,v 1.2 2007-11-28 11:14:04 nichele Exp $
 */
public class VisitorObjectWalk implements VisitorObject {
    
    public Object visitVCalendar(VCalendar vc, Object arg) 
    throws VisitorException {
        Object result = arg;
        Iterator pIter = vc.getAllProperties().iterator();
        while (pIter.hasNext()) {
            VisitorInterface p = (VisitorInterface)pIter.next();
            result = p.accept(this, result);
        }
        Iterator cIter = vc.getAllComponents().iterator();
        while (cIter.hasNext()) {
            VisitorInterface c = (VisitorInterface)cIter.next();
            result = c.accept(this, result);
        }
        return result;
    }

    public Object visitVComponent(VEvent ve, Object arg) 
    throws VisitorException {
        Object result = arg;
        Iterator pIter = ve.getAllProperties().iterator();
        while (pIter.hasNext()) {
            VisitorInterface p = (VisitorInterface)pIter.next();
            result = p.accept(this, result);
        }
        return result;
    }

    public Object visitVComponent(VTodo vt, Object arg) 
    throws VisitorException {
        Object result = arg;
        Iterator pIter = vt.getAllProperties().iterator();
        while (pIter.hasNext()) {
            VisitorInterface p = (VisitorInterface)pIter.next();
            result = p.accept(this, result);
        }
        return result;
    }
    
    public Object visitVComponent(VNote vn, Object arg) 
    throws VisitorException {
       Object result = arg;
       Iterator pIter = vn.getAllProperties().iterator();
       while (pIter.hasNext()) {
           VisitorInterface p = (VisitorInterface)pIter.next();
           result = p.accept(this, result);
       }
       return result;
    }    

    public Object visitVComponent(VTimezone vTimezone, Object argu) {
        return null;
    }

    public Object visitProperty(Property p, Object arg) 
    throws VisitorException {
        Object result = arg;
        Iterator pIter = p.getParameters().iterator();
        while (pIter.hasNext() ) {
            VisitorInterface prmtr = (VisitorInterface)pIter.next();
            result = prmtr.accept(this, result);
        }
        return result;
    }

    public Object visitParameter(Parameter p, Object arg) 
    throws VisitorException {
        Object result = arg;
        return result;
    }
}
