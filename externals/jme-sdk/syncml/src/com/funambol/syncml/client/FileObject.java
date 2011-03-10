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

package com.funambol.syncml.client;

import java.util.Date;
import java.util.Calendar;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParser;
import org.kxml2.io.KXmlParser;

import com.funambol.util.DateUtil;
import com.funambol.util.StringUtil;
import com.funambol.util.Log;

/**
 * This class represents a FileObject which is a file with its meta information
 * as defined by OMA (see OMA File Data Object Specification for more details).
 * This class actually stores the meta information, while the actual content is
 * not part of it. The reason is that in general the content of a file cannot be
 * kept in memory, so the class could just store a URL or a stream, but at the
 * moment this is not even supported as this need is not forseen.
 * Beside storing the file object meta information, this class has also the
 * ability to parse/format an item (at least its meta info). For this purpose
 * there are two sets of methods: <br>
 * 
 * parsePrologue, parseEpilogue and parse <br>
 * formatPrologue and formatEpilogue
 *
 * Parsing is split between the prologue (everything up to the body content
 * [escluded]) and the epilogue (everything after the body content). If the
 * item is known to be small, then it can be parsed in one shot via the parse
 * method.
 * Formatting is als split between the prologue and the epilogue
 */
public class FileObject {

    private static final String FILE_TAG       = "File";
    private static final String NAME_TAG       = "name";
    private static final String BODY_TAG       = "body";
    private static final String CREATED_TAG    = "created";
    private static final String MODIFIED_TAG   = "modified";
    private static final String ACCESSED_TAG   = "accessed";
    private static final String SIZE_TAG       = "size";
    private static final String ATTRIBUTES_TAG = "attributes";
    private static final String HIDDEN_TAG     = "h";
    private static final String SYSTEM_TAG     = "s";
    private static final String ARCHIVED_TAG   = "a";
    private static final String DELETED_TAG    = "d";
    private static final String WRITABLE_TAG   = "w";
    private static final String READABLE_TAG   = "r";
    private static final String EXECUTABLE_TAG = "x";
    private static final String ENC_ATTR       = "enc";
    private static final String BASE64_ENC     = "base64";

    private static final String TRUE            = "TRUE";
    private static final String FALSE           = "FALSE";

    private String  name       = null;
    private Date    modified   = null;
    private Date    created    = null;
    private Date    accessed   = null;
    private int     size       = -1;
    private boolean hidden     = false;
    private boolean system     = false;
    private boolean archived   = false;
    private boolean deleted    = false;
    private boolean writable   = false;
    private boolean readable   = false;
    private boolean executable = false;
    private boolean bodyBase64 = false;

    private String  fileTagName = FILE_TAG;
    private String  bodyTagName = BODY_TAG;

    public FileObject() {
    }

    /**
     * Sets the file name
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the file name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the modification time
     */
    public Date getModified() {
        return modified;
    }

    /**
     * Sets the modification time
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * Sets the creation time
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Gets the creation time
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Sets the last accessed time
     */
    public void setAccessed(Date accessed) {
        this.accessed = accessed;
    }

    /**
     * Gets the last accessed time
     */
    public Date getAccessed() {
        return accessed;
    }

    /**
     * Sets the hidden attribute
     */
    public void setHidden(boolean h) {
        hidden = h;
    }

    /**
     * Gets the hidden attribute
     */
    public boolean getHidden() {
        return hidden;
    }

    /**
     * Sets the system attribute
     */
    public void setSystem(boolean s) {
        system = s;
    }

    /**
     * Gets the system attribute
     */
    public boolean getSystem() {
        return system;
    }

    /**
     * Sets the archived attribute
     */
    public void setArchived(boolean a) {
        archived = a;
    }

    /**
     * Gets the archived attribute
     */
    public boolean getArchived() {
        return archived;
    }

    /**
     * Sets the deleted attribute
     */
    public void setDeleted(boolean d) {
        deleted = d;
    }

    /**
     * Gets the deleted attribute
     */
    public boolean getDeleted() {
        return deleted;
    }

    /**
     * Sets the writable attribute
     */
    public void setWritable(boolean w) {
        writable = w;
    }

    /**
     * Gets the writable attribute
     */
    public boolean getWritable() {
        return writable;
    }

    /**
     * Sets the readable attribute
     */
    public void setReadable(boolean r) {
        readable = r;
    }

    /**
     * Gets the readable attribute
     */
    public boolean getReadable() {
        return readable;
    }

    /**
     * Sets the executable attribute
     */
    public void setExecutable(boolean e) {
        executable = e;
    }

