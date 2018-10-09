package com.nicrosoft.consumoelectrico.activities.reading.impl;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.nicrosoft.consumoelectrico.activities.reading.contracts.ReadingService;
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
 * Created by Eder Xavier Rojas on 16/01/2017.
 */

public class ReadingServiceImpl implements ReadingService {

    private Realm realm;

    ReadingServiceImpl() {
        this.realm = Realm.getDefaultInstance();
    }


    @Override
    public boolean isReadingOverRange(float reading, Date date, Periodo periodo) {
        try {
            String periodo_id = "";
            if(periodo!=null)
                periodo_id = periodo.id;
            //obtener todas las lecturas para el periodo actual
            RealmResults<Lectura> results = realm.where(Lectura.class)
                    .equalTo("periodo.id", periodo_id)
                    .findAll().sort("fecha_lectura");

            if(results.size()==0 && periodo != null) {
                RealmResults<Periodo> periodos = realm.where(Periodo.class)
                        .equalTo("medidor.id", periodo.medidor.id)
                        .equalTo("activo", false)
                        .findAll();
                Periodo p = periodos.get(periodos.size()-1);
                results = realm.where(Lectura.class)
                        .equalTo("periodo.id", p.id)
                        .findAll().sort("fecha_lectura");
            }

            //Obtener las lecturas anterior a la fecha especificada
            RealmResults<Lectura> before = results.where()
                    .lessThan("fecha_lectura", date).findAll().sort("fecha_lectura", Sort.DESCENDING);
            //Obtener las lecturas posterior a la fecha especificada
            RealmResults<Lectura> after = results.where()
                    .greaterThan("fecha_lectura", date).findAll().sort("fecha_lectura");
            boolean beforeOverRange = false;
            boolean afterOverRange = false;
            //berificar que la lectura sea mayor que la ultima anterior y menor quenla primera posterior
            if(before.size()>0)
                beforeOverRange = before.first().lectura > reading;
            if(after.size()>0)
                afterOverRange = after.first().lectura < reading;
            return beforeOverRange || afterOverRange;
        }catch (Exception e){
            Log.e("EDER", e.getMessage());
            return true;
        }
    }

    @NonNull
    @Override
    public Boolean readingForDateExist(Date date, Periodo periodo) {
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
        String periodo_id = "";
        if(periodo!=null)
            periodo_id = periodo.id;

        RealmResults<Lectura> lecturas = realm.where(Lectura.class)
                .equalTo("periodo.id", periodo_id)
                .between("fecha_lectura", from.getTime(), to.getTime())
                .findAll();

        return lecturas.size() > 0;
    }

