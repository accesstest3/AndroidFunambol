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

package com.funambol.syncml.spds;

/**
 * This class groups all configuration properties related to the device.
 * Most of DeviceConfig properties are used to generate the 
 * <pre><DevInf></pre> element for client capabilities.
 * DeviceConfig is a part of SyncConfig.
 *
 */
public class DeviceConfig {

    //--------------------------------------------------------------- Constants
    private static final int MAXMSGSIZE = 16*1024;

    //-------------------------------------------------------------- Attributes
    /**
     * Specifies the major and minor version identifier of the Device
     * Information DTD used in the representation of the Device Information. The
     * value MUST be "1.1". This property is mandatory.
     */
    public String verDTD;

    /**
     * Specifies the name of the manufacturer of the device. Mandatory
     */
    public String man;
    
    /**
     * Specifies the model name or model number of the device. Mandatory
     */
    public String mod;

    /**
     * Specifies the OEM (Original Equipment Manufacturer) of the device. 
     * This property is optional.
     */
    public String oem;

    /**
     * Specifies the firmware version of the device. Mandatory
     */
    public String fwv;

    /**
     * Specifies the software version of the device. Mandatory
     */
    public String swv;

    /**
     * Specifies the hardware version of the device. This property is optional.
     */
    public String hwv;

    /**
     * Specifies the identifier of the source synchronization device. 
     * The content information MUST specify a theoretically, globally unique
     * identifier. This property is mandatory.
     */
    public String devID;

    /**
     * Specifies the type of the source synchronization device. 
     * Type values for this element type can be e.g. "pager", "handheld",
     * "pda", "phone", "smartphone", "server", "workstation". 
     * Other values can also be specified. This property is mandatory.
     */
    public String devType;

    /**
     * Specifies the implemented DS version. This property is optional.           
     */
    public String dsV;

    /**
     * Specifies that the device supports UTC based time. 
     * If utc = TRUE, the server SHOULD send time in UTC format, else MUST
     * send in local time. Default value = TRUE.
     */
    public boolean utc;

    /**
     * Specifies that the device supports handling of large objects. 
     * Default value = FALSE.
     */
    public boolean loSupport;

    /**
     * Specifies that the device supports number of changes. 
     * Default value = FALSE.
     */
    public boolean nocSupport;

    /**
     * Specifies the maximum message size allowed by the device.
     */
    private int maxMsgSize;

    /**
     * Specifies the maximum object size allowed by the device.
     * Default value = 0 (no maxObjSize set).
     */
    public int maxObjSize;

    //------------------------------------------------------------ Constructors

    /**
     * Default constructor. All the mandatory informations are set to 
     * a default value, or to the values actually supported by the current
     * SyncML implementation. The optional fields are set to null.
     *
     * TODO: get the values from the device configuration (e.g. IMEI)
     */
    public DeviceConfig() {
        verDTD = "1.1";
        man = null;
        mod = null;
        oem = null;
        fwv = null;
        swv = null;
        hwv = null;
        devID = "fsc-j2me-api";
        devType = "phone";
        dsV = "1.2";
        utc = true;                     // TODO: check this.
        loSupport = true;
        nocSupport = false;
        maxMsgSize = MAXMSGSIZE;
        maxObjSize = MAXMSGSIZE;
    }

    public int getMaxMsgSize() {
        return maxMsgSize;
    }

    public void setMaxMsgSize(int value) {
        maxMsgSize = value;
    }

}

