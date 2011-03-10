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

/**
 * This class represents the reminder of an event or task.
 *
 * @version $Id: Reminder.java,v 1.3 2008-04-10 10:49:22 mauro Exp $
 */
public class Reminder extends PropertyWithTimeZone {

    // -------------------------------------------------------------- Properties

    private boolean active     ;
    private String  time       ; // alarm time in absolute terms
    private String  soundFile  ; // path of the digital sound to be played
    private int     interval   ; // snooze interval (in minutes)
    private int     repeatCount; // number of snoozes left
    private int     options    ; // extra options
    
    // Obsolete field still used for backward compatibility:
    private int     minutes    ; // alarm time as minutes before event or task 
                                                                       // starts

    /**
     * Returns how many minutes before the start of the event or task the 
     * reminder should activate.
     * 
     * @return value of property minutes
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * Setter for property minutes. setTime(String) must be used whenever no 
     * start date/time is available; if the start date/time is available also
     * this method can be used.
     * NB: Consistence between property minutes and property time cannot be 
     * guaranteed.
     * 
     * @param minutes new value of property minutes
     */
    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }
    
    /**
     * Returns the date and time when the reminder is going to be activated.
     * 
     * @return value of property time as a string in ISO-8601
     *         "yyyyMMdd'T'HHmmss'Z'" or "yyyyMMdd'T'HHmmss" format
     */
    public String getTime() {
        return time;
    }
    
    /**
     * Setter for property time.
     * NB: Consistence between property minutes and property time cannot be 
     * guaranteed.
     * 
     * @param time new value of property time as a string in ISO-8601
     *             "yyyyMMdd'T'HHmmss'Z'" or "yyyyMMdd'T'HHmmss" format
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Returns the path of the sound file played as a reminder.
     * 
     * @return value of property soundFile
     */
    public String getSoundFile() {
        return soundFile;
    }

    /**
     * Setter for property soundFile.
     * 
     * @param soundFile new value of property soundFile
     */
    public void setSoundFile(String soundFile) {
        this.soundFile = soundFile;
    }

    /**
     * Returns the extra options.
     * 
     * @return value of property options
     */
    public int getOptions() {
        return options;
    }

    /**
     * Setter for property options.
     * 
     * @param options new value of property options
     */
    public void setOptions(int options) {
        this.options = options;
    }

    /**
     * Returns whether the reminder is active or switched off.
     * 
     * @return true only if the reminder is set and active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Setter for property active.
     * 
     * @param active new value of property active
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the interval between snoozes, in minutes.
     *
     * @return value of property interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Setter for property interval.
     * 
     * @param interval new value of property interval
     */
    public void setInterval(int interval) {
        this.interval = interval;
    }

    /**
     * Returns the number of times that the reminder has still to be repeated.
     *
     * @return value of property repeatCount
     */
    public int getRepeatCount() {
        return repeatCount;
    }

    /**
     * Setter for property repeatCount.
     * 
     * @param repeatCount new value of property repeatCount
     */
    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    // ------------------------------------------------------------ Constructors

    /**
     * Creates a new empty and inactive instance of Reminder 
     */
    public Reminder() {
        active      = false;
        minutes     = 0;
        options     = 0;
        soundFile   = null;
        interval    = 0;
        repeatCount = 0;
    }

    // ---------------------------------------------------------- Public methods


    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("<Reminder [");
        sb.append("Active: "       ).append(active     );
        sb.append(", Interval: "   ).append(interval   );
        sb.append(", Time: "       ).append(time       );
        sb.append(", Minutes: "    ).append(minutes    );
        sb.append(", Options: "    ).append(options    );
        sb.append(", RepeatCount: ").append(repeatCount);
        sb.append(", SoundFile: "  ).append(soundFile  );
        sb.append("]>");
        
        return sb.toString();
    }
}
