package com.nicrosoft.consumoelectrico.data.daos

import androidx.room.*
import com.nicrosoft.consumoelectrico.data.entities.ElectricBillPeriod
import com.nicrosoft.consumoelectrico.data.entities.PriceRange

@Dao
interface ElectricBillPeriodDAO {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun saveElectricBillPeriod(period: ElectricBillPeriod)

    @Delete
    fun deleteElectricBillPeriod(period: ElectricBillPeriod)

    @Update
    fun updateElectricBillPeriod(period: ElectricBillPeriod)


    // QUERIES
    @Query("SELECT * FROM electric_bill_period where meter_id=:meter_id")
    fun getElectricBillPeriods(meter_id:Int): List<PriceRange>

    @Query("SELECT * FROM electric_bill_period where id=:period_id")
    fun getElectricBillPeriod(period_id:Int): ElectricBillPeriod?

    @Query("SELECT * FROM electric_bill_period where id=:period_id and active=1")
    fun getActiveElectricBillPeriod(period_id:Int): ElectricBillPeriod?
}