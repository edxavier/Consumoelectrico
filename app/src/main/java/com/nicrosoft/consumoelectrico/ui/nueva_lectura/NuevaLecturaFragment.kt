package com.nicrosoft.consumoelectrico.ui.nueva_lectura

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.activities.reading.contracts.ReadingPresenter
import com.nicrosoft.consumoelectrico.activities.reading.contracts.ReadingView
import com.nicrosoft.consumoelectrico.activities.reading.impl.ReadingPresenterImpl
import com.nicrosoft.consumoelectrico.utils.formatDate
import com.nicrosoft.consumoelectrico.utils.toTwoDecimalPlace
import com.nicrosoft.consumoelectrico.realm.Lectura
import com.nicrosoft.consumoelectrico.realm.Periodo
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.fragment_nueva_lectura2.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class NuevaLecturaFragment : Fragment(), ReadingView {
    private lateinit var endPeriodSw: SwitchMaterial
    private lateinit var navController: NavController
    private lateinit var params: NuevaLecturaFragmentArgs
    lateinit var presenter: ReadingPresenter
    var validReading = false
    var periodoActivo: Periodo? = null
    var fecha_lectura: Calendar = Calendar.getInstance()
    val maxDate = Calendar.getInstance()
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_nueva_lectura2, container, false)
        endPeriodSw = root.findViewById(R.id.end_period_sw)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        params = NuevaLecturaFragmentArgs.fromBundle(arguments!!)
        if(!Prefs.getBoolean("isPurchased", false))
            requestInterstialAds()
        navController = Navigation.findNavController(activity!!, R.id.nav_host_fragment)
        requireActivity().onBackPressedDispatcher.addCallback(this) { navController.navigateUp() }

        txt_medidor_name.text = params.name
        presenter = ReadingPresenterImpl(this)
        periodoActivo = presenter.getActivePeriod(params.id)
        var lastReading: Lectura? = null
        periodoActivo.let {
            lastReading = presenter.getLastReading(periodoActivo, true)
        }
        if (lastReading == null) {
            endPeriodSw!!.isEnabled = false
            MaterialDialog(context!!).show {
                title(R.string.notice)
                message(R.string.first_reading_notice)
                positiveButton(R.string.ok)
            }
        } else {
            txt_last_reading.text  = getString(R.string.last_reading_suggest,
                    lastReading?.fecha_lectura?.formatDate(context!!), lastReading?.lectura?.toTwoDecimalPlace())
        }
        //txt_fecha.setText(fecha_lectura.time.formatDate(context!!))
        txt_fecha.setOnClickListener {
            MaterialDialog(context!!).show {
                datePicker (maxDate = maxDate){ _, date ->
                    fecha_lectura.timeInMillis = date.timeInMillis
                    updateDateLabel()
                    //verificar si existe un registro para esa fecha, si no, verificar que sea valido
                    if (presenter.readingForDateExist(fecha_lectura.time, periodoActivo)) {
                       dateError()
                    }else
                        checkIfValidReading()
                }
            }
        }

        fab.setOnClickListener {
            checkIfValidReading()
            if(!validReading)
                return@setOnClickListener

            if (presenter.readingForDateExist(fecha_lectura.time, periodoActivo)) {
                Snackbar.make(coordinator!!, getString(R.string.activity_new_reading_snack_warning), LENGTH_LONG).show()
                return@setOnClickListener
            }

            val lectura = Lectura()
            lectura.fecha_lectura = fecha_lectura.time
            lectura.lectura = txt_lectura!!.text.toString().trim { it <= ' ' }.toFloat()
            lectura.observacion = txtObservacion!!.text.toString()
            if (endPeriodSw!!.isChecked) {
                MaterialDialog(context!!).show {
                    title(R.string.activity_new_reading_dialog_title)
                    message(R.string.activity_new_reading_dialog_content)
                    positiveButton(R.string.end_period_psoitive) {
                        if (presenter.saveReading(lectura, endPeriodSw.isChecked, params.id)) {
                            navController.navigateUp()
                        }else
                            Snackbar.make(coordinator,
                                    resources.getString(R.string.activity_new_reading_snack_warning),
                                    LENGTH_LONG).show();
                    }
                    negativeButton(R.string.cancel){ endPeriodSw.isChecked = false }
                }

            } else {
                if (presenter.saveReading(lectura, endPeriodSw.isChecked, params.id)) {
                    navController.navigateUp()
                } else Snackbar.make(coordinator!!, resources.getString(R.string.activity_new_reading_snack_warning), BaseTransientBottomBar.LENGTH_LONG).show()
            }


        }
    }

    fun dateError(){
        txt_ilayout_fecha.isErrorEnabled = true
        txt_ilayout_fecha.error = resources.getString(R.string.activity_new_reading_snack_warning)
        validReading = false
    }
    private fun updateDateLabel(){
        txt_fecha.setText(fecha_lectura.time.formatDate(context!!))
    }
    private fun checkIfValidReading() {
        txt_ilayout_fecha.isErrorEnabled = false
        txt_ilayout_lectura.isErrorEnabled = false
        txt_ilayout_lectura.error = ""
        txt_ilayout_fecha.error = ""

        if (txt_fecha.text.isNullOrEmpty()) {
            txt_ilayout_fecha.isErrorEnabled = true
            txt_ilayout_fecha.error = resources.getString(R.string.activity_new_reading_input_error)
            validReading = false
        } else {
            validReading = true
            txt_ilayout_fecha.isErrorEnabled = false
            txt_ilayout_fecha.error = ""
            //val lectura = presenter!!.getLastReading();
        }
        if (txt_lectura.text.isNullOrEmpty()) {
            txt_ilayout_lectura.isErrorEnabled = true
            txt_ilayout_lectura.error = resources.getString(R.string.activity_new_reading_input_error)
            validReading = false
        } else {
            validReading = true
            txt_ilayout_lectura.isErrorEnabled = false
            txt_ilayout_lectura.error = ""
            //val lectura = presenter!!.getLastReading();
        }
        if (txt_lectura!!.text.toString().isNotEmpty()) {
            val overRange = presenter
                    .isReadingOverRange(java.lang.Float.valueOf(txt_lectura!!.text.toString()),
                            fecha_lectura.time, periodoActivo)
            if (overRange) {
                txt_ilayout_lectura!!.isErrorEnabled = true
                txt_ilayout_lectura!!.error = resources.getString(R.string.activity_new_reading_input_error2)
                validReading = false
            } else {
                validReading = true
                txt_ilayout_lectura!!.isErrorEnabled = false
                txt_ilayout_lectura!!.error = ""
            }
        }
    }

    override fun showInfo(msg: String?) {
    }

    override fun showWarning(msg: String?) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val ne = Prefs.getInt("num_show_readings", 0)
        Prefs.putInt("num_show_readings", ne + 1)
        if (Prefs.getInt("num_show_readings", 0) == Prefs.getInt("show_after", 5)) {
            Prefs.putInt("num_show_readings", 0)
            val r = Random()
            val Low = 7
            val High = 12
            val rnd = r.nextInt(High - Low) + Low
            Prefs.putInt("show_after", rnd)
            mInterstitialAd?.let {
                if(it.isLoaded)
                    it.show()
            }
        }

    }

    private fun requestInterstialAds() {
        val adRequest = AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR) //.addTestDevice("0B307F34E3DDAF6C6CAB28FAD4084125")
                //.addTestDevice("B0FF48A19BF36BD2D5DCD62163C64F45")
                .addTestDevice("B162B59FFBB489F8FC90FE755FFA25F0")
                .build()
        mInterstitialAd = InterstitialAd(activity)
        mInterstitialAd?.adUnitId = resources.getString(R.string.admob_interstical)
        mInterstitialAd?.loadAd(adRequest)
        mInterstitialAd?.adListener = object : AdListener() {
            override fun onAdClosed() {
                super.onAdClosed()
                requestInterstialAds()
            }
        }
    }

}
