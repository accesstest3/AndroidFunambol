/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2003 - 2009 Funambol, Inc.
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


import java.io.IOException;

import javax.microedition.io.SocketConnection;

import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;

/**
 * This class manages the Inter-Process-Communication between the Email Client 
 * and other application on the system.
 */
class TestServerHttpTransportAgent implements Runnable{

    /** Definition of Disconnected status */
    protected static final int DISCONNECTED = 0;
    /** Definition of Connected status */
    protected static final int CONNECTED = 1;

    /** The system socket connection port*/
    public static final int SYSTEM_SOCKET_PORT = 50005;
    /** The system socket connection port*/
    public static final String SYSTEM_SOCKET_SERVER = "127.0.0.1";

    /** System Socket connection*/
    private SocketConnection sc = null;
    /** System Server Socket */
    private ServerSocketConnection ssc= null;
    /** OutputStream for the connection*/
    protected OutputStream os = null;
    /** Service Status */
    protected int status ;

   
    /** The service port */
    private int serviceRemotePort = SYSTEM_SOCKET_PORT;

    
    /**
     * Carriage return + Line feed (0x0D 0x0A). In the RFC 2822 this is used as
     * line separator between different headers with relative values
     */
    private static final String EOL = "\r\n";

    private static final String CONTENT_LENGTH = "Content-Length";

    private String response;
    private long delayResponse;


    public TestServerHttpTransportAgent(int port) {
        status = DISCONNECTED;
        serviceRemotePort = port;
    }



    

    /**
     * Start the channel listening
     */
    public void startService()  {
        Log.debug("[TestServerHttpTransportAgent.startService]Starting Service");

        Thread startService = new Thread(this);
        startService.start();

    }

    public void run(){
           try {
            if(!isRunning()){
                String url = "socket://:" + serviceRemotePort;
                ssc = (ServerSocketConnection) Connector.open(url);
                status = CONNECTED;
            }
            while (status == CONNECTED ) {
                sc = (SocketConnection)ssc.acceptAndOpen();
                
                // service the connection in a separate thread
                Request r = new Request(sc);
                r.start();
            }

         } catch (IOException e) {
             if(status != DISCONNECTED){
                Log.error("IOException while starting Service " + e.toString());
             }
         }
    }

    /**
     * Stop the channel listening
     */
    public void stopService() {
        Log.debug("[TestServerHttpTransportAgent.stopService]Stopping Service...");
        disconnect();
    }

    /**
     * Send a message to the channel
     * @param msg the message to be sent
     * @throws IOException if the message cannot be sent
     */
    public void sendMessage() throws IOException {
        try {
            Log.debug("[TestServerHttpTransportAgent] waiting "+delayResponse/1000+" sec before to send message ");
            Thread.sleep(delayResponse);
        } catch (InterruptedException ex) {
        }
        os.write(response.getBytes());
        os.flush();
        Log.debug("[TestServerHttpTransportAgent.sendMessage]Message sent: " + response);
    }


    /**
     * Get the current service status
     * @return int the status of the service
     */
    public int getStatus() {
        return status;
    }

    /**
     * Get the running status
     * @return boolean the value true if the service is listening for messages,
     * false otherwise
     */
    public boolean isRunning() {
        return status == CONNECTED;
    }


    /**
     * Close the connection, forcing exceptions if there are pending network IO
     * operations.
     */
    protected void disconnect() {
        Log.debug("[TestServerHttpTransportAgent.disconnect]Disconnecting...");
        closeOutputStream();
        closeSocketConnection();
        closeSocketServer();
        status = DISCONNECTED;
        Log.debug("[TestServerHttpTransportAgent.disconnect]Status DISCONNECTED");
    }

    private void closeOutputStream() {
        if (os != null) {
            try {
                os.close();
            } catch (IOException ioe) {
                Log.debug("[TestServerHttpTransportAgent.closeOutputStream]Exception closing the stream: " + ioe);
            } finally {
                os = null;
            }
        }
    }

    
    private void closeSocketConnection() {
        if (sc != null) {
            try {
                sc.close();
            } catch (IOException e) {
                Log.error("[TestServerHttpTransportAgent.closeSockectConnection]Cannot force socket closure");
            } finally {
                sc = null;
            }
        } else {
            Log.debug("[TestServerHttpTransportAgent.closeSockectConnection]No need to close socket...");
        }
    }

     private void closeSocketServer() {
        if (ssc != null) {
            try {
                ssc.close();
            } catch (IOException e) {
                Log.error("[TestServerHttpTransportAgent.closeSockectServer]Cannot force socket server closure");
            } finally {
                ssc = null;
            }
        } else {
            Log.debug("[TestServerHttpTransportAgent.closeSockectServer]No need to close socket server...");
        }
    }

    public void setResponse(String response){
        this.response = response;
    }

    public void setDelayResponse(long delay){
        delayResponse = delay;
    }

    /**
      * Thread to handle client request.
      */
     class Request extends Thread
     {
         private SocketConnection client;

         public Request(SocketConnection c) {
             client = c;
         }

         /**
          * Handles client request.
          */
         public void run() {
             
             try {
                os = sc.openOutputStream();
                sendMessage();
             }
             catch (Throwable ioe) {
                 //Handle Exceptions any other way you like.
                 //No-op
             }
             finally {
                 try {
                     if (os != null){
                         os.close();
                     }
                     if (client != null){
                         client.close();
                     }
                 }
                 catch (IOException ioee) {
           //Handle Exceptions any other way you like.
           //No-op
                 }
             }
         }


     }

   
}
