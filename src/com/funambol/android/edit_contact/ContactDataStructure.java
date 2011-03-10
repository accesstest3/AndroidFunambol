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

import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.CommonDataKinds.Note;

import android.view.inputmethod.EditorInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a generic Contact data structure
 */
public class ContactDataStructure {

    public static final int INPUT_TYPE_PHONE = EditorInfo.TYPE_CLASS_PHONE;
    public static final int INPUT_TYPE_EMAIL = EditorInfo.TYPE_CLASS_TEXT
           | EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
    public static final int INPUT_TYPE_PERSON_NAME = EditorInfo.TYPE_CLASS_TEXT
           | EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS
           | EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME;
    public static final int INPUT_TYPE_GENERIC_NAME = EditorInfo.TYPE_CLASS_TEXT
           | EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS;
    public static final int INPUT_TYPE_NOTE = EditorInfo.TYPE_CLASS_TEXT
           | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES
           | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
    public static final int INPUT_TYPE_WEBSITE = EditorInfo.TYPE_CLASS_TEXT
           | EditorInfo.TYPE_TEXT_VARIATION_URI;
    public static final int INPUT_TYPE_EVENT = EditorInfo.TYPE_CLASS_DATETIME
           | EditorInfo.TYPE_DATETIME_VARIATION_DATE;
    public static final int INPUT_TYPE_POSTAL = EditorInfo.TYPE_CLASS_TEXT
           | EditorInfo.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
           | EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS
           | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;

    // The list of the contact's data
    private ArrayList<ContactData> contactData = null;

    private static ContactDataStructure instance = null;
    
    private ContactDataStructure() {

        initialize();
    }

    public static ContactDataStructure getInstance() {
        if(instance == null) {
            instance = new ContactDataStructure();
        }
        return instance;
    }

    public ArrayList<ContactData> getContactStructure() {
        return contactData;
    }

    private void initialize() {

        contactData = new ArrayList<ContactData>();
        
        // Primary fields
        addStructuredName(contactData);
        addPhoto         (contactData);
        addPhone         (contactData);
        addEmail         (contactData);
        addIM            (contactData);
        addPostalAddress (contactData);
        addOrganization  (contactData);

        // Secondary fields
        addNotes         (contactData);
        addNickname      (contactData);
        addWebsite       (contactData);
        addEvent         (contactData);

        // Uncomment to add support for relation fields
        //addRelation      (contactData);
    }
    
    private void addStructuredName(ArrayList<ContactData> cData) {

        ContactData data = new ContactData(R.string.label_data_name,
                StructuredName.CONTENT_ITEM_TYPE, false).setMinCount(1);

        data.addDataField(new ContactDataField(R.string.label_name_prefix,
                StructuredName.PREFIX, INPUT_TYPE_PERSON_NAME, true));
        data.addDataField(new ContactDataField(R.string.label_given_name,
                StructuredName.GIVEN_NAME, INPUT_TYPE_PERSON_NAME, false));
        data.addDataField(new ContactDataField(R.string.label_middle_name,
                StructuredName.MIDDLE_NAME, INPUT_TYPE_PERSON_NAME, true));
        data.addDataField(new ContactDataField(R.string.label_family_name,
                StructuredName.FAMILY_NAME, INPUT_TYPE_PERSON_NAME, false));
        data.addDataField(new ContactDataField(R.string.label_name_suffix,
                StructuredName.SUFFIX, INPUT_TYPE_PERSON_NAME, true));

        cData.add(data);
    }

    private void addPhoto(ArrayList<ContactData> cData) {

        ContactData data = new ContactData(-1, Photo.CONTENT_ITEM_TYPE, false)
                .setMinCount(1);

        data.addDataField(new ContactDataField(-1, Photo.PHOTO, -1, false));

        cData.add(data);
    }

