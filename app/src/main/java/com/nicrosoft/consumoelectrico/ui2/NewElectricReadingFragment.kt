package com.nicrosoft.consumoelectrico.ui2

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.datetime.timePicker
import com.google.android.material.snackbar.Snackbar
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.ScopeFragment
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.databinding.FragmentNewEmeterReadingBinding
import com.wajahatkarim3.easyvalidation.core.view_ktx.nonEmpty
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.*
import androidx.lifecycle.Observer
import com.nicrosoft.consumoelectrico.data.entities.ElectricBillPeriod
import com.nicrosoft.consumoelectrico.utils.*
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.coroutines.delay
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.PeriodType
import kotlin.math.min


class NewElectricReadingFragment : ScopeFragment(), KodeinAware {
    private var period: ElectricBillPeriod? = null
    override val kodein by kodein()
    private val vmFactory by instance<ElectricVMFactory>()
    private lateinit var viewModel: ElectricViewModel

    private lateinit var navController: NavController
    private lateinit var binding: FragmentNewEmeterReadingBinding
    private lateinit var params: NewElectricReadingFragmentArgs
    private lateinit var tempReading:ElectricReading
    private val currDatetime = Calendar.getInstance()
    private val minDate = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_new_emeter_reading, container, false)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_emeter_reading, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), vmFactory).get(ElectricViewModel::class.java)
        params = NewElectricReadingFragmentArgs.fromBundle(requireArguments())
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        requireActivity().onBackPressedDispatcher.addCallback(this) { navController.navigateUp() }
        tempReading = ElectricReading()
        initUI()
        viewModel.getAllMeterReadings(viewModel.meter.value!!.code).observe(viewLifecycleOwner, Observer { it ->
            it.forEach {
                Log.e("EDER", "${it.readingDate.formatDate(requireContext())} - " +
                        "${it.readingValue} - ${it.kwConsumption} - ${it.kwAvgConsumption} " +
                        "- ${it.kwAggConsumption} - ${it.consumptionPreviousHours} - ${it.consumptionHours}")
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun initUI() {
        binding.apply {
            meter = viewModel.meter.value
            meter?.let { m ->
                launch {
                    val lastTwoReadings =  m.getLastReading(viewModel)
                    if(lastTwoReadings.isNotEmpty()){
                        nrTxtLastReading.text = "${lastTwoReadings[0].readingValue.toTwoDecimalPlace()} kWh"
                        period = viewModel.getLastElectricPeriod(m.code)
                        val p = Period(LocalDate(period?.fromDate), LocalDate(Date()), PeriodType.days())

                        if(p.days <= m.periodLength-2)
                            binding.nrEndPeriodSw.setHidden()
                        else
                            binding.nrEndPeriodSw.setVisible()
                        nrTxtReadingSince.text = lastTwoReadings[0].readingDate.formatDate(requireContext(), true)
                    }else{
                        nrTxtLastReading.text = "---- kWh"
                        nrTxtReadingSince.text = "Never"
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
                if(period!=null)
                    minDate.timeInMillis = period?.fromDate!!.time
                else
                    minDate.timeInMillis = 0
                MaterialDialog(requireContext()).show {
                    datePicker(minDate= minDate, maxDate = currDatetime){ _, selectedDate ->
                        //tempReading.readingDate.time = selectedDate.timeInMillis
                        MaterialDialog(requireContext()).show {
                            cancelOnTouchOutside(false)
                            cancelable(false)
                            timePicker(currentTime = selectedDate, show24HoursView = false){_, time ->
                                selectedDate.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
                                selectedDate.set(Calendar.MINUTE, time.get(Calendar.MINUTE))
                                tempReading.readingDate.time = selectedDate.timeInMillis
                                nrTxtReadingDate.setText(tempReading.readingDate.formatDate(requireContext(), includeTime = true))
                                validateDatetime()
                            }
                        }
                    }
                }
            }
            nrFab.setOnClickListener {
                launch {
                    if(!validateInputs())
                        return@launch
                    if(!validateReadingValue())
                        return@launch
                    saveReading()
                    Snackbar.make(binding.coordinator, R.string.item_saved, Snackbar.LENGTH_LONG).show()
                    delay(1100)
                    navController.navigateUp()
                }
                nrFab.hideKeyboard()
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
        val isValid = viewModel.validatedReadingValue(tempReading.readingDate, r, binding.meter!!.code)
        if (!isValid){
            binding.nrTxtMeterReading.error = getString(R.string.alert_over_range)
        }
        return isValid
    }

    private suspend fun saveReading() {
        tempReading.readingValue = binding.nrTxtMeterReading.text.toString().toFloat()
        tempReading.comments = binding.nrTxtReadingComments.text.toString()
        viewModel.savedReading(tempReading, binding.meter!!.code, binding.nrEndPeriodSw.isChecked)
    }

}