    @Override
    public boolean saveReading(@NonNull Lectura lectura, boolean finishPeriod, String medidor_id) {
        Periodo activePeriod = getActivePeriod(medidor_id);

        Lectura anterior = getLastReading(activePeriod, false);
        //si no exsite una ultima lectura, es porque es el primer registro para este medidor
        if(anterior==null){
            try {
                realm.executeTransaction(realm1 -> {
                    lectura.consumo = 0;
                    lectura.consumo_acumulado = 0;
                    lectura.consumo_promedio = 0;
                    lectura.dias_periodo = 0;

                    Medidor medidor = realm.where(Medidor.class).equalTo("id", medidor_id).findFirst();
                    if(activePeriod==null) {
                        Periodo nuevoPeriodo = new Periodo(lectura.fecha_lectura, true);
                        nuevoPeriodo.medidor = medidor;
                        nuevoPeriodo = realm.copyToRealm(nuevoPeriodo);
                        lectura.periodo = nuevoPeriodo;
                    }else
                        lectura.periodo = activePeriod;

                    lectura.medidor = medidor;
                    realm.copyToRealm(lectura);
                });
                return true;
            }catch (Exception e){
                Log.e("EDER_save1", e.getMessage());
                return false;
            }
        }else {
            try {
                RealmResults<Lectura> results = realm.where(Lectura.class)
                        .equalTo("periodo.id", activePeriod.id)
                        .findAll().sort("fecha_lectura");
                RealmResults<Lectura> readingsBefore = results.where()
                        .lessThan("fecha_lectura", lectura.fecha_lectura).findAll().sort("fecha_lectura", Sort.DESCENDING);
                RealmResults<Lectura> readingsAfter = results.where()
                        .greaterThan("fecha_lectura", lectura.fecha_lectura).findAll().sort("fecha_lectura");


                realm.executeTransaction(realm1 -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(activePeriod.inicio);

                    LocalDate start = new LocalDate(activePeriod.inicio);
                    LocalDate end = new LocalDate(lectura.fecha_lectura);
                    //inicializar  variable p, para calcular los dias desde que inicio el periodo hasta la fecha de la lectura actual
                    Period p = new Period(start, end, PeriodType.days());

                    //Berificar si hay registro anterior y calcular, actualizar el registro siguiente si existe
                    // en teoria siempre existira un anterior que es el inicio de periodo, antes que eso ya no se puede agregar nada.
                    if(readingsBefore.size()>0) {
                        //Calcular el consumo desde la lectura anterior
                        lectura.consumo = lectura.lectura - readingsBefore.first().lectura;
                        //si la ultima lectura pertenece a un periodo aun activo, calcular el consumo acumulado desde el inicio del periodo
                        if(readingsBefore.first().periodo.activo) {
                            lectura.consumo_acumulado = readingsBefore.first().consumo_acumulado + lectura.consumo;
                        }else {
                            //Si no, la ultima lectura es de un periodo finalizado, por tanto el consumo acumulado es igual al consumo desde la ultima lectura
                            lectura.consumo_acumulado = lectura.consumo;
                        }
                        lectura.dias_periodo = p.getDays();
                        activePeriod.dias_periodo = lectura.dias_periodo;
                        lectura.consumo_promedio = lectura.consumo_acumulado / lectura.dias_periodo;
                        lectura.medidor  = realm.where(Medidor.class).equalTo("id", medidor_id).findFirst();

                        Periodo nuevoPeriodo = null;
                        //si marcaron terminar el periodo
                        if(finishPeriod) {
                            activePeriod.fin = lectura.fecha_lectura;
                            activePeriod.dias_periodo = lectura.dias_periodo;
                            activePeriod.activo = false;
                            nuevoPeriodo = new Periodo(lectura.fecha_lectura, true);
                            nuevoPeriodo.medidor =  realm.where(Medidor.class).equalTo("id", medidor_id).findFirst();
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
                        }
                        lectura.periodo = activePeriod;
                    }
                    realm.copyToRealm(lectura);

                    // ---------------------------------------------------------------
                    //Si existe registro posterior recalcular los valores para esos registros
                    if(readingsAfter.size()>0){
                        //Si se finalizo el periodo actualizar todos los registros despues de lafecha especificada en la lectura ya que pertenecen a el nuevo periodo
                        if(finishPeriod) {
                            Periodo nuevoPeriodo = getActivePeriod(medidor_id);
                            calendar.setTime(nuevoPeriodo.inicio);
                            start = new LocalDate(nuevoPeriodo.inicio);
                            //hay q recalcular los dias periodo
                            for (int i = 0; i < readingsAfter.size(); i++) {
                                if(i==0){
                                    Lectura postRead = readingsAfter.first();
                                    postRead.consumo = postRead.lectura - lectura.lectura;
                                    postRead.consumo_acumulado = postRead.consumo;
                                    postRead.periodo = nuevoPeriodo;
                                    end = new LocalDate(postRead.fecha_lectura);
                                    //calcular los dias transcurridos basados en el inicio del nuevo periodo
                                    p = new Period(start, end, PeriodType.days());
                                    postRead.dias_periodo = p.getDays();
                                    postRead.consumo_promedio = postRead.consumo_acumulado / postRead.dias_periodo;
                                }else {
                                    //por ejemplo recalcular la pocision 1 basado en la pos 0
                                    Lectura l_anterior = readingsAfter.get(i-1);
                                    Lectura l_actual = readingsAfter.get(i);
                                    l_actual.consumo = l_actual.lectura - l_anterior.lectura;
                                    l_actual.consumo_acumulado = l_anterior.consumo_acumulado + l_actual.consumo;
                                    l_actual.periodo = nuevoPeriodo;
                                    end = new LocalDate(l_actual.fecha_lectura);
                                    p = new Period(start, end, PeriodType.days());
                                    l_actual.dias_periodo = p.getDays();
                                    l_actual.consumo_promedio = l_actual.consumo_acumulado / l_actual.dias_periodo;
                                }
                            }
                        }else {
                            Lectura postRead = readingsAfter.first();
                            postRead.consumo = postRead.lectura - lectura.lectura;
                            postRead.consumo_acumulado = lectura.consumo_acumulado + postRead.consumo;
                            postRead.consumo_promedio = postRead.consumo_acumulado / postRead.dias_periodo;
                        }
                    }


                });
                return true;
            }catch (Exception e){
                Log.e("EDER_ERR", e.getMessage());
                return false;
            }
        }
    }

    @Override
    public Periodo getActivePeriod(String medidor_id) {
        return realm.where(Periodo.class)
                .equalTo("medidor.id", medidor_id)
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
            return (res.size()>0) ? res.first(): null;
        }else
            return null;
    }

}
