package com.nicrosoft.consumoelectrico.fragments.main.imp;

import android.content.Context;

import com.nicrosoft.consumoelectrico.fragments.main.contracts.MainPresenter;
import com.nicrosoft.consumoelectrico.fragments.main.contracts.MainService;
import com.nicrosoft.consumoelectrico.fragments.main.contracts.MainView;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Periodo;

import java.util.Date;

/**
 * Created by Eder Xavier Rojas on 11/01/2017.
 */

public class MainPresenterImpl implements MainPresenter {

    Context context;
    MainView view;
    MainService service;

    public MainPresenterImpl(Context context, MainView view) {
        this.context = context;
        this.view = view;
        this.service = new MainServiceImpl(this.context);
    }

    @Override
    public void getResumeData(boolean afterSave) {
       Lectura lectura =  service.getResumeData();
        if(lectura == null)
            view.showEmptyDataMsg();
        else
            view.setResumeData(lectura, afterSave);
    }

    @Override
    public void getHistoryReadings() {
        view.setHistoryReadings(service.getHistoryReadings());
    }

    @Override
    public boolean isRecordsEmpty() {
        return service.isRecordsEmpty();
    }

    @Override
    public boolean thereIsRecordForDate(Date date) {
        return service.thereIsRecordForDate(date);
    }

    @Override
    public boolean endPeriod(Date date) {
        return service.endPeriod(date);
    }

    @Override
    public Periodo getActivePeriod() {
        return service.getActivePeriod();
    }
}
