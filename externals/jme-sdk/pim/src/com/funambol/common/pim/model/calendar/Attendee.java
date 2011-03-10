/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2007 Funambol, Inc.
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
package com.funambol.common.pim.model.calendar;

/**
 * Each instance of this class represents an attendee (in a broad sense as of
 * the ATTENDEE property of iCalendar: it may be a delegate, the organizer, a
 * resource etc.) of an event or task.
 *
 * @version $Id: Attendee.java,v 1.6 2007-11-28 11:14:04 nichele Exp $
 */
public class Attendee extends com.funambol.common.pim.model.common.Property {

    private String name;
    private String uri;
    private short role;
    private short expected;
    private short kind;
    private short status;

    /**
     * Possible value for all short properties (role, expect, status and kind):
     */
    public static final short UNKNOWN = -1;

    /**
     * Possible values for property role:
     */
    public static final short ATTENDEE  = 0;
    public static final short DELEGATE  = 1;
    public static final short ORGANIZER = 2;
    public static final short OWNER     = 3;

    /**
     * Possible values for property expected:
     */
    public static final short NON_PARTICIPANT    = 0;
    public static final short OPTIONAL           = 1;
    public static final short REQUIRED           = 2;
    public static final short REQUIRED_IMMEDIATE = 3;
    public static final short CHAIRMAN           = 4;

   /**
    * Possible values for property kind:
    */
    public static final short INDIVIDUAL = 0;
    public static final short GROUP      = 1;
    public static final short RESOURCE   = 2;
    public static final short ROOM       = 3;

    /**
     * Possible values for property status:
     */
    public static final short DECLINED     = 0;
    public static final short NEEDS_ACTION = 1;
    public static final short SENT         = 2;
    public static final short DELEGATED    = 3;
    public static final short TENTATIVE    = 4;
    public static final short ACCEPTED     = 5;
    public static final short IN_PROCESS   = 6;
    public static final short COMPLETED    = 7;
    
    /**
     * Protocol prefix to use an e-mail address as a URI:
     */
    public static final String MAILTO      = "MAILTO:";

    /** 
     * Creates a new "empty" instance of Attendee (no name, no URI, all other
     * properties set to UNKNOWN).
     */
    public Attendee() {
        this.name     = null   ;
        this.uri      = null   ;
        this.role     = UNKNOWN;
        this.expected = UNKNOWN;
        this.kind     = UNKNOWN;
        this.status   = UNKNOWN;
    }
    
    /** 
     * Creates a new instance of Attendee. 
     *
     * @param name the attendee's name
     * @param uri the attendee's URI
     * @param role the attendee's role
     * @param expected the attendee's expected participation
     * @param kind the attendee's kind
     * @param status the attendee's participation status
     */
    public Attendee(String name    , 
                    String uri     , 
                    short  role    , 
                    short  expected, 
                    short  kind    ,
                    short  status  ) {
        
        this.name     = name    ;
        this.uri      = uri     ;
        this.role     = role    ;
        this.expected = expected;
        this.kind     = kind    ;
        this.status   = status  ;
    }

    /**
     * Gets the display name of the attendee.
     *
     * @return the display name of the attendee
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of the attendee.
     *
     * @param name the display name of the attendee
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the URI of the attendee.
     *
     * @return the URI of the attendee
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the URI of the attendee.
     *
     * @param uri the URI of the attendee
     */
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    /**
     * Retrieves the e-mail address of the attendee from the URI property,
     * provided the URI represents an e-mail address.
     *
     * @return the email address of the attendee (without the "MAILTO:" prefix)
     *         or null if the URI does not represent an e-mail address
     */
    public String getEmail() {
        if (uri.startsWith(MAILTO)) {
            return uri.substring(MAILTO.length());
        } else {
            return null;
        }
    }
    
    /**
     * Sets the URI of the attendee using a MAILTO content.
     *
     * @param email the e-mail address of the attendee (if it's null, the URI
     *              property will be set to null)
     */
    public void setEmail(String email) {
        if (email == null) {
            this.uri = null;
        } else {
            this.uri = MAILTO + email;
        }
    }

    /**
     * Gets the role of the attendee.
     *
     * @return the role of the attendee.
     */
    public short getRole() {
        return role;
    }

    /**
     * Sets the role of the attendee.
     *
     * @param role the role of the attendee.
     */
    public void setRole(short role) {

        switch (role) {
            case ATTENDEE:
            case DELEGATE:
            case ORGANIZER:
            case OWNER:
                this.role = role;
                return;
            default:
                this.role = UNKNOWN;
        }
    }

    /**
     * Gets the expected participation status of the attendee.
     *
     * @return the expected participation status of the attendee
     */
    public short getExpected() {
        return expected;
    }

    /**
     * Sets the expected participation status of the attendee.
     *
     * @param expected the expected participation status of the attendee
     */
    public void setExpected(short expected) {

        switch (expected) {
            case NON_PARTICIPANT:
            case OPTIONAL:
            case REQUIRED:
            case REQUIRED_IMMEDIATE:
            case CHAIRMAN:
                this.expected = expected;
                return;
            default:
                this.expected = UNKNOWN;
        }
    }

    /**
     * Gets the kind of the attendee.
     *
     * @return the kind of the attendee
     */
    public short getKind() {
        return kind;
    }

    /**
     * Sets the kind of the attendee.
     *
     * @param kind the kind of the attendee
     */
    public void setKind(short kind) {

        switch (kind) {
            case NON_PARTICIPANT:
            case OPTIONAL:
            case REQUIRED:
            case REQUIRED_IMMEDIATE:
            case CHAIRMAN:
                this.kind = kind;
                return;
            default:
                this.kind = UNKNOWN;
        }
    }
    
    /**
     * Gets the status of the attendee.
     *
     * @return the status of the attendee
     */
    public short getStatus() {
        return status;
    }

    /**
     * Sets the status of the attendee.
     *
     * @param status the status of the attendee
     */
    public void setStatus(short status) {

        switch (status) {
            case DECLINED:
            case NEEDS_ACTION:
            case SENT:
            case DELEGATED:
            case TENTATIVE:
            case ACCEPTED:
            case IN_PROCESS:
            case COMPLETED:
                this.status = status;
                return;
            default:
                this.status = UNKNOWN;
        }
    }
    
    /**
     * Gets a reasonably reliable hash code for the Attendee object.
     *
     * @return a hash code that follows the Object.hashCode() requirements
     */
    @Override
    public int hashCode() {
        int hc = 0;
        if (uri != null) {
            hc += uri.hashCode();
        }
        if (name != null) {
            hc += name.hashCode();
        }
        hc += role;
        hc += expected;
        hc += kind;
        hc += status;
        
        return hc;
    }
    
    /**
     * Checks whether this Attendee is equal to another Attendee.
     *
     * @param object any other object
     * @return true only if object is an Attendee instance and it has the same
     *              content as this one
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof Attendee) {
            Attendee attendee = (Attendee)object;
            if ((this.name     .equals(attendee.name)   ) &&
                (this.uri      .equals(attendee.uri)    ) &&
                (this.role     ==      attendee.role    ) &&
                (this.expected ==      attendee.expected) &&
                (this.kind     ==      attendee.kind    ) &&
                (this.status   ==      attendee.status  )) {
                return true;
            }     
        }
        return false;
    }
}
