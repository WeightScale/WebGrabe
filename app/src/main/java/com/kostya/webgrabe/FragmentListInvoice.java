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
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.kostya.webgrabe.provider.Invoice;
import com.kostya.webgrabe.provider.Invoice_;
import com.kostya.webgrabe.provider.Weighing;
import com.kostya.webgrabe.provider.Weighing_;
import com.kostya.webgrabe.task.IntentServiceGoogleForm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.reactive.DataObserver;

/**
 * @author Kostya on 10.12.2016.
 */
public class FragmentListInvoice extends Fragment {
    private InvoicesAdapter invoicesAdapter;
    private RecyclerView listInvoiceView;
    //private InvoiceTable invoiceTable;
    //DaoSession daoSession;
    BoxStore boxStore;
    Box<Invoice> invoiceBox;
    List<Invoice> invoiceList;
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
        //daoSession = ((Main)getActivity().getApplication()).getDaoSession();
        boxStore = ((Main)getActivity().getApplication()).getBoxStore();
        invoiceBox = boxStore.boxFor(Invoice.class);
        boxStore.subscribe(Invoice.class).observer(new DataObserver<Class<Invoice>>() {
            @Override
            public void onData(Class<Invoice> data) {
                invoiceList = invoiceBox.query().equal(Invoice_.dateCreate, date).build().find();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        invoicesAdapter.setInvoices(invoiceList);
                    }
                });
                //adapterWeightingList.notifyDataSetChanged();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_invoice, container, false);
        listInvoiceView = view.findViewById(R.id.invoiceList);
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
        ((ActivityMain)Objects.requireNonNull(getActivity())).openFragmentInvoice(_id);
    }

    /** Обновляем данные листа загрузок. */
    private void updateListWeight() {
        //Cursor cursor = invoiceTable.getToday(date);
        invoiceList = invoiceBox.query().equal(Invoice_.dateCreate, date).build().find();
        //Cursor crs = daoSession.getInvoiceDao().queryBuilder().where(InvoiceDao.Properties.DateCreate.eq(date)).buildCursor().query();

        if (invoiceList == null) { return; }
        //int[] to = {R.id.id_row, R.id.date_row, R.id.number_row, R.id.weight_row, R.id.imageReady};
        //String[] column = {InvoiceTable.KEY_ID,InvoiceTable.KEY_DATE_CREATE,InvoiceTable.KEY_NAME_AUTO,InvoiceTable.KEY_TOTAL_WEIGHT, InvoiceTable.KEY_IS_READY};
        /*String[] column = {InvoiceDao.Properties.Id.columnName,
                InvoiceDao.Properties.DateCreate.columnName,
                InvoiceDao.Properties.NameAuto.columnName,
                InvoiceDao.Properties.TotalWeight.columnName,
                InvoiceDao.Properties.IsReady.columnName};*/

        invoicesAdapter = new InvoicesAdapter(invoiceList, getLayoutInflater(), this);
        listInvoiceView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //namesAdapter = new MyCursorAdapter(this, R.layout.item_check, cursor, columns, to);
        //simpleCursorAdapter.setViewBinder(new ListInvoiceViewBinder());
        /*simpleCursorAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
            }
        });*/
        listInvoiceView.setAdapter(invoicesAdapter);
        /*getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        });*/
    }

    /*private class ListInvoiceViewBinder implements SimpleCursorAdapter.ViewBinder {
        private int ready, send;

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            switch (view.getId()){
                case R.id.imageReady:
                    //ready = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_IS_READY));
                    //send = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_IS_CLOUD));
                    ready = cursor.getInt(cursor.getColumnIndex(InvoiceDao.Properties.IsReady.columnName));
                    send = cursor.getInt(cursor.getColumnIndex(InvoiceDao.Properties.IsCloud.columnName));
                    *//*if (ready == InvoiceTable.UNREADY){
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_red));
                    }else if(send == InvoiceTable.READY){
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_check));
                    }else  {
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice));
                    }*//*
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
    }*/

    public class InvoicesAdapter extends RecyclerView.Adapter<InvoicesAdapter.InvoiceHolder> {

        private List<Invoice> invoiceList;
        private LayoutInflater mInflater;
        private Fragment mFragment;

        public InvoicesAdapter(List<Invoice> list, LayoutInflater inflater, Fragment fragment) {
            invoiceList = list;
            mInflater = inflater;
            mFragment = fragment;
        }

        @Override
        public InvoiceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_list_invoice, parent, false);
            return new InvoiceHolder(view);
        }

        @Override
        public void onBindViewHolder(InvoiceHolder holder, int position) {
            Invoice invoice = invoiceList.get(position);
            holder.bindInvoice(invoice);
        }

        @Override
        public int getItemCount() {
            return invoiceList.size();
        }

        public void setInvoices(List<Invoice> list) {
            invoiceList = list;
            notifyDataSetChanged();
        }

        class InvoiceHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView mId;
            private TextView mDate;
            private TextView mNumber;
            private TextView mWeight;
            private ImageView mImageReady;
            private Invoice mInvoice;


            public InvoiceHolder(View itemView) {
                super(itemView);
                mId = itemView.findViewById(R.id.id_row);
                mDate = itemView.findViewById(R.id.date_row);
                mNumber = itemView.findViewById(R.id.number_row);
                mWeight = itemView.findViewById(R.id.weight_row);
                mImageReady = itemView.findViewById(R.id.imageReady);
                itemView.setOnClickListener(this);
            }

            public void bindInvoice(Invoice invoice) {
                mInvoice = invoice;
                mId.setText(String.valueOf(invoice.getId()));
                mDate.setText(invoice.getDateCreate());
                mNumber.setText(invoice.getNameAuto());

                SpannableStringBuilder w = new SpannableStringBuilder(String.valueOf(invoice.getTotalWeight()));
                w.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.background2)), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                SpannableStringBuilder textKg = new SpannableStringBuilder(getResources().getString(R.string.scales_kg));
                //textKg.setSpan(new TextAppearanceSpan(getActivity(), R.style.SpanTextKgListInvoice), 0, textKg.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                w.append(textKg);
                mWeight.setText(w, TextView.BufferType.SPANNABLE);


                boolean ready = invoice.getIsReady();
                boolean send = invoice.getIsCloud();
                if (!ready){
                    mImageReady.setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_red));
                }else if(send){
                    mImageReady.setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_check));
                }else  {
                    mImageReady.setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice));
                }
            }

            @Override
            public void onClick(View view) {
                Invoice invoice = invoiceList.get(getLayoutPosition());
                Long catId = invoice.getId();
                //((MainActivity)mAppCompatActivity).getCatDao().deleteByKey(catId);
                //((MainActivity)mAppCompatActivity).updateCats();

                //Cursor cursor = invoiceTable.getEntryItem((int)l, InvoiceTable.KEY_IS_READY,InvoiceTable.KEY_TOTAL_WEIGHT);
                //Invoice invoice = daoSession.getInvoiceDao().loadByRowId(l);
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
                                    .setMessage("НАКЛАДНАЯ" + " №" + invoice.getId())
                                    .setIcon(R.drawable.ic_notification)
                                    .setPositiveButton("УДАЛИТЬ", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            //invoiceTable.removeEntry((int)l);
                                            //daoSession.getInvoiceDao().loadByRowId(l).delete();

                                            //new WeighingTable(getActivity()).removeEntryInvoice((int) l);
                                            //List<Weighing> weighingList = daoSession.getWeighingDao().queryBuilder().list();
                                            boxStore.boxFor(Weighing.class).query().equal(Weighing_.idInvoice, invoice.getId()).build().remove();
                                            invoiceBox.remove(invoice);
                                            //simpleCursorAdapter.notifyDataSetChanged();
                                            //daoSession.getWeighingDao().queryBuilder().where(WeighingDao.Properties.IdInvoice.eq(l)).build().unique().delete();
                                        }
                                    })
                                    .setNegativeButton("ОТКРЫТЬ", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                            openInvoice(invoice.getId());
                                        }
                                    });
                            builder.create().show();
                        }else {
                            openInvoice(invoice.getId());
                        }
                    }else {
                        getActivity().startService(new Intent(getActivity(), IntentServiceGoogleForm.class).setAction(IntentServiceGoogleForm.ACTION_EVENT_TABLE));
                    }
                }
            }
        }
    }
}
