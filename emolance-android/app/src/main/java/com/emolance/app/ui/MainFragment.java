package com.emolance.app.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.emolance.app.Injector;
import com.emolance.app.R;
import com.emolance.app.data.Report;
import com.emolance.app.service.EmolanceAPI;
import com.emolance.app.util.Constants;

import java.util.List;

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

    @Inject
    EmolanceAPI emolanceAPI;

    private HistoryReportAdapter reportsAdapter;
    private SyncBroadcastReceiver receiver;

    private ProgressDialog progressDialog;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MainFragment newInstance(int sectionNumber) {
        MainFragment fragment = new MainFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);

        //syncHistoryData();

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
        progressDialog = ProgressDialog.show(
                this.getActivity(), "Please Wait ...", "Syncing your pressure data", true);

        emolanceAPI.listReports(new Callback<List<Report>>() {
            @Override
            public void success(List<Report> reports, Response response) {
                progressDialog.dismiss();
                // use the last report to update the main screen
                updateLastCheckResult(reports.get(reports.size() - 1));
                // remove the last report
                reports.remove(reports.size() - 1);
                reportsAdapter = new HistoryReportAdapter(
                        MainFragment.this.getActivity(), reports);
                historyListView.setAdapter(reportsAdapter);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "Failed to get the list of history reports.", error);
                progressDialog.dismiss();
            }
        });
    }

    private void updateLastCheckResult(Report lastReport) {
        currentValue.setText(Double.toString(lastReport.getValue()));
        currentTime.setText(lastReport.getTimestamp());
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
