package com.nicrosoft.consumoelectrico.ui2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.files.folderChooser
import com.afollestad.materialdialogs.list.listItems
import com.nicrosoft.consumoelectrico.BuildConfig
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.ScopeFragment
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.ui2.adapters.ElectricMeterAdapter
import com.nicrosoft.consumoelectrico.ui2.adapters.ElectricMeterAdapter.AdapterItemListener
import com.nicrosoft.consumoelectrico.utils.*
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator
import kotlinx.android.synthetic.main.emeter_list_fragment.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.io.File
import java.util.*

class ElectricListFragment : ScopeFragment(), KodeinAware, AdapterItemListener {
    override val kodein by kodein()
    private val vmFactory by instance<ElectricVMFactory>()
    private lateinit var viewModel: ElectricViewModel


    private lateinit var navController: NavController
    private lateinit var adapter:ElectricMeterAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.emeter_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        viewModel = ViewModelProvider(requireActivity(), vmFactory).get(ElectricViewModel::class.java)

        initLayout()
        loadData()
    }
    
    private fun loadData(){
        viewModel.getElectricMeterList().observe(viewLifecycleOwner, Observer {
            toggleMessageVisibility(it.isEmpty())
            adapter.submitList(it)
        })
    }

    private fun toggleMessageVisibility(isEmpty:Boolean){
        if(isEmpty) {
            message_.setVisible()
            animation_view.fadeZoomIn()
            message_title.slideIn()
            message_body.slideIn()
        }
        else
            message_.setHidden()
    }

    private fun initLayout() {
        adapter = ElectricMeterAdapter(this, viewModel, this)
        val animAdapter = ScaleInAnimationAdapter(adapter)
        animAdapter.setFirstOnly(false)
        animAdapter.setInterpolator(OvershootInterpolator())
        emeter_list.itemAnimator = ScaleInBottomAnimator()
        emeter_list.adapter = animAdapter
        emeter_list.setHasFixedSize(true)
        emeter_list.hideFabButtonOnScroll(fab_new_electric_meter)
        fab_new_electric_meter.setOnClickListener {
            val action = ElectricListFragmentDirections.actionNavEmaterListToNewElectricMeterFragment()
            navController.navigate(action)
        }
    }

    override fun onItemClickListener(meter: ElectricMeter) {
        MaterialDialog(requireContext()).show {
            title(text = meter.name)
            listItems(R.array.medidor_options) { _, index, _ ->
                when(index){
                    0->{
                        val action = ElectricListFragmentDirections.actionNavEmaterListToNewElectricMeterFragment(editingItem = true)
                        // ya que se comparte el VM se establece el objeto y asi evitar hacer consulta para cargarlo
                        viewModel.selectedMeter(meter)
                        navController.navigate(action)
                    }
                    1->{
                        showDeleteConfirmDialog(meter)
                    }
                }
            }
        }

    }

    override fun onItemDetailListener(meter: ElectricMeter) {
        launch {
            viewModel.selectedMeter(meter)
            val action = ElectricListFragmentDirections.actionNavEmaterListToElectricDetailFragment()
            navController.navigate(action)
        }
    }

    override fun onItemNewReading(meter: ElectricMeter) {
        launch {
            viewModel.selectedMeter(meter)
            val action = ElectricListFragmentDirections.actionNavEmaterListToNewEmeterReadingFragment()
            navController.navigate(action)
        }
    }

    private fun showDeleteConfirmDialog(meter: ElectricMeter){
        MaterialDialog(requireContext()).show {
            title(text = meter.name)
            message(R.string.delete_medidor_notice)
            positiveButton(R.string.agree){
                launch { viewModel.deleteElectricMeter(meter) }
            }
            negativeButton(R.string.cancel)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.emeter_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.ac_export_data->{
                exportDialog()
            }
            R.id.action_export_readings_to_ocsv->{

            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun exportDialog(){

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            return
        }
        val initialPath = if(File("/storage/emulated/0/").exists())
            File("/storage/emulated/0/")
        else
            null

        MaterialDialog(requireContext()).show {
            folderChooser(context,
                    initialDirectory = initialPath,
                    emptyTextRes = R.string.title_choose_folder,
                    allowFolderCreation = true) { _, folder ->
                // Folder selected
                launch {
                    //val period = viewModel.getLastPeriod(viewModel.meter.value!!.code)
                    var name = "BACKUP CEH " + BuildConfig.VERSION_NAME + Date().formatDate(context)
                    name = name.replace(" ", "_")
                    JsonBackupHandler.createBackup(viewModel.getDao(), "${folder.path}/$name")
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

}