package com.kostya.webgrabe;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.kostya.webgrabe.provider.DaoSession;
import com.kostya.webgrabe.provider.MyObjectBox;

import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;

/**
 * @author Kostya on 12.11.2016.
 */
public class Main extends Application {
    private BoxStore boxStore;
    private DaoSession daoSession;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

        boxStore = MyObjectBox.builder().androidContext(this).build();
        daoSession = new DaoSession(boxStore);
        if (BuildConfig.DEBUG) {
            new AndroidObjectBrowser(boxStore).start(this);
        }
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
