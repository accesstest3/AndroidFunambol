/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2009 Funambol, Inc.
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

package com.funambol.common.pim.icalendar;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Stack;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.Event;
import javax.microedition.pim.PIMList;
import javax.microedition.pim.EventList;
import javax.microedition.pim.PIMException;

import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncException;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.client.ChangesTracker;
import com.funambol.syncml.protocol.SyncFilter;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.protocol.SyncMLStatus;

//import com.funambol.common.pim.icalendar.ICalendarFormatter;
import com.funambol.common.pim.icalendar.ICalendarSyntaxParserListener;
import com.funambol.common.pim.icalendar.ICalendarSyntaxParser;
import com.funambol.common.pim.PIMSyncSource;

import com.funambol.util.Log;

/**
 */
public class EventSyncSource extends PIMSyncSource
{
    protected static final int[] SUPPORTED_FIELDS =
    {
        Event.SUMMARY,
        Event.LOCATION,
        Event.START,
        Event.END,
        Event.CLASS,
        Event.NOTE
    };

    public EventSyncSource(SourceConfig config, PIMList list, ChangesTracker tracker)
    {
        super(config, list, tracker);
    }

    //---------------------------------------- Abstract methods implementation
    

    /**
     * Creates a single item in the proper PIMList
     *
     * @param content is the item in the sync source standard format (could be a
     * vCard, SIF-C or any other valid format).
     *
     * @return a PIMItem representing the given item
     *
     * @throws PIMException if the PIMItem cannot be created (for example if the
     * textual representation is invalid, or no new items can be added to the
     * list)
     */
    protected PIMItem createItem(String content) throws PIMException {
        Log.trace("[ContactSyncSource.createItem]");
        EventList el = (EventList) list;
        Event event = el.createEvent();
        // Now populate the event
        fillItem(event, content);
        return event;
    }


    /**
     * Get the list of supported fields. This method is needed by the clearItem
     * method. If a derived class redefines the clearItem, then it does not need
     * to give a meaningful implementation of this method (may return null).
     */
    protected int[] getSupportedFields() {
        return SUPPORTED_FIELDS;
    }

    /**
     * Get the value of the UID field. Each PIM Item has the concept of UID
     * which distinguish each PIMItem. This method returns the UID field id.
     * This method is needed by the clearItem method. If a derived class
     * redefines the clearItem, then it does not need to give a meaningful
     * implementation of this method (may return null).
     */
    protected int getUIDField() {
        return Event.UID;
    }

    /**
     * Delete an item from the store
     *
     * @param item the item to be removed (the key is the only relevant field)
     * @return true iff the item was successfully removed
     * @throws PIMException if the item cannot be removed
     */
    protected boolean deleteItem(PIMItem item) throws PIMException {
        EventList el = (EventList)list;
        el.removeEvent((Event)item);
        return true;
    }

    /**
     * Formats an item according to the format supported by the sync source.
     * The item is formatted as a stream of bytes ready to be exchanged with the
     * DS server.
     *
     * @param item the item (cannot be null)
     * @return an array of byte representing the incoming item
     * @throws PIMException if the item cannot be formatted
     */
    protected byte[] formatItem(PIMItem item) throws PIMException {
        // should we remove this line after cleaning out the code
        EventList cl = (EventList)list;
        Event event = (Event)item;

        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        /*
        VCardFormatter formatter = new VCardFormatter("UTF-8");
        formatter.format(contact, ostream, true);
        String vCard = ostream.toString();
        Log.trace("Formatted item: ");
        Log.trace(vCard);
        try {
            return vCard.getBytes("UTF-8");
        } catch (Exception e) {
            Log.error("Cannot get UTF-8 bytes from string");
            return vCard.getBytes();
        }
        */
        return null;
    }

    /**
     * Fills an item according to a textual representation of the same item. The
     * actual format depends on the sync source. A contact could be for example
     * represented as a vCard and thus parsed to geneate a Contact object.
     *
     * @param pitem is the object to be filled
     * @param content is the item textual representation
     * 
     * @throws PIMException if the item cannot be parsed
     */
    protected void fillItem(PIMItem pitem, String content) throws PIMException {
        Log.trace("[EventSyncSource.fillItem]");
        EventList el = (EventList)list;
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes());

        try {
            Event event = (Event)pitem;
            ICalendarParserListener lis = new ICalendarParserListener(event);
            ICalendarSyntaxParser parser = new ICalendarSyntaxParser(is);
            parser.setListener(lis);
            parser.parse();
        } catch (Exception pe) {
            Log.error("Exception while parsing the item " + pe.toString());
            throw new PIMException(pe.toString());
        }
    }
}
