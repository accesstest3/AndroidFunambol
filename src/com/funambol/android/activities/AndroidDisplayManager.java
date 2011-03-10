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
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.widget.Toast;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.funambol.client.controller.Controller;
import com.funambol.client.controller.DialogOption;
import com.funambol.client.ui.Screen;
import com.funambol.client.ui.DisplayManager;
import com.funambol.util.Log;
import java.util.Hashtable;

/**
 * The Display Manager implementation for the Android Client. Manages 3 types of
 * screen views:
 * - Android Client Screen;
 * - Native Alert Dialogs
 * - Custom Alert Dialogs (Selection Dialogs)
 * See DisplayManager interface implementation for further details
 */
public class AndroidDisplayManager implements DisplayManager {

    /** The tag to be written into log messages*/
    private static final String TAG = "AndroidDisplayManager";
    /** Reference object for the native alert dialogs*/
    private Hashtable<Integer, Object> holdingDialogs = new Hashtable<Integer, Object>();
    /** Reference object for the custom alert dialogs*/
    private Hashtable<Integer, AlertDialog> pendingAlerts = new Hashtable<Integer, AlertDialog>();
    /** Reference the runnable to be executed after a specific dialog is dismissed*/
    private Hashtable dismissRunnable = new Hashtable<Integer, Runnable>();
    /** References the native alert dialog with a db sequence-like progression*/
    private static int incrementalId;
    /** Holds the last message shown by the showMessage method. Used for test purpose */
    private String lastMessage = null;

    /**
     * Default constructor
     */
    public AndroidDisplayManager() {
    }

    /**
     * Hide a screen calling the Activity finish method
     * @param screen the Screen to be hidden
     * @throws Exception if the activity related to the encounters
     * any problem
     */
    public void hideScreen(Screen screen) throws Exception {
        Activity activity = (Activity) screen.getUiScreen();
        activity.finish();
    }

    public void showScreen(Screen screen, int screenId) throws Exception {
        showScreen((Activity)screen.getUiScreen(), screenId, null);
    }
    
