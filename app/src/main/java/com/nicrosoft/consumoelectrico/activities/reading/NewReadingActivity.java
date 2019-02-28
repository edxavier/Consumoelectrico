package com.nicrosoft.consumoelectrico.activities.reading;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.nicrosoft.consumoelectrico.MySnackbar;
import com.nicrosoft.consumoelectrico.R;
import com.nicrosoft.consumoelectrico.activities.reading.contracts.ReadingPresenter;
import com.nicrosoft.consumoelectrico.activities.reading.contracts.ReadingView;
import com.nicrosoft.consumoelectrico.activities.reading.impl.ReadingPresenterImpl;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Periodo;
import com.pixplicity.easyprefs.library.Prefs;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.google.android.material.snackbar.Snackbar.LENGTH_LONG;
import static android.text.TextUtils.isEmpty;

public class NewReadingActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, ReadingView {

    @Nullable
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @Nullable
    @BindView(R.id.coordinator)
    CoordinatorLayout coordinator;
    SimpleDateFormat time_format;

    @Nullable
    @BindView(R.id.lbl_fecha_lectura)
    TextView lblFechaLectura;
    @BindView(R.id.txt_medidor_name)
    TextView txt_medidor_name;
    @Nullable
    @BindView(R.id.txt_lectura)
    EditText txtLectura;
    @Nullable
    @BindView(R.id.txt_ilayout_lectura)
    TextInputLayout txtIlayoutLectura;
    @Nullable
    @BindView(R.id.checkBox)
    CheckBox checkBox;
    @Nullable
    @BindView(R.id.txtObservacion)
    EditText txtObservacion;

    Date fecha_lectura;
    ReadingPresenter presenter;
    @Nullable
    @BindView(R.id.fab2)
    FloatingActionButton fab2;
    @BindView(R.id.end_period_sw)
    SwitchCompat end_period_sw;
    @BindView(R.id.txt_last_reading)
    TextView txtLastReading;
    @BindView(R.id.adView)
    AdView adView;

    private Observable<CharSequence> readinObservable;
    boolean validReading = false;
    boolean show_details_after_save = false;
    Periodo periodoActivo;

    @Nullable
    @BindView(R.id.activity_help)
    ScrollView activityHelp;

