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

import com.funambol.util.StringUtil;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Corresponds to the &lt;Ext&gt; element in the SyncML devinf DTD and specifies
 * the non-standard, experimental extensions supported by the device.
 *
 * @version $Id: Ext.java,v 1.2 2007/07/30 22:10:05 stefano_fornari Exp $
 */
public class Ext {

    
    // ext for smart slow sync
    public static final String X_FUNAMBOL_SMARTSLOW = "X-funambol-smartslow";
  
    // ext for multiple email account
    public static final String X_FUNAMBOL_MULTIPLE_EMAIL_ACCOUNT = "X-funambol-multiple-email-account";

    // ext for servers supporting media content upload via http
    public static final String X_FUNAMBOL_MEDIA_UPLOAD_HTTP = "X-funambol-media-http-upload";

    // ------------------------------------------------------------ Private data
    private String xNam;
    private Vector xVal = new Vector();

    // ------------------------------------------------------------ Constructors
    /**
     * In order to expose the server configuration like WS this constructor
     * must be public
     */
    public Ext() {}

    /**
     * Creates a new Ext object with the given name and value
     *
     *  @param xNam corresponds to the &lt;XNam&gt; element - NOT NULL
     *  @param xVal an array of parameters that corresponds to the &ltXVal&gt
     *                  element
     *
     */
    public Ext(final String xNam, final String[] xVal) {
        setXNam(xNam);
        setXVal(xVal);
    }

    // ---------------------------------------------------------- Public methods
    /**
     * Gets the name of the extension
     *
     * @return the name of the extension
     */
    public String getXNam() {
        return xNam;
    }

    /**
     * Sets the name of extension
     *
     * @param xNam the name of extension
     *
     */
    public void setXNam(String xNam) {
        if (xNam == null) {
            throw new IllegalArgumentException("XNam cannot be null");
        }
        this.xNam = xNam;
    }

    /**
     * Gets an array of extension values, if exist
     *
     * @return an array of extension values, if exist
     */
    public Vector getXVal() {
        return xVal;
    }

    /**
     * Sets the array of extension value
     *
     * @param xVal the array of extension value
     *
     */
    public void setXVal(String[] xVal) {
        if (xVal == null) {
            throw new IllegalArgumentException("xVal cannot be null");
        }
        
        this.xVal = StringUtil.getVectorFromArray(xVal);
    }

    /**
     * Sets the array of extension value
     *
     * @param xVal the array of extension value
     *
     */
    public void setXVal(Vector xVal) {
        if (xVal == null) {
            throw new IllegalArgumentException("xVal cannot be null");
        }
           this.xVal = xVal;
    }

    public void addXVal(String val) {
        this.xVal.addElement(val);
    }

    public void serialize(DataOutputStream out) throws IOException {
        // TODO: implementation
        throw new IOException("Not supported yet.");
    }

    public void deserialize(DataInputStream in) throws IOException {
        // TODO: implementation
        throw new IOException("Not supported yet.");
    }
}
