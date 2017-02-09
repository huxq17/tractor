package com.andbase.demo;

import android.app.Application;

import com.andbase.tractor.Tractor;

/**
 * Created by 2144 on 2016/12/21.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Tractor.init(getApplicationContext());
    }
}
