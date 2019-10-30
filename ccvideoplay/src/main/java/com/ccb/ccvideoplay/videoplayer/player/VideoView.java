package com.ccb.ccvideoplay.videoplayer.player;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.ccb.ccvideoplay.R;
import com.ccb.ccvideoplay.videoplayer.controller.BaseVideoController;
import com.ccb.ccvideoplay.videoplayer.controller.MediaPlayerControl;
import com.ccb.ccvideoplay.videoplayer.listener.OnVideoViewStateChangeListener;
import com.ccb.ccvideoplay.videoplayer.listener.PlayerEventListener;
import com.ccb.ccvideoplay.videoplayer.render.IRenderView;
import com.ccb.ccvideoplay.videoplayer.render.SurfaceRenderView;
import com.ccb.ccvideoplay.videoplayer.render.TextureRenderView;
import com.ccb.ccvideoplay.videoplayer.util.L;
import com.ccb.ccvideoplay.videoplayer.util.PlayerUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 播放器
 * Created by Devlin_n on 2017/4/7.
 */

public class VideoView<P extends AbstractPlayer> extends FrameLayout implements MediaPlayerControl, PlayerEventListener {

    protected P mMediaPlayer;//播放器
    protected PlayerFactory<P> mPlayerFactory;//工厂类，用于实例化播放核心
    @Nullable
    protected BaseVideoController mVideoController;//控制器

    protected IRenderView mRenderView;
    /**
     * 真正承载播放器视图的容器
     */
    protected FrameLayout mPlayerContainer;
    protected boolean mIsFullScreen;//是否处于全屏状态
    /**
     * 通过添加和移除这个view来实现隐藏和显示navigation bar，可以避免出现一些奇奇怪怪的问题
     */
    @Nullable
    protected View mHideNavBarView;
    protected static final int FULLSCREEN_FLAGS = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    public static final int SCREEN_SCALE_DEFAULT = 0;
    public static final int SCREEN_SCALE_16_9 = 1;
    public static final int SCREEN_SCALE_4_3 = 2;
    public static final int SCREEN_SCALE_MATCH_PARENT = 3;
    public static final int SCREEN_SCALE_ORIGINAL = 4;
    public static final int SCREEN_SCALE_CENTER_CROP = 5;
    protected int mCurrentScreenScaleType;

    protected int[] mVideoSize = {0, 0};

    protected boolean mIsTinyScreen;//是否处于小屏状态
    protected int[] mTinyScreenSize = {0, 0};

    protected boolean mIsMute;//是否静音

    //--------- data sources ---------//
    protected String mUrl;//当前播放视频的地址
    protected AssetFileDescriptor mAssetFileDescriptor;//assets文件
    protected Map<String, String> mHeaders;//当前视频地址的请求头

    protected long mCurrentPosition;//当前正在播放视频的位置

    //播放器的各种状态
    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_PLAYBACK_COMPLETED = 5;
    public static final int STATE_BUFFERING = 6;
    public static final int STATE_BUFFERED = 7;
    protected int mCurrentPlayState = STATE_IDLE;//当前播放器的状态

    public static final int PLAYER_NORMAL = 10;        // 普通播放器
    public static final int PLAYER_FULL_SCREEN = 11;   // 全屏播放器
    public static final int PLAYER_TINY_SCREEN = 12;   // 小屏播放器
    protected int mCurrentPlayerState = PLAYER_NORMAL;

    /**
     * 监听系统中其他播放的音频焦点改变，当然也包括自己的App内
     */
    protected boolean mEnableAudioFocus;
    @Nullable
    protected AudioFocusHelper mAudioFocusHelper;

    /**
     * OnVideoViewStateChangeListener集合，保存了所有开发者设置的监听器
     */
    protected List<OnVideoViewStateChangeListener> mOnVideoViewStateChangeListeners;

    /**
     * 进度管理器，设置之后播放器会记录播放进度，以便下次播放恢复进度
     */
    @Nullable
    protected ProgressManager mProgressManager;

