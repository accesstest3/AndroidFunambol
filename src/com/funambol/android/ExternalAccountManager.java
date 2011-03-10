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

package com.funambol.android;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncAdapterType;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.net.Uri;

import com.funambol.android.controller.AndroidController;
import com.funambol.client.configuration.Configuration;
import com.funambol.util.Log;
import com.funambol.util.StringUtil;


/**
 * This class manages the import of items owned by accounts that are created
 * externally from the Funambol application into the Funambol account.
 *
 * It is possible also to show/hide those items which have been previously
 * imported.
 */
public class ExternalAccountManager {

    private static final String TAG_LOG = "ExternalAccountManager";

    private static final String CONF_KEY_IMPORTED_ACCOUNTS   = "CONF_KEY_IMPORTED_ACCOUNTS";
    private static final String CONF_KEY_ACCOUNTS_IMPORTED   = "CONF_KEY_ACCOUNTS_IMPORTED";
    private static final String CONF_KEY_PHONE_ONLY_IMPORTED = "CONF_KEY_PHONE_ONLY_IMPORTED";
    private static final String CONF_KEY_SIM_IMPORTED        = "CONF_KEY_SIM_IMPORTED";

    private static final String CONF_KEY_HIDDEN_GROUPS     = "CONF_KEY_HIDDEN_GROUPS";
    private static final String CONF_KEY_HIDDEN_SETTINGS   = "CONF_KEY_HIDDEN_SETTINGS";

    private static final int  CONTACTS_BATCH_COUNT = 10;
    private static final long UNDEFINED_CONTACT_ID = -1;

    private static ExternalAccountManager instance = null;

    private Context context = null;
    
    private ContentResolver resolver = null;
    private Configuration configuration = null;

    private Account targetAccount = null;

    private static final String[] DATA_PROJECTION = {
        Data.MIMETYPE,
        Data.DATA1,
        Data.DATA2,
        Data.DATA3,
        Data.DATA4,
        Data.DATA5,
        Data.DATA6,
        Data.DATA7,
        Data.DATA8,
        Data.DATA9,
        Data.DATA10,
        Data.DATA11,
        Data.DATA12,
        Data.DATA13,
        Data.DATA14,
        Data.DATA15,
        Data.IS_PRIMARY,
        Data.IS_SUPER_PRIMARY
    };

    private static final String[] RAW_CONTACTS_PROJECTION = {
        RawContacts._ID,
        RawContacts.CONTACT_ID
    };

    private ExternalAccountManager(Context context) {
        this.context = context;
        this.resolver = context.getContentResolver();
        this.configuration = AndroidController.getInstance().getConfiguration();
        this.targetAccount = AndroidAccountManager.getNativeAccount(context);
    }

    /**
     * Singleton implementation
     *
     * @param c The application Context
     * @return The single instance of this class
     */
    public static synchronized ExternalAccountManager getInstance(Context c) {
        if (instance == null) {
            instance = new ExternalAccountManager(c);
        }
        return instance;
    }

    /**
     * Used by importAccountItems to notify of the import progress
     */
    public interface ItemsImportListener {

        /**
         * The total amount of items has been updated
         * @param count
         */
        public void setTotalItemsCount(int count);

        /**
         * The amount of imported items has been update
         * @param count
         */
        public void updateImportedItemsCount(int count);

    }

