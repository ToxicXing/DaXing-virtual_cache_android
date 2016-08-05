package com.example.daxing.qualitytest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class DeveloperTabActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final String TAG = DeveloperTabActivity.class.getSimpleName();
//    private Button b_wifi;
//    private Button b_ping;
//    private Button b_writelog;
//    private Button b_celluar;
    private Button b_change_account;
//    private Button b_update_ticket;
//    private TextView t_wifi;
//    private TextView t_ping;
//    private TextView t_celluar;
    private TextView t_tickets;
    private MyPhoneStateListener MyListener;
    private TelephonyManager Tel;
    static final String PING_DEST_URL = "8.8.8.8";

    private static String pingError = "";
    private ArrayList<DeviceSchema> log;
    private GoogleApiClient mGoogleApiClient;
    SharedPreferences sharedPrefs;
    private int currentTicketnum = 0;
    @Override
    public void onConnected(Bundle bundle) {
        //mGoogleApiClient.connect();
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
        setContentView(R.layout.activity_developer_tab);
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

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }
        sharedPrefs = getSharedPreferences("qualityTest", MODE_PRIVATE);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Starting application");
        setUI();
        getMyTicket();
        super.onStart();
    }//onStart


    private void setUI() {
        Log.i(TAG, "Set UI");
//        b_wifi = (Button) findViewById(R.id.b_wifi);
//        b_wifi.setOnClickListener(this);
//
//        b_ping = (Button) findViewById(R.id.b_ping);
//        b_ping.setOnClickListener(this);
//
//        b_writelog = (Button) findViewById(R.id.b_writelog);
//        b_writelog.setOnClickListener(this);
//
//        b_celluar = (Button) findViewById(R.id.b_celluar);
//        b_celluar.setOnClickListener(this);

        b_change_account = (Button) findViewById(R.id.b_change_account);
        b_change_account.setOnClickListener(this);

//        b_update_ticket = (Button) findViewById(R.id.b_update_ticket);
//        b_update_ticket.setOnClickListener(this);
//        t_wifi = (TextView) findViewById(R.id.wifiInfo);
//
//        t_ping = (TextView) findViewById(R.id.pingInfo);
//
//        t_celluar = (TextView) findViewById(R.id.celluar);
//
        t_tickets = (TextView) findViewById(R.id.tickets);
    }

    /**
     * OnClick event handler
     *
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.b_wifi: {
//                getWifiStats();
//                break;
//            }
//            case R.id.b_ping: {
//                onButtonPingClicked();
//                break;
//            }
//
//            case R.id.b_writelog: {
//                onButtonWriteJsonClicked();
//                break;
//            }
//
//            case R.id.b_celluar: {
//                onButtonSignalClicked();
//                break;
//            }

            case R.id.b_change_account: {
                onButtonChangeAccountClicked();
                break;
            }

//            case R.id.b_update_ticket: {
//                onButtonUpdateTicketClicked();
//                break;
//            }
        }
    }

//    public void onButtonUpdateTicketClicked(){
//        String numofticket = sharedPrefs.getString("tickets","");
//        if (numofticket == "") {
//            currentTicketnum = 0;
//        } else {
//            currentTicketnum = Integer.parseInt(numofticket);
//        }
//        t_tickets.setText("Current tickets: " + currentTicketnum);
//    }

    public void onButtonChangeAccountClicked() {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.remove("AccessToken");
        editor.remove("tickets");
        editor.commit();// 提交修改
        Intent changeAccountIntent = new Intent(DeveloperTabActivity.this, LoginActivity.class);
        startActivity(changeAccountIntent);
        finish();
    }


    public void onButtonPingClicked() {
        try {
            String back_server_info = ping(PING_DEST_URL);
//            t_ping.setText("rtt min/avg/max/mdev = " + back_server_info);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onButtonWriteJsonClicked() {
        Log.e("Write Log button", "clicked");
        Log.e("log contents", log.toString());
        String PATH = Environment.getExternalStorageDirectory()+ "/Loginfo/";
        File targetLocation = new File(PATH);
        if (!targetLocation.exists()) {
            targetLocation.mkdirs();
        }
        Gson gson = new Gson();
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

    //Check Celluar Signal Level
    private void onButtonSignalClicked() {
        MyListener = new MyPhoneStateListener();
        Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    void getMyTicket() {
        String numofticket = sharedPrefs.getString("tickets","");
        if (numofticket == "") {
            currentTicketnum = 0;
        } else {
            currentTicketnum = Integer.parseInt(numofticket);
        }
        t_tickets.setText("Current tickets: " + currentTicketnum);
    }

    //Check Celluar SignalLevel
    private class MyPhoneStateListener extends PhoneStateListener {
        /* Get the Signal strength from the provider, each time there is an update*/
        public int signalStrengthValue;
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if (signalStrength.isGsm()) {
                if (signalStrength.getGsmSignalStrength() != 99)
                    signalStrengthValue = signalStrength.getGsmSignalStrength() * 2 - 113;
                else
                    signalStrengthValue = signalStrength.getGsmSignalStrength();
            } else {
                signalStrengthValue = signalStrength.getCdmaDbm();
            }
