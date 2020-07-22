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
data class ElectricReading(
        @PrimaryKey(autoGenerate = true) var id:Int? = null,
        @ColumnInfo(name = "code") var code:String = UUID.randomUUID().toString(),
        @ColumnInfo(name = "period_id") var periodId:Int? = null,
        @ColumnInfo(name = "reading_date") var readingDate:Date = Date(),
        @ColumnInfo(name = "reading_value") var readingValue:Float = 0f,
        @ColumnInfo(name = "kw_consumption") var kwConsumption:Float = 0f,
        @ColumnInfo(name = "kw_avg_consumption") var kwAvgConsumption:Float = 0f,
        @ColumnInfo(name = "kw_agg_consumption") var kwAggConsumption:Float = 0f
)