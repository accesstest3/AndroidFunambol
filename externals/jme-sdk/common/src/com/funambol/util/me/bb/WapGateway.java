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
 */

package com.funambol.util;

/**
 * Wrap around the concept of WAP gateway: provides the informations related to 
 * a WAP configurations like APN, Username, Password and country.
 */
public class WapGateway {

    private String apn;
    private String username;
    private String password;
    private String gatewayIP;
    private String country;

    /**
     * Default empty 
     */
    protected WapGateway() {
        
    }
    
    /**
     * The constructor must receive all of the parameters
     * @param apn is the String representation of the Access Point Name for this 
     * wap gateway
     * @param username required to access the given gateway (mandatory for some 
     * carrier)
     * @param password required to access the given gateway (mandatory for some 
     * carrier)
     * @param country is the country identificator for this gateway
     */
    public WapGateway(String apn, String username, String password, String country) {
        this.apn        = apn;
        this.username   = username;
        this.password   = password;
        this.gatewayIP  = null;
        this.country    = country;
    }

    /**
     * The constructor must receive all of the parameters
     * @param apn is the String representation of the Access Point Name for this 
     * wap gateway
     * @param username required to access the given gateway (mandatory for some 
     * carrier)
     * @param password required to access the given gateway (mandatory for some 
     * carrier)
     * @param gatewayIP the IP of the gateway
     * @param country is the country identificator for this gateway
     */
    public WapGateway(String apn, String username, String password,
                      String gatewayIP, String country) {
        this.apn        = apn;
        this.username   = username;
        this.password   = password;
        this.gatewayIP  = gatewayIP;
        this.country    = country;
    }


    /**
     * Accessor  method
     * @return String representation of the Access Point Name (APN)
     */
    public String getApn() {
        return apn;
    }

    /**
     * Accessor  method
     * @return String representation of the username related to this Access 
     * Point Name (APN)
     */
    public String getUsername() {
        return username;
    }

    /**
     * Accessor  method
     * @return String representation of the password related to this Access 
     * Point Name (APN)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Accessor  method
     * @return String representation of the country related to this Access 
     * Point Name (APN)
     */
    public String getCountry() {
        return country;
    }

    public String getGatewayIP() {
        return gatewayIP;
    }
}
