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

package com.funambol.common.pim.vcard;

import java.io.OutputStream;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.microedition.pim.PIMList;
import javax.microedition.pim.PIMException;

import javax.microedition.pim.Contact;

import com.funambol.util.Log;
import com.funambol.util.QuotedPrintable;
import com.funambol.common.pim.Utils;
import com.funambol.common.pim.PimUtils;

/**
 * This class implements a vCard 2.1 formatter for JSR75 contacts.
 * The class can be extended to customize the way or the order in which fields
 * are emitted. This class, like the ContactParserListener, has a fixed mapping
 * for the multivalue fields. See ContactParserListener for more details on this
 * mapping. In general the two implementations should be kept in sync and
 * consistent. One aspect that client are likely to need redefining is the way
 * photo's type is detected. J2ME has no standard way to detect the type of a
 * picture in a byte stream, so the basic implementation assumes the mime
 * types for contact pictures is "jpeg". Clients can extend the class and
 * redefine the getPhoto method to support custom image type detection.
 *
 * Warning: the current implementation is not finished yet and not all fields
 * are mapped. This implementation is used in the BlackBerry so it handles the
 * BB fields only. It will be extended when needed.
 */
public class VCardFormatter {

    private static final String BEGIN_TAG      = "BEGIN:VCARD";
    private static final String END_TAG        = "END:VCARD";
    private static final String NAME_TAG       = "N";
    protected static final String TEL_HOME_TAG   = "TEL;VOICE;HOME";
    protected static final String TEL_WORK_TAG   = "TEL;VOICE;WORK";
    protected static final String TEL_PAGER_TAG  = "TEL;PAGER";
    protected static final String FAX_WORK_TAG = "TEL;FAX;WORK";
    protected static final String FAX_HOME_TAG = "TEL;FAX;HOME";
    protected static final String FAX_OTHER_TAG= "TEL;FAX";
    protected static final String TEL_MOBILE_TAG = "TEL;CELL";
    protected static final String TEL_OTHER_TAG  = "TEL;VOICE";
    private static final String TITLE_TAG      = "TITLE";
    private static final String URL_TAG        = "URL";
    private static final String ORG_TAG        = "ORG";
    private static final String NOTE_TAG       = "NOTE";
    private static final String ADDR_HOME_TAG  = "ADR;HOME";
    private static final String ADDR_WORK_TAG  = "ADR;WORK";
    private static final String ADDR_OTHER_TAG = "ADR";
    protected static final String EMAIL_OTHER_TAG= "EMAIL;INTERNET";
    protected static final String EMAIL_HOME_TAG = "EMAIL;INTERNET;HOME";
    protected static final String EMAIL_WORK_TAG = "EMAIL;INTERNET;WORK";
    private static final String PHOTO_TAG      = "PHOTO";
    private static final String CATEGORIES_TAG = "CATEGORIES";
    private static final String VERSION_TAG    = "VERSION";
    private static final String BIRTHDAY_TAG   = "BDAY";

    private static final String DEFAULT_PHOTO_TYPE = "jpeg";

    private static final String CHARSET_TAG    = "CHARSET=";

    private String  defaultCharset = "UTF-8";
    protected PimUtils pimUtils  = new PimUtils(defaultCharset);

    public VCardFormatter() {
    }