    /**
     * Import the items belonging to the given accounts to Funambol. Only
     * Contact items are supported.
     *
     * @param accounts The accounts from which the items shall be imported.
     * @param includePhoneOnly Include also items which don't belong to any
     * account.
     * @param includeSim Include also SIM items
     * @param listener The progress status listener
     *
     * @return The amount of imported items. -1 in case of errors
     */
    public int importAccountItems(Vector<Account> accounts, boolean includePhoneOnly,
            boolean includeSim, ItemsImportListener listener) {

        Log.trace(TAG_LOG, "importAccountItems");

        // Keep track of the catched excptions
       Exception lastException = null;

        // Update the target account
        this.targetAccount = AndroidAccountManager.getNativeAccount(context);

        //
        // Build the where clause for the raw contacts selection query
        //
        StringBuffer whereClause = new StringBuffer();

        // Includes the given accounts
        includeAccountsToSelection(whereClause, accounts);
        saveImportedAccounts(accounts);

        // Includes phone only items if needed
        if(includePhoneOnly) {
            includePhoneOnlyItemsToSelection(whereClause);
            savePhoneOnlyImported();
        }

        // Includes sim items if needed
        if(includeSim) {
            includeSimItemsToSelection(whereClause);
            saveSimImported();
        }

        // Excludes Funambol items
        excludeFunambolItemsToSelection(whereClause);

        String selection = whereClause.toString();
        Log.trace(TAG_LOG, "Query selection: " + selection);
        if(selection.length() == 0) {
            // No items to import
            Log.info(TAG_LOG, "No items to import");
            return 0;
        }

        // Queries the RawContacts table to find all the entries associated
        // with the given accounts.
        Cursor rawContactsCursor = resolver.query(RawContacts.CONTENT_URI,
                RAW_CONTACTS_PROJECTION, selection, null, null);

        int totalItemsCount = rawContactsCursor.getCount();
        if(listener != null) {
            listener.setTotalItemsCount(totalItemsCount);
        }

        // Holds the contacts which have been already added to the target account
        Hashtable<Long, Long> contactIds = new Hashtable<Long, Long>();
        int importedItemsCount = 0;

        // Add to the contactIds all the contact ids which already exist in the
        // target account. This is to avoid contacts duplication.
        Cursor targetAccountContactsCursor = resolver.query(RawContacts.CONTENT_URI,
                RAW_CONTACTS_PROJECTION,
                RawContacts.ACCOUNT_TYPE+"='"+targetAccount.type+"' AND "+
                RawContacts.ACCOUNT_NAME+"='"+targetAccount.name+"'",
                null, null);
        if(targetAccountContactsCursor != null &&
           targetAccountContactsCursor.moveToFirst()) {
            do {
                contactIds.put(targetAccountContactsCursor.getLong(1),
                        targetAccountContactsCursor.getLong(0));
            } while(targetAccountContactsCursor.moveToNext());
        }
        if(targetAccountContactsCursor != null) {
            targetAccountContactsCursor.close();
        }

        // Collects all the operations to perform in order to import a
        // single contact
        ArrayList<ContentProviderOperation> ops =
                new ArrayList<ContentProviderOperation>();

        int contactsBatchCount = 0;
        int tempReferenceId = 0;

        Hashtable<Long, Integer> backReferenceIds = new Hashtable<Long, Integer>();

        try {
            if(rawContactsCursor.moveToFirst()) {
                do {
                    long exRawContactId = rawContactsCursor.getLong(0);
                    long contactId = rawContactsCursor.getLong(1);

                    Log.trace(TAG_LOG, "Contact id: " + contactId);
                    if(contactId <= 0) {
                        Log.trace(TAG_LOG, "Invalid contact id: " + contactId);
                        totalItemsCount--;
                        if(listener != null) {
                            listener.setTotalItemsCount(totalItemsCount);
                        }
                        continue;
                    }

                    long newRowContactId = UNDEFINED_CONTACT_ID;

                    // Check if this contact is aggregated
                    boolean isAggregatedContact = contactIds.containsKey(contactId);
                    if(isAggregatedContact) {
                        Log.trace(TAG_LOG, "Aggregated contact found");
                        newRowContactId = contactIds.get(contactId);
                    } else {
                        Log.trace(TAG_LOG, "New contact found");
                        insertNewRawContact(ops);

                        // Keep track of the back reference id to be used while
                        // adding new data records
                        backReferenceIds.put(contactId, tempReferenceId);
                        tempReferenceId++;

                        // Put an undefined contact id. It will be stored once
                        // returned by applyBatch
                        contactIds.put(contactId, UNDEFINED_CONTACT_ID);
                    }

                    // Read the existing contact data
                    whereClause = new StringBuffer();
                    whereClause.append(Data.RAW_CONTACT_ID).append("=")
                            .append(exRawContactId);

                    Cursor dataCursor = resolver.query(Data.CONTENT_URI, DATA_PROJECTION,
                            whereClause.toString(), null, null);
                    try {
                        if(dataCursor.moveToFirst()) {
                            do {
                                Integer id = backReferenceIds.get(contactId);
                                insertNewDataRecord(dataCursor, newRowContactId,
                                        id, ops);
                                tempReferenceId++;
                            } while(dataCursor.moveToNext());
                        }
                    } finally {
                        dataCursor.close();
                    }
                    
                    contactsBatchCount++;
                    importedItemsCount++;
                    if(listener != null) {
                        listener.updateImportedItemsCount(importedItemsCount);
                    }
                    if(contactsBatchCount == CONTACTS_BATCH_COUNT || rawContactsCursor.isLast()) {
                        try {
                            ContentProviderResult[] res = resolver.applyBatch(
                                    ContactsContract.AUTHORITY, ops);
                            // Get the new created contact ids using the back
                            // reference ids
                            if(!backReferenceIds.isEmpty()) {
                                Set<Long> keys = backReferenceIds.keySet();
                                for(long key : keys) {
                                    int backReference = backReferenceIds.get(key);
                                    long id = ContentUris.parseId(res[backReference].uri);
                                    contactIds.put(key, id);
                                }
                            }
                        } catch(Exception ex) {
                             Log.error(TAG_LOG, "Failed to import contacts batch", ex);
                             lastException = ex;
                        } finally {
                            tempReferenceId = 0;
                            contactsBatchCount = 0;
                            backReferenceIds.clear();
                            ops.clear();
                        }
                    }
                    
                } while(rawContactsCursor.moveToNext());
            }
        } catch(Exception ex) {
            Log.error(TAG_LOG, "Exception in importAccountItems", ex);
            lastException = ex;
        } finally {
            Log.info(TAG_LOG, "Imported contacts count: " + importedItemsCount);
            rawContactsCursor.close();
        }
        // Return -1 if something went wrong
        if(lastException != null) {
            return -1;
        } else {
            return importedItemsCount;
        }
    }

