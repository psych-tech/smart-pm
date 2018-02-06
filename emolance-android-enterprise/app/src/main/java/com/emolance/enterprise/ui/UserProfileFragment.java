package com.emolance.enterprise.ui;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.emolance.enterprise.Injector;
import com.emolance.enterprise.R;
import com.emolance.enterprise.data.TestReport;
import com.emolance.enterprise.service.EmolanceAPI;
import com.emolance.enterprise.util.Constants;
import com.emolance.enterprise.util.DateUtils;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by David on 3/10/2017.
 */

public class UserProfileFragment extends Fragment {

    private Long userId;
    private static final String TAG = UserReportsFragment.class.getName();

    @Inject
    EmolanceAPI emolanceAPI;
    @InjectView(R.id.profileImage)
    ImageView imageView;
    @InjectView(R.id.userDashboardLineChart)
    LineChart lineChart;
    @InjectView(R.id.lineChartYAxisLabel)
    TextView lineChartYAxisLabel;
    @InjectView(R.id.lineChartXAxisLabel)
    TextView lineChartXAxisLabel;
    @InjectView(R.id.userProfileTextViewName)
    TextView userProfileTextViewName;
    @InjectView(R.id.userProfileTextViewEmail)
    TextView userProfileTextViewEmail;
    @InjectView(R.id.userProfileTextViewPosition)
    TextView userProfileTextViewPosition;
    @InjectView(R.id.userProfileTextViewGender)
    TextView userProfileTextViewGender;
    @InjectView(R.id.userProfileTextViewDOB)
    TextView userProfileTextViewDOB;
    @InjectView(R.id.userProfileTextViewWeChat)
    TextView userProfileTextViewWeChat;
    @InjectView(R.id.reportsList)
    ListView reportsListView;
    @InjectView(R.id.backButtonProfile)
    ImageButton backButton;
    @InjectView(R.id.testSequenceBtn)
    Button testSequenceBtn;
    @InjectView(R.id.noDataProfile)
    ImageView noDataImage;

