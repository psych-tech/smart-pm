package com.emolance.enterprise.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    private UserReportsFragment adminFragment;

    public UserReportAdapter(Context context, List<TestReport> objects, UserReportsFragment adminFragment) {
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

        final ImageView profileImageView = (ImageView) view.findViewById(R.id.testImage);

        final Button opButton = (Button) view.findViewById(R.id.opButton);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        final TextView qrText = (TextView) view.findViewById(R.id.qrText);
        qrText.setText("QR ID: " + testReport.getReportCode());

        final TextView valueText = (TextView) view.findViewById(R.id.statusText);
        String status = testReport.getStatus();
        Integer level = testReport.getLevel();
        final long id = testReport.getId();
        if (status.equals("Done")) {
            profileImageView.setImageResource(R.drawable.test_icon_complete);
            valueText.setText("Status: " + status + "  Stress level: " + level);
            valueText.setTypeface(null, Typeface.BOLD);
        }
        else {
            profileImageView.setImageResource(R.drawable.test_icon_incomplete);
            valueText.setText("Status: Incomplete");
        }

        //if(status.equals("Not Tested") | status.equals("Incomplete")){
            opButton.setText("Measure");
        //}

        if (status.equals("Testing")) {
            opButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        opButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(opButton.getText() == "Measure"){
                    valueText.setText("Status: Testing");
                    testReport.setStatus("Testing");

                    profileImageView.setImageResource(R.drawable.test_icon_incomplete);
                    opButton.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);

                    boolean videoMode = adminFragment.getVideoMode();
                    if(videoMode){
                        MediaRecorder mediaRecorder = adminFragment.getMediaRecorder();
                        if(mediaRecorder != null && !adminFragment.isRecording()){
                            adminFragment.setRecording(true);
                            adminFragment.setCurrentTestReport(testReport);
                            adminFragment.setResultReadyListener(new ResultReadyListener() {
                                @Override
                                public void onResult() {
                                    valueText.setText("Status: Report is ready");
                                    adminFragment.turnOffFlash();
                                    profileImageView.setImageResource(R.drawable.test_icon_complete);
                                    progressBar.setVisibility(View.GONE);
                                    opButton.setVisibility(View.VISIBLE);
                                    //opButton.setText("Report");
                                }
                            });
                            mediaRecorder.start();
                        }
                    }else{
                        adminFragment.takePhotoForProcessing(testReport, new ResultReadyListener() {
                                @Override
                                public void onResult() {
                                    valueText.setText("Status: Report is ready");
                                    adminFragment.turnOffFlash();
                                    profileImageView.setImageResource(R.drawable.test_icon_complete);
                                    progressBar.setVisibility(View.GONE);
                                    opButton.setVisibility(View.VISIBLE);
                                    //opButton.setText("Report");
                                }
                            }
                        );
                    }

                }
                else{
                    Intent intent = new Intent(context, ReportActivity.class);
                    intent.putExtra("id", id);
                    context.startActivity(intent);
                }
            }
        });

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
