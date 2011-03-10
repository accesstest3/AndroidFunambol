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

package com.funambol.client.controller;

import com.funambol.client.customization.Customization;
import com.funambol.client.localization.Localization;
import com.funambol.client.sapi.SapiHandler;
import com.funambol.client.ui.SignupScreen;
import com.funambol.platform.DeviceInfoInterface;
import com.funambol.util.Log;
import com.funambol.util.StringUtil;
import java.io.IOException;
import java.util.Vector;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 * Handles the signup process in a separated thread. Example:
 * new SignupHandler(SignupScreen, SignupScreenController).start();
 */
public class SignupHandler extends Thread {

    private static final String TAG_LOG = "SignupHandler";

    public static final int VALIDATION_MODE_NONE        = 0;
    public static final int VALIDATION_MODE_CAPTCHA     = 1;
    public static final int VALIDATION_MODE_SMS         = 2;
    public static final int VALIDATION_MODE_SMS_CAPTCHA = 3;

    private static final String JSON_OBJECT_DATA  = "data";
    private static final String JSON_OBJECT_USER  = "user";
    private static final String JSON_OBJECT_ERROR = "error";

    private static final String JSON_OBJECT_ERROR_FIELD_CODE       = "code";
    private static final String JSON_OBJECT_ERROR_FIELD_MESSAGE    = "message";
    private static final String JSON_OBJECT_ERROR_FIELD_CAUSE      = "cause";
    private static final String JSON_OBJECT_ERROR_FIELD_PARAMETERS = "parameters";
    private static final String JSON_OBJECT_ERROR_FIELD_PARAM      = "param";

    private static final String JSON_OBJECT_USER_FIELD_ACTIVE       = "active";
    private static final String JSON_OBJECT_USER_FIELD_PHONE_NUMBER = "phonenumber";
    private static final String JSON_OBJECT_USER_FIELD_PASSWORD     = "password";
    private static final String JSON_OBJECT_USER_FIELD_PLATFORM     = "platform";
    private static final String JSON_OBJECT_USER_FIELD_COUNTRY      = "countrya2";
    private static final String JSON_OBJECT_USER_FIELD_EMAIL        = "useremail";
    private static final String JSON_OBJECT_USER_FIELD_TIMEZONE     = "timezone";
    private static final String JSON_OBJECT_USER_FIELD_MANUFACTURER = "manufacturer";
    private static final String JSON_OBJECT_USER_FIELD_MODEL        = "model";
    private static final String JSON_OBJECT_USER_FIELD_CARRIER      = "carrier";

    public static final String JSON_ERROR_CODE_PRO_1000 = "PRO-1000";
    public static final String JSON_ERROR_CODE_PRO_1001 = "PRO-1001";
    public static final String JSON_ERROR_CODE_PRO_1106 = "PRO-1106";
    public static final String JSON_ERROR_CODE_PRO_1107 = "PRO-1107";
    public static final String JSON_ERROR_CODE_PRO_1113 = "PRO-1113";
    public static final String JSON_ERROR_CODE_PRO_1115 = "PRO-1115";
    public static final String JSON_ERROR_CODE_PRO_1122 = "PRO-1122";
    public static final String JSON_ERROR_CODE_PRO_1126 = "PRO-1126";
    public static final String JSON_ERROR_CODE_PRO_1127 = "PRO-1127";
    public static final String JSON_ERROR_CODE_PRO_1128 = "PRO-1128";
    public static final String JSON_ERROR_CODE_COM_1006 = "COM-1006";
    public static final String JSON_ERROR_CODE_COM_1008 = "COM-1008";

    private SignupScreen signupScreen = null;
    private SignupScreenController signupScreenController = null;

    private Customization customization;
    private Localization localization;

    public SignupHandler(SignupScreen signupScreen,
            SignupScreenController signupScreenController) {
        this.signupScreen = signupScreen;
        this.signupScreenController = signupScreenController;
        this.localization = signupScreenController.localization;
        this.customization = signupScreenController.customization;
    }
    
