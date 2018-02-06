package com.emolance.enterprise.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.emolance.enterprise.DebuggingTools;
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
public class UserReportsFragment extends Fragment implements  SurfaceHolder.Callback {

    private static final String TAG = UserReportsFragment.class.getName();

    @Inject
    EmolanceAPI emolanceAPI;
    @InjectView(R.id.numReports)
    TextView totalTextView;
    @InjectView(R.id.newReportButton)
    ImageButton newUserReportButton;
    @InjectView(R.id.reportListView)
    ListView adminReportListView;
    @InjectView(R.id.surface_view)
    SurfaceView cameraView;

    //private ProgressDialog progress;
    private Context context;
    private UserReportAdapter adminReportAdapter;
    private final int INVALID_PREFERENCE = -1;
    private Camera camera;
    private MediaRecorder mediaRecorder;
    private SurfaceHolder holder;
    private boolean recording = false;
    private int cameraId = 0;
    private SurfaceTexture surfaceTexture = new SurfaceTexture(10);
    private boolean isFlashOn;
    private Long userId;
    private NewMainActivity activity;
    private List<TestReport> testList;
    private Camera.Parameters parameters;
    private String fileLocation;
    private TestReport currentTestReport;
    private ResultReadyListener resultReadyListener;
    private boolean videoMode = false;

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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String val = sharedPreferences.getString(DebuggingTools.KEY_CAMERA_MODE, "NILL");
        if(val.equals("1")){
            Log.i("Video Mode", "Off");
            videoMode = false;
        }
        else if(val.equals("2")){
            Log.i("Video Mode", "On");
            videoMode = true;
        }
    }

    public void initRecorder(){
        camera.unlock();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        mediaRecorder.setOnErrorListener(errorListener);
        mediaRecorder.setOnInfoListener(infoListener);
        CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        mediaRecorder.setProfile(cpHigh);
        fileLocation = "/storage/emulated/0/Pictures/videocapture_example_" + System.currentTimeMillis() +".mp4";
        mediaRecorder.setOutputFile(fileLocation);
        mediaRecorder.setMaxDuration(10000);
        mediaRecorder.setMaxFileSize(5000000);
    }

    public void flash() {
        if(!recording) {
            camera.lock();
        }

        parameters = camera.getParameters();
        parameters.setFlashMode(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH) ? Camera.Parameters.FLASH_MODE_OFF : Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameters);

        if(!recording) {
            camera.unlock();
        }
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Log.e("Error: " + what , String.valueOf(extra));
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            Log.i(String.valueOf(what), " " + mr.getMaxAmplitude() + "");
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED
                    && UserReportsFragment.this.isRecording()) {
                Log.i("MAX SIZE","REACHED.");
                Log.i("TEST", "Video taken and saved at " + fileLocation);
                setRecording(false);
                mediaRecorder.stop();
                mediaRecorder.release();

                Log.i("TEST", "PRE size: " + new File("/storage/emulated/0/Pictures/videocapture_example_1507853678257.mp4").length());
                final File file = new File(fileLocation);
                Log.i("TEST", "FILE SIZE: " + file.length());

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("TEST", "FILE SIZE 2: " + file.length());
                        // create RequestBody instance from file
                        RequestBody requestFile =
                                RequestBody.create(MediaType.parse("multipart/form-data"), file);

                        // MultipartBody.Part is used to send also the actual file name
                        MultipartBody.Part body =
                                MultipartBody.Part.createFormData("video", fileLocation, requestFile);

                        Call<TestReport> responseCall = emolanceAPI.triggerTestWithVideo(currentTestReport.getId(), body);
                        responseCall.enqueue(new Callback<TestReport>() {
                            @Override
                            public void onResponse(Call<TestReport> call, Response<TestReport> response) {
                                Log.i("TEST", "Video process succeeded.");
                                resultReadyListener.onResult();
                                loadReports();
                            }

                            @Override
                            public void onFailure(Call<TestReport> call, Throwable t) {
                                Log.e(TAG, "Failed to submit the test report.");
                                Toast.makeText(getActivity(), "Failed to process test report.", Toast.LENGTH_SHORT).show();
                                if(isFlashOn){
                                    turnOffFlash();
                                }
                            }
                        });
                    }
                }, 10000);
            }

