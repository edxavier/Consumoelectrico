package com.nicrosoft.consumoelectrico.ui2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.ScopeFragment
import com.nicrosoft.consumoelectrico.data.entities.ElectricBillPeriod
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel
import com.nicrosoft.consumoelectrico.ui2.adapters.PeriodsAdapter
import com.nicrosoft.consumoelectrico.utils.fadeZoomIn
import com.nicrosoft.consumoelectrico.utils.setHidden
import com.nicrosoft.consumoelectrico.utils.setVisible
import com.nicrosoft.consumoelectrico.utils.slideIn
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator
import kotlinx.android.synthetic.main.fragment_electric_periods.*
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class ElectricPeriodsFragment : ScopeFragment(), DIAware, PeriodsAdapter.PeriodItemListener {

    override val di by closestDI()
    private val vmFactory by instance<ElectricVMFactory>()
    private lateinit var viewModel: ElectricViewModel
    private lateinit var adapter: PeriodsAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_electric_periods, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), vmFactory)[ElectricViewModel::class.java]
        initLayout()
        loadData()
    }

    private fun loadData(){
        try {
            viewModel.getMeterPeriods(viewModel.meter.value!!.code).observe(viewLifecycleOwner, Observer {
                toggleMessageVisibility(it.isEmpty())
                adapter.submitList(it)
            })
        }catch (e:Exception){}
    }
    private fun toggleMessageVisibility(isEmpty:Boolean){
        if(isEmpty) {
            message_.setVisible()
            animation_view.fadeZoomIn()
            message_title.slideIn()
            message_body.slideIn()
        }
        else
            message_.setHidden()
    }

    private fun initLayout() {
        adapter = PeriodsAdapter(this, viewModel)
        val animAdapter = ScaleInAnimationAdapter(adapter)
        animAdapter.setFirstOnly(false)
        animAdapter.setInterpolator(OvershootInterpolator())
        period_list.itemAnimator = ScaleInBottomAnimator()
        period_list.adapter = animAdapter
        period_list.setHasFixedSize(true)
    }

    override fun onPeriodItemClickListener(period: ElectricBillPeriod) {
    }

}