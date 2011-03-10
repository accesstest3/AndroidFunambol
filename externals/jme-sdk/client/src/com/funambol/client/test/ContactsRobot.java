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

package com.funambol.client.test;

import java.io.ByteArrayOutputStream;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.util.StringUtil;
import com.funambol.util.Log;

public abstract class ContactsRobot extends Robot {
   
    private static final String TAG_LOG = "ContactsRobot";

    protected static final char FOLDING_INDENT_CHAR = ' ';

    protected long currentContactId = -1;

    protected long incrementalServerItemkey = 10000000;

    protected String contactAsVcard = null;

    protected BasicRobot basicRobot;

    protected AppSyncSourceManager appSourceManager;

    public ContactsRobot(AppSyncSourceManager appSourceManager) {
        this.appSourceManager = appSourceManager;
    }

    public void importContactOnServer(String filename) throws Exception {
        this.contactAsVcard = TestFileManager.getInstance().getFile(BasicScriptRunner.getBaseUrl() + "/" + filename);
    }

    public void saveContactOnServer(CheckSyncClient client) throws Throwable {

        CheckSyncSource source = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CONTACTS);
        SyncItem item = new SyncItem(Long.toString(incrementalServerItemkey++));
        item.setContent(getCurrentContactVCard().getBytes());
        
        if(currentContactId != -1) {
            item.setKey(Long.toString(currentContactId));
            source.updateItemFromOutside(item);
        } else {
            source.addItemFromOutside(item);
        }

