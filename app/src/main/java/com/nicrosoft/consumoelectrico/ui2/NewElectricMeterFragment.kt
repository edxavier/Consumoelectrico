package com.nicrosoft.consumoelectrico.ui2

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.ScopeFragment
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.databinding.FragmentNewElectricMeterBinding
import com.wajahatkarim3.easyvalidation.core.view_ktx.nonEmpty
import kotlinx.android.synthetic.main.app_bar_mainkt.*
import kotlinx.android.synthetic.main.fragment_new_electric_meter.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import kotlinx.coroutines.launch


class NewElectricMeterFragment : ScopeFragment(), KodeinAware {
    override val kodein by kodein()
    private val vmFactory by instance<ElectricMeterVMFactory>()
    private lateinit var viewModel: ElectricViewModel
    private lateinit var binding: FragmentNewElectricMeterBinding

    private lateinit var navController: NavController
    private lateinit var params: NewElectricMeterFragmentArgs

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_electric_meter, container, false)
        //return inflater.inflate(R.layout.fragment_new_electric_meter, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        params = NewElectricMeterFragmentArgs.fromBundle(requireArguments())
        viewModel = ViewModelProvider(requireActivity(), vmFactory).get(ElectricViewModel::class.java)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        requireActivity().onBackPressedDispatcher.addCallback(this) { navController.navigateUp() }
        //form_container.fadeIn()
        initLayout()
    }

    private fun initLayout() {
        // Solo usamo one way databinding para rellenar el formulario ya que el 2 way es un dolor de huevo, lo hare a lo tradicional

        binding.meter = if (params.editingItem) {
            requireActivity().toolbar.title = getString(R.string.edit_reading)
            viewModel.meter.value
        }else
            ElectricMeter(name = "")
        fab.setOnClickListener {
            if(params.editingItem)
                updateMeter()
            else
                saveMeter()
            hideKeyboard()
        }
    }

    private fun saveMeter(){
        if(!validatedForm())
            return
        val newMeter = ElectricMeter(name = binding.txtEmeterName.text.toString())
        with(binding){
            try {
                newMeter.description = txtEmeterDescription.text.toString()
                newMeter.fixedPrices = txtEmeterFixedCharges.text.toString().toFloat()
                newMeter.kwPrice = txtEmeterKwPrice.text.toString().toFloat()
                newMeter.kwDiscount = txtEmeterKwDiscount.text.toString().toFloat()
                newMeter.maxKwLimit = txtEmeterKwLimit.text.toString().toInt()
                newMeter.taxes = txtEmeterKwVat.text.toString().toFloat()
                newMeter.loseDiscount = swLoseDiscount.isChecked
                newMeter.periodLength = txtEmeterPeriodLen.text.toString().toInt()
                newMeter.readReminder = txtEmeterReminder.text.toString().toInt()
                launch {
                    viewModel.saveElectricMeter(newMeter)
                    navController.navigateUp()
                }
            }catch (e:Exception){
                Snackbar.make(binding.coordinator, "Unexpected Error", Snackbar.LENGTH_LONG)
                        .setTextColor(ContextCompat.getColor(requireContext(), R.color.md_amber_500))
                        .show()
            }
        }
    }
    private fun updateMeter(){
        if(!validatedForm())
            return
        val updateMeter = viewModel.meter.value
        with(binding){
            try {
                updateMeter?.let {
                    updateMeter.name = txtEmeterName.text.toString()
                    updateMeter.description = txtEmeterDescription.text.toString()
                    updateMeter.fixedPrices = txtEmeterFixedCharges.text.toString().toFloat()
                    updateMeter.kwPrice = txtEmeterKwPrice.text.toString().toFloat()
                    updateMeter.kwDiscount = txtEmeterKwDiscount.text.toString().toFloat()
                    updateMeter.maxKwLimit = txtEmeterKwLimit.text.toString().toInt()
                    updateMeter.taxes = txtEmeterKwVat.text.toString().toFloat()
                    updateMeter.loseDiscount = swLoseDiscount.isChecked
                    updateMeter.periodLength = txtEmeterPeriodLen.text.toString().toInt()
                    updateMeter.readReminder = txtEmeterReminder.text.toString().toInt()
                    launch {
                        viewModel.updateElectricMeter(updateMeter)
                        navController.navigateUp()
                    }
                }
            }catch (e:Exception){
                Snackbar.make(binding.coordinator, "Unexpected Error", Snackbar.LENGTH_LONG)
                        .setTextColor(ContextCompat.getColor(requireContext(), R.color.md_amber_500))
                        .show()
            }
        }
    }

    private fun validatedForm():Boolean{
        with(binding){
            if(!txtEmeterName.nonEmpty { txtEmeterName.error = it
                        txtEmeterName.requestFocus() })
                return false
            if(!txtEmeterKwPrice.nonEmpty { txtEmeterKwPrice.error = it })
                return false
            if(!txtEmeterKwVat.nonEmpty { txtEmeterKwVat.error = it })
                return false
            if(!txtEmeterKwDiscount.nonEmpty { txtEmeterKwDiscount.error = it })
                return false
            if(!txtEmeterFixedCharges.nonEmpty { txtEmeterFixedCharges.error = it })
                return false
            if(!txtEmeterKwLimit.nonEmpty { txtEmeterKwLimit.error = it })
                return false
            if(!txtEmeterPeriodLen.nonEmpty { txtEmeterPeriodLen.error = it })
                return false
            if(!txtEmeterReminder.nonEmpty { txtEmeterReminder.error = it })
                return false
            return true
        }
    }

    fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}