    /**
     * Use the screen's related activity to put the give screen in foreground.
     * The implementation relies on the Intent mechanism which is peculiar to
     * Android OS: screens are shown calling the startActivity() methods and
     * passing the related intent as parameter.
     * @param context
     * @param screenId the Screen related id
     * @throws Exception if the activity related to the screen encounters
     * any problem
     */
    public void showScreen(Context context, int screenId, Bundle extras) throws Exception {
        Intent intent = null;
        switch (screenId) {
            case Controller.CONFIGURATION_SCREEN_ID: {
                intent = new Intent(context, AndroidSettingsScreen.class);
                break;
            }
            case Controller.LOGIN_SCREEN_ID: {
                intent = new Intent(context, AndroidLoginScreen.class);
                break;
            }
            case Controller.SIGNUP_SCREEN_ID: {
                intent = new Intent(context, AndroidSignupScreen.class);
                break;
            }
            case Controller.ABOUT_SCREEN_ID: {
                intent = new Intent(context, AndroidAboutScreen.class);
                break;
            }
            case Controller.ADVANCED_SETTINGS_SCREEN_ID: {
                intent = new Intent(context, AndroidSettingsScreen.class);
                break;
            }
            case Controller.HOME_SCREEN_ID: {
                intent = new Intent(context, AndroidHomeScreen.class);
                break;
            }
            default:
                Log.error(TAG, "Cannot show unknown screen: " + screenId);
        }
        if (intent != null) {
            if(extras != null) {
                intent.putExtras(extras);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    /**
     * To be implemented
     * @param screen
     * @param screenId
     * @param donotwait
     * @throws Exception
     */
    public void showScreen(Screen screen, int screenId, boolean donotwait) throws Exception {
        showScreen(screen, screenId);
    }

    /**
     * Create a native alert dialog with the 2 options "Yes" and "No".
     * This kind of alert are managed by the activity owner when the call to
     * onCreateDialog is done.
     * @param screen the native alert dialog owner Screen
     * @param question the question to be displayed
     * @param yesAction the runnable that defines the yes option
     * @param noAction the runnable that defines the no option
     * @param timeToWait to be defined
     */
    public void askYesNoQuestion(Screen screen, String question,
            Runnable yesAction,
            Runnable noAction, long timeToWait) {

        int dialogId = getNextDialogId();
        Activity activity = (Activity) screen.getUiScreen();

        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setMessage(question);
        alert.setCancelable(false);
        OnButtonListener yesListener = new OnButtonListener(yesAction);
        OnButtonListener noListener = new OnButtonListener(noAction);
        alert.setPositiveButton(android.R.string.ok, yesListener);
        alert.setNegativeButton(android.R.string.cancel, noListener);

        holdingDialogs.put(dialogId, alert);

        activity.showDialog(dialogId);
    }

    /**
     * Create a native dialog referencing it from the ones contained into the
     * native dialog reference container "holdingDialogs". Call this method 
     * when the onCreateDialog is invocked on the activity that must manage
     * this native dialog.
     * @param id the id of the alert dialog to be created/retrieved
     * @return Dialog the AlertDialog instance corresponding to the given id 
     */
    public Dialog createDialog(int id) {
        Log.debug(TAG, "Creating  dialog " + id);
        Object dialog = holdingDialogs.get(id);
        if (dialog instanceof AlertDialog.Builder) {
            AlertDialog.Builder result = (AlertDialog.Builder) dialog;
            return result.create();
        } else if (dialog instanceof Dialog) {
            Dialog result = (Dialog) dialog;
            return result;
        } else {
            Log.debug(TAG, "Unknown dialog id: " + id);
            return null;
        }
    }

    /**
     * Create a native alert dialog with a visual rotating spinner. To be used
     * to inform the user that some backgorund process is in progress.
     * This kind of alert are managed by the activity owner when the call to
     * onCreateDialog is done. For this reason the progress dialog instance is
     * saved on the holdingDialogs reference with its progressive id.
     * @param screen the native alert dialog owner Screen
     * @param prompt the message prompted on the native alert
     * @return int the dialog id value
     */
    public int showProgressDialog(Screen screen, String prompt) {
        return showProgressDialog(screen, prompt, true);
    }

    public int showProgressDialog(Screen screen, String prompt, boolean indeterminate) {

        int dialogId = getNextDialogId();
        Activity activity = (Activity) screen.getUiScreen();

        ProgressDialog dialog = new ProgressDialog(activity);

        dialog.setMessage(prompt);
        dialog.setIndeterminate(indeterminate);
        dialog.setProgressStyle(indeterminate ? ProgressDialog.STYLE_SPINNER :
                                                ProgressDialog.STYLE_HORIZONTAL );
        dialog.setCancelable(false);

        holdingDialogs.put(dialogId, dialog);

        activity.showDialog(dialogId);

        return dialogId;
    }

    /**
     * Dismiss an alert dialog from a screen given its id
     * @param screen the native alert dialog owner Screen
     * @param id the id of the dialog to be dismissed
     */
    public void dismissProgressDialog(Screen screen, int id) {
        Log.debug("Dismissing progress dialog " + id);
        Activity activity = (Activity) screen.getUiScreen();
        activity.dismissDialog(id);
        activity.removeDialog(id);
        holdingDialogs.remove(id);
    }

    public void setProgressDialogMaxValue(int dialogId, int value) {
        Object dialog = holdingDialogs.get(dialogId);
        if(dialog != null && dialog instanceof ProgressDialog) {
            ((ProgressDialog)dialog).setMax(value);
        }
    }

    public void setProgressDialogProgressValue(int dialogId, int value) {
        Object dialog = holdingDialogs.get(dialogId);
        if(dialog != null && dialog instanceof ProgressDialog) {
            ((ProgressDialog)dialog).setProgress(value);
        }
    }

    public int promptMultipleSelection(Screen screen, String title,
            String okButtonLabel, String cancelButtonLabel,
            String[] choices, boolean[] checkedChoices,
            DialogInterface.OnMultiChoiceClickListener multiChoiceClickListener,
            DialogInterface.OnClickListener okButtonClickListener,
            DialogInterface.OnClickListener cancelButtonClickListener) {

        int dialogId = getNextDialogId();
        Activity a = (Activity) screen.getUiScreen();

        AlertDialog.Builder builder = new AlertDialog.Builder(a)
                .setCustomTitle(buildAlertTitle(a, title, 18))
                .setMultiChoiceItems(choices, checkedChoices, multiChoiceClickListener)
                .setPositiveButton(okButtonLabel, okButtonClickListener)
                .setNegativeButton(cancelButtonLabel, cancelButtonClickListener);

        holdingDialogs.put(dialogId, builder.create());
        a.showDialog(dialogId);
        return dialogId;
    }

    public void showOkDialog(Screen screen, String message, String okButtonLabel) {
        showOkDialog(screen, message, okButtonLabel, null);
    }

    public void showOkDialog(Screen screen, String message, String okButtonLabel, Runnable onClickAction) {
        this.showOkDialog(screen, message, okButtonLabel, onClickAction, true);
    }

    public void showOkDialog(Screen screen, String message, String okButtonLabel, Runnable onClickAction,
            boolean cancelable) {
        int dialogId = getNextDialogId();
        Activity a = (Activity) screen.getUiScreen();

        OnButtonListener lis = null;
        if (onClickAction != null) {
            lis = new OnButtonListener(onClickAction);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(a)
                .setMessage(message)
                .setPositiveButton(okButtonLabel, lis)
                .setCancelable(cancelable);

        holdingDialogs.put(dialogId, builder.create());
        a.showDialog(dialogId);
    }

    /**
     * Create a custom alert dialog based on the given DialogOptions. This kind 
     * of dialog has fixed dialog id that depends on the one set by the caller
     * (Usually an instance of DialogController class). The created dialog is
     * built with a custom title and content and must be managed outside the
     * standard activity dialog management onCreatedialog, but using the
     * acitvity Bundle passed into the native activity methods onCreate() and
     * onSaveInstanceState(). The Funambol Android Client implementation use a
     * DialogController instance and the activity related to the give screen to
     * realize this kind of management
     * @param screen the native alert dialog owner Screen
     * @param message the decription of this dialog options
     * @param options the options array to be displayed to the user
     * @param defaultValue the default selection option int formatted
     * @param dialogId the fixed dialog id set by the caller
     */
    public void promptSelection(Screen screen, String message, DialogOption[] options, int defaultValue, int dialogId) {
        Activity a = (Activity) screen.getUiScreen();
        AlertDialog.Builder builder = new AlertDialog.Builder(a);
        LinearLayout titleLayout = buildAlertTitle(a, message, 20);

        builder.setCustomTitle(titleLayout);

        LinearLayout builderView = buildAlertContent(options, a, dialogId);

        builder.setView(builderView);

        builder.setCancelable(true);

        builder.setOnCancelListener(new SelectionCancelListener(dialogId));

        a.runOnUiThread(new PromptSelection(builder, dialogId));

    }

    /**
     * Dismiss a previously created Selection dialog using the call to 
     * promptSelection(...). Not to be used for native dialog like
     * ProgressDialog as the management logic is different and not delegated to
     * the native Activity method onCreateDialogs.
     * @param id the id of the selection dialog to be dismissed.
     */
    public void dismissSelectionDialog(int id) {
        Log.debug("Dismissing selection dialog " + id);
        AlertDialog alert = pendingAlerts.get(id);
        if (alert != null) {
            if (alert.isShowing()) {
                alert.dismiss();
            }
            pendingAlerts.remove(id);
        }
    }

    public void addPostDismissSelectionDialogAction(int id, Runnable dismissAction) {
        Log.debug(TAG, "Putting an action on the dismissRunnable list");
        dismissRunnable.put(id, dismissAction);
    }

    public void removePostDismissSelectionDialogAction(int id) {
        Log.debug(TAG, "Removing an action from the dismissRunnable list");
        dismissRunnable.remove(id);
    }

    /**
     * Removes a pending alert from the pendingAlert reference container.
     * @param id the alert to be removed from the container.
     */
    public void removePendingAlert(int id) {
        pendingAlerts.remove(id);
    }

    /**
     * Accessor method to understand if a given alert is pending on some
     * activity, that means the alert is displayed on the screen hold by the
     * activity itself
     * @param id the alert id which the pending status is to be retrieved
     * @return true is the given alret id reprensents a pending alert, thus is
     * contained into the selection alert reference container.
     */
    public boolean isAlertPending(int id) {
        return pendingAlerts.containsKey(id);
    }

    /**
     * Not yet implemented into the Android Client.
     * @param question the question to be prompted
     * @param defaultyes the default option
     * @param timeToWait time in milliseconds to wait before dismissing this
     * alert
     * @return boolean to be defined
     */
    public boolean askAcceptDenyQuestion(String question, boolean defaultyes, long timeToWait) {
        return true;
    }

    /**
     * Not yet implemented into the Android Client.
     * @param message the question to be prompted
     * @return boolean to be defined
     */
    public boolean promptNext(String message) {
        return true;
    }

    /**
     * Display a time-boxed toast message to be displayed to the user. This kind
     * of prompt alert are internally managed by the Toast android class
     * implementation, so the management is implicit ant they are not referenced
     * in any other way.
     * @param screen the Screen on which the message must be displayed
     * @param message the mesage to be displayed
     */
    public void showMessage(Screen screen, String message) {


        if (screen != null) {
            Activity a = (Activity) screen.getUiScreen();
            if (a != null) {
                int len = message.length() > 40 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
                a.runOnUiThread(new NotifyRunnable(a, message, len));
            }
        }
    }

    public void showMessage(Screen screen, String message, int delay) {
        if (screen != null) {
            Activity a = (Activity) screen.getUiScreen();
            if (a != null) {
                a.runOnUiThread(new NotifyRunnable(a, message, delay));
            }
        }
    }

    /**
     * Read the last message displayed into the toast and reset it
     * @return String the String formatted last message displayed with the toast
     */
    public String readAndResetLastMessage() {
        String result = lastMessage;
        lastMessage = null;
        return result;
    }

    /**
     * The notice container class. This is a runnable as it must be run on
     * the UI thread related to the screen's activity
     */
    class NotifyRunnable extends Thread implements Runnable {

        private String message;
        private int time;
        private Activity activity;

        /**
         * Constructor
         * @param activity the Activity object that owns the toast
         * @param message the String formatted message to be shown
         * @param time the total duration of the toast in milliseconds
         */
        public NotifyRunnable(Activity activity, String message, int time) {
            this.message = message;
            this.time = time;
            this.activity = activity;
        }

        /**
         * Shows the Toast with the given message for the given amount of time
         */
        @Override
        public void run() {

            //No time is specified, so we use the standard ones:
            //Toast.LENGTH_LONG is about 3 secs
            //Toast.LENGTH_SHORT is about 2 secs (1850 ms)
            if (time == Toast.LENGTH_LONG || time == Toast.LENGTH_SHORT) {
                Toast t = Toast.makeText(activity, message, time);
                t.show();
            } else {

                //Here's just a trick to use custom timed toast:
                //A toast is initialized as short timed(Toast.LENGTH_SHORT) and
                //a thread will show it for a given amount of time.

                //1) Calculate how many times the toast will be shown:
                //times is equal to the ratio between the given time in ms and
                //the time defined as Toast.LENGTH_SHORT into the
                //InotificationManager service.
                //2) Additionally the last show() method invokation will last
                //just for the last slice of time that fills the given time,
                //then the cancel() invokation will take place to eliminate the
                //toast

                //The following fields are all final as they must be used in the
                //implicit thread:

                //Total time to show the toast
                final int fullTimes = time / 1850;

                //Last toast must fill the total amount of given time and then 
                //be suppressed
                final int lastToastDuration = time % 1850;

                //Initialize the toast
                final Toast t = Toast.makeText(activity, message, Toast.LENGTH_SHORT);

                //The system must be able to attach the show() calls to the UI
                //Thread, so we declare a chained implicit Thread that will
                //repeatedly show the same toast object for the calculated times
                Thread thread = new Thread() {

                    public void run() {
                        int count = 0;
                        try {
                            while (count < fullTimes) {
                                t.show();
                                sleep(1850);
                                count++;
                            }
                            t.show();
                            sleep(lastToastDuration);
                            t.cancel();
                        } catch (Exception e) {
                            Log.error(TAG, "Cannot show the "
                                    + time + " custom timed toast because of "
                                    + e);
                        }
                    }
                };

                //The custom thread to show the toast starts here.
                thread.start();
            }
            lastMessage = message;
        }
    }

    /**
     * Container for the selection dialog built by the promptSelection method
     */
    public class PromptSelection implements Runnable {

        AlertDialog.Builder builder;
        int dialogId;

        public PromptSelection(AlertDialog.Builder builder, int dialogId) {
            this.builder = builder;
            this.dialogId = dialogId;

        }

        /**
         * Shows the alert and put it into the reference container pendingAlerts
         * with its id for further reference.
         */
        public void run() {
            Log.debug(TAG, "Showing progress dialog: " + dialogId);
            AlertDialog ad = builder.show();
            pendingAlerts.put(dialogId, ad);
        }
    }

    /**
     * To be implemented
     */
    public void toForeground() {
    }

    /**
     * To be implemented
     */
    public void toBackground() {
    }

    /**
     * To be implemented
     */
    public void loadBrowser(String url) {
    }

    /**
     * Listener for user click events. This can be referenced both as a click
     * listener for the DialogInterface and view. When the click event happens
     * this listener runs the related Runnable option.
     */
    private class OnButtonListener implements DialogInterface.OnClickListener, OnClickListener {

        private Runnable action;

        public OnButtonListener(Runnable action) {
            this.action = action;
        }

        public void onClick(DialogInterface dialog, int which) {
            if (action != null) {
                Thread t = new Thread(action);
                t.start();
            }
        }

        public void onClick(View arg0) {
            if (action != null) {
                Thread t = new Thread(action);
                t.start();
            }
        }
    }

    /**
     * Cancel listener for the Selection Alert Dialog. Not to be used for native
     * dialogs.
     */
    class SelectionCancelListener implements OnCancelListener {

        int id;

        public SelectionCancelListener(int id) {
            this.id = id;
        }

        public void onCancel(DialogInterface arg0) {
            dismissSelectionDialog(id);
            Runnable cancelAction = (Runnable) dismissRunnable.get(id);
            Log.debug(TAG, "Check an action is to be performed after dismiss");
            if (cancelAction != null) {
                Log.debug(TAG, "Action found - Starting action thread");
                (new Thread(cancelAction)).start();
                Log.debug(TAG, "Removing Action from dismissRunnable list");
                dismissRunnable.remove(id);
            }
        }
    }

    private LinearLayout buildAlertTitle(Activity a, String message, int fontSize) {
        LinearLayout titleLayout = new LinearLayout(a);
        titleLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        titleLayout.setGravity(Gravity.FILL_VERTICAL);
        TextView title = new TextView(a);
        title.setText(message);
        title.setPadding(0, adaptSizeToDensity(5, a),
                0, adaptSizeToDensity(10, a));
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setTextColor(Color.WHITE);
        title.setTextSize(fontSize);
        title.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        titleLayout.addView(title);
        return titleLayout;
    }

    private LinearLayout buildAlertContent(DialogOption[] options, Activity a, int dialogId) {
        LinearLayout builderView = new LinearLayout(a);
        builderView.setOrientation(LinearLayout.VERTICAL);
        //Leave commented here as it seems there is no way to simulate the
        //default alert. The background is just black
        //builderView.setBackgroundColor(Color.LTGRAY);
        builderView.setPadding(0, adaptSizeToDensity(5, a), 0, 0);
        builderView.setGravity(Gravity.FILL_VERTICAL);
        builderView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        Button[] viewButton = new Button[options.length];
        for (int i = 0; i < options.length; i++) {
            viewButton[i] = new Button(a);
            viewButton[i].setText(options[i].getDescription());
            OnButtonListener ol = new OnButtonListener(options[i]);
            viewButton[i].setOnClickListener(ol);
            builderView.addView(viewButton[i]);
            options[i].setDialogId(dialogId);
        }
        return builderView;
    }

    private int getNextDialogId() {
        return incrementalId++;
    }

    private int adaptSizeToDensity(int size, Context c) {
        return (int) (size * c.getResources().getDisplayMetrics().density);
    }
}
