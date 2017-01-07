package com.emolance.enterprise.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.emolance.enterprise.Injector;
import com.emolance.enterprise.R;
import com.emolance.enterprise.data.EmoUser;
import com.emolance.enterprise.service.EmolanceAPI;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    private ProgressDialog progress;
    private Context context;
    private UserListAdapter adminReportAdapter;

    private SurfaceTexture surfaceTexture = new SurfaceTexture(10);

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

        startProgressDialog();
        loadReports();
    }

    private void startProgressDialog() {
        progress = ProgressDialog.show(this.getActivity(), null, "Loading report data ...", true);
    }

    private void endProgressDialog() {
        progress.dismiss();
    }

    @OnClick(R.id.newUserReportButton)
    void takeNewUserReport() {
        Intent intent = new Intent(AdminFragment.this.getActivity(), QRScanActivity.class);
        startActivity(intent);
    }

    public void loadReports() {
        Call<List<EmoUser>> call = emolanceAPI.listMyUsers();
        call.enqueue(new Callback<List<EmoUser>>() {
            @Override
            public void onResponse(Call<List<EmoUser>> call, Response<List<EmoUser>> response) {
                endProgressDialog();
                if (response.isSuccessful()) {
                    adminReportAdapter = new UserListAdapter(context, response.body(), AdminFragment.this);
                    totalTextView.setText(adminReportAdapter.getCount() + " Users");
                    adminReportListView.setAdapter(adminReportAdapter);
                    adminReportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Bundle bundle = new Bundle();
                            bundle.putLong(UserReportsFragment.USER_ID, adminReportAdapter.getItem(i).getId());
                            UserReportsFragment userReportsFragment = new UserReportsFragment();
                            userReportsFragment.setArguments(bundle);
                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.root_container, userReportsFragment);
                            fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right);
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<EmoUser>> call, Throwable t) {
                Log.e("AdminReport", "Failed to get the list of history reports. ");
                endProgressDialog();
            }
        });
    }

}
