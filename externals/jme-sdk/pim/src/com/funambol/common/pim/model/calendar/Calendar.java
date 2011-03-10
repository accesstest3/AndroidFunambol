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
package com.funambol.common.pim.model.calendar;

import java.util.List;
import java.util.ArrayList;

import com.funambol.common.pim.model.common.Property;
import com.funambol.common.pim.model.common.XTag;

/**
 * An object representing a calendar item with all the information supported by 
 * Funambol it contains.
 * This is the "foundational model" of a calendar item, used to exchange
 * information about such items between server and connectors. It can also be
 * used by clients.
 *
 * @version $Id: Calendar.java,v 1.2 2007-11-28 11:14:04 nichele Exp $
 */
public class Calendar {
    
    private Property   calScale;
    private Property   method  ;
    private Property   prodId  ;
    private Property   version ;
    private List<XTag> xTags   ;
    private Event      event   ;
    private Task       todo    ;

    /**
     * Creates an empty calendar with no calendar content.
     */
    public Calendar() {
        calScale = new Property();
        method   = new Property();
        prodId   = new Property();
        version  = new Property();
        xTags    = null;
    }
    
    /**
     * Creates an empty calendar with the given calendar content.
     * 
     * @param cc the event or task contained
     */
    public Calendar(CalendarContent cc) {
        calScale = new Property();
        method   = new Property();
        prodId   = new Property();
        version  = new Property();
        xTags    = null;
        this.setCalendarContent(cc);
    }

    /**
     * Returns the product ID of this calendar.
     *
     * @return the product ID of this calendar
     */
    public Property getProdId() {
        return prodId;
    }

    /**
     * Returns the version of this calendar.
     *
     * @return the version of this calendar
     */
    public Property getVersion() {
        return version;
    }

    /**
     * Returns the calendar scale of this calendar.
     *
     * @return the calendar scale of this calendar
     */
    public Property getCalScale() {
        return calScale;
    }


    /**
     * Returns the "object method or transaction semantics" of this calendar.
     *
     * @return the "object method or transaction semantics" of this calendar
     */
    public Property getMethod() {
        return method;
    }

    /**
     * Returns the event contained in this calendar.
     *
     * @return the event contained in this calendar, if there is one; null
     *         otherwise
     */
    public Event getEvent() {
        return this.event;
    }

    /**
     * Returns the task contained in this calendar.
     *
     * @return the event contained in this calendar, if there is one; null
     *         otherwise
     */
    public Task getTask() {
        return this.todo;
    }
    
    /**
     * Returns the XTag objects, representing custom properties, contained in
     * this calendar.
     *
     * @return a List of XTag objects
     */
    public List<XTag> getXTags() {
       return xTags;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("Calendar:");
        Property tmp;

        tmp = this.getCalScale();
        if (tmp != null) {
            sb.append("\nCalScale: " + (String)tmp.getPropertyValue());
        }

        tmp = this.getMethod();
        if (tmp != null) {
            sb.append("\nMethod  : " + (String)tmp.getPropertyValue());
        }

        tmp = this.getProdId();
        if (tmp != null) {
            sb.append("\nProdId  : " + (String)tmp.getPropertyValue());
        }

        tmp = this.getVersion();
        if (tmp != null) {
            sb.append("\nVersion : " + (String)tmp.getPropertyValue());
        }

        if(xTags != null) {
            for (int i=0;i<xTags.size();i++) {
                XTag xt = (XTag)xTags.get(i);
                sb.append("\n"+xt.getXTagValue()+" :"+(String)xt.getXTag().getPropertyValue());
            }
        }
        if (this.event != null) {
            sb.append("\nEvent   : " + this.event.toString());
        } else if (this.todo != null) {
            sb.append("\nTodo   : " + this.todo.toString());
        }
        return sb.toString();
    }

    /**
     * Setter for property event.
     * 
     * @param event new value of property event.
     */
    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * Setter for property task.
     * 
     * @param todo new value of property task.
     */
    public void setTask(Task todo) {
        this.todo = todo;
    }
    
    /**
     * Setter for property calScale.
     * 
     * @param calScale new value of property calScale
     */
    public void setCalScale(Property calScale) {
        this.calScale = calScale;
    }

    /**
     * Setter for property method.
     * 
     * @param method new value of property method
     */
    public void setMethod(Property method) {
        this.method = method;
    }

    /**
     * Setter for property prodId.
     * 
     * @param prodId new value of property prodId
     */
    public void setProdId(Property prodId) {
        this.prodId = prodId;
    }

    /**
     * Setter for property version.
     * 
     * @param version new value of property version
     */
    public void setVersion(Property version) {
        this.version = version;
    }

    /**
     * Setter for property xTags.
     * 
     * @param xTags new value of property xTags (must be a List<XTag>)
     */
    public void setXTags(List xTags) {
        this.xTags = xTags;
    }

    /**
     * Adder for xTags list.
     *
     * @param xTag the tag to add
     */
    public void addXTag(XTag xTag) {
        if (xTag == null) {
            return;
        }

        if (xTags == null) {
            xTags = new ArrayList<XTag>();
        }

        for (XTag t : xTags) {
            if (t.getXTagValue().equals(xTag.getXTagValue())) {
                t.getXTag().setPropertyValue(xTag.getXTag().getPropertyValue());
                return;
            }
        }
        xTags.add(xTag);
    }

    /**
     * Gets the calendar content: either an Event or a Task.
     *
     * @return a CalendarContent object (Event or Task)
     */
    public CalendarContent getCalendarContent() {
        if (event != null) {
            return event;
        } else {
            return todo; // hopefully, it's not null
        }
    }
  
    /**
     * Sets the calendar content: either an Event or a Task. The other one is
     * set to null.
     *
     * @param cc a CalendarContent object (Event or Task)
     */    
    public void setCalendarContent(CalendarContent cc) {
        if (cc instanceof Event) {
            event = (Event) cc;
            todo = null;
        } else {
            event = null;
            todo = (Task) cc;
        }
    }
}
