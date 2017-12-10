package com.nicrosoft.consumoelectrico.fragments.main.contracts;

import com.nicrosoft.consumoelectrico.realm.Periodo;

import java.util.Date;

/**
 * Created by Eder Xavier Rojas on 11/01/2017.
 */

public interface MainPresenter {
    void getResumeData(boolean afterSave);
    void getHistoryReadings();
    boolean isRecordsEmpty();
    boolean thereIsRecordForDate(Date date);
    boolean endPeriod(Date date);
    Periodo getActivePeriod();
}
