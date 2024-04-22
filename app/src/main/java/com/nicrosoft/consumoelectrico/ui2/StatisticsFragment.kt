package com.nicrosoft.consumoelectrico.ui2

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.layout.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.databinding.AdNativeLayoutBinding
import com.nicrosoft.consumoelectrico.databinding.FragmentStatisticsBinding
import com.nicrosoft.consumoelectrico.utils.*
import com.nicrosoft.consumoelectrico.utils.charts.*
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class StatisticsFragment : Fragment(), DIAware {
    override val di by closestDI()
    private val vmFactory by instance<ElectricVMFactory>()
    private lateinit var viewModel: ElectricViewModel
    private lateinit var navController: NavController
    private lateinit var binding: FragmentStatisticsBinding
    private var coinSymbol = ""

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStatisticsBinding.inflate(layoutInflater)
        //return inflater.inflate(R.layout.fragment_statistics, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), vmFactory)[ElectricViewModel::class.java]
        navController = findNavController()
        coinSymbol = Prefs.getString("price_simbol", "$")
        loadDetailData()
        loadChartsData()
        loadNativeAd()

    }

    @SuppressLint("SetTextI18n")
    private fun loadDetailData() {
        try {
            val meter = viewModel.meter
            binding.detailMeterName.text = meter.name
            binding.detailMeterDesc.text = meter.description
            lifecycleScope.launch {
                try {
                    var period = viewModel.getMeterLatestPeriod(meter.code)
                    period = viewModel.updatePeriodTotals(period!!)
                    period.let { p ->
                        val lastReading = viewModel.getLastPeriodReading(p.code)
                        binding.detailPeriodConsumption.text = "${p.totalKw.toTwoDecimalPlace()} kWh"
                        lastReading?.let { lr ->
                            binding.detailLastReading.text = "${lr.readingValue.toTwoDecimalPlace()} kWh"
                            binding.detailLastReadingDate.text = lr.readingDate.formatDate(requireContext())
                            binding.detailPeriodDailyAvgConsumption.text = "${(lr.kwAvgConsumption * 24).toTwoDecimalPlace()} kWh"
                            if (lr.consumptionHours / 24 < meter.periodLength)
                                binding.detailPeriodEstimatedConsumption.text = "${lr.getConsumptionProjection(meter).toTwoDecimalPlace()} kWh"
                            else
                                binding.detailPeriodEstimatedConsumption.text = "${p.totalKw.toTwoDecimalPlace()} kWh"
                            binding.detailPeriodDaysConsumed.text = "${(lr.consumptionHours / 24).toTwoDecimalPlace()}/${meter.periodLength}"
                            val periodExp = viewModel.calculateEnergyCosts(p.totalKw, meter)
                            binding.detailPeriodExpenses.text = "$coinSymbol${periodExp.total.toTwoDecimalPlace()}"
                            //Si aun no hemos revasado la duracion periodo estimar el consumo an finalizarlo
                            val dExpenses = if (lr.consumptionHours / 24 < meter.periodLength) {
                                val estimatedExp = viewModel.calculateEnergyCosts(lr.getConsumptionProjection(meter), meter)
                                binding.detailPeriodEstimatedExpenses.text = "$coinSymbol${estimatedExp.total.toTwoDecimalPlace()}"
                                estimatedExp
                            } else {
                                binding.detailPeriodEstimatedExpenses.text = binding.detailPeriodExpenses.text
                                periodExp
                            }
                            binding.detailEnergyExp.text = "$coinSymbol${dExpenses.energy.toTwoDecimalPlace()}"
                            binding.detailDiscounts.text = "-$coinSymbol${dExpenses.discount.toTwoDecimalPlace()}"
                            binding.detailTaxes.text = "$coinSymbol${dExpenses.taxes.toTwoDecimalPlace()}"
                            binding.detailFixedExp.text = "$coinSymbol${dExpenses.fixed.toTwoDecimalPlace()}"
                            binding.detailTotal.text = "$coinSymbol${dExpenses.total.toTwoDecimalPlace()}"
                        }
                        //val firstReading = viewModel.getFirstPeriodReading(p.code)
                        val periods = viewModel.getAllPeriods(meter.code)
                        // Ya que viene en orden inverso, la pos 0 es el periodo actual y la 1 el anterior al actual
                        val firstReading = if (periods.size > 1)
                            viewModel.getLastPeriodReading(periods[1].code)
                        else
                            viewModel.getFirstPeriodReading(p.code)

                        firstReading?.let { fr ->
                            binding.detailInitialReading.text = "${fr.readingValue.toTwoDecimalPlace()} kWh"
                            binding.detailInitialReadingDate.text = fr.readingDate.formatDate(requireContext())
                        }
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }catch (e:Exception){
            FirebaseCrashlytics.getInstance().log("FALLO loadDetailData Statistics")
            FirebaseCrashlytics.getInstance().recordException(e)
        }

    }

    private fun loadChartsData() {
        lifecycleScope.launch {

            binding.aggConsumptionChart.setupLineChartStyle(HoursValueFormatter(), MyMarkerView(requireContext(), R.layout.marker))
            binding.avgConsumptionChart.setupLineChartStyle(HoursValueFormatter(), MyMarkerView(requireContext(), R.layout.marker))
            binding.dailyAvgHistChart.setupLineChartStyle(ReadingAvgKwValueFormatter(), ReadingVsAvgKwMarkerView(requireContext(), R.layout.marker))
            binding.costVsKwhChart.setupLineChartStyle(KwValueFormatter(), CostVsKwMarkerView(requireContext(), R.layout.marker))
            val barLabels = viewModel.getPeriodDataLabels()
            binding.periodsChart.setupBarChartStyle(barLabels)

            binding.aggConsumptionChart.drawLimit(viewModel.meter.maxKwLimit.toFloat())
            binding.avgConsumptionChart.drawLimit((viewModel.meter.maxKwLimit/viewModel.meter.periodLength).toFloat())
            binding.dailyAvgHistChart.drawLimit((viewModel.meter.maxKwLimit/viewModel.meter.periodLength).toFloat())

            val period = viewModel.getMeterLatestPeriod(viewModel.meter.code)
            period?.let {
                val lineData = viewModel.getLineChartData(period)
                binding.aggConsumptionChart.data = lineData.consumptionDs

                binding.avgConsumptionChart.data = lineData.dailyAvgDs
                binding.avgConsumptionChart.axisLeft.axisMaximum = (lineData.dailyAvgDs.yMax) + 1
                binding.avgConsumptionChart.axisLeft.axisMinimum = (lineData.dailyAvgDs.yMin) - 1

                binding.dailyAvgHistChart.data = lineData.dailyAvgHist
                binding.dailyAvgHistChart.axisLeft.axisMaximum = (lineData.dailyAvgHist.yMax) + 1
                binding.dailyAvgHistChart.axisLeft.axisMinimum = (lineData.dailyAvgHist.yMin) - 1

                binding.dailyAvgHistChart.xAxis.axisMinimum = (lineData.dailyAvgHist.xMin)
                binding.dailyAvgHistChart.xAxis.axisMaximum = (lineData.dailyAvgHist.xMax)

                binding.costVsKwhChart.data = lineData.costPerKwDs
                binding.periodsChart.data = lineData.periodDs
                if(barLabels.size>2) {
                    binding.periodsChart.setVisibleXRangeMaximum(12f)
                    binding.periodsChart.setMaxVisibleValueCount(12)
                    binding.periodsChart.moveViewToX((barLabels.size - 1).toFloat())
                    binding.periodsChart.invalidate()
                }

            }
        }
    }


    @SuppressLint("InflateParams")
    private fun loadNativeAd(){
        val builder = AdLoader.Builder(requireContext(), getString(R.string.admob_native))
        builder.forNativeAd { nativeAd ->
            try {
                if (isAdded) {
                    val adBinding = AdNativeLayoutBinding.inflate(layoutInflater)
                    //val nativeAdview = AdNativeLayoutBinding.inflate(layoutInflater).root
                    binding.nativeAdFrameLayout.removeAllViews()
                    binding.nativeAdFrameLayout.addView(populateNativeAd(nativeAd, adBinding))
                }
            }catch (e:Exception){}
        }

        val adLoader = builder.build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAd(nativeAd: NativeAd, adView: AdNativeLayoutBinding): NativeAdView {
        val nativeAdView = adView.root
        with(adView){
            adHeadline.text = nativeAd.headline
            nativeAdView.headlineView = adHeadline
            nativeAd.advertiser?.let {
                adAdvertiser.text = it
                nativeAdView.advertiserView = adAdvertiser
            }
            nativeAd.icon?.let {
                adIcon.setImageDrawable(it.drawable)
                //adIcon.load(it.drawable){transformations(RoundedCornersTransformation(radius = 8f))}
                adIcon.setVisible()
                nativeAdView.iconView = adIcon
            }
            nativeAd.starRating?.let {
                adStartRating.rating = it.toFloat()
                adStartRating.setVisible()
                nativeAdView.starRatingView = adStartRating
            }
            nativeAd.callToAction?.let {
                adBtnCallToAction.text = it
                nativeAdView.callToActionView = adBtnCallToAction
            }
            nativeAd.body?.let {
                adBodyText.text = it
                nativeAdView.bodyView = adBodyText
            }
            nativeAd.mediaContent?.let {
                adMedia.mediaContent = it
                adMedia.setVisible()
                adMedia.setImageScaleType(ImageView.ScaleType.FIT_XY)
                nativeAdView.mediaView = adMedia
            }
        }
        nativeAdView.setNativeAd(nativeAd)
        return nativeAdView
    }

}