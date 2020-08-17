package com.nicrosoft.consumoelectrico.ui.lecturas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.files.folderChooser
import com.afollestad.materialdialogs.input.input
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.fragments.readings.contracts.LecturasPresenter
import com.nicrosoft.consumoelectrico.fragments.readings.contracts.LecturasPresenterImpl
import com.nicrosoft.consumoelectrico.fragments.readings.contracts.LecturasView
import com.nicrosoft.consumoelectrico.utils.*
import com.nicrosoft.consumoelectrico.realm.Lectura
import com.nicrosoft.consumoelectrico.utils.helpers.CSVHelper
import com.pixplicity.easyprefs.library.Prefs
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_lista_lecturas.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class ListaLecturasFragment : Fragment(), LecturasView, RealmChangeListener<Realm> {

    lateinit var presenter: LecturasPresenter
    private lateinit var params: ListaLecturasFragmentArgs
    private lateinit var navController: NavController
    lateinit var realm:Realm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_lista_lecturas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        realm = Realm.getDefaultInstance()
        realm.addChangeListener(this)
        params = ListaLecturasFragmentArgs.fromBundle(arguments!!)
        navController = Navigation.findNavController(activity!!, R.id.nav_host_fragment)
        presenter = LecturasPresenterImpl(this)
        presenter.getReadings(params.id, false)
        fab.setOnClickListener {
            val action = ListaLecturasFragmentDirections.actionListaLecturasFragmentToNavNuevaLectura(id = params.id, name = params.name)
            navController.navigate(action)
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) { navController.navigateUp() }
    }

    override fun setReadings(results: RealmResults<Lectura>?) {
        val groupAdapter: GroupAdapter<GroupieViewHolder> = GroupAdapter()
        lecturas_list.apply {
            layoutManager = LinearLayoutManager(activity!!)
            adapter = groupAdapter
        }
        lecturas_list.hideFabButtonOnScroll(fab)
        val list:MutableList<LecturaItem> = ArrayList()
        results?.forEach { lectura -> list.add(LecturaItem(lectura, activity!!, results, presenter)) }
        groupAdapter.clear()
        groupAdapter.addAll(list)

    }

    override fun showEmptyMsg(show: Boolean) {
        if (show)
            message_body.setVisible()
        else
            message_body.setHidden()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.readings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_all_readings->{
                presenter.getReadings(params.id, true)
            }
            R.id.action_period_reading->{
                presenter.getReadings(params.id, false)
            }
            R.id.action_export->{
                showFolderChooseDialog(true)
            }
            R.id.action_export_period->{
                showFolderChooseDialog(false)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
        realm.removeAllChangeListeners()
        realm.close()
    }


    private fun showFolderChooseDialog(all: Boolean) {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val initialPath = if(File("/storage/emulated/0/").exists())
                File("/storage/emulated/0/")
            else
                null
            MaterialDialog(context!!).show {
                folderChooser(context,
                        initialDirectory = initialPath,
                        emptyTextRes = R.string.folder_choose,
                        allowFolderCreation = true) { dialog, folder ->
                    // Folder selected
                    val name = (params.name+ "_" + Date().formatDate(context))
                    MaterialDialog(context).show {
                        title(R.string.save_as)
                        message(text = folder.path)
                        input(prefill = name.replace(" ", "_")) { _, text ->
                            // Text submitted with the action button
                            if (!CSVHelper.saveActivePeriodReadings(folder.path, text.toString(), activity, params.id, all)){
                                MaterialDialog(activity!!).show{
                                    title(text = "Error!")
                                    message(R.string.export_error)
                                    positiveButton(R.string.agree)
                                }
                            }else {
                                Prefs.putString("last_path", folder.path);
                                MaterialDialog(activity!!).show {
                                    title(R.string.export_succes)
                                    message(text = folder.path + "/" + text.toString())
                                    positiveButton(R.string.agree)
                                }
                            }
                        }
                        positiveButton(R.string.ok)
                    }
                }
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, yay! Do the
            showFolderChooseDialog(false)
        } else {
            Toast.makeText(context, "NO PERMISSIONS GRANTED", Toast.LENGTH_LONG).show()
        }
    }

    override fun onChange(t: Realm) {
        presenter.getReadings(params.id, false)
    }
}
