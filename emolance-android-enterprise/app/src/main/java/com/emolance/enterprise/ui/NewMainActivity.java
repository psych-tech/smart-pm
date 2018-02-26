package com.emolance.enterprise.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import com.emolance.enterprise.util.ResultManager;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by yusun on 6/22/15.
 */
public class NewMainActivity extends FragmentActivity {
    private LinearLayout rootContainer;
    private int tmpDelayTime = GlobalSettings.processingDelay;
    private FragmentTransaction fragmentTransaction;
    private AdminFragment adminFragment;
    private List<EmoUser> users;
    HashMap<Long, List<TestReport>> hashMap;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkAuth();
        
        setContentView(R.layout.activity_new_main);

        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (ContextCompat.checkSelfPermission(NewMainActivity.this, android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA},
                        1);
            }

        }
        adminFragment = new AdminFragment();
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.root_container_left, adminFragment, "AdminFragment");
        fragmentTransaction.add(R.id.root_container_right, new AdminDashboardFragment(), "AdminDashboardFragment");
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
                users = adminFragment.getEmoUserList();
                hashMap = adminFragment.getTestsHashmap();
                adminDashboardFragment.setData(users, hashMap);
            }
        }
        setRootContainerVisibility(true);
    }

    public List<EmoUser> getUserList(){
        return users;
    }

    public HashMap<Long, List<TestReport>> getMaps(){
        return hashMap;
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

    public void measureTestOnClick(View view){
        adminFragment.measureTestOnClick(view);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    Toast.makeText(this, getResources().getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void clearBackStackInclusive() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0 || count == 1) {
            super.onBackPressed();
            Log.i("BACK", "count 0 or 1");
            //additional code
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.root_container_right);
            android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
            if(fragment instanceof TestSequenceFragment){
                super.onBackPressed();
                Log.i("BACK", "Test Sequence");
            }
            else if(fragment instanceof TestResultFragment) {
                Log.i("BACK", "Test Result");
                //Fragment sequence = getSupportFragmentManager().findFragmentByTag("SequenceFragment");
                List<Fragment> list = manager.getFragments();
                if(list.get(list.size() - 2) instanceof TestSequenceFragment){
                    android.support.v4.app.FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(count - 2);
                    manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
                else{
                    super.onBackPressed();
                }
            }
            else if(manager.getBackStackEntryCount() > 1) {
                Log.i("BACK", "count greater than 1");
                android.support.v4.app.FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
                manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
    }
}
