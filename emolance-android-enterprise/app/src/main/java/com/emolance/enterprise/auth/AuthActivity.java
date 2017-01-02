package com.emolance.enterprise.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.emolance.enterprise.R;
import com.emolance.enterprise.util.Constants;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

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

    private Firebase ref;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.activity_login);

        Firebase.setAndroidContext(this);
        ref = new Firebase("https://emolance.firebaseio.com");
        mAccountManager = AccountManager.get(this);

        ImageButton button = (ImageButton) findViewById(R.id.loginButton);
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

        // Create a handler to handle the result of the authentication
        Firebase.AuthResultHandler authResultHandler = new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                // Authenticated successfully with payload authData
                final Intent res = new Intent();
                res.putExtra(AccountManager.KEY_ACCOUNT_NAME, username);
                res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
                res.putExtra(AccountManager.KEY_AUTHTOKEN, password);
                res.putExtra(PARAM_USER_PASS, password);
                finishLogin(res);
            }
            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                // Authenticated failed with error firebaseError
                Log.w(TAG, "Login failed.");
                Toast.makeText(AuthActivity.this, "Incorrect username/password.", Toast.LENGTH_SHORT).show();
            }
        };

        ref.authWithPassword(username, password, authResultHandler);
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
