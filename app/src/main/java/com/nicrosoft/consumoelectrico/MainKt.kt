package com.nicrosoft.consumoelectrico

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.navigation.NavigationView
import com.nicrosoft.consumoelectrico.utils.helpers.RestoreHelper
import com.nicrosoft.consumoelectrico.utils.setHidden
import com.nicrosoft.consumoelectrico.utils.setVisible
import com.nicrosoft.consumoelectrico.ui.destinos.DestinoCompartirApp
import com.nicrosoft.consumoelectrico.ui.destinos.DestinoExport
import com.nicrosoft.consumoelectrico.ui.destinos.DestinoImport
import com.nicrosoft.consumoelectrico.ui.destinos.DestinoValorarApp
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.content_mainkt.*

class MainKt : ScopeActivity(), BillingProcessor.IBillingHandler {

    private lateinit var appBarConfiguration: AppBarConfiguration
    var PRODUCT_SKU = "remove_ads"
    var bp: BillingProcessor? = null
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_mainkt)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        navController.navigatorProvider.addNavigator(DestinoCompartirApp(this))
        navController.navigatorProvider.addNavigator(DestinoValorarApp(this))
        navController.navigatorProvider.addNavigator(DestinoExport(this, this))
        navController.navigatorProvider.addNavigator(DestinoImport(this, this))

        val inflater = navController.navInflater
        navController.graph  = inflater.inflate(R.navigation.mobile_navigation)

        bp = BillingProcessor(this, BuildConfig.APP_BILLING_PUB_KEY, BuildConfig.MERCHANT_ID, this)
        setSupportActionBar(toolbar)
        setupGlobalAdsConfig()
        setupBanner()

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        //val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_settings,
                R.id.nav_emater_list, R.id.nav_help
        ), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        val headerNav = navView.getHeaderView(0) as RelativeLayout
        val version = headerNav.findViewById<TextView>(R.id.version)
        version.text = BuildConfig.VERSION_NAME
        //StringBuilder path = new StringBuilder(getFilesDir().getAbsolutePath());
        RestoreHelper.getInternalStoragePath(this)

    }

    private fun setupGlobalAdsConfig() {
        val adRequest = RequestConfiguration.Builder()
                .setTestDeviceIds(arrayOf(
                        "AC5F34885B0FE7EF03A409EB12A0F949",
                        AdRequest.DEVICE_ID_EMULATOR
                ).toList())
                .build()
        MobileAds.setRequestConfiguration(adRequest)
    }

    /*
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.mainkt, menu)
        return true
    }

     */

    private fun setupBanner() {
        adView.setHidden()

        adView.loadAd(AdRequest.Builder().build())
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                adView.setVisible()
            }
        }
        //nav_view.menu.findItem(R.id.destino_ocultar_publicidad).isVisible = false
    }

    override fun onBillingInitialized() {
        if (bp!!.isPurchased(PRODUCT_SKU)) {
            Prefs.putBoolean("isPurchased", true)
        }
    }

    override fun onPurchaseHistoryRestored() {}

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        Prefs.putBoolean("isPurchased", true)
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {}

    override fun onBackPressed() {
        //if (searchView.onBackPressed()) {
        //  return
        //}
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
