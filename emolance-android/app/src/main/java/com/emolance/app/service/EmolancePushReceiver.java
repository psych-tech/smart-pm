package com.emolance.app.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.emolance.app.util.Constants;
import com.parse.ParsePushBroadcastReceiver;

/**
 * Created by yusun on 5/26/15.
 */
public class EmolancePushReceiver extends ParsePushBroadcastReceiver {

    private static final String TAG = EmolancePushReceiver.class.getName();

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
        Log.i(TAG, "Received the push notification in braodcast receiver.");
        context.sendBroadcast(new Intent(Constants.SYNC_INTENT_FILTER));
    }
}
