package com.nicrosoft.consumoelectrico.data

import com.nicrosoft.consumoelectrico.data.entities.ElectricBillPeriod
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.data.entities.PriceRange

class BackupSkeleton (
        var meters:MutableList<ElectricMeter> = ArrayList(),
        var prices:MutableList<PriceRange> = ArrayList(),
        var periods:MutableList<ElectricBillPeriod> = ArrayList(),
        var readings:MutableList<ElectricReading> = ArrayList()
)