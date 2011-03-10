/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2003 - 2008 Funambol, Inc.
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
 *
 *
 */
package com.funambol.util;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.Connection;

import com.funambol.platform.HttpConnectionAdapter;
import com.funambol.platform.SocketAdapter;
import com.funambol.platform.HttpConnectionAdapterWrapper;

/**
 * Controls all of the connections requested by the API implementations.
 * It is strongly recommended to use this class instead of the direct call to
 * the Connector.open(String url) method. This class is based on the singleton
 * pattern: it has private constructor and just one instance to be referenced
 * calling the method ConnectionManager.getInstance() by other classes. The
 * connection logic switch between all of the BlackberryConfiguration object
 * returned by the Connection config class. The usage of the returned
 * configuration is the following:
 * <br>
 * </br>
 *
 * <br>1. WIFI Network - just on the wifi capable devices, skipped if the wifi
 * bearer is not present;
 * </br>
 * <br>
 * </br>
 * <br>2. Custom TCP configuration - Manually configured by the user
 * </br>
 * <br>
 * </br>
 * <br>3. APN gateway configuration - Carrier's Country based Configuration
 * </br>
 * <br>
 * </br>
 * <br>4. Blackberry ServiceBook configuration WAP Transport entry - Blackberry
 * ServiceBook based Configuration
 * </br>
 * <br>
 * </br>
 * <br>NOTE 1: When a wifi network is no more available the system tries to
 * configure a GPRS data connection in the same session.
 * </br>
 * <br>
 * </br>
 * <br>NOTE 2: When the Manually configured TCP configuration is cancelled or become
 * invalid (i.e.: an invalid APN is substituted to a valid one), the TCP
 * configuration won't ever be used during the same session but the system will
 * switch other provided configurations.
 * </br>
 * <br>
 * </br>
 * <br>Note 3:When a configuration that is not WIFI or TCP based is validated, that
 * configuration will be used for the entire session.
 * </br>
 */
public class ConnectionManager {

    private static final String TAG_LOG = "ConnectionManager";

    /**String representation of SMS connection*/
    private static final String SMS_URL = "sms://:";
    /**WORKING configurations ID*/
    protected static int workingConfigID = ConnectionConfig.CONFIG_NONE;
    /**Array with possible blackberry configuration*/
    private static BlackberryConfiguration[] configurations = null;
    /**The unique instance of this clas*/
    private static ConnectionManager instance = null;
    /**ConnectionListener*/
    private ConnectionListener connectionListener = new BasicConnectionListener();

    private String connectionParameters = null;

    /**
     * Singleton realization: Private constructor
     * Use getInstance() method to acces the public methods
     */
    protected ConnectionManager() {
        configurations = ConnectionConfig.getBlackberryConfigurations();
    }

    /**
     * Singleton implementation:
     * @return the current instance of this class or a new instance if it the
     * current instance is null
     */
    public static ConnectionManager getInstance() {
        if (instance == null) {
            Log.trace(TAG_LOG, "Creating new connection manager");
            instance = new ConnectionManager();
            instance.setConnectionListener(new BasicConnectionListener());
        }
        return instance;
    }

    /**
     * Open up a connection to the given url with the default
     * CLDC 1.0 API default values for the stream (READ_WRITE) and the value
     * TRUE for the availability of InterruptedIOException to be thrown
     * @param url The URL for the connection
     * @return the connection url with the given parameters
     * @throws java.io.IOException
     */
    public synchronized Connection open(String url) throws IOException {
        return open(url, Connector.READ_WRITE, true);
    }

