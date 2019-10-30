package com.ccb.ccvideoplayer.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ccb.ccvideoplay.videocontroller.RotateInFullscreenController;
import com.ccb.ccvideoplay.videocontroller.StandardVideoController;
import com.ccb.ccvideoplay.videoplayer.listener.OnVideoViewStateChangeListener;
import com.ccb.ccvideoplay.videoplayer.player.VideoView;
import com.ccb.ccvideoplayer.R;
import com.ccb.ccvideoplayer.bean.VideoBean;
import com.ccb.ccvideoplayer.utils.VideoScreen;

import java.util.ArrayList;
import java.util.List;

public class VideoRecyclerViewAdapter extends RecyclerView.Adapter<VideoRecyclerViewAdapter.VideoHolder> {

    private List<VideoBean> videos = new ArrayList<>();
    public VideoRecyclerViewAdapter(List<VideoBean> videos) {
        this.videos.addAll(videos);
    }

    @Override
    @NonNull
    public VideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_auto_play, parent, false);
        return new VideoHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull VideoHolder holder, int position) {

        VideoBean videoBean = videos.get(position);

        ImageView thumb = holder.mController.getThumb();
        Glide.with(thumb.getContext())
                .load(videoBean.getThumbRes() == -1 ? videoBean.getThumb() : videoBean.getThumbRes())
                .placeholder(android.R.color.white)
                .into(thumb);
//        VideoScreen.loadVideoScreenshot(thumb.getContext(),videoBean.getUrl() , thumb,1000);
        holder.mController.setEnableOrientation(true);
        holder.mController.setTitle(videoBean.getTitle());

        holder.mVideoView.setUrl(videoBean.getUrl());
        holder.mVideoView.setVideoController(holder.mController);
//        //保存播放进度
//        if (mProgressManager == null)
//            mProgressManager = new ProgressManagerImpl();
//        holder.mVideoView.setProgressManager(mProgressManager);
//        holder.mVideoView.setPlayerFactory(mPlayerFactory);

        holder.mTitle.setText(videoBean.getTitle());
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public void addData(List<VideoBean> videoList) {
        int size = videos.size();
        videos.addAll(videoList);
        //使用此方法添加数据，使用notifyDataSetChanged会导致正在播放的视频中断
        notifyItemRangeChanged(size, videos.size());
    }

    public class VideoHolder extends RecyclerView.ViewHolder {

        private VideoView mVideoView;
        private RotateInFullscreenController mController;
        private TextView mTitle;

        VideoHolder(View itemView) {
            super(itemView);
            mVideoView = itemView.findViewById(R.id.video_player);
            int widthPixels = itemView.getContext().getResources().getDisplayMetrics().widthPixels;
            mVideoView.setLayoutParams(new LinearLayout.LayoutParams(widthPixels, widthPixels * 9 / 16 + 1));
            mController = new RotateInFullscreenController(itemView.getContext());
            mTitle = itemView.findViewById(R.id.tv_title);

            //这段代码用于实现小屏时静音，全屏时有声音
            mVideoView.setOnVideoViewStateChangeListener(new OnVideoViewStateChangeListener() {
                @Override
                public void onPlayerStateChanged(int playerState) {
                    if (playerState == VideoView.PLAYER_FULL_SCREEN) {
                        mVideoView.setMute(false);
                    } else if (playerState == VideoView.PLAYER_NORMAL) {
                        mVideoView.setMute(true);
                    }
                }

                @Override
                public void onPlayStateChanged(int playState) {
                    //小屏状态下播放出来之后，把声音关闭
                    if (playState == VideoView.STATE_PREPARED && !mVideoView.isFullScreen()) {
                        mVideoView.setMute(true);
                    }
                }
            });
        }
    }
}