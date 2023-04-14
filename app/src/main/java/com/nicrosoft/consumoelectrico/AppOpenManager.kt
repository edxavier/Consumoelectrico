package com.nicrosoft.consumoelectrico

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdError
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner



import com.google.android.gms.ads.FullScreenContentCallback
import com.pixplicity.easyprefs.library.Prefs
import java.util.*


class AppOpenManager(myApplication: BaseApp):LifecycleObserver, Application.ActivityLifecycleCallbacks {
    private var appOpenAd: AppOpenAd? = null
    private var loadCallback: AppOpenAdLoadCallback? = null
    private val myApplication: BaseApp = myApplication
    private var isShowingAd = false
    private var currentActivity: Activity? = null
    private var loadTime: Long = 0

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        val isPurchased = Prefs.getBoolean("isPurchased", false)
        if(!isPurchased) {
            showAdIfAvailable()
        }
        //Log.d(LOG_TAG, "onStart")
    }

    init {
        myApplication.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /** Request an ad  */
    fun fetchAd() {
        //Log.d(LOG_TAG, "Fetch ad.")
        if(isAdAvailable)
            return
        loadCallback = object : AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                loadTime = Date().time
                Log.d(LOG_TAG, "Ad loaded.")
            }
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Handle the error.
                Log.d(LOG_TAG, "Ad load Failed.")
            }
        }
        val request: AdRequest = adRequest
        AppOpenAd.load(
            myApplication, AD_UNIT_ID, request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback as AppOpenAdLoadCallback
        )
    }

    /** Creates and returns ad request.  */
    private val adRequest: AdRequest
        get() = AdRequest.Builder().build()

    /** Utility method that checks if ad exists and can be shown.  */
    private val isAdAvailable: Boolean
        get() =  appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)

    /** Utility method to check if ad was loaded more than n hours ago.  */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    companion object {
        private const val LOG_TAG = "AppOpenManager"
        private const val AD_UNIT_ID = "ca-app-pub-9964109306515647/5003278617"
        //private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/3419835294"
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }

    /** Shows the ad if one isn't already showing.  */
    private fun showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isShowingAd && isAdAvailable) {
            //Log.d(LOG_TAG, "Will show ad.")
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        // Set the reference to null so isAdAvailable() returns false.
                        appOpenAd = null
                        isShowingAd = false
                        fetchAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {}
                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                    }
                }
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            currentActivity?.let { appOpenAd?.show(it) }
        } else {
            //Log.d(LOG_TAG, "Can not show ad.")
            fetchAd()
        }
    }
}
