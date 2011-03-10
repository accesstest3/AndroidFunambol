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

package com.funambol.android.activities.settings;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CheckBox;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.funambol.android.R;


public class TwoLinesCheckBox extends LinearLayout implements OnClickListener {

    private LinearLayout checkboxContainer;

    protected CheckBox checkBox;
    protected TextView text1; // Showed with standard font size
    protected TextView text2; // Showed with small font size
    protected RelativeLayout textContainer;

    public TwoLinesCheckBox(Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        checkboxContainer = (LinearLayout)inflater.inflate(
                R.layout.two_lines_checkbox, this, false);
        this.addView(checkboxContainer);

        checkBox = (CheckBox)findViewById(R.id.checkbox);
        text1 = (TextView)findViewById(R.id.text1);
        text2 = (TextView)findViewById(R.id.text2);
        textContainer = (RelativeLayout)findViewById(R.id.textContainer);

        this.setOnClickListener(this);
    }

    public void onClick(View v) {
        if(!isEnabled()) {
            return;
        }
        if(checkBox.isChecked()) {
            checkBox.setChecked(false);
        } else {
            checkBox.setChecked(true);
        }
    }

    public void setText1(String text) {
        text1.setText(text);
        updateLayout();
    }

    public void setText2(String text) {
        text2.setText(text);
        updateLayout();
    }

    private void updateLayout() {
        if(text2.getText() != null && text2.getText().length() > 0) {
            textContainer.addView(text2);
        } else {
            textContainer.removeView(text2);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {

        super.setEnabled(enabled);
        checkBox.setEnabled(enabled);

        if(!enabled) {
            text1.setTextColor(R.color.gray);
            text2.setTextColor(R.color.gray);
        }
    }
    
    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        checkboxContainer.setPadding(left, top, right, bottom);
    }

    public boolean isChecked() {
        return checkBox.isChecked();
    }

    public void setChecked(boolean checked) {
        checkBox.setChecked(checked);
    }
}

