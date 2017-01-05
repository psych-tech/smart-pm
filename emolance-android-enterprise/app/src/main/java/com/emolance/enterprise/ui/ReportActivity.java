package com.emolance.enterprise.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.emolance.enterprise.Injector;
import com.emolance.enterprise.R;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by yusun on 6/23/15.
 */
public class ReportActivity extends FragmentActivity {

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
        Log.i("TEST", "Report Id: " + id);

        if (id != -1) {
//            ref = new Firebase("https://emolance.firebaseio.com/reports/" + id);
//            ref.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Report report = dataSnapshot.getValue(Report.class);
//
//                    int indexProfile = report.getProfilePhotoIndex();
//
//                    profileImage.setImageResource(UserReportCreatorActivity.profileList.get(indexProfile));
//                    nameText.setText(report.getName());
//                    ageText.setText("Age: " + report.getAge());
//                    positionText.setText(report.getPosition());
//
////                    if (!report.getStatus().equals("DONE")) {
////                        return;
////                    }
//
//                    levelText.setText(Integer.toString(report.getLevel()));
//                    percentText.setText(Integer.toString(report.getPercent()));
//                    beerLevelImage.setImageResource(levelResMap.get(report.getLevel()));
//
//                    DateTime dateTime = new DateTime(report.getTimestamp());
//
////                    DateTime.parse(Long.toString(report.getTimestamp()),
////                            DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
////                                    .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));
//
//                    String dateTimeStr = dateTime.toString(DateTimeFormat.forPattern("MM/dd/yyyy' 'HH:mm")
//                            .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));
//
//                    timeText.setText(dateTimeStr);
//                    debugText.setText(report.getResult());
//                }
//
//                @Override
//                public void onCancelled(FirebaseError firebaseError) {
//                    Log.e("TEST", firebaseError.getDetails());
//                }
//            });

//            emolanceAPI.getReport(id, new Callback<Report>() {
//                @Override
//                public void success(Report report, Response response) {
//
//                    String link = report.getLink();
//                    int indexProfile = link == null ? 0 : Integer.parseInt(link);
//
//                    profileImage.setImageResource(UserReportCreatorActivity.profileList.get(indexProfile));
//                    nameText.setText(report.getName());
//                    ageText.setText("Age: " + report.getAge());
//                    positionText.setText(report.getPosition());
//
//                    if (!report.getStatus().equals("DONE")) {
//                        return;
//                    }
//
//                    levelText.setText(Integer.toString(report.getLevel()));
//                    percentText.setText(Integer.toString(report.getPercent()));
//                    beerLevelImage.setImageResource(levelResMap.get(report.getLevel()));
//                    DateTime dateTime = DateTime.parse(Long.toString(report.getTimestamp()),
//                            DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
//                                    .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));
//
//                    String dateTimeStr = dateTime.toString(DateTimeFormat.forPattern("MM/dd/yyyy' 'HH:mm")
//                            .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));
//
//                    timeText.setText(dateTimeStr);
//                    debugText.setText(report.getResult());
//                }
//
//                @Override
//                public void failure(RetrofitError error) {
//
//                }
//            });

        }
    }
}
