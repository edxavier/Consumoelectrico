package com.nicrosoft.consumoelectrico.ui2

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.view.animation.OvershootInterpolator
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.ScopeFragment
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.databinding.FragmentElectricReadingListBinding
import com.nicrosoft.consumoelectrico.ui2.adapters.ElectricReadingAdapter
import com.nicrosoft.consumoelectrico.utils.*
import com.nicrosoft.consumoelectrico.utils.handlers.CsvHandler
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import jp.wasabeef.recyclerview.animators.FadeInDownAnimator
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import java.util.*
import kotlin.time.ExperimentalTime


class ElectricReadingListFragment : ScopeFragment(), DIAware, ElectricReadingAdapter.AdapterItemListener {
    private var tempReadings: List<ElectricReading>? = null
    override val di by closestDI()
    private val vmFactory by instance<ElectricVMFactory>()
    private lateinit var viewModel: ElectricViewModel


    private lateinit var navController: NavController
    private lateinit var mainNavController: NavController
    private lateinit var adapter:ElectricReadingAdapter
    private lateinit var binding:FragmentElectricReadingListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentElectricReadingListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        mainNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        viewModel = ViewModelProvider(requireActivity(), vmFactory)[ElectricViewModel::class.java]
        initLayout()
        loadData()
    }
    
    private fun loadData(showAll: Boolean = false){
        launch {
            val period = try {viewModel.getLastPeriod(viewModel.meter.value!!.code)}catch (e:Exception){null}
            if(period!=null) {
                if(showAll){
                    viewModel.getAllMeterReadings(period.meterCode).observe(viewLifecycleOwner, Observer {
                        tempReadings = it
                        toggleMessageVisibility(it.isEmpty())
                        adapter.submitList(it)
                    })
                }else{
                    viewModel.getPeriodMetersReadings(period.code).observe(viewLifecycleOwner, Observer {
                        tempReadings = it
                        toggleMessageVisibility(it.isEmpty())
                        adapter.submitList(it)
                    })
                }
            }else{
                toggleMessageVisibility(true)
            }
            viewModel.getAllMeterReadings(viewModel.meter.value!!.code)
                .observe(viewLifecycleOwner) {
                    tempReadings = it
                }
        }
    }

    private fun toggleMessageVisibility(isEmpty:Boolean){
        if(isEmpty) {
            binding.message.setVisible()
            binding.animationView.fadeZoomIn()
            binding.messageTitle.slideIn()
            binding.messageBody.slideIn()
        }
        else
            binding.message.setHidden()
    }

    private fun initLayout() {
        adapter = ElectricReadingAdapter(this)
        val animAdapter = ScaleInAnimationAdapter(adapter)
        animAdapter.setFirstOnly(false)
        animAdapter.setInterpolator(OvershootInterpolator())
        binding.emeterList.itemAnimator = FadeInDownAnimator()
        binding.emeterList.adapter = animAdapter
        binding.emeterList.setHasFixedSize(true)

    }

    @SuppressLint("CheckResult")
    @OptIn(ExperimentalTime::class)
    override fun onItemClickListener(reading: ElectricReading) {
        launch {
            val period = viewModel.getLastPeriod(reading.meterCode!!)
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
                launch {
                    val period = viewModel.getLastPeriod(reading.meterCode!!)
                    viewModel.terminatePeriod(reading, period!!, reading.meterCode!!)
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
                launch {
                    if(viewModel.validatedReadingValue(reading.readingDate, text.toString().toFloat(), reading.meterCode!!)){
                        reading.readingValue = text.toString().toFloat()
                        viewModel.updateReadingValue(reading)
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
                launch {
                    val fr = viewModel.getFirstMeterReading(reading.meterCode!!)
                    if(reading.code == fr.code){
                        MaterialDialog(requireContext()).show {
                            title(R.string.notice)
                            message(text = "No es posible eliminar esta lectura, si se equivoco debe eliminar el periodo completo")
                            positiveButton(R.string.ok)
                        }
                    }else
                        viewModel.deleteElectricReading(reading)
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
                loadData(showAll = true)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun exportDialog(){
        if (tempReadings==null) {
            showInfoDialog("No hay datos para exportar")
            return
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            return
        }


        var name = getString(R.string.label_readings) + " " + viewModel.meter.value!!.name + Date().formatDate(requireContext())
        name = name.replace(" ", "_").replace(",", "")
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, name)
            //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startSaveFileForResult.launch(intent)

    }

    private val startSaveFileForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                launch {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK){
            exportDialog()
        }else{
            showInfoDialog("No se concedieron los permisos")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showInfoDialog(message:String){
        MaterialDialog(requireContext()).show {
            title(R.string.notice)
            message(text = message)
            positiveButton(R.string.agree)
        }
    }

    private fun openFileIntent(fileUri:Uri){
        val myMime = MimeTypeMap.getSingleton()
        val newIntent = Intent(Intent.ACTION_VIEW)
        // val file = File(filePath)
        val mimeType = myMime.getMimeTypeFromExtension("csv")
        // val uri: Uri = FileProvider.getUriForFile(requireActivity(), BuildConfig.APPLICATION_ID.toString() + ".provider", file)
        newIntent.setDataAndType(fileUri, mimeType)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            requireContext().startActivity(newIntent)
        } catch (e: ActivityNotFoundException) {
            showInfoDialog("No Application handler for this type of file.")
        }
    }
}