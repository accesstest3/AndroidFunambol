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

import com.funambol.common.pim.model.common.PropertyWithTimeZone;
import com.funambol.common.pim.model.common.Property;
import com.funambol.common.pim.model.common.XTag;

/**
 * An object representing an event contained in a calendar item, with all the 
 * information supported by Funambol it contains.
 * This is the "foundational model" of an event, used to exchange information 
 * about such items between server and connectors. It can also be used by 
 * clients.
 *
 * @version $Id: Event.java,v 1.4 2008-07-17 15:53:16 luigiafassina Exp $
 */
public class Event extends CalendarContent {

    //--------------------------------------------------------------- Properties

    protected PropertyWithTimeZone replyTime;
    protected Property transp;

    /**
     * Returns the reply time.
     * 
     * @return value of property replyTime
     */
    public PropertyWithTimeZone getReplyTime() {
        return replyTime;
    }

    /**
     * Setter for property replyTime.
     * 
     * @param replyTime new value of property replyTime
     */
    public void setReplyTime(PropertyWithTimeZone replyTime) {
        this.replyTime = replyTime;
    }
    
    /**
     * Setter for property replyTime on the basis of a Property (without time 
     * zone).
     * 
     * @param replyTime new value of property replyTime as a Property (the time 
     *                  zone is set to null)
     */
    public void setReplyTime(Property replyTime) {
        this.replyTime = new PropertyWithTimeZone(replyTime, null);
    }

    /**
     * Returns the transparence.
     *
     * @return value of property transp
     */
    public Property getTransp() {
        return transp;
    }

    /**
     * Setter for property transp.
     * 
     * @param transp new value of property transp
     */
    public void setTransp(Property transp) {
        this.transp = transp;
    }

    //------------------------------------------------------------- Constructors

    /**
     * Creates an empty event.
     */
    public Event() {
        super();
        replyTime = new PropertyWithTimeZone();
        transp = new Property();
    }

    //----------------------------------------------------------- Public methods

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("Event:");
        sb.append("\nAccessClass    : ").append(printProperty(this.accessClass));
        sb.append("\nCreated        : ").append(printProperty(this.created));
        sb.append("\nDescription    : ").append(printProperty(this.description));
        sb.append("\nDtStart        : ").append(printProperty(this.dtStart));
        sb.append("\nDtEnd          : ").append(printProperty(this.dtEnd));
        sb.append("\nReplyTime      : ").append(printProperty(this.replyTime));
        sb.append("\nLatitude       : ").append(printProperty(this.latitude));
        sb.append("\nLongitude      : ").append(printProperty(this.longitude));
        sb.append("\nLastModified   : ").append(printProperty(this.lastModified));
        sb.append("\nLocation       : ").append(printProperty(this.location));
        sb.append("\nOrganizer      : ").append(printProperty(this.organizer));
        sb.append("\nPriority       : ").append(printProperty(this.priority));
        sb.append("\nDtStamp        : ").append(printProperty(this.dtStamp));
        sb.append("\nSequence       : ").append(printProperty(this.sequence));
        sb.append("\nStatus         : ").append(printProperty(this.status));
        sb.append("\nUID            : ").append(printProperty(this.uid));
        sb.append("\nUrl            : ").append(printProperty(this.url));
        sb.append("\nDuration       : ").append(printProperty(this.duration));
        sb.append("\nSummary        : ").append(printProperty(this.summary));
        sb.append("\nContact        : ").append(printProperty(this.contact));
        sb.append("\nCategories     : ").append(printProperty(this.categories));
        sb.append("\nTransp         : ").append(printProperty(this.transp));
        sb.append("\nAllDay         : ").append(allDay       );
        sb.append("\nMeetingStatus  : ").append(meetingStatus);
        sb.append("\nMileage        : ").append(mileage      );

        if (xTags != null) {
            for (XTag xt : xTags) {
                sb.append("\n" + xt.getXTagValue() + " :" + 
                          (String) xt.getXTag().getPropertyValue());
            }
        }

        sb.append("\nDAlarm         : ").append(printProperty(this.dalarm));
        sb.append("\nPAlarm         : ").append(printProperty(this.palarm));
        sb.append("\nRecurrencePattern : ").append(this.recurrencePattern);

        return sb.toString();
    }
    
    //-----------------------------------------------------------Private methods
    
    private String printProperty(Property p) {
        return (String) ((p!=null) ? p.getPropertyValueAsString() : p);
    }

}
