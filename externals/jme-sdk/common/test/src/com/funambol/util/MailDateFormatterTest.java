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

package com.funambol.util;

import junit.framework.*;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * A JMUnit test class to test the methods of {@link MailDateFormatter} <p>
 *
 * sysout is used instead of Log.debug() to let messages appear in the output on
 * Ant too
 */
public class MailDateFormatterTest extends TestCase {
    
    // --------------------------------------------------------- Private fields
    
    /**
     * This Date object is created in the SturtUp initialization method starting
     * from an ad-hoc Calendar object
     */
    private Date dateobject = null;
    
    /**
     * The expected value in 'Zulu' notation, defined in the StartUp
     * initialization method. The time is in UTC
     */
    private String utc = "";
    
    /**
     * The expected value in the RFC 2822 notation, defined in the StartUp
     * initialization method. RFC 2822 recommends to use the local time
     */
    private String rfc2822 = "";
    
    
    // ---------------------------------------------------------------- Methods
    
    /**
     * Test of dateToRfc2822 method, of class
     * com.funambol.util.MailDateFormatter
     */
    public void testdateToRfc2822() throws AssertionFailedError {
        Log.debug("dateToRfc2822");
        
        Date date = dateobject;
        String expectedResult = rfc2822;
        String result = MailDateFormatter.dateToRfc2822(date);
        
        System.out.println("\nTESTING dateToRfc2822()");
        System.out.println("UTC: " + utc);
        System.out.println("expetctedResult (rfc2822): " + expectedResult);
        System.out.println("passed Date object: " + date.toString());
        System.out.println("result: " + result);
        assertEquals(expectedResult, result);
    }
    
    
    /**
     * Test of dateToUTC method, of class com.funambol.util.MailDateFormatter
     */
    public void testdateToUTC() throws AssertionFailedError {
        Log.debug("dateToUTC");
        Date d = dateobject;
        String expectedResult = utc;
        String result = MailDateFormatter.dateToUTC(d);
        
        System.out.println("\nTESTING dateToUTC()");
        System.out.println("RFC2822: " + rfc2822);
        System.out.println("The 'Date' object: " + dateobject);
        System.out.println("expetctedResult (utc): " + expectedResult);
        System.out.println("result: " + result);
        
        assertEquals(expectedResult, result);
    }
    
    
    /**
     * Test of parseRfc2822Date method, of class
     * com.funambol.util.MailDateFormatter
     */
    public void testparseRfc2822Date() throws AssertionFailedError {
        Log.debug("parseRfc2822Date");
        System.out.println("\nTESTING parseRfc2822Date()");
        String d = rfc2822;
        Date expectedResultDate = dateobject;
        Date resultDate = MailDateFormatter.parseRfc2822Date(d);
        String expectedResult = expectedResultDate.toString();
        String result = resultDate.toString();
        
        assertEquals(expectedResult, result);

        // 4  Nov 2008 10:30:05 -0400
        d = "4  Nov 2008 10:30:05 -0400";
        resultDate = MailDateFormatter.parseRfc2822Date(d);
        System.out.println("*********************************************");
        System.out.println(resultDate.toString());
    }
    
    
    /**
     * Test of parseUTCDate method, of class com.funambol.util.MailDateFormatter
     */
    public void testparseUTCDate() throws AssertionFailedError {
        Log.debug("parseUTCDate");
        System.out.println("\nTESTING parseUTCDate()");
        
        String utc = this.utc;
        Date expectedResultDate = dateobject;
        Date resultDate = MailDateFormatter.parseUTCDate(utc);
        String expectedResult = expectedResultDate.toString();
        String result = resultDate.toString();
        
        assertEquals(expectedResult, result);
    }
    
    public MailDateFormatterTest(String name) {
        super(name);
    }
    
    /**
     * Prepares the test environment. <p>
     *
     * A <code>Date</code> object containing the date corresponding to
     * November, Tuesday 7th 2006 at 01:13:26 PM GMT (i.e., 13:13:26 UTC or
     * 14:13:26 CET or 14:13:26 GMT+01:00) is created
     *
     * @see jmunit.framework.cldc10.TestCase#setUp()
     */
    public void setUp() {
        
        // here "UTC", but in the scrapbook "Europe/Berlin"
        // System.out.println(TimeZone.getDefault().getID());
        
        // "GMT+1:00"
        System.out.println("[DEBUG]"
                + java.util.TimeZone.getTimeZone("GMT+1:00").getID());
        
        // Gets a Calendar object with time offset +1000
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
        TimeZone tz = cal.getTimeZone();// test this TimeZone
        TimeZone deftz = TimeZone.getDefault();// test the default TimeZone
        System.out.println("[DEBUG]" + tz.getRawOffset());// 360000 (1 hour)
        System.out.println("[DEBUG]" + deftz.getRawOffset());// 0
        System.out.println("[time zone from system properties:] "
                + System.getProperty("user.timezone"));// null
        // Calendar cal = Calendar.getInstance();
        // TimeZone tz = cal.getTimeZone();
        // System.out.println(tz.getRawOffset());//0
        
        cal.set(Calendar.YEAR, 1969);
        cal.set(Calendar.MONTH, Calendar.NOVEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 30);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        cal.set(Calendar.HOUR_OF_DAY, 14);// the Date obj will have 13 (UTC)
        cal.set(Calendar.MINUTE, 13);
        cal.set(Calendar.SECOND, 26);
        
        System.out.println("[DEBUG] date set to" + cal.getTime());
        
        // Warning! getTime() returns here a Date object in UTC,
        // therefore passed Date objects are in UTC
        dateobject = cal.getTime();
        //rfc2822 = "Tue, 7 Nov 2006 14:13:26 +0100";// local time
        rfc2822 = "Sun, 30 Nov 1969 13:13:26 +0000";// UTC
        utc = "19691130T131326Z";
    }
    
    
    public void tearDown() {
    }
}
