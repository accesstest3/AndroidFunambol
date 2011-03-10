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

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import com.funambol.android.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Defines a button which allows to edit a date field using a DatePickerDialog
 */
public class EditContactDateFieldView extends Button implements View.OnClickListener {

    /** The current date value */
    private String value = null;

    /**
     * Create a EditContactDateFieldView given the Context
     * 
     * @param context
     */
    public EditContactDateFieldView(Context context) {
        super(context);
        init();
    }

    /**
     * Create a EditContactDateFieldView given the Context and attrobutes
     *
     * @param context
     * @param attrs 
     */
    public EditContactDateFieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setText(R.string.label_click_to_add);
        setOnClickListener(this);
    }

    public void setValue(String value) {
        this.value = value;
        setText(DateFormat.format("d MMMM yyyy", getValueCalendar(value)));
    }

    public String getValue() {
        return value;
    }

    private Calendar getValueCalendar(String value) {
        if(value == null) {
            return new GregorianCalendar();
        }
        /* YYYYMMDD */
        if(value.length() == 8) {
            int year  = Integer.parseInt(value.substring(0, 4));
            int month = Integer.parseInt(value.substring(4, 6))-1;
            int day   = Integer.parseInt(value.substring(6));
            return new GregorianCalendar(year, month, day);
        }
        /* YYYY-MM-DD YYYY/MM/DD */
        else if(value.length() == 10) {
            int year  = Integer.parseInt(value.substring(0, 4));
            int month = Integer.parseInt(value.substring(5, 7))-1;
            int day   = Integer.parseInt(value.substring(8));
            return new GregorianCalendar(year, month, day);
        }
        return new GregorianCalendar();
    }

    public void onClick(View v) {
        createDateDialog().show();
    }

    private Dialog createDateDialog() {
        Calendar cal = getValueCalendar(value);
        return  new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker dp, int year, int month, int day) {
                setValue(formatValue(year, month, day));
            }
        }, cal.get(Calendar.YEAR),
           cal.get(Calendar.MONTH),
           cal.get(Calendar.DAY_OF_MONTH));
    }

    private String formatValue(int year, int month, int day) {
        return DateFormat.format("yyyy-MM-dd", new GregorianCalendar(year, month, day)).toString();
    }
}
