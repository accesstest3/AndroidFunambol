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
 *
 *
 */

package com.funambol.util;

import com.funambol.util.Observer;

/**
 * this interface is used by classes running tasks to implements Observer / Observable 
 * paradigm.
 *<p>
 * Eache watchable can have multiple watchers and must call the method update(object) of 
 * his watchers each time the watchable state changes.
 */

public interface Observable extends Runnable{
    /**
     * add a observer to this observable
     *
     * @return false if observable
     * does not support more observers 
     *
     */ 
    public boolean addObserver(Observer o);
    
    /**
     * remove an observer from this observable
     */ 
    public void removeObserver (Observer o);
    
    
    /**
     * returns a long between 0 an 100 that indicate the %
     * of the task completed. Should be formatted inside the watchable, i.e.
     * returning 13.742913341 is a bad thing :)
     * long is returned instead of double because of cld 1.0 limitations
     */
    public long getProgressPercent();
    
    /**
     * returns a integere between 0 and max giving the progress of the task.
     * if we are syncronizing 37 contacts progress should return the number of
     * contacts syncronized.
     */
    public int getProgress();
    
    /**
     * returns the number of steps the task will use. if we are syncronizing 37 contacts
     * this should return 37
     */ 
    public int getMax();
     
    /**
     * a string that describes the current operation. 
     * if we are syncronizing contacts this should
     * be something like "syncronizing contacts"
     */
    public String getMessage();
    
    /**
     * boolean returning true if task is finished
     */
    public boolean isFinished();
}
