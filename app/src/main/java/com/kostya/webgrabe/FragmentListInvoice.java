package com.kostya.webgrabe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.kostya.webgrabe.provider.DaoSession;
import com.kostya.webgrabe.provider.Invoice;
import com.kostya.webgrabe.provider.InvoiceDao;
import com.kostya.webgrabe.provider.Weighing;
import com.kostya.webgrabe.provider.WeighingDao;
import com.kostya.webgrabe.task.IntentServiceGoogleForm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * @author Kostya on 10.12.2016.
 */
public class FragmentListInvoice extends ListFragment {
    private SimpleCursorAdapter simpleCursorAdapter;
    //private InvoiceTable invoiceTable;
    DaoSession daoSession;
    private String date;
    private static final String ARG_DATE = "arg_date";

    /**
     * @return A new instance of fragment InvoiceFragment.
     */
    public static FragmentListInvoice newInstance(String date) {
        FragmentListInvoice fragment = new FragmentListInvoice();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            date = getArguments().getString(ARG_DATE);
        }else {
            date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
        }
        //invoiceTable = new InvoiceTable(getActivity());
        daoSession = ((Main)getActivity().getApplication()).getDaoSession();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_invoice, container, false);
        setupBanner(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateListWeight();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setupBanner(View view){
        AdView mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice(Globals.getInstance().getDeviceId())
                .build();
        mAdView.loadAd(adRequest);
        //requestNewInterstitial();
    }

    private void openInvoice(long _id){
        ((ActivityMain)Objects.requireNonNull(getActivity())).openFragmentInvoice(String.valueOf(_id));
    }

    /** Обновляем данные листа загрузок. */
    private void updateListWeight() {
        //Cursor cursor = invoiceTable.getToday(date);
        Cursor crs = daoSession.getInvoiceDao().queryBuilder().where(InvoiceDao.Properties.DateCreate.eq(date)).buildCursor().query();
        if (crs == null) {
            return;
        }
        crs.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
            }
        });
        crs.registerContentObserver(new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
            }

            @Override
            public void onChange(boolean selfChange) {
                simpleCursorAdapter.notifyDataSetChanged();
            }
        });
        int[] to = {R.id.id_row, R.id.date_row, R.id.number_row, R.id.weight_row, R.id.imageReady};
        //String[] column = {InvoiceTable.KEY_ID,InvoiceTable.KEY_DATE_CREATE,InvoiceTable.KEY_NAME_AUTO,InvoiceTable.KEY_TOTAL_WEIGHT, InvoiceTable.KEY_IS_READY};
        String[] column = {InvoiceDao.Properties.Id.columnName,
                InvoiceDao.Properties.DateCreate.columnName,
                InvoiceDao.Properties.NameAuto.columnName,
                InvoiceDao.Properties.TotalWeight.columnName,
                InvoiceDao.Properties.IsReady.columnName};

        simpleCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.item_list_invoice, crs, column, to, CursorAdapter.FLAG_AUTO_REQUERY);
        //namesAdapter = new MyCursorAdapter(this, R.layout.item_check, cursor, columns, to);
        simpleCursorAdapter.setViewBinder(new ListInvoiceViewBinder());
        simpleCursorAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
            }
        });
        setListAdapter(simpleCursorAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, final long l) {
                //Cursor cursor = invoiceTable.getEntryItem((int)l, InvoiceTable.KEY_IS_READY,InvoiceTable.KEY_TOTAL_WEIGHT);
                Invoice invoice = daoSession.getInvoiceDao().loadByRowId(l);
                if (invoice != null){
                    //int isReady = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_IS_READY));
                    boolean isReady = invoice.getIsReady();
                    //int totalWeight = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_TOTAL_WEIGHT));
                    double totalWeight = invoice.getTotalWeight();
                    //if (isReady != InvoiceTable.READY || totalWeight == 0){
                    if (!isReady || totalWeight == 0){
                        if (totalWeight == 0){
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.CustomAlertDialogInvoice));
                            builder.setCancelable(false)
                                    .setTitle("Сообщение")
                                    .setMessage("НАКЛАДНАЯ" + " №"+l)
                                    .setIcon(R.drawable.ic_notification)
                                    .setPositiveButton("УДАЛИТЬ", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            //invoiceTable.removeEntry((int)l);
                                            daoSession.getInvoiceDao().loadByRowId(l).delete();
                                            //new WeighingTable(getActivity()).removeEntryInvoice((int) l);
                                            List<Weighing> weighingList = daoSession.getWeighingDao().queryBuilder().list();
                                            simpleCursorAdapter.notifyDataSetChanged();
                                            //daoSession.getWeighingDao().queryBuilder().where(WeighingDao.Properties.IdInvoice.eq(l)).build().unique().delete();
                                        }
                                    })
                                    .setNegativeButton("ОТКРЫТЬ", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                            openInvoice(l);
                                        }
                                    });
                            builder.create().show();
                        }else {
                            openInvoice(l);
                        }
                    }else {
                        getActivity().startService(new Intent(getActivity(), IntentServiceGoogleForm.class).setAction(IntentServiceGoogleForm.ACTION_EVENT_TABLE));
                    }
                }
            }
        });
    }

    private class ListInvoiceViewBinder implements SimpleCursorAdapter.ViewBinder {
        private int ready, send;

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            switch (view.getId()){
                case R.id.imageReady:
                    //ready = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_IS_READY));
                    //send = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_IS_CLOUD));
                    ready = cursor.getInt(cursor.getColumnIndex(InvoiceDao.Properties.IsReady.columnName));
                    send = cursor.getInt(cursor.getColumnIndex(InvoiceDao.Properties.IsCloud.columnName));
                    /*if (ready == InvoiceTable.UNREADY){
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_red));
                    }else if(send == InvoiceTable.READY){
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_check));
                    }else  {
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice));
                    }*/
                    if (ready == 0){
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_red));
                    }else if(send == 1){
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_check));
                    }else  {
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice));
                    }
                break;
                case R.id.weight_row:

                    SpannableStringBuilder w = new SpannableStringBuilder(cursor.getString(cursor.getColumnIndex(InvoiceDao.Properties.TotalWeight.columnName)));
                    w.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.background2)), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    SpannableStringBuilder textKg = new SpannableStringBuilder(getResources().getString(R.string.scales_kg));
                    //textKg.setSpan(new TextAppearanceSpan(getActivity(), R.style.SpanTextKgListInvoice), 0, textKg.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    w.append(textKg);
                    ((TextView)view).setText(w, TextView.BufferType.SPANNABLE);
                break;
                default:
                    return false;
            }
            return true;
        }
    }
}
