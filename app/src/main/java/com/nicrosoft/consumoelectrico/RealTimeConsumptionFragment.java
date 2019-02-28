package com.nicrosoft.consumoelectrico;


import android.app.Fragment;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jakewharton.rxbinding2.view.RxView;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class RealTimeConsumptionFragment extends Fragment {


    @BindView(R.id.play_icon)
    AppCompatImageView playIcon;
    @BindView(R.id.chronometer)
    Chronometer chronometer;
    @BindView(R.id.timer_area)
    RelativeLayout timerArea;
    @BindView(R.id.txt_consumption)
    TextView txtConsumption;
    @BindView(R.id.consumption_area)
    RelativeLayout consumptionArea;
    Unbinder unbinder;
    boolean playing = false;
    float pulses = 0f;
    @BindView(R.id.consumption_icon)
    AppCompatImageView consumptionIcon;
    @BindView(R.id.txt_resume)
    TextView txtResume;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.txt_hint)
    TextView txtHint;

    private EditText pulses_per_kwh;


    public RealTimeConsumptionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_real_time_consumption, container, false);
        unbinder = ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RxView.clicks(timerArea).subscribe(o -> {
            if (!playing) {
                playing = true;
                txtResume.setText("");
                txtConsumption.setText("00 kWh");
                pulses = 0;
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                playIcon.setImageResource(R.drawable.ic_stop);
                consumptionArea.setAlpha(1f);
                consumptionIcon.setImageResource(R.drawable.ic_pulse_line);
                txtHint.setVisibility(View.VISIBLE);
            } else {
                txtHint.setVisibility(View.GONE);
                calculateConsumption();
                consumptionArea.setAlpha(0.8f);
                playing = false;
                chronometer.stop();
                playIcon.setImageResource(R.drawable.ic_play_arrow);
                consumptionIcon.setImageResource(R.drawable.ic_flash);
            }
        }, throwable -> {
        });

        RxView.clicks(consumptionArea).subscribe(o -> {
            if (playing) {
                calculateConsumption();
            } else {
                consumptionArea.setAlpha(0.7f);
            }
        }, throwable -> {
        });
        if (!Prefs.getBoolean("isPurchased", false))
            showAds();

        if (Prefs.getBoolean("firstTimeCalculator", true))
            showExplanation();

    }


    void showExplanation() {
        //
        MaterialDialog dlg = new MaterialDialog.Builder(getActivity())
                .title(R.string.notice)
                .content(R.string.calculator_explanation)
                .negativeText(R.string.not_show_again)
                .negativeColor(getResources().getColor(R.color.md_red_700))
                .onNegative((dialog, which) -> {
                    Prefs.putBoolean("firstTimeCalculator", false);
                })
                .positiveText(R.string.ok).build();
        dlg.show();
    }

    void calculateConsumption() {
        pulses += 1f;
        float pulses_per_kwh = Float.parseFloat(Prefs.getString("pulses_per_kwh", "1600"));
        float timeElapsed = (SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000;
        float P = (3600f / pulses_per_kwh) * (pulses / timeElapsed);
        txtConsumption.setText(String.format(Locale.getDefault(), "%02.2f kWh", P));
        consumptionArea.setAlpha(1f);
        float dayly_kwh = P * 24;
        /*float discount_kwh = Float.parseFloat(Prefs.getString("discount_kwh", "0"));
        float price_kwh = Float.parseFloat(Prefs.getString("price_kwh", "1"));
        float fixed_charges = Float.parseFloat(Prefs.getString("fixed_charges", "0"));
        float dayly_expenses = (dayly_kwh * (price_kwh - discount_kwh)) + fixed_charges;
        String dayly_expenses_str = Prefs.getString("price_simbol", "$") + String.valueOf(dayly_expenses);*/
        String r = getString(R.string.calculator_resume, String.format(Locale.getDefault(), "%02.2f", dayly_kwh));
        txtResume.setText(r);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void showAds() {
        try {
            AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    if(adView!=null)
                        adView.setVisibility(View.VISIBLE);
                }
            });
        }catch (Exception ignored){}
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.calculator_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_pulses_set) {
            MaterialDialog dlg = new MaterialDialog.Builder(getActivity())
                    .title(R.string.calculator_title)
                    .customView(R.layout.dlg_pulses_per_kwh, true)
                    .positiveText(R.string.save)
                    .positiveColor(getResources().getColor(R.color.md_green_700))
                    .negativeText(android.R.string.cancel)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (pulses_per_kwh != null && !pulses_per_kwh.getText().toString().isEmpty()) {
                                Prefs.putString("pulses_per_kwh", pulses_per_kwh.getText().toString());
                            } else
                                Toast.makeText(getActivity(), getString(R.string.activity_new_reading_snack_warning), Toast.LENGTH_LONG).show();
                        }
                    }).build();
            try {
                pulses_per_kwh = dlg.getCustomView().findViewById(R.id.txt_pulses_kwh);
                pulses_per_kwh.setText(Prefs.getString("pulses_per_kwh", "1600"));
            } catch (Exception ignored) {
            }
            dlg.show();
        }
        if (item.getItemId() == R.id.ac_calculator_help) {
            showExplanation();
        }
        return super.onOptionsItemSelected(item);
    }
}
