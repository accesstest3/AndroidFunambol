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
package com.funambol.common.pim.model.calendar;

import java.util.ArrayList;
import java.util.List;

import java.text.ParseException;

import com.funambol.common.pim.model.common.Property;
import com.funambol.common.pim.model.common.PropertyWithTimeZone;
import com.funambol.common.pim.model.common.XTag;
import com.funambol.common.pim.model.converter.TimeZoneHelper;
import com.funambol.common.pim.model.utility.TimeUtils;

/**
 * This class represents the common features of PIM events and tasks (todo's).
 * It's an abstract class because its type can be defined only when one of its
 * subclasses is instantiated.
 *
 * @version $Id: CalendarContent.java,v 1.7 2008-07-17 15:53:15 luigiafassina Exp $
 */
public abstract class CalendarContent {

    //--------------------------------------------------------------- Properties

    protected PropertyWithTimeZone dalarm;
    protected PropertyWithTimeZone palarm;
    protected Property             categories;
    protected Property             accessClass;
    protected Property             description;
    protected Property             latitude;
    protected Property             longitude;
    protected Property             location;
    protected Property             priority;
    protected Property             status;
    protected Property             summary;
    protected PropertyWithTimeZone dtEnd; // Also used for the DUE property of 
                                          //VTODO objects
    protected PropertyWithTimeZone dtStart;
    protected Property             duration;
    protected Property             organizer;
    protected Property             url;
    protected Property             uid;
    protected Property             contact;
    protected PropertyWithTimeZone created;
    protected PropertyWithTimeZone dtStamp;
    protected PropertyWithTimeZone lastModified;
    protected Property             sequence;
    protected Property             folder;
    protected List<XTag>           xTags;

    /*
     * Since these properties are not part of the iCalendar specifications,
     * we do not need to use the Property object to store them:
     */
    protected Boolean allDay;
    protected Integer mileage;
    protected Short   meetingStatus; // 0 non meeting
                                     // 1 meeting
                                     // 3 meeting received
                                     // 5 meeting cancelled
    protected Short   busyStatus; // 0 olFree
                                  // 1 olTentative
                                  // 2 olBusy
                                  // 3 olOutOfOffice

    /*
     * Complex components:
     */
    protected Reminder          reminder;          // may be null
    protected RecurrencePattern recurrencePattern; // may be null
    protected List<Attendee>    attendees;         // may be empty

   /**
     * Returns the access classification for a calendar component.
     *
     * @return the accessClass property
     */
    public Property getAccessClass() {
        return accessClass;
    }

    /**
     * Returns the date and time that the calendar information was created.
     *
     * @return the created property
     */
    public PropertyWithTimeZone getCreated() {
        return created;
    }

    /**
     * Returns the most complete description of the calendar component.
     *
     * @return the description property
     */
    public Property getDescription() {
        return description;
    }

    /**
     * Returns the start date and time for the calendar item.
     *
     * @return the dtStart property
     */
    public PropertyWithTimeZone getDtStart() {
        return dtStart;
    }

    /**
     * Returns the latitude of the location of this event or task.
     *
     * @return the latitude property
     */
    public Property getLatitude() {
        return latitude;
    }

    /**
     * Returns the longitude of the location of this event or task.
     *
     * @return the longitude property
     */
    public Property getLongitude() {
        return longitude;
    }

    /**
     * Returns the date and time of the last revision of this calendar item.
     *
     * @return the lastModified property
     */
    public PropertyWithTimeZone getLastModified() {
        return lastModified;
    }

    /**
     * Returns the location of this calendar item.
     *
     * @return the location property
     */
    public Property getLocation() {
        return location;
    }

    /**
     * Returns the organizer for the event or task.
     *
     * @return the organizer property
     */
    public Property getOrganizer() {
        return organizer;
    }


    /**
     * Returns the relative priority for the calendar item.
     *
     * @return the priority property
     */
    public Property getPriority() {
        return priority;
    }

    /**
     * Returns the date and time that the iCalendar representation of the 
     * calendar item was created.
     *
     * @return the dtStamp property
     * 
     * @deprecated This information is too strictly related to a particular 
     *             representation of the data than the data itself. The created
     *             property can be used to indicate the creation time of the
     *             calendar item without any reference to the iCalendar format.
     */
    public PropertyWithTimeZone getDtStamp() {
        return dtStamp;
    }

    /**
     * Returns the revision sequence number.
     *
     * @return the sequence property
     */
    public Property getSequence() {
        return sequence;
    }

