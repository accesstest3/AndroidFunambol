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
import java.util.Date;
import java.util.Calendar;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParser;
//import org.xmlpull.v1.XmlPullParserFactory;
import org.kxml2.io.KXmlParser;

import com.funambol.syncml.protocol.SyncML;
import com.funambol.syncml.protocol.DevInf;
import com.funambol.syncml.protocol.VerDTD;
import com.funambol.syncml.protocol.DataStore;
import com.funambol.syncml.protocol.SourceRef;
import com.funambol.syncml.protocol.CTInfo;
import com.funambol.syncml.protocol.SyncCap;
import com.funambol.syncml.protocol.SyncType;
import com.funambol.syncml.protocol.Ext;
import com.funambol.syncml.protocol.Source;
import com.funambol.syncml.protocol.CTCap;
import com.funambol.syncml.protocol.Property;
import com.funambol.syncml.protocol.PropParam;
import com.funambol.syncml.protocol.DSMem;

import com.funambol.util.DateUtil;
import com.funambol.util.StringUtil;
import com.funambol.util.Log;

/**
 * This class is meant to provide a SyncML parser. Such a parser reads a SyncML
 * message and builds a representation of this message based on the objects
 * provided by the com.funambol.syncml.protocol objects.
 * The implementation is based on KXml and relies on its XmlPull interface. As
 * such it is capable of parsing both XML and WBXML.
 * At the moment the implementation is restricted to some components of the
 * SyncML message. In particular the DevInf section.
 * This parser performs a relaxed parsing, allowing unknown tokens to be parsed.
 * These tokens are simply skipped and they are not reflected into the SyncML
 * representation.
 */
public class SyncMLParser {
    public SyncMLParser() {
    }

