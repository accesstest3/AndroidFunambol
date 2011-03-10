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

import android.accounts.Account;
import android.app.Dialog;
import android.app.Activity;
import android.os.Bundle;
import android.accounts.AccountAuthenticatorActivity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.funambol.android.AndroidAppSyncSourceManager;

import com.funambol.android.R;
import com.funambol.android.AppInitializer;
import com.funambol.android.controller.AndroidController;
import com.funambol.android.controller.AndroidLoginScreenController;
import com.funambol.client.configuration.Configuration;
import com.funambol.client.controller.AccountScreenController;
import com.funambol.client.controller.HomeScreenController;
import com.funambol.client.controller.UISyncSourceController;
import com.funambol.client.customization.Customization;
import com.funambol.client.localization.Localization;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.ui.LoginScreen;
import com.funambol.util.Log;

/**
 * Funambol Login Activity
 */
public class AndroidLoginScreen extends AccountAuthenticatorActivity
        implements LoginScreen {

    private static final String TAG_LOG = "AndroidSignupScreen";

    private HomeScreenController homeScreenController;

    private EditText  userField;
    private EditText  passField;
    private EditText  serverUrl;

    private Button    signupButton;
    private Button    loginButton;

    private View screenSeparator = null;

    private AppSyncSource configSource;
    private AndroidLoginScreenController controller;
    private Customization customization;
    private Localization localization;
    private AppSyncSourceManager appSyncSourceManager;

    private AndroidDisplayManager displayManager;

    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if(!AndroidController.isInitialized()) {
            AppInitializer initializer = AppInitializer.getInstance(this);
            initializer.initController();
        }
        
        Account account = AndroidController.getNativeAccount();
        AndroidController gc = AndroidController.getInstance();
        Configuration configuration = gc.getConfiguration();

        // Check if a Funambol account already exists
        if(account != null && !configuration.getCredentialsCheckPending()) {
            String label = getString(R.string.alert_one_account_supported_1) + " " +
                           getString(R.string.account_label) + " " +
                           getString(R.string.alert_one_account_supported_2);
            Toast.makeText(this, label, Toast.LENGTH_LONG).show();
            finish();
        }

        customization = gc.getCustomization();
        localization  = gc.getLocalization();
        displayManager = (AndroidDisplayManager)gc.getDisplayManager();
        appSyncSourceManager = gc.getAppSyncSourceManager();
        homeScreenController = gc.getHomeScreenController();

        setContentView(R.layout.login_screen);

        userField = (EditText)findViewById(R.id.username);
        passField = (EditText)findViewById(R.id.password);
        serverUrl = (EditText)findViewById(R.id.syncUrl);

        screenSeparator = findViewById(R.id.login_screen_separator);
        screenSeparator.setFocusable(true);
        screenSeparator.setFocusableInTouchMode(true);

        int width = userField.getWidth();
        userField.setMaxWidth(width);
        passField.setMaxWidth(width);
        serverUrl.setMaxWidth(width);

        loginButton = (Button)findViewById(R.id.login_button);
        loginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                controller.login();
            }
        });
        loginButton.setId(1);

        signupButton = (Button)findViewById(R.id.signup_button);
        signupButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                controller.switchToSignupScreen();
            }
        });
        signupButton.setId(2);

        // We must initialize the controller here
        controller = new AndroidLoginScreenController(gc, this);
        gc.setLoginScreenController(controller);

        // Remove the login footer if an account was already created
        if(gc.getConfiguration().getSignupAccountCreated()
                || !customization.getMobileSignupEnabled()) {
            ViewGroup mainView = (ViewGroup)findViewById(R.id.signup_main_view);
            mainView.removeView(mainView.findViewById(R.id.login_footer));
        }

        // Check if there's any extras parameter containing the actual credentials
        Intent intent = getIntent();
        if(intent != null && intent.getExtras() != null &&
                intent.getExtras().containsKey("username")) {
            Bundle extras = intent.getExtras();
            String syncUrl = extras.getString("syncurl");
            String username = extras.getString("username");
            String password = extras.getString("password");
            controller.initScreen(syncUrl, username, password);
        } else {
            controller.initScreen();
        }

        screenSeparator.requestFocus();

        initialize(controller);
    }

    public void initialize(AccountScreenController controller) {

        // Bind the source to the controller and view objects
        configSource = appSyncSourceManager.getSource(AndroidAppSyncSourceManager.CONFIG_ID);

        // If a config source is not available, then the client must be
        // configured without credentials checking.
        if (configSource != null) {
            Log.debug(TAG_LOG, "Registering source controller");
            UISyncSourceController uiSourceController = configSource.getUISyncSourceController();
            if(uiSourceController == null) {
                uiSourceController = new UISyncSourceController(customization,
                                                                localization,
                                                                appSyncSourceManager,
                                                                controller.getController(),
                                                                configSource);
            }
            configSource.setUISyncSourceController(uiSourceController);
            configSource.getSyncSource().setListener(uiSourceController);
        }
    }

    public void disableSave() {
    }

    public void enableSave() {
    }

    public void enableCancel() {
    }

    public void disableCancel() {
    }

    public String getSyncUrl() {
        return serverUrl.getText().toString();
    }

    public void setSyncUrl(String originalUrl) {
        if (customization.syncUriEditable()) {
            serverUrl.setText(originalUrl);
        }
    }

    public String getUsername() {
        return userField.getText().toString();
    }

    public void setUsername(String originalUser) {
        userField.setText(originalUser);
    }

    public String getPassword() {
        return passField.getText().toString();
    }

    public void setPassword(String originalPassword) {
        passField.setText(originalPassword);
    }

    public void addShowPasswordField(boolean checked) {
    }

    public Object getUiScreen() {
        return this;
    }

    public void checkFailed() {
    }

    public void checkSucceeded() {
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && homeScreenController != null &&
                homeScreenController.getHomeScreen() != null) {
            // Close the main screen
            ((Activity)homeScreenController.getHomeScreen()).finish();
            finish();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Log.trace(TAG_LOG, "onCreateDialog: " + id);
        Dialog result = null;
        if(displayManager != null) {
            result = displayManager.createDialog(id);
        }
        if(result != null) {
            return result;
        } else {
            return super.onCreateDialog(id);
        }
    }
}

