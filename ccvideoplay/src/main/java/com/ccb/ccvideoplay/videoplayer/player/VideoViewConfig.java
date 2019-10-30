package com.ccb.ccvideoplay.videoplayer.player;


import androidx.annotation.Nullable;

/**
 * 播放器全局配置
 */
public class VideoViewConfig {

    public static Builder newBuilder() {
        return new Builder();
    }

    public final boolean mPlayOnMobileNetwork;

    public final boolean mEnableMediaCodec;

    public final boolean mUsingSurfaceView;

    public final boolean mEnableOrientation;

    public final boolean mEnableAudioFocus;

    public final boolean mEnableParallelPlay;

    public final boolean mIsEnableLog;

    public final ProgressManager mProgressManager;

    public final PlayerFactory mPlayerFactory;

    public final int mScreenScaleType;

    private VideoViewConfig(Builder builder) {
        mIsEnableLog = builder.mIsEnableLog;
        mEnableOrientation = builder.mEnableOrientation;
        mUsingSurfaceView = builder.mUsingSurfaceView;
        mPlayOnMobileNetwork = builder.mPlayOnMobileNetwork;
        mEnableMediaCodec = builder.mEnableMediaCodec;
        mEnableAudioFocus = builder.mEnableAudioFocus;
        mProgressManager = builder.mProgressManager;
        mEnableParallelPlay = builder.mEnableParallelPlay;
        mScreenScaleType = builder.mScreenScaleType;
        if (builder.mPlayerFactory == null) {
            //默认为AndroidMediaPlayer
            mPlayerFactory = AndroidMediaPlayerFactory.create();
        } else {
            mPlayerFactory = builder.mPlayerFactory;
        }
    }


    public final static class Builder {

        private boolean mIsEnableLog;
        private boolean mPlayOnMobileNetwork;
        private boolean mUsingSurfaceView;
        private boolean mEnableOrientation;
        private boolean mEnableMediaCodec;
        private boolean mEnableAudioFocus = true;
        private boolean mEnableParallelPlay;
        private ProgressManager mProgressManager;
        private PlayerFactory mPlayerFactory;
        private int mScreenScaleType;

        /**
         * 是否监听设备方向来切换全屏/半屏， 默认不开启
         */
        public Builder setEnableOrientation(boolean enableOrientation) {
            mEnableOrientation = enableOrientation;
            return this;
        }

        /**
         * 是否启用SurfaceView，默认不启用
         */
        public Builder setUsingSurfaceView(boolean usingSurfaceView) {
            mUsingSurfaceView = usingSurfaceView;
            return this;
        }

        /**
         * 是否使用MediaCodec进行解码（硬解码），默认不开启，使用软解
         */
        public Builder setEnableMediaCodec(boolean enableMediaCodec) {
            mEnableMediaCodec = enableMediaCodec;
            return this;
        }

        /**
         * 在移动环境下调用start()后是否继续播放，默认不继续播放
         */
        public Builder setPlayOnMobileNetwork(boolean playOnMobileNetwork) {
            mPlayOnMobileNetwork = playOnMobileNetwork;
            return this;
        }

        /**
         * 是否开启AudioFocus监听， 默认开启
         */
        public Builder setEnableAudioFocus(boolean enableAudioFocus) {
            mEnableAudioFocus = enableAudioFocus;
            return this;
        }

        /**
         * 设置进度管理器，用于保存播放进度
         */
        public Builder setProgressManager(@Nullable ProgressManager progressManager) {
            mProgressManager = progressManager;
            return this;
        }

        /**
         * 支持多开
         */
        public Builder setEnableParallelPlay(boolean enableParallelPlay) {
            mEnableParallelPlay = enableParallelPlay;
            return this;
        }

        /**
         * 是否打印日志
         */
        public Builder setLogEnabled(boolean enableLog) {
            mIsEnableLog = enableLog;
            return this;
        }

        /**
         * 自定义播放核心
         */
        public Builder setPlayerFactory(PlayerFactory playerFactory) {
            mPlayerFactory = playerFactory;
            return this;
        }

        /**
         * 设置视频比例
         */
        public Builder setScreenScaleType(int screenScaleType) {
            mScreenScaleType = screenScaleType;
            return this;
        }

        public VideoViewConfig build() {
            return new VideoViewConfig(this);
        }
    }
}
