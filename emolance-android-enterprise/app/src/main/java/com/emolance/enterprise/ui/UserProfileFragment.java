package com.emolance.enterprise.ui;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.emolance.enterprise.R;
import com.emolance.enterprise.data.EmoUser;
import com.emolance.enterprise.data.TestReport;
import com.emolance.enterprise.service.EmolanceAPI;
import com.emolance.enterprise.util.Constants;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by David on 3/10/2017.
 */

public class UserProfileFragment extends Fragment {

    private Long userId;
    private static final String TAG = UserReportsFragment.class.getName();

    @InjectView(R.id.profileImage)
    ImageView imageView;
    @InjectView(R.id.userDashboardLineChart)
    LineChart lineChart;
    @InjectView(R.id.lineChartTitle)
    TextView lineChartTitle;
    @InjectView(R.id.lineChartYAxisLabel)
    TextView lineChartYAxisLabel;
    @InjectView(R.id.lineChartXAxisLabel)
    TextView lineChartXAxisLabel;
    @InjectView(R.id.userProfileTextViewName)
    TextView userProfileTextViewName;
    @InjectView(R.id.userProfileTextViewAge)
    TextView userProfileTextViewAge;
    @InjectView(R.id.userProfileTextViewPosition)
    TextView userProfileTextViewPosition;

    private long[] timestamps;
    private SimpleDateFormat inputFormatter, outputFormatter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle bundle = getArguments();
        if (bundle != null) {
            userId = bundle.getLong(Constants.USER_ID);
            String userName = bundle.getString(Constants.USER_NAME);
            String userAge = bundle.getString(Constants.USER_AGE);
            String userPosition = bundle.getString(Constants.USER_POSITION);
            String userImage = bundle.getString(Constants.USER_IMAGE);
            Log.i(TAG, "Getting UserId: " + userId);

            inputFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            outputFormatter = new SimpleDateFormat("MM/dd");
            if (userAge != null) {
                try {
                    Date date = inputFormatter.parse(userAge);
                    String str = outputFormatter.format(date);
                    userProfileTextViewAge.setText("Date of Birth: " + str);
                }
                catch (ParseException e) {
                }
            }
            if (userName != null) {
                userProfileTextViewName.setText(userName);
            }
            if (userPosition != null) {
                userProfileTextViewPosition.setText(userPosition);
            }
            imageView.setImageResource(UserReportCreatorActivity.profileList.get(0));
        }
    }

    //Handle setting up chart from the data
    public void setData(List<TestReport> tests) {
        //Sort the reports chronologically
        Collections.sort(tests, new Comparator<TestReport>() {
            public int compare(TestReport o1, TestReport o2) {
                if (o1.getReportDate() == null || o2.getReportDate() == null)
                    return 0;
                return o1.getReportDate().compareTo(o2.getReportDate());
            }
        });

        //fill lineEntries with data
        List<Entry> lineEntries = new ArrayList<>();
        Date d;
        timestamps = new long[tests.size()];
        //starting index of the tests being displayed
        int offset = 0;
        if(tests.size() > 10){
            offset = tests.size() - 10;
        }
        //Set up the chart data from the tests
        for (int i = offset; i < tests.size(); i++) {
            TestReport testReport = tests.get(i);
            if (testReport != null) {
                Integer stressLevel = testReport.getLevel();
                if (stressLevel == null) {
                    stressLevel = 0;
                }
                String testDate = testReport.getReportDate();

                try {
                    d = inputFormatter.parse(testDate);
                    timestamps[i - offset] = d.getTime();
                    lineEntries.add(new Entry(i - offset, stressLevel));
                }
                catch (ParseException e) {
                    Toast.makeText(getActivity(), "Error getting test date",  Toast.LENGTH_LONG).show();
                }
            }
        }

        //Set up the chart if their is data
        if (lineEntries.size() > 1) {
            lineChartTitle.setVisibility(View.VISIBLE);
            lineChartXAxisLabel.setVisibility(View.VISIBLE);
            lineChartYAxisLabel.setVisibility(View.VISIBLE);
            LineDataSet lineDataSet = new LineDataSet(lineEntries, "");
            lineDataSet.setColors(getResources().getIntArray(R.array.altcolors));
            LineData lineData = new LineData(lineDataSet);

            //Set data to be outputted as integers
            lineData.setValueFormatter(new IValueFormatter() {
                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    DecimalFormat df = new DecimalFormat("##");
                    return df.format(value);
                }
            });

            lineChart.setData(lineData);
            lineChart.getDescription().setText(""); //hide description
            lineChart.getLegend().setEnabled(false); //hide legend

            //Format X Axis labels to show dates
            IAxisValueFormatter dateFormatter = new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    if((int) value < timestamps.length){
                        return outputFormatter.format(timestamps[(int) value]);
                    }
                    return "";
                }
            };
            //Format Y Axis labels to show integers
            IAxisValueFormatter stressLevelFormatter = new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return "" + (int) value;
                }
            };

            //Format X Axis labels
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(lineEntries.size());
            xAxis.setValueFormatter(dateFormatter);
            xAxis.setGranularity(1);
            xAxis.setAxisMinimum(0);
            if(tests.size() < 10){
                xAxis.setAxisMaximum(tests.size() - 1);
            }else{
                xAxis.setAxisMaximum(9);
            }
            xAxis.setAvoidFirstLastClipping(true);

            //Format Y Axis Labels
            lineChart.getAxisRight().setEnabled(false);
            YAxis yAxis = lineChart.getAxisLeft();
            yAxis.setLabelCount(11);
            yAxis.setGranularity(1);
            yAxis.setAxisMinimum(0);
            yAxis.setAxisMaximum(10);
            yAxis.setValueFormatter(stressLevelFormatter);

            lineChart.setExtraRightOffset(25);
            lineChart.animateY(1500); //animate the chart data (over 1.5 sec interval)
            lineChart.invalidate(); //refresh
        }

        else {
            //Show "No data available"
            lineChartTitle.setVisibility(View.GONE);
            lineChartXAxisLabel.setVisibility(View.GONE);
            lineChartYAxisLabel.setVisibility(View.GONE);
            Paint p = lineChart.getPaint(Chart.PAINT_INFO);
            p.setTextSize(25);
            p.setColor(getResources().getColor(R.color.darkblue));
        }
    }
}