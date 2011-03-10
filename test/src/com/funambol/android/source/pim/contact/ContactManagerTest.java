/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2008 Funambol, Inc.
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

import java.io.ByteArrayOutputStream;
import java.util.*;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract;
import android.test.AndroidTestCase;

import com.funambol.util.Log;
import com.funambol.util.StringUtil;
import com.funambol.util.AndroidLogAppender;
import com.funambol.android.AndroidBaseTest;

public class ContactManagerTest extends AndroidBaseTest {

    private ContentResolver resolver;

    private ContactManager cm;

    public void setUp() throws Exception {
        super.setUp();

        Log.initLog(new AndroidLogAppender("ContactManagerTest"), Log.TRACE);

        resolver = getContext().getContentResolver();
        cm = new ContactManager(getContext());
    }

    public void tearDown() {
        resolver.delete(RawContacts.CONTENT_URI, null, null);
        resolver.delete(ContactsContract.Data.CONTENT_URI, null, null);
    }
    
    
    public void testAddLoad() throws Exception {
        Log.info("testAddLoad start");

        Contact c = new Contact();

        Vector suppFields = Contact.getSupportedFields();

        byte[] vcard = getSampleVCard(suppFields, suppFields, true);
        assertEquals(getVCardFieldsCount(vcard), suppFields.size()+4);

        c.setVCard(vcard);

        // Add the new contact
        long id = cm.add(c);

        // Load the same contact
        c = cm.load(id);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        c.toVCard(os, true);
        
        byte[] result = os.toByteArray();

        assertVCardEquals(result, vcard);
        
        Log.info("testAddLoad end");
    }

    public void testUpdate() throws Exception {
        Log.info("testUpdate start");

        Contact c = new Contact();
        
        Vector suppFields = Contact.getSupportedFields();

        byte[] vcard = getSampleVCard(suppFields, suppFields, true);
        byte[] updvcard = getSampleUpdatedVCard(suppFields, suppFields, true);

        c.setVCard(vcard);

        // Add the new contact
        long id = cm.add(c);

        c.setVCard(updvcard);

        cm.update(id, c);

        // Load the same contact
        c = cm.load(id);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        c.toVCard(os, true);

        byte[] result = os.toByteArray();

        assertVCardEquals(result, updvcard);

        Log.info("testUpdate end");
    }

    public void testUpdate_EmptyFields() throws Exception {
        Log.info("testUpdate_EmptyFields start");

        Contact c = new Contact();

        Vector suppFields = Contact.getSupportedFields();

        Vector subFields = (Vector)suppFields.clone();

        byte[] vcard = getSampleVCard(suppFields, suppFields, true);

        c.setVCard(vcard);

        // Add the new contact
        long id = cm.add(c);

        byte[] updvcard;
        for(int i=0; i<suppFields.size()+1; i++) {
            
            updvcard = getSampleUpdatedVCard(subFields, suppFields, true);

            Log.trace("updated vcard: " + new String(updvcard));

            Contact upd = new Contact();
            upd.setVCard(updvcard);

            cm.update(id, upd);

            // Load the same contact
            c = cm.load(id);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            c.toVCard(os, true);

            byte[] result = os.toByteArray();

            assertVCardEquals(result, updvcard);

            // Remove the last field
            if(!subFields.isEmpty()) subFields.remove(subFields.size()-1);
        }
        Log.info("testUpdate_EmptyFields end");
    }

    public void testUpdate_NullFields() throws Exception {
        Log.info("testUpdate_NullFields start");

        Vector suppFields = Contact.getSupportedFields();

        Vector subFields = (Vector)suppFields.clone();

        for(int i=0; i<suppFields.size()+1; i++) {

            byte[] vcard       = getSampleVCard(suppFields, suppFields, true);
            byte[] updvcard    = getSampleUpdatedVCard(subFields, suppFields, false);
            byte[] mergedvcard = getSampleMergedVCard(subFields, suppFields);

            Log.trace("vcard: " + new String(vcard));
            Log.trace("updated vcard: " + new String(updvcard));
            Log.trace("merged vcard: "  + new String(mergedvcard));

            Contact c = new Contact();
            c.setVCard(vcard);

            // Add the new contact
            long id = cm.add(c);

            Contact upd = new Contact();
            upd.setVCard(updvcard);

            cm.update(id, upd);

            // Load the same contact
            c = cm.load(id);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            c.toVCard(os, true);

            byte[] result = os.toByteArray();

            assertVCardEquals(result, mergedvcard);

            // Remove the last field
            if(!subFields.isEmpty()) subFields.remove(subFields.size()-1);
        }

        Log.info("testUpdate_NullFields end");
    }

