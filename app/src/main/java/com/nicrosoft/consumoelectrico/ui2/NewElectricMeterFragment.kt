package com.nicrosoft.consumoelectrico.ui2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.utils.fadeIn
import com.nicrosoft.consumoelectrico.utils.slideIn
import kotlinx.android.synthetic.main.fragment_new_electric_meter.*

class NewElectricMeterFragment : Fragment() {
    private lateinit var navController: NavController

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_electric_meter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        requireActivity().onBackPressedDispatcher.addCallback(this) { navController.navigateUp() }
        form_container.fadeIn()
    }
}