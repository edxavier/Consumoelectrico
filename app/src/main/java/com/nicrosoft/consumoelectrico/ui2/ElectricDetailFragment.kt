package com.nicrosoft.consumoelectrico.ui2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.ScopeFragment
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.fragment_electric_detail.*
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.android.x.kodein
import org.kodein.di.instance
import java.util.*


class ElectricDetailFragment : ScopeFragment(), DIAware {
    override val di by closestDI()
    private val vmFactory by instance<ElectricVMFactory>()
    private lateinit var viewModel: ElectricViewModel
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_electric_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mainNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        val navController2 = requireActivity().findNavController(R.id.nav_host_fragment_detail)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            mainNavController.navigateUp()
            navController2.navigateUp()
        }
        //val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        bottom_nav_details.setupWithNavController(navController2)
        //NavigationUI.setupWithNavController(bottom_nav_details, navController2)
        if(!Prefs.getBoolean("isPurchased", false))
            requestInterstitialAds()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        showInterstitial()
    }

    private fun showInterstitial() {
        val ne = Prefs.getInt("exec_count", 0)
        Prefs.putInt("exec_count", ne + 1)
        //Log.d("EDERne", "${ne+1}")
        //Log.d("EDERsh", "${Prefs.getInt("show_after", 3)}")
        if (ne + 1 == Prefs.getInt("show_after", 3)) {
            Prefs.putInt("exec_count", 0)
            val r = Random()
            val min = 3
            val max = 5
            val rnd = r.nextInt(max - min) + min
            Prefs.putInt("show_after", rnd)
            mInterstitialAd?.show(requireActivity())

        }

    }

    private fun requestInterstitialAds() {
        val adUnitId = resources.getString(R.string.admob_interstical)
        InterstitialAd.load(requireActivity(), adUnitId, AdRequest.Builder().build(), object:
            InterstitialAdLoadCallback(){
            override fun onAdLoaded(p0: InterstitialAd) {
                super.onAdLoaded(p0)
                mInterstitialAd = p0
            }
        })
    }
}