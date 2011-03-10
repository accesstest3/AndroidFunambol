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

import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.Event;
import javax.microedition.pim.ToDo;

import java.io.OutputStream;

import com.funambol.common.pim.PimUtils;
import com.funambol.common.pim.icalendar.VAlarm;

import com.funambol.util.QuotedPrintable;
import com.funambol.util.Log;

/**
 * <p>This class implements the basic vCalendar formatter listener which manages
 * the formatting of common properties shared by the vCalendar 1.0 and iCalendar
 * (vCalendar 2.0) formats.
 * 
 * <p>NOTE: Implementations of the iCalendar and vCalendar formatters
 * should extend this class.</p>
 *
 */
public abstract class BasicVCalendarFormatter {

    private final String LOG_TAG = "BasicVCalendarFormatter";

    protected String defaultCharset = BasicVCalendar.UTF8;
    protected PimUtils pimUtils = new PimUtils(defaultCharset);

    protected VAlarm alarm = new VAlarm();

    /**
     * Create a new BasicVCalendarFormatter using the provided default charset
     * @param defaultCharset
     */
    public BasicVCalendarFormatter(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    public BasicVCalendarFormatter() { }

    /**
     * Format the vCalendar item.
     * 
     * @param pimItem the PIMItem to format
     * @param os the output stream
     * @throws javax.microedition.pim.PIMException
     */
    public void format(PIMItem pimItem, OutputStream os, boolean allFields)
            throws PIMException {
        Log.trace(LOG_TAG, "format");
        PimUtils.println(os, BasicVCalendar.BEGIN_VCALENDAR);
        PimUtils.println(os, getVersion());
        formatTimezone(pimItem, os);
        if(pimItem instanceof Event) {
            // this is an Event
            formatEvent(pimItem, os, allFields);
        } else if(pimItem instanceof ToDo) {
            // this is a ToDo
            formatToDo(pimItem, os, allFields);
        }
        PimUtils.println(os, BasicVCalendar.END_VCALENDAR);
    }

    /**
     * Format the Event common properties
     * 
     * @param pimItem the PIMItem to format
     * @param os the output stream
     * @throws javax.microedition.pim.PIMException
     */
    protected void formatEvent(PIMItem pimItem, OutputStream os, boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatEvent");

        PimUtils.println(os, BasicVCalendar.BEGIN_VEVENT);

        formatSummary       (pimItem, Event.SUMMARY, os, allFields);
        formatLocation      (pimItem, os, allFields);
        formatDTStart       (pimItem, os, allFields);
        formatDTEnd         (pimItem, os, allFields);
        formatRevision      (pimItem, Event.REVISION, os, allFields);
        formatNote          (pimItem, Event.NOTE, os, allFields);
        formatUID           (pimItem, Event.UID, os, allFields);
        formatClass         (pimItem, Event.CLASS, os, allFields);
        formatAlarm         (pimItem, os, allFields);
        formatRRule         (pimItem, os, allFields);
        formatAttendees     (pimItem, os, allFields);
        formatCategories    (pimItem, os, allFields);
        formatFunambolAllday(pimItem, os, allFields);

        PimUtils.println(os, BasicVCalendar.END_VEVENT);
    }

    /**
     * Format the ToDo common properties
     *
     * @param pimItem the PIMItem to format
     * @param os the output stream
     * @throws javax.microedition.pim.PIMException
     */
    protected void formatToDo(PIMItem pimItem, OutputStream os, boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatToDo");

        PimUtils.println(os, BasicVCalendar.BEGIN_VTODO);

        formatSummary       (pimItem, ToDo.SUMMARY, os, allFields);
        formatDue           (pimItem, os, allFields);
        formatNote          (pimItem, ToDo.NOTE, os, allFields);
        formatStatus        (pimItem, os, allFields);
        formatRevision      (pimItem, ToDo.REVISION, os, allFields);
        formatUID           (pimItem, ToDo.UID, os, allFields);
        formatClass         (pimItem, ToDo.CLASS, os, allFields);
        formatPriority      (pimItem, os, allFields);
        formatRRule         (pimItem, os, allFields);
        formatAlarm         (pimItem, os, allFields);
        formatCompleted     (pimItem, os, allFields);
        formatFunambolAllday(pimItem, os, allFields);

        PimUtils.println(os, BasicVCalendar.END_VTODO);
    }

    /**
     * Get the version property string (e.g. VERSION:1.0)
     * @return
     */
    protected abstract String getVersion();

    /** Sigle field formatters **/

    protected void formatSummary(PIMItem pimItem, int pimField, OutputStream os,
            boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatSummary");
        formatSimpleField(pimItem, pimField, BasicVCalendar.SUMMARY, os, 
                true, allFields);
    }
    protected void formatNote(PIMItem pimItem, int pimField, OutputStream os,
            boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatNote");
        formatSimpleField(pimItem, pimField, BasicVCalendar.DESCRIPTION, os, 
                true, allFields);
    }
    protected void formatLocation(PIMItem pimItem, OutputStream os,
            boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatLocation");
        formatSimpleField(pimItem, Event.LOCATION, BasicVCalendar.LOCATION, os, 
                true, allFields);
    }
    protected void formatUID(PIMItem pimItem, int pimField, OutputStream os,
            boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatUID");
        formatSimpleField(pimItem, pimField, BasicVCalendar.UID, os, true, allFields);
    }

    protected void formatPriority(PIMItem pimItem, OutputStream os,
            boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatPriority");
        if(isSupported(pimItem, ToDo.PRIORITY) && pimItem.countValues(ToDo.PRIORITY) > 0) {
            int priority = pimItem.getInt(ToDo.PRIORITY, 0);
            if(priority > 0) {
                // The priority scale of JSR75 is the same of vCalendar
                PimUtils.println(os, BasicVCalendar.PRIORITY + ":" + Integer.toString(priority));
            }
        } else {
            
        }
    }

    protected void formatStatus(PIMItem pimItem, OutputStream os,
            boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatStatus");
        if (isSupported(pimItem, ToDo.COMPLETED) && pimItem.countValues(ToDo.COMPLETED) > 0) {
            String status;
            if(pimItem.getBoolean(ToDo.COMPLETED, 0)) {
                status = BasicVCalendar.STATUS_COMPLETED;
            }
            else {
                status = BasicVCalendar.STATUS_IN_PROCESS;
            }
            PimUtils.println(os, BasicVCalendar.STATUS + ":" + status);
        }
    }

    protected void formatCategories(PIMItem pimItem, OutputStream os,
            boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatCategories");

        String catField = null;
        
        String[] categories = pimItem.getCategories();
        if(categories != null) {
            for(int i=0; i<categories.length; i++) {
                if(catField == null) {
                    catField = categories[i];
                } else {
                    catField += ";" + categories[i];
                }
            }
        }
        if(catField != null) {
            PimUtils.println(os, BasicVCalendar.CATEGORIES + ":" + catField);
        } else if(allFields) {
            PimUtils.println(os, BasicVCalendar.CATEGORIES + ":");
        }
    }
    protected void formatClass(PIMItem pimItem, int pimField, OutputStream os,
            boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatClass");
        String vCalClass = null;
        if (isSupported(pimItem, pimField) && pimItem.countValues(pimField) > 0) {
            int classValue = pimItem.getInt(pimField, 0);
            if(pimItem instanceof Event) {
                if(classValue == Event.CLASS_PUBLIC) {
                    vCalClass = BasicVCalendar.CLASS_PUBLIC;
                } else if(classValue == Event.CLASS_PRIVATE) {
                    vCalClass = BasicVCalendar.CLASS_PRIVATE;
                } else if(classValue == Event.CLASS_CONFIDENTIAL) {
                    vCalClass = BasicVCalendar.CLASS_CONFIDENTIAL;
                } else {
                    Log.error(LOG_TAG, "Unsupported class type: " + classValue);
                }
            } else if(pimItem instanceof ToDo) {
                if(classValue == ToDo.CLASS_PUBLIC) {
                    vCalClass = BasicVCalendar.CLASS_PUBLIC;
                } else if(classValue == ToDo.CLASS_PRIVATE) {
                    vCalClass = BasicVCalendar.CLASS_PRIVATE;
                } else if(classValue == ToDo.CLASS_CONFIDENTIAL) {
                    vCalClass = BasicVCalendar.CLASS_CONFIDENTIAL;
                } else {
                    Log.error(LOG_TAG, "Unsupported class type: " + classValue);
                }
            }
            if(vCalClass != null) {
                PimUtils.println(os, BasicVCalendar.CLASS + ":" + vCalClass);
            }
        }
    }
    protected void formatDTStart(PIMItem pimItem, OutputStream os,
            boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatDTStart");
        formatDateTimeField(os, pimItem, Event.START, BasicVCalendar.DTSTART, 
                true, true, allFields);
    }
    protected void formatDTEnd(PIMItem pimItem, OutputStream os,
            boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatDTEnd");
        formatDateTimeField(os, pimItem, Event.END, BasicVCalendar.DTEND, true, 
                true, allFields);
    }
    protected void formatDue(PIMItem pimItem, OutputStream os,
            boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatDue");
        formatDateTimeField(os, pimItem, ToDo.DUE, BasicVCalendar.DUE, false, 
                true, allFields);
    }
    protected void formatRevision(PIMItem pimItem, int pimField, OutputStream os,
            boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatRevision");
        formatDateTimeField(os, pimItem, pimField, BasicVCalendar.LAST_MODIFIED, 
                false, false, allFields);
    }
    protected void formatCompleted(PIMItem pimItem, OutputStream os,
            boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatCompleted");
        formatDateTimeField(os, pimItem, ToDo.COMPLETION_DATE, 
                BasicVCalendar.COMPLETED, false, false, allFields);
    }
    protected void formatRRule(PIMItem pimItem, OutputStream os,
            boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatRRule");
        // TODO
    }

    protected void formatFunambolAllday(PIMItem pimItem, OutputStream os, boolean allFields)
            throws PIMException { }

    protected void formatAttendees(PIMItem pimItem, OutputStream os,
            boolean allFields) throws PIMException { }
    protected int  getTaskAlarmInterval(PIMItem pimItem) { return -1; }
    
    protected abstract void formatAlarm   (PIMItem pimItem, OutputStream os,
            boolean allFields) throws PIMException;
    protected abstract void formatTimezone(PIMItem pimItem, OutputStream os)
            throws PIMException;

    /**
     * Format a simple field.
     * 
     * @param pimItem the PIMItem that contains the field value
     * @param pimField the PIMItem field index
     * @param vCalField the vCalendar field name
     * @param os the output stream
     * @param checkEncode check whether the field value shall be encoded
     * @throws javax.microedition.pim.PIMException
     */
    protected void formatSimpleField(PIMItem pimItem, int pimField, String vCalField,
                                OutputStream os, boolean checkEncode, boolean allFields) throws PIMException {
        Log.trace(LOG_TAG, "formatSimple Field: " + vCalField);
        if (isSupported(pimItem, pimField)) {
            // format the field also if it's empty
            if(pimItem.countValues(pimField) == 0) {
                if(allFields) {
                    PimUtils.println(os, vCalField + ":");
                }
                return;
            }
            String value  = pimItem.getString(pimField, 0);
            value = pimUtils.escape(value,true,false);
            StringBuffer field = new StringBuffer(vCalField);
            if(checkEncode) {
                String encoded = null;
                if((encoded=encodeField(value)) != null) {
                    value = encoded;
                    field.append(";").append(BasicVCalendar.ENCODING)
                         .append("=").append(BasicVCalendar.QUOTED_PRINTABLE);
                    field.append(";").append(BasicVCalendar.CHARSET)
                         .append("=").append(defaultCharset);
                }
            }
            // Note: we cannot fold all fields, because we miss the logic to
            // fold quoted printable properties, which have a special folding
            // rule. Since folding is not required, we avoid it for all fields
            PimUtils.println(os, field.toString() + ":" + value);
        }
    }

    /**
     * Format a date-time field.
     *
     * @param os The output stream to print the field in.
     * @param pimItem The PIMItem
     * @param pimField The pim field index.
     * @param vCalField The vCalendar field name
     * @param checkAllDay Check whether the allday property shall be updated
     * @param checkTimezone Check whether the TZID param shall be added (only
     * for iCalendar implementations)
     *
     * @throws javax.microedition.pim.PIMException
     */
    protected abstract void formatDateTimeField(OutputStream os, PIMItem pimItem,
            int pimField, String vCalField, boolean checkAllDay,
            boolean checkTimezone, boolean allFields) throws PIMException;

    protected boolean isAllDay(PIMItem pimItem) {
        // implement the default behaviour: return true if the START and END date
        // are equals.
        // NOTE: the BlackBerryEvent implementation include the field ALLDAY
        //       which should be used for this scope
        if(pimItem instanceof Event) {
            long start = 0;
            long end = 1;
            if(isSupported(pimItem, Event.START) && pimItem.countValues(Event.START) > 0) {
                start = pimItem.getDate(Event.START, 0);
            }
            if(isSupported(pimItem, Event.END) && pimItem.countValues(Event.END) > 0) {
                end = pimItem.getDate(Event.END, 0);
            }
            if(start == end) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Encode a field value if it requires encoding
     * @param value the field value
     * @return null if it doesn't require encoding
     * @throws javax.microedition.pim.PIMException
     */
    protected String encodeField(String value) throws PIMException {
        try {
            String qpEncoded = QuotedPrintable.encode(value, defaultCharset);
            if (qpEncoded.length() != value.length()) {
                value = qpEncoded;
                return value;
            }
            return null;
        } catch (Exception e) {
            throw new PIMException(e.toString());
        }
    }

    protected boolean isSupported(PIMItem pimItem, int pimField) {
        return pimItem.getPIMList().isSupportedField(pimField);
    }

    /**
     * Add a day factor to the end date (used for allday events)
     * @param endDate
     */
    protected long fixEndDate(long endDate) {
        return endDate+CalendarUtils.DAY_FACTOR;
    }
}
