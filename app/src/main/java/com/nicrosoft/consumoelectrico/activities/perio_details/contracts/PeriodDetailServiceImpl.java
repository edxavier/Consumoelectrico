package com.nicrosoft.consumoelectrico.activities.perio_details.contracts;

import android.content.Context;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.realm.implementation.RealmLineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.nicrosoft.consumoelectrico.R;
import com.nicrosoft.consumoelectrico.fragments.main.chart_helpers.ChartStyler;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Medidor;
import com.nicrosoft.consumoelectrico.realm.Periodo;
import com.pixplicity.easyprefs.library.Prefs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

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
                    .findAll().sort("fecha_lectura", Sort.DESCENDING);
            return (res.size()>0) ? res.last(): null;
        }else
            return null;
    }

    @Override
    public Lectura getLastReading(Periodo periodo) {
        if(periodo!=null) {
            RealmResults<Lectura> res = realm.where(Lectura.class)
                    .equalTo("periodo.id", periodo.id)
                    .findAll().sort("fecha_lectura", Sort.DESCENDING);
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
                .findAll().sort("fecha_lectura", Sort.ASCENDING);
    }

    private RealmResults<Lectura> getReadingsAVG(Periodo periodo){
        String p_id = "";
        if(periodo!=null)
            p_id = periodo.id;
        return realm.where(Lectura.class)
                .equalTo("periodo.id", p_id)
                .greaterThanOrEqualTo("consumo_promedio", 1f)
                .findAll().sort("fecha_lectura", Sort.ASCENDING);
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
                LineDataSet pds = ChartStyler.setProyectionPeriodLine(lecturas, this.context);
                if(!pds.getValues().isEmpty()) {
                    period_limit_lineData.addDataSet(ChartStyler.setProyectionPeriodLine(lecturas, this.context));
                }
                chart.setData(period_limit_lineData);
            } else {
                chart.setData(period_limit_lineData);
            }
        } catch (Exception ignored) {}
        return chart;
    }

    @Override
    public LineChart setAvgHistory(LineChart chart, Periodo periodo) {
        RealmResults<Lectura> lecturas = getReadingsAVG(periodo);
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
    public BarChart setPeriodHistory(BarChart chart, String medidor_id) {
        RealmResults<Periodo> results = realm.where(Periodo.class)
                .equalTo("medidor.id", medidor_id)
                .findAll().sort("inicio", Sort.ASCENDING);
        try {
            SimpleDateFormat time_format = new SimpleDateFormat("MMMyy", Locale.getDefault());
            ArrayList<BarEntry> BarEntry = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();
            int x = 0;
            Calendar c = Calendar.getInstance();
            for (Periodo result : results) {
                Lectura last = realm.where(Lectura.class)
                        .equalTo("periodo.id", result.id).findAll().sort("fecha_lectura", Sort.ASCENDING).last();
                if(last!=null) {
                    c.setTime(result.inicio);
                    c.add(Calendar.DATE, 3);
                    labels.add(time_format.format(c.getTime()));
                    BarEntry.add(new BarEntry(x, last.consumo_acumulado));
                    x = x + 1;
                }
            }
            if(BarEntry.size()>0) {
                BarDataSet dataSet = new BarDataSet(BarEntry, context.getString(R.string.label_consumption));
                dataSet.setColor(this.context.getResources().getColor(R.color.md_pink_700));
                dataSet.setValueTextColor(this.context.getResources().getColor(R.color.md_black_1000_75));
                BarData data = new BarData(dataSet);
                data.setBarWidth(0.8f);
                /*data.setValueFormatter(new IValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                        return String.format(Locale.getDefault(), "%.0f", value);
                    }
                });*/
                chart.setData(data);
                chart.setVisibleXRangeMaximum(12);
                chart.getXAxis().setAxisMinimum(-0.8f);
                chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                chart.getXAxis().setGranularity(1.0f);
                chart.getAxisLeft().setTextColor(this.context.getResources().getColor(R.color.md_black_1000_50));
                chart.getAxisRight().setTextColor(this.context.getResources().getColor(R.color.md_black_1000_50));
                chart.getAxisRight().setAxisMinimum(0);
                chart.getAxisLeft().setAxisMinimum(0);
                if(labels.size()>3)
                    chart.moveViewToX(labels.size()-1);
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
