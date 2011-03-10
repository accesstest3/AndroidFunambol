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

import junit.framework.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * Test the wap gateway container methods.
 */
public class BlackberryConfigurationTest extends TestCase {

    /**Default test APN*/
    private static final String TEST_URL_PARAMETERS = "common.funambol.apn";
    /**Default test Username*/
    private static final String TEST_DESCRIPTION = "description";

    /**Default BlackberryConfigurationObject*/
    BlackberryConfiguration bc = null;
    /** Creates a new instance of WapGatewayTest */
    public BlackberryConfigurationTest() {
        super(8, "BlackberryConfigurationTest");
        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.DEBUG);
    }
    
    /**
     * Set up the test environment
     * @throws java.lang.Exception
     */
    protected void setUp() throws Exception {
        bc = new BlackberryConfiguration();    
    }

    /**
     * Tear down the test environment
     * @throws java.lang.Exception
     */
    protected void tearDown() throws Exception {
        bc=null;
    }
    
    public void test(int i) throws Throwable {
        switch(i) {
            case 0:
                testSetUrlParameters();
                break;
            case 1:
                testGetUrlParameters();
                break;
            case 2:
                testSetPermission();
                break;
            case 3:
                testGetPermission();
                break;
            case 4:
                testSetDescription();
                break;
            case 5:
                testGetDescription();
                break;
            case 6:
                testSerialize();
                break;
            case 7:
                testDeserialize();
                break;
            default:
                break;
        }
    }

    /**
     * test the accessor method getDescription
     */
    private void testGetDescription() {
        assertTrue(bc.getDescription().equals(ConnectionConfig.NO_DESCRIPTION));
    }

    /**
     * test the accessor method getPermission
     */
    private void testGetPermission() {
        assertTrue(bc.getPermission()==ConnectionConfig.PERMISSION_UNDEFINED);
    }

    /**
     * test the accessor method getUrlParameters
     */
    private void testGetUrlParameters() {
        assertTrue(bc.getUrlParameters().equals(ConnectionConfig.NO_PARAMETERS));
    }

    /**
     * test the accessor method setDescription
     */
    private void testSetDescription() {
        bc.setDescription(TEST_DESCRIPTION);
        assertTrue(bc.getDescription().equals(TEST_DESCRIPTION));
    }

    /**
     * test the accessor method setPermission
     */
    private void testSetPermission() {
        bc.setPermission(ConnectionConfig.PERMISSION_GRANTED);
        assertTrue(bc.getPermission()==ConnectionConfig.PERMISSION_GRANTED);
    }

    /**
     * test the accessor method setUrlParameters
     */
    private void testSetUrlParameters() {
        bc.setUrlParameters(TEST_URL_PARAMETERS);
        assertTrue(bc.getUrlParameters().equals(TEST_URL_PARAMETERS));
    }

    /**
     * test the serialize method
     */
    private void testSerialize() throws IOException {

        //prepare the expected OutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(TEST_URL_PARAMETERS);
        dos.writeInt(ConnectionConfig.PERMISSION_GRANTED);
        dos.writeUTF(TEST_DESCRIPTION);
        byte[] expected = baos.toByteArray();
        
        //Create the test result
        ByteArrayOutputStream resBaos = new ByteArrayOutputStream();
        DataOutputStream resDos = new DataOutputStream(resBaos);
        bc.setUrlParameters(TEST_URL_PARAMETERS);
        bc.setPermission(ConnectionConfig.PERMISSION_GRANTED);
        bc.setDescription(TEST_DESCRIPTION);
        bc.serialize(resDos);
        byte[] result = resBaos.toByteArray();

        assertTrue(areByteArraysEquals(expected, result));
    }

    /**
     * test the deserialize method
     */
    private void testDeserialize() throws IOException {
        //Prepare result InputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        //Set the class properties byte per byte
        dos.writeUTF(TEST_URL_PARAMETERS);
        dos.writeInt(ConnectionConfig.PERMISSION_GRANTED);
        dos.writeUTF(TEST_DESCRIPTION);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);
        bc.deserialize(dis);
        
        assertTrue(
            bc.getUrlParameters().equals(TEST_URL_PARAMETERS)&&
            bc.getPermission()==ConnectionConfig.PERMISSION_GRANTED&&
            bc.getDescription().equals(TEST_DESCRIPTION)
            );
    }

    /**
     * Compare the two given byte array 
     * @param expected
     * @param result
     * @return
     */
    private boolean areByteArraysEquals(byte[] expected, byte[] result) {
        for (int i=0; i<expected.length; i++) {
            if (result[i]!=expected[i]) {
                return false;
            }
        }
        return true;
    }
}
