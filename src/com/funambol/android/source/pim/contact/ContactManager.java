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

package com.funambol.android.source.pim.contact;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Vector;
import java.util.Enumeration;
import java.io.IOException;

import android.net.Uri;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.content.Context;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;

import com.funambol.android.AndroidCustomization;
import com.funambol.android.R;
import com.funambol.android.source.AbstractDataManager;

import com.funambol.common.pim.model.common.Property;
import com.funambol.common.pim.model.contact.Name;
import com.funambol.common.pim.model.contact.Note;
import com.funambol.common.pim.model.contact.Phone;
import com.funambol.common.pim.model.contact.Email;
import com.funambol.common.pim.model.contact.Title;
import com.funambol.common.pim.model.contact.WebPage;
import com.funambol.common.pim.model.contact.Photo;
import com.funambol.common.pim.model.contact.BusinessDetail;
import com.funambol.common.pim.model.contact.PersonalDetail;
import com.funambol.common.pim.model.contact.Address;

import com.funambol.util.StringUtil;
import com.funambol.util.Log;


public class ContactManager extends AbstractDataManager<Contact> {

    private static final String TAG = "ContactManager";

    private static final Uri RAW_CONTACT_URI = ContactsContract.RawContacts.CONTENT_URI;

    public static final String CONTACTS_AUTHORITY = "com.android.contacts";

    private static final String FUNAMBOL_SOURCE_ID_PREFIX = "funambol-";

    private AndroidCustomization customization = AndroidCustomization.getInstance();

    // TODO FIXME: get this from the server caps
    private boolean multipleFieldsSupported = false;

    private boolean callerIsSyncAdapter = true;

    // Constructors------------------------------------------------
    public ContactManager(Context context) {
        this(context, true);
    }

    public ContactManager(Context context, boolean callerIsSyncAdapter) {
        super(context);
        this.callerIsSyncAdapter = callerIsSyncAdapter;
    }

    protected String getAuthority() {
        return CONTACTS_AUTHORITY;
    }

    public Contact load(long key) throws IOException {

        HashMap<String,List<Integer>> fieldsMap = new HashMap<String, List<Integer>>();
        
        return load(key, fieldsMap);
    }

    private Contact load(long key, HashMap<String, List<Integer>> fieldsMap) 
            throws IOException {

        Log.trace(TAG, "Loading contact: " + key);

        // Set the id for this contact
        Contact contact = new Contact();
        contact.setId(key);

        // Load all pieces of information
        loadAllFields(contact, fieldsMap);

        return contact;
    }

    public long add(Contact item) throws IOException {
        Log.trace(TAG, "Saving contact");

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        Uri uri = addCallerIsSyncAdapterFlag(RAW_CONTACT_URI);

        // This is the first insert into the raw contacts table
        ContentProviderOperation i1 = ContentProviderOperation.newInsert(uri)
                                      .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, accountType)
                                      .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
                                      .build();
        ops.add(i1);

        prepareAllFields(item, null, ops);

