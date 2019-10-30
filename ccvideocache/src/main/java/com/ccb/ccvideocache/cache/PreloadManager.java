package com.ccb.ccvideocache.cache;

import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import www.ccb.com.common.utils.LogUtils;

/**
 * 抖音预加载工具，实现AndroidVideoCache实现
 */
public class PreloadManager {

    private static PreloadManager sPreloadManager;

    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    /**
     * 保存正在预加载的{@link PreloadTask}
     */
    private LinkedHashMap<String, PreloadTask> mPreloadTasks = new LinkedHashMap<>();

    private boolean mIsStartPreload = true;

    private HttpProxyCacheServer mHttpProxyCacheServer;

    private PreloadManager(Context context) {
        mHttpProxyCacheServer = ProxyVideoCacheManager.getProxy(context);
    }

    public static PreloadManager getInstance(Context context) {
        if (sPreloadManager == null) {
            synchronized (PreloadManager.class) {
                if (sPreloadManager == null) {
                    sPreloadManager = new PreloadManager(context.getApplicationContext());
                }
            }
        }
        return sPreloadManager;
    }

    /**
     * 开始预加载
     *
     * @param rawUrl 原始视频地址
     */
    public void startPreload(String rawUrl, int position) {
        PreloadTask task = new PreloadTask();
        task.mRawUrl = rawUrl;
        task.mPosition = position;
        task.mCacheServer = mHttpProxyCacheServer;
        LogUtils.i("startPreload: " + position);
        mPreloadTasks.put(rawUrl, task);

        if (mIsStartPreload) {
            //开始预加载
            for (Map.Entry<String, PreloadTask> next : mPreloadTasks.entrySet()) {
                PreloadTask preloadTask = next.getValue();
                preloadTask.executeOn(mExecutorService);
            }
        }
    }

    /**
     * 暂停预加载
     * 根据是否反向滑动取消在position之下或之上的PreloadTask
     *
     * @param position 当前滑到的位置
     * @param isReverseScroll 列表是否反向滑动
     */
    public void pausePreload(int position, boolean isReverseScroll) {
        LogUtils.i("pausePreload：" + position);
        mIsStartPreload = false;
        for (Map.Entry<String, PreloadTask> next : mPreloadTasks.entrySet()) {
            PreloadTask task = next.getValue();
            if (isReverseScroll) {
                if (task.mPosition > position) {
                    task.cancel();
                }
            } else {
                if (task.mPosition < position) {
                    task.cancel();
                }
            }
        }
    }

    /**
     * 恢复预加载
     * 根据是否反向滑动开始在position之下或之上的PreloadTask
     *
     * @param position        当前滑到的位置
     * @param isReverseScroll 列表是否反向滑动
     */
    public void resumePreload(int position, boolean isReverseScroll) {
        LogUtils.i("resumePreload：" + position);
        mIsStartPreload = true;
        for (Map.Entry<String, PreloadTask> next : mPreloadTasks.entrySet()) {
            PreloadTask task = next.getValue();
            if (isReverseScroll) {
                if (task.mPosition < position) {
                    task.executeOn(mExecutorService);
                }
            } else {
                if (task.mPosition > position) {
                    task.executeOn(mExecutorService);
                }
            }
        }
    }

    /**
     * 通过原始地址取消预加载
     *
     * @param rawUrl 原始地址
     * @param remove 是否移除
     */
    public String cancelPreloadByUrl(String rawUrl, boolean remove) {
        Iterator<Map.Entry<String, PreloadTask>> iterator = mPreloadTasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PreloadTask> next = iterator.next();
            if (next.getKey().equals(rawUrl)) {
                PreloadTask task = next.getValue();
                if (remove) {
                    iterator.remove();
                }
                return task.cancel();
            }
        }
        return null;
    }

    /**
     * 取消所有的预加载
     */
    public void cancelAll() {
        Iterator<Map.Entry<String, PreloadTask>> iterator = mPreloadTasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PreloadTask> next = iterator.next();
            PreloadTask task = next.getValue();
            task.cancel();
            iterator.remove();
        }
    }

    /**
     * 获取代理地址，获取不到就返回原始地址
     */
    public String getPlayUrl(String rawUrl) {
        String proxyUrl = cancelPreloadByUrl(rawUrl, false);
        if (proxyUrl == null) {
            return mHttpProxyCacheServer.getProxyUrl(rawUrl);
        } else {
            return proxyUrl;
        }
    }
}