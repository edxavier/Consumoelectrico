package com.nicrosoft.consumoelectrico.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ElectricMeterDAO {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun saveElectricMeter(meter: ElectricMeter)

    @Delete
    fun deleteElectricMeter(meter: ElectricMeter)

    @Update
    fun updateElectricMeter(meter: ElectricMeter)


    //PRICE RANGE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePriceRage(range: PriceRange)

    @Delete
    fun deletePriceRage(range: PriceRange)

    @Update
    fun updatePriceRage(range: PriceRange)

    // QUERIES
    @Query("SELECT * FROM price_range where meter_id=:meter_id")
    fun getPriceRanges(meter_id:Int): List<PriceRange>

    @Query("SELECT * FROM electric_meter")
    fun getMeters(): LiveData<List<ElectricMeter>>
}