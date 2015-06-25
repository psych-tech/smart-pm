package com.emolance.app.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
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
 * Created by yusun on 6/22/15.
 */
public class AdminFragment extends Fragment {

    @Inject
    EmolanceAPI emolanceAPI;
    @InjectView(R.id.numResult)
    TextView totalTextView;
    @InjectView(R.id.newUserReportButton)
    ImageButton newUserReportButton;
    @InjectView(R.id.userListView)
    ListView adminReportListView;

    private Context context;
    private AdminReportAdapter adminReportAdapter;
    private ReportsSyncBroadcastReceiver receiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        receiver = new ReportsSyncBroadcastReceiver();
        this.getActivity().registerReceiver(receiver,
                new IntentFilter(Constants.SYNC_INTENT_FILTER));

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
        qrButtonTake();
    }

    void qrButtonTake() {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes

            startActivityForResult(intent, 0);
        } catch (Exception e) {
            Log.w("QR", "Failed to start the activity. Try suggest");
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
            startActivity(marketIntent);
        }
    }

    public void loadReports() {
        emolanceAPI.listReports(new Callback<List<Report>>() {
            @Override
            public void success(List<Report> reports, Response response) {
                adminReportAdapter = new AdminReportAdapter(context, reports, emolanceAPI);
                totalTextView.setText(adminReportAdapter.getCount() + " Test Results");
                adminReportListView.setAdapter(adminReportAdapter);
                adminReportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(AdminFragment.this.getActivity(), ReportActivity.class);
                        intent.putExtra("id", adminReportAdapter.getItem(i).getId());
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("AdminReport", "Failed to get the list of history reports.", error);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String qrCode = null;
        Log.i("TEST", "reqCode: " + requestCode + " resultCode: " + resultCode);
        if (requestCode == 0) {
            qrCode = data.getStringExtra("SCAN_RESULT");
            Intent intent = new Intent(AdminFragment.this.getActivity(), UserReportCreatorActivity.class);
            intent.putExtra("qr", qrCode);
            startActivity(intent);
        }
    }

    public class ReportsSyncBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Receive", "Received sync intent");
            loadReports();
        }
    }

}
