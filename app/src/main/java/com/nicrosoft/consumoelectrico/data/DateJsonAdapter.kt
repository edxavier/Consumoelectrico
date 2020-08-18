package com.nicrosoft.consumoelectrico.data

import android.util.Log
import com.squareup.moshi.*
import java.text.SimpleDateFormat
import java.util.*

class DateJsonAdapter: JsonAdapter<Date>() {
    private val dateFormat = SimpleDateFormat(SERVER_FORMAT, Locale.getDefault())

    @FromJson
    override fun fromJson(reader: JsonReader): Date? {
        return try {
            val dateAsString = reader.nextString()
            dateFormat.parse(dateAsString)
        } catch (e: Exception) {
            //Log.e("EDER_fromJson", e.toString())
            null
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Date?) {
        value?.let { writer.value(dateFormat.format(value)) }
    }

    companion object {
        //MMMM dd, yyy hh:mm a
        const val SERVER_FORMAT = ("MMMM dd, yyy hh:mm a") // define your server format here
    }
}