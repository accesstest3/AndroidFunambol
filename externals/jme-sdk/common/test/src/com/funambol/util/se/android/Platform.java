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

package com.funambol.util;

import junit.framework.*;
import android.test.*;
import android.os.Environment;

/**
 * Generalize the Platform dependent os calls for Android platform during tests
 * execution.
 */
public class Platform {

    /**
     * Singleton implementation for this class
     */
    private static Platform instance = null;

    AndroidContext ac = new AndroidContext();

    /** private default constructor */
    private Platform() {
        ac = new AndroidContext();
    }

    /**
     * Use this to refer to this class' methods
     * @return Platform the unique instance of this class
     */
    public static Platform getInstance() {
        if (instance == null) {
            instance = new Platform();
        }
        return instance;
    }

    /**
     * Gets the default log file location for this platform. As it is and
     * Android platform, due to file writing restriction and usage of log files
     * to be sent via email, the default log location will be the android SD
     * folder, as the android system grants the usage of stram of MIME types as
     * attachment if and only if the files are located on /sdcard/ folder, that
     * is the common log folder.
     * @return String the location path of the device's SD card
     */
    public String getLogFileDir() {
        return ac.getSdCardDir();
    }

    /**
     * This class will be useful to access Amdroid OS Environment and Context
     * in further implementation. In order consistently reference the class
     * android.content.Context, be sure to address the getInstance method of
     * this class container after the System has loaded the application context:
     * the best way is to use the singleton call after the specific test setUp()
     * method call. Otherwise the calls to context could return null objects.
     */
    private class AndroidContext extends AndroidTestCase {
        /** 
         * Accessor nethod to retrieve the Android System's SD card location
         * @return String the device's SD card location path referring to the 
         * android.os.Environment class.
         */
        public String getSdCardDir() {
            return (Environment.getExternalStorageDirectory().getPath() + System.getProperty("file.separator"));
        }

        /**
         * Accessor method to retrievethe path on the device where the
         * application stores its custom data files. For more information,
         * please refer to the android documentation for class
         * android.content.Context (getFilesDir() method).
         * @return String the representation of the application private data
         * files on the device
         */
        public String getApplicationDir() {
            return getContext().getApplicationContext().getFilesDir().getAbsolutePath() +
                System.getProperty("file.separator");
        }
    }
}
