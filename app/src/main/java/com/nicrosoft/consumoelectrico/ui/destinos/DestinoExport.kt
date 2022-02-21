package com.nicrosoft.consumoelectrico.ui.destinos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.files.folderChooser
import com.afollestad.materialdialogs.input.input
import com.nicrosoft.consumoelectrico.MainKt
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.utils.AppResult
import com.nicrosoft.consumoelectrico.utils.helpers.CSVHelper
import com.nicrosoft.consumoelectrico.utils.formatDate
import com.pixplicity.easyprefs.library.Prefs
import java.io.File
import java.util.*

@Navigator.Name("export_ac")
class DestinoExport (var context: Context, var main:MainKt): ActivityNavigator(context) {
    override fun navigate(destination: Destination, args: Bundle?, navOptions: NavOptions?, navigatorExtras: Navigator.Extras?): NavDestination? {
        //super.navigate(destination, args, navOptions, navigatorExtras)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val initialPath = if(File("/storage/emulated/0/").exists())
                File("/storage/emulated/0/")
            else
                null
            MaterialDialog(context!!).show {
                folderChooser(context,
                        initialDirectory = initialPath,
                        emptyTextRes = R.string.title_choose_folder,
                        allowFolderCreation = true) { _, folder ->
                    // Folder selected

                    val name = context.getString(R.string.app_name) + Date().formatDate(context)
                    MaterialDialog(context).show {
                        title(R.string.save_as)
                        message(text = folder.path)
                        input(prefill = name.replace(" ", "_")) { _, text ->
                            // Text submitted with the action button
                            /*when(val result = CSVHelper.saveAllToCSV(folder.path, text.toString(), context)){
                                is AppResult.OK -> {
                                    Prefs.putString("last_path", folder.path);
                                    MaterialDialog(context).show {
                                        title(R.string.export_succes)
                                        message(text = folder.path + "/" + text.toString())
                                        positiveButton(R.string.agree)
                                    }
                                }
                                is AppResult.AppException -> {
                                    MaterialDialog(context).show {
                                        title(R.string.notice)
                                        message(R.string.export_error)
                                        negativeButton(R.string.report_error){
                                            val uriText = "mailto:edxavier05@gmail.com" +
                                                    "?subject=" + Uri.encode("ERROR EXPORTACION VERSION REALM") +
                                                    "&body=" + Uri.encode(result.exception.stackTraceToString())

                                            val uri = Uri.parse(uriText)
                                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                data = uri
                                            }
                                            context.startActivity(intent)
                                        }
                                        positiveButton(R.string.agree)
                                    }
                                }
                            }

                             */
                        }
                        positiveButton(R.string.ok)
                        negativeButton(R.string.cancel)
                    }
                }
            }
        } else {
            requestPermissions(main, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
        return null
    }
}