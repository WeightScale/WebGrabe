package com.kostya.webgrabe.task;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.kostya.webgrabe.Globals;
import com.kostya.webgrabe.Main;
import com.kostya.webgrabe.R;
import com.kostya.webgrabe.internet.Internet;
import com.kostya.webgrabe.provider.Invoice;
import com.kostya.webgrabe.provider.Invoice_;
import com.kostya.webgrabe.settings.ActivityPreferences;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.params.BasicHttpParams;
import cz.msebera.android.httpclient.params.HttpConnectionParams;
import cz.msebera.android.httpclient.params.HttpParams;
import io.objectbox.query.Query;
import io.objectbox.query.QueryBuilder;

//import android.support.v7.app.NotificationCompat;

//import com.viktyusk.scales.*;


/**
 * @author Kostya  on 28.06.2016.
 */
public class IntentServiceGoogleForm extends IntentService {
    //private SystemTable systemTable;
    private Internet internet;
    private NotificationManager notificationManager;
    //private Toast toast;
    public static final String filePath = "forms/kolosok.xml";
    private static final String nameForm = "EventsForm";
    private static final String TAG = IntentServiceGoogleForm.class.getName();
    private static final String EXTRA_LIST_VALUE_PAIR = "com.kostya.scaleswifinet.task.EXTRA_LIST_VALUE_PAIR";
    private static final String EXTRA_HTTP_PATH = "com.kostya.scaleswifinet.task.EXTRA_HTTP_PATH";
    public static final String ACTION_EVENT_TABLE = "com.kostya.scaleswifinet.task.ACTION_EVENT_TABLE";

