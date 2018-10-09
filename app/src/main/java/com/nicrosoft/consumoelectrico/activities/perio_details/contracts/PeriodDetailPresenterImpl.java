package com.nicrosoft.consumoelectrico.activities.perio_details.contracts;

import android.content.Context;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.nicrosoft.consumoelectrico.activities.perio_details.contracts.PeriodDetailsPresenter;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Medidor;
import com.nicrosoft.consumoelectrico.realm.Periodo;

/**
 * Created by Eder Xavier Rojas on 01/12/2017.
 */

public class PeriodDetailPresenterImpl implements PeriodDetailsPresenter {

    private PeriodDetailsService service;
    private Context context;

    public PeriodDetailPresenterImpl(Context context) {
        this.service = new PeriodDetailServiceImpl(context);
    }

    @Override
    public Periodo getActivePeriod(String medidor_id) {
        return service.getActivePeriod(medidor_id);
    }

    @Override
    public Lectura getFirstReading(Periodo periodo) {
        return service.getFirstReading(periodo);
    }

    @Override
    public Lectura getLastReading(Periodo periodo) {
        return service.getLastReading(periodo);
    }

    @Override
    public int getConsumptionLimit() {
        return service.getConsumptionLimit();
    }

    @Override
    public int getPeriodLength() {
        return service.getPeriodLength();
    }

    @Override
    public float getEstimatedConsumption(Lectura last) {
        return service.getEstimatedConsumption(last);
    }

    @Override
    public float getEstimatedExpense(Lectura last) {
        return service.getEstimatedExpense(last);
    }

    @Override
    public float getEstimatedExpenseWithNoDiscount(Lectura last) {
        return service.getEstimatedExpenseWithNoDiscount(last);
    }

    @Override
    public LineChart setReadingHistory(LineChart chart, Periodo periodo) {
        return service.setReadingHistory(chart, periodo);
    }

    @Override
    public LineChart setAvgHistory(LineChart chart, Periodo periodo) {
        return service.setAvgHistory(chart, periodo);
    }

    @Override
    public BarChart setPeriodHistory(BarChart chart, String medidor) {
        return service.setPeriodHistory(chart, medidor);
    }

    @Override
    public void onCreate() {
        service.onCreate();
    }

    @Override
    public void onDestroy() {
        service.onDestroy();
    }
}
