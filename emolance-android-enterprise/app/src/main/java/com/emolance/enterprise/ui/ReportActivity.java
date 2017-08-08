package com.emolance.enterprise.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.emolance.enterprise.Injector;
import com.emolance.enterprise.R;
import com.emolance.enterprise.data.TestReport;
import com.emolance.enterprise.service.EmolanceAPI;
import com.emolance.enterprise.util.DateUtils;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by yusun on 6/23/15.
 */
public class ReportActivity extends FragmentActivity {

    private static final String TAG = ReportActivity.class.getName();

    @InjectView(R.id.emailText)
    TextView emailText;
    @InjectView(R.id.nameText)
    TextView nameText;
    @InjectView(R.id.positionText)
    TextView positionText;
    @InjectView(R.id.timeText)
    TextView timeText;
    @InjectView(R.id.levelText)
    TextView levelText;
    @InjectView(R.id.percentText)
    TextView percentText;
    @InjectView(R.id.debugText)
    TextView debugText;
    @InjectView(R.id.beerLevelImage)
    ImageView beerLevelImage;
    @InjectView(R.id.profileImageReport)
    ImageView profileImage;

    @Inject
    EmolanceAPI emolanceAPI;

    private static final Map<Integer, Integer> levelResMap =
            new HashMap<Integer, Integer>() {{
                put(10, R.drawable.beer_level_10);
                put(9, R.drawable.beer_level_9);
                put(8, R.drawable.beer_level_8);
                put(7, R.drawable.beer_level_7);
                put(6, R.drawable.beer_level_6);
                put(5, R.drawable.beer_level_5);
                put(4, R.drawable.beer_level_4);
                put(3, R.drawable.beer_level_3);
                put(2, R.drawable.beer_level_2);
                put(1, R.drawable.beer_level_1);
            }};

    private Long id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_report);
        Injector.inject(this);
        ButterKnife.inject(this);

        id = getIntent().getLongExtra("id", -1l);
        Log.i(TAG, "Open report Id: " + id);

        if (id != -1) {
            Call<TestReport> testReportCall = emolanceAPI.getReport(id);
            testReportCall.enqueue(new Callback<TestReport>() {
                @Override
                public void onResponse(Call<TestReport> call, Response<TestReport> response) {
                    TestReport testReport = response.body();
                    String link = null;
                    int indexProfile = link == null ? 0 : Integer.parseInt(link);

                    if(testReport.getOwner().getProfileImage() != null){
                        String uri = testReport.getOwner().getProfileImage();
                        int imageResource = getResources().getIdentifier(uri,null,getApplicationContext().getPackageName());
                        profileImage.setImageDrawable(getResources().getDrawable(imageResource));
                    }
                    nameText.setText(testReport.getOwner().getName());
                    emailText.setText("Email: " + testReport.getOwner().getEmail());
                    positionText.setText(testReport.getOwner().getPosition());

                    if (!testReport.getStatus().equals("Done")) {
                        return;
                    }

                    levelText.setText(Integer.toString(testReport.getLevel()));
                    percentText.setText(Double.toString(testReport.getPercent()));
                    beerLevelImage.setImageResource(levelResMap.get(testReport.getLevel()));
                    DateTime dateTime = DateTime.parse(testReport.getReportDate(),
                            DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
                                    .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

                    String dateTimeStr = dateTime.toString(DateTimeFormat.forPattern("MM/dd/yyyy' 'HH:mm")
                            .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

                    timeText.setText(dateTimeStr);
                    debugText.setText(testReport.getResultValue());
                }

                @Override
                public void onFailure(Call<TestReport> call, Throwable t) {
                    Log.e(TAG, "Failed to get the test report for user: " + id);
                }
            });
        }
    }
}
