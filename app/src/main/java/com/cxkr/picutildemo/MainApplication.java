package com.cxkr.picutildemo;

import android.app.Application;
import android.content.Context;

/**
 * Created by songyuanjin on 16/9/1.
 */
public class MainApplication extends Application{

    public static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }


    public static Context getContext() {
        return context;
    }
}
