package com.raytracer.uniwuemensa;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.FragmentActivity;

import com.example.uniwuemensa.R;

public class SettingsActivity extends FragmentActivity {
    public static final String KEY_PREF_PRICE_TYPE = "pref_priceType";
    public static final String KEY_PREF_HUBLAND = "pref_hubland";
    public static final String KEY_PREF_FRANKENSTUBE = "pref_frankenstube";
    public static final String KEY_PREF_MENSATERIA = "pref_mensateria";


    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
