package com.emolance.enterprise.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.emolance.enterprise.Injector;
import com.emolance.enterprise.R;
import com.emolance.enterprise.data.Report;
import com.emolance.enterprise.service.EmolanceAPI;
import com.emolance.enterprise.service.ImageColorAnalyzer;
import com.emolance.enterprise.service.TestResult;
import com.emolance.enterprise.util.BackupTask;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by yusun on 6/22/15.
 */
public class AdminFragment extends Fragment {

    @Inject
    EmolanceAPI emolanceAPI;
    @InjectView(R.id.numResult)
    TextView totalTextView;
    @InjectView(R.id.newUserReportButton)
    ImageButton newUserReportButton;
    @InjectView(R.id.userListView)
    ListView adminReportListView;

    private ProgressDialog progress;
    private Context context;
    private AdminReportAdapter adminReportAdapter;

    private Camera camera;
    private int cameraId = 0;
    private SurfaceTexture surfaceTexture = new SurfaceTexture(10);


    private Firebase ref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        Firebase.setAndroidContext(getActivity());
        ref = new Firebase("https://emolance.firebaseio.com");

        this.context = getActivity();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        startProgressDialog();
        initCamera();
        loadReports();
    }

    private void startProgressDialog() {
        progress = ProgressDialog.show(this.getActivity(), "Please Wait",
                "Loading report data ...", true);
    }

    private void endProgressDialog() {
        progress.dismiss();
    }

    @OnClick(R.id.newUserReportButton)
    void takeNewUserReport() {
        Intent intent = new Intent(AdminFragment.this.getActivity(), QRScanActivity.class);
        startActivity(intent);
    }

    public void loadReports() {
        ref.child("reports").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("TEST", "Get reports: " + dataSnapshot);
                List<Report> reports = new ArrayList<Report>();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    Report r = ds.getValue(Report.class);
                    reports.add(r);
                }

                adminReportAdapter = new AdminReportAdapter(context, reports, AdminFragment.this);
                totalTextView.setText(adminReportAdapter.getCount() + " Test Results");
                adminReportListView.setAdapter(adminReportAdapter);
                adminReportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(AdminFragment.this.getActivity(), ReportActivity.class);
                        intent.putExtra("id", adminReportAdapter.getItem(i).getId());
                        startActivity(intent);
                    }
                });
                endProgressDialog();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("AdminReport", "Failed to get the list of history reports. "
                        + firebaseError.getMessage());
                endProgressDialog();
            }
        });

    }

    public void takePhotoForProcessing(final Report report, final ResultReadyListener onResultReady) {
        camera.startPreview();
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                try {
                    final File file = new File(
                            Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES), report.getId() + "-" + report.getTimestamp() + ".jpg");
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(data);
                    fos.close();
                    Log.i("TEST", "Photo taken and saved at " + file.getAbsolutePath() + " with size: " + file.length());

                    // analyze here
                    TestResult result = new ImageColorAnalyzer(file).marchThroughImage();
                    report.setValue1(result.getScaledCortisol());
                    report.setValue2(result.getScaledDHEA());
                    report.setTimestamp(System.currentTimeMillis());
                    report.setStatus("Report is ready");

                    ref.child("reports/" + report.getId()).setValue(report, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            Log.i("TEST", "Updated the report");
                            onResultReady.onResult();
                            // backup automatically
                            new BackupTask(ref, file).execute(report);
                        }
                    });
                } catch (IOException e) {
                    Log.e("TEST", "Failed", e);
                }
            }
        });
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

    @Override
    public void onPause() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        super.onPause();
    }

}
