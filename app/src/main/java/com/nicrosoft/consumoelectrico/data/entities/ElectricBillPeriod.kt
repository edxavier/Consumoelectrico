package com.nicrosoft.consumoelectrico.data.entities

import androidx.room.*
import java.util.*


@Entity(
        tableName = "electric_bill_period",
        foreignKeys = [ForeignKey(
         entity = ElectricMeter::class,
                onDelete = ForeignKey.CASCADE,
                parentColumns = ["code"],
                childColumns = ["meter_code"]
        )],
        indices = [Index(value = ["code"], unique = true)]
)
data class ElectricBillPeriod(
        @PrimaryKey(autoGenerate = true) var id:Int? = null,
        @ColumnInfo(name = "code") var code:String = UUID.randomUUID().toString(),
        @ColumnInfo(name = "from_date") var fromDate:Date,
        @ColumnInfo(name = "to_date") var toDate:Date? = null,
        @ColumnInfo(name = "active") var active:Boolean = true,
        @ColumnInfo(name = "total_bill") var totalBill:Float = 0f,
        @ColumnInfo(name = "total_kw") var totalKw:Float = 0f,
        @ColumnInfo(name = "meter_code") var meterCode:String,
        @ColumnInfo(name = "created_at") var createdAt:Date = Date()
)