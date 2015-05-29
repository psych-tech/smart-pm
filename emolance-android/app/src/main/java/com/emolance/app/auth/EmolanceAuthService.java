package com.emolance.app.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by yusun on 5/26/15.
 */
public class EmolanceAuthService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("TEST", "binded executed!");
        EmolanceAuthenticator authenticator = new EmolanceAuthenticator(this);
        return authenticator.getIBinder();
    }
}
