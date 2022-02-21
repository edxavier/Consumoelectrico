package com.nicrosoft.consumoelectrico.ui2

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.view.animation.OvershootInterpolator
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.files.FileFilter
import com.afollestad.materialdialogs.files.fileChooser
import com.afollestad.materialdialogs.files.folderChooser
import com.afollestad.materialdialogs.list.listItems
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nicrosoft.consumoelectrico.BuildConfig
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.ScopeFragment
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.ui2.adapters.ElectricMeterAdapter
import com.nicrosoft.consumoelectrico.ui2.adapters.ElectricMeterAdapter.AdapterItemListener
import com.nicrosoft.consumoelectrico.utils.*
import com.nicrosoft.consumoelectrico.utils.handlers.JsonBackupHandler
import com.nicrosoft.consumoelectrico.utils.helpers.BackupDatabaseHelper
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel
import com.pixplicity.easyprefs.library.Prefs
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator
import kotlinx.android.synthetic.main.emeter_list_fragment.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import java.io.File
import java.util.*

class ElectricListFragment : ScopeFragment(), DIAware, AdapterItemListener {
    private lateinit var userBackupDir: File
    private lateinit var appBackupsDir: File
    override val di by closestDI()
    private val vmFactory by instance<ElectricVMFactory>()
    private val backupHelper by instance<BackupDatabaseHelper>()
    private lateinit var viewModel: ElectricViewModel

