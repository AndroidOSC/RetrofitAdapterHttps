package com.atomone.retrofitadapterhttps;

import android.app.Application;
import android.content.Context;

/**
 * @author atomOne
 * @data on 2018/8/14/014 11:25
 * @describe TODO
 * @email 1299854942@qq.com
 */
public class App extends Application {


    private static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = getApplicationContext();
    }


    public static Context getAppContext() {
        return mAppContext;
    }
}
