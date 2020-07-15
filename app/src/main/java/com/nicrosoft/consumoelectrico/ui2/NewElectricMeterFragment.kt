package com.nicrosoft.consumoelectrico.ui2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.databinding.FragmentNewElectricMeterBinding
import com.nicrosoft.consumoelectrico.ui.DetallesFragmentArgs
import kotlinx.android.synthetic.main.fragment_new_electric_meter.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class NewElectricMeterFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private val vmFactory by instance<ElectricMeterVMFactory>()
    private lateinit var viewModel: ElectricViewModel
    private lateinit var binding: FragmentNewElectricMeterBinding

    private lateinit var navController: NavController
    private lateinit var params: NewElectricMeterFragmentArgs

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_electric_meter, container, false)
        //return inflater.inflate(R.layout.fragment_new_electric_meter, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), vmFactory).get(ElectricViewModel::class.java)
        params = NewElectricMeterFragmentArgs.fromBundle(requireArguments())
        if (params.editingItem)
            binding.meter = viewModel.meter.value
        else
            binding.meter = ElectricMeter(name = "Nuevo")
        if(binding.meter == null)
            Log.e("EDER", "METER NULL")


        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        requireActivity().onBackPressedDispatcher.addCallback(this) { navController.navigateUp() }
                //form_container.fadeIn()
        fab.setOnClickListener {
            Log.e("EDER", binding.meter.toString())
        }
    }
}