    private val EXPORT_REQUEST_RW_PERMISSIONS = 1
    private val IMPORT_REQUEST_RW_PERMISSIONS = 2

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
        launch {
            delay(1500)
            migrate()
        }
        initLayout()
        loadData()
    }

    private fun migrate() {
        if(!Prefs.getBoolean("migrated", false) and backupHelper.migrationDataAvailable()){
            MaterialDialog(requireContext()).show {
                title(R.string.notice)
                message(R.string.migration_message)
                positiveButton(R.string.ok){
                    launch{
                        try {
                            doMigration()
                            Prefs.putBoolean("migrated", true)
                            Toast.makeText(requireContext(), getString(R.string.migration_success), Toast.LENGTH_LONG).show()
                        }catch (e: Exception){
                            Prefs.putBoolean("migrated", true)
                            showErrorDialog("FALLO DE MIGRACION", getString(R.string.migration_error), e.stackTraceToString())
                            FirebaseCrashlytics.getInstance().log("FALLO DE MIGRACION")
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }
                    }
                }
                negativeButton(R.string.no_thanks){  }
            }
        }
    }

    private suspend fun doMigration(){
        //backupHelper.tryMigration()
        initLayout()
        loadData()
    }


    private fun loadData(){
        viewModel.getElectricMeterList().observe(viewLifecycleOwner, Observer {
            toggleMessageVisibility(it.isEmpty())
            adapter.submitList(it)
        })
    }

    private fun toggleMessageVisibility(isEmpty: Boolean){
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
        val appName = getString(R.string.app_name).replace(" ", "_")
        val documents = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        userBackupDir = File("$documents/$appName", "UserBackups")
        appBackupsDir = File("$documents/$appName")
        if (!userBackupDir.exists())
            userBackupDir.mkdirs()
        if(!appBackupsDir.exists())
            appBackupsDir.mkdirs()

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
                    0 -> {
                        val action = ElectricListFragmentDirections.actionNavEmaterListToNewElectricMeterFragment(editingItem = true)
                        // ya que se comparte el VM se establece el objeto y asi evitar hacer consulta para cargarlo
                        viewModel.selectedMeter(meter)
                        navController.navigate(action)
                    }
                    1 -> {
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

    @SuppressLint("CheckResult")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.ac_export_import_data -> {
                MaterialDialog(requireContext()).show {
                    title(R.string.database_menu)
                    listItems(R.array.database_options) { _, index, _ ->
                        when (index) {
                            0 -> {
                                saveFile()
                            }
                            1 -> {
                                openFile()
                            }
                            /*
                            2 -> {
                                launch {
                                    try {
                                        doMigration()
                                        Toast.makeText(requireContext(), getString(R.string.migration_success), Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        showErrorDialog("FALLO DE MIGRACION MANUAL", getString(R.string.migration_error), e.stackTraceToString())
                                        FirebaseCrashlytics.getInstance().log("FALLO DE MIGRACION MANUAL")
                                        FirebaseCrashlytics.getInstance().recordException(e)
                                    }
                                }
                            }
                             */
                        }
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveFile() {
        var name = "USER_BACKUP " + Date().backupFormat(requireContext())
        name = name.replace(" ", "_")
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
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
                    //val period = viewModel.getLastPeriod(viewModel.meter.value!!.code)
                    //var name = "USER_BACKUP " + Date().backupFormat(requireContext())
                    //name = name.replace(" ", "_")
                    when(val backupResult = JsonBackupHandler.createBackup(backupHelper, uri, requireContext())){
                        is AppResult.OK -> {
                            MaterialDialog(requireContext()).show {
                                title(R.string.notice)
                                message(R.string.backup_suggestion)
                                positiveButton(R.string.ok){
                                    sendFileIntent(uri)
                                    Prefs.putString("last_external_backup", Date().backupFormat(requireContext()))
                                }
                                negativeButton(R.string.no_thanks)
                            }
                        }
                        is AppResult.AppException -> {
                            showErrorDialog("FALLO DE EXPORTACION", getString(R.string.import_error), backupResult.exception.stackTraceToString())
                            FirebaseCrashlytics.getInstance().log("FALLO DE EXPORTACION")
                            FirebaseCrashlytics.getInstance().recordException(backupResult.exception)
                        }
                    }
                }

            }
        }
    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startForResult.launch(intent)
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                launch {
                    when(val restoreResult = JsonBackupHandler.restoreBackup(backupHelper, uri, requireContext())){
                        is AppResult.OK -> {
                            delay(300)
                            loadData()
                            showInfoDialog(getString(R.string.import_succes))
                        }
                        is AppResult.AppException -> {
                            FirebaseCrashlytics.getInstance().log("FALLO DE IMPORTACION")
                            FirebaseCrashlytics.getInstance().recordException(restoreResult.exception)
                            showErrorDialog("FALLO DE IMPORTACION", getString(R.string.import_error), restoreResult.exception.stackTraceToString())
                        }
                    }
                }
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()){
        }else{
            showInfoDialog("No se concedieron los permisos")
        }
    }


    private fun showInfoDialog(message: String){
        MaterialDialog(requireContext()).show {
            title(R.string.notice)
            message(text = message)
            positiveButton(R.string.agree)
        }
    }

    private fun showErrorDialog(emailSubject: String, message: String, stackTrace:String){
        MaterialDialog(requireContext()).show {
            title(R.string.notice)
            message(text = message)
            negativeButton(R.string.report_error){
                reportErrorByEmail(emailSubject, stackTrace)
            }
            positiveButton(R.string.agree)
        }
    }
    private fun sendFileIntent(uri: Uri){
        val myMime = MimeTypeMap.getSingleton()
        //val file = File(filePath)
        val mimeType = myMime.getMimeTypeFromExtension("json")
        //val uri: Uri = FileProvider.getUriForFile(requireActivity(), BuildConfig.APPLICATION_ID + ".provider", file)
        val newIntent = Intent(Intent.ACTION_SEND)
        newIntent.setDataAndType(uri, mimeType)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        newIntent.putExtra(Intent.EXTRA_STREAM, uri)
        newIntent.putExtra(Intent.EXTRA_TEXT, "BACKUP ${getString(R.string.app_name)}")

        try {
            requireContext().startActivity(Intent.createChooser(newIntent, "Guardar en:"))
        } catch (e: ActivityNotFoundException) {
            showInfoDialog("No Application handler for this type of file.")
        }
    }

    private fun reportErrorByEmail(title: String, error: String){
        val appDetails = "${BuildConfig.VERSION_CODE}: ${BuildConfig.VERSION_NAME}\n\n\n $error"
        val uriText = "mailto:edxavier05@gmail.com" +
                "?subject=" + Uri.encode(title) +
                "&body=" + Uri.encode(appDetails)

        val uri = Uri.parse(uriText)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = uri
        }
        startActivity(intent)
    }
}