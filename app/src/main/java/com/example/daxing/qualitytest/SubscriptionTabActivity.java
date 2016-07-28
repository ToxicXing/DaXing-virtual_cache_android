package com.example.daxing.qualitytest;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class SubscriptionTabActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {
    private static final String TAG = "SignInActivity";
    private static final String CALLBACK_URL = "http://localhost/oauth2callback";
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001; //request code for sign in
    private TextView mStatusTextView;
    private WebView web_view;
    private Button returnBtn;
    private String accessToken;
    private ListView lv_sublist;
    private SubListItem subListItem;
    private boolean onclickFlag;
    private LogSingleton logSingleton;

    //POST request
    AsyncHttpClient client = new AsyncHttpClient();
    AsyncHttpClient respClient = new AsyncHttpClient();
    AsyncHttpClient getListIDClient = new AsyncHttpClient();
    AsyncHttpClient getListClient = new AsyncHttpClient();
    RequestParams params = new RequestParams();
    RequestParams requestParams = new RequestParams();

    // Instantiate the RequestQueue.
    RequestQueue queue;
    String url ="https://accounts.google.com/o/oauth2/auth?client_id=630371916595-669asffe699iek6nq4nq95k43b030q4q.apps.googleusercontent.com&redirect_uri=http%3A%2F%2Flocalhost%2Foauth2callback&scope=https://www.googleapis.com/auth/youtube&response_type=code&access_type=online";
    StringRequest stringRequest;
    private  VideoList videoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_tab);

        Intent access_token_intent = getIntent();
        accessToken= access_token_intent.getStringExtra("AccessToken");

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        logSingleton = LogSingleton.getInstance();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
            .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
}
        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.
//        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
//        signInButton.setSize(SignInButton.SIZE_STANDARD);
//        signInButton.setScopes(gso.getScopeArray());
//        findViewById(R.id.sign_in_button).setOnClickListener(this);
        mStatusTextView = (TextView) findViewById(R.id.status);
        mStatusTextView.setMovementMethod(new ScrollingMovementMethod());
//        web_view = (WebView) findViewById(R.id.web_view);
//        web_view.getSettings().setJavaScriptEnabled(true);
//        web_view.setWebViewClient(new myWebViewClient());
        respClient.get("https://www.googleapis.com/youtube/v3/subscriptions?part=snippet,contentDetails&mine=true&access_token=" + accessToken, new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String res) {
                Gson gson = new GsonBuilder().create();
                // Define Response class to correspond to the JSON response returned
                subListItem = gson.fromJson(res, SubListItem.class);
                //mStatusTextView.append(subListItem.etag + '\n' + subListItem.kind + "\n" + subListItem.pageInfo.totalResults + "\n" + subListItem.items.get(3).id.toString());
                lv_sublist.setAdapter(new CustomAdapterSubscription(SubscriptionTabActivity.this, subListItem.items));
                // called when response HTTP status is "200 OK"
                onclickFlag = true;
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                mStatusTextView.append("\nbad1!");
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }
        }
        );

        returnBtn = (Button) findViewById(R.id.return_button);
        returnBtn.setOnClickListener(this);
        //listview
        lv_sublist = (ListView) findViewById(R.id.ls_sub);
        lv_sublist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
