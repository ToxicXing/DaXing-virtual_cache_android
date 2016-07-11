package com.example.daxing.qualitytest;

public class ListItem {
    private String VideoTitle;
    private String VideoID;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setVideoTitle(String videotitle) {
        this.VideoTitle = videotitle;
    }

    public String getVideoTitle() {
        return VideoTitle;
    }

    public void setVideoID(String videoid) {
        this.VideoID = videoid;
    }

    public String getVideoID() {
        return VideoID;
    }

}