    /**
     * Open up a connection to the given url with the given access accessMode and
     * @param url The URL for the connection
     * @param accessMode the access accessMode that can be READ, WRITE, READ_WRITE
     * @param enableTimeoutException A flag to indicate that the caller wants to
     * enable the throwing of InterruptedIOException when a connection timeout
     * occurs
     * @return Connection related to the given parameters
     * @throws java.io.IOException
     */
    public synchronized Connection open(String url, int accessMode,
                                        boolean enableTimeoutException)
    throws IOException {

        //A message connection is required. It needs no parameters
        if (url.indexOf(SMS_URL)>0) {
            return Connector.open(url);
        }

        if (this.connectionParameters != null) {
            Log.debug(TAG_LOG, "Fixed Apn parameters detected for tis connection: "
                      + url + this.connectionParameters);
            return Connector.open(url + this.connectionParameters, accessMode,
                                  enableTimeoutException);
        }

        //If the GPRS coverage was lost the ServiceBook could have changed
        //A refesh is needed
        Log.debug(TAG_LOG, "Refreshing the configuration");
        ConnectionConfig.refreshServiceBookConfigurations();

        //Just displays the network coverage
        Log.trace(TAG_LOG, "Current network informations:\n"
                  + BlackberryUtils.getNetworkCoverageReport());

        //Checks the wifi status and removes it if the bearer is offline and
        //it is the current working configuration
        Log.trace(TAG_LOG, "Checking wifi status");
        //Denies the wifi usage permission if the bearer is not available or
        //turned off
        if (!BlackberryUtils.isWifiActive()||!BlackberryUtils.isWifiAvailable()) {
            Log.debug(TAG_LOG, "checkWifiStatus Wifi not available");
            //switch to the TCPConfiguration if the wifi network is not
            //available and the last working configuration was set to WIFI
            if (workingConfigID==ConnectionConfig.WIFI_CONFIG) {
                workingConfigID++;//=ConnectionConfig.CONFIG_NONE;
            }
        }

        //if the GPRS bearer is not available and wifi is available, then it
        //become the working id
        if (workingConfigID > 0 && BlackberryUtils.isWapGprsDataBearerOffline()) {
            Log.debug(TAG_LOG, "GPRS bearer is offline: switching to wifi if available");
            if (BlackberryUtils.isWifiActive() && BlackberryUtils.isWifiAvailable()) {
                Log.debug(TAG_LOG, "WIFI bearer is online: changing working configuration");
                workingConfigID=ConnectionConfig.WIFI_CONFIG;
            } else {
                //Doesn't try the connection because in this case there is no
                //bearer available.
                Log.debug(TAG_LOG, "WIFI bearer is offline: "
                          + "no suitable bearer were found. Throwing IOException");
                throw new IOException();
            }
        }

        if (workingConfigID < 0) {
            Log.debug(TAG_LOG, "Setting up the connection...");
            return setup(url, accessMode, enableTimeoutException);
        } else {
            Log.trace(TAG_LOG, "Working Configuration already assigned - ID="
                      + workingConfigID);
            Log.trace(TAG_LOG, "Using configuration: ID="
                      + workingConfigID + " - Description "
                      + configurations[workingConfigID].getDescription());
            Connection ret = null;
            try {
                Log.debug(TAG_LOG, "Opening connection with: "
                          + configurations[workingConfigID].getDescription());
                String fullUrl = url + configurations[workingConfigID].getUrlParameters();
                Log.trace(TAG_LOG, "Opening url: " + fullUrl);
                ret = Connector.open(fullUrl, accessMode, enableTimeoutException);
            } catch (Exception ioe) {
                Log.debug(TAG_LOG, "Error occured while accessing " +
                          "the network with the last working configuration " + ioe.toString());
                // If the current configuration no longer works, we try all the
                // known connections to see if we find one that works.
                // Connections that have already been granted/denied do not
                // result in an extra prompt to the user, we rather keep his
                // previous answer.
                workingConfigID=ConnectionConfig.CONFIG_NONE;
                // Close the connection if it got opened
                if (ret != null) {
                    try {
                        ret.close();
                    } catch (Exception e) {
                        Log.debug(TAG_LOG, "Setup Failed Closing connection: "
                                  + "setting up another configuration");
                        Log.debug(TAG_LOG, "Exception: " + e);
                    }
                }
                // If all the possible connections are not working, the setup
                // will throw an exception resulting in an exception in the
                // "open".
                ret = setup(url, accessMode, enableTimeoutException);
            }
            return ret;
        }
    }