    /**
     * Gets the executable attribute
     */
    public boolean getExecutable() {
        return executable;
    }

    /**
     * Sets the file size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Gets the file size. This attribute is not mandatory and if the
     * information is not available, -1 is returned.
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns true if the body is encoded in base64 (if not it is assumed to be
     * plain text)
     */
    public boolean isBodyBase64() {
        return bodyBase64;
    }

    /**
     * Formats the prologue of this file object. The prologue is everything from
     * the File tag up to the body content (excluded)
     *
     * @return the formatted prologue
     */
    public String formatPrologue() {
        return formatPrologue(true);
    }


    /**
     * Formats the prologue of this file object. The prologue is everything from
     * the File tag up to the body content (excluded)
     *
     * @param formatBody specifies if the body tag must be formatted
     *
     * @return the formatted prologue
     */
    public String formatPrologue(boolean formatBody) {
        StringBuffer buf = new StringBuffer();

        formatStartTag(buf, FILE_TAG);
        buf.append("\n");
        if (name != null) {
            formatCompleteTag(buf, NAME_TAG, name);
        }
        if (modified != null) {
            String mod = DateUtil.formatDateTimeUTC(modified);
            formatCompleteTag(buf, MODIFIED_TAG, mod);
        }
        if (created != null) {
            String cre = DateUtil.formatDateTimeUTC(created);
            formatCompleteTag(buf, CREATED_TAG, cre);
        }
        if (accessed != null) {
            String acc = DateUtil.formatDateTimeUTC(accessed);
            formatCompleteTag(buf, ACCESSED_TAG, acc);
        }

        // Format all the attributes
        formatStartTag(buf, ATTRIBUTES_TAG);
        buf.append("\n");
        formatCompleteTag(buf, HIDDEN_TAG, (hidden ? TRUE : FALSE));
        formatCompleteTag(buf, SYSTEM_TAG, (system ? TRUE : FALSE));
        formatCompleteTag(buf, ARCHIVED_TAG, (archived ? TRUE : FALSE));
        formatCompleteTag(buf, DELETED_TAG, (deleted ? TRUE : FALSE));
        formatCompleteTag(buf, WRITABLE_TAG, (writable ? TRUE : FALSE));
        formatCompleteTag(buf, READABLE_TAG, (readable ? TRUE : FALSE));
        formatCompleteTag(buf, EXECUTABLE_TAG, (executable ? TRUE : FALSE));
        formatEndTag(buf, ATTRIBUTES_TAG);
        buf.append("\n");

        // Format the size
        if (size != -1) {
            formatCompleteTag(buf, SIZE_TAG, ""+size);
        }

        // Format the body tag if required
        if (formatBody) {
            buf.append("<").append(BODY_TAG).append(" enc=\"base64\">");
        }
        return buf.toString();
    }

    /**
     * Formats the epilogue of this file object. The epilogue is everything
     * after the body content (excluded)
     *
     * @return the formatted epilogue
     */
    public String formatEpilogue() {
        return formatEpilogue(true);
    }

    /**
     * Formats the epilogue of this file object. The epilogue is everything
     * after the body content (excluded)
     *
     * @param formatBody specifies if the body tag must be formatted
     *
     * @return the formatted epilogue
     */
    public String formatEpilogue(boolean formatBody) {
        StringBuffer buf = new StringBuffer();
        if (formatBody) {
            formatEndTag(buf, BODY_TAG);
            buf.append("\n");
        }
        formatEndTag(buf, FILE_TAG);
        return buf.toString();
    }

    /**
     * Parses the prologue of a file object. This method checks the syntax of
     * the item and it stops its analysis once it finds the body tag. During the
     * analysis it sets the memebers of this file object accordingly to the file
     * object properties.
     * If the item contains more than then body (at least the body closure tag)
     * then the method returns null, as the user shall use the parse method to
     * parse the entire item in one shot.
     *
     * @param is the input stream representing the input
     * @return the body content contained in the input stream. In other words
     * the analysis scans all the input stream and returns everything comes
     * after the body tag. The method may return null if the body is not found
     * or the there is no body content.
     * @throws FileObjectException if the parsing fail for any reason
     */
    public String parsePrologue(InputStream is) throws FileObjectException {
        KXmlParser parser = new KXmlParser();

        try {
            parser.setInput(is, "UTF-8");
            // the first token must be the file tag
            parser.next();
            require(parser, parser.START_TAG, null, FILE_TAG);
            fileTagName = parser.getName();
            // parse the rest of the xml
            return parseFile(parser, true);
        } catch (FileObjectException foe) {
            Log.error("Error parsing FileObject: " + foe.toString());
            throw foe;
        } catch (Exception e) {
            Log.error("Error parsing FileObject: " + e.toString());
            throw new FileObjectException("Cannot parse file object: " + e.toString());
        }
    }

