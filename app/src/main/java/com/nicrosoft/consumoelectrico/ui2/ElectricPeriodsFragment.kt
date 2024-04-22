package com.nicrosoft.consumoelectrico.ui2

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.databinding.FragmentElectricPeriodsBinding
import com.nicrosoft.consumoelectrico.screens.periods.PeriodsScreen
import com.nicrosoft.consumoelectrico.screens.ui.theme.ConsumoelectricoTheme
import com.nicrosoft.consumoelectrico.utils.*
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class ElectricPeriodsFragment : Fragment(), DIAware {

    override val di by closestDI()
    private val vmFactory by instance<ElectricVMFactory>()
    private lateinit var viewModel: ElectricViewModel
    private lateinit var binding: FragmentElectricPeriodsBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentElectricPeriodsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), vmFactory)[ElectricViewModel::class.java]
        loadData()
    }

    @SuppressLint("CheckResult")
    private fun loadData(){
        binding.composeList.setContent {
            ConsumoelectricoTheme(darkTheme = false, dynamicColor = false){
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ){ padding ->
                    val readingsState by viewModel.readingUiState.collectAsState()
                    LaunchedEffect(key1 = true){
                        viewModel.getAllPeriods(viewModel.meter.code)
                    }

                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(padding)
                    ) {
                        PeriodsScreen(
                            onItemClick = { period ->
                                MaterialDialog(requireContext()).show {
                                    title(text = "${getString(R.string.undo_period)} ${period.fromDate.formatDate(requireContext())}")
                                    message(R.string.undo_notice)
                                    positiveButton(R.string.agree){
                                        lifecycleScope.launch {
                                            viewModel.undoLastPeriod(period.meterCode)
                                            viewModel.getAllPeriods(viewModel.meter.code)
                                            loadData()
                                        }
                                    }
                                    negativeButton(R.string.cancel)
                                }
                            },
                            periods = readingsState.periods,
                            isLoading = readingsState.isLoading
                        )
                    }
                }
            }
        }
    }



}