package com.emolance.app.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AccountsException;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.emolance.app.util.Constants;

import java.io.IOException;

import static android.accounts.AccountManager.KEY_AUTHTOKEN;

/**
 * Created by yusun on 5/26/15.
 */
public class ApiKeyProvider {

    private AccountManager accountManager;

    private String tokenKey;

    public ApiKeyProvider(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public String getAuthTokenValue() {
        if (tokenKey == null) {
            Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
            if (accounts.length > 0) {
                Account account = accounts[0];
                tokenKey = accountManager.peekAuthToken(account, Constants.TOKEN_TYPE);
            }
            Log.i("Auth", "Get auth token: " + tokenKey);
        }
        return tokenKey;
    }


    public String getAuthKey(final Activity activity) throws AccountsException, IOException {
        final AccountManagerFuture<Bundle> accountManagerFuture
                = accountManager.getAuthTokenByFeatures(Constants.ACCOUNT_TYPE,
                Constants.TOKEN_TYPE, new String[0], activity, null, null, null, null);

        tokenKey = accountManagerFuture.getResult().getString(KEY_AUTHTOKEN);
        return tokenKey;
    }
}