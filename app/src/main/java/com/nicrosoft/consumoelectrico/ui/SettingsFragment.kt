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
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : ScopeFragment() {

    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
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

        container_currency.setOnClickListener {
            val title = getString(R.string.settings_ac_currency)
            val defValue = Prefs.getString("price_simbol", "C$")
            showDialog(title, "price_simbol", defValue, typeText)
        }
        container_price.setOnClickListener {
            val title = getString(R.string.settings_ac_price_kwh)
            val defValue = Prefs.getString("price_kwh", "6")
            showDialog(title, "price_kwh", defValue, typeNumber)
        }
        container_discount.setOnClickListener {
            val title = getString(R.string.settings_ac_discount_kwh)
            val defValue = Prefs.getString("discount_kwh", "2")
            showDialog(title, "discount_kwh", defValue, typeNumber)
        }
        container_fixed_charges.setOnClickListener {
            val title = getString(R.string.settings_ac_fixed_charges)
            val defValue = Prefs.getString("fixed_charges", "40")
            showDialog(title, "fixed_charges", defValue, typeNumber)
        }
        container_limit.setOnClickListener {
            val title = getString(R.string.settings_ac_period_limit)
            val defValue = Prefs.getString("kw_limit", "150")
            showDialog(title, "kw_limit", defValue, typeNumber)
        }
        container_period.setOnClickListener {
            val title = getString(R.string.settings_ac_period)
            val defValue = Prefs.getString("period_lenght", "30")
            showDialog(title, "period_lenght", defValue, typeNumber)
        }
        container_reminder.setOnClickListener {
            val title = getString(R.string.settings_ac_reminder_afer)
            val defValue = Prefs.getString("reminder_after", "14")
            showDialog(title, "reminder_after", defValue, typeNumber)
        }

    }

    private fun showDialog(title:String, setCode:String, default:String, inputType: Int){
        MaterialDialog(requireActivity()).show {
            title(text = title)
            input(prefill = default, inputType = inputType){ dialog, text ->
                Prefs.putString(setCode, text.toString())
            }
            positiveButton(R.string.ok)
        }
    }
}
