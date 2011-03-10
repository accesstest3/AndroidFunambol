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
package com.funambol.common.pim.model.vcard;

import java.util.TimeZone;
import java.util.List;
import java.util.ArrayList;
import java.io.UnsupportedEncodingException;

import com.funambol.common.pim.FieldsList;
import com.funambol.common.pim.ParamList;
import com.funambol.common.pim.vcard.Token;
import com.funambol.common.pim.vcard.ParseException;
import com.funambol.common.pim.vcard.AbstractVCardSyntaxParserListener;
import com.funambol.common.pim.model.common.Property;
import com.funambol.common.pim.model.common.XTag;
import com.funambol.common.pim.model.contact.Contact;
import com.funambol.common.pim.model.contact.Email;
import com.funambol.common.pim.model.contact.Note;
import com.funambol.common.pim.model.contact.Phone;
import com.funambol.common.pim.model.contact.Title;
import com.funambol.common.pim.model.contact.WebPage;
import com.funambol.util.QuotedPrintable;
import com.funambol.util.Log;

/**
 * Represents a VCardSyntaxParserListener used in the pim-framework
 */
public class VCardSyntaxParserListenerImpl extends AbstractVCardSyntaxParserListener {

    private static final String TAG = "VCardSyntaxParserListenerImpl";

    private TimeZone defaultTimeZone;
    private String defaultCharset;

    private int cellTel        = 0;
    private int cellHomeTel    = 0;
    private int cellWorkTel    = 0;
    private int voiceTel       = 0;
    private int voiceHomeTel   = 0;
    private int voiceWorkTel   = 0;
    private int fax            = 0;
    private int faxHome        = 0;
    private int faxWork        = 0;
    private int car            = 0;
    private int pager          = 0;
    private int primary        = 0;
    private int companyMain    = 0;

    // the email
    private int email          = 0;
    private int emailHome      = 0;
    private int emailWork      = 0;
    private int emailMobile    = 0;

    // the body
    private int note           = 0;

    // the web page
    private int webPage        = 0;
    private int webPageHome    = 0;
    private int webPageWork    = 0;

    // the job title
    private int title          = 0;

    // the addresses
    private int addressHome    = 0;
    private int addressWork    = 0;
    private int addressOther   = 0;

    private Contact contact;

    public VCardSyntaxParserListenerImpl(Contact contact, TimeZone defaultTimeZone,
                                         String defaultCharset) {
        this.contact = contact;
        this.defaultTimeZone = defaultTimeZone;
        this.defaultCharset = defaultCharset;
    }

    public void start() {
    }

    public void end() {
    }

    @Override
    public void setCategories(String content, ParamList plist,
                              Token group) throws ParseException
    {
        contact.getCategories().setPropertyValue(unfoldDecodeUnescape(content, plist));
        setParameters(contact.getCategories(), plist, group);
    }

    @Override
    public void addExtension(String tagName, String content, ParamList plist,
                             Token group) throws ParseException
    {
        XTag tmpxTag = new XTag();
        tmpxTag.getXTag().setPropertyValue(unfoldDecodeUnescape(content, plist));
        setParameters(tmpxTag.getXTag(), plist, group);

        tmpxTag.setXTagValue(tagName);
        contact.addXTag(tmpxTag);
    }

    @Override
    public void setVersion(String ver, ParamList plist,
                           Token group) throws ParseException
    {
        Log.trace(TAG, "setVersion");
        if (!(ver.equals("2.1")) && !(ver.equals("3.0"))) {
            throw new ParseException("Encountered a vCard version other than 2.1 or 3.0 ("+ver+")");
        }
    }

    @Override
    public void setTitle(String content, ParamList plist,
                         Token group) throws ParseException
    {
        Log.trace(TAG, "setTitle");

        Title tmptitle = new Title(unfoldDecodeUnescape(content, plist));
        setParameters(tmptitle, plist, group);

        if (title == 0) {
            if (contact.getBusinessDetail().getTitles() == null) {
                contact.getBusinessDetail().setTitles(new ArrayList());
            }
            tmptitle.setTitleType("JobTitle");
        } else {
            tmptitle.setTitleType("JobTitle" + (title + 1));
        }
        contact.getBusinessDetail().addTitle(tmptitle);
        title++;
    }

