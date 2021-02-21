package com.nicrosoft.consumoelectrico.ui2

import android.os.Bundle
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
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
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.ScopeFragment
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.data.entities.PriceRange
import com.nicrosoft.consumoelectrico.databinding.FragmentNewElectricMeterBinding
import com.nicrosoft.consumoelectrico.ui2.adapters.PriceRangeAdapter
import com.nicrosoft.consumoelectrico.utils.hideKeyboard
import com.nicrosoft.consumoelectrico.utils.setHidden
import com.nicrosoft.consumoelectrico.utils.setVisible
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel
import com.wajahatkarim3.easyvalidation.core.view_ktx.nonEmpty
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter
import jp.wasabeef.recyclerview.animators.FadeInRightAnimator
import kotlinx.android.synthetic.main.app_bar_mainkt.*
import kotlinx.android.synthetic.main.dlg_prices.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance


class NewElectricFragment : ScopeFragment(), DIAware, PriceRangeAdapter.PriceItemListener {
    private var meter: ElectricMeter? = null
    private var cargosFijos: String = "----"
    private var impuestos: String = "----"
    private var precios: String = "----"

    override val di by closestDI()
    private val vmFactory by instance<ElectricVMFactory>()
    private lateinit var viewModel: ElectricViewModel
    private lateinit var binding: FragmentNewElectricMeterBinding

