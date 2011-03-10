/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2003 - 2007 Funambol, Inc.
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
package com.funambol.common.pim.model.vcard;

import java.util.List;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Vector;

import com.funambol.common.pim.model.common.Property;
import com.funambol.common.pim.model.common.XTag;
import com.funambol.common.pim.model.common.BaseFormatter;
import com.funambol.common.pim.model.common.FormatterException;
import com.funambol.common.pim.model.contact.Address;
import com.funambol.common.pim.model.contact.Contact;
import com.funambol.common.pim.model.contact.Email;
import com.funambol.common.pim.model.contact.Name;
import com.funambol.common.pim.model.contact.Note;
import com.funambol.common.pim.model.contact.Phone;
import com.funambol.common.pim.model.contact.Title;
import com.funambol.common.pim.model.contact.WebPage;

/**
 * This object is a converter from a Contact object model to a vCard string
 */
public class VCardFormatter extends BaseFormatter {
    
    private String newLine = "\r\n"; // default

    // ------------------------------------------------------------- Constructor
    
    public VCardFormatter(TimeZone timezone, String charset) {
        super(timezone, charset);
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Performs the conversion.
     *
     * @param obj the Contact to be converted in vCard format
     * @return a string containing the vCard representation of this Contact
     * @throws FormatterException
     */
    public String format(Contact contact) throws FormatterException {
        return this.format(contact, null);
    }

    /**
     * Performs the conversion.
     *
     * @param obj the Contact to be converted in vCard format
     * @param supportedFields the list of supported fields
     * @return a string containing the vCard representation of this Contact
     * @throws FormatterException
     */
    public String format(Contact contact, Vector<String> supportedFields) throws FormatterException {

        this.supportedFields = supportedFields;

        StringBuffer output = new StringBuffer("BEGIN:VCARD" + newLine 
                + "VERSION:2.1" + newLine);

        if (contact.getName()!=null) {
            output.append(composeFieldName(contact.getName()));
            output.append(composeFieldFormalName(contact.getName().getDisplayName()));
            output.append(composeFieldNickname(contact.getName().getNickname()));
        }
        if (contact.getPersonalDetail()!= null) {
            output.append(composeFieldAddress(contact.getPersonalDetail().getAddress(), "HOME"));
            output.append(composeFieldAddress(contact.getPersonalDetail().getOtherAddress(), "OTHER"));
            output.append(composeFieldBirthday(contact.getPersonalDetail().getBirthday()));
            output.append(composeFieldOtherLabel(contact.getPersonalDetail().getOtherAddress().getLabel()));
            output.append(composeFieldPersonalLabel(contact.getPersonalDetail().getAddress().getLabel()));
            output.append(composeFieldTelephone(contact.getPersonalDetail().getPhones()));
            output.append(composeFieldEmail(contact.getPersonalDetail().getEmails()));
            output.append(composeFieldWebPage(contact.getPersonalDetail().getWebPages()));
            output.append(composeFieldAnniversary(contact.getPersonalDetail().getAnniversary()));
            output.append(composeFieldChildren(contact.getPersonalDetail().getChildren()));
            output.append(composeFieldSpouse(contact.getPersonalDetail().getSpouse()));
            output.append(composeFieldGeo(contact.getPersonalDetail().getGeo()));
        }
        if (contact.getBusinessDetail()!= null) {
            output.append(composeFieldAddress(contact.getBusinessDetail().getAddress(), "WORK"));
            output.append(composeFieldRole(contact.getBusinessDetail().getRole()));
            output.append(composeFieldTitle(contact.getBusinessDetail().getTitles()));
            output.append(composeFieldOrg(contact.getBusinessDetail().getCompany(),contact.getBusinessDetail().getDepartment()));
            output.append(composeFieldBusinessLabel(contact.getBusinessDetail().getAddress().getLabel()));
            output.append(composeFieldTelephone(contact.getBusinessDetail().getPhones()));
            output.append(composeFieldEmail(contact.getBusinessDetail().getEmails()));
            output.append(composeFieldWebPage(contact.getBusinessDetail().getWebPages()));
            output.append(composeFieldCompanies(contact.getBusinessDetail().getCompanies()));
            output.append(composeFieldManager(contact.getBusinessDetail().getManager()));
        }
        output.append(composeFieldNote(contact.getNotes()));
        output.append(composeFieldXTag(contact.getXTags()));
        output.append(composeFieldRevision(contact.getRevision()));
        output.append(composeFieldCategories(contact.getCategories()));
        output.append(composeFieldPhoto(contact.getPersonalDetail().getPhoto()));
        output.append(composeFieldUid(contact.getUid()));
        output.append(composeFieldFolder(contact.getFolder()));
        output.append(composeFieldFreeBusy(contact.getFreeBusy()));
        output.append(composeFieldLanguages(contact.getLanguages()));
        output.append(composeFieldMileage(contact.getMileage()));
        output.append(composeFieldSubject(contact.getSubject()));
        output.append(composeFieldTimezone(contact.getTimezone()));
        output.append(composeFieldAccessClass(contact.getSensitivity()));
        output.append(composeFieldMailer(contact.getMailer()));

        // Compose the remaining fields if the supported properties Vector is set
        output.append(composeRemainingFields());

        output.append("END:VCARD").append(newLine);
        return output.toString();
    }


    
    /**
     * Sets a new string as the new-line marker.
     *
     * @param newLine the string to use as a new-line marker
     */
    public void setNewLine(String newLine) {
        this.newLine = newLine;
    }
    
   /**
     * Returns the string used as the new-line marker.
     *
     * @return the string to use as a new-line marker
     */
    public String getNewLine() {
        return newLine;
    }
    
    // --------------------------------------------------------- Private methods

    /**
     * @return a representation of the v-card field N:
     */
    private StringBuffer composeFieldName(Name name) throws FormatterException {
        if (name.getLastName().getPropertyValue()   == null &&
            name.getFirstName().getPropertyValue()  == null &&
            name.getMiddleName().getPropertyValue() == null &&
            name.getSalutation().getPropertyValue() == null &&
            name.getSuffix().getPropertyValue()     == null   ) {
            return new StringBuffer(0);
        }

        StringBuffer output  = new StringBuffer(120); // Estimate 120 as needed
        ArrayList<Property> properties = new ArrayList<Property>();

        if (name.getLastName().getPropertyValue() != null) {
            output.append(
                escapeSeparator((String)name.getLastName().getPropertyValue())
            );
            properties.add(name.getLastName());
        }
        output.append(';');
        if (name.getFirstName().getPropertyValue() != null) {
            output.append(
                escapeSeparator((String)name.getFirstName().getPropertyValue())
            );
            properties.add(name.getFirstName());
        }
        output.append(';');
        if (name.getMiddleName().getPropertyValue() != null) {
            output.append(
                escapeSeparator((String)name.getMiddleName().getPropertyValue())
            );
            properties.add(name.getMiddleName());
        }
        output.append(';');
        if (name.getSalutation().getPropertyValue() != null) {
            output.append(
                escapeSeparator((String)name.getSalutation().getPropertyValue())
            );
            properties.add(name.getSalutation());
        }
        output.append(';');
        if (name.getSuffix().getPropertyValue() != null) {
            output.append(
                escapeSeparator((String)name.getSuffix().getPropertyValue())
            );
            properties.add(name.getSuffix());
        }

        return composeVCardComponent(output.toString(), properties, "N");
    }

    /**
     * @return a representation of the v-card field FN:
     */
    private StringBuffer composeFieldFormalName(Property displayName)
    throws FormatterException {
        if (displayName.getPropertyValue() != null) {

            ArrayList<Property> properties = new ArrayList<Property>();
            properties.add(displayName);

            return composeVCardComponent(escapeSeparator((String)displayName.getPropertyValue()),
                                         properties,
                                         "FN"      );
        }
        return new StringBuffer(0);
    }

    /**
     * @return a representation of the v-card field NICKNAME:
     */
    private StringBuffer composeFieldNickname(Property nickname)
    throws FormatterException {

        if (nickname.getPropertyValue() != null) {

            ArrayList<Property> properties = new ArrayList<Property>();
            properties.add(nickname);

            return composeVCardComponent(escapeSeparator((String)nickname.getPropertyValue()),
                                         properties,
                                         "NICKNAME");
        }
        return new StringBuffer(0);
    }

    /**
     * @return a representation of the v-card field ADR, ADR;HOME, ADR;WORK
     * if type = HOME  then set ADR;HOME
     * if type = OTHER then set ADR
     * if type = WORK  then set ADR;WORK
     */
    private StringBuffer composeFieldAddress(Address address, String type)
    throws FormatterException {
        if ((address == null) ||
            (address.getPostOfficeAddress().getPropertyValue() == null &&
             address.getRoomNumber().getPropertyValue()        == null &&
             address.getStreet().getPropertyValue()            == null &&
             address.getCity().getPropertyValue()              == null &&
             address.getState().getPropertyValue()             == null &&
             address.getPostalCode().getPropertyValue()        == null &&
             address.getCountry().getPropertyValue()           == null &&
             address.getExtendedAddress().getPropertyValue()   == null   ))
        {
            return new StringBuffer(0);
        }

        StringBuffer output  = new StringBuffer();
        ArrayList<Property> properties = new ArrayList<Property>();

        if (address.getPostOfficeAddress().getPropertyValue() != null) {
            output.append(escapeSeparator(
                (String)address.getPostOfficeAddress().getPropertyValue())
            );
            properties.add(address.getPostOfficeAddress());
        }
        output.append(';');
        if (address.getExtendedAddress().getPropertyValue() != null) {
            output.append(escapeSeparator(
                (String)address.getExtendedAddress().getPropertyValue())
            );
            properties.add(address.getExtendedAddress());
        }
        output.append(';');
        if (address.getStreet().getPropertyValue() != null) {
            output.append(
                escapeSeparator((String)address.getStreet().getPropertyValue())
            );
            properties.add(address.getStreet());
        }
        output.append(';');
        if (address.getCity().getPropertyValue() != null) {
            output.append(
                escapeSeparator((String)address.getCity().getPropertyValue())
            );
            properties.add(address.getCity());
        }
        output.append(';');
        if (address.getState().getPropertyValue() != null) {
            output.append(
                escapeSeparator((String)address.getState().getPropertyValue())
            );
            properties.add(address.getState());
        }
        output.append(';');
        if (address.getPostalCode().getPropertyValue() != null) {
            output.append(escapeSeparator(
                (String)address.getPostalCode().getPropertyValue())
            );
            properties.add(address.getPostalCode());
        }
        output.append(';');
        if (address.getCountry().getPropertyValue() != null) {
            output.append(
                escapeSeparator((String)address.getCountry().getPropertyValue())
            );
            properties.add(address.getCountry());
        }

        if (("HOME").equals(type)) {
            return composeVCardComponent(output.toString(),
                                         properties       ,
                                         "ADR;HOME"       );
        } else if (("OTHER").equals(type)) {
            return composeVCardComponent(output.toString(),
                                         properties       ,
                                         "ADR"            );
        } else if (("WORK").equals(type)) {
            return composeVCardComponent(output.toString(),
                                         properties       ,
                                         "ADR;WORK"       );
        }

        return new StringBuffer(0);
    }

    /**
     * @return a representation of the v-card field PHOTO:
     */
    private StringBuffer composeFieldPhoto(Property photo) throws FormatterException {
        if (photo.getPropertyValue() != null) {

            ArrayList<Property> properties = new ArrayList<Property>();
            properties.add(photo);

            //
            // The charset must be null (not set) since:
            // 1. it is useless since the content is in base64
            // 2. on some Nokia phone it doesn't work since for some reason the phone
            //    adds a new photo and the result is that a contact has two photos
            //    Examples of wrong phones: Nokia N91, 7610, 6630
            //
            return composeVCardComponent(photo.getPropertyValueAsString(),
                                         properties,
                                         "PHOTO"   ,
                                         true);   // true means "exclude the charset"
        }
        return new StringBuffer(0);
    }

    /**
     * @return a representation of the v-card field BDAY:
     */
    private String composeFieldBirthday(String birthday) throws FormatterException {
        if (birthday != null) {
            ArrayList<Property> properties = new ArrayList<Property>();
            return composeVCardComponent(escapeSeparator(birthday), properties, "BDAY").toString();
        }
        return "";
    }

    /**
     * @return a representation of the v-card field TEL:
     */
    private String composeFieldTelephone(List phones)
    throws FormatterException {

        if ((phones == null) || phones.isEmpty()) {
            return "";
        }

        StringBuffer output  = new StringBuffer();
        ArrayList<Property> properties = new ArrayList<Property>();

        Phone telephone = null;
        String phoneType   = null;

        int size = phones.size();
        for (int i=0; i<size; i++) {

            telephone = (Phone)phones.get(i);
            phoneType = composePhoneType(telephone.getPhoneType());

            properties.clear();
            properties.add(0, telephone);

            output.append(
                composeVCardComponent(escapeSeparator((String)telephone.getPropertyValue()),
                                      properties       ,
                                      "TEL" + phoneType)
            );
        }

        return output.toString();
    }

    /**
     * @return the v-card representation of a telephone type
     */
    private String composePhoneType(String type) {

        if (type == null) {
            return "";
        }

        for (int j=1; j<=10; j++) {
            if (Phone.getMobilePhoneNumberID(j).equals(type)) {
                return ";CELL";
            } else if (Phone.getMobileHomePhoneNumberID(j).equals(type)) {
                return ";CELL;HOME";
            } else if (Phone.getMobileBusinessPhoneNumberID(j).equals(type)) {
                return ";CELL;WORK";
            }

            //
            // Voice
            //
            if (Phone.getOtherPhoneNumberID(j).equals(type)) {
                return ";VOICE";
            } else if (Phone.getHomePhoneNumberID(j).equals(type)) {
                return ";VOICE;HOME";
            } else if (Phone.getBusinessPhoneNumberID(j).equals(type)) {
                return ";VOICE;WORK";
            }

            //
            // Fax
            //
            if (Phone.getOtherFaxNumberID(j).equals(type)) {
                return ";FAX";
            } else if (Phone.getHomeFaxNumberID(j).equals(type)) {
                return ";FAX;HOME";
            } else if (Phone.getBusinessFaxNumberID(j).equals(type)) {
                return ";FAX;WORK";
            }

            //
            // Pager
            //
            if (Phone.getPagerNumberID(j).equals(type)) {
                return ";PAGER";
            }

            //
            // Others
            //
            if (Phone.getCarPhoneNumberID(j).equals(type)) {
                return ";CAR;VOICE";
            } else if (Phone.getCompanyPhoneNumberID(j).equals(type)) {
                return ";WORK;PREF";
            } else if (Phone.getPrimaryPhoneNumberID(j).equals(type)) {
                return ";PREF;VOICE";
            } else if (Phone.getCallbackPhoneNumberID(j).equals(type)) {
                return ";X-FUNAMBOL-CALLBACK";
            } else if (Phone.getRadioPhoneNumberID(j).equals(type)) {
                return ";X-FUNAMBOL-RADIO";
            } else if (Phone.getTelexNumberID(j).equals(type)) {
                return ";X-FUNAMBOL-TELEX";
            } else if (Phone.getDCOnlyPhoneNumberID(j).equals(type)) {
                return ";X-DC";
            } else if (Phone.getMobileDCPhoneNumberID(j).equals(type)) {
                return ";CELL;X-DC";
            }
        }
        return "";
    }

    /**
     * @return a representation of the v-card field EMAIL:
     */
    private String composeFieldEmail(List emails) throws FormatterException {

        if ((emails == null) || emails.isEmpty()) {
            return "";
        }

        StringBuffer output  = new StringBuffer();
        ArrayList<Property> properties = new ArrayList<Property>();

        Email email = null;
        String emailType = null;

        int size = emails.size();
        for (int i=0; i<size; i++) {

            email = (Email)emails.get(i);
            emailType   = composeEmailType(email.getEmailType());

            properties.clear();
            properties.add(0, email);

            output.append(
                composeVCardComponent(escapeSeparator((String)email.getPropertyValue()),
                                      properties         ,
                                      "EMAIL" + emailType)
            );
        }

        return output.toString();
    }

    /**
     * @return the v-card representation of a email type
     */
    private String composeEmailType(String type) {
        if (type == null) {
            return "";
        }

        if (("Email1Address").equals(type)) {
            return ";INTERNET";
        } else if (("Email2Address").equals(type)) {
            return ";INTERNET;HOME";
        } else if (("Email3Address").equals(type)) {
            return ";INTERNET;WORK";
        }

        for (int j=2; j<=10; j++) {
            if (("Other" + j + "EmailAddress").equals(type)) {
                return ";INTERNET";
            } else if (("HomeEmail" + j +"Address").equals(type)) {
                return ";INTERNET;HOME";
            } else if (("BusinessEmail" + j +"Address").equals(type)) {
                return ";INTERNET;WORK";
            }
        }
        
        if (("IMAddress").equals(type)) {
            return ";INTERNET;HOME;X-FUNAMBOL-INSTANTMESSENGER";
        }

        if (("MobileEmailAddress").equals(type)) {
            return ";X-CELL";
        }

        return "";
    }

    private String composeFieldWebPage(List webpages)
    throws FormatterException {

        if ((webpages == null) || webpages.isEmpty()) {
            return "";
        }

        StringBuffer output  = new StringBuffer();
        ArrayList<Property> properties = new ArrayList<Property>();

        WebPage address   = null;
        String webpageType = null;

        int size = webpages.size();
        for (int i=0; i<size; i++) {

            address = (WebPage)webpages.get(i);
            webpageType = composeWebPageType(address.getWebPageType());

            properties.add(0, address);

            output.append(
                composeVCardComponent(escapeSeparator((String)address.getPropertyValue()),
                                      properties ,
                                      webpageType)
            );
        }

        return output.toString();
    }

   /**
    * @return the v-card representation of a web page type
    */
    private String composeWebPageType(String type) {
        if (type == null) {
            return "";
        } else if (("WebPage").equals(type)) {
            return "URL";
        } else if (("HomeWebPage").equals(type)) {
            return "URL;HOME";
        } else if (("BusinessWebPage").equals(type)) {
            return "URL;WORK";
        }

        for (int j=2; j<=10; j++) {
            if (("WebPage" + j).equals(type)) {
                return "URL";
            } else if (("Home" + j +"WebPage").equals(type)) {
                return "URL;HOME";
            } else if (("Business" + j +"WebPage").equals(type)) {
                return "URL;WORK";
            }
        }

        return "";
   }
    
    private String composeFieldFreeBusy(String freeBusy) throws FormatterException {
        if (freeBusy != null && freeBusy.length() > 0 ) {
            ArrayList<Property> properties = new ArrayList<Property>();
            StringBuffer output =
                    composeVCardComponent(freeBusy, properties, "FBURL");
            return output.toString();
        }
        return "";
    }

    /**
     * @return a representation of the v-card field LABEL;HOME:
     */
    private StringBuffer composeFieldPersonalLabel(Property label)
    throws FormatterException {

        if (label.getPropertyValue() != null) {

            ArrayList<Property> properties = new ArrayList<Property>();
            properties.add(label);

            return composeVCardComponent(escapeSeparator((String)label.getPropertyValue()),
                                         properties  ,
                                         "LABEL;HOME");
        }
        return new StringBuffer(0);
    }

    /**
     * @return a representation of the v-card field LABEL;OTHER:
     */
    private StringBuffer composeFieldOtherLabel(Property label)
    throws FormatterException {

        if (label.getPropertyValue() != null) {

            ArrayList<Property> properties = new ArrayList<Property>();
            properties.add(label);

            return composeVCardComponent(escapeSeparator((String)label.getPropertyValue()),
                                         properties   ,
                                         "LABEL;OTHER");
        }
        return new StringBuffer(0);
    }

    /**
     * @return a representation of the v-card field LABEL;WORK:
     */
     private StringBuffer composeFieldBusinessLabel(Property label)
    throws FormatterException {

        if (label.getPropertyValue() != null) {

            ArrayList<Property> properties = new ArrayList<Property>();
            properties.add(label);

            return composeVCardComponent(escapeSeparator((String)label.getPropertyValue()),
                                         properties  ,
                                         "LABEL;WORK");
        }
        return new StringBuffer(0);
    }

    /**
     * @return a representation of the v-card field ROLE:
     */
    private StringBuffer composeFieldRole(Property role) throws FormatterException {

        if (role.getPropertyValue() != null) {

            ArrayList<Property> properties = new ArrayList<Property>();
            properties.add(role);

            return composeVCardComponent(escapeSeparator((String)role.getPropertyValue()),
                                         properties,
                                         "ROLE"    );
        }
        return new StringBuffer(0);
    }

    /**
     * @return a representation of the v-card field TITLE:
     */
    private String composeFieldTitle(List titles) throws FormatterException {
        if ((titles == null) || titles.isEmpty()) {
            return "";
        }

        StringBuffer output  = new StringBuffer();
        ArrayList<Property> properties = new ArrayList<Property>();

        Title title = null;

        int size = titles.size();
        for (int i=0; i<size; i++) {

            title = (Title)titles.get(i);
            properties.add(0, title);

            output.append(
                composeVCardComponent(escapeSeparator((String)title.getPropertyValue()),
                                       properties,
                                       "TITLE"   )
            );
        }

        return output.toString();
    }

    /**
     * @return a representation of the v-card field ORG:
     */
    private StringBuffer composeFieldOrg(Property company, Property department)
    throws FormatterException {
        if (company.getPropertyValue()    == null &&
            department.getPropertyValue() == null   ) {
            return new StringBuffer(0);
        }

        StringBuffer output  = new StringBuffer();
        ArrayList<Property> properties = new ArrayList<Property>();

        if (company.getPropertyValue() != null) {
            output.append(escapeSeparator((String)company.getPropertyValue()));
            properties.add(company);
        }
        output.append(';');
        if (department.getPropertyValue() != null) {
            output.append(escapeSeparator((String)department.getPropertyValue()));
            properties.add(department);
        }

        return composeVCardComponent(output.toString(),
                                     properties       ,
                                     "ORG"            );
    }

    /**
     * @return a representation of the v-card field XTag:
     */
    private String composeFieldXTag(List xTags)
    throws FormatterException {
        if ((xTags == null) || xTags.isEmpty()) {
            return "";
        }

        StringBuffer output  = new StringBuffer();
        ArrayList<Property> properties = new ArrayList<Property>();

        Property xtag = null;

        int size = xTags.size();
        for (int i=0; i<size; i++) {

            XTag xtagObj = (XTag)xTags.get(i);

            xtag  = xtagObj.getXTag();

            properties.clear();
            properties.add(0, xtag);

            output.append(
                composeVCardComponent(escapeSeparator((String)xtag.getPropertyValue()),
                                                      properties                      ,
                                                      xtagObj.getXTagValue())
            );
        }
        return output.toString();
    }

    /**
     * @return a representation of the v-card field NOTE:
     */
    private String composeFieldNote(List notes) throws FormatterException {

        if ((notes == null) || notes.isEmpty()) {
            return "";
        }

        StringBuffer output = new StringBuffer();
        ArrayList<Property> properties = new ArrayList<Property>();

        Note note = null;

        int size = notes.size();
        for (int i=0; i<size; i++) {

            note = (Note)notes.get(i);
            properties.add(0, note);

            output.append(
                composeVCardComponent(escapeSeparator((String)note.getPropertyValue()),
                                       properties,
                                       "NOTE"    )
            );
        }

        return output.toString();
    }

    /**
     * @return a representation of the v-card field UID:
     */
    private String composeFieldUid(String uid) throws FormatterException {
        if (uid!=null) {
            ArrayList<Property> properties = new ArrayList<Property>();
            return composeVCardComponent(uid, properties, "UID").toString();
        }
        return "";
    }

    /**
     * @return a representation of the v-card field TZ:
     */
    private String composeFieldTimezone(String tz) throws FormatterException {
        if (tz != null) {
            ArrayList<Property> properties = new ArrayList<Property>();
            return composeVCardComponent(tz, properties, "TZ").toString();
        }
        return "";
    }

    /**
     * @return a representation of the v-card field REV:
     */
    private String composeFieldRevision(String revision) throws FormatterException {
        if (revision != null) {
            ArrayList<Property> properties = new ArrayList<Property>();
            return composeVCardComponent(revision, properties, "REV").toString();
        }
        return "";
    }

    /**
     * @return a representation of the v-card field CATEGORIES:
     */
    private StringBuffer composeFieldCategories(Property categories)
    throws FormatterException {

        if (categories.getPropertyValue() != null) {

            ArrayList<Property> properties = new ArrayList<Property>();
            properties.add(categories);

            return composeVCardComponent(escapeSeparator((String)categories.getPropertyValue()),
                                         properties  ,
                                         "CATEGORIES");
        }
        return new StringBuffer(0);
    }

    /**
     * @return a representation of the v-card field CLASS:
     */
    private String composeFieldAccessClass(Short sensitivity) throws FormatterException {

        if (sensitivity == null) {
            return "";
        }

        String accessClass;
        if (Contact.SENSITIVITY_NORMAL.equals(sensitivity)) {
                accessClass = Contact.CLASS_PUBLIC;
        } else if (Contact.SENSITIVITY_PRIVATE.equals(sensitivity)) {
                accessClass = Contact.CLASS_PRIVATE;
        } else if (Contact.SENSITIVITY_CONFIDENTIAL.equals(sensitivity)) {
                accessClass = Contact.CLASS_CONFIDENTIAL;
        } else {
                accessClass = Contact.CLASS_CUSTOM;
        }
        ArrayList<Property> properties = new ArrayList<Property>();
        return composeVCardComponent(escapeSeparator(accessClass),
                    properties, "CLASS").toString();
    }

    /**
     * @return a representation of the vCard field GEO:
     */
    private StringBuffer composeFieldGeo(Property geo)
    throws FormatterException {

        if (geo.getPropertyValue() != null) {

            ArrayList<Property> properties = new ArrayList<Property>();
            properties.add(geo);

            return composeVCardComponent(geo.getPropertyValueAsString(),
                                         properties                    ,
                                         "GEO");
        }
        return new StringBuffer(0);
    }

    /**
     * @return a representation of the vCard field MAILER
     */
    private String composeFieldMailer(String mailer) throws FormatterException {
        if (mailer != null) {
            ArrayList<Property> properties = new ArrayList<Property>();
            return composeVCardComponent(escapeSeparator(mailer),
                    properties, "MAILER").toString();
        }
        return "";
    }
    
    /**
     * @return a representation of the vCard field X-FUNAMBOL-FOLDER
     */
    private String composeFieldFolder(String folder) throws FormatterException {
        if (folder != null) {
            ArrayList<Property> properties = new ArrayList<Property>();
            return composeVCardComponent(escapeSeparator(folder),
                    properties, "X-FUNAMBOL-FOLDER").toString();
        }
        return "";
    }
    
    /**
     * Returns a custom vCard representation of the Anniversary property.
     * 
     * @return a representation of the vCard field X-ANNIVERSARY
     */
    private String composeFieldAnniversary(String anniversary) throws FormatterException {
        if (anniversary != null) {
            ArrayList<Property> properties = new ArrayList<Property>();
            return composeVCardComponent(escapeSeparator(anniversary),
                    properties, "X-ANNIVERSARY").toString();
        }
        return "";
    }
    
    /**
     * Returns a custom vCard representation of the Children property.
     * 
     * @return a representation of the vCard field X-CHILDREN
     */
    private String composeFieldChildren(String children) throws FormatterException {
        if (children != null) {
            ArrayList<Property> properties = new ArrayList<Property>();
            return composeVCardComponent(children, properties,
                    "X-FUNAMBOL-CHILDREN").toString();
        }
        return "";
    }

    /**
     * Returns a custom vCard representation of the Companies property.
     * 
     * @return a representation of the vCard field X-FUNAMBOL-COMPANIES
     */
    private String composeFieldCompanies(String companies) throws FormatterException {
        if (companies != null) {
            ArrayList<Property> properties = new ArrayList<Property>();
            return composeVCardComponent(companies, properties,
                    "X-FUNAMBOL-COMPANIES").toString();
        }
        return "";
    }
    
    /**
     * Returns a custom vCard representation of the Language property.
     * 
     * @return a representation of the vCard field X-FUNAMBOL-LANGUAGES
     */
    private String composeFieldLanguages(String languages) throws FormatterException {
        if (languages != null) {
            ArrayList<Property> properties = new ArrayList<Property>();
            return composeVCardComponent(languages, properties,
                    "X-FUNAMBOL-LANGUAGES").toString();
        }
        return "";
    }
    
    /**
     * Returns a custom vCard representation of the ManagerName property.
     * 
     * @return a representation of the vCard field X-MANAGER
     */
    private String composeFieldManager(String manager) throws FormatterException {
        if (manager != null) {
            ArrayList<Property> properties = new ArrayList<Property>();
            return composeVCardComponent(manager, properties,
                    "X-MANAGER").toString();
        }
        return "";
    }
    
    /**
     * Returns a custom vCard representation of the Mileage property.
     * 
     * @return a representation of the vCard field X-FUNAMBOL-MILEAGE
     */
    private String composeFieldMileage(String mileage) throws FormatterException {
        if (mileage != null) {
            ArrayList<Property> properties = new ArrayList<Property>();
            return composeVCardComponent(escapeSeparator(mileage), properties,
                    "X-FUNAMBOL-MILEAGE").toString();
        }
        return "";
    }
    
    /**
     * Returns a custom vCard representation of the Spouse property.
     * 
     * @return a representation of the vCard field X-SPOUSE
     */
    private String composeFieldSpouse(String spouse) throws FormatterException {
        if (spouse != null) {
            ArrayList<Property> properties = new ArrayList<Property>();
            return composeVCardComponent(escapeSeparator(spouse), properties,
                    "X-SPOUSE").toString();
        }
        return "";
    }
    
    /**
     * Returns a custom vCard representation of the Subject property.
     * 
     * @return a representation of the vCard field X-FUNAMBOL-SUBJECT
     */
    private String composeFieldSubject(String subject) throws FormatterException {
        if (subject != null) {
            ArrayList<Property> properties = new ArrayList<Property>();
            return composeVCardComponent(escapeSeparator(subject), properties,
                    "X-FUNAMBOL-SUBJECT").toString();
        }
        return "";
    }
}
