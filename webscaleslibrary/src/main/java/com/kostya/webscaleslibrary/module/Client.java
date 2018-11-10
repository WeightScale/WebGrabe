package com.kostya.webscaleslibrary.module;

import android.os.Handler;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client{
    ObjectCommand response;
    //List<BasicNameValuePair> extraHeaders = Collections.singletonList(new BasicNameValuePair("Cookie", "session=abcd"));
    private final Handler handler = new Handler();
    private AtomicBoolean working;
    private final MessageListener listener;
    private final String host;
    private WebSocket ws;
    private static final int TIME_OUT_CONNECT = 5000; /** Время в милисекундах. */
    private static final int TIME_PING_INTERVAL = 5000;
    private static final String TAG = "Websocket";

    Client(MessageListener listener, String host){
        this.host = "ws://"+host+"/ws";
        this.listener = listener;
    }

    Client(MessageListener listener, InetSocketAddress address){
        this.host = "ws://"+ address.getHostString()+"/ws";
        this.listener = listener;
    }


    void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (ws != null) {
                    reconnect();
                } else {
                    try {
                        WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(TIME_OUT_CONNECT);
                        //SSLContext context = NaiveSSLContext.getInstance("TLS");
                        //factory.setSSLContext(context);
                        ws = factory.createSocket(host);
                        ws.addListener(new SocketListener());
                        //ws.setMissingCloseFrameAllowed(true);
                        //ws.setPingSenderName("Scales");
                        EventBus.getDefault().post(new MessageEventSocket(MessageEventSocket.Message.CONNECTING, Module.HOST));
                        ws.connectAsynchronously();
                    } catch (IOException e) {
                        //EventBus.getDefault().post(new MessageEventSocket(MessageEventSocket.Message.ERROR, e.toString()));
                    }
                }
            }
        }).start();
    }

    private void reconnect() {
        try {
            ws = ws.recreate().connect();
        } catch (WebSocketException | IOException e) {
            e.printStackTrace();
        }
    }

    WebSocket getConnection() {
        return ws;
    }

    public void close() {
        ws.disconnect();
    }

    void send(String data) {
        ws.sendText(data);
    }

    private final Runnable pongTimeOutRunable = new Runnable() {
        @Override
        public void run() {
            close();
            //handler.postDelayed(pongTimeOutRunable, 10000);
        }
    };

    class SocketListener extends WebSocketAdapter {

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
            super.onConnected(websocket, headers);
            InetAddress uri = websocket.getSocket().getInetAddress();
            if (!uri.getHostAddress().equals(Module.HOST)){
                Module.HOST = uri.getHostAddress();
                websocket.disconnect(10);
            }else {
                EventBus.getDefault().post(new MessageEventSocket(MessageEventSocket.Message.CONNECT, Module.HOST));
                websocket.setPingInterval(TIME_PING_INTERVAL);
            }
        }

        public void onTextMessage(WebSocket websocket, String message) throws Exception {
            super.onTextMessage(websocket,message);
            listener.onSocketMessage(message);
            //Log.i(TAG, "Message --> " + message);
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
            super.onDisconnected(websocket,serverCloseFrame,clientCloseFrame,closedByServer);
            //Log.i(TAG, "onDisconnected");
            //EventBus.getDefault().post(new MessageEventSocket(MessageEventSocket.Message.DISCONNECT, "Disconnected"));
            websocket.setPingInterval(0);
            if (closedByServer) {
                reconnect();
            }
        }

        @Override
        public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
            super.onUnexpectedError(websocket,cause);
            //EventBus.getDefault().post(new MessageEventSocket(MessageEventSocket.Message.UNEXPECTED, cause.toString()));
            //Log.i(TAG, "Error -->" + cause.getMessage());
            reconnect();
        }

        @Override
        public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
            super.onPongFrame(websocket, frame);
            handler.removeCallbacks(pongTimeOutRunable);
            handler.postDelayed(pongTimeOutRunable, 10000);
            //websocket.sendPing("Are you there?");
        }

        @Override
        public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
            super.onStateChanged(websocket, newState);
            EventBus.getDefault().post(new MessageEventSocket(MessageEventSocket.Message.STATE, newState.name()));
        }

    }

    static class MessageEventSocket{
        enum Message{
            CONNECTING("Пробуем Соединится"),
            CONNECT("Есть соединение"),
            DISCONNECT("Нет соединения"),
            ERROR("Ошибка соединения"),
            STATE(""),
            UNEXPECTED("");
            String message;
            Message(String message){
                this.message = message;
            }

            public String toString() {return message;}
        }

        final Message message;
        final String text;

        MessageEventSocket(Message message, String text){
            this.message = message;
            this.text = text;
        }
    }

    public interface MessageListener {
        void onSocketMessage(String message);
    }

}
