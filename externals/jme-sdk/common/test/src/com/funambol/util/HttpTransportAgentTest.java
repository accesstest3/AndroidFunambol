/*
 * Copyright (C) 2006-2007 Funambol
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 */

package com.funambol.util;

import com.funambol.util.HttpTransportAgent;
import com.funambol.util.Log;

import java.io.IOException;
import junit.framework.*;

/**
 * Test the HTTPTransportAgent class
 */
public class HttpTransportAgentTest extends TestCase {
    
    private TestServerHttpTransportAgent server;
    private  HttpTransportAgent ta;
     
    public static final int PORT = 50005;
    
    public static final String SERVER = "127.0.0.1";

    /**
    * Carriage return + Line feed (0x0D 0x0A). In the RFC 2822 this is used as
    * line separator between different headers with relative values
    */
    private static final String EOL = "\r\n";

    private static final String CONTENT_LENGTH = "Content-Length";

    public HttpTransportAgentTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.DEBUG);
        server = new TestServerHttpTransportAgent(PORT);
        String url = "http://"+SERVER+":"+PORT;
        ta = new HttpTransportAgent(url, false, false);
    }

    /**
     * set up the tests
     */
    public void setUp() {
        
    }

    /**
     * Tear down the tests
     */
    public void tearDown() {
       
    }
    
    /**
     * Test the constructor giving a null URL
     */
    public void testConstructorUrlNull() throws Exception {
        boolean result = true;
        String msg ="";
        try{
            HttpTransportAgent hta = new HttpTransportAgent(null,false,false);
            result=false;
            msg ="NullPointerException doesn't thrown.";
        }catch (NullPointerException nex){
            //test pass if NullPointerException was thrown    
        }
        assertTrue(msg,result);
    }

    /**
     * Test the constructor giving a null URL
     */
    public void DISABLED_test5secDelay() throws Exception {
        server.startService();
        String resp  = "test5secDelay";
        server.setResponse(createStandardResp(resp));
        server.setDelayResponse(5000);
        String result = ta.sendMessage("");
        assertEquals(resp,result);
        server.stopService();
    }

     /**
     * Test the constructor giving a null URL
     */
    public void DISABLED_test1minDelay() throws Exception {
        server.startService();
        
        String resp  = "test1minDelay";
        server.setResponse(createStandardResp(resp));
        server.setDelayResponse(20000);
        String result = ta.sendMessage("");
        assertEquals(resp,result);
        server.stopService();
    }

     /**
     * Test the constructor giving a null URL
     */
    public void testDelayExpired() throws Exception {
        server.startService();

        String resp  = "testResponseAfterDelay";
        boolean testResult = false;
        server.setResponse(createStandardResp(resp));
        //Delay in HttpTransportAgent is 5 mins.
        server.setDelayResponse(60000 * 6);

        try{
            String result = ta.sendMessage("");
        }catch (Exception ex){
            testResult = true;
        }
        assertTrue(testResult);
        server.stopService();
    }

     /**
     * Test the constructor giving a null URL
     */
    public void testServerNotStarted() throws Exception {

        String resp  = "testServerNotStarted";
        boolean testResult = false;
        server.setResponse(createStandardResp(resp));

        try{
            String result = ta.sendMessage("");
        }catch (Exception ex){
            testResult = true;
        }
        assertTrue(testResult);

    }
    
    private String createStandardResp(String respone){
        return addHeaders(respone);
    }

    private String addHeaders(String msg){
        int msgLength = msg.length();
        StringBuffer buf = new StringBuffer("HTTP/1.0 200 OK");
        buf.append(EOL).append(CONTENT_LENGTH).append(": ").append(msgLength)
                .append(EOL).append(EOL).append(msg);
        return buf.toString();
    }
}

