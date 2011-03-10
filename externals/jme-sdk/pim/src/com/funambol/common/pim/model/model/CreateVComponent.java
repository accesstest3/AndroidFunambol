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

import java.util.Iterator;
import java.util.Hashtable;

import com.funambol.common.pim.model.common.VisitorException;
import com.funambol.common.pim.model.common.VisitorInterface;
import com.funambol.common.pim.model.common.VisitorObjectWalk;

import com.funambol.common.pim.model.model.VNote;

/**
 *
 * @version $Id: CreateVComponent.java,v 1.2 2007-11-28 11:14:05 nichele Exp $
 */
public class CreateVComponent extends VisitorObjectWalk {
   protected Hashtable lookups;


   public CreateVComponent() {
   }
   

   /**
    *
    */
   public Object visit(VCalendar vcal, Object arg) throws VisitorException {
       
       lookups = (Hashtable)arg;
       
       //Iterator propIter = vcal.properties.iterator();
       Iterator propIter = vcal.getAllProperties ().iterator ();
       while (propIter.hasNext()) {
           VisitorInterface prop = (VisitorInterface)propIter.next();
           //prop.accept(this, vcal.propTable);
           prop.accept(this, vcal.getAllProperties());
       }
       
       //Iterator vcompIter = vcal.components.iterator();
       Iterator vcompIter = vcal.getAllComponents().iterator();
       while (vcompIter.hasNext()) {
           VisitorInterface vcomp = (VisitorInterface)vcompIter.next();
           vcomp.accept(this, vcal);
       }
       return vcal;
   }
   
   /**
    *
    */
   public Object visit(VEvent event, Object arg) throws VisitorException {
       
       Iterator propIter = event.getAllProperties ().iterator ();
       while (propIter.hasNext()) {
           VisitorInterface prop = (VisitorInterface)propIter.next();
           prop.accept(this, event.getAllProperties());
       }
       Property dtstart = (Property)event.getProperty("DTSTART");
       Property dtend = (Property)event.getProperty("DTEND");
       
       if ((dtstart != null) && (dtend != null)) {
           String fullDay = ValueDateTime.FullDay( dtstart.getValue(), 
                                                   dtend.getValue());
           if (fullDay != null) {
               dtstart.setValue(fullDay);
               Parameter paramfordate = new Parameter("VALUE", "DATE");
               dtstart.addParameter(paramfordate);
               //event.delProperty("DTEND");
               event.delProperty(dtend);
           }
       }
       return null;
   }
   
   /**
    *
    */
   public Object visit(VTodo todo, Object arg) throws VisitorException  {
       Iterator propIter = todo.getAllProperties().iterator();
       while (propIter.hasNext()) {
           VisitorInterface prop = (VisitorInterface)propIter.next();
           prop.accept(this, todo.getAllProperties());
       }
       return null;
   }
   
   /**
    *
    */
   public Object visit(VNote note, Object arg) throws VisitorException  {
       lookups = (Hashtable)arg;
       Iterator propIter = note.getAllProperties().iterator();
       while (propIter.hasNext()) {
           VisitorInterface prop = (VisitorInterface)propIter.next();
           prop.accept(this, note.getAllProperties());
       }
       return null;
   }
   
   /**
    *
    */
   public Object visit(Property prop, Object arg) throws VisitorException  {
       Hashtable table = (Hashtable)arg;
       if (!prop.isCustom()) {
           PropertySemantics ps = (PropertySemantics)lookups.get(prop.getName());
           if (ps == null) {
               //log.info(prop.name + " not understood property");
           } else {
               prop.setPropertySemantics(ps);
               switch (ps.getMaxOccurrences()) {
               case 1:
                   if (table.containsKey(prop.getName())) {
                       //log.info(prop.name + " exists more then once");
                   } else {
                       table.put(prop.getName(), prop);
                   }
               case Integer.MAX_VALUE:
                   // do nothing
               default:
                   // do nothing
               }
               //prop.parameters
               Iterator paramIter = prop.getParameters().iterator();
               while (paramIter.hasNext()) {
                   VisitorInterface param = (VisitorInterface)paramIter.next();
                   param.accept(this, ps);
               }
           }
       }
       return null;
   }
   
   /**
    *
    */
   public Object visit(Parameter param, Object arg) {
       if (!param.custom) {
           PropertySemantics ps = (PropertySemantics)arg;
           if (!ps.checkParameter(param.name, param.value)) {
               //log.info(param.name + " not allowed as parameter for " + ps.name);
           }
       }
       return null;
   }
}
