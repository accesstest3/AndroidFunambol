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

import java.util.List;
import java.util.ArrayList;

import com.funambol.common.pim.model.common.XTag;
import com.funambol.common.pim.model.common.Property;

/**
 * An object representing a contact with all the information supported by 
 * Funambol it contains.
 * This is the "foundational model" of a contact, used to exchange information 
 * about such items between server and connectors. It can also be used by 
 * clients.
 */
public class Contact {
    
    //--------------------------------------------------------------- Properties
    
    private String         uid;
    private Name           name;
    private List<Note>     notes;
    private String         timezone;
    private String         revision;
    private BusinessDetail businessDetail;
    private PersonalDetail personalDetail;
    private List<XTag>     xTags;
    private Property       categories;
    private String         languages;
    private Short          importance;
    private Short          sensitivity;
    private String         subject;
    private String         mileage;
    private String         folder;
    private String         freeBusy;
    private String         mailer;

    public static final Short SENSITIVITY_NORMAL       = 0; // OlSensitivity.olNormal
    public static final Short SENSITIVITY_PERSONAL     = 1; // OlSensitivity.olPersonal
    public static final Short SENSITIVITY_PRIVATE      = 2; // OlSensitivity.olPrivate
    public static final Short SENSITIVITY_CONFIDENTIAL = 3; // OlSensitivity.olConfidential

    // vCard equivalents for sensitivity values:
    public static final String CLASS_PUBLIC       = "PUBLIC";
    public static final String CLASS_PRIVATE      = "PRIVATE";
    public static final String CLASS_CONFIDENTIAL = "CONFIDENTIAL";
    public static final String CLASS_CUSTOM       = "X-PERSONAL";

    /**
     * Returns the unique ID (as defined by a client) of this contact.
     *
     * @return value of property uid
     */
    public String getUid () {
        return uid;
    }

    /**
     * Returns the preferred time zone for this contact.
     *
     * @return value of property timezone
     */
    public String getTimezone () {
        return timezone;
    }

    /**
     * Returns all the notes abouts this contact.
     *
     * @return a List that contains Note objects
     */
    public List getNotes () {
        return notes;
    }

    /**
     * Setter for property notes.
     * 
     * @param notes new value of property notes
     */
    public void setNotes(List notes) {
        this.notes = notes;
    }

    /**
     * Adds a new note to the note list.
     *
     * @param note the new note
     */
    public void addNote(Note note) {
        if (note == null) {
            return;
        }

        if (notes == null) {
            notes = new ArrayList<Note>();
        }

        for (Note n : notes) {
            if (n.getPropertyType().equals(note.getPropertyType())) {
                n.setPropertyValue(note.getPropertyValue());
                return;
            }
        }
        notes.add(note);
    }

    /**
     * Returns the list of the custom properties of this contact.
     *
     * @return a List containing XTag objects
     */
    public List getXTags () {
        return xTags;
    }

    /**
     * Setter for list xTags.
     * 
     * @param xTags a list containing XTag objects
     */
    public void setXTags(List xTags) {
        this.xTags = xTags;
    }
    
   /**
     * Returns the revision number of this contact.
     *
     * @return value of property revision
     */
    public String getRevision () {
        return revision;
    }

    /**
     * Returns the full name of this contact.
     *
     * @return value of property name
     */
    public Name getName () {
        return name;
    }


    /**
     * Returns the business details of this contact.
     *
     * @return the business details of this contact
     */
    public BusinessDetail getBusinessDetail () {
        return businessDetail;
    }

    /**
     * Returns the personal details of this contact.
     *
     * @return the personal details of this contact
     */
    public PersonalDetail getPersonalDetail () {
        return personalDetail;
    }

    /**
     * Sets the unique ID (as defined by a client) of this contact.
     *
     * @param uid new value of property uid
     */
    public void setUid (String uid) {
        this.uid = uid;
    }

    /**
     * Sets the preferred timezone for this contact.
     *
     * @param timezone new value of property timezone
     */
    public void setTimezone (String timezone) {
        this.timezone = timezone;
    }

    /**
     * Sets the revision number of this contact.
     *
     * @param revision new value of property revision
     */
    public void setRevision (String revision) {
        this.revision = revision;
    }

    /**
     * Returns the categories this contact belongs to.
     * 
     * @return value of property categories
     */
    public Property getCategories() {
        return categories;
    }

