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

package com.funambol.android.activities.settings;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import com.funambol.android.AndroidAccountManager;
import com.funambol.android.AndroidAppSyncSource;

import com.funambol.client.ui.SettingsUIItem;
import com.funambol.client.configuration.Configuration;

import com.funambol.android.R;
import com.funambol.android.controller.AndroidController;
import java.util.Enumeration;

/**
 * Implements a LinearLayout to display the C2S push setting
 */
public class C2SPushSettingView extends TwoLinesCheckBox implements SettingsUIItem {

    private boolean originalValue;

    public C2SPushSettingView(Context context) {
        super(context);
        setText1(getContext().getString(R.string.conf_c2s_push));
    }

    public boolean hasChanges() {
        return originalValue != isChecked() && isEnabled();
    }

    public void loadSettings(Configuration configuration) {
    }
    
    public void saveSettings(Configuration conf) {
        
        boolean checked = isChecked();
        Account account = AndroidAccountManager.getNativeAccount(getContext());

        // Save the auto sync setting for each source
        Enumeration sources = AndroidController.getInstance()
                .getAppSyncSourceManager().getWorkingSources();
        while(sources.hasMoreElements()) {
            AndroidAppSyncSource source = (AndroidAppSyncSource)sources.nextElement();
            String authority = source.getAuthority();
            if(authority != null) {
                ContentResolver.setSyncAutomatically(account, authority, checked);
            }
        }
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        originalValue = checked;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(!enabled) {
            super.setChecked(false);
            setText2(getContext().getString(R.string.conf_c2s_push_help));
        } else {
            setText2(null);
        }
    }
}

