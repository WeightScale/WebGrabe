package com.kostya.webgrabe;

//import android.app.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.kostya.webgrabe.provider.DaoSession;
import com.kostya.webgrabe.provider.Invoice;
import com.kostya.webgrabe.provider.InvoiceDao;
import com.kostya.webgrabe.provider.Weighing;
import com.kostya.webgrabe.provider.WeighingDao;
import com.kostya.webgrabe.scales.InterfaceModule;
import com.kostya.webscaleslibrary.module.ObjectScales;
import com.kostya.webgrabe.settings.ActivityPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 *
 */
public class FragmentInvoice extends Fragment implements View.OnClickListener {
    KeyguardManager.KeyguardLock keyguardLock;
    PowerManager.WakeLock wakeLock;
    private Vibrator vibrator; //вибратор
    private SoundPool soundPool;
    private SimpleCursorAdapter adapterWeightingList;
    DaoSession daoSession;
    //private InvoiceTable invoiceTable;
    //private WeighingTable weighingTable;
    //private ContentValues values = new ContentValues();
    private Invoice values = new Invoice();
    private EditText nameInvoice, loadingInvoice, totalInvoice;
    private TextView dateInvoice,textViewStage, textViewBatch;
    private Button buttonClosed;
    private ListView listInvoice;
    private BaseReceiver baseReceiver; //приёмник намерений
    //private Settings settings;
    private long entryID;
    private int shutterSound, shutterSound3;
    private int deltaStab, capture;
    private double grab, grab_virtual,auto,loading;
    private static final int REQUEST_CLOSED_INVOICE = 1;
    private static final String ARG_DATE = "date";
    private static final String ARG_TIME = "time";
    private static final String ARG_ID = "_id";
    private STAGE stage = STAGE.START;
    private String _id = null;
    private static final String TAG = FragmentInvoice.class.getName();
    private boolean stable;
    private boolean switch_loading, switch_closing;

    private OnFragmentInvoiceListener mListener;

    enum STAGE{
        START("Старт"),
        LOADING("Загрузка"),
        STABLE("Стабилизация"),
        UNLOADING("Разгрузка"),
        BATCHING("Дозирование"),
        UPLOADED("Готово!!!");

        final String name;

        STAGE(String s) {name = s;}

        CharSequence getName() {return name;}
    }