    public void run() {
        Log.info(TAG_LOG, "Signing up");

        signupScreen.disableSignup();
        signupScreenController.signupStarted();

        // Get the current values from UI
        String serverUrl   = signupScreen.getSyncUrl();
        String phoneNumber = signupScreen.getUsername();
        String password    = signupScreen.getPassword();
        String token       = signupScreen.getCaptchaToken();

        String signupUrl = StringUtil.extractAddressFromUrl(serverUrl);

        SapiHandler sapiHandler = new SapiHandler(signupUrl);
        String jsessionId = signupScreenController.getCurrentJSessionId();
        if(jsessionId != null) {
            sapiHandler.enableJSessionAuthentication(true);
            sapiHandler.forceJSessionId(jsessionId);
        }
        try {
            Log.debug(TAG_LOG, "Sending Signup SAPI request");
            JSONObject request = createMobileSignupRequest(phoneNumber, password);
            Vector params = new Vector();
            StringBuffer tokenParam = new StringBuffer();
            tokenParam.append("token=").append(token);
            params.addElement(tokenParam.toString());
            JSONObject res = sapiHandler.query("mobile", "signup", params, null, request);
            if(!userActivated(res)) {
                Log.debug(TAG_LOG, "Account not activated by the server");
                handleResponseError(res, signupUrl);
                return;
            } else {
                Log.info(TAG_LOG, "Account activated by the server");
            }
        } catch(IOException ex) {
            // This is a network failure
            Log.error(TAG_LOG, "Unable to signup", ex);
            signupScreenController.signupFailed(
                    localization.getLanguage("signup_failed_network"));
            signupScreenController.promptCredentials();
            return;
        } catch(Exception ex) {
            // This is a generic failure
            Log.error(TAG_LOG, "Unable to signup", ex);
            signupScreenController.signupFailed(
                    localization.getLanguage("signup_failed_generic_message"));
            signupScreenController.promptCredentials();
            return;
        }
        // Signup Succeeded
        signupScreenController.signupSucceeded();
    }

    private JSONObject createMobileSignupRequest(String phoneNumber,
            String password) throws JSONException {

        JSONObject request = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject deviceInfo = new JSONObject();

        DeviceInfoInterface devInfo = signupScreenController.getDeviceInfo();

        deviceInfo.put(JSON_OBJECT_USER_FIELD_PHONE_NUMBER, phoneNumber);
        deviceInfo.put(JSON_OBJECT_USER_FIELD_PASSWORD,     password);
        deviceInfo.put(JSON_OBJECT_USER_FIELD_PLATFORM,     devInfo.getFunambolPlatform());
        deviceInfo.put(JSON_OBJECT_USER_FIELD_COUNTRY,      devInfo.getCountryCode());
        deviceInfo.put(JSON_OBJECT_USER_FIELD_EMAIL,        devInfo.getEmailAddress());
        deviceInfo.put(JSON_OBJECT_USER_FIELD_TIMEZONE,     devInfo.getTimezone());
        deviceInfo.put(JSON_OBJECT_USER_FIELD_MANUFACTURER, devInfo.getManufacturer());
        deviceInfo.put(JSON_OBJECT_USER_FIELD_MODEL,        devInfo.getDeviceModel());
        deviceInfo.put(JSON_OBJECT_USER_FIELD_CARRIER,      devInfo.getCarrier());

        data.put(JSON_OBJECT_USER, deviceInfo);
        request.put(JSON_OBJECT_DATA, data);

        return request;
    }

    private boolean userActivated(JSONObject response) {
        boolean active = false;
        try {
            JSONObject resData = response.getJSONObject(JSON_OBJECT_DATA);
            if(resData != null) {
                JSONObject resUser = resData.getJSONObject(JSON_OBJECT_USER);
                if(resUser != null) {
                    active = resUser.getBoolean(JSON_OBJECT_USER_FIELD_ACTIVE);
                }
            }
        } catch(JSONException ex) {
            Log.debug(TAG_LOG, "Failed to retrieve user data json object");
        } 
        return active;
    }