    private Context context;
    private long[] timestamps;
    private SimpleDateFormat inputFormatter, outputFormatter;
    private ProgressDialog progress;
    private List<TestReport> reports;
    private UserReportAdapter adminReportAdapter;
    private final String USER_ID = "user";
    private TestSequenceFragment testSequenceFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        this.context = getActivity();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);
        ButterKnife.inject(this, rootView);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        testSequenceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                //ft.hide(getActivity().getSupportFragmentManager().findFragmentByTag("AdminDashboardFragment"));
                testSequenceFragment = new TestSequenceFragment();
                Bundle bundle = new Bundle();
                bundle.putLong(USER_ID,userId);
                testSequenceFragment.setArguments(bundle);
                ft.replace(R.id.root_container_right,testSequenceFragment,"SequenceFragment");
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle bundle = getArguments();
        if (bundle != null) {
            userId = bundle.getLong(Constants.USER_ID);
            String userName = bundle.getString(Constants.USER_NAME);
            String userEmail = bundle.getString(Constants.USER_EMAIL);
            String userPosition = bundle.getString(Constants.USER_POSITION);
            String userImage = bundle.getString(Constants.USER_IMAGE);
            String userDOB = bundle.getString(Constants.USER_DOB);
            String userWeChat = bundle.getString(Constants.USER_WECHAT);

            inputFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            outputFormatter = new SimpleDateFormat("MM/dd");
            if (userEmail != null) {
                userProfileTextViewEmail.setText(getResources().getString(R.string.user_profile_email) + ": " + userEmail);
            }
            if (userName != null) {
                userProfileTextViewName.setText(userName);
            }
            if (userPosition != null) {
                userProfileTextViewPosition.setText(userPosition);
            }
            if(userDOB != null){
                userProfileTextViewDOB.setText(getResources().getString(R.string.user_profile_dob) + ": " + DateUtils.getDateBirthInStr(userDOB));
            }
            if(userImage != null){
                String uri = userImage;
                int imageResource = getResources().getIdentifier(uri,null,getActivity().getPackageName());
                if(imageResource != 1){
                    imageView.setImageDrawable(getResources().getDrawable(imageResource));
                }
            }
        }
        startProgressDialog();
        loadReports();
    }

    public void loadReports() {
        Call<List<TestReport>> call = emolanceAPI.listReports(userId);
        call.enqueue(new Callback<List<TestReport>>() {
            @Override
            public void onResponse(Call<List<TestReport>> call, Response<List<TestReport>> response) {
                endProgressDialog();
                if (response.isSuccessful()) {
                    reports = response.body();
                    if(reports.size() == 0){
                        reportsListView.setVisibility(View.GONE);
                        lineChartXAxisLabel.setVisibility(View.GONE);
                        lineChartYAxisLabel.setVisibility(View.GONE);
                        lineChart.setVisibility(View.GONE);
                        noDataImage.setVisibility(View.VISIBLE);
                    }
                    else{
                        NewMainActivity activity = (NewMainActivity) getActivity();
                        activity.transferDataUser(); //transfer the data to the UserProfileFragment
                        Collections.sort(reports, new Comparator<TestReport>() {
                            public int compare(TestReport o1, TestReport o2) {
                                if (o1.getReportDate() == null || o2.getReportDate() == null)
                                    return 0;
                                return o2.getReportDate().compareTo(o1.getReportDate());
                                }
                        });
                        adminReportAdapter = new UserReportAdapter(getContext(), reports, UserProfileFragment.this);
                        //totalTextView.setText(adminReportAdapter.getCount() + context.getResources().getString(R.string.test_reports_user_profile_items));
                        reportsListView.setAdapter(adminReportAdapter);
                        reportsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Toast.makeText(getActivity(), "Clickable action", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(UserProfileFragment.this.getActivity(), ReportActivity.class);
                                intent.putExtra("id", adminReportAdapter.getItem(i).getId());
                                startActivity(intent);
                            }
                        });
                        setData(reports);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TestReport>> call, Throwable t) {
                Log.e("AdminReport", getResources().getString(R.string.api_user_list_error));
                endProgressDialog();
                Toast.makeText(getActivity(),getResources().getString(R.string.api_user_list_error),  Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Handle setting up chart from the data
    public void setData(List<TestReport> tests) {

        //fill lineEntries with data
        List<Entry> lineEntries = new ArrayList<>();
        Date d;
        timestamps = new long[tests.size()];
        //starting index of the tests being displayed
        int offset = 9;
        if(tests.size() < 10){
            offset = tests.size() - 1;
        }
        //Set up the chart data from the tests
        int count = 0;
        for (int i = offset; i >= 0; i--) {
            TestReport testReport = tests.get(i);
            if (testReport != null) {
                Integer stressLevel = testReport.getLevel();
                if (stressLevel == null) {
                    stressLevel = 0;
                }
                String testDate = testReport.getReportDate();
                try {
                    d = inputFormatter.parse(testDate);
                    timestamps[count] = d.getTime();
                    lineEntries.add(new Entry(count, stressLevel));
                    count++;
                }
                catch (ParseException e) {
                    Toast.makeText(getActivity(), "Error getting test date",  Toast.LENGTH_LONG).show();
                }
            }
        }

        //Set up the chart if their is data
        if (lineEntries.size() > 0) {
            //lineChartTitle.setVisibility(View.VISIBLE);
            lineChartXAxisLabel.setVisibility(View.VISIBLE);
            lineChartYAxisLabel.setVisibility(View.VISIBLE);
            LineDataSet lineDataSet = new LineDataSet(lineEntries, "");
            lineDataSet.setColors(getResources().getIntArray(R.array.altcolors));
            LineData lineData = new LineData(lineDataSet);

            //Set data to be outputted as integers
            lineData.setValueFormatter(new IValueFormatter() {
                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    DecimalFormat df = new DecimalFormat("##");
                    return df.format(value);
                }
            });

            lineChart.setData(lineData);
            lineChart.getDescription().setText(""); //hide description
            lineChart.getLegend().setEnabled(false); //hide legend

            //Format X Axis labels to show dates
            IAxisValueFormatter dateFormatter = new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    if((int) value < timestamps.length){
                        return outputFormatter.format(timestamps[(int) value]);
                    }
                    return "";
                }
            };
            //Format Y Axis labels to show integers
            IAxisValueFormatter stressLevelFormatter = new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return "" + (int) value;
                }
            };

            //Format X Axis labels
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(lineEntries.size());
            xAxis.setValueFormatter(dateFormatter);
            xAxis.setGranularity(1);
            xAxis.setAxisMinimum(0);
            if(tests.size() < 10){
                xAxis.setAxisMaximum(tests.size() - 1);
            }else{
                xAxis.setAxisMaximum(9);
            }
            xAxis.setAvoidFirstLastClipping(true);

            //Format Y Axis Labels
            lineChart.getAxisRight().setEnabled(false);
            YAxis yAxis = lineChart.getAxisLeft();
            yAxis.setLabelCount(11);
            yAxis.setGranularity(1);
            yAxis.setAxisMinimum(0);
            yAxis.setAxisMaximum(10);
            yAxis.setValueFormatter(stressLevelFormatter);

            lineChart.setExtraRightOffset(25);
            lineChart.animateY(1500); //animate the chart data (over 1.5 sec interval)
            lineChart.invalidate(); //refresh
        }

        else {
            //Show "No data available"
            //lineChartTitle.setVisibility(View.GONE);
            lineChartXAxisLabel.setVisibility(View.GONE);
            lineChartYAxisLabel.setVisibility(View.GONE);
            Paint p = lineChart.getPaint(Chart.PAINT_INFO);
            p.setTextSize(25);
            p.setColor(getResources().getColor(R.color.darkblue));
        }
    }

    private void startProgressDialog() {
        progress = ProgressDialog.show(this.getActivity(), null, "Loading report data ...", true);
    }

    private void endProgressDialog() {
        progress.dismiss();
    }

    public void measureTestOnClick(View view){
        testSequenceFragment.measureTestOnClick(view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}