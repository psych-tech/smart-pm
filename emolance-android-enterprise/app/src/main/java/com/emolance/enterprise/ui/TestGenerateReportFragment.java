package com.emolance.enterprise.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.emolance.enterprise.R;

public class TestGenerateReportFragment extends Fragment {

    public TestGenerateReportFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static TestGenerateReportFragment newInstance(String param1, String param2) {
        TestGenerateReportFragment fragment = new TestGenerateReportFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_test_generate_report, container, false);
    }
}