//            if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
//                Log.i("DURATION ","REACHED.");
//                Log.i("TEST", "Photo taken and saved at " + fileLocation);
//                setRecording(false);
//                Bitmap bitmap;
//                FFmpegMediaMetadataRetriever retriever = new  FFmpegMediaMetadataRetriever();
//                try {
//                    retriever.setDataSource(fileLocation); //file's path
//                    bitmap = retriever.getFrameAtTime(1000,FFmpegMediaMetadataRetriever.OPTION_CLOSEST_SYNC );
//                    saveThumbnail(bitmap);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    try{
//                        retriever.setDataSource(fileLocation); //file's path
//                        bitmap = retriever.getFrameAtTime(1000,FFmpegMediaMetadataRetriever.OPTION_CLOSEST_SYNC );
//                        saveThumbnail(bitmap);
//                    }catch (Exception ex){
//                        ex.printStackTrace();
//                    }
//                }
//                finally{
//                    retriever.release();
//                }
//            }
        }
    };

    public void prepareRecorder(){
        if (holder.getSurface() == null){
            Log.e("Error: " , "Surface does not exist.");
            // preview surface does not exist
            return;
        }
        mediaRecorder.setPreviewDisplay(holder.getSurface());

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void initCamera() {
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
                    camera = Camera.open();
                    Camera.Parameters p = camera.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(p);
                    if(videoMode){
                        camera.setPreviewDisplay(holder);
                        camera.startPreview();
                        mediaRecorder = new MediaRecorder();
                    }
                    else{
                        camera.setPreviewTexture(surfaceTexture);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*private void saveThumbnail(Bitmap bitmap){
        FileOutputStream out = null;
        try {
            final File file = new File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES), "ID" + "-" + System.currentTimeMillis() + ".png");
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored

            Log.i("TEST", "Thumbnail saved at " + file.getAbsolutePath() + " with size: " + file.length());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_reports, container, false);
        ButterKnife.inject(this, rootView);
        if(videoMode){
            holder = cameraView.getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
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
        //progress = ProgressDialog.show(this.getActivity(), null, "Loading report data ...", true);
    }

    private void endProgressDialog() {
        //progress.dismiss();
    }

    @OnClick(R.id.newReportButton)
    void takeNewUserReport() {
        Intent intent = new Intent(UserReportsFragment.this.getActivity(), QRScanActivity.class);
        intent.putExtra(Constants.USER_ID, userId);
        startActivityForResult(intent, 200);
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
                    adminReportAdapter = new UserReportAdapter(context, testList, new UserProfileFragment());
                    totalTextView.setText(adminReportAdapter.getCount() + context.getResources().getString(R.string.test_reports_user_profile_testing));
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
                                    resultReadyListener.onResult();
                                    onResultReady.onResult();
                                    loadReports();
                                }

                                @Override
                                public void onFailure(Call<TestReport> call, Throwable t) {
                                    Log.e(TAG, "Failed to submit the test report.");
                                    Toast.makeText(getActivity(), "Failed to process test report.", Toast.LENGTH_SHORT).show();
                                    if(isFlashOn){
                                        turnOffFlash();
                                    }

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
            if (context.getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                if (camera != null) {
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            }
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public MediaRecorder getMediaRecorder() {
        return mediaRecorder;
    }

    public boolean getVideoMode(){
        return videoMode;
    }

    @Override
    public void onPause() {
        if (camera != null) {
            camera.release();
            camera = null;
            if(mediaRecorder != null){
                mediaRecorder.release();
            }
        }
        super.onPause();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera();
        initRecorder();
        prepareRecorder();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("Destroyed","Surface");
        if (recording) {
            mediaRecorder.stop();
            mediaRecorder.release();
            turnOffFlash();
            recording = false;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(camera != null){
            if(mediaRecorder != null){
                mediaRecorder.stop();
                mediaRecorder.release();
            }
            camera.release();
            camera = null;
        }
    }

    public TestReport getCurrentTestReport() {
        return currentTestReport;
    }

    public void setCurrentTestReport(TestReport currentTestReport) {
        this.currentTestReport = currentTestReport;
    }

    public void setResultReadyListener(ResultReadyListener resultReadyListener) {
        this.resultReadyListener = resultReadyListener;
    }
}