    @Override
    public void setMail(String content, ParamList plist,
                        Token group) throws ParseException
    {
        /*
         * NOTE: The email addresses supported by Microsoft Outlook are
         * Email1Address, Email2Address and Email3Address.
         * So, the first EMAIL;INTERNET property is Email1Address, and the other
         * EMAIL;INTERNET properties are labeled as OtherEmail2Address,
         * OtherEmail3Address...
         * The first EMAIL;INTERNET;HOME property is Email2Address, and the
         * others are HomeEmail2Address etc.
         * The first EMAIL;INTERNET;WORK property is Email3Address, and the
         * others are BusinessEmail2Address etc.
         * EMAIL;INTERNET;HOME;X-FUNAMBOL-INSTANTMESSENGER is IMAddress.
         * EMAIL;X-CELL is MobileEmailAddress.         *
         * If the email type is not specified, treat the first one as
         * Email1Address and the others as OtherEmail2Address etc.
         */
        if ((emailMobile == 0) && (plist.getXParams().containsKey("X-CELL"))) {
            Email tmpmail = new Email(unfoldDecodeUnescape(content, plist));
            setParameters(tmpmail,plist,group);
            if (emailHome == 0) {
                if (contact.getPersonalDetail().getEmails() == null) {
                    contact.getPersonalDetail().setEmails(new ArrayList());
                }
            }
            tmpmail.setEmailType("MobileEmailAddress");
            contact.getPersonalDetail().addEmail(tmpmail);
            emailMobile++;
        } else if (plist.getSize() == 0                                       ||
                  (plist.getSize() == 1 && plist.containsKey("INTERNET"))     ||
                  (plist.containsKey("PREF") && plist.containsKey("INTERNET"))
        ) {
            Email tmpmail = new Email(unfoldDecodeUnescape(content, plist));
            setParameters(tmpmail,plist,group);
            if (email == 0) {
                if (emailHome == 0) {
                    if (contact.getPersonalDetail().getEmails() == null) {
                        contact.getPersonalDetail().setEmails(new ArrayList());
                    }
                }
                tmpmail.setEmailType("Email1Address");
            } else {
                tmpmail.setEmailType("OtherEmail" + (email + 1) + "Address");
            }
            contact.getPersonalDetail().addEmail(tmpmail);
            email++;
        } else if (plist.containsKey("HOME")) {

            Email tmpmail = new Email(unfoldDecodeUnescape(content, plist));
            setParameters(tmpmail,plist,group);

            if (plist.getXParams().containsKey("X-FUNAMBOL-INSTANTMESSENGER")) {
                if (email == 0) {
                    if (contact.getPersonalDetail().getEmails() == null) {
                        contact.getPersonalDetail().setEmails(new ArrayList());
                    }
                }
                tmpmail.setEmailType("IMAddress");
                contact.getPersonalDetail().addEmail(tmpmail);
            } else {
            if (emailHome == 0) {
                if (email == 0) {
                    if (contact.getPersonalDetail().getEmails() == null) {
                        contact.getPersonalDetail().setEmails(new ArrayList());
                    }
                }
                tmpmail.setEmailType("Email2Address");
            } else {
                tmpmail.setEmailType("HomeEmail" + (emailHome + 1) + "Address");
            }
            contact.getPersonalDetail().addEmail(tmpmail);
            emailHome++;
            }
        } else if (plist.containsKey("WORK")) {
            Email tmpmail = new Email(unfoldDecodeUnescape(content, plist));
            setParameters(tmpmail,plist,group);
            if (emailWork == 0) {
                if (contact.getBusinessDetail().getEmails() == null) {
                    contact.getBusinessDetail().setEmails(new ArrayList());
                }
                tmpmail.setEmailType("Email3Address");
            } else {
                tmpmail.setEmailType("BusinessEmail" + (emailWork + 1) + "Address");
            }
            contact.getBusinessDetail().addEmail(tmpmail);
            emailWork++;
        } else {
            Email tmpmail = new Email(unfoldDecodeUnescape(content, plist));
            setParameters(tmpmail,plist,group);
            if (email == 0) {
                if (emailHome == 0) {
                    if (contact.getPersonalDetail().getEmails() == null) {
                        contact.getPersonalDetail().setEmails(new ArrayList());
                    }
                }
                tmpmail.setEmailType("Email1Address");
            } else {
                tmpmail.setEmailType("OtherEmail" + (email + 1) + "Address");
            }
            contact.getPersonalDetail().addEmail(tmpmail);
            email++;
        }
    }

    @Override
    public void setUrl(String content, ParamList plist,
                       Token group) throws ParseException
    {
        if (!plist.containsKey("HOME") && !plist.containsKey("WORK")) {
            WebPage tmppage = new WebPage();
            tmppage.setPropertyValue(unfoldDecodeUnescape(content, plist));
            setParameters(tmppage,plist,group);

            if (webPage == 0) {
                if (webPageHome == 0) {
                    if (contact.getPersonalDetail().getWebPages() == null) {
                        contact.getPersonalDetail().setWebPages(new ArrayList());
                    }
                }
                tmppage.setWebPageType("WebPage");
            } else {
                tmppage.setWebPageType("WebPage" + (webPage + 1));
            }
            contact.getPersonalDetail().addWebPage(tmppage);
            webPage++;
        }

        if (plist.containsKey("HOME")) {
            WebPage tmppage = new WebPage();
            tmppage.setPropertyValue(unfoldDecodeUnescape(content, plist));
            setParameters(tmppage,plist,group);

            if (webPageHome == 0) {
                if (webPage == 0) {
                    if (contact.getPersonalDetail().getWebPages() == null) {
                        contact.getPersonalDetail().setWebPages(new ArrayList());
                    }
                }
                tmppage.setWebPageType("HomeWebPage");
            } else {
                tmppage.setWebPageType("Home" + (webPageHome + 1) + "WebPage");
            }
            contact.getPersonalDetail().addWebPage(tmppage);
            webPageHome++;
        }

        if (plist.containsKey("WORK")) {
            WebPage tmppage = new WebPage();
            tmppage.setPropertyValue(unfoldDecodeUnescape(content, plist));
            setParameters(tmppage,plist,group);

            if (webPageWork == 0) {
                if (contact.getBusinessDetail().getWebPages() == null) {
                    contact.getBusinessDetail().setWebPages(new ArrayList());
                }
                tmppage.setWebPageType("BusinessWebPage");
            } else {
                tmppage.setWebPageType("Business" + (webPageWork + 1) + "WebPage");
            }
            contact.getBusinessDetail().addWebPage(tmppage);
            webPageWork++;
        }
    }

