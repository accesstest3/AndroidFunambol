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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import com.funambol.android.R;

import com.funambol.android.edit_contact.ContactDataStructure.ContactData;
import com.funambol.android.edit_contact.ContactDataStructure.ContactDataType;
import com.funambol.android.edit_contact.ContactDataStructure.ContactDataField;
import com.funambol.android.edit_contact.ContactDataStructure.ContactDataDateField;
import com.funambol.android.edit_contact.ContactDataStructure.EditorListener;

import android.content.Context;
import android.content.DialogInterface;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.funambol.util.StringUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Represents a view for editing a generic contact field
 */
public class EditContactFieldView extends RelativeLayout implements View.OnClickListener {

    private static final int RES_FIELD = R.layout.edit_contact_field_edit_view;
    private static final int RES_DATE_FIELD = R.layout.edit_contact_date_field_edit_view;
    private static final int RES_LABEL_ITEM = android.R.layout.simple_list_item_1;
    
    private LayoutInflater inflater;

    private ContactData cData;
    private ContentValues cValues;
    
    private ContactDataType dataType;
    private List<ContactDataType> dialogDataTypes;

    private TextView label;
    private ViewGroup fields;
    private View delete;
    private View more;

    private boolean isRefresh = false;

    private boolean hideOptional = true;

    private EditorListener listener = null;
    
    public EditContactFieldView(Context context) {
        super(context);
    }

    public EditContactFieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        inflater = (LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        label = (TextView)findViewById(R.id.edit_label);
        label.setOnClickListener(this);

        fields = (ViewGroup)findViewById(R.id.edit_fields);

        delete = findViewById(R.id.edit_delete);
        delete.setOnClickListener(this);

        more = findViewById(R.id.edit_more);
        more.setOnClickListener(this);
    }

    public void setState(ContactDataType dataType, ContactData cData,
            ContentValues cValues, boolean refresh) {

        this.dataType = dataType;
        this.cData = cData;
        this.cValues = cValues;
        this.isRefresh = refresh;

        this.rebuild();        
    }


    private void rebuild() {

        rebuildLabel();

        label.setVisibility(hasTypes() ? View.VISIBLE : View.GONE);
        label.setEnabled(true);

        // Build out set of fields
        fields.removeAllViews();
        boolean hidePossible = false;
        for (ContactDataField field : cData.fields) {

            View fieldView;
            boolean setValue = false;

            String value = null;
            // Set the value
            if(cValues != null) {
                value = cValues.getAsString(field.column);
                if(value != null) {
                    if(!StringUtil.isNullOrEmpty(value) || isRefresh) {
                        setValue = true;
                    }
                }
            }
            if(field instanceof ContactDataDateField) {
                fieldView = (EditContactDateFieldView)inflater.inflate(
                        RES_DATE_FIELD, fields, false);
                if(setValue) {
                    ((EditContactDateFieldView)fieldView).setValue(value); 
                }
            } else {
                fieldView = (TextView)inflater.inflate(RES_FIELD, fields, false);
                if (field.nameRes > 0) {
                    ((TextView)fieldView).setHint(getContext().getString(field.nameRes));
                }
                int inputType = field.inputType;
                ((TextView)fieldView).setInputType(inputType);
                if (inputType == ContactDataStructure.INPUT_TYPE_PHONE) {
                    ((TextView)fieldView).addTextChangedListener(new PhoneNumberFormattingTextWatcher());
                }
                if(setValue) {
                    ((TextView)fieldView).setText(value);
                }
            }
            fieldView.setTag(field.column);

            boolean couldHide = (field.optional && !setValue);
            boolean willHide = (hideOptional && couldHide);
            fieldView.setVisibility(willHide ? View.GONE : View.VISIBLE);

            hidePossible = hidePossible || couldHide;

            fields.addView(fieldView);
        }

        // When hiding fields, place expandable
        more.setVisibility(hidePossible ? View.VISIBLE : View.GONE);
    }

    public void getContactValues(ContactValues values) {

        updateContactValues();

        String key = cData.mimeType+getFieldType();

        values.put(key, cValues);
    }

