package com.kostya.webscaleslibrary.preferences;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.kostya.webscaleslibrary.R;
import com.kostya.webscaleslibrary.module.Module;

import java.util.List;
import java.util.Objects;

public class FragmentSettingsNet extends PreferenceFragment {

    public enum KEY{
        STATIC_IP(R.string.KEY_STATIC_IP){
            @Override
            void setup(Preference name) {
                final Context mContext = name.getContext();
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        return true;
                    }
                });
            }
        },
        IP(R.string.KEY_IP){
            @Override
            void setup(Preference name) {
                final CharSequence title = name.getTitle();
                name.setTitle(title + " " + name.getSharedPreferences().getString(name.getKey(), "192.168.1.100") );
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        preference.setTitle(title + " " + o);
                        return true;
                    }
                });
            }
        },
        MASK(R.string.KEY_MASK){
            @Override
            void setup(Preference name) {
                final CharSequence title = name.getTitle();
                name.setTitle(title + " " + name.getSharedPreferences().getString(name.getKey(), "255.255.1.1") );
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        preference.setTitle(title + " " + o);
                        return true;
                    }
                });
            }
        },
        GATEWAY(R.string.KEY_GATEWAY){
            @Override
            void setup(Preference name) {
                final CharSequence title = name.getTitle();
                name.setTitle(title + " " + name.getSharedPreferences().getString(name.getKey(), "192.168.1.1") );
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        preference.setTitle(title + " " + o);
                        return true;
                    }
                });
            }
        },
        NET(R.string.KEY_SSID){
            @Override
            void setup(final Preference name) {
                final Context mContext = name.getContext();
                try {
                    name.setTitle("ИМЯ СЕТИ: " + name.getSharedPreferences().getString(name.getKey(), "scales") );
                }catch (Exception e){}
                //name.setSummary("Сеть по умолчанию. Для выбора конкретной сети из списка кофигураций если есть.");
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty()) {
                            Toast.makeText(mContext, "Ошибка", Toast.LENGTH_SHORT).show();
                            name.setTitle("ИМЯ СЕТИ: " + "???");
                            return false;
                        }
                        if (!o.toString().equals(Module.SSID)){
                            Module.SSID = o.toString();
                            WifiManager wifi =  (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            if (wifi != null) {
                                wifi.setWifiEnabled(false);
                            }
                            name.setTitle("ИМЯ СЕТИ: " + Module.SSID);
                            return  true;
                        }
                        return false;
                    }
                });
            }

            String getNameOfId(Context context, int id){
                List<WifiConfiguration> list = ((WifiManager)Objects.requireNonNull(context.getApplicationContext().getSystemService(Context.WIFI_SERVICE))).getConfiguredNetworks();
                for (WifiConfiguration wifiConfiguration : list){
                    if (wifiConfiguration.networkId == id){
                        return  wifiConfiguration.SSID.replace("\"", "");
                    }
                }
                return "";
            }
        },
        HOST(R.string.KEY_HOST){
            @Override
            void setup(Preference name) {
                final Context context = name.getContext();
                final CharSequence title = name.getTitle();
                name.setTitle(title + " " + name.getSharedPreferences().getString(name.getKey(), "scales"));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        preference.setTitle(title + " " + o);
                        return true;
                    }
                });
            }
        },
        CLOSED(R.string.KEY_CLOSED){
            @Override
            void setup(Preference name) {
                final Context context = name.getContext();
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        ((Activity) preference.getContext()).onBackPressed();
                        return false;
                    }
                });
            }
        };
        private final int resId;
        abstract void setup(Preference name);

        KEY(int key){
            resId = key;
        }
        int getResId() { return resId; }
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_net);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.settings_net, false);
        //settings = new Settings(getActivity(), ScalesView.SETTINGS);
        //scalesView = ScalesView.getInstance();
        initPreferences();
    }

    private void initPreferences(){
        for (KEY enumPreference : KEY.values()){
            Preference preference = findPreference(getString(enumPreference.getResId()));
            if(preference != null){
                try {
                    enumPreference.setup(preference);
                } catch (Exception e) {
                    preference.setEnabled(false);
                }
            }
        }
    }
}