    @Override
    public void setTelephone(String content, ParamList plist,
                             Token group) throws ParseException
    {
        Log.trace(TAG, "setTelephone");

        content = unfoldDecodeUnescape(content, plist);
        List<String> telPlist=new ArrayList<String>();
        String[] telParameters =
        {
            "PREF",
            "WORK",
            "HOME",
            "VOICE",
            "FAX",
            "MSG",
            "CELL",
            "PAGER",
            "BBS",
            "MODEM",
            "CAR",
            "ISDN",
            "VIDEO",
            "X-FUNAMBOL-RADIO",
            "X-FUNAMBOL-CALLBACK",
            "X-FUNAMBOL-TELEX",
            "X-DC"
        };
        for (String parameter : telParameters) {
            if (plist.containsKey(parameter) ||
                plist.getXParams().containsKey(parameter)) {
                telPlist.add(parameter);
            }
        }

        if (telPlist.contains("WORK")) {
            Phone tmphone = new Phone(content);
            setParameters(tmphone, plist, group);
            // Check if it is the very first for a business detail.
            if ((cellWorkTel == 0) && (voiceWorkTel == 0) &&
                (faxWork == 0) && (pager == 0) && (primary == 0) &&
                (companyMain == 0)) {
                    if (contact.getBusinessDetail().getPhones() == null) {
                        contact.getBusinessDetail().setPhones(new ArrayList());
                    }
            }

            if (telPlist.contains("CELL")) {
                if (cellWorkTel == 0) {
                    tmphone.setPhoneType("MobileBusinessTelephoneNumber");
                } else {
                    tmphone.setPhoneType("MobileBusiness" + (cellWorkTel + 1) + "TelephoneNumber");
                }
                contact.getBusinessDetail().addPhone(tmphone);
                cellWorkTel++;
            }

            if (telPlist.contains("VOICE") || (telPlist.size() == 1)) {
                if (voiceWorkTel == 0) {
                    tmphone.setPhoneType("BusinessTelephoneNumber");
                } else {
                    tmphone.setPhoneType("Business" + (voiceWorkTel + 1) + "TelephoneNumber");
                }
                contact.getBusinessDetail().addPhone(tmphone);
                voiceWorkTel++;
            }
            if (telPlist.contains("FAX")) {
                if (faxWork == 0) {
                    tmphone.setPhoneType("BusinessFaxNumber");
                } else {
                    tmphone.setPhoneType("Business" + (faxWork + 1) + "FaxNumber");
                }
                contact.getBusinessDetail().addPhone(tmphone);
                faxWork++;
            }
            // suppose that can exists only one voice work telephone pref.
            if ((companyMain == 0) && telPlist.contains("PREF")) {
                tmphone.setPhoneType("CompanyMainTelephoneNumber");
                contact.getBusinessDetail().addPhone(tmphone);
                companyMain++;
            }

        } else if ((telPlist.contains("CELL") && telPlist.size() == 1) ||
            (telPlist.contains("CELL") && telPlist.contains("VOICE"))) {
            Phone tmphone = new Phone(content);
            setParameters(tmphone, plist, group);

            if (cellTel == 0) {
                if ((cellHomeTel == 0) && (voiceTel == 0) &&
                    (voiceHomeTel == 0) && (fax == 0)     &&
                    (faxHome == 0) && (car == 0)            ) {
                    if (contact.getPersonalDetail().getPhones() == null) {
                        contact.getPersonalDetail().setPhones(new ArrayList());
                    }
                }
                tmphone.setPhoneType("MobileTelephoneNumber");
            } else {
                tmphone.setPhoneType("Mobile" + (cellTel + 1) + "TelephoneNumber");
            }
            contact.getPersonalDetail().addPhone(tmphone);
            cellTel++;

        } else if (telPlist.contains("HOME") && telPlist.contains("CELL")) {
            Phone tmphone = new Phone(content);
            setParameters(tmphone, plist, group);
            if (cellHomeTel == 0) {
                if ((cellTel == 0) && (voiceTel == 0) &&
                    (voiceHomeTel == 0) && (fax == 0) &&
                    (faxHome == 0) && (car == 0)        ) {
                    if (contact.getPersonalDetail().getPhones() == null) {
                        contact.getPersonalDetail().setPhones(new ArrayList());
                    }
                }
                tmphone.setPhoneType("MobileHomeTelephoneNumber");
            } else {
                tmphone.setPhoneType("MobileHome" + (cellHomeTel + 1) + "TelephoneNumber");
            }
            contact.getPersonalDetail().addPhone(tmphone);
            cellHomeTel++;

        } else if ((telPlist.size() == 1 && telPlist.contains("VOICE")) ||
            (telPlist.size() == 0)) {
            Phone tmphone = new Phone(content);
            setParameters(tmphone,plist,group);
            if (voiceTel == 0) {
                if ((cellTel == 0) && (cellHomeTel == 0) &&
                    (voiceHomeTel == 0) && (fax == 0)    &&
                    (faxHome == 0) && (car == 0)           ) {
                    if (contact.getPersonalDetail().getPhones() == null) {
                        contact.getPersonalDetail().setPhones(new ArrayList());
                   }
                }
                tmphone.setPhoneType("OtherTelephoneNumber");
            } else {
                tmphone.setPhoneType("Other" + (voiceTel + 1) + "TelephoneNumber");
            }
            contact.getPersonalDetail().addPhone(tmphone);
            voiceTel++;

        } else if ((telPlist.contains("VOICE") && telPlist.contains("HOME"))  ||
            (telPlist.size() == 1 && telPlist.contains("HOME"))  )       {
            Phone tmphone = new Phone(content);
            setParameters(tmphone, plist, group);
            if (voiceHomeTel == 0) {
                if ((cellTel == 0) && (cellHomeTel == 0) && (voiceTel == 0) &&
                    (fax == 0) && (faxHome == 0) && (car == 0)               ) {
                    if (contact.getPersonalDetail().getPhones() == null) {
                        contact.getPersonalDetail().setPhones(new ArrayList());
                    }
                }
                tmphone.setPhoneType("HomeTelephoneNumber");
            } else {
                tmphone.setPhoneType("Home" + (voiceHomeTel + 1) + "TelephoneNumber");
            }
            contact.getPersonalDetail().addPhone(tmphone);
            voiceHomeTel++;

        } else if (telPlist.size() == 1 && telPlist.contains("FAX")) {
            Phone tmphone = new Phone(content);
            setParameters(tmphone, plist, group);
            if (fax == 0) {
                if ((cellTel == 0) && (cellHomeTel == 0) && (voiceTel == 0) &&
                    (voiceHomeTel == 0) && (faxHome == 0) && (car == 0)      ) {
                    if (contact.getPersonalDetail().getPhones() == null) {
                        contact.getPersonalDetail().setPhones(new ArrayList());
                    }
                }
                tmphone.setPhoneType("OtherFaxNumber");
            } else {
                tmphone.setPhoneType("Other" + (fax + 1) + "FaxNumber");
            }
            contact.getPersonalDetail().addPhone(tmphone);
            fax++;

        } else if (telPlist.contains("HOME") && telPlist.contains("FAX")) {
            Phone tmphone = new Phone(content);
            setParameters(tmphone,plist,group);
            if (faxHome == 0) {
                if ((cellTel == 0) && (cellHomeTel == 0) && (voiceTel == 0) &&
                    (voiceHomeTel == 0) && (fax == 0) && (car == 0)          ) {

                    if (contact.getPersonalDetail().getPhones() == null) {
                        contact.getPersonalDetail().setPhones(new ArrayList());
                    }
                }
                tmphone.setPhoneType("HomeFaxNumber");
            } else {
                tmphone.setPhoneType("Home" + (faxHome + 1) + "FaxNumber");
            }
            contact.getPersonalDetail().addPhone(tmphone);
            faxHome++;

        } else if (telPlist.contains("CAR")) {
            Phone tmphone = new Phone(content);
            setParameters(tmphone,plist,group);
            tmphone.setPhoneType("CarTelephoneNumber");
            if ((car == 0) && (cellTel == 0) && (cellHomeTel == 0)   &&
                (voiceTel == 0) && (voiceHomeTel == 0) && (fax == 0) &&
                (faxHome == 0)                                         ) {
                if (contact.getPersonalDetail().getPhones() == null) {
                    contact.getPersonalDetail().setPhones(new ArrayList());
                }
            }
            contact.getPersonalDetail().addPhone(tmphone);
            car++;

        } else if (telPlist.contains("PAGER")) {
            Phone tmphone = new Phone(content);
            setParameters(tmphone,plist,group);
            if (pager == 0) {
                if ((cellWorkTel == 0) && (voiceWorkTel == 0)              &&
                    (faxWork == 0) && (primary == 0) && (companyMain == 0)   ) {
                    if (contact.getBusinessDetail().getPhones() == null) {
                        contact.getBusinessDetail().setPhones(new ArrayList());
                    }
                }
                tmphone.setPhoneType("PagerNumber");
            } else {
                tmphone.setPhoneType("PagerNumber" + (pager + 1));
            }
            contact.getBusinessDetail().addPhone(tmphone);
            pager++;

        } else if ((primary == 0) && // suppose that can exists only one voice telephone pref.
            ((telPlist.contains("PREF") && telPlist.contains("VOICE")) ||
            (telPlist.contains("PREF") && telPlist.size() == 1))) {

            Phone tmphone = new Phone(content);
            setParameters(tmphone,plist,group);
            if ((primary == 0) && (cellWorkTel == 0) && (voiceWorkTel == 0) &&
                (faxWork == 0) && (pager == 0) && (companyMain == 0)         ) {
                if (contact.getPersonalDetail().getPhones() == null) {
                    contact.getPersonalDetail().setPhones(new ArrayList());
                }
            }
            tmphone.setPhoneType("PrimaryTelephoneNumber");
            contact.getPersonalDetail().addPhone(tmphone);
            primary++;

        } else if (telPlist.contains("X-FUNAMBOL-CALLBACK")) {
            Phone tmphone = new Phone(content);
            setParameters(tmphone, plist, group);
            tmphone.setPhoneType("CallbackTelephoneNumber");
            if ((cellWorkTel == 0) && (voiceWorkTel == 0) &&
                (faxWork == 0) && (primary == 0) && (companyMain == 0)   ) {
                if (contact.getBusinessDetail().getPhones() == null) {
                    contact.getBusinessDetail().setPhones(new ArrayList());
                }
            }
            contact.getBusinessDetail().addPhone(tmphone);

        } else if (telPlist.contains("X-FUNAMBOL-RADIO")) {
            Phone tmphone = new Phone(content);
            setParameters(tmphone, plist, group);
            tmphone.setPhoneType("RadioTelephoneNumber");
            if ((faxHome == 0) && (cellTel == 0) && (cellHomeTel == 0) &&
                (voiceTel == 0) && (voiceHomeTel == 0) && (fax == 0) &&
                (car == 0)) {
                if (contact.getPersonalDetail().getPhones() == null) {
                    contact.getPersonalDetail().setPhones(new ArrayList());
                }
            }
            contact.getPersonalDetail().addPhone(tmphone);

        }  else if (telPlist.contains("X-FUNAMBOL-TELEX")) {
            Phone tmphone = new Phone(content);
            setParameters(tmphone, plist, group);
            tmphone.setPhoneType("TelexNumber");
            if ((cellWorkTel == 0) && (voiceWorkTel == 0) &&
                (faxWork == 0) && (primary == 0) && (companyMain == 0)   ) {
                if (contact.getBusinessDetail().getPhones() == null) {
                    contact.getBusinessDetail().setPhones(new ArrayList());
                }
            }
            contact.getBusinessDetail().addPhone(tmphone);
        }  else if (telPlist.contains("X-DC")) {
            Phone tmphone = new Phone(content);
            setParameters(tmphone, plist, group);
            if (telPlist.contains("CELL")) {
                tmphone.setPhoneType("MobileDCTelephoneNumber");
            } else {
                tmphone.setPhoneType("DCOnlyTelephoneNumber");
            }
            if ((cellWorkTel == 0) && (voiceWorkTel == 0) &&
                (faxWork == 0) && (primary == 0) && (companyMain == 0)   ) {
                if (contact.getBusinessDetail().getPhones() == null) {
                    contact.getBusinessDetail().setPhones(new ArrayList());
                }
            }
            contact.getBusinessDetail().addPhone(tmphone);
        }
    }