    /**
     * @param date Parameter 1.
     * @param _id Parameter 2.
     * @return A new instance of fragment InvoiceFragment.
     */
    public static FragmentInvoice newInstance(String date, String time, String _id) {
        FragmentInvoice fragment = new FragmentInvoice();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        args.putString(ARG_TIME, time);
        if(_id != null){
            args.putString(ARG_ID, _id);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
        wakeLock.acquire();*/

        /*WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.screenBrightness = 1.0f;
        getActivity().getWindow().setAttributes(lp);*/

        Objects.requireNonNull(getActivity()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*KeyguardManager keyguardManager = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        keyguardLock = keyguardManager.newKeyguardLock("TAG");
        keyguardLock.disableKeyguard();*/

        if (getArguments() != null) {
            //values.put(InvoiceTable.KEY_DATE_CREATE, getArguments().getString(ARG_DATE));
            //values.put(InvoiceTable.KEY_TIME_CREATE, getArguments().getString(ARG_TIME));
            values.setDateCreate(getArguments().getString(ARG_DATE));
            values.setTimeCreate(getArguments().getString(ARG_TIME));
            _id = getArguments().getString(ARG_ID);
        }else {
            //values.put(InvoiceTable.KEY_DATE_CREATE, new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date()));
            //values.put(InvoiceTable.KEY_TIME_CREATE, new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
            //values.put(InvoiceDao.Properties.DateCreate.columnName, new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date()));
            //values.put(InvoiceDao.Properties.TimeCreate.columnName, new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
            values.setDateCreate(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date()));
            values.setTimeCreate(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
        }
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        //settings = new Settings(getActivity()/*, ActivityTest.SETTINGS*/);

        //invoiceTable = new InvoiceTable(getActivity());
        daoSession = ((Main)getActivity().getApplication()).getDaoSession();
        if (_id == null){
            //entryID = Integer.valueOf(invoiceTable.insertNewEntry(values).getLastPathSegment());
            entryID = daoSession.getInvoiceDao().insert(values);
        }else {
            entryID = Long.valueOf(_id);
        }

        try {
            //values = invoiceTable.getValuesItem(entryID);
            values = daoSession.getInvoiceDao().loadByRowId(entryID);
            //values = Invoice.getValuesItem(daoSession.getInvoiceDao(), entryID);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        //weighingTable = new WeighingTable(getActivity());

        baseReceiver = new BaseReceiver(getActivity());
        baseReceiver.register();

        soundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
        shutterSound = soundPool.load(getActivity(), R.raw.busone, 0);
        shutterSound3 = soundPool.load(getActivity(), R.raw.bus, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        //updateSettings(settings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invoice, null);

        textViewStage = view.findViewById(R.id.textViewStage);
        textViewBatch = view.findViewById(R.id.textViewBatch);

        dateInvoice = view.findViewById(R.id.invoiceDate);
        //dateInvoice.setText(values.getAsString(InvoiceTable.KEY_DATE_CREATE));
        //dateInvoice.append(' ' +values.getAsString(InvoiceTable.KEY_TIME_CREATE));
        dateInvoice.setText(values.getDateCreate());
        dateInvoice.append(' ' +values.getTimeCreate());

        nameInvoice = view.findViewById(R.id.invoiceName);
        //nameInvoice.setText(values.getAsString(InvoiceTable.KEY_NAME_AUTO));
        nameInvoice.setText(values.getNameAuto());
        nameInvoice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().isEmpty()) {
                    //values.put(InvoiceTable.KEY_NAME_AUTO, editable.toString());
                    //invoiceTable.updateEntry(entryID, values);
                    values.setNameAuto(editable.toString());
                    values.update();
                }
            }
        });

        loadingInvoice = view.findViewById(R.id.invoiceLoading);
        setLoadingDefault();
        loadingInvoice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /*char c = charSequence.charAt(i);
                SpannableStringBuilder w = new SpannableStringBuilder(String.valueOf(loading));
                w.append(c);
                Spannable inputStr = (Spannable)charSequence;*/
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().isEmpty()) {
                    loading = Double.valueOf(editable.toString());
                }
            }
        });

        listInvoice = view.findViewById(R.id.invoiceList);
        updateListWeight();
        totalInvoice = view.findViewById(R.id.invoiceTotal);
        //totalInvoice.setText(values.getAsString(InvoiceTable.KEY_TOTAL_WEIGHT));
        totalInvoice.setText(String.valueOf(values.getTotalWeight()));

        buttonClosed = view.findViewById(R.id.buttonCloseInvoice);
        buttonClosed.setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInvoiceListener) {
            mListener = (OnFragmentInvoiceListener) activity;
            mListener.onEnableStable(false);
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInvoiceListener) {
            mListener = (OnFragmentInvoiceListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //updateSettings(settings);
        baseReceiver.unregister();
        mListener = null;
        //soundPool.stop(shutterSound);
        soundPool.unload(shutterSound);
        soundPool.unload(shutterSound3);
        soundPool.release();
        soundPool = null;
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //wakeLock.release();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.buttonCloseInvoice){
            onCloseForDialog(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CLOSED_INVOICE:
                    CustomDialogFragment.BUTTON button = CustomDialogFragment.BUTTON.values()[data.getIntExtra(CustomDialogFragment.ARG_BUTTON, CustomDialogFragment.BUTTON.CANCEL.ordinal())];
                        switch (button){
                            case OK:
                                //values.put(InvoiceTable.KEY_IS_READY, InvoiceTable.READY);
                                values.setIsReady(true);
                            break;
                            default:{}
                        }
                        onClose();
                    break;
                default:
            }
        }
    }

    /*@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
            Log.i("", "Dispath event power");
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
            return true;
        }

        return super.dispatchKeyEvent(event);
    }*/

    /** Закрыть накладную. */
    private void onClose() {
        //invoiceTable.updateEntry(entryID, values);
        values.update();
        ((ActivityMain)Objects.requireNonNull(getActivity())).closedFragmentInvoice();
    }

    /** Кнопка закрыть накладную. */
    private void onCloseForDialog(boolean flag) {
        if (flag){
            DialogFragment fragment = CustomDialogFragment.newInstance(CustomDialogFragment.DIALOG.ALERT_DIALOG2);
            fragment.setTargetFragment(this, REQUEST_CLOSED_INVOICE);
            fragment.show(getFragmentManager(), fragment.getClass().getName());
        }else
            onClose();
    }

    private void setLoadingDefault(){
        for(ActivityPreferences.KEY key : ActivityPreferences.KEY.values()){
            switch (key){
                case SWITCH_LOADING:
                    //switch_loading = settings.read(key.getResId(), false);
                    break;
                case WEIGHT_LOADING:
                    if (switch_loading)
                        //loading = settings.read(key.getResId(), 1000);
                    break;
                default:
            }
        }

        loadingInvoice.setText(String.valueOf(loading));
    }

    public void onShutter() {
        soundPool.play(shutterSound, 1.0f, 1.0f, 0, 0, 2);
    }

    /*public void updateSettings(Settings settings){
        if (settings == null)
            return;
        for(ActivityPreferences.KEY key : ActivityPreferences.KEY.values()){
            switch (key){
                case DELTA_STAB:
                    deltaStab = settings.read(key.getResId(), 20);
                    //Globals.getInstance().getScaleModule().setDeltaStab(deltaStab);       //todo
                break;
                case CAPTURE:
                    capture = settings.read(key.getResId(), 100);
                break;
                case CLOSING_INVOICE:
                    switch_closing = settings.read(key.getResId(), false);
                break;
                default:
            }
        }
    }*/

    private void removeRowWeight(double weight, long id) {
        vibrator.vibrate(100);
        auto-= weight;
        //values.put(InvoiceTable.KEY_TOTAL_WEIGHT, auto);
        //invoiceTable.updateEntry(entryID, values);
        values.setTotalWeight(auto);
        values.update();
        updateTotal(auto);
        //weighingTable.removeEntry(id);
        daoSession.getWeighingDao().loadByRowId(id).delete();
    }

    private void addRowWeight(double weight){
        vibrator.vibrate(100);
        auto += weight;
        //values.put(InvoiceTable.KEY_TOTAL_WEIGHT, auto);
        //weighingTable.insertNewEntry(entryID, weight);
        //invoiceTable.updateEntry(entryID, values);
        values.setTotalWeight(auto);
        values.update();
        Weighing weighing = new Weighing();
        weighing.setIdInvoice(entryID);
        weighing.setWeight(weight);
        daoSession.getWeighingDao().insert(weighing);
        updateTotal(auto);
    }

    private void updateTotal(double weight){
        SpannableStringBuilder w = new SpannableStringBuilder(String.valueOf(weight));
        w.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.background2)), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        SpannableStringBuilder textKg = new SpannableStringBuilder(getResources().getString(R.string.scales_kg));
        textKg.setSpan(new TextAppearanceSpan(getActivity(), R.style.SpanTextKgMiniInvoice),0,textKg.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        w.append(textKg);
        totalInvoice.setText(w, TextView.BufferType.SPANNABLE);
    }

    /** Обновляем данные листа загрузок. */
    private void updateListWeight() {
        //Cursor cursor = weighingTable.getEntryInvoice(entryID);
        Cursor cursor = daoSession.getWeighingDao().queryBuilder().where(WeighingDao.Properties.IdInvoice.eq(entryID)).buildCursor().query();
        List<Weighing> weighings = daoSession.getWeighingDao().queryBuilder().where(WeighingDao.Properties.IdInvoice.eq(entryID)).build().list();
        if (cursor == null) {return;}

        int[] to = {R.id.bottomText, R.id.topText};

        //adapterWeightingList = new SimpleCursorAdapter(getActivity(), R.layout.list_item_weight, cursor, WeighingTable.COLUMN_FOR_INVOICE, to, CursorAdapter.FLAG_AUTO_REQUERY);
        adapterWeightingList = new SimpleCursorAdapter(getActivity(), R.layout.list_item_weight, cursor, new String[]{WeighingDao.Properties.DateTimeCreate.columnName,WeighingDao.Properties.Weight.columnName}, to, CursorAdapter.FLAG_AUTO_REQUERY);
        //namesAdapter = new MyCursorAdapter(this, R.layout.item_check, cursor, columns, to);
        //adapterWeightingList.setViewBinder(new ListWeightViewBinder());
        listInvoice.setItemsCanFocus(false);
        listInvoice.setAdapter(adapterWeightingList);
        listInvoice.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, final long l) {
                TextView textView = view.findViewById(R.id.topText);
                final int weight = Integer.valueOf(textView.getText().toString());
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.CustomAlertDialogInvoice));
                builder.setCancelable(false)
                        .setTitle("Сообщение")
                        .setMessage("ВЫ ХОТИТЕ УДАЛИТЬ ЗАПИСЬ?")
                        .setIcon(R.drawable.ic_notification)
                        .setPositiveButton("ДА", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                removeRowWeight(weight, (int) l);
                            }
                        })
                        .setNegativeButton("НЕТ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                });
                builder.create().show();
                return true;
            }
        });
    }

    /** Процесс обработки стадий загрузки.
     * @param objectScales Обьект весой.
     */
    private void doProcess(ObjectScales objectScales){
        switch (stage){
            case LOADING:/* Стадия загрузки ковша. */
                if (objectScales.getWeight() >= capture){
                    setStage(STAGE.STABLE);
                    mListener.onEnableStable(true);
                }
            break;
            case UNLOADING:/* Стадия разгруски ковша. */
                if (objectScales.getWeight()< grab || objectScales.getWeight() > grab){
                    /* Разница то что высыпалось. */
                    double temp = objectScales.getWeight() - grab;
                    /* Добавляем разницу в виртуальный кузов. */
                    grab_virtual -= temp;
                    /* Отнимаем разницу из ковша. */
                    grab += temp;
                }
                if (stable && objectScales.getWeight() > 0){
                    stable = false;
                    addRowWeight(grab_virtual);
                    grab_virtual = 0;
                }else if(objectScales.getWeight() <= 0){
                    addRowWeight(grab_virtual);
                    setStage(STAGE.LOADING);
                    mListener.onEnableStable(false);
                }
            break;
            case BATCHING:/* Стадия дозирования. */
                if (objectScales.getWeight() < grab || objectScales.getWeight() > grab){
                    /* Разница то что высыпалось. */
                    double temp = objectScales.getWeight() - grab;
                    /* Добавляем разницу в виртуальный кузов. */
                    grab_virtual -= temp;
                    /* Отнимаем разницу из ковша. */
                    grab += temp;
                }
                if(auto + grab_virtual >= loading){
                    addRowWeight(grab_virtual);
                    setStage(STAGE.UPLOADED);
                    mListener.onEnableStable(false);
                    soundPool.stop(shutterSound3);
                    soundPool.play(shutterSound3, 1.0f, 1.0f, 0, 10, 0.4f);
                    //vibrator.vibrate(1000);
                    return;
                }
                double b = loading - auto -grab_virtual;
                buttonClosed.setText(String.valueOf(b));
                //soundPool.setLoop(shutterSound3, 2);
                vibrator.vibrate(100);
                //soundPool.play(shutterSound, 1f, 1f, 0, 0, 0.5f);
            break;
            case UPLOADED:/* Загружено. */
                //soundPool.play(shutterSound, 1f, 1f, 0, 0, 1);
                if (switch_closing){
                    //values.put(InvoiceTable.KEY_IS_READY, InvoiceTable.READY);
                    values.setIsReady(true);
                    onCloseForDialog(false);
                }
                vibrator.vibrate(50);
            break;
            case STABLE:/* Стадия стабилизации ковша после загрузки. */
                if (stable){
                    /* Если вес больше или равно автозахвата. */
                    if (objectScales.getWeight() >= capture){
                        /* Сохранчем вес коша. */
                        soundPool.play(shutterSound, 1.0f, 1.0f, 0, 0, 2);
                        vibrator.vibrate(200);
                        grab = objectScales.getWeight();
                        grab_virtual = 0;
                        /* Если норма загрузки указана и вес авто и текущего ковша превышает норму загрузки.*/
                        if (loading != 0 && (grab + auto) > loading){
                            /* Стадия дозирования. */
                            setStage(STAGE.BATCHING);
                            soundPool.play(shutterSound3, 1.0f, 1.0f, 0, 20, 1);
                        }else {
                            /* Стадия разгруски. */
                            setStage(STAGE.UNLOADING);
                        }
                        /* Ложное срабатывание обратно в загрузку. */
                    }else {
                        /* Стадия загрузки. */
                        setStage(STAGE.LOADING);
                    }
                    stable = false;
                }
            break;
            /* Определяем текущую стадию. */
            default:{
                if (objectScales.getWeight() <= 0){
                    setStage(STAGE.LOADING);
                    mListener.onEnableStable(false);
                }else if (objectScales.getWeight() >= capture){
                    setStage(STAGE.STABLE);
                    mListener.onEnableStable(true);
                }
            }
        }
    }

    private void setStage(STAGE stage){
        textViewStage.setText(stage.getName());
        this.stage = stage;
    }

    /** Интерфейс обратного вызова. */
    public interface OnFragmentInvoiceListener {
        //void onInvoiceClosePressedButton(boolean flag);
        void onEnableStable(boolean enable);
    }

    class BaseReceiver extends BroadcastReceiver {
        private final Context mContext;
        private SpannableStringBuilder w;
        private Rect bounds;
        private ProgressDialog dialogSearch;
        private final IntentFilter intentFilter;
        boolean isRegistered;

        BaseReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(InterfaceModule.ACTION_WEIGHT_STABLE);
            intentFilter.addAction(InterfaceModule.ACTION_SCALES_RESULT);
        }

        @Override
        public void onReceive(Context context, Intent intent) { //обработчик Bluetooth
            String action = intent.getAction();
            if (action != null) {
                ObjectScales obj = (ObjectScales) intent.getSerializableExtra(InterfaceModule.EXTRA_SCALES);
                if (obj == null)
                    return;
                switch (action) {
                    case InterfaceModule.ACTION_SCALES_RESULT:
                        break;
                    case InterfaceModule.ACTION_WEIGHT_STABLE:
                        stable = true;
                        break;
                    default:
                        return;
                }
                doProcess(obj);
            }
        }

        void register() {
            isRegistered = true;
            mContext.registerReceiver(this, intentFilter);
        }

        void unregister() {
            if (isRegistered) {
                mContext.unregisterReceiver(this);  // edited
                isRegistered = false;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(com.kostya.webscaleslibrary.module.Commands.ClassSWT event){
        stable = true;
        ObjectScales obj = new ObjectScales();
        obj.setWeight(event.weight);
        doProcess(obj);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(com.kostya.webscaleslibrary.module.Commands.ClassWT event){
        ObjectScales obj = new ObjectScales();
        obj.setWeight(Double.valueOf(event.weight));
        doProcess(obj);
    }
}
