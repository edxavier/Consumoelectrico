package com.nicrosoft.consumoelectrico.ui.destinos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator

import com.nicrosoft.consumoelectrico.R

@Navigator.Name("compartir_app")
class DestinoCompartirApp (var context: Context): ActivityNavigator(context) {
    override fun navigate(destination: Destination, args: Bundle?, navOptions: NavOptions?, navigatorExtras: Navigator.Extras?): NavDestination? {
        //super.navigate(destination, args, navOptions, navigatorExtras)

        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_SUBJECT, context.resources.getString(R.string.app_name))
        val sAux = "https://play.google.com/store/apps/details?id=" + context.packageName + " \n\n"
        i.putExtra(Intent.EXTRA_TEXT, sAux)
        context.startActivity(Intent.createChooser(i, context.resources.getString(R.string.share_app_msg)))
        return null
    }
}