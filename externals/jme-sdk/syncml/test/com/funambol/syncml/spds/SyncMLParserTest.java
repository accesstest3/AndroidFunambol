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

package com.funambol.syncml.spds;

import java.util.Vector;
import java.util.Hashtable;

import com.funambol.util.Log;
import com.funambol.util.ConsoleAppender;
import com.funambol.syncml.protocol.DevInf;
import com.funambol.syncml.protocol.DataStore;
import com.funambol.syncml.protocol.SourceRef;
import com.funambol.syncml.protocol.VerDTD;
import com.funambol.syncml.protocol.CTInfo;
import com.funambol.syncml.protocol.SyncCap;
import com.funambol.syncml.protocol.SyncType;
import com.funambol.syncml.protocol.CTCap;
import com.funambol.syncml.protocol.Property;
import com.funambol.syncml.protocol.DSMem;

import junit.framework.*;

public class SyncMLParserTest extends TestCase {

    public SyncMLParserTest(String name) {
        super(name);
        Log.initLog(new ConsoleAppender(), Log.TRACE);
    }

    public void testParseResults1() throws Exception {

        StringBuffer results = new StringBuffer();
        String verDTD = "1.2";
        String man = "Funambol";
        String mod = "DS Server CarEd";
        String oem = "OEM";
        String fwv = "0.1";
        String swv = "8.0-SNAPSHOT";
        String hwv = "0.2";
        String devId = "funambol";
        String devTyp = "server";
        String sourceRef = "source";
        String displayName = "My Source";
        long   maxGuidSize = 32;
        String rxPref = "application/*";
        String txPref = "application/*";
        String rx0    = "image/*";
        String customExt = "X-funambol-smartslow";
        String propName  = "CTType";
        String valEnum0 = "image/*";
        long   maxMem   = 65539;
        long   maxId    = 512;

        results.append("<CmdID>5</CmdID><MsgRef>1</MsgRef>\n")
               .append("<CmdRef>3</CmdRef>\n")
               .append("<Meta>\n")
               .append("<Type xmlns='syncml:metinf'>application/vnd.syncml-devinf+xml</Type>\n")
               .append("</Meta>\n")
               .append("<Item>\n")
               .append("<Source>\n")
               .append("<LocURI>./devinf12</LocURI>\n")
               .append("</Source>\n")
               .append("<Data>\n")
               .append("<DevInf xmlns=\"syncml:devinf\">\n")
               .append("<VerDTD>").append(verDTD).append("</VerDTD>\n")
               .append("<Man>").append(man).append("</Man>\n")
               .append("<Mod>").append(mod).append("</Mod>")
               .append("<OEM>").append(oem).append("</OEM>\n")
               .append("<FwV>").append(fwv).append("</FwV>")
               .append("<SwV>").append(swv).append("</SwV>")
               .append("<HwV>").append(hwv).append("</HwV>\n")
               .append("<DevID>").append(devId).append("</DevID>\n")
               .append("<DevTyp>").append(devTyp).append("</DevTyp>\n")
               .append("<UTC/><SupportLargeObjs/>\n")
               .append("<SupportNumberOfChanges/>\n")
               .append("<DataStore>\n")
               .append("<SourceRef>").append(sourceRef).append("</SourceRef>\n")
               .append("<DisplayName>").append(displayName).append("</DisplayName>\n")
               .append("<MaxGUIDSize>").append(maxGuidSize).append("</MaxGUIDSize>\n")
               .append("<Rx-Pref>\n")
               .append("<CTType>").append(rxPref).append("</CTType>\n")
               .append("<VerCT>1.0</VerCT>\n")
               .append("</Rx-Pref>\n")
               .append("<Rx>\n")
               .append("<CTType>").append(rx0).append("</CTType>\n")
               .append("<VerCT>1.0</VerCT>\n")
               .append("</Rx>\n")
               .append("<Tx-Pref>\n")
               .append("<CTType>").append(txPref).append("</CTType>\n")
               .append("<VerCT>1.0</VerCT>\n")
               .append("</Tx-Pref>\n")
               .append("<SyncCap>\n")
               .append("<SyncType>1</SyncType>\n")
               .append("<SyncType>2</SyncType>\n")
               .append("<SyncType>3</SyncType>\n")
               .append("<SyncType>4</SyncType>\n")
               .append("<SyncType>5</SyncType>\n")
               .append("<SyncType>6</SyncType>\n")
               .append("<SyncType>7</SyncType>\n")
               .append("</SyncCap>\n")
               .append("<SupportHierarchicalSync/>\n")
               .append("<CTCap>\n")
               .append("<CTType>").append(rxPref).append("</CTType>\n")
               .append("<VerCT>2.1</VerCT>\n")
               .append("<Property>\n")
               .append("<PropName>").append(propName).append("</PropName>\n")
               .append("<ValEnum>").append(valEnum0).append("</ValEnum>\n")
               .append("</Property>\n")
               .append("</CTCap>\n")
               .append("<DSMem>\n")
               .append("<SharedMem/>\n")
               .append("<MaxMem>").append(maxMem).append("</MaxMem>\n")
               .append("<MaxID>").append(maxId).append("</MaxID>\n")
               .append("</DSMem>\n")
               .append("</DataStore>\n")
               .append("<Ext>\n")
               .append("<XNam>").append(customExt).append("</XNam>\n")
               .append("</Ext>\n")
               .append("</DevInf>\n")
               .append("</Data>\n")
               .append("</Item>\n");

        SyncMLParser parser = new SyncMLParser();
        DevInf devInf = parser.parseResults(results.toString());
        // Now check that the DevInf has been properly filled

        VerDTD devInfVerDTD = devInf.getVerDTD();
        assertTrue(verDTD.equals(devInfVerDTD.getValue()));
        assertTrue(man.equals(devInf.getMan()));
        assertTrue(mod.equals(devInf.getMod()));
        assertTrue(oem.equals(devInf.getOEM()));
        assertTrue(fwv.equals(devInf.getFwV()));
        assertTrue(swv.equals(devInf.getSwV()));
        assertTrue(hwv.equals(devInf.getHwV()));
        assertTrue(devId.equals(devInf.getDevID()));
        assertTrue(devTyp.equals(devInf.getDevTyp()));
        assertTrue(devInf.isUTC());
        assertTrue(devInf.isSupportLargeObjs());
        assertTrue(devInf.isSupportNumberOfChanges());
        
        Vector dataStores = devInf.getDataStores();
        assertTrue(dataStores != null && dataStores.size() == 1);
        DataStore ds = (DataStore)dataStores.elementAt(0);
        SourceRef sr = ds.getSourceRef();
        assertTrue(sourceRef.equals(sr.getValue()));
        assertTrue(displayName.equals(ds.getDisplayName()));
        assertTrue(maxGuidSize == ds.getMaxGUIDSize());
        CTInfo rxPrefInfo = ds.getRxPref();
        assertTrue(rxPref.equals(rxPrefInfo.getCTType()));
        CTInfo txPrefInfo = ds.getTxPref();
        assertTrue(txPref.equals(txPrefInfo.getCTType()));
        Vector rxs = ds.getRxs();
        assertTrue(rxs != null && rxs.size() == 1);
        CTInfo rx = (CTInfo) rxs.elementAt(0);
        assertTrue(rx0.equals(rx.getCTType()));
        assertTrue(devInf.isSupportHierarchicalSync());
        
        SyncCap syncCap = ds.getSyncCap();
        Vector syncType = syncCap.getSyncType();
        assertTrue(syncType != null && syncType.size() == 7);

        Vector ctCaps = ds.getCTCaps();
        assertTrue(ctCaps != null && ctCaps.size() == 1);
        CTCap ctCap = (CTCap)ctCaps.elementAt(0);
        Vector capProperties = ctCap.getProperties();
        assertTrue(capProperties != null && capProperties.size() == 1);
        Property prop = (Property)capProperties.elementAt(0);
        Vector vals = prop.getValEnums();
        assertTrue(propName.equals(prop.getPropName()));
        assertTrue(vals != null && vals.size() == 1);
        String val = (String)vals.elementAt(0);
        assertTrue(valEnum0.equals(val));

        // Check DSMem
        DSMem dsMem = ds.getDSMem();
        assertTrue(dsMem.isSharedMem());
        assertTrue(dsMem.getMaxMem() == maxMem);
        assertTrue(dsMem.getMaxID() == maxId);
        
        Vector exts = devInf.getExts();
        assertTrue(exts != null && exts.size() == 1);
    }
}


