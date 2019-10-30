package com.ccb.ccvideoplayer.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.ccb.ccvideoplayer.R;

import www.ccb.com.common.base.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyFragment extends BaseFragment {


    public MyFragment() {
        // Required empty public constructor
    }

    @Override
    protected View initContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my, container, false);
    }

    @Override
    public void initView(View view) {

    }

    @Override
    public void loadData() {

    }

    @Override
    public void initListener() {

    }

}
