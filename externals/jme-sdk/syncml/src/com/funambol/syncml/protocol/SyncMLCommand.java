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

package com.funambol.syncml.protocol;

/**
 * This class is a container for SyncML command
 */
public class SyncMLCommand {
    
    //----------------------------------------------------------------- Constants

    //-------------------------------------------------------------- Private data

    /** The command tag name */
    private String name;

    /** The id of this command */
    private String cmdId;
    
    /** The mime-type of the items in this command.  */
    private String type;
    
    //------------------------------------------------------------- Constructors
    public SyncMLCommand(String name, String cmdId) {
        this(name, cmdId, null);
    }
    
    public SyncMLCommand(String name, String cmdId, String type) {
        this.name = name;
        this.cmdId = cmdId;
        this.type = type;
    }

    //----------------------------------------------------------- Public methods
    
    /**
     * Get the command tag name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Get the command id
     */
    public String getCmdId() {
        return this.cmdId;
    }
    
    /**
     * Get the mime type of the items of this command
     */
    public String getType() {
        return this.type;
    }
    
    /**
     * Set the mime type of the items of this command
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Return a string representation of this object
     */
    public String toString() {
        StringBuffer ret = new StringBuffer();

        ret.append("<Command>").append(name).append("</Command>\n").
            append("<CmdId>").append(cmdId).append("</CmdId>\n").
            append("<Type>").append(type).append("</Type>\n\n");

        return ret.toString();
    }

}