    /**
     * Add optional parameters (such as APN configurations or wifi interface) to
     * the given url
     * @param url is the request url without configurations parameters
     * @return String representing the url with the optional parameter added
     */
    private Connection setup(String url, int accessMode, boolean enableTimeoutException)
    throws IOException {

        Connection ret = null;
        String requestUrl = null;
        for (int i = 0; i < ConnectionConfig.MAX_CONFIG_NUMBER; i++) {
            try {
                Log.debug(TAG_LOG, "Looping configurations: "
                          + (i+1) + "/" + ConnectionConfig.MAX_CONFIG_NUMBER);
                //If the open operation fails a subclass of IOException is thrown by the system
                if (isConfigurationAllowed(i)) {
                    Log.debug(TAG_LOG, "Configuration Allowed: " + (i+1));
                    workingConfigID = i % configurations.length;
                    String options = configurations[i].getUrlParameters();
                    Log.debug(TAG_LOG, "Using parameters: " + options);
                    requestUrl = url + options;
                } else {
                    Log.debug(TAG_LOG, "Setup config " + (i+1) + " cannot be used.");
                    continue;
                }

                Log.debug(TAG_LOG, "Connecting to: " + requestUrl);
                ret = Connector.open(requestUrl, accessMode, enableTimeoutException);
                //If the open call is succesfull it could be useful to notify
                //the current working configuration changes to the listener
                connectionListener.connectionConfigurationChanged();
                Log.debug(TAG_LOG, "Listener notified");

                // If we connected with a BIS configuration, then the user is
                // not on BES and we can safely disable the BES configuration.
                // This is more than an optimization, it is required to
                // guarantee the correct execution because users with direct TCP
                // experiences fake connections and weird timeouts when BES is
                // being used
                if (ConnectionConfig.isDirectTCP(workingConfigID)) {
                    Log.debug(TAG_LOG, "Direct TCP works, disable BES configuration");
                    int besId = ConnectionConfig.getBESConfigurationID();
                    if (besId != -1) {
                        configurations[besId].setPermission(ConnectionConfig.PERMISSION_DENIED);
                    }
                }
                return ret;
            } catch (Exception ioe) {
                Log.debug(TAG_LOG, "Connection not opened at attempt " + (i + 1));
                Log.debug(TAG_LOG, ioe.toString());

                // Close the connection in case it got opened
                if (ret != null) {
                    try {
                        ret.close();
                    } catch (Exception e) {
                        Log.debug(TAG_LOG, "Failed Closing connection: trying next");
                        Log.debug(TAG_LOG, "Exception: " + e);
                    }
                }
                ret = null;
            }
        }

        if (ret != null) {
            return ret;
        } else {
            workingConfigID = ConnectionConfig.CONFIG_NONE;
            //Doesn't return a null connection but throws an IOException
            throw new IOException("[ConnectionManager.setup]Cannot find a suitable configuration");
        }
    }

    /**
     * Return the Availability of a configurations
     * @param configNumber the Configuration ID
     * @return boolean true if the configurations can be used, false otherwise
     */
    private boolean isConfigurationAllowed(int configNumber) {
        Log.debug(TAG_LOG, "isConfigurationAllowed Config number: " + configNumber);
        if (!ConnectionConfig.isAvailable(configNumber)) {
            Log.debug(TAG_LOG, "isConfigurationAllowed Connection not available");
            return false;
        }

        //Calculates the apn String
        Log.debug(TAG_LOG, "Calculating APN");
        String apn = ConnectionConfig.getAPNFromConfig(configNumber);
        Log.debug(TAG_LOG, "APN Found: " + apn);

        //Permission is denied
        if (configurations[configNumber].getPermission()==ConnectionConfig.PERMISSION_DENIED){
            Log.debug(TAG_LOG, "Permission DENIED for: " + apn);
            return false;
        }

        //Permission is granted
        if (configurations[configNumber].getPermission()==ConnectionConfig.PERMISSION_GRANTED){
            Log.debug(TAG_LOG, "Permission GRANTED for: " + apn);
            return true;
        }

        //null check is performed because the apn is returned as null when the
        //mobile network is not available
        if (apn==null) {
            if (configNumber>ConnectionConfig.TCP_CONFIG) {
                Log.debug(TAG_LOG, "Retrieved null APN: availability DENIED");
                return false;
            }
        }

        //Connection listener logic implemented for undefined permission when network is covered
        if (configurations[configNumber].getPermission()==ConnectionConfig.PERMISSION_UNDEFINED){
            Log.debug(TAG_LOG, "Permission not defined for: " + apn);
            boolean isConfigurationAllowed = connectionListener.isConnectionConfigurationAllowed(apn);
            if (isConfigurationAllowed) {
                Log.debug(TAG_LOG, "Permission set to GRANTED");
                configurations[configNumber].setPermission(ConnectionConfig.PERMISSION_GRANTED);
            } else {
                Log.debug(TAG_LOG, "Permission set to DENIED");
                configurations[configNumber].setPermission(ConnectionConfig.PERMISSION_DENIED);
            }
            return isConfigurationAllowed;
        }
        Log.debug(TAG_LOG, "No suitable condition "
                  + "to allow the configuration (" + (configNumber+1) + ") was found");
        return false;
    }

