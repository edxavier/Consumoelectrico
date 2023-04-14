package com.nicrosoft.consumoelectrico.ui

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.ScopeFragment
import com.nicrosoft.consumoelectrico.databinding.FragmentSettingsBinding
import com.pixplicity.easyprefs.library.Prefs

class SettingsFragment : ScopeFragment() {

    private lateinit var navController: NavController
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        requireActivity().onBackPressedDispatcher.addCallback(this) { navController.navigateUp() }
        initLayout()
    }

    private fun initLayout() {
        val typeText = InputType.TYPE_CLASS_TEXT
        val typeNumber = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        binding.containerCurrency.setOnClickListener {
            val title = getString(R.string.settings_ac_currency)
            val defValue = Prefs.getString("price_simbol", "C$")
            showDialog(title, "price_simbol", defValue, typeText)
        }

        binding.backupReminder.setOnCheckedChangeListener { _, isChecked ->
            Prefs.putBoolean("backup_reminder_enabled", isChecked)
        }
    }

    private fun showDialog(title:String, setCode:String, default:String, inputType: Int){
        MaterialDialog(requireActivity()).show {
            title(text = title)
            input(prefill = default, inputType = inputType){ _, text ->
                Prefs.putString(setCode, text.toString())
            }
            positiveButton(R.string.ok)
        }
    }
}
