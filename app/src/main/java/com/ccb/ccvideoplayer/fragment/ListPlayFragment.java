package com.ccb.ccvideoplayer.fragment;


import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ccb.ccvideocache.cache.ProxyVideoCacheManager;
import com.ccb.ccvideoplay.videoplayer.player.VideoView;
import com.ccb.ccvideoplay.videoplayer.player.VideoViewManager;
import com.ccb.ccvideoplayer.R;
import com.ccb.ccvideoplayer.adapter.VideoRecyclerViewAdapter;
import com.ccb.ccvideoplayer.utils.DataUtil;

import www.ccb.com.common.base.BaseFragment;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;


/**
 * 列表自动播放
 */
public class ListPlayFragment extends BaseFragment {


    private LinearLayoutManager linearLayoutManager;

    public ListPlayFragment() {
        // Required empty public constructor
    }

    @Override
    protected View initContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listplay, container, false);
    }

    private RecyclerView recyclerView;
    @Override
    public void initView(View view) {
      recyclerView = view.findViewById(R.id.rv);
        linearLayoutManager = new LinearLayoutManager(getActivity());
      recyclerView.setLayoutManager(linearLayoutManager);
        VideoRecyclerViewAdapter adapter = new VideoRecyclerViewAdapter(DataUtil.getVideoList());
      recyclerView.setAdapter(adapter);
    }

    @Override
    public void loadData() {
        recyclerView.post(() -> {
            //自动播放第一个
            View view = recyclerView.getChildAt(0);
            VideoView videoView = view.findViewById(R.id.video_player);
            videoView.start();
        });
    }

    @Override
    public void initListener() {
         recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
             @Override
             public void onChildViewAttachedToWindow(@NonNull View view) {

             }

             @Override
             public void onChildViewDetachedFromWindow(@NonNull View view) {
                 VideoView videoView = view.findViewById(R.id.video_player);
                 if (videoView != null && !videoView.isFullScreen()) {
                     videoView.release();
                 }
             }
         });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            int firstVisibleItem, lastVisibleItem, visibleCount;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                switch (newState) {
                    case SCROLL_STATE_IDLE: //滚动停止
                        autoPlayVideo(recyclerView);
                        break;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                visibleCount = lastVisibleItem - firstVisibleItem;//记录可视区域item个数
            }

            private void autoPlayVideo(RecyclerView view) {
                //循环遍历可视区域videoview,如果完全可见就开始播放
                for (int i = 0; i < visibleCount; i++) {
                    if (view == null || view.getChildAt(i) == null) continue;
                    VideoView videoView = view.getChildAt(i).findViewById(R.id.video_player);
                    if (videoView != null) {
                        Rect rect = new Rect();
                        videoView.getLocalVisibleRect(rect);
                        int videoHeight = videoView.getHeight();
                        if (rect.top == 0 && rect.bottom == videoHeight) {
                            videoView.start();
                            return;
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
        VideoViewManager.instance().pause();
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        VideoViewManager.instance().resume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        VideoViewManager.instance().release();
    }
}
