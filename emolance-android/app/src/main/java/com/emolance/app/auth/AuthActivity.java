package com.emolance.app.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.emolance.app.R;
import com.emolance.app.service.EmolanceAuthAPI;
import com.emolance.app.util.Constants;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.client.Response;

/**
 * Created by yusun on 5/26/15.
 */
public class AuthActivity extends AccountAuthenticatorActivity {

    public static final String PARAM_USER_PASS = "userpass";
    public static final String ARG_IS_ADDING_NEW_ACCOUNT = "isAddingNewAccount";
    public static final String ARG_ACCOUNT_TYPE = "accountType";
    public static final String ARG_AUTH_TYPE = "authType";
    private static final String TAG = "AuthActivity";

    private AccountManager mAccountManager;
    private EmolanceAuthAPI emolanceAuthAPI;
    private String token;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.setContentView(R.layout.activity_login);

        mAccountManager = AccountManager.get(this);

        Button button = (Button) findViewById(R.id.loginButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });
    }

    public void submit() {
        final String username = ((TextView) findViewById(R.id.usernameText)).getText().toString();
        final String password = ((TextView) findViewById(R.id.passwordText)).getText().toString();

        if (username != null && password != null) {
            RestAdapter.Builder builder = new RestAdapter.Builder()
                    .setEndpoint(Constants.ENDPOINT)
                    .setClient(new OkClient(new OkHttpClient()));
            // concatenate username and password with colon for authentication
            final String credentials = username + ":" + password;
            builder.setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    // create Base64 encodet string
                    token = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                    request.addHeader("Accept", "application/json");
                    request.addHeader("Authorization", token);
                }
            });
            emolanceAuthAPI = builder.build().create(EmolanceAuthAPI.class);
        } else {
            Toast.makeText(this, "Please input the correct username/password",
                    Toast.LENGTH_LONG).show();
        }

        new AsyncTask<Void, Void, Intent>() {
            @Override
            protected Intent doInBackground(Void... params) {
                try {
                    Response response = emolanceAuthAPI.authenticate();
                    Log.i(TAG, "Auth response: " + response.getStatus() + " " + token);
                    if (response.getStatus() == 200) {
                        final Intent res = new Intent();
                        res.putExtra(AccountManager.KEY_ACCOUNT_NAME, username);
                        res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
                        res.putExtra(AccountManager.KEY_AUTHTOKEN, token);
                        res.putExtra(PARAM_USER_PASS, password);
                        return res;
                    } else {
                        return null;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Login failed.");
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Intent intent) {
                if (intent != null) {
                    finishLogin(intent);
                }
            }
        }.execute();
    }

    private void finishLogin(Intent intent) {
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = Constants.TOKEN_TYPE;
            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            Log.i("Auth", "Adding " + account + " " + authtokenType + " " + authtoken);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
        } else {
            mAccountManager.setPassword(account, accountPassword);
        }
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }
}
