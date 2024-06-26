package com.nicrosoft.consumoelectrico.ui2

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.afollestad.materialdialogs.datetime.timePicker
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.ElectricBillPeriod
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.databinding.AdNativeLayoutBinding
import com.nicrosoft.consumoelectrico.databinding.FragmentNewEmeterReadingBinding
import com.nicrosoft.consumoelectrico.utils.*
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel
import kotlinx.coroutines.launch
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.PeriodType
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import java.util.*
import kotlin.time.ExperimentalTime


class NewElectricReadingFragment : Fragment(), DIAware {
    private var meter: ElectricMeter? = null
    private var period: ElectricBillPeriod? = null
    override val di by closestDI()
    private val vmFactory by instance<ElectricVMFactory>()
    private lateinit var viewModel: ElectricViewModel

    private lateinit var navController: NavController
    private lateinit var binding: FragmentNewEmeterReadingBinding
    private lateinit var tempReading:ElectricReading
    private var currDatetime = Calendar.getInstance()
    //private var minDate = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_new_emeter_reading, container, false)
        binding = FragmentNewEmeterReadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(ExperimentalTime::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity(), vmFactory).get(ElectricViewModel::class.java)
        navController = findNavController()
        requireActivity().onBackPressedDispatcher.addCallback(this) { navController.navigateUp() }
        tempReading = ElectricReading()
        initUI()
        loadNativeAd()
    }

    @ExperimentalTime
    @SuppressLint("SetTextI18n")
    private fun initUI() {
        binding.apply {
            meter = viewModel.meter
            meter?.let { m ->
                nrTxtMedidorName.text = m.name
                lifecycleScope.launch {
                    val lastReading =  viewModel.getMeterReadings(m.code, true).firstOrNull()
                    if(lastReading!=null){
                        nrTxtLastReading.text = "${getString(R.string.label_last_reading)}\n${lastReading.readingValue.toTwoDecimalPlace()} kWh"
                        nrTxtReadingSince.text = lastReading.readingDate.formatDate(requireContext(), true)
                        verifyEndPeriod(Date())
                    }else{
                        nrTxtLastReading.text = "---- kWh"
                        nrTxtReadingSince.text = getString(R.string.never)
                        nrEndPeriodSw.setHidden()
                        MaterialDialog(requireContext()).show {
                            title(R.string.notice)
                            message(R.string.first_reading_notice)
                            positiveButton(R.string.ok)
                        }
                    }
                }
            }
            nrTxtReadingDate.setOnClickListener {
                MaterialDialog(requireContext()).show {
                    datePicker(maxDate = currDatetime){ _, selectedDate ->
                        //tempReading.readingDate.time = selectedDate.timeInMillis
                        MaterialDialog(requireContext()).show {
                            cancelOnTouchOutside(false)
                            cancelable(false)
                            timePicker(currentTime = selectedDate, show24HoursView = false, requireFutureTime = false){_, time ->
                                selectedDate.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
                                selectedDate.set(Calendar.MINUTE, time.get(Calendar.MINUTE))
                                tempReading.readingDate.time = selectedDate.timeInMillis
                                nrTxtReadingDate.setText(tempReading.readingDate.formatDate(requireContext(), includeTime = true))
                                validateDatetime()
                                verifyEndPeriod(selectedDate.time)
                            }
                        }
                    }
                }
            }
            nrFab.setOnClickListener {
                lifecycleScope.launch {
                    if(!validateInputs())
                        return@launch
                    if(!validateReadingValue())
                        return@launch
                    if(saveReading())
                        navController.navigateUp()
                    //Snackbar.make(binding.coordinator, R.string.item_saved, Snackbar.LENGTH_LONG).show()
                }
                nrFab.hideKeyboard()
            }
            nrEndPeriodSw.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked){
                    MaterialDialog(requireContext()).show {
                        title(R.string.end_period)
                        message(R.string.end_period_notice)
                        positiveButton(R.string.ok){}
                    }
                }
            }
        }
    }

    private fun verifyEndPeriod(date: Date) {
        lifecycleScope.launch {
            period = viewModel.getMeterLatestPeriod(viewModel.meter.code)
            val p = Period(LocalDate(period?.fromDate), LocalDate(date), PeriodType.days())
            // Hide end period check if period passed time its less than 1 day
            if(p.days < 1) {
                binding.nrEndPeriodSw.setHidden()
            }else {
                binding.nrEndPeriodSw.setVisible()
                // binding.nrEndPeriodSw.isEnabled = p.days < viewModel.meter.periodLength+5
            }
        }
    }

    private fun validateInputs():Boolean{
        val message = getString(R.string.non_empty_message)
        with(binding){
            nrTxtIlayoutFecha.isErrorEnabled = true
            if(!validateDatetime())
                return false
            if(nrTxtMeterReading.text.isNullOrEmpty()) {
                nrTxtMeterReading.error = message
                nrTxtMeterReading.requestFocus()
                return false
            }
            return true
        }
    }
    private fun validateDatetime():Boolean{
        val message = getString(R.string.non_empty_message)
        with(binding) {
            return if (nrTxtReadingDate.text.isNullOrEmpty()) {
                nrTxtIlayoutFecha.error = message
                false
            } else {
                nrTxtIlayoutFecha.isErrorEnabled = false
                nrTxtIlayoutFecha.error = ""
                true
            }
        }
    }

    private suspend fun validateReadingValue():Boolean{
        val r = binding.nrTxtMeterReading.text.toString().toFloat()
        val isValid = viewModel.validatedReadingValue(tempReading.readingDate, r, meter!!.code)
        if (!isValid){
            binding.nrTxtMeterReading.error = getString(R.string.alert_over_range)
        }
        return isValid
    }

    @ExperimentalTime
    private suspend fun saveReading(): Boolean{
        return try {
            tempReading.readingValue = binding.nrTxtMeterReading.text.toString().toFloat()
            tempReading.comments = binding.nrTxtReadingComments.text.toString()
            viewModel.savedReading(tempReading, meter!!.code, binding.nrEndPeriodSw.isChecked)
            true
        }catch (e:Exception){
            FirebaseCrashlytics.getInstance().log("ERROR saveReading")
            FirebaseCrashlytics.getInstance().recordException(e)
            MaterialDialog(requireContext()).show {
                title(R.string.notice)
                message(text = e.message)
                positiveButton(R.string.ok){}
            }
            false
        }

    }

    @SuppressLint("InflateParams")
    private fun loadNativeAd(){
        val builder = AdLoader.Builder(requireContext(), getString(R.string.admob_native))
        try {
            builder.forNativeAd { nativeAd ->
                if (isAdded) {
                    val adBinding = AdNativeLayoutBinding.inflate(layoutInflater)
                    //val nativeAdview = AdNativeLayoutBinding.inflate(layoutInflater).root
                    binding.nativeAdFrameLayout.removeAllViews()
                    binding.nativeAdFrameLayout.addView(populateNativeAd(nativeAd, adBinding))
                }

            }
            val adLoader = builder.build()
            adLoader.loadAd(AdRequest.Builder().build())
        }catch (e:Exception){}
    }

    private fun populateNativeAd(nativeAd: NativeAd, adView: AdNativeLayoutBinding): NativeAdView {
        val nativeAdView = adView.root
        with(adView){
            adHeadline.text = nativeAd.headline
            nativeAdView.headlineView = adHeadline
            nativeAd.advertiser?.let {
                adAdvertiser.setVisible()
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

        }
        nativeAdView.setNativeAd(nativeAd)
        return nativeAdView
    }


}

