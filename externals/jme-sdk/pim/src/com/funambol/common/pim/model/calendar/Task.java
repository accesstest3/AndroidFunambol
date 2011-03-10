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
package com.funambol.common.pim.model.calendar;

import com.funambol.common.pim.model.common.PropertyWithTimeZone;
import com.funambol.common.pim.model.common.Property;
import com.funambol.common.pim.model.converter.CalendarStatus;

import com.funambol.util.StringUtil;

/**
 * An object representing a task (todo) contained in a calendar item, with all 
 * the information supported by Funambol it contains.
 * This is the "foundational model" of a task, used to exchange information 
 * about such items between server and connectors. It can also be used by 
 * clients.
 * This object provide normalization of the following properties:
 * - status
 * - complete
 * - percent complete
 * if one or more of these propeties state that the task is completed, the other
 * properties will be filled with proper values.
 * That is, if one of the properties listed above says that the task is completed
 * both the left properties will say the same:
 * status = 4 => complete = true, percent complete = 100
 * complete = true=> status = 4 , percent complete = 100
 * percent complete = 100 => complete = true, status = 4
 * On the other side, if none of these properties say that the task is completed,
 * the date completed property is returned as empty, without considering any values
 * set before:
 * status != 4, percent complete != 100, complete = false => date complete = null
 *
 * 
 * @version $Id: Task.java,v 1.4 2008-04-17 17:03:00 mauro Exp $
 */
public class Task extends CalendarContent {
    public static final String HUNDRED_PERCENT = "100";

    //--------------------------------------------------------------- Properties

    private Property actualWork;
    private Property billingInformation;
    private Property companies;
    private Property complete;
    private PropertyWithTimeZone dateCompleted;
    private Property owner;
    private Property percentComplete;
    private Property teamTask;
    private Property totalWork;

    /**
     * Returns the time spent so far for the completion of the task.
     * 
     * @return value of property actualWork
     */
    public Property getActualWork() {
        return this.actualWork;
    }

    /**
     * Returns information for the task's billing.
     * 
     * @return value of property billingInformation
     */
    public Property getBillingInformation() {
        return this.billingInformation;
    }

    /**
     * Returns the date/time when the task has been completed.
     * 
     * @return value of property dateCompleted
     */
    public PropertyWithTimeZone getDateCompleted() {
        if(isTaskCompleted()) {
            return this.dateCompleted;
        } else
            return new PropertyWithTimeZone();
    }

    /**
     * Returns the owner of the task.
     * 
     * @return value of property owner
     */
    public Property getOwner() {
        return this.owner;
    }

    /**
     * Returns the task's completion percentage.
     * 
     * @return value of property percentComplete
     */
    public Property getPercentComplete() {
        if(isTaskCompleted()) {
            if(percentComplete==null) {
                percentComplete = new Property();
            }
            percentComplete.setPropertyValue(HUNDRED_PERCENT);
        }
        return percentComplete;
    }

    /**
     * Returns whether this is a team task or an individual one.
     * 
     * @return value of property teamTask
     * @deprecated Since version 7.1.0, getTeamTask should be used instead,
     *             because methods starting with "is" usually return booleans.
     */
    public Property isTeamTask() {
        return this.teamTask;
    }
    
    /**
     * Returns whether this is a team task or an individual one.
     * 
     * @return value of property teamTask
     */
    public Property getTeamTask() {
        return this.teamTask;
    }

    /**
     * Returns the total time that should be spent for the completion of the 
     * task.
     * 
     * @return value of property totalWork
     */
    public Property getTotalWork() {
        return this.totalWork;
    }

    /**
     * Returns whether the task has been completed or not.
     * 
     * @return value of property complete
     */    
    public Property getComplete() {
        if(isTaskCompleted()) {
            if(this.complete==null) {
                this.complete = new Property(); 
            }
            this.complete.setPropertyValue(Boolean.TRUE);
        }
        return this.complete;
    }

    /**
     * Setter for property actualWork.
     * 
     * @param actualWork new value of property actualWork
     */
    public void setActualWork(Property actualWork) {
        this.actualWork = actualWork;
    }
    
    /**
     * Setter for property billingInformation.
     * 
     * @param billingInformation new value of property billingInformation
     */
    public void setBillingInformation(Property billingInformation) {
        this.billingInformation = billingInformation;
    }
    
    /**
     * Setter for property complete.
     * 
     * @param complete new value of property complete
     */
    public void setComplete(Property complete) {
        this.complete = complete;
    }
    
    /**
     * Setter for property dateCompleted.
     * 
     * @param dateCompleted new value of property dateCompleted
     */
    public void setDateCompleted(PropertyWithTimeZone dateCompleted) {
        this.dateCompleted = dateCompleted;
    }
    
