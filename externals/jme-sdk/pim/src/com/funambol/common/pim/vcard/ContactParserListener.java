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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Calendar;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;

import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMList;
import javax.microedition.pim.PIMException;

import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;

import com.funambol.util.Log;
import com.funambol.util.DateUtil;

import com.funambol.common.pim.vcard.VCardSyntaxParser;
import com.funambol.common.pim.vcard.AbstractVCardSyntaxParserListener;
import com.funambol.common.pim.ParamList;
import com.funambol.common.pim.FieldsList;
import com.funambol.common.pim.Utils;
import com.funambol.common.pim.PimUtils;
import com.funambol.common.pim.vcard.Token;
import com.funambol.common.pim.vcard.ParseException;

/**
 * This class implements a VCard parser listener that generates a Contact (JSR75
 * definition). This class provides a basic implementation for this task, but it
 * is designed to be extended so that client can customize several things.
 * One major aspect that may require specialization is the mapping of the
 * multiple fields. Today this mapping is hardcoded, but the client can derive
 * the class e specialize few methods to change the behavior. In the future we
 * may decide to have a table to force a particular mapping.
 * The mapping is the following:
 *
 * Contact field  | VCard field
 * ------------------------------
 * first email    | INTERNET EMAIL
 * second email   | HOME EMAIL
 * third email    | WORK EMAIL
 * tel, fax       | BUSINESS FAX
 * tel, work      | TEL VOICE WORK
 * tel, home      | TEL VOICE HOME
 * tel, other     | TEL VOICE
 *
 * Warning: the current implementation is not finished yet and not all fields
 * are mapped. This implementation is used in the BlackBerry so it handles the
 * BB fields only. It will be extended when needed.
 *
 */

public class ContactParserListener extends AbstractVCardSyntaxParserListener
{
    private static final String TAG_LOG = "ContactParserListener";

    private static final String defaultCharset = "UTF-8";

    protected boolean faxSet    = false;
    protected boolean pagerSet  = false;
    protected boolean mobileSet = false;
    protected boolean workSet   = false;
    protected boolean homeSet   = false;
    protected boolean otherSet  = false;

    protected PimUtils   pimUtils  = new PimUtils(defaultCharset);
    
    //To create a new category on PIMList
    private boolean addNewCategory;

    /**
     * Emails addresses. The current implementation supports 3 different emails
     * emails[0] is INTERNET EMAIL
     * emails[1] is HOME EMAIL
     * emails[2] is WORK EMAIL
     * Other emails are simply discarded
     */
    private String emails[]   = new String[3];

    protected Contact contact;

    protected int getUrlMaxValues() {
        return 1;
    }

    protected int getTitleMaxValues() {
        return 1;
    }

    public ContactParserListener(Contact contact, boolean addNewCategory) {
        this.contact = contact;
        this.addNewCategory = addNewCategory;
    }

    public void start() {
        Log.info(TAG_LOG, "Starting vcard parsing");
        for (int i=0;i<emails.length;++i) {
            emails[i] = null;
        }
    }

    public void end() {

        Log.debug(TAG_LOG, "Vcard finalizing emails");

        boolean emailDefined = false;
        for(int i=0;i<emails.length;++i) {
            if (emails[i] != null && emails[i].length() != 0) {
                emailDefined = true;
            } else {
                emails[i] = "";
            }
        }

        Log.debug(TAG_LOG, "This contact has: " + contact.countValues(Contact.EMAIL) + " emails");

        if (emailDefined) {
            PIMList list = contact.getPIMList();
            if (isSupportedField(list, Contact.EMAIL)) {
                for(int i=0;i<emails.length;++i) {
                    Log.debug(TAG_LOG, "Adding email address: " + emails[i]);
                    contact.addString(Contact.EMAIL, Contact.ATTR_NONE, emails[i]);
                }
            }
        }

        Log.info(TAG_LOG, "Vcard parsing ended");
    }


    public void setCategories(String content, ParamList plist,
                              Token group) throws ParseException
    {
        String text = pimUtils.unfold(content);
        text = pimUtils.decode(text,plist.getEncoding(), plist.getCharset());
        text = pimUtils.unescape(text);

        PIMList list = contact.getPIMList();
        try {
            pimUtils.addCategories(text,list,contact,addNewCategory);
        } catch (Exception e) {
            Log.error(TAG_LOG, "adding categories failed");
        }
    }

    public void addExtension(String tagName, String content, ParamList plist,
                             Token group) throws ParseException
    {
        // At the moment no extensions are supported
    }

