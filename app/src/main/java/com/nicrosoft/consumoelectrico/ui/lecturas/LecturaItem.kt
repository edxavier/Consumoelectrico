/*package com.nicrosoft.consumoelectrico.ui.lecturas

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.view.View
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.databinding.ReadingTimeLineItemBinding
import com.nicrosoft.consumoelectrico.fragments.readings.contracts.LecturasPresenter
import com.nicrosoft.consumoelectrico.realm.Lectura
import com.nicrosoft.consumoelectrico.utils.formatDate
import com.nicrosoft.consumoelectrico.utils.toTwoDecimalPlace
import com.xwray.groupie.viewbinding.BindableItem
import io.realm.RealmResults
import java.util.*


class LecturaItem(
        val lectura:Lectura,
        private val ctxt: Context,
        val results: RealmResults<Lectura>?,
        val presenter:LecturasPresenter
): BindableItem<ReadingTimeLineItemBinding>(){

    @SuppressLint("SetTextI18n")
    override fun bind(viewHolder: ReadingTimeLineItemBinding, position: Int) {
        viewHolder.apply {
            if (position < results!!.size - 1) {
                val la: Lectura = results[position + 1]!!
                val dias: Float = lectura.dias_periodo - la.dias_periodo
                labelConsumption.text = ctxt.getString(R.string.label_consumption_since_last_reading, dias.toTwoDecimalPlace())
                when {
                    lectura.consumo_promedio > la.consumo_promedio -> {
                        txtTendencia.rotation = 270f
                        txtTendencia.setColorFilter(ctxt.resources.getColor(R.color.md_red_600))
                        txtPromedio.setTextColor(ctxt.resources.getColor(R.color.md_red_600))
                    }
                    lectura.consumo_promedio < la.consumo_promedio -> {
                        txtTendencia.rotation = 90f
                        txtTendencia.setColorFilter(ctxt.resources.getColor(R.color.md_green_600))
                        txtPromedio.setTextColor(ctxt.resources.getColor(R.color.md_green_600))
                    }
                    else -> {
                        txtTendencia.rotation = 0f
                        txtTendencia.setColorFilter(ctxt.resources.getColor(R.color.md_blue_grey_600))
                        txtPromedio.setTextColor(ctxt.resources.getColor(R.color.md_blue_grey_600))
                    }
                }
            } else {
                txtTendencia.rotation = 0f
                txtTendencia.setColorFilter(ctxt.resources.getColor(R.color.md_blue_grey_600))
                txtPromedio.setTextColor(ctxt.resources.getColor(R.color.md_blue_grey_600))
                labelConsumption.text = ctxt.getString(R.string.label_consumption_since_last_reading, String.format(Locale.getDefault(), "%02.0f", 0f))
            }

            if(lectura.observacion!=null && lectura.observacion.isNotEmpty()) {
                txtObservaciones.text = lectura.observacion
                txtObservaciones.visibility = View.VISIBLE
                observacionFlag.visibility = View.VISIBLE
            }else {
                observacionFlag.visibility = View.GONE
                txtObservaciones.visibility = View.GONE
                txtObservaciones.text = ""
            }
            txtFecha.text = lectura.fecha_lectura.formatDate(ctxt)
            val lect = String.format(Locale.getDefault(), "%.0f kWh", lectura.lectura)
            txtLectura.text = lect
            txtPromedio.text = String.format(Locale.getDefault(), "%.2f kWh", lectura.consumo_promedio)
            txtConsumption.text = String.format(Locale.getDefault(), "%.2f kWh", lectura.consumo)
            readingRowContainer.setOnClickListener {
                MaterialDialog(ctxt).show {
                    title(text = lectura.fecha_lectura.formatDate(context = ctxt))
                    listItems(R.array.readings_options){ _, index, _ ->
                        when (index){
                            0 -> { finalizar() }
                            1 -> { editar() }
                            2 -> { eliminar() }
                        }
                    }
                }
            }

        }
    }

    override fun getLayout(): Int = R.layout.reading_time_line_item

    private fun finalizar(){
        MaterialDialog(ctxt).show {
            title(text = lectura.fecha_lectura.formatDate(ctxt))
            message (R.string.end_period_ask)
            positiveButton(R.string.end_period_psoitive){
                presenter.endPeriod(lectura)
            }
            negativeButton(R.string.cancel)
        }
    }

    fun editar(){
        MaterialDialog(ctxt).show {
            title(text = lectura.fecha_lectura.formatDate(ctxt))
            message(R.string.lectura_editar_hint)
            //message(R.string.edit_reading)
            input (prefill = lectura.lectura.toString(), inputType = InputType.TYPE_CLASS_NUMBER)
            { _, text ->
                if(presenter.isValueOverange(lectura, text.toString()))
                    Toast.makeText(context, R.string.alert_over_range, Toast.LENGTH_LONG).show();
                else
                    presenter.updateReading(lectura, text.toString())
            }
            positiveButton(R.string.save)
            negativeButton(R.string.cancel)
        }
    }

    fun eliminar(){
        MaterialDialog(ctxt).show {
            title(text = lectura.fecha_lectura.formatDate(ctxt))
            message(R.string.delete_reading)
            negativeButton(R.string.cancel)
            positiveButton(R.string.ok){
                    if(!presenter.deleteEntry(lectura))
                        Toast.makeText(context, "No fue posible elimiar el registro", Toast.LENGTH_LONG).show()
                    else
                        Toast.makeText(context, "Registro Elminiado" , Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun initializeViewBinding(view: View): ReadingTimeLineItemBinding {
        return ReadingTimeLineItemBinding.bind(view)
    }
}

 */