/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2009 Funambol, Inc.
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

package com.funambol.common.pim.vcalendar;

import java.util.Vector;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.Event;
import javax.microedition.pim.ToDo;
import javax.microedition.pim.PIMList;

import com.funambol.util.Log;
import com.funambol.util.StringUtil;

import com.funambol.common.pim.xvcalendar.ParseException;
import com.funambol.common.pim.ParserProperty;
import com.funambol.common.pim.ParserParam;
import com.funambol.common.pim.Utils;
import com.funambol.common.pim.ArrayList;
import com.funambol.common.pim.icalendar.VAlarm;

/**
 * <p>This class implements the a basic vCalendar parser listener which manages 
 * the parsing of common properties shared by the vCalendar 1.0 and iCalendar
 * (vCalendar 2.0) formats, and fills those common properties to a JSR75 PIMItem
 * object.</p>
 *
 * <p>Assumptions:
 * <li> <i>vCalendar</i> represents a vCalendar version 1.0 </li>
 * <li> <i>iCalendar</i> represents a vCalendar version 2.0 </li>
 * </p>
 * 
 * <p>NOTE: Implementations of the iCalendar and vCalendar parser listeners 
 * should extend this class.</p>
 *
 */
public abstract class BasicVCalendarParserListener {

    private final String TAG_LOG = "BasicVCalendarParserListener";

    protected PIMItem pimItem   = null;
    protected PIMList pimList   = null;
    protected Utils   pimUtils  = new Utils(defaultCharset);

    private static final String defaultCharset = BasicVCalendar.UTF8;

    /** Check whether this is an allday event */
    protected boolean allDay = false;

    /** Specifies the alarm related times, used to compute the alarm interval */
    protected long alarmStartRelatedTime = CalendarUtils.UNDEFINED_TIME;
    protected long alarmEndRelatedTime =   CalendarUtils.UNDEFINED_TIME;

    // Used to keep track alarm infos
    protected VAlarm  alarm = null;

    /**
     * The construtor accepts a <code>PIMItem</code> object that will be
     * populated of all the common vCalendar properties.
     * @param pimItem the PIMItem object
     */
    public BasicVCalendarParserListener(PIMItem pimItem) {
        this.pimItem = pimItem;
        this.pimList = (PIMList) pimItem.getPIMList();
    }

    /**
     * Called when a new attendee is found during the parsing. It should be
     * redefined by implementations which support this information.
     * @param value
     */
    protected void addAttendee(String value) { }

    public void addEventProperty(ParserProperty property) throws Exception {

        Log.trace(TAG_LOG, "addEventProperty: " + property.getName());

        String name  = property.getName();
        String value = getClearValue(property);

        if(pimItem instanceof Event) {
            if (StringUtil.equalsIgnoreCase(BasicVCalendar.SUMMARY, name)) {
                if (pimList.isSupportedField(Event.SUMMARY)) {
                    setSummary(Event.SUMMARY, value);
                }
            } else if (StringUtil.equalsIgnoreCase(BasicVCalendar.CLASS, name)) {
                if (pimList.isSupportedField(Event.CLASS)) {
                    setClass(Event.CLASS, value);
                }
            } else if (StringUtil.equalsIgnoreCase(BasicVCalendar.DESCRIPTION, name)) {
                if (pimList.isSupportedField(Event.NOTE)) {
                    setNote(Event.NOTE, value);
                }
            } else if (StringUtil.equalsIgnoreCase(BasicVCalendar.UID, name)) {
                if (pimList.isSupportedField(Event.UID)) {
                    setUID(Event.UID, value);
                }
            } else if (StringUtil.equalsIgnoreCase(BasicVCalendar.LAST_MODIFIED, name)) {
                if (pimList.isSupportedField(Event.REVISION)) {
                    setRevision(Event.REVISION, value);
                }
            } else if (StringUtil.equalsIgnoreCase(BasicVCalendar.LOCATION, name)) {
                if (pimList.isSupportedField(Event.LOCATION)) {
                    setLocation(value);
                }
            } else if (StringUtil.equalsIgnoreCase(BasicVCalendar.ATTENDEE, name)) {
                addAttendee(value);
            } else {
                Log.error(TAG_LOG, "Unsupported property: " + name);
            }
        }
    }

