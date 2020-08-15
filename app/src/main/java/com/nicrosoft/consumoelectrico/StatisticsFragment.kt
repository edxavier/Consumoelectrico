package com.nicrosoft.consumoelectrico

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.nicrosoft.consumoelectrico.databinding.FragmentStatisticsBinding
import com.nicrosoft.consumoelectrico.ui2.ElectricVMFactory
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel
import com.nicrosoft.consumoelectrico.utils.*
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class StatisticsFragment : ScopeFragment(), KodeinAware {
    override val kodein by kodein()
    private val vmFactory by instance<ElectricVMFactory>()
    private lateinit var viewModel: ElectricViewModel
    private lateinit var navController: NavController
    private lateinit var bind: FragmentStatisticsBinding


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bind = DataBindingUtil.inflate(inflater, R.layout.fragment_statistics, container, false)
        //return inflater.inflate(R.layout.fragment_statistics, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), vmFactory).get(ElectricViewModel::class.java)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        loadDetailData()
        loadChartsData()
    }

    @SuppressLint("SetTextI18n")
    private fun loadDetailData() {
        val meter = viewModel.meter.value!!
        bind.detailMeterName.text = meter.name
        bind.detailMeterDesc.text = meter.description
        launch {
            val period = viewModel.getLastPeriod(meter.code)
            period?.let { p ->
                val lastReading = viewModel.getLastPeriodReading(p.code)
                lastReading?.let { lr ->
                    bind.detailLastReading.text = "${lr.readingValue.toTwoDecimalPlace()} kWh"
                    bind.detailLastReadingDate.text = lr.readingDate.formatDate(requireContext())
                    bind.detailPeriodDailyAvgConsumption.text = "${(lr.kwAvgConsumption * 24).toTwoDecimalPlace()} kWh"
                    if(lr.consumptionHours / 24 < meter.periodLength)
                        bind.detailPeriodEstimatedConsumption.text = "${lr.getConsumptionProjection(meter).toTwoDecimalPlace()} kWh"
                    else
                        bind.detailPeriodEstimatedConsumption.text = "${p.totalKw.toTwoDecimalPlace()} kWh"
                    bind.detailPeriodDaysConsumed.text = "${(lr.consumptionHours/24).toTwoDecimalPlace()}/${meter.periodLength}"
                    val periodExp = viewModel.calculateEnergyCosts(p.totalKw, meter)
                    bind.detailPeriodExpenses.text = "C$${periodExp.total.toTwoDecimalPlace()}"
                    //Si aun no hemos revasado la duracion periodo estimar el consumo an finalizarlo
                    val dExpenses = if(lr.consumptionHours / 24 < meter.periodLength) {
                        val estimatedExp = viewModel.calculateEnergyCosts(lr.getConsumptionProjection(meter), meter)
                        bind.detailPeriodEstimatedExpenses.text = "C$${estimatedExp.total.toTwoDecimalPlace()}"
                        estimatedExp
                    }else {
                        bind.detailPeriodEstimatedExpenses.text = bind.detailPeriodExpenses.text
                        periodExp
                    }
                    bind.detailEnergyExp.text = "C$${dExpenses.energy.toTwoDecimalPlace()}"
                    bind.detailDiscounts.text = "C$${dExpenses.discount.toTwoDecimalPlace()}"
                    bind.detailTaxes.text = "C$${dExpenses.taxes.toTwoDecimalPlace()}"
                    bind.detailFixedExp.text = "C$${dExpenses.fixed.toTwoDecimalPlace()}"
                    bind.detailTotal.text = "C$${dExpenses.total.toTwoDecimalPlace()}"
                }
                //val firstReading = viewModel.getFirstPeriodReading(p.code)
                val periods = viewModel.getMeterAllPeriods(meter.code)
                val firstReading = if(periods.size>1)
                    viewModel.getLastPeriodReading(periods[1].code)
                else
                    viewModel.getFirstPeriodReading(p.code)

                firstReading?.let { fr ->
                    bind.detailInitialReading.text = "${fr.readingValue.toTwoDecimalPlace()} kWh"
                    bind.detailInitialReadingDate.text = fr.readingDate.formatDate(requireContext())
                }
                bind.detailPeriodConsumption.text = "${p.totalKw.toTwoDecimalPlace()} kWh"
            }
        }
    }

    private fun loadChartsData() {
        launch {
            agg_consumption_chart.setupLineChartStyle(HoursValueFormatter(), MyMarkerView(requireContext(), R.layout.marker))
            avg_consumption_chart.setupLineChartStyle(HoursValueFormatter(), MyMarkerView(requireContext(), R.layout.marker))
            cost_vs_day_chart.setupLineChartStyle(HoursValueFormatter(), CostVsDayMarkerView(requireContext(), R.layout.marker))
            cost_vs_kwh_chart.setupLineChartStyle(KwValueFormatter(), CostVsKwMarkerView(requireContext(), R.layout.marker))
            periods_chart.setupBarChartStyle(viewModel.getPeriodDataLabels())

            agg_consumption_chart.drawLimit(viewModel.meter.value!!.maxKwLimit.toFloat())
            avg_consumption_chart.drawLimit((viewModel.meter.value!!.maxKwLimit/viewModel.meter.value!!.periodLength).toFloat())

            val period = viewModel.getLastPeriod(viewModel.meter.value!!.code)
            period?.let {
                val lineData = viewModel.getLineChartData(period)
                agg_consumption_chart.data = lineData.consumptionDs
                avg_consumption_chart.data = lineData.dailyAvgDs
                cost_vs_day_chart.data = lineData.costPerDayDs
                cost_vs_kwh_chart.data = lineData.costPerKwDs
                periods_chart.data = lineData.periodDs

            }
        }
    }



}