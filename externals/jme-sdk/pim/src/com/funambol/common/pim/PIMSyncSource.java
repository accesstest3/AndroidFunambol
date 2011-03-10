/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2008 Funambol, Inc.
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

package com.funambol.common.pim;

import java.util.Stack;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMList;
import javax.microedition.pim.PIMException;

import com.funambol.syncml.spds.SourceConfig;
import com.funambol.syncml.spds.SyncException;
import com.funambol.syncml.spds.SyncItem;
import com.funambol.syncml.spds.SyncSource;
import com.funambol.syncml.client.TrackableSyncSource;
import com.funambol.syncml.client.ChangesTracker;
import com.funambol.syncml.protocol.SyncFilter;
import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.protocol.SyncMLStatus;

import com.funambol.util.Log;

/**
 * Basic sync source for PIM sync management.
 * This class is the base class for all classes manipulating JSR75 PIM data
 * (such as Contact, Calendar and so on).
 * The class is a TrackableSyncSource, so that the tracking mechanism can be
 * easily customized by clients (default is CacheTracking, based on items
 * finger prints).
 */
public abstract class PIMSyncSource extends TrackableSyncSource
{
    protected int totalAdded,totalUpdated,totalDeleted;
    protected int totalNewSent,totalUpdatedSent,totalDeletedSent;
    
    /**
     * Status of the sync source summarized in an integer value. See constants
     * defined in SyncSource
     */
    protected int globalStatus;

    protected PIMList        list;
    
    public PIMSyncSource(SourceConfig config, PIMList list, ChangesTracker tracker)
    {
        super(config, tracker);
        this.list = list;
    }

    //----------------------------------------------------------- Public Methods
    //------------------------------------------------ SyncSource implementation
    public void beginSync(int syncMode) throws SyncException {
        super.beginSync(syncMode);
    }
    
    /**
     * Called just before committing the synchronization process by the
     * SyncManager. The SyncSource can stop the commit phase raising an
     * exception here.
     * @throws SyncException in case of error, to stop the commit.
     */
    public void endSync() throws SyncException
    {
        Log.info("End sync for source " + getName());
        int totalSent = totalNewSent + totalDeletedSent + totalUpdatedSent;
        Log.info(
                "For source ["+getName()+"]: \n"+
                "there were ["+totalSent+"] total items sent \n" +
                
                "there were ["+totalNewSent    +"] NEW items sent \n" +
                "there were ["+totalDeletedSent+"] DELETED items sent \n" +
                "there were ["+totalUpdatedSent+"] UPDATED items sent \n" +
                
                "there were ["+totalAdded  +"] total items tried to add \n" +
                "there were ["+totalDeleted+"] total items tried to delete \n" +
                "there were ["+totalUpdated+"] total items tried to update \n"
        );
        tracker.end();
    }
     
    public void dataReceived(String date, int size)
    {
        //Do nothing here
    }

    
    /*
     * (non-Javadoc)
     * @see com.funambol.syncml.spds.SyncSource#addItem(com.funambol.syncml.spds.SyncItem)
     */
    public int addItem(SyncItem item) throws SyncException
    {
        Log.trace("[PIMSyncSource.addItem] " + item.getKey());
        totalAdded++;
        try
        {
            PIMItem pitem = createItem(new String(item.getContent()));
            if (pitem != null)
            {
                try {
                    Log.info("Commiting a new item into the store: " + item.getKey());
                    pitem.commit();
                } catch (PIMException e) {
                    Log.error("Unable to save new item in source [" + getName() + "]: " + e.toString());
                    return SyncMLStatus.GENERIC_ERROR;
                }
                Log.trace("Before trying to get UID of PIMItem.");
                String key = getUID(pitem); 
                item.setKey(key);
                
                Log.trace("Purposfully deleting new item key: " + key);
                tracker.removeItem(item);
                key = null;
                return SyncMLStatus.SUCCESS;
            } else {
                Log.error("Can not create a blank PIMItem.");
            }
        } catch(final PIMException e) {
            Log.error("PIMException while adding item to SyncSource [" + getName() + "]: " + e.toString());
            return SyncMLStatus.GENERIC_ERROR;
        } catch(final Exception e) {
            Log.error("RuntimeException while updating item to SyncSource ["
                      + getName() + "]: " + e.toString());
            return SyncMLStatus.GENERIC_ERROR;
        }
        Log.error("Unable to save new item in source [" + getName() + "]: no error");
        return SyncMLStatus.GENERIC_ERROR;
    }
    
