package com.kostya.webscaleslibrary.module;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kostya.webscaleslibrary.R;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.net.InetSocketAddress;


public class WebScalesClient extends Module implements Client.MessageListener, InterfaceModule {
    private Client clientWebSocket;

    private final Gson gson;
    private final Handler socketConnectionHandler;
    private static final String TAG = "WebScalesClient";


    WebScalesClient(Context context) {
        super(context);
        Commands.setInterfaceCommand(this);
        socketConnectionHandler = new Handler();
        gson = new GsonBuilder().create();
    }

    private void startCheckConnection() {
        socketConnectionHandler.postDelayed(checkConnectionRunnable, 5000);
    }

    private void stopCheckConnection() {
        socketConnectionHandler.removeCallbacks(checkConnectionRunnable);
    }

    private boolean isConnected() {
        return clientWebSocket != null &&
                clientWebSocket.getConnection() != null &&
                clientWebSocket.getConnection().isOpen();
    }

    private final Runnable checkConnectionRunnable = () -> {
        try{
            if (!clientWebSocket.getConnection().isOpen()) {
                openConnection();
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            startCheckConnection();
        }
    };

    private void openConnection() {
        if (clientWebSocket != null)
            clientWebSocket.close();
        try {
            clientWebSocket = new Client(this, HOST);
            clientWebSocket.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //initScreenStateListener();
        startCheckConnection();
    }

    private void closeConnection() {
        if (clientWebSocket != null) {
            clientWebSocket.close();
            clientWebSocket = null;
        }
        //releaseScreenStateListener();
        stopCheckConnection();
    }

    private final BackgroundManager.Listener appActivityListener = new BackgroundManager.Listener() {
        public void onBecameForeground() {
            openConnection();
        }

        public void onBecameBackground() {
            closeConnection();
        }

        @Override
        public void onBecameDestroy() {
            if(isConnected()){
                closeConnection();
            }
            wifiBaseManager.terminate();
            System.runFinalizersOnExit(true);
            System.exit(0);
        }
    };

    @Override
    public void onSocketMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            if (jsonObject.has("cmd")){
                switch (jsonObject.getString("cmd")){
                    case "swt":
                        EventBus.getDefault().post(gson.fromJson(message,Commands.ClassSWT.class));
                        break;
                    case "wt":
                        EventBus.getDefault().post(gson.fromJson(message, Commands.ClassWT.class));
                        break;
                }
            }

        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
    }

    @Override
    public ObjectCommand sendCommand(String cmd) {
        try {
            clientWebSocket.send("{'cmd':'"+cmd+"'}");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onWiFiConnect(String ssid, InetSocketAddress ipAddress) {
        BackgroundManager.get((Application) mContext).registerListener(appActivityListener);
        HOST = settings.read( mContext.getString(R.string.KEY_HOST) , "scales");
        if(!isConnected()){
            openConnection();
        }
    }

    @Override
    public void onWiFiDisconnect() {
        BackgroundManager.get((Application) mContext).unregisterListener(appActivityListener);
    }
}
