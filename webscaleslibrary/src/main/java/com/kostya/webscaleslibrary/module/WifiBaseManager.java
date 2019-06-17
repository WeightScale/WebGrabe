package com.kostya.webscaleslibrary.module;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;


/**
 * @author Kostya
 */
public class WifiBaseManager {
    private final Context context;
    private ConnectionReceiver connectionReceiver;
    private final BaseReceiver baseReceiver;
    private SupplicantDisconnectReceiver supplicantDisconnectReceiver;
    private final ScanWifiReceiver scanWifiReceiver;
    //private final Internet internet;
    /** Обратный вызов события соединения с сетью. */
    private final OnWifiBaseManagerListener onWifiBaseManagerListener;
    private final WifiManager wifiManager;
    private InetSocketAddress inetSocketAddress;
    /** Порт соединения с сервером. Зарезервирован для весов. */
    //private static final int PORT = 1011;
    private static final int PORT = 80;
    //private String ssid = ""/*, pass = ""*/;
    private static final String TAG = WifiBaseManager.class.getName();
    private static final String PSK = "PSK";
    private static final String WEP = "WEP";
    private static final String OPEN = "Open";

    public interface OnWifiBaseManagerListener{
        /** Событие соединение с конкретной сетью.
         * @param ssid Имя Сети
         * @param ipAddress Адресс сервера.
         */
        void onWiFiConnect(String ssid, InetSocketAddress ipAddress);

        /** Событие рассоединение с конкретной сетью. */
        void onWiFiDisconnect();
    }

    /** Конструктор.
     * @param context Контекст программы.
     * @param listener Слушатель событий соединения.
     */
    WifiBaseManager(Context context, /*String ssid, String key,*/ OnWifiBaseManagerListener listener){
        this.context = context;
        //pass = key;
        onWifiBaseManagerListener = listener;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //internet = new Internet(context);
        scanWifiReceiver = new ScanWifiReceiver(context);
        baseReceiver = new BaseReceiver(context);
        baseReceiver.register();
        //handleWIFI();
    }

    /** Вызывается когда присоединились к конкретной сети. */
    private void onAttachNetwork(){
        /* Регистрируем приемник на disconnect. */
        supplicantDisconnectReceiver = new SupplicantDisconnectReceiver(context);
        supplicantDisconnectReceiver.register();
        try {
            inetSocketAddress = getInetAddressServer(PORT);
            onWifiBaseManagerListener.onWiFiConnect(Module.SSID, inetSocketAddress);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    //public void setSsid(String ssid) {this.ssid = ssid;}

    //public void setPass(String pass) {this.pass = pass;}

    /** Получить TCP адресс сервера в формате InetSocketAddress.
     * @return InetSocketAddress.
     */
    public InetSocketAddress getInetSocketAddress() { return inetSocketAddress; }

    /** Начало подключения к определенной сети wifi. */
    protected void handleWIFI() {
        if (wifiManager.isWifiEnabled()) {
            connectToSpecificNetwork();
        }
    }

    /** Подключение к определенной сети wifi. */
    private void connectToSpecificNetwork() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = Objects.requireNonNull(cm).getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        try {
            /* Проверяем сеть на соединение и имя конкретной сети.
              Если верно то вызываем обратный вызов  и запускаем приемник на событие disconnect.  */
            if (networkInfo.isConnected() && wifiInfo.getSSID().replace("\"", "").equals(Module.SSID)) {
                onAttachNetwork();
                return;
            }else {
                wifiManager.disconnect();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        /* Регистритуем приемник на прием события результат сканирования.*/
        scanWifiReceiver.register();          //todo
        /* Запускаем сканирование сети. */
        wifiManager.startScan();
    }

    private void connectNet(int netId){
        try {connectionReceiver.unregister();} catch (Exception e) {} // do nothing
        connectionReceiver = new ConnectionReceiver(context);
        connectionReceiver.register();
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }

    /** Получаем тип безопасности сети.
     * @param scanResult Результат сканирования wifi.
     * @return Тип WEP, PSK, OPEN
     */
    private String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = {WEP, PSK};
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }
        return OPEN;
    }

    private InetSocketAddress getInetAddressServer(int port) throws IOException {
        int ip = wifiManager.getDhcpInfo().serverAddress;
        byte[] byteAddress = {
                (byte) (ip & 0xff),
                (byte) (ip >> 8 & 0xff),
                (byte) (ip >> 16 & 0xff),
                (byte) (ip >> 24 & 0xff)
        };
        return new InetSocketAddress(InetAddress.getByAddress(byteAddress), port);
    }

    /** Приемник событий связаных со сканированием wifi. */
    private class ScanWifiReceiver extends BroadcastReceiver {
        private final Context mContext;
        private final IntentFilter intentFilter;
        boolean isRegistered;

        ScanWifiReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            /* Получаем результат сканирования сети.*/
            List<ScanResult> scanResultList = Objects.requireNonNull(wifiManager).getScanResults();
            try {
                boolean found = false;
                /* Сравниваем результат с конктетной сетью. */
                for (ScanResult scanResult : scanResultList) {
                    /* Если верно то конкретная сеть есть в сети.*/
                    if (scanResult.SSID.equals(Module.SSID)) {
                        /* Флаг конкретная сеть в сети. */
                        found = true;
                        break; // found don't need continue
                    }
                }
                /* Провероверяем конкретную сеть в сохраненных конфигурациях.*/
                if (found) {
                    boolean isConfigNet = false;
                    WifiConfiguration conf = new WifiConfiguration();
                    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                    for (WifiConfiguration wifiConfiguration : list){
                        try {
                            /* Если конкретная сеть есть в конфигурациях. */
                            if (wifiConfiguration.SSID.replace("\"", "").equals(Module.SSID)){
                                /* сохраняем конфигурацию во временный переменную.*/
                                conf = wifiConfiguration;
                                /* Флаг конкретная сеть есть в конфигурациях.*/
                                isConfigNet = true;
                                break;
                            }
                        }catch (Exception e){
                            /* Значит конфигурация сети дает исключение. Удаляем конфигурацию. */
                            wifiManager.removeNetwork(wifiConfiguration.networkId);
                            wifiManager.saveConfiguration();
                        }
                    }
                    /* Удаляем регистрацию приемника. */
                    try {connectionReceiver.unregister();} catch (Exception e) {} // do nothing
                    /* Регестрируем приемник заново. */
                    connectionReceiver = new ConnectionReceiver(context);
                    connectionReceiver.register();
                    /* Если есть конфигурация тогда соеденяемся. */
                    if(isConfigNet){
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(conf.networkId, true);
                        wifiManager.reconnect();
                    }
                    /* Удаляем регистрацию приемника. */
                    unregister();
                    return; /* Выходим после реконекта. */
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Objects.requireNonNull(wifiManager).startScan();
                }
            }, 5000);
            //Todo сделать сообщение что нет соединения с сетью
        }

