/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2010 Funambol, Inc.
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

package com.funambol.android.source.pim.calendar;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;

import com.funambol.common.pim.model.common.Property;
import com.funambol.common.pim.model.converter.ConverterException;
import com.funambol.common.pim.model.converter.VCalendarContentConverter;
import com.funambol.common.pim.model.converter.VCalendarConverter;
import com.funambol.common.pim.model.converter.VComponentWriter;
import com.funambol.common.pim.model.model.VCalendar;
import com.funambol.common.pim.model.common.XTag;
import com.funambol.common.pim.model.xvcalendar.XVCalendarSyntaxParserListenerImpl;

import com.funambol.common.pim.xvcalendar.XVCalendarSyntaxParser;
import com.funambol.common.pim.xvcalendar.ParseException;

import com.funambol.util.Log;
import java.util.TimeZone;

/**
 * A Calendar item. This object extends the pim framework data model, by adding
 * the ability to be loaded/saved into the Android Calendar
 */
public class Calendar extends com.funambol.common.pim.model.calendar.Calendar {

    private static final String TAG = "Calendar";

    private long id = -1;

    public Calendar() {
        super();
    }

    public void setId(String id) {
        setId(Long.parseLong(id));
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setVCalendar(byte vCalendar[]) throws ParseException, ConverterException {
        Log.trace(TAG, "Creating Calendar from vCalendar");
        ByteArrayInputStream is = new ByteArrayInputStream(vCalendar);
        VCalendar vcal = new VCalendar();

        XVCalendarSyntaxParserListenerImpl listener = new XVCalendarSyntaxParserListenerImpl(vcal);
        XVCalendarSyntaxParser parser = new XVCalendarSyntaxParser(is);
        parser.setListener(listener);
        parser.parse();

        boolean allday = false;
        // The converter does not take the all day extra into account, so we handle
        // it here (this is just for events)
        com.funambol.common.pim.model.model.Property allDayProperty;
        if (vcal.getFirstVEvent() != null) {
            allDayProperty = vcal.getFirstVEvent().getProperty("X-FUNAMBOL-ALLDAY");
            if (allDayProperty != null) {
                if ("1".equals(allDayProperty.getValue())) {
                    allday = true;
                }
            }
        }

        VCalendarConverter converter = getConverter(allday);
        com.funambol.common.pim.model.calendar.Calendar cal = converter.vcalendar2calendar(vcal);
        if (allday) {
            cal.getEvent().setAllDay(true);
        }

        this.setCalendarContent(cal.getCalendarContent());
        this.setCalScale(cal.getCalScale());
        this.setMethod  (cal.getMethod());
        this.setProdId  (cal.getProdId());
        this.setVersion (cal.getVersion());
        this.setXTags   (cal.getXTags());
    }

    public void toVCalendar(OutputStream os, boolean allFields) throws IOException, ConverterException {

        boolean allday = false;
        // The converter does not take the all day extra into account, so we handle
        // it here (this is just for events)
        com.funambol.common.pim.model.model.Property allDayProperty;
        if (getEvent() != null) {
            allday = getEvent().isAllDay();
        }

        VCalendarConverter converter = getConverter(allday);
        VCalendar vcal = converter.calendar2vcalendar(this, true);

        VComponentWriter writer = new VComponentWriter();
        os.write(writer.toString(vcal).getBytes());
    }

    private VCalendarContentConverter getConverter(boolean allday) {
        if (allday) {
            return new VCalendarContentConverter(null, "UTF-8", false);
        } else {
            return new VCalendarContentConverter(TimeZone.getDefault(), "UTF-8", false);
        }
    }

}
