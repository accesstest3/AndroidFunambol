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
import android.app.AlarmManager;
import android.app.Instrumentation;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.funambol.util.Log;

/**
 * The report activity that display the test results (PASSED or FAILED) and the
 * eventual report.
 */
public class AndroidTestReport extends Activity {

    /** Tag used to address this class for logging purposes */
    private static final String TAG_LOG = "AndroidTestReport";

    private ScrollView sv;
    private TextView tv;

    boolean isTouchActionRunning = false;

    /**
     * Get the inrformation related to the user interaction with this activity's
     * screen.
     * @return boolean true if an user interaction occurred, false otherwise
     */
    public boolean isTouchActionRunning() {
        return isTouchActionRunning;
    }


    /**
     * Initialize the activity views when the activity is first created.
     * @param savedInstanceState the Bundle object to be passed t the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }


    /**
     * When the system invokes this method it means that the instrumentation 
     * related to the test suite is finished and one of the following 
     * circumstances occurred: the tester manually exited the activity or the 
     * instrumentation class called the onDestroy method. In any case after this 
     * call the command line displays the result of the test suite that is 0 if 
     * the test is passed, -1 if it failed. This implementation should give an
     * easier way to examine the test result using an automatic test runner just
     * parsing the value returned by instrumentation without referring to the 
     * log file entries. 
     */
    @Override
    protected void onDestroy() {
        AndroidScriptRunner runner = AndroidScriptRunner.getInstance();
        runner.finish(runner.getBasicScriptRunner().getErrorCode(), new Bundle());
        super.onDestroy();
    }

    /**
     * Manages a touch event occurred on this view setting the related variable
     * to true
     * @param event the MotionEvent to be considered.
     * @return boolean true always because the touch event considered in this
     * activity is pure scrolling and it needs to be deactivated on finish.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.error(TAG_LOG, "Touch performed");
        isTouchActionRunning = true;
        return super.onTouchEvent(event);
    }

    /**
     * Initialize the main scroll view of this activity filling it with the test
     * suite report information. Finally set the defined view as the principal
     * to this activity
     */
    private void initViews() {
        sv = new ScrollView(this);
        tv = new TextView(this);
        tv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        tv.setTextSize(10);

        boolean success = false;
        String reportContent = AndroidScriptRunner.getInstance().getReportContent();
        if(reportContent.trim().length() == 0) {
            reportContent = "\nSUCCESS";
            success = true;
        }
        android.util.Log.i(TAG_LOG, "---------------------------------------------------");
        android.util.Log.i(TAG_LOG, "Funambol Integration test result: ");
        android.util.Log.i(TAG_LOG,  reportContent);
        android.util.Log.i(TAG_LOG, "---------------------------------------------------");
        tv.setText(reportContent);
        if(!success) {
            tv.setTextColor(Color.RED);
        }
        tv.setVisibility(TextView.VISIBLE);
        tv.setScrollContainer(true);
        sv.addView(tv);
        setContentView(sv);
    }
}
