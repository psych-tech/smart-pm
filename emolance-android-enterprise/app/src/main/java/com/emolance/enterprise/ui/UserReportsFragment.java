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
import android.os.Handler;

import com.emolance.enterprise.Injector;
import com.emolance.enterprise.R;
import com.emolance.enterprise.data.TestReport;
import com.emolance.enterprise.service.EmolanceAPI;
import com.emolance.enterprise.util.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by yusun on 6/22/15.
 */
public class UserReportsFragment extends Fragment {

    private static final String TAG = UserReportsFragment.class.getName();

    @Inject
    EmolanceAPI emolanceAPI;
    @InjectView(R.id.numReports)
    TextView totalTextView;
    @InjectView(R.id.newReportButton)
    ImageButton newUserReportButton;
    @InjectView(R.id.reportListView)
    ListView adminReportListView;

    private ProgressDialog progress;
    private Context context;
    private UserReportAdapter adminReportAdapter;

    private Camera camera;
    private int cameraId = 0;
    private SurfaceTexture surfaceTexture = new SurfaceTexture(10);
    private boolean isFlashOn;
    private Long userId;
    private NewMainActivity activity;
    private List<TestReport> testList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);

        this.context = getActivity();

        Bundle bundle = getArguments();
        if (bundle != null) {
            userId = bundle.getLong(Constants.USER_ID);
            Log.i(TAG, "Getting UserId: " + userId);
        }
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
        View rootView = inflater.inflate(R.layout.fragment_user_reports, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        activity = (NewMainActivity) getActivity();
        activity.setRootContainerVisibility(false);

        startProgressDialog();
        //initCamera();
        loadReports();
    }

    private void startProgressDialog() {
        progress = ProgressDialog.show(this.getActivity(), null, "Loading report data ...", true);
    }

    private void endProgressDialog() {
        progress.dismiss();
    }

    @OnClick(R.id.newReportButton)
    void takeNewUserReport() {
        Intent intent = new Intent(UserReportsFragment.this.getActivity(), QRScanActivity.class);
        intent.putExtra(Constants.USER_ID, userId);
        startActivity(intent);
    }

    public void loadReports() {
        Call<List<TestReport>> call = emolanceAPI.listReports(userId);
        call.enqueue(new Callback<List<TestReport>>() {
            @Override
            public void onResponse(Call<List<TestReport>> call, Response<List<TestReport>> response) {
                endProgressDialog();
                if (response.isSuccessful()) {
                    testList = response.body();
                    NewMainActivity activity = (NewMainActivity) getActivity();
                    activity.transferDataUser(); //transfer the data to the UserProfileFragment
                    adminReportAdapter = new UserReportAdapter(context, testList, UserReportsFragment.this);
                    totalTextView.setText(adminReportAdapter.getCount() + " Test Reports");
                    adminReportListView.setAdapter(adminReportAdapter);
                    adminReportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Intent intent = new Intent(UserReportsFragment.this.getActivity(), ReportActivity.class);
                            intent.putExtra("id", adminReportAdapter.getItem(i).getId());
                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<TestReport>> call, Throwable t) {
                Log.e("AdminReport", "Failed to get the list of history reports. ");
                endProgressDialog();
                Toast.makeText(getActivity(),"Failed to get the list of reports. ",  Toast.LENGTH_SHORT).show();
            }
        });
    }

    public List<TestReport> getTestList() {
        return testList;
    }

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
                                    Log.e(TAG, "Failed to submit the test report.");
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
        //try {
            if (context.getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        //} //catch (Exception e) {
            //Log.e("Camera", e.getMessage(), e);
        //}

        //if (isFlashOn) {
          //  if (camera == null) {
            //    return;
            //}

            //Camera.Parameters params = camera.getParameters();
            //params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            //camera.setParameters(params);
          //  camera.stopPreview();
           // isFlashOn = false;
        //}
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
