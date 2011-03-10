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

import com.funambol.common.pim.model.common.Visitor;
import com.funambol.common.pim.model.common.VisitorException;
import com.funambol.common.pim.model.common.VisitorInterface;
import com.funambol.common.pim.model.common.VisitorObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * This object represents a property for ICalendar (V2), VCalendar, VNote object
 * (i.e. its value and its parameters)
 *
 * @version $Id: Property.java,v 1.3 2008-04-10 11:13:38 mauro Exp $
 */
public class Property implements VisitorInterface, Comparable {
    /**
     * Property semantics.
     */
    private PropertySemantics ps;
    /**
     * name.
     */
    private String name;
    /**
     * custom indicator.
     */
    private boolean custom;
    /**
     * The paramters of the property.
     */
    private List parameters;
    private Map parametersTable = new Hashtable ();
    /**
     * value.
     */
    private String value;

    // ---------------------------------------------------------- Constructors

    /**
     * Constructor for a property
     *
     * @param n The name of the property.
     * @param x Indicator if it is a custom property.
     * @param p The parameter list.
     * @param v The value of the property.
     */
    public Property ( String n, boolean x, List p, String v ) {
        ps = null;
        name = n;
        custom = x;
        value = v;

        setParameters ( p );
    }

    /**
     * Constructor for a property
     *
     * @param n The name of the property.
     * @param v The value of the property.
     */
    public Property ( String n, String v ) {
        this ( n, n.charAt ( 0 ) == 'X', new ArrayList (), v );
    }

    /**
     * Constructor for a property
     *
     * @param ps The semantics of the property.
     * @param v  The value of the property.
     */
    public Property ( PropertySemantics ps, String v ) {
        this ( ps.name, v );
        this.ps = ps;
    }

    /**
     * Adds the semantics object of the property.
     */
    public void setPropertySemantics ( PropertySemantics ps ) {
        this.ps = ps;
    }
    // -------------------------------------------------------------- Visitors

    /**
     * visitor.
     */
    public void accept ( Visitor v ) throws VisitorException {
        v.visitProperty ( this );
    }

    /**
     * visitor with arguments.
     */
    public Object accept ( VisitorObject v, Object argu ) throws VisitorException {
        return v.visitProperty ( this, argu );
    }

    public String toString () {
        return toString ( new StringBuffer () ).toString ();
    }

    public StringBuffer toString ( StringBuffer buffer ) {
        buffer.append ( name );
        Iterator pIter = parameters.iterator ();
        while ( pIter.hasNext () ) {
            buffer.append ( ";" );
            buffer.append ( pIter.next ().toString () );
        }
        buffer.append ( ":" );
        buffer.append ( value.replaceAll ( "\r\n", "\\\\N" ).replaceAll ( "\n", "\\\\N" ) );
        buffer.append ( "\r\n" );
        return buffer;
    }

    public boolean equals ( Object obj ) {
        if ( obj instanceof  Property ) {
            Property anotherProperty = (Property) obj;
            return this.toString ().equals ( anotherProperty.toString () );
        }

        throw new IllegalArgumentException ( "can compare only Property objects" );
    }

    public int compareTo ( Object o ) {
        if ( o instanceof Property ) {
            Property anotherProperty = (Property) o;
            return this.toString ().compareTo ( anotherProperty.toString () );
        }

        throw new IllegalArgumentException ( "can compare only Property objects" );
    }

    public void addParameter ( Parameter p ) {
        this.parameters.add ( p );
        this.parametersTable.put ( p.name.toUpperCase (), p );
    }

    public void delParameter ( Parameter p ) {
        this.parameters.remove ( p );
        this.parametersTable.remove ( p.name.toUpperCase () );
    }

    public Parameter getParameter ( String name ) {
        return (Parameter) parametersTable.get ( name.toUpperCase () );
    }

    public void setParameter ( Parameter p ) {
        parametersTable.put ( p.name.toUpperCase (), p );
        if ( ! this.parameters.contains ( p ) ) {
            this.parameters.add ( p );
        }
    }

    public PropertySemantics getPs () {
        return ps;
    }

    public String getName () {
        return name;
    }

    public boolean isCustom () {
        return custom;
    }

    public List getParameters () {
        return parameters;
    }

    public void setParameters ( List parameters ) {
        this.parameters = parameters;
        this.parametersTable.clear ();
        for ( int i = 0; i < this.parameters.size (); i++ ) {
            Parameter p = (Parameter) this.parameters.get ( i );
            this.parametersTable.put ( p.name, p );
        }
    }

    public String getValue () {
        return value;
    }

    public void setValue ( String value ) {
        this.value = value;
    }

    /** @noinspection CloneDoesntCallSuperClone*/
    public Object clone () throws CloneNotSupportedException {
        return new Property ( this.name, this.custom, this.parameters, this.value );
    }
}
