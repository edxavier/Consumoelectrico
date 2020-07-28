package com.nicrosoft.consumoelectrico.ui.destinos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.files.FileFilter
import com.afollestad.materialdialogs.files.fileChooser
import com.nicrosoft.consumoelectrico.MainKt
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.utils.CSVHelper
import java.io.File


@Navigator.Name("import_ac")
class DestinoImport (var context: Context, var main:MainKt): ActivityNavigator(context) {
    override fun navigate(destination: Destination, args: Bundle?, navOptions: NavOptions?, navigatorExtras: Navigator.Extras?): NavDestination? {
        //super.navigate(destination, args, navOptions, navigatorExtras)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val initialPath = if(File("/storage/emulated/0/").exists())
                File("/storage/emulated/0/")
            else
                null
            val myFilter: FileFilter = { it.isDirectory || it.name.endsWith(".csv", true) }
            MaterialDialog(context).show {
                fileChooser(context,
                        initialDirectory = initialPath,
                        filter = myFilter,
                        allowFolderCreation = false) { _, file  ->
                    // Folder selected
                    if (!CSVHelper.restoreAllFromCSV(file.path, context)){
                        MaterialDialog(context).show{
                            title(text = "Error!")
                            message(R.string.export_error)
                            positiveButton(R.string.agree)
                        }
                    }else {
                        MaterialDialog(context).show {
                            title(R.string.export_succes)
                            message(text = file.path)
                            positiveButton(R.string.agree)
                        }
                    }

                }
            }
        } else {
            requestPermissions(main, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
        return null
    }
}