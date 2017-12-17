package com.nicrosoft.consumoelectrico.fragments.main;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.realm.implementation.RealmBarDataSet;
import com.github.mikephil.charting.data.realm.implementation.RealmLineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.NativeExpressAdView;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.jakewharton.rxbinding2.view.RxView;
import com.nicrosoft.consumoelectrico.BuildConfig;
import com.nicrosoft.consumoelectrico.MySnackbar;
import com.nicrosoft.consumoelectrico.R;
import com.nicrosoft.consumoelectrico.activities.Main;
import com.nicrosoft.consumoelectrico.activities.reading.NewReadingActivity;
import com.nicrosoft.consumoelectrico.fragments.main.chart_helpers.ChartStyler;
import com.nicrosoft.consumoelectrico.fragments.main.contracts.MainPresenter;
import com.nicrosoft.consumoelectrico.fragments.main.contracts.MainView;
import com.nicrosoft.consumoelectrico.fragments.main.imp.MainPresenterImpl;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.pixplicity.easyprefs.library.Prefs;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements MainView, OnChartValueSelectedListener, DatePickerDialog.OnDateSetListener, BillingProcessor.IBillingHandler {


    @Nullable
    @BindView(R.id.resumen_consumo_arc)
    DecoView resumenConsumoArc;
    @Nullable
    @BindView(R.id.txtUltimaLectura)
    TextView txtUltimaLectura;

    MainPresenter presenter;
    Main activity;
    @Nullable
    @BindView(R.id.textPercentage)
    TextView textPercentage;
    @Nullable
    @BindView(R.id.coordinator)
    CoordinatorLayout coordinator;
    @Nullable
    LineData combinedLineData = null;

    @Nullable
    @BindView(R.id.chart2)
    CombinedChart combinedChart;
    @Nullable
    @BindView(R.id.txtUltimaLecturaValor)
    TextView txtUltimaLecturaValor;

    SeriesItem seriesItem2;
    int series1Index;
    @Nullable
    @BindView(R.id.chart)
    LineChart chart;
    @Nullable
    @BindView(R.id.fab2)
    FloatingActionButton fab2;
    @Nullable
    @BindView(R.id.txt_period_avg)
    TextView txtPeriodAvg;
    @Nullable
    @BindView(R.id.txt_inicio_periodo)
    TextView txtInicioPeriodo;
    @Nullable
    @BindView(R.id.txt_dias_periodo)
    TextView txtDiasPeriodo;
    @Nullable
    @BindView(R.id.activity_help)
    NestedScrollView activityHelp;
    @Nullable
    @BindView(R.id.txt_longitud_periodo)
    TextView txtLongitudPeriodo;
    @Nullable
    @BindView(R.id.txt_periodo_proyection)
    TextView txtPeriodoProyection;
    @Nullable
    @BindView(R.id.card_resume)
    CardView cardResume;
    @Nullable
    @BindView(R.id.card_backgnd)
    ImageView cardBackgnd;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.admob_container)
    LinearLayout admobContainer;
    BillingProcessor bp;


    boolean purchased = false;
    private String PRODUCT_SKU = "remove_ads";

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_main, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bp = new BillingProcessor(getActivity(), BuildConfig.APP_BILLING_PUB_KEY, BuildConfig.MERCHANT_ID, this);

        activity = (Main) getActivity();
        presenter = new MainPresenterImpl(getActivity(), this);
        initCharts();
        presenter.getResumeData(false);
        presenter.getHistoryReadings();
        setRandomBackground();
        RxView.clicks(resumenConsumoArc).subscribe(click -> callDialog());
        RxView.clicks(cardResume).subscribe(click -> callDialog());
    }

    private void setRandomBackground() {
        Random rand = new Random();
        int x = rand.nextInt(3);
        switch (x) {
            case 0:
                cardBackgnd.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.green_bulb));
                break;
            case 1:
                cardBackgnd.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.turbinas));
                break;
            case 2:
                cardBackgnd.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.room));
                break;
            case 3:
                cardBackgnd.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.room));
                break;


        }
    }

    private void callDialog() {
        MaterialDialog.Builder dlg = new MaterialDialog.Builder(getActivity())
                //.icon(ContextCompat.getDrawable(context, R.drawable.ic_bulb))
                .title(R.string.options)
                .items(R.array.main_options)
                .itemsCallback((dialog, itemView, position1, text) -> {
                    switch (position1) {
                        case 0:
                            startActivityForResult(new Intent(getActivity(), NewReadingActivity.class), 1);
                            break;
                        case 1:
                            if (!presenter.isRecordsEmpty()) {
                                showDialog();
                            } else {
                                String err = getActivity().getResources().getString(R.string.main_fragment_end_period_snack_content_error2);
                                MySnackbar.warning(coordinator,
                                        err, Snackbar.LENGTH_LONG)
                                        .show();
                            }
                            break;
                    }
                });
        dlg.itemsDisabledIndices(1);
        dlg.show();

    }


    void initCharts() {
        int kw_limit = Integer.parseInt(Prefs.getString("kw_limit", "150"));
        SeriesItem seriesItem = new SeriesItem.Builder(getResources().getColor(R.color.decoview_color_total_color))
                .setRange(0, kw_limit, kw_limit)
                .setLineWidth(18f)
                .build();
        seriesItem2 = new SeriesItem.Builder(getResources().getColor(R.color.decoview_color_value_color))
                .setRange(0, kw_limit, 0)
                .setLineWidth(18f)
                .build();

        resumenConsumoArc.addSeries(seriesItem);
        series1Index = resumenConsumoArc.addSeries(seriesItem2);
        chart = (LineChart) ChartStyler.setup(chart, getActivity());
        combinedChart = (CombinedChart) ChartStyler.setup(combinedChart, getActivity());
        combinedChart.setOnChartValueSelectedListener(this);

    }


    @Override
    public void setResumeData(@Nullable Lectura lectura, boolean afterSave) {
        int dias_periodo = Integer.parseInt(Prefs.getString("period_lenght", "30"));
        if (lectura != null) {
            SimpleDateFormat time_format = new SimpleDateFormat("dd MMM yyy", Locale.getDefault());
            txtUltimaLectura.setText(time_format.format(lectura.fecha_lectura));
            txtUltimaLecturaValor.setText(String.format(Locale.getDefault(), "%.0f  kWh", lectura.lectura));
            showArcAnimated(lectura);
            String days = getResources().getString(R.string.label_days);
            String day = getResources().getString(R.string.label_day);

            txtPeriodAvg.setText(String.format(Locale.getDefault(), "%.2f  kWh/" + day.toLowerCase(), lectura.consumo_promedio));
            txtInicioPeriodo.setText(time_format.format(lectura.periodo.inicio));
            txtDiasPeriodo.setText(String.format(Locale.getDefault(), "%.0f " + days.toLowerCase(), lectura.dias_periodo));
            txtLongitudPeriodo.setText(String.format(Locale.getDefault(), "%02d " + days.toLowerCase(), dias_periodo));
            int dias_restantes = (int) (dias_periodo - lectura.dias_periodo);
            float proyection = (dias_restantes * lectura.consumo_promedio) + lectura.consumo_acumulado;
            txtPeriodoProyection.setText(String.format(Locale.getDefault(), "%.2f  kWh", proyection));

        }
    }

    @Override
    public void setHistoryReadings(@NonNull RealmResults<Lectura> lecturas) {
        try {
            LineData period_limit_lineData;
            CombinedData combinedData = new CombinedData();
            period_limit_lineData = new LineData(ChartStyler.drawPeriodLimitLine(getActivity()));
            combinedLineData = new LineData(ChartStyler.drawAvgLimitLine(getActivity()));

            if (lecturas.size() > 0) {
                RealmLineDataSet<Lectura> combinedLineDataSet = new RealmLineDataSet<>(lecturas, "dias_periodo", "consumo_promedio");
                RealmLineDataSet<Lectura> acumuladoDataSet = new RealmLineDataSet<>(lecturas, "dias_periodo", "consumo_acumulado");

                RealmBarDataSet<Lectura> barDataset = new RealmBarDataSet<>(lecturas, "dias_periodo", "consumo");
                combinedData.setData(ChartStyler.setBardata(barDataset, getActivity()));

                period_limit_lineData.addDataSet(ChartStyler.setAcumuladoPeriodLine(acumuladoDataSet, getActivity()));
                period_limit_lineData.addDataSet(ChartStyler.setProyectionPeriodLine(lecturas, getActivity()));
                chart.setData(period_limit_lineData);
                combinedLineData.addDataSet(ChartStyler.setCombinedLine(combinedLineDataSet, getActivity()));
                combinedData.setData(combinedLineData);
                combinedChart.setData(combinedData);
                combinedChart.invalidate();
            } else {
                chart.setData(period_limit_lineData);
                chart.invalidate();
                combinedData.setData(combinedLineData);
                combinedChart.setData(combinedData);
                combinedChart.invalidate();
            }
        } catch (Exception e) {
            //Log.e("EDER_Exception", e.getMessage());
        }
    }


    void showArcAnimated(@NonNull Lectura lectura) {
        seriesItem2.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                //float percentFilled = ((currentPosition - seriesItem2.getMinValue()) / (seriesItem2.getMaxValue() - seriesItem2.getMinValue()));
                textPercentage.setText(String.format(Locale.getDefault(), "%.0f", currentPosition));
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {
            }
        });
        if (resumenConsumoArc != null) {
            resumenConsumoArc.addEvent(new DecoEvent.Builder(lectura.consumo_acumulado)
                    .setIndex(series1Index)
                    .setDuration(1500)
                    .build());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.getResumeData(true);
        presenter.getHistoryReadings();

        if(!purchased)
            requestAds();
    }
    private void requestAds() {
        int ne =  Prefs.getInt("num_show_readings", 0);
        Prefs.putInt("num_show_readings", ne + 1);

        if(Prefs.getInt("num_show_readings", 0) >= Prefs.getInt("show_after", 8)) {
            Prefs.putInt("num_show_readings", 0);
            Random r = new Random();
            int Low = 10;int High = 17;
            int rnd = r.nextInt(High-Low) + Low;
            Prefs.putInt("show_after", rnd);

            AdRequest adRequest = new AdRequest.Builder()
                    //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    //.addTestDevice("0B307F34E3DDAF6C6CAB28FAD4084125")
                    //.addTestDevice("B0FF48A19BF36BD2D5DCD62163C64F45")
                    .build();

            InterstitialAd mInterstitialAd;
            mInterstitialAd = new InterstitialAd(getActivity());
            mInterstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstical));
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mInterstitialAd.show();
                }
            });
            mInterstitialAd.loadAd(adRequest);
        }
    }



    @Override
    public void showEmptyDataMsg() {
        MySnackbar.info(coordinator,
                getString(R.string.no_data_chart), Snackbar.LENGTH_LONG)
                .show();
    }

    @OnClick(R.id.fab2)
    public void onClick() {
        startActivityForResult(new Intent(getActivity(), NewReadingActivity.class), 1);
    }

    @Override
    public void onValueSelected(Entry e, @NonNull Highlight h) {
        Toast.makeText(getActivity(), String.valueOf(h.getY()) + " kWh", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar ca = Calendar.getInstance();
        ca.set(year, monthOfYear, dayOfMonth);
        SimpleDateFormat time_format = new SimpleDateFormat("dd-MMM-yy", Locale.getDefault());
        if (!presenter.thereIsRecordForDate(ca.getTime())) {
            startActivityForResult(new Intent(getActivity(), NewReadingActivity.class), 1);
        } else {
            String error = getActivity().getResources().getString(R.string.main_fragment_end_period_snack_content_error);
            String ok = getActivity().getResources().getString(R.string.main_fragment_end_period_snack_content_ok);
            if (presenter.endPeriod(ca.getTime()))
                MySnackbar.info(coordinator, ok, Snackbar.LENGTH_LONG).show();
            else
                MySnackbar.alert(coordinator, error, Snackbar.LENGTH_LONG).show();
        }

    }

    void showDialog() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.activity_new_reading_dialog_title)
                .content(R.string.activity_main_dialog_end_period_content)
                .onPositive((dialog1, which) -> {
                    Calendar now = Calendar.getInstance();
                    Calendar ago = Calendar.getInstance();
                    DatePickerDialog dpd = DatePickerDialog.newInstance(
                            this,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );
                    dpd.setMaxDate(now);
                    if (presenter.getActivePeriod() != null) {
                        ago.setTime(presenter.getActivePeriod().inicio);
                        dpd.setMinDate(ago);
                    }

                    dpd.show(getFragmentManager(), "Datepickerdialog");
                })
                .positiveText(R.string.end_period_psoitive)
                .negativeText(R.string.cancel)
                .show();

    }

    private void showAds() {

        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("0B307F34E3DDAF6C6CAB28FAD4084125")
                //.addTestDevice("B0FF48A19BF36BD2D5DCD62163C64F45")
                .build();

        NativeExpressAdView ads = new NativeExpressAdView(getActivity());
        ads.setAdSize(new AdSize(300, 82));
        ads.setAdUnitId(getActivity().getResources().getString(R.string.admob_nativeS002));
        ads.loadAd(adRequest);
        ads.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                admobContainer.setVisibility(View.VISIBLE);
            }
        });
        admobContainer.addView(ads);
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
    public void onProductPurchased(String productId, TransactionDetails details) {
        adView.setVisibility(View.GONE);
        admobContainer.setVisibility(View.GONE);
    }

    @Override
    public void onPurchaseHistoryRestored() {
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Log.e("EDER", "SHOW");
        showAds();
    }

    @Override
    public void onBillingInitialized() {
        if(!bp.isPurchased(PRODUCT_SKU))
            showAds();
        else
            purchased = true;
    }
}