    public void setVersion(String ver, ParamList plist,
                           Token group) throws ParseException
    {
        if (!(ver.equals("2.1")) && !(ver.equals("3.0"))) {
            throw new ParseException("Encountered a vCard version other than 2.1 or 3.0 ("+ver+")");
        }
    }

    public void setTitle(String content, ParamList plist,
                         Token group) throws ParseException
    {
        Log.trace(TAG_LOG, "Setting title");

        int maxTitleValues = getTitleMaxValues();

        if (contact.countValues(Contact.TITLE) == maxTitleValues) {
            Log.error(TAG_LOG, "Dropping title");
            return;
        }

        String text = pimUtils.unfold(content);
        text = pimUtils.decode(text, plist.getEncoding(), plist.getCharset());
        text = pimUtils.unescape(text);

        PIMList list = contact.getPIMList();
        if (isSupportedField(list, Contact.TITLE))
        {
            contact.addString(Contact.TITLE, Contact.ATTR_NONE, text);
        }
    }

    public void setMail(String content, ParamList plist,
                        Token group) throws ParseException
    {
        Log.trace(TAG_LOG, "Setting Email");

        if (plist.containsKey("INTERNET") && plist.getSize() == 1) {
            emails[0] = content;
        } else if (plist.containsKey("HOME")) {
            emails[1] = content;
        } else if (plist.containsKey("WORK")) {
            emails[2] = content;
        } else {
            emails[0] = content;
        }
    }

    public void setUrl(String content, ParamList plist,
                       Token group) throws ParseException
    {
        Log.trace(TAG_LOG, "Setting Url");

        int maxUrlValues = getUrlMaxValues();

        if (contact.countValues(Contact.URL) == maxUrlValues) {
            Log.error(TAG_LOG, "Dropping Url");
            return;
        }

        // We do not distinguish the different url addresses and
        // fill the only available one
        PIMList list = contact.getPIMList();
        if (isSupportedField(list, Contact.URL))
        {
            contact.addString(Contact.URL, Contact.ATTR_NONE, content);
        }
    }

    public void setTelephone(String content, ParamList plist,
                             Token group) throws ParseException
    {
        Log.trace(TAG_LOG, "Setting Telephone");
        content = pimUtils.unfold(content);
        content = pimUtils.decode(content,plist.getEncoding(), plist.getCharset());
        content = pimUtils.unescape(content);

        PIMList list = contact.getPIMList();
        if (plist.containsKey("FAX")) {
            setFax(content, list, plist);
        } else if (plist.containsKey("PAGER")) {
            // BB does not distinguish between home and work pager, so we
            // simply store the first one and discard others
            if (!pagerSet && isSupportedAttributedField(list, Contact.TEL, Contact.ATTR_PAGER))
            {
                Log.debug(TAG_LOG, "Setting PAGER telephone to: " + content);
                contact.addString(Contact.TEL, Contact.ATTR_PAGER, content);
                pagerSet = true;
            }
        } else if (plist.containsKey("CELL")) {
            if (!mobileSet && isSupportedAttributedField(list, Contact.TEL,Contact.ATTR_MOBILE))
            {
                Log.debug(TAG_LOG, "Setting MOBILE telephone to: " + content);
                contact.addString(Contact.TEL, Contact.ATTR_MOBILE, content);
                mobileSet = true;
            }
        } else if (plist.containsKey("WORK") && plist.containsKey("VOICE") && plist.getSize() == 2) {
            if (!workSet && isSupportedAttributedField(list, Contact.TEL, Contact.ATTR_WORK))
            {
                Log.debug(TAG_LOG, "Setting WORK telephone to: " + content);
                contact.addString(Contact.TEL, Contact.ATTR_WORK, content);
                workSet = true;
            }

        } else if (plist.containsKey("HOME") && plist.containsKey("VOICE") && plist.getSize() == 2) {
            if (!homeSet && isSupportedAttributedField(list, Contact.TEL, Contact.ATTR_HOME))
            {
                Log.debug(TAG_LOG, "Setting HOME telephone to: " + content);
                contact.addString(Contact.TEL, Contact.ATTR_HOME, content);
                homeSet = true;
            }
        } else if (plist.containsKey("WORK")) {
            if (!workSet && isSupportedAttributedField(list, Contact.TEL, Contact.ATTR_WORK))
            {
                Log.debug(TAG_LOG, "Setting WORK telephone to: " + content);
                contact.addString(Contact.TEL, Contact.ATTR_WORK, content);
                workSet = true;
            }
        } else if (plist.containsKey("HOME")) {
            if (!homeSet && isSupportedAttributedField(list, Contact.TEL, Contact.ATTR_HOME))
            {
                Log.debug(TAG_LOG, "Setting HOME telephone to: " + content);
                contact.addString(Contact.TEL, Contact.ATTR_HOME, content);
                homeSet = true;
            }
        } else if (plist.containsKey("VOICE")) {
            if (!otherSet && isSupportedAttributedField(list, Contact.TEL, Contact.ATTR_OTHER))
            {
                Log.debug(TAG_LOG, "Setting OTHER telephone to: " + content);
                contact.addString(Contact.TEL, Contact.ATTR_OTHER, content);
                otherSet = true;
            }
        }
    }

