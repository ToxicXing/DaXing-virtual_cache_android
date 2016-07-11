package com.example.daxing.qualitytest;

/**
 * Created by edward on 7/5/16.
 */
public class VideoSchema {
    public String name;
    public String length;
    public String duration;
    public String battery;
    public String dataQuality;
    public String wifiQuality;
    public String id;
    VideoSchema(String name,String id, String length, String duration, String battery, String dataQuality,
                String wifiQuality){
        this.name = name;
        this.id = id;
        this.length = length;
        this.duration = duration;
        this.battery = battery;
        this.dataQuality = dataQuality;
        this.wifiQuality = wifiQuality;
    }
}