    private void addPhone(ArrayList<ContactData> cData) {

        ContactData data = new ContactData(R.string.label_data_phone,
                Phone.CONTENT_ITEM_TYPE, false).setMinCount(1);

        data.addDataType(new ContactDataType(Phone.TYPE_HOME,
                  Phone.getTypeLabelResource(Phone.TYPE_HOME)));
        data.addDataType(new ContactDataType(Phone.TYPE_WORK,
                  Phone.getTypeLabelResource(Phone.TYPE_WORK)));
        data.addDataType(new ContactDataType(Phone.TYPE_MOBILE,
                  Phone.getTypeLabelResource(Phone.TYPE_MOBILE)));
        data.addDataType(new ContactDataType(Phone.TYPE_OTHER,
                  Phone.getTypeLabelResource(Phone.TYPE_OTHER)));
        data.addDataType(new ContactDataType(Phone.TYPE_FAX_HOME,
                  Phone.getTypeLabelResource(Phone.TYPE_FAX_HOME)));
        data.addDataType(new ContactDataType(Phone.TYPE_FAX_WORK,
                  Phone.getTypeLabelResource(Phone.TYPE_FAX_WORK)));
        data.addDataType(new ContactDataType(Phone.TYPE_PAGER,
                  Phone.getTypeLabelResource(Phone.TYPE_PAGER)));
        data.addDataType(new ContactDataType(Phone.TYPE_COMPANY_MAIN,
                  Phone.getTypeLabelResource(Phone.TYPE_COMPANY_MAIN)));
        data.addDataType(new ContactDataType(Phone.TYPE_OTHER_FAX,
                  Phone.getTypeLabelResource(Phone.TYPE_OTHER_FAX)));
        data.addDataType(new ContactDataType(Phone.TYPE_MAIN,
                  Phone.getTypeLabelResource(Phone.TYPE_MAIN)));
        data.addDataType(new ContactDataType(Phone.TYPE_CUSTOM,
                  R.string.label_work2_phone).setIsCustom());
        
        data.addDataField(new ContactDataField(R.string.label_data_phone,
                Phone.DATA, INPUT_TYPE_PHONE, false));

        cData.add(data);
    }

    private void addEmail(ArrayList<ContactData> cData) {

        ContactData data = new ContactData(R.string.label_data_email,
                Email.CONTENT_ITEM_TYPE, false).setMinCount(1);

        data.addDataType(new ContactDataType(Email.TYPE_HOME,
                  Email.getTypeLabelResource(Email.TYPE_HOME)));
        data.addDataType(new ContactDataType(Email.TYPE_WORK,
                  Email.getTypeLabelResource(Email.TYPE_WORK)));
        data.addDataType(new ContactDataType(Email.TYPE_OTHER,
                  Email.getTypeLabelResource(Email.TYPE_OTHER)));

        data.addDataField(new ContactDataField(R.string.label_data_email,
                Email.DATA, INPUT_TYPE_EMAIL, false));

        cData.add(data);
    }

    private void addIM(ArrayList<ContactData> cData) {

        ContactData data = new ContactData(R.string.label_data_im,
                Im.CONTENT_ITEM_TYPE, false);

        data.setDefaultType(new ContactDataType(Im.TYPE_HOME, -1));

        data.addDataType(null);

        data.addDataField(new ContactDataField(R.string.label_data_im,
                Im.DATA, INPUT_TYPE_EMAIL, false));

        cData.add(data);
    }

