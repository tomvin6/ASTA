package com.example.astaScreens;
import com.example.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * settings activity - enable user to change settings.
 * setting values are in XML below.
 * for adding a new preference, edit "preference.xml" file.
 */
public class SettingsActivity extends PreferenceActivity {
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}