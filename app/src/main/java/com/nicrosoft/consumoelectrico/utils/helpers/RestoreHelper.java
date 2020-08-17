package com.nicrosoft.consumoelectrico.utils.helpers;

import android.content.Context;

import com.nicrosoft.consumoelectrico.R;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Medidor;
import com.nicrosoft.consumoelectrico.realm.Periodo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

/**
 * Created by Eder Xavier Rojas on 14/12/2017.
 */

public class RestoreHelper {

    private static Medidor getMedidor(String medidor_id){
        Realm realm = Realm.getDefaultInstance();
        Medidor exist = realm.where(Medidor.class)
                .equalTo("id", medidor_id)
                .findFirst();
        realm.close();
        return exist;
    }

    private static Periodo getPeriodo(String _id){
        Realm realm = Realm.getDefaultInstance();
        Periodo exist = realm.where(Periodo.class)
                .equalTo("id", _id)
                .findFirst();
        realm.close();
        return exist;
    }

    private static Lectura getLectura(String _id){
        Realm realm = Realm.getDefaultInstance();
        Lectura exist = realm.where(Lectura.class)
                .equalTo("id", _id)
                .findFirst();
        realm.close();
        return exist;
    }

    static Medidor guardarMedidor(String medidor_id, String medidor_name, String medidor_desc){
        Realm realm = Realm.getDefaultInstance();
        Medidor medidor = getMedidor(medidor_id);
        if(medidor==null){
            medidor = realm.copyToRealm(new Medidor(medidor_id, medidor_name, medidor_desc));
        }
        realm.close();
        return medidor;
    }

    static Periodo guardarPeriodo(String _id, String inicio, String fin, String dias_periodo, String activo, Medidor medidor, Context context){
        Realm realm = Realm.getDefaultInstance();
        Periodo periodo = getPeriodo(_id);
        if(periodo==null){
            SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.date_time_format), Locale.getDefault());
            Date dateInicio = null;
            Date dateFin= null;
            float dias = 0;
            try {
                dateInicio = dateFormat.parse(inicio);
                if(!fin.isEmpty()&&fin.length()>2)
                    dateFin = dateFormat.parse(fin);
                if(!dias_periodo.isEmpty())
                    dias = Float.parseFloat(dias_periodo);
                periodo = realm.copyToRealm(new Periodo(_id, dateInicio, dateFin, dias, Boolean.valueOf(activo), medidor));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        realm.close();
        return periodo;
    }

    static void guardarLectura(String _id, String _lectura, String fecha, String consumo, String consumoAcumulado, String consumoPromedio,
                                   String dias_periodo, String observacion, Periodo periodo,  Medidor medidor, Context context){
        Realm realm = Realm.getDefaultInstance();
        Lectura lectura = getLectura(_id);
        if(lectura==null){
            SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.date_time_format), Locale.getDefault());
            Date dateLectura = null;
            try {
                dateLectura = dateFormat.parse(fecha);
                realm.copyToRealm(new Lectura(_id, medidor, periodo, Float.parseFloat(_lectura), Float.parseFloat(consumo),
                        Float.parseFloat(consumoAcumulado), Float.parseFloat(consumoPromedio), Float.parseFloat(dias_periodo),
                        dateLectura, observacion));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        realm.close();
    }

    public static String getInternalStoragePath(Context context){
        try {
            String path = context.getExternalFilesDir(null).getAbsolutePath();
            String[] res = Arrays.copyOfRange(path.split("/"), 0, path.split("/").length -4);
            StringBuilder pathBuilder = new StringBuilder();
            for (String re : res) {
                pathBuilder.append("/").append(re);
            }
            path = pathBuilder.toString().substring(1);
            return path;
        }catch (Exception e){
            return "/storage/emulated/0";
        }

    }

}
