package com.kostya.webscaleslibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.kostya.webscaleslibrary.module.Client;
import com.kostya.webscaleslibrary.module.Commands;
import com.kostya.webscaleslibrary.module.InterfaceModule;
import com.kostya.webscaleslibrary.module.Module;
import com.kostya.webscaleslibrary.module.ObjectScales;
import com.kostya.webscaleslibrary.module.WebScalesClient;
import com.kostya.webscaleslibrary.preferences.ActivityProperties;
import com.kostya.webscaleslibrary.preferences.Settings;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentWebTerminal#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentWebTerminal extends FragmentView implements View.OnClickListener/*, View.OnLongClickListener*/ {
    static Context mContext;
    /** Настройки для весов. */
    public Settings settings;
    WebScalesClient webScalesClient;
    private final Handler handler = new Handler();
    //private Module scaleModuleWiFi;
    //private ModuleWiFi scaleModule;
    private SpannableStringBuilder textKg;
    private ProgressBar progressBarStable;
    private ProgressBar progressBarWeight;
    private TextView weightTextView, textViewBattery, textViewTemperature;
    private ImageView imageViewBluetooth;
    private LinearLayout layoutSearch;
    private Drawable dProgressWeight, dWeightDanger;
    private SimpleGestureFilter detectorWeightView;
    private Vibrator vibrator; //вибратор
    private BaseReceiver baseReceiver; //приёмник намерений
    private static final String ARG_VERSION = FragmentWebTerminal.class.getSimpleName()+"VERSION";
    private static final String ARG_SSID = FragmentWebTerminal.class.getSimpleName()+"SSID";
    private String version;
    private String ssid;
    private double moduleWeight;
    private boolean touchWeightView;
    private boolean weightViewIsSwipe;
    protected boolean isStable;

    //private OnInteractionListener onInteractionListener;
    private static OnInteractionListener onListener;

    public FragmentWebTerminal(){}

    //protected void loadModule(ScaleModule scaleModule) {this.scaleModule = scaleModule;}

    /*public void setOnInteractionListener(OnInteractionListener listener){
        onInteractionListener = listener;
    }*/

    public static FragmentWebTerminal newInstance(String version, String ssid, OnInteractionListener listener) {
        onListener = listener;
        FragmentWebTerminal fragment = new FragmentWebTerminal();
        Bundle args = new Bundle();
        args.putString(ARG_VERSION, version);
        args.putString(ARG_SSID, ssid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            version = getArguments().getString(ARG_VERSION);
            ssid = getArguments().getString(ARG_SSID);
        }
        settings = new Settings(getActivity()/*, ScalesView.SETTINGS*/);
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        textKg = new SpannableStringBuilder(getResources().getString(R.string.scales_kg));
        textKg.setSpan(new TextAppearanceSpan(getActivity(), R.style.SpanTextKgMini),0,textKg.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        baseReceiver = new BaseReceiver(getActivity());
        baseReceiver.register();

        //setupWeightView();
        createScalesModule(ssid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scales_mini, null);
        progressBarWeight = view.findViewById(R.id.progressBarWeight);
        progressBarStable = view.findViewById(R.id.progressBarStable);
        weightTextView = view.findViewById(R.id.weightTextView);

        view.findViewById(R.id.buttonSettings).setOnClickListener(this);
        //view.findViewById(R.id.buttonSearch).setOnClickListener(this);
        layoutSearch = view.findViewById(R.id.layoutSearch);

        textViewBattery = view.findViewById(R.id.textBattery);
        textViewTemperature = view.findViewById(R.id.textTemperature);

        setupWeightView();

        return view;
    }

    /*@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnInteractionListener) {
            onInteractionListener = (OnInteractionListener) activity;
        } else {
            throw new RuntimeException(activity + " must implement OnInteractionListener");
        }
    }*/

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        if (webScalesClient.isConnected()){
            startCheckGetWeight();
        }
        //try {scaleModule.scalesProcessEnable(true);}catch (Exception e){}

    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        stopCheckGetWeight();
        weightTextView.setText(String.valueOf("--"));
        //try {scaleModule.scalesProcessEnable(false);}catch (Exception e){}
    }

    @Override
    public void onDetach() {
        /*try {scaleModule.scalesProcessEnable(false);}catch (Exception e){}
        baseReceiver.unregister();
        if (scaleModule != null)
            scaleModule.dettach();*/
        if (webScalesClient != null){
            webScalesClient.destroy();
            webScalesClient = null;
        }
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ScalesView.REQUEST_DEVICE:
                    /*String ssid = data.getStringExtra(SearchDialog.ARG_DEVICE);
                    if (scaleModule != null)
                        scaleModule.dettach();
                    createScalesModule(ssid);*/
                    break;
                case ScalesView.REQUEST_BROKEN:
                    /*if (scaleModule != null)
                        scaleModule.dettach();*/
                    //ModuleWiFi.getInstance().dettach();
                    //openSearchDialog("");
                    break;
                default:
            }
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.buttonSettings) {
            if (!ActivityProperties.isActive())
                startActivity(new Intent(getActivity(), ActivityProperties.class));
        }else if (i == R.id.buttonSearch){
            openSearchDialog("");
        }
    }

    private void createScalesModule(String ssid){
        webScalesClient = new WebScalesClient(getActivity().getApplicationContext());
        try {
            /*ModuleWiFi.create(getActivity(), version, ssid, new InterfaceCallbackScales() {
                *//** Сообщение о результате соединения.
                 * @param module Модуль с которым соединились. *//*
                @Override
                public void onCreate(Module module) {
                    if (module instanceof ModuleWiFi){
                        scaleModule = (ModuleWiFi) module;
                        onListener.onScaleModuleCallback(module);
                        //onListener.onUpdateSettings(settings);
                        scaleModule.scalesProcessEnable(true);

                    }
                    //settings.write(R.string.KEY_ADDRESS, module.getAddressBluetoothDevice());
                }
            });*/
        }catch (Exception e) {
            getActivity().sendBroadcast(new Intent(InterfaceModule.ACTION_CONNECT_ERROR).putExtra(InterfaceModule.EXTRA_MESSAGE, e.getMessage()));
            //openSearchDialog(e.getMessage());
        }
    }

    @Override
    public void openSearchDialog(String msg) {
        /*DialogFragment fragment = SearchDialogWiFi.newInstance(msg);
        fragment.setTargetFragment(this, ScalesView.REQUEST_DEVICE);
        fragment.show(getFragmentManager(), fragment.getClass().getName());*/
    }

    public void openSearchProgress(String msg){
        /*DialogFragment fragment = SearchProgressFragment.newInstance(msg);
        fragment.setTargetFragment(this, ScalesView.REQUEST_ATTACH);
        fragment.show(getFragmentManager(), fragment.getClass().getName());*/
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupWeightView() {

        /*if (scaleModule != null){
           progressBarWeight.setMax(scaleModule.getMarginTenzo());
            progressBarWeight.setSecondaryProgress(scaleModule.getLimitTenzo());

        }*/
        progressBarStable.setMax(Module.STABLE_NUM_MAX);
        progressBarStable.setProgress(0);

        dProgressWeight = getResources().getDrawable(R.drawable.progress_weight);
        dWeightDanger = getResources().getDrawable(R.drawable.progress_weight_danger);

        SimpleGestureFilter.SimpleGestureListener weightViewGestureListener = new SimpleGestureFilter.SimpleGestureListener() {
            @Override
            public void onSwipe(int direction) {

                switch (direction) {
                    case SimpleGestureFilter.SWIPE_RIGHT:
                        //case SimpleGestureFilter.SWIPE_LEFT:
                        weightViewIsSwipe = true;
                        getActivity().sendBroadcast(new Intent(InterfaceModule.ACTION_WEIGHT_STABLE));
                        break;
                    case SimpleGestureFilter.SWIPE_DOWN:
                        openSearchDialog("Выбор устройства для соединения");
                        break;
                    default:
                }
            }

            @Override
            public void onDoubleTap() {
                progressBarStable.setProgress(0);
                vibrator.vibrate(100);
                new ZeroThread(getActivity()).start();
            }

            @Override
            public void onLongClick() {
                //onInteractionListener.onSaveWeight(moduleWeight);
                ObjectScales objectScales = new ObjectScales();
                objectScales.setWeight(moduleWeight);
                objectScales.setFlagStab(true);
                getActivity().sendBroadcast(new Intent(InterfaceModule.ACTION_WEIGHT_STABLE).putExtra(InterfaceModule.EXTRA_SCALES, objectScales));
            }


        };

        detectorWeightView = new SimpleGestureFilter(getActivity(), weightViewGestureListener);
        detectorWeightView.setSwipeMinVelocity(50);
        weightTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                detectorWeightView.setSwipeMaxDistance(v.getMeasuredWidth());
                detectorWeightView.setSwipeMinDistance(detectorWeightView.getSwipeMaxDistance() / 3);
                detectorWeightView.onTouchEvent(event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        touchWeightView = true;
                        //vibrator.vibrate(5);
                        //int progress = (int) (event.getX() / (detectorWeightView.getSwipeMaxDistance() / progressBarStable.getMax()));
                        //progressBarStable.setProgress(progress);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        //progressBarStable.setProgress(0);
                        touchWeightView = false;
                        break;
                    default:
                }
                return true;
            }
        });
        /*weightTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onInteractionListener.onSaveWeight(moduleWeight);
                return true;
            }
        });*/
    }

    /**
     * Обработка обнуления весов.
     */
    private class ZeroThread extends Thread {
        private final ProgressDialog dialog;

        ZeroThread(Context context) {
            // Создаём новый поток
            super(getString(R.string.Zeroing));
            dialog = new ProgressDialog(getActivity());
            dialog.setCancelable(false);
            dialog.setIndeterminate(false);
            dialog.show();
            dialog.setContentView(R.layout.zeroing_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            TextView tv1 = dialog.findViewById(R.id.textView1);
            tv1.setText(R.string.Zeroing);
        }

        @Override
        public void run() {
            Commands.TP.getParam();
            //scaleModule.setOffsetScale();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    class BaseReceiver extends BroadcastReceiver {
        private final Context mContext;
        private SpannableStringBuilder w;
        private Rect bounds;
        private ProgressDialog dialogSearch;
        private final IntentFilter intentFilter;
        protected boolean isRegistered;

        BaseReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(InterfaceModule.ACTION_ATTACH_START);
            intentFilter.addAction(InterfaceModule.ACTION_SCALES_RESULT);
            intentFilter.addAction(InterfaceModule.ACTION_WEIGHT_STABLE);
            intentFilter.addAction(InterfaceModule.ACTION_LOAD_OK);
            intentFilter.addAction(InterfaceModule.ACTION_RECONNECT_OK);
            intentFilter.addAction(InterfaceModule.ACTION_CONNECT_ERROR);
        }

        @Override
        public void onReceive(Context context, Intent intent) { //обработчик Bluetooth
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case InterfaceModule.ACTION_LOAD_OK:
                        setupWeightView();
                        //layoutSearch.setVisibility(View.GONE);

                        break;
                    case InterfaceModule.ACTION_RECONNECT_OK:
                        /*scalesFragment.loadModule(scaleModule);
                        fragmentManager.beginTransaction().show(scalesFragment).commit();*/
                        break;
                    case InterfaceModule.ACTION_ATTACH_START:
                        String msg = intent.getStringExtra(InterfaceModule.EXTRA_DEVICE_NAME);
                        openSearchProgress(msg);
                        break;
                    case InterfaceModule.ACTION_CONNECT_ERROR:
                        SpannableStringBuilder text = new SpannableStringBuilder("нет соединения");
                        text.setSpan(new TextAppearanceSpan(getActivity(), R.style.SpanTextKgMini),0,text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        weightTextView.setText(text, TextView.BufferType.SPANNABLE);
                        String message = intent.getStringExtra(InterfaceModule.EXTRA_MESSAGE);
                        if (message == null)
                            message = "";
                        openSearchDialog(message);
                        break;

                    case InterfaceModule.ACTION_SCALES_RESULT:
                        ObjectScales obj = (ObjectScales) intent.getSerializableExtra(InterfaceModule.EXTRA_SCALES);
                        if (obj == null)
                            return;
                        moduleWeight = obj.getWeight();
                        final String textWeight = String.valueOf(moduleWeight);
                        /* Обновляем прогресс стабилизации веса. */
                        progressBarStable.setProgress(obj.getStableNum());
                        //handler.obtainMessage(Action.UPDATE_PROGRESS.ordinal(), obj.getStableNum(), 0).sendToTarget();
                        switch (obj.getResultWeight()) {
                            case WEIGHT_NORMAL:
                                w = new SpannableStringBuilder(textWeight);
                                w.setSpan(new ForegroundColorSpan(Color.WHITE), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                                w.append(textKg);
                                progressBarWeight.setProgress(obj.getTenzoSensor());
                                bounds = progressBarWeight.getProgressDrawable().getBounds();
                                progressBarWeight.setProgressDrawable(dProgressWeight);
                                progressBarWeight.getProgressDrawable().setBounds(bounds);
                                break;
                            case WEIGHT_LIMIT:
                                w = new SpannableStringBuilder(textWeight);
                                w.setSpan(new ForegroundColorSpan(Color.RED), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                                w.append(textKg);
                                progressBarWeight.setProgress(obj.getTenzoSensor());
                                bounds = progressBarWeight.getProgressDrawable().getBounds();
                                progressBarWeight.setProgressDrawable(dWeightDanger);
                                progressBarWeight.getProgressDrawable().setBounds(bounds);
                                break;
                            case WEIGHT_MARGIN:
                                w = new SpannableStringBuilder(String.valueOf(moduleWeight));
                                w.setSpan(new ForegroundColorSpan(Color.RED), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                                w.append(textKg);
                                progressBarWeight.setProgress(obj.getTenzoSensor());
                                vibrator.vibrate(100);
                                break;
                            case WEIGHT_ERROR:
                                w = new SpannableStringBuilder("- - -");
                                w.setSpan(new ForegroundColorSpan(Color.RED), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                                moduleWeight = 0;
                                progressBarWeight.setProgress(0);
                                break;
                            default:
                        }
                        weightTextView.setText(w, TextView.BufferType.SPANNABLE);
                        textViewTemperature.setText(obj.getTemperature() + "°C");
                        textViewBattery.setText(obj.getBattery() + "%");
                        textViewBattery.setTextColor(Color.WHITE);
                        if (obj.getBattery() > 90) {
                            textViewBattery.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_full, 0, 0, 0);
                        } else if (obj.getBattery() > 80){
                            textViewBattery.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_90, 0, 0, 0);
                        } else if (obj.getBattery() > 60){
                            textViewBattery.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_80, 0, 0, 0);
                        } else if (obj.getBattery() > 50){
                            textViewBattery.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_60, 0, 0, 0);
                        } else if (obj.getBattery() > 30){
                            textViewBattery.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_50, 0, 0, 0);
                        } else if (obj.getBattery() > 20){
                            textViewBattery.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_30, 0, 0, 0);
                        } else if (obj.getBattery() >= 0) {
                            //textViewBattery.setText(obj.getBattery() + "%");
                            textViewBattery.setTextColor(Color.RED);
                            textViewBattery.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_20, 0, 0, 0);
                        }else {
                            textViewBattery.setText("нет данных!!!");
                            textViewBattery.setTextColor(Color.BLUE);
                            textViewBattery.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_20, 0, 0, 0);
                        }
                        //    }
                        break;
                    /*case InterfaceModule.ACTION_WEIGHT_STABLE:
                        isStable = true;
                        handler.obtainMessage(Action.STORE_WEIGHTING.ordinal(), moduleWeight, 0).sendToTarget();                 //сохраняем стабильный вес
                        break;*/
                    default:
                }
            }
        }

        public void register() {
            isRegistered = true;
            mContext.registerReceiver(this, intentFilter);
        }

        public void unregister() {
            if (isRegistered) {
                mContext.unregisterReceiver(this);  // edited
                isRegistered = false;
            }
        }
    }

    private void startCheckGetWeight() {
        handler.postDelayed(checkGetWeightRunable, 5000);
    }

    private void stopCheckGetWeight() {
        handler.removeCallbacks(checkGetWeightRunable);
    }

    private final Runnable checkGetWeightRunable = new Runnable() {
        @Override
        public void run() {
            weightTextView.setText(String.valueOf("--"));
            Commands.WT.getParam();
            //handler.postDelayed(checkGetWeightRunable, 5000);
            startCheckGetWeight();
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSocket(Client.MessageEventSocket eventSocket){
        weightTextView.setText(String.valueOf(eventSocket.text));
        if(eventSocket.message == Client.MessageEventSocket.Message.CONNECT){
            Commands.WT.getParam();
            //handler.postAtTime(checkGetWeightRunable, 5000);
            startCheckGetWeight();
        }
        //Log.i("Event", eventSocket.text);
    }

    private static String setPrecision(double amt, int precision){
        return String.format("%." + precision + "f", amt);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Commands.ClassWT event){
        stopCheckGetWeight();
        //handler.removeCallbacks(checkGetWeightRunable);
        //moduleWeight = event.weight;
        //final String textWeight = setPrecision(moduleWeight, 3);
        /* Обновляем прогресс стабилизации веса. */
        progressBarStable.setProgress(Module.STABLE_NUM_MAX - event.stable);                          //todo
        weightTextView.setText(event.weight);
        try {Thread.sleep(10);} catch (InterruptedException e) {}
        Commands.WT.getParam();
        startCheckGetWeight();
        //handler.postDelayed(checkGetWeightRunable, 5000);
    }


}
