package com.ccb.ccvideoplayer.bean;

/**
 * Created by Devlin_n on 2017/6/1.
 */

public class VideoBean {

    private String title;
    private String url;
    private String thumb;
    private int thumbRes = -1;

    public Integer getThumbRes() {
        return thumbRes;
    }

    public void setThumbRes(Integer thumbRes) {
        this.thumbRes = thumbRes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public VideoBean(String title, String thumb, String url) {
        this.title = title;
        this.url = url;
        this.thumb = thumb;

    }
    public VideoBean(String title, String thumb, int thumbRes, String url) {
        this.title = title;
        this.url = url;
        this.thumb = thumb;
        this.thumbRes = thumbRes;

    }
}
