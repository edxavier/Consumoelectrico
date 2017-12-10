package com.nicrosoft.consumoelectrico.activities.perio_details.contracts;

import android.content.Context;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.realm.implementation.RealmBarDataSet;
import com.github.mikephil.charting.data.realm.implementation.RealmLineDataSet;
import com.nicrosoft.consumoelectrico.fragments.main.chart_helpers.ChartStyler;
import com.nicrosoft.consumoelectrico.fragments.main.chart_helpers.MyYAxisValueFormatter;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Periodo;
import com.pixplicity.easyprefs.library.Prefs;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Eder Xavier Rojas on 01/12/2017.
 */

public class PeriodDetailServiceImpl implements PeriodDetailsService {
    private Realm realm;
    private Context context;

    public PeriodDetailServiceImpl(Context context) {
        this.context = context;
    }

    @Override
    public Periodo getActivePeriod(String medidor_id) {
        return realm.where(Periodo.class)
                .equalTo("medidor.id", medidor_id)
                .equalTo("activo", true)
                .findFirst();
    }

    @Override
    public Lectura getFirstReading(Periodo periodo) {
        if(periodo!=null) {
            RealmResults<Lectura> res = realm.where(Lectura.class)
                    .equalTo("periodo.id", periodo.id)
                    .findAllSorted("fecha_lectura", Sort.DESCENDING);
            return (res.size()>0) ? res.last(): null;
        }else
            return null;
    }

    @Override
    public Lectura getLastReading(Periodo periodo) {
        if(periodo!=null) {
            RealmResults<Lectura> res = realm.where(Lectura.class)
                    .equalTo("periodo.id", periodo.id)
                    .findAllSorted("fecha_lectura", Sort.DESCENDING);
            return (res.size()>0) ? res.first(): null;
        }else
            return null;
    }

    @Override
    public int getConsumptionLimit() {
        return  Integer.parseInt(Prefs.getString("kw_limit", "150"));
    }

    @Override
    public int getPeriodLength() {
        return Integer.parseInt(Prefs.getString("period_lenght", "30"));
    }

    @Override
    public float getEstimatedConsumption(Lectura last) {
        float dias_periodo = getPeriodLength();
        int dias_restantes = (int) (dias_periodo - last.dias_periodo);
        return (dias_restantes * last.consumo_promedio) + last.consumo_acumulado;
    }

    @Override
    public float getEstimatedExpense(Lectura last) {
        float discount_kwh = Float.parseFloat(Prefs.getString("discount_kwh", "0"));
        float price_kwh = Float.parseFloat(Prefs.getString("price_kwh", "1"));
        float fixed_charges = Float.parseFloat(Prefs.getString("fixed_charges", "0"));
        return (getEstimatedConsumption(last) * (price_kwh - discount_kwh)) + fixed_charges;
    }

    @Override
    public float getEstimatedExpenseWithNoDiscount(Lectura last) {
        float price_kwh = Float.parseFloat(Prefs.getString("price_kwh", "1"));
        float fixed_charges = Float.parseFloat(Prefs.getString("fixed_charges", "0"));

        return (getEstimatedConsumption(last) * (price_kwh )) + fixed_charges;
    }

    private RealmResults<Lectura> getReadings(Periodo periodo){
        String p_id = "";
        if(periodo!=null)
            p_id = periodo.id;
        return realm.where(Lectura.class)
                .equalTo("periodo.id", p_id)
                .findAllSorted("fecha_lectura", Sort.ASCENDING);
    }

    @Override
    public LineChart setReadingHistory(LineChart chart, Periodo periodo) {
        RealmResults<Lectura> lecturas = getReadings(periodo);
        try {
            LineData period_limit_lineData;
            period_limit_lineData = new LineData(ChartStyler.drawPeriodLimitLine(this.context));

            if (lecturas.size() > 0) {
                RealmLineDataSet<Lectura> acumuladoDataSet = new RealmLineDataSet<>(lecturas, "dias_periodo", "consumo_acumulado");

                period_limit_lineData.addDataSet(ChartStyler.setAcumuladoPeriodLine(acumuladoDataSet, this.context));
                period_limit_lineData.addDataSet(ChartStyler.setProyectionPeriodLine(lecturas, this.context));
                chart.setData(period_limit_lineData);
            } else {
                chart.setData(period_limit_lineData);
            }
        } catch (Exception e) {
            //Log.e("EDER_Exception", e.getMessage());
        }
        return chart;
    }

    @Override
    public LineChart setAvgHistory(LineChart chart, Periodo periodo) {
        RealmResults<Lectura> lecturas = getReadings(periodo);
        try {
            LineData period_limit_lineData;
            period_limit_lineData = new LineData(ChartStyler.drawAvgLimitLine(this.context));

            if (lecturas.size() > 0) {
                RealmLineDataSet<Lectura> acumuladoDataSet = new RealmLineDataSet<>(lecturas, "dias_periodo", "consumo_promedio");

                period_limit_lineData.addDataSet(ChartStyler.setAvgPeriodLine(acumuladoDataSet, this.context));
                chart.setData(period_limit_lineData);
            } else {
                chart.setData(period_limit_lineData);
            }
        } catch (Exception e) {
            //Log.e("EDER_Exception", e.getMessage());
        }
        return chart;
    }

    @Override
    public void onCreate() {
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onDestroy() {
        if(!realm.isClosed())
            realm.close();
    }
}
