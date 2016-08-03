package com.example.daxing.qualitytest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class TrendingTabActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final String TAG = TrendingTabActivity.class.getSimpleName();
    public final  String VIDEO_ID_MESSAGE = "VIDEOID";
    private Button b_trending;
    private List<String> region_code = new ArrayList<String>();
    private ArrayAdapter<String> region_adapter;
    private String country_code = "US";
    private Spinner mySpinner;
    private ListView lv_videolist;
    private LogSingleton logSingleton;
    private String UserID;

    private GoogleApiClient mGoogleApiClient;
//    AsyncHttpClient client = new AsyncHttpClient();
//    StringEntity se = null;
//    private ArrayList<DeviceSchema> log;
    HashMap<String, String> region_map = new HashMap<String, String>();

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("google play onConnected", "connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            Log.i(TAG, "API UNAVAILABLE");
        }
    }


   // AsyncHttpClient ajax = new AsyncHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending_tab);
        Intent access_userid_intent = getIntent();
        UserID = access_userid_intent.getStringExtra("account");
//        String content = "";
//        Gson gson = new Gson();
//
//
//        try {
//            File targetFile = new File(Environment.getExternalStorageDirectory() + "/Loginfo/", "log.txt");
//            if(targetFile.length() > 0){
//                content =  FileUtils.readFileToString(targetFile);
//                Log.e("onCreate content",content);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if(content.length() > 0){
//            Log.e("onCreate", "logs found!");
//            DeviceSchema[] array = gson.fromJson(content, DeviceSchema[].class);
//            log = new ArrayList<DeviceSchema>(Arrays.asList(array));
//        } else {
//            Log.e("onCreate", "logs not found :(");
//            log = new ArrayList<DeviceSchema>();
//        }
        logSingleton = LogSingleton.getInstance();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }
        initial_region_map();
        region_code.add("United States");
        region_code.add("Austria");
        region_code.add("Australia");
        region_code.add("Brazil");
        region_code.add("Canada");
        region_code.add("Switzerland");
        region_code.add("China");
        region_code.add("Colombia");
        region_code.add("Spain");
        region_code.add("Finland");
        region_code.add("United Kingdom");
        region_code.add("Greece");
        region_code.add("Hong Kong");
        region_code.add("Italy");
        region_code.add("Japan");
        region_code.add("Korea");
        region_code.add("Macau");
        region_code.add("Mexico");
        region_code.add("Malaysia");
        region_code.add("New Zealand");
        region_code.add("Philippines");
        region_code.add("Poland");
        region_code.add("Portugal");
        region_code.add("Singapore");
        region_code.add("Sweden");
        region_code.add("Thailand");
        region_code.add("Taiwan");
        region_code.add("South Africa");



    }
    protected void initial_region_map() {
        region_map.put("United States", "US");
        region_map.put("Austria", "AT");
        region_map.put("Australia", "AU");
        region_map.put("Brazil", "BR");
        region_map.put("Canada", "CA");
        region_map.put("Switzerland", "CH");
        region_map.put("China", "CN");
        region_map.put("Colombia", "CO");
        region_map.put("Spain", "ES");
        region_map.put("Finland", "FI");
        region_map.put("United Kingdom", "GB");
        region_map.put("Greece", "GR");
        region_map.put("Hong Kong", "HK");
        region_map.put("Italy", "IT");
        region_map.put("Japan", "JP");
        region_map.put("Korea", "KR");
        region_map.put("Macau", "MO");
        region_map.put("Mexico", "MX");
        region_map.put("Malaysia", "MY");
        region_map.put("New Zealand", "NZ");
        region_map.put("Philippines", "PH");
        region_map.put("Poland", "PL");
        region_map.put("Portugal", "PT");
        region_map.put("Russian", "RU");
        region_map.put("Singapore", "SG");
        region_map.put("Sweden", "SE");
        region_map.put("Thailand", "TH");
        region_map.put("Taiwan", "TW");
        region_map.put("South Africa", "ZA");
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Starting application");
        mGoogleApiClient.connect();
        setUI();
        super.onStart();
    }//onStart

    private void setUI() {
        Log.i(TAG, "Set UI");
        b_trending = (Button) findViewById(R.id.b_trending);
        b_trending.setOnClickListener(this);

        lv_videolist = (ListView) findViewById(R.id.ls_video);
        lv_videolist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onItemVideoClicked(adapterView, view, i, l);
            }
        });

        mySpinner = (Spinner)findViewById(R.id.region_code);
        region_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, region_code);
        region_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(region_adapter);
        mySpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                country_code = region_map.get(region_adapter.getItem(arg2));
                arg0.setVisibility(View.VISIBLE);
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                country_code = "US";
                arg0.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.b_trending: {
                try {
                    onButtonTrendClicked();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Override
    protected void onStop() {
        onButtonWriteJsonClicked();
        super.onStop();
    }

    public void onButtonWriteJsonClicked() {
//        Log.e("Write Log button", "clicked");
//        Log.e("log contents", log.toString());
//        String PATH = Environment.getExternalStorageDirectory()+ "/Loginfo/";
//        File targetLocation = new File(PATH);
//        if (!targetLocation.exists()) {
//            targetLocation.mkdirs();
//        }
//        Gson gson = new Gson();
//        // Get Directory of SD card
//        String FILE_PATH = Environment.getExternalStorageDirectory() + "/Loginfo/" + "log.txt";
//        try {
//            PrintWriter writer = new PrintWriter(FILE_PATH);
//            writer.print(gson.toJson(log.toArray(),DeviceSchema[].class));
//            writer.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        logSingleton.print();
    }

    public void onButtonTrendClicked() throws ExecutionException, InterruptedException, JSONException {
        String trend_url = "https://www.googleapis.com/youtube/v3/videos?part=contentDetails,statistics,status,snippet&chart=mostPopular&regionCode=" + country_code +"&maxResults=25&key=AIzaSyAzmrXIdc2sU6zqUUhCBLsxCtoB1EtoicM";
        JSONObject trend_vid = new GetTrendVideo().execute(trend_url).get();
        JSONArray items = (JSONArray) trend_vid.get("items");
        ArrayList<ListItem> myList = new ArrayList<ListItem>();
        int size = items.length();
        for (int i = 0; i < size; i++) {
            ListItem newItem = new ListItem();
//            HashMap<String, Object> myMap = new HashMap<String, Object>();

            JSONObject single_video = (JSONObject) items.get(i);

            String vid_id = single_video.get("id").toString();
            System.out.println("ID is " + vid_id);
            newItem.setVideoID(vid_id);

            JSONObject video_snippet = (JSONObject) single_video.get("snippet");
            String video_title = video_snippet.get("title").toString();
            System.out.println("Title is " + video_title);

            JSONObject vid_thumbnails = (JSONObject) video_snippet.get("thumbnails");
            JSONObject vid_thumb_default = (JSONObject) vid_thumbnails.get("default");
            String thumb_url = vid_thumb_default.get("url").toString();
            newItem.setVideoTitle(video_title);
            newItem.setUrl(thumb_url);
            Log.i(TAG, thumb_url);
            myList.add(newItem);
        }
        prettyPrint(myList);
    }

    public void prettyPrint(ArrayList<ListItem> mylist) {
        lv_videolist.setAdapter(new CustomAdapter(this, mylist));
    }

    void onItemVideoClicked(AdapterView<?> adapterView, View view, int i, long l) {
//        String video_id = ((TextView)(view.findViewById(R.id.VideoID))).getText().toString();
//        String video_name = ((TextView)(view.findViewById(R.id.VideoTitle))).getText().toString();
////        SearchResult singleVideo = newResult.get(i);
////        ResourceId rId = singleVideo.getId();
////        String video_name = singleVideo.getSnippet().getTitle();
//
////        String content = generate_log_info(video_name, 44332);
////        write_to_log(content);
////        read_and_send();
//        sendJson(video_name, 123);
//        Intent intent = new Intent(this, PlayVideoActivity.class);
//        intent.putExtra(VIDEO_ID_MESSAGE, video_id);
//        startActivity(intent);
        String video_id = ((TextView)(view.findViewById(R.id.VideoID))).getText().toString();
        String video_name = ((TextView)(view.findViewById(R.id.VideoTitle))).getText().toString();

        Intent intent = new Intent(this, PlayVideoActivity.class);
        intent.putExtra("VIDEOID", video_id);
        intent.putExtra("VIDEONAME", video_name);
        Location temp = getLastLocation(mGoogleApiClient);
        double[] foo = {temp.getLongitude(), temp.getLatitude()};
        intent.putExtra("account", UserID);
        intent.putExtra("LOCATION", foo);

        startActivity(intent);
    }

//    public void sendJson(String title, int size) {
//        DeviceSchema device;
//        Gson gson  = new Gson();
//        if(log.size() > 0){
//            device =log.get(log.size()-1);
//        } else {
//            device = new DeviceSchema();
//        }
//
////      SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd    hh:mm:ss");
//        Date date =new java.util.Date();
//        String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
//
//        device.name = android_id;
//        //device.time = date.getTime();
//        Location temp = getLastLocation(mGoogleApiClient);
//        device.loc = new double[]{temp.getLongitude(), temp.getLatitude()};
//       // device.cache.add(new VideoSchema(title,size));
//
//
//        String json = gson.toJson(device);
//
//        //store DeviceSchema in the global log
//        log.add(device);
//
//
//        StringEntity se;
//        try {
//            se = new StringEntity(json);
//            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//            ajax.post(null, "http://www.edward-hu.com/logs", se, "application/json", new AsyncHttpResponseHandler() {
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                    Log.e("ajax", "success");
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                    Log.e("ajax", "success");
//                }
//            });
//
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//            Log.e("string entity", "failed");
//        }
//    }

    protected Location getLastLocation(GoogleApiClient mGoogleApiClient) {
        Log.i(TAG,"Get last location");
        Location mLastLocation;


        Log.d("google play onConnected", "connected");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        return mLastLocation;
    }

    protected void onResume() {
        super.onResume();
        initGPS();
    }

    //start: check if the location service or GPS is open
    private void initGPS() {
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        if ((!locationManager
                .isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))&&(!locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            Toast.makeText(getApplicationContext(), "Please open Location Service",
                    Toast.LENGTH_SHORT).show();
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Please open Location Service");
            dialog.setPositiveButton("OK",
                    new android.content.DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, 0);

                        }
                    });
            dialog.setNeutralButton("Cancel", new android.content.DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.dismiss();
                }
            } );
            dialog.show();
        } else if (!locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            Toast.makeText(getApplicationContext(), "Please open GPS",
                    Toast.LENGTH_SHORT).show();
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Open GPS?");
            dialog.setPositiveButton("Yes",
                    new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, 0);
                        }
                    });
            dialog.setNeutralButton("No", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.dismiss();
                }
            } );
            dialog.show();
        } else
        {

        }
    }
    //end: check if the location service or GPS is open
}
