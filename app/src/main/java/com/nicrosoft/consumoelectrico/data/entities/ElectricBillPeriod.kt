package com.nicrosoft.consumoelectrico.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*


@Entity(
        tableName = "electric_bill_period",
        foreignKeys = [ForeignKey(
         entity = ElectricMeter::class,
                onDelete = ForeignKey.CASCADE,
                parentColumns = ["id"],
                childColumns = ["meter_id"]
        )]
)
data class ElectricBillPeriod(
        @PrimaryKey(autoGenerate = true) var id:Int? = null,
        @ColumnInfo(name = "code") var code:String = UUID.randomUUID().toString(),
        @ColumnInfo(name = "from_date") var fromKw:Date,
        @ColumnInfo(name = "to_date") var toKw:Date,
        @ColumnInfo(name = "active") var active:Boolean,
        @ColumnInfo(name = "total_bill") var totalBill:Float = 0f,
        @ColumnInfo(name = "total_kw") var totalKw:Float = 0f,
        @ColumnInfo(name = "meter_id") var meterId:Int,
        @ColumnInfo(name = "created_at") var createdAt:Date
)