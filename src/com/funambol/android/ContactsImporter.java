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

import java.util.Vector;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.funambol.android.activities.AndroidDisplayManager;
import com.funambol.android.controller.AndroidController;
import com.funambol.android.controller.AndroidAdvancedSettingsScreenController;
import com.funambol.android.source.pim.contact.ContactAppSyncSourceConfig;
import com.funambol.client.controller.Controller;
import com.funambol.client.localization.Localization;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.ui.Screen;
import com.funambol.util.Log;
import com.funambol.util.StringUtil;

/**
 * This class is responsible of the contacts import functionality. The entry
 * point is the importContacts method which will popup a multiple selection
 * dialog in order to let the user to select the accounts to import. Once
 * selected and pressed the "Start Import" button the import contacts will run
 * through an ASyncTask which makes use of the ExternalAccountManager to import
 * contacts belonging to the selected accounts.
 */
public class ContactsImporter {

    private static final String TAG_LOG = "ContactsImporter";

    private AndroidDisplayManager displayManager;
    private Localization localization;

    private Context context = null;
    private AndroidController controller = null;
    private int screenId;
    private AndroidAdvancedSettingsScreenController advSettController;
    
    public ContactsImporter(int screenId, Context context,
                            AndroidAdvancedSettingsScreenController advSettController)
    {

        this.screenId = screenId;
        this.controller = AndroidController.getInstance();

        this.context = context;
        this.localization = controller.getLocalization();
        this.displayManager = (AndroidDisplayManager)controller.getDisplayManager();
        this.advSettController = advSettController;
    }

    /**
     * The entry point for the import contacts process
     *
     * @param reset Resets the import configuration
     */
    public void importContacts(boolean reset) {

        // Check screen validity
        if(getScreen() == null) {
            Log.debug(TAG_LOG, "Screen not yet initialized: " + screenId);
            return;
        }

        // Init the external account manager used to handle contacts belonging
        // to other accounts
        final ExternalAccountManager aManager = ExternalAccountManager.getInstance(context);
        if(reset) {
            aManager.reset();
        }

        // This is the first call of contacts import if there aren't any accounts
        // imported and the caller requested a reset
        final boolean isFirstImport = !aManager.accountsImported() && reset;

        // Prompt contacts import dialog
        ((Activity)getScreen().getUiScreen()).runOnUiThread(new Runnable() {

            public void run() {

                String title = null;
                if(isFirstImport) {
                    title = localization.getLanguage("dialog_import_contacts_first");
                } else {
                    title = localization.getLanguage("dialog_import_contacts_later");
                }
                String okButtonLabel = localization.getLanguage("dialog_start_import");
                String cancelButtonLabel = localization.getLanguage("dialog_cancel_import");
                
                Vector<Account> accounts = aManager.listContactAccounts(true, true);

                // Filter accounts which don't include any item
                for(int i=0; i<accounts.size(); i++) {
                    Account account = accounts.elementAt(i);
                    if(aManager.getAccountItemsCount(account) <= 0) {
                        accounts.remove(account);
                        i--;
                    }
                }

                int choicesCount = accounts.size();

                // Include Phone only contacts
                boolean showPhoneOnlyOption = aManager.hasPhoneOnlyItems() &&
                        !aManager.phoneOnlyImported();
                int phoneOnlyOptionIndex = -1;
                if(showPhoneOnlyOption) {
                    phoneOnlyOptionIndex = accounts.size();
                    choicesCount++;
                }
                // Include SIM contacts
                boolean showSimOption = aManager.hasSimItems() &&
                        !aManager.simImported();
                int simOptionIndex = -1;
                if(showSimOption) {
                    choicesCount++;
                    if(!showPhoneOnlyOption) {
                        simOptionIndex = accounts.size();
                    } else {
                        simOptionIndex = accounts.size()+1;
                    }
                }

                // Return in the case there are no address books to import
                if(choicesCount == 0) {
                    Log.debug(TAG_LOG, "No address books to import"); 
                    if(!isFirstImport) {
                        displayManager.showOkDialog(getScreen(),
                                localization.getLanguage("dialog_import_no_address_books"),
                                localization.getLanguage("dialog_ok"));
                    }
                    return;
                }

                // Populate the import choices with the address books label
                String[] choices = new String[choicesCount];
                boolean[] checkedChoices = new boolean[choicesCount];
                for(int i=0; i<accounts.size(); i++) {
                    Account account = accounts.elementAt(i);
                    String label = getAccountLabel(account.type);
                    choices[i] = account.name;
                    if(!StringUtil.isNullOrEmpty(label)) {
                        choices[i] += " (" + label + ")";
                    }
                    checkedChoices[i] = true;
                }

                // Show Phone only contacts option if needed
                if(showPhoneOnlyOption) {
                    choices[phoneOnlyOptionIndex] = localization
                            .getLanguage("dialog_import_phone_contacts");
                    checkedChoices[phoneOnlyOptionIndex] = true;
                }
                // Show SIM contacts option if needed
                if(showSimOption) {
                    choices[simOptionIndex] = localization
                        .getLanguage("dialog_import_sim_contacts");
                    checkedChoices[simOptionIndex] = true;
                }

                MultipleChoicesClickListener listener =
                        new MultipleChoicesClickListener(checkedChoices,
                        accounts, phoneOnlyOptionIndex, simOptionIndex,
                        isFirstImport);

                displayManager.promptMultipleSelection(getScreen(), title,
                        okButtonLabel, cancelButtonLabel, choices, checkedChoices,
                        listener, listener, listener);
            }
        });
    }