//                Toast.makeText(SubscriptionTabActivity.this, "item " + i + " clicked", Toast.LENGTH_SHORT).show();
                if (onclickFlag == true) {
                    getListIDClient.get("https://www.googleapis.com/youtube/v3/channels?part=contentDetails&id=" + subListItem.items.get(i).snippet.resourceId.getChannelId() + "&key=AIzaSyBIMBuLc9rCul-cL-E76oZbu8-FL5Z0peM", new TextHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, final String res) {
                                    Gson gson = new GsonBuilder().create();
                                    // Define Response class to correspond to the JSON response returned
                                    ChannelList channelList = gson.fromJson(res, ChannelList.class);
                                    getListClient.get("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=" + channelList.items.get(0).contentDetails.relatedPlaylists.getUploads() + "&key=AIzaSyBIMBuLc9rCul-cL-E76oZbu8-FL5Z0peM", new TextHttpResponseHandler() {
                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                            mStatusTextView.append("\nbad3!");
                                        }

                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                            Gson gson = new GsonBuilder().create();
                                            videoList = gson.fromJson(responseString, VideoList.class);
                                            lv_sublist.setAdapter(new CustomAdapterVideoList(SubscriptionTabActivity.this, videoList.items));
                                            onclickFlag = false;
                                            returnBtn.setVisibility(View.VISIBLE);
                                        }
                                    });
                                    //lv_sublist.setAdapter(new CustomAdapterSubscription(SubscriptionTabActivity.this, subListItem.items));
                                    // called when response HTTP status is "200 OK"
                                }
                                @Override
                                public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                                    mStatusTextView.append("\nbad2!");
                                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                                }
                            }
                    );
                }
                else
                {
                    String video_id = videoList.items.get(i).snippet.resourceId.getVideoId();
                    String video_name = ((TextView)(view.findViewById(R.id.VideoTitle))).getText().toString();

                    Intent intent = new Intent(SubscriptionTabActivity.this, PlayVideoActivity.class);
                    intent.putExtra("VIDEOID", video_id);
                    intent.putExtra("VIDEONAME", video_name);
                    Location temp = getLastLocation(mGoogleApiClient);
                    double[] foo = {temp.getLongitude(), temp.getLatitude()};
                    intent.putExtra("LOCATION", foo);
                    startActivity(intent);
                }
            }
        });
    }

   protected Location getLastLocation(GoogleApiClient mGoogleApiClient) {
        Log.i(TAG,"Get last location");
        Location mLastLocation;
        Log.d("google play onConnected", "connected");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        return mLastLocation;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("google play onConnected", "connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

//    class myWebViewClient extends WebViewClient {
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            String urlHolder;
//            String verifExtrctr;
//            urlHolder = url.substring(0, url.indexOf('?'));
//            if(urlHolder.equalsIgnoreCase(CALLBACK_URL))
//            {
//                //view.loadUrl("http://www.google.com");
//                web_view.setVisibility(View.GONE);
//                verifExtrctr = url.substring(url.indexOf('=') + 1);
//                //mStatusTextView.append(verifExtrctr);
//                params.put("code", verifExtrctr);
//                params.put("client_id", "630371916595-669asffe699iek6nq4nq95k43b030q4q.apps.googleusercontent.com");
//                params.put("client_secret", "885RGnNT9lDHdQZVoOFSvMBU");
//                params.put("redirect_uri", "http://localhost/oauth2callback");
//                params.put("grant_type", "authorization_code");
//                client.post("https://accounts.google.com/o/oauth2/token", params, new TextHttpResponseHandler() {
//                            @Override
//                            public void onSuccess(int statusCode, Header[] headers, String res) {
//                                //mStatusTextView.append("\ngood!");
//                                Gson gson = new GsonBuilder().create();
//                                // Define Response class to correspond to the JSON response returned
//                                com.example.daxing.qualitytest.Response resp = gson.fromJson(res, com.example.daxing.qualitytest.Response.class);
//                                accessToken = resp.access_token;
//                                respClient.get("https://www.googleapis.com/youtube/v3/subscriptions?part=snippet,contentDetails&mine=true&access_token=" + accessToken, new TextHttpResponseHandler() {
//                                            @Override
//                                            public void onSuccess(int statusCode, Header[] headers, String res) {
//                                                Gson gson = new GsonBuilder().create();
//                                                // Define Response class to correspond to the JSON response returned
//                                                subListItem = gson.fromJson(res, SubListItem.class);
//                                                //mStatusTextView.append(subListItem.etag + '\n' + subListItem.kind + "\n" + subListItem.pageInfo.totalResults + "\n" + subListItem.items.get(3).id.toString());
//                                                lv_sublist.setAdapter(new CustomAdapterSubscription(SubscriptionTabActivity.this, subListItem.items));
//                                                // called when response HTTP status is "200 OK"
//                                                onclickFlag = true;
//                                            }
//                                            @Override
//                                            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
//                                                mStatusTextView.append("\nbad1!");
//                                                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
//                                            }
//                                        }
//                                );
//                                // called when response HTTP status is "200 OK"
//                            }
//                            @Override
//                            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
//                                mStatusTextView.append("\nbad!");
//                                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
//                            }
//                        }
//                );
//
//                /*if(verifExtrctr.equalsIgnoreCase("oauth_verifier"))
//                {
//                    params[5] = verifExtrctr[1];
//                    return true;
//                }
//                else
//                {
//                    System.out.println("Inocorrect callback URL format.");
//                }*/
//                //view.loadUrl(verifExtrctr);
//            }
//            else
//            {
//                view.loadUrl(url);
//            }
//            return false;
//        }
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.sign_in_button:
//                signIn();
//                findViewById(R.id.sign_in_button).setVisibility(View.GONE);
//                break;

            case R.id.return_button:
                returnBtn.setVisibility(View.GONE);
                lv_sublist.setAdapter(new CustomAdapterSubscription(SubscriptionTabActivity.this, subListItem.items));
                onclickFlag = true;
                break;
            // ...
        }
    }

//    private void signIn() {
//        web_view.setVisibility(View.VISIBLE);
//        queue = Volley.newRequestQueue(this);
//        // Request a string response from the provided URL.
//        stringRequest = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        // Display the first 500 characters of the response string.
//                        //mStatusTextView.setText("Response is: "+ response.substring(0,500));
//                        web_view.loadData(response, "text/html", null);
//                        //web_view.loadUrl(url);
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                mStatusTextView.setText("That didn't work!");
//            }
//        });
//        // Add the request to the RequestQueue.
//        queue.add(stringRequest);
//    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connecting to Google API Service failed");
    }

    @Override
    protected void onStop() {
        logSingleton.print();
        super.onStop();
    }
}
