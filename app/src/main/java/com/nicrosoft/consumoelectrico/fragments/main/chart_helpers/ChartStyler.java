package com.nicrosoft.consumoelectrico.fragments.main.chart_helpers;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.realm.implementation.RealmBarDataSet;
import com.github.mikephil.charting.data.realm.implementation.RealmLineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.nicrosoft.consumoelectrico.R;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 23/01/2017.
 */

public class ChartStyler {

    @NonNull
    public static Chart<?> setup(@NonNull Chart<?> chart, @NonNull Context context, Boolean drawAvgLimit) {

        float kw_limit = 150;
        float period_days = 30;
        try{
            kw_limit =  Float.parseFloat(Prefs.getString("kw_limit", "150"));
            period_days = Float.parseFloat(Prefs.getString("period_lenght", "30"));
        }catch (Exception ignored){}

        float avg_limit = kw_limit / period_days;

        // no description text
        chart.getDescription().setEnabled(false);
        chart.setNoDataText("Sin datos para mostrar");

        // enable touch gestures
        chart.setTouchEnabled(true);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        chart.getXAxis().setTextColor(context.getResources().getColor(R.color.md_black_1000_75));

        chart.getXAxis().setDrawLabels(true);
        chart.getXAxis().setAxisLineColor(context.getResources().getColor(R.color.md_black_1000_75));
        chart.getLegend().setTextColor(context.getResources().getColor(R.color.md_black_1000_75));
        chart.getLegend().setWordWrapEnabled(true);
        chart.getXAxis().setDrawLabels(true);
        chart.setExtraOffsets(5f, 15f, 0f, 0f);
        chart.getXAxis().setAxisLineColor(context.getResources().getColor(R.color.md_black_1000_75));
        chart.animateXY(500, 0);

        if (chart instanceof LineChart) {
            LineChart mChart = (LineChart) chart;
            mChart.setGridBackgroundColor(context.getResources().getColor(R.color.md_cyan_800));
            mChart.getAxisLeft().setTextColor(context.getResources().getColor(R.color.md_black_1000_75));
            mChart.setDrawBorders(false);
            mChart.setBorderColor(context.getResources().getColor(R.color.md_black_1000_50));
            mChart.getAxisLeft().setAxisLineColor(context.getResources().getColor(R.color.md_cyan_800));
            mChart.getAxisRight().setAxisLineColor(context.getResources().getColor(R.color.md_cyan_800));
            mChart.getAxisRight().setDrawLabels(false);
            mChart.setDrawGridBackground(false);
            // enable scaling and dragging
            mChart.setDragEnabled(true);
            mChart.setScaleEnabled(true);
            // if disabled, scaling can be done on x- and y-axis separately
            mChart.setPinchZoom(false);
            mChart.getAxisRight().setEnabled(false);
            YAxis leftAxis = mChart.getAxisLeft();
            leftAxis.setGridColor(context.getResources().getColor(R.color.md_black_1000));
            leftAxis.setDrawZeroLine(false);
            leftAxis.setValueFormatter(new MyYAxisValueFormatter());
            leftAxis.setAxisMinimum(0f);
            leftAxis.setDrawGridLines(false);

            //Esta linea se vera luego de pasar el limite perido
            if(!drawAvgLimit) {
                LimitLine upper_limit2 = new LimitLine(kw_limit, "");
                upper_limit2.setLineWidth(1.3f);
                upper_limit2.setTextColor(context.getResources().getColor(R.color.md_black_1000_75));
                upper_limit2.enableDashedLine(15f, 15f, 0f);
                upper_limit2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
                upper_limit2.setTextSize(9f);
                upper_limit2.setLineColor(context.getResources().getColor(R.color.md_amber_700));
                //reset all limit lines to avoid overlapping lines
                leftAxis.removeAllLimitLines();
                leftAxis.addLimitLine(upper_limit2);
            }else {
                LimitLine avg_upper_limit = new LimitLine(avg_limit, "");
                avg_upper_limit.setLineWidth(0.9f);
                avg_upper_limit.setTextColor(context.getResources().getColor(R.color.md_black_1000_75));
                avg_upper_limit.enableDashedLine(15f, 15f, 0f);
                avg_upper_limit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
                avg_upper_limit.setTextSize(9f);
                avg_upper_limit.setLineColor(context.getResources().getColor(R.color.md_amber_700));
                leftAxis.removeAllLimitLines();
                leftAxis.addLimitLine(avg_upper_limit);
            }

            return mChart;
        }
        else {
            return chart;
        }

    }

