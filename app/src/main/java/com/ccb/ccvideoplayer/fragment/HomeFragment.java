package com.ccb.ccvideoplayer.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ccb.ccvideocache.cache.PreloadManager;
import com.ccb.ccvideocache.cache.ProxyVideoCacheManager;
import com.ccb.ccvideoplay.videoplayer.player.VideoView;
import com.ccb.ccvideoplay.videoplayer.player.VideoViewManager;
import com.ccb.ccvideoplay.videoplayer.util.L;
import com.ccb.ccvideoplayer.R;
import com.ccb.ccvideoplayer.adapter.Tiktok2Adapter;
import com.ccb.ccvideoplayer.bean.TiktokBean;
import com.ccb.ccvideoplayer.utils.DataUtil;

import java.util.List;

import www.ccb.com.common.base.BaseFragment;
import www.ccb.com.common.widget.VerticalViewPager;

/**
 * 抖音  有缓存 预加载
 */
public class HomeFragment extends BaseFragment {


    public HomeFragment() {
        // Required empty public constructor
    }


    private int mCurrentPosition;
    private int mPlayingPosition;
    private List<TiktokBean> mVideoList;
    private Tiktok2Adapter mTiktok2Adapter;
    private VerticalViewPager mViewPager;

    private PreloadManager mPreloadManager;

    /**
     * VerticalViewPager是否反向滑动
     */
    private boolean mIsReverseScroll;

    /**
     * 当前正在播放的VideoView
     */
    private VideoView mCurrentVideoView;

    @Override
    protected View initContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void initView(View view) {
        mVideoList = DataUtil.getTiktokDataFromAssets(getActivity());
        initViewPager();
        mPreloadManager = PreloadManager.getInstance(getActivity());
    }


    private void initViewPager() {
        mViewPager = findViewById(R.id.vvp);
        mViewPager.setOffscreenPageLimit(4);
        mTiktok2Adapter = new Tiktok2Adapter(mVideoList);
        mViewPager.setAdapter(mTiktok2Adapter);
        mViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (position > mPlayingPosition) {
                    mIsReverseScroll = false;
                } else if (position < mPlayingPosition) {
                    mIsReverseScroll = true;
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mCurrentPosition = position;
                if (mCurrentVideoView != null) {
                    mCurrentVideoView.release();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (mCurrentPosition == mPlayingPosition) return;
                if (state == VerticalViewPager.SCROLL_STATE_IDLE) {
                    mViewPager.post(new Runnable() {
                        @Override
                        public void run() {
                            startPlay();
                        }
                    });
                    mPreloadManager.resumePreload(mCurrentPosition, mIsReverseScroll);
                } else {
                    mPreloadManager.pausePreload(mCurrentPosition, mIsReverseScroll);
                }
            }
        });


        mViewPager.post(new Runnable() {
            @Override
            public void run() {
                startPlay();
            }
        });


    }

    private void startPlay() {
        if (!isVisible()){return;}
        View itemView = mTiktok2Adapter.getCurrentItemView();
        VideoView videoView = itemView.findViewById(R.id.video_view);
        TiktokBean tiktokBean = mVideoList.get(mCurrentPosition);
        String playUrl = mPreloadManager.getPlayUrl(tiktokBean.videoDownloadUrl);
        L.i("startPlay: " + "position: " + mCurrentPosition + "  url: " + playUrl);
        videoView.setUrl(playUrl);
//        videoView.setUrl(tiktokBean.videoDownloadUrl);
        videoView.start();
        mPlayingPosition = mCurrentPosition;
        mCurrentVideoView = videoView;
    }

    @Override
    public void loadData() {

    }

    @Override
    public void initListener() {

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
        //清除缓存，实际使用可以不需要清除，这里为了方便测试
        ProxyVideoCacheManager.clearAllCache(getActivity());
    }

}
