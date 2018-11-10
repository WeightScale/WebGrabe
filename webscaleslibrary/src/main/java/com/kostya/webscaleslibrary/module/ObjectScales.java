package com.kostya.webscaleslibrary.module;

import com.kostya.webscaleslibrary.module.Module;

import java.io.Serializable;

/**
 * @author Kostya on 19.07.2016.
 */
public class ObjectScales implements Serializable {
    private static final long serialVersionUID = 443475064124921197L;
    /** Значение тензодатчика. */
    private int tenzoSensor;
    /** Значение веса. */
    private double weight;
    /** Процент заряда батареи модуля (0-100%). */
    private int battery;
    /** Значение температуры модуля. */
    private int temperature;
    /** Предельное показани датчика. */
    protected int marginTenzo;
    /** Максимальное показание датчика. */
    private int limitTenzo;
    /** Сколько уже стабильно показание веса. */
    private int stableNum;
    /** Флаг вес стабильный. */
    private boolean flagStab;

    public Module.ResultWeight getResultWeight() {
        return resultWeight;
    }

    public void setResultWeight(Module.ResultWeight resultWeight) {
        this.resultWeight = resultWeight;
    }

    private Module.ResultWeight resultWeight = Module.ResultWeight.WEIGHT_ERROR;

    public int getTenzoSensor() {return tenzoSensor;}
    public void setTenzoSensor(int tenzoSensor) {this.tenzoSensor = tenzoSensor;}
    public double getWeight() {return weight;}
    public void setWeight(double weight) {this.weight = weight;}
    public int getBattery() {return battery;}
    public void setBattery(int battery) {this.battery = battery;}
    public int getTemperature() {return temperature;}
    public void setTemperature(int temperature) {this.temperature = temperature;}
    public int getStableNum() {return stableNum;}
    public int setStableNum(int stableNum) {return this.stableNum = stableNum;}
    public boolean isFlagStab() {return flagStab;}
    public void setFlagStab(boolean flagStab) {this.flagStab = flagStab;}
}
