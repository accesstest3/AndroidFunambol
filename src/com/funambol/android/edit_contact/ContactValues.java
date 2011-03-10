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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Parcel;

import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContactsEntity;
import android.provider.ContactsContract.Intents.Insert;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;

import com.funambol.android.edit_contact.ContactDataStructure.ContactData;
import com.funambol.android.edit_contact.ContactDataStructure.ContactDataType;

import java.util.Hashtable;

/**
 * Represents the set of a contact values. The key is built using the mimetype
 * and the data type: vnd.android.cursor.item/phone_v22 represents a mobile phone.
 *
 * The values includes all the data values contained in the contacts data table.
 */
public class ContactValues extends Hashtable<String, ContentValues>
        implements Parcelable {

    public long rawContactId = -1;
    
    public ContactValues() {
        super();
    }

    /**
     * Read ContactValues from the store, given the rawContactId.
     * 
     * @param resolver
     * @param rawContactId
     * @return
     */
    public static ContactValues fromRawContactId(ContentResolver resolver,
            long rawContactId) {

        ContactValues result = new ContactValues();

        result.rawContactId = rawContactId;

        // We cannot ask for all fields (using null in the columns list because
        // on some devices this query fails. The problem was reported for the
        // Samsung Mesmerize (os 2.1)). For this reason we list here only the
        // fields we actually use.
        String cols[] = { RawContactsEntity.MIMETYPE,
                          RawContactsEntity.DATA2,
                          RawContactsEntity.DATA_ID,
                          ContactsContract.Data.IS_SUPER_PRIMARY,
                          "data1",
                          "data2",
                          "data3",
                          "data4",
                          "data5",
                          "data6",
                          "data7",
                          "data8",
                          "data9",
                          "data10",
                          "data11",
                          "data12",
                          "data13",
                          "data14",
                          "data15"};

        // Query RawContact's data
        Cursor c = resolver.query(RawContactsEntity.CONTENT_URI, cols,
            RawContactsEntity._ID+"="+rawContactId, null, null);
        
        try {
            while (c.moveToNext()) {

                String mime = c.getString(c.getColumnIndex(RawContactsEntity.MIMETYPE));
                int type = 0;

                // Check if the current contact doesn't contain any data row
                if(mime == null) {
                    break;
                }

                if(!mime.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE) &&
                   !mime.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
                    type = c.getInt(c.getColumnIndex(RawContactsEntity.DATA2));
                }
                long dataId = c.getLong(c.getColumnIndex(RawContactsEntity.DATA_ID));
                
                if(ContactDataStructure.getInstance().contains(mime, type)) {

                    // The key includes the mime type and the data type
                    String key = mime+type;

                    // Populate a ContentValue object with all the data values
                    ContentValues value = new ContentValues();

                    value.put(ContactsContract.Data.MIMETYPE, mime);
                    value.put(ContactsContract.Data._ID, dataId);

                    for(int i=1; i<=14; i++) {
                        value.put("data"+i, c.getString(c.getColumnIndex("data"+i)));
                    }
                    // Photo data
                    value.put("data15", c.getBlob(c.getColumnIndex("data15")));

                    value.put(ContactsContract.Data.IS_SUPER_PRIMARY, c.getInt(
                            c.getColumnIndex(ContactsContract.Data.IS_SUPER_PRIMARY)));

                    result.put(key, value);
                }
            }
        } finally {
            c.close();
        }

        return result;
    }

    /**
     * Read ContactValues from the given extras
     * 
     * @param extras
     * @param values
     * @return
     */
    public static ContactValues parseExtras(Bundle extras, ContactValues values) {

        if (extras == null || extras.size() == 0) {
            // No data to parse
            return values;
        }

        // Name
        parseExtra(extras, values, StructuredName.CONTENT_ITEM_TYPE, Insert.NAME,
                null, StructuredName.GIVEN_NAME);

        // StructuredPostal
        parseExtra(extras, values, StructuredPostal.CONTENT_ITEM_TYPE, Insert.POSTAL,
                Insert.POSTAL_TYPE, StructuredPostal.STREET);

        // Phone
        parseExtra(extras, values, Phone.CONTENT_ITEM_TYPE, Insert.PHONE,
                Insert.PHONE_TYPE, Phone.NUMBER);
        parseExtra(extras, values, Phone.CONTENT_ITEM_TYPE, Insert.SECONDARY_PHONE,
                Insert.SECONDARY_PHONE_TYPE, Phone.NUMBER);
        parseExtra(extras, values, Phone.CONTENT_ITEM_TYPE, Insert.TERTIARY_PHONE,
                Insert.TERTIARY_PHONE_TYPE, Phone.NUMBER);

        // Email
        parseExtra(extras, values, Email.CONTENT_ITEM_TYPE, Insert.EMAIL,
                Insert.EMAIL_TYPE, Phone.DATA);
        parseExtra(extras, values, Email.CONTENT_ITEM_TYPE, Insert.SECONDARY_EMAIL,
                Insert.SECONDARY_EMAIL_TYPE, Phone.DATA);
        parseExtra(extras, values, Email.CONTENT_ITEM_TYPE, Insert.TERTIARY_EMAIL,
                Insert.TERTIARY_EMAIL_TYPE, Phone.DATA);

        // Im
        parseExtra(extras, values, Im.CONTENT_ITEM_TYPE, Insert.IM_HANDLE,
                Insert.IM_PROTOCOL, Im.DATA);

        return values;
    }

    private static void parseExtra(Bundle extras, ContactValues values,
            String mimeType, String extraKey, String extraType, String extraColumn) {

        int typeValue = 0;
        CharSequence extraValue = extras.getCharSequence(extraKey);

        if(extraValue == null) {
            return;
        } else if(extraType != null) {
            typeValue = extras.getInt(extraType, -1);
            if(typeValue == -1 || !ContactDataStructure.getInstance().contains(
                    mimeType, typeValue)) {
                typeValue = getFirstAvailableType(mimeType, values);
                if(typeValue == -1) {
                    return;
                }
            }
        }

        String key = mimeType+typeValue;
        ContentValues value = values.get(key);
        if(value == null) {
            value = new ContentValues();
        }
        value.put(ContactsContract.Data.MIMETYPE, mimeType);
        value.put(extraColumn, extraValue.toString());
        if(typeValue > 0) {
            value.put(ContactsContract.Data.DATA2, typeValue);
        }
        values.put(key, value);
    }

    private static int getFirstAvailableType(String mimeType, ContactValues values) {
        ContactDataStructure structure = ContactDataStructure.getInstance();
        for(ContactData data : structure.getContactStructure()) {
            if(data.mimeType.equals(mimeType)) {
                ContactDataType defType = data.defaultType;
                for(ContactDataType type : data.types) {
                    if(type != null) {
                        String key = mimeType+type.type;
                        if(!values.containsKey(key)) {
                            return type.type;
                        }
                    } else if(defType != null) {
                        String key = mimeType+defType.type;
                        if(!values.containsKey(key)) {
                            return defType.type;
                        }
                    } else { return -1; }
                } break;
            }
        } return -1;
    }

    public boolean isNewContact() {
        return rawContactId == -1;
    }

    //------------------------------------------------ Parcelable implementation

    private int mData;

    private ContactValues(Parcel in) {
        mData = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mData);
    }

    public int describeContents() {
        return 0;
    }
    
    public static final Parcelable.Creator<ContactValues> CREATOR
            = new Parcelable.Creator<ContactValues>() {
        public ContactValues createFromParcel(Parcel in) {
            return new ContactValues(in);
        }
        public ContactValues[] newArray(int size) {
            return new ContactValues[size];
        }
    };
}