    /**
     * Setter for property dateCompleted on the basis of a Property (without 
     * time zone).
     * 
     * @param dateCompleted new value of property dateCompleted as a Property 
     *                      (the time zone is set to null)
     */
    public void setDateCompleted(Property dateCompleted) {
        this.dateCompleted = new PropertyWithTimeZone(dateCompleted, null);
    }
    
    /**
     * Setter for property owner.
     * 
     * @param owner new value of property owner
     */
    public void setOwner(Property owner) {
        this.owner = owner;
    }
    
    /**
     * Setter for property percentComplete.
     * 
     * @param percentComplete new value of property percentComplete
     */
    public void setPercentComplete(Property percentComplete) {
        this.percentComplete = percentComplete ;
    }
    
    /**
     * Setter for property teamTask.
     * 
     * @param teamTask new value of property teamTask
     */
    public void setTeamTask(Property teamTask) {
        this.teamTask = teamTask;
    }
    
    /**
     * Setter for property totalWork.
     * 
     * @param totalWork new value of property totalWork
     */
    public void setTotalWork(Property totalWork) {
        this.totalWork = totalWork;
    }
    
    /**
     * Returns the due date. Property dueDate is treated as an alias for dtEnd.
     * 
     * @return value of property dtEnd
     */
    public PropertyWithTimeZone getDueDate() {
        return getDtEnd();
    }
    
    /**
     * Setter for the due date. Property dueDate is treated as an alias for 
     * dtEnd.
     * 
     * @param dueDate new value of property dtEnd
     */
    public void setDueDate(PropertyWithTimeZone dueDate) {
        setDtEnd(dueDate);
    }

    /**
     * Setter for the due date on the basis of a Property (without time zone).
     * Property dueDate is treated as an alias for dtEnd.
     * 
     * @param dueDate new value of property dtEnd as a Property (the time zone 
     *                is set to null)
     */
    public void setDueDate(Property dueDate) {
        setDtEnd(dueDate);
    }

    /**
     * Returns the task's importance. Property importance is treated as an alias
     * for priority.
     * 
     * @return value of property priority
     */
    public Property getImportance() {
        return getPriority(); // importance is treated as an alias for priority
    }

    /**
     * Setter for the importance. Property importance is treated as an alias for
     * priority.
     * 
     * @param importance new value of property priority
     */
    public void setImportance(Property importance) {
        setPriority(importance); // importance is treated as an alias for priority
    }

    /**
     * Returns the task's sensitivity. Property sensitivity is treated as an 
     * alias for accessClass.
     * 
     * @return value of property accessClass
     */    
    public Property getSensitivity() {
        return getAccessClass(); // sensitivity is treated as an alias for
    }                                                             // accessClass

    /**
     * Setter for the sensitivity. Property sensitivity is treated as an alias 
     * for accessClass.
     * 
     * @param sensitivity new value of property accessClass
     */    
    public void setSensitivity(Property sensitivity) {
        setAccessClass(sensitivity); // sensitivity is treated as an alias for
    }

    /**
     * Returns the status of the task item.
     *
     * @return the status property
     */
    public Property getStatus() {
        if(isTaskCompleted()) {
            if(status==null) {
                this.status = new Property();
            }
            status.setPropertyValue(CalendarStatus.COMPLETED.getServerValue());
        }
        return super.getStatus();
    }


   //-------------------------------------------------------------- Constructors

    /**
     * Creates en empty task.
     */
    public Task() {
        super();
        actualWork         = new Property();
        billingInformation = new Property();
        companies          = new Property();
        complete           = new Property();
        dateCompleted      = new PropertyWithTimeZone();
        owner              = new Property();
        percentComplete    = new Property();
        teamTask           = new Property();
        totalWork          = new Property();
    }

    //----------------------------------------------------------- Public methods                                                           // accessClass

    /**
     * allows to understand if a task is completed considering three different
     * properties, the task status, the complete property and the percentComplete
     *
     * @return true if the task is completed, that is if the complete boolean is
     * set to true or the percent complete is set to 100 or the status is set
     * to CalendarStatus.COMPLETED.serverValue, false otherwise.
     */

    private boolean isTaskCompleted() {
        return
           (this.status!=null && CalendarStatus.COMPLETED.getServerValue().equals(status.getPropertyValue())) ||
           (this.complete!=null && Boolean.TRUE.equals(this.complete.getPropertyValue())) ||
           (this.percentComplete!=null && HUNDRED_PERCENT.equals(this.percentComplete.getPropertyValue()));
    }
}
