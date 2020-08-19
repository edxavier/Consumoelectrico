package com.nicrosoft.consumoelectrico

import android.app.backup.BackupHelper
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import androidx.work.*
import com.firebase.jobdispatcher.*
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.nicrosoft.consumoelectrico.data.AppDataBase
import com.nicrosoft.consumoelectrico.realm.Migration
import com.nicrosoft.consumoelectrico.ui2.ElectricVMFactory
import com.nicrosoft.consumoelectrico.utils.workers.BackupWorker
import com.nicrosoft.consumoelectrico.utils.DumpDataService
import com.nicrosoft.consumoelectrico.utils.ReminderService
import com.nicrosoft.consumoelectrico.utils.helpers.BackupDatabaseHelper
import com.nicrosoft.consumoelectrico.utils.workers.ExternalBackupWorker
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
        //Fabric.with(this, Crashlytics())
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        MobileAds.initialize(this, getString(R.string.admob_app_id))
        FirebaseApp.initializeApp(this)
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
        val dayly = 3600 * 24
        val hourly = 3600
        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
        val myJob = dispatcher.newJobBuilder()
                .setService(DumpDataService::class.java) // the JobService that will be called
                .setTag(DumpDataService.JOB_TAG) // uniquely identifies the job
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(0, dayly)) // Start after, Repeat every
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(false)
                .build()
        val myJob2 = dispatcher.newJobBuilder()
                .setService(ReminderService::class.java) // the JobService that will be called
                .setTag(ReminderService.JOB_TAG) // uniquely identifies the job
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(0, hourly)) // Start after, Repeat every
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(false)
                .build()
        dispatcher.mustSchedule(myJob)
        dispatcher.mustSchedule(myJob2)

        val workManager = WorkManager.getInstance(this)
        val constraints: Constraints = Constraints.Builder()
                //.setRequiresBatteryNotLow(true)
                //.setRequiresStorageNotLow(true)
                .build()
        //The minimum time interval between reruns of a task is 15 minute or 900000 seconds.
        val myPeriodicWorkRequest = PeriodicWorkRequestBuilder<BackupWorker>(5, TimeUnit.MINUTES)
                .setConstraints(constraints)
                //.setInitialDelay(5, TimeUnit.SECONDS)
                .build()
        val myPeriodicWorkRequest2 = PeriodicWorkRequestBuilder<ExternalBackupWorker>(30, TimeUnit.DAYS)
                .setConstraints(constraints)
                .setInitialDelay(30, TimeUnit.DAYS)
                .build()


        workManager.enqueue(myPeriodicWorkRequest)
        workManager.enqueue(myPeriodicWorkRequest2)
    }
}