    protected void setFax(String content, PIMList list, ParamList plist){
        if (!faxSet && isSupportedAttributedField(list, Contact.TEL, Contact.ATTR_FAX))
        {
            Log.debug(TAG_LOG, "Setting FAX telephone to: " + content);
            contact.addString(Contact.TEL, Contact.ATTR_FAX, content);
            faxSet = true;
        }
    }

    public void setFName(String content, ParamList plist,
                         Token group) throws ParseException {

        Log.trace(TAG_LOG, "Setting Formatted Name");
        PIMList list = contact.getPIMList();
        if (isSupportedField(list, Contact.FORMATTED_NAME) &&
            contact.countValues(Contact.FORMATTED_NAME) == 0) {

            String text = pimUtils.unfold(content);
            text = pimUtils.decode(text, plist.getEncoding(), plist.getCharset());
            text = pimUtils.unescape(text);
            contact.addString(Contact.FORMATTED_NAME, Contact.ATTR_NONE, text);
        }
    }

    public void setRole(String content, ParamList plist,
                        Token group) throws ParseException {

        // Not supported
    }

    public void setRevision(String content, ParamList plist,
                            Token group) throws ParseException {

        PIMList list = contact.getPIMList();
        if (isSupportedField(list, Contact.REVISION))
        {
            try {
                if (content != null && content.length() > 0) {
                    Calendar date = DateUtil.parseDateTime(content);
                    contact.addDate(Contact.REVISION, Contact.ATTR_NONE, date.getTime().getTime());
                }
            } catch (Exception e) {
                Log.error(TAG_LOG, "Unrecognizable date format in revision field:" +content, e);
                throw new ParseException("Unrecognizable date format");
            }
        }
    }

    public void setNickname(String content, ParamList plist,
                            Token group) throws ParseException {
        // Not supported
        Log.trace(TAG_LOG, "Setting Nickname");
        PIMList list = contact.getPIMList();
        if (isSupportedField(list, Contact.NICKNAME) &&
            contact.countValues(Contact.NICKNAME) == 0) {

            String text = pimUtils.unfold(content);
            text = pimUtils.decode(text, plist.getEncoding(), plist.getCharset());
            text = pimUtils.unescape(text);
            contact.addString(Contact.NICKNAME, Contact.ATTR_NONE, text);
        }
    }

    public void setOrganization(String content, ParamList plist,
                                Token group) throws ParseException {

        Log.trace(TAG_LOG, "Setting Organization");

        String encoding  = null            ;
        FieldsList flist = new FieldsList();
        flist.addValue(content);

        int pos;  // Position in tlist (i.e. position of the current value field)

        // Organization Name
        pos = 0;
        if (flist.size() > pos) {
            String text = pimUtils.unfold(flist.getElementAt(pos));
            text = pimUtils.decode(text,plist.getEncoding(), plist.getCharset());
            contact.addString(Contact.ORG, Contact.ATTR_NONE, text);
        }

        // Organizational Unit
        pos = 1;
        if (flist.size() > pos) {
            String text = pimUtils.unfold(flist.getElementAt(pos));
            text = pimUtils.decode(text,plist.getEncoding(), plist.getCharset());
            // Not supported
        }
    }