    @Override
    public void setFName(String content, ParamList plist,
                         Token group) throws ParseException {

        contact.getName().getDisplayName().setPropertyValue(unfoldDecodeUnescape(content, plist));
        setParameters(contact.getName().getDisplayName(), plist, group);
    }

    @Override
    public void setRole(String content, ParamList plist,
                        Token group) throws ParseException {

        contact.getBusinessDetail().getRole().setPropertyValue(unfoldDecodeUnescape(content, plist));
        setParameters(contact.getBusinessDetail().getRole(), plist, group);
    }

    @Override
    public void setRevision(String content, ParamList plist,
                            Token group) throws ParseException {
        contact.setRevision(unfoldDecodeUnescape(content, plist));
    }

    @Override
    public void setNickname(String content, ParamList plist,
                            Token group) throws ParseException {
        contact.getName().getNickname().setPropertyValue(unfoldDecodeUnescape(content, plist));
        setParameters(contact.getName().getNickname(),plist,group);
    }

    @Override
    public void setOrganization(String content, ParamList plist,
                                Token group) throws ParseException {

        Log.trace(TAG, "setOrganization");

        FieldsList flist = new FieldsList();
        flist.addValue(content);

        int pos;  // Position in tlist (i.e. position of the current value field)

        // Organization Name
        pos = 0;
        if (flist.size() > pos) {
            String text = unfoldDecode(flist.getElementAt(pos), plist);
            contact.getBusinessDetail().getCompany().setPropertyValue(text);
            setParameters(contact.getBusinessDetail().getCompany(), plist, group);
        }

        // Organizational Unit
        pos = 1;
        if (flist.size() > pos) {
            String text = unfoldDecode(flist.getElementAt(pos), plist);
            contact.getBusinessDetail().getDepartment().setPropertyValue(text);
            setParameters(contact.getBusinessDetail().getDepartment(), plist, group);
        }
    }

