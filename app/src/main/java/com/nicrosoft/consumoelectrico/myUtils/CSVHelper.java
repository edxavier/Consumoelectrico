package com.nicrosoft.consumoelectrico.myUtils;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nicrosoft.consumoelectrico.R;
import com.nicrosoft.consumoelectrico.activities.Main;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.nicrosoft.consumoelectrico.realm.Medidor;
import com.nicrosoft.consumoelectrico.realm.Periodo;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Eder Xavier Rojas on 14/12/2017.
 */

public class CSVHelper {
    public static boolean saveAllToCSV(String fullPath, String name, Context context)  {
        CSVWriter writer = null;
        try {
            Realm realm = Realm.getDefaultInstance();
            RealmResults<Lectura> res = realm.where(Lectura.class)
                    .findAll().sort("fecha_lectura", Sort.DESCENDING);
            if(res.size()>0) {
                List<String[]> data = new ArrayList<String[]>();
                String[] country = {"Medidor_id", "Medidor", "Descripcion",
                        "ID_periodo", "Inicio_periodo", "Fin_periodo", "Dias_periodo", "Activo",
                        "ID_lectura", "Lectura", "Fecha_lectura", "Consumo", "Consumo_acumulado", "Consumo_promedio", "Dias_periodo",
                        "Observacion"
                };
                data.add(country);
                for (Lectura re : res) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.date_time_format), Locale.getDefault());
                    String fin_p = "";
                    if (re.periodo.fin != null)
                        fin_p = dateFormat.format(re.periodo.fin);
                    data.add(new String[]{re.medidor.id, re.medidor.name, re.medidor.descripcion,
                            re.periodo.id, dateFormat.format(re.periodo.inicio), fin_p, String.valueOf(re.periodo.dias_periodo), String.valueOf(re.periodo.activo),
                            re.id, String.valueOf(re.lectura), dateFormat.format(re.fecha_lectura), String.valueOf(re.consumo), String.valueOf(re.consumo_acumulado),
                            String.valueOf(re.consumo_promedio), String.valueOf(re.dias_periodo), re.observacion
                    });
                }
                writer = new CSVWriter(new FileWriter(fullPath + "/" + name + ".csv"));
                writer.writeAll(data);
                writer.close();
                realm.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("EDER", e.getMessage());
            return false;
        }
    }

    public static boolean restoreAllFromCSV(String fullPath, Context context)  {
        Realm realm = Realm.getDefaultInstance();

        try {
            InputStream inputStream = new FileInputStream(fullPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String csvLine;
            reader.readLine();
             while ((csvLine = reader.readLine()) != null) {
                csvLine = csvLine.replace('"', ' ');
                //Log.e("EDER", csvLine);
                String[] row = csvLine.split(",");
                realm.executeTransaction(realm1 -> {
                    String observ = "";
                    if(row.length>15)
                        observ = row[15].trim();
                    Medidor medidor = RestoreHelper.guardarMedidor(row[0].trim(),row[1].trim(), row[2].trim());
                    Periodo periodo = RestoreHelper.guardarPeriodo(row[3].trim(),row[4].trim(), row[5].trim(),row[6].trim(),row[7].trim(), medidor, context);
                    RestoreHelper.guardarLectura(row[8].trim(),row[9].trim(), row[10].trim(),row[11].trim(),
                            row[12].trim(),row[13].trim(),row[14].trim(),observ, periodo, medidor, context);
                });
            }
            inputStream.close();
            realm.close();

            return true;
        }
        catch (Exception ex) {
            Log.e("EDER", ex.getMessage());
            if(!realm.isClosed())
                realm.close();
            return false;
        }
    }


    public static boolean saveActivePeriodReadings(String fullPath, String name, Context context, String medidor_id, boolean all)  {
        CSVWriter writer = null;
        try {
            Realm realm = Realm.getDefaultInstance();
            RealmResults<Lectura> res;
            if(all) {
                res = realm.where(Lectura.class)
                        .equalTo("medidor.id", medidor_id)
                        .findAll().sort("fecha_lectura", Sort.DESCENDING);
            }else {
                res = realm.where(Lectura.class)
                        .equalTo("medidor.id", medidor_id)
                        .equalTo("periodo.activo", true)
                        .findAll().sort("fecha_lectura", Sort.DESCENDING);
            }
            List<String[]> data = new ArrayList<String[]>();
            String [] titulos ={ "Fecha_lectura", "Lectura",  "Consumo", "Consumo_acumulado", "Consumo_promedio", "Dias_periodo",
                    "Observacion"};
            String [] titulo1 = { "Medidor", "Descripcion"};
            data.add(titulo1);
            try {
                data.add(new String[] { res.first().medidor.name, res.first().medidor.descripcion});
            }catch (Exception ignored){}
            data.add(titulos);
            for (Lectura re : res) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.date_time_format), Locale.getDefault());
                data.add(new String[] {
                        dateFormat.format(re.fecha_lectura), String.valueOf(re.lectura),
                        String.valueOf(re.consumo), String.valueOf(re.consumo_acumulado),
                        String.valueOf(re.consumo_promedio), String.valueOf(re.dias_periodo), re.observacion
                });
            }
            writer = new CSVWriter(new FileWriter(fullPath+"/"+name+".csv"));
            writer.writeAll(data);
            writer.close();
            realm.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("EDER", e.getMessage());
            return false;
        }
    }


}
