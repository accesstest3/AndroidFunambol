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

import com.funambol.android.R;
import com.funambol.android.edit_contact.ContactDataStructure.ContactData;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import com.funambol.android.controller.AndroidController;

/**
 * Represents a View for editing a specific RawContact.
 */
public class EditContactView extends LinearLayout implements OnClickListener {

    private Context context;
    
    private LayoutInflater inflater;

    private EditContactFieldView name;
    private EditContactPhotoView photo;

    private ViewGroup primaryFields;
    private ViewGroup secondaryFields;
    
    private TextView  secondaryFieldsHeader;
    private boolean   secondaryFieldsVisible;
    private Drawable  secondaryFieldsOpen;
    private Drawable  secondaryFieldsClosed;

    private ImageView accountIcon;
    private TextView  accountType;
    private TextView  accountName;

    private long rawContactId = -1;
    
    public EditContactView(Context context) {
        super(context);
        this.context = context;
    }

    public EditContactView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        inflater = (LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        name = (EditContactFieldView)findViewById(R.id.edit_name);
        name.setDeletable(false);
        
        photo = (EditContactPhotoView)findViewById(R.id.edit_photo);

        primaryFields = (ViewGroup)findViewById(R.id.primary_fields);
        secondaryFields = (ViewGroup)findViewById(R.id.secondary_fields);

        accountIcon = (ImageView) findViewById(R.id.header_account_icon);
        accountType = (TextView) findViewById(R.id.header_account_type);
        accountName = (TextView) findViewById(R.id.header_account_name);

        secondaryFieldsHeader = (TextView)findViewById(R.id.head_secondary);
        secondaryFieldsHeader.setOnClickListener(this);

        Resources res = getResources();
        secondaryFieldsOpen = res.getDrawable(R.drawable.expander_ic_maximized);
        secondaryFieldsClosed = res.getDrawable(R.drawable.expander_ic_minimized);

        this.setSecondaryVisible(false);
    }

    public void onClick(View v) {
        boolean makeVisible = secondaryFields.getVisibility() != View.VISIBLE;
        this.setSecondaryVisible(makeVisible);
        secondaryFieldsHeader.requestFocus();
    }

    public void setState(ContactDataStructure data, ContactValues values, boolean refresh) {

        if(values != null) {
            this.rawContactId = values.rawContactId;
        }
        
        // Remove any existing fields
        primaryFields.removeAllViews();
        secondaryFields.removeAllViews();

        // Fill in the header info
        String accountContact = context.getString(R.string.label_account_contact);
        String accountLabel = context.getString(R.string.account_label);
        accountContact = accountContact.replaceAll("__ACCOUNT_LABEL__", accountLabel);
        
        setAccountType(accountContact);
        setAccountName(context.getString(R.string.label_from) + " " + AndroidController.getNativeAccount(context).name);
        setAccountIcon(R.drawable.logo);

        // Create editor sections for each possible data kind
        for (ContactData cData : data.getContactStructure()) {

            String mimeType = cData.mimeType;

            if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                name.setState(null, cData, values != null ?
                    values.get(StructuredName.CONTENT_ITEM_TYPE+0): null, refresh);
            } else if (Photo.CONTENT_ITEM_TYPE.equals(mimeType)) {
                photo.setState(null, cData, values != null ?
                    values.get(Photo.CONTENT_ITEM_TYPE+0): null, refresh);
            } else {
                
                if (cData.fields.size() == 0) continue;

                ViewGroup parent = cData.secondary ? secondaryFields : primaryFields;
                EditContactDataView cDataView = (EditContactDataView)inflater.inflate(
                        R.layout.edit_contact_data_view, parent, false);

                cDataView.setState(cData, values, refresh);
                parent.addView(cDataView);
            }
        }

        if (secondaryFields.getChildCount() > 0) {
            secondaryFieldsHeader.setVisibility(View.VISIBLE);
            if (secondaryFieldsVisible) {
                secondaryFields.setVisibility(View.VISIBLE);
            } else {
                secondaryFields.setVisibility(View.GONE);
            }
        } else {
            secondaryFieldsHeader.setVisibility(View.GONE);
            secondaryFields.setVisibility(View.GONE);
        }
    }

    public void setPhotoBitmap(Bitmap bitmap) {
        photo.setPhotoBitmap(bitmap);
    }

    public EditContactPhotoView getPhoto() {
        return photo;
    }

    public long getRawContactId() {
        return rawContactId;
    }

    public ContactValues getContactValues() {
        ContactValues values = new ContactValues();

        // Add Name values
        name.getContactValues(values);

        // Add Photo value
        if(photo.hasSetPhoto()) {
            photo.getContactValues(values);
        }

        // Add primary fields values
        for(int i=0; i<primaryFields.getChildCount(); i++) {
            EditContactDataView contactDataView =
                    (EditContactDataView)primaryFields.getChildAt(i);
            contactDataView.getContactValues(values);
        }
        // Add secondary fields values
        for(int i=0; i<secondaryFields.getChildCount(); i++) {
            EditContactDataView contactDataView =
                    (EditContactDataView)secondaryFields.getChildAt(i);
            contactDataView.getContactValues(values);
        }
        return values;
    }

    /**
     * Set the visibility of secondary fields, along with header icon.
     */
    private void setSecondaryVisible(boolean makeVisible) {
        secondaryFields.setVisibility(makeVisible ? View.VISIBLE : View.GONE);
        secondaryFieldsHeader.setCompoundDrawablesWithIntrinsicBounds(makeVisible ? 
            secondaryFieldsOpen : secondaryFieldsClosed, null, null, null);
        secondaryFieldsVisible = makeVisible;
    }

    public void setAccountType(String type) {
        accountType.setText(type);
    }

    public void setAccountName(String name) {
        accountName.setText(name);
    }

    public void setAccountIcon(int resId) {
        accountIcon.setImageResource(resId);
    }
}
