package com.nicrosoft.consumoelectrico.data

import android.util.Log
import com.squareup.moshi.*
import java.text.SimpleDateFormat
import java.util.*

class DateJsonAdapter: JsonAdapter<Date>() {
    private val dateFormat = SimpleDateFormat(SERVER_FORMAT, Locale.getDefault())
    private val dateFormat_US_EN = SimpleDateFormat(SERVER_FORMAT_US_EN, Locale.ENGLISH)
    //private val formatter  = DateTimeFormatter.ofPattern(SERVER_FORMAT, Locale.getDefault())

    @FromJson
    override fun fromJson(reader: JsonReader): Date? {
        val dateAsString = reader.nextString()
        return try {
            dateFormat_US_EN.parse(dateAsString)
        } catch (e: Exception) {
            return try {
                Log.e("EDER", "DEFAULT")
                dateFormat.parse(dateAsString)
            }catch (e:Exception){
                e.printStackTrace()
                null
            }
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Date?) {
        value?.let { writer.value(dateFormat_US_EN.format(value)) }
    }

    companion object {
        //MMMM dd, yyy hh:mm a
        const val SERVER_FORMAT = ("MMMM dd, yyy hh:mm a") // define your server format here
        const val SERVER_FORMAT_US_EN = ("yyy.MM.dd HH:mm") // define your server format here
    }
}