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

package com.funambol.push;

import com.funambol.util.Log;
import com.funambol.util.ThreadPool;

import java.io.IOException;
import javax.microedition.io.Connector;
import javax.wireless.messaging.*;


/**
 * This class implements a service to listen to OTA messages for push
 * notification. The actions to respond to the notifications are deletegated to
 * the PushNotificationListener that can be registered.
 */
public class OTAService implements MessageListener, Runnable {
    
    private MessageConnection smsconn;
    private String smsConnection;
    private ThreadPool threadPool = null;
    private OTANotificationListener pushListener = null;
    
    public static boolean smsHandling;
    public static boolean syncViaSMS;
    public static boolean startViaOTA;
    public static boolean firstSMS = true;
    
    private int pending = 0;
    
    /** Creates a new instance of OTAMessagesListener */
    public OTAService(String smsPort) {
        smsConnection = "sms://:" + smsPort;
    }

    public OTAService(String smsPort, ThreadPool threadPool) {
        smsConnection = "sms://:" + smsPort;
        this.threadPool = threadPool;
    }

    public void setPushNotificationListener(OTANotificationListener pushListener) {
        this.pushListener = pushListener;
    }
    
    /**
     * Starts the service. This service does not require real work to be
     * performed. Instead of having active wait of incoming events, the notify
     * method is invoked from the underlying system.
     * Starting service implies registering this object as SMS listener.
     */
    public void startService() {
        pending = 0;
        if (smsconn == null) {
            try {
                Log.info("[OTAService] Start");
                smsconn = (MessageConnection)Connector.open(smsConnection);
                Log.info("[OTAService] smsConnection open");
                smsconn.setMessageListener(this);
                Log.info("[OTAService] set MessageListener");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                Log.error("[OTAService] IOException opening smsConnection "
                          + "or setting message listener: " + ioe.getMessage());
            }
        }
    }

    /**
     * Stop the service. This implies closing the connection with the SMS
     * delivery system.
     */
    public void stopService() {
        if (smsconn != null) {
//#ifdef isBlackberry
//#             Log.info("[OTAService] On BlackBerry device we never close");
//#else
            try {
                Log.info("[OTAService] Closing");
                smsconn.close();
                Log.info("[OTAService] Closed");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                Log.error("[OTAService] IOException closing smsConnection");
            }
//#endif
        }
    }
    
    public void setConnection(MessageConnection mc) throws IOException {
        this.smsconn = mc;
        smsconn.setMessageListener(this);
    }
    
    /**
     * Callback Method invoked when an SMS is received on a specific port
     */
    public void notifyIncomingMessage(MessageConnection messageConnection) {
        Log.info("[OTAMessagesListener] notifyIncomingMessage invoked");
        Log.info("[OTAMessagesListener] smsHandling: " + smsHandling);
        
        pending++;
        smsHandling = true;
        if(!startViaOTA){
            syncViaSMS = true;
        }else{
            startViaOTA = false;
        }
        threadPool.startThread(this);
    }
    
    /**
     * Fired in a separated thread by notifyIncomingMessage() when a binary SMS
     * is received
     */
    public void run() {
        try {
            while (pending > 0) {
                Log.info("[OTAMessagesListener] Message receiving");
                try {
                    Message msg = smsconn.receive();
                    
                    if (msg != null && pushListener != null) {
                        pushListener.handleMessage(msg);
                    }
                } catch (Exception e) {
                    Log.error("Error: " + e.toString());
                    e.printStackTrace();
                }  finally {
                    // Decrement the pending message counter, unless it's already 0
                    if (pending > 0) {
                        pending --;
                        Log.info("[OTAMessageListener] " +
                                "message parsed, " + pending + " pending messages");
                    }
                }
            }
        } finally {
            smsHandling = false;
        }
    }
}
