package com.emolance.enterprise.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.emolance.enterprise.R;
import com.emolance.enterprise.data.Report;
import com.emolance.enterprise.data.TestReport;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;

import java.util.List;
import java.util.Locale;

/**
 * Created by yusun on 5/26/15.
 */
public class UserReportAdapter extends ArrayAdapter<TestReport> {

    private List<TestReport> reports;
    private Context context;
    private UserReportsFragment adminFragment;

    public UserReportAdapter(Context context, List<TestReport> objects, UserReportsFragment adminFragment) {
        super(context, R.layout.list_user_report_item, objects);
        this.context = context;
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

        TextView nameText = (TextView) view.findViewById(R.id.nameText);
        nameText.setText(reports.get(position).getOwner().getName());

        final ImageView profileImageView = (ImageView) view.findViewById(R.id.profileImage);
        int profileIndex = 0; //reports.get(position).getProfilePhotoIndex();
        profileImageView.setImageResource(UserReportCreatorActivity.profileList.get(profileIndex));

        final Button opButton = (Button) view.findViewById(R.id.opButton);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        final TextView qrText = (TextView) view.findViewById(R.id.qrText);
        qrText.setText("QR ID: " + reports.get(position).getReportCode());

        final TextView valueText = (TextView) view.findViewById(R.id.statusText);
        valueText.setText("Status: " + reports.get(position).getResultValue());

//        if (reports.get(position).getStatus().equals("TESTING")) {
//            opButton.setVisibility(View.GONE);
//            progressBar.setVisibility(View.VISIBLE);
//        }

        opButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                valueText.setText("Status: Testing");
//                reports.get(position).setStatus("Testing");

                opButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                adminFragment.takePhotoForProcessing(reports.get(position), new ResultReadyListener() {
                    @Override
                    public void onResult() {
                        valueText.setText("Status: Report is ready");
                        progressBar.setVisibility(View.GONE);
                        opButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        return view;
    }


}
