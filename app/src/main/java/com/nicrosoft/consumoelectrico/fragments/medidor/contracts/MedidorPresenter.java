package com.nicrosoft.consumoelectrico.fragments.medidor.contracts;

import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Medidor;
import com.nicrosoft.consumoelectrico.realm.Periodo;

import java.util.Date;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 11/01/2017.
 */

public interface MedidorPresenter {
    void getMedidores();
    Periodo getActivePeriod(Medidor medidor);
    Lectura getLastReading(Periodo periodo, boolean old_readings_if_more_than_a_period);
    Lectura getFirstReading(Periodo periodo);

}