    public void setAddress(String content, ParamList plist,
                           Token group)throws ParseException
    {
        Log.trace(TAG_LOG, "Setting Address");

        FieldsList flist = new FieldsList();
        flist.addValue(content);

        int pos;  // Position in tlist (i.e. position of the current value field)
        String extendedAddr, street, locality, region, postalcode, country, pobox;

        // Business Address
        String arrayField[] = new String[7];

        //Post Office Address
        pos = 0;
        String text;
        if (flist.size()>pos) {
            text = pimUtils.unfold(flist.getElementAt(pos));
            pobox = pimUtils.decode(text,plist.getEncoding(), plist.getCharset());
        } else {
            pobox="";
        }
        // Not supported

        // Extended Address
        pos = 1;
        if (flist.size()>pos) {
            text=pimUtils.unfold(flist.getElementAt(pos));
            extendedAddr=pimUtils.decode(text,plist.getEncoding(), plist.getCharset());
        } else {
            extendedAddr="";
        }
       
        // Street
        pos = 2;
        if (flist.size()>pos) {
            text   = pimUtils.unfold(flist.getElementAt(pos));
            street = pimUtils.decode(text,plist.getEncoding(), plist.getCharset());
        } else {
            street="";
        }

        // Locality
        pos = 3;
        if (flist.size()>pos) {
            text     = pimUtils.unfold(flist.getElementAt(pos));
            locality = pimUtils.decode(text,plist.getEncoding(), plist.getCharset());
        } else {
            locality="";
        }

        // Region
        pos = 4;
        if (flist.size()>pos) {
            text   = pimUtils.unfold(flist.getElementAt(pos));
            region = pimUtils.decode(text,plist.getEncoding(), plist.getCharset());
        } else {
            region="";
        }

        // Postal Code
        pos = 5;
        if (flist.size()>pos) {
            text       = pimUtils.unfold(flist.getElementAt(pos));
            postalcode = pimUtils.decode(text,plist.getEncoding(), plist.getCharset());
        } else {
            postalcode="";
        }

        // Country
        pos = 6;
        if (flist.size()>pos) {
            text    = pimUtils.unfold(flist.getElementAt(pos));
            country = pimUtils.decode(text,plist.getEncoding(), plist.getCharset());
        } else {
            country="";
        }

        arrayField[Contact.ADDR_POBOX] = pobox;
        arrayField[Contact.ADDR_EXTRA] = extendedAddr;
        arrayField[Contact.ADDR_STREET] = street;
        arrayField[Contact.ADDR_LOCALITY] = locality;
        arrayField[Contact.ADDR_REGION] = region;
        arrayField[Contact.ADDR_POSTALCODE] = postalcode;
        arrayField[Contact.ADDR_COUNTRY] = country;

        if (plist.containsKey("WORK")) {
            contact.addStringArray(Contact.ADDR, Contact.ATTR_WORK,arrayField);
        } else if (plist.containsKey("HOME")) {
            contact.addStringArray(Contact.ADDR, Contact.ATTR_HOME,arrayField);
        }
    }

    public void setBirthday(String content, ParamList plist,
                            Token group) throws ParseException {
        Log.debug(TAG_LOG, "Setting Birthday");
        
        PIMList list = contact.getPIMList();
        if (isSupportedField(list, Contact.BIRTHDAY))
        {
            try {
                if (content != null && content.length() > 0) {
                    Calendar date = DateUtil.parseDateTime(content);
                    // We need to translate the date so that the UTC conversion
                    // occurring in the getTime() will not change the date
                    date = DateUtil.getSafeTimeForAllDay(date);
                    contact.addDate(Contact.BIRTHDAY, Contact.ATTR_NONE, date.getTime().getTime());
                }
            } catch (Exception e) {
                Log.error(TAG_LOG, "Unrecognizable date format in birthday field:" +content, e);
                throw new ParseException("Unrecognizable date format");
            }
        }
    }

    public void setLabel(String content, ParamList plist,
                         Token group) throws ParseException {

        // Not supported
    }

    public void setTimezone(String content, ParamList plist,
                            Token group) throws ParseException
    {
        // Not supported
    }

    public void setLogo(String content, ParamList plist,
                        Token group) throws ParseException
    {
        // Not supported
    }

    public void setNote(String content, ParamList plist,
                        Token group) throws ParseException
    {
        Log.trace(TAG_LOG, "Setting Note");

        String text = pimUtils.unfold(content);
        text        = pimUtils.decode(text,plist.getEncoding(), plist.getCharset());
        text        = pimUtils.unescape(text);

        PIMList list = contact.getPIMList();
        if (isSupportedField(list, Contact.NOTE)) {
            text = preprocessNote(text);
            contact.addString(Contact.NOTE, Contact.ATTR_NONE, text);
        }
    }

    public void setUid(String content, ParamList plist,
                       Token group) throws ParseException
    {
        Log.trace(TAG_LOG, "Setting UID");

        PIMList list = contact.getPIMList();
        if (isSupportedField(list, Contact.UID))
        {
            if (content != null && content.length() > 0) {
                contact.addString(Contact.UID, Contact.ATTR_NONE, content);
            }
        }
    }

