package com.nicrosoft.consumoelectrico.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*


@Entity(
        tableName = "electric_meter_reading",
        foreignKeys = [ForeignKey(
         entity = ElectricBillPeriod::class,
                onDelete = ForeignKey.CASCADE,
                parentColumns = ["id"],
                childColumns = ["period_id"]
        )]
)
data class ElectricMeterReading(
        @PrimaryKey(autoGenerate = true) val id:Int,
        @ColumnInfo(name = "code") val code:String = UUID.randomUUID().toString(),
        @ColumnInfo(name = "period_id") val periodId:Int,
        @ColumnInfo(name = "reading_date") val readingDate:Date,
        @ColumnInfo(name = "reading_value") val totalBill:Float = 0f
)