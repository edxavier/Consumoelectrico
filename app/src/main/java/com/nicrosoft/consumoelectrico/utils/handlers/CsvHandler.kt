package com.nicrosoft.consumoelectrico.utils.handlers

import android.content.Context
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.utils.formatDate
import com.nicrosoft.consumoelectrico.utils.toTwoDecimalPlace
import com.opencsv.CSVWriter
import java.io.*
import java.util.*

/**
 * Created by Eder Xavier Rojas on 14/18/2020.
 */
object CsvHandler {

    fun exportMeterReadings(readings:List<ElectricReading>, filePathName:String, ctx:Context):String? {
        try {
            val s = "$filePathName.csv"
            val writer = CSVWriter(FileWriter(s))
            val data: MutableList<Array<String>> = ArrayList()
            val titles = arrayOf(
                    ctx.getString(R.string.label_date),
                    ctx.getString(R.string.label_reading),
                    ctx.getString(R.string.label_days_consumption),
                    ctx.getString(R.string.label_consumption),
                    ctx.getString(R.string.period_consumption)
            )
            data.add(titles)

            readings.sortedBy { it.readingDate }.forEach { r ->
                data.add(
                        arrayOf(
                                r.readingDate.formatDate(ctx, true),
                                r.readingValue.toTwoDecimalPlace(),
                                (r.consumptionHours/24).toTwoDecimalPlace(),
                                r.kwConsumption.toTwoDecimalPlace(),
                                r.kwAggConsumption.toTwoDecimalPlace()
                        )
                )
            }
            writer.writeAll(data)
            writer.close()
            return s
        }catch (e:Exception){
            return null
        }
    }
}