package com.nicrosoft.consumoelectrico.ui2

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nicrosoft.consumoelectrico.data.daos.ElectricMeterDAO
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ElectricViewModel(val context: Context, private val dao:ElectricMeterDAO) : ViewModel() {

    private val _meter = MutableLiveData<ElectricMeter>()
    val meter: LiveData<ElectricMeter> = _meter
    fun selectedMeter(meter: ElectricMeter) { _meter.value = meter }

    fun getElectricMeterList() = dao.getMeters()
    suspend fun saveElectricMeter(meter:ElectricMeter) = withContext(Dispatchers.IO){ dao.saveElectricMeter(meter) }
    suspend fun deleteElectricMeter(meter:ElectricMeter) = withContext(Dispatchers.IO){ dao.deleteElectricMeter(meter) }

}