    public void testDelete() throws Exception {
        Log.info("testDelete start");

        Contact c = new Contact();

        Vector suppFields = Contact.getSupportedFields();

        byte[] vcard = getSampleVCard(suppFields, suppFields, true);
        assertEquals(getVCardFieldsCount(vcard), suppFields.size()+4);

        c.setVCard(vcard);

        long[] ids = new long[5];

        // Add some contacts
        for(int i=0; i<ids.length; i++) {
            ids[i] = cm.add(c);
        }

        // Check if they exist
        for(int i=0; i<ids.length; i++) {
            assertTrue(cm.exists(ids[i]));
        }

        // Delete all
        for(int i=0; i<ids.length; i++) {
            cm.delete(ids[i]);
        }

        // Check if they exist
        for(int i=0; i<ids.length; i++) {
            assertTrue(!cm.exists(ids[i]));

            //Ensure there's no more rows in RawContacts table
            Cursor cur = resolver.query(ContactsContract.RawContacts.CONTENT_URI, null,
                    ContactsContract.RawContacts._ID+"="+ids[i],
                    null, null);

            assertTrue(cur.getCount() == 0);
            
            //Ensure there's no more rows in Data table
            cur = resolver.query(ContactsContract.Data.CONTENT_URI, null,
                    ContactsContract.Data.RAW_CONTACT_ID+"="+ids[i],
                    null, null);

            assertTrue(cur.getCount() == 0);
        }

        Log.info("testDelete end");
    }

    public void testGetAllKeys() throws Exception {
        Log.info("testGetAllKeys start");

        Contact c = new Contact();

        Vector suppFields = Contact.getSupportedFields();

        byte[] vcard = getSampleVCard(suppFields, suppFields, true);
        assertEquals(getVCardFieldsCount(vcard), suppFields.size()+4);

        c.setVCard(vcard);

        long[] ids = new long[5];
        // Add some contacts
        for(int i=0; i<ids.length; i++) {
            ids[i] = cm.add(c);
        }

        // Check if they exist
        for(int i=0; i<ids.length; i++) {
            assertTrue(cm.exists(ids[i]));
        }

        boolean[] found = new boolean[ids.length];
        for(int i=0; i<found.length; i++) {
            found[i] = false;
        }
        Enumeration en = cm.getAllKeys();
        int count = 0;
        while(en.hasMoreElements()) {
            count++;
            String key = (String)en.nextElement();
            for(int i=0; i<ids.length; i++) {
                if(Long.parseLong(key) == ids[i]) {
                    found[i] = true;
                    break;
                }
            }
        }
        assertEquals(count, ids.length);
        for(int i=0; i<found.length; i++) {
            assertTrue(found[i]);
        }

        Log.info("testGetAllKeys end");
    }

    public void testExists() throws Exception {
        Log.info("testExists start");

        Contact c = new Contact();

        Vector suppFields = Contact.getSupportedFields();

        byte[] vcard = getSampleVCard(suppFields, suppFields, true);
        c.setVCard(vcard);

        // Add the new contact
        long id = cm.add(c);
        assertTrue(cm.exists(id));

        cm.delete(id);
        assertTrue(!cm.exists(id));

        Log.info("testExists end");
    }


    private void assertVCardEquals(byte[] result, byte[] expected) {

        String resultStr = orderVCard(new String(result));
        String expectedStr = orderVCard(new String(expected));

        assertEquals(resultStr, expectedStr);
    }

    private int getVCardFieldsCount(byte[] vcard) {
        return getFieldsVector(new String(vcard)).size();
    }

