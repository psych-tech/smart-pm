package com.emolance.enterprise.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.emolance.enterprise.R;
import com.emolance.enterprise.data.EmoUser;
import com.emolance.enterprise.data.EmoUserType;
import com.emolance.enterprise.service.EmolanceAuthAPI;
import com.emolance.enterprise.util.Constants;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

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
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.activity_login);

        mAccountManager = AccountManager.get(this);

        ImageButton button = (ImageButton) findViewById(R.id.loginButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });
    }

    private Interceptor getLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    private OkHttpClient getSimpleOkHttpClient(String username, String password) {
        final String credentials = username + ":" + password;
        Log.i("TEST", "get simple client");
        return new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        token = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                        Log.i("TEST", "get simple client in the chain. " + token);
                        request = request.newBuilder()
                                .addHeader("Authorization", token)
                                .addHeader("Acceppt", "application/json")
                                .build();

                        return chain.proceed(request);
                    }
                })
                .addInterceptor(getLoggingInterceptor())
                .connectTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .build();
    }

    public void submit() {
        final String username = ((TextView) findViewById(R.id.usernameText)).getText().toString();
        final String password = ((TextView) findViewById(R.id.passwordText)).getText().toString();

        if (username != null && password != null) {
            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(Constants.ENDPOINT)
                    .client(getSimpleOkHttpClient(username, password))
                    .addConverterFactory(JacksonConverterFactory.create());
            emolanceAuthAPI = builder.build().create(EmolanceAuthAPI.class);
        } else {
            Toast.makeText(this, "Please input the correct username/password",
                    Toast.LENGTH_LONG).show();
        }

        new AsyncTask<Void, Void, Intent>() {
            @Override
            protected Intent doInBackground(Void... params) {
                try {
                    Call<EmoUser> authCall = emolanceAuthAPI.authenticate();
                    retrofit2.Response<EmoUser> response = authCall.execute();
                    if (response.isSuccessful()) {
                        EmoUser user = response.body();
                        if (user.getType() == EmoUserType.ENTERPRISE) {
                            final Intent res = new Intent();
                            res.putExtra(AccountManager.KEY_ACCOUNT_NAME, username);
                            res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
                            res.putExtra(AccountManager.KEY_AUTHTOKEN, token);
                            res.putExtra(PARAM_USER_PASS, password);
                            return res;
                        }
                    }
                    return null;
                } catch (Exception e) {
                    Log.w(TAG, "Login failed.", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Intent intent) {
                if (intent != null) {
                    finishLogin(intent);
                } else {
                    Toast.makeText(AuthActivity.this, "Failed to login. Please make sure the username/password is correct.",
                            Toast.LENGTH_LONG).show();
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