    /**
     * Parses the epilogue of a file object. The epilogue is everything after
     * the body content (starting with the body closure tag)
     */
    public void parseEpilogue(String epilogue) throws FileObjectException {
        KXmlParser parser = new KXmlParser();

        // The epilogue does not have the opening tags, and this will make the
        // parser fails, so we add them here
        StringBuffer ep = new StringBuffer();
        ep.append("<").append(fileTagName).append(">").
           append("<").append(bodyTagName).append(">").append(epilogue);
        epilogue = ep.toString();

        ByteArrayInputStream bis = new ByteArrayInputStream(epilogue.getBytes());
        
        try {
            parser.setInput(bis, "UTF-8");
            // the first token must be body tag closure (unless the body was
            // empty)
            // the first token must be the file tag
            parser.next();
            require(parser, parser.START_TAG, null, FILE_TAG);
            // the second token must be the body tag
            nextSkipSpaces(parser);
            require(parser, parser.START_TAG, null, BODY_TAG);
            // Now we expect the body end tag
            nextSkipSpaces(parser);
            require(parser, parser.END_TAG, null, BODY_TAG);
            // Now we can parse all the remaining items
            parseFile(parser, false);
            // the last token must be the file tag
            require(parser, parser.END_TAG, null, FILE_TAG);
        } catch (FileObjectException foe) {
            Log.error("Error parsing FileObject: " + foe.toString());
            throw foe;
        } catch (Exception e) {
            Log.error("Error parsing FileObject: " + e.toString());
            throw new FileObjectException("Cannot parse file object: " + e.toString());
        }
    }

    /**
     * This method parses a file object which is readable entirely from the
     * input stream. This method loads in the memory the entire item's body,
     * so it must be used only when it is known that the item body is small
     * enough.
     *
     * @param is the stream from which data is read
     * @return the item body content
     * @throws FileObjectException if any error occurs during the parsing
     */
    public String parse(InputStream is) throws FileObjectException {
        KXmlParser parser = new KXmlParser();
        String body = null;
        try {
            parser.setInput(is, "UTF-8");
            // the first token must be the file tag
            parser.next();
            require(parser, parser.START_TAG, null, FILE_TAG);
            // parse the rest of the xml
            body = parseFile(parser, false);
            // Now we expect the body end tag
            nextSkipSpaces(parser);
            require(parser, parser.END_TAG, null, BODY_TAG);
            // Now we can parse all the remaining items
            parseFile(parser, false);
            // the last token must be the file tag
            require(parser, parser.END_TAG, null, FILE_TAG);
        } catch (FileObjectException foe) {
            Log.error("Error parsing FileObject: " + foe.toString());
            throw foe;
        } catch (Exception e) {
            Log.error("Error parsing FileObject: " + e.toString());
            throw new FileObjectException("Cannot parse file object: " + e.toString());
        }
        return body;
    }


    private String parseFile(XmlPullParser parser, boolean checkBodyInterrupted)
                                                   throws XmlPullParserException,
                                                          IOException,
                                                          FileObjectException
    {
        // Scan until we find the BODY
        boolean bodyFound = false;
        String bodyText = null;
        nextSkipSpaces(parser);
        while (parser.getEventType() == parser.START_TAG) {
            String tagName = parser.getName();
            if (StringUtil.equalsIgnoreCase(NAME_TAG,tagName)) {
                parseName(parser);
            } else if (StringUtil.equalsIgnoreCase(CREATED_TAG,tagName)) {
                parseCreated(parser);
            } else if (StringUtil.equalsIgnoreCase(MODIFIED_TAG,tagName)) {
                parseModified(parser);
            } else if (StringUtil.equalsIgnoreCase(ACCESSED_TAG,tagName)) {
                parseAccessed(parser);
            } else if (StringUtil.equalsIgnoreCase(SIZE_TAG,tagName)) {
                parseSize(parser);
            } else if (StringUtil.equalsIgnoreCase(ATTRIBUTES_TAG,tagName)) {
                parseAttributes(parser);
            } else if (StringUtil.equalsIgnoreCase(BODY_TAG,tagName)) {
                bodyTagName = parser.getName();
                // Check if the body is base64
                int numAttributes = parser.getAttributeCount();
                for(int i=0;i<numAttributes;++i) {
                    String attribute = parser.getAttributeName(i);
                    if (StringUtil.equalsIgnoreCase(attribute, ENC_ATTR)) {
                        String value = parser.getAttributeValue(i);
                        if (StringUtil.equalsIgnoreCase(value, BASE64_ENC)) {
                            bodyBase64 = true;
                        }
                    }
                }
                // Get the first part of the body
                parser.next();
                bodyText = parser.getText();
                bodyFound = true;
                if (checkBodyInterrupted) {
                    // Check that we are at the end of the document. If we are not,
                    // then return null
                    try {
                        parser.next();
                        if (parser.getEventType() != parser.END_DOCUMENT) {
                            bodyText = null;
                        }
                    } catch (XmlPullParserException xppe) {
                        // This exception is thrown if the closure tag is not
                        // complete. we interpret this as we have part of the
                        // closure tag
                        bodyText = null;
                    }
                }
                break;
            } else {
                Log.error("Unknown token: " + tagName);
                throw new FileObjectException("Unknown token: " + tagName);
            }
            nextSkipSpaces(parser);
        }
        return bodyText;
    }

