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
package com.funambol.common.pim.model.contact;

import com.funambol.common.pim.model.common.TypifiedProperty;
import java.util.List;
import java.util.ArrayList;

/**
 * An object containing details on how to reach a contact (phone numbers, 
 * e-mail addresses, webpage etc.).
 */
public class ContactDetail {

    //--------------------------------------------------------------- Properties
    
    private List<Phone>   phones = new ArrayList<Phone>();
    private List<Email>   emails = new ArrayList<Email>(3);
    private List<WebPage> webPages = new ArrayList<WebPage>(3);

    /**
     * Returns the list of telephone numbers.
     *
     * @return a List containing Phone objects
     */
    public List getPhones() {
       return phones;
    }

    /**
     * Returns the list of e-mail addresses.
     *
     * @return a List containing Email objects
     */
    public List getEmails() {
       return emails;
    }

    /**
     * Returns the list of webpages.
     *
     * @return a List containing WebPage objects
     */
    public List getWebPages() {
       return webPages;
    }

    /**
     * Sets a new list of phones.
     * 
     * @param phones new value of list phones
     */
    public void setPhones(List phones) {
        this.phones = phones;
    }

    /**
     * Sets a new list of e-mail addresses.
     * 
     * @param emails new value of list emails
     */
    public void setEmails(List emails) {
        this.emails = emails;
    }

    /**
     * Sets a new list of webpages
     * 
     * @param webPages new value of list webpages
     */
    public void setWebPages(List webPages) {
        this.webPages = webPages;
    }
    
    //----------------------------------------------------------- Public methods

     /**
     * Adds a new phone number to the list.
     *
     * @param phone the new phone number
     *
     */
    public void addPhone(Phone phone) {
        if (phone == null) {
            return;
        }

        if (phones == null) {
            phones = new ArrayList<Phone>();
        }

        addTypifiedProperty(phones, phone);
    }
    
    /**
     * Adds a new email address to the list.
     *
     * @param email the new email address
     *
     */
    public void addEmail(Email email) {
        if (email == null) {
            return;
        }

        if (emails == null) {
            emails = new ArrayList<Email>(3);
        }

        addTypifiedProperty(emails, email);
    }
    
    /**
     * Adds a new webpage to the list.
     *
     * @param page the new webpage
     *
     */
    public void addWebPage(WebPage page) {
        if (page == null) {
            return;
        }

        if (webPages == null) {
            webPages = new ArrayList<WebPage>(3);
        }

        addTypifiedProperty(webPages, page);
    }
    
    /**
     * Adds a typified property to a list if no property of that type is already 
     * there, or updates it if there is one.
     * 
     * @param list a list of objects of some class that extends TypifiedProperty
     * @param addition the item to add or replace in the list
     */
    protected void addTypifiedProperty(List<? extends TypifiedProperty> list, 
                                       TypifiedProperty addition) {
        for (TypifiedProperty tp : list) {
            if (tp.getPropertyType().equals(addition.getPropertyType())) {
                tp.setPropertyValue(addition.getPropertyValue());
                return;
            }
        }
        ((List)list).add(addition);
    }

}
