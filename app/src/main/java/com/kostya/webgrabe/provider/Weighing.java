package com.kostya.webgrabe.provider;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Id;

@Entity(active = true, nameInDb = "WEIGHING")
public class Weighing {
    @Id
    private Long id;
    long idInvoice;
    String dateTimeCreate;
    double weight;
    String data0;
    String data1;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 669432483)
    private transient WeighingDao myDao;
    @Generated(hash = 1327429399)
    public Weighing(Long id, long idInvoice, String dateTimeCreate, double weight,
            String data0, String data1) {
        this.id = id;
        this.idInvoice = idInvoice;
        this.dateTimeCreate = dateTimeCreate;
        this.weight = weight;
        this.data0 = data0;
        this.data1 = data1;
    }
    @Generated(hash = 798490771)
    public Weighing() {
    }
    public long getIdInvoice() {
        return this.idInvoice;
    }
    public void setIdInvoice(long idInvoice) {
        this.idInvoice = idInvoice;
    }
    public String getDateTimeCreate() {
        return this.dateTimeCreate;
    }
    public void setDateTimeCreate(String dateTimeCreate) {
        this.dateTimeCreate = dateTimeCreate;
    }
    public double getWeight() {
        return this.weight;
    }
    public void setWeight(double weight) {
        this.weight = weight;
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
    @Generated(hash = 1261890456)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getWeighingDao() : null;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
