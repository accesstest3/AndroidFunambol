/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2010 Funambol, Inc.
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

package com.funambol.platform;

import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.RadioInfo;

import com.funambol.util.Log;

public class NetworkStatus {

    private static final String TAG_LOG = "NetworkStatus";

    public NetworkStatus() {
    }

    public boolean isWiFiConnected() {
        return isWifiAvailable() && isWifiActive();
    }

    public boolean isMobileConnected() {
        // TODO FIXME!!!!
        return !isRadioOff();
    }

    public boolean isConnected() {
        return isWiFiConnected() || isMobileConnected();
    }

    public boolean isRadioOff() {
        return RadioInfo.getState()==RadioInfo.STATE_OFF ||
               RadioInfo.getSignalLevel() == RadioInfo.LEVEL_NO_COVERAGE;
    }

    /**
     * Give the information about the presence o a wifi bearer on the device
     * @return true if the wifi communication interface bearer is supported by 
     * the device, false otherwise
     */
    protected boolean isWifiAvailable() {
        Log.info(TAG_LOG, "Checking WIFI Availability");
        boolean isWifiEnabled;
        if (RadioInfo.areWAFsSupported(RadioInfo.WAF_WLAN)) {
            Log.info(TAG_LOG, "WIFI Supported");
            isWifiEnabled = true;
        } else {
            Log.info(TAG_LOG, "WIFI NOT Supported");
            isWifiEnabled = false;
        }
        return isWifiEnabled;
    }

    /**
     * Give information about the presence of active wifi connections. 
     * @return true if the device is connected to a wifi network with its wifi 
     * bearer, false otherwise
     */
    protected boolean isWifiActive() {
        Log.info(TAG_LOG, "Checking WIFI Availability");

        int active = RadioInfo.getActiveWAFs();
        int wifi = RadioInfo.WAF_WLAN;

        Log.debug(TAG_LOG, "Active WAFs Found: " + active);
        Log.debug(TAG_LOG, "WIFI WAF DEFINITION: " + wifi);

        return active >= wifi;
    }
}