    /*
     * (non-Javadoc)
     * @see com.funambol.syncml.spds.SyncSource#updateItem(com.funambol.syncml.spds.SyncItem)
     */
    public int updateItem(SyncItem item) throws SyncException
    {
        Log.trace("[PIMSyncSource.updateItem] " + item.getKey());
        totalUpdated++;
        try
        {
            PIMItem pitem = findItem(item.getKey());
            if (pitem!=null)
            {
                // This log is left because of the infamous bug in OS 4.5
                // where removing fields may generate an unexpected exception
                Log.trace("Before clearing properties of found PIMItem.");
                clearItem(pitem);
                Log.trace("Before calling setPIMProperties functions.");

                fillItem(pitem,new String(item.getContent()));
                Log.trace("Before commiting PIMItem.");
                try {
                    pitem.commit();
                } catch (PIMException e) {
                    Log.error("Unable to save updated item in source [" +
                              getName() + "]: " + e.toString());
                    return SyncMLStatus.GENERIC_ERROR;
                }
                Log.trace("Before trying to get UID of PIMItem.");
                String key = getUID(pitem); 
                item.setKey(key);

                Log.trace("Purposfully deleting updated item key: " + key);
                tracker.removeItem(item);
                
                return SyncMLStatus.SUCCESS;
            }
            Log.error("Unable to save updated item in source [" + getName() + "]: item not found");
            return SyncMLStatus.NOT_FOUND;
        } catch(final PIMException e) {
            Log.error("PIMException while updating item to SyncSource [" +
                      getName() + "]: " + e.toString());
            return SyncMLStatus.GENERIC_ERROR;
        } catch (Exception e) {
            // We had some sort of generic runtime exception
            Log.error("RuntimeException while updating item to SyncSource ["
                      + getName() + "]: " + e.toString());
            return SyncMLStatus.GENERIC_ERROR;
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.funambol.syncml.spds.SyncSource#deleteItem(java.lang.String)
     */
    public int deleteItem(String key) throws SyncException
    {
        Log.trace("[PIMSyncSource.deleteItem] " + key);
        totalDeleted++;
        try
        {
            PIMItem item = findItem(key);
            if (item == null) {
                Log.error("Unable to delete item in source [" + getName() + "]: no error");
                return SyncMLStatus.NOT_FOUND;
            }
            deleteItem(item);
            Log.trace("Purposfully deleting deleted item key: " + item);
            SyncItem tmpItem = new SyncItem(key);
            tmpItem.setState(SyncItem.STATE_DELETED);
            tracker.removeItem(tmpItem);
            return SyncMLStatus.SUCCESS;
        }
        catch(final Exception e)
        {
            Log.error("Exception while deleting item from SyncSource ["+getName()+"]");
            return SyncMLStatus.GENERIC_ERROR;
        }
    }

    public void deleteAllItems() throws SyncException {
        try {
            Enumeration items = list.items();
            while(items.hasMoreElements()) {
                PIMItem item = (PIMItem)items.nextElement();
                deleteItem(item);
            }
        } catch (Exception e) {
            throw new SyncException(SyncException.CLIENT_ERROR, "Cannot delete item " + e.toString());
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.funambol.syncml.spds.SyncSource#getItemContent(com.funambol.syncml.spds.SyncItem)
     */
    protected SyncItem getItemContent(SyncItem item) throws SyncException
    {
        Log.trace("[PIMSyncSource.getItemContent] " + item.getKey());
        try
        {
            PIMItem pitem = findItem(item.getKey());
            if (pitem!=null)
            {
                item.setContent(formatItem(pitem));
                return item;
            }
        }
        catch(final PIMException e)
        {
            throw new SyncException(SyncException.CLIENT_ERROR,
                                    "Exception while retrieving content of item from SyncSource ["
                                    +getName()+"]");
        }
        return null;
    }
    
    /*
     * (non-Javadoc)
     * @see com.funambol.syncml.spds.SyncSource#initAllItems()
     */
    protected Enumeration getAllItemsKeys() throws SyncException
    {
        Log.trace("[PIMSyncSource.getAllItemsKeys]");
        try
        {
            String key = null;
            Vector itemsVector = new Vector();
            Enumeration items = list.items();
            while(items.hasMoreElements())
            {
                key = getUID((PIMItem)items.nextElement());
                itemsVector.addElement(key);
            }
            items = null;
            key = null;
            return itemsVector.elements();
        }
        catch(final PIMException e)
        {
            throw new SyncException(SyncException.CLIENT_ERROR,
                                    "Exception while initializing all items of SyncSource ["
                                    +getName()+"]");
        }
    }
    
    protected void clearItem(PIMItem item) throws PIMException
    {
        Log.trace("[PIMSyncSource.clearItem]");
        int fieldIDs[] = getSupportedFields();
        int UID        = getUIDField();
        int oldvalues[] = item.getFields();
        for(int i=0;i<oldvalues.length;i++)
        {
            if (UID != oldvalues[i])
            {
                for (int t=0;t<fieldIDs.length;t++)
                {
                    if (oldvalues[i] == fieldIDs[t])
                    {
                        int count = item.countValues(oldvalues[i]);
                        Log.debug("Field ID ["+oldvalues[i]+"] has ["+count+"] values");
                        for (int j=count-1;0<=j;j--)
                        {
                            // This operation on OS 4.5 may throw an
                            // IndexOutOfBoundsException for apparently no
                            // good reason. On OS 4.5 we replace items by
                            // deleting them and recreating them. If this
                            // happens on other versions of the OS, then we need
                            // to apply this strategy on other versions as well
                            Log.trace("Removing value ["+j+"] of Field ID ["+oldvalues[i]+"]");
                            item.removeValue(oldvalues[i],j);
                            Log.trace("Value removed");
                        }
                        count = item.countValues(oldvalues[i]);
                        Log.debug("After deletion Field ID ["+oldvalues[i]+"] has ["+count+"] values");
                        break;
                    }
                }
            }
        }
        String oldCategories[] = item.getCategories();
        for(int i=0;i<oldCategories.length;i++)
        {
            item.removeFromCategory(oldCategories[i]);
            Log.debug("Remove old category ["+oldCategories[i]+"]");
        }
        oldvalues = null;
    }

    /**
     * Finds an item in the source. The item is searched by its key.
     *
     * @return the item PIM representation or null if not found
     *
     * @throws PIMException on error, for example if the list of items cannot be
     * accessed
     */
    protected PIMItem findItem(String key) throws PIMException {
        PIMItem item = null;
        Enumeration items = list.items();
        while(items.hasMoreElements()) {
            item = (PIMItem)items.nextElement();
            if (key.equals(this.getUID(item))) {
                return item;
            }
        }
        return null;
    }

    /**
     * By default UID and Luid are the same. This method can be overloaded by
     * subclasses that want different values for luids and uids
     */
    protected String getLuidFromUID(String uid) {
        return uid;
    }

    protected String getUID(PIMItem item) throws PIMException {
        Log.trace("[PIMSyncSource.getUID] ");
        int uidField = getUIDField();

        try
        {
            return item.getString(uidField,0);
        }
        catch(final Exception e)
        {
            final String msg = "Exception while retrieving UID for PIMItem of SyncSource [" + getName() + "]";
            Log.error(msg);
            throw new PIMException(msg);
        }
    }

    ////////////////// Abstract methods that derived classes need to implement
    ////////////////// All these methods provide basic and simple functions

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
    protected abstract PIMItem createItem(String content) throws PIMException;

    /**
     * Get the list of supported fields. This method is needed by the clearItem
     * method. If a derived class redefines the clearItem, then it does not need
     * to give a meaningful implementation of this method (may return null).
     */
    protected abstract int[] getSupportedFields();

    /**
     * Get the value of the UID field. Each PIM Item has the concept of UID
     * which distinguish each PIMItem. This method returns the UID field id.
     * This method is needed by the clearItem method. If a derived class
     * redefines the clearItem, then it does not need to give a meaningful
     * implementation of this method (may return null).
     */
    protected abstract int getUIDField();

    /**
     * Delete an item from the store
     *
     * @param item the item to be removed (the key is the only relevant field)
     * @return true iff the item was successfully removed
     * @throws PIMException if the item cannot be removed
     */
    protected abstract boolean deleteItem(PIMItem item) throws PIMException;

    /**
     * Formats an item according to the format supported by the sync source.
     * The item is formatted as a stream of bytes ready to be exchanged with the
     * DS server.
     *
     * @param item the item (cannot be null)
     * @return an array of byte representing the incoming item
     * @throws PIMException if the item cannot be formatted
     */
    protected abstract byte[] formatItem(PIMItem item) throws PIMException;

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
    protected abstract void fillItem(PIMItem pitem, String content) throws PIMException;
}
