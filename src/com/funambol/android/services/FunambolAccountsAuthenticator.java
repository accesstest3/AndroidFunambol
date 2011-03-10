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

package com.funambol.android.services;

import android.app.Service;
import android.os.IBinder;
import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.funambol.android.AndroidCustomization;
import com.funambol.android.activities.AndroidLoginScreen;
import com.funambol.android.activities.AndroidSignupScreen;
import com.funambol.android.controller.AndroidController;

import com.funambol.util.Log;

/**
 * Defines a custom account authenticator
 */
public class FunambolAccountsAuthenticator extends Service {

    private final String TAG = "FunambolAccountsAuthenticator";

    private AccountAuthenticator saa;
    private Context context;

    @Override
    public IBinder onBind(Intent intent) {

        IBinder ret = null;

        if (intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT)) {
            ret = getAuthenticator().getIBinder();
        }
        return ret;
    }

    private AccountAuthenticator getAuthenticator() {
        if (saa == null) {
            saa = new AccountAuthenticator(this);
        }
        return saa;
    }

    private class AccountAuthenticator extends AbstractAccountAuthenticator {

        public AccountAuthenticator(Context c) {
            super(c);
            context = c;
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, 
                    String accountType, String authTokenType,
                    String[] requiredFeatures, Bundle options) throws NetworkErrorException {

            Log.debug(TAG, "Adding account of type: " + accountType);

            Bundle ret = new Bundle();

            Intent intent = new Intent(context, getAuthenticatorActivityClass());
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

            ret.putParcelable(AccountManager.KEY_INTENT, intent);
            return ret;
        }

        private Class getAuthenticatorActivityClass() {
            AndroidCustomization customization = AndroidCustomization.getInstance();
            AndroidController controller = AndroidController.getInstance();
            if(customization.getMobileSignupEnabled() &&
                    !controller.getConfiguration().getSignupAccountCreated()) {
                return AndroidSignupScreen.class;
            } else {
                String accountScreenClassName = customization.getLoginScreenClassName();
                Class accountScreenClass = null;
                try {
                    accountScreenClass = Class.forName(accountScreenClassName);
                    Log.trace(TAG, "accountScreenClass = " + accountScreenClass);
                } catch (Exception e) {
                    Log.error(TAG, "Cannot find account screen class " + accountScreenClassName);
                }
                // In case of error we revert to the standard one
                if (accountScreenClass == null) {
                    accountScreenClass = AndroidLoginScreen.class;
                }
                return accountScreenClass;
            }
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
            Log.trace(TAG, "confirmCredentials");
            return null;
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
            Log.trace(TAG, "editProperties");
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
                                   String authTokenType, Bundle loginOptions) throws NetworkErrorException {
            Log.trace(TAG, "getAuthToken");
            return null;
        }

        @Override
        public String getAuthTokenLabel(String authTokenType) {
            Log.trace(TAG, "getAuthTokenLabel");
            return null;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
                                  String[] features) throws NetworkErrorException
        {
            Log.trace(TAG, "hasFeatures");
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response,
                                        Account account, String authTokenType, Bundle loginOptions)
        {
            Log.trace(TAG, "updateCredentials");
            return null;
        }
    }
}
