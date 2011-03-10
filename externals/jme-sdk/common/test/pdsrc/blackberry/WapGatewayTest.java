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

package com.funambol.util;

import j2meunit.framework.*;


/**
 * Test the wap gateway container methods.
 */
public class WapGatewayTest extends FunBasicTest {

    /**Default test APN*/
    private static final String TEST_APN = "common.funambol.apn";
    /**Default test Username*/
    private static final String TEST_USERNAME = "funambol";
    /**Default test Password*/
    private static final String TEST_PASSWORD = "password";
    /**Default test Country*/
    private static final String TEST_COUNTRY = "worldwide";
    
    /**The default Wapgateway tes object*/
    WapGateway wg = null;

    /** Creates a new instance of WapGatewayTest */
    public WapGatewayTest() {
        super(4, "WapGatewayTest");
        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.DEBUG);
    }
    
    /**
     * Set up the test environment
     * @throws java.lang.Exception
     */
    protected void setUp() throws Exception {
        //Init the default test object
        wg = new WapGateway(
                TEST_APN,
                TEST_USERNAME,
                TEST_PASSWORD,
                TEST_COUNTRY
                );
    }

    /**
     * Tear down the test environment
     * @throws java.lang.Exception
     */
    protected void tearDown() throws Exception {
        wg=null;
    }
    
    public void test(int i) throws Throwable {
        switch(i) {
            case 0:
                testGetAPN();
                break;
            case 1:
                testGetUsername();
                break;
            case 2:
                testGetPassword();
                break;
            case 3:
                testGetCountry();
                break;
            default:
                break;
        }
    }

    /**
     * Test the accessor method getAPN
     */
    private void testGetAPN() {
        assertTrue(wg.getApn().equals(TEST_APN));
    }

    /**
     * Test the accessor method getCountry
     */
    private void testGetCountry() {
        assertTrue(wg.getCountry().equals(TEST_COUNTRY));
    }

    /**
     * Test the accessor method getPassword
     */
    private void testGetPassword() {
        assertTrue(wg.getPassword().equals(TEST_PASSWORD));
    }

    /**
     * Test the accessor method getUsername
     */
    private void testGetUsername() {
        assertTrue(wg.getUsername().equals(TEST_USERNAME));
    }
}    
