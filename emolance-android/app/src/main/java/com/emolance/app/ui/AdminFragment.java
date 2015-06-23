package com.emolance.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.emolance.app.Injector;
import com.emolance.app.R;
import com.emolance.app.data.Report;
import com.emolance.app.service.EmolanceAPI;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by yusun on 6/22/15.
 */
public class AdminFragment extends Fragment {

    @Inject
    EmolanceAPI emolanceAPI;

    @InjectView(R.id.newUserReportButton)
    ImageButton newUserReportButton;
    @InjectView(R.id.userListView)
    ListView adminReportListView;

    private Context context;
    private AdminReportAdapter adminReportAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        this.context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReports();
    }

    @OnClick(R.id.newUserReportButton)
    void takeNewUserReport() {
        Intent intent = new Intent(context, UserReportCreatorActivity.class);
        startActivity(intent);
    }

    private void loadReports() {
        emolanceAPI.listReports(new Callback<List<Report>>() {
            @Override
            public void success(List<Report> reports, Response response) {
                adminReportAdapter = new AdminReportAdapter(context, reports, emolanceAPI);
                adminReportListView.setAdapter(adminReportAdapter);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("AdminReport", "Failed to get the list of history reports.", error);
            }
        });
    }
}
