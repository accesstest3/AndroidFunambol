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

package com.funambol.android.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ImageView;

import com.funambol.android.R;
import com.funambol.android.AppInitializer;
import com.funambol.android.controller.AndroidController;

import com.funambol.client.ui.AboutScreen;
import com.funambol.client.ui.Bitmap;
import com.funambol.client.controller.AboutScreenController;
import com.funambol.client.controller.Controller;
import com.funambol.client.customization.Customization;

/**
 * Funambol About Activity
 */
public class AndroidAboutScreen extends Activity implements AboutScreen
{   
    private AppInitializer initializer;

    private AboutScreenController aboutScreenController;

    private TextView copyTitle;
    private ImageView logo;
    private TextView copyText;
    private TextView company;
    private TextView copyRight;
    private TextView copyUrl;
    private TextView license;
    private TextView poweredBy;
    private ImageView poweredByLogo;

    /** 
     * Called with the activity is first created. 
     */
    public void onCreate(Bundle icicle)
    { 
        super.onCreate(icicle);
        setContentView(R.layout.about);

        copyTitle = (TextView)findViewById(R.id.aboutCopyTitle);
        copyText  = (TextView)findViewById(R.id.aboutCopyText);
        copyUrl   = (TextView)findViewById(R.id.aboutCopyUrl);
        license   = (TextView)findViewById(R.id.aboutLicense);
        poweredBy = (TextView)findViewById(R.id.poweredBy);
        poweredByLogo = (ImageView)findViewById(R.id.poweredByLogo);
        copyRight  = (TextView)findViewById(R.id.aboutCopyRight);
        company = (TextView) findViewById(R.id.aboutCompanyName);

        Button closeButton = ((Button) findViewById(R.id.aboutClose));
        closeButton.setOnClickListener(new CloseListener());

        // Initialize the view for this controller
        initializer = AppInitializer.getInstance(this);
        Controller cont = AndroidController.getInstance();
        Customization customization = initializer.getCustomization();
        aboutScreenController = new AboutScreenController(cont, this, customization);
        aboutScreenController.addNecessaryFields();
    }

    public Object getUiScreen() {
        return this;
    }

    public void addApplicationName(String name) {
        copyTitle.setText(name);
        copyTitle.setVisibility(View.VISIBLE);
    }

    public void addCompanyName(String companyName) {
        company.setText(companyName);
        company.setVisibility(View.VISIBLE);
    }

    public void addCopyright(String copyright) {
        copyText.setText(copyright);
        copyText.setVisibility(View.VISIBLE);
    }

    public void addWebAddress(String url) {
        copyUrl.setText(url);
        copyUrl.setVisibility(View.VISIBLE);
    }

    public void addLicence(String license) {
        this.license.setText(license);
        this.license.setVisibility(View.VISIBLE);
    }

    public void addPoweredBy(String poweredBy) {
        this.poweredBy.setText(poweredBy);
        this.poweredBy.setVisibility(View.VISIBLE);
    }

    public void addPoweredByLogo(Bitmap logo) {
        Integer id = (Integer)logo.getOpaqueDescriptor();
        poweredByLogo.setImageResource(id.intValue());
        poweredByLogo.setVisibility(View.VISIBLE);
    }

    public void close() {
        finish();
    }

    /**
     * A call-back for when the user presses the close button.
     */
    private class CloseListener implements OnClickListener {
        public void onClick(View v) {
            aboutScreenController.close();
        }
    }

}
