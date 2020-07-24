package com.nicrosoft.consumoelectrico.data.entities

import androidx.room.*
import java.util.*


@Entity(
        tableName = "electric_meter_reading",
        foreignKeys = [ForeignKey(entity = ElectricBillPeriod::class,
                onDelete = ForeignKey.CASCADE,
                parentColumns = ["code"],
                childColumns = ["period_code"]
        ), ForeignKey(entity = ElectricMeter::class,
                onDelete = ForeignKey.CASCADE,
                parentColumns = ["code"],
                childColumns = ["meter_code"]
        )
        ],
        indices = [Index(value = ["code"], unique = true)]
)
data class ElectricReading(
        @PrimaryKey(autoGenerate = true) var id:Int? = null,
        @ColumnInfo(name = "code") var code:String = UUID.randomUUID().toString(),
        @ColumnInfo(name = "period_code") var periodCode:String? = null,
        @ColumnInfo(name = "meter_code") var meterCode:String? = null,
        @ColumnInfo(name = "reading_date") var readingDate:Date = Date(),
        @ColumnInfo(name = "reading_value") var readingValue:Float = 0f,
        @ColumnInfo(name = "kw_consumption") var kwConsumption:Float = 0f,
        @ColumnInfo(name = "kw_avg_consumption") var kwAvgConsumption:Float = 0f,
        @ColumnInfo(name = "kw_agg_consumption") var kwAggConsumption:Float = 0f,
        @ColumnInfo(name = "consumption_hours") var consumptionHours:Float = 0f,
        @ColumnInfo(name = "consumption_previous_hours") var consumptionPreviousHours:Float = 0f,
        @ColumnInfo(name = "comments") var comments:String = ""

)