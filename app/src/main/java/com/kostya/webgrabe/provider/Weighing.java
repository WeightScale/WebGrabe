package com.kostya.webgrabe.provider;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Weighing {
    @Id
    private Long id;
    long idInvoice;
    String dateTimeCreate;
    double weight;
    String data0;
    String data1;

    public Weighing(Long id, long idInvoice, String dateTimeCreate, double weight,
            String data0, String data1) {
        this.id = id;
        this.idInvoice = idInvoice;
        this.dateTimeCreate = dateTimeCreate;
        this.weight = weight;
        this.data0 = data0;
        this.data1 = data1;
    }
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


    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
