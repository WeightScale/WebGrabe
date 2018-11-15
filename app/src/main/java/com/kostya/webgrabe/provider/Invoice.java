package com.kostya.webgrabe.provider;

import android.content.ContentQueryMap;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;
import java.util.Map;

@Entity(active = true, nameInDb = "INVOICE")
public class Invoice {
    @Id
    private Long id;
    String dateCreate;
    String timeCreate;
    String nameAuto;
    double totalWeight;
    boolean isReady;
    boolean isCloud;
    String data0;
    String data1;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1192627946)
    private transient InvoiceDao myDao;
    @Generated(hash = 723549051)
    public Invoice(Long id, String dateCreate, String timeCreate, String nameAuto,
            double totalWeight, boolean isReady, boolean isCloud, String data0,
            String data1) {
        this.id = id;
        this.dateCreate = dateCreate;
        this.timeCreate = timeCreate;
        this.nameAuto = nameAuto;
        this.totalWeight = totalWeight;
        this.isReady = isReady;
        this.isCloud = isCloud;
        this.data0 = data0;
        this.data1 = data1;
    }
    @Generated(hash = 1296330302)
    public Invoice() {
    }
    public String getDateCreate() {
        return this.dateCreate;
    }
    public void setDateCreate(String dateCreate) {
        this.dateCreate = dateCreate;
    }
    public String getTimeCreate() {
        return this.timeCreate;
    }
    public void setTimeCreate(String timeCreate) {
        this.timeCreate = timeCreate;
    }
    public String getNameAuto() {
        return this.nameAuto;
    }
    public void setNameAuto(String nameAuto) {
        this.nameAuto = nameAuto;
    }
    public double getTotalWeight() {
        return this.totalWeight;
    }
    public void setTotalWeight(double totalWeight) {
        this.totalWeight = totalWeight;
    }
    public boolean getIsReady() {
        return this.isReady;
    }
    public void setIsReady(boolean isReady) {
        this.isReady = isReady;
    }
    public boolean getIsCloud() {
        return this.isCloud;
    }
    public void setIsCloud(boolean isCloud) {
        this.isCloud = isCloud;
    }
    public String getData0() {
        return this.data0;
    }
    public void setData0(String data0) {
        this.data0 = data0;
    }
    public String getData1() {
        return this.data1;
    }
    public void setData1(String data1) {
        this.data1 = data1;
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2096295247)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getInvoiceDao() : null;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public static long dayDiff(Date d1, Date d2) {
        final long DAY_MILLIS = 1000 * 60 * 60 * 24;
        long day1 = d1.getTime() / DAY_MILLIS;
        long day2 = d2.getTime() / DAY_MILLIS;
        return day1 - day2;
    }
}