    public IntentServiceGoogleForm(String name) { super(name);  }
    public IntentServiceGoogleForm() { super(IntentServiceGoogleForm.class.getName());  }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            internet = new Internet(getApplicationContext());
            String action = intent.getAction();
            switch (Objects.requireNonNull(action)){
                case ACTION_EVENT_TABLE:
                    runSendEventTable();
                    //sendServerEventTable();
                break;
                default:{
                    Bundle bundle = intent.getExtras();
                    String http = bundle.getString(EXTRA_HTTP_PATH);
                    List<ValuePair> results = bundle.getParcelableArrayList(EXTRA_LIST_VALUE_PAIR);
                    internet.getConnection(5000, 10);
                    submitData(http, results);
                }
            }
        } catch (FileNotFoundException e){
            onNotificationFileNotFound("Не выбран файл Google", "Выберите в настройках файл.");
        } catch (SAXParseException e){
            onNotificationFileNotFound("Не правельный файл Google", "Выберите в настройках файл.");
        } catch (Exception e){
            sendNotification("Уведомление","Ошибка при отправке", e.getMessage());
        }
    }

    /*@Override
    public void onDestroy() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (toast != null)
                    toast.cancel();
                IntentServiceGoogleForm.super.onDestroy();
            }
        }, 1000);
        //super.onDestroy();
    }*/

    /*void sendServerEventTable(){
        if (!Internet.getConnection(10000, 10)) {return;}

        try {
            Cursor event = new EventsTable(getApplicationContext()).getPreliminary();
            if (event.getCount() > 0) {
                Scale scale = new Scale(systemTable.getProperty(SystemTable.Name.TERMINAL), systemTable.getProperty(SystemTable.Name.PIN_TABLE));
                event.moveToFirst();
                if (!event.isAfterLast()) {
                    do {
                        String date = event.getString(event.getColumnIndex(EventsTable.KEY_DATE_TIME));
                        String deviceId = event.getString(event.getColumnIndex(EventsTable.KEY_DEVICE_ID));
                        String eventName = event.getString(event.getColumnIndex(EventsTable.KEY_EVENT));
                        String eventValue = event.getString(event.getColumnIndex(EventsTable.KEY_EVENT_TEXT));

                        Event ev = new Event(date, eventName, eventValue);
                        try {
                            scale.addEvent(ev);
                            int id = event.getInt(event.getColumnIndex(EventsTable.KEY_ID));
                            new EventsTable(getApplicationContext()).updateEntry(id, EventsTable.KEY_STATE, EventsTable.State.CHECK_ON_SERVER.ordinal());
                        }catch (ScaleNotExistsException e){
                            Log.e(TAG, "Весы не существуют");
                        }catch (WrongPINException e){
                            Log.e(TAG, "Не правильный ПИН-код");
                        }
                    } while (event.moveToNext());
                }
            }
            event.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }*/

    private void runSendEventTable() throws Exception {

        if (!internet.getConnection(10000, 10)) {return;}
            /* Класс формы для передачи данных весового чека.*/
        //String path = new SystemTable(getApplicationContext()).getProperty(SystemTable.Name.PATH_FORM);
        //GoogleForms.Form form = new GoogleForms(getApplicationContext().getAssets().open(filePath)).createForm(nameForm);
        //InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(Uri.parse(path));
        //String FilePath = getApplicationContext().getFilesDir() + File.separator + "forms" + File.separator + "form.xml";
        File file = new File(Globals.getInstance().pathLocalForms,"form.xml");
        //Uri uri = Uri.parse(FilePath);
        InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(Uri.fromFile(file));
        //InputStream inputStream = new FileInputStream(path);
        GoogleForms.Form form = new GoogleForms(inputStream).createForm(nameForm);
        //Cursor invoice = new InvoiceTable(getApplicationContext()).getPreliminary(new Date());
        //DaoSession daoSession = ((Main)getApplication()).getDaoSession();
        String d = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
        QueryBuilder<Invoice> queryBuilder = ((Main)getApplication()).getBoxStore().boxFor(Invoice.class).query(); // daoSession.getInvoiceDao().queryBuilder();
        queryBuilder.equal(Invoice_.isCloud,false)
                .equal(Invoice_.isReady,true)
                .or().notEqual(Invoice_.dateCreate, d);
        //queryBuilder.where(InvoiceDao.Properties.IsCloud.eq(false), InvoiceDao.Properties.IsReady.eq(true));
        //queryBuilder.whereOr(InvoiceDao.Properties.DateCreate.notEq(d),null);
        List<Invoice> invoices = queryBuilder.build().find();
        if (invoices.size() > 0) {
            for (Invoice invoice : invoices){
                double weight = invoice.getTotalWeight();
                if (weight == 0){
                    continue;
                }
                String http = form.getHttp();

                Collection<BasicNameValuePair> values = form.getEntrys();
                List<ValuePair> results = new ArrayList<>();

                for (BasicNameValuePair valuePair : values){
                    try {
                        if(valuePair.getValue().equals(Invoice_.isReady.dbName)){
                            boolean i = invoice.getIsReady();
                            if (i){
                                results.add(new ValuePair(valuePair.getName(), ""));
                            }else {
                                results.add(new ValuePair(valuePair.getName(), "НЕ ЗАКРЫТА"));
                            }
                        }else
                            //results.add(new ValuePair(valuePair.getName(), invoice.getString(invoice.getColumnIndex(valuePair.getValue()))));
                            results.add(new ValuePair(valuePair.getName(), valuePair.getValue()));
                    } catch (Exception e) {}
                }
                try {
                    submitData(http, results);
                    invoice.setIsCloud(true);
                    ((Main)getApplication()).getBoxStore().boxFor(Invoice.class).put(invoice);
                    //invoice.update();
                }catch (Exception e){
                    Log.e(TAG, e.getMessage());
                }
            }

           /* invoice.moveToFirst();
            if (!invoice.isAfterLast()) {
                do {
                    int weight = invoice.getInt(invoice.getColumnIndex(InvoiceTable.KEY_TOTAL_WEIGHT));
                    if (weight == 0){
                        continue;
                    }
                    String http = form.getHttp();

                    Collection<BasicNameValuePair> values = form.getEntrys();
                    List<ValuePair> results = new ArrayList<>();

                    for (BasicNameValuePair valuePair : values){
                        try {
                            if(valuePair.getValue().equals(InvoiceTable.KEY_IS_READY)){
                                int i = invoice.getInt(invoice.getColumnIndex(InvoiceTable.KEY_IS_READY));
                                if (i == InvoiceTable.READY){
                                    results.add(new ValuePair(valuePair.getName(), ""));
                                }else {
                                    results.add(new ValuePair(valuePair.getName(), "НЕ ЗАКРЫТА"));
                                }
                            }else
                                results.add(new ValuePair(valuePair.getName(), invoice.getString(invoice.getColumnIndex(valuePair.getValue()))));
                        } catch (Exception e) {}
                    }
                    try {
                        submitData(http, results);
                        int id = invoice.getInt(invoice.getColumnIndex(InvoiceTable.KEY_ID));
                        new InvoiceTable(getApplicationContext()).updateEntry(id, InvoiceTable.KEY_IS_CLOUD, InvoiceTable.READY);
                    }catch (Exception e){
                        Log.e(TAG, e.getMessage());
                    }

                } while (invoice.moveToNext());
            }*/
        }
        //invoice.close();
    }

    private void submitData(String http_post, List<ValuePair> results) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(http_post);
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 15000);
        HttpConnectionParams.setSoTimeout(httpParameters, 30000);
        post.setParams(httpParameters);
        post.setEntity(new UrlEncodedFormEntity(results, "UTF-8"));
        HttpResponse httpResponse = client.execute(post);
        if (httpResponse.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK)
            throw new Exception(httpResponse.toString());
        //return httpResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK;
    }

    static class ValuePair extends BasicNameValuePair implements Parcelable {

        private static final long serialVersionUID = -4619452320173946427L;

        ValuePair(String name, String value) {
            super(name, value);
        }

        ValuePair(Parcel in) {
            super(in.readString(), in.readString());
        }

        public static final Creator<ValuePair> CREATOR = new Creator<ValuePair>() {
            @Override
            public ValuePair createFromParcel(Parcel in) {
                return new ValuePair(in);
            }

            @Override
            public ValuePair[] newArray(int size) {
                return new ValuePair[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(getValue());
            parcel.writeString(getName());
        }


        @Override
        public ValuePair clone() throws CloneNotSupportedException {
            return (ValuePair) super.clone();
        }
    }

    static class GoogleForms {
        private final Document document;

        GoogleForms(Context context, int xmlRawResource) throws IOException, SAXException, ParserConfigurationException {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = documentBuilder.parse(context.getResources().openRawResource(xmlRawResource));
        }

        GoogleForms(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = documentBuilder.parse(inputStream);
        }

        Form createForm(String name) throws Exception {
            Form form = new Form();
            Node node = document.getElementsByTagName(name).item(0);
            if(node == null)
                throw new Exception("Нет формы с именем " + name + " в файле disk.xml");
            form.setHttp(node.getAttributes().getNamedItem("http").getNodeValue());
            for (int i=0; i < node.getChildNodes().getLength() ; i++){
                Node entrys = node.getChildNodes().item(i);
                if("Entrys".equals(entrys.getNodeName())){
                    for (int e=0; e < entrys.getChildNodes().getLength(); e++){
                        Node table = entrys.getChildNodes().item(e);
                        if("Table".equals(table.getNodeName())){
                            form.setTable(table.getAttributes().getNamedItem("name").getNodeValue());
                            for (int t=0; t < table.getChildNodes().getLength(); t++){
                                Node columns = table.getChildNodes().item(t);
                                if("Columns".equals(columns.getNodeName())){
                                    NamedNodeMap map = columns.getAttributes();
                                    Collection<BasicNameValuePair> collection = new ArrayList<>();
                                    for (int m=0; m < map.getLength(); m++){
                                        collection.add(new BasicNameValuePair(map.item(m).getNodeName(), map.item(m).getNodeValue()));
                                    }
                                    form.setEntrys(collection);
                                    return form;
                                }
                            }
                        }
                    }
                }
            }
            return form;
        }

        public static class Form{
            private String http = "";
            private String table = "";
            private Collection<BasicNameValuePair> entrys = new ArrayList<>();

            String getHttp() {
                return http;
            }

            void setHttp(String http) {
                this.http = http;
            }

            Collection<BasicNameValuePair> getEntrys() {
                return entrys;
            }

            void setEntrys(Collection<BasicNameValuePair> entrys) {
                this.entrys = entrys;
            }

            public String getTable() {
                return table;
            }

            void setTable(String table) {
                this.table = table;
            }

            String getParams(){
                return TextUtils.join(" ", entrys);
            }

            public String[] getArrayParams(){
                String text = getParams();
                return text.split(" ");
            }

        }
    }

    private void onNotificationFileNotFound(String title, String text){
        Intent notificationIntent = new Intent(this, ActivityPreferences.class);

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(contentIntent)
                //.setOngoing(true)   //Can't be swiped out
                .setSmallIcon(R.drawable.ic_info)
                .setAutoCancel(true)
                //.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.large))   // большая картинка
                .setTicker("Уведомление!")
                .setContentTitle(title) //Заголовок
                .setContentText(text) // Текст уведомления
                .setWhen(System.currentTimeMillis());

        Notification notification = Build.VERSION.SDK_INT <= 15 ? builder.getNotification() : builder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private void sendNotification(String Ticker, String Title, String Text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder/*.setContentIntent(contentIntent)*/
                //.setOngoing(true)   //Can't be swiped out
                .setSmallIcon(R.drawable.ic_info)
                //.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.large))   // большая картинка
                .setTicker(Ticker)
                .setContentTitle(Title) //Заголовок
                .setContentText(Text) // Текст уведомления
                .setWhen(System.currentTimeMillis());

        Notification notification = Build.VERSION.SDK_INT <= 15 ? builder.getNotification() : builder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