    @NonNull
    public static LineDataSet drawAvgLimitLine(@NonNull Context context){
        List<Entry> avg_limit_dataset = new ArrayList<Entry>();
        float kw_limit = 150;
        float period_days = 30;
        try{
            kw_limit =  Float.parseFloat(Prefs.getString("kw_limit", "150"));
            period_days = Float.parseFloat(Prefs.getString("period_lenght", "30"));
        }catch (Exception ignored){}

        for (int i = 0; i<= period_days; i++ ) {
            float limit = kw_limit/period_days;
            avg_limit_dataset.add(new Entry(i, limit));
        }
        String title = context.getResources().getString(R.string.chart_legend_avg_limit);
        LineDataSet avgLimitDataSet = new LineDataSet(avg_limit_dataset, title);
        avgLimitDataSet.setDrawCircles(false);
        avgLimitDataSet.setLineWidth(1.2f);
        avgLimitDataSet.setDrawValues(false);
        avgLimitDataSet.setHighlightEnabled(false);
        avgLimitDataSet.setColor(context.getResources().getColor(R.color.md_yellow_700));
        return avgLimitDataSet;
    }
    @NonNull
    public static LineDataSet drawPeriodLimitLine(@NonNull Context context){
        List<Entry> limit_dataset = new ArrayList<Entry>();
        float kw_limit = 150;
        float period_days = 30;
        try{
            kw_limit =  Float.parseFloat(Prefs.getString("kw_limit", "150"));
            period_days = Float.parseFloat(Prefs.getString("period_lenght", "30"));
        }catch (Exception ignored){}

        for (int i = 0; i<= period_days; i++ ) {
            limit_dataset.add(new Entry(i, kw_limit));
        }
        String title = context.getResources().getString(R.string.chart_legend_period_limit);

        LineDataSet periodDataSet = new LineDataSet(limit_dataset,title);
        periodDataSet.setDrawCircles(false);
        periodDataSet.setLineWidth(1.2f);
        periodDataSet.setDrawValues(false);
        periodDataSet.setHighlightEnabled(false);
        periodDataSet.setColor(context.getResources().getColor(R.color.md_amber_700));
        return periodDataSet;
    }

    @NonNull
    public static BarData setBardata(@NonNull RealmBarDataSet barDataset, @NonNull Context context){
        String title = context.getResources().getString(R.string.chart_legend_bar);
        barDataset.setLabel(title);
        barDataset.setValueTextColor(context.getResources().getColor(R.color.md_black_1000_75));
        barDataset.setColor(context.getResources().getColor(R.color.md_blue_grey_400));
        barDataset.setValueFormatter(new MyValueFormatter());
        barDataset.setDrawValues(false);
        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(barDataset); // add the dataset
        // create a data object with the dataset list
        return new BarData(dataSets);
    }

    @NonNull
    public static RealmLineDataSet setCombinedLine(@NonNull RealmLineDataSet combinedLineDataSet, @NonNull Context context){
        String title = context.getResources().getString(R.string.chart_legend_avg);
        combinedLineDataSet.setLabel(title);
        combinedLineDataSet.setValueFormatter(new MyAvgFormatter());
        combinedLineDataSet.setDrawFilled(false);
        combinedLineDataSet.setHighlightEnabled(false);
        combinedLineDataSet.setLineWidth(1.2f);
        combinedLineDataSet.setDrawCircleHole(false);
        combinedLineDataSet.setCircleColor(context.getResources().getColor(R.color.md_black_1000_75));
        //combinedLineDataSet.enableDashedLine(10f, 10f, 0f);
        //combinedLineDataSet.setMode(LineDataSet.Mode.STEPPED);
        //combinedLineDataSet.setCubicIntensity(0.1f);
        combinedLineDataSet.setValueTextSize(8f);
        combinedLineDataSet.setValueTextColor(context.getResources().getColor(R.color.md_black_1000_75));
        combinedLineDataSet.setColor(context.getResources().getColor(R.color.md_pink_500));
        combinedLineDataSet.setFillColor(context.getResources().getColor(R.color.md_pink_500));
        return combinedLineDataSet;
    }