    private void parseName(XmlPullParser parser) throws XmlPullParserException,
                                                        IOException,
                                                        FileObjectException {
        parser.next();
        require(parser, parser.TEXT, null, null);
        name = parser.getText();
        parser.next();
        require(parser, parser.END_TAG, null, NAME_TAG);
    }

    private void parseCreated(XmlPullParser parser) throws XmlPullParserException,
                                                           IOException,
                                                           FileObjectException {
        parser.next();
        require(parser, parser.TEXT, null, null);
        created = parseDate(parser.getText());
        parser.next();
        require(parser, parser.END_TAG, null, CREATED_TAG);
    }

    private void parseModified(XmlPullParser parser) throws XmlPullParserException,
                                                            IOException,
                                                            FileObjectException {
        parser.next();
        require(parser, parser.TEXT, null, null);
        modified = parseDate(parser.getText());
        parser.next();
        require(parser, parser.END_TAG, null, MODIFIED_TAG);
    }

    private void parseAccessed(XmlPullParser parser) throws XmlPullParserException,
                                                            IOException,
                                                            FileObjectException {
        parser.next();
        require(parser, parser.TEXT, null, null);
        accessed = parseDate(parser.getText());
        parser.next();
        require(parser, parser.END_TAG, null, ACCESSED_TAG);
    }

    private void parseSize(XmlPullParser parser) throws XmlPullParserException,
                                                        IOException,
                                                        FileObjectException {
        parser.next();
        require(parser, parser.TEXT, null, null);
        size = parseInt(parser.getText());
        parser.next();
        require(parser, parser.END_TAG, null, SIZE_TAG);
    }

    private void parseAttributes(XmlPullParser parser) throws XmlPullParserException,
                                                              IOException,
                                                              FileObjectException {
        // If we have text here, we check it is only whitespaces or CR
        nextSkipSpaces(parser);
        while (parser.getEventType() == parser.START_TAG) {

            String tagName = parser.getName();
            if (StringUtil.equalsIgnoreCase(HIDDEN_TAG,tagName)) {
                parseHidden(parser);
            } else if (StringUtil.equalsIgnoreCase(SYSTEM_TAG,tagName)) {
                parseSystem(parser);
            } else if (StringUtil.equalsIgnoreCase(DELETED_TAG,tagName)) {
                parseDeleted(parser);
            } else if (StringUtil.equalsIgnoreCase(ARCHIVED_TAG,tagName)) {
                parseArchived(parser);
            } else if (StringUtil.equalsIgnoreCase(WRITABLE_TAG,tagName)) {
                parseWritable(parser);
            } else if (StringUtil.equalsIgnoreCase(READABLE_TAG,tagName)) {
                parseReadable(parser);
            } else if (StringUtil.equalsIgnoreCase(EXECUTABLE_TAG,tagName)) {
                parseExecutable(parser);
            } else {
                throw new FileObjectException("Unknown attribute in file object " + tagName);
            }
            // If we have text here, we check it is only whitespaces or CR
            nextSkipSpaces(parser);
        }
        require(parser, parser.END_TAG, null, ATTRIBUTES_TAG);
    }

    private void nextSkipSpaces(XmlPullParser parser) throws FileObjectException,
                                                             XmlPullParserException,
                                                             IOException {
        int eventType = parser.next();
        if (eventType == parser.TEXT) {
            if (!parser.isWhitespace()) {
                Log.error("Unexpected text: " + parser.getText());
                throw new FileObjectException("Unexpected text: " + parser.getText());
            }
            parser.next();
        }
    }

