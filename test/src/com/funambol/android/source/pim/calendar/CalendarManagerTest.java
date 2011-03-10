/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2010 Funambol, Inc.
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

package com.funambol.android.source.pim.calendar;

import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.Vector;

import com.funambol.android.AppInitializer;
import com.funambol.android.AndroidBaseTest;

import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.common.pim.Utils;
import com.funambol.util.Log;
import com.funambol.util.StringUtil;

public class CalendarManagerTest extends AndroidBaseTest {

    private static final String TAG_LOG = "CalendarManagerTest";
    
    private CalendarManager cm;

    private AppSyncSource appSyncSource;

    public void setUp() throws Exception {

        super.setUp();

        AppInitializer appInitializer = AppInitializer.getInstance(getContext());
        appInitializer.init();

        try {
            Thread.sleep(5000);
        } catch (Exception e) {}

        AppSyncSourceManager appSyncSourceManager = appInitializer.getAppSyncSourceManager();
        appSyncSource = appSyncSourceManager.getSource(AppSyncSourceManager.EVENTS_ID);

        CalendarAppSyncSourceConfig appSyncSourceConfig = null;
        // If the appSyncSource is null, then the source is disabled in the
        // customization, but we need one to complete the test
        if (appSyncSource == null) {
            appSyncSource = new AppSyncSource("Calendar");
        } else {
            appSyncSourceConfig = (CalendarAppSyncSourceConfig)appSyncSource.getConfig();
        }

        if (appSyncSourceConfig == null) {
            appSyncSourceConfig = new CalendarAppSyncSourceConfig(appSyncSource,
                    appInitializer.getCustomization(),
                    appInitializer.getConfiguration());
            appSyncSource.setConfig(appSyncSourceConfig);
        }

        cm = new CalendarManager(getContext(), appSyncSource);
        CalendarManager.CalendarDescriptor calDesc = cm.getDefaultCalendar();
        appSyncSourceConfig.setCalendarId(calDesc.getId());

        try { cm.deleteAll(); } catch(Exception ex) {}
    }

    public void tearDown() {

    }
    
    public void testAddLoad() throws Throwable {
        Log.info("testAddLoad start");

        Calendar c = new Calendar();

        byte[] vcal = getSampleVCalendar();
        c.setVCalendar(vcal);

        // Add the new contact
        long id = cm.add(c);

        // Load the same contact
        Calendar result = cm.load(id);

        assertEquals(result, c);

        Log.info("testAddLoad end");
    }

    public void testUpdate() throws Throwable {
        Log.info("testUpdate start");

        Calendar c = new Calendar();

        byte[] vcal = getSampleVCalendar();
        byte[] vcal_upd = getUpdatedVCalendar();

        c.setVCalendar(vcal);

        // Add the new contact
        long id = cm.add(c);

        c.setVCalendar(vcal_upd);

        cm.update(id, c);

        // Load the same contact
        Calendar result = cm.load(id);

        assertEquals(result, c);

        Log.info("testUpdate end");
    }

    public void testDelete() throws Exception {
        Log.info("testDelete start");

        Calendar c = new Calendar();

        byte[] vcal = getSampleVCalendar();
        c.setVCalendar(vcal);

        long[] ids = new long[5];

        // Add some calendars
        for(int i=0; i<ids.length; i++) {
            ids[i] = cm.add(c);
        }

        // Check if they exist
        for(int i=0; i<ids.length; i++) {
            assertTrue(cm.exists(ids[i]));
        }

        // Delete all
        for(int i=0; i<ids.length; i++) {
            cm.delete(ids[i]);
        }

        // Check if they exist
        for(int i=0; i<ids.length; i++) {
            assertTrue(!cm.exists(ids[i]));
        }

        Log.info("testDelete end");
    }

    public void testGetAllKeys() throws Exception {
        Log.info("testGetAllKeys start");

        Calendar c = new Calendar();

        byte[] vcal = getSampleVCalendar();
        c.setVCalendar(vcal);

        long[] ids = new long[5];
        // Add some contacts
        for(int i=0; i<ids.length; i++) {
            ids[i] = cm.add(c);
        }

        // Check if they exist
        for(int i=0; i<ids.length; i++) {
            assertTrue(cm.exists(ids[i]));
        }

        boolean[] found = new boolean[ids.length];
        for(int i=0; i<found.length; i++) {
            found[i] = false;
        }
        Enumeration en = cm.getAllKeys();
        int count = 0;
        while(en.hasMoreElements()) {
            count++;
            String key = (String)en.nextElement();
            for(int i=0; i<ids.length; i++) {
                if(Long.parseLong(key) == ids[i]) {
                    found[i] = true;
                    break;
                }
            }
        }
        assertEquals(count, ids.length);
        for(int i=0; i<found.length; i++) {
            assertTrue(found[i]);
        }

        Log.info("testGetAllKeys end");
    }

