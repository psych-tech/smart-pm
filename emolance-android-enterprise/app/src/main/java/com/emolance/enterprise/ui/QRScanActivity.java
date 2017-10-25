package com.emolance.enterprise.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
    private Camera camera;
    private int cameraId = 0;
    private SurfaceTexture surfaceTexture = new SurfaceTexture(10);
    private Context context;
    private boolean isFlashOn;
    private Long userId;
    private EmoUser currentEmoUser;
    private String qr;
    private TestReport testReport;
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
        context = QRScanActivity.this;
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
                    qr = s;
                    create(qr);
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
                    takePhotoForProcessing(testReport, new ResultReadyListener() {
                        @Override
                        public void onResult() {
                            Toast.makeText(QRScanActivity.this,"Status: Report is ready.",Toast.LENGTH_SHORT).show();
                            turnOffFlash();
                            QRScanActivity.this.finish();
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(),"No QR code available.",Toast.LENGTH_SHORT).show();
                    QRScanActivity.this.finish();
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

    public void generateTestReportForQR(){
        testReport = new TestReport();
        testReport.setReportCode(qr);
        testReport.setOwner(currentEmoUser);
        testReport.setStatus("Not Tested");

        Call<TestReport> createCall = emolanceAPI.createUserReport(testReport);
        createCall.enqueue(new Callback<TestReport>() {
            @Override
            public void onResponse(Call<TestReport> call, Response<TestReport> response) {
                if (!response.isSuccessful()) {
                    Log.i("ID",String.valueOf(response.body().getId()));
                    Toast.makeText(QRScanActivity.this,
                            "Failed to add the report.", Toast.LENGTH_SHORT).show();
                    QRScanActivity.this.finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Test report has been added.", Toast.LENGTH_SHORT).show();
                    testReport = response.body();
                    rightLayout.setVisibility(View.INVISIBLE);
                    leftLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<TestReport> call, Throwable t) {
                Toast.makeText(QRScanActivity.this,
                        "Failed to add the report.", Toast.LENGTH_SHORT).show();
                QRScanActivity.this.finish();
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
/*        Report report = new Report();
        report.setId(System.currentTimeMillis());
        report.setQrcode(qr);
        report.setName("");
        report.setAge("");
        report.setPosition("");
        report.setProfilePhotoIndex(0);
        report.setTimestamp(System.currentTimeMillis());
        report.setStatus("Ready to Measure");*/

        testReport = new TestReport();
        testReport.setReportCode(qr);
        testReport.setOwner(currentEmoUser);
        testReport.setStatus("Not Tested");

        Call<TestReport> createCall = emolanceAPI.createUserReport(testReport);
        createCall.enqueue(new Callback<TestReport>() {
            @Override
            public void onResponse(Call<TestReport> call, Response<TestReport> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(QRScanActivity.this,
                            "Failed to add the report.", Toast.LENGTH_SHORT).show();
                }
                rightLayout.setVisibility(View.INVISIBLE);
                leftLayout.setVisibility(View.VISIBLE);
                //QRScanActivity.this.finish();
            }

            @Override
            public void onFailure(Call<TestReport> call, Throwable t) {
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
                qr = data.getStringExtra("SCAN_RESULT");
                generateTestReportForQR();
            }
            if(resultCode == RESULT_CANCELED){
                Toast.makeText(getApplicationContext(), "QR SCANNING WAS CANCELED.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //Following functions enable measuring of the test reports
    public void takePhotoForProcessing(final TestReport report, final ResultReadyListener onResultReady) {
        initCamera();
        camera.startPreview();
        isFlashOn=true;
        new Handler().postDelayed(new Runnable(){
            @Override public void run(){
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        try {

                            final File file = new File(
                                    Environment.getExternalStoragePublicDirectory(
                                            Environment.DIRECTORY_PICTURES), report.getId() + "-" + System.currentTimeMillis() + ".jpg");
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(data);
                            fos.close();
                            Log.i("TEST", "Photo taken and saved at " + file.getAbsolutePath() + " with size: " + file.length());

                            // create RequestBody instance from file
                            RequestBody requestFile =
                                    RequestBody.create(MediaType.parse("multipart/form-data"), file);

                            // MultipartBody.Part is used to send also the actual file name
                            MultipartBody.Part body =
                                    MultipartBody.Part.createFormData("image", file.getName(), requestFile);

                            Call<TestReport> responseCall = emolanceAPI.triggerTest(report.getId(), body);
                            responseCall.enqueue(new Callback<TestReport>() {
                                @Override
                                public void onResponse(Call<TestReport> call, Response<TestReport> response) {
                                    onResultReady.onResult();
                                }

                                @Override
                                public void onFailure(Call<TestReport> call, Throwable t) {
                                    Log.e(TAG, "Failed to submit the test report.", t);
                                    Toast.makeText(QRScanActivity.this, "Failed to process test report.", Toast.LENGTH_SHORT).show();
                                    if(isFlashOn){
                                        turnOffFlash();
                                    }
                                    QRScanActivity.this.finish();
                                }
                            });
                        } catch (IOException e) {
                            Log.e("TEST", "Failed", e);
                        }
                    }
                });
            }
        }, 5000);

    }

    private void initCamera() {
        if (!context.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(context, "No camera on this device", Toast.LENGTH_LONG)
                    .show();
        } else {
            cameraId = findBackFacingCamera();
            if (cameraId < 0) {
                Toast.makeText(context, "No front facing camera found.",
                        Toast.LENGTH_LONG).show();
            } else {
                try {
                    camera = Camera.open(cameraId);
                    Camera.Parameters p = camera.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(p);

                    camera.setPreviewTexture(surfaceTexture);

                } catch (IOException e) {
                    Log.e("Camera", e.getMessage(), e);
                }
            }
        }
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Log.d("AdminReport", "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }
    public void turnOffFlash() {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_FLASH)) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
