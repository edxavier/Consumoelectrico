package com.nicrosoft.consumoelectrico.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import androidx.annotation.NonNull;

import com.nicrosoft.consumoelectrico.R;
import com.pixplicity.easyprefs.library.Prefs;

/**
 * Created by Eder Xavier Rojas on 10/01/2017.
 */

public class AppPreference extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        this.initSummaries(this.getPreferenceScreen());

        this.getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }


    /**
     * Set the summaries of all preferences
     */
    private void initSummaries(@NonNull PreferenceGroup pg) {
        for (int i = 0; i < pg.getPreferenceCount(); ++i) {
            Preference p = pg.getPreference(i);
            if (p instanceof PreferenceGroup)
                this.initSummaries((PreferenceGroup) p); // recursion
            else
                this.setSummary(p);
        }
    }

    /**
     * Set the summaries of the given preference
     */
    private void setSummary(Preference pref) {
        // react on type or key
        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }else if(pref instanceof EditTextPreference){
            String ps = Prefs.getString("price_simbol", "$");
            EditTextPreference ep = (EditTextPreference) pref;
            ep.setSummary(ep.getText());
            if(ep.getKey().equals("price_simbol")){
                EditTextPreference pk = (EditTextPreference) findPreference("price_kwh");
                pk.setSummary(ps + pk.getText());
            }else if(ep.getKey().equals("price_kwh")){
                ep.setSummary(ps + ep.getText());
            }
        }
    }

    /**
     * used to change the summary of a preference
     */

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        this.setSummary(pref);
    }
}
