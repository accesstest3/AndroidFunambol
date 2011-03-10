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

package com.funambol.common.pim.icalendar;

import com.funambol.common.pim.*;

public interface ICalendarSyntaxParserListener {

    public void start();
    public void end();
    public void addProperty(ParserProperty property) throws ParseException;
    public void startEvent() throws ParseException;
    public void endEvent() throws ParseException;
    public void addEventProperty(ParserProperty property) throws ParseException;
    public void startToDo() throws ParseException;
    public void endToDo() throws ParseException;
    public void addToDoProperty(ParserProperty property) throws ParseException;
    public void startAlarm() throws ParseException;
    public void endAlarm() throws ParseException;
    public void addAlarmProperty(ParserProperty property) throws ParseException;
    public void addAlarm() throws ParseException;
    public void startTimezone() throws ParseException;
    public void endTimezone() throws ParseException;
    public void addTimezoneProperty(ParserProperty property) throws ParseException;
    public void addTimezoneStandardC() throws ParseException;
    public void addTimezoneDayLightC() throws ParseException;
    public void startTimezoneStandardC() throws ParseException;
    public void endTimezoneStandardC() throws ParseException;
    public void addStandardCProperty(ParserProperty property) throws ParseException;
    public void startTimezoneDayLightC() throws ParseException;
    public void endTimezoneDayLightC() throws ParseException;
    public void addDayLightCProperty(ParserProperty property) throws ParseException;
}

