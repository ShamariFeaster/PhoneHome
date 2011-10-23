package edu.fsu.ed.feaster;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class LauncherActivity extends Activity {
	
	//SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
	
	private static final String TAG = "LauncherActivity";
	//Editor mPrefEditor;
	//Boolean mImageFlag = true;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);

    	//RadioGroup mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
    	Button mSetPrefs = (Button) findViewById(R.id.button1); 
    	Button mCloserButton = (Button) findViewById(R.id.button2);
/*
    	mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
    	      public void onCheckedChanged(RadioGroup rg, int id) {
    	    	  switch (id) {
    	    	  		case R.id.radio0:
    	    	  			mImageFlag = true;
    	    	  			break;
    	    	  		case R.id.radio1:
    	    	  			mImageFlag = false;
    	    	  			break;
    	    	  }
    	      }
    	        
    	      });
*/
    	mSetPrefs.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
    			
    			SharedPreferences preferences = getSharedPreferences("preferences", 3);
    			SharedPreferences.Editor editor = preferences.edit();
    			Log.d(TAG, "mSetPrefs Clicked");
    			
    			//SharedPreferences Settings;
    			EditText EditTxt = (EditText) findViewById(R.id.editText1);
    			EditText message_to_finder = (EditText) findViewById(R.id.message_to_finder_text);
    			
    			//Settings = getSharedPreferences(SHARED_PREF_NAME, 2);
    			
    			editor.putString("password", EditTxt.getText().toString());
    			editor.putString("message", message_to_finder.getText().toString());
    			
    			//mPrefEditor = Settings.edit();  
    			//mPrefEditor.putString("key", EditTxt.getText().toString());  
    			//mPrefEditor.putString("msg", message_to_finder.getText().toString()); 
    			//mPrefEditor.putBoolean("show_image", mImageFlag);  
    			//mPrefEditor.commit(); 
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
