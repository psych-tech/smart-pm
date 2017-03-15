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
import android.widget.ListView;
import android.widget.Toast;

import com.emolance.enterprise.Injector;
import com.emolance.enterprise.R;
import com.emolance.enterprise.data.EmoUser;
import com.emolance.enterprise.data.TestReport;
import com.emolance.enterprise.service.EmolanceAPI;
import com.emolance.enterprise.util.Constants;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by yusun on 6/22/15.
 */
public class AdminFragment extends Fragment {

    @Inject
    EmolanceAPI emolanceAPI;
    @InjectView(R.id.userListView)
    ListView adminReportListView;

    private ProgressDialog progress;
    private Context context;
    private UserListAdapter adminReportAdapter;

    //Used to send data to AdminDashboardFragment
    private List<EmoUser> myUsers;
    private HashMap<Long, List<TestReport>> hashMap;
    private EmoUser emoUser;
    private int counter;
    private NewMainActivity activity;
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

        activity = (NewMainActivity) getActivity();
        activity.setRootContainerVisibility(false);
        startProgressDialog();
        loadReports();
    }

    private void startProgressDialog() {
        progress = ProgressDialog.show(this.getActivity(), null, "Loading report data ...", true);
    }

    private void endProgressDialog() {
        progress.dismiss();
    }

    public void loadReports() {
        Call<List<EmoUser>> call = emolanceAPI.listMyUsers();
        call.enqueue(new Callback<List<EmoUser>>() {
            @Override
            public void onResponse(Call<List<EmoUser>> call, Response<List<EmoUser>> response) {
                if (response.isSuccessful()) {
                    myUsers = response.body();
                    counter = 0;
                    hashMap = new HashMap<>(); //used to map tests to users
                    loadHashmap();
                    adminReportAdapter = new UserListAdapter(context, myUsers, AdminFragment.this);
                    adminReportListView.setAdapter(adminReportAdapter);
                    adminReportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            openUserTestsFragment(i);
                        }
                    });
                    persistLoadedMyUsers(myUsers);
                }
            }

            @Override
            public void onFailure(Call<List<EmoUser>> call, Throwable t) {
                Log.e("AdminReport", "Failed to get the list of history reports. ");
                Toast.makeText(getActivity(),"Failed to get the list of history reports. ",  Toast.LENGTH_LONG).show();
                endProgressDialog();
            }
        });
    }

    //Loads the hashmap with the lists of test reports and then transfers the data between the fragments
    private void loadHashmap() {
        //Load all the tests
        for (int i = 0; i < myUsers.size(); i++) {
            emoUser = myUsers.get(i);
            Long userId = emoUser.getId();
            Call<List<TestReport>> testReportCall = emolanceAPI.listReports(userId);
            testReportCall.enqueue(new Callback<List<TestReport>>() {
                @Override
                public void onResponse(Call<List<TestReport>> call, Response<List<TestReport>> response) {
                    counter++;
                    if (response.isSuccessful()) {
                        List<TestReport> list = response.body();
                        if (list != null && list.size() > 0) {
                            Long tempId = list.get(0).getOwner().getId();
                            hashMap.put(tempId, list);
                        }
                    }
                    //Once the last response has been received, transfer the data
                    if (counter == myUsers.size()) {
                        activity.transferData();
                        endProgressDialog();
                    }
                }

                @Override
                public void onFailure(Call<List<TestReport>> call, Throwable t) {
                    Log.e("AdminReport", "Failed to get the list of individual history reports. ");
                    Toast.makeText(getActivity(),"Failed to get the list of history reports. ",  Toast.LENGTH_LONG).show();
                    endProgressDialog();
                }
            });
        }
    }

    //Used to send data to AdminDashboardFragment
    public List<EmoUser> getEmoUserList() {
        return myUsers;
    }
    public HashMap<Long, List<TestReport>> getTestsHashmap() {
        return hashMap;
    }

    public void openUserTestsFragment(int i) {
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.USER_ID, adminReportAdapter.getItem(i).getId());
        UserReportsFragment userReportsFragment = new UserReportsFragment();
        UserProfileFragment userProfileFragment = new UserProfileFragment();
        userReportsFragment.setArguments(bundle);
        userProfileFragment.setArguments(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.root_container_left, userProfileFragment, "UserProfileFragment");
        fragmentTransaction.replace(R.id.root_container_right, userReportsFragment, "UserReportsFragment");
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void persistLoadedMyUsers(List<EmoUser> myUsers) {
        for(EmoUser eu : myUsers) {
            Paper.book(Constants.DB_EMOUSER).write(Long.toString(eu.getId()), eu);
        }
        Paper.book(Constants.DB_MYUSERS).write(Constants.DB_MYUSERS_KEY, myUsers);
    }

}