    /**
     * Accessor method to set the connection listener
     * @param cl the connection listener to be set
     */
    public void setConnectionListener(ConnectionListener cl) {
        this.connectionListener = cl;
    }

    /**
     * Accessor method to get the current connection listener
     * @return ConnectionListener related to this ConnectionManager instance
     */
    public ConnectionListener getConnectionListener() {
        return connectionListener;
    }

    /**
     * Set the Connection parameters for this connection given the apn string
     * @param apn the fixed apn to be used.
     */
    public void setFixedApn(String apn) {
        if (apn!=null) {
            this.connectionParameters = ";deviceside=true;apn=" + apn + ";WapGatewayAPN=" + apn+
                    ";ConnectionTimeout=300000";
        } else {
            this.connectionParameters = null;
        }
    }

    /**
     * Disable the connection parameters for the fixed APN usage. Use this
     * method after a setConnectionPaarameters call in order to use the device
     * apn instead of the fixed one
     */
    public void unsetFixedApn() {
        this.connectionParameters=null;
    }



    /**
     * Open up a connection to the given url with the given access accessMode and
     * @param url The URL for the connection
     * @param extra is some extra information that can be specified to specific
     * implementations
     * @return the connection (cannot be null)
     * @throws java.io.IOException
     */
    public synchronized HttpConnectionAdapter openHttpConnection(String url, Object extra) throws IOException
    {
        if (this.connectionParameters != null) {
            Log.debug(TAG_LOG, "Fixed Apn parameters detected for tis connection: "
                      + url + this.connectionParameters);
            HttpConnectionAdapter conn = createAdapter(extra);
            conn.open(url + this.connectionParameters);
            return conn;
        }

        //If the GPRS coverage was lost the ServiceBook could have changed
        //A refesh is needed
        Log.debug(TAG_LOG, "Refreshing the configuration");
        ConnectionConfig.refreshServiceBookConfigurations();

        //Just displays the network coverage
        Log.trace(TAG_LOG, "Current network informations:\n"
                  + BlackberryUtils.getNetworkCoverageReport());

        //Checks the wifi status and removes it if the bearer is offline and
        //it is the current working configuration
        Log.trace(TAG_LOG, "Checking wifi status");
        //Denies the wifi usage permission if the bearer is not available or
        //turned off
        if (!BlackberryUtils.isWifiActive()||!BlackberryUtils.isWifiAvailable()) {
            Log.debug(TAG_LOG, "checkWifiStatus Wifi not available");
            //switch to the TCPConfiguration if the wifi network is not
            //available and the last working configuration was set to WIFI
            if (workingConfigID==ConnectionConfig.WIFI_CONFIG) {
                workingConfigID++;//=ConnectionConfig.CONFIG_NONE;
            }
        }

        //if the GPRS bearer is not available and wifi is available, then it
        //become the working id
        if (workingConfigID > 0 && BlackberryUtils.isWapGprsDataBearerOffline()) {
            Log.debug(TAG_LOG, "GPRS bearer is offline: switching to wifi if available");
            if (BlackberryUtils.isWifiActive() && BlackberryUtils.isWifiAvailable()) {
                Log.debug(TAG_LOG, "WIFI bearer is online: changing working configuration");
                workingConfigID=ConnectionConfig.WIFI_CONFIG;
            } else {
                //Doesn't try the connection because in this case there is no
                //bearer available.
                Log.debug(TAG_LOG, "WIFI bearer is offline: "
                          + "no suitable bearer were found. Throwing IOException");
                throw new IOException();
            }
        }

        if (workingConfigID < 0) {
            Log.debug(TAG_LOG, "Setting up the connection...");
            return setupHttpConnection(url, extra);
        } else {
            Log.trace(TAG_LOG, "Working Configuration already assigned - ID="
                      + workingConfigID);
            Log.trace(TAG_LOG, "Using configuration: ID="
                      + workingConfigID + " - Description "
                      + configurations[workingConfigID].getDescription());
            HttpConnectionAdapter ret = null;
            try {
                Log.debug(TAG_LOG, "Opening connection with: "
                          + configurations[workingConfigID].getDescription());
                String fullUrl = url + configurations[workingConfigID].getUrlParameters();
                Log.trace(TAG_LOG, "Opening url: " + fullUrl);
                ret = createAdapter(extra);
                ret.open(fullUrl);
            } catch (Exception ioe) {
                Log.debug(TAG_LOG, "Error occured while accessing " +
                          "the network with the last working configuration");
                // If the current configuration no longer works, we try all the
                // known connections to see if we find one that works.
                // Connections that have already been granted/denied do not
                // result in an extra prompt to the user, we rather keep his
                // previous answer.
                workingConfigID=ConnectionConfig.CONFIG_NONE;
                // Close the connection if it got opened
                if (ret != null) {
                    try {
                        ret.close();
                    } catch (Exception e) {
                        Log.debug(TAG_LOG, "Setup Failed Closing connection: "
                                  + "setting up another configuration");
                        Log.debug(TAG_LOG, "Exception: " + e);
                    }
                }
                // If all the possible connections are not working, the setup
                // will throw an exception resulting in an exception in the
                // "open".
                ret = setupHttpConnection(url, extra);
            }
            return ret;
        }
    }

