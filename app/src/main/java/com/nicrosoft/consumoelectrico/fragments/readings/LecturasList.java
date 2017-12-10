package com.nicrosoft.consumoelectrico.fragments.readings;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.nicrosoft.consumoelectrico.R;
import com.nicrosoft.consumoelectrico.activities.Main;
import com.nicrosoft.consumoelectrico.fragments.readings.contracts.LecturasPresenter;
import com.nicrosoft.consumoelectrico.fragments.readings.contracts.LecturasPresenterImpl;
import com.nicrosoft.consumoelectrico.fragments.readings.contracts.LecturasView;
import com.nicrosoft.consumoelectrico.realm.Lectura;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;


/**
 * A simple {@link Fragment} subclass.
 */
public class LecturasList extends Fragment implements LecturasView {


    @Nullable
    @BindView(R.id.recycler_readings)
    RecyclerView recyclerReadings;
    @Nullable
    @BindView(R.id.message_body)
    LinearLayout messageBody;

    LecturasPresenter presenter;

    public LecturasList() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lecturas_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle arg = getArguments();
        if (arg != null) {
            String medidor_id = arg.getString("id");
            String medidor_name = (arg.getString("name"));
            presenter = new LecturasPresenterImpl(this);
            presenter.getReadings(medidor_id);
        }
    }

    @Override
    public void setReadings(RealmResults<Lectura> results) {
        AdapterReadings adapterReadings = new AdapterReadings(getActivity(), results, this, presenter);
        recyclerReadings.setAdapter(adapterReadings);
        recyclerReadings.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void showEmptyMsg(boolean show) {
        if(show)
            messageBody.setVisibility(View.VISIBLE);
        else
            messageBody.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