    @Override
    public void setAddress(String content, ParamList plist,
                           Token group)throws ParseException
    {
        FieldsList flist = new FieldsList();
        flist.addValue(content);

        int pos;  // Position in tlist (i.e. position of the current value field)
        String text;

        if (plist.containsKey("WORK")) {
        // Business Address

            addressWork++;

            //Post Office Address
            pos = 0;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getBusinessDetail().getAddress().getPostOfficeAddress().setPropertyValue(text);
            setParameters(contact.getBusinessDetail().getAddress().getPostOfficeAddress(), plist, group);

            // Extended Address
            pos = 1;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getBusinessDetail().getAddress().getExtendedAddress().setPropertyValue(text);
            setParameters(contact.getBusinessDetail().getAddress().getExtendedAddress(), plist, group);

            // Street
            pos = 2;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getBusinessDetail().getAddress().getStreet().setPropertyValue(text);
            setParameters(contact.getBusinessDetail().getAddress().getStreet(), plist, group);

            // Locality
            pos = 3;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getBusinessDetail().getAddress().getCity().setPropertyValue(text);
            setParameters(contact.getBusinessDetail().getAddress().getCity(), plist, group);

            // Region
            pos = 4;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getBusinessDetail().getAddress().getState().setPropertyValue(text);
            setParameters(contact.getBusinessDetail().getAddress().getState(), plist, group);

            // Postal Code
            pos = 5;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getBusinessDetail().getAddress().getPostalCode().setPropertyValue(text);
            setParameters(contact.getBusinessDetail().getAddress().getPostalCode(), plist, group);

            // Country
            pos = 6;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getBusinessDetail().getAddress().getCountry().setPropertyValue(text);
            setParameters(contact.getBusinessDetail().getAddress().getCountry(), plist, group);
        }

        if (plist.containsKey("HOME")) {
        // Home Address

            addressHome++;

            //Post Office Address
            pos = 0;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getPersonalDetail().getAddress().getPostOfficeAddress().setPropertyValue(text);
            setParameters(contact.getPersonalDetail().getAddress().getPostOfficeAddress(), plist, group);

            // Extended Address
            pos = 1;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getPersonalDetail().getAddress().getExtendedAddress().setPropertyValue(text);
            setParameters(contact.getPersonalDetail().getAddress().getExtendedAddress(), plist, group);

            // Street
            pos = 2;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getPersonalDetail().getAddress().getStreet().setPropertyValue(text);
            setParameters(contact.getPersonalDetail().getAddress().getStreet(), plist, group);

            // Locality
            pos = 3;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getPersonalDetail().getAddress().getCity().setPropertyValue(text);
            setParameters(contact.getPersonalDetail().getAddress().getCity(), plist, group);

            // Region
            pos = 4;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getPersonalDetail().getAddress().getState().setPropertyValue(text);
            setParameters(contact.getPersonalDetail().getAddress().getState(), plist, group);

            // Postal Code
            pos = 5;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getPersonalDetail().getAddress().getPostalCode().setPropertyValue(text);
            setParameters(contact.getPersonalDetail().getAddress().getPostalCode(), plist, group);

            // Country
            pos = 6;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getPersonalDetail().getAddress().getCountry().setPropertyValue(text);
            setParameters(contact.getPersonalDetail().getAddress().getCountry(), plist, group);
        }

        // other address
        if (!plist.containsKey("HOME") && !plist.containsKey("WORK")) {
        // Other Address

            addressOther++;

            //Post Office Address
            pos = 0;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getPersonalDetail().getOtherAddress().getPostOfficeAddress().setPropertyValue(text);
            setParameters(contact.getPersonalDetail().getOtherAddress().getPostOfficeAddress(), plist, group);

            // Extended Address
            pos = 1;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getPersonalDetail().getOtherAddress().getExtendedAddress().setPropertyValue(text);
            setParameters(contact.getPersonalDetail().getOtherAddress().getExtendedAddress(), plist, group);

            // Street
            pos = 2;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getPersonalDetail().getOtherAddress().getStreet().setPropertyValue(text);
            setParameters(contact.getPersonalDetail().getOtherAddress().getStreet(), plist, group);

            // Locality
            pos = 3;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getPersonalDetail().getOtherAddress().getCity().setPropertyValue(text);
            setParameters(contact.getPersonalDetail().getOtherAddress().getCity(), plist, group);

            // Region
            pos = 4;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getPersonalDetail().getOtherAddress().getState().setPropertyValue(text);
            setParameters(contact.getPersonalDetail().getOtherAddress().getState(), plist, group);

            // Postal Code
            pos = 5;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getPersonalDetail().getOtherAddress().getPostalCode().setPropertyValue(text);
            setParameters(contact.getPersonalDetail().getOtherAddress().getPostalCode(), plist, group);

            // Country
            pos = 6;
            if (flist.size()>pos) {
                text = unfoldDecode(flist.getElementAt(pos), plist);
            } else {
                text="";
            }
            contact.getPersonalDetail().getOtherAddress().getCountry().setPropertyValue(text);
            setParameters(contact.getPersonalDetail().getOtherAddress().getCountry(), plist, group);
        }
    }