    /**
     * Setter for property categories.
     * 
     * @param categories new value of property categories.
     */
    public void setCategories(Property categories) {
        this.categories = categories;
    }

    /**
     * Returns the languages spoken by this contact.
     * 
     * @return value of property languages
     */
    public String getLanguages() {
        return languages;
    }

    /**
     * Setter for property languages.
     * 
     * @param languages new value of property languages
     */
    public void setLanguages(String languages) {
        this.languages = languages;
    }

    /**
     * Setter for property name. 
     * Note that this should not be used to set just a Name's property. In such 
     * cases calling one of the setters within this.getName() is better.
     *
     * @param name new value of property name
     */
    public void setName(Name name) {
        this.name = name;
    }

    /**
     * Setter for property businessDetail.
     * Note that this should not be used to set just a BusinessDetail's 
     * property. In such cases calling one of the setters within 
     * this.getBusinessDetail() is better.
     *
     * @param businessDetail new value of property businessDetail
     */
    public void setBusinessDetail(BusinessDetail businessDetail) {
        this.businessDetail = businessDetail;
    }

    /**
     * Setter for property personalDetail.
     * Note that this should not be used to set just a PersonalDetail's 
     * property. In such cases calling one of the setters within 
     * this.getPersonalDetail() is better.
     *
     * @param personalDetail new value of property personalDetail
     */
    public void setPersonalDetail(PersonalDetail personalDetail) {
        this.personalDetail = personalDetail;
    }

    /**
     * Returns the importance (priority) level of this contact.
     * 
     * @return value of property importance
     */
    public Short getImportance() {
        return importance;
    }

    /**
     * Setter for property importance (priority).
     * 
     * @param importance new value of property importance
     */
    public void setImportance(Short importance) {
        this.importance = importance;
    }

    /**
     * Returns the sensitivity of this contact's information (public, private 
     * etc.).
     * 
     * @return value of property sensitivity
     */
    public Short getSensitivity() {
        return sensitivity;
    }

    /**
     * Setter for property sensitivity.
     * 
     * @param sensitivity new value of property sensitivity
     */
    public void setSensitivity(Short sensitivity) {
        this.sensitivity = sensitivity;
    }

    /**
     * Returns the subject for this contact's information page.
     * 
     * @return value of property subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Setter for property subject.
     * 
     * @param subject new value of property subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Returns the mileage information for this contact.
     * 
     * @return value of property mileage
     */
    public String getMileage() {
        return mileage;
    }

    /**
     * Setter for property mileage.
     * 
     * @param mileage new value of property mileage
     */
    public void setMileage(String mileage) {
        this.mileage = mileage;
    }

    /**
     * Returns the folder in which this contact's information are to be saved.
     * 
     * @return value of property folder
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Setter for property folder.
     * 
     * @param folder new value of property folder
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }
    
    /**
     * Setter for property freeBusy.
     * 
     * @param freeBusy new value of property freeBusy
     */
    public void setFreeBusy(String freeBusy) {
        this.freeBusy = freeBusy;
    }

    /**
     * Returns the free/busy status information for this contact.
     * 
     * @return value of property freeBusy
     */
    public String getFreeBusy() {
        return freeBusy;
    }

    /**
     * Setter for property mailer.
     *
     * @param mailer new value of property mailer
     */
    public void setMailer(String mailer) {
        this.mailer = mailer;
    }

    /**
     * Returns the mailer program name for this contact.
     *
     * @return value of property mailer
     */
    public String getMailer() {
        return mailer;
    }

    //------------------------------------------------------------- Constructors
    
    /**
     * Creates an empty contact.
     */
    public Contact () {
        name = new Name();

        notes  = null;
        xTags  = null;

        businessDetail = new BusinessDetail();
        personalDetail = new PersonalDetail();
        categories= new Property();
    }

    //----------------------------------------------------------- Public methods

    /**
     * Adds a new xtag to the xtag list
     *
     * @param tag the new xtag
     */
    public void addXTag(XTag tag) {
        if (tag == null) {
            return;
        }

        if (xTags == null) {
            xTags = new ArrayList<XTag>();
        }

        for (XTag t : xTags) {
            if (t.getXTagValue().equals(tag.getXTagValue())) {
                t.getXTag().setPropertyValue(tag.getXTag().getPropertyValue());
                return;
            }
        }
        xTags.add(tag);
    }
}
