package com.emolance.app;

import android.app.Application;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

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

        // parse init
        Parse.initialize(this, "LLqu5BZybPgJ0ntI1aS5Ra3Z47VgMjaX8GG1lbxU", "nJX8iM7eQ2W6uQq95XGkFGZEX4tTU8pkKtgkp4Zl");

        // parse push notification register
        String username = "admin";
        final String subId = "user_" + username;
        ParsePush.subscribeInBackground(subId, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                    // update to the server
                    updateUserMapping("user", subId);
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });
    }

    private void updateUserMapping(final String username, final String subId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserMapping");
        query.whereEqualTo("username", username);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (list == null || list.size() == 0) {
                    ParseObject userRecord = new ParseObject("UserMapping");
                    userRecord.put("username", username);
                    userRecord.put("subId", subId);
                    userRecord.saveInBackground();
                    Log.i(TAG, "Updated user mapping record for " + username + " -> " + subId);
                }
            }
        });
    }
}