//            t_celluar.setText("SIGNAL" + String.valueOf(signalStrength.getGsmSignalStrength()));
        }
    }

    @Override
    protected void onStop() {
        onButtonWriteJsonClicked();
        super.onStop();
    }
    public static String ping(String host) throws IOException, InterruptedException {
        StringBuffer echo = new StringBuffer();
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec("ping -c 3 " + host);
        proc.waitFor();
        int exit = proc.exitValue();
        if (exit == 0) {
            InputStreamReader reader = new InputStreamReader(proc.getInputStream());
            BufferedReader buffer = new BufferedReader(reader);
            String line = "";
            while ((line = buffer.readLine()) != null) {
                echo.append(line + "\n");
            }
            return getPingStats(echo.toString());
        } else if (exit == 1) {
            pingError = "failed, exit = 1";
            System.out.println(pingError);
            return null;
        } else {
            pingError = "error, exit = 2";
            System.out.println(pingError);
            return null;
        }
    }


    public static String getPingStats(String s) {
        /**
         * Format:
         * --- 127.0.0.1 ping statistics ---
         * 4 packets transmitted, 4 received, 0% packet loss, time 0ms
         * rtt min/avg/max/mdev = 0.251/0.285/0.300/0.019 ms
         **/
        if (s.contains("0% packet loss")) {
            int start = s.indexOf("/mdev = ");
            int end = s.indexOf(" ms\n", start);
            s = s.substring(start + 8, end);
            return s;
            //s = s.substring(0,2);
//            String stats[] = s.split("/");
//            return stats[2];
        } else if (s.contains("100% packet loss")) {
            pingError = "100% packet loss";
            System.out.println(pingError);
            return null;
        } else if (s.contains("% packet loss")) {
            pingError = "partial packet loss";
            System.out.println(pingError);
            return null;
        } else if (s.contains("unknown host")) {
            pingError = "unknown host";
            System.out.println(pingError);
            return null;
        } else {
            pingError = "unknown error in getPingStats";
            System.out.println(pingError);
            return null;
        }
    }


    public void getWifiStats() {
        //Wifi signal level
        String wserviceName = Context.WIFI_SERVICE;
        WifiManager wm = (WifiManager) getSystemService(wserviceName);
        WifiInfo info = wm.getConnectionInfo();
        int strength = info.getRssi();
        int speed = info.getLinkSpeed();
        String units = WifiInfo.LINK_SPEED_UNITS;
        String ssid = info.getSSID();
        String ip_addr = intToIp(info.getIpAddress());

        String text = "We are connecting to " + ssid + " at " + String.valueOf(speed) + "  " + String.valueOf(units) + " with IP addr. " + ip_addr  + ". Strength : " + strength;
//        t_wifi.setText(text);
    }

    private String intToIp(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
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
