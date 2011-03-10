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

package com.funambol.client.ui;

import com.funambol.client.source.AppSyncSource;

/**
 * This interface is a model for a generic representation of a SyncSource in the
 * UI. A sync source is visualized with a title, an icon, a status message and a
 * status icon that may change if the item is enabled/disabled.
 * Each concrete implementation can decide to represent the above properties in
 * some different way. For example a button representation can use labels and
 * icons, while a progress bar representation can use a gauge with messages.
 * The representation has also the concept of being selectable, meaning the user
 * chose this source as the current one. In such a case the representation can
 * decide to represent itself in a particular way, typically by showing the
 * selected status icon and using the colors for the selected/deselected mode.
 */
public interface UISyncSource {

    /**
     * Set the title
     */
    public void setTitle(String value);

    /**
     * Set the title color when the item is enabled
     */
    public void setTitleEnabledColor(int color);

    /**
     * Set the title color when the item is disabled
     */
    public void setTitleDisabledColor(int color);

    /**
     * Enable or disabled this item
     */
    public void setEnabled(boolean flag);

    /**
     * Check if the item is enabled/disabled
     */
    public boolean isDisabled();

    /**
     * Set the item icon
     */
    public void setIcon(Bitmap image);

    /**
     * Set the status icon to be used when the item is not selected
     */
    public void setStatusIcon(Bitmap image);

    /**
     * Set the status string
     */
    public void setStatusString(String value);

    /**
     * Set the selected status
     * @param selected is the selection status
     * @param fromUi specifies if the selection was manually performed from the
     * UI
     */
    public void setSelection(boolean selected, boolean fromUi);

    /**
     * Return the selected status
     */
    public boolean isSelected();

    /**
     * Returns the AppSyncSource this item represents
     */
    public AppSyncSource getSource();

    /**
     * Set the AppSyncSource this item represents
     */
    public void setSource(AppSyncSource source);

    /**
     * Forces the current item to be re-drawn
     */
    public void redraw();

    /**
     * Set the container this item belongs to
     */
    public void setContainer(UISyncSourceContainer container);

    /**
     * Set the item font used for the title and the status
     */
    public void setFont(Font font);

    /**
     * This method is invoked when a sync for this source is started
     */
    public void syncStarted();

    /**
     * This method is invoked when a sync for this source is completed
     */
    public void syncEnded();
}

