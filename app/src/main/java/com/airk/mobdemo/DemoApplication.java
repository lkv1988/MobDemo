package com.airk.mobdemo;

import android.app.Application;

import cn.smssdk.SMSSDK;

/**
 * Created by kevin on 2014/10/8.
 */
public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SMSSDK.initSDK(this,
                "yours",
                "yours");
    }

}
