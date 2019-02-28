package com.nicrosoft.consumoelectrico.fragments.readings;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nicrosoft.consumoelectrico.R;
import com.nicrosoft.consumoelectrico.activities.reading.NewReadingActivity;
import com.nicrosoft.consumoelectrico.fragments.readings.contracts.LecturasPresenter;
import com.nicrosoft.consumoelectrico.fragments.readings.contracts.LecturasPresenterImpl;
import com.nicrosoft.consumoelectrico.fragments.readings.contracts.LecturasView;
import com.nicrosoft.consumoelectrico.realm.Lectura;
import com.pixplicity.easyprefs.library.Prefs;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;


/**
 * A simple {@link Fragment} subclass.
 */
public class LecturasList extends Fragment implements LecturasView {

    @BindView(R.id.adView)
    AdView adView;

    @Nullable
    @BindView(R.id.recycler_readings)
    RecyclerView recyclerReadings;
    @Nullable
    @BindView(R.id.message_body)
    LinearLayout messageBody;

    LecturasPresenter presenter;
    @BindView(R.id.list_title)
    TextView listTitle;
    private String medidor_id;
    private Bundle arg;
    private String medidor_name;

    public LecturasList() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lecturas_list, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        arg = getArguments();
        if (arg != null) {
            medidor_id = arg.getString("id");
            medidor_name = (arg.getString("name"));
            presenter = new LecturasPresenterImpl(this);
            presenter.getReadings(medidor_id, false);
        }
        if(!Prefs.getBoolean("isPurchased", false))
            showAds();
    }

    @Override
    public void setReadings(RealmResults<Lectura> results) {
        AdapterReadings adapterReadings = new AdapterReadings(getActivity(), results, this, presenter);
        recyclerReadings.setAdapter(adapterReadings);
        recyclerReadings.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        if(results.size()>0){
            SimpleDateFormat time_format = new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault());
            String  fin = time_format.format(results.first().fecha_lectura);
            String inicio = time_format.format(results.last().fecha_lectura);
            listTitle.setText(getString(R.string.lecturas_desde_hasta, inicio, fin));
        }
    }

    @Override
    public void showEmptyMsg(boolean show) {
        if (show) {
            messageBody.setVisibility(View.VISIBLE);
        }
        else {
            messageBody.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void showAds() {

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("0B307F34E3DDAF6C6CAB28FAD4084125")
                //.addTestDevice("B0FF48A19BF36BD2D5DCD62163C64F45")
                .build();

        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
                recyclerReadings.setPadding(2,2,2,118);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.readings_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_all_readings:
                presenter.getReadings(medidor_id, true);
            break;
            case R.id.action_period_reading:
                presenter.getReadings(medidor_id, false);
            break;
            case R.id.action_export:
                showFolderChooseDialog(true);
                break;
            case R.id.action_export_period:
                showFolderChooseDialog(false);
                break;
            case R.id.action_new_readings:
                Intent intent2 = new Intent(getActivity(), NewReadingActivity.class);
                intent2.putExtras(arg);
                startActivityForResult(intent2, 1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFolderChooseDialog(boolean all) {
        // Initialize Builder
        /*StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(getActivity())
                .withFragmentManager(getFragmentManager())
                .withMemoryBar(true)
                .allowAddFolder(true)
                .allowCustomPath(true)
                .setType(StorageChooser.DIRECTORY_CHOOSER)
                .build();
        chooser.show();
        */
        // get path that the user has chosen
        /* chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                SimpleDateFormat time_format = new SimpleDateFormat(getString(R.string.date_format_save), Locale.getDefault());
                String name = getString(R.string.label_consumption)+ "_" +medidor_name+ "_" + time_format.format(new Date());
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.save_as)
                        .content(path)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("", name, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                // Do something
                                if (!CSVHelper.saveActivePeriodReadings(path, input.toString(), getActivity(), medidor_id, all)){
                                    new MaterialDialog.Builder(getActivity())
                                            .title("Error!")
                                            .content(R.string.export_error)
                                            .positiveText(R.string.agree)
                                            .show();
                                }else {
                                    Prefs.putString("last_path", path);
                                    new MaterialDialog.Builder(getActivity())
                                            .title(R.string.export_succes)
                                            .content(path+"/"+input.toString())
                                            .positiveText(R.string.agree)
                                            .show();
                                }
                            }
                        }).show();
            }
        });
        */
    }

}
