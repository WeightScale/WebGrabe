package com.kostya.webgrabe;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.kostya.webgrabe.internet.Internet;
import com.kostya.webgrabe.provider.Invoice;
import com.kostya.webgrabe.provider.Invoice_;
import com.kostya.webgrabe.provider.Weighing;
import com.kostya.webgrabe.provider.Weighing_;
import com.kostya.webscaleslibrary.module.Module;
import com.kostya.webgrabe.settings.ActivityPreferences;
import com.kostya.webgrabe.task.IntentServiceGoogleForm;
import com.kostya.webscaleslibrary.module.InterfaceCallbackScales;
import com.kostya.webscaleslibrary.ScalesView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.objectbox.BoxStore;
import io.objectbox.query.Query;

/**
 * @author Kostya on 02.10.2016.
 */
public class ActivityMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        com.kostya.webgrabe.FragmentInvoice.OnFragmentInvoiceListener/*, OnInteractionListener*/ {
    private Vibrator vibrator; //вибратор
    private float screenBrightness;
    //private PowerManager.WakeLock wakeLock;
    private DrawerLayout drawer;
    private FloatingActionButton fab;
    private ScalesView scalesView;
    private Module scaleModule;
    private FragmentManager fragmentManager;
    private FragmentInvoice fragmentInvoice;
    private FragmentListInvoice fragmentListInvoice;
    private InterstitialAd mInterstitialAd;
    private Globals globals;
    /** Настройки общии для модуля. */
    public static final String SETTINGS = ActivityMain.class.getName() + ".SETTINGS"; //
    private static final int ALERT_DIALOG1 = 1;
    private static final int ALERT_DIALOG2 = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        /*WindowManager.LayoutParams lp = getWindow().getAttributes();
        screenBrightness = lp.screenBrightness;*/

