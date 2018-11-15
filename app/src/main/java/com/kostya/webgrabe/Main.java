package com.kostya.webgrabe;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.kostya.webgrabe.provider.DaoMaster;
import com.kostya.webgrabe.provider.DaoSession;

import org.greenrobot.greendao.database.Database;

/**
 * @author Kostya on 12.11.2016.
 */
public class Main extends Application {
    private DaoSession daoSession;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "mycats-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
