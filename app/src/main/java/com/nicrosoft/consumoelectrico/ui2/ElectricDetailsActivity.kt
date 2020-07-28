package com.nicrosoft.consumoelectrico.ui2

import com.nicrosoft.consumoelectrico.R
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.nicrosoft.consumoelectrico.ScopeActivity
import kotlinx.android.synthetic.main.activity_electric_details.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class ElectricDetailsActivity : ScopeActivity(), KodeinAware {

    private lateinit var appBarConfiguration: AppBarConfiguration
    override val kodein by kodein()
    private val vmFactory by instance<ElectricVMFactory>()
    private lateinit var viewModel: ElectricViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_electric_details)
        setSupportActionBar(toolbar)
        viewModel = ViewModelProvider(this, vmFactory).get(ElectricViewModel::class.java)
        getMeter()
    }

    private fun getMeter() {
        launch {
            val meterCode = intent.getStringExtra("meterCode")
            viewModel.meter.value = viewModel.getMeter(meterCode!!)
            val navView: BottomNavigationView = findViewById(R.id.nav_view)
            val navController = findNavController(R.id.nav_host_fragment_detail)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            appBarConfiguration = AppBarConfiguration(setOf(
                    R.id.nav_electric_readings, R.id.nav_electric_details, R.id.nav_electric_load))
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
        }

    }


    override fun onBackPressed() {
        //if (searchView.onBackPressed()) {
        //  return
        //}
        super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}