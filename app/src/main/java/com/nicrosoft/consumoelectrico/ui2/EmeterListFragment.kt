package com.nicrosoft.consumoelectrico.ui2

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.ScopeFragment
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.ui2.adapters.ElectricMeterAdapter
import com.nicrosoft.consumoelectrico.utils.fadeZoomIn
import com.nicrosoft.consumoelectrico.utils.hideFabButtonOnScroll
import com.nicrosoft.consumoelectrico.utils.slideIn
import kotlinx.android.synthetic.main.emeter_list_fragment.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class EmeterListFragment : ScopeFragment(), KodeinAware {
    override val kodein by kodein()
    private val vmFactory by instance<ElectricMeterVMFactory>()
    private lateinit var viewModel: EmeterViewModel


    private lateinit var navController: NavController
    private lateinit var adapter:ElectricMeterAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.emeter_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        viewModel = ViewModelProvider(this, vmFactory).get(EmeterViewModel::class.java)
        message_.visibility = View.GONE

        initLayout()

        launch {
            viewModel.getElectricMeterList().observe(viewLifecycleOwner, Observer {
                Log.e("EDER", it.size.toString())
                if(it.isEmpty())
                    message_.visibility = View.VISIBLE
                adapter.submitList(it)
            })
        }



    }

    private fun initLayout() {
        adapter = ElectricMeterAdapter()
        val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation);
        emeter_list.layoutAnimation = controller
        emeter_list.setHasFixedSize(true)
        emeter_list.adapter = adapter
        emeter_list.hideFabButtonOnScroll(fab_new_electric_meter)

        fab_new_electric_meter.setOnClickListener {
            val action = EmeterListFragmentDirections.actionNavEmaterListToNewEmeterReadingFragment()
            val action2 = EmeterListFragmentDirections.actionNavEmaterListToNewElectricMeterFragment()
            //navController.navigate(action2)
            launch {
                viewModel.saveElectricMeter(ElectricMeter(name = "Prueba statica"))
            }
        }
        animation_view.fadeZoomIn()
        message_title.slideIn()
        message_body.slideIn()
    }

}