package com.ccb.ccvideoplay.videoplayer.listener;

/**
 * Created by xinyu on 2017/12/21.
 */

public interface PlayerEventListener {

    void onError();

    void onCompletion();

    void onInfo(int what, int extra);

    void onPrepared();

    void onVideoSizeChanged(int width, int height);

}
