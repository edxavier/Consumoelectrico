package com.nicrosoft.consumoelectrico.fragments.readings.contracts;

import com.nicrosoft.consumoelectrico.realm.Lectura;

/**
 * Created by Eder Xavier Rojas on 17/02/2017.
 */

public interface LecturasPresenter {
    void onDestroy();
    void getReadings(String medidor_id, boolean get_all);
    void updateReading(Lectura lectura, String newReading);
    void endPeriod(Lectura lectura);
    boolean isValueOverange(Lectura lectura, String newValue);
    boolean deleteEntry(Lectura lectura);


}
