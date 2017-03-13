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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
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
        Context context = getActivity();
        font = Typeface.createFromAsset(context.getAssets(), "fonts/DataGothic.otf"); //create font

        totalUsersTextView.setTypeface(font);
        totalTestsTextView.setTypeface(font);
        stressStatsTextView.setTypeface(font);
        stressStatsTextView.setPaintFlags(stressStatsTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
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

        //Create chart data
        List<BarEntry> barEntries = new ArrayList<>();
        List<PieEntry> pieEntries = new ArrayList<>();
        int low = 0, mid = 0, high = 0; //used for making bar chart

        for (Map.Entry<Integer, Integer> entry : levelsHashmap.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            barEntries.add(new BarEntry(key, value));
            if (value > 0) {
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
        }

        //Prevent showing the value if it is 0 (makes chart look nicer)
        if (low > 0) {
            pieEntries.add(new PieEntry(low, "1-3"));
        }
        if (mid > 0) {
            pieEntries.add(new PieEntry(mid, "4-6"));
        }
        if (high > 0) {
            pieEntries.add(new PieEntry(high, "7-10"));
        }

        //Set up pie chart and style it
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setSliceSpace(3); //styling
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS); //styling
        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(20f);
        pieData.setValueTextColor(Color.BLACK);
        pieChart.setData(pieData);

        //More styling
        pieChart.getDescription().setText("");
        pieChart.setUsePercentValues(true);
        pieChart.setHoleRadius(20);
        pieChart.setTransparentCircleRadius(25);
        pieChart.getLegend().setEnabled(false); //hide legend

        //animate the chart data (over 1.5 sec interval)
        pieChart.animateX(1500);
        pieChart.animateY(1500);

        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        BarData barData = new BarData(barDataSet);
        barData.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                DecimalFormat df = new DecimalFormat("#,###");
                return df.format(value);
            }
        });
        barChart.setData(barData);
        barChart.getDescription().setText("");
        barChart.setFitBars(true);
        barChart.getLegend().setEnabled(false);

        //Format X Axis labels
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(11);

        //Format Y Axis Labels
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMinimum(0f);

        barChart.animateY(1500); //animate the chart data (over 1.5 sec interval)

        pieChart.invalidate(); //refresh
        barChart.invalidate(); //refresh

        Log.i("TAG", "total tests: " + numTests);
        String str = numUsers + " Total Users";
        totalUsersTextView.setText(str);
        str = numTests + " Total Tests";
        totalTestsTextView.setText(str);
    }
}