    private void addPostalAddress(ArrayList<ContactData> cData) {

        ContactData data = new ContactData(R.string.label_data_address,
                StructuredPostal.CONTENT_ITEM_TYPE, false);

        data.addDataType(new ContactDataType(StructuredPostal.TYPE_HOME,
                  StructuredPostal.getTypeLabelResource(
                  StructuredPostal.TYPE_HOME)));
        data.addDataType(new ContactDataType(StructuredPostal.TYPE_WORK,
                  StructuredPostal.getTypeLabelResource(
                  StructuredPostal.TYPE_WORK)));
        data.addDataType(new ContactDataType(StructuredPostal.TYPE_OTHER,
                  StructuredPostal.getTypeLabelResource(
                  StructuredPostal.TYPE_OTHER)));

        data.addDataField(new ContactDataField(R.string.label_street,
                StructuredPostal.STREET, INPUT_TYPE_POSTAL, false));
        data.addDataField(new ContactDataField(R.string.label_pobox,
                StructuredPostal.POBOX, INPUT_TYPE_POSTAL, true));
        data.addDataField(new ContactDataField(R.string.label_neighborhood,
                StructuredPostal.NEIGHBORHOOD, INPUT_TYPE_POSTAL, true));
        data.addDataField(new ContactDataField(R.string.label_city,
                StructuredPostal.CITY, INPUT_TYPE_POSTAL, false));
        data.addDataField(new ContactDataField(R.string.label_state,
                StructuredPostal.REGION, INPUT_TYPE_POSTAL, false));
        data.addDataField(new ContactDataField(R.string.label_zip_code,
                StructuredPostal.POSTCODE, INPUT_TYPE_POSTAL, false));
        data.addDataField(new ContactDataField(R.string.label_country,
                StructuredPostal.COUNTRY, INPUT_TYPE_POSTAL, true));

        cData.add(data);
    }

    private void addOrganization(ArrayList<ContactData> cData) {

        ContactData data = new ContactData(R.string.label_data_organization,
                Organization.CONTENT_ITEM_TYPE, false);

        data.setDefaultType(new ContactDataType(Organization.TYPE_WORK, -1));

        data.addDataType(null);

        data.addDataField(new ContactDataField(R.string.label_company,
                Organization.COMPANY, INPUT_TYPE_GENERIC_NAME, false));
        data.addDataField(new ContactDataField(R.string.label_position,
                Organization.TITLE, INPUT_TYPE_GENERIC_NAME, false));
        data.addDataField(new ContactDataField(R.string.label_department,
                Organization.DEPARTMENT, INPUT_TYPE_GENERIC_NAME, true));

        cData.add(data);
    }

    private void addEvent(ArrayList<ContactData> cData) {

        ContactData data = new ContactData(R.string.label_data_event,
                Event.CONTENT_ITEM_TYPE, true);

        data.addDataType(new ContactDataType(Event.TYPE_BIRTHDAY,
                  R.string.label_type_birthday));
        data.addDataType(new ContactDataType(Event.TYPE_ANNIVERSARY,
                  R.string.label_type_anniversary));

        data.addDataField(new ContactDataDateField(R.string.label_data_event,
                Event.START_DATE, false));

        cData.add(data);
    }

    private void addRelation(ArrayList<ContactData> cData) {

        ContactData data = new ContactData(R.string.label_data_relation,
                Relation.CONTENT_ITEM_TYPE, true);

        data.addDataType(new ContactDataType(Relation.TYPE_CHILD,
                  R.string.label_type_child));
        data.addDataType(new ContactDataType(Relation.TYPE_SPOUSE,
                  R.string.label_type_spouse));

        data.addDataField(new ContactDataField(R.string.label_data_relation,
                Relation.NAME, INPUT_TYPE_PERSON_NAME, false));

        cData.add(data);
    }

    private void addWebsite(ArrayList<ContactData> cData) {

        ContactData data = new ContactData(R.string.label_data_website,
                Website.CONTENT_ITEM_TYPE, true);

        data.addDataType(new ContactDataType(Website.TYPE_HOME,
                R.string.label_type_home));
        data.addDataType(new ContactDataType(Website.TYPE_WORK,
                R.string.label_type_work));
        data.addDataType(new ContactDataType(Website.TYPE_OTHER,
                R.string.label_other));

        data.addDataField(new ContactDataField(R.string.label_data_website, 
                Website.DATA, INPUT_TYPE_WEBSITE, false));

        cData.add(data);
    }

    private void addNickname(ArrayList<ContactData> cData) {

        ContactData data = new ContactData(R.string.label_data_nickname,
                Nickname.CONTENT_ITEM_TYPE, true);

        data.setDefaultType(new ContactDataType(Nickname.TYPE_DEFAULT, -1));

        data.addDataType(null);

        data.addDataField(new ContactDataField(R.string.label_data_nickname,
                Nickname.NAME, INPUT_TYPE_PERSON_NAME, false));

        cData.add(data);
    }

