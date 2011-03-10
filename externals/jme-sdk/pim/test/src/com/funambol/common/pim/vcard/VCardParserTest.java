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

package com.funambol.common.pim.vcard;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import javax.microedition.pim.PIM;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.Contact;
import javax.microedition.pim.PIMItem;

import com.funambol.util.Log;
import com.funambol.util.ConsoleAppender;

import junit.framework.*;

public class VCardParserTest extends TestCase {

    
    public VCardParserTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender());
        Log.setLogLevel(Log.DEBUG);
    }
    
    public void setUp() {
    }
    
    public void tearDown() {
    }

    public void testVCard1() throws Throwable {

        String email[] = {"email@ha.com"};
        String displayName = "Hall, Ava";
        String title = "Secretary";
        String web = "www.ava.com";
        String workTel  = "123456";
        String faxTel   = "234567";
        String pagerTel = "345678";
        String cellTel  = "456789";
        String homeTel  = "567890";
        String otherTel = "678901";
        String org      = "Data Services";
        String note     = "Carla's friend";

        String name[] = new String[10];
        name[Contact.NAME_FAMILY] = "Hall";
        name[Contact.NAME_GIVEN] = "Ava";
        name[Contact.NAME_PREFIX] = "Miss";

        String homeAddress[] = new String[20];
        homeAddress[Contact.ADDR_STREET] = "45, Michigan Street";
        homeAddress[Contact.ADDR_REGION] = "Georgia";
        homeAddress[Contact.ADDR_POSTALCODE] = "23456";
        homeAddress[Contact.ADDR_COUNTRY] = "USA";
        homeAddress[Contact.ADDR_LOCALITY] = "Atlanta";

        String workAddress[] = new String[20];
        workAddress[Contact.ADDR_STREET] = "1, Main Street";
        workAddress[Contact.ADDR_REGION] = "Georgia";
        workAddress[Contact.ADDR_POSTALCODE] = "23456";
        workAddress[Contact.ADDR_COUNTRY] = "USA";
        workAddress[Contact.ADDR_LOCALITY] = "Atlanta";

        String vcard = createVCard(name, displayName, title, email, web,
                                   workTel, faxTel, pagerTel, cellTel, homeTel, otherTel,
                                   org, homeAddress, workAddress, note, "");

        Log.debug(vcard);

        ContactList list;
        list = (ContactList) PIM.getInstance().openPIMList(PIM.CONTACT_LIST,PIM.READ_WRITE);
        Contact contact = list.createContact();
        ByteArrayInputStream is = new ByteArrayInputStream(vcard.getBytes());

        ContactParserListener lis = new ContactParserListener(contact, false);
        VCardSyntaxParser parser = new VCardSyntaxParser(is);
        parser.setListener(lis);
        parser.parse();

        // Now check the Contact fields
        String[] cName = contact.getStringArray(Contact.NAME, 0);
        assertTrue(name[Contact.NAME_FAMILY].equals(cName[Contact.NAME_FAMILY]));
        assertTrue(name[Contact.NAME_GIVEN].equals(cName[Contact.NAME_GIVEN]));
        assertTrue(name[Contact.NAME_PREFIX].equals(cName[Contact.NAME_PREFIX]));

        String[] cWorkAddress = null;
        String[] cHomeAddress = null;

        for (int i = 0; i < contact.countValues(Contact.ADDR); i++) {
            String [] address = contact.getStringArray(Contact.ADDR, i);
            int addrType = contact.getAttributes(Contact.ADDR, i);
            if (addrType == Contact.ATTR_HOME) {
                cHomeAddress = address;
            } else {
                cWorkAddress = address;
            }
        }

        assertTrue(cHomeAddress != null);
        assertTrue(homeAddress[Contact.ADDR_STREET].equals(cHomeAddress[Contact.ADDR_STREET]));
        assertTrue(homeAddress[Contact.ADDR_REGION].equals(cHomeAddress[Contact.ADDR_REGION]));
        assertTrue(homeAddress[Contact.ADDR_POSTALCODE].equals(cHomeAddress[Contact.ADDR_POSTALCODE]));
        assertTrue(homeAddress[Contact.ADDR_COUNTRY].equals(cHomeAddress[Contact.ADDR_COUNTRY]));
        assertTrue(homeAddress[Contact.ADDR_LOCALITY].equals(cHomeAddress[Contact.ADDR_LOCALITY]));

        assertTrue(cWorkAddress != null);
        assertTrue(workAddress[Contact.ADDR_STREET].equals(cWorkAddress[Contact.ADDR_STREET]));
        assertTrue(workAddress[Contact.ADDR_REGION].equals(cWorkAddress[Contact.ADDR_REGION]));
        assertTrue(workAddress[Contact.ADDR_POSTALCODE].equals(cWorkAddress[Contact.ADDR_POSTALCODE]));
        assertTrue(workAddress[Contact.ADDR_COUNTRY].equals(cWorkAddress[Contact.ADDR_COUNTRY]));
        assertTrue(workAddress[Contact.ADDR_LOCALITY].equals(cWorkAddress[Contact.ADDR_LOCALITY]));

        assertTrue(title.equals(contact.getString(Contact.TITLE, 0)));
        assertTrue(web.equals(contact.getString(Contact.URL, 0)));
        assertTrue(org.equals(contact.getString(Contact.ORG, 0)));
        assertTrue(note.equals(contact.getString(Contact.NOTE, 0)));

        // Check Phone numbers
        int phonesChecked = 0;
        for (int i = 0; i < contact.countValues(Contact.TEL); i++)
        {
            final String value = contact.getString(Contact.TEL, i);
            final int type = contact.getAttributes(Contact.TEL, i);
            switch (type) {
                case Contact.ATTR_HOME:
                    assertTrue(homeTel.equals(value));
                    ++phonesChecked;
                    break;
                case Contact.ATTR_WORK:
                    assertTrue(workTel.equals(value));
                    ++phonesChecked;
                    break;
                case Contact.ATTR_PAGER:
                    assertTrue(pagerTel.equals(value));
                    ++phonesChecked;
                    break;
                case Contact.ATTR_FAX:
                    assertTrue(faxTel.equals(value));
                    ++phonesChecked;
                    break;
                case Contact.ATTR_MOBILE:
                    assertTrue(cellTel.equals(value));
                    ++phonesChecked;
                    break;
                case Contact.ATTR_OTHER:
                    assertTrue(otherTel.equals(value));
                    ++phonesChecked;
                    break;
            }
        }
        assertTrue(phonesChecked == 6);
        //System.getProperty(FileConnection.dir.photos);
    }

    private String createVCard(String name[], String displayName, String title,
                               String email[], String url, String workTel, String faxTel,
                               String pagerTel, String cellTel, String homeTel,
                               String otherTel, String org, String homeAddress[],
                               String workAddress[], String note, String photo) {

        StringBuffer vcard = new StringBuffer();

        vcard.append("BEGIN:VCARD\r\n");
        vcard.append("VERSION:2.1\r\n");

        vcard.append("N:").append(name[Contact.NAME_FAMILY]).append(";");
        vcard.append(name[Contact.NAME_GIVEN]).append(";");
        vcard.append(";");
        vcard.append(name[Contact.NAME_PREFIX]).append(";");
        vcard.append(";\r\n");

        vcard.append("FN:").append(displayName).append("\r\n");

        vcard.append("TEL;VOICE;WORK:").append(workTel).append("\r\n");
        vcard.append("TEL;FAX;WORK:").append(faxTel).append("\r\n");
        vcard.append("TEL;PAGER:").append(pagerTel).append("\r\n");
        vcard.append("TEL;CELL:").append(cellTel).append("\r\n");
        vcard.append("TEL;VOICE;HOME:").append(homeTel).append("\r\n");
        vcard.append("TEL;VOICE:").append(otherTel).append("\r\n");

        vcard.append("ADR;HOME:").append(";");
        vcard.append(";");
        vcard.append(homeAddress[Contact.ADDR_STREET]).append(";");
        vcard.append(homeAddress[Contact.ADDR_LOCALITY]).append(";");
        vcard.append(homeAddress[Contact.ADDR_REGION]).append(";");
        vcard.append(homeAddress[Contact.ADDR_POSTALCODE]).append(";");
        vcard.append(homeAddress[Contact.ADDR_COUNTRY]).append("\r\n");

        vcard.append("ADR;WORK:").append(";");
        vcard.append(";");
        vcard.append(workAddress[Contact.ADDR_STREET]).append(";");
        vcard.append(workAddress[Contact.ADDR_LOCALITY]).append(";");
        vcard.append(workAddress[Contact.ADDR_REGION]).append(";");
        vcard.append(workAddress[Contact.ADDR_POSTALCODE]).append(";");
        vcard.append(workAddress[Contact.ADDR_COUNTRY]).append("\r\n");

        vcard.append("ORG:").append(org).append("\r\n");
        vcard.append("TITLE:").append(title).append("\r\n");
        vcard.append("NOTE:").append(note).append("\r\n");
        vcard.append("URL:").append(url).append("\r\n");

        vcard.append("END:VCARD\r\n");

        return vcard.toString();
    }

    public void testParserBlindly() throws Throwable {

        InputStream vcardsStream = getClass().getResourceAsStream("/res/vcard/vcards.txt");
        String vcard;
        int total = 0;

        do {
            vcard = getNextVCard(vcardsStream);
            if (vcard.length() > 0) {
                ++total;

                try {

                    ContactList list = (ContactList) PIM.getInstance().openPIMList(PIM.CONTACT_LIST,PIM.READ_WRITE);
                    Contact contact = list.createContact();
                    ByteArrayInputStream is = new ByteArrayInputStream(vcard.getBytes());

                    ContactParserListener lis = new ContactParserListener(contact, false);
                    VCardSyntaxParser parser = new VCardSyntaxParser(is);
                    parser.setListener(lis);
                    parser.parse();
                } catch (Exception e) {
                    Log.error("Error while parsing this vCard");
                    Log.error(vcard);
                    assertTrue(false);
                }
            }
        } while (vcard.length() > 0);
        Log.debug("Number of vCards parsed: " + total);
    }

    private String getNextVCard(InputStream is) throws IOException {
        // We read as long as we get to the END:VCARD token
        StringBuffer vcard  = new StringBuffer();
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
                    vcard.append(line.toString());

                    if (line.toString().indexOf("END:VCARD") >= 0) {
                        // This is the end of the vCard
                        begun = false;
                        break;
                    }
                } else {
                    lookAhead = false;
                }
                line = new StringBuffer();
            } else {
                begun = true;
                line.append(ch);
            }
        }
        return vcard.toString();
    }
}

