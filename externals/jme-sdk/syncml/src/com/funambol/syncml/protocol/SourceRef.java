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

package com.funambol.syncml.protocol;

/**
 * This class represents the &lt;SourceRef&gt; element as defined by the SyncML
 * representation specifications
 */
public class SourceRef {

    // ------------------------------------------------------------ Private data
    private String value;
    private Source source;

    // ------------------------------------------------------------ Constructors

    /**
     * In order to expose the server configuration like WS this constructor
     * must be public
     */
    public SourceRef() {}

    /**
     * Creates a new SourceRef object given the referenced value. A null value
     * is considered an empty string
     *
     * @param value the referenced value - NULL ALLOWED
     *
     * @throws IllegalArgumentException if value is null
     */
    public SourceRef(final String value) {
        setValue(value);
    }

    /**
     * Creates a new SourceRef object from an existing Source.
     *
     * @param source the source to extract the reference from - NOT NULL
     *
     * @throws IllegalArgumentException if source is null
     *
     */
    public SourceRef(final Source source) {
        setSource(source);

        value = source.getLocURI();
    }

    // ---------------------------------------------------------- Public methods

    /**
     * Returns the value
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the reference value. If value is null, the empty string is adopted.
     *
     * @param value the reference value - NULL
     */
    public void setValue(String value) {
        this.value = (value == null) ? "" : value;
    }

    /**
     * Gets the Source property
     *
     * @return source the Source object property
     */
    public Source getSource() {
        return this.source;
    }

    /**
     * Sets the Source property
     *
     * @param source the Source object property - NOT NULL
     */
    public void setSource(Source source) {
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null");
        }
        this.source = source;
    }
}
