package com.nicrosoft.consumoelectrico.ui.home
/*
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItems
import com.nicrosoft.consumoelectrico.MainKt
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.databinding.MedidorItemBinding
import com.nicrosoft.consumoelectrico.fragments.medidor.contracts.MedidorPresenter
import com.nicrosoft.consumoelectrico.realm.Lectura
import com.nicrosoft.consumoelectrico.realm.Medidor
import com.nicrosoft.consumoelectrico.realm.Periodo
import com.nicrosoft.consumoelectrico.utils.setHidden
import com.nicrosoft.consumoelectrico.utils.setVisible
import com.pixplicity.easyprefs.library.Prefs
import com.xwray.groupie.viewbinding.BindableItem
import io.realm.Realm
import kotlinx.android.synthetic.main.dlg_medidor.*
import java.text.DecimalFormat
import java.util.*


class MedidorItem(
        val medidor:Medidor,
        private val context: Context,
        private val activity: MainKt,
        private val presenter: MedidorPresenter,
        private val realm: Realm
): BindableItem<MedidorItemBinding>() {

    @SuppressLint("SetTextI18n")
    override fun bind(binding: MedidorItemBinding, position: Int) {

        binding.apply {
            var kw_limit = 150
            var period_limit = 30
            try {
                kw_limit = Prefs.getString("kw_limit", "150").toInt()
                period_limit = Prefs.getString("period_lenght", "30").toInt()
            } catch (ignored: Exception) {
            }
            circularProgress.maxProgress = kw_limit.toDouble()
            circularProgress2.maxProgress = period_limit.toDouble()

            if (medidor.name != null && medidor.name.isNotEmpty()) txtMedidor.text = medidor.name else txtMedidor.text = "-----"
            var periodo: Periodo? = null
            periodo = presenter.getActivePeriod(medidor)
            periodo?.let {
                val lectura: Lectura = presenter.getLastReading(periodo, true)
                lectura.let {
                    val avg_limit = kw_limit / period_limit.toFloat()
                    if (avg_limit < lectura.consumo_promedio) {
                        warningIcon.setVisible()
                        warningMsg.setVisible()
                    } else {
                        warningIcon.setHidden()
                        warningMsg.setHidden()
                    }
                    if (lectura.consumo_acumulado > kw_limit) {
                        val v = context.getString(R.string.initial_reading_val, String.format(Locale.getDefault(), "+%02.0f", lectura.consumo_acumulado - kw_limit))
                        excesoConsumo.setVisible()
                        excesoConsumo.text = v
                    } else {
                        excesoConsumo.setHidden()
                    }
                    if (lectura.dias_periodo > period_limit) {
                        val decimalFormat = DecimalFormat("#.##")
                        val twoDigitsF = java.lang.Float.valueOf(decimalFormat.format((lectura.dias_periodo - period_limit).toDouble()))
                        val v = context.getString(R.string.days_consumed_val, twoDigitsF)
                        excesoPeriodo.setVisible()
                        excesoPeriodo.text = "+$v"
                    } else {
                        excesoPeriodo.setHidden()
                    }
                    circularProgress.setCurrentProgress(lectura.consumo_acumulado.toDouble())
                    circularProgress2.setCurrentProgress(lectura.dias_periodo.toDouble())
                    txtPeriodAvg.text = context.getString(R.string.initial_reading_val, String.format(Locale.getDefault(), "%02.1f", lectura.consumo_promedio))
                    txtLastReading.text = context.getString(R.string.initial_reading_val, String.format(Locale.getDefault(), "%02.0f", lectura.lectura))
                }
            }

            val navController: NavController = Navigation.findNavController(activity, R.id.nav_host_fragment)

            bntDetails.setOnClickListener {
                val action = MedidorFragmentDirections.actionNavHomeToDetallesFragment(id = medidor.id, name = medidor.name, desc = medidor.descripcion)
                navController.navigate(action)
            }

            bntNewReadings.setOnClickListener {
                val action = MedidorFragmentDirections.actionNavHomeToNuevaLecturaFragment(id = medidor.id, name = medidor.name)
                navController.navigate(action)
            }
            bntReadings.setOnClickListener {
                val action = MedidorFragmentDirections.actionNavHomeToListaLecturasFragment(id = medidor.id, name = medidor.name, desc = medidor.descripcion)
                navController.navigate(action)
            }

            cardResume.setOnClickListener {
                MaterialDialog(context).show {
                    title(text = medidor.name)
                    listItems(R.array.medidor_options) { _, index, _ ->
                        when(index){
                            0->{
                                val dlg = MaterialDialog(context, BottomSheet()).show {
                                    noAutoDismiss()
                                    title(R.string.new_medidor)
                                    positiveButton (R.string.save){
                                        // Pull the password out of the custom view when the positive button is pressed
                                        val med = txt_medidor_name.text.toString().trim()
                                        val desc = txt_medidor_desc.text.toString().trim()
                                        realm.executeTransaction {
                                            if (med.isNotEmpty()) {
                                                medidor.name = med
                                                if (desc.isNotEmpty()) {
                                                    medidor.descripcion = desc
                                                }
                                                dismiss()
                                            } else
                                                Toast.makeText(activity, activity.getString(R.string.invalid_medidor_name), Toast.LENGTH_LONG).show()
                                        }

                                    }
                                    negativeButton (R.string.cancel){dismiss()}
                                    customView(R.layout.dlg_medidor, scrollable = true, horizontalPadding = true)
                                }
                                try {
                                    val name = dlg.getCustomView().findViewById(R.id.txt_medidor_name) as EditText
                                    val desc = dlg.getCustomView().findViewById(R.id.txt_medidor_desc) as EditText
                                    name.setText(medidor.name)
                                    desc.setText(medidor.descripcion)
                                } catch (e:Exception) { }
                            }
                            1->{
                                MaterialDialog(context).show {
                                    title(text = medidor.name)
                                    message(R.string.delete_medidor_notice)
                                    positiveButton(R.string.agree){
                                        realm.executeTransaction {
                                            val delLectures = realm.where(Lectura::class.java).equalTo("medidor.id", medidor.id).findAll()
                                            val periodos = realm.where(Periodo::class.java).equalTo("medidor.id", medidor.id).findAll()
                                            delLectures.deleteAllFromRealm()
                                            periodos.deleteAllFromRealm()
                                            medidor.deleteFromRealm()
                                        }
                                    }
                                    negativeButton(R.string.cancel)
                                }
                            }
                        }
                    }
                }
            }


        }
    }

    override fun getLayout(): Int = R.layout.medidor_item

    override fun initializeViewBinding(view: View): MedidorItemBinding {
        return MedidorItemBinding.bind(view)
    }

}

 */