    /**
     * Returns the status of the calendar item.
     *
     * @return the status property
     */
    public Property getStatus() {
        return status;
    }

    /**
     * Returns the unique ID of this calendar item. This is not the internal ID
     * of the DS server, but an ID set by the original system where this item 
     * was created. Its uniqueness cannot actually be guaranteed across the
     * synchronisation.
     *
     * @return the uid property
     */
    public Property getUid() {
        return uid;
    }

    /**
     * Returns the url for the calendar item.
     *
     * @return the url property
     */
    public Property getUrl() {
        return url;
    }

    /**
     * Returns the end date and time for the event, or the due date and time for
     * the task.
     *
     * @return the dtEnd property
     */
    public PropertyWithTimeZone getDtEnd() {
        return dtEnd;
    }

    /**
     * Returns the duration of the event or task.
     *
     * @return the duration property
     */
    public Property getDuration() {
        return duration;
    }

    /**
     * Returns the summary.
     *
     * @return the summary property
     */
    public Property getSummary() {
        return summary;
    }

    /**
     * Returns the categories this calendar item belongs to.
     *
     * @return the categories property
     */
    public Property getCategories() {
        return categories;
    }

    /**
     * Returns the contact information or alternately a reference to contact
     * information associated with the calendar component.
     *
     * @return the contact property
     */
    public Property getContact() {
        return contact;
    }

    /**
     * Returns a list of custom tags.
     *
     * @return a List of XTag objects
     */
    public List<XTag> getXTags() {
       return xTags;
    }

    /**
     * Returns the display reminder (i.e. a visual alarm).
     *
     * @return the dalarm property
     * 
     * @deprecated This information is too strictly related to a particular 
     *             representation of the data than the data itself. The display
     *             reminder will be supported in a future version as a special
     *             case of the reminder property, represented by a Reminder 
     *             object.
     */
    public PropertyWithTimeZone getDAlarm() {
        return dalarm;
    }

    /**
     * Returns the procedure reminder (i.e. an alarm that launches a procedure).
     * 
     * @return the palarm property
     * 
     * @deprecated This information is too strictly related to a particular 
     *             representation of the data than the data itself. The 
     *             procedure reminder will be supported in a future version as a
     *             special case of the reminder property, represented by a 
     *             Reminder object.
     */
    public PropertyWithTimeZone getPAlarm() {
        return palarm;
    }

    /**
     * Returns the all-day flag of the calendar item.
     * 
     * @return true if the event is all-day, false if it is timed; if the allDay
     *         property is not set, false is returned by default
     */
    public boolean isAllDay() {
        return (allDay != null) ? allDay.booleanValue() : false;
    }

    /**
     * Getter for property allDay.
     * 
     * @return the allDay property (may be null)
     * 
     * @deprecated The null case does not have a clear semantics. Method 
     *             isAllDay (returning a boolean instead of a Boolean) is 
     *             clearer and must be used instead of this.
     */
    public Boolean getAllDay() {
        return allDay;
    }

    /**
     * Setter for property allDay.
     * 
     * @param allDay new value of property allDay
     */
    public void setAllDay(Boolean allDay) {
        this.allDay = allDay;
    }
    