    public void testExists() throws Exception {
        Log.info("testExists start");

        Calendar c = new Calendar();

        byte[] vcal = getSampleVCalendar();
        c.setVCalendar(vcal);

        // Add the new contact
        long id = cm.add(c);
        assertTrue(cm.exists(id));

        cm.delete(id);
        assertTrue(!cm.exists(id));

        Log.info("testExists end");
    }


    private void assertEquals(Calendar result, Calendar expected) throws Throwable {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        result.toVCalendar(os, true);
        byte[] result_ba = os.toByteArray();

        os = new ByteArrayOutputStream();
        expected.toVCalendar(os, true);
        byte[] expected_ba = os.toByteArray();

        assertVCalendarEquals(result_ba, expected_ba);
    }

    private void assertVCalendarEquals(byte[] result, byte[] expected) throws Throwable {

        String resultStr = orderVCalendar(new String(result));
        String expectedStr = orderVCalendar(new String(expected));

        assertEquals(resultStr, expectedStr);
    }

    /**
     * Order the vCalendar item fields alphabetically.
     */
    private String orderVCalendar(String vcard) {

        Vector fields_al = getFieldsVector(vcard);

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

    private Vector getFieldsVector(String vcard) {
        String sep[] = {"\r\n"};
        String lines[] = StringUtil.split(new String(vcard), sep);

        Vector fields_al = new Vector();
        String field = "";
        for(int i=0;i<lines.length;++i) {
            String line = lines[i];
            if(line.length() > 0 && line.charAt(0) == Utils.FOLDING_INDENT_CHAR) {
                // this is a multi line field
                field += line.substring(1); // cut the indent char
            } else {
                if(!field.equals("")) {
                    fields_al.add(field);
                }
                field = line;
            }
        }
        // add the latest field
        fields_al.add(field);

        return fields_al;
    }

    private byte[] getSampleVCalendar() {
        return ("BEGIN:VCALENDAR\r\n" +
                "VERSION:1.0\r\n" +
                "TZ:+0100\r\n" +
                "DAYLIGHT:TRUE;+02;20090329T020000;20091025T030000;;\r\n" +
                "DAYLIGHT:TRUE;+02;20100328T020000;20101031T030000;;\r\n" +
                "DAYLIGHT:TRUE;+02;20110327T020000;20111030T030000;;\r\n" +
                "DAYLIGHT:TRUE;+02;20120325T020000;20121028T030000;;\r\n" +
                "BEGIN:VEVENT\r\n" +
                "SUMMARY:summary\r\n" +
                "DESCRIPTION:description\r\n" +
                "LOCATION:location\r\n" +
                "DTSTART:20091229T200000\r\n" +
                "DTEND:20091229T220000\r\n" +
                "END:VEVENT\r\n" +
                "END:VCALENDAR\r\n").getBytes();
    }

    private byte[] getUpdatedVCalendar() {
        return ("BEGIN:VCALENDAR\r\n" +
                "VERSION:1.0\r\n" +
                "TZ:+0100\r\n" +
                "DAYLIGHT:TRUE;+02;20090329T020000;20091025T030000;;\r\n" +
                "DAYLIGHT:TRUE;+02;20100328T020000;20101031T030000;;\r\n" +
                "DAYLIGHT:TRUE;+02;20110327T020000;20111030T030000;;\r\n" +
                "DAYLIGHT:TRUE;+02;20120325T020000;20121028T030000;;\r\n" +
                "BEGIN:VEVENT\r\n" +
                "SUMMARY:Updated summary\r\n" +
                "DESCRIPTION:Updated description\r\n" +
                "LOCATION:Updated location\r\n" +
                "DTSTART:20091229T200000\r\n" +
                "DTEND:20091229T210000\r\n" +
                "END:VEVENT\r\n" +
                "END:VCALENDAR\r\n").getBytes();
    }

}
