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

package com.funambol.util;


import java.util.Calendar;
import java.util.Random;

import com.funambol.util.Log;
import com.funambol.util.ConsoleAppender;

import junit.framework.*;


/**
 * Testing the DateUtil implementation.
 */
public class DateUtilTest extends TestCase {
    
    public DateUtilTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.DEBUG);
    }
    
    public void testExtractAddressFromUrl() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.APRIL);
        cal.set(Calendar.YEAR,  2007);
        cal.set(Calendar.DAY_OF_MONTH, 5);
        cal.set(Calendar.HOUR, 10);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 00);
        cal.set(Calendar.AM_PM, Calendar.AM);
        String format = DateUtil.formatDateTimeUTC(cal);
        assertTrue(format.equals("20070405T103000Z"));
    }

    public void testParseDateTime() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.APRIL);
        cal.set(Calendar.YEAR,  2007);
        cal.set(Calendar.DAY_OF_MONTH, 5);
        cal.set(Calendar.HOUR, 10);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 00);
        cal.set(Calendar.AM_PM, Calendar.AM);
        Calendar cal2 = DateUtil.parseDateTime("20070405T103000Z");

        assertTrue(compareCals(cal, cal2));
    }

    public void testParseDateTime2() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.OCTOBER);
        cal.set(Calendar.YEAR,  1953);
        cal.set(Calendar.DAY_OF_MONTH, 15);
        cal.set(Calendar.HOUR, 11);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);
        cal.set(Calendar.AM_PM, Calendar.PM);
        Calendar cal2 = DateUtil.parseDateTime("1953-10-15T23:00:00Z");

        assertTrue(compareCals(cal, cal2));
    }

    public void testParseDateTime3() throws Exception {
        Calendar cal2 = DateUtil.parseDateTime("1970-05-25");
        assertTrue(cal2.get(Calendar.YEAR) == 1970);
        assertTrue(cal2.get(Calendar.MONTH) == Calendar.MAY);
        assertTrue(cal2.get(Calendar.DAY_OF_MONTH) == 25);
    }

    public void testRound() throws Exception {
        long seed = 1973;
        Random rand = new Random(seed);
        Calendar cal = Calendar.getInstance();
        for(int i=0;i<100;++i) {
            int month = getNextRand(rand.nextInt(), 12);
            cal.set(Calendar.MONTH, month);

            int year = 1970 + getNextRand(rand.nextInt(), 100);
            cal.set(Calendar.YEAR, year);

            int day = getNextRand(rand.nextInt(), 28);
            cal.set(Calendar.DAY_OF_MONTH, 1+day);

            int hour = getNextRand(rand.nextInt(), 24);
            cal.set(Calendar.HOUR_OF_DAY, hour);

            int minute = getNextRand(rand.nextInt(), 60);
            cal.set(Calendar.MINUTE, minute);

            int sec = getNextRand(rand.nextInt(), 60);
            cal.set(Calendar.SECOND, sec);

            // Format and parse it back
            String format = null;
            Calendar cal2 = null;
            format = DateUtil.formatDateTimeUTC(cal);
            cal2 = DateUtil.parseDateTime(format);
            assertTrue(compareCals(cal, cal2));
        }
    }

    private int getNextRand(int r, int max) {
        if (r < 0) {
            r = 0-r;
        }
        return (r % max);
    }

    private boolean compareCals(Calendar cal1, Calendar cal2) {
        int fields[] = { Calendar.MONTH,
                         Calendar.YEAR,
                         Calendar.DAY_OF_MONTH,
                         Calendar.HOUR,
                         Calendar.MINUTE,
                         Calendar.SECOND,
                         Calendar.AM_PM };

        for(int i=0;i<fields.length;++i) {
            int field = fields[i];
            if (cal1.get(field) != cal2.get(field)) {
                return false;
            }
        }
        return true;
    }



}


