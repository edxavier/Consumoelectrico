package com.nicrosoft.consumoelectrico.fragments.readings.contracts;

import com.nicrosoft.consumoelectrico.realm.Lectura;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 17/02/2017.
 */

public interface LecturasService {
    RealmResults<Lectura> getReadings(String medidor_id, boolean get_all);

    void onDestroy();

    void updateReading(Lectura lectura, String newReading);
    void endPeriod(Lectura lectura);
    boolean deleteEntry(Lectura lectura);
    boolean isValueOverange(Lectura lectura, String newValue);

}
