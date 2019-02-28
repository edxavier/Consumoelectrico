package com.nicrosoft.consumoelectrico.fragments.medidor.contracts;


import androidx.annotation.Nullable;

import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Medidor;
import com.nicrosoft.consumoelectrico.realm.Periodo;

import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 11/01/2017.
 */

public interface MedidorService {
    RealmResults<Medidor> getMedidores();
    Lectura getFirstReading(Periodo periodo);

    Periodo getActivePeriod(Medidor medidor);
    Lectura getLastReading(Periodo periodo, boolean old_readings_if_more_than_a_period);


}
