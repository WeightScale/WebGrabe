package com.kostya.webgrabe;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;


import com.kostya.webscaleslibrary.module.Module;

import java.io.File;

/**
 * @author Kostya
 */
public class Globals {
    private static Globals instance = new Globals();
    public static File pathLocalForms;
    /** Папка для хранения локальных данных программы. */
    private static final String FOLDER_LOCAL_FORMS = "forms";
    private Module scaleModule;
    /** Настройки для весов. */
    private com.kostya.webgrabe.Preferences preferencesScale;
    /** Настройки для обновления весов. */
    private PackageInfo packageInfo;
    /** Версия пограммы весового модуля. */
    private final int microSoftware = 4;
    /** Шаг измерения (округление). */
    private int stepMeasuring;
    /** Шаг захвата (округление). */
    private int autoCapture;
    /** Время задержки для авто захвата после которого начинается захват в секундах. */
    private int timeDelayDetectCapture;
    /** Минимальное значение авто захвата веса килограммы. */
    private final int defaultMinAutoCapture = 20;
    /** Процент заряда батареи (0-100%). */
    private int battery;
    private final String deviceId = "";
    /** Флаг есть соединение. */
    //private boolean isScaleConnect;
    private static final String TAG = Globals.class.getName();

    public int getBattery() { return battery; }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public Module getScaleModule() {
        return scaleModule;
    }

    public void setScaleModule(Module scaleModule) {
        this.scaleModule = scaleModule;
    }

    public void setStepMeasuring(int stepMeasuring) {
        this.stepMeasuring = stepMeasuring;
    }

    public int getStepMeasuring() {
        return stepMeasuring;
    }

    public int getAutoCapture() {
        return autoCapture;
    }

    public void setAutoCapture(int autoCapture) {
        this.autoCapture = autoCapture;
    }

    public com.kostya.webgrabe.Preferences getPreferencesScale() {
        return preferencesScale;
    }

    public String readPreferencesScale(String key, String defaultValue){
        return preferencesScale.read(key, defaultValue);
    }

    /*public boolean isScaleConnect() {
        return isScaleConnect;
    }*/

    /*public void setScaleConnect(boolean scaleConnect) {
        isScaleConnect = scaleConnect;
    }*/

    public int getDefaultMinAutoCapture() {
        return defaultMinAutoCapture;
    }

    public int getMicroSoftware() { return microSoftware; }

    public int getTimeDelayDetectCapture() { return timeDelayDetectCapture; }

    public void initialize(Context context) {
        /*TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getDeviceId() != null) {
            deviceId = tm.getDeviceId();
        } else {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }*/
        /*PreferenceManager.setDefaultValues(this, R.xml.preferences, false);*/
        try {
            PackageManager packageManager = context.getPackageManager();
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {  }

        preferencesScale = new com.kostya.webgrabe.Preferences(context.getApplicationContext());//загрузить настройки

        //stepMeasuring = preferencesScale.read(context.getString(R.string.KEY_STEP), context.getResources().getInteger(R.integer.default_step_scale));
        autoCapture = preferencesScale.read(context.getString(R.string.KEY_AUTO_CAPTURE), context.getResources().getInteger(R.integer.default_max_auto_capture));
        //scaleModule.setTimerNull(Preferences.read(getString(R.string.KEY_TIMER_NULL), default_max_time_auto_null));
        //scaleModule.setWeightError(Preferences.read(getString(R.string.KEY_MAX_NULL), default_limit_auto_null));
        timeDelayDetectCapture = context.getResources().getInteger(R.integer.time_delay_detect_capture);
        /* Создаем путь к папке для для хранения файлов с данными формы google disk form. */
        pathLocalForms = new File(context.getFilesDir() + File.separator + FOLDER_LOCAL_FORMS);
        /* Если нет папки тогда создаем. */
        if (!pathLocalForms.exists()) {
            if (!pathLocalForms.mkdirs()) {
                Log.e(TAG, "Путь не созданый: " + pathLocalForms.getPath());
            }
        }
    }

    public static Globals getInstance() { return instance; }

    public static void setInstance(Globals instance) { Globals.instance = instance; }

    public String getDeviceId() {
        return deviceId;
    }
}