    @Override
    public void setBirthday(String content, ParamList plist,
                            Token group) throws ParseException {

        String birthday = unfoldDecodeUnescape(content, plist);

        try {
            // TODO FIXME
            //birthday = TimeUtils.normalizeToISO8601(birthday, defaultTimeZone);
            contact.getPersonalDetail().setBirthday(birthday);
        } catch (Exception e) {
            //
            // If the birthday isn't in a valid format
            // (see TimeUtils.normalizeToISO8601), ignore it
            //
        }
    }

    @Override
    public void setLabel(String content, ParamList plist,
                         Token group) throws ParseException {

        String text = unfoldDecodeUnescape(content, plist);
        if (plist.containsKey("WORK")) {
            if (plist.containsKey("X-FUNAMBOL-PRESERVE")
                    && !(("0").equals(plist.getValue("X-FUNAMBOL-PRESERVE")))) {
                contact.getBusinessDetail().getAddress().getLabel().setPropertyValue(text);
                setParameters(contact.getBusinessDetail().getAddress().getLabel(), plist, group);
            } else if (addressWork == 0) {
                contact.getBusinessDetail().getAddress().explodeLabel(text);
            }
        }
        if (plist.containsKey("HOME")) {
            if (plist.containsKey("X-FUNAMBOL-PRESERVE")
                    && !(("0").equals(plist.getValue("X-FUNAMBOL-PRESERVE")))) {
                contact.getPersonalDetail().getAddress().getLabel().setPropertyValue(text);
                setParameters(contact.getPersonalDetail().getAddress().getLabel(), plist, group);
            } else if (addressHome == 0) {
                contact.getPersonalDetail().getAddress().explodeLabel(text);
            }
        }
        if (!plist.containsKey("HOME") && !plist.containsKey("WORK")) {
            if (plist.containsKey("X-FUNAMBOL-PRESERVE")
                    && !(("0").equals(plist.getValue("X-FUNAMBOL-PRESERVE")))) {
                contact.getPersonalDetail().getOtherAddress().getLabel().setPropertyValue(text);
                setParameters(contact.getPersonalDetail().getOtherAddress().getLabel(), plist, group);
            } else if (addressOther == 0) {
                contact.getPersonalDetail().getOtherAddress().explodeLabel(text);
            }
        }
    }

    @Override
    public void setTimezone(String content, ParamList plist,
                            Token group) throws ParseException
    {
        contact.setTimezone(unfoldDecodeUnescape(content, plist));
    }

    @Override
    public void setGeo(String content, ParamList plist,
                       Token group) throws ParseException
    {
        // Does not unescape because it contains a pair of values
        Property geo = new Property(unfoldDecodeUnescape(content, plist));
        contact.getPersonalDetail().setGeo(geo);
    }

    @Override
    public void setMailer(String content, ParamList plist,
                       Token group) throws ParseException
    {
        contact.setMailer(unfoldDecodeUnescape(content, plist));
    }

    @Override
    public void setLogo(String content, ParamList plist,
                        Token group) throws ParseException
    {
        contact.getBusinessDetail().getLogo().setPropertyValue(unfoldDecodeUnescape(content, plist));
        setParameters(contact.getBusinessDetail().getLogo(), plist, group);
    }

    @Override
    public void setNote(String content, ParamList plist,
                        Token group) throws ParseException
    {
        Log.trace(TAG, "setNote");
        Note tmpnote = new Note();
        tmpnote.setPropertyValue(unfoldDecodeUnescape(content, plist));
        setParameters(tmpnote, plist, group);

        note++;
        if (note == 1) {
            if (contact.getNotes() == null) {
                contact.setNotes(new ArrayList());
            }
            tmpnote.setNoteType("Body");
        } else {
            tmpnote.setNoteType("Body" + note);
        }
        contact.addNote(tmpnote);
    }

