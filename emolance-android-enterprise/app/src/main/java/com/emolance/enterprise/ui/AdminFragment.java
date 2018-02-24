package com.emolance.enterprise.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import com.emolance.enterprise.Injector;
import com.emolance.enterprise.R;
import com.emolance.enterprise.data.EmoUser;
import com.emolance.enterprise.data.TestReport;
import com.emolance.enterprise.service.EmolanceAPI;
import com.emolance.enterprise.util.Constants;
import com.scalified.fab.ActionButton;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import butterknife.ButterKnife;
import butterknife.InjectView;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by yusun on 6/22/15.
 */
public class AdminFragment extends Fragment implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    @Inject
    EmolanceAPI emolanceAPI;
    @InjectView(R.id.userListView)
    ListView adminReportListView;
    @InjectView(R.id.searchViewUsers)
    SearchView searchUser;
    @InjectView(R.id.searchViewHolder)
    LinearLayout linearLayout;
    ActionButton fab;



    private ProgressDialog progress;
    private Context context;
    private UserListAdapter adminReportAdapter;

    //Used to send data to AdminDashboardFragment
    private List<EmoUser> myUsers;
    private HashMap<Long, List<TestReport>> hashMap;
    private int counter;
    private NewMainActivity activity;
    private View rootView;
    private UserProfileFragment userProfileFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        this.context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_new, container, false);
        ButterKnife.inject(this, rootView);
        fab = (ActionButton) rootView.findViewById(R.id.fab_new_user);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.hide(getActivity().getSupportFragmentManager().findFragmentByTag("AdminDashboardFragment"));
                Fragment frag = new UserReportCreatorFragment();
                Bundle bundle = new Bundle();
                bundle.putStringArray(Constants.LIST_EMAILS,getEmails());
                bundle.putStringArray(Constants.LIST_USERNAMES,getUsernames());
                frag.setArguments(bundle);
                ft.add(R.id.root_container_right,frag);
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();
            }
        });

        //SearchView search = (SearchView) item.getActionView();
        //search.setLayoutParams(new ActionBar.LayoutParams(Gravity.RIGHT));
        searchUser.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    Log.i("search focus", "yes");
                }
                else{
                    Log.i("search focus", "no");
                }
            }
        });
        linearLayout.setGravity(Gravity.END);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        activity = (NewMainActivity) getActivity();
        activity.setRootContainerVisibility(false);
        startProgressDialog();
        GetDataAsyncTask asyncTask = new GetDataAsyncTask();
        asyncTask.execute();
    }

    private String[] getEmails(){
        String[] emails = new String[myUsers.size()];
        for(int i = 0; i < myUsers.size();i++){
            emails[i] = myUsers.get(i).getEmail();
        }
        return emails;
    }

    private String[] getUsernames(){
        String[] userNames = new String[myUsers.size()];

        for(int i = 0; i < myUsers.size();i++){
            userNames[i] = myUsers.get(i).getName();
        }
        return userNames;
    }

    private void startProgressDialog() {
        progress = ProgressDialog.show(this.getActivity(), null, "Loading report data ...", true);
    }

    private void endProgressDialog() {
        progress.dismiss();
    }

    //Used to send data to AdminDashboardFragment
    public List<EmoUser> getEmoUserList() {
        return myUsers;
    }

    public HashMap<Long, List<TestReport>> getTestsHashmap() {
        return hashMap;
    }

    public void updateAdapter(){
        startProgressDialog();
        GetDataAsyncTask asyncTask = new GetDataAsyncTask();
        asyncTask.execute();
    }

    public void openUserTestsFragment(int i) {
        /*UserReportsFragment userReportsFragment = new UserReportsFragment();
        UserProfileFragment userProfileFragment = new UserProfileFragment();
        Bundle bundle = new Bundle();
        EmoUser emoUser = adminReportAdapter.getItem(i);

        bundle.putLong(Constants.USER_ID, emoUser.getId());
        userReportsFragment.setArguments(bundle);
        bundle.putString(Constants.USER_NAME, emoUser.getName());
        bundle.putString(Constants.USER_EMAIL, emoUser.getEmail());
        bundle.putString(Constants.USER_POSITION, emoUser.getPosition());
        bundle.putString(Constants.USER_IMAGE, emoUser.getProfileImage());
        userProfileFragment.setArguments(bundle);*/
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(getActivity().getSupportFragmentManager().findFragmentByTag("AdminDashboardFragment"));
        userProfileFragment = new UserProfileFragment();
        Bundle bundle = new Bundle();
        EmoUser emoUser = adminReportAdapter.getItem(i);
        bundle.putLong(Constants.USER_ID, emoUser.getId());
        userProfileFragment.setArguments(bundle);
        bundle.putString(Constants.USER_NAME, emoUser.getName());
        bundle.putString(Constants.USER_EMAIL, emoUser.getEmail());
        bundle.putString(Constants.USER_POSITION, emoUser.getPosition());
        bundle.putString(Constants.USER_IMAGE, emoUser.getProfileImage());
        bundle.putString(Constants.USER_DOB, emoUser.getDatebirth());
        bundle.putString(Constants.USER_GENDER, emoUser.getGender());
        //TODO: need to add wechat
        bundle.putString(Constants.USER_WECHAT, getResources().getString(R.string.user_profile_na));
        userProfileFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.root_container_right, userProfileFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();

        //LinearLayout rootContainer = (LinearLayout) rootView.findViewById(R.id.root_container);

//        FragmentManager fragmentManager = getFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.root_container_left, userProfileFragment, "UserProfileFragment");
//        fragmentTransaction.replace(R.id.root_container_right, userReportsFragment, "UserReportsFragment");
//        fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right);
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();
    }

    private void persistLoadedMyUsers(List<EmoUser> myUsers) {
        for(EmoUser eu : myUsers) {
            Paper.book(Constants.DB_EMOUSER).write(Long.toString(eu.getId()), eu);
        }
        Paper.book(Constants.DB_MYUSERS).write(Constants.DB_MYUSERS_KEY, myUsers);
    }

    private void setUserInformation(EmoUser emoUser) {
        String information = emoUser.getName();
        if(information.contains(" HH ")){
            String[] array = information.split("\\s+");
            if(array.length == 4 && array[1].equals("HH")){ //user with first name
                emoUser.setName(array[0]);
                emoUser.setDatebirth(array[2]);
                emoUser.setGender(array[3]);
            }
            else if(array.length == 5 && array[2].equals("HH")){ //user with first,last name
                emoUser.setName(array[0] + " " + array[1]);
                emoUser.setDatebirth(array[3]);
                emoUser.setGender(array[4]);
            }
            else if(array.length == 6 && array[3].equals("HH")){ //user with first,middle,last name
                emoUser.setName(array[0] + " " + array[1] + array[2]);
                emoUser.setDatebirth(array[4]);
                emoUser.setGender(array[5]);
            }
        }
    }

    private class GetDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Call<List<EmoUser>> call = emolanceAPI.listMyUsers();
            call.enqueue(new Callback<List<EmoUser>>() {
                @Override
                public void onResponse(Call<List<EmoUser>> call, Response<List<EmoUser>> response) {
                    if (response.isSuccessful()) {
                        myUsers = response.body();
                        counter = 0;
                        EmoUser emoUser;
                        hashMap = new HashMap<>(); //used to map tests to users
                        //Loads the hashmap with the lists of test reports and then transfers the data between the fragments
                        for (int i = 0; i < myUsers.size(); i++) {
                            emoUser = myUsers.get(i);
                            Long userId = emoUser.getId();
                            setUserInformation(emoUser);
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
                                        publishProgress();
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<TestReport>> call, Throwable t) {
                                    Log.e("AdminReport", getResources().getString(R.string.api_user_list_error));
                                    Toast.makeText(getActivity(), getResources().getString(R.string.api_user_list_error),  Toast.LENGTH_LONG).show();
                                    endProgressDialog();
                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<EmoUser>> call, Throwable t) {
                    Log.e("AdminReport", "Failed to get the list of history reports. ");
                    Toast.makeText(getActivity(),getResources().getString(R.string.api_user_list_error),  Toast.LENGTH_LONG).show();
                    endProgressDialog();
                }
            });

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            //Once the last response has been received, transfer the data
            if (counter == myUsers.size()) {
                activity.transferData();
                endProgressDialog();

                adminReportAdapter = new UserListAdapter(context, myUsers, AdminFragment.this);
                adminReportListView.setAdapter(adminReportAdapter);
                adminReportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        openUserTestsFragment(i);
                    }
                });
                persistLoadedMyUsers(myUsers);
                adminReportListView.setTextFilterEnabled(true);
                setupSearchView();
            }
        }
    }

    private void setupSearchView()
    {
        searchUser.setOnQueryTextListener(this);
        searchUser.setOnCloseListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            adminReportListView.clearTextFilter();
        } else {
            //adminReportListView.setFilterText(newText);
            adminReportAdapter.filter(newText);
            return false;
        }
        return true;
    }

    @Override
    public boolean onClose() {
        adminReportAdapter.reset();
        return false;
    }

    public void measureTestOnClick(View view){
        userProfileFragment.measureTestOnClick(view);
    }
}