    public VCardFormatter(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    public void format(Contact contact, OutputStream os, boolean allFields) throws PIMException {
        Log.info("Formatting VCard");
        pimUtils.println(os, BEGIN_TAG);

        formatFields(contact, os, allFields);

        pimUtils.println(os, END_TAG);
        Log.info("Formatting VCard done");
    }

    protected void formatFields(Contact contact, OutputStream os, boolean allFields) throws PIMException {

        PIMList list = contact.getPIMList();

        Log.debug("Formatting Version");
        formatVersion(contact, os);

        Log.debug("Formatting Name");
        if (isSupportedField(list, Contact.NAME)) {
            formatName(contact, os, allFields);
        }

        Log.debug("Formatting Telephone");
        if (isSupportedField(list, Contact.TEL)) {
            formatTel(contact, os, allFields);
        }

        Log.debug("Formatting Address");
        if (isSupportedField(list, Contact.ADDR)) {
            formatAddress(contact, os, allFields);
        }

        Log.debug("Formatting Birthday");
        if (isSupportedField(list, Contact.BIRTHDAY)) {
            formatBirthday(contact, os, allFields);
        }

        Log.debug("Formatting Email");
        if (isSupportedField(list, Contact.EMAIL)) {
            formatEmail(contact, os, allFields);
        }

        Log.debug("Formatting Title");
        if (isSupportedField(list, Contact.TITLE)) {
            formatSimple(contact, Contact.TITLE, TITLE_TAG, os, allFields);
        }

        Log.debug("Formatting Url");
        if (isSupportedField(list, Contact.URL)) {
            formatSimple(contact, Contact.URL, URL_TAG, os, allFields);
        }

        Log.debug("Formatting Org");
        if (isSupportedField(list, Contact.ORG)) {
            formatSimple(contact, Contact.ORG, ORG_TAG, os, allFields);
        }

        Log.debug("Formatting Note");
        if (isSupportedField(list, Contact.NOTE)) {
            formatSimple(contact, Contact.NOTE, NOTE_TAG, os, allFields);
        }

        Log.debug("Formatting Photo");
        if (isSupportedField(list, Contact.PHOTO)) {
            formatPhoto(contact, os, allFields);
        }

        Log.debug("Formatting Categories");
        formatCategories(contact, os, allFields);
    }

    protected String getPhotoType(byte[] photo) {
        return DEFAULT_PHOTO_TYPE;
    }

    protected void formatVersion(Contact contact, OutputStream os)
    throws PIMException
    {
        pimUtils.println(os, VERSION_TAG + ":2.1");
    }

    protected void formatPhoto(Contact contact, OutputStream os, boolean allFields)
    throws PIMException
    {
        String type  = null;
        String photo = "";
        if (contact.countValues(Contact.PHOTO) > 0) {
            try {
                byte[] photoEncoded = contact.getBinary(Contact.PHOTO, Contact.ATTR_NONE);

                type = getPhotoType(photoEncoded);
                photo = new String(photoEncoded);
            } catch (Exception e) {
                // If we cannot format the photo we log the error, but do not
                // give up on formatting the card
                Log.error("Error formatting the photo: " + e.toString());
            }
        }

        if (allFields || photo.length() > 0) {
            StringBuffer field = new StringBuffer();
            field.append(PHOTO_TAG);
            if (type != null) {
                field.append(";ENCODING=BASE64");
                field.append(";TYPE=").append(type);
            }
            field.append(":");
            photo = pimUtils.fold(photo);
            field.append(photo);
            pimUtils.println(os, field.toString());
        }
    }


    protected void formatName(Contact contact, OutputStream os, boolean allFields)
    throws PIMException
    {
        // Print the name
        int numValues = contact.countValues(Contact.NAME);
        if (allFields || numValues > 0) {
            StringBuffer name = new StringBuffer();

            String firstName  = null;
            String lastName   = null;
            String salutation = null;

            if (numValues > 0) {
                String[] cName = contact.getStringArray(Contact.NAME, 0);
                firstName  = cName[Contact.NAME_GIVEN];
                lastName   = cName[Contact.NAME_FAMILY];
                salutation = cName[Contact.NAME_PREFIX];
            }

            // These tests are necessary because the values extracted from the
            // contact may be null
            if (firstName == null) {
                firstName = "";
            }

            if (lastName == null) {
                lastName = "";
            }

            if (salutation == null) {
                salutation = "";
            }

            firstName   = pimUtils.escape(firstName,false);
            lastName    = pimUtils.escape(lastName,false);
            salutation  = pimUtils.escape(salutation,false);

            name.append(lastName).append(";");   // Last name
            name.append(firstName).append(";");  // First name
            name.append(";");                    // Middle name
            name.append(salutation).append(";"); // Salutation
            formatField(NAME_TAG, name.toString(), os);
        }
    }

    protected void formatField(String tag, String value, OutputStream os) 
    throws PIMException
    {
        StringBuffer field = new StringBuffer();
        field.append(tag);
        boolean charsetTagAdded = false;
        if (!defaultCharset.equals("UTF-8")) {
            field.append(";").append(CHARSET_TAG).append(defaultCharset);
            charsetTagAdded = true;
        }
        try {
            String qpEncoded = QuotedPrintable.encode(value, defaultCharset);
            if (qpEncoded.length() != value.length()) {
                if (!charsetTagAdded) {
                    field.append(";").append(CHARSET_TAG).append(defaultCharset);
                    charsetTagAdded = true;
                }
                field.append(";ENCODING=QUOTED-PRINTABLE");
                value = qpEncoded;
            }
            field.append(":");
            // Note: we cannot fold all fields, because we miss the logic to
            // fold quoted printable properties, which have a special folding
            // rule. Since folding is not required, we avoid it for all fields
            // but the photo
            field.append(value);
            pimUtils.println(os, field.toString());
        } catch (Exception e) {
            throw new PIMException(e.toString());
        }
    }

    protected void formatTag(StringBuffer out, String tag) {
        if (defaultCharset.equals("UTF-8")) {
            out.append(tag).append(':');
        } else {
            out.append(tag).append(";").append(CHARSET_TAG).append(defaultCharset).append(':');
        }
    }

    protected void formatTel(Contact contact, OutputStream os, boolean allFields)
    throws PIMException
    {
        // Print the telephones
        String home   = "";
        String work   = "";
        String pager  = "";
        String fax    = "";
        String mobile = "";
        String other  = "";
        for (int i = 0; i < contact.countValues(Contact.TEL); i++)
        {
            final String value = contact.getString(Contact.TEL, i);
            if (value != null) {
                final int type = contact.getAttributes(Contact.TEL, i);
                switch (type) {
                    case Contact.ATTR_HOME:
                        home = pimUtils.escape(value,false);
                        break;
                    case Contact.ATTR_WORK:
                        work = pimUtils.escape(value,false);
                        break;
                    case Contact.ATTR_PAGER:
                        pager = pimUtils.escape(value,false);
                        break;
                    case Contact.ATTR_FAX:
                        fax = pimUtils.escape(value,false);
                        break;
                    case Contact.ATTR_MOBILE:
                        mobile = pimUtils.escape(value,false);
                        break;
                    case Contact.ATTR_OTHER:
                        other = pimUtils.escape(value,false);
                        break;
                    default:
                        Log.error("Unsupported telephone type");
                }
            }
        }

        // Now print everything
        if (allFields || home.length() > 0) {
            StringBuffer value = new StringBuffer();
            formatTag(value, TEL_HOME_TAG);
            value.append(home);
            pimUtils.println(os, value.toString());
        }
        if (allFields || work.length() > 0) {
            StringBuffer value = new StringBuffer();
            formatTag(value, TEL_WORK_TAG);
            value.append(work);
            pimUtils.println(os, value.toString());
        }
        if (allFields || pager.length() > 0) {
            StringBuffer value = new StringBuffer();
            formatTag(value, TEL_PAGER_TAG);
            value.append(pager);
            pimUtils.println(os, value.toString());
        }
        if (allFields || fax.length() > 0) {
            StringBuffer value = new StringBuffer();
            //formatTag(value, FAX_WORK_TAG);
            formatFaxTag(value, FAX_WORK_TAG);
            value.append(fax);
            pimUtils.println(os, value.toString());
        }
        if (allFields || mobile.length() > 0) {
            StringBuffer value = new StringBuffer();
            formatTag(value, TEL_MOBILE_TAG);
            value.append(mobile);
            pimUtils.println(os, value.toString());
        }
        if (allFields || other.length() > 0) {
            StringBuffer value = new StringBuffer();
            formatTag(value, TEL_OTHER_TAG);
            value.append(other);
            pimUtils.println(os, value.toString());
        }
    }

    protected void formatFaxTag (StringBuffer out, String tag){
        formatTag(out, tag);
    }

    protected void formatAddress(Contact contact, OutputStream os, boolean allFields)
    throws PIMException
    {
        String homeAddress[]  = null;
        String workAddress[]  = null;
        String otherAddress[] = null;
        for (int i = 0; i < contact.countValues(Contact.ADDR); i++) {
            String [] address = contact.getStringArray(Contact.ADDR, i);
            int addrType = contact.getAttributes(Contact.ADDR, i);
            if (addrType == Contact.ATTR_HOME) {
                homeAddress = address;
            } else if (addrType == Contact.ATTR_WORK) {
                workAddress = address;
            } else {
                otherAddress = address;
            }
        }

        if (homeAddress != null) {
            formatAddress(homeAddress, ADDR_HOME_TAG, os);
        } else if (allFields) {
            formatEmptyAddress(ADDR_HOME_TAG, os);
        }

        if (workAddress != null) {
            formatAddress(workAddress, ADDR_WORK_TAG, os);
        } else if (allFields) {
            formatEmptyAddress(ADDR_WORK_TAG, os);
        }

        if (otherAddress != null) {
            formatAddress(otherAddress, ADDR_OTHER_TAG, os);
        } else if (allFields) {
            formatEmptyAddress(ADDR_OTHER_TAG, os);
        }
    }

    protected void formatAddress(String address[], String tag, OutputStream os) throws PIMException {
        StringBuffer addr = new StringBuffer();

        addr.append(";");     //postoffice             

        String extendedAddress = pimUtils.escape(address[Contact.ADDR_EXTRA],false);
        if (extendedAddress == null){
            extendedAddress = "";
        }

        String street = pimUtils.escape(address[Contact.ADDR_STREET],false);
        if (street == null) {
            street = "";
        }

        String locality = pimUtils.escape(address[Contact.ADDR_LOCALITY],false);
        if (locality == null) {
            locality = "";
        }

        String region = pimUtils.escape(address[Contact.ADDR_REGION],false);
        if (region == null) {
            region = "";
        }

        String postalcode = pimUtils.escape(address[Contact.ADDR_POSTALCODE],false);
        if (postalcode == null) {
            postalcode = "";
        }

        String country = pimUtils.escape(address[Contact.ADDR_COUNTRY],false);
        if (country == null) {
            country = "";
        }

        addr.append(extendedAddress).append(";");
        addr.append(street).append(";");
        addr.append(locality).append(";");
        addr.append(region).append(";");
        addr.append(postalcode).append(";");
        addr.append(country);

        formatField(tag, addr.toString(), os);
    }

    private void formatEmptyAddress(String tag, OutputStream os) throws PIMException {
        StringBuffer addr = new StringBuffer();
        formatTag(addr, tag);
        addr.append(";");                    // postoffice
        addr.append(";");                    // extended address
        addr.append(";");
        addr.append(";");
        addr.append(";");
        addr.append(";");
        pimUtils.println(os, addr.toString());
    }


    protected void formatEmail(Contact contact, OutputStream os, boolean allFields)
    throws PIMException
    {
        String other = "";
        String home  = "";
        String work  = "";

        for(int i=0;i<contact.countValues(Contact.EMAIL);++i) {
            Log.debug("Fetching email number: " + i);
            String value = contact.getString(Contact.EMAIL, i);
            if (value != null && value.length() > 0) {
                switch (i) {
                    case 0:
                        other = pimUtils.escape(value,false);
                        break;
                    case 1:
                        home = pimUtils.escape(value,false);
                        break;
                    case 2:
                        work = pimUtils.escape(value,false);
                        break;
                }
            }
        }

        if (allFields || other.length() > 0) {
            formatField(EMAIL_OTHER_TAG, other, os);
        }
        if (allFields || home.length() > 0) {
            formatField(EMAIL_HOME_TAG, home, os);
        }
        if (allFields || work.length() > 0) {
            formatField(EMAIL_WORK_TAG, work, os);
        }
    }

    protected void formatCategories(Contact contact, OutputStream os,
                                    boolean allFields) throws PIMException
    {
        String categories[] = contact.getCategories();
        boolean first = true;
        StringBuffer value = new StringBuffer();

        for(int i=0;i<categories.length;++i) {
            String category = categories[i];
            if (!first) {
                value.append("; ");
            }
            value.append(category);
            first = false;
        }
        String v = pimUtils.escape(value.toString(),false);
        if (v == null) {
            v = "";
        }

        if (allFields || v.length() > 0) {
            formatField(CATEGORIES_TAG, v, os);
        }
    }

    protected void formatBirthday(Contact contact, OutputStream os,
                                    boolean allFields) throws PIMException
    {
         String birthDay = "";


        if (contact.countValues(Contact.BIRTHDAY) > 0){
            long birthdayLong  = contact.getDate(Contact.BIRTHDAY, Contact.ATTR_NONE);
            Date birthDate = new Date (birthdayLong);
            // Use a GMT calendar to avoid any conversion when building from the
            // long value (GMT)
            TimeZone gmtTz = TimeZone.getTimeZone("GMT");
            Calendar cal = Calendar.getInstance(gmtTz);
            cal.setTime(birthDate);
            StringBuffer value = new StringBuffer();

            // Format the value. Since the birthday is a sort of all day event,
            // we do not express it as GMT (appending the Z) even if the value
            // was computed as GMT
            value.append(cal.get(Calendar.YEAR)).append("-");

            int monthInt =cal.get(Calendar.MONTH)+1;
            String month= (monthInt<10 ? "0"+monthInt : ""+monthInt );
            value.append(month).append("-");

            int dayInt =cal.get(Calendar.DAY_OF_MONTH);
            String day= (dayInt<10 ? "0"+dayInt : ""+dayInt );
            value.append(day);

            birthDay = value.toString();
        }

        if (allFields) {
            formatField(BIRTHDAY_TAG, birthDay, os);
        }
    }

    /**
     * This method can be redefined to change the default behavior and ignore
     * unwanted fields.
     *
     * @return a list of fields that are going to be supported or null if all
     *         fields shall be supported.
     */
    protected int[] getSupportedFields() {
        return null;
    }

    protected boolean isSupportedField(PIMList list, int field) {
        boolean supported = list.isSupportedField(field);
        int supportedFields[] = getSupportedFields();
        boolean found = false;
        if (supportedFields != null) {
            // Check if the field is supported
            for(int i=0;i<supportedFields.length;++i) {
                if (supportedFields[i] == field) {
                    found = true;
                }
            }
        } else {
            // In this case there is no filtering, so everything is supported
            found = true;
        }
        return supported && found;
    }

    private void formatSimple(Contact contact, int field, String tag,
                              OutputStream os, boolean allFields)
    throws PIMException
    {
        PIMList list = contact.getPIMList();
        if (list.isSupportedField(field)) {
            String value = "";
            if (contact.countValues(field) > 0) {
                value  = contact.getString(field, 0);
                if (value == null) {
                    value = "";
                }
            }
            if (allFields || value.length() > 0) {
                // Do not escape LF and comma. LF will be encoded in quoted
                // printable, while commas do not need to be escaped
                value = pimUtils.escape(value,false, false);
                formatField(tag, value, os);
            }
        }
    }
}

