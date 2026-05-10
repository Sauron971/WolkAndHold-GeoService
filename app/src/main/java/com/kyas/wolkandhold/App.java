package com.kyas.wolkandhold;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;


import com.yandex.mapkit.MapKitFactory;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        String apiKey = BuildConfig.YANDEX_API_KEY;

        MapKitFactory.setApiKey(apiKey);
        MapKitFactory.initialize(this);
    }
}
