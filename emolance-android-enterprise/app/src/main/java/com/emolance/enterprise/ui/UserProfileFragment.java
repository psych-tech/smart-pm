package com.emolance.enterprise.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.emolance.enterprise.R;
import com.emolance.enterprise.data.TestReport;
import com.emolance.enterprise.util.Constants;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import junit.framework.Test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by David on 3/10/2017.
 */

public class UserProfileFragment extends Fragment {

    private Long userId;
    private static final String TAG = UserReportsFragment.class.getName();

    @InjectView(R.id.userDashboardLineChart)
    LineChart lineChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            userId = bundle.getLong(Constants.USER_ID);
            Log.i(TAG, "Getting UserId: " + userId);
        }
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

        //
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
        for (TestReport testReport : tests) {
            if (testReport != null) {
                Integer stressLevel = testReport.getLevel();
                String testDate = testReport.getReportDate();
                lineEntries.add(new Entry(new Random().nextInt(10), new Random().nextInt(10)));
            }
        }

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

        //Format X Axis labels
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        //Format Y Axis Labels
        lineChart.getAxisRight().setEnabled(false);
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setLabelCount(11);
        yAxis.setAxisMinimum(0f);

        lineChart.animateY(1500); //animate the chart data (over 1.5 sec interval)
        lineChart.invalidate(); //refresh
    }
}