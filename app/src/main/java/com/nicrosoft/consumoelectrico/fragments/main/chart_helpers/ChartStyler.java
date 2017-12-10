package com.nicrosoft.consumoelectrico.fragments.main.chart_helpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.realm.implementation.RealmBarDataSet;
import com.github.mikephil.charting.data.realm.implementation.RealmLineDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
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
    public static Chart<?> setup(@NonNull Chart<?> chart, @NonNull Context context) {
        float period_days = Float.parseFloat(Prefs.getString("period_lenght", "30"));
        float kw_limit = Float.parseFloat(Prefs.getString("kw_limit", "150"));

        float avg_limit = kw_limit / period_days;

        // no description text
        chart.getDescription().setEnabled(false);
        chart.setNoDataText("Sin datos para mostrar");

        // enable touch gestures
        chart.setTouchEnabled(true);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        chart.getXAxis().setTextColor(context.getResources().getColor(R.color.md_cyan_200));

        chart.getXAxis().setDrawLabels(true);
        chart.getXAxis().setAxisLineColor(context.getResources().getColor(R.color.md_cyan_800));
        chart.getLegend().setTextColor(context.getResources().getColor(R.color.md_cyan_200));
        chart.getLegend().setWordWrapEnabled(true);
        chart.getXAxis().setDrawLabels(true);
        chart.setExtraOffsets(5f, 15f, 0f, 0f);
        chart.getXAxis().setAxisLineColor(context.getResources().getColor(R.color.md_cyan_600));
        chart.animateXY(500, 0);

        if (chart instanceof LineChart) {
            LineChart mChart = (LineChart) chart;
            mChart.setGridBackgroundColor(context.getResources().getColor(R.color.md_cyan_800));
            mChart.getAxisLeft().setTextColor(context.getResources().getColor(R.color.md_cyan_200));
            mChart.setDrawBorders(false);
            mChart.setBorderColor(context.getResources().getColor(R.color.md_cyan_500_25));
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
            LimitLine upper_limit2 = new LimitLine(kw_limit, "");
            upper_limit2.setLineWidth(1.3f);
            upper_limit2.setTextColor(context.getResources().getColor(R.color.label_text_light));
            upper_limit2.enableDashedLine(15f, 15f, 0f);
            upper_limit2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            upper_limit2.setTextSize(9f);
            upper_limit2.setLineColor(context.getResources().getColor(R.color.md_amber_500));

            YAxis leftAxis = mChart.getAxisLeft();

            //reset all limit lines to avoid overlapping lines
            leftAxis.removeAllLimitLines();
            leftAxis.addLimitLine(upper_limit2);
            leftAxis.setGridColor(context.getResources().getColor(R.color.md_cyan_A200));
            leftAxis.setDrawZeroLine(false);
            leftAxis.setDrawLimitLinesBehindData(true);
            leftAxis.setValueFormatter(new MyYAxisValueFormatter());
            leftAxis.setAxisMinimum(0f);
            leftAxis.setDrawGridLines(false);

            return mChart;
        }else if (chart instanceof CombinedChart) {
            CombinedChart combinedChart = (CombinedChart) chart;

            combinedChart.getAxisRight().setAxisMinimum(0f);
            combinedChart.getAxisRight().setDrawGridLines(false);

            combinedChart.setDrawBorders(false);
            combinedChart.setBorderColor(context.getResources().getColor(R.color.md_green_500_25));
            combinedChart.getAxisLeft().setAxisLineColor(context.getResources().getColor(R.color.md_green_800));
            combinedChart.getAxisRight().setAxisLineColor(context.getResources().getColor(R.color.md_green_800));
            combinedChart.getAxisRight().setDrawLabels(false);
            combinedChart.getLegend().setTextColor(context.getResources().getColor(R.color.primary_light));
            combinedChart.getAxisLeft().setTextColor(context.getResources().getColor(R.color.primary_light));
            //combinedChart.setData(lineData);
            combinedChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                    CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE,
            });
            LimitLine avg_upper_limit = new LimitLine(avg_limit, "");
            avg_upper_limit.setLineWidth(0.9f);
            avg_upper_limit.setTextColor(context.getResources().getColor(R.color.label_text_light));
            avg_upper_limit.enableDashedLine(15f, 15f, 0f);
            avg_upper_limit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            avg_upper_limit.setTextSize(9f);
            avg_upper_limit.setLineColor(context.getResources().getColor(R.color.md_amber_500));

            YAxis leftAxis2 = combinedChart.getAxisLeft();
            leftAxis2.removeAllLimitLines();
            leftAxis2.addLimitLine(avg_upper_limit);
            leftAxis2.setGridColor(context.getResources().getColor(R.color.md_light_blue_100));
            leftAxis2.setDrawZeroLine(false);
            leftAxis2.setDrawLimitLinesBehindData(true);
            leftAxis2.setValueFormatter(new MyYAxisValueFormatter());
            leftAxis2.setAxisMinimum(0f);
            leftAxis2.setDrawGridLines(false);
            YAxis rightAxis = combinedChart.getAxisRight();
            rightAxis.setDrawGridLines(false);
            rightAxis.setAxisMinimum(0f);
            return combinedChart;
        }
        else
            return chart;

    }

    @NonNull
    public static LineDataSet drawAvgLimitLine(@NonNull Context context){
        List<Entry> avg_limit_dataset = new ArrayList<Entry>();
        float period_lenght = Float.parseFloat(Prefs.getString("period_lenght", "30"));
        float kw_limit = Float.parseFloat(Prefs.getString("kw_limit", "150"));

        for (int i = 0; i<= period_lenght; i++ ) {
            float limit = kw_limit/period_lenght;
            avg_limit_dataset.add(new Entry(i, limit));
        }
        String title = context.getResources().getString(R.string.chart_legend_avg_limit);
        LineDataSet avgLimitDataSet = new LineDataSet(avg_limit_dataset, title);
        avgLimitDataSet.setDrawCircles(false);
        avgLimitDataSet.setLineWidth(1.2f);
        avgLimitDataSet.setDrawValues(false);
        avgLimitDataSet.setHighlightEnabled(false);
        avgLimitDataSet.setColor(context.getResources().getColor(R.color.md_yellow_500));
        return avgLimitDataSet;
    }
    @NonNull
    public static LineDataSet drawPeriodLimitLine(@NonNull Context context){
        List<Entry> limit_dataset = new ArrayList<Entry>();
        int period_lenght = Integer.parseInt(Prefs.getString("period_lenght", "30"));
        int kw_limit = Integer.parseInt(Prefs.getString("kw_limit", "150"));

        for (int i = 0; i<= period_lenght; i++ ) {
            limit_dataset.add(new Entry(i, kw_limit));
        }
        String title = context.getResources().getString(R.string.chart_legend_period_limit);

        LineDataSet periodDataSet = new LineDataSet(limit_dataset,title);
        periodDataSet.setDrawCircles(false);
        periodDataSet.setLineWidth(1.2f);
        periodDataSet.setDrawValues(false);
        periodDataSet.setHighlightEnabled(false);
        periodDataSet.setColor(context.getResources().getColor(R.color.md_amber_500));
        return periodDataSet;
    }

    @NonNull
    public static BarData setBardata(@NonNull RealmBarDataSet barDataset, @NonNull Context context){
        String title = context.getResources().getString(R.string.chart_legend_bar);
        barDataset.setLabel(title);
        barDataset.setValueTextColor(context.getResources().getColor(R.color.label_text_light));
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
        combinedLineDataSet.setCircleColor(context.getResources().getColor(R.color.label_text_light));
        //combinedLineDataSet.enableDashedLine(10f, 10f, 0f);
        //combinedLineDataSet.setMode(LineDataSet.Mode.STEPPED);
        //combinedLineDataSet.setCubicIntensity(0.1f);
        combinedLineDataSet.setValueTextSize(8f);
        combinedLineDataSet.setValueTextColor(context.getResources().getColor(R.color.label_text_light));
        combinedLineDataSet.setColor(context.getResources().getColor(R.color.md_grey_100));
        combinedLineDataSet.setFillColor(context.getResources().getColor(R.color.chart_consumo_line_fill));
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
        acumuladoDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //acumuladoDataSet.setCubicIntensity(0.1f);
        acumuladoDataSet.setValueTextSize(8f);
        acumuladoDataSet.setCircleColor(context.getResources().getColor(R.color.label_text_light));
        acumuladoDataSet.setValueTextColor(context.getResources().getColor(R.color.label_text_light));
        acumuladoDataSet.setColor(context.getResources().getColor(R.color.md_grey_100));
        acumuladoDataSet.setFillColor(context.getResources().getColor(R.color.chart_consumo_line_fill));

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
        acumuladoDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //acumuladoDataSet.setCubicIntensity(0.1f);
        acumuladoDataSet.setValueTextSize(9f);
        acumuladoDataSet.setCircleColor(context.getResources().getColor(R.color.label_text_light));
        acumuladoDataSet.setValueTextColor(context.getResources().getColor(R.color.label_text_light));
        acumuladoDataSet.setColor(context.getResources().getColor(R.color.md_grey_100));
        acumuladoDataSet.setFillColor(context.getResources().getColor(R.color.chart_consumo_line_fill));

        return acumuladoDataSet;
    }

    @NonNull
    public static LineDataSet setProyectionPeriodLine(@NonNull RealmResults<Lectura> lecturas, @NonNull Context context){

        Lectura lectura = lecturas.last();
        /*acumuladoDataSet.setLabel("Proyeccion Consumo kWh");
        acumuladoDataSet.setValueFormatter(new MyValueFormatter());
        acumuladoDataSet.setDrawFilled(false);
        acumuladoDataSet.setDrawCircleHole(false);
        acumuladoDataSet.setDrawCircles(false);
        acumuladoDataSet.setLineWidth(1.2f);
        acumuladoDataSet.enableDashedLine(10f, 10f, 0f);
        //acumuladoDataSet.setMode(LineDataSet.Mode.LINEAR);
        //acumuladoDataSet.setCubicIntensity(0.1f);
        acumuladoDataSet.setValueTextSize(8f);

*/
        List<Entry> proyection_dataset = new ArrayList<Entry>();
        int period_lenght = Integer.parseInt(Prefs.getString("period_lenght", "30"));
        int kw_limit = Integer.parseInt(Prefs.getString("kw_limit", "150"));
        float cp = lectura.consumo_acumulado;

        for (int i = (int) lectura.dias_periodo; i<= period_lenght; i++ ) {
            proyection_dataset.add(new Entry(i, cp));
            cp = (cp + lectura.consumo_promedio);
        }
        String title = context.getResources().getString(R.string.chart_legend_estimate);

        LineDataSet periodDataSet = new LineDataSet(proyection_dataset, title);
        periodDataSet.setDrawCircles(false);
        //periodDataSet.setLineWidth(0.8f);
        periodDataSet.setDrawValues(false);
        periodDataSet.setHighlightEnabled(false);
        periodDataSet.setColor(context.getResources().getColor(R.color.md_lime_600));
        periodDataSet.enableDashedLine(10f, 10f, 0f);
        return periodDataSet;

    }
}
