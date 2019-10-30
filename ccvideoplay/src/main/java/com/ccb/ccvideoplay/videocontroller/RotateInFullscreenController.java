package com.ccb.ccvideoplay.videocontroller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ccb.ccvideoplay.R;
import com.ccb.ccvideoplay.videoplayer.player.VideoView;
import com.ccb.ccvideoplay.videoplayer.util.PlayerUtils;

/**
 * 列表播放时 点击进入全屏状态
 */
public class RotateInFullscreenController extends StandardVideoController {

    @Nullable
    private Activity mActivity;

    public RotateInFullscreenController(@NonNull Context context) {
        this(context, null);
    }

    public RotateInFullscreenController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateInFullscreenController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mActivity = PlayerUtils.scanForActivity(context);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (!mMediaPlayer.isFullScreen()) {
            mMediaPlayer.startFullScreen();
            return true;
        }
        if (mShowing) {
            hide();
        } else {
            show();
        }
        return true;
    }

    @Override
    protected void doStartStopFullScreen() {
        if (mActivity == null) return;
        int o = mActivity.getRequestedOrientation();
        if (o == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        mFullScreenButton.setSelected(o == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void setPlayerState(int playerState) {
        super.setPlayerState(playerState);
        switch (playerState) {
            case VideoView.PLAYER_FULL_SCREEN:
                mFullScreenButton.setSelected(false);
                getThumb().setVisibility(GONE);
                mOrientationHelper.disable();
                break;
            case VideoView.PLAYER_NORMAL:
                mOrientationHelper.disable();
                break;
        }
    }

    @Override
    public void onClick(View v) {

        int i = v.getId();
        if (i == R.id.fullscreen) {
            doStartStopFullScreen();
        } else if (i == R.id.lock) {
            doLockUnlock();
        } else if (i == R.id.iv_play) {
            doPauseResume();
        } else if (i == R.id.back) {
            stopFullScreenFromUser();
        } else if (i == R.id.thumb) {
            mMediaPlayer.start();
            mMediaPlayer.startFullScreen();
        } else if (i == R.id.iv_replay) {
            mMediaPlayer.replay(true);
            mMediaPlayer.startFullScreen();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mIsLocked) {
            show();
            Toast.makeText(getContext(), R.string.dkplayer_lock_tip, Toast.LENGTH_SHORT).show();
            return true;
        }
        if (mMediaPlayer.isFullScreen()) {
            stopFullScreenFromUser();
            return true;
        }
        return super.onBackPressed();
    }
}