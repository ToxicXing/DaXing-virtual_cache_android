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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class SearchingTabActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final String TAG = SearchingTabActivity.class.getSimpleName();

    private Button b_search;
    private ListView lv_videolist;
    private EditText et_keyword;
//    private ArrayList<DeviceSchema> log;
    private LogSingleton log;
    private ArrayList<ListItem> newResult;
    private GoogleApiClient mGoogleApiClient;
    private String UserID;

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Successfully connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            Log.i(TAG, "API UNAVAILABLE");
        }
        Log.e(TAG, "Connection to Google Play Service Failed.");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching_tab);
        log = LogSingleton.getInstance();
        Intent access_userid_intent = getIntent();
        UserID = access_userid_intent.getStringExtra("account");
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }
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

        b_search = (Button) findViewById(R.id.b_search);
        b_search.setOnClickListener(this);

        lv_videolist = (ListView) findViewById(R.id.ls_video);
        lv_videolist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onItemVideoClicked(adapterView, view, i, l);
            }
        });

        et_keyword = (EditText) findViewById(R.id.et_keyword);
    }

    @Override
    public void onClick(View view) {
        initGPS();
        switch (view.getId()) {
            case R.id.b_search: {
                onButtonSearchClicked();
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
        log.print();

    }

    public void onButtonSearchClicked()  {
        String keyword = et_keyword.getText().toString();
        try {
            newResult = new SearchYoutube().execute(keyword).get();
            if (newResult == null) {
                Log.i(TAG, "newResult is null");
            }
            prettyPrint(newResult);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    void onItemVideoClicked(AdapterView<?> adapterView, View view, int i, long l) {
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
//
//    }

    protected Location getLastLocation(GoogleApiClient mGoogleApiClient) {
        Log.i(TAG,"Get last location");
        Location mLastLocation;
        Log.d("google play onConnected", "connected");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        return mLastLocation;
    }

    public void prettyPrint(ArrayList<ListItem> mylist) {
        lv_videolist.setAdapter(new CustomAdapter(this, mylist));
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
