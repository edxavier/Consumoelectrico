package com.nicrosoft.consumoelectrico.ui2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.databinding.FragmentElectricDetailBinding
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel
import com.pixplicity.easyprefs.library.Prefs
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import java.util.*


class ElectricDetailFragment : Fragment(), DIAware {
    override val di by closestDI()
    private val vmFactory by instance<ElectricVMFactory>()
    private lateinit var viewModel: ElectricViewModel
    private lateinit var binding: FragmentElectricDetailBinding
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentElectricDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainNavController = findNavController()
        val navController2 = requireActivity().findNavController(R.id.nav_host_fragment_detail)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            mainNavController.navigateUp()
            navController2.navigateUp()
        }
        binding.bottomNavDetails.setupWithNavController(navController2)
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
            val min = 2
            val max = 3
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