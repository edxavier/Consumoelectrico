package com.nicrosoft.consumoelectrico

import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.firebase.jobdispatcher.*
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.nicrosoft.consumoelectrico.data.AppDataBase
import com.nicrosoft.consumoelectrico.realm.Migration
import com.nicrosoft.consumoelectrico.ui2.ElectricVMFactory
import com.nicrosoft.consumoelectrico.utils.helpers.BackupDatabaseHelper
import com.nicrosoft.consumoelectrico.utils.workers.BackupWorker
import com.nicrosoft.consumoelectrico.utils.workers.ExternalBackupWorker
import com.nicrosoft.consumoelectrico.utils.workers.ReadReminderWorker
import com.pixplicity.easyprefs.library.Prefs
import io.realm.Realm
import io.realm.RealmConfiguration
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import java.util.concurrent.TimeUnit


/**
 * Created by Eder Xavier Rojas on 06/12/2016.
 */
class BaseApp : MultiDexApplication(), KodeinAware {
    override val kodein = Kodein.lazy {
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
        //MobileAds.initialize(this, getString(R.string.admob_app_id))
        FirebaseApp.initializeApp(this)
        MobileAds.initialize(this)
        Realm.init(this)
        val config = RealmConfiguration.Builder()
                .schemaVersion(1) // Must be bumped when the schema changes
                .migration(Migration()) // Migration to run instead of throwing an exception
                .build()
        try {
            Realm.compactRealm(config)
        } catch (ignored: Exception) {
        }
        Realm.setDefaultConfiguration(config)
        Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(packageName)
                .setUseDefaultSharedPreference(true)
                .build()

        /*LocalDateTime now = LocalDateTime.now();
        int hour = now.getHourOfDay();
        if(hour >=18 || hour <=5) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }*/

        val workManager = WorkManager.getInstance(this)
        val constraints: Constraints = Constraints.Builder()
                //.setRequiresBatteryNotLow(true)
                //.setRequiresStorageNotLow(true)
                .build()
        //The minimum time interval between reruns of a task is 15 minute or 900000 seconds.
        val backupWorker = PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(24, TimeUnit.HOURS)
                .build()
        val externalBackupWorker = PeriodicWorkRequestBuilder<ExternalBackupWorker>(4, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(30, TimeUnit.DAYS)
                .build()
        val readReminderWork = PeriodicWorkRequestBuilder<ReadReminderWorker>(5, TimeUnit.MINUTES)
                .setConstraints(constraints)
                //.setInitialDelay(5, TimeUnit.SECONDS)
                .build()

        workManager.enqueue(readReminderWork)
        workManager.enqueue(backupWorker)
        workManager.enqueue(externalBackupWorker)
    }
}