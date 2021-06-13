package com.nicrosoft.consumoelectrico.ui

import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.ScopeFragment
import com.nicrosoft.consumoelectrico.activities.perio_details.contracts.PeriodDetailPresenterImpl
import com.nicrosoft.consumoelectrico.activities.perio_details.contracts.PeriodDetailsPresenter
import com.nicrosoft.consumoelectrico.fragments.main.chart_helpers.ChartStyler
import com.nicrosoft.consumoelectrico.utils.formatDate
import com.nicrosoft.consumoelectrico.utils.setHidden
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.fragment_detalles.*
import java.util.*

class DetallesFragment : ScopeFragment() {
    private lateinit var presenter: PeriodDetailsPresenter
    private lateinit var params: DetallesFragmentArgs
    private lateinit var navController: NavController
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detalles, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!Prefs.getBoolean("isPurchased", false))
            requestInterstialAds()
        setHasOptionsMenu(true)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        requireActivity().onBackPressedDispatcher.addCallback(this) { navController.navigateUp() }

        params = DetallesFragmentArgs.fromBundle(requireArguments())
        presenter = PeriodDetailPresenterImpl(context)
        presenter.onCreate()
        setupBillingPeriodDetails(params.id)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.details_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_readings ->{
                val action = DetallesFragmentDirections.actionDetallesFragmentToListaLecturasFragment(params.id, params.name)
                navController.navigate(action)
            }
            R.id.action_new_readings ->{
                val action = DetallesFragmentDirections.actionDetallesFragmentToNavNuevaLectura(params.id, params.name)
                navController.navigate(action)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setupBillingPeriodDetails(medidor_id: String) {
        val period = presenter.getActivePeriod(medidor_id)
        val ultimaLectura = presenter.getLastReading(period)
        val primerLectura = presenter.getFirstReading(period)
        var chartVar = ChartStyler.setup(chart, requireContext(), false) as LineChart
        var chart2Var = ChartStyler.setup(chart2, requireContext(), true) as LineChart
        var chart3Var = ChartStyler.setup(chart3, requireContext(), false) as BarChart

        try {
            chartVar = presenter.setReadingHistory(chartVar, period)
            chartVar.invalidate()
        } catch (ignored: Exception) {
        }
        try {
            chart2Var = presenter.setAvgHistory(chart2Var, period)
            chart2Var.invalidate()
        } catch (ignored: Exception) {
        }
        try {
            chart3Var = presenter.setPeriodHistory(chart3Var, medidor_id)
            chart3Var.invalidate()
        } catch (ignored: Exception) {
        }
        try {
            if (params.desc.isNotEmpty()) description.text = params.desc else card_desc.setHidden()

            txt_beginning_period.text = period.inicio.formatDate(requireContext())
            txt_initial_reading.text = getString(R.string.initial_reading_val, String.format(Locale.getDefault(), "%02.0f", primerLectura.lectura))
            txt_last_reading.text = getString(R.string.initial_reading_val, String.format(Locale.getDefault(), "%02.0f", ultimaLectura.lectura))
            txt_current_consumption.text = getString(R.string.initial_reading_val, String.format(Locale.getDefault(), "%02.0f", ultimaLectura.consumo_acumulado))
            txt_days_consumed.text = getString(R.string.days_consumed_val, ultimaLectura.dias_periodo)
            txt_period_len.text = getString(R.string.days_consumed_val, presenter.periodLength.toFloat())
            txt_last_reading_date.text = (ultimaLectura.fecha_lectura.formatDate(requireContext()))
            txt_avg_consumption.text = getString(R.string.avg_consumption_val, String.format(Locale.getDefault(), "%02.1f", ultimaLectura.consumo_promedio))
            txt_estimate_consumptio_kwh.text = getString(R.string.initial_reading_val, String.format(Locale.getDefault(), "%02.1f", presenter.getEstimatedConsumption(ultimaLectura)))
            txt_estimate_expense.text = getString(R.string.estimated_expense_val,
                    Prefs.getString("price_simbol", "$"), String.format(Locale.getDefault(), "%02.1f", presenter.getEstimatedExpense(ultimaLectura)))
            txt_estimate_expense_no_discount.text = getString(R.string.estimated_unsubsidized_expense_val,
                    Prefs.getString("price_simbol", "$"), String.format(Locale.getDefault(), "%02.1f", presenter.getEstimatedExpenseWithNoDiscount(ultimaLectura)))
        } catch (ignored: Exception) {
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
        val ne = Prefs.getInt("num_show_readings", 0)
        Prefs.putInt("num_show_readings", ne + 1)
        if (Prefs.getInt("num_show_readings", 0) == Prefs.getInt("show_after", 5)) {
            Prefs.putInt("num_show_readings", 0)
            val r = Random()
            val Low = 7
            val High = 12
            val rnd = r.nextInt(High - Low) + Low
            Prefs.putInt("show_after", rnd)
            mInterstitialAd?.show(requireActivity())
        }

    }

    private fun requestInterstialAds() {
        val adUnitId = resources.getString(R.string.admob_interstical)
        InterstitialAd.load(requireActivity(), adUnitId, AdRequest.Builder().build(), object:
            InterstitialAdLoadCallback(){
            override fun onAdLoaded(p0: InterstitialAd) {
                super.onAdLoaded(p0)
                mInterstitialAd = p0
            }
        })
    }
}