        void register() {
            isRegistered = true;
            context.registerReceiver(this, intentFilter);
        }

        void unregister() {
            if (isRegistered) {
                mContext.unregisterReceiver(this);  // edited
                isRegistered = false;
            }
        }
    }

    /** Приемник событий связаных с соединением wifi.  */
    private class ConnectionReceiver extends BroadcastReceiver {
        private final Context mContext;
        private final IntentFilter intentFilter;
        boolean isRegistered;

        ConnectionReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = Objects.requireNonNull(cm).getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (networkInfo.isConnected()){
                //SupplicantState state = intent.getParcelableExtra(ConnectivityManager.);
                WifiInfo wifiInfo = Objects.requireNonNull(wifiManager).getConnectionInfo();
                if (wifiInfo.getSSID().replace("\"", "").equals(Module.SSID)){
                    /*Если верно удаляем приемник сообщений.*/
                    unregister();
                    /*Посылаем событие соединение.*/
                    onAttachNetwork();
                }else {
                    /* Удаляем приемник сообщений. */
                    unregister();
                    //wifiManager.disconnect();
                    /*Запускаем приемник на прием события результат сканирования.*/
                    scanWifiReceiver.register();          //todo
                    /*Запускаем сканирование сети. */
                    wifiManager.startScan();
                }
            }
        }

        void register(){
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

    /** Приемник событий связяных с вкл/выкл wifi. */
    private class BaseReceiver extends BroadcastReceiver {
        private final Context mContext;
        private final IntentFilter intentFilter;
        boolean isRegistered;

        BaseReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            /* Проверяем событие на выключение и включение WiFi приемника. */
            String action = intent.getAction();
            switch (Objects.requireNonNull(action)){
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                    switch(extraWifiState){
                        case WifiManager.WIFI_STATE_DISABLED:
                            /* Если приемник был выключен заново включаем. */
                            turnOnWiFiConnection(true);
                            break;
                        case WifiManager.WIFI_STATE_ENABLED:
                            /* Приемник включен. Соеденяемся с конкретной сетью заново. */
                            connectToSpecificNetwork();
                            break;
                        case WifiManager.WIFI_STATE_UNKNOWN:
                            break;
                        default:
                    }
                    break;
                default:
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

    /**
     * Выполнить соединение с интернетом по wifi.
     *
     * @param on true - включить.
     */
    private void turnOnWiFiConnection(boolean on) {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return;
        }
        wifi.setWifiEnabled(on);
        while (wifi.isWifiEnabled() != on) ;
    }

    /** Приемник событий связяных с disconnect. */
    private class SupplicantDisconnectReceiver extends BroadcastReceiver {
        private final Context mContext;
        boolean isRegistered;
        private final IntentFilter intentFilter;

        SupplicantDisconnectReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (Objects.requireNonNull(action)){
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                    /* Если событие disconnect. */
                    if (state == SupplicantState.DISCONNECTED){
                        onWifiBaseManagerListener.onWiFiDisconnect();
                        /* Удаляем приемник сообщений. */
                        unregister();
                        /* Запускаем приемник на прием события результат сканирования.*/
                        scanWifiReceiver.register();
                        /* Запускаем сканирование сети. */
                        Objects.requireNonNull(wifiManager).startScan();
                        Log.i(TAG, "Разьединение с сетью " + Module.SSID);
                    }
                    break;
                default:
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

    void terminate(){
        try {connectionReceiver.unregister();}catch (NullPointerException e){}
        try {supplicantDisconnectReceiver.unregister();}catch (NullPointerException e){}
        baseReceiver.unregister();
        scanWifiReceiver.unregister();
    }

}
