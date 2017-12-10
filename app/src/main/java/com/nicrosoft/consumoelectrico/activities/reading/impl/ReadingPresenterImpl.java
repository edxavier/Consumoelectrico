package com.nicrosoft.consumoelectrico.activities.reading.impl;

import com.nicrosoft.consumoelectrico.activities.reading.contracts.ReadingPresenter;
import com.nicrosoft.consumoelectrico.activities.reading.contracts.ReadingService;
import com.nicrosoft.consumoelectrico.activities.reading.contracts.ReadingView;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Medidor;
import com.nicrosoft.consumoelectrico.realm.Periodo;

import java.util.Date;

/**
 * Created by Eder Xavier Rojas on 16/01/2017.
 */

public class ReadingPresenterImpl implements ReadingPresenter {
    private ReadingService service;
    private ReadingView view;

    public ReadingPresenterImpl(ReadingView view) {
        this.view = view;
        this.service = new ReadingServiceImpl();
    }

    @Override
    public Lectura getLastReading(Periodo periodo, boolean old_readings_if_more_than_a_period) {
        return service.getLastReading(periodo, old_readings_if_more_than_a_period);
    }

    @Override
    public Boolean readingForDateExist(Date date, Periodo periodo) {
        return service.readingForDateExist(date, periodo);
    }

    @Override
    public boolean saveReading(Lectura lectura, boolean finishPeriod, String medidor_id) {
        return service.saveReading(lectura, finishPeriod, medidor_id);
    }

    @Override
    public boolean isReadingOverRange(float reading, Date date, Periodo periodo) {
        return service.isReadingOverRange(reading, date, periodo);
    }

    @Override
    public Periodo getActivePeriod(String medidor_id) {
        return service.getActivePeriod(medidor_id);
    }
}
