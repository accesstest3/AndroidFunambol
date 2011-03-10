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

package com.funambol.android.providers;

import android.net.Uri;
import android.provider.MediaStore.Video.Media;
import android.provider.MediaStore.Video;

/**
 * This is a private content provider meant to support videos synchronization
 * in the Funambol sync client.
 * At the moment the provider is READ ONLY and it is basically a wrapper around
 * the MediaStore.
 */
public class VideosContentProvider extends MediaContentProvider {

    private static final String TAG = "VideosContentProvider";

    private static final String CONTENT_URI_STRING = "content://com.funambol.videos";
    private static final String EXTERNAL_URI_STRING = CONTENT_URI_STRING + EXTERNAL_CONTENT_URI_STRING;
    private static final String INTERNAL_URI_STRING = CONTENT_URI_STRING + INTERNAL_CONTENT_URI_STRING;

    public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
    public static final Uri EXTERNAL_CONTENT_URI = Uri.parse(EXTERNAL_URI_STRING);
    public static final Uri INTERNAL_CONTENT_URI = Uri.parse(INTERNAL_URI_STRING);

    private static final String[] SUPPORTED_MIME_TYPES = {"video/3gpp", "video/mp4",
                                                          "video/rtsp", "video/flv"};

    private static final Uri MEDIA_PROVIDER_EXT_URI = Media.EXTERNAL_CONTENT_URI;
    private static final Uri MEDIA_PROVIDER_INT_URI = Media.INTERNAL_CONTENT_URI;

    private static final String TYPE = "vnd.android.cursor.item/vnd.funambol.videos";

    public VideosContentProvider() {
        super(CONTENT_URI_STRING, MEDIA_PROVIDER_INT_URI, MEDIA_PROVIDER_EXT_URI,
              TYPE, Video.VideoColumns.BUCKET_ID);
    }

    @Override
    protected String[] getSupportedMimeTypes() {
        return SUPPORTED_MIME_TYPES;
    }
}