    /**
     * 使用SurfaceView渲染视频，默认是TextureView
     */
    protected boolean mUsingSurfaceView;

    /**
     * 循环播放
     */
    protected boolean mIsLooping;

    /**
     * 启用MediaCodec解码,就是所谓的硬解码，此设置仅适用于ijkplayer
     */
    protected boolean mEnableMediaCodec;

    /**
     * 支持多开，注意这里的多开不仅仅是指同一个页面需要同时存在多个播放器，也适用于Activity和Activity之间以及Activity
     * 和Fragment之间，甚至是Fragment和Fragment之间的场景。
     */
    protected boolean mEnableParallelPlay;

    public VideoView(@NonNull Context context) {
        this(context, null);
    }

    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //读取全局配置
        VideoViewConfig config = VideoViewManager.getConfig();
        mUsingSurfaceView = config.mUsingSurfaceView;
        mEnableMediaCodec = config.mEnableMediaCodec;
        mEnableAudioFocus = config.mEnableAudioFocus;
        mEnableParallelPlay = config.mEnableParallelPlay;
        mProgressManager = config.mProgressManager;
        //默认使用系统的MediaPlayer进行解码
        mPlayerFactory = config.mPlayerFactory;
        mCurrentScreenScaleType = config.mScreenScaleType;

