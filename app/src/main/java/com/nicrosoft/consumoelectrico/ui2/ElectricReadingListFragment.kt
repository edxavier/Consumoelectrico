package com.nicrosoft.consumoelectrico.ui2

import android.os.Bundle
import android.text.InputType
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.google.android.material.snackbar.Snackbar
import com.nicrosoft.consumoelectrico.ElectricDetailFragmentDirections
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.ScopeFragment
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.ui2.adapters.ElectricReadingAdapter
import com.nicrosoft.consumoelectrico.utils.*
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import jp.wasabeef.recyclerview.animators.FadeInDownAnimator
import kotlinx.android.synthetic.main.emeter_list_fragment.animation_view
import kotlinx.android.synthetic.main.emeter_list_fragment.emeter_list
import kotlinx.android.synthetic.main.emeter_list_fragment.message_
import kotlinx.android.synthetic.main.emeter_list_fragment.message_body
import kotlinx.android.synthetic.main.emeter_list_fragment.message_title
import kotlinx.android.synthetic.main.fragment_electric_reading_list.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import kotlin.time.ExperimentalTime

class ElectricReadingListFragment : ScopeFragment(), KodeinAware, ElectricReadingAdapter.AdapterItemListener {
    override val kodein by kodein()
    private val vmFactory by instance<ElectricVMFactory>()
    private lateinit var viewModel: ElectricViewModel


    private lateinit var navController: NavController
    private lateinit var mainNavController: NavController
    private lateinit var adapter:ElectricReadingAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_electric_reading_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_detail)
        mainNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        viewModel = ViewModelProvider(requireActivity(), vmFactory).get(ElectricViewModel::class.java)
        //requireActivity().onBackPressedDispatcher.addCallback(this) { navController.navigateUp() }

        initLayout()
        loadData()
    }
    
    private fun loadData(){
        launch {
            val period = viewModel.getLastPeriod(viewModel.meter.value!!.code)
            if(period!=null) {
                viewModel.getPeriodMetersReadings(period.code).observe(viewLifecycleOwner, Observer {
                    toggleMessageVisibility(it.isEmpty())
                    adapter.submitList(it)
                })
            }else{
                toggleMessageVisibility(true)
            }
        }
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
        adapter = ElectricReadingAdapter(this)
        val animAdapter = ScaleInAnimationAdapter(adapter)
        animAdapter.setFirstOnly(false)
        animAdapter.setInterpolator(OvershootInterpolator())
        emeter_list.itemAnimator = FadeInDownAnimator()
        emeter_list.adapter = animAdapter
        emeter_list.setHasFixedSize(true)

    }

    @ExperimentalTime
    override fun onItemClickListener(reading: ElectricReading) {
        launch {
            val period = viewModel.getLastPeriod(reading.meterCode!!)
            val meter = viewModel.getMeter(reading.meterCode!!)
            val options = resources.getStringArray(R.array.readings_options).toMutableList()
            if(reading.readingDate.hoursSinceDate(period!!.fromDate)/24<=meter.periodLength-5)
                options.removeAt(0)
            MaterialDialog(requireContext()).show {
                title(text = reading.readingValue.toTwoDecimalPlace())
                listItems(items = options) { _, index, _ ->
                    var option = index
                    if(options.size == 2)
                        option+=1
                    when(option){
                        0->showTerminatePeriodDialog(reading)
                        1->showEditReadingDialog(reading)
                        2->showDeleteConfirmDialog(reading)
                    }
                }
            }
        }
    }

    @ExperimentalTime
    private fun showTerminatePeriodDialog(reading: ElectricReading) {
        MaterialDialog(requireContext()).show {
            title(R.string.end_period)
            message(R.string.end_period_notice)
            negativeButton(R.string.cancel)
            positiveButton(R.string.agree){
                launch {
                    val period = viewModel.getLastPeriod(reading.meterCode!!)
                    viewModel.terminatePeriod(reading, period!!, reading.meterCode!!)
                    loadData()
                }
            }
        }
    }

    @ExperimentalTime
    private fun showEditReadingDialog(reading: ElectricReading) {
        MaterialDialog(requireContext()).show {
            title(text = reading.readingValue.toTwoDecimalPlace())
            message(R.string.lectura_editar_hint)
            //message(R.string.edit_reading)
            input (prefill = reading.readingValue.toTwoDecimalPlace(), inputType = InputType.TYPE_CLASS_NUMBER)
            { _, text ->
                launch {
                    if(viewModel.validatedReadingValue(reading.readingDate, text.toString().toFloat(), reading.meterCode!!)){
                        reading.readingValue = text.toString().toFloat()
                        viewModel.updateReadingValue(reading)
                    }else{
                        Toast.makeText(context, getString(R.string.invalid_kw_range), Toast.LENGTH_SHORT)
                                .show()
                    }
                }
            }
            positiveButton(R.string.save)
            negativeButton(R.string.cancel)
        }
    }

    @ExperimentalTime
    private fun showDeleteConfirmDialog(reading: ElectricReading){
        MaterialDialog(requireContext()).show {
            title(text = reading.readingValue.toTwoDecimalPlace())
            message(R.string.delete_notice)
            positiveButton(R.string.agree){
                launch {
                    val fr = viewModel.getFirstMeterReading(reading.meterCode!!)
                    if(reading.code == fr.code){
                        MaterialDialog(requireContext()).show {
                            title(text = "Aviso")
                            message(text = "No es posible eliminar esta lectura, si se equivoco debe eliminar el periodo completo")
                            positiveButton(R.string.ok){}
                        }
                    }else
                        viewModel.deleteElectricReading(reading)
                }
            }
            negativeButton(R.string.cancel)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.electric_readings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_new_reading->{
                val action = ElectricDetailFragmentDirections.actionElectricDetailFragmentToNewEmeterReadingFragment()
                mainNavController.navigate(action)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}