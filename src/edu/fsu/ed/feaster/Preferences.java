package edu.fsu.ed.feaster;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Preferences extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference);
    }
}
