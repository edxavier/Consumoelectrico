/**
 * Created by Eder Xavier Rojas on 18/01/2017.

 package com.nicrosoft.consumoelectrico.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class Medidor extends RealmObject {
    @PrimaryKey
    public String id;
    public String name;
    public String descripcion;

    public Medidor(String name) {
        this.id = UUIDGenerator.nextUUID();
        this.name = name;
        this.descripcion = "";
    }

    public Medidor(String id, String name, String descripcion) {
        this.id = id;
        this.name = name;
        this.descripcion = descripcion;
    }
    public Medidor() {
    }
}
 */