    @Override
    public void setUid(String content, ParamList plist,
                       Token group) throws ParseException
    {
        contact.setUid(unfoldDecodeUnescape(content, plist));
    }

    public void setPhoto(String content, ParamList plist,
                         Token group) throws ParseException
    {
        contact.getPersonalDetail().getPhoto().setPropertyValue(unfoldDecodeUnescape(content, plist));
        setParameters(contact.getPersonalDetail().getPhoto(), plist, group);
        if (plist != null) {
            contact.getPersonalDetail().getPhoto().setType(plist.getValue("TYPE"));
        }
    }

    @Override
    public void setName(String content, ParamList plist,
                        Token group) throws ParseException
    {
        Log.trace(TAG, "setName");
        FieldsList flist = new FieldsList();
        flist.addValue(content);

        int pos;  // Position in tlist (i.e. position of the current value field)

        // Last name
        pos=0;
        if (flist.size() > pos) {
            String text = unfoldDecode(flist.getElementAt(pos), plist);
            contact.getName().getLastName().setPropertyValue(text);
            setParameters(contact.getName().getLastName(), plist, group);
        }
        // First name
        pos=1;
        if (flist.size() > pos) {
            String text = unfoldDecode(flist.getElementAt(pos), plist);
            contact.getName().getFirstName().setPropertyValue(text);
            setParameters(contact.getName().getFirstName(), plist, group);
        }
        // Middle name
        pos=2;
        if (flist.size() > pos) {
            String text = unfoldDecode(flist.getElementAt(pos), plist);
            contact.getName().getMiddleName().setPropertyValue(text);
            setParameters(contact.getName().getMiddleName(), plist, group);
        }
        // Prefix
        pos=3;
        if (flist.size() > pos) {
            String text = unfoldDecode(flist.getElementAt(pos), plist);
            contact.getName().getSalutation().setPropertyValue(text);
            setParameters(contact.getName().getSalutation(), plist, group);
        }
        // Suffix
        pos=4;
        if (flist.size() > pos) {
            String text = unfoldDecode(flist.getElementAt(pos), plist);
            contact.getName().getSuffix().setPropertyValue(text);
            setParameters(contact.getName().getSuffix(), plist, group);
        }
    }

    @Override
    public void setFolder(String content, ParamList plist,
                          Token group) throws ParseException
    {
        contact.setFolder(unfoldDecodeUnescape(content, plist));
    }

    @Override
    public void setFreebusy(String content, ParamList plist,
                            Token group) throws ParseException
    {
        contact.setFreeBusy(unfoldDecodeUnescape(content, plist));
    }

    @Override
    public void setAnniversary(String content, ParamList plist,
                          Token group) throws ParseException
    {
        contact.getPersonalDetail().setAnniversary(unfoldDecodeUnescape(content, plist));
    }

    @Override
    public void setChildren(String content, ParamList plist,
                          Token group) throws ParseException
    {
        // Does not unescape because it could contain a list of values
        contact.getPersonalDetail().setChildren(unfoldDecode(content, plist));
    }

    @Override
    public void setCompanies(String content, ParamList plist,
                          Token group) throws ParseException
    {
        // Does not unescape because it could contain a list of values
        contact.getBusinessDetail().setCompanies(unfoldDecode(content, plist));
    }

    @Override
    public void setLanguages(String content, ParamList plist,
                          Token group) throws ParseException
    {
        // Does not unescape because it could contain a list of values
        contact.setLanguages(unfoldDecode(content, plist));
    }

    @Override
    public void setManager(String content, ParamList plist,
                          Token group) throws ParseException
    {
        contact.getBusinessDetail().setManager(unfoldDecodeUnescape(content, plist));
    }

    @Override
    public void setMileage(String content, ParamList plist,
                          Token group) throws ParseException
    {
        contact.setMileage(unfoldDecodeUnescape(content, plist));
    }

    @Override
    public void setSpouse(String content, ParamList plist,
                          Token group) throws ParseException
    {
        contact.getPersonalDetail().setSpouse(unfoldDecodeUnescape(content, plist));
    }

    @Override
    public void setSubject(String content, ParamList plist,
                          Token group) throws ParseException
    {
        contact.setSubject(unfoldDecodeUnescape(content, plist));
    }

    @Override
    public void setAccessClass(String content, ParamList plist,
                                Token group) throws ParseException
    {
        String accessClass = unfoldDecodeUnescape(content, plist);
        Short sensitivity;
        if (Contact.CLASS_PUBLIC.equalsIgnoreCase(accessClass)) {
            sensitivity = Contact.SENSITIVITY_NORMAL;
        } else if (Contact.CLASS_CONFIDENTIAL.equalsIgnoreCase(accessClass)) {
            sensitivity = Contact.SENSITIVITY_CONFIDENTIAL;
        } else if (Contact.CLASS_PRIVATE.equalsIgnoreCase(accessClass)) {
            sensitivity = Contact.SENSITIVITY_PRIVATE;
        } else {
            sensitivity = Contact.SENSITIVITY_PERSONAL;
        }
        contact.setSensitivity(sensitivity);
    }

    /**
     * Sets the parameters encoding, charset, language, value for a given property
     * fetching them from the given ParamList.
     * Notice that if the items are not set (i.e. are null) in the ParamList, they
     * will be set to null in the property too (this is to avoid inconsistency when
     * the same vCard property is encountered more than one time, and thus overwritten
     * in the Contact object model).
     */
    private void setParameters(Property property, ParamList plist) {
        if (plist != null) {
            property.setEncoding(plist.getEncoding());
            property.setCharset (plist.getCharset());
            property.setLanguage(plist.getLanguage());
            property.setValue   (plist.getValue   ());
            property.setXParams (plist.getXParams ());
        }
    }