    String medidor_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reading);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        time_format = new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault());
        fecha_lectura = new Date();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            medidor_id = extras.getString("id");
            txt_medidor_name.setText(extras.getString("name"));
        }

        presenter = new ReadingPresenterImpl(this);
        periodoActivo = presenter.getActivePeriod(medidor_id);
        Lectura lastReading = null;
        if (periodoActivo != null) {
            lastReading = presenter.getLastReading(periodoActivo, true);
        }

        if (lastReading == null) {
            end_period_sw.setEnabled(false);
            new MaterialDialog.Builder(this)
                    .title(R.string.notice)
                    .content(R.string.first_reading_notice)
                    .positiveText(R.string.ok)
                    .show();
        } else {
            String lrd = time_format.format(lastReading.fecha_lectura);
            String val_hint = getString(R.string.last_reading_suggest, lrd, String.format(Locale.getDefault(), "%02.0f", lastReading.lectura));
            txtLastReading.setText(val_hint);
        }

        lblFechaLectura.setText(time_format.format(fecha_lectura));
        readinObservable = RxTextView.textChanges(txtLectura).skip(1);
        readinObservable.debounce(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(charSequence -> {
                    if (!(!isEmpty(charSequence) && charSequence.length() >= 1)) {
                        txtIlayoutLectura.setErrorEnabled(true);
                        txtIlayoutLectura.setError(getResources().getString(R.string.activity_new_reading_input_error));
                        validReading = false;
                    } else {
                        validReading = true;
                        txtIlayoutLectura.setErrorEnabled(false);
                        txtIlayoutLectura.setError("");
                        //Lectura lectura = presenter.getLastReading();
                        checkIfValidReading();
                    }
                }, throwable -> {
                });

        if(!Prefs.getBoolean("isPurchased", false))
            showAds();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            case R.id.action_calendar:
                Calendar now = Calendar.getInstance();
                Calendar ago = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        NewReadingActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setMaxDate(now);
                dpd.setOkColor(ContextCompat.getColor(this, R.color.md_white_1000));
                dpd.setCancelColor(ContextCompat.getColor(this, R.color.md_white_1000));
                if (periodoActivo != null) {
                    ago.setTime(periodoActivo.inicio);
                    dpd.setMinDate(ago);
                }

                dpd.show(getSupportFragmentManager(), "Datepickerdialog");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar ca = Calendar.getInstance();
        ca.set(year, monthOfYear, dayOfMonth);
        SimpleDateFormat time_format = new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault());
        lblFechaLectura.setText(time_format.format(ca.getTime()));
        fecha_lectura = ca.getTime();
        //verificar si existe un registro para esa fecha, si no, verificar que sea valido
        if (presenter.readingForDateExist(fecha_lectura, periodoActivo)) {
            MySnackbar.warning(coordinator,
                    getResources().getString(R.string.activity_new_reading_snack_warning)
                    , LENGTH_LONG).show();
        } else {
            checkIfValidReading();
        }

    }

    private void checkIfValidReading() {
        if (!txtLectura.getText().toString().isEmpty()) {
            boolean overRange = presenter
                    .isReadingOverRange(Float.valueOf(txtLectura.getText().toString()),
                            fecha_lectura, periodoActivo);
            if (overRange) {
                txtIlayoutLectura.setErrorEnabled(true);
                txtIlayoutLectura.setError(getResources().getString(R.string.activity_new_reading_input_error2));
                validReading = false;
            } else {
                validReading = true;
                txtIlayoutLectura.setErrorEnabled(false);
                txtIlayoutLectura.setError("");
            }
        }
    }


    @Override
    public void showInfo(String msg) {


    }

    @Override
    public void showWarning(String msg) {

    }


    @OnClick(R.id.fab2)
    public void onClick2() {
        hideKeyboard();
        if (!validReading) {
            MySnackbar.warning(coordinator, getString(R.string.activity_new_reading_error1), LENGTH_LONG).show();
            return;
        }
        if (presenter.readingForDateExist(fecha_lectura, periodoActivo)) {
            MySnackbar.warning(coordinator, getString(R.string.activity_new_reading_snack_warning), LENGTH_LONG).show();
            return;
        }
        Lectura lectura = new Lectura();
        lectura.fecha_lectura = fecha_lectura;
        lectura.lectura = Float.valueOf(txtLectura.getText().toString().trim());
        lectura.observacion = txtObservacion.getText().toString();

        if (end_period_sw.isChecked()) {
            new MaterialDialog.Builder(this)
                    .title(R.string.activity_new_reading_dialog_title)
                    .content(R.string.activity_new_reading_dialog_content)
                    .positiveText(R.string.end_period_psoitive)
                    .negativeText(R.string.cancel)
                    .positiveColor(getResources().getColor(R.color.md_red_500))
                    .onNegative((dialog, which) -> {
                        end_period_sw.setChecked(false);
                    })
                    .onPositive((dialog, which) -> {
                        if (presenter.saveReading(lectura, end_period_sw.isChecked(), medidor_id)) {
                            finish();
                        }else
                            MySnackbar.alert(coordinator,
                                    getResources().getString(R.string.activity_new_reading_snack_warning),
                                    LENGTH_LONG).show();
                    })
                    .show();
        } else {
            if (presenter.saveReading(lectura, end_period_sw.isChecked(), medidor_id)) {
                finish();
            }else
                MySnackbar.alert(coordinator, getResources().getString(R.string.activity_new_reading_snack_warning), LENGTH_LONG).show();
        }


    }


    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception ignored) {
        }
    }

    private void showAds() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
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

}
