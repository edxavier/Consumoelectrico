package com.nicrosoft.consumoelectrico.data.entities

import androidx.room.*
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import java.util.*


@Entity(
        tableName = "price_range",
        foreignKeys = [ForeignKey(
         entity = ElectricMeter::class,
                onDelete = ForeignKey.CASCADE,
                parentColumns = ["code"],
                childColumns = ["meter_code"]
        )],
        indices = [Index(value = ["code"], unique = true)]
)
data class PriceRange(
        @PrimaryKey(autoGenerate = true) var id:Int? = null,
        @ColumnInfo(name = "code") var code:String = UUID.randomUUID().toString(),
        @ColumnInfo(name = "from_kw") var fromKw:Int = 0,
        @ColumnInfo(name = "to_kw") var toKw:Int = 0,
        @ColumnInfo(name = "price") var price:Float = 0f,
        @ColumnInfo(name = "meter_code") var meterCode:String
)