    /**
     * Sets the parameters encoding, charset, language, value and group for a given property
     * fetching them from the given ParamList and the group Token.
     */
    private void setParameters(Property property, ParamList plist, Token group) {
        if (!(group==null)) {
            property.setGroup(group.image);
        }
        else {
            property.setGroup(null);
        }
        setParameters(property,plist);
    }

    /**
     * Unfolds a string (i.e. removes all the CRLF characters)
     */
    private String unfold (String str) {
        int ind = str.indexOf("\r\n");
        if (ind == -1) {
            return unfoldNewline(str);
        }
        else {
            String tmpString1 = str.substring(0,ind);
            String tmpString2 = str.substring(ind+2);
            return unfoldNewline(unfold(tmpString1+tmpString2));
        }
    }

    /**
     * Unfolds a string (i.e. removes all the line break characters).
     * This function is meant to ensure compatibility with vCard documents
     * that adhere loosely to the specification
     */
    private String unfoldNewline (String str) {
        int ind = str.indexOf("\n");
        if (ind == -1) {
            return str;
        }
        else {
            String tmpString1 = str.substring(0,ind);
            String tmpString2 = str.substring(ind+1);
            return unfoldNewline(tmpString1+tmpString2);
        }
    }

    /**
     * Decode the given text according to the given encoding and charset
     *
     * @param text the text to decode
     * @param encoding the encoding
     * @param propertyCharset the charset
     *
     * @return the text decoded
     */
    private String decode(String text, String encoding, String propertyCharset)
    throws ParseException {
        if (text == null) {
            return null;
        }

        //
        // If input charset is null then set it with default charset
        //
        if (propertyCharset == null) {
            propertyCharset = defaultCharset; // we use the default charset
        }
        if (encoding != null) {
            if ("QUOTED-PRINTABLE".equals(encoding)) {
                try {
                    //
                    // Some phone, like the Sony Ericsson k750i can send something
                    // like that:
                    //
                    // BEGIN:VCARD
                    // VERSION:2.1
                    // N;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:;=C3=85=C3=A5=C3=A6
                    // TITLE;CHARSET=UTF-8:Title
                    // ORG;CHARSET=UTF-8:Compan
                    // TEL;CELL:0788554422
                    // EMAIL;INTERNET;PREF;CHARSET=UTF-8:ac0@dagk.com
                    // ADR;HOME;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:;;S=CE=A6;City;Stat;6;Peru=
                    //
                    // X-IRMC-LUID:000200000102
                    // END:VCARD
                    //
                    // At the end of the address there is a '=\r\n\r\n'. This is replaced
                    // with '=\r\n' by SourceUtils.handleDelimiting so here the vcard is:
                    //
                    // BEGIN:VCARD
                    // VERSION:2.1
                    // N;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:;=C3=85=C3=A5=C3=A6
                    // TITLE;CHARSET=UTF-8:Title
                    // ORG;CHARSET=UTF-8:Compan
                    // TEL;CELL:0788554422
                    // EMAIL;INTERNET;PREF;CHARSET=UTF-8:ac0@dagk.com
                    // ADR;HOME;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:;;S=CE=A6;City;Stat;6;Peru=
                    // X-IRMC-LUID:000200000102
                    // END:VCARD
                    //
                    // The problem is with the address becasue the value is in QP but
                    // it finishes with a '=' so this is not a valid QP
                    // (Invalid quoted-printable encoding)
                    // To fix the problem, before to decode the string, we remove the
                    // '=' at the end of the string
                    //
                    text = removeLastEquals(text);

                    byte t[] = text.getBytes(propertyCharset);
                    int len = QuotedPrintable.decode(t);
                    String value = new String(t, 0, len, propertyCharset);
                    return value;
                } catch (Exception e) {
                    throw new ParseException(e.getMessage());
                }
            }
        } else {
            try {
                return new String(text.getBytes(), propertyCharset);
            } catch (UnsupportedEncodingException ue) {
                throw new ParseException(ue.getMessage());
            }
        }

        return text;
    }

    /**
     * Removes the last equals from the end of the given String
     */
    private String removeLastEquals(String data) {
        if (data == null) {
            return data;
        }
        data = data.trim();
        while (data.endsWith("=")) {
            data = data.substring(0, data.length() - 1);
        }
        return data;
    }

    /**
     * Unescape backslash and semicolon.
     *
     * @param text the text to unescape
     * @return String the unescaped text
     */
    private String unescape(String text) {

        if (text == null) {
            return text;
        }

        StringBuffer value = new StringBuffer();
        int length = text.length();
        boolean foundSlash = false;
        for (int i=0; i<length; i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '\\':
                    if (foundSlash) {
                        foundSlash = false;
                        value.append('\\');
                    } else {
                        foundSlash = true;
                    }
                    break;
                case ';':
                    value.append(';');
                    foundSlash = false;
                    break;
                default:
                    if (foundSlash) {
                        foundSlash = false;
                        value.append('\\');
                    }
                    value.append(ch);
                    break;
            }
        }
        return value.toString();
    }

    private String unfoldDecodeUnescape(String content, ParamList plist)
    throws ParseException {
        String text = unfold(content);
        text = decode(text, plist.getEncoding(), plist.getCharset());
        text = unescape(text);
        return text;
    }

    private String unfoldDecode(String content, ParamList plist)
    throws ParseException {
        String text = unfold(content);
        text = decode(text, plist.getEncoding(), plist.getCharset());
        return text;
    }

}