    /**
     * Handles the response error if any
     * @param response
     * @param signupUrl
     * @throws JSONException
     */
    private void handleResponseError(JSONObject response, String signupUrl) throws Exception {

        try {
            // Check for errors
            JSONObject error = response.getJSONObject(JSON_OBJECT_ERROR);
            if(error != null) {
                String code    = error.getString(JSON_OBJECT_ERROR_FIELD_CODE);
                String message = error.getString(JSON_OBJECT_ERROR_FIELD_MESSAGE);
                String cause   = error.getString(JSON_OBJECT_ERROR_FIELD_CAUSE);

                StringBuffer logMsg = new StringBuffer(
                        "Error in SAPI response").append("\r\n");
                logMsg.append("code: ").append(code).append("\r\n");
                logMsg.append("cause: ").append(cause).append("\r\n");
                logMsg.append("message: ").append(message).append("\r\n");

                JSONArray parameters = error.getJSONArray(JSON_OBJECT_ERROR_FIELD_PARAMETERS);
                for(int i=0; i<parameters.length(); i++) {
                    JSONObject parameter = parameters.getJSONObject(i);
                    String param = parameter.getString(JSON_OBJECT_ERROR_FIELD_PARAM);
                    logMsg.append("param: ").append(param).append("\r\n");
                }
                Log.debug(TAG_LOG, logMsg.toString());

                // Handle error codes
                boolean handled = false;
                if(JSON_ERROR_CODE_PRO_1000.equals(code)) {
                    // Unknown exception in profile handling
                } else if(JSON_ERROR_CODE_PRO_1001.equals(code)) {
                    // Missing the following mandatory parameter(s).
                } else if(JSON_ERROR_CODE_PRO_1106.equals(code)) {
                    // The userid is not valid. Only letters (a-z), numbers (0-9),
                    // and periods (.) are allowed. Userid must be less than 16
                    // characters and include at least one letter.
                } else if(JSON_ERROR_CODE_PRO_1107.equals(code)) {
                    // You can not specify an existing e-mail address.
                } else if(JSON_ERROR_CODE_PRO_1113.equals(code)) {
                    // You can not specify an existing username.
                    String msg = localization.getLanguage("signup_failed_username_exists");
                    signupScreenController.signupFailed(msg);
                    signupScreenController.promptCredentials();
                    handled = true;
                } else if(JSON_ERROR_CODE_PRO_1115.equals(code)) {
                    // The password is not valid. Only letters (a-z) and numbers
                    // (0-9) are allowed. Minimum 4, maximum 16 characters.
                    signupScreenController.signupFailed(localization.getLanguage(
                            "signup_failed_bad_password_message"));
                    signupScreenController.promptCredentials();
                    handled = true;
                } else if(JSON_ERROR_CODE_PRO_1122.equals(code)) {
                    // The e-mail address is not valid.
                } else if(JSON_ERROR_CODE_PRO_1126.equals(code) && 
                        customization.getDefaultMSUValidationMode() == VALIDATION_MODE_CAPTCHA ||
                        customization.getDefaultMSUValidationMode() == VALIDATION_MODE_SMS_CAPTCHA) {
                    // Invalid CAPTCHA token
                    signupScreenController.signupFailed(null, false);
                    signupScreenController.requestNewCaptcha(true);
                    handled = true;
                } else if(JSON_ERROR_CODE_PRO_1127.equals(code)) {
                    // Phone number already exist
                    String msg = localization.getLanguage("signup_failed_username_exists");
                    signupScreenController.signupFailed(msg);
                    signupScreenController.promptCredentials();
                    handled = true;
                } else if(JSON_ERROR_CODE_PRO_1128.equals(code)) {
                    // The phone number provided is not valid.
                    signupScreenController.signupFailed(localization.getLanguage(
                            "signup_failed_bad_phone_message"));
                    signupScreenController.promptCredentials();
                    handled = true;
                } else if(JSON_ERROR_CODE_COM_1006.equals(code)) {
                    // Invalid timezone.
                } else if(JSON_ERROR_CODE_COM_1008.equals(code)) {
                    // Invalid data type or data format is not as expected.
                }
                if(!handled) {
                    Log.error(TAG_LOG, "Unhandled error code: " + code);
                    throw new Exception("Unhandled error code: " + code);
                }
            }
        } catch(JSONException ex) {
            Log.debug(TAG_LOG, "Failed to retrieve error json object");
        }
    }
}
