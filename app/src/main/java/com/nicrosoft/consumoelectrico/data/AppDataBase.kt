package com.nicrosoft.consumoelectrico.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nicrosoft.consumoelectrico.data.daos.ElectricMeterDAO
import com.nicrosoft.consumoelectrico.data.entities.ElectricBillPeriod
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeterReading
import com.nicrosoft.consumoelectrico.data.entities.PriceRange

@TypeConverters(Converters::class)
@Database(entities = [
    ElectricMeter::class, ElectricBillPeriod::class, PriceRange::class, ElectricMeterReading::class
], version = 1)
abstract class AppDataBase:RoomDatabase() {
    abstract fun ElectricMeterDao(): ElectricMeterDAO

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: AppDataBase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance?: buildDataBase(context).also { instance = it }
        }

        private fun buildDataBase(context: Context): AppDataBase {
            return Room.databaseBuilder(context.applicationContext, AppDataBase::class.java, "app_ceh.db")
                    .fallbackToDestructiveMigration()
                    //.addMigrations(FROM_1_TO_2)
                    .build()
        }
    }
}