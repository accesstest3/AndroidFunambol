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


package com.funambol.android;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import com.funambol.util.Log;

/**
 * This activity is helpful for the tester in order to change the test suite url
 * after the command line run of the instrumentation application.
 */
public class AndroidTestUrl extends Activity {

    private static final String TAG_LOG = "AndroidTestUrl";

    private EditText url;

    private boolean isTouchActionRunning = false;
    private boolean isEditComplete = false;

    public boolean isTouchActionRunning() {
        return isTouchActionRunning;
    }

    /**
     * Same as onCreate()
     */
    @Override
    protected void onResume() {
        initViews();
        super.onResume();
    }

    /**
     * Initialize the views for this activity. Actually there is only an
     * EditText view to change the Test suite url, but it can be enriched with
     * other parameters.
     * @param savedInstanceState the Bundle object related to this activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initViews();
        super.onCreate(savedInstanceState);
    }


    /**
     * Listen any kind of user interaction on this activity. Set the related
     * value to true
     */
    @Override
    public void onUserInteraction() {
        Log.error(TAG_LOG, "Touch performed");
        isTouchActionRunning = true;
        isEditComplete = false;
        super.onUserInteraction();
    }

    /**
     * Retrieve the test suite url modified or confirmedby the user.
     * @return String the String formatted test url
     */
    public String getTestUrl() {
        return url.getText().toString();
    }

    /**
     * Creates the start menu
     * @param menu the main activity Menu object
     * @return boolean the native implementation value
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Start");
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * There is only one item that the user can select so in any case the
     * invokation of this method (system dependent) set the edit action as
     * finished and closes the main menu.
     * @param featureId the index of them menu item
     * @param item the MenuItem object
     * @return boolean the value defined in the parent of this
     * class
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        Log.error(TAG_LOG, "Started!!");
        isEditComplete = true;
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * Accessor method
     * @return boolean true if the edit action is finished, false otherwise
     */
    public boolean isEditComplete() {
        return isEditComplete;
    }

    /**
     * This activity just owns an EditText view for now. The initial value is 
     * taken as the default url value (hard coded) or as the url input as 
     * command line argument by the tester before to run the main
     * Instrumentation.
     */
    private void initViews() {
        url = new EditText(this);
        url.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        url.setTextSize(10);
        url.setText(AndroidScriptRunner.getInstance().getReportContent());
        url.setTextColor(Color.BLUE);
        url.setVisibility(EditText.VISIBLE);
        url.append(AndroidScriptRunner.getInstance().getMainTestUrl());
        setContentView(url);
    }

}
