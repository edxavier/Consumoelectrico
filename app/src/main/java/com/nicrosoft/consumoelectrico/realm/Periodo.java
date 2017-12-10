package com.nicrosoft.consumoelectrico.realm;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Eder Xavier Rojas on 18/01/2017.
 */

public class Periodo extends RealmObject {

    @PrimaryKey
    public String id;
    public Medidor medidor;
    public Date inicio;
    public Date fin;
    public boolean activo;
    public float dias_periodo;

    public Periodo() {
        this.id = UUIDGenerator.nextUUID();
    }

    public Periodo(Date inicio, boolean activo) {
        this.inicio = inicio;
        this.activo = activo;
        this.id = UUIDGenerator.nextUUID();

    }
}
