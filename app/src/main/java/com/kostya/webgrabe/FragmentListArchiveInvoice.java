package com.kostya.webgrabe;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.kostya.webgrabe.provider.Invoice;
import com.kostya.webgrabe.provider.Invoice_;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.objectbox.BoxStore;

/**
 * @author Kostya on 10.12.2016.
 */
public class FragmentListArchiveInvoice extends Fragment {
    private SimpleCursorAdapter simpleCursorAdapter;
    //private InvoiceTable invoiceTable;
    //DaoSession daoSession;
    BoxStore boxStore;
    private TextView dateText, totalWeight;
    private RecyclerView recyclerView;
    //ListView listView;
    private String date, total;
    private static final String ARG_DATE = "arg_date";
    private static final String ARG_TOTAL = "arg_total";

    /**
     * @return A new instance of fragment InvoiceFragment.
     */
    public static FragmentListArchiveInvoice newInstance(String date, String total) {
        FragmentListArchiveInvoice fragment = new FragmentListArchiveInvoice();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        args.putString(ARG_TOTAL, total);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            date = getArguments().getString(ARG_DATE);
            total = getArguments().getString(ARG_TOTAL);
        }else {
            date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
            total = "";
        }
        //invoiceTable = new InvoiceTable(getActivity());
        //daoSession = ((Main)getActivity().getApplication()).getDaoSession();
        boxStore = ((Main)getActivity().getApplication()).getBoxStore();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_archive_invoice, container, false);

        recyclerView = view.findViewById(R.id.blank);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        dateText = view.findViewById(R.id.date_archive_invoice);
        dateText.setText(date);

        totalWeight = view.findViewById(R.id.totalWeight);
        SpannableStringBuilder w = new SpannableStringBuilder(total);
        w.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.background2)), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        SpannableStringBuilder textKg = new SpannableStringBuilder(getResources().getString(R.string.scales_kg));
        //textKg.setSpan(new TextAppearanceSpan(getActivity(), R.style.SpanTextKgListInvoice), 0, textKg.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        w.append(textKg);
        totalWeight.setText(w, TextView.BufferType.SPANNABLE);

        //listView = (ListView)view.findViewById(R.id.list);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateRecyclerView();

    }

    private void updateRecyclerView() {
        //Cursor cursor = invoiceTable.getToday(date);
        //List<Invoice> invoices = daoSession.getInvoiceDao().queryBuilder().where(InvoiceDao.Properties.DateCreate.eq(date)).list();
        List<Invoice> invoices = boxStore.boxFor(Invoice.class).find(Invoice_.dateCreate, date);
        /*if (cursor == null) {
            return;
        }*/
        if (invoices == null) {
            return;
        }

        recyclerView.setAdapter(new CursorRecyclerViewAdapter(getActivity(), invoices) {
            @Override
            public void onBindViewHolder(CursorRecyclerViewAdapter.InvoiceHolder viewHolder, Invoice invoice) {
                viewHolder._id.setText(String.valueOf(invoice.getId()));
                viewHolder.date.setText(date);
                viewHolder.number.setText(invoice.getNameAuto());
                boolean ready = invoice.getIsReady();
                boolean send = invoice.getIsCloud();
                if (!ready){
                    viewHolder.ready.setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_red));
                }else if(send){
                    viewHolder.ready.setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_check));
                }else  {
                    viewHolder.ready.setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice));
                }
                SpannableStringBuilder w = new SpannableStringBuilder(String.valueOf(invoice.getTotalWeight()));
                w.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.background2)), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                SpannableStringBuilder textKg = new SpannableStringBuilder(getResources().getString(R.string.scales_kg));
                //textKg.setSpan(new TextAppearanceSpan(getActivity(), R.style.SpanTextKgListInvoice), 0, textKg.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                w.append(textKg);
                viewHolder.weight.setText(w, TextView.BufferType.SPANNABLE);
                //((TextView)view).setText(w, TextView.BufferType.SPANNABLE);
            }
        });

    }

    /*private class ListInvoiceViewBinder implements SimpleCursorAdapter.ViewBinder {
        private int ready, send;

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            switch (view.getId()){
                case R.id.imageReady:
                    ready = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_IS_READY));
                    send = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_IS_CLOUD));
                    if (ready == InvoiceTable.UNREADY){
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_red));
                    }else if(send == InvoiceTable.READY){
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_check));
                    }else  {
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice));
                    }
                break;
                case R.id.weight_row:
                    SpannableStringBuilder w = new SpannableStringBuilder(cursor.getString(cursor.getColumnIndex(InvoiceTable.KEY_TOTAL_WEIGHT)));
                    w.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.background2)), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    SpannableStringBuilder textKg = new SpannableStringBuilder(getResources().getString(R.string.scales_kg));
                    textKg.setSpan(new TextAppearanceSpan(getActivity(), R.style.SpanTextKgListInvoice), 0, textKg.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    w.append(textKg);
                    ((TextView)view).setText(w, TextView.BufferType.SPANNABLE);
                break;
                default:
                    return false;
            }
            return true;
        }
    }*/

    /*protected abstract class CursorRecyclerViewAdapter extends RecyclerView.Adapter<CursorRecyclerViewAdapter.InvoiceHolder> {

        private final Context mContext;
        private final Cursor mCursor;
        private boolean mDataValid;
        private final int mRowIdColumn;
        private DataSetObserver mDataSetObserver;

        CursorRecyclerViewAdapter(Context context, Cursor cursor) {
            mContext = context;
            mCursor = cursor;
            mDataValid = cursor != null;
            mRowIdColumn = mDataValid ? mCursor.getColumnIndex("_id") : -1;
            *//*mDataSetObserver = new NotifyingDataSetObserver();
            if (mCursor != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }*//*
        }

        public Cursor getCursor() {
            return mCursor;
        }

        @Override
        public int getItemCount() {
            if (mDataValid && mCursor != null) {
                return mCursor.getCount();
            }
            return 0;
        }

        @Override
        public long getItemId(int position) {
            if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
                return mCursor.getLong(mRowIdColumn);
            }
            return 0;
        }

        protected abstract void onBindViewHolder(CursorRecyclerViewAdapter.InvoiceHolder viewHolder, Cursor cursor);

        @Override
        public void onBindViewHolder(CursorRecyclerViewAdapter.InvoiceHolder viewHolder, int position) {
            if (!mDataValid) {
                throw new IllegalStateException("this should only be called when the cursor is valid");
            }
            if (!mCursor.moveToPosition(position)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }
            onBindViewHolder(viewHolder, mCursor);
        }

        @Override
        public InvoiceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_archive_invoice, parent, false);
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_invoice, parent, false);
            return new InvoiceHolder(itemView);
        }

        public class InvoiceHolder extends RecyclerView.ViewHolder {
            private final TextView _id;
            private final TextView date;
            private final TextView number;
            private final TextView weight;
            private final ImageView ready;


            InvoiceHolder(View view) {
                super(view);
                _id = view.findViewById(R.id.id_row);
                date = view.findViewById(R.id.date_row);
                number = view.findViewById(R.id.number_row);
                weight = view.findViewById(R.id.weight_row);
                ready = view.findViewById(R.id.imageReady);
            }
        }

        private class NotifyingDataSetObserver extends DataSetObserver {
            @Override
            public void onChanged() {
                super.onChanged();
                mDataValid = true;
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                mDataValid = false;
                notifyDataSetChanged();
                //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
            }
        }
    }*/

    protected abstract class CursorRecyclerViewAdapter extends RecyclerView.Adapter<CursorRecyclerViewAdapter.InvoiceHolder> {

        private final Context mContext;
        private List<Invoice> invoiceList;
        private boolean mDataValid;
        //private final int mRowIdColumn;
        private DataSetObserver mDataSetObserver;

        CursorRecyclerViewAdapter(Context context, List<Invoice> list) {
            mContext = context;
            invoiceList = list;
            mDataValid = invoiceList != null;
            //mRowIdColumn = mDataValid ? mCursor.getColumnIndex("_id") : -1;
            /*mDataSetObserver = new NotifyingDataSetObserver();
            if (mCursor != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }*/
        }

        /*public Cursor getCursor() {
            return mCursor;
        }*/

        @Override
        public int getItemCount() {
            if (mDataValid && invoiceList != null) {
                return invoiceList.size();
            }
            return 0;
        }

        /*@Override
        public long getItemId(int position) {
            if (mDataValid && invoiceList != null && mCursor.moveToPosition(position)) {
                return mCursor.getLong(mRowIdColumn);
            }
            return 0;
        }*/

        protected abstract void onBindViewHolder(CursorRecyclerViewAdapter.InvoiceHolder viewHolder, Invoice invoice);

        @Override
        public void onBindViewHolder(CursorRecyclerViewAdapter.InvoiceHolder viewHolder, int position) {
            if (!mDataValid) {
                throw new IllegalStateException("this should only be called when the cursor is valid");
            }
            Invoice invoice = invoiceList.get(position);
            onBindViewHolder(viewHolder, invoice);
        }

        @Override
        public InvoiceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_archive_invoice, parent, false);
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_invoice, parent, false);
            return new InvoiceHolder(itemView);
        }

        public class InvoiceHolder extends RecyclerView.ViewHolder {
            private final TextView _id;
            private final TextView date;
            private final TextView number;
            private final TextView weight;
            private final ImageView ready;


            InvoiceHolder(View view) {
                super(view);
                _id = view.findViewById(R.id.id_row);
                date = view.findViewById(R.id.date_row);
                number = view.findViewById(R.id.number_row);
                weight = view.findViewById(R.id.weight_row);
                ready = view.findViewById(R.id.imageReady);
            }
        }

        private class NotifyingDataSetObserver extends DataSetObserver {
            @Override
            public void onChanged() {
                super.onChanged();
                mDataValid = true;
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                mDataValid = false;
                notifyDataSetChanged();
                //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
            }
        }
    }
}
