package com.example.daxing.qualitytest;


import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

/**
 * Created by edward on 7/5/16.
 */
public class LogSingleton {
    private static LogSingleton instance;
    private DeviceSchema device;
    private ArrayList<DeviceSchema> log;
    private AsyncHttpClient ajax = new AsyncHttpClient();
    private LogSingleton(){
        String content = "";
        Gson gson = new Gson();

        try {
            File targetFile = new File(Environment.getExternalStorageDirectory() + "/Loginfo/", "log.txt");
            if(targetFile.length() > 0){
                content =  FileUtils.readFileToString(targetFile);
                Log.e("onCreate content",content);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(content.length() > 0){
            Log.e("onCreate", "logs found!");
            DeviceSchema[] array = gson.fromJson(content, DeviceSchema[].class);
            log = new ArrayList<DeviceSchema>(Arrays.asList(array));
        } else {
            Log.e("onCreate", "logs not found :(");
            log = new ArrayList<DeviceSchema>();
        }
    }
    public static LogSingleton getInstance(){
        if(instance == null){
            instance = new LogSingleton();
        }
        return instance;
    }
    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
    public void setLog(String android_id, double[] loc, String title, String video_id, String length,
                       String duration, String battery, String wifiQuality, String dataQuality, String ticket) {
        DeviceSchema device;
        if(log.size() > 0){
            device =log.get(log.size()-1);
        } else {
            device = new DeviceSchema();
        }

        device.ticket = ticket;
        device.name = android_id;
        device.time = getCurrentTimeStamp();
        device.loc = new double[]{loc[0], loc[1]};
        device.cache.add(new VideoSchema(title,video_id,length, duration, battery, dataQuality,
                wifiQuality));

        //store DeviceSchema in the global log
        log.add(device);
    }

    public void send(UserIdPair pair) throws Exception {
        StringEntity se;
        Gson gson  = new Gson();
        DeviceSchema device;
        if(log.size() > 0){
            device =log.get(log.size()-1);
        } else {
           throw new Exception("tried to get empty log");
        }
        device.account = pair.UserID;
        device.token = pair.UserID_key;
        final String json = gson.toJson(device);
        try {
            se = new StringEntity(json);
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            ajax.post(null, "http://www.edward-hu.com/logs", se, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.e("ajax", "success");
                    Log.e("ajax contents", json);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.e("ajax", "success");
                }
            });

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e("string entity", "failed");
        }
    }

    public void updateCurrentVideo(String duration) throws Exception {
        DeviceSchema device;
        if(log.size() > 0){
            device =log.get(log.size()-1);
        } else {
            throw new Exception("tried to get empty log");
        }
        if(device.cache.size() > 0){
            VideoSchema current = device.cache.get(device.cache.size()-1);
            current.duration = duration;
            device.cache.set(device.cache.size()-1,current);
        } else {
            throw new Exception("tried to get empty cache");
        }

    }


    public void print(){
        Log.e("Write Log button", "clicked");
        Gson gson = new Gson();
        for(DeviceSchema d : log){
            Log.e("DeviceSchema", gson.toJson(d, DeviceSchema.class));
        }
        String PATH = Environment.getExternalStorageDirectory()+ "/Loginfo/";
        File targetLocation = new File(PATH);
        if (!targetLocation.exists()) {
            targetLocation.mkdirs();
        }

        // Get Directory of SD card
        String FILE_PATH = Environment.getExternalStorageDirectory() + "/Loginfo/" + "log.txt";
        try {
            PrintWriter writer = new PrintWriter(FILE_PATH);
            writer.print(gson.toJson(log.toArray(),DeviceSchema[].class));
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
