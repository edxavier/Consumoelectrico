package com.nicrosoft.consumoelectrico.myUtils;

import androidx.appcompat.app.AppCompatDelegate;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.nicrosoft.consumoelectrico.R;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.pixplicity.easyprefs.library.Prefs;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.Date;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ReminderService extends JobService {
    public static final String JOB_TAG = "ReminderService";
    CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    public boolean onStartJob(JobParameters job) {
        // Answers the question: "Is there still work going on?"
        //Log.e("EDER", "ReminderService START");
        try {
            Realm realm = Realm.getDefaultInstance();
            RealmResults<Lectura> res = realm.where(Lectura.class)
                    .findAll().sort("fecha_lectura", Sort.DESCENDING);
            if (res.size() > 0) {
                Lectura lectura = res.first();
                if (lectura != null) {
                    int dias_recordatorio = 5;
                    try {
                        dias_recordatorio = Integer.parseInt(Prefs.getString("reminder_after", "7"));
                    } catch (Exception ignored) {
                    }
                    Days days = Days.daysBetween(new LocalDate(lectura.fecha_lectura), new LocalDate(new Date()));
                    if (days.getDays() > dias_recordatorio) {
                        LocalDateTime now = LocalDateTime.now();
                        int hour = now.getHourOfDay();
                        if (hour >= 7 && hour <= 21) {
                            NotificationHelper helper = new NotificationHelper(getApplicationContext());
                            helper.createNotification(getApplicationContext().getString(R.string.reminder), getApplicationContext().getString(R.string.reminder_msg, String.valueOf(days.getDays())));
                        }
                    }
                }
            }
            realm.close();
            return false;
        } catch (Exception f){
            return false;
        }
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        // si el trabajo falla retornar true para reintentar
        return true;
    }
}
