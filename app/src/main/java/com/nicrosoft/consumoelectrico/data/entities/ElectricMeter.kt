package com.nicrosoft.consumoelectrico.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity(tableName = "electric_meter")
data class ElectricMeter(
        @PrimaryKey(autoGenerate = true) val id:Int,
        @ColumnInfo(name = "code") val code:String = UUID.randomUUID().toString(),
        @ColumnInfo(name = "name") val name:String,
        @ColumnInfo(name = "description") val description:String?,
        @ColumnInfo(name = "kw_price") val kwPrice:Float = 0f,
        @ColumnInfo(name = "fixed_prices") val fixedPrices:Float = 0f,
        @ColumnInfo(name = "kw_discount") val kwDiscount:Float = 0f,
        @ColumnInfo(name = "max_kw_limit") val maxKwLimit:Int = 150,
        @ColumnInfo(name = "period_length") val periodLength:Int  = 30,
        @ColumnInfo(name = "read_reminder") val readReminder:Int  = 14
)