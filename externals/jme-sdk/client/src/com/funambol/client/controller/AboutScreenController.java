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

package com.funambol.client.controller;

import com.funambol.client.ui.AboutScreen;
import com.funambol.client.ui.Bitmap;
import com.funambol.client.customization.Customization;
import com.funambol.client.localization.Localization;

/**
 */
public class AboutScreenController {

    private static final String TAG_LOG = "AboutScreenController";

    private AboutScreen          aboutScreen;
    private Controller           controller;
    private Customization        customization;
    private Localization         localization;

    public AboutScreenController(Controller controller, AboutScreen aboutScreen,
                                 Customization customization)
    {
        this.aboutScreen   = aboutScreen;
        this.controller    = controller;
        this.customization = customization;
        this.localization = controller.getLocalization();

        controller.setAboutScreenController(this);
    }
   
    /**
     * Adds the necessary fields in the about screen. The set of visible fields
     * depends on the Customization and the product version
     */
    public void addNecessaryFields() {

        // Add application name
        String applicationName = customization.getApplicationFullname();
        if (applicationName != null && applicationName.length() > 0) {
            String version = localization.getLanguage("about_version") + " " + customization.getVersion();
            // Customization has precedence over anything else
            if (version != null) {
                applicationName = applicationName + " " + version;
            }
            aboutScreen.addApplicationName(applicationName);
        }

        //Add Company name
        String companyName = customization.getCompanyName();
        if (companyName != null && companyName.length() > 0) {
            aboutScreen.addCompanyName(companyName);
        }
        
        // Add copyright if necessary
        String copyright = customization.getAboutCopyright();
        if (copyright != null && copyright.length() > 0) {
            aboutScreen.addCopyright(copyright);
        }

        // Add application URL
        String url = customization.getAboutSite();
        if (url != null && url.length() > 0) {
            aboutScreen.addWebAddress(url);
        }

        // Add license
        if (customization.showAboutLicence()) {
            String license = customization.getLicense();
            if (license != null && license.length() > 0) {
                aboutScreen.addLicence(license);
            }
        }

        // Add powered by
        if (customization.showPoweredBy()) {
            String poweredBy = customization.getPoweredBy();
            if (poweredBy != null && poweredBy.length() > 0) {
                aboutScreen.addPoweredBy(poweredBy);
            }
            Bitmap poweredByLogo = customization.getPoweredByLogo();
            if (poweredByLogo != null) {
                aboutScreen.addPoweredByLogo(poweredByLogo);
            }
        }
    }

    public void close() {
        aboutScreen.close();
    }

    public AboutScreen getAboutScreen() {
        return aboutScreen;
    }
}


