package com.kostya.webgrabe.provider;

import java.util.Date;
import java.util.Map;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
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
