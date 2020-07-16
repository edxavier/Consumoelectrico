package com.nicrosoft.consumoelectrico.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity(tableName = "electric_meter")
data class ElectricMeter(
        @PrimaryKey(autoGenerate = true) val id:Int? = null,
        @ColumnInfo(name = "code") var code:String = UUID.randomUUID().toString(),
        @ColumnInfo(name = "name") var name:String,
        @ColumnInfo(name = "description") var description:String? = "",
        @ColumnInfo(name = "kw_price") var kwPrice:Float = 0f,
        @ColumnInfo(name = "fixed_prices") var fixedPrices:Float = 0f,
        @ColumnInfo(name = "taxes") var taxes:Float = 0f,
        @ColumnInfo(name = "kw_discount") var kwDiscount:Float = 0f,
        @ColumnInfo(name = "max_kw_limit") var maxKwLimit:Int = 150,
        @ColumnInfo(name = "lose_discount_over_limit") var loseDiscount:Boolean = true,
        @ColumnInfo(name = "period_length") var periodLength:Int  = 30,
        @ColumnInfo(name = "read_reminder") var readReminder:Int  = 14
){


    fun equals(other:ElectricMeter):Boolean {
        if(this.id != other.id)
            return false
        if(this.name != other.name)
            return false
        if(this.description != other.description)
            return false
        if(this.fixedPrices != other.fixedPrices)
            return false
        if(this.maxKwLimit != other.maxKwLimit)
            return false
        if(this.periodLength != other.periodLength)
            return false
        return true
    }
}