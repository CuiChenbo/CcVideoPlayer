package com.ccb.ccvideoplayer.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ccb.ccvideoplay.videocontroller.StandardVideoController;
import com.ccb.ccvideoplay.videoplayer.player.VideoView;
import com.ccb.ccvideoplay.videoplayer.player.VideoViewManager;
import com.ccb.ccvideoplayer.R;

import www.ccb.com.common.base.BaseActivity;

public class VideoPlayActivity extends BaseActivity {


    @Override
    public int getContentViewResource() {
        return R.layout.activity_video_play;
    }

    private VideoView videoView;
    @Override
    protected void initView() {
     videoView = findViewById(R.id.video_view);
    }

    private String videoUrl;
    @Override
    protected void initData() {
        if (getIntent() == null) return;
     videoUrl = getIntent().getExtras().getString("url");
     videoView.setUrl(videoUrl);
     videoView.setVideoController(new StandardVideoController(mContext));
     videoView.start();
    }

    @Override
    protected void initList() {

    }
    @Override
    protected void onPause() {
        super.onPause();
        VideoViewManager.instance().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        VideoViewManager.instance().resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoViewManager.instance().release();
    }

    @Override
    public void onBackPressed() {
        if (!VideoViewManager.instance().onBackPressed()){
            super.onBackPressed();
        }
    }

}
