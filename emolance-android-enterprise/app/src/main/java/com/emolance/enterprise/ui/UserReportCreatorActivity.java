package com.emolance.enterprise.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.emolance.enterprise.Injector;
import com.emolance.enterprise.R;
import com.emolance.enterprise.data.Report;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.mitac.cell.device.bcr.McBcrConnection;
import com.mitac.cell.device.bcr.McBcrMessage;
import com.mitac.cell.device.bcr.MiBcrListener;
import com.mitac.cell.device.bcr.utility.BARCODE;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by yusun on 6/22/15.
 */
public class UserReportCreatorActivity extends FragmentActivity {


    @InjectView(R.id.qrButton)
    Button qrButton;
    @InjectView(R.id.qrIdText)
    TextView qrIdText;
    @InjectView(R.id.nameEditText)
    EditText nameEditText;
    @InjectView(R.id.ageEditText)
    EditText ageEditText;
    @InjectView(R.id.professionEditText)
    EditText positionEditText;

    @InjectView(R.id.profileImageSelector)
    ImageView profileImage;

    private McBcrConnection mBcr;	// McBcrConnection help BCR control

    private int profileIndex = 0;
    private Firebase ref;

    public static final List<Integer> profileList =
            new ArrayList<Integer>() {{
                add(R.drawable.persona_landing_1);
                add(R.drawable.persona_landing_2);
                add(R.drawable.persona_landing_3);
                add(R.drawable.persona_landing_4);
                add(R.drawable.persona_landing_5);
                add(R.drawable.persona_landing_6);
                add(R.drawable.persona_landing_7);
            }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        this.setContentView(R.layout.activity_new_user_report);
        ButterKnife.inject(this);

        Firebase.setAndroidContext(this);
        ref = new Firebase("https://emolance.firebaseio.com");

        mBcr = new McBcrConnection(this);

        mBcr.setListener(new MiBcrListener() {
            @Override
            public void onScanned(String s, BARCODE.TYPE type, int i) {
                Log.i("User", s + " " + type + " " + i);
                qrIdText.setText(s);
                qrButton.setText("Not You, Rescan?");
            }

            @Override
            public void onStatusChanged(int i) {
                Log.i("User", "Status: " + i);
                if (i == McBcrMessage.Status_Ready || i == McBcrMessage.Status_ServiceConnected) {
                    mBcr.set("must://bcr/bcr?keybd_wedge=false");
                    Log.i("User", "Set keybd to false.");
                }
            }
        });

        String qrCode = this.getIntent().getStringExtra("qr");
        if (qrCode != null) {
            qrIdText.setText(qrCode);
        }

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileIndex = (profileIndex + 1) % profileList.size();
                int next = profileList.get(profileIndex);
                profileImage.setImageResource(next);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBcr.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBcr.stopListening();
    }

    @Override
    protected void onDestroy() {
        mBcr.close();
        super.onDestroy();
    }

    @OnClick(R.id.createButton)
    void createButton() {
        Report report = new Report();
        report.setId(System.currentTimeMillis());
        report.setQrcode(qrIdText.getText().toString());
        report.setName(nameEditText.getText().toString());
        report.setAge(ageEditText.getText().toString());
        report.setPosition(positionEditText.getText().toString());
        report.setProfilePhotoIndex(profileIndex);
        report.setTimestamp(System.currentTimeMillis());
        report.setStatus("Ready to Measure");

        ref.child("reports/" + report.getId()).setValue(report, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Toast.makeText(UserReportCreatorActivity.this,
                                "Failed to added the report.", Toast.LENGTH_SHORT).show();
                    UserReportCreatorActivity.this.finish();
                } else {
                    Toast.makeText(UserReportCreatorActivity.this,
                                "Added a report successfully.", Toast.LENGTH_SHORT).show();
                    UserReportCreatorActivity.this.finish();
                }
            }
        });

    }

    @OnClick(R.id.qrButton)
    void qrButtonTake() {
        mBcr.startListening();
        mBcr.scan(true);
    }

}
