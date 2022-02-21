/**
 package com.nicrosoft.consumoelectrico.fragments.medidor.imp;

import android.content.Context;

import com.nicrosoft.consumoelectrico.fragments.main.imp.MainServiceImpl;
import com.nicrosoft.consumoelectrico.fragments.medidor.contracts.MedidorPresenter;
import com.nicrosoft.consumoelectrico.fragments.medidor.contracts.MedidorService;
import com.nicrosoft.consumoelectrico.fragments.medidor.contracts.MedidorView;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Medidor;
import com.nicrosoft.consumoelectrico.realm.Periodo;

import java.util.Date;

import io.realm.RealmResults;

 * Created by Eder Xavier Rojas on 11/01/2017.

public class MedidorPresenterImpl implements MedidorPresenter {

    Context context;
    MedidorView view;
    MedidorService service;

    public MedidorPresenterImpl(Context context, MedidorView view) {
        this.context = context;
        this.view = view;
        this.service = new MedidorServiceImpl(this.context);
    }

    @Override
    public void getMedidores() {
        RealmResults<Medidor> medidores = service.getMedidores();
        view.setMedidores(medidores);
        if(medidores!=null && medidores.size()>0) {
            view.hideEmptyDataMsg();
        }else {
            view.showEmptyDataMsg();
        }
    }

    @Override
    public Periodo getActivePeriod(Medidor medidor) {
        return service.getActivePeriod(medidor);
    }

    @Override
    public Lectura getLastReading(Periodo periodo,  boolean old_readings_if_more_than_a_period) {
        return service.getLastReading(periodo, old_readings_if_more_than_a_period);
    }

    @Override
    public Lectura getFirstReading(Periodo periodo) {
        return service.getFirstReading(periodo);
    }


}
 */
