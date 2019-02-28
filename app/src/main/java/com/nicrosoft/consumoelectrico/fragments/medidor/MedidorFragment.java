package com.nicrosoft.consumoelectrico.fragments.medidor;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.jakewharton.rxbinding2.view.RxView;
import com.nicrosoft.consumoelectrico.BuildConfig;
import com.nicrosoft.consumoelectrico.R;
import com.nicrosoft.consumoelectrico.activities.Main;
import com.nicrosoft.consumoelectrico.fragments.medidor.contracts.MedidorPresenter;
import com.nicrosoft.consumoelectrico.fragments.medidor.contracts.MedidorView;
import com.nicrosoft.consumoelectrico.fragments.medidor.imp.MedidorPresenterImpl;
import com.nicrosoft.consumoelectrico.realm.Medidor;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class MedidorFragment extends Fragment implements MedidorView, BillingProcessor.IBillingHandler {


    MedidorPresenter presenter;
    Main activity;
    BillingProcessor bp;


    boolean purchased = false;
    @BindView(R.id.list)
    RecyclerView medidoreList;
    @BindView(R.id.empty_bacground)
    AppCompatImageView emptyBacground;
    @BindView(R.id.scrim_bacground)
    AppCompatImageView scrimBacground;
    @BindView(R.id.message_body)
    LinearLayout messageBody;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.adView)
    AdView adView;
    private String PRODUCT_SKU = "remove_ads";
    private EditText med;
    private EditText desc;
    private AdapterMedidor adapter;
    private InterstitialAd mInterstitialAd;

    public MedidorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_medidor_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bp = new BillingProcessor(getActivity(), BuildConfig.APP_BILLING_PUB_KEY, BuildConfig.MERCHANT_ID, this);
        activity = (Main) getActivity();
        presenter = new MedidorPresenterImpl(getActivity(), this);
        presenter.getMedidores();
        setFabListener();
        if (!purchased)
            requestInterstialAds();
    }


    private void setFabListener() {
        RxView.clicks(fab).subscribe(o -> {
            MaterialDialog dlg = new MaterialDialog.Builder(getActivity())
                    .title(R.string.new_medidor)
                    .customView(R.layout.dlg_medidor, true)
                    .positiveText(R.string.save)
                    .positiveColor(getActivity().getResources().getColor(R.color.md_green_700))
                    .negativeText(android.R.string.cancel)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Realm realm = Realm.getDefaultInstance();
                            realm.executeTransaction(realm1 -> {
                                Medidor medidor;
                                if (med != null && !med.getText().toString().isEmpty()) {
                                    medidor = new Medidor(med.getText().toString());
                                    if (desc != null && !desc.getText().toString().isEmpty()) {
                                        medidor.descripcion = desc.getText().toString();
                                    }
                                    realm.copyToRealm(medidor);
                                } else
                                    Toast.makeText(getActivity(), getActivity().getString(R.string.invalid_medidor_name), Toast.LENGTH_LONG).show();

                            });
                            realm.close();
                        }
                    }).build();
            try {
                med = dlg.getCustomView().findViewById(R.id.txt_medidor_name);
                desc = dlg.getCustomView().findViewById(R.id.txt_medidor_desc);
            } catch (Exception ignored) {
            }
            dlg.show();
        }, throwable -> {
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.getMedidores();
        medidoreList.scrollToPosition(requestCode);
        if (!purchased)
            showInterstial();
    }

    private void showInterstial(){

        int ne = Prefs.getInt("num_show_readings", 0);
        Prefs.putInt("num_show_readings", ne + 1);
        if (Prefs.getInt("num_show_readings", 0) == Prefs.getInt("show_after", 5)) {
            Prefs.putInt("num_show_readings", 0);
            Random r = new Random();
            int Low = 7;
            int High = 10;
            int rnd = r.nextInt(High - Low) + Low;
            Prefs.putInt("show_after", rnd);
            if(mInterstitialAd!=null && mInterstitialAd.isLoaded()){
                mInterstitialAd.show();
            }
        }

    }

    private void requestInterstialAds() {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    //.addTestDevice("0B307F34E3DDAF6C6CAB28FAD4084125")
                    //.addTestDevice("B0FF48A19BF36BD2D5DCD62163C64F45")
                    .build();
            mInterstitialAd = new InterstitialAd(getActivity());
            mInterstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstical));
            mInterstitialAd.loadAd(adRequest);
            mInterstitialAd.setAdListener(new AdListener() {

                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    requestInterstialAds();
                }
            });
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
                medidoreList.setPadding(6,105,6,6);
            }
        });

    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
    }

    @Override
    public void onPurchaseHistoryRestored() {
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        showAds();
    }

    @Override
    public void onBillingInitialized() {
        if (!bp.isPurchased(PRODUCT_SKU)) {
            showAds();
        }else {
            purchased = true;
        }
    }


    @Override
    public void setMedidores(RealmResults<Medidor> medidores) {
        adapter = new AdapterMedidor((Main) getActivity(), medidores, this, presenter);
        medidoreList.setAdapter(adapter);
    }

    @Override
    public void showEmptyDataMsg() {
        scrimBacground.setVisibility(View.VISIBLE);
        emptyBacground.setVisibility(View.VISIBLE);
        messageBody.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideEmptyDataMsg() {
        scrimBacground.setVisibility(View.GONE);
        emptyBacground.setVisibility(View.GONE);
        messageBody.setVisibility(View.GONE);
    }

    @Override
    public void startNewReadingActivity(Intent intent, int index) {
        startActivityForResult(intent, index);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}
