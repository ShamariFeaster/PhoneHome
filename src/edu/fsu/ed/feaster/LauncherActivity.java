
package edu.fsu.ed.feaster;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LauncherActivity extends Activity {

    private static final String TAG = "LauncherActivity";

    private final int PASSWORD_DIALOG = 0;

    private Button mSetPrefs;

    private Button mCloserButton;

    private EditText mPwEditText;

    private EditText mMsgEditText;

    private EditText mEnterPassword;

    private Boolean mPwEnterCorrectly = false;

    private String mFirstExectuion;

    SharedPreferences mPreferences;

    SharedPreferences.Editor mEditor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mPreferences = getSharedPreferences("preferences", MODE_WORLD_READABLE);
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);
        mEditor = mPreferences.edit();

        mSetPrefs = (Button)findViewById(R.id.button1);
        mCloserButton = (Button)findViewById(R.id.button2);
        mPwEditText = (EditText)findViewById(R.id.editText1);
        mMsgEditText = (EditText)findViewById(R.id.message_to_finder_text);

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        super.onCreateDialog(id);
        switch (id) {

            case PASSWORD_DIALOG:
                LayoutInflater factory = LayoutInflater.from(this);
                final View textEntryView = factory.inflate(R.layout.password_dialog_layout, null);
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.alert_dialog_text_entry)
                        .setView(textEntryView)
                        .setPositiveButton(R.string.alert_dialog_ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        mPwEnterCorrectly = false;
                                        EditText EnterPassword = (EditText)textEntryView
                                                .findViewById(R.id.passwrodText);
                                        final String current_pw = mPreferences.getString(
                                                "password", "default_password");
                                        if (!EnterPassword.getText().toString().equals(current_pw)) {
                                            Log.d("in edittext", ""
                                                    + EnterPassword.getText().toString().length());
                                            dialog.cancel();
                                            SMSLocatorService.sShowToast(getApplicationContext(),
                                                    "Incorrect Password");
                                        } else {
                                            mPwEnterCorrectly = true;
                                            SMSLocatorService.sShowToast(getApplicationContext(),
                                                    "Correct Password");
                                        }
                                        EnterPassword.setText("");
                                    }
                                }).create();
            default:
                return null;
        }

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        mMsgEditText.requestFocus();

        final String current_password = mPreferences.getString("password", "default_password");
        Log.d("curent password", current_password + " " + current_password.length());
        /*
         * mPwEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
         * public void onFocusChange(View v, boolean hasFocus) { if
         * (v.hasFocus() && !current_password.equals("default_password")) {
         * showDialog(PASSWORD_DIALOG); } } });
         * mMsgEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
         * public void onFocusChange(View v, boolean hasFocus) { if
         * (v.hasFocus() && !current_password.equals("default_password")) {
         * showDialog(PASSWORD_DIALOG); } } });
         */
        mSetPrefs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mFirstExectuion = LauncherActivity.this.mPreferences.getString("first_execution",
                        "false");
                if (mFirstExectuion.equals("false") && !mPwEnterCorrectly) {
                    showDialog(PASSWORD_DIALOG);
                    Log.d(TAG, "Password dialog did hang exec!");
                } else {
                    LauncherActivity.this.mEditor.putString("password", mPwEditText.getText()
                            .toString());
                    LauncherActivity.this.mEditor.putString("message", mMsgEditText.getText()
                            .toString());

                    // Log.d(TAG, mPwEditText.getText().toString() + " "
                    // + mPwEditText.getText().toString().length());
                    SMSLocatorService.sShowToast(getApplicationContext(), "Preferences Saved");
                    LauncherActivity.this.mEditor.putString("first_execution", "false");
                    LauncherActivity.this.mEditor.commit();
                }

            }

        });

        mCloserButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.d(TAG, "mCloserButton Clicked");
                if (!mPwEnterCorrectly) {
                    showDialog(PASSWORD_DIALOG);
                    Log.d(TAG, "Password dialog did hang exec!");
                } else {
                    if (stopService(new Intent(getApplicationContext(), SMSLocatorService.class)))
                        SMSLocatorService.sShowToast(getApplicationContext(),
                                "The Phone Home Service Has Been Stopped");
                }
                Log.d(TAG, "StopService Intent to SMSLocatorService");
            }
        });
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mPwEditText.setText("");
        mPwEnterCorrectly = false;
    }
}
