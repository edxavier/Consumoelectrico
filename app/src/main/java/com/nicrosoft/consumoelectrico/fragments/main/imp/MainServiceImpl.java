package com.nicrosoft.consumoelectrico.fragments.main.imp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nicrosoft.consumoelectrico.fragments.main.contracts.MainService;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Periodo;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Eder Xavier Rojas on 11/01/2017.
 */

public class MainServiceImpl implements MainService {

    Context context;
    private Realm realm;


    public MainServiceImpl(Context context) {
        this.context = context;
        this.realm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public Lectura getResumeData() {
        try {
           /* RealmResults<Lectura> res = realm.where(Lectura.class).findAllSorted("fecha_lectura");
            for (Lectura re : res) {
                Log.e("EDER--", String.valueOf(re.fecha_lectura));
            }*/
            return realm.where(Lectura.class).findAllSorted("fecha_lectura").last();
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public Periodo getActivePeriod() {
        return realm.where(Periodo.class)
                .equalTo("activo", true)
                .findFirst();
    }

    @Override
    public RealmResults<Lectura> getHistoryReadings() {
        RealmResults<Periodo> periodos = realm.where(Periodo.class).findAll();
        RealmResults<Lectura> lecturas = realm.where(Lectura.class)
                                            .equalTo("periodo.activo", true)
                                            .findAllSorted("fecha_lectura");

        //Si el ultimo periodo activp no tiene registros mostrar los del anterior
        if(periodos.size()>1 && lecturas.size()==0){
            Periodo p = periodos.get(periodos.size()-2);
            lecturas = realm.where(Lectura.class)
                    .equalTo("periodo.inicio", p.inicio)
                    .findAllSorted("fecha_lectura");
        }

        return  lecturas;
    }

    @Override
    public boolean isRecordsEmpty() {
        RealmResults<Periodo> periodos = realm.where(Periodo.class).findAll();
        RealmResults<Lectura> lecturas = realm.where(Lectura.class)
                .equalTo("periodo.activo", true)
                .findAllSorted("fecha_lectura");
        //Si el ultimo periodo activp no tiene registros mostrar los del anterior
        return periodos.size() > 1 && lecturas.size() == 0;

    }

    @Override
    public boolean thereIsRecordForDate(Date date) {
        return readingForDateExist(date);
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
    @Override
    public boolean endPeriod(Date date) {
        try {
            realm.beginTransaction();
            Periodo pa = getActivePeriod();
            pa.fin = date;
            pa.activo = false;
            Periodo nuevoPeriodo = new Periodo(date, true);
            nuevoPeriodo.medidor = pa.medidor;
            realm.copyToRealm(nuevoPeriodo);
            realm.commitTransaction();
            pa = getActivePeriod();
            updateReadings(pa, date);
            return true;
        }catch (Exception ignored){
            return false;
        }
    }



    private void updateReadings(@NonNull Periodo periodo, Date fecha_lectura){
        Calendar calendar = Calendar.getInstance();
        Lectura lectura = getReadingForDate(fecha_lectura);
        calendar.setTime(periodo.inicio);
        LocalDate start = new LocalDate(periodo.inicio);
        RealmResults<Lectura> after = realm.where(Lectura.class)
                .greaterThan("fecha_lectura", periodo.inicio).findAllSorted("fecha_lectura");
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
