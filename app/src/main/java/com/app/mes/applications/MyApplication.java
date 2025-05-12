package com.app.mes.applications;

import android.app.Application;

import com.app.mes.helper.CloudinaryHelper;

public class MyApplication extends Application {
    private static Application application;
    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        CloudinaryHelper.init(this);
    }
}
