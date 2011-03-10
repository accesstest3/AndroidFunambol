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

package com.funambol.android.source.pim.calendar;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;

import com.funambol.client.source.ExternalAppManager;
import com.funambol.client.source.AppSyncSource;
import com.funambol.util.Log;

public class CalendarExternalAppManager implements ExternalAppManager {

    private static final String TAG_LOG  = "CalendarExternalAppManager";
    
    private Context context;

    private final String[] PACKAGE_NAMES = {"com.android.calendar",
                                            "com.htc.calendar",
                                            "com.google.android.calendar"};

    public CalendarExternalAppManager(Context context, AppSyncSource source) {
        this.context = context;
    }

    public void launch(AppSyncSource source, Object args[]) throws Exception {
        PackageManager pm = context.getPackageManager();
        for(int i=0; i<PACKAGE_NAMES.length; i++) {
            Intent intent = pm.getLaunchIntentForPackage(PACKAGE_NAMES[i]);
            if(intent != null) {
                try {
                    context.startActivity(intent);
                    return;
                } catch(ActivityNotFoundException ex) {
                    Log.debug(TAG_LOG, "Activity not found for Intent: " + intent);
                }
            }
        }
        Log.error(TAG_LOG, "Cannot launch Calendar app");
    }
}