    public static boolean contactsImported(Context c) {
        return ExternalAccountManager.getInstance(c).accountsImported();
    }

    /**
     * @return the screen related to the screenId attribute
     */
    private Screen getScreen() {
        if(screenId == Controller.HOME_SCREEN_ID) {
            return controller.getHomeScreenController().getHomeScreen();
        } else if(screenId == Controller.ADVANCED_SETTINGS_SCREEN_ID) {
            return controller.getAdvancedSettingsScreenController().getAdvancedSettingsScreen();
        } else {
            throw new IllegalStateException("Invalid screen id: " + screenId);
        }
    }

    private class ImportContactsTask extends AsyncTask<Void, Void, Integer> {

        private Vector<Account> accounts;
        private boolean includePhoneOnly;
        private boolean includeSim;

        private int dialogId = -1;

        private int total = 0;
        private int count = 0;
        private int syncLockId = SyncLock.FORBIDDEN;

        public ImportContactsTask(Vector<Account> accounts, boolean includePhoneOnly,
            boolean includeSim) {
            this.accounts = accounts;
            this.includePhoneOnly = includePhoneOnly;
            this.includeSim = includeSim;
        }

        /**
         * Shows the progress dialog
         */
        @Override
        protected void onPreExecute() {

            // Before starting the actual import, we must make sure the sync
            // lock is not acquired, because we cannot perform an import if a
            // sync is in progress
            AppInitializer appInitializer = AppInitializer.getInstance(context);
            SyncLock syncLock = appInitializer.getSyncLock();
            syncLockId = syncLock.acquireLock();
            if (syncLockId != SyncLock.FORBIDDEN) {
                String text = localization.getLanguage("dialog_importing_contacts");
                dialogId = displayManager.showProgressDialog(getScreen(), text, false);
            } else {
                Log.info(TAG_LOG, "Cannot acquire sync lock, the import cannot be performed");
                // Show a timed alert and skip the import completely
                String text = localization.getLanguage("dialog_import_sync_in_progress");
                displayManager.showMessage(getScreen(), text);
            }
        }

        /**
         * Imports the contacts and update the progress dialog
         * 
         * @param params
         * @return the number of imported contacts, -1 in case of errors
         */
        protected Integer doInBackground(Void... params) {
            int importedCount = 0;
            if (syncLockId != SyncLock.FORBIDDEN) {
                try {
                    ExternalAccountManager m = ExternalAccountManager.getInstance(context);
                    importedCount = m.importAccountItems(accounts, includePhoneOnly, includeSim,
                        new ExternalAccountManager.ItemsImportListener() {

                        public void setTotalItemsCount(int count) {
                            ImportContactsTask.this.total = count;
                            publishProgress();
                        }

                        public void updateImportedItemsCount(int count) {
                            ImportContactsTask.this.count = count;
                            publishProgress();
                        }
                        });
                } catch(Exception ex) {
                    Log.error(TAG_LOG, "Exception while importing contacts", ex);
                    return -1;
                }
            }
            return importedCount;
        }

