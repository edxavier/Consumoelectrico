package com.nicrosoft.consumoelectrico.fragments.readings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jakewharton.rxbinding2.view.RxView;
import com.nicrosoft.consumoelectrico.R;
import com.nicrosoft.consumoelectrico.activities.Main;
import com.nicrosoft.consumoelectrico.fragments.readings.contracts.LecturasPresenter;
import com.nicrosoft.consumoelectrico.fragments.readings.contracts.LecturasView;
import com.nicrosoft.consumoelectrico.realm.Lectura;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 19/09/2016.
 */

public class AdapterReadings extends RecyclerView.Adapter<AdapterReadings.ViewHolder> implements RealmChangeListener<RealmResults<Lectura>> {

    private Context context;
    private LecturasView view;
    RealmResults<Lectura> list;
    LecturasPresenter presenter;

    public AdapterReadings(Context context, @NonNull RealmResults<Lectura> list, LecturasView view, LecturasPresenter presenter) {
        this.context = context;
        this.view = view;
        this.list = list;
        list.addChangeListener(this);
        this.presenter = presenter;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //declarar los Widgets

        @Nullable
        @BindView(R.id.txtObservaciones)
        TextView txtObservaciones;
        @Nullable
        @BindView(R.id.txtFecha)
        TextView txtFecha;
        @Nullable
        @BindView(R.id.txtLectura)
        TextView txtLectura;
        @Nullable
        @BindView(R.id.txtPromedio)
        TextView txtPromedio;
        @Nullable
        @BindView(R.id.txtTendencia)
        ImageView txtTendencia;
        @Nullable
        @BindView(R.id.reading_row_container)
        LinearLayout readingRowContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.reading_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        SimpleDateFormat time_format = new SimpleDateFormat(context.getString(R.string.date_format), Locale.getDefault());

        Lectura lect = list.get(position);
        if (position < list.size() - 1) {
            Lectura la = list.get(position + 1);
            if (lect.consumo_promedio > la.consumo_promedio) {
                holder.txtTendencia.setRotation(315);
            } else if (lect.consumo_promedio < la.consumo_promedio) {
                holder.txtTendencia.setRotation(45);
            } else {
                holder.txtTendencia.setRotation(0);
            }
        } else {
            holder.txtTendencia.setRotation(0);
            DrawableCompat.setTint(holder.txtTendencia.getDrawable(), ContextCompat.getColor(context, R.color.md_black_1000_75));
        }

        if(lect.observacion !=null && !lect.observacion.isEmpty()) {
            holder.txtObservaciones.setText(lect.observacion);
            holder.txtObservaciones.setVisibility(View.VISIBLE);
        }else {
            holder.txtObservaciones.setVisibility(View.GONE);
            holder.txtObservaciones.setText("");
        }
        if( (position + 1) % 2 !=0) {
            holder.readingRowContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.md_green_50));
        }
        holder.txtFecha.setText(time_format.format(lect.fecha_lectura));
        String lectura = String.format(Locale.getDefault(), "%.0f kWh", lect.lectura);
        String lectura_val = String.format(Locale.getDefault(), "%.0f", lect.lectura);
        holder.txtLectura.setText(lectura);
        holder.txtPromedio.setText(String.format(Locale.getDefault(), "%.2f kWh", lect.consumo_promedio));
        RxView.clicks(holder.readingRowContainer).subscribe(click -> {
            new MaterialDialog.Builder(context)
                    .title(time_format.format(lect.fecha_lectura))
                    .items(R.array.readings_options)
                    .itemsCallback((dialog, itemView, position1, text) -> {
                        switch (position1) {
                            case 0:
                                    new MaterialDialog.Builder(context)
                                            .title(time_format.format(lect.fecha_lectura))
                                            .content(R.string.end_period_ask)
                                            .onPositive((dialog1, which) -> {
                                                presenter.endPeriod(lect);
                                            })
                                            .positiveText(R.string.end_period_psoitive)
                                            .negativeText(R.string.cancel)
                                            .show();
                                break;
                            case 1:
                                new MaterialDialog.Builder(context)
                                        //.icon(ContextCompat.getDrawable(context, R.drawable.ic_mode_edit))
                                        .title(time_format.format(lect.fecha_lectura))
                                        .content(R.string.edit_reading)
                                        .inputType(InputType.TYPE_CLASS_NUMBER)
                                        .input(null, lectura_val, new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(MaterialDialog dialog, @NonNull CharSequence input) {
                                                if(presenter.isValueOverange(lect, input.toString()))
                                                    Toast.makeText(context, R.string.alert_over_range, Toast.LENGTH_LONG).show();
                                                else
                                                    presenter.updateReading(lect, input.toString());
                                            }
                                        }).positiveText(R.string.save)
                                        .negativeText(R.string.cancel)
                                        .show();
                                break;
                        }
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    @Override
    public void onChange(@NonNull RealmResults<Lectura> element) {
        notifyDataSetChanged();
        if (element.isEmpty()) {
            //view.showEmptyMessage();
        } else {
        }
        //view.hideEmptyMessage();
    }

}