    /**
     * Parse the results section of the server response. At the moment this
     * method assumes this section contains only the server device info. This is
     * not true in general, but this is currently a limitation. In the future it
     * will be made more general.
     *
     * @param results is the results section of the server response. This value
     * shall not contain the &lt;Results&gt; tag
     * @return a DevInf object representing the DeviceInfo
     * @throws SyncMLParserException if the text cannot be parser properly. Note
     * that if the text contains unknown tags, they are simply skipped, but if
     * it has malformed xml, an exception is thrown.
     */
    public DevInf parseResults(String results) throws SyncMLParserException {

/*
        // KXml minimal version does not have the factory parser,
        // therefore we need direct instantiation
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
*/
        XmlPullParser parser = new KXmlParser();

        DevInf devInf = null;
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(results.getBytes());
            parser.setInput(is, "UTF-8");
            do {
                nextSkipSpaces(parser);
                if (parser.getEventType() != parser.END_DOCUMENT) {
                    require(parser, parser.START_TAG, null, null);
                    String tagName = parser.getName();

                    if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_CMDID)) {
                        parseCmdId(parser);
                    } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_MSGREF)) {
                        parseMsgRef(parser);
                    } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_CMDREF)) {
                        parseCmdRef(parser);
                    } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_META)) {
                        parseMeta(parser);
                    } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_ITEM)) {
                        devInf = parseItem(parser);
                    } else {
                        String msg = "Error parsing device info tag. Skipping unexpected token: " + tagName;
                        Log.error(msg);
                        skipUnknownToken(parser, tagName);
                    }
                }
            } while(parser.getEventType() != parser.END_DOCUMENT);
        } catch (Exception e) {
            Log.error("Error parsing DeviceInfo: " + e.toString());
            throw new SyncMLParserException("Cannot parse device info: " + e.toString());
        }
        return devInf;
    }

    /**
     * Parse a put command of a SyncML message. At the moment this
     * method assumes this section contains only the server device info. This is
     * not true in general, but this is currently a limitation. In the future it
     * will be made more general.
     *
     * @param put is the put command of the server response. This value
     * shall not contain the &lt;Results&gt; tag
     * @return a DevInf object representing the DeviceInfo
     * @throws SyncMLParserException if the text cannot be parser properly. Note
     * that if the text contains unknown tags, they are simply skipped, but if
     * it has malformed xml, an exception is thrown.
     */
    public DevInf parsePut(String put) throws SyncMLParserException {
        XmlPullParser parser = new KXmlParser();

        DevInf devInf = null;
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(put.getBytes());
            parser.setInput(is, "UTF-8");
            do {
                nextSkipSpaces(parser);
                if (parser.getEventType() != parser.END_DOCUMENT) {
                    require(parser, parser.START_TAG, null, null);
                    String tagName = parser.getName();

                    if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_CMDID)) {
                        parseCmdId(parser);
                    } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_MSGREF)) {
                        parseMsgRef(parser);
                    } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_CMDREF)) {
                        parseCmdRef(parser);
                    } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_META)) {
                        parseMeta(parser);
                    } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_ITEM)) {
                        devInf = parseItem(parser);
                    } else {
                        String msg = "Error parsing device info tag. Skipping unexpected token: " + tagName;
                        Log.error(msg);
                        skipUnknownToken(parser, tagName);
                    }
                }
            } while(parser.getEventType() != parser.END_DOCUMENT);
        } catch (Exception e) {
            Log.error("Error parsing DeviceInfo: " + e.toString());
            throw new SyncMLParserException("Cannot parse device info: " + e.toString());
        }
        return devInf;
    }

    private void parseCmdId(XmlPullParser parser) throws XmlPullParserException,
                                                         IOException {
        parseNumber(parser);
        parser.next();
        require(parser, parser.END_TAG, null, SyncML.TAG_CMDID);
    }

    private void parseMsgRef(XmlPullParser parser) throws XmlPullParserException,
                                                          IOException {
        parseNumber(parser);
        parser.next();
        require(parser, parser.END_TAG, null, SyncML.TAG_MSGREF);
    }

    private void parseCmdRef(XmlPullParser parser) throws XmlPullParserException,
                                                          IOException {
        parseNumber(parser);
        parser.next();
        require(parser, parser.END_TAG, null, SyncML.TAG_CMDREF);
    }

    private void parseMeta(XmlPullParser parser) throws XmlPullParserException,
                                                        IOException,
                                                        SyncMLParserException {
        nextSkipSpaces(parser);
        while (parser.getEventType() == parser.START_TAG) {
            String tagName = parser.getName();
            if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_TYPE)) {
                parseSimpleStringTag(parser, SyncML.TAG_TYPE);
            } else {
                String msg = "Error parsing META tag. Skipping unexpected token: " + tagName;
                Log.error(msg);
                skipUnknownToken(parser, tagName);
            }
            nextSkipSpaces(parser);
        }
        require(parser, parser.END_TAG, null, SyncML.TAG_META);
    }

    private DevInf parseItem(XmlPullParser parser) throws XmlPullParserException,
                                                          IOException,
                                                          SyncMLParserException {

        DevInf devInf = null;
        nextSkipSpaces(parser);
        Source source = null;
        while (parser.getEventType() == parser.START_TAG) {
            String tagName = parser.getName();
            if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_SOURCE)) {
                source = parseItemSource(parser);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DATA)) {
                if (source != null && SyncML.DEVINF12.equals(source.getLocURI())) {
                    devInf = parseDevInfData(parser);
                } else {
                    parseData(parser);
                }
            } else {
                String msg = "Error parsing ITEM tag. Skipping unexpected token: " + tagName;
                Log.error(msg);
                skipUnknownToken(parser, tagName);
            }
            nextSkipSpaces(parser);
        }
        require(parser, parser.END_TAG, null, SyncML.TAG_ITEM);

        return devInf;
    }

    private Source parseItemSource(XmlPullParser parser) throws XmlPullParserException,
                                                                IOException,
                                                                SyncMLParserException {
        nextSkipSpaces(parser);
        Source source = new Source();
        while (parser.getEventType() == parser.START_TAG) {
            String tagName = parser.getName();
            if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_LOCURI)) {
                String locUri = parseSimpleStringTag(parser, SyncML.TAG_LOCURI);
                source.setLocURI(locUri);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_LOCNAME)) {
                String locName = parseSimpleStringTag(parser, SyncML.TAG_LOCNAME);
                source.setLocName(locName);
            } else {
                String msg = "Error parsing ITEM tag. Skipping unexpected token: " + tagName;
                Log.error(msg);
                skipUnknownToken(parser, tagName);
            }
            nextSkipSpaces(parser);
        }
        require(parser, parser.END_TAG, null, SyncML.TAG_SOURCE);
        return source;
    }

    private DevInf parseDevInfData(XmlPullParser parser) throws XmlPullParserException,
                                                                IOException,
                                                                SyncMLParserException {
        DevInf devInf = parseDevInf(parser);
        nextSkipSpaces(parser);
        require(parser, parser.END_TAG, null, SyncML.TAG_DATA);
        return devInf;
    }

    private void parseData(XmlPullParser parser) throws XmlPullParserException,
                                                        IOException,
                                                        SyncMLParserException {

        // We skip everything until we find the DATA closure tag
        // TODO: we shall return the data somehow
        skipUnknownToken(parser, SyncML.TAG_DATA);
    }


    /**
     * Parse a Device Info section.
     * @param parser is the parser
     */
    private DevInf parseDevInf(XmlPullParser parser) throws XmlPullParserException,
                                                            IOException,
                                                            SyncMLParserException {
        // In general the item parsing depends on its type
        // At the moment we only expect devinf, so we start parsing a dev inf
        // item's data
        DevInf devInf = new DevInf();

        nextSkipSpaces(parser);
        require(parser, parser.START_TAG, null, SyncML.TAG_DEVINF);
        nextSkipSpaces(parser);
        while (parser.getEventType() == parser.START_TAG) {
            String tagName = parser.getName();
            if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_VERDTD)) {
                parseVerDTD(parser, devInf);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DEVINFMAN)) {
                String man = parseSimpleStringTag(parser, SyncML.TAG_DEVINFMAN);
                devInf.setMan(man);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DEVINFMOD)) {
                String mod = parseSimpleStringTag(parser, SyncML.TAG_DEVINFMOD);
                devInf.setMod(mod);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DEVINFOEM)) {
                String oem = parseSimpleStringTag(parser, SyncML.TAG_DEVINFOEM);
                devInf.setOEM(oem);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DEVINFFWV)) {
                String fwv = parseSimpleStringTag(parser, SyncML.TAG_DEVINFFWV);
                devInf.setFwV(fwv);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DEVINFSWV)) {
                String swv = parseSimpleStringTag(parser, SyncML.TAG_DEVINFSWV);
                devInf.setSwV(swv);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DEVINFHWV)) {
                String hwv = parseSimpleStringTag(parser, SyncML.TAG_DEVINFHWV);
                devInf.setHwV(hwv);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DEVINFDEVID)) {
                String devId = parseSimpleStringTag(parser, SyncML.TAG_DEVINFDEVID);
                devInf.setDevID(devId);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DEVINFDEVTYP)) {
                String devTyp = parseSimpleStringTag(parser, SyncML.TAG_DEVINFDEVTYP);
                devInf.setDevTyp(devTyp);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DEVINFUTC)) {
                parseDevInfUtc(parser, devInf);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DEVINFLO)) {
                parseDevInfLo(parser, devInf);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DEVINFNC)) {
                parseDevInfNc(parser, devInf);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DEVINFDATASTORE)) {
                DataStore ds = parseDevInfDataStore(parser, devInf);
                devInf.addDataStore(ds);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_EXT)) {
                Vector exts = parseExt(parser);
                devInf.addExts(exts);
            } else {
                String msg = "Error parsing ITEM tag. Skipping unexpected token: " + tagName;
                Log.error(msg);
                skipUnknownToken(parser, tagName);
            }
            nextSkipSpaces(parser);
        }
        require(parser, parser.END_TAG, null, SyncML.TAG_DEVINF);
        return devInf;
    }

    private void parseVerDTD(XmlPullParser parser, DevInf devInf)
                                                 throws XmlPullParserException,
                                                        IOException {
        String verDtd = parseString(parser);
        VerDTD ver = new VerDTD(verDtd);
        devInf.setVerDTD(ver);
        parser.next();
        require(parser, parser.END_TAG, null, SyncML.TAG_VERDTD);
    }

    private void parseDevInfUtc(XmlPullParser parser, DevInf devInf)
                                                   throws XmlPullParserException,
                                                          IOException {
        devInf.setUTC(true);
        parser.next();
        require(parser, parser.END_TAG, null, SyncML.TAG_DEVINFUTC);
    }

    private void parseDevInfLo(XmlPullParser parser, DevInf devInf)
                                                   throws XmlPullParserException,
                                                          IOException {
        devInf.setSupportLargeObjs(true);
        parser.next();
        require(parser, parser.END_TAG, null, SyncML.TAG_DEVINFLO);
    }

    private void parseDevInfNc(XmlPullParser parser, DevInf devInf)
                                                   throws XmlPullParserException,
                                                          IOException {
        devInf.setSupportNumberOfChanges(true);
        parser.next();
        require(parser, parser.END_TAG, null, SyncML.TAG_DEVINFNC);
    }



    private DataStore parseDevInfDataStore(XmlPullParser parser, DevInf devInf)
                                                      throws SyncMLParserException,
                                                             XmlPullParserException,
                                                             IOException {
        DataStore ds = new DataStore();

        nextSkipSpaces(parser);
        while (parser.getEventType() == parser.START_TAG) {
            String tagName = parser.getName();
            if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_SOURCEREF)) {
                SourceRef sourceRef = parseSourceRef(parser);
                ds.setSourceRef(sourceRef);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DISPLAYNAME)) {
                String displayName = parseSimpleStringTag(parser, SyncML.TAG_DISPLAYNAME);
                ds.setDisplayName(displayName);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_MAXGUIDSIZE)) {
                long size = parseSimpleLongTag(parser, SyncML.TAG_MAXGUIDSIZE);
                ds.setMaxGUIDSize(size);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_RX)) {
                parseRxs(parser, ds);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_RXPREF)) {
                parseRxPref(parser, ds);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_TX)) {
                parseTxs(parser, ds);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_TXPREF)) {
                parseTxPref(parser, ds);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_SYNCCAP)) {
                SyncCap cap = parseSyncCap(parser);
                ds.setSyncCap(cap);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_CTCAP)) {
                CTCap cap = parseCTCap(parser);
                ds.addCTCap(cap);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DSMEM)) {
                DSMem dsMem = parseDSMem(parser);
                ds.setDSMem(dsMem);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DATASTOREHS)) {
                parseDevInfHs(parser, devInf);
            } else {
                String msg = "Error parsing DATA STORE tag. Skipping unexpected token: " + tagName;
                Log.error(msg);
                skipUnknownToken(parser, tagName);
            }
            nextSkipSpaces(parser);
        }
        require(parser, parser.END_TAG, null, SyncML.TAG_DEVINFDATASTORE);

        return ds;
    }

    private SourceRef parseSourceRef(XmlPullParser parser) throws XmlPullParserException,
                                                                  IOException {
        String name = parseString(parser);
        SourceRef sr = new SourceRef(name);
        parser.next();
        require(parser, parser.END_TAG, null, SyncML.TAG_SOURCEREF);

        return sr;
    }

    private void parseRxs(XmlPullParser parser, DataStore ds)
                                                 throws SyncMLParserException,
                                                        XmlPullParserException,
                                                        IOException {

        CTInfo ctInfo = parseCTInfo(parser);
        ds.addRxs(ctInfo);
        require(parser, parser.END_TAG, null, SyncML.TAG_RX);
    }

    private void parseRxPref(XmlPullParser parser, DataStore ds)
                                                throws SyncMLParserException,
                                                       XmlPullParserException,
                                                       IOException {
        CTInfo ctInfo = parseCTInfo(parser);
        ds.setRxPref(ctInfo);
        require(parser, parser.END_TAG, null, SyncML.TAG_RXPREF);
    }

    private void parseTxs(XmlPullParser parser, DataStore ds)
                                                 throws SyncMLParserException,
                                                        XmlPullParserException,
                                                        IOException {

        CTInfo ctInfo = parseCTInfo(parser);
        ds.addTxs(ctInfo);
        require(parser, parser.END_TAG, null, SyncML.TAG_TX);
    }

    private void parseTxPref(XmlPullParser parser, DataStore ds)
                                                throws SyncMLParserException,
                                                       XmlPullParserException,
                                                       IOException {
        CTInfo ctInfo = parseCTInfo(parser);
        ds.setTxPref(ctInfo);
        require(parser, parser.END_TAG, null, SyncML.TAG_TXPREF);
    }


    private CTInfo parseCTInfo(XmlPullParser parser) throws SyncMLParserException,
                                                            XmlPullParserException,
                                                            IOException {
        CTInfo ctInfo = new CTInfo();

        nextSkipSpaces(parser);
        while (parser.getEventType() == parser.START_TAG) {
            String tagName = parser.getName();
            if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_CTTYPE)) {
                String type = parseSimpleStringTag(parser, SyncML.TAG_CTTYPE);
                ctInfo.setCTType(type);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_VERCT)) {
                String ver = parseSimpleStringTag(parser, SyncML.TAG_VERCT);
                ctInfo.setVerCT(ver);
            } else {
                String msg = "Error parsing CTINFO tag. Skipping Unexpected token: " + tagName;
                Log.error(msg);
                skipUnknownToken(parser, tagName);
            }
            nextSkipSpaces(parser);
        }
        return ctInfo;
    }

    private DSMem parseDSMem(XmlPullParser parser) throws SyncMLParserException,
                                                          XmlPullParserException,
                                                          IOException {
        DSMem dsMem = new DSMem();

        System.out.println("Parsing DSMem");

        nextSkipSpaces(parser);
        while (parser.getEventType() == parser.START_TAG) {
            String tagName = parser.getName();
            System.out.println("tagName="+tagName);
            if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_SHAREDMEM)) {
                parseSimpleStringTag(parser, SyncML.TAG_SHAREDMEM);
                dsMem.setSharedMem(true);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_MAXMEM)) {
                long maxMem = parseSimpleLongTag(parser, SyncML.TAG_MAXMEM);
                dsMem.setMaxMem(maxMem);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_MAXID)) {
                long maxId = parseSimpleLongTag(parser, SyncML.TAG_MAXID);
                dsMem.setMaxID(maxId);
            } else {
                String msg = "Error parsing DSMEM tag. Skipping Unexpected token: " + tagName;
                Log.error(msg);
                skipUnknownToken(parser, tagName);
            }
            nextSkipSpaces(parser);
        }
        require(parser, parser.END_TAG, null, SyncML.TAG_DSMEM);
        System.out.println("Done");
        return dsMem;
    }


    private SyncCap parseSyncCap(XmlPullParser parser) throws XmlPullParserException,
                                                              IOException,
                                                              SyncMLParserException {

        SyncCap syncCap = new SyncCap();

        nextSkipSpaces(parser);
        while (parser.getEventType() == parser.START_TAG) {
            String tagName = parser.getName();
            if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_SYNCTYPE)) {
                SyncType type = parseSyncType(parser);
                syncCap.addSyncType(type);
            } else {
                String msg = "Error parsing DATA STORE tag. Unexpected token: " + tagName;
                Log.error(msg);
                skipUnknownToken(parser, tagName);
            }
            nextSkipSpaces(parser);
        }
        require(parser, parser.END_TAG, null, SyncML.TAG_SYNCCAP);
        return syncCap;
    }

    private CTCap parseCTCap(XmlPullParser parser) throws XmlPullParserException,
                                                          IOException,
                                                          SyncMLParserException {

        CTCap ctCap = new CTCap();
        CTInfo ctInfo = new CTInfo();
        nextSkipSpaces(parser);
        while (parser.getEventType() == parser.START_TAG) {
            String tagName = parser.getName();
            if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_CTTYPE)) {
                String type = parseSimpleStringTag(parser, SyncML.TAG_CTTYPE);
                ctInfo.setCTType(type);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_VERCT)) {
                String ver = parseSimpleStringTag(parser, SyncML.TAG_VERCT);
                ctInfo.setVerCT(ver);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_PROPERTY)) {
                Property property = parseProperty(parser);
                ctCap.addProperty(property);
            } else {
                String msg = "Error parsing CTINFO tag. Skipping Unexpected token: " + tagName;
                Log.error(msg);
                skipUnknownToken(parser, tagName);
            }
            nextSkipSpaces(parser);
        }
        ctCap.setCTInfo(ctInfo);
        require(parser, parser.END_TAG, null, SyncML.TAG_CTCAP);
        return ctCap;
    }
    
    private void parseDevInfHs(XmlPullParser parser, DevInf devInf)
                                                   throws XmlPullParserException,
                                                          IOException {
        devInf.setSupportHierarchicalSync(true);
        parser.next();
        require(parser, parser.END_TAG, null, SyncML.TAG_DATASTOREHS);
    }


    private Property parseProperty(XmlPullParser parser) throws XmlPullParserException,
                                                                IOException,
                                                                SyncMLParserException {
        
        nextSkipSpaces(parser);
        Property property = new Property();

        while (parser.getEventType() == parser.START_TAG) {
            String tagName = parser.getName();
            if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_PROPNAME)) {
                String name = parseSimpleStringTag(parser, SyncML.TAG_PROPNAME);
                property.setPropName(name);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_MAXSIZE)) {
                long maxSize = parseSimpleLongTag(parser, SyncML.TAG_MAXSIZE);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_VALENUM)) {
                String val = parseSimpleStringTag(parser, SyncML.TAG_VALENUM);
                property.addValEnum(val);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_MAXOCCUR)) {
                long maxOccur = parseSimpleLongTag(parser, SyncML.TAG_MAXOCCUR);
                property.setMaxOccur((int)maxOccur);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_PROPPARAM)) {
                PropParam param = parsePropertyParam(parser);
                property.addPropParam(param);
            } else {
                String msg = "Error parsing PROPERTY tag. Skipping unexpected token: " + tagName;
                Log.error(msg);
                skipUnknownToken(parser, tagName);
            }
            nextSkipSpaces(parser);
        }
        require(parser, parser.END_TAG, null, SyncML.TAG_PROPERTY);
        return property;
    }

    private PropParam parsePropertyParam(XmlPullParser parser) throws XmlPullParserException,
                                                                      IOException,
                                                                      SyncMLParserException {
        
        nextSkipSpaces(parser);
        PropParam propParam = new PropParam();

        while (parser.getEventType() == parser.START_TAG) {
            String tagName = parser.getName();
            if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_PARAMNAME)) {
                String name = parseSimpleStringTag(parser, SyncML.TAG_PARAMNAME);
                propParam.setParamName(name);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DISPLAYNAME)) {
                String displayName = parseSimpleStringTag(parser, SyncML.TAG_DISPLAYNAME);
                propParam.setDisplayName(displayName);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_VALENUM)) {
                String val = parseSimpleStringTag(parser, SyncML.TAG_VALENUM);
                propParam.addValEnum(val);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_DATATYPE)) {
                String dataType = parseSimpleStringTag(parser, SyncML.TAG_DATATYPE);
                propParam.setDataType(dataType);
            } else {
                String msg = "Error parsing EXT tag. Skipping unexpected token: " + tagName;
                Log.error(msg);
                skipUnknownToken(parser, tagName);
            }
            nextSkipSpaces(parser);
        }
        require(parser, parser.END_TAG, null, SyncML.TAG_PROPPARAM);
        return propParam;
    }


    private SyncType parseSyncType(XmlPullParser parser) throws XmlPullParserException,
                                                                IOException,
                                                                SyncMLParserException {

        long type = parseLong(parser);
        SyncType syncType = new SyncType((int)type);
        parser.next();
        require(parser, parser.END_TAG, null, SyncML.TAG_SYNCTYPE);

        return syncType;
    }

    private String parseSimpleStringTag(XmlPullParser parser, String tag) throws XmlPullParserException,
                                                                                 IOException,
                                                                                 SyncMLParserException {

        String value = "";
        parser.next();
        if (parser.getEventType() == parser.TEXT) {
            value = parser.getText();
            parser.next();
        }
        require(parser, parser.END_TAG, null, tag);

        return value;
    }

    private long parseSimpleLongTag(XmlPullParser parser, String tag) throws XmlPullParserException,
                                                                             IOException,
                                                                             SyncMLParserException {
        long value = parseLong(parser);
        parser.next();
        require(parser, parser.END_TAG, null, tag);

        return value;
    }
 
    private Vector parseExt(XmlPullParser parser) throws XmlPullParserException,
                                                         IOException,
                                                         SyncMLParserException {

        Vector exts = new Vector();

        nextSkipSpaces(parser);
        Ext ext = null;
        while (parser.getEventType() == parser.START_TAG) {
            String tagName = parser.getName();
            if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_XNAM)) {
                String name = parseSimpleStringTag(parser, SyncML.TAG_XNAM);
                ext = new Ext();
                ext.setXNam(name);
                exts.addElement(ext);
            } else if (StringUtil.equalsIgnoreCase(tagName, SyncML.TAG_XVAL)) {
                String value = parseSimpleStringTag(parser, SyncML.TAG_XVAL);
                if (ext == null) {
                    String msg = "Error parsing EXT tag. Found value without name. Skipping it"
                                  + tagName;
                    Log.error(msg);
                } else {
                    ext.addXVal(value);
                }
            } else {
                String msg = "Error parsing EXT tag. Skipping unexpected token: " + tagName;
                Log.error(msg);
                skipUnknownToken(parser, tagName);
            }
            nextSkipSpaces(parser);
        }
        require(parser, parser.END_TAG, null, SyncML.TAG_EXT);
        return exts;
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

    private void nextSkipSpaces(XmlPullParser parser) throws SyncMLParserException,
                                                             XmlPullParserException,
                                                             IOException {
        int eventType = parser.next();
        if (eventType == parser.TEXT) {
            if (!parser.isWhitespace()) {
                Log.error("Unexpected text: " + parser.getText());
                throw new SyncMLParserException("Unexpected text: " + parser.getText());
            }
            parser.next();
        }
    }

    private void skipUnknownToken(XmlPullParser parser, String tagName)
                                                   throws  SyncMLParserException,
                                                           XmlPullParserException,
                                                           IOException
    {
        do {
            parser.next();
        } while (parser.getEventType() != parser.END_TAG && !tagName.equals(parser.getName()));
    }

    private String parseNumber(XmlPullParser parser) throws XmlPullParserException,
                                                            IOException {
        parser.next();
        String value = parser.getText();
        return value;
    }

    private String parseString(XmlPullParser parser) throws XmlPullParserException,
                                                            IOException {
        parser.next();
        String value = parser.getText();
        return value;
    }

    private long parseLong(XmlPullParser parser) throws XmlPullParserException,
                                                        IOException,
                                                        SyncMLParserException {
        parser.next();
        String value = parser.getText();
        try {
            long l = Long.parseLong(value);
            return l;
        } catch (Exception e) {
            String msg = "Error while parsing long " + e.toString();
            SyncMLParserException pe = new SyncMLParserException(msg);
            throw pe;
        }
    }
}

