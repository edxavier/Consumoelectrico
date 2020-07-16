package com.nicrosoft.consumoelectrico.ui2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItems
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.ScopeFragment
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.data.entities.PriceRange
import com.nicrosoft.consumoelectrico.databinding.FragmentNewElectricMeterBinding
import com.nicrosoft.consumoelectrico.ui2.adapters.ElectricMeterAdapter
import com.nicrosoft.consumoelectrico.ui2.adapters.PriceRangeAdapter
import com.nicrosoft.consumoelectrico.utils.setHidden
import com.nicrosoft.consumoelectrico.utils.setVisible
import com.nicrosoft.consumoelectrico.utils.toTwoDecimalPlace
import com.wajahatkarim3.easyvalidation.core.view_ktx.nonEmpty
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter
import jp.wasabeef.recyclerview.animators.FadeInRightAnimator
import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator
import kotlinx.android.synthetic.main.app_bar_mainkt.*
import kotlinx.android.synthetic.main.emeter_list_fragment.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.nio.file.Files.delete


class NewElectricMeterFragment : ScopeFragment(), KodeinAware, PriceRangeAdapter.PriceItemListener {
    override val kodein by kodein()
    private val vmFactory by instance<ElectricMeterVMFactory>()
    private lateinit var viewModel: ElectricViewModel
    private lateinit var binding: FragmentNewElectricMeterBinding

    private lateinit var navController: NavController
    private lateinit var params: NewElectricMeterFragmentArgs
    private lateinit var adapter: PriceRangeAdapter

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
        if(params.editingItem)
            loadPrices()
        else {
            binding.priceMessage.setVisible()
            binding.priceRanges.setHidden()
        }
    }

    private fun initLayout() {
        // Solo usamo one way databinding para rellenar el formulario ya que el 2 way es un dolor de huevo, lo hare a lo tradicional
        adapter = PriceRangeAdapter(this)
        binding.apply {
            meter = if (params.editingItem) {
                requireActivity().toolbar.title = getString(R.string.edit)
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
            buttonAddPrice.setOnClickListener {
                launch {
                    if(params.editingItem){
                        launch {
                            viewModel.savePrice(PriceRange(fromKw = 0, toKw = 50, meterId = viewModel.meter.value?.id!!))
                            Snackbar.make(binding.coordinator, "Price has been saved", Snackbar.LENGTH_SHORT)
                                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.md_green_500))
                                    .show()
                        }
                    }else{
                        Snackbar.make(binding.coordinator, "Please save your new meter before adding price range", Snackbar.LENGTH_LONG)
                                .setTextColor(ContextCompat.getColor(requireContext(), R.color.md_amber_500))
                                .show()
                    }
                }
            }

            val animAdapter = SlideInRightAnimationAdapter(adapter)
            animAdapter.setFirstOnly(false)
            animAdapter.setInterpolator(OvershootInterpolator())
            priceRanges.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            priceRanges.itemAnimator = FadeInRightAnimator()
            priceRanges.adapter = animAdapter
            priceRanges.setHasFixedSize(true)
        }
    }

    private fun loadPrices(){
        viewModel.getPriceList(viewModel.meter.value?.id!!).observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            if(it.isEmpty()) {
                binding.priceMessage.setVisible()
                binding.priceRanges.setHidden()
            }else{
                binding.priceMessage.setHidden()
                binding.priceRanges.setVisible()
            }
        })
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
        val message = getString(R.string.non_empty_message)
        with(binding){
            if(!txtEmeterName.nonEmpty { txtEmeterName.error = message
                        txtEmeterName.requestFocus() })
                return false
            if(!txtEmeterKwPrice.nonEmpty { txtEmeterKwPrice.error = message })
                return false
            if(!txtEmeterKwVat.nonEmpty { txtEmeterKwVat.error = message })
                return false
            if(!txtEmeterKwDiscount.nonEmpty { txtEmeterKwDiscount.error = message })
                return false
            if(!txtEmeterFixedCharges.nonEmpty { txtEmeterFixedCharges.error = message })
                return false
            if(!txtEmeterKwLimit.nonEmpty { txtEmeterKwLimit.error = message })
                return false
            if(!txtEmeterPeriodLen.nonEmpty { txtEmeterPeriodLen.error = message })
                return false
            if(!txtEmeterReminder.nonEmpty { txtEmeterReminder.error = message })
                return false
            return true
        }
    }

    fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onPriceItemClickListener(price: PriceRange) {
        MaterialDialog(requireContext()).show {
            title(R.string.options)
            listItems(R.array.medidor_options) { _, index, _ ->
                when(index){
                    0->{
                        //val action = EmeterListFragmentDirections.actionNavEmaterListToNewElectricMeterFragment(editingItem = true)
                        // ya que se comparte el VM se establece el objeto y asi evitar hacer consulta para cargarlo
                        //viewModel.selectedMeter(meter)
                        //navController.navigate(action)
                        showEditDialog(price)
                    }
                    1->{
                        showDeleteConfirmDialog(price)
                    }
                }
            }
        }
    }

    private fun showEditDialog(price: PriceRange) {
        val dlg = MaterialDialog(requireContext()).show {
            customView(R.layout.dlg_prices, scrollable = true)
                    .noAutoDismiss()
                    .title(R.string.edit)
                    .negativeButton(R.string.cancel) { dismiss() }
                    .positiveButton(R.string.ok) {
                        dismiss()
                    }
        }
        try {
            val fromKw = dlg.getCustomView().findViewById(R.id.txt_from_kw) as TextInputEditText
            val toKw = dlg.getCustomView().findViewById(R.id.txt_to_kw) as TextInputEditText
            val priceKw = dlg.getCustomView().findViewById(R.id.txt_to_kw) as TextInputEditText
            fromKw.setText(price.fromKw.toString())
            toKw.setText(price.toKw.toString())
            priceKw.setText(price.price.toString())
        } catch (e:Exception) { }

    }

    private fun showDeleteConfirmDialog(price: PriceRange){
        MaterialDialog(requireContext()).show {
            title(R.string.delete)
            message(R.string.delete_medidor_notice)
            positiveButton(R.string.agree){
                launch { viewModel.deleteElectricMeter(price) }
            }
            negativeButton(R.string.cancel)
        }
    }

}