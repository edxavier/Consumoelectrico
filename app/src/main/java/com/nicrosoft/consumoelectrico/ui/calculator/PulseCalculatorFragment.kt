package com.nicrosoft.consumoelectrico.ui.calculator

import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.databinding.FragmentRealTimeConsumptionBinding
import com.pixplicity.easyprefs.library.Prefs
import java.util.*

class PulseCalculatorFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentRealTimeConsumptionBinding
    private lateinit var navController: NavController
    private lateinit var txtPulsesPerKwh: EditText
    private var playing = false
    private var pulses = 0f

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentRealTimeConsumptionBinding.inflate(inflater)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        requireActivity().onBackPressedDispatcher.addCallback(this) { navController.navigateUp() }

        binding.timerArea.setOnClickListener {
            if (!playing) {
                playing = true
                binding.txtResume.text = ""
                pulses = 0f
                binding.chronometer.base = SystemClock.elapsedRealtime()
                binding.chronometer.start()
                binding.playIcon.setImageResource(R.drawable.ic_stop)
                binding.consumptionArea.alpha = 1f
                binding.consumptionIcon.setImageResource(R.drawable.ic_pulse_line)
                binding.txtHint.visibility = View.VISIBLE
            } else {
                binding.txtHint.visibility = View.GONE
                calculateConsumption()
                binding.consumptionArea.alpha = 0.9f
                playing = false
                binding.chronometer.stop()
                binding.playIcon.setImageResource(R.drawable.ic_play_arrow)
                binding.consumptionIcon.setImageResource(R.drawable.ic_gauge)
            }
        }
        binding.consumptionArea.setOnClickListener {
            if (playing)
                calculateConsumption()
            else
                binding.consumptionArea.alpha = 0.9f
        }
        if (Prefs.getBoolean("firstTimeCalculator", true)) showExplanation()
    }
    private fun calculateConsumption() {
        pulses += 1f
        val pulsesPerKwh = Prefs.getString("pulses_per_kwh", "1600").toFloat()
        val timeElapsed = (SystemClock.elapsedRealtime() - binding.chronometer.base) / 1000.toFloat()
        val p = 3600f / pulsesPerKwh * (pulses / timeElapsed)
        val dailyKwh = p * 24
        binding.txtConsumption.text = String.format(Locale.getDefault(), "%02.2f kWh", dailyKwh).padStart(9,'0')
        binding.consumptionArea.alpha = 1f

        val hourlyConsumption = String.format(Locale.getDefault(), "%02.2f", p).padStart(5,'0')
        val r = getString(R.string.calculator_resume, hourlyConsumption)
        binding.txtResume.text = r

    }


    private fun showExplanation() {
        MaterialDialog(requireActivity()).show {
            title(R.string.notice)
            message(R.string.calculator_explanation)
            negativeButton(R.string.not_show_again){
                Prefs.putBoolean("firstTimeCalculator", false)
            }
            positiveButton(R.string.ok)
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.calculator_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.action_pulses_set -> {
                val dlg = MaterialDialog(requireContext(), BottomSheet()).show {
                    title(R.string.calculator_title)
                    positiveButton (R.string.save){
                        val pulsesPerKwh = txtPulsesPerKwh.text.toString().trim()
                        if (pulsesPerKwh.isNotEmpty())
                            Prefs.putString("pulses_per_kwh", pulsesPerKwh)
                        else
                            Toast.makeText(activity, getString(R.string.activity_new_reading_snack_warning),
                                Toast.LENGTH_LONG).show()
                    }
                    negativeButton (R.string.cancel){dismiss()}
                    customView(R.layout.dlg_pulses_per_kwh, scrollable = true, horizontalPadding = true)
                }
                try {
                    txtPulsesPerKwh = dlg.getCustomView().findViewById(R.id.txt_pulses_kwh)
                    txtPulsesPerKwh.setText(Prefs.getString("pulses_per_kwh", "1600"))
                } catch (_:Exception) { }
                true
            }
            R.id.ac_calculator_help -> {
                showExplanation()
                true
            }
            else -> false
        }
    }

}
