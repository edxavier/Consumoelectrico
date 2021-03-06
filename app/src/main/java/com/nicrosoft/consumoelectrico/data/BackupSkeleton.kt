package com.nicrosoft.consumoelectrico.data

import com.nicrosoft.consumoelectrico.data.entities.ElectricBillPeriod
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.data.entities.PriceRange

class BackupSkeleton (
        var meters:List<ElectricMeter> = ArrayList<ElectricMeter>(),
        var prices:List<PriceRange> = ArrayList<PriceRange>(),
        var periods:List<ElectricBillPeriod> = ArrayList<ElectricBillPeriod>(),
        var readings:List<ElectricReading> = ArrayList<ElectricReading>()
)