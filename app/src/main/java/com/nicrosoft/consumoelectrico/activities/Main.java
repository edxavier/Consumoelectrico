package com.nicrosoft.consumoelectrico.activities;

import android.Manifest;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.nicrosoft.consumoelectrico.myUtils.CSVHelper;
import com.nicrosoft.consumoelectrico.myUtils.RestoreHelper;
import com.obsez.android.lib.filechooser.ChooserDialog;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BillingProcessor.IBillingHandler{

    private static final int REQUEST_WRITE_STORAGE_PERMISSIONS = 100;
    private static final int REQUEST_READ_STORAGE_PERMISSIONS = 200;
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

    CompositeDisposable mDisposable = new CompositeDisposable();

    String PRODUCT_SKU = "remove_ads";
    InterstitialAd mInterstitialAd;


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
        TextView version = headerNav.findViewById(R.id.version);
        version.setText(BuildConfig.VERSION_NAME);
        //StringBuilder path = new StringBuilder(getFilesDir().getAbsolutePath());
        RestoreHelper.getInternalStoragePath(this);

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
            if(!bp.isPurchased(PRODUCT_SKU) && mInterstitialAd!=null && timeToShowInterstial()) {
                if(mInterstitialAd.isLoaded())
                    mInterstitialAd.show();
            }
            getSupportActionBar().setTitle(R.string.title_activity_main);
            MedidorFragment mainFragment = new MedidorFragment();
            Bundle args = new Bundle();
            args.putBoolean("isPurchased", bp.isPurchased(PRODUCT_SKU));
            mainFragment.setArguments(args);
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, mainFragment).commit();
        } else if (id == R.id.nav_readings) {
            if(!bp.isPurchased(PRODUCT_SKU) && mInterstitialAd!=null && timeToShowInterstial()) {
                if(mInterstitialAd.isLoaded())
                    mInterstitialAd.show();
            }
            getSupportActionBar().setTitle(R.string.calculator_title);
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, new RealTimeConsumptionFragment()).commit();
        } else if (id == R.id.nav_settings) {
            if(!bp.isPurchased(PRODUCT_SKU) && mInterstitialAd!=null && timeToShowInterstial()) {
                if(mInterstitialAd.isLoaded())
                    mInterstitialAd.show();
            }
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
        else if (id == R.id.nav_export) {
            if( ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                showFolderChooseDialog();
            }else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE_PERMISSIONS);
            }
        }else if (id == R.id.nav_import) {
            if( ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                showFileChooseDialog();
            }else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE_PERMISSIONS);
            }
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
        if(bp.isPurchased(PRODUCT_SKU)) {
            Prefs.putBoolean("isPurchased", true);
            hideItem();
        }
        if(!bp.isPurchased(PRODUCT_SKU))
            requestInterstialAds();
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
        if(!mDisposable.isDisposed())
            mDisposable.dispose();
        super.onDestroy();
    }

    private void hideItem() {
        Prefs.putBoolean("isPurchased", true);
        Menu nav_Menu = navView.getMenu();
        nav_Menu.findItem(R.id.nav_remove_ads).setVisible(false);
    }

    private boolean timeToShowInterstial(){
        int ne =  Prefs.getInt("num_show_readings", 0);
        Prefs.putInt("num_show_readings", ne + 1);

        if(Prefs.getInt("num_show_readings", 0) >= Prefs.getInt("show_after", 10)) {
            Prefs.putInt("num_show_readings", 0);
            Random r = new Random();
            int Low = 15;int High = 20;
            int rnd = r.nextInt(High-Low) + Low;
            Prefs.putInt("show_after", rnd);
            return true;
        }else {
            return false;
        }
    }
    private void requestInterstialAds() {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    //.addTestDevice("0B307F34E3DDAF6C6CAB28FAD4084125")
                    //.addTestDevice("B0FF48A19BF36BD2D5DCD62163C64F45")
                    .build();

            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstical));
            mInterstitialAd.setAdListener(new AdListener() {

                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    requestInterstialAds();
                }
            });
            mInterstitialAd.loadAd(adRequest);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    showFolderChooseDialog();
                } else {
                    Toast.makeText(this, "NO PERMISSIONS GRANTED", Toast.LENGTH_LONG).show();
                }
            }
            case REQUEST_READ_STORAGE_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    showFileChooseDialog();
                } else {
                    Toast.makeText(this, "NO PERMISSIONS GRANTED", Toast.LENGTH_LONG).show();
                }
                break;

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    private void showFolderChooseDialog() {

        MaterialDialog dialog = new MaterialDialog.Builder(Main.this)
                .title(R.string.exporting_data)
                .content(R.string.importing_msg)
                .progressIndeterminateStyle(true)
                .progress(true, 0).build();

        new ChooserDialog().with(this)
                .withFilter(true, false)
                .withStartFile(RestoreHelper.getInternalStoragePath(this))
                .withDateFormat("dd/MM/yy HH:mm")
                .withResources(R.string.title_choose_folder, R.string.choose, R.string.cancel)
                .withNavigateUpTo(new ChooserDialog.CanNavigateUp() {
                    @Override
                    public boolean canUpTo(File dir) {
                        return true;
                    }
                })
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        SimpleDateFormat time_format = new SimpleDateFormat(getString(R.string.date_format_save), Locale.getDefault());
                        String name = getString(R.string.app_name) + time_format.format(new Date());
                        name = name.replace(' ', '_').replace('.', '_');


                        String finalName = name;
                        mDisposable.add(
                                Single.create((SingleOnSubscribe<Boolean>) e -> e.onSuccess(CSVHelper.saveAllToCSV(path, finalName, Main.this)))
                                        .subscribeOn(Schedulers.io()) //Run on IO Thread
                                        .observeOn(AndroidSchedulers.mainThread()) //Run on UI Thread
                                        .doOnSubscribe(__ -> {
                                            dialog.show();
                                        })
                                        .doFinally(dialog::dismiss)
                                        .subscribe(res -> {
                                            if (!res){
                                                new MaterialDialog.Builder(Main.this)
                                                        .title("Error!")
                                                        .content(R.string.export_error)
                                                        .positiveText(R.string.agree)
                                                        .show();
                                            }else {
                                                Prefs.putString("last_path", path);
                                                new MaterialDialog.Builder(Main.this)
                                                        .title(R.string.export_succes)
                                                        .content(path+"/"+finalName)
                                                        .positiveText(R.string.agree)
                                                        .show();
                                            }
                                        },throwable -> {
                                            new MaterialDialog.Builder(Main.this)
                                                    .title("Error!")
                                                    .content(R.string.export_error)
                                                    .positiveText(R.string.agree)
                                                    .show();
                                        })
                        );
                    }
                })
                .build()
                .show();

        // Initialize Builder
        /*StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(this)
                .withFragmentManager(getFragmentManager())
                .withMemoryBar(true)
                .allowAddFolder(true)
                .allowCustomPath(true)
                .setType(StorageChooser.DIRECTORY_CHOOSER)
                .build();
        chooser.show();*/
        // get path that the user has chosen
        /* chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                SimpleDateFormat time_format = new SimpleDateFormat(getString(R.string.date_format_save), Locale.getDefault());
                String name = getString(R.string.app_name) + time_format.format(new Date());
                name = name.replace(' ', '_');

                new MaterialDialog.Builder(Main.this)
                        .title(R.string.save_as)
                        .content(path)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("", name, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                // Do something
                                if (!CSVHelper.saveAllToCSV(path, input.toString(), getApplicationContext())){
                                    new MaterialDialog.Builder(Main.this)
                                            .title("Error!")
                                            .content(R.string.export_error)
                                            .positiveText(R.string.agree)
                                            .show();
                                }else {
                                    Prefs.putString("last_path", path);
                                    new MaterialDialog.Builder(Main.this)
                                            .title(R.string.export_succes)
                                            .content(path+"/"+input.toString())
                                            .positiveText(R.string.agree)
                                            .show();
                                }
                            }
                        }).show();
            }
        }); */
    }


    private void showFileChooseDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(Main.this)
                .title(R.string.importing_data)
                .content(R.string.importing_msg)
                .progressIndeterminateStyle(true)
                .progress(true, 0).build();

        new ChooserDialog().with(this)
                .withStartFile(RestoreHelper.getInternalStoragePath(this))
                .withResources(R.string.title_choose_file, R.string.choose, R.string.cancel)
                .withDateFormat("dd/MM/yy HH:mm")
                .withNavigateUpTo(new ChooserDialog.CanNavigateUp() {
                    @Override
                    public boolean canUpTo(File dir) {
                        return true;
                    }
                })
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {

                        mDisposable.add(
                            Single.create((SingleOnSubscribe<Boolean>) e -> e.onSuccess(CSVHelper.restoreAllFromCSV(path, getApplicationContext())))
                                .subscribeOn(Schedulers.io()) //Run on IO Thread
                                .observeOn(AndroidSchedulers.mainThread()) //Run on UI Thread
                                .doOnSubscribe(__ -> {
                                    dialog.show();
                                })
                                .doFinally(dialog::dismiss)
                                .subscribe(res -> {
                                    if (!res){
                                        new MaterialDialog.Builder(Main.this)
                                                .title("Error!")
                                                .content(R.string.import_error)
                                                .positiveText(R.string.agree)
                                                .show();
                                    }else {
                                        new MaterialDialog.Builder(Main.this)
                                                .title(R.string.import_succes)
                                                .content(path)
                                                .positiveText(R.string.agree)
                                                .show();
                                    }
                                },throwable -> {
                                    new MaterialDialog.Builder(Main.this)
                                            .title("Error!")
                                            .content(R.string.import_error)
                                            .positiveText(R.string.agree)
                                            .show();
                                })
                        );
                    }
                })
                .build()
                .show();
    }

}
