/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2003 - 2009 Funambol, Inc.
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

package com.funambol.android;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import android.app.Instrumentation;
import android.provider.ContactsContract;

import com.funambol.common.pim.model.common.Property;
import com.funambol.common.pim.model.contact.Photo;
import com.funambol.android.source.pim.contact.Contact;
import com.funambol.android.source.pim.contact.ContactManager;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.util.Log;
import com.funambol.platform.FileAdapter;

import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.test.CheckSyncClient;
import com.funambol.client.test.CheckSyncSource;
import com.funambol.client.test.ClientTestException;
import com.funambol.client.test.ContactsCommandRunner;
import com.funambol.client.test.ContactsRobot;
import com.funambol.client.test.BasicRobot;

public class AndroidContactsRobot extends ContactsRobot {
   
    private static final String LOG_TAG = "AndroidContactsRobot";

    private Instrumentation instrumentation = null;

    private ContactManager cm = null;
    private Contact      currentContact = null;
    private StringBuffer currentContactVCard = null;

    // Associates script field names to vCard fields
    private Hashtable<String,String> vCardFields = new Hashtable<String,String>();

    public AndroidContactsRobot(Instrumentation instrumentation, BasicRobot basicRobot) {

        // The app source manager is not available yet, it will be set later
        super(null);

        this.basicRobot = basicRobot;

        this.instrumentation = instrumentation;

        // Init vCard fields
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_DISPLAY_NAME,  "FN");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_NICK_NAME,     "NICKNAME");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_TEL_HOME,      "TEL;VOICE;HOME");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_TEL_WORK,      "TEL;VOICE;WORK");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_TEL_OTHER,     "TEL;VOICE");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_TEL_OTHER2,    "TEL;PREF;VOICE");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_TEL_CELL,      "TEL;CELL");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_TEL_PAGER,     "TEL;PAGER");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_TEL_FAX_HOME,  "TEL;FAX;HOME");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_TEL_FAX_WORK,  "TEL;FAX;WORK");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_TEL_COMPANY,   "TEL;WORK;PREF");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_TEL_OTHER_FAX, "TEL;FAX");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_EMAIL_HOME,    "EMAIL;INTERNET;HOME");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_EMAIL_WORK,    "EMAIL;INTERNET;WORK");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_EMAIL_OTHER,   "EMAIL;INTERNET");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_EMAIL_IM,      "EMAIL;INTERNET;HOME;X-FUNAMBOL-INSTANTMESSENGER");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_ADR_OTHER,     "ADR");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_ADR_HOME,      "ADR;HOME");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_ADR_WORK,      "ADR;WORK");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_WEB,           "URL");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_WEB_HOME,      "URL;HOME");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_WEB_WORK,      "URL;WORK");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_BDAY,          "BDAY");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_ANNIVERSARY,   "X-ANNIVERSARY");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_CHILDREN,      "X-FUNAMBOL-CHILDREN");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_SPOUSE,        "X-SPOUSE");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_TITLE,         "TITLE");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_ORGANIZATION,  "ORG");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_NOTE,          "NOTE");
        vCardFields.put(ContactsCommandRunner.CONTACT_FIELD_PHOTO,         "PHOTO");
    }

    public ContactManager getContactManager() {
        if(cm == null) {
            // Init contact manager
            cm = new ContactManager(instrumentation.getContext(), false);
        }
        return cm;
    }


    public void createEmptyContact() throws Throwable {
        currentContact = new Contact();
        initEmptyContactVCard();
    }

    private void initEmptyContactVCard() {
        currentContactVCard = new StringBuffer();
        currentContactVCard.append("BEGIN:VCARD").append("\r\n");
        currentContactVCard.append("VERSION:2.1").append("\r\n");
    }

    public void setContactField(String field, String value) throws Throwable {
        if(currentContact == null) {
            throw new ClientTestException("You have to inizialize the contact before edotong it");
        }
        if(ContactsCommandRunner.CONTACT_FIELD_FIRST_NAME.equals(field)) {
            currentContact.getName().setFirstName(new Property(value));
        } else if(ContactsCommandRunner.CONTACT_FIELD_LAST_NAME.equals(field)) {
            currentContact.getName().setLastName(new Property(value));
        } else if(ContactsCommandRunner.CONTACT_FIELD_MIDDLE_NAME.equals(field)) {
            currentContact.getName().setMiddleName(new Property(value));
        } else if(ContactsCommandRunner.CONTACT_FIELD_PREFIX_NAME.equals(field)) {
            currentContact.getName().setSalutation(new Property(value));
        } else if(ContactsCommandRunner.CONTACT_FIELD_SUFFIX_NAME.equals(field)) {
            currentContact.getName().setSuffix(new Property(value));
        } else if(ContactsCommandRunner.CONTACT_FIELD_NICK_NAME.equals(field)) {
            currentContact.getName().setNickname(new Property(value));
        } else if(ContactsCommandRunner.CONTACT_FIELD_DISPLAY_NAME.equals(field)) {
            if(AndroidCustomization.getInstance().isDisplayNameSupported()) {
                currentContact.getName().setDisplayName(new Property(value));
            }
        } else if(ContactsCommandRunner.CONTACT_FIELD_PHOTO.equals(field)
                && value.length()>0) {
            
            Photo photo = new Photo();

            FileAdapter fa = new FileAdapter(value, true);
            InputStream is = fa.openInputStream();
            
            int bytesCount = 0;
            while(is.read() != -1) {
                bytesCount++;
            }

            byte[] bytes = new byte[bytesCount];
            is = fa.openInputStream();
            is.read(bytes);
            
            photo.setImage(bytes);
            currentContact.getPersonalDetail().setPhotoObject(photo);
        } else {
            String vCardField = vCardFields.get(field);
            if(vCardField == null) {
                throw new IllegalArgumentException("Unknown field: " + field);
            } else {
                currentContactVCard.append(vCardField).append(":")
                        .append(value).append("\r\n");
            }
        }
    }

    public void loadContact(String firstName, String lastName) throws Throwable {
        currentContactId = findContactKey(firstName, lastName);
        currentContact = getContactManager().load(currentContactId);
        initEmptyContactVCard();
    }

    public void saveContact() throws Throwable {

        Contact contact = new Contact();

        // Finish contact formatting
        currentContactVCard.append("END:VCARD").append("\r\n");
        contact.setVCard(currentContactVCard.toString().getBytes());

        contact.setName(currentContact.getName());
        contact.getPersonalDetail().setPhotoObject(currentContact.
                getPersonalDetail().getPhotoObject());

        // Check if firstname and lastname are set
        if(contact.getName().getFirstName().getPropertyValue() == null ||
           contact.getName().getLastName().getPropertyValue() == null) {
            throw new ClientTestException("You must set firstname and lastname before saving the contact");
        }

        if(currentContactId != -1) {
            getContactManager().update(currentContactId, contact);
        } else {
            getContactManager().add(contact);
        }
        
        // Reset current contact
        currentContact = null;
        currentContactVCard = null;
        currentContactId = -1;
    }

    public void deleteContact(String firstname, String lastname) throws Throwable {
        instrumentation.getTargetContext().getContentResolver().delete(
                ContactsContract.RawContacts.CONTENT_URI,
                ContactsContract.RawContacts._ID+"="+
                findContactKey(firstname, lastname), null);
    }

    public void deleteAllContacts() throws Throwable {
        instrumentation.getTargetContext().getContentResolver().delete(
                ContactsContract.RawContacts.CONTENT_URI, null, null);
    }

    public void checkNewContact(String firstname, String lastname,
            CheckSyncClient client, boolean checkContent) throws Throwable {

        CheckSyncSource source = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CONTACTS);

        String key = findContactKeyOnServer(firstname, lastname, client);

        Contact remoteContact = new Contact();

        SyncItem item = new SyncItem(key);
        item = source.getItemContent(item);
        remoteContact.setVCard(item.getContent());

        Contact localContact = getContactManager().load(findContactKey(firstname, lastname));

        checkContact(localContact, remoteContact, checkContent);
    }

    public void checkUpdatedContact(String firstname, String lastname,
            CheckSyncClient client, boolean checkContent) throws Throwable {
        checkNewContact(firstname, lastname, client, checkContent);
    }

    public void checkDeletedContact(String firstname, String lastname, 
            CheckSyncClient client) throws Throwable {

        boolean found = false;
        try {
            findContactKey(firstname, lastname);
            found = true;
        } catch(ClientTestException ex) {
            // OK Item not found
        }
        if(found) {
            throw new ClientTestException("Deleted item found: " + firstname +
                    " " + lastname);
        }
    }

    protected void checkContactAsVCard(String vcard) throws Throwable {
    }


    private void checkContact(Contact localContact, Contact remoteContact,
            boolean checkContent) throws Throwable {
        if(checkContent) {
            assertEquals(localContact, remoteContact, "Contacts mismatch");
        }
    }

    public void checkNewContactOnServer(String firstname, String lastname,
            CheckSyncClient client, boolean checkContent) throws Throwable {

        Contact localContact = getContactManager().load(findContactKey(firstname, lastname));
        Enumeration items = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CONTACTS).getAddedItems();

        checkContactOnServer(firstname, lastname, checkContent, localContact, items);
    }

    public void checkUpdatedContactOnServer(String firstname, String lastname,
            CheckSyncClient client, boolean checkContent) throws Throwable {

        Contact localContact = getContactManager().load(findContactKey(firstname, lastname));
        Enumeration items = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CONTACTS).getUpdatedItems();

        checkContactOnServer(firstname, lastname, checkContent, localContact, items);
    }

    public void checkDeletedContactOnServer(String firstname, String lastname,
            CheckSyncClient client) throws Throwable {

        boolean found = false;
        try {
            findContactKeyOnServer(firstname, lastname, client);
            found = true;
        } catch(ClientTestException ex) {
            // OK Item not found
        }
        if(found) {
            throw new ClientTestException("Deleted item found on server: " + firstname +
                    " " + lastname);
        }
    }

    private void checkContactOnServer(String firstname, String lastname,
            boolean checkContent, Contact localContact, Enumeration items) throws Throwable {

        while(items.hasMoreElements()) {

            SyncItem syncItem = (SyncItem)items.nextElement();
            byte[] remote = syncItem.getContent();

            Contact remoteContact = new Contact();

            remoteContact.setVCard(remote);
            
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            remoteContact.toVCard(os, true);
            byte[] c1_ba = os.toByteArray();

            if(remoteContact.getName().getFirstName().getPropertyValueAsString().equals(firstname) &&
               remoteContact.getName().getLastName().getPropertyValueAsString().equals(lastname)) {
                if(checkContent) {
                    assertEquals(localContact, remoteContact, "Contacts mismatch");
                }
                return;
            }
        }
        throw new ClientTestException("Can't find contact on server: " + firstname + " " + lastname);
    }

    public void loadContactOnServer(String firstName, String lastName,
            CheckSyncClient client) throws Throwable {

        CheckSyncSource source = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CONTACTS);

        String key = findContactKeyOnServer(firstName, lastName, client);
        currentContactId = Long.parseLong(key);
        currentContact = new Contact();

        SyncItem item = new SyncItem(key);
        item = source.getItemContent(item);
        currentContact.setVCard(item.getContent());

        initEmptyContactVCard();
    }

    @Override
    public void saveContactOnServer(CheckSyncClient client) throws Throwable {

        // Check if firstname and lastname are set
        if(currentContact.getName().getFirstName().getPropertyValue() == null ||
           currentContact.getName().getLastName().getPropertyValue() == null) {
            throw new ClientTestException("You must set firsname and lastname before saving the contact");
        }

        // Finish contact formatting
        currentContactVCard.append("END:VCARD").append("\r\n");

        CheckSyncSource source = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CONTACTS);

        SyncItem item = new SyncItem(Long.toString(incrementalServerItemkey++));

        currentContact.setVCard(currentContactVCard.toString().getBytes());

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        currentContact.toVCard(os, true);

        item.setContent(os.toByteArray());
        
        if(currentContactId != -1) {
            item.setKey(Long.toString(currentContactId));
            source.updateItemFromOutside(item);
        } else {
            source.addItemFromOutside(item);
        }

        // Reset current contact
        currentContact = null;
        currentContactVCard = null;
        currentContactId = -1;
    }

    protected AppSyncSourceManager getAppSyncSourceManager() {
        if (appSourceManager == null) {
            AppInitializer appInitializer = AppInitializer.getInstance(instrumentation.getContext());
            appSourceManager = appInitializer.getAppSyncSourceManager();
        }
        return appSourceManager;
    }

    protected String getCurrentContactVCard() throws Throwable {
        if (currentContactVCard != null) {
            return currentContactVCard.toString();
        } else if (currentContact != null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            currentContact.toVCard(os, true);
            return os.toString();
        } else {
            throw new ClientTestException("No current contact");
        }
    }

    private long findContactKey(String firstName, String lastName) throws Throwable {
        Enumeration allkeys = getContactManager().getAllKeys();
        while(allkeys.hasMoreElements()) {
            long key = Long.parseLong((String)allkeys.nextElement());
            Contact contact = getContactManager().load(key);
            if(contact.getName().getFirstName().getPropertyValueAsString().equals(firstName) &&
               contact.getName().getLastName().getPropertyValueAsString().equals(lastName)) {
               return key;
            }
        }
        throw new ClientTestException("Can't find contact: " + firstName + " " + lastName);
    }

    protected String findContactKeyOnServer(String firstName, String lastName,
            CheckSyncClient client) throws Throwable {

        Hashtable<String,SyncItem> allItems = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CONTACTS).getAllItems();

        Enumeration<SyncItem> allElements = allItems.elements();
        
        while(allElements.hasMoreElements()) {
            SyncItem item = allElements.nextElement();
            Contact contact = new Contact();
            contact.setVCard(item.getContent());
            if(contact.getName().getFirstName().getPropertyValueAsString().equals(firstName) &&
               contact.getName().getLastName().getPropertyValueAsString().equals(lastName)) {
               return item.getKey();
            }
        }
        throw new ClientTestException("Can't find contact on server: " + firstName + " " + lastName);
    }

    private void assertEquals(Contact c1, Contact c2, String msg) throws ClientTestException {

        String expectedStr = null;
        String resultStr = null;

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            c1.toVCard(os, true);
            byte[] c1_ba = os.toByteArray();

            os = new ByteArrayOutputStream();
            c2.toVCard(os, true);
            byte[] c2_ba = os.toByteArray();

            expectedStr = orderVCard(new String(c1_ba));
            resultStr = orderVCard(new String(c2_ba));

            assertTrue(resultStr.equals(expectedStr), msg);

        } catch(ClientTestException ex) {
            Log.error(LOG_TAG, "Expected: " + expectedStr + " -- Found: " + resultStr);
            throw new ClientTestException(msg);
        } catch(Exception ex) {
            throw new ClientTestException(msg);
        }
    }
}