    /**
     * Order the vCard item fields alphabetically.
     */
    private String orderVCard(String vcard) {

        Vector fields_al = getFieldsVector(vcard);

        // order the fields array list
        String result = "";
        String[] fields = StringUtil.getStringArray(fields_al);
        for(int i=0; i<fields.length; i++) {
            for(int j=fields.length-1; j>i; j--) {
                if(fields[j].compareTo(fields[j-1])<0) {
                    String temp = fields[j];
                    fields[j] = fields[j-1];
                    fields[j-1] = temp;
                }
            }
            result += fields[i] + "\r\n";
        }
        return result;
    }

    private Vector getFieldsVector(String vcard) {
        String sep[] = {"\r\n"};
        String lines[] = StringUtil.split(new String(vcard), sep);

        Vector fields_al = new Vector();
        String field = "";
        for(int i=0;i<lines.length;++i) {
            String line = lines[i];
            if(line.length() > 0 && line.charAt(0) == com.funambol.common.pim.Utils.FOLDING_INDENT_CHAR) {
                // this is a multi line field
                field += line.substring(1); // cut the indent char
            } else {
                if(!field.equals("")) {
                    fields_al.add(field);
                }
                field = line;
            }
        }
        // add the latest field
        fields_al.add(field);

        return fields_al;
    }


    private byte[] getSampleVCard(Vector<String> fields, Vector<String> allFields,
            boolean formatAllFields) {
        
        StringBuffer res = new StringBuffer();

        res.append("BEGIN:VCARD").append("\r\n");
        res.append("VERSION:2.1").append("\r\n");

        for(int i=0; i<allFields.size(); i++) {
            String field = allFields.elementAt(i);
            String value = "";
            if(fields.contains(field)) {
                if(field.equals("N")) {
                    value = "lastname;firstname;midlle;salutation;suffix";
                } else if(field.startsWith("NICKNAME")) {
                    value = "nickname";
                } else if(field.startsWith("TEL")) {
                    value = "+1234567890" + i;
                } else if(field.startsWith("EMAIL")) {
                    value = "email@email" + i + ".com"; 
                } else if(field.startsWith("ADR")) {
                    value = "address" + i + "pobox;ext-address;street;city;state;code;country";
                } else if(field.startsWith("URL")) {
                    value = "web.url" + i + ".com";
                } else if(field.startsWith("BDAY") || field.startsWith("X-ANNIVERSARY")) {
                    value = "1967-12-25";
                } else if(field.startsWith("X-FUNAMBOL-CHILDREN") || field.startsWith("X-SPOUSE")) {
                    value = "person" + i;
                } else if(field.startsWith("TITLE")) {
                    value = "title";
                } else if(field.startsWith("ORG")) {
                    value = "company;dep";
                } else if(field.startsWith("NOTE")) {
                    value = "notes";
                } else if(field.startsWith("PHOTO")) {
                    value = getSamplePhoto();
                    field += ";ENCODING=BASE64;TYPE=image/jpeg";
                }
            }
            if(!"".equals(value) || "".equals(value) && formatAllFields) {
                if("".equals(value) && formatAllFields) {
                    if(field.startsWith("ORG") && fields.contains("TITLE")) {
                        value = ";";
                    }
                }
                res.append(field).append(":").append(value).append("\r\n");
            }
        }

        res.append("END:VCARD").append("\r\n");

        return res.toString().getBytes();
    }