    private void parseHidden(XmlPullParser parser) throws XmlPullParserException,
                                                          IOException,
                                                          FileObjectException {

        parser.next();
        require(parser, parser.TEXT, null, null);
        hidden = parseBoolean(parser.getText());
        parser.next();
        require(parser,parser.END_TAG, null, HIDDEN_TAG);
    }

    private void parseSystem(XmlPullParser parser) throws XmlPullParserException,
                                                          IOException,
                                                          FileObjectException {
        parser.next();
        require(parser, parser.TEXT, null, null);
        system = parseBoolean(parser.getText());
        parser.next();
        require(parser,parser.END_TAG, null, SYSTEM_TAG);
    }

    private void parseDeleted(XmlPullParser parser) throws XmlPullParserException,
                                                          IOException,
                                                          FileObjectException {
        parser.next();
        require(parser, parser.TEXT, null, null);
        deleted = parseBoolean(parser.getText());
        parser.next();
        require(parser,parser.END_TAG, null, DELETED_TAG);
    }



    private void parseArchived(XmlPullParser parser) throws XmlPullParserException,
                                                            IOException,
                                                            FileObjectException {
        parser.next();
        require(parser, parser.TEXT, null, null);
        archived = parseBoolean(parser.getText());
        parser.next();
        require(parser,parser.END_TAG, null, ARCHIVED_TAG);
    }

    private void parseWritable(XmlPullParser parser) throws XmlPullParserException,
                                                            IOException,
                                                            FileObjectException {
        parser.next();
        require(parser, parser.TEXT, null, null);
        writable = parseBoolean(parser.getText());
        parser.next();
        require(parser,parser.END_TAG, null, WRITABLE_TAG);
    }

    private void parseReadable(XmlPullParser parser) throws XmlPullParserException,
                                                            IOException,
                                                            FileObjectException {
        parser.next();
        require(parser, parser.TEXT, null, null);
        readable = parseBoolean(parser.getText());
        parser.next();
        require(parser,parser.END_TAG, null, READABLE_TAG);
    }

    private void parseExecutable(XmlPullParser parser) throws XmlPullParserException,
                                                              IOException,
                                                              FileObjectException {
        parser.next();
        require(parser, parser.TEXT, null, null);
        executable = parseBoolean(parser.getText());
        parser.next();
        require(parser,parser.END_TAG, null, EXECUTABLE_TAG);
    }

    private void require(XmlPullParser parser, int type, String namespace,
                         String name) throws XmlPullParserException
    {
        if (type != parser.getEventType()
            || (namespace != null && !StringUtil.equalsIgnoreCase(namespace,parser.getNamespace()))
            || (name != null &&  !StringUtil.equalsIgnoreCase(name,parser.getName())))
        {
            throw new XmlPullParserException("expected "+ parser.TYPES[ type ]+
                                              parser.getPositionDescription());
        }
    }


    private boolean parseBoolean(String value) throws FileObjectException {
        boolean v;
        if (value != null && StringUtil.equalsIgnoreCase(value, "true")) {
            v = true;
        } else if (value != null && StringUtil.equalsIgnoreCase(value, "false")) {
            v = false;
        } else {
            throw new FileObjectException("Expected boolean, found: " + value);
        }
        return v;
    }

    private Date parseDate(String value) throws FileObjectException {
        try {
            Calendar date = DateUtil.parseDateTime(value);
            Date d = date.getTime();
            return d;
        } catch (Exception e) {
            Log.error("Cannot parse date " + value + " " + e.toString());
            throw new FileObjectException("Cannot parse date " + value);
        }
    }

    private int parseInt(String value) throws FileObjectException {
        int v;
        try {
            v = Integer.parseInt(value);
        } catch (Exception e) {
            Log.error("Cannot parse int value: " + value);
            throw new FileObjectException("Cannot parse int " + value);
        }
        return v;
    }

    private void formatCompleteTag(StringBuffer buf, String tagName, String tagValue) {
        buf.append("<").append(tagName).append(">")
           .append(tagValue)
           .append("</").append(tagName).append(">").append("\n");
    }

    private void formatStartTag(StringBuffer buf, String tagName) {
        buf.append("<").append(tagName).append(">");
    }

    private void formatEndTag(StringBuffer buf, String tagName) {
        buf.append("</").append(tagName).append(">");
    }


}

