package com.nicrosoft.consumoelectrico.ui2

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nicrosoft.consumoelectrico.BuildConfig
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.databinding.EmeterListFragmentBinding
import com.nicrosoft.consumoelectrico.screens.CircularProgress
import com.nicrosoft.consumoelectrico.screens.NoDataScreen
import com.nicrosoft.consumoelectrico.ui2.compose.main.MeterList
import com.nicrosoft.consumoelectrico.ui2.compose.main.MeterPreviewCard
import com.nicrosoft.consumoelectrico.utils.*
import com.nicrosoft.consumoelectrico.utils.handlers.JsonBackupHandler
import com.nicrosoft.consumoelectrico.utils.helpers.BackupDatabaseHelper
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import java.io.File
import java.util.*

class ElectricListFragment : Fragment(), DIAware, MenuProvider {
    private lateinit var userBackupDir: File
    private lateinit var appBackupsDir: File
    override val di by closestDI()
    private val vmFactory by instance<ElectricVMFactory>()
    private val backupHelper by instance<BackupDatabaseHelper>()
    private lateinit var viewModel: ElectricViewModel

    private lateinit var navController: NavController
    private lateinit var binding:EmeterListFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = EmeterListFragmentBinding.inflate(inflater)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        viewModel = ViewModelProvider(requireActivity(), vmFactory)[ElectricViewModel::class.java]
        initLayout()
    }


    @OptIn(ExperimentalFoundationApi::class)
    private fun initLayout() {
        binding.composeList.setContent {
            val metersState by viewModel.meterUiState.collectAsState()
            val listState = rememberLazyListState()
            val expandedFabState = remember {
                derivedStateOf {
                    listState.firstVisibleItemScrollOffset
                }
            }
            LaunchedEffect(true){
                viewModel.getElectricMeterList()
            }
            if(metersState.isLoading){
                CircularProgress()
            }else if(metersState.meterList.isNotEmpty()) {
                // toggleMessageVisibility(false)
                MeterList(
                    viewModel = viewModel,
                    onFabClick = {
                        fabAction()
                    },
                    children = { p ->
                        LaunchedEffect(key1 = expandedFabState.value) {
                            viewModel.expandedFab = viewModel.firstVisible >= expandedFabState.value
                            viewModel.firstVisible = expandedFabState.value
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(p),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            state = listState
                        ) {
                            items(items = metersState.meterList, key = { it.code }) { myMeter ->
                                MeterPreviewCard(
                                    onMoreVertClick = { showMoreVertOptions(meter = myMeter) },
                                    meter = myMeter,
                                    viewModel = viewModel,
                                    onDetailsClick = {
                                        showItemDetails(it)
                                    },
                                    modifier = Modifier.animateItemPlacement(
                                        animationSpec = tween(
                                            durationMillis = 350,
                                            easing = EaseInOutCubic
                                        )
                                    )
                                )

                            }
                        }

                    }
                )
            }else{
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NoDataScreen(
                        message = getString(R.string.main_empty_suggest),
                        imageId = R.raw.empty_box,
                        title = getString(R.string.main_emty_title)
                    )
                    Button(onClick = {
                        fabAction()
                    }) {
                        Text(text = getString(R.string.add))
                    }
                }
            }
        }

        val appName = getString(R.string.app_name).replace(" ", "_")
        val documents = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        userBackupDir = File("$documents/$appName", "UserBackups")
        appBackupsDir = File("$documents/$appName")
        if (!userBackupDir.exists())
            userBackupDir.mkdirs()
        if(!appBackupsDir.exists())
            appBackupsDir.mkdirs()
    }
    private fun fabAction(){
        val action =
            ElectricListFragmentDirections.actionNavEmaterListToNewElectricMeterFragment()
        navController.navigate(action)
    }
    @SuppressLint("CheckResult")
    fun showMoreVertOptions(meter: ElectricMeter) {
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

    private fun showItemDetails(meter: ElectricMeter) {
        lifecycleScope.launch {
            viewModel.selectedMeter(meter)
            val action = ElectricListFragmentDirections.actionNavEmaterListToElectricDetailFragment()
            navController.navigate(action)
        }
    }

    private fun showDeleteConfirmDialog(meter: ElectricMeter){
        MaterialDialog(requireContext()).show {
            title(text = "${getString(R.string.delete)} ${meter.name}?")
            message(R.string.delete_medidor_notice)
            positiveButton(R.string.agree){
                lifecycleScope.launch {
                    viewModel.deleteElectricMeter(meter)
                    viewModel.getElectricMeterList()
                }
            }
            negativeButton(R.string.cancel)
        }
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
                lifecycleScope.launch {
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
                lifecycleScope.launch {
                    when(val restoreResult = JsonBackupHandler.restoreBackup(backupHelper, uri, requireContext())){
                        is AppResult.OK -> {
                            delay(300)
                            viewModel.getElectricMeterList()
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
        if(grantResults.isEmpty()){
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

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.emeter_menu, menu)
    }

    @SuppressLint("CheckResult")
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.ac_export_import_data -> {
                MaterialDialog(requireContext()).show {
                    title(R.string.database_menu)
                    listItems(R.array.database_options) { _, index, _ ->
                        when (index) {
                            0 -> saveFile()
                            1 -> openFile()
                        }
                    }
                }
                true
            }
            else -> false
        }
    }
}