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

package com.funambol.android.source.pim.contact;

import android.widget.LinearLayout;
import android.widget.CheckBox;
import android.app.Activity;

import com.funambol.android.R;
import com.funambol.android.activities.settings.AndroidSettingsUISyncSource;
import com.funambol.android.activities.settings.TwoLinesCheckBox;
import com.funambol.client.configuration.Configuration;
import com.funambol.util.Log;


public class ContactSettingsUISyncSource extends AndroidSettingsUISyncSource {

    private static final String TAG = "ContactSettingsUISyncSource";

    public static final int DEFAULT_ADDRESS_BOOK_SETTING = 1000;

    private TwoLinesCheckBox makeDefault;
    private boolean  makeDefaultInitialValue;

    public ContactSettingsUISyncSource(Activity activity) {

        super(activity);

        // Create the items specific to the contact sync source
        makeDefault = new TwoLinesCheckBox(activity);

        String label = loc.getLanguage("conf_default_address_book");

        String accountLabel = getContext().getString(R.string.account_label);
        label = label.replaceAll("__ACCOUNT_LABEL__", accountLabel);

        makeDefault.setText1(label);
        makeDefault.setPadding(0, makeDefault.getPaddingTop(), makeDefault.getPaddingRight(),
                makeDefault.getPaddingBottom());
    }

    @Override
    public void saveSettings(Configuration configuration) {
        Log.trace(TAG, "Saving custom settings for contact source");
        super.saveSettings(configuration);
        ContactAppSyncSourceConfig config = (ContactAppSyncSourceConfig)appSyncSource.getConfig();
        boolean makeDefaultValue = makeDefault.isChecked();
        config.setMakeDefaultAddressBook(makeDefaultValue);

        /* Uncomment if you want to make funambol the default address book by
           hiding the imported accounts
         
        if(makeDefaultValue) {
            ExternalAccountManager.getInstance(getContext()).hideImportedAccounts();
        } else {
            ExternalAccountManager.getInstance(getContext()).showHiddenAccounts();
        }

         */
    }

    @Override
    public void loadSettings(Configuration configuration) {
        Log.trace(TAG, "Loading custom settings for contact source");
        super.loadSettings(configuration);
        ContactAppSyncSourceConfig config = (ContactAppSyncSourceConfig)appSyncSource.getConfig();
        boolean checked = config.getMakeDefaultAddressBook();
        makeDefault.setChecked(checked);
        makeDefaultInitialValue = checked;
    }

    @Override
    public boolean hasChanges() {
        boolean changes = super.hasChanges();
        changes |= (makeDefaultInitialValue != makeDefault.isChecked());
        return changes;
    }

    @Override
    public void layout() {

        super.layout();

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                    LayoutParams.WRAP_CONTENT);
        mainLayout.addView(makeDefault, lp);
    }
}