        // Reset current contact
        currentContactId = -1;
        contactAsVcard = null;
    }

    public void deleteContactOnServer(String firstname, String lastname,
            CheckSyncClient client) throws Throwable {
        CheckSyncSource source = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CONTACTS);
        String itemKey = findContactKeyOnServer(firstname, lastname, client);
        source.deleteItemFromOutside(itemKey);
    }

    public void deleteAllContactsOnServer(CheckSyncClient client) throws Throwable {
        CheckSyncSource source = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CONTACTS);
        source.deleteAllFromOutside();
    }

    public void setContactAsVCard(String vCard) throws Throwable{
        String[] sep = new String[]{"\\r\\n"};
        String[] parts = StringUtil.split(vCard, sep);

        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        for (int i=0;i<parts.length;i++){
            ostream.write(parts[i].getBytes());
            ostream.write("\r\n".getBytes());
        }
        contactAsVcard = ostream.toString();
        ostream.close();
    }

    public void resetContacts(CheckSyncClient client) throws Throwable {
        CheckSyncSource source = client.getSyncSource(
                CheckSyncClient.SOURCE_NAME_CONTACTS);
        // Remove locally
        deleteAllContacts();
        // Clean the check sync client and perform a refresh from client to
        // server
        client.clear(source);
    }

    
    public void setContactFromServer(String vCard) throws Throwable {

        Enumeration sources = getAppSyncSourceManager().getWorkingSources();
        AppSyncSource appSource = null;

        while(sources.hasMoreElements()) {
            appSource = (AppSyncSource)sources.nextElement();
            if (appSource.getId() == AppSyncSourceManager.CONTACTS_ID) {
                break;
            }
        }

        // We add an item via the SyncSource
        SyncSource source = appSource.getSyncSource();
        SyncItem item = new SyncItem("guid", "text/x-vcard", SyncItem.STATE_NEW, null);
        item.setContent(vCard.getBytes("UTF-8"));

        source.addItem(item);
    }

    /**
     * Order the vCard item fields alphabetically.
     * @param vcard
     * @return
     */
    protected String orderVCard(String vcard) {
        return orderVCard(vcard, null, null);
    }

    protected AppSyncSourceManager getAppSyncSourceManager() {
        return appSourceManager;
    }

    protected String orderVCard(String vcard, String supportedFields[], Hashtable supportedValues) {

        Log.trace(TAG_LOG, "Ordering vcard: " + vcard);
        Vector fieldsAl = getFieldsVector(vcard);

        // order the fields array list
        String result = "";
        String[] fields = StringUtil.getStringArray(fieldsAl);
        for(int i=0; i<fields.length; i++) {
            for(int j=fields.length-1; j>i; j--) {
                if(fields[j].compareTo(fields[j-1])<0) {
                    String temp = fields[j];
                    fields[j] = fields[j-1];
                    fields[j-1] = temp;
                }
            }

            // Trim any leading/trailing white space
            fields[i] = fields[i].trim();

            // Exclude last occurrence of ";" from all fields
            while (fields[i].endsWith(";")) {
                fields[i] = new String(fields[i].substring(0, fields[i].length()-1));
            }
            
            // Order ENCODING and CHARSET parameters
            int index = fields[i].indexOf("ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8");
            int length = "ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8".length();
            if(index != -1) {
                StringBuffer field = new StringBuffer();
                field.append(fields[i].substring(0, index));
                field.append("CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE");
                field.append(fields[i].substring(index+length));
                fields[i] = field.toString();
            }
            
            // Exclude empty fields and fields which are not supported by the
            // device
            if(!fields[i].endsWith(":")) {
                if (supportedFields != null) {
                    int fieldNameIdx = fields[i].indexOf(":");
                    if (fieldNameIdx != -1) {
                        String fieldName = fields[i].substring(0, fieldNameIdx);

                        for(int j=0;j<supportedFields.length;++j) {
                        
                            String supportedFieldWithEncoding = supportedFields[j] + 
                                ";CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE";
                            if (fieldName.equals(supportedFields[j]) || 
                                fieldName.equals(supportedFieldWithEncoding)) {

                                if (fieldNameIdx + 1 < fields[i].length()) {
                                    String value = fields[i].substring(fieldNameIdx + 1);
                                    value = cleanField(fieldName, value, supportedValues);

                                    // Exclude last occurrence of ";" from all fields
                                    while (value.endsWith(";")) {
                                        value = new String(value.substring(0, value.length()-1));
                                    }

                                    result += fieldName + ":" + value + "\r\n";
                                } else {
                                    result += fields[i] + "\r\n";
                                }
                                break;
                            }
                        }
                    } else {
                        result += fields[i] + "\r\n";
                    }
                } else {
                    result += fields[i] + "\r\n";
                }
            }
        }
        
        // Replace all the encoded \r\n occurences with \n
        result = StringUtil.replaceAll(result, "=0D=0A", "=0A");
        
        Log.trace(TAG_LOG, "Ordered vcard: " + result);
        return result;
    }

    private String cleanField(String fieldName, String value, Hashtable supportedValues) {
        String filter = (String)supportedValues.get(fieldName); 
        if (filter != null) {
            Log.trace(TAG_LOG, "Found filter for field: " + fieldName + "," + filter);
            String values[] = StringUtil.split(value, ";");
            String filters[] = StringUtil.split(filter, ";");
            String res = "";

            for(int i=0;i<values.length;++i) {
                String v = values[i];
                boolean include;
                if (i<filters.length) {
                    String f = filters[i];
                    if (f.length() > 0) {
                        include = true;
                    } else {
                        include = false;
                    }
                } else {
                    include = true;
                }

                if (include) {
                    res = res + v;
                }
                if (i != values.length - 1) {
                    res = res + ";";
                }
            }
            return res;

        } else {
            return value;
        }
    }

    private Vector getFieldsVector(String vcard) {

        String sep[] = {"\r\n"};
        String lines[] = StringUtil.split(new String(vcard), sep);

        Vector fieldsAl = new Vector();
        String field = "";
        for(int i=0;i<lines.length;++i) {
            String line = lines[i];
            if(line.length() > 0 && line.charAt(0) == FOLDING_INDENT_CHAR) {
                // this is a multi line field
                field += line.substring(1); // cut the indent char
            } else {
                if(!field.equals("")) {
                    fieldsAl.addElement(field);
                }
                field = line;
            }
        }
        // add the latest field
        fieldsAl.addElement(field);

        return fieldsAl;
    }

    protected void checkContactAsVCard(String vcard) throws Throwable {
        String currentVCard = getCurrentContactVCard();
        // The incoming vcard has \r\n as strings, we shall replace them
        vcard = StringUtil.replaceAll(vcard, "\\r\\n", "\r\n");

        Log.trace(TAG_LOG, "vcard.length=" + vcard.length());
        Log.trace(TAG_LOG, "currentVCard.length=" + currentVCard.length());

        assertTrue(currentVCard, vcard, "VCard mismatch");
    }

    public abstract void createEmptyContact() throws Throwable;
    public abstract void setContactField(String field, String value) throws Throwable;

    public abstract void loadContact(String firstName, String lastName) throws Throwable;
    public abstract void saveContact() throws Throwable;
    public abstract void deleteContact(String firstname, String lastname) throws Throwable;
    public abstract void deleteAllContacts() throws Throwable;

    public abstract void checkNewContact(String firstname, String lastname,
            CheckSyncClient client, boolean checkContent) throws Throwable;

    public abstract void checkUpdatedContact(String firstname, String lastname,
            CheckSyncClient client, boolean checkContent) throws Throwable;

    public abstract void checkDeletedContact(String firstname, String lastname,
            CheckSyncClient client) throws Throwable;

    public abstract void checkNewContactOnServer(String firstname, String lastname,
            CheckSyncClient client, boolean checkContent) throws Throwable;

    public abstract void checkUpdatedContactOnServer(String firstname, String lastname,
            CheckSyncClient client, boolean checkContent) throws Throwable;

    public abstract void checkDeletedContactOnServer(String firstname, String lastname,
            CheckSyncClient client) throws Throwable;

    public abstract void loadContactOnServer(String firstName, String lastName,
            CheckSyncClient client) throws Throwable;

    protected abstract String getCurrentContactVCard() throws Throwable;

    protected abstract String findContactKeyOnServer(String firstName, String lastName,
            CheckSyncClient client) throws Throwable;

}