    private byte[] getSampleUpdatedVCard(Vector<String> fields, Vector<String> allFields,
            boolean formatAllFields) {

        StringBuffer res = new StringBuffer();

        res.append("BEGIN:VCARD").append("\r\n");
        res.append("VERSION:2.1").append("\r\n");

        for(int i=0; i<allFields.size(); i++) {
            String field = allFields.elementAt(i);
            String value = "";
            if(fields.contains(field)) {
                if(field.equals("N")) {
                    value = "upd_lastname;upd_firstname;upd_midlle;upd_salutation;upd_suffix";
                } else if(field.startsWith("NICKNAME")) {
                    value = "upd_nickname";
                } else if(field.startsWith("TEL")) {
                    value = "++1234567890" + i;
                } else if(field.startsWith("EMAIL")) {
                    value = "upd_email@email" + i + ".com";
                } else if(field.startsWith("ADR")) {
                    value = "upd_address" + i + "upd_pobox;upd_ext-address;upd_street;upd_city;upd_state;upd_code;upd_country";
                } else if(field.startsWith("URL")) {
                    value = "upd_web.url" + i + ".com";
                } else if(field.startsWith("BDAY") || field.startsWith("X-ANNIVERSARY")) {
                    value = "1970-12-25";
                } else if(field.startsWith("X-FUNAMBOL-CHILDREN") || field.startsWith("X-SPOUSE")) {
                    value = "upd_person" + i;
                } else if(field.startsWith("TITLE")) {
                    value = "upd_title";
                } else if(field.startsWith("ORG")) {
                    value = "upd_company;upd_dep";
                } else if(field.startsWith("NOTE")) {
                    value = "upd_notes";
                } else if(field.startsWith("PHOTO")) {
                    value = getUpdatedPhoto();
                    field += ";ENCODING=BASE64;TYPE=image/jpeg";
                }
            }
            if(!"".equals(value) || "".equals(value) && formatAllFields) {
                if("".equals(value) && formatAllFields) {
                    if(field.startsWith("ORG")) {
                        value = ";";
                    } else if(field.startsWith("ADR")) {
                        value = ";;;;;;";
                    }
                }
                res.append(field).append(":").append(value).append("\r\n");
            }
        }

        res.append("END:VCARD").append("\r\n");

        return res.toString().getBytes();
    }

    private byte[] getSampleMergedVCard(Vector<String> fields, Vector<String> allFields) {

        StringBuffer res = new StringBuffer();

        res.append("BEGIN:VCARD").append("\r\n");
        res.append("VERSION:2.1").append("\r\n");

        for(int i=0; i<allFields.size(); i++) {
            String field = allFields.elementAt(i);
            String value = "";
            if(fields.contains(field)) {
                if(field.equals("N")) {
                    value = "upd_lastname;upd_firstname;upd_midlle;upd_salutation;upd_suffix";
                } else if(field.startsWith("NICKNAME")) {
                    value = "upd_nickname";
                } else if(field.startsWith("TEL")) {
                    value = "++1234567890" + i;
                } else if(field.startsWith("EMAIL")) {
                    value = "upd_email@email" + i + ".com";
                } else if(field.startsWith("ADR")) {
                    value = "upd_address" + i + "upd_pobox;upd_ext-address;upd_street;upd_city;upd_state;upd_code;upd_country";
                } else if(field.startsWith("URL")) {
                    value = "upd_web.url" + i + ".com";
                } else if(field.startsWith("BDAY") || field.startsWith("X-ANNIVERSARY")) {
                    value = "1970-12-25";
                } else if(field.startsWith("X-FUNAMBOL-CHILDREN") || field.startsWith("X-SPOUSE")) {
                    value = "upd_person" + i;
                } else if(field.startsWith("TITLE")) {
                    value = "upd_title";
                } else if(field.startsWith("ORG")) {
                    value = "upd_company;upd_dep";
                } else if(field.startsWith("NOTE")) {
                    value = "upd_notes";
                } else if(field.startsWith("PHOTO")) {
                    value = getUpdatedPhoto();
                    field += ";ENCODING=BASE64;TYPE=image/jpeg";
                }
            } else {
                if(field.equals("N")) {
                    value = "lastname;firstname;midlle;salutation;suffix";
                } else if(field.startsWith("NICKNAME")) {
                    value = "nickname";
                } else if(field.startsWith("TEL")) {
                    value = "+1234567890" + i;
                } else if(field.startsWith("EMAIL")) {
                    value = "email@email" + i + ".com";
                } else if(field.startsWith("ADR")) {
                    value = "address" + i + "pobox;ext-address;street;city;state;code;country";
                } else if(field.startsWith("URL")) {
                    value = "web.url" + i + ".com";
                } else if(field.startsWith("BDAY") || field.startsWith("X-ANNIVERSARY")) {
                    value = "1967-12-25";
                } else if(field.startsWith("X-FUNAMBOL-CHILDREN") || field.startsWith("X-SPOUSE")) {
                    value = "person" + i;
                } else if(field.startsWith("TITLE")) {
                    value = "title";
                } else if(field.startsWith("ORG")) {
                    value = "company;dep";
                } else if(field.startsWith("NOTE")) {
                    value = "notes";
                } else if(field.startsWith("PHOTO")) {
                    value = getSamplePhoto();
                    field += ";ENCODING=BASE64;TYPE=image/jpeg";
                }
            }
            if(!"".equals(value)) {
                res.append(field).append(":").append(value).append("\r\n");
            }
        }

        res.append("END:VCARD").append("\r\n");

        return res.toString().getBytes();
    }

