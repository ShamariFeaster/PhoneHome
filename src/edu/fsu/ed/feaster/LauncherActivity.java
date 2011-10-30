
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        PreferenceManager.setDefaultValues(this, R.xml.preference, false);

        Button mSetPrefs = (Button) findViewById(R.id.button1);
        Button mCloserButton = (Button) findViewById(R.id.button2);

        mSetPrefs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                SharedPreferences preferences = getSharedPreferences("preferences",
                        MODE_WORLD_READABLE);
                SharedPreferences.Editor editor = preferences.edit();
                Log.d(TAG, "mSetPrefs Clicked");

                EditText EditTxt = (EditText) findViewById(R.id.editText1);
                EditText message_to_finder = (EditText) findViewById(R.id.message_to_finder_text);

                editor.putString("password", EditTxt.getText().toString());
                editor.putString("message", message_to_finder.getText().toString());

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
