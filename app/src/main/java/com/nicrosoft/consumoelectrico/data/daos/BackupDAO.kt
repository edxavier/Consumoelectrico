package com.nicrosoft.consumoelectrico.data.daos

import androidx.room.*
import com.nicrosoft.consumoelectrico.data.entities.ElectricBillPeriod
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.data.entities.PriceRange

@Dao
interface BackupDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveMeters(meter: List<ElectricMeter>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePrices(meter: List<PriceRange>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePeriods(meter: List<ElectricBillPeriod>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveReadings(meter: List<ElectricReading>)


    // QUERIES
    @Query("SELECT * FROM electric_meter where code=:meterCode")
    fun checkMeterExist(meterCode:String): ElectricMeter?
    @Query("SELECT * FROM electric_bill_period where code=:periodCode")
    fun checkPeriodExist(periodCode:String): ElectricBillPeriod?
    @Query("SELECT * FROM price_range where code=:priceCode")
    fun checkPriceExist(priceCode:String): PriceRange?
    @Query("SELECT * FROM electric_meter_reading where code=:readingCode")
    fun checkReadingExist(readingCode:String): ElectricReading?

    //BACKUP RESTORE
    @Query("SELECT * FROM electric_meter order by id desc")
    suspend fun getMeterList(): List<ElectricMeter>

    @Query("SELECT * FROM price_range order by id desc")
    suspend fun getPricesList(): List<PriceRange>

    @Query("SELECT * FROM electric_bill_period order by id desc")
    suspend fun getPeriodList(): List<ElectricBillPeriod>

    @Query("SELECT * FROM electric_meter_reading order by id desc")
    suspend fun getReadingList(): List<ElectricReading>

}