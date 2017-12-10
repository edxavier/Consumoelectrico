package com.nicrosoft.consumoelectrico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.nicrosoft.consumoelectrico.R;
import com.nicrosoft.consumoelectrico.fragments.readings.LecturasList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PeriodReadingsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fragment_container)
    FrameLayout fragmentContainer;
    @BindView(R.id.coordinator)
    CoordinatorLayout coordinator;
    private String medidor_id;
    private String medidor_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_period_readings);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            medidor_id = extras.getString("id");
            medidor_name = (extras.getString("name"));
        }
        getSupportActionBar().setTitle(medidor_name);
        LecturasList frg = new LecturasList();
        frg.setArguments(extras);
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, frg).commit();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
