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

package com.funambol.util;

/**
 * Log Container class. Holds the properties of the log appender which belongs 
 * to and the references the log content. This class can be used by all log 
 * appenders.
 */ 
public class LogContent {
   
    public static final int FILE_CONTENT   = 0;
    public static final int STRING_CONTENT = 1;

    private int contentType;
    private String content = null;
    
    /**
     * Build a log container specifying the appender information
     */         
    public LogContent(int contentType, String content) {
        this.contentType = contentType;
        this.content = content;
    }

    /**
     * @return the content type. Possible values are:
     *  <ul>
     *    <li> FILE_CONTENT if the content is in a file </li>
     *    <li> STRING_CONTENT if the content is inlined </li>
     *  </ul>
     */             
    public int getContentType() {
        return contentType;
    }

    /**
     * @return the log content as a formatted string. Depending on the content
     * type the  value returned may represent a filename or the inlined log
     * content. This method can return null if the content type is NULL_CONTENT
     */
    public String getContent() {
        return content;
    }
}
