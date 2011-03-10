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

import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.IOException;

import com.funambol.util.QuotedPrintable;
import com.funambol.util.Log;
import com.funambol.util.StringUtil;

/**
 * This class contains utilities for the PIM module. In particular it provides
 * methods to fold, unfold, escape, unescape, encode e decode. All these tasks
 * are required by most PIM formats (vCard, vCal and so on).
 */
public class Utils {

    private final int MAX_FOLDING_LINE_LENGHT = 75;
    public static final char FOLDING_INDENT_CHAR = ' ';
    
    private String defaultCharset = null;

    public Utils(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    /**
     * Folds a string (i.e. folds a text field in order to not exceed 75 chars
     * for each string line, by inserting a CLRF line break and a tabulation as a
     * prefix for each line)
     */
    public String fold(String str) {
        StringBuffer result = new StringBuffer();
        for(int i=0;i<str.length();i+=MAX_FOLDING_LINE_LENGHT) {
            if (i + MAX_FOLDING_LINE_LENGHT < str.length()) {
                result.append(str.substring(i, i + MAX_FOLDING_LINE_LENGHT));
                result.append("\r\n" + FOLDING_INDENT_CHAR);
            } else {
                result.append(str.substring(i));
            }
        }
        return result.toString();
    }

    /**
     * Unfolds a string (i.e. removes all the CRLF characters)
     */
    public String unfold (String str) {
        int ind = str.indexOf("\r\n");
        if (ind == -1) {
            return unfoldNewline(str);
        }
        else {
            String tmpString1 = str.substring(0,ind);
            String tmpString2 = str.substring(ind+2);
            return unfoldNewline(unfold(tmpString1+tmpString2));
        }
    }

    /**
     * Unfolds a string (i.e. removes all the line break characters).
     * This function is meant to ensure compatibility with vCard documents
     * that adhere loosely to the specification
     */
    public String unfoldNewline (String str) {
        int ind = str.indexOf("\n");
        if (ind == -1) {
            return str;
        }
        else {
            String tmpString1 = str.substring(0,ind);
            String tmpString2 = str.substring(ind+1);
            return unfoldNewline(tmpString1+tmpString2);
        }
    }

    /**
     * Decode the given text according to the given encoding and charset
     *
     * @param text the text to decode
     * @param encoding the encoding
     * @param propertyCharset the charset
     *
     * @return the text decoded
     */
    public String decode(String text, String encoding, String propertyCharset)
    {
        if (text == null) {
            return null;
        }

        //
        // If input charset is null then set it with default charset
        //
        if (propertyCharset == null) {
            propertyCharset = defaultCharset; // we use the default charset
        }
        if (encoding != null) {
            if ("QUOTED-PRINTABLE".equals(encoding)) {
                try {
                    byte textBytes[] = text.getBytes(propertyCharset);
                    int len = QuotedPrintable.decode(textBytes);
                    String res = new String(textBytes, 0, len, propertyCharset);
                    return res;
                } catch (UnsupportedEncodingException ue) {
                    Log.error("Cannot decode quoted printable: " + text);
                    // In this case we keep this value
                    return text;
                }
            }
        } else {
            try {
                return new String(text.getBytes(propertyCharset), propertyCharset);
            } catch (UnsupportedEncodingException ue) {
                // In this case we keep this value
                return text;
            }
        }
        return text;
    }

    /**
     * Removes the last equals from the end of the given String
     */
    private String removeLastEquals(String data) {
        if (data == null) {
            return data;
        }
        data = data.trim();
        while (data.endsWith("=")) {
            data = data.substring(0, data.length() - 1);
        }
        return data;
    }

    /**
     * Unescape '\' ',' ';' '\n' '\N' chars
     *
     * @param text the text to unescape
     * @return String the unescaped text
     */
    public String unescape(String text) {

        if (text == null) {
            return text;
        }
        StringBuffer value = new StringBuffer();
        int length = text.length();
        boolean foundSlash = false;
        for (int i=0; i<length; i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '\\':
                    if (foundSlash) {
                        foundSlash = false;
                        value.append('\\');
                    } else {
                        foundSlash = true;
                    }
                    break;
                case ';':
                    value.append(';');
                    foundSlash = false;
                    break;
                case ',':
                    value.append(',');
                    foundSlash = false;
                    break;
                case 'n':
                    if(foundSlash) {
                        value.append('\n');
                    } else {
                        value.append('n');
                    }
                    foundSlash = false;
                    break;
                case 'N':
                    if(foundSlash) {
                        value.append('\n');
                    } else {
                        value.append('N');
                    }
                    foundSlash = false;
                    break;
                default:
                    if (foundSlash) {
                        foundSlash = false;
                        value.append('\\');
                    }
                    value.append(ch);
                    break;
            }
        }
        return value.toString();
    }

    /**
     * Escape special chars: '\' ';' ',' '\n'
     * @param msg message to escape,
     * @param escapeComma boolean to escape or not the comma character. In some cases
     *                    for Vcard is not necessary escape commas.
     * @return the escaped message
     */
    public String escape(String msg, boolean escapeComma) {
        return escape(msg, escapeComma, true);
    }

    /**
     * Escape special chars: '\' ';' ',' '\n'
     * @param msg message to escape,
     * @param escapeComma boolean to escape or not the comma character. In some cases
     *                    for Vcard is not necessary escape commas.
     * @param escapeLF boolean to escape LF or not
     * @return the escaped message
     */
    public String escape(String msg, boolean escapeComma, boolean escapeLF) {
        if (msg == null) {
            return null;
        }
        StringBuffer res = new StringBuffer();
        for(int i=0;i<msg.length();++i) {
            char ch = msg.charAt(i);
            if (ch == '\\') {
                res.append("\\\\");
            } else if (ch == ';') {
                res.append("\\;");
            } else if (ch == ',' && escapeComma) {
                res.append("\\,");
            } else if (ch == '\n' && escapeLF) {
                res.append("\\n");
            } else {
                res.append(ch);
            }
        }
        try {
            String enc = new String(res.toString().getBytes(defaultCharset), defaultCharset);
            return enc;
        } catch (UnsupportedEncodingException e) {
            Log.error("[Utils.escape] Cannot convert string " + e.toString());
        }
        return res.toString();
    }
}


