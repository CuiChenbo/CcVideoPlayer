package com.ccb.ccvideoplayer.fragment;


import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ccb.ccvideoplayer.R;
import com.ccb.ccvideoplayer.activity.VideoPlayActivity;
import com.ccb.ccvideoplayer.bean.VideoBean;
import com.ccb.ccvideoplayer.utils.DataUtil;
import com.ccb.ccvideoplayer.utils.VideoScreen;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import www.ccb.com.common.base.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class SpcFragment extends BaseFragment {


    private BaseQuickAdapter quickAdapter;

    public SpcFragment() {
        // Required empty public constructor
    }

    private RecyclerView recyclerView;
    @Override
    protected View initContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_spc, container, false);
    }

    @Override
    public void initView(View view) {
     recyclerView = view.findViewById(R.id.rv);
     recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
     if (quickAdapter == null) {
         quickAdapter = new BaseQuickAdapter<VideoBean, BaseViewHolder>(R.layout.item_grid_video_play, DataUtil.getVideoList()) {
             @Override
             protected void convert(BaseViewHolder helper, VideoBean item) {
                 helper.setText(R.id.tv_title,item.getTitle());
                 Glide.with(mContext)
                         .load(item.getThumbRes() == -1 ? item.getThumb() : item.getThumbRes())
                         .into((ImageView) helper.getView(R.id.iv));
//                 VideoScreen.loadVideoScreenshot(getActivity(),item.getUrl(),helper.getView(R.id.iv),5*1000);
                 helper.itemView.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         Bundle bundle = new Bundle();
                         bundle.putString("url",item.getUrl());
                         start(VideoPlayActivity.class,bundle);
                     }
                 });
             }

         };
     }
     recyclerView.setAdapter(quickAdapter);
    }

    @Override
    public void loadData() {

    }

    @Override
    public void initListener() {

    }

}
