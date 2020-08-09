package com.nicrosoft.consumoelectrico

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.nicrosoft.consumoelectrico.databinding.FragmentStatisticsBinding
import com.nicrosoft.consumoelectrico.ui2.ElectricVMFactory
import com.nicrosoft.consumoelectrico.ui2.ElectricViewModel
import com.nicrosoft.consumoelectrico.utils.calculateExpenses
import com.nicrosoft.consumoelectrico.utils.estimateConsumption
import com.nicrosoft.consumoelectrico.utils.formatDate
import com.nicrosoft.consumoelectrico.utils.toTwoDecimalPlace
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
        loadData()
    }

    @SuppressLint("SetTextI18n")
    private fun loadData() {
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
                        bind.detailPeriodEstimatedConsumption.text = "${lr.estimateConsumption(meter).toTwoDecimalPlace()} kWh"
                    else
                        bind.detailPeriodEstimatedConsumption.text = "${lr.readingValue.toTwoDecimalPlace()} kWh"
                    bind.detailPeriodDaysConsumed.text = "${(lr.consumptionHours/24).toTwoDecimalPlace()}/${meter.periodLength}"
                    bind.detailPeriodExpenses.text = "C$${p.totalKw.calculateExpenses(meter).total.toTwoDecimalPlace()}"
                    val dExpenses = if(lr.consumptionHours / 24 < meter.periodLength) {
                        val detailExp = lr.estimateConsumption(meter).calculateExpenses(meter)
                        bind.detailPeriodEstimatedExpenses.text = "C$${detailExp.total.toTwoDecimalPlace()}"
                        detailExp
                    }else {
                        bind.detailPeriodEstimatedExpenses.text = bind.detailPeriodExpenses.text
                        p.totalKw.calculateExpenses(meter)
                    }
                    bind.detailEnergyExp.text = "C$${dExpenses.energy.toTwoDecimalPlace()}"
                    bind.detailDiscounts.text = "C$${dExpenses.discount.toTwoDecimalPlace()}"
                    bind.detailTaxes.text = "C$${dExpenses.taxes.toTwoDecimalPlace()}"
                    bind.detailFixedExp.text = "C$${dExpenses.fixed.toTwoDecimalPlace()}"
                    bind.detailTotal.text = "C$${dExpenses.total.toTwoDecimalPlace()}"
                }
                val firstReading = viewModel.getFirstPeriodReading(p.code)
                firstReading?.let { fr ->
                    bind.detailInitialReading.text = "${fr.readingValue.toTwoDecimalPlace()} kWh"
                    bind.detailInitialReadingDate.text = fr.readingDate.formatDate(requireContext())
                }
                bind.detailPeriodConsumption.text = "${p.totalKw.toTwoDecimalPlace()} kWh"
            }
        }
    }
}