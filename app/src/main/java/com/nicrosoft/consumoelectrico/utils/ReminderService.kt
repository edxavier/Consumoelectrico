package com.nicrosoft.consumoelectrico.utils

import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.realm.Lectura
import com.nicrosoft.consumoelectrico.utils.helpers.NotificationHelper
import com.pixplicity.easyprefs.library.Prefs
import io.realm.Realm
import io.realm.Sort
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.util.*

class ReminderService : JobService() {
    override fun onStartJob(job: JobParameters): Boolean {
        // Answers the question: "Is there still work going on?"
        //Log.e("EDER", "ReminderService START");
        return try {
            val realm = Realm.getDefaultInstance()
            val res = realm.where(Lectura::class.java)
                    .findAll().sort("fecha_lectura", Sort.DESCENDING)
            if (res.size > 0) {
                val lectura = res.first()
                if (lectura != null) {
                    var dias_recordatorio = 5
                    try {
                        dias_recordatorio = Prefs.getString("reminder_after", "7").toInt()
                    } catch (ignored: Exception) {
                    }
                    val days = Days.daysBetween(LocalDate(lectura.fecha_lectura), LocalDate(Date()))
                    if (days.days > dias_recordatorio) {
                        val now = LocalDateTime.now()
                        val hour = now.hourOfDay
                        if (hour in 7..21) {
                            val helper = NotificationHelper(applicationContext)
                            helper.createNotification(applicationContext.getString(R.string.reminder), applicationContext.getString(R.string.reminder_msg, days.days.toString()))
                        }
                    }
                }
            }
            realm.close()
            false
        } catch (f: Exception) {
            false
        }
    }

    override fun onStopJob(job: JobParameters): Boolean {
        // si el trabajo falla retornar true para reintentar
        return true
    }

    companion object {
        const val JOB_TAG = "ReminderService"
    }
}