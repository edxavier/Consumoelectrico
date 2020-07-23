package com.nicrosoft.consumoelectrico.data.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.nicrosoft.consumoelectrico.data.entities.ElectricBillPeriod
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.data.entities.PriceRange
import java.util.*

@Dao
interface ElectricMeterDAO {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun saveElectricMeter(meter: ElectricMeter)

    @Delete
    fun deleteElectricMeter(meter: ElectricMeter)

    @Update
    fun updateElectricMeter(meter: ElectricMeter):Int


    //PRICE RANGE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePriceRage(range: PriceRange)

    //PRICE READING
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun saveReading(reading: ElectricReading)

    //PRICE READING
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun savePeriod(period: ElectricBillPeriod)

    @Delete
    fun deletePriceRage(range: PriceRange)

    @Update
    fun updatePriceRage(range: PriceRange)

    // QUERIES
    @Query("SELECT * FROM price_range where meter_id=:meter_id order by from_kw")
    fun getPriceRanges(meter_id:Int): LiveData<List<PriceRange>>

    @Query("SELECT * FROM electric_meter order by id desc")
    fun getMeters(): LiveData<List<ElectricMeter>>

    //Cargar los rangos que se sobreponen o contienen al rango especificado
    @Query("SELECT * FROM price_range where from_kw BETWEEN :min AND :max OR to_kw BETWEEN :min AND :max OR (:min BETWEEN from_kw AND to_kw AND :max BETWEEN from_kw AND to_kw)")
    fun getOverlappingPrice(min:Int, max:Int): PriceRange?

    @Query("SELECT * FROM electric_meter_reading where period_id=:period_id order by reading_date desc limit 2")
    fun getLastTwoElectricReadings(period_id: Int): List<ElectricReading>

    @Query("SELECT * FROM electric_meter_reading order by reading_date desc limit 2")
    fun getLastTwoElectricReadings(): List<ElectricReading>

    @Query("SELECT * FROM electric_bill_period where meter_id=:meter_id order by from_date desc limit 1")
    fun getLastElectricPeriod(meter_id: Int): ElectricBillPeriod?

    @Query("SELECT count(*) FROM electric_meter_reading where reading_date>:readingDate AND reading_value<:readingValue")
    fun countInvalidReadings(readingDate: Date, readingValue:Float): Int

}
