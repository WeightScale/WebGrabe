package com.kostya.webgrabe;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.kostya.webgrabe.provider.Invoice;
import com.kostya.webgrabe.provider.Invoice_;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.TreeMap;
import java.util.stream.Collectors;

import io.objectbox.BoxStore;
import io.objectbox.query.Query;
import io.objectbox.query.QueryBuilder;

public class ActivityArchive extends AppCompatActivity {
    //private SectionsPagerAdapter mSectionsPagerAdapter;
    private CursorFragmentPagerAdapter cursorFragmentPagerAdapter;
    //private InvoiceTable invoiceTable;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);


        //DaoSession daoSession = ((Main)getApplication()).getDaoSession();
        BoxStore boxStore = ((Main)getApplication()).getBoxStore();
        QueryBuilder<Invoice> queryBuilder = boxStore.boxFor(Invoice.class).query();
        queryBuilder.notNull(Invoice_.dateCreate);
        Query<Invoice> query = queryBuilder.build();
        /*Cursor cursor = daoSession.getDatabase()
                .rawQuery("SELECT "
                        + InvoiceDao.Properties.Id.columnName
                        +", "+InvoiceDao.Properties.DateCreate.columnName
                        +", sum(" + InvoiceDao.Properties.TotalWeight.columnName + ") AS \""+InvoiceDao.Properties.TotalWeight.columnName+"\" FROM " + InvoiceDao.TABLENAME+" where "
                        + InvoiceDao.Properties.DateCreate.columnName+" IS NOT NULL GROUP BY "+InvoiceDao.Properties.DateCreate.columnName, new String []{});*/
        //invoiceTable = new InvoiceTable(this);
        //Cursor cursor = invoiceTable.getAllGroupDate();
        List<Invoice> invoices = query.find();
        Map<String, Double> counting = new HashMap<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            counting = invoices.stream().collect(Collectors.groupingBy(Invoice::getDateCreate, Collectors.summingDouble(Invoice::getTotalWeight)));
        }else {
            Iterator<Invoice> inv = invoices.iterator();

            while (inv.hasNext()){
                Invoice i = inv.next();
                String date = i.getDateCreate();
                double sum = i.getTotalWeight();
                if(counting.containsKey(date)){
                    double s = counting.get(date);
                    sum +=s;
                    //counting.remove(date);
                }
                counting.put(date,sum);
            }
        }
        Map<String, Double> treeMap = new TreeMap<>(counting);
        //cursor.moveToFirst();
        if (invoices.size() == 0){
            findViewById(R.id.archive_empty).setVisibility(View.VISIBLE);
            return;
        }
        mViewPager = findViewById(R.id.container);
        cursorFragmentPagerAdapter = new CursorFragmentPagerAdapter(this, getSupportFragmentManager(), invoices) {
            @Override
            public Fragment getItem(Context context, Invoice invoice) {
                //String d = cursor.getString(cursor.getColumnIndex(InvoiceTable.KEY_DATE_CREATE));
                String d = invoice.getDateCreate();// cursor.getString(cursor.getColumnIndex(InvoiceDao.Properties.DateCreate.columnName));
                //int w = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_TOTAL_WEIGHT));
                //double w = cursor.getDouble(cursor.getColumnIndex(InvoiceTable.KEY_TOTAL_WEIGHT));
                double w = invoice.getTotalWeight(); // cursor.getDouble(cursor.getColumnIndex(InvoiceDao.Properties.TotalWeight.columnName));
                return FragmentListArchiveInvoice.newInstance(d, String.valueOf(w));
            }
        };
        mViewPager.setAdapter(cursorFragmentPagerAdapter);
        mViewPager.setCurrentItem(Objects.requireNonNull(mViewPager.getAdapter()).getCount());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_archive, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    abstract class CursorFragmentPagerAdapter extends FragmentPagerAdapter {

        boolean mDataValid;
        List<Invoice> invoiceList;
        //Cursor mCursor;
        Context mContext;
        SparseIntArray mItemPositions;
        HashMap<Object, Integer> mObjectMap;
        long mRowIDColumn;

        CursorFragmentPagerAdapter(Context context, FragmentManager fm, List<Invoice> list) {
            super(fm);

            init(context, list);
        }

        void init(Context context, List<Invoice> list) {
            mObjectMap = new HashMap<Object, Integer>();
            boolean cursorPresent = list != null;
            invoiceList = list;
            mDataValid = cursorPresent;
            mContext = context;

            //mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
        }

        /*public Cursor getCursor() {
            return mCursor;
        }*/

        @Override
        public int getItemPosition(Object object) {
            Integer rowId = mObjectMap.get(object);
            if (rowId != null && mItemPositions != null) {
                return mItemPositions.get(rowId, POSITION_NONE);
            }
            return POSITION_NONE;
        }

        void setItemPositions() {
            mItemPositions = null;

            if (mDataValid) {
                int count = invoiceList.size();
                mItemPositions = new SparseIntArray(count);
                //mCursor.moveToPosition(-1);
                Iterator<Invoice> invoiceIterator = invoiceList.iterator();
                while (invoiceIterator.hasNext()){
                    Invoice invoice =invoiceIterator.next();
                    long rowId = invoice.getId();
                    int cursorPos = 0;// = mCursor.getPosition();
                    mItemPositions.append((int) rowId, cursorPos);
                }
                /*while (mCursor.moveToNext()) {
                    int rowId = mCursor.getInt(mRowIDColumn);
                    int cursorPos = mCursor.getPosition();
                    mItemPositions.append(rowId, cursorPos);
                }*/
            }
        }

        @Override
        public Fragment getItem(int position) {
            if (mDataValid) {
                //mCursor.moveToPosition(position);
                return getItem(mContext, invoiceList.get(position));
            } else {
                return null;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mObjectMap.remove(object);

            super.destroyItem(container, position, object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (!mDataValid) {
                throw new IllegalStateException("this should only be called when the cursor is valid");
            }
            /*if (!mCursor.moveToPosition(position)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }*/
            if (invoiceList.get(position)==null) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }

            long rowId = invoiceList.get((int) mRowIDColumn).getId();
            Object obj = super.instantiateItem(container, position);
            mObjectMap.put(obj, (int)rowId);

            return obj;
        }

        protected abstract Fragment getItem(Context context, Invoice invoice);

        @Override
        public int getCount() {
            if (mDataValid) {
                return invoiceList.size();
            } else {
                return 0;
            }
        }

        /*public void changeCursor(Cursor cursor) {
            Cursor old = swapCursor(cursor);
            if (old != null) {
                old.close();
            }
        }*/

        /*Cursor swapCursor(Cursor newCursor) {
            if (newCursor == mCursor) {
                return null;
            }
            Cursor oldCursor = mCursor;
            mCursor = newCursor;
            if (newCursor != null) {
                mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
                mDataValid = true;

            } else {
                mRowIDColumn = -1;
                mDataValid = false;
            }

            setItemPositions();
            if (mDataValid){
                notifyDataSetChanged();
            }


            return oldCursor;
        }*/

    }
}
