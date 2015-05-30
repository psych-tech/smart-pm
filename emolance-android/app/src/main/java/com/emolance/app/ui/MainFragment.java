package com.emolance.app.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.emolance.app.Injector;
import com.emolance.app.R;
import com.emolance.app.data.Report;
import com.emolance.app.service.EmolanceAPI;
import com.emolance.app.util.Constants;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by yusun on 5/22/15.
 */
public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    @InjectView(R.id.checkNowButton)
    Button checkNowButton;
    @InjectView(R.id.historyListView)
    ListView historyListView;
    @InjectView(R.id.currentValue)
    TextView currentValue;
    @InjectView(R.id.currentTime)
    TextView currentTime;
    @InjectView(R.id.swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;
    @InjectView(R.id.mainScrollView)
    ScrollView scrollView;

    @Inject
    EmolanceAPI emolanceAPI;

    private HistoryReportAdapter reportsAdapter;
    private SyncBroadcastReceiver receiver;
    private Context context;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static Fragment newInstance(int sectionNumber) {
        ReportFragment fragment = new ReportFragment();
        //MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        receiver = new SyncBroadcastReceiver();
        this.getActivity().registerReceiver(receiver,
                new IntentFilter(Constants.SYNC_INTENT_FILTER));
        this.context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                syncHistoryData();
            }
        });

        swipeRefreshLayout.setColorScheme(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume called!");
        syncHistoryData();
    }

    @OnClick(R.id.checkNowButton)
    void onClickCheckNowButton() {
        Log.i(TAG, "Trigger the process ... ");
        emolanceAPI.triggerProcess(new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                Log.i(TAG, "Successfully trigger the process.");
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Failed to trigger the process.", error);
            }
        });
    }


    private void syncHistoryData() {
        swipeRefreshLayout.setRefreshing(true);

        emolanceAPI.listReports(new Callback<List<Report>>() {
            @Override
            public void success(List<Report> reports, Response response) {
                //swipeRefreshLayout.setRefreshing(false);
                closeRefreshing();
                // use the first report to update the main screen
                updateLastCheckResult(reports.get(0));
                // remove the first report
                reports.remove(0);
                reportsAdapter = new HistoryReportAdapter(
                        context, reports);
                historyListView.setAdapter(reportsAdapter);

                setListViewHeightBasedOnChildren(historyListView, reportsAdapter);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Failed to get the list of history reports.", error);
                closeRefreshing();
            }
        });
    }

    private void closeRefreshing() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    private void updateLastCheckResult(Report lastReport) {
        currentValue.setText(Double.toString(lastReport.getValue()));

        DateTime dateTime = DateTime.parse(lastReport.getTimestamp(),
                DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));

        String dateTimeStr = dateTime.toString(DateTimeFormat.forPattern("MM/dd/yyyy' 'HH:mm")
                .withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC()));
        currentTime.setText(dateTimeStr);
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public void setListViewHeightBasedOnChildren(ListView listView, HistoryReportAdapter listAdapter) {
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();

        scrollView.fullScroll(ScrollView.FOCUS_UP);
        scrollView.smoothScrollTo(0, 0);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop! unregister");
        this.getActivity().unregisterReceiver(receiver);
    }

    public class SyncBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received sync intent");
            syncHistoryData();
        }
    }
}
