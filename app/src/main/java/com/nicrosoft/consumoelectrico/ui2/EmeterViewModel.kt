package com.nicrosoft.consumoelectrico.ui2

import android.content.Context
import androidx.lifecycle.ViewModel
import com.nicrosoft.consumoelectrico.data.daos.ElectricMeterDAO
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmeterViewModel(val context: Context, private val dao:ElectricMeterDAO) : ViewModel() {

    suspend fun getElectricMeterList() = withContext(Dispatchers.IO){ return@withContext dao.getMeters() }
    suspend fun saveElectricMeter(meter:ElectricMeter) = dao.saveElectricMeter(meter)

}