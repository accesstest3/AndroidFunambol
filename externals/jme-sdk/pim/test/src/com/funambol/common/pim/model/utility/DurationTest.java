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

package com.funambol.common.pim.model.utility;


import com.funambol.util.Log;
import com.funambol.util.ConsoleAppender;

import com.funambol.common.pim.model.utility.Duration;
import junit.framework.*;

public class DurationTest extends TestCase {

    private final String[] DURATION_STRING = {"-PT10M",
                                              "P2D",
                                              "-PT5H",
                                              "PT18M",
                                              "-PT50S",
                                              "P8DT1H1M1S",
                                              "-P2DT10H",
                                              "PT1H1M1S",
                                              "-PT10M40S",
                                              "P2YT1S",
                                              "P4MT4H",
                                              "-P13Y3M29DT14H59M16S"};
    
    private final long[] DURATION_MILLIS = {      -600000L,
                                                172800000L,
                                                -18000000L,
                                                  1080000L,
                                                   -50000L,
                                                694861000L,
                                               -208800000L,
                                                  3661000L,
                                                  -640000L,
                                              63072001000L,
                                              10382400000L,
                                            -420303556000L};
    
    private final String[] DURATION_STRING_FAIL = {"6PT10M",
                                                   "T18M",
                                                   "--PT18M",
                                                   "PT18",
                                                   "PT18M5",
                                                   "P4DT4",
                                                   "aeiou",
                                                   "APT5H",
                                                   "-",
                                                   ""};
    
    private Duration duration = null;

    public DurationTest(String name) {
        super(name);
    }
    
    public void setUp() {
    }
    
    public void tearDown() {
    }

    public void testParseFormat() {
        duration = new Duration();
        for(int i=0; i<DURATION_STRING.length; i++) {
            duration.parse(DURATION_STRING[i]);
            assertEquals(duration.getMillis(), DURATION_MILLIS[i]);
            assertEquals(duration.format(), DURATION_STRING[i]);
            duration.setMillis(DURATION_MILLIS[i]);
            assertEquals(duration.getMillis(), DURATION_MILLIS[i]);
            assertEquals(duration.format(), DURATION_STRING[i]);
        }
    }
    
    public void testParseFail() {
        duration = new Duration();
        for(int i=0; i<DURATION_STRING_FAIL.length; i++) {
            try {
                duration.parse(DURATION_STRING_FAIL[i]);
                assertTrue(false);
            } catch(IllegalArgumentException ex) { }
        }
    }
}

