package com.nicrosoft.consumoelectrico;

import android.content.ContextWrapper;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.nicrosoft.consumoelectrico.myUtils.DumpDataService;
import com.nicrosoft.consumoelectrico.myUtils.ReminderService;
import com.nicrosoft.consumoelectrico.realm.Migration;
import com.pixplicity.easyprefs.library.Prefs;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Eder Xavier Rojas on 06/12/2016.
 */

public class BaseApp extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(1) // Must be bumped when the schema changes
                .migration(new Migration()) // Migration to run instead of throwing an exception
                .build();
        try {
            Realm.compactRealm(config);
        }catch (Exception ignored){}

        Realm.setDefaultConfiguration(config);

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        /*LocalDateTime now = LocalDateTime.now();
        int hour = now.getHourOfDay();
        if(hour >=18 || hour <=5) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }*/
        int dayly  = (3600 * 24);
        int hourly  = (3600);

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(DumpDataService.class) // the JobService that will be called
                .setTag(DumpDataService.JOB_TAG)        // uniquely identifies the job
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(0, dayly)) // Start after, Repeat every
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(false)
                .build();

        Job myJob2 = dispatcher.newJobBuilder()
                .setService(ReminderService.class) // the JobService that will be called
                .setTag(ReminderService.JOB_TAG)        // uniquely identifies the job
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(0, hourly)) // Start after, Repeat every
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(false)
                .build();

        dispatcher.mustSchedule(myJob);
        dispatcher.mustSchedule(myJob2);
    }
}


