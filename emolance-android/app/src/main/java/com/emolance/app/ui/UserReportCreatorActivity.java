package com.emolance.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.emolance.app.Injector;
import com.emolance.app.R;
import com.emolance.app.service.EmolanceAPI;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by yusun on 6/22/15.
 */
public class UserReportCreatorActivity extends FragmentActivity {

    @Inject
    EmolanceAPI emolanceAPI;

    @InjectView(R.id.qrButton)
    Button qrButton;
    @InjectView(R.id.qrIdText)
    TextView qrIdText;
    @InjectView(R.id.nameEditText)
    EditText nameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        this.setContentView(R.layout.activity_new_user_report);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.createButton)
    void createButton() {
        emolanceAPI.createUserReport(qrIdText.getText().toString(),
                nameEditText.getText().toString(), "1", new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        UserReportCreatorActivity.this.finish();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        UserReportCreatorActivity.this.finish();
                    }
                });
    }

    @OnClick(R.id.qrButton)
    void qrButtonTake() {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes

            startActivityForResult(intent, 0);
        } catch (Exception e) {
            Log.w("QR", "Failed to start the activity. Try suggest");
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
            startActivity(marketIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                qrIdText.setText(contents);
            }
            if(resultCode == RESULT_CANCELED){
                //handle cancel
            }
        }
    }
}
