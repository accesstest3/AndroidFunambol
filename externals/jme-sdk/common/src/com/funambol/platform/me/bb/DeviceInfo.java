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

import java.util.TimeZone;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.system.RadioInfo;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.ServiceConfiguration;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ControlledAccessException;

import com.funambol.util.Log;
import net.rim.device.api.i18n.Locale;

public class DeviceInfo implements DeviceInfoInterface {

    private static final String TAG_LOG = "DeviceInfo";

    private String osVersion;

    public DeviceInfo() {
        initVersion();
    }

    /**
     * Returns the phone number or null if not available.
     */
    public String getPhoneNumber() {
        try {
            return Phone.getDevicePhoneNumber(false);
        } catch(ControlledAccessException ex) {
            // The user didn't allowed to read the phone number from the device
            Log.error(TAG_LOG, "The user didn't allowed to read the phone number");
            return null;
        }
    }

    /**
     * Returns the platform or null if not available. The platform here is a
     * Funambol identification of the client build.
     */
    public String getFunambolPlatform() {
        if (checkOS(4, 7, 0)) {
            return "bbpim_os47";
        } else {
            return "bbpim";
        }
    }


    /**
     * Returns the main email adddress or null if not available.
     */
    public String getEmailAddress() {
        Session session = Session.getDefaultInstance();
        if (session != null) {
            ServiceConfiguration config = session.getServiceConfiguration();
            if (config != null) {
                return config.getEmailAddress();
            }
        }
        return "";
    }

    /**
     * Returns the device timezone or null if not available.
     */
    public String getTimezone() {
        return TimeZone.getDefault().getID();
    }

    /**
     * Returns the device manufacturer or null if not available.
     */
    public String getManufacturer() {
        return net.rim.device.api.system.DeviceInfo.getManufacturerName();
    }

    /**
     * Returns the device model or null if not available.
     */
    public String getDeviceModel() {
        return net.rim.device.api.system.DeviceInfo.getDeviceName();
    }

    /**
     * Returns the carrier name, or null if not available.
     */
    public String getCarrier() {
        return RadioInfo.getCurrentNetworkName();
    }

    /**
     * Returns the A2 country code, or null if not available.
     */
    public String getCountryCode() {
        return Locale.getDefaultForSystem().getCountry();
    }

    public String getHardwareVersion() {
        return net.rim.device.api.system.DeviceInfo.getPlatformVersion();
    }

    public String getDeviceId() {
        return "" + net.rim.device.api.system.DeviceInfo.getDeviceId();
    }

    public boolean isRoaming() {
        return RadioInfo.getNetworkService() == RadioInfo.NETWORK_SERVICE_ROAMING;
    }

    public String getOSVersion() {
        return osVersion;
    }

    public boolean checkOS(int high, int mid, int low)
    {
        String os = getOSVersion();

        if (os == null || !(os.length() > 3)) {
            return false;
        }

        try {

            final int major = Integer.valueOf(os.substring(0, 1)).intValue();
            final int middle = Integer.valueOf(os.substring(2, 3)).intValue();

            int minor = 0;

            if (os.length() > 4) {
                minor = Integer.valueOf(os.substring(4, 5)).intValue();
            }

            boolean gte = (
                    (major > high)
                    ||
                    (major == high && middle > mid)
                    ||
                    (major == high && middle == mid && minor >= low)
                    );

            return gte;
        } catch (Exception e) {
            Log.error(TAG_LOG, "Cannot check OS version: " + os, e);
            return false;
        }
    }

    private void initVersion() {
        final ApplicationDescriptor[] appDes = ApplicationManager.getApplicationManager()
                .getVisibleApplications();
        String version = "";
        for (int i = appDes.length - 1; i >= 0; --i) {
            if (appDes[i].getModuleName().equals("net_rim_bb_ribbon_app")) {
                version = appDes[i].getVersion();
                break;
            }
        }
        osVersion = version;
    }
}
