package com.nicrosoft.consumoelectrico.fragments.medidor.imp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nicrosoft.consumoelectrico.fragments.medidor.contracts.MedidorService;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Medidor;
import com.nicrosoft.consumoelectrico.realm.Periodo;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Eder Xavier Rojas on 11/01/2017.
 */

public class MedidorServiceImpl implements MedidorService {

    private Context context;
    private Realm realm;


    MedidorServiceImpl(Context context) {
        this.context = context;
        this.realm = Realm.getDefaultInstance();
    }

    @Override
    public RealmResults<Medidor> getMedidores() {
        try {
            return realm.where(Medidor.class).findAll().sort("name", Sort.ASCENDING);
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public Periodo getActivePeriod(Medidor medidor) {
        return realm.where(Periodo.class)
                .equalTo("medidor.id", medidor.id)
                .equalTo("activo", true)
                .findFirst();
    }

    @Override
    public Lectura getLastReading(Periodo periodo, boolean old_readings_if_more_than_a_period) {
        if(periodo!=null) {
            RealmResults<Lectura> res = realm.where(Lectura.class)
                    .equalTo("periodo.id", periodo.id)
                    .findAll().sort("fecha_lectura", Sort.DESCENDING);
            //Si el ultimo periodo activp no tiene registros mostrar los del anterior
            if(res.size()==0 && old_readings_if_more_than_a_period){
                RealmResults<Periodo> periodos = realm.where(Periodo.class)
                        .equalTo("medidor.id", periodo.medidor.id)
                        .equalTo("activo", false)
                        .findAll();
                //.findAll().sort("inicio", Sort.DESCENDING);
                Periodo p = periodos.get(periodos.size()-1);
                res = realm.where(Lectura.class)
                        .equalTo("periodo.id", p.id)
                        .findAll().sort("fecha_lectura", Sort.DESCENDING);
            }
            return (res.size()>0)? res.first(): null;
        }else
            return null;
    }

    @Override
    public Lectura getFirstReading(Periodo periodo) {
        if(periodo!=null) {
            periodo = getActivePeriod(periodo.medidor);
            RealmResults<Lectura> res = realm.where(Lectura.class)
                    .equalTo("periodo.id", periodo.id)
                    .findAll().sort("fecha_lectura", Sort.DESCENDING);
            
            return (res.size()>0)? res.last(): null;
        }else
            return null;
    }


    private Boolean readingForDateExist(Date date) {
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.setTime(date);
        to.setTime(date);
        from.set(Calendar.HOUR_OF_DAY, 0);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);
        to.set(Calendar.HOUR_OF_DAY, 23);
        to.set(Calendar.MINUTE, 59);
        to.set(Calendar.SECOND, 59);

        RealmResults<Lectura> lecturas = realm.where(Lectura.class)
                .between("fecha_lectura", from.getTime(), to.getTime())
                .findAll();

        return lecturas.size() > 0;
    }

    private Lectura getReadingForDate(Date date) {
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.setTime(date);
        to.setTime(date);
        from.set(Calendar.HOUR_OF_DAY, 0);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);
        to.set(Calendar.HOUR_OF_DAY, 23);
        to.set(Calendar.MINUTE, 59);
        to.set(Calendar.SECOND, 59);

        RealmResults<Lectura> lecturas = realm.where(Lectura.class)
                .between("fecha_lectura", from.getTime(), to.getTime())
                .findAll();

        return lecturas.first();
    }


    private void updateReadings(@NonNull Periodo periodo, Date fecha_lectura){
        Calendar calendar = Calendar.getInstance();
        Lectura lectura = getReadingForDate(fecha_lectura);
        calendar.setTime(periodo.inicio);
        LocalDate start = new LocalDate(periodo.inicio);
        RealmResults<Lectura> after = realm.where(Lectura.class)
                .greaterThan("fecha_lectura", periodo.inicio).findAll().sort("fecha_lectura");
        if(!after.isEmpty()) {
            LocalDate end = null;
            realm.beginTransaction();
            for (int i = 0; i < after.size(); i++) {
                if (i == 0) {
                    Lectura postRead = after.first();
                    end = new LocalDate(postRead.fecha_lectura);
                    Period p = new Period(start, end, PeriodType.days());
                    postRead.dias_periodo = p.getDays();
                    postRead.consumo = postRead.lectura - lectura.lectura;
                    postRead.consumo_acumulado = postRead.consumo;
                    postRead.consumo_promedio = postRead.consumo_acumulado / postRead.dias_periodo;
                    postRead.periodo = periodo;

                } else {
                    Lectura ant = after.get(i - 1);
                    Lectura act = after.get(i);
                    act.consumo = act.lectura - ant.lectura;
                    act.consumo_acumulado = ant.consumo_acumulado + act.consumo;
                    act.consumo_promedio = act.consumo_acumulado / act.dias_periodo;
                    act.periodo = periodo;
                    end = new LocalDate(act.fecha_lectura);
                    Period p = new Period(start, end, PeriodType.days());
                    act.dias_periodo = p.getDays();
                }
            }
            realm.commitTransaction();
        }
    }

}
