package com.kostya.webscaleslibrary.module;

import android.content.Context;
import com.kostya.webscaleslibrary.R;
import com.kostya.webscaleslibrary.preferences.Settings;

public abstract class Module implements WifiBaseManager.OnWifiBaseManagerListener{
    final Context mContext;
    WifiBaseManager wifiBaseManager;
    final Settings settings;
    public static String SSID;
    public static String HOST;
    /** Количество стабильных показаний веса для авто сохранения. */
    public static final int STABLE_NUM_MAX = 10;
    public static final String SETTINGS = Module.class.getName() + ".SETTINGS"; //

    Module(Context context) {
        mContext = context;
        settings = new Settings(mContext);
        SSID = settings.read( mContext.getString(R.string.KEY_SSID) , "KONST");
        wifiBaseManager = new WifiBaseManager(mContext,this);
    }

    public int getVersionNum() {
        return 1;
    }

    public String getTemperature() {
        return "test";
    }

    public long getCoefficientA() {
        return 0;
    }

    public String getWeightMax() {
        return null;
    }

    public String getTimeOff() {
        return null;
    }

    /** Константы результата взвешивания. */
    public enum ResultWeight {
        /** Значение веса неправильное. */
        WEIGHT_ERROR,
        /** Значение веса в диапазоне весового модуля. */
        WEIGHT_NORMAL,
        /** Значение веса в диапазоне лилита взвешивания. */
        WEIGHT_LIMIT,
        /** Значение веса в диапазоне перегрузки. */
        WEIGHT_MARGIN
    }

}
