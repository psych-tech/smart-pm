package com.emolance.enterprise.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.emolance.enterprise.R;

public class TestInsertTestFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("TestFragment","onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("TestFragment","onCreateView");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_test_insert_test, container, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("TestFragment","onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("TestFragment","onStop");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("TestFragment","onResume");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("TestFragment","onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("TestFragment","onDestroy");
    }
}
