package com.nicrosoft.consumoelectrico.data

import androidx.room.TypeConverter
import java.util.*

/**
 * Type converters to allow Room to reference complex data types.
 */
class Converters {
    @TypeConverter
    fun calendarToDateStamp(calendar: Date): Long = calendar.time

    @TypeConverter fun dateStampToCalendar(value: Long): Date =
            Date(value)
}