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

/*
 * This code makes use of Android native sources:
 *
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.funambol.android.edit_contact;

import android.os.Build;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.util.Log;

import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.funambol.android.AndroidAppSyncSourceManager;
import com.funambol.android.AppInitializer;
import com.funambol.android.BuildInfo;
import com.funambol.android.R;
import com.funambol.android.services.EmptyService;
import com.funambol.android.source.pim.contact.ContactAppSyncSourceConfig;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.util.StringUtil;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * Activity used to edit/create contacts from a Funambol account.
 */
public class AndroidEditContact extends Activity implements View.OnClickListener {

    private static final String TAG_LOG = "AndroidEditContact";
    
    private final int MENU_SAVE_ID   = Menu.FIRST;
    private final int MENU_REVERT_ID = MENU_SAVE_ID + 1;
    private final int MENU_DELETE_ID = MENU_REVERT_ID + 1;

    private final int DIALOG_CONFIRM_DELETE = 1;
    private final int DIALOG_SELECT_ACCOUNT = 2;

    /** The launch code when picking a photo and the raw data is returned */
    private static final int PHOTO_PICKED_WITH_DATA = 3021;

    private final String KEY_CURRENT_CONTACTS_VALUES  = "current_contacts_values";
    private final String KEY_RAW_CONTACT_ID_REQUESTING_PHOTO = "photorequester";
    
    // This is the container for all the Contacts
    private LinearLayout contacts_view;

    // The Contact current state
    private ContactValues[] currentStates;

    private String accountType = null;
    private String accountName = null;

    // When the DIALOG_SELECT_ACCOUNT is displayed, this flag specifies if the
    // contact is being edited or created
    private boolean editing = false;

    // The Contact original state
    private static ContactValues[] originalStates;
    private static int intentContactsCount = 0;

    private long rawContactIdRequestingPhoto = -1;

    private SaveContactTask saveContactTask = new SaveContactTask();

    private AndroidAppSyncSourceManager sManager;

    /**
     * Called with the activity is first created. 
     */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        AppInitializer appInitializer = AppInitializer.getInstance(this);
        appInitializer.init();

        sManager = appInitializer.getAppSyncSourceManager();

        initAccount();

        setContentView(R.layout.edit_contact);

        contacts_view = (LinearLayout)findViewById(R.id.contact_editors);

        findViewById(R.id.done_button).setOnClickListener(this);
        findViewById(R.id.revert_button).setOnClickListener(this);
        
        Intent intent = getIntent();
        String action = intent.getAction();

        final boolean hasIncomingState = icicle != null &&
                icicle.containsKey(KEY_CURRENT_CONTACTS_VALUES);

