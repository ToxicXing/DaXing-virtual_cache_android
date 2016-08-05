package com.example.daxing.qualitytest;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.PlaylistEventListener;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class PlayVideoActivity extends YouTubeFailureRecoveryActivity implements  YouTubePlayer.OnInitializedListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = PlayVideoActivity.class.getSimpleName();

    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private static final String KEY_CURRENTLY_SELECTED_ID = "currentlySelectedId";
    String video_id;
    double[] location;
    SharedPreferences sharedPrefs;
    int currentTicketnum = 0;
    UserIdPair userIdPair;
    private StringBuilder logString;
    private YouTubePlayerView youTubePlayerView;

//    private Button b_play;
//    private Button b_pause;
private YouTubePlayer player;
    private TextView tv_video_status;
    private TextView tv_log_info;
    private TextView user_duration;
    private ListView lv_related_video;
    private MyPlaylistEventListener playlistEventListener;
    private MyPlayerStateChangeListener playerStateChangeListener;
    private MyPlaybackEventListener playbackEventListener;
    private LogSingleton log;
    private String video_name;
    private String duration;
    private String length;
    private MyPhoneStateListener mpsl;
    private GoogleApiClient mGoogleApiClient;

    private static final int parseInt(String intString, int defaultValue) {
        try {
            return intString != null ? Integer.valueOf(intString) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_play_video);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        userIdPair = new UserIdPair();
        Intent intent = getIntent();
//        userIdPair.UserID = intent.getStringExtra("account");
        sharedPrefs = getSharedPreferences("qualityTest", MODE_PRIVATE);
        userIdPair.UserID = sharedPrefs.getString("account", "");
        try {
            userIdPair.UserID_key = SHAUtil.shaEncode(userIdPair.UserID + "virtual_cache");
        } catch (Exception e) {
            e.printStackTrace();
        }
        video_id = intent.getStringExtra("VIDEOID");
        video_name = intent.getStringExtra("VIDEONAME");
        location = intent.getDoubleArrayExtra("LOCATION");
//        Log.e("INTENT", loc.toString());
        Log.i(TAG,"Video ID is " + video_id);
        Log.i(TAG, "Video Name is " + video_name);
        log = LogSingleton.getInstance();
        mpsl = new MyPhoneStateListener();
        setUI();

        playlistEventListener = new MyPlaylistEventListener();
        playerStateChangeListener = new MyPlayerStateChangeListener();
        playbackEventListener = new MyPlaybackEventListener();
        logString = new StringBuilder();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        try {
            getRelatedVideo();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Starting application");
        mGoogleApiClient.connect();
        super.onStart();
    }//onStart

    protected void setUI() {
        Log.i(TAG, "Set UI");
        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        youTubePlayerView.initialize(DeveloperKey.DEVELOPER_KEY, this);

        tv_video_status = (TextView) findViewById(R.id.video_status);

        tv_log_info = (TextView) findViewById(R.id.tv_log_info);

//        b_play = (Button) findViewById(R.id.b_play);
//        b_play.setOnClickListener(this);
//
//        b_pause = (Button) findViewById(R.id.b_pause);
//        b_pause.setOnClickListener(this);

        lv_related_video = (ListView) findViewById(R.id.ls_relatedVideo);
        lv_related_video.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onItemVideoClicked(adapterView, view, i, l);
            }
        });

        user_duration = (TextView) findViewById(R.id.user_duration);
        Log.i(TAG, "UI finished");

    }

    void onItemVideoClicked(AdapterView<?> adapterView, View view, int i, long l) {
        String video_id = ((TextView)(view.findViewById(R.id.VideoID))).getText().toString();
        String video_name = ((TextView)(view.findViewById(R.id.VideoTitle))).getText().toString();

        Intent intent = new Intent(this, PlayVideoActivity.class);
        intent.putExtra("VIDEOID", video_id);
        intent.putExtra("VIDEONAME", video_name);
        Location temp = getLastLocation(mGoogleApiClient);
        double[] foo = {temp.getLongitude(), temp.getLatitude()};
        intent.putExtra("LOCATION", foo);
        startActivity(intent);
        finish();
    }

    private void setControlsEnabled(boolean enabled) {
//        b_play.setEnabled(enabled);
//        b_pause.setEnabled(enabled);
        user_duration.setEnabled(enabled);
    }

    public void getRelatedVideo() throws ExecutionException, InterruptedException, JSONException {
        String trend_url = "https://www.googleapis.com/youtube/v3/search?part=snippet&relatedToVideoId=" + video_id + "&type=video&maxResults=25&key=AIzaSyAzmrXIdc2sU6zqUUhCBLsxCtoB1EtoicM";
        JSONObject trend_vid = new GetTrendVideo().execute(trend_url).get();
        JSONArray items = (JSONArray) trend_vid.get("items");
        ArrayList<ListItem> myList = new ArrayList<ListItem>();
        int size = items.length();
        for (int i = 0; i < size; i++) {
            ListItem newItem = new ListItem();
//            HashMap<String, Object> myMap = new HashMap<String, Object>();

            JSONObject single_video = (JSONObject) items.get(i);

            JSONObject id = (JSONObject) single_video.get("id");
            String vid_id = id.get("videoId").toString();
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
        lv_related_video.setAdapter(new CustomAdapter(this, mylist));
    }

    protected Location getLastLocation(GoogleApiClient mGoogleApiClient) {
        Log.i(TAG,"Get last location");
        Location mLastLocation;


        Log.d("google play onConnected", "connected");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        return mLastLocation;
    }

    //
    void TicketAddOne() {
        try {
            //log.print();
            log.updateCurrentVideo(formatTime(player.getCurrentTimeMillis()));
            Log.e("LogSingleton", "after update current video and before send");
            log.send(userIdPair);
        } catch (Exception e) {
            Log.e("onStop", e.getMessage());
        }
        String numofticket = sharedPrefs.getString("tickets","");
        if (numofticket == "") {
            currentTicketnum = 0;
        } else {
            currentTicketnum = Integer.parseInt(numofticket);
        }
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("tickets", String.valueOf(currentTicketnum + 1));
        editor.commit();
    }

    String getTickets() {
        String numofticket = sharedPrefs.getString("tickets", "");
        return numofticket;
    }

    protected void updateText() {
        Log.i(TAG, "Update Text");
//        tv_video_status.setText(String.format("Current state: %s %s %s",
//                playerStateChangeListener.playerState, playbackEventListener.playbackState,
//                playbackEventListener.bufferingState));
    }

    private void log(String message) {
        logString.append(message + "\n");
//        tv_log_info.setText(logString);
        Log.i("play video activity", message);
    }

    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;

        return (hours == 0 ? "" : hours + ":")
                + String.format("%02d:%02d", minutes % 60, seconds % 60);
    }

    private String getTimesText() {
        int currentTimeMillis = player.getCurrentTimeMillis();
        int durationMillis = player.getDurationMillis();
        return String.format("(%s/%s)", formatTime(currentTimeMillis), formatTime(durationMillis));
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        Log.i(TAG, "Initialize YouTubePlayer");
        System.out.println("Initialize YouTubePlayer");
        this.player = player;
        Log.i(TAG,"after set player");
        player.setPlaylistEventListener(playlistEventListener);
        Log.i(TAG,"after set Listener");
        player.setPlayerStateChangeListener(playerStateChangeListener);
        player.setPlaybackEventListener(playbackEventListener);

        if (!wasRestored) {
            Log.i(TAG, "play video");
            playVideo();
        }
        setControlsEnabled(true);
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return youTubePlayerView;
    }

