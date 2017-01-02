package com.emolance.enterprise;

import android.app.Application;

/**
 * Created by yusun on 5/21/15.
 */
public class EmolanceApplication extends Application {

    private static final String TAG = "Application";

    private static EmolanceApplication instance;

    public static EmolanceApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        // injector
        Injector.init((Object) new AppModule(), this);

        // parse push notification register
        String username = "admin";
        final String subId = "user_" + username;
    }

}
