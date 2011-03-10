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

package com.funambol.android.source.media;

import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager;
import android.content.ActivityNotFoundException;

import com.funambol.client.source.ExternalAppManager;
import com.funambol.client.source.AppSyncSource;

import java.util.List;

public class MediaExternalAppManager implements ExternalAppManager {

    private AppSyncSource source;
    private Context       context;

    public MediaExternalAppManager(Context context, AppSyncSource source) {
        this.context = context;
        this.source = source;
    }

    public void launch(AppSyncSource source, Object args[]) throws Exception {
        try {
            
            Intent intent = new Intent(Intent.ACTION_VIEW, Media.EXTERNAL_CONTENT_URI);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            
        } catch(ActivityNotFoundException ex) {
            
            // Can't find default gallery application -> search for another one
            PackageManager pm = context.getPackageManager();
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            
            String pName = null;
            int priority = -1;
            List<ResolveInfo> intentList = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER );
            for (int i=0; i<intentList.size(); i++) {
                ResolveInfo r = intentList.get(i);
                if(r.preferredOrder > priority) {
                    pName = r.activityInfo.packageName;
                }
            }
            if(pName != null) {
                intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setPackage(pName);
                intentList = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
                
                if(intentList.size() > 0) {
                    ResolveInfo r = intentList.get(0);
                    String className = r.activityInfo.name;
                    intent = new Intent();
                    intent.setClassName(pName, className);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        }
    }
}
