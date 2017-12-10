package com.emolance.enterprise.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.emolance.enterprise.R;
import com.emolance.enterprise.data.TestReport;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yusun on 5/26/15.
 */
public class UserReportAdapter extends ArrayAdapter<TestReport> {

    private List<TestReport> reports;
    private Context context;
    private UserProfileFragment adminFragment;

    public UserReportAdapter(Context context, List<TestReport> objects, UserProfileFragment adminFragment) {
        super(context, R.layout.list_user_report_item, objects);
        this.context = context;
        sortList(objects);
        this.reports = objects;
        this.adminFragment = adminFragment;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.list_user_report_item, parent, false);

//        DateTime dateTime = new DateTime(reports.get(position).getTimestamp());
//        String dateTimeStr = dateTime.toString(DateTimeFormat.forPattern("MM/dd/yyyy' 'HH:mm")
//                .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

        final TestReport testReport = reports.get(position);

        final View resultColorView = view.findViewById(R.id.resultColor);

        //final Button opButton = (Button) view.findViewById(R.id.opButton);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        final TextView qrText = (TextView) view.findViewById(R.id.qrText);
        qrText.setText(context.getResources().getString(R.string.test_reports_user_profile_qr_id) +": " + testReport.getReportCode());

        final TextView valueText = (TextView) view.findViewById(R.id.statusText);
        String status = testReport.getStatus();
        Integer level = testReport.getLevel();
        final long id = testReport.getId();
        if (status.equals("Done")) {
            valueText.setText(context.getResources().getString(R.string.test_reports_user_profile_status) + ": " + status + " " +
                    context.getResources().getString(R.string.test_reports_user_profile_stress_level) + ": " + level);
            valueText.setTypeface(null, Typeface.BOLD);
            resultColorView.setBackgroundColor(Color.GREEN);

        }
        else {
            valueText.setText(context.getResources().getString(R.string.test_reports_user_profile_status) + ": "
                    + context.getResources().getString(R.string.test_reports_user_profile_incomplete));
            resultColorView.setBackgroundColor(Color.YELLOW);
        }

        return view;
    }

    private void sortList(List<TestReport> reports){
        Collections.sort(reports, new Comparator<TestReport>() {
            public int compare(TestReport lhs, TestReport rhs) {
                return lhs.getReportDateinMillseconds().compareTo(rhs.getReportDateinMillseconds());
            }
        });
        Collections.reverse(reports);
    }
}
