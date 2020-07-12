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
        @PrimaryKey(autoGenerate = true) val id:Int,
        @ColumnInfo(name = "code") val code:String = UUID.randomUUID().toString(),
        @ColumnInfo(name = "from_date") val fromKw:Date,
        @ColumnInfo(name = "to_date") val toKw:Date,
        @ColumnInfo(name = "active") val active:Boolean,
        @ColumnInfo(name = "total_bill") val totalBill:Float = 0f,
        @ColumnInfo(name = "total_kw") val totalKw:Float = 0f,
        @ColumnInfo(name = "meter_id") val meterId:Int,
        @ColumnInfo(name = "created_at") val createdAt:Date
)