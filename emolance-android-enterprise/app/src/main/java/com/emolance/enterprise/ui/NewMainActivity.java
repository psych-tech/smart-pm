package com.emolance.enterprise.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.emolance.enterprise.DebuggingTools;
import com.emolance.enterprise.R;
import com.emolance.enterprise.data.EmoUser;
import com.emolance.enterprise.data.TestReport;
import com.emolance.enterprise.util.Constants;
import com.emolance.enterprise.util.GlobalSettings;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by yusun on 6/22/15.
 */
public class NewMainActivity extends FragmentActivity {

//    NewMainActivityPageViewerAdapter pagerAdapter;
//    ViewPager mViewPager;

    private LinearLayout rootContainer;
    private int tmpDelayTime = GlobalSettings.processingDelay;
    private FragmentTransaction fragmentTransaction;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkAuth();

        setContentView(R.layout.activity_new_main);

        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        /*
        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

            }
        };

        // Add 1 tab, specifying the tab's text and TabListener
        actionBar.addTab(actionBar.newTab()
                            .setText("New")
                            .setTabListener(tabListener));*/

        // ViewPager and its adapters use support library
//        // fragments, so use getSupportFragmentManager.
//        pagerAdapter = new NewMainActivityPageViewerAdapter(
//                getSupportFragmentManager());
//        mViewPager = (ViewPager) findViewById(R.id.pager);
//        mViewPager.setAdapter(pagerAdapter);

        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.root_container_left, new AdminFragment(), "AdminFragment");
        fragmentTransaction.replace(R.id.root_container_right, new AdminDashboardFragment(), "AdminDashboardFragment");
        fragmentTransaction.commit();

        rootContainer = (LinearLayout) findViewById(R.id.root_container);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void checkAuth() {
        final AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        if (accounts.length == 0) {
            accountManager.addAccount(Constants.ACCOUNT_TYPE, null, null, null, this,
                    null, null);
        }
    }

    //Used to transfer data from AdminFragment to AdminDashboardFragment
    public void transferData() {
        AdminFragment adminFragment = (AdminFragment) getSupportFragmentManager().findFragmentByTag("AdminFragment");
        if (adminFragment != null) {
            AdminDashboardFragment adminDashboardFragment = (AdminDashboardFragment)
                    getSupportFragmentManager().findFragmentByTag("AdminDashboardFragment");

            if (adminDashboardFragment != null) {
                List<EmoUser> myUsers = adminFragment.getEmoUserList();
                HashMap<Long, List<TestReport>> hashMap = adminFragment.getTestsHashmap();
                adminDashboardFragment.setData(myUsers, hashMap);
            }
        }
        setRootContainerVisibility(true);
    }

    public void updateList(){
        AdminFragment adminFragment = (AdminFragment) getSupportFragmentManager().findFragmentByTag("AdminFragment");
        adminFragment.updateAdapter();
    }
    //Used to transfer data from UserReportsFragment to UserProfileFragment
    public void transferDataUser() {
        UserReportsFragment userReportsFragment = (UserReportsFragment) getSupportFragmentManager()
                .findFragmentByTag("UserReportsFragment");
        if (userReportsFragment != null) {
            UserProfileFragment userProfileFragment = (UserProfileFragment)
                    getSupportFragmentManager().findFragmentByTag("UserProfileFragment");

            if (userProfileFragment != null) {
                List<TestReport> testReportList = userReportsFragment.getTestList();
                userProfileFragment.setData(testReportList);
            }
        }
        setRootContainerVisibility(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            openDelaySettingDialog();
            return true;
        } else if (id == R.id.action_refresh) {
            this.sendBroadcast(new Intent(Constants.SYNC_INTENT_FILTER));
        }
        else if(id == R.id.action_debugging){
            Intent debugIntent = new Intent(this, DebuggingTools.class);
            startActivity(debugIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Sets the visibility of the container (cleaner look when data is loading)
    public void setRootContainerVisibility(boolean visible) {
        if (visible) {
            rootContainer.setVisibility(View.VISIBLE);
        }
        else {
            rootContainer.setVisibility(View.GONE);
        }
    }

    private void openDelaySettingDialog() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog_setting_input, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView delayText = (TextView) promptsView.findViewById(R.id.delayText);
        SeekBar delaySeekBar = (SeekBar) promptsView.findViewById(R.id.delayMinSeekBar);
        delaySeekBar.setProgress(GlobalSettings.processingDelay);
        delayText.setText("Processing Delay (min): " + GlobalSettings.processingDelay);
        delaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                delayText.setText("Processing Delay (min): " + i);
                tmpDelayTime = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                GlobalSettings.processingDelay = tmpDelayTime;
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
