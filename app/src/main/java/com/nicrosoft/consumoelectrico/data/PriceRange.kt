package com.nicrosoft.consumoelectrico.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*


@Entity(
        tableName = "price_range",
        foreignKeys = [ForeignKey(
         entity = ElectricMeter::class,
                onDelete = ForeignKey.CASCADE,
                parentColumns = ["id"],
                childColumns = ["meter_id"]
        )]
)
data class PriceRange(
        @PrimaryKey(autoGenerate = true) val id:Int,
        @ColumnInfo(name = "code") val code:String = UUID.randomUUID().toString(),
        @ColumnInfo(name = "from_kw") val fromKw:Int = 0,
        @ColumnInfo(name = "to_kw") val toKw:Int = 0,
        @ColumnInfo(name = "price") val price:Float = 0f,
        @ColumnInfo(name = "meter_id") val meterId:Int
)