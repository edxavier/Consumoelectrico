package com.nicrosoft.consumoelectrico.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.preference.*
import com.nicrosoft.consumoelectrico.R
import com.pixplicity.easyprefs.library.Prefs

/**
 * Created by Eder Xavier Rojas on 10/01/2017.
 */
class AppPreference : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        requireActivity().onBackPressedDispatcher.addCallback(this) { navController.navigateUp() }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        initSummaries(this.preferenceScreen)
        this.preferenceScreen.sharedPreferences
                .registerOnSharedPreferenceChangeListener(this)
    }

    /**
     * Set the summaries of all preferences
     */
    private fun initSummaries(pg: PreferenceGroup) {
        for (i in 0 until pg.preferenceCount) {
            val p = pg.getPreference(i)
            if (p is PreferenceGroup) initSummaries(p) // recursion
            else setSummary(p)
        }
    }

    /**
     * Set the summaries of the given preference
     */
    private fun setSummary(pref: Preference?) {
        // react on type or key
        pref?.let {
            if (pref is ListPreference) {
                pref.setSummary(pref.entry)
            } else if (pref is EditTextPreference) {
                val ps = Prefs.getString("price_simbol", "$")
                val ep = pref
                ep.summary = ep.text
                if (ep.key == "price_simbol") {
                    val pk: EditTextPreference? = findPreference<EditTextPreference>("price_kwh")
                    pk?.summary = ps + pk?.text
                } else if (ep.key == "price_kwh") {
                    ep.summary = ps + ep.text
                }
            }
        }

    }

    /**
     * used to change the summary of a preference
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val pref = findPreference<Preference>(key)
        setSummary(pref)
    }
}