        if (Intent.ACTION_EDIT.equals(action) && !hasIncomingState) {
            new EditContactTask().execute();
        } else if (Intent.ACTION_INSERT.equals(action) && !hasIncomingState) {
            new AddContactTask().execute();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if(hasValidStates()) {
            outState.putParcelableArray(KEY_CURRENT_CONTACTS_VALUES,
                getContactValuesFromUI());
        }
        outState.putLong(KEY_RAW_CONTACT_ID_REQUESTING_PHOTO, rawContactIdRequestingPhoto);
        
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedState) {

        currentStates = (ContactValues[])savedState.getParcelableArray(
                KEY_CURRENT_CONTACTS_VALUES);

        rawContactIdRequestingPhoto = savedState.getLong(
                KEY_RAW_CONTACT_ID_REQUESTING_PHOTO);

        bindUI(true);

        super.onRestoreInstanceState(savedState);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case DIALOG_CONFIRM_DELETE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.deleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DeleteContactTask())
                        .setCancelable(false)
                        .create();
            case DIALOG_SELECT_ACCOUNT:
                return createSelectAccountDialog();
        }
        return super.onCreateDialog(id);
    }

    @Override
    public void onBackPressed() {
        doSave();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Ignore failed requests
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case PHOTO_PICKED_WITH_DATA: {
                EditContactView requestingContactView = null;
                for (int i = 0; i < contacts_view.getChildCount(); i++) {
                    View childView = contacts_view.getChildAt(i);
                    if (childView instanceof EditContactView) {
                        EditContactView contactView = (EditContactView) childView;
                        if (contactView.getRawContactId() == rawContactIdRequestingPhoto) {
                            requestingContactView = contactView;
                            break;
                        }
                    }
                }
                if (requestingContactView != null) {
                    final Bitmap photo = data.getParcelableExtra("data");
                    requestingContactView.setPhotoBitmap(photo);
                    rawContactIdRequestingPhoto = -1;
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem saveItem = menu.add(0, MENU_SAVE_ID, Menu.NONE,
                getApplicationContext().getString(R.string.menu_save));
        saveItem.setIcon(android.R.drawable.ic_menu_save);
        saveItem.setAlphabeticShortcut('\n');

        MenuItem revertItem = menu.add(0, MENU_REVERT_ID, Menu.NONE,
                getApplicationContext().getString(R.string.menu_revert));
        revertItem.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        revertItem.setAlphabeticShortcut('q');

        MenuItem deleteItem = menu.add(0, MENU_DELETE_ID, Menu.NONE,
                getApplicationContext().getString(R.string.menu_delete));
        deleteItem.setIcon(android.R.drawable.ic_menu_delete);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SAVE_ID:
                return doSave();
            case MENU_REVERT_ID:
                return doRevert();
            case MENU_DELETE_ID:
                return doDelete();
        }
        return false;
    }

    public void initAccount() {

        AccountManager am = AccountManager.get(getApplicationContext());
        Account[] accounts = am.getAccounts();

        SyncAdapterType[] syncs = ContentResolver.getSyncAdapterTypes();
        ArrayList<Account> contactAccounts = new ArrayList<Account>();
        ArrayList<String> contactAccountTypes = new ArrayList<String>();

        for (SyncAdapterType sync : syncs) {
            if (ContactsContract.AUTHORITY.equals(sync.authority) && sync.supportsUploading()) {
                contactAccountTypes.add(sync.accountType);
            }
        }

        for (Account acct: accounts) {
            if (contactAccountTypes.contains(acct.type)) {
                contactAccounts.add(acct);
            }
        }

        if (contactAccounts.size() == 0) {
            accountName = null;
            accountType = null;
        } else if (contactAccounts.size() == 1) {
            Account account = contactAccounts.get(0);
            accountName = account.name;
            accountType = account.type;
            if (!getString(R.string.account_type).equals(accountType)) {
                accountName = null;
                accountType = null;
            }
        } else {
            for(Account account: contactAccounts) {
                if (getApplicationContext().getString(R.string.account_type).equals(account.type))
                {
                    accountName = account.name;
                    accountType = account.type;
                    break;
                }
            }
        }
    }

    /**
     * Bind the UI with the background state
     */
    private void bindUI(boolean refresh) {

        if(!hasValidStates()) {
            return;
        }
        
        LayoutInflater inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        // Remove any existing contact views
        contacts_view.removeAllViews();

        for (int i = 0; i < currentStates.length; i++) {

            EditContactView contactView;
            contactView = (EditContactView)inflater.inflate(
                    R.layout.edit_contact_view, contacts_view, false);

            EditContactPhotoView photoEditor = contactView.getPhoto();
            photoEditor.setEditorListener(new PhotoListener(currentStates[i].rawContactId,
                    photoEditor));

            contactView.setState(ContactDataStructure.getInstance(), currentStates[i], refresh);
            contacts_view.addView(contactView);
        }

        // Show the contact editors
        contacts_view.setVisibility(View.VISIBLE);
    }

    /**
     * Returns true if the contact has a valid state. In our application a
     * contact is considered valid if it meets one of two possible conditions:
     *
     * <ul>
     *   <li> it has at least one field and all its fields belong to the
     *   Funambol account (isFunambolOnly)</li>
     *   <li> it has at least one field and some fields (1+) belong Funambol
     *   while others (1+) belong to another account (isAggregated)</li>
     * </ul>
     */
    private boolean hasValidStates() {
        return isFunambolOnly() || isAggregated();
    }

    private boolean isAggregated() {
        return currentStates != null &&
               currentStates.length != intentContactsCount &&
               currentStates.length > 0;
    }

    private boolean isFunambolOnly() {
        return currentStates != null &&
               currentStates.length == intentContactsCount &&
               currentStates.length > 0;
    }

    /** Called when one of the done or revert buttons is clicked */
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.done_button:
                doSave();
                break;
            case R.id.revert_button:
                doRevert();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(saveContactTask != null) {
            saveContactTask.activityDestroyed();
        }
    }

    /**
     * Populates the contacts state data from the UI
     * @return
     */
    private ContactValues[] getContactValuesFromUI() {

        ContactValues values = null;

        for(int i=0; i<contacts_view.getChildCount(); i++) {

            EditContactView contactView = (EditContactView)contacts_view.getChildAt(i);
            values = contactView.getContactValues();
            currentStates[i] = values;

            // Keep the original raw contact id
            currentStates[i].rawContactId = originalStates[i].rawContactId;
        }
        return currentStates;
    }

    /**
     * Populates the contacts state data from the store
     * @param intent
     * @return
     */
    private ContactValues[] getContactValuesFromStore() {

        final ContentResolver resolver = getContentResolver();

        // Handle both legacy and new authorities
        final Uri data = getIntent().getData();
        final String authority = data.getAuthority();
        final String mimeType = getIntent().resolveType(resolver);

        String selection = "0";

        if (ContactsContract.AUTHORITY.equals(authority)) {

            if (ContactsContract.Contacts.CONTENT_ITEM_TYPE.equals(mimeType)) {

                // Handle selected aggregate
                final long contactId = ContentUris.parseId(data);
                selection = ContactsContract.RawContacts.CONTACT_ID + "=" + contactId;
            } else if (ContactsContract.RawContacts.CONTENT_ITEM_TYPE.equals(mimeType)) {

                final long rawContactId = ContentUris.parseId(data);
                Cursor contactIdCursor = null;
                long contactId = -1;

                try {
                    contactIdCursor = resolver.query(ContactsContract.RawContacts.CONTENT_URI,
                    new String[] {ContactsContract.RawContacts.CONTACT_ID},
                    ContactsContract.RawContacts._ID + "=" + rawContactId, null, null);
                    // Normally this query shall always find something, but in
                    // some cases we have seen it returning an empty set because
                    // the raw contact id specified in the Intent is not a raw
                    // contact id, but rather a contact id. For this reason if
                    // the query returns an empty set, we use it as a contact
                    // id.
                    if (contactIdCursor != null && contactIdCursor.moveToFirst()) {
                        contactId = contactIdCursor.getLong(0);
                    } else {
                        contactId = rawContactId;
                    }
                } finally {
                    if (contactIdCursor != null) {
                        contactIdCursor.close();
                    }
                }
                selection = ContactsContract.RawContacts.CONTACT_ID + "=" + contactId;
            }
        } else if (android.provider.Contacts.AUTHORITY.equals(authority)) {
            final long rawContactId = ContentUris.parseId(data);
            selection = ContactsContract.Contacts.Data.RAW_CONTACT_ID + "=" + rawContactId;
        }

        // Build current states
        Cursor c = resolver.query(ContactsContract.RawContacts.CONTENT_URI,
                new String[] {ContactsContract.RawContacts._ID,
                ContactsContract.RawContacts.CONTACT_ID,
                ContactsContract.RawContacts.ACCOUNT_TYPE}, selection, null, null);
        try {
            Vector<ContactValues> statesTemp = new Vector<ContactValues>();

            intentContactsCount = c.getCount();

            if (c.moveToFirst()) {
                do {
                    int rawContactId = c.getInt(c.getColumnIndex(
                                ContactsContract.RawContacts._ID));
                    String account = c.getString(c.getColumnIndex(
                                ContactsContract.RawContacts.ACCOUNT_TYPE));

                    if(this.accountType != null && this.accountType.equals(account)) {
                        statesTemp.add(ContactValues
                                .fromRawContactId(resolver, rawContactId));
                    }
                } while(c.moveToNext());
            }

            originalStates = new ContactValues[statesTemp.size()];
            for(int i=0; i<statesTemp.size(); i++) { 
                originalStates[i] = statesTemp.get(i);
            }

        } finally {
            c.close();
        }

        currentStates = originalStates.clone();
        return currentStates;
    }

    private void initEmptyContactState() {

        intentContactsCount = 1;
        
        originalStates    = new ContactValues[1];
        originalStates[0] = new ContactValues();

        currentStates = originalStates.clone();
    }

    private boolean doSave() {
        AsyncTask.Status taskStatus = saveContactTask.getStatus();
        if(taskStatus == AsyncTask.Status.RUNNING || taskStatus == AsyncTask.Status.FINISHED) {
            Log.e(TAG_LOG, "Cannot save contact twice");
            return false;
        } else {
            try {
                saveContactTask.execute();
                return true;
            } catch(Exception ex) {
                Log.e(TAG_LOG, "Error while saving contact: " + ex);
                return false;
            }
        }
    }

    private boolean doDelete() {
        showDialog(DIALOG_CONFIRM_DELETE);
        return true;
    }

    private boolean doRevert() {
        // Simply close the activity
        finish();
        return true;
    }

    private boolean doPickPhotoAction(long rawContactId) {

        try {
            // Launch picker to choose photo for selected contact
            startActivityForResult(getPhotoPickIntent(), PHOTO_PICKED_WITH_DATA);
            rawContactIdRequestingPhoto = rawContactId;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private Intent getPhotoPickIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 96);
        intent.putExtra("outputY", 96);
        intent.putExtra("return-data", true);
        return intent;
    }

    /**
     * Forward request to the native Contacts application
     */
    private void forwardEditRequest() {

        Intent intent = getIntent();

        // Reset component info
        intent.setComponent(null);

        PackageManager pm = getApplicationContext().getPackageManager();

        List<ResolveInfo> intentList = pm.queryIntentActivities(
                      intent, PackageManager.MATCH_DEFAULT_ONLY |
                      PackageManager.GET_RESOLVED_FILTER );

        String pName = null;

        for (int i=0; i<intentList.size(); i++) {

            ResolveInfo r = intentList.get(i);
            String rpName = r.activityInfo.packageName;

            // Continue if the package name is the same as the funambol app
            if(rpName == null || rpName.equals(BuildInfo.PACKAGE_NAME)) {
                continue;
            }
            // Check if the default contacts app is installed
            else if(rpName.equals("com.android.contacts")) {
                pName = rpName;
                break;
            }
            // Check if an android contacts app is installed
            else if(rpName.startsWith("com.android") || pName == null) {
                pName = rpName;
            }
        }

        if(pName != null) {
            intent.setPackage(pName);
            startActivity(intent);
        } else {
            Log.e(TAG_LOG, "Cannot find resolver for Intent: " + intent);
            // Remove funambol app from preferred activities
            pm.clearPackagePreferredActivities(BuildInfo.PACKAGE_NAME);
        }
        finish();
    }

    /**
     * Edit an esxisting contact
     */
    private class EditContactTask extends AsyncTask<Void, Void, Void> {

        private boolean showDialog = false;

        @Override
        protected void onPreExecute() {

            // Read the current contact's state from the store
            getContactValuesFromStore();

            // Handle any incoming values that should be inserted
            final Bundle extras = getIntent().getExtras();
            final boolean hasExtras = extras != null && extras.size() > 0;
            if (hasExtras && hasValidStates()) {
                ContactValues.parseExtras(extras, currentStates[0]);
            }

            if(hasValidStates()) {
                // This contact has some Funambol info.
                // If it is aggregated with other accounts, then we show a
                // dialog to choose the portion to edit, otherwise we forward
                // the request
                if (isAggregated()) {
                    showDialog = true;
                }
            } else {
                forwardEditRequest();
                this.cancel(true);
            }
            super.onPreExecute();
        }
        
        protected Void doInBackground(Void... params) {
            // Just a workaround to be sure that the activity layout is displayed
            // before onPostExecute
            try { Thread.sleep(50); } catch(Exception ex) { }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(showDialog) {
                editing = true;
                AndroidEditContact.this.showDialog(DIALOG_SELECT_ACCOUNT);
            } else {
                bindUI(false);
            }
        }
    }

    /**
     * Build dialog that handles adding a new contact
     */
    private class AddContactTask extends
            AsyncTask<Void, Void, Void> {

        private boolean showDialog = false;
        
        @Override
        protected void onPreExecute() {

            boolean accountFound = false;
            AccountManager am = AccountManager.get(getApplicationContext());
            Account[] accounts = am.getAccounts();
            for(int i=0; i<accounts.length; i++) {
                if(getString(R.string.account_type).equals(accounts[i].type)) {
                    accountFound = true;
                }
            }

            if(!accountFound) {
                forwardEditRequest(); // Quit the activity
                this.cancel(true);
            }

            initEmptyContactState();

            // Handle any incoming values that should be inserted
            final Bundle extras = getIntent().getExtras();
            final boolean hasExtras = extras != null && extras.size() > 0;
            if (hasExtras && hasValidStates()) {
                ContactValues.parseExtras(extras, currentStates[0]);
            }

            // We show the account in which the contact must be added only on OS
            // 2.0 and 2.1. On other versions we directly open the Funambol edit
            if (Build.VERSION.SDK_INT <= 7) {
                showDialog = accountFound && accounts.length > 1;
            } else {
                showDialog = false;
            }
            
            // Check if the Funambol addressbook must be chosen as default.
            // In that case we don't show the select account dialog
            AppSyncSource source = sManager.getSource(AppSyncSourceManager.CONTACTS_ID);
            boolean isDefaultAddressBook = ((ContactAppSyncSourceConfig)
                                            source.getConfig()).getMakeDefaultAddressBook();
            if(isDefaultAddressBook) {
                showDialog = false;
            }
            super.onPreExecute();
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            // Just a workaround to be sure that the activity layout is displayed
            // before onPostExecute
            try { Thread.sleep(50); } catch(Exception ex) { }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(showDialog) {
                editing = false;
                AndroidEditContact.this.showDialog(DIALOG_SELECT_ACCOUNT);
            } else {
                bindUI(false);
            }
        }
    }

    private Dialog createSelectAccountDialog() {

        // Wrap our context to inflate list items using correct theme
        final Context dialogContext = new ContextThemeWrapper(AndroidEditContact.this,
                android.R.style.Theme_Light);

        final LayoutInflater dialogInflater = (LayoutInflater)dialogContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final String[] choices = new String[2];
        choices[0] = getString(R.string.account_label);
        choices[1] = getString(R.string.label_other);

        final ArrayAdapter<String> accountAdapter = new ArrayAdapter<String>(
                AndroidEditContact.this, android.R.layout.simple_list_item_2,
                choices) {
            
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = dialogInflater.inflate(android.R.layout.simple_list_item_1,
                            parent, false);
                }

                TextView choiceView = (TextView)convertView.findViewById(android.R.id.text1);
                choiceView.setText(this.getItem(position));

                return convertView;
            }
        };

        final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // Create new contact based on selected source
                final String choice = accountAdapter.getItem(which);

                if(!choice.equals(choices[0])) {
                    forwardEditRequest(); // Quit the activity
                } else {
                    bindUI(false);
                }
            }
        };

        final DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(AndroidEditContact.this);
        // This dialog can be used for both creating or editing a contact. The
        // title must be set accordingly
        if (editing) {
            builder.setTitle(R.string.dialog_edit_contact_account);
        } else {
            builder.setTitle(R.string.dialog_new_contact_account);
        }
        builder.setSingleChoiceItems(accountAdapter, 0, clickListener);
        builder.setOnCancelListener(cancelListener);

        return builder.create();
    }

    /**
     * Saves the current contacts to the store
     */
    private class SaveContactTask extends AsyncTask<Void, Void, Integer> {

        private WeakReference<ProgressDialog> progress;
        private boolean dismissDialog = true;
        
        @Override
        protected void onPreExecute() {

            this.progress = new WeakReference<ProgressDialog>(
                    ProgressDialog.show(AndroidEditContact.this, null,
                    getString(R.string.message_saving_contact)));

            Context c = getApplicationContext();
            c.startService(new Intent(c, EmptyService.class));

            super.onPreExecute();
        }

        protected Integer doInBackground(Void... v) {

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            ContentProviderOperation.Builder builder;

            boolean isNewContact = true;
            ContactValues[] uiState = getContactValuesFromUI();
            for(int i=0; i<uiState.length; i++) {

                ContactValues currentState = uiState[i];
                ContactValues originalState = (ContactValues)originalStates[i].clone();

                // Add a new row in the RawContacts table if this is a new contact
                if(currentState.isNewContact()) {
                    ops.add(ContentProviderOperation.newInsert(
                            ContactsContract.RawContacts.CONTENT_URI)
                                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, accountType)
                                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
                                .build());
                } else {
                    isNewContact = false;
                }

                // Build diff
                Set<String> keys = currentState.keySet();
                for(String key : keys) {

                    // Check for updated fields
                    if(originalState.containsKey(key)) {

                        ContentValues newValues = currentState.get(key);
                        ContentValues oldValues = originalState.get(key);

                        Uri uri = null;
                        if(oldValues != null && oldValues.containsKey(
                                ContactsContract.Data._ID)) {

                            // This is an updated field
                            uri = ContentUris.withAppendedId(
                                    ContactsContract.Data.CONTENT_URI,
                                oldValues.getAsLong(ContactsContract.Data._ID));
                            builder = ContentProviderOperation.newUpdate(uri);
                        } else {

                            // This is a new field (added through the quick edit features)
                            uri = ContactsContract.Data.CONTENT_URI;
                            builder = ContentProviderOperation.newInsert(uri);
                        }

                        if(!prepareOperationBuilder(builder, newValues)) {

                            // Delete the fields since the fields are actually
                            // in the UI but they are empty
                            builder = ContentProviderOperation.newDelete(uri);
                        } else {
                            if(currentState.isNewContact()) {
                                builder = builder.withValueBackReference(
                                        ContactsContract.Data.RAW_CONTACT_ID, 0);
                            } else {
                                builder = builder.withValue(
                                        ContactsContract.Data.RAW_CONTACT_ID,
                                    originalState.rawContactId);
                            }
                        }
                        ops.add(builder.build());
                        originalState.remove(key);
                    }

                    // Check for new fields
                    else {
                        ContentValues newValues = currentState.get(key);
                        
                        builder = ContentProviderOperation.newInsert(
                                ContactsContract.Data.CONTENT_URI);
                        if(currentState.isNewContact()) {
                            builder = builder.withValueBackReference(
                                    ContactsContract.Data.RAW_CONTACT_ID, 0);
                        } else {
                            builder = builder.withValue(
                                    ContactsContract.Data.RAW_CONTACT_ID,
                                    originalState.rawContactId);
                        }

                        if(prepareOperationBuilder(builder, newValues)) {
                            ops.add(builder.build());
                        }
                    }
                }

                // Check for deleted fields
                keys = originalState.keySet();
                for(String key : keys) {

                    ContentValues values = originalState.get(key);

                    // The data id may not exist, in this case we have nothing
                    // to delete
                    if(values.get(ContactsContract.Data._ID) != null) {
                        Uri uri = ContentUris.withAppendedId(
                                ContactsContract.Data.CONTENT_URI,
                                values.getAsLong(ContactsContract.Data._ID));

                        builder = ContentProviderOperation.newDelete(uri);
                        ops.add(builder.build());
                    }
                }
            }

            // Do nothing if all the operations are relative all empty contacts
            if(isNewContact && (uiState.length == ops.size())) {
                return 0;
            }

            try {
                // Apply modifications
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch(Exception ex) {
                ex.printStackTrace();
                Log.e(TAG_LOG, "Exception in SaveContactTask: " + ex);
            }
            
            return ops.size();
        }

        /**
         * Prepare a ContentProviderOperation Builder with all the values
         * contained in the given ContentValues.
         * 
         * @param builder
         * @param values
         * @return true if at least one not-null value has been set
         */
        private boolean prepareOperationBuilder(
                ContentProviderOperation.Builder builder, ContentValues values) {

            builder = builder.withValue(ContactsContract.Data.MIMETYPE,
                                values.get(ContactsContract.Data.MIMETYPE));
            Object value = null;
            int count = 0;
            
            for(int i=1; i<=15; i++) {
                value = values.get("data"+i);
                if(value instanceof String) {
                    if(StringUtil.isNullOrEmpty((String)value)) {
                        value = null;
                    } else {
                        count++;
                    }
                }
                // Check if the photo field is set
                else if(i == 15) {
                    if(value != null) {
                        count++;
                    }
                }
                builder = builder.withValue("data"+i, value);
            }
            if(values.get(ContactsContract.Data.IS_SUPER_PRIMARY) != null) {
                builder = builder.withValue(ContactsContract.Data.IS_SUPER_PRIMARY,
                    values.get(ContactsContract.Data.IS_SUPER_PRIMARY));
            }
            
            return count > 0;
        }

        @Override
        protected void onPostExecute(Integer count) {
            
            if(count > 0) {
                Toast.makeText(AndroidEditContact.this, getString(
                    R.string.message_contact_saved), Toast.LENGTH_SHORT).show();
            }

            if(dismissDialog) {
                progress.get().dismiss();
            }

            // Stop the service that was protecting us
            Context c = getApplicationContext();
            c.stopService(new Intent(c, EmptyService.class));

            // Make sure the dialog is dismissed before closing the activity
            while(dismissDialog && progress.get().isShowing()) {
                try { Thread.sleep(50); } catch(Exception ex) { }
            }
            
            // Finally cose the activity
            finish();
        }

        public void activityDestroyed() {
            dismissDialog = false;
        }
    }

    /**
     * Delete the current contacts from the store
     */
    private class DeleteContactTask extends AsyncTask<Void, Void, Void> 
            implements DialogInterface.OnClickListener {

        public void onClick(DialogInterface dialog, int which) {
            new DeleteContactTask().execute();
        }
        
        protected Void doInBackground(Void... v) {
            for(int i=0; i<currentStates.length; i++) {
                getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI,
                ContactsContract.RawContacts._ID+"="+currentStates[i].rawContactId, null);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Toast.makeText(AndroidEditContact.this, getString(
                    R.string.message_contact_deleted), Toast.LENGTH_SHORT).show();

            // Finally close the activity
            finish();
        }
    }

    /**
     * Class that listens to requests coming from photo editors
     */
    private class PhotoListener implements ContactDataStructure.EditorListener,
            DialogInterface.OnClickListener {
        
        private long rawContactId;
        private EditContactPhotoView photo;

        public PhotoListener(long rawContactId, EditContactPhotoView photo) {
            this.rawContactId = rawContactId;
            this.photo = photo;
        }

        public void onDeleted() {
            // Do nothing
        }

        public void onRequest(int request) {
            if (!hasValidStates()) return;

            if (request == ContactDataStructure.EditorListener.REQUEST_PICK_PHOTO) {
                if (photo.hasSetPhoto()) {
                    createPhotoDialog().show();
                } else {
                    doPickPhotoAction(rawContactId);
                }
            }
        }

       public Dialog createPhotoDialog() {
            
            Context context = AndroidEditContact.this;

            // Wrap our context to inflate list items using correct theme
            final Context dialogContext = new ContextThemeWrapper(context,
                    android.R.style.Theme_Light);

            String[] choices = new String[3];
            choices[0] = getString(R.string.use_photo_as_primary);
            choices[1] = getString(R.string.removePicture);
            choices[2] = getString(R.string.changePicture);
            
            final ListAdapter adapter = new ArrayAdapter<String>(dialogContext,
                    android.R.layout.simple_list_item_1, choices);

            final AlertDialog.Builder builder = new AlertDialog.Builder(dialogContext);
            builder.setTitle(R.string.attachToContact);
            builder.setSingleChoiceItems(adapter, -1, this);
            return builder.create();
        }

        /**
         * Called when something in the dialog is clicked
         */
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();

            switch (which) {
                case 0:
                    // Set the photo as super primary
                    photo.setSuperPrimary(true);

                    // And set all other photos as not super primary
                    int count = contacts_view.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View childView = contacts_view.getChildAt(i);
                        if (childView instanceof EditContactView) {
                            EditContactView editor = (EditContactView) childView;
                            EditContactPhotoView photoEditor = editor.getPhoto();
                            if (!photoEditor.equals(photo)) {
                                photoEditor.setSuperPrimary(false);
                            }
                        }
                    }
                    break;

                case 1:
                    // Remove the photo
                    photo.setPhotoBitmap(null);
                    break;

                case 2:
                    // Pick a new photo for the contact
                    doPickPhotoAction(rawContactId);
                    break;
            }
        }
    }
}
