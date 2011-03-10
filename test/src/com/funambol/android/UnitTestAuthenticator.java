/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2009 Funambol, Inc.
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

package com.funambol.android;

import android.accounts.Account;
import android.os.Bundle;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;

import com.funambol.android.controller.AndroidController;
import com.funambol.util.Log;

/**
 * Funambol Unit Test Authenticator. This class simply creates an account if it
 * does not exist already.
 */
public class UnitTestAuthenticator extends AccountAuthenticatorActivity {

    private static final String TAG = "UnitTestAuthenticator";

    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Log.info(TAG, "Creating authenticator activity");

        Account account = AndroidController.getNativeAccount();

        // Check if a Funambol account already exists
        if(account == null) {
            // Create the Account
            Log.info(TAG, "Adding account explicitly");

            // The account label must be the username
            String accountLabel = "Test";
            //Set the account type
            String accountType = this.getString(R.string.account_type);

            // Create the new account
            account = new Account(accountLabel, accountType);

            AccountManager am = AccountManager.get(this);

            if (am.addAccountExplicitly(account, null, null)) {
                Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME,
                                 accountLabel);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                ((AccountAuthenticatorActivity)this).setAccountAuthenticatorResult(result);
            }
        }
        finish();
    }
}
