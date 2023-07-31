package com.nicrosoft.consumoelectrico

import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import com.nicrosoft.consumoelectrico.data.AppDataBase
import com.nicrosoft.consumoelectrico.ui2.ElectricVMFactory
import com.nicrosoft.consumoelectrico.utils.helpers.BackupDatabaseHelper
import com.nicrosoft.consumoelectrico.utils.workers.ReadReminderWorker
import com.pixplicity.easyprefs.library.Prefs
import org.kodein.di.*
import org.kodein.di.android.x.androidXModule
import java.util.concurrent.TimeUnit


/**
 * Created by Eder Xavier Rojas on 06/12/2016.
 */
class BaseApp : MultiDexApplication(), DIAware {
    private lateinit var appOpenManager:AppOpenManager

    override val di = DI.lazy {
        import(androidXModule(this@BaseApp))
        // BasedeDatos
        bind() from singleton { AppDataBase(instance()) }
        //Daos
        bind() from singleton { instance<AppDataBase>().electricMeterDAO() }
        bind() from singleton { instance<AppDataBase>().backupDAO() }
        // ViewModelFactory
        bind() from provider { ElectricVMFactory(instance(), instance()) }
        //Helper
        bind() from singleton { BackupDatabaseHelper(instance(), instance()) }
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        AndroidThreeTen.init(this)
        FirebaseApp.initializeApp(this)
        //if(BuildConfig.DEBUG) {
        //   FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        MobileAds.initialize(this)
        val conf = RequestConfiguration
            .Builder()
            .setTestDeviceIds(
                listOf("AC5F34885B0FE7EF03A409EB12A0F949", AdRequest.DEVICE_ID_EMULATOR)
            )
            .build()

        MobileAds.setRequestConfiguration(conf)

        val workManager = WorkManager.getInstance(this)
        val constraints: Constraints = Constraints.Builder()
                //.setRequiresBatteryNotLow(true)
                //.setRequiresStorageNotLow(true)
                .build()
        val readReminderWork = PeriodicWorkRequestBuilder<ReadReminderWorker>(4, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(4, TimeUnit.HOURS)
                .build()

        workManager.enqueue(readReminderWork)
        //workManager.enqueue(backupWorker)

        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()
        appOpenManager = AppOpenManager(this)

    }
}