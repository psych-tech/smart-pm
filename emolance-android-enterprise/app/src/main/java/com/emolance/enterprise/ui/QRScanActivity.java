package com.emolance.enterprise.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.emolance.enterprise.Injector;
import com.emolance.enterprise.R;
import com.emolance.enterprise.data.EmoUser;
import com.emolance.enterprise.data.Report;
import com.emolance.enterprise.data.TestReport;
import com.emolance.enterprise.service.EmolanceAPI;
import com.emolance.enterprise.util.Constants;
import com.mitac.cell.device.bcr.McBcrConnection;
import com.mitac.cell.device.bcr.McBcrMessage;
import com.mitac.cell.device.bcr.MiBcrListener;
import com.mitac.cell.device.bcr.utility.BARCODE;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.paperdb.Paper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by yusun on 6/22/15.
 */
public class QRScanActivity extends FragmentActivity {

    @Inject
    EmolanceAPI emolanceAPI;

    private static final String TAG = QRScanActivity.class.getName();

    private boolean hasScanned = false;
    private McBcrConnection mBcr;	// McBcrConnection help BCR control

    private UserReportsFragment adminFragment;
    private Long userId;
    private EmoUser currentEmoUser;
    private TestReport testReport;
    private String qr;
    @InjectView(R.id.qr_scan_right_layout)
    LinearLayout rightLayout;
    @InjectView(R.id.qr_scan_left_layout)
    LinearLayout leftLayout;
    @InjectView(R.id.button_measure)
    Button measureButton;
    @InjectView(R.id.button_qr_scan)
    Button qrScanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        this.setContentView(R.layout.activity_scan);
        ButterKnife.inject(this);
        userId = getIntent().getLongExtra(Constants.USER_ID, -1);
        Log.i(TAG, "Get user id: " + userId);
        currentEmoUser = Paper.book(Constants.DB_EMOUSER).read(Long.toString(userId), null);
        if (currentEmoUser == null) {
            Log.e(TAG, "Failed to find the chosen user. Id: " + userId);
            finish();
        }
        leftLayout.setVisibility(View.INVISIBLE);

        try{
            mBcr = new McBcrConnection(this);
            mBcr.setListener(new MiBcrListener() {
                @Override
                public void onScanned(final String s, BARCODE.TYPE type, int i) {
                    if (hasScanned) return;
                    hasScanned = true;
                    //mBcr.scan(false);

                    Log.i("Scanner", s + " " + type + " " + i);
                    Log.i("Scanner", "Shutting down the BarCode scanner.");

                    create(s);
                /*
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(QRScanActivity.this, UserReportCreatorActivity.class);
                        intent.putExtra("qr", s);
                        intent.putExtra(Constants.USER_ID, userId);
                        startActivity(intent);
                    }
                }, 1000);
                */
                }

                @Override
                public void onStatusChanged(int i) {
                    Log.i("TEST", "Changed " + i + " ");
                    if (!hasScanned && (McBcrMessage.Status_Ready == i || McBcrMessage.Status_ServiceConnected == i)) {
                        mBcr.scan(true);
                    }
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
        measureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(qr != null){
                    Toast.makeText(getApplicationContext(),"Report is processing.",Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("qr", qr);
                    setResult(QRScanActivity.RESULT_OK, resultIntent);
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(),"No QR available.",Toast.LENGTH_SHORT).show();
                }
            }
        });
        qrScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScan();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mBcr != null){
            mBcr.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBcr != null){
            mBcr.stopListening();
        }
    }

    @Override
    protected void onDestroy() {
        if(mBcr != null){
            mBcr.close();
        }
        super.onDestroy();
    }

    protected void create(String qr) {
        Report report = new Report();
        report.setId(System.currentTimeMillis());
        report.setQrcode(qr);
        report.setName("");
        report.setAge("");
        report.setPosition("");
        report.setProfilePhotoIndex(0);
        report.setTimestamp(System.currentTimeMillis());
        report.setStatus("Ready to Measure");

        final TestReport testReport = new TestReport();
        testReport.setReportCode(qr);
        testReport.setOwner(currentEmoUser);
        testReport.setStatus("Not Tested");

        Call<ResponseBody> createCall = emolanceAPI.createUserReport(testReport);
        createCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(QRScanActivity.this,
                            "Failed to add the report.", Toast.LENGTH_SHORT).show();
                }
                QRScanActivity.this.finish();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(QRScanActivity.this,
                        "Failed to add the report.", Toast.LENGTH_SHORT).show();
                QRScanActivity.this.finish();
            }
        });
    }

    private void qrScan(){
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
            this.startActivityForResult(intent, 111);
        } catch (Exception e) {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
            startActivityForResult(marketIntent,111);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111) {
            if (resultCode == RESULT_OK) {
                rightLayout.setVisibility(View.INVISIBLE);
                leftLayout.setVisibility(View.VISIBLE);
                qr = data.getStringExtra("SCAN_RESULT");
            }
            if(resultCode == RESULT_CANCELED){
                Toast.makeText(getApplicationContext(), "QR SCANNING WAS CANCELED.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
