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

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.xml.parsers.SAXParserFactory;
import net.rim.device.api.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * 
 */
public final class SimpleXMLParser 
{
    private OneLevelHandler _ParserHelper;
    private SAXParser _MainParser;
    //----------------------------------------
    public SimpleXMLParser() throws Throwable
    {
        this._ParserHelper = new OneLevelHandler("");
        this._MainParser = SAXParserFactory.newInstance().newSAXParser();
    }
    //----------------------------------------
    public void reset()
    {
        this._ParserHelper.reset(this._ParserHelper.getRootTag());
    }
    //----------------------------------------
    public void addMultiHashtableValueTag(String tag)
    {
        this._ParserHelper.addMultiHashtableValueTag(tag);
    }
    //----------------------------------------
    public void addHashtableValueTag(String tag)
    {
        this._ParserHelper.addHashtableValueTag(tag);
    }
    //----------------------------------------
    public void addMultiValueTag(String tag)
    {
        this._ParserHelper.addMultiValueTag(tag);
    }
    //----------------------------------------
    public Hashtable parseData(String roottag, InputSource is) throws Throwable
    {
        this._MainParser.parse(is,this._ParserHelper);
        return this._ParserHelper.getResult();
    }
    //----------------------------------------
    public Hashtable parseData(String roottag, String is) throws Throwable
    {
        return this.parseData(roottag, new InputSource(new ByteArrayInputStream(is.getBytes())));
    }
    //----------------------------------------
    //----------------------------------------
    //----------------------------------------
    static final class OneLevelHandler extends org.xml.sax.helpers.DefaultHandler
    {
        private Hashtable _MultiHashtableValueTag;
        private Hashtable _HashtableValueTag;
        private Hashtable _MultiValueTag;
        private Hashtable _Result;
        
        private String _RootTag;
        private StringBuffer _StringData;
        private String _CurrentTag;
        private String _CurrentHashtableTag;
        private Hashtable _TempResult;
        //-------------------------------------
        public OneLevelHandler(String roottag)
        {
            super();
            this._MultiHashtableValueTag = new Hashtable();
            this._HashtableValueTag = new Hashtable();
            this._MultiValueTag = new Hashtable();
            this._Result = new Hashtable();
            this._RootTag = roottag;
            this._StringData = null;
            this._CurrentTag = null;
            this._TempResult = null;
        }
        //-------------------------------------
        public void addMultiHashtableValueTag(String tag)
        {
            this._MultiHashtableValueTag.put(tag,"");
        }
        //-------------------------------------
        public void addHashtableValueTag(String tag)
        {
            this._HashtableValueTag.put(tag,"");
        }
        //-------------------------------------
        public void addMultiValueTag(String tag)
        {
            this._MultiValueTag.put(tag,"");
        }
        //-------------------------------------
        public String getRootTag()
        {
            return this._RootTag;
        }
        //-------------------------------------
        public Hashtable getResult()
        {
            return this._Result;
        }
        //-------------------------------------
        public void reset(String newroottag)
        {
            this._MultiHashtableValueTag.clear();
            this._HashtableValueTag.clear();
            this._MultiValueTag.clear();
            this._Result.clear();
            this._RootTag = newroottag;
            this._StringData = null;
            this._CurrentTag = null;
            this._CurrentHashtableTag = null;
        }
        //-------------------------------------
        public void startElement(String uri, String localName, String qName, Attributes properties)  throws SAXException
        {
            if (!qName.equals(this._RootTag))
            {
                if (this._MultiHashtableValueTag.containsKey(qName))
                {
                    this._CurrentHashtableTag = qName;
                    this._TempResult = new Hashtable();
                }
                else if (this._HashtableValueTag.containsKey(qName))
                {
                    this._CurrentHashtableTag = qName;
                }
                else
                {
                    this._StringData = new StringBuffer();
                    this._CurrentTag = qName;
                }
            }
        }
        //-------------------------------------
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            if (this._CurrentTag!=null && this._CurrentTag.equals(qName))
            {
                if (this._MultiValueTag.containsKey(qName))
                {
                    Vector tmp = (Vector)this._Result.get(qName);
                    if (tmp==null)
                    {
                        tmp = new Vector();
                    }
                    tmp.addElement(decodeUtf8(this._StringData.toString()));
                    this._Result.put(this._CurrentTag,tmp);
                    tmp=null;
                }
                else if (this._CurrentHashtableTag!=null)
                {
                    if (this._MultiHashtableValueTag.containsKey(this._CurrentHashtableTag))
                    {
                        this._TempResult.put(this._CurrentTag,decodeUtf8(this._StringData.toString()));
                    }
                    else
                    {
                        Hashtable tmp = (Hashtable)this._Result.get(this._CurrentHashtableTag);
                        if (tmp==null)
                        {
                            tmp = new Hashtable();
                        }
                        tmp.put(qName, decodeUtf8(this._StringData.toString()));
                        this._Result.put(this._CurrentHashtableTag,tmp);
                        tmp=null;
                    }
                }
                else
                {
                    // Decode utf 8
                    this._Result.put(this._CurrentTag,decodeUtf8(this._StringData.toString()));
                }
            }
            else if (this._CurrentHashtableTag!=null && this._CurrentHashtableTag.equals(qName))
            {
                if (this._MultiHashtableValueTag.containsKey(qName))
                {
                    Vector tmp = (Vector)this._Result.get(this._CurrentHashtableTag);
                    if (tmp==null)
                    {
                        tmp = new Vector();
                    }
                    tmp.addElement(this._TempResult);
                    this._TempResult = null;
                    this._Result.put(this._CurrentHashtableTag,tmp);
                    tmp = null;
                }
                this._CurrentHashtableTag = null;
            }
        }
        //-------------------------------------
        public void characters(char[] ch, int start, int length) throws SAXException
        {
            if (this._StringData != null)
            {
                this._StringData.append(ch,start,length);
            }
        }
        //-------------------------------------
        public String decodeUtf8(String src)
        {
            String dest = src;
            try {
                byte[]data = src.getBytes("UTF-8");
                String tempstr = new String(data, 0, data.length, "UTF-8");
                dest = tempstr;
            } catch (java.io.UnsupportedEncodingException e) {
                // Use original string - nothing to do
                Log.error("[SimpleXMLparser - decodeUtf8] Parse error: " + e.getMessage());
            }
            return dest;
        }
        //-------------------------------------
    }
} 
