/* 
 * Copyright (c) 2004 Harrie Hazewinkel. All rights reserved.
 */

/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2006 - 2007 Funambol, Inc.
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
package com.funambol.common.pim.model.model;

import java.util.*;

import com.funambol.common.pim.model.utility.TimeUtils;

/**
 *
 * @version $Id: VComponent.java,v 1.3 2008-04-10 11:16:44 mauro Exp $
 */
public abstract class VComponent implements Comparable {
    
    private List<VComponent> components;
    private List<Property> properties;

    public static final String TYPE_APPOINTMENT = "appointment";
    public static final String TYPE_TASK = "task";

    public static final String[] DATE_PROPERTIES = { "DTSTART", "DTEND", "DUE",
            "COMPLETED", "DCREATED",
            "CREATED", "LAST-MODIFIED", };

    /**
     * Should be overridden in concrete subclass to return component name,
     * for example VEVENT, VTODO or similar
     */
    public abstract String getVComponentName ();

    public abstract String getSifType ();

    public VComponent () {
        this.components = new Vector ();
        this.properties = new Vector ();
    }

    public void addComponent ( VComponent comp ) {
        this.components.add ( comp );
    }

    public void delComponent ( VComponent comp ) {
        this.components.remove ( comp );
    }

    public List<VComponent> getComponents (String name) {

        List<VComponent> list = new ArrayList<VComponent>();
        
        if (name == null) {
            return list; // empty
        }
        
        for (VComponent component : components) {
            if (name.equals(component.getVComponentName())) {
                list.add(component);
            }
        }
        return list; // filled
    }
    
    public VComponent getComponent(String name) {
        
        if (name == null) {
            return null;
        }
        
        for (VComponent component : components) {
            if (name.equals(component.getVComponentName())) {
                return component;
            }
        }
        return null;
    }

    public void addProperty ( Property property ) {
        this.properties.add( property );
    }

    public void addProperty ( String name, String value ) {
        addProperty ( new Property ( name, value ) );
    }

    public void delProperty ( Property property ) {
        this.properties.remove ( property );
    }

    public void setProperty ( Property property ) {
        
        for (Property oldProperty : properties) {
            if (property.getName().equals(oldProperty.getName())) {
                int i = properties.indexOf(oldProperty);
                properties.set(i, property);
                return;
            }
        }
        this.addProperty(property);
    }

    public Property getProperty ( String name ) {
        
        if (name == null) {
            return null;
        }
        
        for (Property property : properties) {
            if (name.equals(property.getName())) {
                return property;
            }
        }
        return null;
    }

    public List<Property> getProperties(String name) {

        List<Property> list = new ArrayList<Property>();
        
        if (name == null) {
            return list; // empty
        }
        
        for (Property property : properties) {
            if (name.equals(property.getName())) {
                list.add(property);
            }
        }
        return list; // filled
    }
    
    public List getAllComponents () {
        return components;
    }

    public List getAllProperties () {
        return properties;
    }

    @Override
    public String toString () {
        return toStringBuffer ( new StringBuffer () ).toString ();
    }

    public StringBuffer toStringBuffer ( StringBuffer buffer ) {
        Iterator pIter = properties.iterator ();
        Object o = null;
        while ( pIter.hasNext () ) {
            o = pIter.next ();
            buffer.append ( o.toString () );
        }
        Iterator cIter = components.iterator ();
        while ( cIter.hasNext () ) {
            buffer.append ( cIter.next ().toString () );
        }
        return buffer;
    }

    public boolean hasProperty ( Property prop ) {
        return false;
    }

    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof VComponent ) {
            VComponent anotherVComponent = (VComponent) obj;
            return this.toString ().equals ( anotherVComponent.toString () );
        }

        return false;
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + 
                (this.components != null ? this.components.hashCode() : 0);
        hash = 79 * hash + 
                (this.properties != null ? this.properties.hashCode() : 0);
        return hash;
    }

    public int compareTo ( Object o ) {
        if ( o instanceof VComponent ) {
            VComponent anotherVComponent = (VComponent) o;
            return this.toString ().compareTo ( anotherVComponent.toString () );
        }

        throw new IllegalArgumentException ( "can compare only VComponent objects" );
    }

    /**
     * @noinspection CloneDoesntCallSuperClone
     */
    @Override
    public Object clone () throws CloneNotSupportedException {
        Class thisInstanceClass = this.getClass ();
        try {
            VComponent newInstanceObject = 
                    (VComponent)thisInstanceClass.newInstance ();
            
            for (VComponent component : components) {
                newInstanceObject.addComponent((VComponent)component.clone());
            }
            for (Property property : properties) {
                newInstanceObject.addProperty((Property)property.clone());
            }


            return newInstanceObject;
        } catch ( InstantiationException e ) {
            throw new RuntimeException ( e );
        } catch ( IllegalAccessException e ) {
            throw new RuntimeException ( e );
        }
    }

    public void convertUTCDatesToLocal ( TimeZone tz ) {
        for ( int i = 0; i < DATE_PROPERTIES.length; i++ ) {
            Property p = getProperty ( DATE_PROPERTIES[i] );
            if ( p != null ) {
                String originalValue = p.getValue ();
                try {
                    p.setValue ( TimeUtils.convertUTCDateToLocal ( originalValue, tz ) );
                } catch ( Exception e ) {
                    throw new IllegalArgumentException ( e.getMessage () );
                }
            }
        }
    }
}
