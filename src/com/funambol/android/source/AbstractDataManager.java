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

package com.funambol.android.source;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncAdapterType;
import com.funambol.android.R;
import com.funambol.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;


public abstract class AbstractDataManager<E> {

    private static final String TAG_LOG = "AbstractDataManager";

    protected Context context;
    protected ContentResolver resolver;

    protected String accountType = null;
    protected String accountName = null;
    
    public AbstractDataManager(Context context) {
        this.context = context;
        resolver = context.getContentResolver();
        initAccount();
    }

    /**
     * @return the specific authority for this source
     */
    protected abstract String getAuthority();

    /**
     * Loads a generic item, given the key.
     * 
     * @param key
     * @return
     * @throws IOException
     */
    public abstract E load(long key) throws IOException;

    /**
     * Adds a generic item to the source.
     * 
     * @param item
     * @return
     * @throws IOException
     */
    public abstract long add(E item) throws IOException;

    /**
     * Updates a existing item.
     * 
     * @param id
     * @param newItem
     * @throws IOException
     */
    public abstract void update(long id, E newItem) throws IOException;

    /**
     * Deletes a specific item given the id.
     * 
     * @param id
     * @throws IOException
     */
    public abstract void delete(long id) throws IOException;

    /**
     * Deletes all the items from this source
     * 
     * @throws IOException
     */
    public abstract void deleteAll() throws IOException;

    /**
     * Checks if and item exists.
     * 
     * @param id
     * @return
     */
    public abstract boolean exists(long id);

    /**
     * @return an <code>Enumeration</code> containing all the items keys.
     * 
     * @throws IOException
     */
    public abstract Enumeration getAllKeys() throws IOException;
    
    
    /**
     * Initializes the account information
     */
    public void initAccount() {
        Log.trace(TAG_LOG, "Initializing");

        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccounts();

        SyncAdapterType[] syncs = ContentResolver.getSyncAdapterTypes();
        ArrayList<Account> accounts_al = new ArrayList<Account>();
        ArrayList<String> accountTypes = new ArrayList<String>();

        for (SyncAdapterType sync : syncs) {
            if (getAuthority().equals(sync.authority) && sync.supportsUploading()) {
                accountTypes.add(sync.accountType);
            }
        }
        for (Account acct: accounts) {
            if (accountTypes.contains(acct.type)) {
                accounts_al.add(acct);
            }
        }
        if (accounts_al.size() == 0) {
            Log.info(TAG_LOG, "No accounts defined, this is an error");
            accountName = null;
            accountType = null;
        } else if (accounts_al.size() == 1) {
            Log.info(TAG_LOG, "Single account defined, use it");
            Account account = accounts_al.get(0);
            accountName = account.name;
            accountType = account.type;

            if (!context.getString(R.string.account_type).equals(accountType)) {
                accountName = null;
                accountType = null;
            } else {
                Log.info(TAG_LOG, "Account found " + accountType + "," + accountName);
            }
        } else {
            // We have more than one account, the user shall be prompted to
            // choose one....
            Log.info(TAG_LOG, "Multiple accounts defined, search for the right one");
            for(Account account: accounts_al) {
                Log.info(TAG_LOG, "Found account " + account.name + "," + account.type);
                if (context.getString(R.string.account_type).equals(account.type))
                {
                    // Found it!
                    Log.info(TAG_LOG, "Account found");
                    accountName = account.name;
                    accountType = account.type;
                    break;
                }
            }
            if (accountName == null) {
                // TODO FIXME: how do we handle this error? shall we recreate the
                // account here?
            }
        }
    }
}
