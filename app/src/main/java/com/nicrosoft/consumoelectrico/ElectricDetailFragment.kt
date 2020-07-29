package com.nicrosoft.consumoelectrico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.nicrosoft.consumoelectrico.ui2.ElectricVMFactory
import com.nicrosoft.consumoelectrico.ui2.ElectricViewModel
import kotlinx.android.synthetic.main.fragment_electric_detail.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class ElectricDetailFragment : ScopeFragment(), KodeinAware {
    override val kodein by kodein()
    private val vmFactory by instance<ElectricVMFactory>()
    private lateinit var viewModel: ElectricViewModel

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
    }
}