        // Now create the contact with a single batch operation
        try {
            ContentProviderResult[] res = resolver.applyBatch(ContactsContract.AUTHORITY, ops);
            // The first insert is the one generating the ID for this contact
            long id = ContentUris.parseId(res[0].uri); 
            Log.trace(TAG, "The new contact has id: " + id);
            item.setId(id);

            finalizeCommand(id);

            return id;
        } catch (Exception e) {
            Log.error(TAG, "Cannot create contact ", e);
            throw new IOException("Cannot create contact in db");
        }
    }

    public void update(long rawContactId, Contact item) throws IOException {

        Log.trace(TAG, "Updating contact: " + rawContactId);

        // If the contact does not exist, then we perform an add
        if (!exists(rawContactId)) {
            Log.info(TAG, "Tried to update a non existing contact. Creating a new one ");
            add(item);
            return;
        }

        // Set the id
        item.setId(rawContactId);

        // Prepare the operations
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        // Load the old contact and fill the fields map
        HashMap<String,List<Integer>> fieldsMap = new HashMap<String, List<Integer>>();
        load(rawContactId, fieldsMap);

        prepareAllFields(item, fieldsMap, ops);

        // Now create the contact with a single batch operation
        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            Log.error(TAG, "Cannot update contact ", e);
            throw new IOException("Cannot update contact in db");
        }
    }

    public void delete(long rawContactId) throws IOException {

        Log.trace(TAG, "Deleting contact: " + rawContactId);

        int count = hardDelete(rawContactId);

        Log.debug(TAG, "Deleted contacts count: " + count);
        if (count < 1) {
            Log.error(TAG, "Cannot delete contact: " + rawContactId);
            throw new IOException("Cannot delete contact: " + rawContactId);
        }
    }

    /**
     * Hard delete the contact from the store
     * @param rawContactId
     * @return
     */
    private int hardDelete(long rawContactId) {

        Log.trace(TAG, "Hard deleting contact: " + rawContactId);

        Uri uri = addCallerIsSyncAdapterFlag(RAW_CONTACT_URI);

        // Delete from raw_contacts (related rows in Data table are
        // automatically deleted)
        return resolver.delete(uri,
                ContactsContract.RawContacts._ID+"="+rawContactId, null);
    }

    public void deleteAll() throws IOException {

        Log.trace(TAG, "Deleting all contacts");

        Uri uri = addCallerIsSyncAdapterFlag(RAW_CONTACT_URI);
        
        // Delete from raw_contacts (related rows in Data table are
        // automatically deleted)
        // Note: delete only contacts from funambol accounts
        int count = resolver.delete(uri,
                ContactsContract.RawContacts.ACCOUNT_TYPE+"='"+accountType+"'", null);

        Log.debug(TAG, "Deleted contacts count: " + count);
        if (count < 0) {
            Log.error(TAG, "Cannot delete all contacts");
            throw new IOException("Cannot delete contacts");
        }
    }

    public boolean exists(long id) {

        String cols[] = {ContactsContract.RawContacts._ID, ContactsContract.RawContacts.DELETED};
        Uri uri = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, id);
        Cursor cur = resolver.query(uri, cols, null, null, null);

        boolean found;
        if (!cur.moveToFirst()) {
            found = false;
        } else {
            int deleted = cur.getInt(1);
            if (deleted == 0) {
                found = true;
            } else {
                found = false;
            }
        }

        cur.close();
        return found;
    }

    public Enumeration getAllKeys() throws IOException {

        String cols[] = {ContactsContract.RawContacts._ID};
        StringBuffer whereClause = new StringBuffer();
        if (accountName != null) {
            whereClause.append(ContactsContract.RawContacts.ACCOUNT_NAME).append("='").append(accountName).append("'");
            whereClause.append(" AND ");
            whereClause.append(ContactsContract.RawContacts.ACCOUNT_TYPE).append("='").append(accountType).append("'");
            whereClause.append(" AND ");
        }
        whereClause.append(ContactsContract.RawContacts.DELETED).append("=").append("0");
        Cursor peopleCur = resolver.query(ContactsContract.RawContacts.CONTENT_URI,
                                          cols, whereClause.toString(), null, null);

        try {
            int contactListSize = peopleCur.getCount();
            Vector<String> itemKeys = new Vector<String>(contactListSize);

            if (!peopleCur.moveToFirst()) {
                return itemKeys.elements();
            }

            for (int i = 0; i < contactListSize; i++) {
                String key = peopleCur.getString(0);
                Log.trace(TAG, "Found item with key: " + key);
                itemKeys.addElement(key);
                peopleCur.moveToNext();
            }
            return itemKeys.elements();
        } catch (Exception e) {
            Log.error(TAG, "Cannot get all items keys: ", e);
            throw new IOException("Cannot get all items keys");
        } finally {
            peopleCur.close();
        }
    }

    public void finalizeCommand(long contactId) {
        Log.trace(TAG, "Finalizing command for contact: " + contactId);
        if(exists(contactId)) {
            Log.trace(TAG, "The contact exists, setting source_id attribute");
            ContentValues cv = new ContentValues();
            cv.put(ContactsContract.RawContacts.SOURCE_ID, FUNAMBOL_SOURCE_ID_PREFIX + contactId);
            resolver.update(ContentUris.withAppendedId(RAW_CONTACT_URI, contactId), cv, null, null);
        } else {
            Log.trace(TAG, "The contact doesn't exist, performing hard delete");
            hardDelete(contactId);
        }
    }

    private void loadAllFields(Contact contact, HashMap<String,List<Integer>> fieldsMap)
            throws IOException {
        
        long id = contact.getId();
        Log.trace(TAG, "Loading all fields for: " + id);

        PersonalDetail pd = contact.getPersonalDetail();
        if (pd == null) {
            pd = new PersonalDetail();
            contact.setPersonalDetail(pd);
        }
        BusinessDetail bd = contact.getBusinessDetail();
        if (bd == null) {
            bd = new BusinessDetail();
            contact.setBusinessDetail(bd);
        }

        Cursor allFields = resolver.query(ContactsContract.Data.CONTENT_URI, null,
                ContactsContract.Data.RAW_CONTACT_ID+"="+id, null, null);

        // Move to first element
        if (!allFields.moveToFirst()) {
            if(!exists(id)) {
                throw new IOException("Cannot find person " + id);
            } else {
                // The contact exists but there is nothing to load
                return;
            }
        }

        loadFromCursor(contact, allFields, fieldsMap);
    }

    private void loadFromCursor(Contact contact, Cursor cur,
            HashMap<String,List<Integer>> fieldsMap) throws IOException {

        try {
            do {
                String mimeType = cur.getString(cur.getColumnIndexOrThrow(
                        ContactsContract.Data.MIMETYPE));
                Log.trace(TAG, "Found a raw of type: " + mimeType);

                if (CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    loadNameField(contact, cur, fieldsMap);
                } else if (CommonDataKinds.Nickname.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    loadNickNameField(contact, cur, fieldsMap);
                } else if (CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    loadPhoneField(contact, cur, fieldsMap);
                } else if (CommonDataKinds.Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    loadEmailField(contact, cur, fieldsMap);
                } else if (CommonDataKinds.Photo.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    loadPhotoField(contact, cur, fieldsMap);
                } else if (CommonDataKinds.Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    loadOrganizationField(contact, cur, fieldsMap);
                } else if (CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    loadPostalAddressField(contact, cur, fieldsMap);
                } else if (CommonDataKinds.Event.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    loadEventField(contact, cur, fieldsMap);
                } else if (CommonDataKinds.Im.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    loadImField(contact, cur, fieldsMap);
                } else if (CommonDataKinds.Note.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    loadNoteField(contact, cur, fieldsMap);
                } else if (CommonDataKinds.Website.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    loadWebsiteField(contact, cur, fieldsMap);
                }  else if (CommonDataKinds.Relation.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    loadRelationField(contact, cur, fieldsMap);
                } else {
                    Log.info("Ignoring unknown row of type: " + mimeType);
                }
            } while(cur.moveToNext());
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(TAG, "Cannot load contact ", e);
            throw new IOException("Cannot load contact");
        } finally {
            cur.close();
        }
    }

    private void prepareAllFields(Contact contact,
                                  HashMap<String, List<Integer>> fieldsMap,
                                  List<ContentProviderOperation> ops)
    {
        prepareName          (contact, fieldsMap, ops);
        prepareNickname      (contact, fieldsMap, ops);
        preparePhones        (contact, fieldsMap, ops);
        prepareEmail         (contact, fieldsMap, ops);
        preparePhoto         (contact, fieldsMap, ops);
        prepareOrganization  (contact, fieldsMap, ops);
        preparePostalAddress (contact, fieldsMap, ops);
        prepareEvent         (contact, fieldsMap, ops);
        prepareNote          (contact, fieldsMap, ops);
        prepareWebsite       (contact, fieldsMap, ops);
        prepareRelation      (contact, fieldsMap, ops);
    }

    private void appendFieldId(HashMap<String,List<Integer>> fieldsMap,
                               String key, int rowId)
    {
        List<Integer> l = fieldsMap.get(key);
        if (l == null) {
            l = new ArrayList<Integer>();
            fieldsMap.put(key, l);
        }
        l.add(new Integer(rowId));
        Log.trace(TAG, "Appended fieldId " + rowId + " for " + key);
    }

    /**
     * Retrieve the People fields from a Cursor
     */
    private void loadNameField(Contact contact, Cursor cur, HashMap<String,List<Integer>> fieldsMap) {

        long id = contact.getId();
        Log.trace(TAG, "Loading name for: " + id);

        Name nameField = contact.getName();
        if(nameField == null) {
            nameField = new Name();
        }
        String dn = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.StructuredName.DISPLAY_NAME));
        if (dn != null && customization.isDisplayNameSupported()) {
            // setting firstName and lastName from the combined name
            Property dnProp = new Property(dn);
            nameField.setDisplayName(dnProp);
        }
        String firstName = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.StructuredName.GIVEN_NAME));
        if (firstName != null) {
            Property firstNameProp = new Property(firstName);
            nameField.setFirstName(firstNameProp);
        }
        String middleName = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.StructuredName.MIDDLE_NAME));
        if (middleName != null) {
            Property middleNameProp = new Property(middleName);
            nameField.setMiddleName(middleNameProp);
        }
        String lastName = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.StructuredName.FAMILY_NAME));
        if (lastName != null) {
            Property lastNameProp = new Property(lastName);
            nameField.setLastName(lastNameProp);
        }
        String prefixName = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.StructuredName.PREFIX));
        if (prefixName != null) {
            Property prefixNameProp = new Property(prefixName);
            nameField.setSalutation(prefixNameProp);
        }
        String suffixName = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.StructuredName.SUFFIX));
        if (suffixName != null) {
            Property suffixNameProp = new Property(suffixName);
            nameField.setSuffix(suffixNameProp);
        }
        contact.setName(nameField);

        loadFieldToMap(CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, 0, cur, fieldsMap);
    }

    /**
     * Retrieve the NickName field from a Cursor
     */
    private void loadNickNameField(Contact contact, Cursor cur, HashMap<String,List<Integer>> fieldsMap) {

        long id = contact.getId();
        Log.trace(TAG, "Loading nickname for: " + id);

        Name nameField = contact.getName();
        if(nameField == null) {
            nameField = new Name();
        }

        String nickName = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.Nickname.NAME));
        int nickNameType = cur.getInt(cur.getColumnIndexOrThrow(CommonDataKinds.Nickname.TYPE));
        if (nickNameType == CommonDataKinds.Nickname.TYPE_DEFAULT) {
            Property nickNameProp = new Property(nickName);
            nameField.setNickname(nickNameProp);
        }
        contact.setName(nameField);

        loadFieldToMap(CommonDataKinds.Nickname.CONTENT_ITEM_TYPE, 0, cur, fieldsMap);
    }


    /**
     * Retrieve the Phone fields from a Cursor
     */
    private void loadPhoneField(Contact contact, Cursor cur, HashMap<String, List<Integer>> fieldsMap) {

        long id = contact.getId();
        Log.debug(TAG, "Load Phone Field for: " + id);

        PersonalDetail pd = contact.getPersonalDetail();
        BusinessDetail bd = contact.getBusinessDetail();

        int idx = 1;
        String number = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.Phone.NUMBER));
        int phoneType = cur.getInt(cur.getColumnIndexOrThrow(CommonDataKinds.Phone.TYPE));

        String label = null;

        if (phoneType == CommonDataKinds.Phone.TYPE_HOME) {
            Phone phone = new Phone(number);
            phone.setPhoneType(Phone.getHomePhoneNumberID(idx));
            pd.addPhone(phone);
        } else if (phoneType == CommonDataKinds.Phone.TYPE_WORK) {
            Phone phone = new Phone(number);
            phone.setPhoneType(Phone.getBusinessPhoneNumberID(idx));
            bd.addPhone(phone);
        } else if (phoneType == CommonDataKinds.Phone.TYPE_MOBILE) {
            Phone phone = new Phone(number);
            phone.setPhoneType(Phone.getMobilePhoneNumberID(idx));
            pd.addPhone(phone);
        } else if (phoneType == CommonDataKinds.Phone.TYPE_OTHER) {
            Phone phone = new Phone(number);
            phone.setPhoneType(Phone.getOtherPhoneNumberID(idx));
            pd.addPhone(phone);
        } else if (phoneType == CommonDataKinds.Phone.TYPE_FAX_HOME) {
            Phone phone = new Phone(number);
            phone.setPhoneType(Phone.getHomeFaxNumberID(idx));
            pd.addPhone(phone);
        } else if (phoneType == CommonDataKinds.Phone.TYPE_FAX_WORK) {
            Phone phone = new Phone(number);
            phone.setPhoneType(Phone.getBusinessFaxNumberID(idx));
            bd.addPhone(phone);
        } else if (phoneType == CommonDataKinds.Phone.TYPE_PAGER) {
            Phone phone = new Phone(number);
            phone.setPhoneType(Phone.getPagerNumberID(idx));
            pd.addPhone(phone);
        } else if (phoneType == CommonDataKinds.Phone.TYPE_COMPANY_MAIN) {
            Phone phone = new Phone(number);
            phone.setPhoneType(Phone.getCompanyPhoneNumberID(idx));
            bd.addPhone(phone);
        } else if (phoneType == CommonDataKinds.Phone.TYPE_OTHER_FAX) {
            Phone phone = new Phone(number);
            phone.setPhoneType(Phone.getOtherFaxNumberID(idx));
            pd.addPhone(phone);
        } else if (phoneType == CommonDataKinds.Phone.TYPE_MAIN) {
            Phone phone = new Phone(number);
            phone.setPhoneType(Phone.getPrimaryPhoneNumberID(idx));
            pd.addPhone(phone);
        } else if (phoneType == CommonDataKinds.Phone.TYPE_CUSTOM) {
            label = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.Phone.LABEL));
            if (context.getString(R.string.label_work2_phone).equals(label)) {
                Phone work2Phone = new Phone(number);
                work2Phone.setPhoneType(Phone.getBusinessPhoneNumberID(2));
                bd.addPhone(work2Phone);
            } else {
                Log.info(TAG, "Ignoring custom phone number: " + label);
            }
        } else {
            Log.error(TAG, "Ignoring unknwon phone type: " + phoneType);
        }

        loadFieldToMap(CommonDataKinds.Phone.CONTENT_ITEM_TYPE, phoneType, label, cur, fieldsMap);
    }

    /**
     * Retrieve the email fields from a Cursor
     */
    private void loadEmailField(Contact contact, Cursor cur, HashMap<String, List<Integer>> fieldsMap) {

        long id = contact.getId();
        Log.trace(TAG, "Load Email Field for: " + id);

        PersonalDetail pd = contact.getPersonalDetail();
        BusinessDetail bd = contact.getBusinessDetail();

        String email = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.Email.DATA));
        
        int emailType = cur.getInt(cur.getColumnIndexOrThrow(CommonDataKinds.Email.TYPE));
        switch(emailType) {
            case CommonDataKinds.Email.TYPE_HOME:
            {
                Email emailObj = new Email(email);
                emailObj.setEmailType(Email.MAIN_EMAIL);
                pd.addEmail(emailObj);
                break;
            }
            case CommonDataKinds.Email.TYPE_WORK:
            {
                Email emailObj = new Email(email);
                emailObj.setEmailType(Email.WORK_EMAIL);
                bd.addEmail(emailObj);
                break;
            }
            case CommonDataKinds.Email.TYPE_OTHER:
            {
                Email emailObj = new Email(email);
                emailObj.setEmailType(Email.OTHER_EMAIL);
                pd.addEmail(emailObj);
                break;
            }
            default:
                Log.error(TAG, "Ignoring unknown email type: " + emailType);
        }

        loadFieldToMap(CommonDataKinds.Email.CONTENT_ITEM_TYPE, emailType, cur, fieldsMap);
    }

    /**
     * Retrieve the photo fields from a Cursor
     */
    private void loadPhotoField(Contact contact, Cursor cur, HashMap<String, List<Integer>> fieldsMap) {

        long id = contact.getId();
        Log.trace(TAG, "Loading Photo Field for: " + id);

        PersonalDetail pd = contact.getPersonalDetail();

        byte data[] = cur.getBlob(cur.getColumnIndexOrThrow(CommonDataKinds.Photo.PHOTO));
        if (data != null) {
            Log.trace(TAG, "This contact has a photo associated");

            String type = detectImageFormat(data);
            Log.trace(TAG, "Photo type: " + type);

            Photo photo = new Photo();
            photo.setImage(data);
            photo.setType(type);
            pd.setPhotoObject(photo);

            loadFieldToMap(CommonDataKinds.Photo.CONTENT_ITEM_TYPE, 0, cur, fieldsMap);
        }
    }

    /**
     * Detects the file format of the given image.
     * Supported formats are:
     *  image/bmp
     *  image/jpeg
     *  image/png
     *  image/gif
     * @param image
     * @return the image mime type or null if unknown
     */
    private String detectImageFormat(byte[] image) {

        final byte[] BMP_PREFIX  = {(byte)0x42, (byte)0x4D};
        final byte[] JPEG_PREFIX = {(byte)0xFF, (byte)0xD8, (byte)0xFF};
        final byte[] PNG_PREFIX  = {(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47};
        final byte[] GIF_PREFIX  = {(byte)0x47, (byte)0x49, (byte)0x46, (byte)0x38};

        final String BMP_MIME_TYPE  = "image/bmp";
        final String JPEG_MIME_TYPE = "image/jpeg";
        final String PNG_MIME_TYPE  = "image/png";
        final String GIF_MIME_TYPE  = "image/gif";
        
        // Check image prefixes
        if(checkImagePrefix(image, BMP_PREFIX))  return BMP_MIME_TYPE;
        if(checkImagePrefix(image, JPEG_PREFIX)) return JPEG_MIME_TYPE;
        if(checkImagePrefix(image, PNG_PREFIX))  return PNG_MIME_TYPE;
        if(checkImagePrefix(image, GIF_PREFIX))  return GIF_MIME_TYPE;

        // Image type not detected
        return null;
    }

    private boolean checkImagePrefix(byte[] image, byte[] prefix) {
        if(image == null || prefix == null) {
            return false;
        }
        boolean match = true;
        for(int i=0; i<prefix.length; i++) {
            if(i < image.length) {
                match &= image[i] == prefix[i];
            } else {
                match = false;
                break;
            }
        }
        return match;
    }

    /**
     * Retrieve the Organization fields from a Cursor
     */
    private void loadOrganizationField(Contact contact, Cursor cur,
                                       HashMap<String,List<Integer>> fieldsMap) throws IOException
    {
        long id = contact.getId();
        Log.trace(TAG, "Load Organization Field for: " + id);

        BusinessDetail bd = contact.getBusinessDetail();

        int orgType = cur.getInt(cur.getColumnIndexOrThrow(CommonDataKinds.Organization.TYPE));
        if (orgType == CommonDataKinds.Organization.TYPE_WORK) {
            int colId = cur.getColumnIndexOrThrow(CommonDataKinds.Organization.COMPANY);
            String company = cur.getString(colId);
            if (company != null) {
                Property companyProp = new Property(company);
                bd.setCompany(companyProp);
            }

            colId = cur.getColumnIndexOrThrow(CommonDataKinds.Organization.TITLE);
            String title = cur.getString(colId);
            if (title != null) {
                ArrayList titles = new ArrayList();
                Title titleProp = new Title(title);
                titles.add(titleProp);
                bd.setTitles(titles);
            }

            colId = cur.getColumnIndexOrThrow(CommonDataKinds.Organization.DEPARTMENT);
            String department = cur.getString(colId);
            if (department != null) {
                Property departmentProp = new Property(department);
                bd.setDepartment(departmentProp);
            }

            colId = cur.getColumnIndexOrThrow(CommonDataKinds.Organization.OFFICE_LOCATION);
            String location = cur.getString(colId);
            if (location != null) {
                bd.setOfficeLocation(location);
            }
        } else {
            Log.info(TAG, "Ignoring organization of type " + orgType);
        }

        loadFieldToMap(CommonDataKinds.Organization.CONTENT_ITEM_TYPE, 0, cur, fieldsMap);
    }

    /**
     * Retrieve the postal address fields from a Cursor
     */
    private void loadPostalAddressField(Contact contact, Cursor cur, HashMap<String, List<Integer>> fieldsMap) {

        long id = contact.getId();
        Log.trace(TAG, "Load Address Fields for: " + id);

        PersonalDetail pd = contact.getPersonalDetail();
        BusinessDetail bd = contact.getBusinessDetail();
        Address  address = new Address();

        String city  = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.StructuredPostal.CITY));
        if (city != null) {
            Property cityProp = new Property(city);
            address.setCity(cityProp);
        }
        String country  = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.StructuredPostal.COUNTRY));
        if (country != null) {
            Property countryProp = new Property(country);
            address.setCountry(countryProp);
        }
        String pobox  = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.StructuredPostal.POBOX));
        if (pobox != null) {
            Property poboxProp = new Property(pobox);
            address.setPostOfficeAddress(poboxProp);
        }
        String poCode  = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.StructuredPostal.POSTCODE));
        if (poCode != null) {
            Property poCodeProp = new Property(poCode);
            address.setPostalCode(poCodeProp);
        }
        String region  = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.StructuredPostal.REGION));
        if (region != null) {
            Property stateProp = new Property(region);
            address.setState(stateProp);
        }
        String street  = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.StructuredPostal.STREET));
        if (street != null) {
            Property streetProp = new Property(street);
            address.setStreet(streetProp);
        }
        String extAddress  = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.StructuredPostal.NEIGHBORHOOD));
        if (extAddress != null) {
            Property extAddressProp = new Property(extAddress);
            address.setExtendedAddress(extAddressProp);
        }

        int type  = cur.getInt(cur.getColumnIndexOrThrow(CommonDataKinds.StructuredPostal.TYPE));
        if (type == CommonDataKinds.StructuredPostal.TYPE_WORK) {
            bd.setAddress(address);
        } else if (type == CommonDataKinds.StructuredPostal.TYPE_HOME){
            pd.setAddress(address);
        } else if (type == CommonDataKinds.StructuredPostal.TYPE_OTHER){
            pd.setOtherAddress(address);
        } else {
            Log.info(TAG, "Ignoring other address");
        }

        loadFieldToMap(CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE, type, cur, fieldsMap);
    }

    /**
     * Retrieve the Event field from a Cursor
     */
    private void loadEventField(Contact contact, Cursor cur, HashMap<String,List<Integer>> fieldsMap) {

        long id = contact.getId();
        Log.trace(TAG, "Loading event for: " + id);

        PersonalDetail pd = contact.getPersonalDetail();

        String eventDate = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.Event.START_DATE));
        int eventType = cur.getInt(cur.getColumnIndexOrThrow(CommonDataKinds.Event.TYPE));

        if (eventType == CommonDataKinds.Event.TYPE_BIRTHDAY) {
            pd.setBirthday(eventDate);
        } else if (eventType == CommonDataKinds.Event.TYPE_ANNIVERSARY) {
            pd.setAnniversary(eventDate);
        } else {
            Log.error(TAG, "Ignoring unknown event type: " + eventType);
        }

        loadFieldToMap(CommonDataKinds.Event.CONTENT_ITEM_TYPE, eventType, cur, fieldsMap);
    }

    /**
     * Retrieve the Im field from a Cursor
     */
    private void loadImField(Contact contact, Cursor cur, HashMap<String,List<Integer>> fieldsMap) {

        long id = contact.getId();
        Log.trace(TAG, "Loading Im for: " + id);

        PersonalDetail pd = contact.getPersonalDetail();
        BusinessDetail bd = contact.getBusinessDetail();

        String im = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.Im.DATA));
        int imType =  cur.getInt(cur.getColumnIndexOrThrow(CommonDataKinds.Im.TYPE));
        int imProtocol = cur.getInt(cur.getColumnIndexOrThrow(CommonDataKinds.Im.PROTOCOL));

        if(imProtocol == CommonDataKinds.Im.PROTOCOL_AIM) {

            Email emailObj = new Email(im);
            emailObj.setEmailType("IMAddress");
            
            HashMap<String,String> params = new HashMap<String,String>();
            params.put("X-FUNAMBOL-INSTANTMESSENGER", null);
            emailObj.setXParams(params);

            if (imType == CommonDataKinds.Im.TYPE_HOME ||
                imType == CommonDataKinds.Im.TYPE_OTHER) {
                pd.addEmail(emailObj);
            } else if (imType == CommonDataKinds.Im.TYPE_WORK) {
                bd.addEmail(emailObj);
            } else {
                Log.error(TAG, "Ignoring unknown Im type: " + imType);
            }
        } else {
            Log.error(TAG, "Ignoring unknown Im protocol: " + imProtocol);
        }

        loadFieldToMap(CommonDataKinds.Im.CONTENT_ITEM_TYPE, imType, cur, fieldsMap);
    }

    /**
     * Retrieve the Note field from a Cursor
     */
    private void loadNoteField(Contact contact, Cursor cur, HashMap<String,List<Integer>> fieldsMap) {

        long id = contact.getId();
        Log.trace(TAG, "Loading note for: " + id);

        String note = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.Note.NOTE));

        if (note != null) {
            // The device cannot display \r characters, so we add them back here
            note = StringUtil.replaceAll(note, "\n", "\r\n");
        }

        Note noteField = new Note();
        noteField.setPropertyValue(note);
        // Ensure the property type is set
        noteField.setPropertyType("");
        contact.addNote(noteField);

        loadFieldToMap(CommonDataKinds.Note.CONTENT_ITEM_TYPE, 0, cur, fieldsMap);
    }

    /**
     * Retrieve the Website field from a Cursor
     */
    private void loadWebsiteField(Contact contact, Cursor cur, HashMap<String,List<Integer>> fieldsMap) {

        long id = contact.getId();
        Log.trace(TAG, "Loading Website for: " + id);

        PersonalDetail pd = contact.getPersonalDetail();
        BusinessDetail bd = contact.getBusinessDetail();
        
        String url = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.Website.URL));
        int websiteType =  cur.getInt(cur.getColumnIndexOrThrow(CommonDataKinds.Website.TYPE));

        WebPage website = new WebPage(url);
        if (websiteType == CommonDataKinds.Website.TYPE_OTHER) {
            website.setPropertyType("WebPage");
            pd.addWebPage(website);
        } else if (websiteType == CommonDataKinds.Website.TYPE_HOME) {
            website.setPropertyType("HomeWebPage");
            pd.addWebPage(website);
        } else if (websiteType == CommonDataKinds.Website.TYPE_WORK) {
            website.setPropertyType("BusinessWebPage");
            bd.addWebPage(website);
        } else {
            Log.error(TAG, "Ignoring unknown Website type: " + websiteType);
        }

        loadFieldToMap(CommonDataKinds.Website.CONTENT_ITEM_TYPE, websiteType, cur, fieldsMap);
    }

    /**
     * Retrieve the Relation field from a Cursor
     */
    private void loadRelationField(Contact contact, Cursor cur, HashMap<String,List<Integer>> fieldsMap) {

        long id = contact.getId();
        Log.trace(TAG, "Loading Relation for: " + id);

        PersonalDetail pd = contact.getPersonalDetail();
        
        String relName = cur.getString(cur.getColumnIndexOrThrow(CommonDataKinds.Relation.NAME));
        int relType =  cur.getInt(cur.getColumnIndexOrThrow(CommonDataKinds.Relation.TYPE));

        if (relType == CommonDataKinds.Relation.TYPE_CHILD) {
            pd.setChildren(relName);
        } else if (relType == CommonDataKinds.Relation.TYPE_SPOUSE) {
            pd.setSpouse(relName);
        } else {
            Log.error(TAG, "Ignoring unknown Relation type: " + relType);
        }

        loadFieldToMap(CommonDataKinds.Relation.CONTENT_ITEM_TYPE, relType, cur, fieldsMap);
    }

    /**
     * Load the given field to the fieldsMap if needed
     */
    private void loadFieldToMap(String field, int fieldType, Cursor cur,
            HashMap<String,List<Integer>> fieldsMap) {
        loadFieldToMap(field, fieldType, null, cur, fieldsMap);
    }

    private void loadFieldToMap(String field, int fieldType, String label,
            Cursor cur, HashMap<String,List<Integer>> fieldsMap) {
        if (fieldsMap != null) {
            String fieldId = createFieldId(new Object[] {field, fieldType, label});
            if (multipleFieldsSupported || fieldsMap.get(fieldId) == null) {
                int rowId = cur.getInt(cur.getColumnIndexOrThrow("_ID"));
                appendFieldId(fieldsMap, fieldId, rowId);
            }
        }
    }

    /**
     * Check if the given date string is well formatted. Supported formats:
     *  1. yyyymmdd
     *  2. yyyy-mm-dd
     *  3. yyyy/mm/dd
     */
    private boolean checkDate(String date) {

        if (date.length() == 8) {
            try {
                // date must contain only digits
                Integer.parseInt(date);
                return true;
            } catch(NumberFormatException ex) {
                return false;
            }
        } else if (date.length() == 10) {
            if((date.charAt(4) == '-' && date.charAt(7) == '-') ||
               (date.charAt(4) == '/' && date.charAt(7) == '/')) {
                try {
                    Integer.parseInt(date.substring(0, 4));  // yyyy
                    Integer.parseInt(date.substring(5, 7));  // mm
                    Integer.parseInt(date.substring(8, 10)); // dd
                    return true;
                } catch(NumberFormatException ex) {
                    return false;
                }
            } else {
                // Invalid separators
                return false;
            }
        } else {
            // Invalid length
            return false;
        }
    }

    private ContentProviderOperation.Builder prepareBuilder(long contactId, String fieldId,
                                                            HashMap<String, List<Integer>> fieldsMap,
                                                            List<ContentProviderOperation> ops)
    {
        List<Integer> rowIds = null;
        if (fieldsMap != null) {
            rowIds = fieldsMap.get(fieldId);
            Log.trace(TAG, "Found " + rowIds + " for " + fieldId);
        }
        boolean insert = true;
        ContentProviderOperation.Builder builder = null;
        if (rowIds != null && rowIds.size() > 0) {
            if (multipleFieldsSupported) {
                // We must delete all the old fields
                prepareRowDeletion(rowIds, ops);
                // After deleting all the entries of the given type, we
                // add them back with the new values
            } else {
                // We update the first one here
                // TODO: check if we actually need to delete this entry
                Log.trace(TAG, "prepareBuilder will perform an update");
                long rowId = rowIds.get(0);
                Uri uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, rowId);
                uri = addCallerIsSyncAdapterFlag(uri);
                builder = ContentProviderOperation.newUpdate(uri);
                insert = false;
            }
        }
        if (insert) {
            Log.trace(TAG, "prepareBuilder will perform an insert");
            Uri uri = addCallerIsSyncAdapterFlag(ContactsContract.Data.CONTENT_URI);
            builder = ContentProviderOperation.newInsert(uri);
        }
        // Set the contact id
        if (contactId != -1) {
            Log.trace(TAG, "Updating contact data: " + contactId);
            builder = builder.withValue(ContactsContract.Data.RAW_CONTACT_ID, contactId);
        } else {
            Log.trace(TAG, "Inserting new contact data");
            builder = builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
        }

        return builder;
    }

    protected void prepareName(Contact contact,
                               HashMap<String, List<Integer>> fieldsMap,
                               List<ContentProviderOperation> ops)
    {
        String fieldId = createFieldId(CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, 0);
        ContentProviderOperation.Builder builder;
        builder = prepareBuilder(contact.getId(), fieldId, fieldsMap, ops);

        Name nameField = contact.getName();

        if (nameField == null) {
            return;
        }

        // Set all the various bits and pieces
        Property dnProp = nameField.getDisplayName();
        Property firstNameProp = nameField.getFirstName();
        Property middleNameProp = nameField.getMiddleName();
        Property lastNameProp = nameField.getLastName();
        Property suffixProp = nameField.getSuffix();
        Property salutationProp = nameField.getSalutation();

        Property props[] = {firstNameProp, middleNameProp, lastNameProp,
                            suffixProp, salutationProp};
        if (isNull(props)) {
            // Simply return if the server didn't send anything
            return;
        }

        String displayName = dnProp != null ? dnProp.getPropertyValueAsString() : null;
        String firstName   = firstNameProp.getPropertyValueAsString();
        String middleName  = middleNameProp.getPropertyValueAsString();
        String lastName    = lastNameProp.getPropertyValueAsString();
        String suffix      = suffixProp.getPropertyValueAsString();
        String salutation  = salutationProp.getPropertyValueAsString();

        String propValues[] = {firstName, middleName, lastName, suffix, salutation};

        if (isNull(propValues)) {
            // Simply return if the server didn't send anything
            return;
        } else if (isFieldEmpty(propValues)) {
            if(fieldsMap != null) {
                // In this case the server sent an empty name, we shall remove the
                // old entry to clean it
                prepareRowDeletion(fieldsMap.get(fieldId), ops);
            }
            return;
        }

        builder = builder.withValue(ContactsContract.Data.MIMETYPE,
                                    CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);

        if (customization.isDisplayNameSupported()) {
            if (displayName != null) {
                builder = builder.withValue(CommonDataKinds.StructuredName.DISPLAY_NAME, displayName);
            }
        } else {
            // If the display name is not supported as a stand alone field, then
            // we create it as the concatenation of the name components
            StringBuffer dn = new StringBuffer();
            if (firstName != null) {
                dn.append(firstName);
            }
            if (middleName != null && middleName.length() > 0) {
                if (dn.length() > 0) {
                    dn.append(" ");
                }
                dn.append(middleName);
            }
            if (lastName != null && lastName.length() > 0) {
                if (dn.length() > 0) {
                    dn.append(" ");
                }
                dn.append(lastName);
            }
            if (dn.length() > 0) {
                builder = builder.withValue(CommonDataKinds.StructuredName.DISPLAY_NAME, dn.toString());
            }
        }

        if (firstName != null) {
            builder = builder.withValue(CommonDataKinds.StructuredName.GIVEN_NAME, firstName);
        }

        if (middleName != null) {
            builder = builder.withValue(CommonDataKinds.StructuredName.MIDDLE_NAME, middleName);
        }

        if (lastName != null) {
            builder = builder.withValue(CommonDataKinds.StructuredName.FAMILY_NAME, lastName);
        }

        if (suffix != null) {
            builder = builder.withValue(CommonDataKinds.StructuredName.SUFFIX, suffix);
        }

        if (salutation != null) {
            builder = builder.withValue(CommonDataKinds.StructuredName.PREFIX, salutation);
        }

        // Append the operation
        ContentProviderOperation operation = builder.build();
        ops.add(operation);
    }

    protected void prepareNickname(Contact contact,
                               HashMap<String, List<Integer>> fieldsMap,
                               List<ContentProviderOperation> ops)
    {
        String fieldId = createFieldId(CommonDataKinds.Nickname.CONTENT_ITEM_TYPE, 0);
        ContentProviderOperation.Builder builder;
        builder = prepareBuilder(contact.getId(), fieldId, fieldsMap, ops);

        Name nameField = contact.getName();

        if (nameField == null) {
            // No name specified, do not add anything
            return;
        }

        Property nnProp = nameField.getNickname();

        if (nnProp == null) {
            // Check if the server did not send anything. In this case we simply
            // return
            return;
        }

        String nickName = nnProp.getPropertyValueAsString();
        if (nickName == null) {
            // Check if the server did not send anything. In this case we simply
            // return
            return;
        } else if ("".equals(nickName)) {
            if(fieldsMap != null) {
                // In this case the server sent an empty name, we shall remove the
                // old entry to clean it
                prepareRowDeletion(fieldsMap.get(fieldId), ops);
            }
            return;
        }

        builder = builder.withValue(ContactsContract.Data.MIMETYPE,
                CommonDataKinds.Nickname.CONTENT_ITEM_TYPE);

        builder = builder.withValue(CommonDataKinds.Nickname.TYPE, 
                CommonDataKinds.Nickname.TYPE_DEFAULT);

        builder = builder.withValue(CommonDataKinds.Nickname.NAME, nickName);

        ContentProviderOperation operation = builder.build();
        ops.add(operation);
    }

    private void preparePhones(Contact contact,
                               HashMap<String,List<Integer>> fieldsMap,
                               List<ContentProviderOperation> ops)
    {
        PersonalDetail pd = contact.getPersonalDetail();
        if (pd != null) {
            List<Phone> phones = pd.getPhones();
            if (phones != null) {
                preparePhones(contact, fieldsMap, phones, ops);
            }
        }
        BusinessDetail bd = contact.getBusinessDetail();
        if (bd != null) {
            List<Phone> phones = bd.getPhones();
            if (phones != null) {
                preparePhones(contact, fieldsMap, phones, ops);
            }
        }
    }

    private void preparePhones(Contact contact,
                               HashMap<String,List<Integer>> fieldsMap,
                               List<Phone> phones,
                               List<ContentProviderOperation> ops)
    {
        for( Phone phone : phones) {

            String number = phone.getPropertyValueAsString();
            StringBuffer label = new StringBuffer();
            int type = getPhoneType(phone, label);
            if(label.length() == 0) {
                label = null;
            }
            if (type != -1) {

                String fieldId = createFieldId(new Object[] 
                {CommonDataKinds.Phone.CONTENT_ITEM_TYPE, type, label});
                
                if (StringUtil.isNullOrEmpty(number)) {
                    if(fieldsMap != null) {
                        // The field is empty, we shall remove it as the server
                        // wants to emtpy it
                        prepareRowDeletion(fieldsMap.get(fieldId), ops);
                    }
                    continue;
                }

                ContentProviderOperation.Builder builder;
                builder = prepareBuilder(contact.getId(), fieldId, fieldsMap, ops);
                builder = builder.withValue(ContactsContract.Data.MIMETYPE,
                                            CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                builder = builder.withValue(CommonDataKinds.Phone.NUMBER, number);
                builder = builder.withValue(CommonDataKinds.Phone.TYPE, type);
                if (label != null && label.length() > 0) {
                    builder = builder.withValue(CommonDataKinds.Phone.LABEL, label.toString());
                }
                ContentProviderOperation operation = builder.build();
                ops.add(operation);
            } else {
                Log.info(TAG, "Ignoring unknown phone number of type: " + phone.getPhoneType());
            }
        }
    }

    private int getPhoneType(Phone phone, StringBuffer customLabel) {
        String phoneType = phone.getPhoneType();

        int t = -1;
        Log.trace(TAG, "Getting phone type for: " + phoneType);

        for(int i=1;i<10;++i) {
            if (Phone.getCompanyPhoneNumberID(i).equals(phoneType)) {
                t = CommonDataKinds.Phone.TYPE_COMPANY_MAIN;
                break;
            } else if (Phone.getPagerNumberID(i).equals(phoneType)) {
                t = CommonDataKinds.Phone.TYPE_PAGER;
                break;
            } else if (Phone.getMobilePhoneNumberID(i).equals(phoneType)) {
                t = CommonDataKinds.Phone.TYPE_MOBILE;
                break;
            } else if (Phone.getOtherPhoneNumberID(i).equals(phoneType)) {
                t = CommonDataKinds.Phone.TYPE_OTHER;
                break;
            } else if (Phone.getHomePhoneNumberID(i).equals(phoneType)) {
                t = CommonDataKinds.Phone.TYPE_HOME;
                break;
            } else if (Phone.getBusinessPhoneNumberID(i).equals(phoneType)) {
                if (i == 1) {
                    t = CommonDataKinds.Phone.TYPE_WORK;
                } else {
                    customLabel.append(context.getString(R.string.label_work2_phone));
                    t = CommonDataKinds.Phone.TYPE_CUSTOM;
                }
                break;
            } else if (Phone.getOtherFaxNumberID(i).equals(phoneType)) {
                t = CommonDataKinds.Phone.TYPE_OTHER_FAX;
                break;
            } else if (Phone.getHomeFaxNumberID(i).equals(phoneType)) {
                t = CommonDataKinds.Phone.TYPE_FAX_HOME;
                break;
            } else if (Phone.getBusinessFaxNumberID(i).equals(phoneType)) {
                t = CommonDataKinds.Phone.TYPE_FAX_WORK;
                break;
            } else if (Phone.getPrimaryPhoneNumberID(i).equals(phoneType)) {
                t = CommonDataKinds.Phone.TYPE_MAIN;
                break;
            }
        }
        Log.trace(TAG, "Phone type mapped to: " + t);
        return t;
    }

    private void prepareEmail(Contact contact,
                              HashMap<String, List<Integer>> fieldsMap,
                              List<ContentProviderOperation> ops)
    {
        long id = contact.getId();
        PersonalDetail pd = contact.getPersonalDetail();
        if (pd != null) {
            List<Email> emails = pd.getEmails();
            for(Email email : emails) {
                String addr = email.getPropertyValueAsString();
                if (Email.MAIN_EMAIL.equals(email.getEmailType())) {
                    prepareEmail(id, addr, CommonDataKinds.Email.TYPE_HOME,
                            fieldsMap, ops);
                } else if (Email.OTHER_EMAIL.equals(email.getEmailType())) {
                    prepareEmail(id, addr, CommonDataKinds.Email.TYPE_OTHER,
                            fieldsMap, ops);
                } else if ("IMAddress".equals(email.getEmailType())) {
                    prepareIm(id, addr, CommonDataKinds.Im.TYPE_HOME,
                            fieldsMap, ops);
                } else {
                    Log.info(TAG, "Ignoring email address " + email.getEmailType());
                }
            }
        }

        BusinessDetail bd = contact.getBusinessDetail();
        if (bd != null) {
            List<Email> emails = bd.getEmails();
            for(Email email : emails) {
                String addr = email.getPropertyValueAsString();
                if (Email.WORK_EMAIL.equals(email.getEmailType())) {
                    prepareEmail(id, addr, CommonDataKinds.Email.TYPE_WORK,
                            fieldsMap, ops);
                } else if ("IMAddress".equals(email.getEmailType())) {
                    prepareIm(id, addr, CommonDataKinds.Im.TYPE_WORK,
                            fieldsMap, ops);
                } else {
                    Log.info(TAG, "Ignoring email address " + email.getEmailType());
                }
            }
        }
    }

    protected void prepareEmail(long id, String address, int type,
            HashMap<String,List<Integer>> fieldsMap,
            List<ContentProviderOperation> ops)
    {
        String fieldId = createFieldId(CommonDataKinds.Email.CONTENT_ITEM_TYPE, type);
        if (StringUtil.isNullOrEmpty(address)) {
            if(fieldsMap != null) {
                // The field is empty, so we can just remove it
                prepareRowDeletion(fieldsMap.get(fieldId), ops);
            }
            return;
        }
        ContentProviderOperation.Builder builder;
        builder = prepareBuilder(id, fieldId, fieldsMap, ops);

        builder = builder.withValue(ContactsContract.Data.MIMETYPE,
                                    CommonDataKinds.Email.CONTENT_ITEM_TYPE);

        builder = builder.withValue(CommonDataKinds.Email.DATA, address);
        builder = builder.withValue(CommonDataKinds.Email.TYPE, type);
        
        ops.add(builder.build());
    }

    protected void prepareIm(long id, String im, int type,
            HashMap<String,List<Integer>> fieldsMap,
            List<ContentProviderOperation> ops)
    {
        String fieldId = createFieldId(CommonDataKinds.Im.CONTENT_ITEM_TYPE, type);
        if (StringUtil.isNullOrEmpty(im)) {
            if(fieldsMap != null) {
                // The field is empty, so we can just remove it
                prepareRowDeletion(fieldsMap.get(fieldId), ops);
            }
            return;
        }
        ContentProviderOperation.Builder builder;
        builder = prepareBuilder(id, fieldId, fieldsMap, ops);

        builder = builder.withValue(ContactsContract.Data.MIMETYPE,
                                    CommonDataKinds.Im.CONTENT_ITEM_TYPE);

        builder = builder.withValue(CommonDataKinds.Im.DATA, im);
        builder = builder.withValue(CommonDataKinds.Im.PROTOCOL,
                CommonDataKinds.Im.PROTOCOL_AIM);
        builder = builder.withValue(CommonDataKinds.Im.TYPE, type);

        ContentProviderOperation operation = builder.build();
        ops.add(operation);
    }


    protected void preparePhoto(Contact contact,
                                HashMap<String,List<Integer>> fieldsMap,
                                List<ContentProviderOperation> ops)
    {
        PersonalDetail pd = contact.getPersonalDetail();
        Photo photo = pd.getPhotoObject();
        if (photo != null) {
            byte[] photoBytes = photo.getImage();
            String fieldId = createFieldId(CommonDataKinds.Photo.CONTENT_ITEM_TYPE, 0);
            if (photoBytes != null && photoBytes.length>0) {
                ContentProviderOperation.Builder builder;
                builder = prepareBuilder(contact.getId(), fieldId, fieldsMap, ops);

                builder = builder.withValue(ContactsContract.Data.MIMETYPE,
                                            CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                builder = builder.withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1);
                builder = builder.withValue(CommonDataKinds.Photo.PHOTO, photoBytes);
                ContentProviderOperation operation = builder.build();
                ops.add(operation);
            } else if(fieldsMap != null) {
                // The photo is sent empty, we need to remove it
                prepareRowDeletion(fieldsMap.get(fieldId), ops);
                return;
            }
        }
    }

    protected void prepareOrganization(Contact contact,
                                       HashMap<String,List<Integer>> fieldsMap,
                                       List<ContentProviderOperation> ops)
    {
        BusinessDetail bd = contact.getBusinessDetail();

        Property companyProp = bd.getCompany();
        Property depProp     = bd.getDepartment();
        String location      = bd.getOfficeLocation();

        Title t = null;
        List<Title> titles = bd.getTitles();
        if (titles != null && titles.size() > 0) {
            t = titles.get(0);
        }

        String company    = null;
        String title      = null;
        String department = null;

        if (companyProp != null) {
            company = companyProp.getPropertyValueAsString();
        }
        if (t != null) {
            title = t.getPropertyValueAsString();
        }
        if (depProp != null) {
            department = depProp.getPropertyValueAsString();
        }

        String fieldId = createFieldId(CommonDataKinds.Organization.CONTENT_ITEM_TYPE, 0);

        String allFields[] = {company, title, department, location};
        if (isNull(allFields)) {
            // If all the properties are empty, then the server did not send this
            // field. We can simply return in this case.
            return;
        } else if (isFieldEmpty(allFields)) {
            if(fieldsMap != null) {
                // The field is empty, so we can just remove it
                prepareRowDeletion(fieldsMap.get(fieldId), ops);
            }
            return;
        } else {
            ContentProviderOperation.Builder builder;
            builder = prepareBuilder(contact.getId(), fieldId, fieldsMap, ops);

            builder = builder.withValue(ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Organization.CONTENT_ITEM_TYPE);

            if (company != null) {
                builder = builder.withValue(CommonDataKinds.Organization.COMPANY, company);
            }

            if (title != null) {
                builder = builder.withValue(CommonDataKinds.Organization.TITLE, title);
            }

            if (department != null) {
                builder = builder.withValue(CommonDataKinds.Organization.DEPARTMENT, department);
            }

            if (location != null) {
                builder = builder.withValue(CommonDataKinds.Organization.OFFICE_LOCATION, location);
            }

            // Add the type
            builder = builder.withValue(CommonDataKinds.Organization.TYPE, CommonDataKinds.Organization.TYPE_WORK);

            ContentProviderOperation operation = builder.build();
            ops.add(operation);
        }
    }


    protected void preparePostalAddress(Contact contact,
                                        HashMap<String,List<Integer>> fieldsMap,
                                        List<ContentProviderOperation> ops)
    {
        Address address = null;

        PersonalDetail pd = contact.getPersonalDetail();
        address = pd.getAddress();
        if (address != null) {
            preparePostalAddress(contact, address, CommonDataKinds.StructuredPostal.TYPE_HOME,
                                 fieldsMap, ops);
        }
        address = pd.getOtherAddress();
        if (address != null) {
            preparePostalAddress(contact, address, CommonDataKinds.StructuredPostal.TYPE_OTHER,
                                 fieldsMap, ops);
        }

        BusinessDetail bd = contact.getBusinessDetail();
        address = bd.getAddress();
        if (address != null) {
            preparePostalAddress(contact, address, CommonDataKinds.StructuredPostal.TYPE_WORK,
                                 fieldsMap, ops);
        }
    }

    protected void preparePostalAddress(Contact contact, Address address, int type,
                                        HashMap<String,List<Integer>> fieldsMap,
                                        List<ContentProviderOperation> ops)
    {
        String city = null;
        String country = null;
        String pobox = null;
        String poCode = null;
        String region = null;
        String street = null;
        String extAddress = null;

        Property cityProp = address.getCity();
        if (cityProp != null) {
            city = cityProp.getPropertyValueAsString();
        }
        Property countryProp = address.getCountry();
        if (countryProp != null) {
            country = countryProp.getPropertyValueAsString();
        }
        Property poboxProp = address.getPostOfficeAddress();
        if (poboxProp != null) {
            pobox = poboxProp.getPropertyValueAsString();
        }
        Property pocodeProp = address.getPostalCode();
        if (pocodeProp != null) {
            poCode = pocodeProp.getPropertyValueAsString();
        }
        Property regionProp = address.getState();
        if (regionProp != null) {
            region = regionProp.getPropertyValueAsString();
        }
        Property streetProp = address.getStreet();
        if (streetProp != null) {
            street = streetProp.getPropertyValueAsString();
        }
        Property extAddressProp = address.getExtendedAddress();
        if (extAddressProp != null) {
            extAddress = extAddressProp.getPropertyValueAsString();
        }

        String fieldId = createFieldId(CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE, type);

        String allFields[] = {city,country,pobox,poCode,region,street};
        if (isNull(allFields)) {
            // The server did not send this field, just ignore it
            return;
        } else if (isFieldEmpty(allFields)) {
            if(fieldsMap != null) {
                prepareRowDeletion(fieldsMap.get(fieldId), ops);
            }
            return;
        } else {
            ContentProviderOperation.Builder builder;
            builder = prepareBuilder(contact.getId(), fieldId, fieldsMap, ops);

            builder = builder.withValue(ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);

            if (city != null) {
                builder = builder.withValue(CommonDataKinds.StructuredPostal.CITY, city);
            }
            if (country != null) {
                builder = builder.withValue(CommonDataKinds.StructuredPostal.COUNTRY, country);
            }
            if (pobox != null) {
                builder = builder.withValue(CommonDataKinds.StructuredPostal.POBOX, pobox);
            }
            if (poCode != null) {
                builder = builder.withValue(CommonDataKinds.StructuredPostal.POSTCODE, poCode);
            }
            if (region != null) {
                builder = builder.withValue(CommonDataKinds.StructuredPostal.REGION, region);
            }
            if (street != null) {
                builder = builder.withValue(CommonDataKinds.StructuredPostal.STREET, street);
            }
            if (extAddress != null) {
                builder = builder.withValue(CommonDataKinds.StructuredPostal.NEIGHBORHOOD, extAddress);
            }
            builder = builder.withValue(CommonDataKinds.StructuredPostal.TYPE, type);
            ContentProviderOperation operation = builder.build();
            ops.add(operation);
        }
    }

    protected void prepareEvent(Contact contact,
                                HashMap<String,List<Integer>> fieldsMap,
                                List<ContentProviderOperation> ops)
    {
        PersonalDetail pd = contact.getPersonalDetail();
        
        prepareEvent(contact, pd.getBirthday(),
                CommonDataKinds.Event.TYPE_BIRTHDAY, fieldsMap, ops);
        prepareEvent(contact, pd.getAnniversary(),
                CommonDataKinds.Event.TYPE_ANNIVERSARY, fieldsMap, ops);
    }

    protected void prepareEvent(Contact contact, String eventDate, int type,
            HashMap<String,List<Integer>> fieldsMap,
            List<ContentProviderOperation> ops)
    {
        String fieldId = createFieldId(CommonDataKinds.Event.CONTENT_ITEM_TYPE, type);

        if (eventDate == null) {
            // The server did not send this field, just ignore it
            return;
        } else if ("".equals(eventDate)) {
            if(fieldsMap != null) {
                prepareRowDeletion(fieldsMap.get(fieldId), ops);
            }
            return;
        } else {
            // Insert date separator if needed
            if(eventDate.length() == 8) {
                StringBuffer date = new StringBuffer(10);
                date.append(eventDate.substring(0, 4)).append("-");
                date.append(eventDate.substring(4, 6)).append("-");
                date.append(eventDate.substring(6));
                eventDate = date.toString();
            }
            ContentProviderOperation.Builder builder;
            builder = prepareBuilder(contact.getId(), fieldId, fieldsMap, ops);

            builder = builder.withValue(ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Event.CONTENT_ITEM_TYPE);
            builder = builder.withValue(CommonDataKinds.Event.START_DATE,
                    eventDate);
            builder = builder.withValue(CommonDataKinds.Event.TYPE, type);

            ContentProviderOperation operation = builder.build();
            ops.add(operation);
        }
    }

    protected void prepareNote(Contact contact,
                               HashMap<String, List<Integer>> fieldsMap,
                               List<ContentProviderOperation> ops)
    {
        String fieldId = createFieldId(CommonDataKinds.Note.CONTENT_ITEM_TYPE, 0);

        List<Note> notes = contact.getNotes();

        if(notes != null) {
            for( Note noteField : notes) {
                String note = noteField.getPropertyValueAsString();

                if (note != null) {
                    // The device cannot display \r characters, so we remove them
                    // here and add them back to outgoing items
                    note = StringUtil.replaceAll(note, "\r\n", "\n");
                    note = StringUtil.replaceAll(note, "\r",   "\n");
                }

                if (StringUtil.isNullOrEmpty(note)) {
                    if(fieldsMap != null) {
                        // The field is empty, we shall remove it as the server
                        // wants to emtpy it
                        prepareRowDeletion(fieldsMap.get(fieldId), ops);
                    }
                    continue;
                }

                ContentProviderOperation.Builder builder;
                builder = prepareBuilder(contact.getId(), fieldId, fieldsMap, ops);

                builder = builder.withValue(ContactsContract.Data.MIMETYPE,
                                            CommonDataKinds.Note.CONTENT_ITEM_TYPE);
                builder = builder.withValue(CommonDataKinds.Note.NOTE, note);

                ContentProviderOperation operation = builder.build();
                ops.add(operation);
            }
        }
    }

    protected void prepareWebsite(Contact contact,
                                  HashMap<String,List<Integer>> fieldsMap,
                                  List<ContentProviderOperation> ops)
    {
        PersonalDetail pd = contact.getPersonalDetail();
        if (pd != null) {
            List<WebPage> websites = pd.getWebPages();
            if (websites != null) {
                prepareWebsite(contact, CommonDataKinds.Website.TYPE_HOME,
                        fieldsMap, websites, ops);
            }
        }
        BusinessDetail bd = contact.getBusinessDetail();
        if (bd != null) {
            List<WebPage> websites = bd.getWebPages();
            if (websites != null) {
                prepareWebsite(contact, CommonDataKinds.Website.TYPE_WORK,
                        fieldsMap, websites, ops);
            }
        }
    }

    private void prepareWebsite(Contact contact, int baseType,
                                HashMap<String,List<Integer>> fieldsMap,
                                List<WebPage> websites,
                                List<ContentProviderOperation> ops)
    {
        for( WebPage website : websites) {

            int type = baseType;
            
            if("WebPage".equals(website.getPropertyType())) {
                type = CommonDataKinds.Website.TYPE_OTHER;
            }

            String url = website.getPropertyValueAsString();

            String fieldId = createFieldId(CommonDataKinds.Website.CONTENT_ITEM_TYPE, type);

            if (StringUtil.isNullOrEmpty(url)) {
                if(fieldsMap != null) {
                    // The field is empty, we shall remove it as the server
                    // wants to emtpy it
                    prepareRowDeletion(fieldsMap.get(fieldId), ops);
                }
                continue;
            }

            ContentProviderOperation.Builder builder;
            builder = prepareBuilder(contact.getId(), fieldId, fieldsMap, ops);

            builder = builder.withValue(ContactsContract.Data.MIMETYPE,
                                        CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            builder = builder.withValue(CommonDataKinds.Website.URL, url);
            builder = builder.withValue(CommonDataKinds.Website.TYPE, type);

            ContentProviderOperation operation = builder.build();
            ops.add(operation);
        }
    }

    protected void prepareRelation(Contact contact,
                                HashMap<String,List<Integer>> fieldsMap,
                                List<ContentProviderOperation> ops)
    {
        PersonalDetail pd = contact.getPersonalDetail();

        prepareRelation(contact, pd.getChildren(),
                CommonDataKinds.Relation.TYPE_CHILD, fieldsMap, ops);
        prepareRelation(contact, pd.getSpouse(),
                CommonDataKinds.Relation.TYPE_SPOUSE, fieldsMap, ops);
    }

    protected void prepareRelation(Contact contact, String relName, int type,
            HashMap<String,List<Integer>> fieldsMap,
            List<ContentProviderOperation> ops)
    {
        String fieldId = createFieldId(CommonDataKinds.Relation.CONTENT_ITEM_TYPE, type);

        if (relName == null) {
            // The server did not send this field, just ignore it
            return;
        } else if ("".equals(relName)) {
            if(fieldsMap != null) {
                prepareRowDeletion(fieldsMap.get(fieldId), ops);
            }
            return;
        } else {
            ContentProviderOperation.Builder builder;
            builder = prepareBuilder(contact.getId(), fieldId, fieldsMap, ops);

            builder = builder.withValue(ContactsContract.Data.MIMETYPE,
                    CommonDataKinds.Relation.CONTENT_ITEM_TYPE);
            builder = builder.withValue(CommonDataKinds.Relation.NAME,
                    relName);
            builder = builder.withValue(CommonDataKinds.Relation.TYPE, type);

            ContentProviderOperation operation = builder.build();
            ops.add(operation);
        }
    }

    private String createFieldId(String mimeType, int type) {
        return createFieldId(new String[] { mimeType, Integer.toString(type) });
    }
    
    private String createFieldId(Object[] values) {
        if(values == null || values.length == 0) {
            return "";
        }
        StringBuffer res = new StringBuffer();
        Object value = values[0];
        if(value != null) {
            res.append(value.toString());
        }
        for(int i=1; i<values.length; i++) {
            value = values[i];
            if(value != null) {
                res.append("-").append(values[i].toString());
            }
        }
        return res.toString();
    }

    private boolean isFieldEmpty(String allFields[]) {
        boolean empty = true;

        for(int i=0;i<allFields.length;++i) {
            String field = allFields[i];
            if (!StringUtil.isNullOrEmpty(field)) {
                empty = false;
                break;
            }
        }
        return empty;
    }

    private boolean isNull(Object objs[]) {
        for(int i=0;i<objs.length;++i) {
            if (objs[i] != null) {
                return false;
            }
        }
        return true;
    }


    private void prepareRowDeletion(List<Integer> rows, List<ContentProviderOperation> ops) {
        if (rows != null) {
            ContentProviderOperation.Builder builder;
            for(int rowId: rows) {
                Uri uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, rowId);
                uri = addCallerIsSyncAdapterFlag(uri);
                builder = ContentProviderOperation.newDelete(uri);
                ops.add(builder.build());
            }
        }
    }

    private Uri addCallerIsSyncAdapterFlag(Uri uri) {
        if(callerIsSyncAdapter) {
            Uri.Builder b = uri.buildUpon();
            b.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true");
            return b.build();
        } else {
            return uri;
        }
    }
}