        /**
         * Update the progress dialog values
         * 
         * @param values
         */
        @Override
        protected void onProgressUpdate(Void... values) {
            displayManager.setProgressDialogMaxValue(dialogId, total);
            displayManager.setProgressDialogProgressValue(dialogId, count);
        }

        /**
         * Dismiss the progress dialog and shows import results
         * @param result
         */
        @Override
        protected void onPostExecute(Integer result) {

            // First thing to do is to release the sync lock, so we are sure to
            // do it
            if (syncLockId != SyncLock.FORBIDDEN) {
                AppInitializer appInitializer = AppInitializer.getInstance(context);
                SyncLock syncLock = appInitializer.getSyncLock();
                syncLock.releaseLock(syncLockId);

                displayManager.dismissProgressDialog(getScreen(), dialogId);

                String text = null;
                if(result == 0) {
                    text = localization.getLanguage("dialog_import_no_items");
                } else if(result == -1) {
                    text = localization.getLanguage("dialog_import_error");
                }
                if(text != null) {
                    OkCallBack okCallBack = new OkCallBack();
                    displayManager.showOkDialog(getScreen(), text,
                            localization.getLanguage("dialog_ok"), okCallBack);
                }

                // Contacts have been imported. Now we have to make funambol the
                // default address book
                AppSyncSource appSyncSource = controller.getAppSyncSourceManager()
                    .getSource(AppSyncSourceManager.CONTACTS_ID);
                ContactAppSyncSourceConfig config = (ContactAppSyncSourceConfig)appSyncSource.getConfig();
                config.setMakeDefaultAddressBook(true);
                config.save();
            }
        }
    }
    
    /**
     * Manages interactions with the multiple selection dialog
     */
    private class MultipleChoicesClickListener implements
            DialogInterface.OnMultiChoiceClickListener,
            DialogInterface.OnClickListener {

        private boolean[] choices;
        private Vector<Account> accounts;
        private int phoneOnlyOptionIndex;
        private int simOptionIndex;
        private boolean isFirstImport;

        public MultipleChoicesClickListener(boolean[] choices,
                Vector<Account> accounts, int phoneOnlyOptionIndex, 
                int simOptionIndex, boolean isFirstImport) {
            this.choices = choices;
            this.accounts = accounts;
            this.phoneOnlyOptionIndex = phoneOnlyOptionIndex;
            this.simOptionIndex = simOptionIndex;
            this.isFirstImport = isFirstImport;
        }

        // Called when a choice is clicked
        public void onClick(DialogInterface dialog, int whichButton, boolean isChecked) {
            choices[whichButton] = isChecked;
        }

        // Called when a button is clicked
        public void onClick(DialogInterface dialog, int whichButton) {
            if(whichButton == AlertDialog.BUTTON_POSITIVE) {
                Vector<Account> accountsToImport = new Vector<Account>();
                for(int i=0; i<accounts.size(); i++) {
                    if(choices[i]) {
                        accountsToImport.add(accounts.elementAt(i));
                    }
                }
                boolean importPhoneContacts = phoneOnlyOptionIndex != -1 
                        && choices[phoneOnlyOptionIndex];
                boolean importSimItems = simOptionIndex != -1
                        && choices[simOptionIndex];

                // Begin the actual import
                new ImportContactsTask(accountsToImport, importPhoneContacts,
                        importSimItems).execute();
            } else if(whichButton == AlertDialog.BUTTON_NEGATIVE && isFirstImport) {
                displayManager.showOkDialog(getScreen(),
                       localization.getLanguage("dialog_import_later"),
                       localization.getLanguage("dialog_ok"));
            }
        }
    }

    private String getAccountLabel(String accountType) {
        AuthenticatorDescription[] desc = AccountManager.get(context)
                .getAuthenticatorTypes();
        PackageManager pm = context.getPackageManager();

        for(AuthenticatorDescription des : desc) {
            if(accountType.equals(des.type)) {
                try {
                    return pm.getText(des.packageName, des.labelId, null).toString();
                } catch(Exception ex) { }
            }
        }
        Log.error(TAG_LOG, "Account label not found for type: " + accountType);
        return null;
    }

    private class OkCallBack implements Runnable {

        public void run() {
            Log.trace(TAG_LOG, "Import terminated");
            // Signal to the controller that the import is complete
            // Note that the controller can be null if the import is not started
            // from the settings screen
            if (advSettController != null) {
                advSettController.importContactsCompleted();
            }
        }
    }

}
