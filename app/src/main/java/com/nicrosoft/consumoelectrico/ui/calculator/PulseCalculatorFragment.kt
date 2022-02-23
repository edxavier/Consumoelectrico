package com.nicrosoft.consumoelectrico.ui.calculator

import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.nicrosoft.consumoelectrico.R
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.dlg_pulses_per_kwh.*
import kotlinx.android.synthetic.main.fragment_real_time_consumption.*
import java.util.*

class PulseCalculatorFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var pulses_per_kwh: EditText
    private lateinit var slideshowViewModel: SlideshowViewModel
    var playing = false
    var pulses = 0f

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        slideshowViewModel =
                ViewModelProviders.of(this).get(SlideshowViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_real_time_consumption, container, false)
        setHasOptionsMenu(true)
        //val textView: TextView = root.findViewById(R.id.text_slideshow)
        //slideshowViewModel.text.observe(viewLifecycleOwner, Observer {
          //  textView.text = it
        //})
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(activity!!, R.id.nav_host_fragment)
        requireActivity().onBackPressedDispatcher.addCallback(this) { navController.navigateUp() }

        timer_area.setOnClickListener {
            if (!playing) {
                playing = true
                txt_resume.text = ""
                txt_consumption.text = "00 kWh"
                pulses = 0f
                chronometer.base = SystemClock.elapsedRealtime()
                chronometer.start()
                play_icon.setImageResource(R.drawable.ic_stop)
                consumption_area.alpha = 1f
                consumption_icon.setImageResource(R.drawable.ic_pulse_line)
                txt_hint.visibility = View.VISIBLE
            } else {
                txt_hint.visibility = View.GONE
                calculateConsumption()
                consumption_area.alpha = 0.8f
                playing = false
                chronometer.stop()
                play_icon.setImageResource(R.drawable.ic_play_arrow)
                consumption_icon.setImageResource(R.drawable.ic_gauge)
            }
        }
        consumption_area.setOnClickListener {
            if (playing)
                calculateConsumption()
            else
                consumption_area.alpha = 0.7f
        }
        if (Prefs.getBoolean("firstTimeCalculator", true)) showExplanation()
    }
    private fun calculateConsumption() {
        pulses += 1f
        val pulses_per_kwh = Prefs.getString("pulses_per_kwh", "1600").toFloat()
        val timeElapsed = (SystemClock.elapsedRealtime() - chronometer.base) / 1000.toFloat()
        val P = 3600f / pulses_per_kwh * (pulses / timeElapsed)
        txt_consumption.text = String.format(Locale.getDefault(), "%02.2f kWh", P)
        consumption_area.alpha = 1f
        val dayly_kwh = P * 24
        /*float discount_kwh = Float.parseFloat(Prefs.getString("discount_kwh", "0"));
        float price_kwh = Float.parseFloat(Prefs.getString("price_kwh", "1"));
        float fixed_charges = Float.parseFloat(Prefs.getString("fixed_charges", "0"));
        float dayly_expenses = (dayly_kwh * (price_kwh - discount_kwh)) + fixed_charges;
        String dayly_expenses_str = Prefs.getString("price_simbol", "$") + String.valueOf(dayly_expenses);*/
        val r = getString(R.string.calculator_resume, String.format(Locale.getDefault(), "%02.2f", dayly_kwh))
        txt_resume.text = r
    }


    fun showExplanation() {
        MaterialDialog(activity!!).show {
            title(R.string.notice)
            message(R.string.calculator_explanation)
            negativeButton(R.string.not_show_again){
                Prefs.putBoolean("firstTimeCalculator", false)
            }
            positiveButton(R.string.ok)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.calculator_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_pulses_set) {
            val dlg = MaterialDialog(context!!, BottomSheet()).show {
                title(R.string.calculator_title)
                positiveButton (R.string.save){
                    val pulsesPerKwh = txt_pulses_kwh.text.toString().trim()
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
                pulses_per_kwh = dlg.getCustomView().findViewById(R.id.txt_pulses_kwh)
                pulses_per_kwh.setText(Prefs.getString("pulses_per_kwh", "1600"))
            } catch (e:Exception) { }
        }
        if (item.itemId == R.id.ac_calculator_help)
            showExplanation()

        return super.onOptionsItemSelected(item)
    }

}
