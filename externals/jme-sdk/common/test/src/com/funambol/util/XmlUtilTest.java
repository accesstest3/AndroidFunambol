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

import junit.framework.*;


/**
 * A JMUnit test class to test the methods of {@link MailDateFormatter} <p>
 *
 * sysout is used instead of Log.debug() to let messages appear in the output on
 * Ant too
 */
public class XmlUtilTest extends TestCase {

    private final String TAG_NAME = "tagName";
    private final String[] ATTRIBUTES = {"attr1=\"attr1\"","attr2=\"attr2\""};
    private final String CONTENT = "Content";
    
    
    // ---------------------------------------------------------------- Methods
    
    /**
     * Test of addElementWithTagAttr method, of class
     * com.funambol.util.MailDateFormatter
     */
    public void testAddElementWithTagAttr() throws AssertionFailedError {
        
        String result = "<"+TAG_NAME+" "+ ATTRIBUTES[0]+" "+ATTRIBUTES[1]+">"+CONTENT+"</"+TAG_NAME+">";
        StringBuffer buffer = new StringBuffer();

        XmlUtil.addElementWithTagAttr(buffer, TAG_NAME, CONTENT, ATTRIBUTES);
        assertEquals(result,buffer.toString());
        
    }
    
     /**
     * Test of addElementWithTagAttr method, of class
     * com.funambol.util.MailDateFormatter
     */
    public void testAddNewLinetWithTagAttr() throws AssertionFailedError {
        
        String result = "<"+TAG_NAME+" "+ ATTRIBUTES[0]+" "+ATTRIBUTES[1]+">"+CONTENT+"</"+TAG_NAME+">\n";
        StringBuffer buffer = new StringBuffer();

        XmlUtil.addElementNewLineWithTagAttr(buffer, TAG_NAME, CONTENT,false, ATTRIBUTES);
        assertEquals(result,buffer.toString());
        
    }

    public void testGetTagValue1() throws Throwable {
        String testTag = "<Test>This is the escaped content &lt;</Test>";
        ChunkedString t = new ChunkedString(testTag);
        ChunkedString content = XmlUtil.getTagValue(t, "Test");
        assertEquals(content.toString(), "This is the escaped content <");
    }

    public void testGetTagValue2() throws Throwable {
        String testTag = "<Test><![CDATA[This is the CDATA content &lt;]]></Test>";
        ChunkedString t = new ChunkedString(testTag);
        ChunkedString content = XmlUtil.getTagValue(t, "Test");
        assertEquals(content.toString(), "This is the CDATA content &lt;");
    }

    public void testGetTagValue3() throws Throwable {
        // Note that the CDATA is incorrect
        String testTag = "<Test><![CDATA[This is the CDATA content &lt;]</Test>";
        ChunkedString t = new ChunkedString(testTag);
        boolean pass = false;
        try {
            ChunkedString content = XmlUtil.getTagValue(t, "Test");
        } catch (XmlException xe) {
            pass = true;
        }
        assertTrue(pass);
    }

    public XmlUtilTest(String name) {
        super(name);
    }
    
    /**
     * Prepares the test environment. <p>
     *
     *
     * @see jmunit.framework.cldc10.TestCase#setUp()
     */
    public void setUp() {
        
    }
    
    
    public void tearDown() {
    }
}
