package com.nicrosoft.consumoelectrico.fragments.medidor;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.jakewharton.rxbinding2.view.RxView;
import com.nicrosoft.consumoelectrico.R;
import com.nicrosoft.consumoelectrico.activities.Main;
import com.nicrosoft.consumoelectrico.activities.PeriodReadingsActivity;
import com.nicrosoft.consumoelectrico.activities.perio_details.DetailsActivity;
import com.nicrosoft.consumoelectrico.activities.reading.NewReadingActivity;
import com.nicrosoft.consumoelectrico.fragments.medidor.contracts.MedidorPresenter;
import com.nicrosoft.consumoelectrico.fragments.medidor.contracts.MedidorView;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Medidor;
import com.nicrosoft.consumoelectrico.realm.Periodo;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 19/09/2016.
 */

public class AdapterMedidor extends RecyclerView.Adapter<AdapterMedidor.ViewHolder> implements RealmChangeListener<RealmResults<Medidor>> {

    private Main context;
    private MedidorView view;
    RealmResults<Medidor> list;
    MedidorPresenter presenter;
    private EditText med;
    private EditText desc;

    public AdapterMedidor(Main context, @NonNull RealmResults<Medidor> list, MedidorView view, MedidorPresenter presenter) {
        this.context = context;
        this.view = view;
        this.list = list;
        list.addChangeListener(this);
        this.presenter = presenter;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //declarar los Widgets
        @BindView(R.id.txt_period_limit)
        TextView txtPeriodLimit;
        @BindView(R.id.period_bar)
        NumberProgressBar periodBar;
        @BindView(R.id.bnt_details)
        Button bntDetails;
        @BindView(R.id.bnt_readings)
        Button bntReadings;
        @BindView(R.id.bnt_new_readings)
        Button bnt_new_readings;

        @BindView(R.id.txt_medidor)
        TextView txtMedidor;
        @BindView(R.id.txt_initial_reading)
        TextView txtInitialReading;
        @BindView(R.id.txt_last_reading)
        TextView txtLastReading;
        @BindView(R.id.txt_current_consumption)
        TextView txtCurrentConsumption;
        @BindView(R.id.txt_days_consumed)
        TextView txtDaysConsumed;
        @BindView(R.id.txt_consumption_limit)
        TextView txtConsumptionLimit;
        @BindView(R.id.consumption_bar)
        NumberProgressBar consumptionBar;
        @BindView(R.id.card_resume)
        CardView cardResume;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.medidor_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Realm realm = Realm.getDefaultInstance();

        Medidor medidor = list.get(position);
        if (medidor != null) {

            int kw_limit = Integer.parseInt(Prefs.getString("kw_limit", "150"));
            int period_limit = Integer.parseInt(Prefs.getString("period_lenght", "30"));
            if (medidor.name != null && !medidor.name.isEmpty())
                holder.txtMedidor.setText(medidor.name);
            else
                holder.txtMedidor.setText("-----");
            Periodo periodo = presenter.getActivePeriod(medidor);
            if (periodo != null) {
                Lectura lectura = presenter.getLastReading(periodo, true);
                if (lectura != null) {
                    holder.txtDaysConsumed.setText(context.getString(R.string.days_consumed_val, String.format(Locale.getDefault(), "%02.0f", lectura.dias_periodo)));
                    holder.txtCurrentConsumption.setText(context.getString(R.string.initial_reading_val, String.format(Locale.getDefault(), "%02.0f", lectura.consumo_acumulado)));
                    holder.txtLastReading.setText(context.getString(R.string.initial_reading_val, String.format(Locale.getDefault(), "%02.0f", lectura.lectura)));
                    holder.consumptionBar.setProgress((int) lectura.consumo_acumulado);
                    holder.periodBar.setProgress((int) lectura.dias_periodo);
                }
                lectura = presenter.getFirstReading(periodo);
                if (lectura != null)
                    holder.txtInitialReading.setText(context.getString(R.string.initial_reading_val, String.format(Locale.getDefault(), "%02.0f", lectura.lectura)));
            } else {
                holder.txtInitialReading.setText("-----");
                holder.txtDaysConsumed.setText("-----");
                holder.txtCurrentConsumption.setText("-----");
                holder.txtLastReading.setText("-----");
                holder.consumptionBar.setProgress(0);
            }
            holder.consumptionBar.setMax(kw_limit);
            holder.periodBar.setMax(period_limit);
            holder.txtPeriodLimit.setText(context.getString(R.string.days_consumed_val, String.format(Locale.getDefault(), "%02d", period_limit)));
            holder.txtConsumptionLimit.setText(context.getString(R.string.initial_reading_val, String.format(Locale.getDefault(), "%02d", kw_limit)));
            RxView.clicks(holder.bntDetails).subscribe(o -> {
                Intent intentDetails = new Intent(context, DetailsActivity.class);
                intentDetails.putExtra("id", medidor.id);
                intentDetails.putExtra("name", medidor.name);
                intentDetails.putExtra("description", medidor.descripcion);
                //context.startActivityForResult(intent, 1);
                view.startNewReadingActivity(intentDetails, position);
            });
            RxView.clicks(holder.bntReadings).subscribe(o -> {
                Intent intent = new Intent(context, PeriodReadingsActivity.class);
                intent.putExtra("id", medidor.id);
                intent.putExtra("name", medidor.name);
                view.startNewReadingActivity(intent, position);
            });
            RxView.clicks(holder.bnt_new_readings).subscribe(o -> {
                Intent intent = new Intent(context, NewReadingActivity.class);
                intent.putExtra("id", medidor.id);
                intent.putExtra("name", medidor.name);
                //context.startActivityForResult(intent, 1);
                view.startNewReadingActivity(intent, position);
            });

            RxView.clicks(holder.cardResume).subscribe(o -> {
                new MaterialDialog.Builder(context)
                        .title(medidor.name)
                        .items(R.array.medidor_options)
                        .itemsCallback((dialog, itemView, position1, text) -> {
                            switch (position1) {
                                /*case 0:
                                    Intent intent = new Intent(context, NewReadingActivity.class);
                                    intent.putExtra("id", medidor.id);
                                    intent.putExtra("name", medidor.name);
                                    //context.startActivityForResult(intent, 1);
                                    view.startNewReadingActivity(intent, position);
                                    break;
                                case 1:
                                    Intent intentDetails = new Intent(context, DetailsActivity.class);
                                    intentDetails.putExtra("id", medidor.id);
                                    intentDetails.putExtra("name", medidor.name);
                                    intentDetails.putExtra("description", medidor.descripcion);
                                    //context.startActivityForResult(intent, 1);
                                    view.startNewReadingActivity(intentDetails, position);
                                    break;*/
                                case 0:
                                    MaterialDialog dlg = new MaterialDialog.Builder(context)
                                            .title(medidor.name)
                                            .customView(R.layout.dlg_medidor, true)
                                            .positiveText(R.string.save)
                                            .positiveColor(context.getResources().getColor(R.color.md_green_700))
                                            .negativeText(android.R.string.cancel)
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    realm.executeTransaction(realm1 -> {
                                                        if (med != null && !med.getText().toString().isEmpty()) {
                                                            medidor.name = med.getText().toString();
                                                        } else
                                                            Toast.makeText(context, context.getString(R.string.invalid_medidor_name), Toast.LENGTH_LONG).show();
                                                        if (desc != null) {
                                                            medidor.descripcion = desc.getText().toString();
                                                        }
                                                    });
                                                }
                                            }).build();
                                    try {
                                        med = dlg.getCustomView().findViewById(R.id.txt_medidor_name);
                                        desc = dlg.getCustomView().findViewById(R.id.txt_medidor_desc);
                                        med.setText(medidor.name);
                                        desc.setText(medidor.descripcion);
                                    } catch (Exception ignored) {
                                    }
                                    dlg.show();
                                    break;
                                case 1:
                                    new MaterialDialog.Builder(context)
                                            .title(medidor.name)
                                            .content(R.string.delete_medidor_notice)
                                            .positiveColor(context.getResources().getColor(R.color.md_red_700))
                                            .positiveText(R.string.agree).negativeText(R.string.cancel)
                                            .onPositive((dialog1, which) -> {
                                                realm.executeTransaction(realm1 -> {
                                                    RealmResults<Lectura> lecturas = realm.where(Lectura.class).equalTo("medidor.id", medidor.id).findAll();
                                                    RealmResults<Periodo> periodos = realm.where(Periodo.class).equalTo("medidor.id", medidor.id).findAll();
                                                    lecturas.deleteAllFromRealm();
                                                    periodos.deleteAllFromRealm();
                                                    medidor.deleteFromRealm();
                                                });
                                            })
                                            .show();

                                    break;
                            }
                        })
                        .show();
            }, throwable -> {
            });
        }
        if (!realm.isClosed())
            realm.close();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    @Override
    public void onChange(@NonNull RealmResults<Medidor> element) {
        notifyDataSetChanged();
        if (element.isEmpty()) {
            view.showEmptyDataMsg();
        } else {
            view.hideEmptyDataMsg();
        }
    }

}
