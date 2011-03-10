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

/*
 * This code makes use of Android native sources:
 *
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.funambol.android.edit_contact;

import android.content.ContentValues;
import com.funambol.android.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.funambol.android.edit_contact.ContactDataStructure.ContactData;
import com.funambol.android.edit_contact.ContactDataStructure.EditorListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Represents a View for editing the Contact's photo
 */
public class EditContactPhotoView extends ImageView implements OnClickListener {
    
    private static final String TAG = "EditContactPhotoView";

    private boolean hasSetPhoto = false;

    private ContactData cData;
    private ContentValues cValues;
    
    private EditorListener listener;

    private byte[] photo_bytes = null;

    public EditContactPhotoView(Context context) {
        super(context);
    }

    public EditContactPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (listener != null) {
            listener.onRequest(EditorListener.REQUEST_PICK_PHOTO);
        }
    }

    public void setState(ContactDataStructure.ContactDataType dataType, ContactData cData,
            ContentValues cValues, boolean refresh) {

        this.cData = cData;
        this.cValues = cValues;
        
        if (cValues != null) {

            photo_bytes = cValues.getAsByteArray(Photo.PHOTO);

            if (photo_bytes != null) {
                 Bitmap photo = BitmapFactory.decodeByteArray(photo_bytes, 0,
                        photo_bytes.length);

                setScaleType(ImageView.ScaleType.CENTER_CROP);
                setImageBitmap(photo);
                setEnabled(true);
                
                hasSetPhoto = true;
            } else {
                resetDefault();
            }
        } else {
            resetDefault();
        }
    }

    public void getContactValues(ContactValues values) {
        updateContactValues();
        values.put(cData.mimeType+0, cValues);
    }

    private void updateContactValues() {
        ContentValues value = new ContentValues();
        value.put(ContactsContract.Data.MIMETYPE, cData.mimeType);
        if(hasSetPhoto) {
            value.put(Photo.PHOTO, photo_bytes);
            if(cValues != null) {
                value.put(Photo.IS_SUPER_PRIMARY,
                    cValues.getAsInteger(Photo.IS_SUPER_PRIMARY));
            }
        }
        this.cValues = value;
    }

    /**
     * Set the super primary bit on the photo.
     */
    public void setSuperPrimary(boolean superPrimary) {
        if(hasSetPhoto && cValues != null) {
            cValues.put(Photo.IS_SUPER_PRIMARY, superPrimary ? 1 : 0);
        }
    }

    /**
     * Return true if a valid {@link Photo} has been set.
     */
    public boolean hasSetPhoto() {
        return hasSetPhoto;
    }

    public void setEditorListener(EditorListener listener) {
        this.listener = listener;
    }

    public void setPhotoBitmap(Bitmap photo) {
        if (photo == null) {
            // Clear any existing photo and return
            resetDefault();
            return;
        }

        final int size = photo.getWidth() * photo.getHeight() * 4;
        final ByteArrayOutputStream out = new ByteArrayOutputStream(size);

        try {
            photo.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            setImageBitmap(photo);
            photo_bytes = out.toByteArray();
            
            setEnabled(true);
            hasSetPhoto = true;

            updateContactValues();

        } catch (IOException e) {
            Log.w(TAG, "Unable to serialize photo: " + e.toString());
        }
    }

    protected void resetDefault() {
        // Invalid photo, show default "add photo" place-holder
        setScaleType(ImageView.ScaleType.CENTER);
        setImageResource(R.drawable.ic_menu_add_picture);
        setEnabled(true);
        hasSetPhoto = false;
        photo_bytes = null;

        updateContactValues();
    }
}


