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

package com.funambol.android.source.pim.contact;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.util.Vector;

import com.funambol.common.pim.model.vcard.VCardSyntaxParserListenerImpl;
import com.funambol.common.pim.model.vcard.VCardFormatter;
import com.funambol.common.pim.model.common.FormatterException;

import com.funambol.common.pim.vcard.VCardSyntaxParser;
import com.funambol.common.pim.vcard.ParseException;

import com.funambol.android.AndroidCustomization;

import com.funambol.util.Log;

/**
 * A Contact item. This object extends the pim framework data model, by adding
 * the ability to be loaded/saved into the Android address book
 */
public class Contact extends com.funambol.common.pim.model.contact.Contact {

    private static final String TAG = "Contact";

    private long id = -1;

    // Constructors------------------------------------------------
    public Contact() {
        super();
    }

    /**
     * Set the person identifier
     */
    public void setId(String id) {
        this.id = Long.parseLong(id);
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * Get the person identifier
     */
    public long getId() {
        return id;
    }

    public void setVCard(byte vCard[]) throws ParseException {
        Log.trace(TAG, "Creating contact from vCard");
        ByteArrayInputStream is = new ByteArrayInputStream(vCard);
        VCardSyntaxParserListenerImpl listener = new VCardSyntaxParserListenerImpl(this, null, "UTF-8");
        VCardSyntaxParser parser = new VCardSyntaxParser(is);
        parser.setListener(listener);
        parser.parse();
    }

    public void toVCard(OutputStream os, boolean allFields) throws FormatterException, IOException {
        VCardFormatter formatter = new VCardFormatter(null, null);
        String value = formatter.format(this, allFields ? getSupportedFields() : null);
        os.write(value.getBytes());
    }

    public static Vector<String> getSupportedFields() {
        Vector<String> res = new Vector<String>();
        res.add("N");
        if (AndroidCustomization.getInstance().isDisplayNameSupported()) {
            res.add("FN");
        }
        res.add("NICKNAME");
        res.add("TEL;VOICE;HOME");
        res.add("TEL;VOICE;WORK");
        res.add("TEL;CELL");
        res.add("TEL;VOICE");
        res.add("TEL;FAX;HOME");
        res.add("TEL;FAX;WORK");
        res.add("TEL;PAGER");
        res.add("TEL;WORK;PREF");
        res.add("TEL;FAX");
        res.add("TEL;PREF;VOICE");
        res.add("EMAIL;INTERNET");
        res.add("EMAIL;INTERNET;HOME");
        res.add("EMAIL;INTERNET;WORK");
        res.add("EMAIL;INTERNET;HOME;X-FUNAMBOL-INSTANTMESSENGER");
        res.add("ADR");
        res.add("ADR;HOME");
        res.add("ADR;WORK");
        res.add("URL");
        res.add("URL;HOME");
        res.add("URL;WORK");
        res.add("BDAY");
        res.add("X-ANNIVERSARY");
        res.add("X-FUNAMBOL-CHILDREN");
        res.add("X-SPOUSE");
        res.add("TITLE");
        res.add("ORG");
        res.add("NOTE");
        res.add("PHOTO");
        return res;
    }
}
