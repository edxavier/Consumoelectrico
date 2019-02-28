package com.nicrosoft.consumoelectrico;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.widget.TextView;


import com.anjlab.android.iab.v3.BillingProcessor;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    BillingProcessor bp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TextView textPercentage = (TextView) findViewById(R.id.textPercentage);
        final TextView rem = (TextView) findViewById(R.id.txtUltimaLectura);
        rem.setText("33 kw");

        LineChart chart = (LineChart) findViewById(R.id.chart2);
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry(1, 2));
        entries.add(new Entry(2, 3));
        entries.add(new Entry(3, 3));
        entries.add(new Entry(4, 5));
        entries.add(new Entry(5, 3));
        entries.add(new Entry(6, 7));
        entries.add(new Entry(7, 4));
        entries.add(new Entry(10, 12));
        entries.add(new Entry(11, 4));
        entries.add(new Entry(12, 5));

        List<Entry> entries2 = new ArrayList<Entry>();
        entries2.add(new Entry(1, 2));
        entries2.add(new Entry(2, 2.5f));
        entries2.add(new Entry(3, 4));
        entries2.add(new Entry(4, 4));
        entries2.add(new Entry(5, 4.5f));
        entries2.add(new Entry(6, 6));
        entries2.add(new Entry(7, 6));
        entries2.add(new Entry(10, 7));
        entries2.add(new Entry(11, 6));
        entries2.add(new Entry(12, 6));


        //BuildConfig.APP_BILLING_PUB_KEY;

        LineDataSet dataSet = new LineDataSet(entries, "Consumo kw");
        dataSet.setDrawFilled(true);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.1f);
        dataSet.setValueTextColor(getResources().getColor(R.color.label_text_light));
        dataSet.setColor(getResources().getColor(R.color.chart_consumo_line_color));
        dataSet.setFillColor(getResources().getColor(R.color.chart_consumo_line_fill));

        LineDataSet dataSet2 = new LineDataSet(entries2, "Consumo promedio");
        dataSet2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet2.setCubicIntensity(0.1f);
        dataSet2.setColor(getResources().getColor(R.color.md_grey_100));
        dataSet2.enableDashedLine(20f, 20f, 0);
        dataSet2.setDrawValues(false);
        dataSet2.setDrawCircles(false);
        dataSet2.setHighlightEnabled(false);
        dataSet2.setCircleColor(getResources().getColor(R.color.md_grey_100));


        LineData lineData = new LineData(dataSet2);
        lineData.addDataSet(dataSet);
        chart.setData(lineData);

        LimitLine upper_limit = new LimitLine(10f, "");
        upper_limit.setLineWidth(1.5f);
        upper_limit.enableDashedLine(10f, 10f, 0f);
        upper_limit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        upper_limit.setTextSize(10f);
        upper_limit.setLineColor(getResources().getColor(R.color.md_yellow_500));
        YAxis leftAxis = chart.getAxisLeft();
        //reset all limit lines to avoid overlapping lines
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(upper_limit);
        leftAxis.setGridColor(getResources().getColor(R.color.md_light_blue_100));
        leftAxis.setDrawZeroLine(false);
        leftAxis.setDrawLimitLinesBehindData(true);


        chart.animateXY(500, 0);
        chart.setNoDataText("Sin datos para mostrar");
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        chart.getXAxis().setTextColor(getResources().getColor(R.color.primary_light));
        chart.getAxisLeft().setTextColor(getResources().getColor(R.color.primary_light));
        Description description = new Description();
        description.setText("");
        description.setEnabled(false);
        chart.setDescription(description);
        chart.getXAxis().setDrawLabels(true);
        chart.setDrawBorders(false);
        chart.setBorderColor(getResources().getColor(R.color.md_green_500_25));
        chart.getXAxis().setDrawGridLines(false);


        chart.getXAxis().setAxisLineColor(getResources().getColor(R.color.md_green_800));
        chart.getAxisLeft().setAxisLineColor(getResources().getColor(R.color.md_green_800));
        chart.getAxisRight().setAxisLineColor(getResources().getColor(R.color.md_green_800));
        chart.getAxisRight().setDrawLabels(false);
        chart.setExtraOffsets(5f, 15f, 0f, 0f);
        chart.getLegend().setTextColor(getResources().getColor(R.color.primary_light));
        chart.invalidate();

    }


}
