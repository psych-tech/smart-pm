package com.emolance.enterprise.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;

import com.emolance.enterprise.R;
import com.emolance.enterprise.util.Constants;

/**
 * Created by yusun on 6/20/15.
 */
public class SplashActivity extends Activity {

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 2000;

    private boolean isFirstOpen = true;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_splash);

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isFirstOpen = false;
                checkAuth();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirstOpen) {
            checkAuth();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void checkAuth() {
        Log.i("TEST", "Checking auth...");
        final AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        Log.i("TEST", "Checking auth..." + accounts.length);
        if (accounts.length == 0) {
            accountManager.addAccount(Constants.ACCOUNT_TYPE, null, null, null, this,
                    null, null);
        } else {
            /* Create an Intent that will start the Menu-Activity. */
            Intent mainIntent = new Intent(SplashActivity.this, NewMainActivity.class);
            SplashActivity.this.startActivity(mainIntent);
            SplashActivity.this.finish();
        }
    }
}
