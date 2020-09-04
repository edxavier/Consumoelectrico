package com.nicrosoft.consumoelectrico.utils.helpers

import android.content.Context
import android.util.Log
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.realm.Lectura
import com.nicrosoft.consumoelectrico.utils.AppResult
import com.opencsv.CSVWriter
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Eder Xavier Rojas on 14/12/2017.
 */
object CSVHelper {
    fun saveAllToCSV(fullPath: String, name: String, context: Context): AppResult {
        var writer: CSVWriter? = null
        try {
            val realm = Realm.getDefaultInstance()
            val res = realm.where(Lectura::class.java)
                    .findAll().sort("fecha_lectura", Sort.DESCENDING)
            if (res.size > 0) {
                val data: MutableList<Array<String>> = ArrayList()
                val country = arrayOf("Medidor_id", "Medidor", "Descripcion",
                        "ID_periodo", "Inicio_periodo", "Fin_periodo", "Dias_periodo", "Activo",
                        "ID_lectura", "Lectura", "Fecha_lectura", "Consumo", "Consumo_acumulado", "Consumo_promedio", "Dias_periodo",
                        "Observacion"
                )
                data.add(country)
                for (re in res) {
                    val dateFormat = SimpleDateFormat(context.getString(R.string.date_time_format), Locale.getDefault())
                    var fin_p = ""
                    if(re.periodo!=null) {
                        if (re.periodo.fin != null) fin_p = dateFormat.format(re.periodo.fin)
                        data.add(arrayOf(re.medidor.id, re.medidor.name, re.medidor.descripcion,
                                re.periodo.id, dateFormat.format(re.periodo.inicio), fin_p, re.periodo.dias_periodo.toString(), re.periodo.activo.toString(),
                                re.id, re.lectura.toString(), dateFormat.format(re.fecha_lectura), re.consumo.toString(), re.consumo_acumulado.toString(), re.consumo_promedio.toString(), re.dias_periodo.toString(), re.observacion
                        ))
                    }
                }
                writer = CSVWriter(FileWriter("$fullPath/$name.csv"))
                writer.writeAll(data)
                writer.close()
                realm.close()
            }
            return AppResult.OK
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("EDER", e.message!!)
            return AppResult.AppException(e)
        }
    }

    fun restoreAllFromCSV(fullPath: String?, context: Context?): Boolean {
        val realm = Realm.getDefaultInstance()
        return try {
            val inputStream: InputStream = FileInputStream(fullPath)
            Log.e("EDER", fullPath!!)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var csvLine: String
            reader.readLine()
            while (reader.readLine().also { csvLine = it } != null) {
                csvLine = csvLine.replace('"', ' ')
                //Log.e("EDER", csvLine);
                val row = csvLine.split(",".toRegex()).toTypedArray()
                realm.executeTransaction { realm1: Realm? ->
                    var observ = ""
                    if (row.size > 15) observ = row[15].trim { it <= ' ' }
                    val medidor = RestoreHelper.guardarMedidor(row[0].trim { it <= ' ' }, row[1].trim { it <= ' ' }, row[2].trim { it <= ' ' })
                    val periodo = RestoreHelper.guardarPeriodo(row[3].trim { it <= ' ' }, row[4].trim { it <= ' ' }, row[5].trim { it <= ' ' }, row[6].trim { it <= ' ' }, row[7].trim { it <= ' ' }, medidor, context)
                    RestoreHelper.guardarLectura(row[8].trim { it <= ' ' }, row[9].trim { it <= ' ' }, row[10].trim { it <= ' ' }, row[11].trim { it <= ' ' },
                            row[12].trim { it <= ' ' }, row[13].trim { it <= ' ' }, row[14].trim { it <= ' ' }, observ, periodo, medidor, context)
                }
            }
            inputStream.close()
            realm.close()
            true
        } catch (ex: Exception) {
            Log.e("EDER", ex.toString())
            ex.printStackTrace()
            if (!realm.isClosed) realm.close()
            false
        }
    }

    fun saveActivePeriodReadings(fullPath: String, name: String, context: Context, medidor_id: String?, all: Boolean): Boolean {
        var writer: CSVWriter? = null
        return try {
            val realm = Realm.getDefaultInstance()
            val res: RealmResults<Lectura>
            res = if (all) {
                realm.where(Lectura::class.java)
                        .equalTo("medidor.id", medidor_id)
                        .findAll().sort("fecha_lectura", Sort.DESCENDING)
            } else {
                realm.where(Lectura::class.java)
                        .equalTo("medidor.id", medidor_id)
                        .equalTo("periodo.activo", true)
                        .findAll().sort("fecha_lectura", Sort.DESCENDING)
            }
            val data: MutableList<Array<String>> = ArrayList()
            val titulos = arrayOf("Fecha_lectura", "Lectura", "Consumo", "Consumo_acumulado", "Consumo_promedio", "Dias_periodo",
                    "Observacion")
            val titulo1 = arrayOf("Medidor", "Descripcion")
            data.add(titulo1)
            try {
                data.add(arrayOf(res.first()!!.medidor.name, res.first()!!.medidor.descripcion))
            } catch (ignored: Exception) {
            }
            data.add(titulos)
            for (re in res) {
                val dateFormat = SimpleDateFormat(context.getString(R.string.date_time_format), Locale.getDefault())
                data.add(arrayOf(
                        dateFormat.format(re.fecha_lectura), re.lectura.toString(), re.consumo.toString(), re.consumo_acumulado.toString(), re.consumo_promedio.toString(), re.dias_periodo.toString(), re.observacion
                ))
            }
            writer = CSVWriter(FileWriter("$fullPath/$name.csv"))
            writer.writeAll(data)
            writer.close()
            realm.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("EDER", e.message!!)
            false
        }
    }
}