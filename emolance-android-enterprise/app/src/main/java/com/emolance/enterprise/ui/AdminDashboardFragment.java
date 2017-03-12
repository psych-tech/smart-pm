package com.emolance.enterprise.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.widget.TextView;

import com.emolance.enterprise.R;
import com.emolance.enterprise.data.EmoUser;
import com.emolance.enterprise.data.TestReport;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;

import java.util.HashMap;
import java.util.List;

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
    @InjectView(R.id.adminDashboardBarChart)
    BarChart barChart;
    @InjectView(R.id.adminDashboardPieChart)
    PieChart pieChart;

    private int numUsers;
    private List<EmoUser> myUsers;
    private HashMap<Long, List<TestReport>> reportsHashmap;
    private Typeface font;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Context context = getActivity();
        font = Typeface.createFromAsset(context.getAssets(), "fonts/Orena.ttf"); //create font

        //totalUsersTextView.setTypeface(font);
        //totalTestsTextView.setTypeface(font);
    }

    //Takes the test data from the AdminFragment and adjusts the UI according to the stats
    public void setData(List<EmoUser> list, HashMap<Long, List<TestReport>> hashMap) {
        myUsers = list;
        reportsHashmap = hashMap;
        numUsers = myUsers.size();
        int numTests = 0;

        for (EmoUser emoUser : myUsers) {
            long userId = emoUser.getId();
            List<TestReport> tempList = reportsHashmap.get(userId);
            if (tempList != null) {
                numTests += tempList.size();
                
            }
        }
        Log.i("TAG", "total " + numTests);
        String str = numUsers + " Total Users";
        totalUsersTextView.setText(str);
        str = numTests + " Total Tests";
        totalTestsTextView.setText(str);
    }
}