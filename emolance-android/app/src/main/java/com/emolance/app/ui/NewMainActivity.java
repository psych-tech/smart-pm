package com.emolance.app.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.emolance.app.R;
import com.emolance.app.util.Constants;
import com.emolance.app.util.GlobalSettings;

/**
 * Created by yusun on 6/22/15.
 */
public class NewMainActivity extends FragmentActivity {

    NewMainActivityPageViewerAdapter pagerAdapter;
    ViewPager mViewPager;

    private int tmpDelayTime = GlobalSettings.processingDelay;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkAuth();

        setContentView(R.layout.activity_new_main);

        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

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

        // Add 2 tabs, specifying the tab's text and TabListener
        actionBar.addTab(actionBar.newTab()
                            .setText("New")
                            .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab()
                            .setText("Historical")
                            .setTabListener(tabListener));

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        pagerAdapter = new NewMainActivityPageViewerAdapter(
                getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(pagerAdapter);
    }

    private void checkAuth() {
        Log.i("TEST", "Checking auth...");
        final AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        Log.i("TEST", "Checking auth..." + accounts.length);
        if (accounts.length == 0) {
            accountManager.addAccount(Constants.ACCOUNT_TYPE, null, null, null, this,
                    null, null);
        }
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

        return super.onOptionsItemSelected(item);
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
