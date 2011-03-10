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
package com.funambol.util;

import java.util.Vector;
import java.util.Hashtable;

/**
 * Utility class that XML manipulation functions.
 */
public class XmlUtil {

    // This class cannot be instantiated
    private XmlUtil() {

    }

    /**
     * <p>Escapes the characters in a <code>String</code> using XML entities.</p>
     *
     *
     * <p>Supports only the four basic XML entities (gt, lt, quot, amp).
     * Does not support DTDs or external entities.</p>
     *
     * @param str  the <code>String</code> to escape, may be null
     * @return a new escaped <code>String</code>, <code>null</code> if null string input
     */
    public static String escapeXml(String str) {
        if (str == null) {
            return null;
        }
        return Entities.XML.escape(str);
    }

    /**
     * <p>Unescapes a string containing XML entity escapes to a string
     * containing the actual Unicode characters corresponding to the
     * escapes.</p>
     *
     * <p>Supports only the four basic XML entities (gt, lt, quot, amp).
     * Does not support DTDs or external entities.</p>
     *
     * @param str  the <code>String</code> to unescape, may be null
     * @return a new unescaped <code>String</code>, <code>null</code> if null string input
     */
    public static String unescapeXml(String str) {
        if (str == null) {
            return null;
        }
        return Entities.XML.unescape(str);
    }

    /**
     * Return the index of <i>tag</i>, validating also the presence
     * of the end tag.
     *
     * @param xml xml msg
     * @param tag tag to find 
     * @return tag index or -1 if the tag is not found.
     */
    public static int getTag(ChunkedString xml, String tag) {

        String startTag = null;
        String endTag = null;
        int ret = -1;

        startTag = "<" + tag + ">";
        endTag = "</" + tag + ">";

        // Try <tag>...</tag>
        ret = xml.indexOf(startTag);
        if ((ret != -1) && xml.indexOf(endTag, ret) != -1) {
            return ret;         // Tag without attributes found
        }
        // Try <tag attr="xxx">...</tag>
        startTag = "<" + tag + " ";
        ret = xml.indexOf(startTag);
        if ((ret != -1) && xml.indexOf(endTag, ret) != -1) {
            return ret;         // Tag with attributes found
        }
        // Try <tag/>        
        if ((ret = xml.indexOf("<" + tag + "/>")) != -1) {
            return ret;         // Empty tag found
        }
        // tag not found
        return -1;
    }

