package com.emolance.enterprise.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.emolance.enterprise.R;
import com.emolance.enterprise.data.EmoUser;
import com.emolance.enterprise.data.TestReport;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by David on 3/10/2017.
 */

public class AdminDashboardFragment extends Fragment {

    @InjectView(R.id.totalUsersTextView)
    TextView totalUsersTextView;
    @InjectView(R.id.totalTestsTextView)
    TextView totalTestsTextView;
    @InjectView(R.id.stressStatsTextView)
    TextView stressStatsTextView;
    @InjectView(R.id.adminDashboardBarChart)
    BarChart barChart;
    @InjectView(R.id.adminDashboardPieChart)
    PieChart pieChart;

    private int numUsers;
    private List<EmoUser> myUsers;
    private HashMap<Long, List<TestReport>> reportsHashmap;
    private HashMap<Integer, Integer> levelsHashmap;
    private Typeface font;

    private int[] colors;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_admin_dashboard_alt, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        //Set text style
        Context context = getActivity();

        colors = getResources().getIntArray(R.array.altcolors);
    }

    //Takes the test data from the AdminFragment and adjusts the UI according to the stats
    public void setData(List<EmoUser> list, HashMap<Long, List<TestReport>> hashMap) {
        myUsers = list;
        reportsHashmap = hashMap;
        numUsers = myUsers.size(); //Total number of users
        int numTests = 0; //Total number of tests

        //Create hashmap with keys 0-10 (representing levels of stress) and fill it with 0 values
        levelsHashmap = new HashMap<>();
        for (int i = 0; i < 11; i++) {
            levelsHashmap.put(i, 0);
        }

        //Add to total number of tests and increment the value of each stress level in the hashmap based
        //on the level of the TestReport
        for (EmoUser emoUser : myUsers) {
            long userId = emoUser.getId();
            List<TestReport> tempList = reportsHashmap.get(userId);
            if (tempList != null) {
                numTests += tempList.size();
                for (TestReport test : tempList) {
                    Integer level = test.getLevel();
                    if (level == null) {
                        level = 0;
                    }
                    int temp = levelsHashmap.get(level);
                    temp++; //increment the count
                    levelsHashmap.put(level, temp);
                }
            }
        }

        //Hardcoded sample data (DELETE THIS LATER!)
        levelsHashmap.put(1, 3);
        levelsHashmap.put(2, 4);
        levelsHashmap.put(3, 6);
        levelsHashmap.put(4, 5);
        levelsHashmap.put(5, 8);
        levelsHashmap.put(7, 2);
        levelsHashmap.put(8, 14);
        levelsHashmap.put(10, 5);

        //Create chart data
        List<BarEntry> barEntries = new ArrayList<>();
        List<PieEntry> pieEntries = new ArrayList<>();
        int low = 0, mid = 0, high = 0; //used for making bar chart

        for (Map.Entry<Integer, Integer> entry : levelsHashmap.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            barEntries.add(new BarEntry(key, value));
            if (key < 4) {
                low += value;
            }
            else if (key > 6) {
                high += value;
            }
            else {
                mid += value;
            }
        }

        //Prevent showing the value if it is 0 (makes chart look nicer)
        if (low > 0) {
            String entry = getResources().getString(R.string.dashboard_pie_low);
            pieEntries.add(new PieEntry(low, entry + " (1-3)"));
        }
        if (mid > 0) {
            String entry = getResources().getString(R.string.dashboard_pie_normal);
            pieEntries.add(new PieEntry(mid, entry + " (4-6)"));
        }
        if (high > 0) {
            String entry = getResources().getString(R.string.dashboard_pie_high);
            pieEntries.add(new PieEntry(high, entry + " (7-10)"));
        }

        //Set up pie chart and style it
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setSliceSpace(3); //styling
        pieDataSet.setColors(colors); //styling
        PieData pieData = new PieData(pieDataSet);

        //Percentages will be displayed as integers if they are whole numbers, but will be
        //displayed as decimals if they are not.
        pieData.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                DecimalFormat df = new DecimalFormat("##.#");
                return df.format(value) + "%";
            }
        });
        pieData.setValueTextSize(20f);
        pieData.setValueTextColor(Color.BLACK);
        pieChart.setData(pieData);

        //More styling
        pieChart.getDescription().setText(""); //hide description
        String title = getResources().getString(R.string.dashboard_pie_title);
        pieChart.setCenterText(title);
        pieChart.setCenterTextSize(16);
        pieChart.setCenterTextOffset(0, -5);
        pieChart.setDrawCenterText(true);
        pieChart.setUsePercentValues(true);
        pieChart.setHoleRadius(35);
        pieChart.setTransparentCircleRadius(38);
        pieChart.setDrawSliceText(false);

        //animate the chart data (over 1.5 sec interval)
        pieChart.animateX(1500);
        pieChart.animateY(1500);

        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        barDataSet.setColors(colors);
        BarData barData = new BarData(barDataSet);

        //Set data to be outputted as integers
        barData.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                DecimalFormat df = new DecimalFormat("#,###");
                return df.format(value);
            }
        });
        barChart.setData(barData);
        barChart.getDescription().setText(""); //hide description
        barChart.getLegend().setEnabled(false); //hide legend
        barChart.setFitBars(true);

        //Format X Axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(11);
        xAxis.setDrawGridLines(false);

        //Format Y Axis
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setDrawGridLines(false);

        barChart.animateY(1500); //animate the chart data (over 1.5 sec interval)

        pieChart.invalidate(); //refresh
        barChart.invalidate(); //refresh

        Log.i("TAG", "total tests: " + numTests);
        String str = numUsers + " " + getResources().getString(R.string.dashboard_total_users);
        totalUsersTextView.setText(str);
        str = numTests + " " + getResources().getString(R.string.dashboard_total_tests);
        totalTestsTextView.setText(str);
    }
}