    private void updateContactValues() {

        int type = getFieldType();
        
        ContentValues value = new ContentValues();
        value.put(ContactsContract.Data.MIMETYPE, cData.mimeType);

        // Set the data type only if it exists (this is not the case of the
        // StructuredName)
        if(type != 0 || isCustom()) {
            value.put(ContactsContract.Data.DATA2, type);
        }
        for(int i=0; i<fields.getChildCount(); i++) {
            View editField = fields.getChildAt(i);
            if(editField.getVisibility() == View.VISIBLE) {
                String fieldValue = null;
                if(editField instanceof EditContactDateFieldView) {
                    fieldValue = ((EditContactDateFieldView)editField).getValue();
                } else {
                    fieldValue = ((EditText)editField).getText().toString();
                }
                if(!StringUtil.isNullOrEmpty(fieldValue)) {
                    value.put((String)editField.getTag(), fieldValue);
                }
            }
        }

        if(isCustom()) {
            // Set the custom label
            value.put(ContactsContract.Data.DATA3, getLabel());
        }

        this.cValues = value;
    }

    private int getFieldType() {
        int type = 0;
        if(dataType != null) {
            type = dataType.type;
        } else if(cData.defaultType != null) {
            type = cData.defaultType.type;
        }
        return type;
    }

    private String getLabel() {
        int labelRes = -1;
        if(dataType != null) {
            labelRes = dataType.labelRes;
        } else if(cData.defaultType != null) {
            labelRes = cData.defaultType.labelRes;
        }
        if(labelRes != -1) {
            return getContext().getString(labelRes);
        } else {
            return null;
        }
    }

    private boolean isCustom() {
        if(dataType != null) {
            return dataType.isCustom;
        } else if(cData.defaultType != null) {
            return cData.defaultType.isCustom;
        }
        return false;
    }

    private boolean hasTypes() {
        boolean result = false;
        for(ContactDataType type : cData.types) {
            if(type != null) result = true;
        }
        return result;
    }
    
    public void setDeletable(boolean deletable) {
        delete.setVisibility(deletable ? View.VISIBLE : View.INVISIBLE);
    }

    public void setEditorListener(EditorListener listener) {
        this.listener = listener;
    }

    private void rebuildLabel() {
        if(dataType != null) {
            label.setText(dataType.labelRes);
        } else {
            label.setText(getContext().getString(R.string.label_unknown));
        }
    }

    /**
     * Prepare dialog for picking a new data type
     */
    public Dialog createLabelDialog() {

        // Wrap our context to inflate list items using correct theme
        final Context dialogContext = new ContextThemeWrapper(getContext(),
                android.R.style.Theme_Light);
        
        final LayoutInflater dialogInflater = inflater.cloneInContext(dialogContext);

        if(dialogDataTypes != null) {
            dialogDataTypes.clear();
        }

        dialogDataTypes = new ArrayList<ContactDataType>();

        // First add the current data type
        dialogDataTypes.add(dataType);
        for(ContactDataType type : cData.availableTypes) {
            dialogDataTypes.add(type);
        }

        final ListAdapter typeAdapter = new ArrayAdapter<ContactDataType>(getContext(),
                RES_LABEL_ITEM, dialogDataTypes) {
            
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = dialogInflater.inflate(RES_LABEL_ITEM, parent, false);
                }

                ContactDataType type = this.getItem(position);
                TextView textView = (TextView)convertView;
                textView.setText(type.labelRes);
                return textView;
            }
        };

        final DialogInterface.OnClickListener clickListener =
                new DialogInterface.OnClickListener() {
            
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ContactDataType selected = dialogDataTypes.get(which);
                if(!selected.equals(dataType)) {
                    cData.availableTypes.remove(selected);
                    cData.availableTypes.add(dataType);
                    dataType = selected;
                    rebuildLabel();
                }
            }
        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(getContext().getString(R.string.label_select));
        
        builder.setSingleChoiceItems(typeAdapter, 0, clickListener);
        return builder.create();
    }

    public ContactDataType getContactDataType() {
        return dataType;
    }

    public void onClick(View v) {

        final ViewGroup parent = (ViewGroup)getParent();
        
        switch (v.getId()) {
            case R.id.edit_label: {
                createLabelDialog().show();
                break;
            }
            case R.id.edit_delete: {

                // Remove view from parent
                parent.removeView(this);

                cData.availableTypes.add(dataType);

                if(listener != null) {
                    listener.onDeleted();
                }
                parent.requestFocus();
                break;
            }
            case R.id.edit_more: {
                hideOptional = !hideOptional;
                
                updateContactValues();
                rebuild();
               
                this.requestFocus();
                break;
            }
        }
    }
}
