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

package com.funambol.storage;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

import com.funambol.platform.FileAdapter;

import com.funambol.util.Log;

/**
 *
 */
public class StringKeyValueFileStore implements StringKeyValueStore {

    protected Hashtable store;
    protected String    fileName;

    private static final int  LF = 10;
    private static final char SEPARATOR = '=';

    public StringKeyValueFileStore(String fileName) {
        store = new Hashtable();
        this.fileName = fileName;
    }

    public void add(String key, String value) {
        put(key, value);
    }

    public void update(String key, String value) {
        put(key, value);
    }

    public String put(String key, String value) {
        return (String)store.put(key, value);
    }

    public String get(String key) {
        return (String)store.get(key);
    }

    public Enumeration keys() {
        return store.keys();
    }

    public Enumeration keyValuePairs() {

        final Enumeration keys   = store.keys();
        final Enumeration values = store.elements();

        return new Enumeration () {

            boolean last = false;

            public Object nextElement() {

                String key   = (String)keys.nextElement();
                String value = (String)values.nextElement();

                return new StringKeyValuePair(key, value);
            }

            public boolean hasMoreElements() {
                return keys.hasMoreElements() && values.hasMoreElements();
            }
        };
    }

    public boolean contains(String key) {
        return store.get(key) != null;
    }

    public String remove(String key) {
        return (String)store.remove(key);
    }

    public void save() throws IOException {
        FileAdapter file = null;
        OutputStream os  = null;
        try {
            file = new FileAdapter(fileName);
            os = file.openOutputStream();

            Enumeration keys = store.keys();
            while(keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                String value = this.get(key);
                os.write(key.getBytes());
                os.write((int)SEPARATOR);
                os.write(value.getBytes());
                os.write((int)LF);
            }
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ioe) {
                }
            }
            if (file != null) {
                try {
                    file.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    public void load() throws IOException {
        FileAdapter file = null;
        InputStream is   = null;
        try {
            file = new FileAdapter(fileName);
            is = file.openInputStream();
            StringBuffer currentLine = new StringBuffer();
            for(long i=0, l=file.getSize(); i<l; ++i) {
                char b = (char)is.read();
                if (b == (char)LF) {
                    // This is the end of a line
                    String line = currentLine.toString().trim();
                    int pos = line.indexOf(SEPARATOR);
                    if (pos > 0) {
                        String key = line.substring(0, pos);
                        String value = line.substring(pos + 1, line.length());
                        this.put(key, value);
                    } else {
                        throw new IOException("Malformed String Store file");
                    }
                    currentLine = new StringBuffer();
                } else {
                    currentLine.append((char)b);
                }
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {}
            }
            if (file != null) {
                try {
                    file.close();
                } catch (IOException ioe) {}
            }
        }
    }

    public void reset() throws IOException {
        FileAdapter file = null;
        OutputStream os = null;
        try {
            file = new FileAdapter(fileName);
            os = file.openOutputStream();
        } catch (Exception e) {
            throw new IOException(e.toString());
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ioe) {
                }
            }
            if (file != null) {
                try {
                    file.close();
                } catch (IOException ioe) {
                }
            }
        }
        store = new Hashtable();
    }
}

