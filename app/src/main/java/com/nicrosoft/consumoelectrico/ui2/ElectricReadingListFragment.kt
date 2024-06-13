package com.nicrosoft.consumoelectrico.ui2

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.databinding.FragmentElectricReadingListBinding
import com.nicrosoft.consumoelectrico.screens.readings.ReadingsScreen
import com.nicrosoft.consumoelectrico.screens.ui.theme.ConsumoelectricoTheme
import com.nicrosoft.consumoelectrico.utils.*
import com.nicrosoft.consumoelectrico.utils.handlers.CsvHandler
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import java.util.*
import kotlin.time.ExperimentalTime


class ElectricReadingListFragment : Fragment(), DIAware {
    private var tempReadings: List<ElectricReading>? = null
    override val di by closestDI()
    private val vmFactory by instance<ElectricVMFactory>()
    private lateinit var viewModel: ElectricViewModel


    private lateinit var navController: NavController
    private lateinit var mainNavController: NavController
    private lateinit var binding:FragmentElectricReadingListBinding
    lateinit var myAdView: FrameLayout

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)
        binding = FragmentElectricReadingListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        mainNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        viewModel = ViewModelProvider(requireActivity(), vmFactory)[ElectricViewModel::class.java]
        loadData()
        activity?.apply {
            myAdView = findViewById(R.id.adViewContainer)
            myAdView.setHidden()
        }
    }
    
    private fun loadData(showAll: Boolean = false){

        binding.readingsCompose.setContent {
            ConsumoelectricoTheme(darkTheme = false, dynamicColor = false){
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ){ padding ->
                    val readingsState by viewModel.readingUiState.collectAsState()
                    LaunchedEffect(key1 = true){
                        viewModel.getMeterReadings(viewModel.meter.code, allReadings = showAll)
                    }

                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                            .padding(padding)
                    ) {
                        ReadingsScreen(
                            onItemClick = { reading->
                                onItemClickListener(reading = reading)
                            },
                            readings = readingsState.readingList,
                            isLoading = readingsState.isLoading
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    @OptIn(ExperimentalTime::class)
    private fun onItemClickListener(reading: ElectricReading) {
        lifecycleScope.launch {
            val period = viewModel.getMeterLatestPeriod(reading.meterCode?:"")
            // val meter = viewModel.getMeter(reading.meterCode!!)
            val options = resources.getStringArray(R.array.readings_options).toMutableList()
            // if(reading.readingDate.hoursSinceDate(period!!.fromDate)/24<=meter.periodLength-5)
            // Hide end period check if period passed time its less than 1 day
            if((reading.readingDate.hoursSinceDate(period!!.fromDate)/24)<1)
                options.removeAt(0)
            MaterialDialog(requireContext()).show {
                title(text = "${getString(R.string.label_reading)}: ${reading.readingValue.toTwoDecimalPlace()} kWh")
                listItems(items = options) { _, index, _ ->
                    var option = index
                    if(options.size == 2)
                        option+=1
                    when(option){
                        0->showTerminatePeriodDialog(reading)
                        1->showEditReadingDialog(reading)
                        2->showDeleteConfirmDialog(reading)
                    }
                }
            }
        }
    }

    @ExperimentalTime
    private fun showTerminatePeriodDialog(reading: ElectricReading) {
        MaterialDialog(requireContext()).show {
            title(R.string.end_period)
            message(R.string.end_period_notice)
            negativeButton(R.string.cancel)
            positiveButton(R.string.agree){
                lifecycleScope.launch {
                    val period = viewModel.getMeterLatestPeriod(reading.meterCode!!)
                    viewModel.terminatePeriod(reading, period!!, reading.meterCode!!)
                    viewModel.getMeterReadings(viewModel.meter.code)
                    loadData()
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    @ExperimentalTime
    private fun showEditReadingDialog(reading: ElectricReading) {
        MaterialDialog(requireContext()).show {
            title(text = reading.readingValue.toTwoDecimalPlace())
            message(R.string.lectura_editar_hint)
            //message(R.string.edit_reading)
            input (prefill = reading.readingValue.toTwoDecimalPlace(), inputType = InputType.TYPE_CLASS_NUMBER)
            { _, text ->
                lifecycleScope.launch {
                    if(viewModel.validatedReadingValue(reading.readingDate, text.toString().toFloat(), reading.meterCode!!)){
                        reading.readingValue = text.toString().toFloat()
                        viewModel.updateReadingValue(reading)
                        viewModel.getMeterReadings(viewModel.meter.code)
                        loadData()
                    }else{
                        Toast.makeText(context, getString(R.string.invalid_kw_range), Toast.LENGTH_SHORT)
                                .show()
                    }
                }
            }
            positiveButton(R.string.save)
            negativeButton(R.string.cancel)
        }
    }

    @ExperimentalTime
    private fun showDeleteConfirmDialog(reading: ElectricReading){
        MaterialDialog(requireContext()).show {
            title(text = reading.readingValue.toTwoDecimalPlace())
            message(R.string.delete_notice)
            positiveButton(R.string.agree){
                lifecycleScope.launch {
                    val fr = viewModel.getFirstMeterReading(reading.meterCode!!)
                    if(reading.code == fr.code){
                        MaterialDialog(requireContext()).show {
                            title(R.string.notice)
                            message(text = "No es posible eliminar esta lectura, si se equivoco debe eliminar el periodo completo")
                            positiveButton(R.string.ok)
                        }
                    }else {
                        viewModel.deleteElectricReading(reading)
                        viewModel.getMeterReadings(viewModel.meter.code)
                        loadData()
                    }
                }
            }
            negativeButton(R.string.cancel)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.electric_readings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_new_reading->{
                val action = ElectricDetailFragmentDirections.actionElectricDetailFragmentToNewEmeterReadingFragment()
                mainNavController.navigate(action)
            }
            R.id.action_export_readings_to_ocsv->{
                exportDialog()
            }
            R.id.action_show_all_meter_readings->{
                lifecycleScope.launch {
                    viewModel.getMeterReadings(viewModel.meter.code, allReadings = true)
                    loadData()
                }
            }
            R.id.action_show_period_readings->{
                lifecycleScope.launch {
                    viewModel.getMeterReadings(viewModel.meter.code, allReadings = false)
                    loadData()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun exportDialog(){
        tempReadings = viewModel.readingUiState.value.readingList
        if (tempReadings?.size==0 ) {
            showInfoDialog("No hay datos para exportar")
            return
        }


        var name = getString(R.string.label_readings) + " " + viewModel.meter.name + Date().formatDate(requireContext())
        name = name.replace(" ", "_").replace(",", "")
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, name)
        }
        startSaveFileForResult.launch(intent)

    }

    private val startSaveFileForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                lifecycleScope.launch {
                    tempReadings?.let {
                        val resultPath = CsvHandler.exportMeterReadings(it, uri, requireContext())
                        if (resultPath!=null) {
                            openFileIntent(resultPath)
                        }else
                            showInfoDialog(getString(R.string.export_error))
                    }
                }
            }
        }
    }


    private fun showInfoDialog(message:String){
        MaterialDialog(requireContext()).show {
            title(R.string.notice)
            message(text = message)
            positiveButton(R.string.agree)
        }
    }

    private fun openFileIntent(fileUri:Uri){
        // This try to open the file after creation

        val myMime = MimeTypeMap.getSingleton()
        val newIntent = Intent(Intent.ACTION_VIEW)
        val mimeType = myMime.getMimeTypeFromExtension("csv")
        newIntent.setDataAndType(fileUri, mimeType)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            requireContext().startActivity(newIntent)
        } catch (e: ActivityNotFoundException) {
            showInfoDialog("No Application handler for this type of file.")
        }
    }

    override fun onPause() {
        super.onPause()
        myAdView.setVisible()
    }
}