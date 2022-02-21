
/**
 * Created by Eder Xavier Rojas on 10/01/2017.
package com.nicrosoft.consumoelectrico.realm;


import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

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

    public Lectura(String id, Medidor medidor, Periodo periodo, float lectura, float consumo, float consumo_acumulado, float consumo_promedio, float dias_periodo, Date fecha_lectura, String observacion) {
        this.id = id;
        this.medidor = medidor;
        this.periodo = periodo;
        this.lectura = lectura;
        this.consumo = consumo;
        this.consumo_acumulado = consumo_acumulado;
        this.consumo_promedio = consumo_promedio;
        this.dias_periodo = dias_periodo;
        this.fecha_lectura = fecha_lectura;
        this.observacion = observacion;
    }
    public Lectura() {
        this.id = UUIDGenerator.nextUUID();
    }

}
 */
