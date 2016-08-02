package com.example.daxing.qualitytest;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.PlaylistEventListener;
import com.google.android.youtube.player.YouTubePlayerView;



public class PlayVideoActivity extends YouTubeFailureRecoveryActivity implements  YouTubePlayer.OnInitializedListener, View.OnClickListener {
    private static final String TAG = PlayVideoActivity.class.getSimpleName();

    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private StringBuilder logString;
    private static final String KEY_CURRENTLY_SELECTED_ID = "currentlySelectedId";

    String video_id;
    double[] location;

    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer player;

    private TextView tv_video_status;
    private TextView tv_log_info;

    private Button b_play;
    private Button b_pause;

    private TextView user_duration;

    private MyPlaylistEventListener playlistEventListener;
    private MyPlayerStateChangeListener playerStateChangeListener;
    private MyPlaybackEventListener playbackEventListener;

    private LogSingleton log;
    private String video_name;
    private String duration;
    private String length;
    private MyPhoneStateListener mpsl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_play_video);
        Intent intent = getIntent();
        video_id = intent.getStringExtra("VIDEOID");
        video_name = intent.getStringExtra("VIDEONAME");
        location = intent.getDoubleArrayExtra("LOCATION");
//        Log.e("INTENT", loc.toString());
        Log.i(TAG,"Video ID is " + video_id);
        log = LogSingleton.getInstance();
        mpsl = new MyPhoneStateListener();
        setUI();

        playlistEventListener = new MyPlaylistEventListener();
        playerStateChangeListener = new MyPlayerStateChangeListener();
        playbackEventListener = new MyPlaybackEventListener();
        logString = new StringBuilder();

    }

    protected void setUI() {
        Log.i(TAG, "Set UI");
        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        youTubePlayerView.initialize(DeveloperKey.DEVELOPER_KEY, this);

        tv_video_status = (TextView) findViewById(R.id.video_status);

        tv_log_info = (TextView) findViewById(R.id.tv_log_info);

        b_play = (Button) findViewById(R.id.b_play);
        b_play.setOnClickListener(this);

        b_pause = (Button) findViewById(R.id.b_pause);
        b_pause.setOnClickListener(this);

        user_duration = (TextView) findViewById(R.id.user_duration);
        Log.i(TAG, "UI finished");

    }

    private void setControlsEnabled(boolean enabled) {
        b_play.setEnabled(enabled);
        b_pause.setEnabled(enabled);
        user_duration.setEnabled(enabled);
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



    private static final int parseInt(String intString, int defaultValue) {
        try {
            return intString != null ? Integer.valueOf(intString) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
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
        player.loadVideo(video_id);
    }

    @Override
    public void onClick(View view) {
        if (view == b_play) {
            player.play();
        } else if (view == b_pause) {
            player.pause();
        }
    }

    @Override
    protected void onStop() {
        try {
            log.print();
            log.updateCurrentVideo(formatTime(player.getCurrentTimeMillis()));
            log.send();
            findAccurateDuration();
        } catch (Exception e) {
            Log.e("onStopped", "send failed");
        }
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
            log.setLog(android_id,loc,title,video_id,length,duration,battery,wifiQuality,dataQuality);
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
    public float getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f;
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