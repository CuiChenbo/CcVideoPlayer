package com.ccb.ccvideoplay.videoplayer.player;

import android.app.Application;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;


import com.ccb.ccvideoplay.videoplayer.util.PlayerUtils;

import java.util.Map;

public class AndroidMediaPlayer extends AbstractPlayer {

    protected MediaPlayer mMediaPlayer;
    private boolean isLooping;
    private int mBufferedPercent;

    @Override
    public void initPlayer() {
        mMediaPlayer = new MediaPlayer();
        setOptions();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnErrorListener(onErrorListener);
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
        mMediaPlayer.setOnInfoListener(onInfoListener);
        mMediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
        mMediaPlayer.setOnPreparedListener(onPreparedListener);
        mMediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) {
        try {
            Application application = PlayerUtils.getApplication();
            if (application != null) {
                mMediaPlayer.setDataSource(application, Uri.parse(path), headers);
            }
        } catch (Exception e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void setDataSource(AssetFileDescriptor fd) {
        try {
            mMediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
        } catch (Exception e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void start() {
        try {
            mMediaPlayer.start();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void pause() {
        try {
            mMediaPlayer.pause();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void stop() {
        try {
            mMediaPlayer.stop();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void prepareAsync() {
        try {
            mMediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void reset() {
        mMediaPlayer.release();
        initPlayer();
        mMediaPlayer.setVolume(1, 1);
        mMediaPlayer.setLooping(isLooping);
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(long time) {
        try {
            mMediaPlayer.seekTo((int) time);
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void release() {
        mMediaPlayer.setOnErrorListener(null);
        mMediaPlayer.setOnCompletionListener(null);
        mMediaPlayer.setOnInfoListener(null);
        mMediaPlayer.setOnBufferingUpdateListener(null);
        mMediaPlayer.setOnPreparedListener(null);
        mMediaPlayer.setOnVideoSizeChangedListener(null);
        new Thread() {
            @Override
            public void run() {
                try {
                    mMediaPlayer.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public long getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public int getBufferedPercentage() {
        return mBufferedPercent;
    }

    @Override
    public void setSurface(Surface surface) {
        mMediaPlayer.setSurface(surface);
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        mMediaPlayer.setDisplay(holder);
    }

    @Override
    public void setVolume(float v1, float v2) {
        mMediaPlayer.setVolume(v1, v2);
    }

    @Override
    public void setLooping(boolean isLooping) {
        this.isLooping = isLooping;
        mMediaPlayer.setLooping(isLooping);
    }

    @Override
    public void setEnableMediaCodec(boolean isEnable) {
        // no support
    }

    @Override
    public void setOptions() {
        // no support
    }

    @Override
    public void setSpeed(float speed) {
        // no support
    }

    @Override
    public long getTcpSpeed() {
        // no support
        return 0;
    }

    private MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mPlayerEventListener.onError();
            return true;
        }
    };

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mPlayerEventListener.onCompletion();
        }
    };

    private MediaPlayer.OnInfoListener onInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            mPlayerEventListener.onInfo(what, extra);
            return true;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mBufferedPercent = percent;
        }
    };


    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mPlayerEventListener.onPrepared();
            start();
        }
    };

    private MediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            int videoWidth = mp.getVideoWidth();
            int videoHeight = mp.getVideoHeight();
            if (videoWidth != 0 && videoHeight != 0) {
                mPlayerEventListener.onVideoSizeChanged(videoWidth, videoHeight);
            }
        }
    };
}
