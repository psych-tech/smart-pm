package com.emolance.app.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.emolance.app.Injector;
import com.emolance.app.R;
import com.emolance.app.data.Report;
import com.emolance.app.service.EmolanceAPI;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by yusun on 6/23/15.
 */
public class ReportActivity extends FragmentActivity {

    @Inject
    EmolanceAPI emolanceAPI;

    @InjectView(R.id.ageText)
    TextView ageText;
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_report);
        Injector.inject(this);
        ButterKnife.inject(this);

        Long id = getIntent().getLongExtra("id", -1l);
        if (id != -1) {
            emolanceAPI.getReport(id, new Callback<Report>() {
                @Override
                public void success(Report report, Response response) {

                    String link = report.getLink();
                    int indexProfile = link == null ? 0 : Integer.parseInt(link);

                    profileImage.setImageResource(UserReportCreatorActivity.profileList.get(indexProfile));
                    nameText.setText(report.getName());
                    ageText.setText("Age: " + report.getAge());
                    positionText.setText(report.getPosition());

                    if (!report.getStatus().equals("DONE")) {
                        return;
                    }

                    levelText.setText(Integer.toString(report.getLevel()));
                    percentText.setText(Integer.toString(report.getPercent()));
                    beerLevelImage.setImageResource(levelResMap.get(report.getLevel()));
                    DateTime dateTime = DateTime.parse(report.getTimestamp(),
                            DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                                    .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

                    String dateTimeStr = dateTime.toString(DateTimeFormat.forPattern("MM/dd/yyyy' 'HH:mm")
                            .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

                    timeText.setText(dateTimeStr);
                    debugText.setText(report.getResult());
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
    }
}
