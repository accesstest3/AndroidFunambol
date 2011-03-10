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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Container class for blackberry configurations. It provides information over
 * the configuration to be used in order to have a working connection over 
 * blackberry based systems. This class implements the serializable interface as 
 * it is useful for eventual persistent implementation. This is also the reason 
 * why the constructor is explicitly defined.
 */
public class BlackberryConfiguration {

    /**The permission to use the configuration owned by this class*/
    private int permission = ConnectionConfig.PERMISSION_UNDEFINED;
    /**The String to be added to the URL when a connection is requested*/
    private String urlParameters = ConnectionConfig.NO_PARAMETERS;
    /**The String formatted description for this configuration*/
    private String description = ConnectionConfig.NO_DESCRIPTION;

    /**
     * Default public empty constructor. It is explicitly defined because in 
     * further implementation it is not to be excluded that this class will 
     * implement the Serializable interface.
     */
    public BlackberryConfiguration() {
    }

    /**
     * Accessor method to get the permission field of this class
     * @return int permission associated with this configuration
     */
    public int getPermission() {
        return this.permission;
    }

    /**
     * Accessor method to set the permission field of this configuration
     * @param permission is the permission to be set for this configuration
     */
    public void setPermission(int permission) {
        this.permission = permission;
    }

    /**
     * Accessor method to get the urlParameters field of this configuration
     * @return String configuration to be added to the request URL in order to 
     * activate this configuration when the connector is opened
     */
    public String getUrlParameters() {
        return this.urlParameters;
    }

    /**
     * Accessor method to set the urlParameters for this configuration
     * @param urlParameters is the parameter string to be set for connections 
     * requests
     */
    public void setUrlParameters(String urlParameters) {
        this.urlParameters = urlParameters;
    }

    /**
     * Accessor method to get this configuration description
     * @return String description of this configuration
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Accessor method to set this configuration's description
     * @param description new description for this configuration
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Implementation of the Serializable interface method
     * @param out is the DataOutputStream where this class' fields must be 
     * serialized
     * @throws java.io.IOException
     */
    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(this.urlParameters);
        out.writeInt(this.permission);
        out.writeUTF(this.description);
    }

    /**
     * Implementation of the Serializable interface method
     * @param in is the DataInputStream to be read in order to deserialize this 
     * class into a BlackberryConfiguration object
     * @throws java.io.IOException
     */
    public void deserialize(DataInputStream in) throws IOException {
        urlParameters = in.readUTF();
        permission = in.readInt();
        description = in.readUTF();
    }
}
