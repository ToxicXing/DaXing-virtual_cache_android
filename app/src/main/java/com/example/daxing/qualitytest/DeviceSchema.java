package com.example.daxing.qualitytest;

import java.util.ArrayList;

/**
 * Created by edward on 7/5/16.
 */
public class DeviceSchema {
    public String account;
    public String token;
    public String ticket;
    public String name;
    public ArrayList<VideoSchema> cache = new ArrayList<VideoSchema>();
    public int d2d = 0;
    public String time;
    public double loc[];
    DeviceSchema(){}
}
