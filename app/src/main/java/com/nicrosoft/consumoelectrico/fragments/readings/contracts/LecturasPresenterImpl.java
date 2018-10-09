package com.nicrosoft.consumoelectrico.fragments.readings.contracts;

import android.util.Log;

import com.nicrosoft.consumoelectrico.realm.Lectura;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 17/02/2017.
 */

public class LecturasPresenterImpl implements LecturasPresenter {

    LecturasView view;
    LecturasService service;

    public LecturasPresenterImpl(LecturasView view) {
        this.view = view;
        this.service = new LecturasServiceImpl();
    }

    @Override
    public void onDestroy() {
        service.onDestroy();
    }

    @Override
    public void getReadings(String medidor, boolean get_all) {
        RealmResults<Lectura> results = service.getReadings(medidor, get_all);
        view.showEmptyMsg(results.isEmpty());
        view.setReadings(results);
    }

    @Override
    public void updateReading(Lectura lect, String reading) {
        service.updateReading(lect, reading);
    }

    @Override
    public void endPeriod(Lectura lectura) {
        service.endPeriod(lectura);
    }

    @Override
    public boolean isValueOverange(Lectura lectura, String newValue) {
        return service.isValueOverange(lectura, newValue);
    }

    @Override
    public boolean deleteEntry(Lectura lectura) {
        return service.deleteEntry(lectura);
    }
}