    private String getSamplePhoto() {
        return "/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEABALDA4MChAODQ4SERATGCgaGBYWGDEj\r\n" +
               " JR0oOjM9PDkzODdASFxOQERXRTc4UG1RV19iZ2hnPk1xeXBkeFxlZ2MBERISGBUY\r\n" +
               " LxoaL2NCOEJjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2Nj\r\n" +
               " Y2NjY2NjY2NjY//AABEIADwAUAMBIgACEQEDEQH/xAGiAAABBQEBAQEBAQAAAAAA\r\n" +
               " AAAAAQIDBAUGBwgJCgsQAAIBAwMCBAMFBQQEAAABfQECAwAEEQUSITFBBhNRYQci\r\n" +
               " cRQygZGhCCNCscEVUtHwJDNicoIJChYXGBkaJSYnKCkqNDU2Nzg5OkNERUZHSElK\r\n" +
               " U1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6g4SFhoeIiYqSk5SVlpeYmZqio6Slpqeo\r\n" +
               " qaqys7S1tre4ubrCw8TFxsfIycrS09TV1tfY2drh4uPk5ebn6Onq8fLz9PX29/j5\r\n" +
               " +gEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoLEQACAQIEBAMEBwUEBAABAncA\r\n" +
               " AQIDEQQFITEGEkFRB2FxEyIygQgUQpGhscEJIzNS8BVictEKFiQ04SXxFxgZGiYn\r\n" +
               " KCkqNTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqCg4SFhoeI\r\n" +
               " iYqSk5SVlpeYmZqio6Slpqeoqaqys7S1tre4ubrCw8TFxsfIycrS09TV1tfY2dri\r\n" +
               " 4+Tl5ufo6ery8/T19vf4+fr/2gAMAwEAAhEDEQA/AORsdKkvGUn5IyfvUmo6ZJZX\r\n" +
               " JiZgRjKnGMiusCxwPGDhVUVB4nhiutN+0QsGaEgHHoaUrLRDSujjMUUuKXFAhtFO\r\n" +
               " xRikA2jFPxRigBmKUCnYpcYouFjqJr8ucqACOoJ6VJcSodIu0ZsO8fC49PesCPzX\r\n" +
               " lXg4NXJJJDG0bHGeB71D1ZV9DGC1K0WIVbHWrCwq3Cgjj9akaNjEEIOBzTlcSt1K\r\n" +
               " ASl2Vq2ulT3SuYImcIMtgdBVVodrEHrRcCps4o24rf0HS7fUbwQ3EwiTaTmpNQ0C\r\n" +
               " O1tjMJg43lVx0IHei4HN4pCOKtvblMqR+NQsv7vrTA0Yf9fHkcc84rUNrFPDzuyv\r\n" +
               " IwM81jvO0ax/KCAD0/CpYtQc8KDWcqbezHGVnsTW1o7RuCm2RW6swAx/jWjcbZbG\r\n" +
               " C2ItozHkmUMMn2OKxkdlOBGVJJyTzQZVDcxdKtpkmla3k+mmRLa4Qh12nbzxVE7Z\r\n" +
               " HJPU0xJQDlAEPf3pPMl3cEDJzwKLAX7CG2hV2uDIxx8m35QD71evL5J40R/LVEGF\r\n" +
               " BPasf5xgs5OfU0wOivlhnPoKLIEWLh7ecqiBRwAWC4x71TMEUcpEhJA6EVYMmRhV\r\n" +
               " GPpUbkHrkUXHYR4lMqDBICnv9KkjiVOkWD61ZcA3CcD7h/mKsbRnpWjFYoOJCp28\r\n" +
               " VGkDHlhWkVGelAQY6VLGkUHhG3hcEelQSlolB2kZ6E1rlRs6YpjRq0fzAH2qWOxl\r\n" +
               " lncCjDYAPbpV1oI0QsopREpXNS2UkRQq2MkEr7VMFR+gJ/CnwqMMD26VOvBoTHY/\r\n" +
               " /9k=";
    }
    
