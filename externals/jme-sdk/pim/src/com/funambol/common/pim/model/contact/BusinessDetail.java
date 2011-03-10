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

import com.funambol.common.pim.model.common.Property;
import java.util.List;
import java.util.ArrayList;

/**
 * A container for the business details of a contact.
 */
public class BusinessDetail extends ContactDetail {

    //--------------------------------------------------------------- Properties
    
    private Address     address;
    private Property    role;
    private List<Title> titles = new ArrayList<Title>();
    private Property    company;
    private Property    department;
    private Property    logo;
    private String      manager;
    private String      assistant;
    private String      officeLocation;
    private String      companies;  // this differs from company since it is one 
                                  // or more companies associated to the contact

    /**
     * Returns the contact's business role.
     *
     * @return the value of property role
     */
    public Property getRole() {
        return role;
    }

    /**
     * Returns the contact's titles.
     *
     * @return a List containing Title objects
     */
    public List getTitles() {
        return titles;
    }

    /**
     * Sets property titles.
     * 
     * @param titles a List containing Title objects
     */
    public void setTitles(List titles) {
        this.titles = titles;
    }

    /**
     * Adds a new title to the title list, unless it is already there.
     *
     * @param title the new title to add
     */
    public void addTitle(Title title) {
        if (title == null) {
            return;
        }

        if (titles == null) {
            titles = new ArrayList<Title>();
        }

        addTypifiedProperty(titles, title);
    }

    /**
     * Returns the contact's work address.
     *
     * @return the value of property address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Returns the company the contact works for.
     *
     * @return the value of property company
     */
    public Property getCompany() {
        return company;
    }

    /**
     * Returns the department where the contact works.
     *
     * @return the value of property department
     */
    public Property getDepartment() {
        return department;
    }

    /**
     * Returns the name of the contact's manager.
     *
     * @return the value of property manager
     */
    public String getManager() {
        return manager;
    }

    /**
     * Returns the name of the contact's assistant
     *
     * @return the value of property assistant
     */
    public String getAssistant() {
        return assistant;
    }

    /**
     * Returns a logo for the contact.
     *
     * @return the value of property logo
     * 
     * @deprecated The logo field is not supported.
     */
    public Property getLogo() {
        return logo;
    }

    /**
     * Sets the name of the contact's manager.
     *
     * @param manager new value of property manager
     */
    public void setManager (String manager) {
        this.manager = manager;
    }

    /**
     * Sets the name of the contact's assistant.
     *
     * @param assistant new value of property assistant
     */
    public void setAssistant (String assistant) {
        this.assistant = assistant;
    }

    /**
     * Gets the location of the contact's office.
     * 
     * @return value of property officeLocation
     */
    public String getOfficeLocation() {
        return officeLocation;
    }

    /**
     * Gets the location of the contact's office.
     * 
     * @param officeLocation new value of property officeLocation
     */
    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    /**
     * Gets a semicolon-separated list of companies this contact is associated
     * with.
     * 
     * @return value of property companies
     */
    public String getCompanies() {
        return companies;
    }

    /**
     * Sets property companies.
     * 
     * @param companies new value of property companies
     */
    public void setCompanies(String companies) {
        this.companies = companies;
    }
    
    /**
     * Sets property company.
     * 
     * @param company new value of property company
     */
    public void setCompany(Property company) {
        this.company = company;
    }
    
    /**
     * Sets property department.
     * 
     * @param department new value of property department
     */
    public void setDepartment(Property department) {
        this.department = department;
    }
    
    /**
     * Sets property role.
     * 
     * @param role new value of property role
     */
    public void setRole(Property role) {
        this.role = role;
    }
    
    
    /**
     * Sets property address.
     * 
     * @param address new value of property address
     */    
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * Sets property logo.
     * 
     * @param logo new value of property logo
     * 
     * @deprecated The logo field is not supported.
     */    
    public void setLogo(Property logo) {
        this.logo = logo;
    }    

    //------------------------------------------------------------- Constructors
    
    /**
     * Creates an empty container of business details.
     */
    public BusinessDetail() {
        super();
        role = new Property();
        address = new Address();
        company = new Property();
        department = new Property();
        logo = new Property();
        manager = null;
        assistant = null;
        officeLocation = null;
        companies = null;
    }
}