    public void setPhoto(String content, ParamList plist,
                         Token group) throws ParseException
    {
        Log.trace(TAG_LOG, "Setting Photo");

        String text = pimUtils.unfold(content);
        text        = pimUtils.decode(text,plist.getEncoding(), plist.getCharset());
        text        = pimUtils.unescape(text);

        if (plist.containsKey("VALUE") && "URL".equals(plist.getValue("VALUE"))) {
            Log.error(TAG_LOG, "Photo with remote url are not supported");
            return;
        }

        try{
            PIMList list = contact.getPIMList();
            if (isSupportedField(list, Contact.PHOTO) && text.length() > 0)
            {
                byte[] byteField = text.getBytes();
                contact.addBinary(Contact.PHOTO, Contact.ATTR_NONE, byteField ,0,byteField.length);
            }
        }
        catch (final Throwable e)
        {
            Log.error(TAG_LOG, "Cannot set photo as the underlying system does not accept the binary data", e);
        }       
    }
 
    public void setName(String content, ParamList plist,
                        Token group) throws ParseException
    {
        Log.trace(TAG_LOG, "Setting Name to " + content);

        FieldsList flist = new FieldsList();
        flist.addValue(content);

        int pos;  // Position in tlist (i.e. position of the current value field)

        String[] arrayField = new String[5];

        // Last name
        pos=0;
        if (flist.size() > pos) {
            String text = flist.getElementAt(pos);
            text = pimUtils.unfold(text);
            Log.trace(TAG_LOG, text);
            text        = pimUtils.decode(text,plist.getEncoding(), plist.getCharset());
            Log.trace(TAG_LOG, text);
            arrayField[Contact.NAME_FAMILY] = text; 
        }

        // First name
        pos=1;
        if (flist.size() > pos) {
            String text = pimUtils.unfold(flist.getElementAt(pos));
            text        = pimUtils.decode(text,plist.getEncoding(), plist.getCharset());
            arrayField[Contact.NAME_GIVEN]  = text;
        }

        // Middle name
        pos=2;
        if (flist.size() > pos) {
            String text = pimUtils.unfold(flist.getElementAt(pos));
            text        = pimUtils.decode(text,plist.getEncoding(), plist.getCharset());
            // Not supported
        }

        // Prefix
        pos=3;
        if (flist.size() > pos) {
            String text = pimUtils.unfold(flist.getElementAt(pos));
            text        = pimUtils.decode(text,plist.getEncoding(), plist.getCharset());
            arrayField[Contact.NAME_PREFIX] = text;
        }

        // Suffix
        pos=4;
        if (flist.size() > pos) {
            String text = pimUtils.unfold(flist.getElementAt(pos));
            text        = pimUtils.decode(text,plist.getEncoding(), plist.getCharset());
            arrayField[Contact.NAME_SUFFIX] = text;
        }

        PIMList list = contact.getPIMList();
        if (isSupportedField(list, Contact.NAME)) {
            contact.addStringArray(Contact.NAME, PIMItem.ATTR_NONE, arrayField);
        }
    }

    public void setFolder(String content, ParamList plist,
                          Token group) throws ParseException
    {
        // Not supported
    }

    public void setFreebusy(String content, ParamList plist,
                            Token group) throws ParseException
    {
        // Not supported
    }

    public void setAnniversary(String content, ParamList plist,
                               Token group) throws ParseException
    {
        // Not supported
    }

    public void setChildren(String content, ParamList plist,
                            Token group) throws ParseException
    {
        // Not supported
    }

    public void setCompanies(String content, ParamList plist,
                             Token group) throws ParseException
    {
        // Not supported
    }

    public void setLanguages(String content, ParamList plist,
                             Token group) throws ParseException
    {
        // Not supported
    }

    public void setManager(String content, ParamList plist,
                           Token group) throws ParseException
    {
        // Not supported
    }

    public void setMileage(String content, ParamList plist,
                           Token group) throws ParseException
    {
        // Not supported
    }

    public void setSpouse(String content, ParamList plist,
                          Token group) throws ParseException
    {
        // Not supported
    }

    public void setSubject(String content, ParamList plist,
                           Token group) throws ParseException
    {
        // Not supported
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

    protected String preprocessNote(String text) {
        return text;
    }

    private boolean isSupportedField(PIMList list, int field) {
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

    protected boolean isSupportedAttributedField(PIMList list, int field, int attribute) {
        boolean supported = list.isSupportedField(field) &&
                            list.isSupportedAttribute(field, attribute);

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
}