    public static Hashtable getTagAttributes(ChunkedString xml, String tag) {
        String startTag = null;
        String endTag = null;
        int tagPos = 0, endPos = 0;

        Hashtable ret = new Hashtable();

        // Try <tag attr="xxx">...</tag>
        startTag = "<" + tag + " ";
        tagPos = xml.indexOf(startTag);
        endPos = xml.indexOf(">", tagPos);

        if ((tagPos != -1) && (endPos != -1)) {
            int space = xml.indexOf(" ");
            if (space != -1) {
                ChunkedString[] attrlist = xml.substring(space, endPos).split(",");

                for (int i = 0,  l = attrlist.length; i < l; i++) {
                    // TODO: find only the first '=', not with split but with indexOf.
                    ChunkedString[] attr = attrlist[i].split("=");
                    if (attr.length > 1) {
                        String val = StringUtil.trim(attr[1].toString().trim(), '\"');
                        ret.put(attr[0].toString().trim(), val);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Make a String by value of <i>tag</i>. The returned content is also
     * unescaped unless it is enclosed in a CDATA section. In this case the
     * CDATA prefix/suffix is removed.
     *
     * @param xml xml msg
     * @param tag tag to find + sourceType + 
     * @return tag value
     */
    public static ChunkedString getTagValue(ChunkedString xml, String tag)
            throws XmlException {

        String startTag = "<" + tag + ">";
        String endTag = "</" + tag + ">";

        //Log.info(xml.toString());

        try {
            // Find start tag
            int stidx = xml.indexOf(startTag);
            if (stidx == -1) {
                // Try with namespace or attributes
                startTag = "<" + tag + " ";
                stidx = xml.indexOf(startTag);
                if (stidx == -1) {
                    throw new XmlException("getTagValue: can't find tag: " + tag);
                } else {
                    // Find closing '>' for tag with attr or namespace
                    stidx = xml.indexOf(">", stidx);
                    if (stidx == -1) {
                        throw new XmlException("getTagValue: unclosed tag: " + tag);
                    }
                    stidx++;    // Skip the '>'
                }
            } else {
                // Point to the end of the tag
                stidx += startTag.length();
            }
            // Find end tag
            int endidx = xml.indexOf(endTag, stidx);
            if (endidx == -1) {
                throw new XmlException("getTagValue: can't find tag end: " + tag);
            }

            // Get the tag content
            ChunkedString content = xml.substring(stidx, endidx);

            // If the tag is enclosed in a CDATA section, then we don't unescape
            if (content.startsWith("<![CDATA[")) {
                if (content.endsWith("]]>")) {
                    content = content.substring(9, content.length() - 3);
                } else {
                    // The CDATA is not properly formatted, throw an exception
                    throw new XmlException("Malformed CDATA section " + content);
                }
            } else {
                // In this case we must unescape the content
                // Note that we unescape also encoded values (such as b64)
                // but this has no functional side effects 
                String c = content.toString();
                content = new ChunkedString(unescapeXml(c));
            }
            return content;
        } catch (StringIndexOutOfBoundsException e) {
            // should not happen anymore.
            Log.error("StringIndexOutofBound in getTagValue");
            throw new XmlException("Error parsing xml, tag: " + tag);
        }

    }

    /**
     * Return a Vector of String with tags matching the search tag.
     *
     * @param xmlInput Vector of XML tags to search in
     * @param tag to find
     * @return found tags (empty if no one found)
     */
    public static Vector getTagValues(Vector xmlInput, String tag)
            throws XmlException {
        Vector xmlReturn = new Vector();

        String plainTag = "<" + tag + ">";
        String attrTag = "<" + tag + " ";
        String endTag = "</" + tag + ">";
        int endIdx = 0;

        for (int j = 0,  l = xmlInput.size(); j < l; j++) {

            ChunkedString xmlInputTag = (ChunkedString) xmlInput.elementAt(j);

            //
            // tag without namespace
            // or tag with namespace
            //
            while (xmlInputTag.indexOf(plainTag) != -1 ||
                    xmlInputTag.indexOf(attrTag) != -1) {

                xmlReturn.addElement(getTagValue(xmlInputTag, tag));

                endIdx = xmlInputTag.indexOf(endTag) + endTag.length();
                if (endIdx == -1) {
                    Log.error("getTagValues: can't find '" + endTag + "'");
                    throw new XmlException("getTagValues: parse exception.");
                }
                xmlInputTag = xmlInputTag.substring(endIdx);

            }

        }

        return xmlReturn;
    }

    /**
     * Return a Vector of String with tags matching the search tag.
     *
     * @param xmlInput XML document to search in
     * @param tag to find
     * @return find tags
     */
    public static Vector getTagValues(ChunkedString xmlInput, String tag)
            throws XmlException {
        Vector tmp = new Vector(1);
        tmp.addElement(xmlInput);

        return getTagValues(tmp, tag);
    }

    /**
     * Add an empty tag to the StringBuffer out.
     *
     * @param out the buffer to append to
     * @param tag tag to be appended
     */
    public static void addElement(StringBuffer out, String tag) {
        out.append("<").append(tag).append("/>");
    }

    /**
     * Add a tag to the StringBuffer out.
     *
     * @param out the buffer to append to
     * @param tag tag to be appended
     */
    public static void addElement(StringBuffer out, String tag, String content) {
        out.append("<").append(tag).append(">").
                append(content).
                append("</").append(tag).append(">");
    }

    /**
     * This function builds a simple Tag with newline char after the close tag.
     * @param out StringBuffer to store output
     * @param tag The Tag will be generated
     * @param content The content data of the Tag
     * @param escape Flag to tell if data need to be escaped
     */
    public static void addElementNewLine(StringBuffer out, String tag, String content, boolean escape) {
        if (content == null) {
            content = "";
            escape = false;
        }
        if (escape) {
            content = XmlUtil.escapeXml(content);
        }
        out.append("<").append(tag).append(">").
                append(content).
                append("</").append(tag).append(">\n");
    }
    
    /**
     * Add a tag with attributes to the StringBuffer out.
     *
     * @param out the buffer to append to
     * @param tag tag to be appended
     * @param content The content data of the Tag
     * @param attr The attributes array
     * 
     */
    public static void addElementWithTagAttr(StringBuffer out, String tag, String content, String[] attr) {
        out.append("<").append(tag);
        for(int i = 0; i<attr.length; i++){
            out.append(" ").
            append(attr[i]);
        }
        out.append(">").
        append(content).
        append("</").append(tag).append(">");
    }

    /**
     * This function builds a simple Tag with newline char after the close tag.
     * @param out StringBuffer to store output
     * @param tag The Tag will be generated
     * @param content The content data of the Tag
     * @param escape Flag to tell if data need to be escaped
     * @param attr The attributes array
     * 
     */
    public static void addElementNewLineWithTagAttr(StringBuffer out, String tag, String content, boolean escape, String[] attr) {
        if (content == null) {
            content = "";
            escape = false;
        }
        if (escape) {
            content = XmlUtil.escapeXml(content);
        }
        out.append("<").append(tag);
        for(int i = 0; i<attr.length; i++){
            out.append(" ").
            append(attr[i]);
        }
        out.append(">").
        append(content).
        append("</").append(tag).append(">\n");
    }


    public static String createOpenTag(String tagname) {
        return "<" + tagname + ">";
    }

    public static String createOpenTagNewLine(String tagname) {
        return "<" + tagname + ">\n";
    }

    public static String createCloseTag(String tagname) {
        return "</" + tagname + ">";
    }

    public static String createCloseTagNewLine(String tagname) {
        return "</" + tagname + ">\n";
    }
}