    private lateinit var navController: NavController
    private lateinit var params: NewElectricFragmentArgs
    private lateinit var adapter: PriceRangeAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentNewElectricMeterBinding.inflate(inflater, container, false)
        //return inflater.inflate(R.layout.fragment_new_electric_meter, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        params = NewElectricFragmentArgs.fromBundle(requireArguments())
        viewModel = ViewModelProvider(requireActivity(), vmFactory).get(ElectricViewModel::class.java)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        requireActivity().onBackPressedDispatcher.addCallback(this) { navController.navigateUp() }
        //form_container.fadeIn()
        initLayout()
        getRemoteConfig()
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
            if (params.editingItem) {
                requireActivity().toolbar.title = getString(R.string.edit)
                meter = viewModel.meter.value
                meter?.let {
                    binding.txtEmeterName.setText(it.name)
                    binding.txtEmeterDescription.setText(it.description)
                    binding.txtEmeterFixedCharges.setText(it.fixedPrices.toString())
                    binding.txtEmeterKwDiscount.setText(it.kwDiscount.toString())
                    binding.txtEmeterKwLimit.setText(it.maxKwLimit.toString())
                    binding.txtEmeterKwPrice.setText(it.kwPrice.toString())
                    binding.txtEmeterKwVat.setText(it.taxes.toString())
                    binding.txtEmeterPeriodLen.setText(it.periodLength.toString())
                    binding.txtEmeterReminder.setText(it.readReminder.toString())
                    binding.swLoseDiscount.isChecked = it.loseDiscount

                }
            }
            fab.setOnClickListener {
                if(params.editingItem)
                    updateMeter()
                else
                    saveMeter()
                fab.hideKeyboard()
            }
            buttonAddPrice.setOnClickListener {
                launch {
                    if(params.editingItem){
                        showCreateDialog()
                    }else{
                        Snackbar.make(binding.coordinator, R.string.first_save_new_meter, Snackbar.LENGTH_LONG)
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
        try {
            viewModel.getPriceList(viewModel.meter.value!!.code).observe(viewLifecycleOwner, Observer {
                adapter.submitList(it)
                if(it.isEmpty()) {
                    binding.priceMessage.setVisible()
                    binding.priceRanges.setHidden()
                }else{
                    binding.priceMessage.setHidden()
                    binding.priceRanges.setVisible()
                }
            })
        }catch (e:Exception){}
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

    override fun onPriceItemClickListener(price: PriceRange) {
        MaterialDialog(requireContext()).show {
            title(R.string.options)
            listItems(R.array.medidor_options) { _, index, _ ->
                when(index){
                    0-> showEditDialog(price)
                    1-> showDeleteConfirmDialog(price)
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
                        if(checkNonEmptyEditText(dlg_txt_to_kw) && checkNonEmptyEditText(dlg_txt_price_kw)){
                            val from = dlg_txt_from_kw.text.toString().toInt()
                            val to = dlg_txt_to_kw.text.toString().toInt()
                            // No permitir que el rango mayor se menor que el rango minimo
                            if(from >= to){
                                dlg_txt_to_kw.error = getString(R.string.invalid_kw_range)
                                dlg_txt_to_kw.requestFocus()
                            }else{
                                launch {
                                    val next = viewModel.getNextPriceRange(meter!!.code, from)
                                    if (next!=null){
                                        // No permitir que el rango mayor Nuevo sea mayor o igual al rango mayor-1 del siguienterango
                                        if(to>=next.toKw-1){
                                            dlg_txt_to_kw.error = getString(R.string.invalid_kw_range)
                                            dlg_txt_to_kw.requestFocus()
                                        }else {
                                            // Reasignar el rango minimo del siguiete para que no queden rangos fuera
                                            next.fromKw = to + 1
                                            viewModel.updatePriceRange(next)
                                            price.toKw = to
                                            price.price = dlg_txt_price_kw.text.toString().toFloat()
                                            viewModel.updatePriceRange(price)
                                            dismiss()
                                            Snackbar.make(binding.coordinator, R.string.item_updated, Snackbar.LENGTH_SHORT).show()
                                        }
                                    }else{
                                        price.toKw = to
                                        price.price = dlg_txt_price_kw.text.toString().toFloat()
                                        viewModel.updatePriceRange(price)
                                        dismiss()
                                        Snackbar.make(binding.coordinator, R.string.item_updated, Snackbar.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
        }
        try {
            val fromKw = dlg.getCustomView().findViewById(R.id.dlg_txt_from_kw) as TextInputEditText
            val toKw = dlg.getCustomView().findViewById(R.id.dlg_txt_to_kw) as TextInputEditText
            val priceKw = dlg.getCustomView().findViewById(R.id.dlg_txt_price_kw) as TextInputEditText
            fromKw.setText(price.fromKw.toString())
            toKw.setText(price.toKw.toString())
            priceKw.setText(price.price.toString())
        } catch (e:Exception) { }

    }

    private fun showCreateDialog() {
        val dlg = MaterialDialog(requireContext()).show {
            customView(R.layout.dlg_prices, scrollable = true)
                    .noAutoDismiss()
                    .title(R.string.edit)
                    .negativeButton(R.string.cancel) { dismiss() }
                    .positiveButton(R.string.ok) {
                        if(checkNonEmptyEditText(dlg_txt_to_kw) && checkNonEmptyEditText(dlg_txt_price_kw)){
                            if(checkValidRangeValues(dlg_txt_from_kw, dlg_txt_to_kw)){
                                val from = dlg_txt_from_kw.text.toString().toInt()
                                val to = dlg_txt_to_kw.text.toString().toInt()
                                launch {
                                    val op = viewModel.getOverlappingPrice(from, to, viewModel.meter.value!!.code)
                                    if(op!= null){
                                        dlg_txt_from_kw.error = getString(R.string.range_overlaps)
                                        dlg_txt_to_kw.error = getString(R.string.range_overlaps)
                                        dlg_txt_from_kw.requestFocus()
                                    }else{
                                        val price = PriceRange(meterCode = viewModel.meter.value!!.code)
                                        price.fromKw = from
                                        price.toKw = to
                                        price.price = dlg_txt_price_kw.text.toString().toFloat()
                                        viewModel.savePrice(price)
                                        dismiss()
                                        Snackbar.make(binding.coordinator, R.string.item_saved, Snackbar.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    }
        }
        try {
            launch {
                val lastPrice = viewModel.getLastPriceRange(viewModel.meter.value!!.code)
                val fromKw = dlg.getCustomView().findViewById(R.id.dlg_txt_from_kw) as TextInputEditText
                fromKw.setText("0")
                lastPrice?.let { fromKw.setText((it.toKw+1).toString()) }
            }
        } catch (e:Exception) { }

    }

    private fun showDeleteConfirmDialog(price: PriceRange){
        MaterialDialog(requireContext()).show {
            title(R.string.delete)
            message(R.string.delete_item_notice)
            positiveButton(R.string.agree){
                launch {
                    val next = viewModel.getNextPriceRange(meter!!.code, price.fromKw)
                    next?.let {
                        next.fromKw = price.fromKw
                        viewModel.updatePriceRange(next)
                    }
                    viewModel.deletePriceRange(price)
                }
            }
            negativeButton(R.string.cancel)
        }
    }

    private fun checkNonEmptyEditText(editText: TextInputEditText): Boolean{
        return editText.validator().nonEmpty().addErrorCallback {
            editText.error = getString(R.string.non_empty_message)
        }.check()
    }

    private fun checkValidRangeValues(from: TextInputEditText, to: TextInputEditText): Boolean{
        val fromVal = from.text.toString().toInt()
        val toVal = to.text.toString().toInt()
        if((toVal-fromVal < 1)) {
            from.error = getString(R.string.invalid_kw_range)
            to.error = getString(R.string.invalid_kw_range)
            from.requestFocus()
            return false
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.new_meter_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.ac_new_meter_help -> {
                val dlg = MaterialDialog(requireContext()).show {
                    title(R.string.help)
                    customView(R.layout.new_meter_help, scrollable = true, noVerticalPadding = false, horizontalPadding = true)
                    positiveButton(R.string.ok)
                }
                try {
                    val txtPriceKw = dlg.getCustomView().findViewById(R.id.txt_help_kw_price) as TextView
                    val txtFixedPrices = dlg.getCustomView().findViewById(R.id.txt_help_fixed_prices) as TextView
                    val txtTaxes = dlg.getCustomView().findViewById(R.id.txt_help_taxes) as TextView
                    txtPriceKw.text = HtmlCompat.fromHtml(precios, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    txtFixedPrices.text = HtmlCompat.fromHtml(cargosFijos, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    txtTaxes.text = HtmlCompat.fromHtml(impuestos, HtmlCompat.FROM_HTML_MODE_LEGACY)
                } catch (e:Exception) { }

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getRemoteConfig(){
        try {
            val remoteConfig = Firebase.remoteConfig
            val configSettings = remoteConfigSettings { minimumFetchIntervalInSeconds = 60 }
            remoteConfig.setConfigSettingsAsync(configSettings)
            remoteConfig.setDefaultsAsync(
                    mapOf(
                            "cargos_fijos" to "['Informacion no disponible']",
                            "impuestos" to "['Datos no disponibles']",
                            "precios" to "['Datos no disponibles']"
                    )
            )

            cargosFijos = processJson(remoteConfig.getString("cargos_fijos"))
            impuestos = processJson(remoteConfig.getString("impuestos"))
            precios = processJson(remoteConfig.getString("precios"))

            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    cargosFijos = processJson(remoteConfig.getString("cargos_fijos"))
                    impuestos = processJson(remoteConfig.getString("impuestos"))
                    precios = processJson(remoteConfig.getString("precios"))
                }
            }
        }catch (e:Exception){}
    }

    private fun processJson(json:String): String{
        val jsonArray = JSONArray(json)
        var str = ""
        for (i in 0 until jsonArray.length()) {
            str += jsonArray.getString(i) + "\n"
        }
        return str
    }
}