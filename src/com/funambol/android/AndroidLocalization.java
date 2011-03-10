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

package com.funambol.android;

import java.util.Date;
import java.util.HashMap;
import java.lang.reflect.Field;

import android.text.format.DateFormat;
import android.content.Context;

import com.funambol.client.localization.Localization;
import com.funambol.util.Log;

/**
 * Singleton implementation of a container class to access the Funambol Android
 * Client localization logic. The singleton instance can be accessed using the
 * default static getter method
 */
public class AndroidLocalization implements Localization {

    private static final String TAG = "AndroidLocalization";

    private static AndroidLocalization instance = null;
    private Class stringsClass;
    private Context context;
    private HashMap<String,String> cache = new HashMap<String,String>();

    /**
     * Private constructor to enforce the Singleton pattern
     * @param context is the application Contetxt related object
     */
    private AndroidLocalization(Context context) {
        // Get an handle to the resource class
        this.context = context;
        try {
            stringsClass = Class.forName("com.funambol.android.R$string");
        } catch (Exception e) {
            Log.error(TAG, "Cannot load strings ", e);
        }
    }

    /**
     * Instance getter method
     * @param context is the application Contetxt related object
     * @return this class instance
     */
    public static AndroidLocalization getInstance(Context context) {
        if (instance == null) {
            instance = new AndroidLocalization(context);
        }
        return instance;
    }

    /**
     * Dispose this object setting it to a NULL reference
     */
    public static void dispose() {
        instance = null;
    }

    /**
     * Get the language corresponding to the give key
     * @param key the key related to the requested language
     * @return String the String formatted language representation corresponding
     * to the given key or the key itself if no value was found
     */
    public String getLanguage(String key) {

        String value = cache.get(key);
        if (value != null) {
            return value;
        }

        if (stringsClass != null) {
            try {
                Field field = stringsClass.getField(key);
                if (field != null) {
                    Integer id = (Integer)field.get(null);
                    value = context.getString(id.intValue());
                    if (value != null) {
                        cache.put(key, value);
                        return value;
                    }
                }
            } catch (Exception e) {
                Log.error(TAG, "Cannot load string named: " + key, e);
            }
        }

        // By default we return the key...
        return key;
    }

    /**
     * Get the "E, MMM dd, yyyy" formatted string date given a long date
     * @param date the String representation of the date
     * @return String the "E, MMM dd, yyyy" formatted string corresponding to
     * the given date
     */
    public String getDate(long date) {
        CharSequence val = DateFormat.format("E, MMM dd, yyyy", new Date(date));
        return val.toString();
    }

    /**
     * Get the "h:mmaa" formatted string time given a long date reference
     * @param date the String representation of the date
     * @return String the "E, MMM dd, yyyy" formatted string corresponding to
     * the given date
     */
    public String getTime(long date) {
        CharSequence val = DateFormat.format("h:mmaa", new Date(date));
        return val.toString();
    }

}
