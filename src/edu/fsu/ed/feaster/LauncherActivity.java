
package edu.fsu.ed.feaster;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LauncherActivity extends Activity {

    private static final String TAG = "LauncherActivity";
    private Button mSetPrefs;
    private Button mCloserButton;
    private EditText mPwEditText;
    private EditText mMsgEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        PreferenceManager.setDefaultValues(this, R.xml.preference, false);

        mSetPrefs = (Button) findViewById(R.id.button1);
        mCloserButton = (Button) findViewById(R.id.button2);
        mPwEditText = (EditText) findViewById(R.id.editText1);
        mMsgEditText = (EditText) findViewById(R.id.message_to_finder_text);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        mSetPrefs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                SharedPreferences preferences = getSharedPreferences("preferences",
                        MODE_WORLD_READABLE);
                SharedPreferences.Editor editor = preferences.edit();
                Log.d(TAG, "mSetPrefs Clicked");

                editor.putString("password", mPwEditText.getText().toString());
                editor.putString("message", mMsgEditText.getText().toString());

                editor.commit();
                Log.d(TAG, "Preferences Saved");

            }

        });

        mCloserButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.d(TAG, "mCloserButton Clicked");
                stopService(new Intent(getApplicationContext(), SMSLocatorService.class));
                Log.d(TAG, "StopService Intent to SMSLocatorService");
            }
        });
    }
}