    public void addToDoProperty(ParserProperty property) throws Exception {
        Log.trace(TAG_LOG, "addToDoProperty: " + property.getName());

        String name  = property.getName();
        String value = getClearValue(property);

        if(pimItem instanceof ToDo) {
            if (StringUtil.equalsIgnoreCase(BasicVCalendar.SUMMARY, name)) {
                if (pimList.isSupportedField(ToDo.SUMMARY)) {
                    setSummary(ToDo.SUMMARY, value);
                }
            } else if (StringUtil.equalsIgnoreCase(BasicVCalendar.CLASS, name)) {
                if (pimList.isSupportedField(ToDo.CLASS)) {
                    setClass(ToDo.CLASS, value);
                }
            } else if (StringUtil.equalsIgnoreCase(BasicVCalendar.DESCRIPTION, name)) {
                if (pimList.isSupportedField(ToDo.NOTE)) {
                    setNote(ToDo.NOTE, value);
                }
            } else if (StringUtil.equalsIgnoreCase(BasicVCalendar.UID, name)) {
                if (pimList.isSupportedField(ToDo.UID)) {
                    setUID(ToDo.UID, value);
                }
            } else if (StringUtil.equalsIgnoreCase(BasicVCalendar.LAST_MODIFIED, name)) {
                if (pimList.isSupportedField(ToDo.REVISION)) {
                    setRevision(ToDo.REVISION, value);
                }
            } else if (StringUtil.equalsIgnoreCase(BasicVCalendar.STATUS, name)) {
                if (pimList.isSupportedField(ToDo.COMPLETED)) {
                    setStatus(value);
                }
            } else if (StringUtil.equalsIgnoreCase(BasicVCalendar.PRIORITY, name)) {
                if (pimList.isSupportedField(ToDo.PRIORITY)) {
                    setPriority(value);
                }
            } else if (StringUtil.equalsIgnoreCase(BasicVCalendar.ATTENDEE, name)) {
                addAttendee(value);
            } else {
                Log.error(TAG_LOG, "Unsupported property: " + name);
            }
        }
    }

    protected void setSummary(int pimField, String value) {
        Log.trace(TAG_LOG, "setSummary");
        pimItem.addString(pimField, 0, value);
    }
    protected void setRevision(int pimField, String value) {
        Log.trace(TAG_LOG, "setRevision");
        if(pimItem.countValues(pimField) == 0) {
            pimItem.addDate(pimField, 0, CalendarUtils.getLocalDateTime(value, "GMT"));
        }
    }
    protected void setNote(int pimField, String value) {
        Log.trace(TAG_LOG, "setNote");
        pimItem.addString(pimField, 0, value);
    }
    protected void setClass(int pimField, String value) {
        Log.trace(TAG_LOG, "setClass");
        int classValue;
        if (StringUtil.equalsIgnoreCase(value, BasicVCalendar.CLASS_PRIVATE)) {
            classValue = (pimItem instanceof Event) ? Event.CLASS_PRIVATE:
                                                      ToDo.CLASS_PRIVATE;
        } else if (StringUtil.equalsIgnoreCase(value, BasicVCalendar.CLASS_CONFIDENTIAL)) {
            classValue = (pimItem instanceof Event) ? Event.CLASS_CONFIDENTIAL:
                                                      ToDo.CLASS_CONFIDENTIAL;
        } else {
            // Default value is PUBLIC
            classValue = (pimItem instanceof Event) ? Event.CLASS_PUBLIC:
                                                      ToDo.CLASS_PUBLIC;
        }
        pimItem.addInt(pimField, 0, classValue);
    }
    protected void setUID(int pimField, String value) {
        Log.trace(TAG_LOG, "setUID");
        pimItem.addString(pimField, 0, value);
    } 
    protected void setLocation(String value) {
        Log.trace(TAG_LOG, "setLocation");
        pimItem.addString(Event.LOCATION, Event.ATTR_NONE, value);
    }
    protected void setStatus(String value) {
        Log.trace(TAG_LOG, "setStatus");
        boolean completed = StringUtil.equalsIgnoreCase(value, BasicVCalendar.STATUS_COMPLETED);
        pimItem.addBoolean(ToDo.COMPLETED, ToDo.ATTR_NONE, completed);
    }
    protected void setPriority(String value) {
        Log.trace(TAG_LOG, "setPriority");
        pimItem.addInt(ToDo.PRIORITY, ToDo.ATTR_NONE, Integer.parseInt(value));
    }

    protected void setEventAlarm(VAlarm alarm) throws Exception {
        Log.trace(TAG_LOG, "setEventAlarm");
        if (alarm != null && pimList.isSupportedField(Event.ALARM)) {
            alarm.setCalStartAbsoluteTime(alarmStartRelatedTime);
            alarm.setCalEndAbsoluteTime(alarmEndRelatedTime);
            int interval = (int)alarm.getAlarmInterval()/1000;
            if(interval != CalendarUtils.UNDEFINED_TIME) {
                pimItem.addInt(Event.ALARM, 0, interval);
            }
        }
    }
    
    /**
     * Get the clear value from ParserProperty: unfolded, decoded, unescaped
     * @param property
     * @return the clear value
     */
    protected String getClearValue(ParserProperty property) {

        String enc = getEncoding(property.getParameters());
        String charset = getCharset(property.getParameters());
        String value = property.getValue();

        value = pimUtils.unfold(value);
        value = pimUtils.decode(value, enc, charset);
        value = pimUtils.unescape(value);
        return value;
    }

    /**
     * Get a parameter value from the specified params array
     * @param params the array of the params
     * @param paramName the param name
     * @return the param value
     */
    protected String getParameter(ArrayList params, String paramName) {
        for (int i=0;i<params.size();++i) {
            ParserParam param = (ParserParam) params.elementAt(i);
            if (StringUtil.equalsIgnoreCase(paramName, param.getName())) {
                return param.getValue();
            }
        }
        return null;
    }
    protected String getEncoding(ArrayList params) {
        return getParameter(params, BasicVCalendar.ENCODING);
    }
    protected String getCharset(ArrayList params) {
        return getParameter(params, BasicVCalendar.CHARSET);
    }
}
