package com.example.nick.popularmovies;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;


/**
 * Settings Fragment to choose sort order
 *
 * Can probably remove ShardPreference Changed Class
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    final String LOG_TAG = SettingsFragment.class.getSimpleName();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        addPreferencesFromResource(R.xml.pref_general);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    /**
     * Settings automatically update in shared preferences so this is just for echoing
     *
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String value = sharedPreferences.getString(key, "not set");
        Log.v(LOG_TAG, "what changed key:"+key+"value"+value);

    }
}
