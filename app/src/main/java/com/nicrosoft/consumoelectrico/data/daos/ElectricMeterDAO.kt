package com.nicrosoft.consumoelectrico.data.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.data.entities.PriceRange

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

    //@Query("SELECT * FROM price_range where from_kw BETWEEN :min AND :max OR to_kw BETWEEN :min AND :max")
    //fun countOverlappingPrices(min:Int, max:Int): List<PriceRange>
}