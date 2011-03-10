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
import java.util.StringTokenizer;

/**
 * An object representing an address of a contact.
 */
public class Address {

    //--------------------------------------------------------------- Properties
    
    private Property postOfficeAddress;
    private Property roomNumber;
    private Property street;
    private Property city;
    private Property state;
    private Property postalCode;
    private Property country;
    private Property label;
    private Property extendedAddress;

    /**
     * Returns the post office of this address.
     *
     * @return the post office of this address
     */
    public Property getPostOfficeAddress () {
        return postOfficeAddress;
    }

    /**
     * Returns the room number of this address.
     *
     * @return the room number of this address
     */
    public Property getRoomNumber () {
        return roomNumber;
    }

    /**
     * Returns the street of this address.
     *
     * @return the street of this address
     */
    public Property getStreet () {
        return street;
    }

    /**
     * Returns the city of this address.
     *
     * @return the city of this address
     */
    public Property getCity () {
        return city;
    }

    /**
     * Returns the state of this address.
     *
     * @return the state of this address
     */
    public Property getState () {
        return state;
    }

    /**
     * Returns the postal code of this address.
     *
     * @return the postal code of this address
     */
    public Property getPostalCode () {
        return postalCode;
    }

    /**
     * Returns the country of this address.
     *
     * @return the country of this address
     */
    public Property getCountry () {
        return country;
    }

    /**
     * Returns the label of this address.
     *
     * @return the label of this address
     */
    public Property getLabel () {
        return label;
    }

    /**
     * Returns the extended address of this address.
     *
     * @return the extended address of this address
     */
    public Property getExtendedAddress() {
        return extendedAddress;
    }
    
    /**
     * Setter for property postOfficeAddress.
     * 
     * @param postOfficeAddress new value of property postOfficeAddress
     */
    public void setPostOfficeAddress(Property postOfficeAddress) {
        this.postOfficeAddress = postOfficeAddress;
    }
    
    /**
     * Setter for property roomNumber.
     * 
     * @param roomNumber new value of property roomNumber
     */
    public void setRoomNumber(Property roomNumber) {
        this.roomNumber = roomNumber;
    }
    
    /**
     * Setter for property street.
     * 
     * @param street new value of property street
     */
    public void setStreet(Property street) {
        this.street = street;
    }
    
    /**
     * Setter for property city.
     * 
     * @param city new value of property city
     */
    public void setCity(Property city) {
        this.city = city;
    }
    
    /**
     * Setter for property state.
     * 
     * @param state new value of property state
     */
    public void setState(Property state) {
        this.state = state;
    }
    
    /**
     * Setter for property postalCode.
     * 
     * @param postalCode new value of property postalCode
     */
    public void setPostalCode(Property postalCode) {
        this.postalCode = postalCode;
    }
    
    /**
     * Setter for property country.
     * 
     * @param country new value of property country
     */
    public void setCountry(Property country) {
        this.country = country;
    }
    
    /**
     * Setter for property label.
     * 
     * @param label new value of property label
     */
    public void setLabel(Property label) {
        this.label = label;
    }
    
    /**
     * Setter for property extendedAddress.
     * 
     * @param extendedAddress new value of property extendedAddress
     */    
    public void setExtendedAddress(Property extendedAddress) {
        this.extendedAddress = extendedAddress;
    }    
    
    //------------------------------------------------------------- Constructors
    
    /**
     * Creates an empty address
     */
    public Address() {
        postOfficeAddress = new Property();
        roomNumber = new Property();
        street = new Property();
        city = new Property();
        state = new Property();
        postalCode = new Property();
        country = new Property();
        label = new Property();
        extendedAddress = new Property();
    }

    //----------------------------------------------------------- Public methods
    
    /**
     * Explodes a label into its components, tries to infer which ones the
     * corresponding address properties are and sets them. Typical label formats
     * that are correctly exploded are:
     * <ul>
     * <li>
     * Street
     * </li>
     * <li>
     * Street<br>
     * ZIP, City (State)
     * </li>
     * <li>
     * PO Box<br>
     * ZIP, City (State)
     * </li>
     * <li>
     * Street<br>
     * ZIP, City (State)<br>
     * Country
     * </li>
     * <li>
     * PO Box<br>
     * ZIP, City (State)<br>
     * Country
     * </li>
     * </ul>
     * There are other formats that are correctly understood (but many are not), 
     * for instance the Street component is allowed to be split into several 
     * lines.
     *
     * @param labelText the label as a String object
     */
    public void explodeLabel(String labelText) {
        StringTokenizer lines = new StringTokenizer(labelText, "\r\n\t");
        String poBox = "";
        String xAddress = ""; // currently unused
        String street = "";
        String cityAndMore = "";
        String city = "";
        String region = "";
        String zip = "";
        String country = "";
        
        int numberOfLines = lines.countTokens();
        switch (numberOfLines) {
            case 0: // nothing
                break;
                
            case 1: // just cityAndMore or street
                if (labelText.matches(".*[0-9].*")) {
                    street = labelText;
                } else {
                    cityAndMore = labelText;
                }
                break;
                
            case 2: // poBox or street + cityAndMore
                poBox = lines.nextToken();
                cityAndMore = lines.nextToken();
                break;
                
            case 3: // poBox or street + cityAndMore + country
                poBox = lines.nextToken();
                cityAndMore = lines.nextToken();
                country = lines.nextToken();
                break;
                
            default: // poBox (maybe) + street + cityAndMore + country
                poBox = lines.nextToken();
                street = lines.nextToken();
                for (int i = 3; i < numberOfLines - 1; i++) {
                    street += '\n' + lines.nextToken();
                }
                cityAndMore = lines.nextToken();
                country = lines.nextToken();
                break;
        }
        
        // Is poBox really a PO box?
        if ((poBox.length() != 0) && 
                !poBox.matches(
                "(?i)(.*P.*O.*BOX.*[0-9]+)|([0-9]+.*P.*O.*BOX.*)")) {
            if (street.length() > 0) {
                street = poBox + '\n' + street;
            } else {
                street = poBox;
            }
            poBox = "";
        }
        
        StringTokenizer cityPieces = new StringTokenizer(cityAndMore, " ,");
        String piece;
        while (cityPieces.hasMoreTokens()) {
            piece = cityPieces.nextToken();
            if ((zip.length() == 0) && piece.matches("[0-9,-]+")) {
                zip = piece;
            } else if ((region.length() == 0) &&
                    ((piece.length() == 2) ||
                    (piece.matches("\\(.*\\)")))) {
                region = piece;
            } else {
                if (city.length() == 0) {
                    city = piece;
                } else {
                    city += ' ' + piece;
                }
            }
        }

        getPostOfficeAddress().setPropertyValue(poBox);
        getExtendedAddress().setPropertyValue(xAddress); // always empty
        getStreet().setPropertyValue(street);
        getCity().setPropertyValue(city);
        getState().setPropertyValue(region);
        getPostalCode().setPropertyValue(zip);
        getCountry().setPropertyValue(country);
    }
}
