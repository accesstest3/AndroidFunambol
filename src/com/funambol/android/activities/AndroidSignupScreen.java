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
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.funambol.android.R;
import com.funambol.android.AndroidAppSyncSourceManager;
import com.funambol.android.AppInitializer;
import com.funambol.android.controller.AndroidController;
import com.funambol.android.controller.AndroidSignupScreenController;

import com.funambol.client.controller.AccountScreenController;
import com.funambol.client.controller.HomeScreenController;
import com.funambol.client.controller.SignupHandler;
import com.funambol.client.controller.SignupScreenController;
import com.funambol.client.controller.UISyncSourceController;
import com.funambol.client.customization.Customization;
import com.funambol.client.localization.Localization;
import com.funambol.client.source.AppSyncSource;
import com.funambol.client.source.AppSyncSourceManager;
import com.funambol.client.ui.SignupScreen;
import com.funambol.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Funambol Signup Activity
 */
public class AndroidSignupScreen extends AccountAuthenticatorActivity
        implements SignupScreen {

    private static final String TAG_LOG = "AndroidSignupScreen";

    private HomeScreenController homeScreenController;

    private EditText  userField;
    private EditText  passField;
    private String    serverUrl;

    private EditText  captchaField;
    private ImageView captchaImage;
    private byte[]    captchaBitmap;

    private CheckBox  showPassword;
    
    private TextView  termsAndConditionsField;

    private Button    continueButton;
    private Button    signupButton;
    private Button    loginButton;

    private ScrollView signupScrollView          = null;
    private ViewGroup  signupViewsContainer      = null;
    private ViewGroup  credentialsViewsContainer = null;
    private ViewGroup  captchaViewsContainer     = null;
    private View       screenSeparator           = null;

    private UpdateSignupFieldsUIThread updateSignupFieldsUIThread;
    private SetCaptchaImageUIThread setCaptchaImageUIThread;
    private SetCaptchaTokenUIThread setCaptchaTokenUIThread;
    
    private AppSyncSource configSource;
    private SignupScreenController controller;
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

        Account account = AndroidController.getNativeAccount();

        // Check if a Funambol account already exists
        if(account != null) {
            String label = getString(R.string.alert_one_account_supported_1) + " " +
                           getString(R.string.account_label) + " " +
                           getString(R.string.alert_one_account_supported_2);
            Toast.makeText(this, label, Toast.LENGTH_LONG).show();
            finish();
        }

        if(!AndroidController.isInitialized()) {
            AppInitializer initializer = AppInitializer.getInstance(this);
            initializer.initController();
        }
        
        AndroidController gc = AndroidController.getInstance();
        customization = gc.getCustomization();
        localization  = gc.getLocalization();
        displayManager = (AndroidDisplayManager)gc.getDisplayManager();
        appSyncSourceManager = gc.getAppSyncSourceManager();
        homeScreenController = gc.getHomeScreenController();

        updateSignupFieldsUIThread = new UpdateSignupFieldsUIThread();
        setCaptchaImageUIThread = new SetCaptchaImageUIThread();
        setCaptchaTokenUIThread = new SetCaptchaTokenUIThread();

        setContentView(R.layout.signup_screen);

        signupScrollView = (ScrollView)findViewById(R.id.signup_scroll_view);
        signupViewsContainer = (ViewGroup)findViewById(R.id.signup_container);
        credentialsViewsContainer = new CredentialsFieldsView(this);
        captchaViewsContainer = new CaptchaFieldsView(this);

        screenSeparator = findViewById(R.id.signup_screen_separator);
        screenSeparator.setFocusable(true);
        screenSeparator.setFocusableInTouchMode(true);
        
        userField = (EditText)credentialsViewsContainer.findViewById(R.id.username);
        userField.setInputType(InputType.TYPE_CLASS_PHONE);
        userField.setHint(localization.getLanguage("signup_username_hint"));

        passField = (EditText)credentialsViewsContainer.findViewById(R.id.password);

        captchaField = (EditText)captchaViewsContainer.findViewById(R.id.captcha);
        captchaField.setInputType(InputType.TYPE_CLASS_NUMBER);
        captchaImage = (ImageView)captchaViewsContainer.findViewById(R.id.captcha_image);

        if(customization.getDefaultMSUValidationMode() == SignupHandler.VALIDATION_MODE_NONE) {
            termsAndConditionsField = (TextView)credentialsViewsContainer.findViewById(
                R.id.signup_terms_and_conditions_cred);
        } else {
            termsAndConditionsField = (TextView)captchaViewsContainer.findViewById(
                R.id.signup_terms_and_conditions);
        }
        
        String tAndC = localization.getLanguage("signup_terms_and_conditions2");
        String pp = localization.getLanguage("signup_terms_and_conditions4");
        StringBuffer termsText = new StringBuffer();
        termsText.append(localization.getLanguage("signup_terms_and_conditions1"));
        termsText.append(" ");
        termsText.append(tAndC);
        termsText.append(" ");
        termsText.append(localization.getLanguage("signup_terms_and_conditions3"));
        termsText.append(" ");
        termsText.append(pp);
        
        termsAndConditionsField.setText(termsText.toString());
        Linkify.addLinks(termsAndConditionsField, Pattern.compile(tAndC), "",
                null, new Linkify.TransformFilter() {
            public final String transformUrl(final Matcher match, String url) {
                return customization.getTermsAndConditionsUrl();
            }
        });

        Linkify.addLinks(termsAndConditionsField, Pattern.compile(pp), "",
                null, new Linkify.TransformFilter() {
            public final String transformUrl(final Matcher match, String url) {
                return customization.getPrivacyPolicyUrl();
            }
        });
        
        showPassword = (CheckBox)credentialsViewsContainer.findViewById(
                R.id.show_password);
        showPassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
                if(isChecked) {
                    passField.setTransformationMethod(null);
                } else {
                    passField.setTransformationMethod(
                            new PasswordTransformationMethod());
                }
            }
        });
        showPassword.setChecked(true);

        int width = userField.getWidth();
        userField.setMaxWidth(width);
        passField.setMaxWidth(width);

        if(customization.getDefaultMSUValidationMode() == SignupHandler.VALIDATION_MODE_NONE) {
            signupButton = (Button)credentialsViewsContainer.findViewById(R.id.signup_button_cred);
        } else {
            signupButton = (Button)captchaViewsContainer.findViewById(R.id.signup_button);
        }

        signupButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                controller.signup();
            }
        });
        signupButton.setId(1);

        continueButton = (Button)credentialsViewsContainer.findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                controller.continueWithSignup();
            }
        });
        continueButton.setId(2);

        loginButton = (Button)findViewById(R.id.login_button);
        loginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                controller.switchToLoginScreen();
            }
        });
        loginButton.setId(4);

        if(customization.getDefaultMSUValidationMode() == SignupHandler.VALIDATION_MODE_NONE) {
            credentialsViewsContainer.removeView(continueButton);
            signupButton.setVisibility(View.VISIBLE);
            termsAndConditionsField.setVisibility(View.VISIBLE);
        }

        promptCredentials();

        // We must initialize the controller here
        controller = new AndroidSignupScreenController(gc, gc.getCustomization(), 
                gc.getConfiguration(), gc.getLocalization(),
                gc.getAppSyncSourceManager(), this);
        gc.setSignupScreenController(controller);

        // Check if there's any extras parameter containing the actual credentials
        Intent intent = getIntent();
        if(intent != null && intent.getExtras() != null &&
                intent.getExtras().containsKey("username")) {
            Bundle extras = intent.getExtras();
            // Server url must be always set to default
            String syncUrl = extras.getString("syncurl");
            String username = extras.getString("username");
            String password = extras.getString("password");
            controller.initScreen(syncUrl, username, password);
        } else {
            controller.initScreen();
        }

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

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putBoolean("captcha", isCaptchaPrompted());
        if(captchaBitmap != null) {
            b.putByteArray("captcha_image", captchaBitmap);
        }
        b.putString("captcha_token", getCaptchaToken());

        b.putString("account_username", getUsername());
        b.putString("account_password", getPassword());

        b.putBoolean("account_show_password", showPassword.isChecked());

        b.putString("jsessionid", controller.getCurrentJSessionId());
    }

    @Override
    public void onRestoreInstanceState(Bundle b) {
        super.onRestoreInstanceState(b);
        if(b.getBoolean("captcha", false)) {
            promptCaptcha();
        }
        if(b.containsKey("captcha_token")) {
            setCaptchaToken(b.getString("captcha_token"));
        }
        if(b.containsKey("captcha_image")) {
            setCaptchaImage(b.getByteArray("captcha_image"));
        }
        if(b.containsKey("account_username")) {
            setUsername(b.getString("account_username"));
        }
        if(b.containsKey("account_password")) {
            setPassword(b.getString("account_password"));
        }
        if(b.getBoolean("account_show_password", false)) {
            showPassword.setChecked(true);
        }
        if(b.containsKey("jsessionid")) {
            controller.setCurrentJSessionId(b.getString("jsessionid"));
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

    public void disableSignup() {
    }

    public void enableSignup() {
    }

    public String getSyncUrl() {
        return serverUrl;
    }
    
    public void setSyncUrl(String originalUrl) {
        if (customization.syncUriEditable()) {
            serverUrl = originalUrl;
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

    public void setCaptchaToken(String token) {
        setCaptchaTokenUIThread.setToken(token);
        runOnUiThread(setCaptchaTokenUIThread);
    }

    public String getCaptchaToken() {
        return captchaField.getText().toString();
    }

    public void setCaptchaImage(byte[] image) {
        setCaptchaImageUIThread.setImage(image);
        runOnUiThread(setCaptchaImageUIThread);
    }

    public void promptCredentials() {
        updateSignupFieldsUIThread.setView(credentialsViewsContainer);
        runOnUiThread(updateSignupFieldsUIThread);
    }

    public void promptCaptcha() {
        updateSignupFieldsUIThread.setView(captchaViewsContainer);
        runOnUiThread(updateSignupFieldsUIThread);
    }

    private boolean isCaptchaPrompted() {
        return (signupViewsContainer.getChildCount() == 1 &&
                signupViewsContainer.getChildAt(0) == captchaViewsContainer);
    }

    public boolean isPasswordShowed() {
        return !(passField.getTransformationMethod()
                instanceof PasswordTransformationMethod);
    }

    public void addShowPasswordField(boolean checked) {
        View v = credentialsViewsContainer.findViewById(R.id.show_password_row);
        v.setVisibility(View.VISIBLE);
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

    private class UpdateSignupFieldsUIThread implements Runnable {

        private View view;

        public void setView(View view) {
            this.view = view;
        }

        public void run() {
            if(signupViewsContainer.getChildCount() == 1 &&
               signupViewsContainer.getChildAt(0) != view) {
                signupViewsContainer.removeView(signupViewsContainer.getChildAt(0));
                signupViewsContainer.addView(view); 
            } else if(signupViewsContainer.getChildCount() == 0) {
                signupViewsContainer.addView(view); 
            }
            // Update scroll view position
            signupScrollView.scrollTo(0, 0);

            // Update focused field
            screenSeparator.requestFocus();
        }
    }

    private class SetCaptchaImageUIThread implements Runnable {

        private byte[] image;

        public void setImage(byte[] image) {
            this.image = image;
        }

        public void run() {
            android.graphics.Bitmap captcha = BitmapFactory.decodeByteArray(
                image, 0, image.length);
            captchaImage.setImageBitmap(captcha);
            captchaBitmap = image;
        }
    }

    private class SetCaptchaTokenUIThread implements Runnable {

        private String token;

        public void setToken(String token) {
            this.token = token;
        }

        public void run() {
            captchaField.setText(token);
        }
    }

    private class CredentialsFieldsView extends LinearLayout {

        private ViewGroup container;

        public CredentialsFieldsView(Context c) {
            super(c);
            View.inflate(c, R.layout.signup_credentials_view, this);
            container = (ViewGroup)findViewById(R.id.signup_credentials);
        }

        @Override
        public void removeView(View view) {
            container.removeView(view);
        }
    }

    private class CaptchaFieldsView extends LinearLayout {
        public CaptchaFieldsView(Context c) {
            super(c);
            View.inflate(c, R.layout.signup_captcha_view, this);
        }
    }
}
