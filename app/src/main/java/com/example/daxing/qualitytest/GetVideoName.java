package com.example.daxing.qualitytest;

import android.os.AsyncTask;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GetVideoName extends AsyncTask<String, String, List<String>> {

    public static List<String> LIST = new ArrayList<String>();
    @Override
    protected List<String> doInBackground(String... url) {
        if (url[0].contains("youtube")) {
            try {
                if (url[0] != null) {
                    URL embededURL = new URL("http://www.youtube.com/oembed?url=" +
                            url[0] + "&format=json"
                    );
                    LIST.add(new JSONObject(IOUtils.toString(embededURL)).getString("title"));
                    LIST.add(new JSONObject(IOUtils.toString(embededURL)).getString("author_name"));
                    return LIST;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(url[0].contains("vimeo")) {
            try {
                if (url[0] != null) {
                    URL embededURL = new URL("https://vimeo.com/api/oembed.json?url=" + url[0]);
                    LIST.add(new JSONObject(IOUtils.toString(embededURL)).getString("title"));
                    LIST.add(new JSONObject(IOUtils.toString(embededURL)).getString("author_name"));
                    return LIST;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    protected void onPostExecute(List<String> str) {
        System.out.println("Video Name: " + str.get(0));
        System.out.println("Author_Name: " + str.get(1));
    }
}
