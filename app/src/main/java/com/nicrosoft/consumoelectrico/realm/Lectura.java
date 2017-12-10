package com.nicrosoft.consumoelectrico.realm;


import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Eder Xavier Rojas on 10/01/2017.
 */
public class Lectura extends RealmObject {
    @PrimaryKey
    public String id;


    public Medidor medidor;
    public Periodo periodo;

    public float lectura;
    public float consumo;
    public float consumo_acumulado;
    public float consumo_promedio;
    public float dias_periodo;
    public Date fecha_lectura;
    public String observacion;

    public Lectura() {
        this.id = UUIDGenerator.nextUUID();
    }
}
