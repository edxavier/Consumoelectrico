package com.nicrosoft.consumoelectrico.data

import androidx.room.*
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import java.util.*


data class ExpenseDetail(
        var energy:Float = 0f,
        var discount:Float = 0f,
        var taxes:Float = 0f,
        var fixed:Float = 0f,
        var total:Float = 0f
)