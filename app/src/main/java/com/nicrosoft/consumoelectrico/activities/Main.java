package com.nicrosoft.consumoelectrico.activities;

import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.nicrosoft.consumoelectrico.BuildConfig;
import com.nicrosoft.consumoelectrico.R;
import com.nicrosoft.consumoelectrico.RealTimeConsumptionFragment;
import com.nicrosoft.consumoelectrico.activities.reading.NewReadingActivity;
import com.nicrosoft.consumoelectrico.fragments.AppPreference;
import com.nicrosoft.consumoelectrico.fragments.main.MainFragment;
import com.nicrosoft.consumoelectrico.fragments.medidor.MedidorFragment;
import com.nicrosoft.consumoelectrico.fragments.readings.LecturasList;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BillingProcessor.IBillingHandler {

    @Nullable
    @BindView(R.id.fragment_container)
    FrameLayout fragmentContainer;
    FragmentManager fragmentManager;
    @Nullable
    @BindView(R.id.coordinator)
    public CoordinatorLayout coordinator;
    BillingProcessor bp;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    String PRODUCT_SKU = "remove_ads";
    InterstitialAd mInterstitialAd;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this);
        bp = new BillingProcessor(this, BuildConfig.APP_BILLING_PUB_KEY, BuildConfig.MERCHANT_ID, this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fragmentManager = getFragmentManager();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Main.this, NewReadingActivity.class));
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        navView.getMenu().getItem(0).setChecked(true);
        navView.setNavigationItemSelectedListener(this);
        MainFragment mainFragment = new MainFragment();
        MedidorFragment medidorFragment = new MedidorFragment();
        Bundle args = new Bundle();
        args.putBoolean("isPurchased", bp.isPurchased(PRODUCT_SKU));
        mainFragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, medidorFragment).commit();
        RelativeLayout headerNav = (RelativeLayout) navView.getHeaderView(0);
        TextView version = (TextView) headerNav.findViewById(R.id.version);
        version.setText(BuildConfig.VERSION_NAME);
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_consumption) {
            if(!bp.isPurchased(PRODUCT_SKU))
                requestAds();
            getSupportActionBar().setTitle(R.string.title_activity_main);
            MedidorFragment mainFragment = new MedidorFragment();
            Bundle args = new Bundle();
            args.putBoolean("isPurchased", bp.isPurchased(PRODUCT_SKU));
            mainFragment.setArguments(args);
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, mainFragment).commit();
        } else if (id == R.id.nav_readings) {
            getSupportActionBar().setTitle(R.string.calculator_title);
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, new RealTimeConsumptionFragment()).commit();
        } else if (id == R.id.nav_settings) {
            if(!bp.isPurchased(PRODUCT_SKU))
                requestAds();
            getSupportActionBar().setTitle(R.string.action_settings);
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AppPreference())
                    .commit();
        } else if (id == R.id.nav_share) {
            //bp.consumePurchase(PRODUCT_SKU);
            try {
                Intent rate_intent = new Intent(Intent.ACTION_SEND);
                rate_intent.setType("text/plain");
                rate_intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                String sAux = getResources().getString(R.string.share_app_msg);
                sAux = sAux + "\n https://play.google.com/store/apps/details?id=" + getPackageName() + " \n\n";
                rate_intent.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(rate_intent, getResources().getString(R.string.share_using)));
            } catch (Exception ignored) {
            }
        } else if (id == R.id.nav_send) {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        } else if (id == R.id.nav_remove_ads) {
            bp.purchase(this, PRODUCT_SKU);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        new MaterialDialog.Builder(this)
                .title(R.string.remove_ads)
                .content(R.string.remove_ads_thank)
                .positiveText(R.string.ok)
                .show();

        hideItem();
    }

    @Override
    public void onPurchaseHistoryRestored() {}

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        new MaterialDialog.Builder(this)
                .title(R.string.notice)
                .content(R.string.billing_error)
                .positiveText(R.string.ok)
                .show();
    }

    @Override
    public void onBillingInitialized() {
        if(bp.isPurchased(PRODUCT_SKU))
            hideItem();
        if(!bp.isPurchased(PRODUCT_SKU))
            requestAds();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (bp != null)
            bp.release();
        super.onDestroy();
    }

    private void hideItem() {
        Prefs.putBoolean("isPurchased", true);
        Menu nav_Menu = navView.getMenu();
        nav_Menu.findItem(R.id.nav_remove_ads).setVisible(false);
    }
    private void requestAds() {
        int ne =  Prefs.getInt("num_show_readings", 0);
        Prefs.putInt("num_show_readings", ne + 1);

        if(Prefs.getInt("num_show_readings", 0) == Prefs.getInt("show_after", 5)) {
            Prefs.putInt("num_show_readings", 0);
            Random r = new Random();
            int Low = 7;int High = 10;
            int rnd = r.nextInt(High-Low) + Low;
            Prefs.putInt("show_after", rnd);

            AdRequest adRequest = new AdRequest.Builder()
                    //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    //.addTestDevice("0B307F34E3DDAF6C6CAB28FAD4084125")
                    //.addTestDevice("B0FF48A19BF36BD2D5DCD62163C64F45")
                    .build();

            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstical));
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mInterstitialAd.show();
                }
            });
            mInterstitialAd.loadAd(adRequest);
        }
    }

}
