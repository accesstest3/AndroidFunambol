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

package com.funambol.common.pim.vcard;

import java.io.InputStream;
import java.io.IOException;

public class VCardTestUtils {

    public static String getNextVCardItem(InputStream is) throws IOException {

        // We read as long as we get to the END:VCARD token
        StringBuffer event  = new StringBuffer();
        StringBuffer line   = new StringBuffer();
        boolean begun = false;
        char ahead = (char)0;
        boolean lookAhead = false;

        while(lookAhead || is.available() > 0) {
            char ch;
            if (lookAhead) {
                ch = ahead;
                lookAhead = false;
            } else {
                ch = (char)is.read();
            }
            if (ch == '\r' || ch == '\n') {
                if (is.available() > 0) {
                    if(ch == '\n') {
                        ahead = '\n';  
                    }
                    else {
                        ahead = (char)is.read();
                    }
                    lookAhead = true;
                }
                // Found an EOL
                if (begun) {
                    line.append('\r');
                    if (lookAhead && ahead == '\n') {
                        line.append('\n');
                        lookAhead = false;
                    }
                    event.append(line.toString());

                    if (line.toString().indexOf("END:VCARD") >= 0) {
                        // This is the end of the event
                        begun = false;
                        break;
                    }
                } else {
                    lookAhead = false;
                }
                line = new StringBuffer();
            } else {
                line.append(ch);
                if (line.toString().indexOf("BEGIN:VCARD") >= 0) {
                    begun = true;
                }
            }
        }
        return event.toString();
    }
}