        //getWindow().setAttributes(lp);

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        /*PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        wakeLock.acquire();*/
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //SnackBar.make(view, "Replace with your own action", SnackBar.LENGTH_LONG).setAction("Action", null).show();
                openFragmentInvoice(0);
            }
        });

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                return false;
            }
        });

        globals = Globals.getInstance();
        globals.initialize(this);

        //setupInterstitial();

        fragmentManager = getSupportFragmentManager();

        fragmentListInvoice = com.kostya.webgrabe.FragmentListInvoice.newInstance(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date()));
        fragmentManager.beginTransaction().add(R.id.fragmentInvoice, fragmentListInvoice, com.kostya.webgrabe.FragmentListInvoice.class.getSimpleName()).commit();

        scalesView = findViewById(R.id.scalesView);
        scalesView.create(globals.getPackageInfo().versionName, new InterfaceCallbackScales() {
            @Override
            public void onCreate(Object obj) {
                scaleModule = (Module) obj;
                //globals.setScaleModule(obj);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scales, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.preferences:
                startActivity(new Intent(getApplicationContext(), ActivityPreferences.class));
            break;
            case R.id.search:
                //scalesView.openSearchScales();
            break;
            case R.id.power_off:
                finish();
            break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.archive_invoice:
                /*if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    startActivity(new Intent(getApplicationContext(), ActivityArchive.class));
                }*/
                startActivity(new Intent(getApplicationContext(), com.kostya.webgrabe.ActivityArchive.class));
            break;
            case R.id.search_scales:
                //scalesView.openSearchScales();
            break;
            case R.id.settings:
                startActivity(new Intent(getApplicationContext(), ActivityPreferences.class));
            break;
            case R.id.new_invoice:
                openFragmentInvoice(0);
            break;
            case R.id.power:
                finish();
            break;
            case R.id.help:
                startActivity(new Intent(getApplicationContext(), com.kostya.webgrabe.ActivityHelp.class));
            break;
            default:
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //scalesView.resume();
        new Internet(this).turnOnWiFiConnection(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //scalesView.pause();
        //new Internet(this).turnOnWiFiConnection(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //scalesView.exit();
        removeInvoiceIsCloud(10);
        startService(new Intent(this, IntentServiceGoogleForm.class).setAction(IntentServiceGoogleForm.ACTION_EVENT_TABLE));
    }

    @Override
    public void onBackPressed() {
        /*if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            //exit();
            return;
        }
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.press_again_to_exit, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);*/
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.CustomAlertDialogInvoice));
        builder.setCancelable(false)
                .setTitle("Сообщение");
        switch(id) {
            case ALERT_DIALOG1:
                builder.setMessage("Накладная уже открыта. Закройте накладную после создайте новую.")
                        .setIcon(R.drawable.ic_notification)
                        .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //Do something here
                                dialog.dismiss();
                            }
                        });
                break;
            case ALERT_DIALOG2:
                builder.setMessage("ВЫ ХОТИТЕ ЗАКРЫТЬ НАКЛАДНУЮ?")
                        .setIcon(R.drawable.ic_notification)
                        .setPositiveButton("ДА", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                fragmentManager.beginTransaction().remove(fragmentInvoice).commit();
                                //fab.setVisibility(View.VISIBLE);
                            }
                        }).setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).setNegativeButton("НЕТ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                break;
            default:
                return null;
        }
        return builder.create();

    }

    /**
     * Закрыть накладную.
     */
    @SuppressLint("RestrictedApi")
    public void closedFragmentInvoice(){
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(com.kostya.webgrabe.FragmentInvoice.class.getSimpleName());
        if (fragment instanceof com.kostya.webgrabe.FragmentInvoice){
            fragmentManager.beginTransaction().remove(fragment).commit();
            fab.setVisibility(View.VISIBLE);
            startService(new Intent(this, IntentServiceGoogleForm.class).setAction(IntentServiceGoogleForm.ACTION_EVENT_TABLE));
        }
        /*WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = screenBrightness;
        getWindow().setAttributes(lp);*/
    }

    @Override
    public void onEnableStable(boolean enable) {
        /*if (scaleModule != null)
            scaleModule.setEnableProcessStable(enable);*/
    }

    @SuppressLint("RestrictedApi")
    public boolean openFragmentInvoice(long id){
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(com.kostya.webgrabe.FragmentInvoice.class.getSimpleName());
        if (fragment instanceof com.kostya.webgrabe.FragmentInvoice){
            return false;
            //showDialog(ALERT_DIALOG1);
        }else {
            vibrator.vibrate(50);
            fragmentInvoice = com.kostya.webgrabe.FragmentInvoice.newInstance(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date()),
                    new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()), id);
            fragmentManager.beginTransaction().add(R.id.fragmentInvoice, fragmentInvoice, com.kostya.webgrabe.FragmentInvoice.class.getSimpleName()).commit();
            fab.setVisibility(View.INVISIBLE);
            return true;
        }
    }

    /*@Override
    public void onUpdateSettings(Settings settings) {

    }

    @Override
    public void onScaleModuleCallback(Module obj) {
        scaleModule = obj;
    }*/

    /*@Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        switch (event.getKeyCode()){
            case KeyEvent.KEYCODE_VOLUME_UP:
                openFragmentInvoice(null);
            return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:

                return true;
            default:
                return super.dispatchKeyEvent(event);
        }

        *//*if (event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
            Log.i("", "Dispath event power");
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
            return true;
        }else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP){
            openFragmentInvoice(null);
            return true;
        }

        return super.dispatchKeyEvent(event);*//*
    }*/

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_POWER) {
            // Do something here...
            event.startTracking(); // Needed to track long presses
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/

    /** Удалять накладные старше количества дней, и те которые не отправлены на сервер.
     * @param dayAfter Количество дней.
     */
    private void removeInvoiceIsCloud(long dayAfter) {
        try{
            //InvoiceTable invoiceTable = new InvoiceTable(this);
            //WeighingTable weighingTable = new WeighingTable(this);
            //Cursor result = invoiceTable.getIsCloud();
            //DaoSession daoSession = ((Main)getApplication()).getDaoSession();
            //InvoiceDao invoiceDao = daoSession.getInvoiceDao();
            BoxStore boxStore = ((Main)getApplication()).getBoxStore();
            List<Invoice> invoices = boxStore.boxFor(Invoice.class).query().equal(Invoice_.isCloud, true).build().find();// invoiceDao.queryBuilder().where(InvoiceDao.Properties.IsCloud.eq(true)).list();
            List<Weighing> weighings = boxStore.boxFor(Weighing.class).getAll(); //daoSession.getWeighingDao().loadAll();
            Query<Weighing> weighingQuery = boxStore.boxFor(Weighing.class).query().build();
            //result.moveToFirst();

            //if (!result.isAfterLast()) {

            for (Invoice invoice : invoices) {
                long id = invoice.getId();
                String date = invoice.getDateCreate();
                long day = 0;
                try {
                    day = Invoice.dayDiff(new Date(), new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(date));
                } catch (ParseException e) {
                    Log.e("TAG", e.getMessage());
                }
                if (day > dayAfter) {
                    //invoiceTable.removeEntry(id);
                    //daoSession.getInvoiceDao().deleteByKey(id);
                    boxStore.boxFor(Invoice.class).remove(invoice);
                    //weighingTable.removeEntryInvoice(id);
                    //daoSession.getWeighingDao().queryBuilder().where(WeighingDao.Properties.IdInvoice.eq(id)).unique().delete();
                    weighingQuery.setParameter(Weighing_.idInvoice, id).remove();
                }
            }
                /*do {
                    int id = result.getInt(result.getColumnIndex(InvoiceTable.KEY_ID));
                    //int id = invoices.getInt(result.getColumnIndex(InvoiceTable.KEY_ID));
                    String date = result.getString(result.getColumnIndex(InvoiceTable.KEY_DATE_CREATE));
                    long day = 0;
                    try {
                        day = InvoiceTable.dayDiff(new Date(), new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(date));
                    } catch (ParseException e) {
                        Log.e("TAG", e.getMessage());
                    }
                    if (day > dayAfter) {
                        invoiceTable.removeEntry(id);
                        weighingTable.removeEntryInvoice(id);
                    }
                } while (result.moveToNext());*/
            //}
            //result.close();
        } catch (Exception e) {
        }
    }

    /**
     * Настройка межстарничного рекламного блок.
     */
    private void setupInterstitial(){
        mInterstitialAd = new InterstitialAd(getApplicationContext());
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_id));
        mInterstitialAd.setAdListener(new AdListener() {
           /* @Override
            public void onAdLoaded() {
                Toast.makeText(ActivityScales.this,"The interstitial is loaded", Toast.LENGTH_SHORT).show();
                mInterstitialAd.show();
            }*/

            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                startActivity(new Intent(getApplicationContext(), com.kostya.webgrabe.ActivityArchive.class));
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
            }

        });
        requestNewInterstitial();
    }

    /**
     * запрос на новый межстраничный рекламный блок.
     */
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)// This is for emulators
                //.addTestDevice(Globals.getInstance().getDeviceId())
                .build();
        mInterstitialAd.loadAd(adRequest);
    }
}