        //读取xml中的配置，并综合全局配置
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VideoView);
        mUsingSurfaceView = a.getBoolean(R.styleable.VideoView_usingSurfaceView, mUsingSurfaceView);
        mEnableAudioFocus = a.getBoolean(R.styleable.VideoView_enableAudioFocus, mEnableAudioFocus);
        mEnableMediaCodec = a.getBoolean(R.styleable.VideoView_enableMediaCodec, mEnableMediaCodec);
        mEnableParallelPlay = a.getBoolean(R.styleable.VideoView_enableParallelPlay, mEnableParallelPlay);
        mIsLooping = a.getBoolean(R.styleable.VideoView_looping, false);
        mCurrentScreenScaleType = a.getInt(R.styleable.VideoView_screenScaleType, mCurrentScreenScaleType);
        a.recycle();

        initView();
    }

    /**
     * 初始化播放器视图
     */
    protected void initView() {
        mPlayerContainer = new FrameLayout(getContext());
        mPlayerContainer.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mPlayerContainer, params);
    }

    /**
     * 开始播放，注意：调用此方法后必须调用{@link #release()}释放播放器，否则会导致内存泄漏
     */
    @Override
    public void start() {
        boolean isStarted = false;
        if (isInIdleState()) {
            startPlay();
            isStarted = true;
        } else if (isInPlaybackState()) {
            startInPlaybackState();
            isStarted = true;
        }
        if (isStarted) {
            setKeepScreenOn(true);
            if (mAudioFocusHelper != null)
                mAudioFocusHelper.requestFocus();
        }
    }

    /**
     * 第一次播放
     */
    protected void startPlay() {
        if (!mEnableParallelPlay) {
            VideoViewManager.instance().release();
        }
        VideoViewManager.instance().addVideoView(this);

        //如果要显示移动网络提示则不继续播放
        if (showNetWarning()) return;

        //监听音频焦点改变
        if (mEnableAudioFocus) {
            mAudioFocusHelper = new AudioFocusHelper(this);
        }

        //读取播放进度
        if (mProgressManager != null) {
            mCurrentPosition = mProgressManager.getSavedProgress(mUrl);
        }

        initPlayer();
        startPrepare(false);
    }

    /**
     * 是否显示移动网络提示，可在Controller中配置
     */
    protected boolean showNetWarning() {
        //播放本地数据源时不检测网络
        if (isLocalDataSource()) return false;
        return mVideoController != null && mVideoController.showNetWarning();
    }

    /**
     * 判断是否为本地数据源，包括 本地文件、Asset、raw
     */
    protected boolean isLocalDataSource() {
        if (mAssetFileDescriptor != null) {
            return true;
        } else if (!TextUtils.isEmpty(mUrl)) {
            Uri uri = Uri.parse(mUrl);
            return ContentResolver.SCHEME_ANDROID_RESOURCE.equals(uri.getScheme())
                    || ContentResolver.SCHEME_FILE.equals(uri.getScheme())
                    || "rawresource".equals(uri.getScheme());
        }
        return false;
    }

    /**
     * 初始化播放器
     */
    protected void initPlayer() {
        mMediaPlayer = mPlayerFactory.createPlayer();
        mMediaPlayer.setPlayerEventListener(this);
        setInitOptions();
        mMediaPlayer.initPlayer();
        setOptions();
    }

    /**
     * 初始化之前的配置项
     */
    protected void setInitOptions() {
    }

    /**
     * 初始化之后的配置项
     */
    protected void setOptions() {
        mMediaPlayer.setEnableMediaCodec(mEnableMediaCodec);
        mMediaPlayer.setLooping(mIsLooping);
    }

    /**
     * 初始化视频渲染View
     */
    protected void addDisplay() {
        if (mRenderView != null) {
            mPlayerContainer.removeView(mRenderView.getView());
            mRenderView.release();
        }
        if (mUsingSurfaceView) {
            mRenderView = new SurfaceRenderView(getContext(), mMediaPlayer);
        } else {
            mRenderView = new TextureRenderView(getContext(), mMediaPlayer);
        }
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        mPlayerContainer.addView(mRenderView.getView(), 0, params);
    }

    /**
     * 开始准备播放（直接播放）
     */
    protected void startPrepare(boolean reset) {
        if (reset) mMediaPlayer.reset();
        if (prepareDataSource()) {
            mMediaPlayer.prepareAsync();
            setPlayState(STATE_PREPARING);
            setPlayerState(isFullScreen() ? PLAYER_FULL_SCREEN : isTinyScreen() ? PLAYER_TINY_SCREEN : PLAYER_NORMAL);
        }
    }

    /**
     * 设置播放数据
     * @return 播放数据是否设置成功
     */
    protected boolean prepareDataSource() {
        if (mAssetFileDescriptor != null) {
            mMediaPlayer.setDataSource(mAssetFileDescriptor);
            return true;
        } if (!TextUtils.isEmpty(mUrl)) {
            mMediaPlayer.setDataSource(mUrl, mHeaders);
            return true;
        }
        return false;
    }

    /**
     * 播放状态下开始播放
     */
    protected void startInPlaybackState() {
        mMediaPlayer.start();
        setPlayState(STATE_PLAYING);
    }

    /**
     * 暂停播放
     */
    @Override
    public void pause() {
        if (isPlaying()) {
            mMediaPlayer.pause();
            setPlayState(STATE_PAUSED);
            setKeepScreenOn(false);
            if (mAudioFocusHelper != null)
                mAudioFocusHelper.abandonFocus();
        }
    }

    /**
     * 继续播放
     */
    public void resume() {
        if (isInPlaybackState()
                && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            setPlayState(STATE_PLAYING);
            if (mAudioFocusHelper != null)
                mAudioFocusHelper.requestFocus();
            setKeepScreenOn(true);
        }
    }

    /**
     * 停止播放
     *
     * @deprecated 使用 {@link #release()} 代替
     */
    @Deprecated
    public void stopPlayback() {
        release();
    }

    /**
     * 释放播放器
     */
    public void release() {
        VideoViewManager.instance().removeVideoView(this);
        if (mVideoController != null) {
            mVideoController.hideNetWarning();
        }
        if (!isInIdleState()) {
            saveProgress();
            mMediaPlayer.release();
            mMediaPlayer = null;
            if (mAssetFileDescriptor != null) {
                try {
                    mAssetFileDescriptor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            setKeepScreenOn(false);
            if (mAudioFocusHelper != null) {
                mAudioFocusHelper.abandonFocus();
            }
            if (mRenderView != null) {
                mPlayerContainer.removeView(mRenderView.getView());
                mRenderView.release();
            }
            mCurrentPosition = 0;
            setPlayState(STATE_IDLE);
        }
    }

    /**
     * 保存播放进度
     */
    protected void saveProgress() {
        if (mCurrentPosition != 0 && mProgressManager != null) {
            L.d("saveProgress: " + mCurrentPosition);
            mProgressManager.saveProgress(mUrl, mCurrentPosition);
        }
    }

    /**
     * 是否处于播放状态
     */
    protected boolean isInPlaybackState() {
        return mMediaPlayer != null
                && mCurrentPlayState != STATE_ERROR
                && mCurrentPlayState != STATE_IDLE
                && mCurrentPlayState != STATE_PREPARING
                && mCurrentPlayState != STATE_PLAYBACK_COMPLETED;
    }

    /**
     * 是否处于未播放转态，此时{@link #mMediaPlayer}为null
     */
    protected boolean isInIdleState() {
        return mMediaPlayer == null
                || mCurrentPlayState == STATE_IDLE;
    }

    /**
     * 重新播放
     *
     * @param resetPosition 是否从头开始播放
     */
    @Override
    public void replay(boolean resetPosition) {
        if (resetPosition) {
            mCurrentPosition = 0;
        }
        startPrepare(true);
    }

    /**
     * 获取视频总时长
     */
    @Override
    public long getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    /**
     * 获取当前播放的位置
     */
    @Override
    public long getCurrentPosition() {
        if (isInPlaybackState()) {
            mCurrentPosition = mMediaPlayer.getCurrentPosition();
            return mCurrentPosition;
        }
        return 0;
    }

    /**
     * 调整播放进度
     */
    @Override
    public void seekTo(long pos) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(pos);
        }
    }

    /**
     * 是否处于播放状态
     */
    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    /**
     * 获取当前缓冲百分比
     */
    @Override
    public int getBufferedPercentage() {
        return mMediaPlayer != null ? mMediaPlayer.getBufferedPercentage() : 0;
    }

    /**
     * 设置静音
     */
    @Override
    public void setMute(boolean isMute) {
        if (mMediaPlayer != null) {
            this.mIsMute = isMute;
            float volume = isMute ? 0.0f : 1.0f;
            mMediaPlayer.setVolume(volume, volume);
        }
    }

    /**
     * 是否处于静音状态
     */
    @Override
    public boolean isMute() {
        return mIsMute;
    }

    /**
     * 视频播放出错回调
     */
    @Override
    public void onError() {
        setPlayState(STATE_ERROR);
    }

    /**
     * 视频播放完成回调
     */
    @Override
    public void onCompletion() {
        setPlayState(STATE_PLAYBACK_COMPLETED);
        setKeepScreenOn(false);
        mCurrentPosition = 0;
        if (mProgressManager != null) {
            //播放完成，清除进度
            mProgressManager.saveProgress(mUrl, 0);
        }
    }

    @Override
    public void onInfo(int what, int extra) {
        switch (what) {
            case AbstractPlayer.MEDIA_INFO_BUFFERING_START:
                setPlayState(STATE_BUFFERING);
                break;
            case AbstractPlayer.MEDIA_INFO_BUFFERING_END:
                setPlayState(STATE_BUFFERED);
                break;
            case AbstractPlayer.MEDIA_INFO_VIDEO_RENDERING_START: // 视频开始渲染
                setPlayState(STATE_PLAYING);
                if (getWindowVisibility() != VISIBLE) pause();
                break;
            case AbstractPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                if (mRenderView != null)
                    mRenderView.setVideoRotation(extra);
                break;
        }
    }

    /**
     * 视频缓冲完毕，准备开始播放时回调
     */
    @Override
    public void onPrepared() {
        setPlayState(STATE_PREPARED);
        addDisplay();
        if (mCurrentPosition > 0) {
            seekTo(mCurrentPosition);
        }
    }

    /**
     * 获取当前播放器的状态
     */
    public int getCurrentPlayerState() {
        return mCurrentPlayerState;
    }

    /**
     * 获取当前的播放状态
     */
    public int getCurrentPlayState() {
        return mCurrentPlayState;
    }

    /**
     * 获取缓冲速度
     */
    @Override
    public long getTcpSpeed() {
        return mMediaPlayer != null ? mMediaPlayer.getTcpSpeed() : 0;
    }

    /**
     * 设置播放速度
     */
    @Override
    public void setSpeed(float speed) {
        if (isInPlaybackState()) {
            mMediaPlayer.setSpeed(speed);
        }
    }

    /**
     * 设置视频地址
     */
    public void setUrl(String url) {
        setUrl(url, null);
    }

    /**
     * 设置包含请求头信息的视频地址
     *
     * @param url     视频地址
     * @param headers 请求头
     */
    public void setUrl(String url, Map<String, String> headers) {
        mUrl = url;
        mHeaders = headers;
    }

    /**
     * 用于播放assets里面的视频文件
     */
    public void setAssetFileDescriptor(AssetFileDescriptor fd) {
        this.mAssetFileDescriptor = fd;
    }

    /**
     * 一开始播放就seek到预先设置好的位置
     */
    public void skipPositionWhenPlay(int position) {
        this.mCurrentPosition = position;
    }

    /**
     * 设置音量 0.0f-1.0f 之间
     *
     * @param v1 左声道音量
     * @param v2 右声道音量
     */
    public void setVolume(float v1, float v2) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(v1, v2);
        }
    }

    /**
     * 设置进度管理器，用于保存播放进度
     */
    public void setProgressManager(@Nullable ProgressManager progressManager) {
        this.mProgressManager = progressManager;
    }

    /**
     * 循环播放， 默认不循环播放
     */
    public void setLooping(boolean looping) {
        mIsLooping = looping;
        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(looping);
        }
    }

    /**
     * 是否启用SurfaceView，默认不启用
     */
    public void setUsingSurfaceView(boolean usingSurfaceView) {
        mUsingSurfaceView = usingSurfaceView;
    }

    /**
     * 是否开启AudioFocus监听， 默认开启
     */
    public void setEnableAudioFocus(boolean enableAudioFocus) {
        mEnableAudioFocus = enableAudioFocus;
    }

    /**
     * 是否使用MediaCodec进行解码（硬解码），默认不开启，使用软解
     */
    public void setEnableMediaCodec(boolean enableMediaCodec) {
        mEnableMediaCodec = enableMediaCodec;
    }

    /**
     * 自定义播放核心，继承{@link PlayerFactory}实现自己的播放核心
     */
    public void setPlayerFactory(PlayerFactory<P> playerFactory) {
        if (playerFactory == null) {
            throw new IllegalArgumentException("PlayerFactory can not be null!");
        }
        mPlayerFactory = playerFactory;
    }

    /**
     * 支持多开
     */
    public void setEnableParallelPlay(boolean enableParallelPlay) {
        mEnableParallelPlay = enableParallelPlay;
    }

    /**
     * 进入全屏
     */
    @Override
    public void startFullScreen() {
        if (mIsFullScreen)
            return;

        ViewGroup decorView = getDecorView();
        if (decorView == null)
            return;

        //隐藏NavigationBar和StatusBar
        if (mHideNavBarView == null) {
            mHideNavBarView = new View(getContext());
        }
        mHideNavBarView.setSystemUiVisibility(FULLSCREEN_FLAGS);
        mPlayerContainer.addView(mHideNavBarView);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //从当前FrameLayout中移除播放器视图
        this.removeView(mPlayerContainer);
        //将播放器视图添加到DecorView中即实现了全屏
        decorView.addView(mPlayerContainer);

        mIsFullScreen = true;
        setPlayerState(PLAYER_FULL_SCREEN);
    }

    /**
     * 退出全屏
     */
    @Override
    public void stopFullScreen() {
        if (!mIsFullScreen)
            return;

        ViewGroup decorView = getDecorView();
        if (decorView == null)
            return;

        //显示NavigationBar和StatusBar
        mPlayerContainer.removeView(mHideNavBarView);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //把播放器视图从DecorView中移除并添加到当前FrameLayout中即退出了全屏
        decorView.removeView(mPlayerContainer);
        this.addView(mPlayerContainer);

        mIsFullScreen = false;
        setPlayerState(PLAYER_NORMAL);
    }

    /**
     * 获取DecorView
     */
    protected ViewGroup getDecorView() {
        Activity activity = getActivity();
        if (activity == null) return null;
        return (ViewGroup) activity.getWindow().getDecorView();
    }

    /**
     * 获取activity中的content view,其id为android.R.id.content
     */
    protected ViewGroup getContentView() {
        Activity activity = getActivity();
        if (activity == null) return null;
        return activity.findViewById(android.R.id.content);
    }

    /**
     * 获取Activity
     */
    protected Activity getActivity() {
        Activity activity = PlayerUtils.scanForActivity(getContext());
        if (activity == null) {
            if (mVideoController == null) return null;
            activity = PlayerUtils.scanForActivity(mVideoController.getContext());
        }
        return activity;
    }

    /**
     * 判断是否处于全屏状态
     */
    @Override
    public boolean isFullScreen() {
        return mIsFullScreen;
    }

    /**
     * 开启小屏
     */
    public void startTinyScreen() {
        if (mIsTinyScreen) return;
        ViewGroup contentView = getContentView();
        if (contentView == null) return;
        this.removeView(mPlayerContainer);
        int width = mTinyScreenSize[0];
        if (width <= 0) {
            width = PlayerUtils.getScreenWidth(getContext(), false) / 2;
        }

        int height = mTinyScreenSize[1];
        if (height <= 0) {
            height = width * 9 / 16;
        }

        LayoutParams params = new LayoutParams(width, height);
        params.gravity = Gravity.BOTTOM | Gravity.END;
        contentView.addView(mPlayerContainer, params);
        mIsTinyScreen = true;
        setPlayerState(PLAYER_TINY_SCREEN);
    }

    /**
     * 退出小屏
     */
    public void stopTinyScreen() {
        if (!mIsTinyScreen) return;

        ViewGroup contentView = getContentView();
        if (contentView == null) return;
        contentView.removeView(mPlayerContainer);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mPlayerContainer, params);

        mIsTinyScreen = false;
        setPlayerState(PLAYER_NORMAL);
    }

    public boolean isTinyScreen() {
        return mIsTinyScreen;
    }

    @Override
    public void onVideoSizeChanged(int videoWidth, int videoHeight) {
        mVideoSize[0] = videoWidth;
        mVideoSize[1] = videoHeight;

        if (mRenderView != null) {
            mRenderView.setScaleType(mCurrentScreenScaleType);
            mRenderView.setVideoSize(videoWidth, videoHeight);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            //重新获得焦点时保持全屏状态
            if (mHideNavBarView != null) {
                mHideNavBarView.setSystemUiVisibility(FULLSCREEN_FLAGS);
            }
        }
    }

    /**
     * 设置控制器，传null表示移除控制器
     */
    public void setVideoController(@Nullable BaseVideoController mediaController) {
        mPlayerContainer.removeView(mVideoController);
        mVideoController = mediaController;
        if (mediaController != null) {
            mediaController.setMediaPlayer(this);
            LayoutParams params = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mPlayerContainer.addView(mVideoController, params);
        }
    }

    /**
     * 设置视频比例
     */
    @Override
    public void setScreenScaleType(int screenScaleType) {
        mCurrentScreenScaleType = screenScaleType;
        if (mRenderView != null) {
            mRenderView.setScaleType(screenScaleType);
        }
    }

    /**
     * 设置镜像旋转，暂不支持SurfaceView
     */
    @Override
    public void setMirrorRotation(boolean enable) {
        if (mRenderView != null) {
            mRenderView.getView().setScaleX(enable ? -1 : 1);
        }
    }

    /**
     * 截图，暂不支持SurfaceView
     */
    @Override
    public Bitmap doScreenShot() {
        if (mRenderView != null) {
            return mRenderView.doScreenShot();
        }
        return null;
    }

    /**
     * 获取视频宽高,其中width: mVideoSize[0], height: mVideoSize[1]
     */
    @Override
    public int[] getVideoSize() {
        return mVideoSize;
    }

    /**
     * 旋转视频画面
     *
     * @param rotation 角度
     */
    @Override
    public void setRotation(float rotation) {
        if (mRenderView != null) {
            mRenderView.setVideoRotation((int) rotation);
        }
    }

    /**
     * 设置小屏的宽高
     *
     * @param tinyScreenSize 其中tinyScreenSize[0]是宽，tinyScreenSize[1]是高
     */
    public void setTinyScreenSize(int[] tinyScreenSize) {
        this.mTinyScreenSize = tinyScreenSize;
    }

    /**
     * 向Controller设置播放状态，用于控制Controller的ui展示
     */
    protected void setPlayState(int playState) {
        mCurrentPlayState = playState;
        if (mVideoController != null)
            mVideoController.setPlayState(playState);
        if (mOnVideoViewStateChangeListeners != null) {
            for (int i = 0, z = mOnVideoViewStateChangeListeners.size(); i < z; i++) {
                OnVideoViewStateChangeListener listener = mOnVideoViewStateChangeListeners.get(i);
                if (listener != null) {
                    listener.onPlayStateChanged(playState);
                }
            }
        }
    }

    /**
     * 向Controller设置播放器状态，包含全屏状态和非全屏状态
     */
    protected void setPlayerState(int playerState) {
        mCurrentPlayerState = playerState;
        if (mVideoController != null)
            mVideoController.setPlayerState(playerState);
        if (mOnVideoViewStateChangeListeners != null) {
            for (int i = 0, z = mOnVideoViewStateChangeListeners.size(); i < z; i++) {
                OnVideoViewStateChangeListener listener = mOnVideoViewStateChangeListeners.get(i);
                if (listener != null) {
                    listener.onPlayerStateChanged(playerState);
                }
            }
        }
    }

    /**
     * 监听播放状态变化
     */
    public void addOnVideoViewStateChangeListener(@NonNull OnVideoViewStateChangeListener listener) {
        if (mOnVideoViewStateChangeListeners == null) {
            mOnVideoViewStateChangeListeners = new ArrayList<>();
        }
        mOnVideoViewStateChangeListeners.add(listener);
    }

    /**
     * 移除播放状态监听
     */
    public void removeOnVideoViewStateChangeListener(@NonNull OnVideoViewStateChangeListener listener) {
        if (mOnVideoViewStateChangeListeners != null) {
            mOnVideoViewStateChangeListeners.remove(listener);
        }
    }

    /**
     * 设置播放状态监听
     */
    public void setOnVideoViewStateChangeListener(@NonNull OnVideoViewStateChangeListener listener) {
        if (mOnVideoViewStateChangeListeners == null) {
            mOnVideoViewStateChangeListeners = new ArrayList<>();
        } else {
            mOnVideoViewStateChangeListeners.clear();
        }
        mOnVideoViewStateChangeListeners.add(listener);
    }

    /**
     * 移除所有播放状态监听
     */
    public void clearOnVideoViewStateChangeListeners() {
        if (mOnVideoViewStateChangeListeners != null) {
            mOnVideoViewStateChangeListeners.clear();
        }
    }

    /**
     * 改变返回键逻辑，用于activity
     */
    public boolean onBackPressed() {
        return mVideoController != null && mVideoController.onBackPressed();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        L.d("onSaveInstanceState: " + mCurrentPosition);
        //activity切到后台后可能被系统回收，故在此处进行进度保存
        saveProgress();
        return super.onSaveInstanceState();
    }
}
