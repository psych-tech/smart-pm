package com.emolance.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.emolance.app.Injector;
import com.emolance.app.R;
import com.emolance.app.service.EmolanceAPI;

import java.util.ArrayList;
import java.util.List;

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
    @InjectView(R.id.ageEditText)
    EditText ageEditText;
    @InjectView(R.id.professionEditText)
    EditText positionEditText;

    @InjectView(R.id.profileImageSelector)
    ImageView profileImage;

    private int profileIndex = 0;

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

    @OnClick(R.id.createButton)
    void createButton() {
        Log.i("TEST", "Called!");
        emolanceAPI.createUserReport(
                qrIdText.getText().toString(),
                nameEditText.getText().toString(),
                Integer.toString(profileIndex),
                ageEditText.getText().toString(),
                positionEditText.getText().toString(),
                new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Toast.makeText(UserReportCreatorActivity.this,
                                "Added a report successfully.", Toast.LENGTH_SHORT).show();
                        UserReportCreatorActivity.this.finish();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(UserReportCreatorActivity.this,
                                "Failed to added the report.", Toast.LENGTH_SHORT).show();
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
