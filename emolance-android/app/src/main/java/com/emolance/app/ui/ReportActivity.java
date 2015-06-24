package com.emolance.app.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.emolance.app.Injector;
import com.emolance.app.R;
import com.emolance.app.data.Report;
import com.emolance.app.service.EmolanceAPI;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;

import java.util.Locale;

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
                    nameText.setText(report.getName());
                    levelText.setText(Double.toString(report.getValue()));

                    DateTime dateTime = DateTime.parse(report.getTimestamp(),
                            DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                                    .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

                    String dateTimeStr = dateTime.toString(DateTimeFormat.forPattern("MM/dd/yyyy' 'HH:mm")
                            .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

                    timeText.setText(dateTimeStr);
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
    }
}
