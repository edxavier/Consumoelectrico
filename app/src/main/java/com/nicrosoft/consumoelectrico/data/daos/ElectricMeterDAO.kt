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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveElectricMeter(meter: ElectricMeter)

    @Delete
    fun deleteElectricMeter(meter: ElectricMeter)

    @Delete
    fun deleteBillingPeriod(period: ElectricBillPeriod)

    @Delete
    fun deleteElectricReading(reading: ElectricReading)

    @Update
    fun updateElectricMeter(meter: ElectricMeter):Int

    @Update
    fun updateElectricReading(reading: ElectricReading):Int
    @Update
    fun updatePeriod(reading: ElectricBillPeriod):Int

    //PRICE RANGE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePriceRage(range: PriceRange)

    //READING
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveReading(reading: ElectricReading)

    //READING
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePeriod(period: ElectricBillPeriod)

    @Delete
    fun deletePriceRage(range: PriceRange)

    @Update
    fun updatePriceRage(range: PriceRange)

    @Query("SELECT * FROM price_range where meter_code=:meterCode order by from_kw")
    fun getPriceRanges(meterCode:String): LiveData<List<PriceRange>>

    @Query("SELECT * FROM price_range where meter_code=:meterCode order by from_kw")
    fun getPricesList(meterCode:String): List<PriceRange>

    @Query("SELECT * FROM price_range where meter_code=:meterCode order by from_kw desc limit 1")
    fun getLastPriceRange(meterCode:String): PriceRange?

    @Query("SELECT * FROM price_range where meter_code=:meterCode and from_kw >:priceFrom  order by from_kw asc limit 1")
    fun getNextPriceRange(meterCode:String, priceFrom:Int): PriceRange?

    @Query("SELECT * FROM price_range where meter_code=:meterCode and from_kw <:priceFrom order by from_kw desc limit 1")
    fun getPreviousPriceRange(meterCode:String, priceFrom:Int): PriceRange?

    @Query("SELECT * FROM electric_meter order by id desc")
    fun getMeters(): List<ElectricMeter>


    @Query("SELECT * FROM electric_bill_period where meter_code=:meterCode order by from_date desc")
    fun getMeterPeriods(meterCode: String): List<ElectricBillPeriod>

    @Query("SELECT * FROM electric_bill_period where meter_code=:meterCode order by from_date desc")
    fun getAllPeriods(meterCode: String):List<ElectricBillPeriod>

    @Query("SELECT * FROM electric_meter where code=:meterCode order by id desc")
    fun getMeter(meterCode: String): ElectricMeter

    @Query("SELECT * FROM electric_bill_period where code=:periodCode order by from_date desc")
    fun getPeriod(periodCode: String): ElectricBillPeriod

    @Query("SELECT * FROM electric_meter_reading where period_code=:periodCode order by reading_date desc")
    fun getPeriodMetersReadings(periodCode: String): List<ElectricReading>

    @Query("SELECT * FROM electric_meter_reading where period_code=:periodCode order by reading_date asc")
    fun getPeriodReadings(periodCode: String): List<ElectricReading>

    @Query("SELECT count(*) FROM electric_meter_reading where period_code=:periodCode")
    fun getTotalPeriodReading(periodCode: String): Int

    @Query("SELECT * FROM electric_meter_reading where meter_code=:meterCode order by id desc")
    fun getAllMeterReadings(meterCode: String): List<ElectricReading>
    @Query("SELECT * FROM electric_meter_reading where meter_code=:meterCode order by id asc")
    fun getAllMeterReadingsAsc(meterCode: String): List<ElectricReading>

    @Query("SELECT * FROM electric_meter_reading where meter_code=:meterCode order by reading_date asc limit 1")
    fun getFirstMeterReading(meterCode: String): ElectricReading

    //Cargar los rangos que se sobreponen o contienen al rango especificado
    @Query("SELECT * FROM price_range where meter_code=:meterCode AND ((from_kw BETWEEN :min AND :max OR to_kw BETWEEN :min AND :max) OR (:min BETWEEN from_kw AND to_kw AND :max BETWEEN from_kw AND to_kw))")
    fun getOverlappingPrice(min:Int, max:Int, meterCode: String): PriceRange?

    @Query("SELECT * FROM electric_meter_reading where period_code=:periodCode order by reading_date desc limit 1")
    fun getLastPeriodReading(periodCode: String): ElectricReading?

    @Query("SELECT * FROM electric_meter_reading where period_code=:periodCode order by reading_date asc limit 1")
    fun getFirstPeriodReading(periodCode: String): ElectricReading?

    @Query("SELECT * FROM electric_meter_reading where period_code=:periodCode AND reading_date>:readingDate order by reading_date asc limit 1")
    fun getNextReading(periodCode: String, readingDate: Date): ElectricReading?

    @Query("SELECT * FROM electric_meter_reading where period_code=:periodCode AND reading_date<:readingDate order by reading_date desc limit 1")
    fun getPreviousReading(periodCode: String, readingDate: Date): ElectricReading?

    @Query("SELECT * FROM electric_meter_reading where period_code=:periodCode AND reading_date>:readingDate order by reading_date")
    fun getReadingsAfter(periodCode: String, readingDate: Date): List<ElectricReading>

    @Query("SELECT * FROM electric_meter_reading where meter_code=:meterCode AND reading_date<:readingDate order by reading_date desc limit 1")
    fun getLastMeterReading(meterCode: String, readingDate: Date): ElectricReading?


    @Query("SELECT * FROM electric_bill_period where meter_code=:meterCode order by from_date desc limit 1")
    fun getLastElectricPeriod(meterCode: String): ElectricBillPeriod?


    @Query("SELECT count(*) FROM electric_meter_reading where meter_code=:meterCode AND reading_date>:readingDate AND reading_value<:readingValue")
    fun countInvalidFutureReadings(readingDate: Date, readingValue:Float, meterCode: String): Int

    @Query("SELECT count(*) FROM electric_meter_reading where meter_code=:meterCode AND reading_date<:readingDate AND :readingValue<reading_value")
    fun countInvalidPastReadings(readingDate: Date, readingValue:Float, meterCode: String): Int

    @Query("SELECT sum(kw_consumption) FROM electric_meter_reading where period_code=:periodCode")
    fun getTotalPeriodKw(periodCode: String): Float

}
