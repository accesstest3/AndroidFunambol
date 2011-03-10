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
import com.funambol.util.Base64;

/**
 * An object containing the personal details of a contact
 */
public class PersonalDetail extends ContactDetail {

    private Address  address;
    private Address  otherAddress;
    private Property photo;
    private Property geo;
    private String   spouse;
    private String   children;
    private String   anniversary;
    private String   birthday;
    private String   gender;
    private String   hobbies;

    /**
     * Creates an empty list of personal details
     */
    public PersonalDetail() {
        super();
        address = new Address();
        otherAddress = new Address();
        photo = new Property();
        geo = new Property();
        spouse = null;
        children = null;
        anniversary = null;
        birthday = null;
        gender = null;
        hobbies = null;
    }

    /**
     * Returns the address for this Personal Detail
     *
     * @return the address for this Personal Detail
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Returns the other address for this Personal Detail
     *
     * @return the other address for this Personal Detail
     */
    public Address getOtherAddress() {
        return otherAddress;
    }

    /**
     * Returns the geo for this Personal Detail
     *
     * @return the geo for this Personal Detail
     */
    public Property getGeo() {
        return geo;
    }

    /**
     * Returns the spouse for this Personal Detail
     *
     * @return the spouse for this Personal Detail
     */
    public String getSpouse() {
        return spouse;
    }

    /**
     * Returns the children for this Personal Detail
     *
     * @return the children for this Personal Detail
     */
    public String getChildren() {
        return children;
    }

    /**
     * Returns the anniversary for this Personal Detail
     *
     * @return the anniversary for this Personal Detail
     */
    public String getAnniversary() {
        return anniversary;
    }

    /**
     * Returns the birthday for this Personal Detail
     *
     * @return the birthday for this Personal Detail
     */
    public String getBirthday() {
        return birthday;
    }

    /**
     * Returns the gender for this Personal Detail
     *
     * @return the gender for this Personal Detail
     */
    public String getGender() {
        return gender;
    }

    /**
     * Returns the photo for this Personal Detail
     *
     * @return the photo for this Personal Detail
     * @deprecated Since v65, use getPhotoObject and setPhotoObject
     */
    public Property getPhoto() {
        return photo;
    }

    /**
     * Returns the photo as <code>Photo</code> and not just as Property
     * @return the photo for this Personal Detail
     */
    public Photo getPhotoObject() {
        if (photo == null) {
            return null;
        }
        Photo photoObject = new Photo();
        String type      = photo.getType();
        String value     = photo.getPropertyValueAsString();

        if (value == null) {
            return null;
        }

        if (value.length() == 0) {
            return photoObject;
        }

        photoObject.setType(type);

        String encoding  = photo.getEncoding();
        String valueType = photo.getValue();

        Object oValue = null;

        if ("B".equalsIgnoreCase(encoding) ||
            "BASE64".equalsIgnoreCase(encoding)) {
            if (value != null && value.length() > 0) {
                oValue  = Base64.decode(value);
            } else {
                oValue = new byte[0];
            }
        } else {
            oValue = value;
        }

        if ("URL".equalsIgnoreCase(valueType)) {
            if (oValue instanceof byte[]) {
                //
                // really strange....an url sent in base 64
                //
                photoObject.setUrl(new String((byte[])oValue));
            } else {
                photoObject.setUrl((String)oValue);
            }
        } else {
            photoObject.setImage((byte[])oValue);
        }


        return photoObject;
    }

    /**
     * Sets the geo for this Personal Detail
     *
     * @param geo the geo to set
     */
    public void setGeo(Property geo) {
        this.geo = geo;
    }

    /**
     * Sets the spouse for this Personal Detail
     *
     * @param spouse the spouse to set
     */
    public void setSpouse (String spouse) {
        this.spouse = spouse;
    }

    /**
     * Sets the children for this Personal Detail
     *
     * @param children the children to set
     */
    public void setChildren (String children) {
        this.children = children;
    }

    /**
     * Sets the anniversary for this Personal Detail
     *
     * @param anniversary the anniversary to set
     */
    public void setAnniversary (String anniversary) {
        this.anniversary = anniversary;
    }

    /**
     * Sets the birthday for this Personal Detail
     *
     * @param birthday the spouse to set
     */
    public void setBirthday (String birthday) {
        this.birthday = birthday;
    }

    /**
     * Sets the gender for this Personal Detail
     *
     * @param gender the gender to set
     */
    public void setGender (String gender) {
        this.gender = gender;
    }

    /**
     * Getter for property hobbies.
     * @return Value of property hobbies.
     */
    public java.lang.String getHobbies() {
        return hobbies;
    }

    /**
     * Setter for property hobbies.
     * @param hobbies New value of property hobbies.
     */
    public void setHobbies(java.lang.String hobbies) {
        this.hobbies = hobbies;
    }

    /**
     * Setter for property address.
     * @param address New value of property address.
     */
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * Setter for property otherAddress.
     * @param otherAddress New value of property otherAddress.
     */
    public void setOtherAddress(Address otherAddress) {
        this.otherAddress = otherAddress;
    }

    /**
     * Setter for property photo.
     * @param photo New value of property photo.
     * @deprecated Since v65, use setPhotoObject
     */
    public void setPhoto(Property photo) {
        this.photo = photo;
    }

    /**
     * Setter for property photo.
     * @param photo New value of property photo.
     */
    public void setPhotoObject(Photo photo) {
        if (photo == null) {
            this.photo = new Property();
            return;
        }
        this.photo.setType(photo.getType());
        if (photo.getUrl() != null && photo.getUrl().length() > 0) {
            this.photo.setValue("URL");
            this.photo.setPropertyValue(photo.getUrl());
        } else {
            if (photo.getImage() != null && photo.getImage().length > 0) {
                String b64 = new String(Base64.encode(photo.getImage()));
                this.photo.setPropertyValue(b64);
                this.photo.setEncoding("BASE64");
                //
                // The charset must be null since:
                // 1. it is useless since the content is in base64
                // 2. on some Nokia phone it doesn't work since for some reason the phone
                //    adds a new photo and the result is that a contact has two photos
                //    Examples of wrong phones: Nokia N91, 7610, 6630
                //
                this.photo.setCharset(null);
            } else {
                this.photo.setPropertyValue("");
            }
        }
    }
}
