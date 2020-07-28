package com.nicrosoft.consumoelectrico.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.nicrosoft.consumoelectrico.MainKt
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.fragments.medidor.contracts.MedidorPresenter
import com.nicrosoft.consumoelectrico.fragments.medidor.contracts.MedidorView
import com.nicrosoft.consumoelectrico.fragments.medidor.imp.MedidorPresenterImpl
import com.nicrosoft.consumoelectrico.utils.hideFabButtonOnScroll
import com.nicrosoft.consumoelectrico.utils.setHidden
import com.nicrosoft.consumoelectrico.utils.setVisible
import com.nicrosoft.consumoelectrico.realm.Medidor
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import kotlinx.android.synthetic.main.dlg_medidor.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*

class MedidorFragment : Fragment(), MedidorView, RealmChangeListener<Realm> {

    private lateinit var realm: Realm
    private lateinit var homeViewModel: MedidorViewModel
    lateinit var presenter: MedidorPresenter
    lateinit var mainActivity: MainKt


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProviders.of(this).get(MedidorViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        //val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            //textView.text = it
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        realm = Realm.getDefaultInstance()
        realm.addChangeListener(this)
        mainActivity = (activity as MainKt)
        presenter = MedidorPresenterImpl(activity, this)
        presenter.getMedidores()
        setFabListener()
        
    }

    private fun setFabListener() {
        fab.setOnClickListener {
            MaterialDialog(context!!, BottomSheet()).show {
                noAutoDismiss()
                title(R.string.new_medidor)
                positiveButton (R.string.save){
                    // Pull the password out of the custom view when the positive button is pressed
                    val med = txt_medidor_name.text.toString().trim()
                    val desc = txt_medidor_desc.text.toString().trim()
                    realm.executeTransaction {
                        val medidor: Medidor
                        if (med.isNotEmpty()) {
                            medidor =  Medidor(med)
                            if (desc.isNotEmpty()) {
                                medidor.descripcion = desc
                            }
                            realm.copyToRealm(medidor)
                            dismiss()
                        } else
                            Toast.makeText(activity!!, activity!!.getString(R.string.invalid_medidor_name), Toast.LENGTH_LONG).show()
                    }

                }
                negativeButton (R.string.cancel){dismiss()}
                customView(R.layout.dlg_medidor, scrollable = true, horizontalPadding = true)
            }
        }
    }

    override fun showEmptyDataMsg() {
        message_body.setVisible()
    }

    override fun setMedidores(medidores: RealmResults<Medidor>?) {
        val groupAdapter: GroupAdapter<GroupieViewHolder> = GroupAdapter()
        val cntx = activity

        medidores_list.apply {
            layoutManager = LinearLayoutManager(cntx)
            adapter = groupAdapter
        }
        medidores_list.hideFabButtonOnScroll(fab)
        val list:MutableList<MedidorItem> = ArrayList()
        medidores?.forEach { medidor -> list.add(MedidorItem(medidor, cntx!!, activity!! as MainKt, presenter, realm)) }
        groupAdapter.clear()
        groupAdapter.addAll(list)

    }

    override fun hideEmptyDataMsg() {
        message_body.setHidden()
    }

    override fun startNewReadingActivity(intent: Intent?, index: Int) {
        startActivityForResult(intent, index)
    }

    override fun onChange(t: Realm) {
        try {
            presenter.getMedidores()
        }catch (e:Exception){
            Log.e("EDER", "medidores onChange ERROR")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        realm.removeAllChangeListeners()
        realm.close()
    }

}
