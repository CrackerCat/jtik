package com.zxc.jtik.demo;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.zxc.jtik.Jtik;

/**
 * Created by zxc
 */
public class App extends Application {
    public static Context sAppContext;
    @Override
    public void onCreate() {
        super.onCreate();
        sAppContext = getApplicationContext();
    }
}
