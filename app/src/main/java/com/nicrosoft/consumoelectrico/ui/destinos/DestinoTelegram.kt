package com.nicrosoft.consumoelectrico.ui.destinos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator

import com.nicrosoft.consumoelectrico.R

@Navigator.Name("nav_telegram")
class DestinoTelegram (var context: Context): ActivityNavigator(context) {
    override fun navigate(destination: Destination, args: Bundle?, navOptions: NavOptions?, navigatorExtras: Navigator.Extras?): NavDestination? {

        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse("https://t.me/joinchat/CCA2CRn9Es33ck3NVzK6mA")
        context.startActivity(Intent(i))
        return null
    }
}