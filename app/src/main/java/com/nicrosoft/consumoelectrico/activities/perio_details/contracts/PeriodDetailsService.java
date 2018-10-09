package com.nicrosoft.consumoelectrico.activities.perio_details.contracts;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Medidor;
import com.nicrosoft.consumoelectrico.realm.Periodo;

/**
 * Created by Eder Xavier Rojas on 01/12/2017.
 */

public interface PeriodDetailsService {
    Periodo getActivePeriod(String medidor_id);
    Lectura getFirstReading(Periodo periodo);
    Lectura getLastReading(Periodo periodo);
    int getConsumptionLimit();
    int getPeriodLength();
    float getEstimatedConsumption(Lectura last);
    float getEstimatedExpense(Lectura last);
    float getEstimatedExpenseWithNoDiscount(Lectura last);

    LineChart setReadingHistory(LineChart chart, Periodo periodo);
    LineChart setAvgHistory(LineChart chart, Periodo periodo);
    BarChart setPeriodHistory(BarChart chart, String medidor_id);

    void onCreate();
    void onDestroy();


}
