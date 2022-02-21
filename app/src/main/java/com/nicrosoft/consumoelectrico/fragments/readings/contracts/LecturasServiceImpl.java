/**
 package com.nicrosoft.consumoelectrico.fragments.readings.contracts;

import androidx.annotation.NonNull;

import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Medidor;
import com.nicrosoft.consumoelectrico.realm.Periodo;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

 * Created by Eder Xavier Rojas on 17/02/2017.

public class LecturasServiceImpl implements LecturasService {
    private Realm realm;


    LecturasServiceImpl() {
        this.realm = Realm.getDefaultInstance();
    }

    @Override
    public RealmResults<Lectura> getReadings(String medidor_id, boolean get_all) {
        if(get_all){
            return realm.where(Lectura.class)
                    .equalTo("periodo.medidor.id", medidor_id)
                    .findAll().sort("fecha_lectura", Sort.DESCENDING);
        }else {
            return realm.where(Lectura.class)
                    .equalTo("periodo.activo", true)
                    .equalTo("periodo.medidor.id", medidor_id)
                    .findAll().sort("fecha_lectura", Sort.DESCENDING);
        }
    }

    @Override
    public void onDestroy() {
        realm.close();
    }

    @Override
    public void updateReading(@NonNull Lectura lectura, String newReading) {
        realm.beginTransaction();
        lectura.lectura = Float.valueOf(newReading);
        RealmResults<Lectura> results = realm.where(Lectura.class)
                .equalTo("periodo.id", lectura.periodo.id)
                .findAll().sort("fecha_lectura");

        RealmResults<Lectura> before = results.where()
                .lessThan("fecha_lectura", lectura.fecha_lectura).findAll().sort("fecha_lectura", Sort.DESCENDING);
        RealmResults<Lectura> after = results.where()
                .greaterThan("fecha_lectura", lectura.fecha_lectura).findAll().sort("fecha_lectura");
        //Si existe registro posterior recalcular los valores para ese registro
        if(!before.isEmpty()){
            Lectura preRead = before.first();
            lectura.consumo = lectura.lectura - preRead.lectura;
            lectura.consumo_acumulado = lectura.consumo + preRead.consumo_acumulado;
            lectura.consumo_promedio = lectura.consumo_acumulado / lectura.dias_periodo;
        }

        if(!after.isEmpty()){
            Lectura postRead = after.first();
            postRead.consumo = postRead.lectura - lectura.lectura;
            postRead.consumo_acumulado = lectura.consumo_acumulado + postRead.consumo;
            postRead.consumo_promedio = postRead.consumo_acumulado / postRead.dias_periodo;
        }

        realm.commitTransaction();
    }

    @Override
    public void endPeriod(@NonNull Lectura lectura) {
        realm.beginTransaction();
        Periodo pa = getActivePeriod(lectura.medidor.id);
        pa.fin = lectura.fecha_lectura;
        pa.dias_periodo = lectura.dias_periodo;
        pa.activo = false;
        Periodo nuevoPeriodo = new Periodo(lectura.fecha_lectura, true);
        nuevoPeriodo.medidor =  realm.where(Medidor.class).equalTo("id", lectura.medidor.id).findFirst();
        nuevoPeriodo = realm.copyToRealm(nuevoPeriodo);
        //la ultima lectura del periodo activo es la primera del nuevo periodo
        Lectura lectura_inicio_periodo = new Lectura();
        lectura_inicio_periodo.consumo = 0;
        lectura_inicio_periodo.consumo_acumulado = 0;
        lectura_inicio_periodo.consumo_promedio = 0;
        lectura_inicio_periodo.dias_periodo = 0;
        lectura_inicio_periodo.periodo = nuevoPeriodo;
        lectura_inicio_periodo.medidor = nuevoPeriodo.medidor;
        lectura_inicio_periodo.lectura = lectura.lectura;
        lectura_inicio_periodo.fecha_lectura = lectura.fecha_lectura;
        realm.copyToRealm(lectura_inicio_periodo);

        nuevoPeriodo.medidor = pa.medidor;
        realm.copyToRealm(nuevoPeriodo);
        realm.commitTransaction();
        pa = getActivePeriod(pa.medidor.id);
        updateReadings(pa, lectura_inicio_periodo);
    }

    @Override
    public boolean deleteEntry(Lectura lectura) {
        try {
            final boolean[] success = {true};
            realm.executeTransaction(realm1 -> {
                //obtener las lecturas del periodo activo
                RealmResults<Lectura> results = realm.where(Lectura.class)
                        .equalTo("periodo.id", lectura.periodo.id)
                        .findAll().sort("fecha_lectura");

                RealmResults<Lectura> before = results.where()
                        .lessThan("fecha_lectura", lectura.fecha_lectura).findAll().sort("fecha_lectura", Sort.DESCENDING);

                // obtener las lecturas posteriores a la que se va a eliminar
                RealmResults<Lectura> after = results.where()
                        .greaterThan("fecha_lectura", lectura.fecha_lectura).findAll().sort("fecha_lectura");
                //Si existe registro posterior recalcular los valores para ese registro
                Lectura preRead = null;
                if (!before.isEmpty()) {
                    preRead = before.first();
                    lectura.deleteFromRealm();
                }else {
                    success[0] = false;
                    return;
                }

                if (!after.isEmpty()) {
                    Lectura postRead = after.first();
                    postRead.consumo = postRead.lectura - preRead.lectura;
                    postRead.consumo_acumulado = preRead.consumo_acumulado + postRead.consumo;
                    postRead.consumo_promedio = postRead.consumo_acumulado / postRead.dias_periodo;
                }
            });
            return success[0];
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean isValueOverange(@NonNull Lectura lectura, String newValue){
        try {
            float value = Float.valueOf(newValue);
            RealmResults<Lectura> results = realm.where(Lectura.class)
                    .equalTo("periodo.id", lectura.periodo.id)
                    .findAll().sort("fecha_lectura");
            RealmResults<Lectura> before = results.where()
                    .lessThan("fecha_lectura", lectura.fecha_lectura).findAll().sort("fecha_lectura", Sort.DESCENDING);
            RealmResults<Lectura> after = results.where()
                    .greaterThan("fecha_lectura", lectura.fecha_lectura).findAll().sort("fecha_lectura");
            boolean beforeOverRange = false;
            boolean afterOverRange = false;


            if(before.size()>0) {
                beforeOverRange = before.first().lectura > value;
            }
            if(after.size()>0) {
                afterOverRange = after.first().lectura < value;
            }
            return beforeOverRange || afterOverRange;
        }catch (Exception e){
            return true;
        }
    }

    private Periodo getActivePeriod(String medidor_id) {
        return realm.where(Periodo.class)
                .equalTo("medidor.id", medidor_id)
                .equalTo("activo", true)
                .findFirst();
    }

    private void updateReadings(@NonNull Periodo periodo, Lectura primerLectura){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(periodo.inicio);
        LocalDate start = new LocalDate(periodo.inicio);
        RealmResults<Lectura> after = realm.where(Lectura.class)
                .equalTo("periodo.medidor.id", periodo.medidor.id)
                .greaterThan("fecha_lectura", periodo.inicio)
                .findAll().sort("fecha_lectura");
        if(!after.isEmpty()) {
            LocalDate end = null;
            realm.beginTransaction();
            for (int i = 0; i < after.size(); i++) {
                if (i == 0) {
                    Lectura postRead = after.first();
                    end = new LocalDate(postRead.fecha_lectura);
                    Period p = new Period(start, end, PeriodType.days());
                    postRead.dias_periodo = p.getDays();
                    postRead.consumo = postRead.lectura - primerLectura.lectura;
                    postRead.consumo_acumulado = postRead.consumo;
                    postRead.consumo_promedio = postRead.consumo_acumulado / postRead.dias_periodo;
                    postRead.periodo = periodo;
                } else {
                    Lectura ant = after.get(i - 1);
                    Lectura act = after.get(i);
                    end = new LocalDate(act.fecha_lectura);
                    Period p = new Period(start, end, PeriodType.days());
                    act.dias_periodo = p.getDays();
                    act.consumo = act.lectura - ant.lectura;
                    act.consumo_acumulado = ant.consumo_acumulado + act.consumo;
                    act.consumo_promedio = act.consumo_acumulado / act.dias_periodo;
                    act.periodo = periodo;

                }
            }
            realm.commitTransaction();
        }
    }
}
 */
