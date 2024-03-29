package com.nicrosoft.consumoelectrico.ui2

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nicrosoft.consumoelectrico.data.daos.ElectricMeterDAO
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel

@Suppress("UNCHECKED_CAST")
class ElectricVMFactory(
        private val context: Context, private val dao:ElectricMeterDAO
): ViewModelProvider.NewInstanceFactory()  {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ElectricViewModel(context, dao) as T
    }
}