    private String getUpdatedPhoto() {
        return "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQY\r\n" +
               " GBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYa\r\n" +
               " KCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAAR\r\n" +
               " CAA2AEgDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAA\r\n" +
               " AgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkK\r\n" +
               " FhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWG\r\n" +
               " h4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl\r\n" +
               " 5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREA\r\n" +
               " AgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYk\r\n" +
               " NOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOE\r\n" +
               " hYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk\r\n" +
               " 5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwDirWAZ4rpNHTawVuhqnLZva3DLLAY885A+\r\n" +
               " X86nbXLGD7PaeWFlzh5ienOR/P8ASur6ypxVtbn6BDD+wldnTpbgEdxUptR1xTdGu4L62DRS\r\n" +
               " K+O4Na8cBkXK44GTmsvbJbne3FIylthg8VVntcvwK2zHtzUIjByxqlW6haMkYVzBsTGKwNQi\r\n" +
               " zkAV1l4Ac1hXkXJ4rSFUxr0brQ5G8gxmitS7iHPFFae1PKlhtTt2VJ4huAYEd647xPoWyCW4\r\n" +
               " towe7jv9a1Yr9wPkK+2TVuLUGdCJwn/ATmvlKeMlTPq6uEbXK1dHDeFL0adqUc80kiwA/OE/\r\n" +
               " wr1LR9cgndfs8gkJG4Ljt3rz7xDpSbnurP5RjmOMdT60/wAFQ3C6iocPGmDlmGOMe9dNXFQn\r\n" +
               " BzTOdYJQg01oenSTxTOIyyI78gDkj8BVHVbqKxhLPINm7bnuT9KhbT47a3VjKTIxwJFk+ZfT\r\n" +
               " 6iuP1nV7iLeJ0jkMT9+c9jnFctDGOpKyZFDDqbvF6I3BfJcIJI2yrVTuZk+prl2uG1C+DiJr\r\n" +
               " W3UBhsbGT2NaovIEHqfU13fWHE7VQ5+lkJcgv0FFVLrVolztIoqvrM+xjLC077nEW3jOMICx\r\n" +
               " kU+mM1fh8a2v8UxH1U1z6aRYEfMG/M046NpoXO2T/vqspUaT6M8eljszitZRfrf9DrE8Y2IX\r\n" +
               " L3C8egNOg+IemxPytwwHcL/9euMOl2BOFWT8Wq/pWhaXLOFuBJt9nxWMsJR5W5JmjxmZYhqE\r\n" +
               " OVfedpL8U9HkhES218p7swXH86ybrxdpk27ypEy394Ff6U1/CmhLHu2T/wDfdYl9ouloW8pX\r\n" +
               " xnjLGs6OHw7fuJoqm8zwkW5uL9b/AKE1x4ojkJEbKp6Z5P61Tk15m/5bg1UOlWStwH/76pp0\r\n" +
               " 2zH9/wD76r1IU6cVojzMRmGPm/ekl6Nkkmrbv+WuaKqPYWg6B/8AvqitLR7HmyxWKv8AEvvZ\r\n" +
               " oB+BSs5wfaiiuZHsoiDkPV/TZi03TgUUUT+ArBzl7VamwNQZgy7eV9+OlZF7KS2O1FFcuG+I\r\n" +
               " 9bHzl7HczfN3N3x0pHcg8dDRRXpHytQrSuTxk0UUVR58tz//2Q==";
        }
}
