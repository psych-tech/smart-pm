package com.emolance.enterprise.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.emolance.enterprise.R;
import com.emolance.enterprise.data.TestReport;
import com.emolance.enterprise.util.DateUtils;
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
        this.reports = objects;
        this.adminFragment = adminFragment;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.list_user_report_item, parent, false);

//        DateTime dateTime = new DateTime(reports.get(position).getTimestamp());
//        String dateTimeStr = dateTime.toString(DateTimeFormat.forPattern("MM/dd/yyyy' 'HH:mm")
//                .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

        final TestReport testReport = reports.get(position);

        final View resultColorView = view.findViewById(R.id.resultColor);

        //final Button opButton = (Button) view.findViewById(R.id.opButton);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        final TextView levelText = (TextView) view.findViewById(R.id.valueText);
        final TextView statusText = (TextView) view.findViewById(R.id.statusText);
        final TextView dateText = (TextView) view.findViewById(R.id.dateText);
        String status = testReport.getStatus();
        Integer level = testReport.getLevel();
        String date = DateUtils.getDateInMMDDYYYYFormat(testReport.getReportDate());
        if(date == null){
            dateText.setText(context.getResources().getString(R.string.test_reports_user_date_unknown));
        }
        else{
            dateText.setText(date);
        }
        if (status.equals(context.getResources().getString(R.string.test_reports_user_profile_done))) {
            int color = colorPicker(level);
            resultColorView.setBackgroundColor(color);
            statusText.setText(status);
            levelText.setText(String.valueOf(level));
        }
        else {
            statusText.setText(context.getResources().getString(R.string.test_reports_user_profile_incomplete));
            resultColorView.setBackgroundColor(Color.GRAY);
            levelText.setText("N/A");
        }

        return view;
    }

    private int colorPicker(int val){
        if(val <= 2 && val > 0){
            return Color.GREEN;
        }
        else if(val <= 4 && val > 2){
            return Color.YELLOW;
        }
        else if(val <= 6 && val > 4){
            return Color.RED;
        }
        else{
            return Color.GRAY;
        }
    }
}