    @NonNull
    public static RealmLineDataSet setAcumuladoPeriodLine(@NonNull RealmLineDataSet acumuladoDataSet, @NonNull Context context){
        String title = context.getResources().getString(R.string.chart_legend_accumulated);
        acumuladoDataSet.setLabel(title);
        acumuladoDataSet.setValueFormatter(new MyValueFormatter());
        acumuladoDataSet.setDrawFilled(false);
        acumuladoDataSet.setDrawCircleHole(false);
        acumuladoDataSet.setLineWidth(1.2f);
        //acumuladoDataSet.enableDashedLine(10f, 10f, 0f);
        acumuladoDataSet.setMode(LineDataSet.Mode.LINEAR);
        //acumuladoDataSet.setCubicIntensity(0.1f);
        acumuladoDataSet.setValueTextSize(8f);
        acumuladoDataSet.setCircleColor(context.getResources().getColor(R.color.md_pink_500));
        acumuladoDataSet.setValueTextColor(context.getResources().getColor(R.color.md_black_1000_75));
        acumuladoDataSet.setColor(context.getResources().getColor(R.color.md_pink_500));
        acumuladoDataSet.setFillColor(context.getResources().getColor(R.color.md_pink_500));

        return acumuladoDataSet;
    }

    @NonNull
    public static RealmLineDataSet setAvgPeriodLine(@NonNull RealmLineDataSet acumuladoDataSet, @NonNull Context context){
        String title = context.getResources().getString(R.string.chart_legend_avg);
        acumuladoDataSet.setLabel(title);
        acumuladoDataSet.setValueFormatter(new MyAvgFormatter());
        acumuladoDataSet.setDrawFilled(false);
        acumuladoDataSet.setDrawCircleHole(false);
        acumuladoDataSet.setLineWidth(1.2f);
        //acumuladoDataSet.enableDashedLine(10f, 10f, 0f);
        acumuladoDataSet.setMode(LineDataSet.Mode.LINEAR);
        //acumuladoDataSet.setCubicIntensity(0.1f);
        acumuladoDataSet.setValueTextSize(9f);
        acumuladoDataSet.setCircleColor(context.getResources().getColor(R.color.md_pink_500));
        acumuladoDataSet.setValueTextColor(context.getResources().getColor(R.color.md_black_1000_75));
        acumuladoDataSet.setColor(context.getResources().getColor(R.color.md_pink_500));
        acumuladoDataSet.setFillColor(context.getResources().getColor(R.color.md_pink_500));
        //acumuladoDataSet.setDrawValues(false);

        return acumuladoDataSet;
    }

    @NonNull
    public static LineDataSet setProyectionPeriodLine(@NonNull RealmResults<Lectura> lecturas, @NonNull Context context){

        Lectura lectura = lecturas.last();
        List<Entry> proyection_dataset = new ArrayList<Entry>();
        float kw_limit = 150;
        float period_days = 30;
        try{
            kw_limit =  Float.parseFloat(Prefs.getString("kw_limit", "150"));
            period_days = Float.parseFloat(Prefs.getString("period_lenght", "30"));
        }catch (Exception ignored){}
        float cp = lectura.consumo_acumulado;

        for (int i = (int) lectura.dias_periodo; i<= period_days; i++ ) {
            proyection_dataset.add(new Entry(i, cp));
            cp = (cp + lectura.consumo_promedio);
        }
        String title = context.getResources().getString(R.string.chart_legend_estimate);

        LineDataSet periodDataSet = new LineDataSet(proyection_dataset, title);
        periodDataSet.setDrawCircles(false);
        //periodDataSet.setLineWidth(0.8f);
        periodDataSet.setDrawValues(false);
        periodDataSet.setHighlightEnabled(false);
        periodDataSet.setColor(context.getResources().getColor(R.color.md_lime_800));
        periodDataSet.enableDashedLine(10f, 10f, 0f);
        return periodDataSet;
    }
}
