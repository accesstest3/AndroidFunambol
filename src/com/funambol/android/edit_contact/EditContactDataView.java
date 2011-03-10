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

import android.content.ContentValues;
import com.funambol.android.R;
import com.funambol.android.edit_contact.ContactDataStructure.ContactData;
import com.funambol.android.edit_contact.ContactDataStructure.ContactDataType;
import com.funambol.android.edit_contact.ContactDataStructure.EditorListener;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Represents a View for editing a specific Contact's data kind (e.g. phone
 * number, email address). It is represented by a title (the data kind name), a
 * fields list to collect all the data types belonging to this data kind, and an
 * add button used to add new data types to the fields list.
 */
public class EditContactDataView extends LinearLayout implements EditorListener, OnClickListener {
    
    private LayoutInflater inflater;

    private TextView  title;
    private View      addButton;
    private ViewGroup fields;

    private ContactData cData;
    private ContactValues cValues;

    private boolean isRefresh = false;

    public EditContactDataView(Context context) {
        super(context);
    }

    public EditContactDataView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        inflater = (LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        title = (TextView)findViewById(R.id.data_title);

        addButton = findViewById(R.id.data_header);
        addButton.setOnClickListener(this);

        fields = (ViewGroup)findViewById(R.id.data_fields);
    }

    public void setState(ContactData cData, ContactValues cValues, boolean refresh) {

        this.cData = cData;
        this.cValues = cValues;
        this.isRefresh = refresh;
        
        this.rebuild();
        this.updateAddEnabled();
        this.updateFieldsVisible();
    }

    private void rebuild() {
        title.setText(getContext().getString(cData.nameRes));

        // Remove any existing fields
        fields.removeAllViews();

        cData.availableTypes.clear();
        for(ContactDataType type : cData.types) {
            cData.availableTypes.add(type);
        }

        int minCount = cData.minCount;
        for (ContactDataType dataType : cData.types) {
            if(addDataType(dataType, false, false)) {
                minCount--;
            }
        }

        // Show min count data types if this is a new contact
        if(cValues == null || (cValues.rawContactId == -1)) {
            for (ContactDataType dataType : cData.types) {
                if(minCount<=0) {
                    break;
                }
                if(addDataType(dataType, true, false)) {
                    minCount--;
                }
            }
        }

        this.updateAddEnabled();
        this.updateFieldsVisible();
    }

    public void getContactValues(ContactValues values) {
        for(int i=0; i<fields.getChildCount(); i++) {
            EditContactFieldView contactFieldView = (EditContactFieldView)fields.getChildAt(i);
            contactFieldView.getContactValues(values);
        }
    }

    public void onDeleted() {
        this.updateAddEnabled();
        this.updateFieldsVisible();
    }

    public void onRequest(int request) {
        // Do nothing
    }

    private void updateFieldsVisible() {
        final boolean hasChildren = fields.getChildCount() > 0;
        fields.setVisibility(hasChildren ? View.VISIBLE : View.GONE);
    }

    private void updateAddEnabled() {
        if(cData.availableTypes.size() > 0) {
            addButton.setEnabled(true);
        } else {
            addButton.setEnabled(false);
        }
    }

    public void onClick(View v) {
        addDataType(getFirstAvailableDataType(), true, true);

        this.updateAddEnabled();
        this.updateFieldsVisible();
    }

    private boolean addDataType(ContactDataType type, boolean addEmpty,
            boolean requestFocus) {

        EditContactFieldView fieldView = (EditContactFieldView)inflater.inflate(
                    R.layout.edit_contact_field_view, fields, false);

        String key = cData.mimeType;
        if(type != null) {
            key += type.type;
        } else if(cData.defaultType != null) {
            key += cData.defaultType.type;
        } else {
            key += 0;
        }
        
        ContentValues values = cValues != null ? cValues.get(key) : null;
        if(values != null || addEmpty) {
            if(addEmpty) {
                values = null;
            }
            fieldView.setState(type, cData, values, isRefresh);
            fieldView.setEditorListener(this);
            
            cData.availableTypes.remove(type);
            
            fields.addView(fieldView);

            if(requestFocus) {
                fieldView.requestFocus();
            }
            
            return true;
        }
        else {
            return false;
        }
    }

    private ContactDataType getFirstAvailableDataType() {
        if(cData.availableTypes.size() > 0) {
            return cData.availableTypes.get(0);
        } else {
            return null;
        }
    }
}