    private void addNotes(ArrayList<ContactData> cData) {

        ContactData data = new ContactData(R.string.label_data_note,
                Note.CONTENT_ITEM_TYPE, true);

        data.addDataType(null);
        
        data.addDataField(new ContactDataField(R.string.label_data_note,
                Note.NOTE, INPUT_TYPE_NOTE, false));

        cData.add(data);
    }

    /**
     * Checks if the given mimeType and data type is included in the current
     * ContactDataStructure.
     * 
     * @param aMimeType
     * @param aType
     * @return
     */
    public boolean contains(String aMimeType, int aType) {

        boolean result = false;
        for(ContactData data : contactData) {
            if(data.mimeType.equals(aMimeType)) {
                if(!data.hasTypes()) {
                    result = true; break;
                }
                ContactDataType defType = data.defaultType;
                for(ContactDataType type : data.types) {
                    if(type != null) {
                        if(type.type == aType) {
                            result = true; break;
                        }
                    } else if(defType != null) {
                        if(defType.type == aType) {
                            result = true; break;
                        }
                    } else {
                        result = true; break;
                    }
                }
                if(result) { break; }
            }
        }
        return result;
    }

    /**
     * Represents a specific Contact's data kind (e.g. a email address, a phone
     * number, etc.). It can includes multiple data types (e.g. work or home
     * phone numbers) and different fields (e.g. company name and title for the
     * organization info)
     */
    public class ContactData {

        // The data kind name resource
        public int nameRes;

        // The data mimetype
        public String mimeType;

        // The list of data types belonging to this data kind
        public List<ContactDataType>  types;

        // The list of the actual available types
        public List<ContactDataType>  availableTypes;

        // The list of edit fields
        public List<ContactDataField> fields;

        // Represents the default data type to use if the types list is empty
        public ContactDataType defaultType;

        // The number of types to show
        public int minCount = 0;
        
        public boolean secondary;

        public ContactData(int nameRes, String mymeType, boolean secondary) {

            this.nameRes = nameRes;
            this.mimeType = mymeType;
            this.secondary = secondary;

            this.types = new ArrayList<ContactDataType>();
            this.availableTypes = new ArrayList<ContactDataType>();
            this.fields = new ArrayList<ContactDataField>();
        }

        public void addDataType(ContactDataType type) {
            types.add(type);
        }

        public void addDataField(ContactDataField field) {
            fields.add(field);
        }

        public ContactData setDefaultType(ContactDataType type) {
            defaultType = type;
            return this;
        }

        public ContactData setMinCount(int count) {
            minCount = count;
            return this;
        }

        public boolean hasTypes() {
            return types.size() > 0;
        }
    }

    /**
     * Represents a specific Contact's data type (e.g. home or work)
     */
    public class ContactDataType {

        // The label resource
        public int labelRes;

        // The type reference
        public int type;

        public boolean isCustom = false;

        public ContactDataType(int type, int labelRes) {
            this.type = type;
            this.labelRes = labelRes;
        }

        public ContactDataType setIsCustom() {
            isCustom = true;
            return this;
        }
    }

    /**
     * Represents a specific edit field of a Contact's data kind
     */
    public class ContactDataField {

        // The field name resource
        public int     nameRes = -1;

        // The related db column name
        public String  column;

        // The input field constraints
        public int     inputType;

        public boolean optional;

        public ContactDataField(int nameRes, String column, int inputType,
                boolean optional) {
            this.nameRes = nameRes;
            this.column = column;
            this.inputType = inputType;
            this.optional = optional;
        }
    }

    /**
     * Represents a specific date edit field of a Contact's data kind
     */
    public class ContactDataDateField extends ContactDataField {
        public ContactDataDateField(int nameRes, String column, boolean optional) {
            super(nameRes, column, 0 /* Undefined*/, optional);
        }
    }

    public interface EditorListener {

        public static final int REQUEST_PICK_PHOTO = 1;
        
        public void onDeleted();

        public void onRequest(int request);
    }
}
