package com.emolance.enterprise.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.emolance.enterprise.DebuggingTools;
import com.emolance.enterprise.Injector;
import com.emolance.enterprise.R;
import com.emolance.enterprise.data.EmoUser;
import com.emolance.enterprise.data.TestReport;
import com.emolance.enterprise.service.EmolanceAPI;
import com.emolance.enterprise.util.Constants;
import com.mitac.cell.device.bcr.McBcrConnection;
import com.mitac.cell.device.bcr.McBcrMessage;
import com.mitac.cell.device.bcr.MiBcrListener;
import com.mitac.cell.device.bcr.utility.BARCODE;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import junit.framework.Test;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestSequenceFragment extends Fragment implements  SurfaceHolder.Callback {

    @Inject
    EmolanceAPI emolanceAPI;
    @InjectView(R.id.backButtonTestScreen)
    ImageButton backButton;
    @InjectView(R.id.surface_view_test_sequence)
    SurfaceView cameraView;


    private Context context;
    private FragmentPagerItemAdapter adapter;
    private ViewPager viewPager;
    private TestQRScanFragment qrScanFragment;
    private TestInsertTestFragment insertTestFragment;
    private TestGenerateReportFragment generateReportFragment;
    private EmoUser currentEmoUser;
    private long userId;
    private McBcrConnection mBcr;
    private String qr;
    private TestReport testReport;
    private boolean hasScanned = false;
    private boolean videoMode;
    private boolean isFlashOn;
    private boolean recording = false;
    private int cameraId = 0;
    private String fileLocation;
    private Camera camera;
    private MediaRecorder mediaRecorder;
    private SurfaceHolder holder;
    private SurfaceTexture surfaceTexture = new SurfaceTexture(10);
    private ResultReadyListener resultReadyListener;
    private final String USER_ID = "user";
    private static final String TAG = QRScanActivity.class.getName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        this.context = getActivity();
        Bundle bundle = getArguments();
        if(bundle != null){
            userId = bundle.getLong(USER_ID);
            currentEmoUser = Paper.book(Constants.DB_EMOUSER).read(Long.toString(userId), null);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_test_sequence, container, false);
        Log.i("TestSequence","OnCreateView");
        ButterKnife.inject(this,rootView);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        qrScanFragment = new TestQRScanFragment();
        insertTestFragment = new TestInsertTestFragment();
        generateReportFragment = new TestGenerateReportFragment();
        adapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(getActivity())
                .add(getResources().getString(R.string.test_sequence_tab_1), qrScanFragment.getClass())
                .add(getResources().getString(R.string.test_sequence_tab_2), insertTestFragment.getClass())
                .add(getResources().getString(R.string.test_sequence_tab_3), generateReportFragment.getClass())
                .create());
        final View touchView = rootView.findViewById(R.id.viewpager);
        touchView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });
        viewPager = (ViewPager) touchView;
        viewPager.setAdapter(adapter);
        SmartTabLayout viewPagerTab = (SmartTabLayout) rootView.findViewById(R.id.viewpagertab);
        viewPagerTab.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if(position == 1){
                    Log.i("Pager scroll test", "turn on camera");
                }
                else if(position == 2){
                    Log.i("Pager scroll test", "measure for result");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPagerTab.setViewPager(viewPager);
        if(videoMode){
            holder = cameraView.getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        for(int i = 0; i <= 2; i++) {
            viewPagerTab.getTabAt(i).setClickable(false);
        }
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(currentEmoUser == null){
            Log.e(TAG, "Failed to find the chosen user. Id: " + userId);
            getChildFragmentManager().beginTransaction().remove(TestSequenceFragment.this).commit();
        }
        else{
            try{
                mBcr = new McBcrConnection(getActivity());
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
                Toast.makeText(context, "Could not use scanner.", Toast.LENGTH_SHORT).show();
                if(mBcr != null){
                    mBcr.close();
                }
            }
        }
    }

    protected void create(String qr) {
        testReport = new TestReport();
        testReport.setReportCode(qr);
        testReport.setOwner(currentEmoUser);
        testReport.setStatus("Not Tested");
        Call<TestReport> createCall = emolanceAPI.createUserReport(testReport);
        createCall.enqueue(new Callback<TestReport>() {
            @Override
            public void onResponse(Call<TestReport> call, Response<TestReport> response) {
                if(response.isSuccessful()){
                    viewPager.setCurrentItem(1);
                    testReport = response.body();
                }
                if (!response.isSuccessful()) {
                    Toast.makeText(getContext(),
                            "Failed to add the report.", Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                }
            }

            @Override
            public void onFailure(Call<TestReport> call, Throwable t) {
                Toast.makeText(getContext(),
                        "Failed to add the report.", Toast.LENGTH_SHORT).show();
                if(mBcr != null){
                    mBcr.close();
                }
                //getActivity().getSupportFragmentManager().beginTransaction().remove(TestSequenceFragment.this).commit();
                getActivity().onBackPressed();
            }
        });
    }

    public void measureTestOnClick(View view){
        if(qr != null){
            viewPager.setCurrentItem(2);
            if(videoMode){
                recording = true;
                mediaRecorder.start();
            }
            else{
                takePhotoForProcessing(testReport, new ResultReadyListener() {
                    @Override
                    public void onResult() {
                        Toast.makeText(getActivity(),"Status: Report is ready.",Toast.LENGTH_SHORT).show();
                        turnOffFlash();
                        FragmentTransaction fm = getFragmentManager().beginTransaction();
                        Fragment testResult = new TestResultFragment();
                        fm.setCustomAnimations(R.anim.slide_in_up,R.anim.slide_out_up);
                        fm.replace(R.id.root_container_right,testResult);
                        fm.commit();
                    }
                });
            }
        }
        else{
            Toast.makeText(getActivity(),"No QR code available.",Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
        }
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
                                    Log.i("TEST", "Image process succeeded.");
                                    onResultReady.onResult();
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
                    camera = Camera.open(cameraId);
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

    private int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Log.d("TestSequenceFragment", "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
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
                    && TestSequenceFragment.this.isRecording()) {
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

                        Call<TestReport> responseCall = emolanceAPI.triggerTestWithVideo(testReport.getId(), body);
                        responseCall.enqueue(new Callback<TestReport>() {
                            @Override
                            public void onResponse(Call<TestReport> call, Response<TestReport> response) {
                                Log.i("TEST", "Video process succeeded.");
                                //Toast.makeText(getActivity(),"Status: Report is ready.",Toast.LENGTH_SHORT).show();
                                turnOffFlash();
                                FragmentTransaction fm = getFragmentManager().beginTransaction();
                                Fragment testResult = new TestResultFragment();
                                fm.setCustomAnimations(R.anim.slide_in_up,R.anim.slide_out_up);
                                fm.replace(R.id.root_container_right,testResult);
                                fm.commit();

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
        }
    };

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
        if (recording) {
            mediaRecorder.stop();
            mediaRecorder.release();
            turnOffFlash();
            recording = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mBcr != null){
            mBcr.stopScan();
            mBcr.stopListening();
        }
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
       /* adapter.destroyItem(viewPager,0,qrScanFragment);
        adapter.destroyItem(viewPager,1,insertTestFragment);
        adapter.destroyItem(viewPager,2,generateReportFragment);
        adapter.notifyDataSetChanged();
        qrScanFragment.onPause();
        qrScanFragment.onStop();
        qrScanFragment.onDestroyView();
        insertTestFragment.onPause();
        insertTestFragment.onStop();
        insertTestFragment.onDestroyView();
        generateReportFragment.onPause();
        generateReportFragment.onStop();
        generateReportFragment.onDestroyView();*/
        if(mBcr != null){
            mBcr.close();
        }
        Log.i("TestSequence","OnDestroy");
    }

    public void setResultReadyListener(ResultReadyListener resultReadyListener) {
        this.resultReadyListener = resultReadyListener;
    }
}
