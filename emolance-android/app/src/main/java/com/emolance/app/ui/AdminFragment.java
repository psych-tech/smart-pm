package com.emolance.app.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.emolance.app.R;

import butterknife.ButterKnife;

/**
 * Created by yusun on 6/22/15.
 */
public class AdminFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new, container, false);
        ButterKnife.inject(this, rootView);

        return rootView;
    }
}