    /**
     * Sets the all-day flag.
     * 
     * @param allDay a boolean that will be stored in the allDay property (a 
     *               Boolean object)
     */
    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }    

    /**
     * Returns the meeting status of the calendar item.
     * 
     * @return value of property meetingStatus
     */
    public Short getMeetingStatus() {
        return meetingStatus;
    }

    /**
     * Setter for property meetingStatus.
     * 
     * @param meetingStatus new value of property meetingStatus
     */
    public void setMeetingStatus(Short meetingStatus) {
        this.meetingStatus = meetingStatus;
    }

    /**
     * Returns the busy status of the calendar item.
     * 
     * @return value of property busyStatus
     */
    public Short getBusyStatus() {
        return busyStatus;
    }

    /**
     * Setter for property busyStatus.
     * 
     * @param busyStatus new value of property busyStatus
     */
    public void setBusyStatus(Short busyStatus) {
        this.busyStatus = busyStatus;
    }

    /**
     * Returns the mileage attached to this calendar item.
     * 
     * @return value of property mileage
     */
    public Integer getMileage() {
        return mileage;
    }

    /**
     * Setter for property mileage.
     * 
     * @param mileage new value of property mileage
     */
    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    /**
     * Returns the calendar item's recurrence pattern.
     * 
     * @return value of property recurrencePattern
     */
    public RecurrencePattern getRecurrencePattern() {
        return recurrencePattern;
    }

    /**
     * Setter for property recurrencePattern.
     * 
     * @param recurrencePattern new value of property recurrencePattern
     */
    public void setRecurrencePattern(RecurrencePattern recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }

    /**
     * Setter for property categories.
     * 
     * @param categories new value of property categories
     */
    public void setCategories(Property categories) {
        this.categories = categories;
    }

    /**
     * Setter for property accessClass.
     *
     * @param accessClass new value of property accessClass
     */
    public void setAccessClass(Property accessClass) {
        this.accessClass = accessClass;
    }

    /**
     * Setter for property description.
     * 
     * @param description new value of property description
     */
    public void setDescription(Property description) {
        this.description = description;
    }

    /**
     * Setter for property latitude.
     * 
     * @param latitude new value of property latitude
     */
    public void setLatitude(Property latitude) {
        this.latitude = latitude;
    }

    /**
     * Setter for property longitude.
     * 
     * @param longitude new value of property longitude
     */
    public void setLongitude(Property longitude) {
        this.longitude = longitude;
    }

    /**
     * Setter for property location.
     * 
     * @param location new value of property location
     */
    public void setLocation(Property location) {
        this.location = location;
    }

    /**
     * Setter for property priority.
     * 
     * @param priority new value of property priority
     */
    public void setPriority(Property priority) {
        this.priority = priority;
    }

    /**
     * Setter for property status.
     * 
     * @param status new value of property status
     */
    public void setStatus(Property status) {
        this.status = status;
    }

    /**
     * Setter for property summary.
     * 
     * @param summary new value of property summary
     */
    public void setSummary(Property summary) {
        this.summary = summary;
    }

    /**
     * Setter for property dtEnd.
     * 
     * @param dtEnd new value of property dtEnd
     */
    public void setDtEnd(PropertyWithTimeZone dtEnd) {
        this.dtEnd = dtEnd;
    }

    /**
     * Setter for property dtEnd.
     * 
     * @param dtEnd new value of property dtEnd
     */
    public void setDtEnd(Property dtEnd) {
        this.dtEnd = new PropertyWithTimeZone(dtEnd, null);
    }

    /**
     * Setter for property dtStart.
     * 
     * @param dtStart new value of property dtStart
     */
    public void setDtStart(PropertyWithTimeZone dtStart) {
        this.dtStart = dtStart;
    }

    /**
     * Setter for property dtStart.
     * 
     * @param dtStart new value of property dtStart
     */
    public void setDtStart(Property dtStart) {
        this.dtStart = new PropertyWithTimeZone(dtStart, null);
    }

    /**
     * Setter for property duration.
     * 
     * @param duration new value of property duration
     */
    public void setDuration(Property duration) {
        this.duration = duration;
    }

    /**
     * Setter for property organizer.
     * 
     * @param organizer new value of property organizer
     */
    public void setOrganizer(Property organizer) {
        this.organizer = organizer;
    }

    /**
     * Setter for property url.
     * 
     * @param url new value of property url
     */
    public void setUrl(Property url) {
        this.url = url;
    }

    /**
     * Setter for property uid.
     * 
     * @param uid new value of property uid
     */
    public void setUid(Property uid) {
        this.uid = uid;
    }

    /**
     * Setter for property contact.
     * 
     * @param contact new value of property contact
     */
    public void setContact(Property contact) {
        this.contact = contact;
    }

    /**
     * Setter for property created.
     * 
     * @param created new value of property created
     */
    public void setCreated(PropertyWithTimeZone created) {
        this.created = created;
    }

    /**
     * Setter for property created.
     * 
     * @param created new value of property created
     */
    public void setCreated(Property created) {
        this.created = new PropertyWithTimeZone(created, null);
    }

    /**
     * Setter for property dtStamp.
     * 
     * @param dtStamp new value of property dtStamp
     */
    public void setDtStamp(PropertyWithTimeZone dtStamp) {
        this.dtStamp = dtStamp;
    }

    /**
     * Setter for property dtStamp.
     * 
     * @param dtStamp new value of property dtStamp
     */
    public void setDtStamp(Property dtStamp) {
        this.dtStamp = new PropertyWithTimeZone(dtStamp, null);
    }

    /**
     * Setter for property lastModified.
     * 
     * @param lastModified new value of property lastModified
     */
    public void setLastModified(PropertyWithTimeZone lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Setter for property lastModified.
     * 
     * @param lastModified new value of property lastModified
     */
    public void setLastModified(Property lastModified) {
        this.lastModified = new PropertyWithTimeZone(lastModified, null);
    }

    /**
     * Setter for property xTags.
     * 
     * @param xTags new value of property xTags
     */
    public void setXTag(List xTags) {
        this.xTags = xTags;
    }

    /**
     * Setter for property dalarm.
     * 
     * @param dalarm new value of property dalarm
     */
    public void setDAlarm(PropertyWithTimeZone dalarm) {
        this.dalarm = dalarm;
    }

    /**
     * Setter for property dalarm on the basis of a Property (without time 
     * zone).
     * 
     * @param dalarm new value of property dalarm as a Property (the time zone
     *               is set to null)
     */
    public void setDAlarm(Property dalarm) {
        this.dalarm = new PropertyWithTimeZone(dalarm, null);
    }

    /**
     * Setter for property palarm.
     * 
     * @param palarm new value of property palarm
     */
    public void setPAlarm(PropertyWithTimeZone palarm) {
        this.palarm = palarm;
    }

    /**
     * Setter for property palarm on the basis of a Property (without time 
     * zone).
     * 
     * @param palarm new value of property palarm as a Property (the time zone
     *               is set to null)
     */
    public void setPAlarm(Property palarm) {
        this.palarm = new PropertyWithTimeZone(palarm, null);
    }

    /**
     * Setter for property sequence.
     * 
     * @param sequence new value of property sequence
     */
    public void setSequence(Property sequence) {
        this.sequence = sequence;
    }

    /**
     * Getter for property reminder.
     * 
     * @return Value of property reminder.
     */
    public Reminder getReminder() {
        return reminder;
    }

    /**
     * Setter for property reminder.
     * @param reminder new value of property reminder.
     */
    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }

    /**
     * Returns the folder where this calendar item has to be stored.
     * 
     * @return value of property folder
     */
    public Property getFolder() {
        return folder;
    }

    /**
     * Setter for property folder.
     * 
     * @param folder new value of property folder
     */
    public void setFolder(Property folder) {
        this.folder = folder;
    }

    /**
     * Gets the attendee list.
     *
     * @return a List of Attendee objects
     */
    public List<Attendee> getAttendees() {
        return attendees;
    }    
    
    //---------------------------------------------------------------- Constants
    
    private final static long ONE_YEAR = 31622400000L; // 366 days
    private final static long ONE_DAY = 86400000L; // 24 hours
    
    //------------------------------------------------------------- Constructors

    /**
     * Creates a new empty instance of CalendarContent.
     */
    public CalendarContent() {
        categories   = new Property();
        accessClass  = new Property();
        description  = new Property();
        latitude     = new Property();
        longitude    = new Property();
        location     = new Property();
        lastModified = new PropertyWithTimeZone();
        priority     = new Property();
        dtStamp      = new PropertyWithTimeZone();
        sequence     = new Property();
        status       = new Property();
        summary      = new Property();
        uid          = new Property();
        url          = new Property();
        dtEnd        = new PropertyWithTimeZone();
        dtStart      = new PropertyWithTimeZone();
        duration     = new Property();
        organizer    = new Property();
        contact      = new Property();
        created      = new PropertyWithTimeZone();
        dalarm       = new PropertyWithTimeZone();
        palarm       = new PropertyWithTimeZone();
        folder       = new Property();

        xTags         = null;
        allDay        = null;
        meetingStatus = null;
        mileage       = null;

        recurrencePattern = null;
        reminder          = null;
        attendees         = new ArrayList<Attendee>();
    }

    //----------------------------------------------------------- Public methods

    /**
     * Returns whether this event is recurrent or not, i.e. if it has got a
     * recurrence pattern or not.
     *
     * @return true if this event is recurrent, false otherwise
     */
    public boolean isRecurrent() {
        if (recurrencePattern != null) {
            return true;
        }
        return false;
    }

    /**
     * Makes this event not recurrent by removing its recurrence pattern.
     */
    public void removeRecurrence() {
        recurrencePattern = null;
    }

    /**
     * Adds a custom X-tag to the xTags list. If the list does not exist yet, it
     * is created.
     *
     * @param xTag the tag to add
     */
    public void addXTag(XTag xTag) {
        if (xTag == null) {
            return;
        }

        if (xTags == null) {
            xTags = new ArrayList<XTag>();
        }

        for (XTag t : xTags) {
            if (t.getXTagValue().equals(xTag.getXTagValue())) {
                t.getXTag().setPropertyValue(xTag.getXTag().getPropertyValue());
                return;
            }
        }
        xTags.add(xTag);
    }
 
    /**
     * Adds an attendee to the list.
     *
     * @param attendee the Attendee object to add
     */
    public void addAttendee(Attendee attendee) {
        this.attendees.add(attendee);
    }

    /**
     * Clears the attendee list.
     */
    public void resetAttendees() {
        this.attendees.clear();
    }
    
    /**
     * Extracts a time interval roughly large enough to contain the whole
     * event/task and, in case it's a recurrent one, all its occurrences.
     *
     * @return an array of 2 long integers, the first one being the lower end of
     *         the interval and the other one being the upper end
     */
    public long[] extractInterval() {

        final long DEFAULT_FROM = TimeZoneHelper.getReferenceTime()
                - ONE_YEAR; // 1 year ago
        final long DEFAULT_TO = TimeZoneHelper.getReferenceTime()
                + (ONE_YEAR * 2); // 2 years in the future
        final long DEFAULT_TO_UNLIMITED = DEFAULT_TO
                + (ONE_YEAR * 2); // 4 years in the future

        String low = null;
        if (getDtStart() != null) {
            // If there is a start date/time, it is the lower end of the 
            // interval
            low = getDtStart().getPropertyValueAsString();
        }
        if ((low == null) || ("".equals(low))) {
            if (getDtEnd() != null) {
                // If there is no start date/time, but there is and end 
                // date/time, it is the lower end of the interval
                low = getDtEnd().getPropertyValueAsString();
            }
        }
        long from, to;
        if ((low == null) || ("".equals(low))) {
            // If no lower end has been set, the default one is used
            from = DEFAULT_FROM;
        } else {
            // The lower end is moved back to the first midnight
            try {
                from = TimeUtils.getMidnightTime(low);
            } catch (ParseException e) {
                from = DEFAULT_FROM;
            }
        }

        if (isRecurrent()){
            RecurrencePattern rp = getRecurrencePattern();
            if (rp.getOccurrences() != -1) { // finite number of occurrences
                int period;
                switch (rp.getTypeId()) {
                    case RecurrencePattern.TYPE_DAILY:
                        period = 1;
                        break;
                    case RecurrencePattern.TYPE_WEEKLY:
                        period = 7;
                        break;
                    case RecurrencePattern.TYPE_MONTHLY:
                    case RecurrencePattern.TYPE_MONTH_NTH:
                        period = 31; // large enough for all months
                        break;
                    case RecurrencePattern.TYPE_YEARLY:
                    case RecurrencePattern.TYPE_YEAR_NTH:
                        period = 366; // large enough for all years
                        break;
                    default:
                        period = 0;
                }
                if (period != 0) {
                    // The upper end is set to a sufficient distance from the
                    // lower end in order to be able to contain the whole
                    // recurrence
                    period *= rp.getInterval();
                    to = from + (period * rp.getOccurrences() * ONE_DAY);
                } else {
                    // If something goes wrong, the default upper end is used
                    to = DEFAULT_TO;
                }
            } else { // no occurrence number specified
                if (rp.isNoEndDate()) { // unlimited recurrence
                    // If the recurrence is unlimited, a special default value
                    // is used
                    to = DEFAULT_TO_UNLIMITED;
                } else {
                    // If the recurrence has an end date/time, the following
                    // midnight is used as the interval's upper end
                    try {
                        String high = rp.getEndDatePattern();
                        to = TimeUtils.getMidnightTime(high) + ONE_DAY;
                    } catch (Exception e) {
                        to = DEFAULT_TO;
                    }
                }
            }
            // If a positive exception extends the duration of the recurrence 
            // beyond the upper end of the interval, the upper end is moved 
            // onwards until the first midnight after that exception
            for (ExceptionToRecurrenceRule etrr : rp.getExceptions()) {
                if (etrr.isAddition()) {
                    String rdate = etrr.getDate();
                    try {
                        long extra = TimeUtils.getMidnightTime(rdate) + ONE_DAY;
                        if (extra > to) {
                            to = extra;
                        }
                    } catch (ParseException e) {
                        // Ignores this positive exception (RDATE)
                    }
                }  // Negative exceptions (EXDATE) are ignored
            }
        } else { // no recurrence
            to = DEFAULT_TO;
        }

        // In any case, the interval is always large at least as the default 
        // interval
        if (from > DEFAULT_FROM) {
           from = DEFAULT_FROM;
        }
        if (to < DEFAULT_TO) {
            to = DEFAULT_TO;
        }
        
        return new long[]{from, to};
    }    
}