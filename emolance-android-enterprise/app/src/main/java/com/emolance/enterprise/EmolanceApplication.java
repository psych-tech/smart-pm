package com.emolance.enterprise;

import android.app.Application;

import com.instabug.library.Instabug;
import com.instabug.library.invocation.InstabugInvocationEvent;

import io.paperdb.Paper;

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

        // paper db init
        Paper.init(this);

        new Instabug.Builder(this, "125a778e9668362c123857c3e9846c89")
                .setInvocationEvent(InstabugInvocationEvent.SHAKE)
                .build();
    }

}
