package com.nicrosoft.consumoelectrico.activities.perio_details;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nicrosoft.consumoelectrico.R;
import com.nicrosoft.consumoelectrico.activities.PeriodReadingsActivity;
import com.nicrosoft.consumoelectrico.activities.perio_details.contracts.PeriodDetailPresenterImpl;
import com.nicrosoft.consumoelectrico.activities.perio_details.contracts.PeriodDetailsPresenter;
import com.nicrosoft.consumoelectrico.activities.reading.NewReadingActivity;
import com.nicrosoft.consumoelectrico.fragments.main.chart_helpers.ChartStyler;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Periodo;
import com.pixplicity.easyprefs.library.Prefs;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.coordinator)
    CoordinatorLayout coordinator;
    @BindView(R.id.txt_beginning_period)
    TextView txtBeginningPeriod;
    @BindView(R.id.txt_initial_reading)
    TextView txtInitialReading;
    @BindView(R.id.txt_last_reading_date)
    TextView txtLastReadingDate;
    @BindView(R.id.txt_period_len)
    TextView txtPeriodLen;
    @BindView(R.id.txt_days_consumed)
    TextView txtDaysConsumed;
    @BindView(R.id.card_resume)
    CardView cardResume;
    @BindView(R.id.txt_last_reading)
    TextView txtLastReading;
    @BindView(R.id.txt_current_consumption)
    TextView txtCurrentConsumption;
    @BindView(R.id.arc_progress_consumo)
    ArcProgress arcProgressConsumo;
    @BindView(R.id.arc_progress_period)
    ArcProgress arcProgressPeriod;
    @BindView(R.id.txt_avg_consumption)
    TextView txtAvgConsumption;
    @BindView(R.id.txt_estimate_consumptio_kwh)
    TextView txtEstimateConsumptioKwh;
    @BindView(R.id.txt_estimate_expense)
    TextView txtEstimateExpense;
    @BindView(R.id.txt_estimate_expense_no_discount)
    TextView txtEstimateExpenseNoDiscount;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.chart)
    LineChart chart;
    @BindView(R.id.chart2)
    LineChart chart2;
    private String medidor_id;
    private String medidor_name;
    private PeriodDetailsPresenter presenter;
    private SimpleDateFormat time_format;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        time_format = new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault());
        extras = getIntent().getExtras();
        if (extras != null) {
            medidor_id = extras.getString("id");
            medidor_name = (extras.getString("name"));
        }
        getSupportActionBar().setTitle(medidor_name);
        presenter = new PeriodDetailPresenterImpl(this);
        presenter.onCreate();
        setupBillingPeriodDetails(medidor_id);
        if(!Prefs.getBoolean("isPurchased", false))
            showAds();
    }

    private void setupBillingPeriodDetails(String medidor_id) {
        Periodo period = presenter.getActivePeriod(medidor_id);
        Lectura ultima_lectura = presenter.getLastReading(period);
        Lectura primer_lectura = presenter.getFirstReading(period);
        chart = (LineChart) ChartStyler.setup(chart, this);
        chart2 = (LineChart) ChartStyler.setup(chart2, this);

        chart = presenter.setReadingHistory(chart, period);
        chart.invalidate();
        chart2 = presenter.setAvgHistory(chart2, period);
        chart2.invalidate();

        try {

            txtBeginningPeriod.setText(time_format.format(period.inicio));
            txtInitialReading.setText(getString(R.string.initial_reading_val, String.format(Locale.getDefault(), "%02.0f", primer_lectura.lectura)));
            txtLastReading.setText(getString(R.string.initial_reading_val, String.format(Locale.getDefault(), "%02.0f", ultima_lectura.lectura)));
            txtCurrentConsumption.setText(getString(R.string.initial_reading_val, String.format(Locale.getDefault(), "%02.0f", ultima_lectura.consumo_acumulado)));
            txtDaysConsumed.setText(getString(R.string.days_consumed_val, String.format(Locale.getDefault(), "%02.0f", ultima_lectura.dias_periodo)));
            txtPeriodLen.setText(getString(R.string.days_consumed_val, String.valueOf(presenter.getPeriodLength())));
            txtLastReadingDate.setText(time_format.format(ultima_lectura.fecha_lectura));
            txtAvgConsumption.setText(getString(R.string.avg_consumption_val, String.format(Locale.getDefault(), "%02.1f", ultima_lectura.consumo_promedio)));
            txtEstimateConsumptioKwh.setText(getString(R.string.initial_reading_val, String.format(Locale.getDefault(), "%02.1f", presenter.getEstimatedConsumption(ultima_lectura))));
            txtEstimateExpense.setText(
                    getString(R.string.estimated_expense_val,
                            Prefs.getString("price_simbol", "$"),
                            String.format(Locale.getDefault(), "%02.1f", presenter.getEstimatedExpense(ultima_lectura))
                    ));
            txtEstimateExpenseNoDiscount.setText(
                    getString(R.string.estimated_unsubsidized_expense_val,
                            Prefs.getString("price_simbol", "$"),
                            String.format(Locale.getDefault(), "%02.1f", presenter.getEstimatedExpenseWithNoDiscount(ultima_lectura))
                    ));

            arcProgressConsumo.setProgress((int) ultima_lectura.consumo_acumulado);
            arcProgressConsumo.setMax(presenter.getConsumptionLimit());
            arcProgressPeriod.setMax(presenter.getPeriodLength());
            arcProgressPeriod.setProgress((int) ultima_lectura.dias_periodo);
        } catch (Exception e) {
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    private void showAds() {

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("0B307F34E3DDAF6C6CAB28FAD4084125")
                //.addTestDevice("B0FF48A19BF36BD2D5DCD62163C64F45")
                .build();

        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            case R.id.action_readings:
                Intent intent = new Intent(this, PeriodReadingsActivity.class);
                intent.putExtras(extras);
                startActivityForResult(intent, 0);
                return true;
            case R.id.action_new_readings:
                Intent intent2 = new Intent(this, NewReadingActivity.class);
                intent2.putExtras(extras);
                startActivityForResult(intent2, 1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setupBillingPeriodDetails(medidor_id);
    }
}
