/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.funambol.util;


/**
 * the simplest conneection handler ever. just save the config
 * 
 */
public class BasicConnectionListener implements ConnectionListener {

    /**
     * Check if the connection configuration is allowed
     * @param config is the configuration to be checked
     * @return true in the basic implementation because no real check is 
     * performed on the configuration permission
     */
    public boolean isConnectionConfigurationAllowed(String config) {
        Log.debug("[BasicConnectionListener]Configuration is always allowed in this implementation");
        return true;
    }

    /**
     * Notify that a connection was succesfully opened
     */
    public void connectionOpened() {
        Log.debug("[BasicConnectionListener]Connection Opened");
    }

    /**
     * Notify that a data request was succesfully written on the connection 
     * stream
     */
    public void requestWritten() {
        Log.debug("[BasicConnectionListener]Request written");
    }

    /**
     * Notify that a response was received after the request was sent
     */
    public void responseReceived() {
        Log.debug("[BasicConnectionListener]response received");
    }

    /**
     * Notify that a previously opened connection has been closed
     */
    public void connectionClosed() {
        Log.debug("[BasicConnectionListener]Connection closed");
    }

    public void connectionConfigurationChanged() {
        Log.debug("Connection Configuration changed");
    }
}
