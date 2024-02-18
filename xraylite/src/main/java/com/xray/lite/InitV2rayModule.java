package com.xray.lite;

import android.app.Application;
import android.content.Context;

public class InitV2rayModule {
    public static Application MainApplication = null;
    public static Context AppContext;

    public static void ModuleInitVoid(Context context, Application mainApp) {
        AppContext = context.getApplicationContext();
        MainApplication = mainApp;
    }
}
