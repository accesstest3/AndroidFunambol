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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.funambol.common.pim.ArrayList;
import com.funambol.util.StringUtil;

import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMList;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.Contact;

import com.funambol.util.Log;
import com.funambol.util.ConsoleAppender;
import junit.framework.*;

/**
 * This is a ent-to-end test for the vCard parsing/formatting process.
 */
public class VCardEndToEndTest extends TestCase {

    public VCardEndToEndTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.ERROR);
    }
    
    public void setUp() {
        Log.setLogLevel(Log.ERROR);
    }
    
    public void tearDown() {
    }
    
    /**
     * Iterate all the vCard test cases
     */
    public void disabledtestEndToEnd() throws Throwable {

        Log.debug("--- endToEndTest ---");
        
        InputStream cardsStream = getClass().getResourceAsStream(
                "/res/vcard/goodvcards.txt");
        
        ContactList cList = (ContactList) PIM.getInstance().openPIMList(PIM.CONTACT_LIST,PIM.READ_WRITE);

        String vcard;
        do {
            vcard = VCardTestUtils.getNextVCardItem(cardsStream);
            if (vcard.length() > 0) {
                Contact item = cList.createContact();
                singleVCardTest(vcard, item);
            }
        } while (vcard.length() > 0);
    }

    /**
     * Test flow for each test case:
     * 1 - parse vCard item -> PIMItem
     * 2 - format PIMItem -> vCard item
     * 3 - order vCard items fields of the items coming from points 1 and 2
     * 4 - compare the vCard strings
     */
    public void singleVCardTest(String originalVcard, Contact pimItem) throws Throwable {

        // parsing vCard
        ByteArrayInputStream is = new ByteArrayInputStream(originalVcard.getBytes());
        VCardSyntaxParserListener lis = new ContactParserListener(pimItem, false);
        VCardSyntaxParser parser = new VCardSyntaxParser(is);
        parser.setListener(lis);
        parser.parse();

        // formatting vCard
        VCardFormatter formatter = new VCardFormatter();
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        formatter.format(pimItem, ostream, false);
        String formattedVcard = ostream.toString();

        // ordering
        formattedVcard = orderVCard(formattedVcard);
        originalVcard  = orderVCard(originalVcard);

        // comparing
        if(!formattedVcard.equals(originalVcard)) {
            Log.error("[VCalendarEndToEndTest.singleVCardTest] formattedVcard: ");
            Log.error(formattedVcard);
            Log.error("[VCalendarEndToEndTest.singleVCardTest] originalVcard: ");
            Log.error(originalVcard);
        }
        assertTrue(formattedVcard.equals(originalVcard));
    }

    /**
     * Order the iCalendar item fields alphabetically.
     */
    private String orderVCard(String vcard) {
        
        ArrayList fields_al = new ArrayList();
        String line = "";
        String field = "";
        while(vcard.length() > 0) {
            int lbIndex = vcard.indexOf('\n');
            if(lbIndex>=0) {
                String value = vcard.substring(0, lbIndex);
                if (value != null && value.length() > 0) {
                    line = StringUtil.trim(value, '\r');
                } else {
                    line = "";
                }
                vcard = vcard.substring(lbIndex+1);
            } else {
                line = StringUtil.trim(vcard, '\n');
                line = StringUtil.trim(line, '\r');
                vcard = "";
            }
            if (line.length() > 0) {
                if(line.charAt(0) == com.funambol.common.pim.Utils.FOLDING_INDENT_CHAR) {
                    // this is a multi line field
                    field += line.substring(1); // cut the indent char
                }
                else {
                    // We also discard emtpy fields as we are not formatting
                    // all fields
                    boolean skip = false;
                    field = field.trim();
                    if(!field.equals("") && !field.endsWith(":")) {
                        // We shall skip all the unsupported fields/attributes
                        String upField = field.toUpperCase();
                        if (upField.startsWith("ORG")) {
                            // The unit is not supported
                            int pos = upField.indexOf(";");
                            if (pos >= 0) {
                                field = field.substring(0, pos);
                            }
                        } else if (upField.startsWith("TEL") &&
                                   upField.indexOf("FAX") > 0) {

                            // The home work attr is not supported here
                            int pos = upField.indexOf("HOME");
                            if (pos > 0) {
                                field = field.substring(0, pos) + "WORK" + field.substring(pos + 4);
                            }
                        } else if (upField.startsWith("TEL") &&
                                   upField.indexOf("PREF") > 0) {
                            // We ignore the pref because it is not supported
                            int pos = upField.indexOf(";PREF");
                            if (pos > 0) {
                                field = field.substring(0, pos) + field.substring(pos+5);
                            }
                        } else if (upField.startsWith("ADR")) {
                            int col = upField.indexOf(":");
                            if (col != -1) {
                                int end = upField.indexOf(";", col);
                                if (end != col + 1) {
                                    field = field.substring(0, col + 1) + field.substring(end);
                                }
                            }
                        }

                        if (upField.startsWith("TEL") &&
                            upField.indexOf("CELL") > 0 && upField.indexOf("VOICE") > 0) {

                            int pos = upField.indexOf(";VOICE");
                            if (pos > 0) {
                                field = field.substring(0, pos) + field.substring(pos+6);
                            }
                        }

                        if (upField.startsWith("EMAIL") && upField.indexOf("INTERNET") > 0) {

                            int pos = upField.indexOf(";INTERNET");
                            if (pos > 0) {
                                field = field.substring(0, pos) + field.substring(pos+9);
                            }
                        }

                        if (upField.startsWith("PHOTO")) {
                            int pos = upField.indexOf(";TYPE=JPEG");
                            if (pos > 0) {
                                field = field.substring(0, pos) + field.substring(pos+10);
                            }
                        }

                        if (upField.startsWith("TEL") && upField.indexOf("CELL") > 0
                            && upField.indexOf("HOME") > 0) {

                            skip = true;
                        }

                        if (upField.startsWith("TEL") && upField.indexOf("CELL") > 0
                            && upField.indexOf("WORK") > 0) {

                            skip = true;
                        }

                        if (upField.startsWith("TEL") && upField.indexOf("VIDEO") > 0) {
                            skip = true;
                        }

                        if (upField.startsWith("X-")) {
                            skip = true;
                        }

                        if (upField.startsWith("LABEL")) {
                            skip = true;
                        }

                        if (upField.startsWith("FN")) {
                            skip = true;
                        }

                        if (upField.startsWith("BDAY")) {
                            skip = true;
                        }

                        // If a field ends with a bunch of ';', we just ignore
                        // them
                        do {
                            if (field.endsWith(";")) {
                                field = field.substring(0, field.length() - 1);
                            }
                        } while(field.endsWith(";"));
                        // The field may be empty now, after removing ;

                        if (field.endsWith(":")) {
                            skip = true;
                        }


                        if (!skip) {
                            fields_al.add(field);
                        }
                    }
                    field = line;
                }
            }
        }
        // add the latest field
        fields_al.add(field);

        // order the fields array list
        String result = "";
        String[] fields = StringUtil.getStringArray(fields_al);
        for(int i=0; i<fields.length; i++) {
            for(int j=fields.length-1; j>i; j--) {
                if(fields[j].compareTo(fields[j-1])<0) {
                    String temp = fields[j];
                    fields[j] = fields[j-1];
                    fields[j-1] = temp;
                }
            }
            result += fields[i] + "\r\n";
        }
        return result;
    }
}