    private void includeAccountsToSelection(StringBuffer whereClause,
            Vector<Account> accounts) {
        Log.debug(TAG_LOG, "Include accounts");
        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.elementAt(i);
            Log.info(TAG_LOG, "Importing account: " + account);
            if (i != 0) {
                whereClause.append(" OR ");
            }
            String accountName = account.name;
            String accountType = account.type;
            whereClause
                    .append("(")
                    .append(RawContacts.ACCOUNT_NAME)
                    .append("='").append(accountName).append("'")
                    .append(" AND ")
                    .append(RawContacts.ACCOUNT_TYPE)
                    .append("='").append(accountType).append("'")
                    .append(")");
        }
    }

    private void includePhoneOnlyItemsToSelection(StringBuffer whereClause) {
        Log.debug(TAG_LOG, "Include phone only items");

        // To include phone only contacts we have just to include contacts
        // which don't belong to any account

        Vector<Account> allAccounts = listContactAccounts(true, false);
        if(allAccounts.isEmpty()) {
            whereClause.append("(1)");
        }
        boolean first = true;
        for(int i=0; i<allAccounts.size(); i++) {
            Account a = allAccounts.elementAt(i);
            String accountName = a.name;
            String accountType = a.type;
            if(first) {
                if(whereClause.length() > 0) {
                    whereClause.append(" OR ");
                }
                whereClause.append("(");
                first = false;
            } else {
                whereClause.append(" AND ");
            }
            whereClause
                .append("(")
                .append(RawContacts.ACCOUNT_NAME)
                .append("!='").append(accountName).append("'")
                .append(" AND ")
                .append(RawContacts.ACCOUNT_TYPE)
                .append("!='").append(accountType).append("'")
                .append(")");
        }
        if(!first) {
            whereClause.append(")");
            // Exclude sim contacts
            whereClause
                    .append(" AND (")
                    .append(RawContacts.ACCOUNT_TYPE)
                    .append(" NOT LIKE '%.sim%')");
        }
    }

    private void includeSimItemsToSelection(StringBuffer whereClause) {
        Log.debug(TAG_LOG, "Include SIM items");
        if(whereClause.length() > 0) {
            whereClause.append(" OR ");
        }
        whereClause
                .append("(")
                .append(RawContacts.ACCOUNT_TYPE)
                .append(" LIKE '%.sim%')");
    }

    private void excludeFunambolItemsToSelection(StringBuffer whereClause) {
        // Make sure to not include funambol items
        String funType = context.getString(R.string.account_type);
        if(whereClause.length() > 0) {
            whereClause.append(" AND ");
            whereClause
                    .append("(")
                    .append(RawContacts.ACCOUNT_TYPE)
                    .append("!='" + funType + "')");
        }
    }

    /**
     * Returns the amount of items belonging to the given account
     *
     * @param account
     * @return
     */
    public int getAccountItemsCount(Account account) {
        int count = 0;
        Cursor result = null;
        try {
            result = resolver.query(RawContacts.CONTENT_URI, null,
                    RawContacts.ACCOUNT_NAME + "='" + account.name + "' AND " +
                    RawContacts.ACCOUNT_TYPE + "='" + account.type+ "'", null, null);
            if(result != null) {
                count = result.getCount();
            }
        } finally {
            if(result != null) {
                result.close();
            }
        }
        return count;
    }

    /**
     * @return true if the provider actually contains SIM items
     */
    public boolean hasSimItems() {
        Cursor simCursor = resolver.query(RawContacts.CONTENT_URI,
                new String[] {RawContacts._ID},
                RawContacts.ACCOUNT_TYPE+" LIKE '%.sim%'", null, null);
        boolean result = false;
        if(simCursor != null) {
            result = simCursor.getCount() > 0;
            simCursor.close();
        }
        return result;
    }

    /**
     * @return true if the provider actually contains phone only items
     */
    public boolean hasPhoneOnlyItems() {

        StringBuffer whereClause = new StringBuffer();
        includePhoneOnlyItemsToSelection(whereClause);
        excludeFunambolItemsToSelection(whereClause);

        Cursor phoneOnlyCursor = resolver.query(RawContacts.CONTENT_URI,
                new String[] {RawContacts._ID},
                whereClause.toString(), null, null);
        boolean result = false;
        if(phoneOnlyCursor != null) {
            result = phoneOnlyCursor.getCount() > 0;
            phoneOnlyCursor.close();
        }
        return result;
    }

    /**
     * @return true if any account has been already imported
     */
    public boolean accountsImported() {
        return configuration.loadBooleanKey(CONF_KEY_ACCOUNTS_IMPORTED, false);
    }

    /**
     * @return true if phone only items have been already imported
     */
    public boolean phoneOnlyImported() {
        return configuration.loadBooleanKey(CONF_KEY_PHONE_ONLY_IMPORTED, false);
    }

    /**
     * @return true if the SIM items have been already imported
     */
    public boolean simImported() {
        return configuration.loadBooleanKey(CONF_KEY_SIM_IMPORTED, false);
    }

    /**
     * Resets the configuration values
     */
    public void reset() {
        configuration.saveBooleanKey(CONF_KEY_ACCOUNTS_IMPORTED, false);
        configuration.saveBooleanKey(CONF_KEY_PHONE_ONLY_IMPORTED, false);
        configuration.saveBooleanKey(CONF_KEY_SIM_IMPORTED, false);
        configuration.saveStringKey(CONF_KEY_IMPORTED_ACCOUNTS, "");
        configuration.saveStringKey(CONF_KEY_HIDDEN_GROUPS, "");
        configuration.saveStringKey(CONF_KEY_HIDDEN_SETTINGS, "");
        configuration.save();
    }

    /**
     * Adds a new ContentProviderOperation to the given operations list which
     * add a new contact Data record by importing the given cursor's data fields
     *
     * @param c The Cursor from which the data fields shall be imported
     * @param ops The List of operations
     */
    private void insertNewDataRecord(Cursor c, long newRowContactId,
            Integer backReferenceId, List<ContentProviderOperation> ops) {

        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);

        if(newRowContactId != UNDEFINED_CONTACT_ID) {
            builder = builder.withValue(ContactsContract.Data.RAW_CONTACT_ID, newRowContactId);
        } else {
            builder = builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backReferenceId);
        }

        builder = builder.withValue(Data.MIMETYPE, c.getString(0));
        builder = builder.withValue(Data.DATA1, c.getString(1));
        builder = builder.withValue(Data.DATA2, c.getString(2));
        builder = builder.withValue(Data.DATA3, c.getString(3));
        builder = builder.withValue(Data.DATA4, c.getString(4));
        builder = builder.withValue(Data.DATA5, c.getString(5));
        builder = builder.withValue(Data.DATA6, c.getString(6));
        builder = builder.withValue(Data.DATA7, c.getString(7));
        builder = builder.withValue(Data.DATA8, c.getString(8));
        builder = builder.withValue(Data.DATA9, c.getString(9));
        builder = builder.withValue(Data.DATA10, c.getString(10));
        builder = builder.withValue(Data.DATA11, c.getString(11));
        builder = builder.withValue(Data.DATA12, c.getString(12));
        builder = builder.withValue(Data.DATA13, c.getString(13));
        builder = builder.withValue(Data.DATA14, c.getString(14));
        builder = builder.withValue(Data.DATA15, c.getBlob(15));
        builder = builder.withValue(Data.IS_PRIMARY, new Integer(c.getInt(16)));
        builder = builder.withValue(Data.IS_SUPER_PRIMARY, new Integer(c.getInt(17)));

        ops.add(builder.build());
    }

    /**
     * Adds a new ContentProviderOperation to the given operations list which
     * inserts a new RawContact item.
     *
     * @param ops The List of operations
     */
    private void insertNewRawContact(List<ContentProviderOperation> ops) {
        ContentProviderOperation i1 = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
            .withValue(RawContacts.ACCOUNT_NAME, targetAccount.name)
            .withValue(RawContacts.ACCOUNT_TYPE, targetAccount.type)
            .withValue(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DEFAULT)
            .build();
        ops.add(i1);
    }

    /**
     * Lists the accounts which support contact items.
     *
     * @param includeReadOnlyAccounts
     * @param excludeImportedAccounts
     * @return The Vector of the accounts
     */
    public Vector<Account> listContactAccounts(boolean includeReadOnlyAccounts,
            boolean excludeImportedAccounts) {

        Log.trace(TAG_LOG, "listContactAccounts: " + includeReadOnlyAccounts);

        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccounts();

        SyncAdapterType[] syncs = ContentResolver.getSyncAdapterTypes();
        Vector<Account> contactAccounts = new Vector<Account>();
        Vector<String> contactAccountTypes = new Vector<String>();

        for (SyncAdapterType sync : syncs) {
            if (ContactsContract.AUTHORITY.equals(sync.authority) &&
                    (includeReadOnlyAccounts || sync.supportsUploading())) {
                contactAccountTypes.add(sync.accountType);
            }
        }
        String funType = context.getString(R.string.account_type);
        for (Account acct: accounts) {
            // Exclude Funambol accounts
            if (!funType.equals(acct.type) &&
                    contactAccountTypes.contains(acct.type)) {
                if(excludeImportedAccounts) {
                    Vector<Account> importedAccounts = loadImportedAccounts();
                    boolean isImported = false;
                    for(int i=0; i<importedAccounts.size(); i++) {
                        Account account = importedAccounts.elementAt(i);
                        if(account.name.equals(acct.name) &&
                           account.type.equals(acct.type)) {
                            isImported = true;
                        }
                    }
                    if(!isImported) {
                        contactAccounts.add(acct);
                    }
                } else {
                    contactAccounts.add(acct);
                }
            }
        }
        return contactAccounts;
    }

    /**
     * Makes the previously imported accounts items (e.g. contacts) to be
     * hidden to the user.
     */
    public void hideImportedAccounts() {
        Log.info(TAG_LOG, "Hiding imported accounts");
        showHideImportedAccounts(false);
    }

    /**
     * Makes all the device's accounts items (e.g. contacts) to be hidden to the
     * user.
     */
    public void hideAllAccounts() {
        Log.info(TAG_LOG, "Hiding all accounts");
        showHideAllAccounts(false);
    }

    /**
     * Makes the accounts items (e.g. contacts) which have been hidden by the
     * hideImportedAccounts method, to be visible to the user.
     */
    public void showHiddenAccounts() {
        Log.info(TAG_LOG, "Showing hidden accounts");
        showHideImportedAccounts(true);
    }

    /**
     * Shows/Hides the imported accounts items depending on the show parameter.
     * Actually only contact items are supported.
     *
     * @param show
     */
    private void showHideImportedAccounts(boolean show) {
        Vector<Account> accounts = new Vector<Account>();
        if(!show) {
            accounts = loadImportedAccounts();
        }
        showHideAccounts(accounts, show);
    }

    /**
     * Shows/Hides all the accounts items depending on the show parameter.
     * Actually only contact items are supported.
     *
     * @param show
     */
    private void showHideAllAccounts(boolean show) {
        Vector<Account> accounts = new Vector<Account>();
        if(!show) {
            accounts = listContactAccounts(true, false);
        }
        showHideAccounts(accounts, show);
    }

    /**
     * Shows/Hides the give accounts depending on the show parameter.
     *
     * @param accounts
     * @param show
     */
    private void showHideAccounts(Vector<Account> accounts, boolean show) {
        showHideAccountGroups(accounts, show);
        showHideAccountSettings(accounts, show);
    }

    /**
     * Shows/Hides the contact groups belonging to the given accounts depending
     * on the show parameter.
     *
     * @param accounts
     * @param show
     */
    private void showHideAccountGroups(Vector<Account> accounts, boolean show) {
        Log.trace(TAG_LOG, "showHideAccountGroups: " + show);

        String[] hiddenGroupIds;

        if(!show) {

            // If the settings shall be hidden we must retrieve the visible
            // groups for the given accounts.

            StringBuffer whereClause = new StringBuffer();
            whereClause.append("(");
            for(int i=0; i<accounts.size(); i++) {
                Account account = accounts.elementAt(i);
                if(i != 0) {
                    whereClause.append(" OR ");
                }
                whereClause
                        .append("(")
                        .append(ContactsContract.Groups.ACCOUNT_NAME)
                        .append("='").append(account.name).append("'")
                        .append(" AND ")
                        .append(ContactsContract.Groups.ACCOUNT_TYPE)
                        .append("='").append(account.type).append("'")
                        .append(")");
            }
            whereClause.append(")");
            whereClause.append(" AND ")
                    .append(ContactsContract.Groups.GROUP_VISIBLE).append("=1");

            Cursor groups = resolver.query(ContactsContract.Groups.CONTENT_URI,
                    new String[] { ContactsContract.Groups._ID },
                    whereClause.toString(), null, null);

            Vector<String> tempIds = new Vector<String>();
            if(groups.moveToFirst()) {
                do {
                    String id = groups.getString(0);
                    Log.trace(TAG_LOG, "Found group to show/hide id: " + id);
                    tempIds.add(id);
                } while(groups.moveToNext());
            }
            groups.close();

            hiddenGroupIds = new String[tempIds.size()];
            tempIds.toArray(hiddenGroupIds);

            // Save the hidden groups into the configuration
            saveHiddenGroupIds(hiddenGroupIds);
        } else {
            // The hidden groups are already known by the configuration
            hiddenGroupIds = loadHiddenGroupIds();
        }
        // Show/hide groups
        StringBuffer whereClause = new StringBuffer();
        for(String id : hiddenGroupIds) {
            if(whereClause.length() > 0) {
                whereClause.append(" OR ");
            }
            Log.debug(TAG_LOG, "Setting visibility for group id: " + id);
            whereClause.append(ContactsContract.Groups._ID).append("=").append(id);
        }
        ContentValues cv = new ContentValues();
        int visible = show ? 1 : 0;
        cv.put(ContactsContract.Groups.GROUP_VISIBLE, visible);
        resolver.update(ContactsContract.Groups.CONTENT_URI, cv,
                whereClause.toString(), null);
    }

    /**
     * Shows/Hides the contacts which doesn't belong to any account group. This
     * is performed by setting the UNGROUPED_VISIBLE field into the account
     * Settings table.
     *
     * See http://developer.android.com/reference/android/provider/
     *      ContactsContract.SettingsColumns.html#UNGROUPED_VISIBLE
     *
     * @param accounts
     * @param show
     */
    private void showHideAccountSettings(Vector<Account> accounts, boolean show) {
        Log.trace(TAG_LOG, "showHideAccountSettings: " + show);

        Vector<Account> hiddenAccountSettings = new Vector();

        if(!show) {

            // If the settings shall be hidden we must retrieve the visible
            // settings for the given accounts.

            StringBuffer whereClause = new StringBuffer();
            whereClause.append("(");
            for(int i=0; i<accounts.size(); i++) {
                Account account = accounts.elementAt(i);
                if(i != 0) {
                    whereClause.append(" OR ");
                }
                whereClause
                        .append("(")
                        .append(ContactsContract.Settings.ACCOUNT_NAME)
                        .append("='").append(account.name).append("'")
                        .append(" AND ")
                        .append(ContactsContract.Settings.ACCOUNT_TYPE)
                        .append("='").append(account.type).append("'")
                        .append(")");
            }
            whereClause.append(")");
            whereClause.append(" AND ")
                    .append(ContactsContract.Settings.UNGROUPED_VISIBLE).append("=1");

            Cursor settings = resolver.query(ContactsContract.Settings.CONTENT_URI,
                    new String[] { ContactsContract.Settings.ACCOUNT_NAME,
                                   ContactsContract.Settings.ACCOUNT_TYPE },
                    whereClause.toString(), null, null);

            if(settings.moveToFirst()) {
                do {
                    String name = settings.getString(0);
                    String type = settings.getString(1);
                    Account account = new Account(name, type);
                    Log.trace(TAG_LOG, "Found account to show/hide: " + account);
                    hiddenAccountSettings.add(account);
                } while(settings.moveToNext());
            }
            settings.close();
            // Save the hidden settings into the configuration
            saveHiddenAccountSettings(hiddenAccountSettings);
        } else {
            // The hidden settings are already known by the configuration
            hiddenAccountSettings = loadHiddenAccountSettings();
        }
        // Show/hide settings
        StringBuffer whereClause = new StringBuffer();
        for(int i=0; i<hiddenAccountSettings.size(); i++) {
            Account account = hiddenAccountSettings.elementAt(i);
            Log.debug(TAG_LOG, "Setting visibility for account: " + account);
            if(i != 0) {
                whereClause.append(" OR ");
            }
            whereClause.append("(");
            whereClause.append(ContactsContract.Settings.ACCOUNT_NAME).append("='")
                    .append(account.name).append("'");
            whereClause.append(" AND ");
            whereClause.append(ContactsContract.Settings.ACCOUNT_TYPE).append("='")
                    .append(account.type).append("'");
            whereClause.append(")");
        }
        ContentValues cv = new ContentValues();
        int visible = show ? 1 : 0;
        cv.put(ContactsContract.Settings.UNGROUPED_VISIBLE, visible);
        resolver.update(ContactsContract.Settings.CONTENT_URI, cv,
                whereClause.toString(), null);
    }

    private String[] loadHiddenGroupIds() {
        Log.debug(TAG_LOG, "Loading hidden groups from the configuration");
        String value = configuration.loadStringKey(CONF_KEY_HIDDEN_GROUPS, null);
        if(value != null) {
            return StringUtil.split(value, "\t");
        }
        return new String[0];
    }

    private Vector<Account> loadHiddenAccountSettings() {
        Log.debug(TAG_LOG, "Loading hidden account settings from the configuration");
        return loadAccountsFromConfiguration(CONF_KEY_HIDDEN_SETTINGS);
    }

    private Vector<Account> loadImportedAccounts() {
        Log.debug(TAG_LOG, "Loading imported accounts from the configuration");
        return loadAccountsFromConfiguration(CONF_KEY_IMPORTED_ACCOUNTS);
    }

    private Vector<Account> loadAccountsFromConfiguration(String confKey) {
        Log.trace(TAG_LOG, "loadAccountsFromConfiguration: " + confKey);
        Vector<Account> result = new Vector<Account>();
        String accountsString = configuration.loadStringKey(CONF_KEY_IMPORTED_ACCOUNTS, null);
        if(StringUtil.isNullOrEmpty(accountsString)) {
            return result;
        }
        // Parse the accounts string
        String[] accountsStringArray = StringUtil.split(accountsString, "\n");
        for(String accountString : accountsStringArray) {
            String[] accountInfo = StringUtil.split(accountString, "\t");
            if(accountInfo != null && accountInfo.length == 2) {
                String name = accountInfo[0];
                String type = accountInfo[1];
                Account account = new Account(name, type);
                Log.debug(TAG_LOG, "Loading account: " + account);
                result.add(account);
            } else {
                Log.error(TAG_LOG, "Invalid account string: " + accountString);
            }
        }
        return result;
    }

    private void saveHiddenGroupIds(String[] ids) {
        Log.debug(TAG_LOG, "Saving hidden groups information");
        String value = StringUtil.join(ids, "\t");
        configuration.saveStringKey(CONF_KEY_HIDDEN_GROUPS, value);
        configuration.save();
    }

    private void saveHiddenAccountSettings(Vector<Account> accounts) {
        Log.debug(TAG_LOG, "Saving hidden account settings information");
        saveAccountsToConfiguration(CONF_KEY_HIDDEN_SETTINGS, accounts, false);
    }

    private void saveImportedAccounts(Vector<Account> accounts) {
        Log.debug(TAG_LOG, "Saving imported accounts information");
        configuration.saveBooleanKey(CONF_KEY_ACCOUNTS_IMPORTED, true);
        saveAccountsToConfiguration(CONF_KEY_IMPORTED_ACCOUNTS, accounts, true);
    }

    private void savePhoneOnlyImported() {
        configuration.saveBooleanKey(CONF_KEY_PHONE_ONLY_IMPORTED, true);
        configuration.save();
    }

    private void saveSimImported() {
        configuration.saveBooleanKey(CONF_KEY_SIM_IMPORTED, true);
        configuration.save();
    }

    private void saveAccountsToConfiguration(String confKey,
            Vector<Account> accounts, boolean append) {
        Log.trace(TAG_LOG, "saveAccountsToConfiguration: " + confKey);
        StringBuffer toSave = new StringBuffer();
        if(append && accountsImported()) {
            String existingData = configuration.loadStringKey(confKey, null);
            if(existingData != null) {
                toSave.append(existingData).append("\n");
            }
        }
        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.elementAt(i);
            Log.debug(TAG_LOG, "Saving account: " + account);
            if (i != 0) {
                toSave.append("\n");
            }
            toSave.append(account.name).append("\t").append(account.type);
        }
        configuration.saveStringKey(confKey, toSave.toString());
        configuration.save();
    }
}
