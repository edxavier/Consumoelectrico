package com.nicrosoft.consumoelectrico.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
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
        @PrimaryKey(autoGenerate = true) var id:Int? = null,
        @ColumnInfo(name = "code") var code:String = UUID.randomUUID().toString(),
        @ColumnInfo(name = "from_kw") var fromKw:Int = 0,
        @ColumnInfo(name = "to_kw") var toKw:Int = 0,
        @ColumnInfo(name = "price") var price:Float = 0f,
        @ColumnInfo(name = "meter_id") var meterId:Int
)