//    @Override
//    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//        Log.i(TAG, "Editor Action");
//        if (v == et_skip) {
//            int skipToSecs = parseInt(et_skip.getText().toString(), 0);
//            player.seekToMillis(skipToSecs * 1000);
//            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(et_skip.getWindowToken(), 0);
//            return true;
//        }
//        return false;
//    }

    private void playVideo() {
        Log.i(TAG, "Play Video with Video ID " + video_id);
        //player.cueVideo(video_id);
        // un-comment the next line just for fun
//        video_id = "c3t9hM6jcbY";
        player.loadVideo(video_id);
    }

    @Override
    public void onClick(View view) {
//        if (view == b_play) {
//            player.play();
//        } else if (view == b_pause) {
//            player.pause();
//        }
    }

    @Override
    protected void onStop() {
        findAccurateDuration();

        super.onStop();
    }

    protected void findAccurateDuration(){
        if(logString.toString().contains("SEEKTO")) {
            Log.i("PlayVideoActivity", "Sorry, you didn't get any ticket at this time");
            Log.i("PlayVideoActivity", "Do not jump forward or backward if you wanna get a raffle ticket");
        } else {
            int current = player.getCurrentTimeMillis();
            int duration = player.getDurationMillis();
            float percentage = current * 1.0f/duration * 100;
            Log.i("PlayVideoActivity", "Your watching time is " + formatTime(current) + " Percentage:" + String.valueOf(percentage));
            if (percentage > 50) {
                TicketAddOne();
                Log.i("PlayVideoActivity", "Cong. You got a Raffle ticket");
            } else {
                Log.i("PlayVideoActivity", "Sorry, you didn't make it this time. Try to watch longer next time");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        //super.onSaveInstanceState(state);
        state.putString(KEY_CURRENTLY_SELECTED_ID, video_id);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        //video_id = state.getString(KEY_CURRENTLY_SELECTED_ID);
        video_id="123456789";
    }

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

    public float getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float) level / (float) scale) * 100.0f;
    }

    private final class MyPlaylistEventListener implements PlaylistEventListener {
        @Override
        public void onNext() {
            log("NEXT VIDEO");
        }

        @Override
        public void onPrevious() {
            log("PREVIOUS VIDEO");
        }

        @Override
        public void onPlaylistEnded() {
            log("PLAYLIST ENDED");
        }
    }

    private final class MyPlaybackEventListener implements PlaybackEventListener {
        String playbackState = "NOT_PLAYING";
        String bufferingState = "";
        @Override
        public void onPlaying() {
            playbackState = "PLAYING";
            updateText();
            log("\tPLAYING " + getTimesText());
        }

        @Override
        public void onBuffering(boolean isBuffering) {
            bufferingState = isBuffering ? "(BUFFERING)" : "";
            updateText();
            log("\t\t" + (isBuffering ? "BUFFERING " : "NOT BUFFERING ") + getTimesText());
        }

        @Override
        public void onStopped() {
            playbackState = "STOPPED";
            updateText();
            log("\tSTOPPED");

        }

        @Override
        public void onPaused() {
            playbackState = "PAUSED";
            updateText();
            log("\tPAUSED " + getTimesText());
            try {
                log.updateCurrentVideo(formatTime(player.getCurrentTimeMillis()));
            } catch (Exception e) {
                Log.e("onPaused", "update failed");
            }
        }

        @Override
        public void onSeekTo(int endPositionMillis) {
            log(String.format("\tSEEKTO: (%s/%s)",
                    formatTime(endPositionMillis),
                    formatTime(player.getDurationMillis())));
        }
    }

    private final class MyPlayerStateChangeListener implements PlayerStateChangeListener {
        String playerState = "UNINITIALIZED";
        @Override
        public void onLoading() {
            playerState = "LOADING";
            updateText();
            log(playerState);
        }

        @Override
        public void onLoaded(String videoId) {
            playerState = String.format("LOADED %s", videoId);
            updateText();
            log(playerState);
        }

        @Override
        public void onAdStarted() {
            playerState = "AD_STARTED";
            updateText();
            log(playerState);
        }

        @Override
        public void onVideoStarted() {
            playerState = "VIDEO_STARTED";
            updateText();
            log(playerState);
            double[] loc = location;
            String title =video_name;
            //Log.e("LOCATION", loc.toString());
            String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
//            Log.e("duration", Integer.toString(player.getDurationMillis()));
            String length = formatTime(player.getDurationMillis());
            String duration = formatTime(player.getCurrentTimeMillis());
            //get wifi
            WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            String wifiQuality = Integer.toString(manager.getConnectionInfo().getRssi());
            String dataQuality = Integer.toString(mpsl.signalStrengthValue);
            String battery = Float.toString(getBatteryLevel());
            log.setLog(android_id, loc, title, video_id, length, duration, battery, wifiQuality, dataQuality, getTickets());
        }

        @Override
        public void onVideoEnded() {
            playerState = "VIDEO_ENDED";
            updateText();
            log(playerState);
        }

        @Override
        public void onError(ErrorReason reason) {
            playerState = "ERROR (" + reason + ")";
            if (reason == ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
                // When this error occurs the player is released and can no longer be used.
                player = null;
                setControlsEnabled(false);
            }
            updateText();
            log(playerState);
        }

    }

}

//Check Celluar SignalLevel
class MyPhoneStateListener extends PhoneStateListener {
    /* Get the Signal strength from the provider, each time there is an update*/
    public int signalStrengthValue = 0;
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
    }
}