    /**
     * Open a socket connection to the given URL
     * @param addr is the server address
     * @param port the port
     * @param mode can be READ_WRITE
     * @param timeout enable timeout on IO operations
     */
    public SocketAdapter openSocketConnection(String addr, int port, int mode, boolean timeout) throws IOException {
        SocketAdapter res = new SocketAdapter(addr, port, mode, timeout);
        return res;
    }

    /**
     * Add optional parameters (such as APN configurations or wifi interface) to
     * the given url
     * @param url is the request url without configurations parameters
     * @return String representing the url with the optional parameter added
     */
    private HttpConnectionAdapter setupHttpConnection(String url, Object extra)
    throws IOException {

        HttpConnectionAdapter ret = null;
        String requestUrl = null;
        for (int i = 0; i < ConnectionConfig.MAX_CONFIG_NUMBER; i++) {
            try {
                Log.debug(TAG_LOG, "Looping configurations: "
                          + (i+1) + "/" + ConnectionConfig.MAX_CONFIG_NUMBER);
                //If the open operation fails a subclass of IOException is thrown by the system
                if (isConfigurationAllowed(i)) {
                    Log.debug(TAG_LOG, "Configuration Allowed: " + (i+1));
                    workingConfigID = i % configurations.length;
                    String options = configurations[i].getUrlParameters();
                    Log.debug(TAG_LOG, "Using parameters: " + options);
                    requestUrl = url + options;
                } else {
                    Log.debug(TAG_LOG, "Setup config " + (i+1) + " cannot be used.");
                    continue;
                }

                Log.debug(TAG_LOG, "Connecting to: " + requestUrl);
                ret = createAdapter(extra);
                ret.open(requestUrl);
                //If the open call is succesfull it could be useful to notify
                //the current working configuration changes to the listener
                connectionListener.connectionConfigurationChanged();
                Log.debug(TAG_LOG, "Listener notified");

                // If we connected with a BIS configuration, then the user is
                // not on BES and we can safely disable the BES configuration.
                // This is more than an optimization, it is required to
                // guarantee the correct execution because users with direct TCP
                // experiences fake connections and weird timeouts when BES is
                // being used
                if (ConnectionConfig.isDirectTCP(workingConfigID)) {
                    Log.debug(TAG_LOG, "Direct TCP works, disable BES configuration");
                    int besId = ConnectionConfig.getBESConfigurationID();
                    if (besId != -1) {
                        configurations[besId].setPermission(ConnectionConfig.PERMISSION_DENIED);
                    }
                }
                return ret;
            } catch (Exception ioe) {
                Log.debug(TAG_LOG, "Connection not opened at attempt " + (i + 1));
                Log.debug(TAG_LOG, ioe.toString());

                // Close the connection in case it got opened
                if (ret != null) {
                    try {
                        ret.close();
                    } catch (Exception e) {
                        Log.debug(TAG_LOG, "Failed Closing connection: trying next");
                        Log.debug(TAG_LOG, "Exception: " + e);
                    }
                }
                ret = null;
            }
        }

        if (ret != null) {
            return ret;
        } else {
            workingConfigID = ConnectionConfig.CONFIG_NONE;
            //Doesn't return a null connection but throws an IOException
            throw new IOException("[ConnectionManager.setup]Cannot find a suitable configuration");
        }
    }

    private HttpConnectionAdapter createAdapter(Object extra) {
        HttpConnectionAdapter res = null;
        if (extra != null && extra instanceof String) {
            String e = (String)extra;
            if ("wrapper".equals(e)) {
                res = new HttpConnectionAdapterWrapper();
            }
        }
        if (res == null) {
            res = new HttpConnectionAdapter();
        }
        return res;
    }
}

