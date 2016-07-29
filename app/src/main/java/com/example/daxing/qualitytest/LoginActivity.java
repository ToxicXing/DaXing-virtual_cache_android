package com.example.daxing.qualitytest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "LoginActivity";
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
    SharedPreferences sharedPrefs;
    //POST request
    AsyncHttpClient client = new AsyncHttpClient();
    AsyncHttpClient respClient = new AsyncHttpClient();
    AsyncHttpClient getListIDClient = new AsyncHttpClient();
    AsyncHttpClient getListClient = new AsyncHttpClient();
    RequestParams params = new RequestParams();
    RequestParams requestParams = new RequestParams();

    // Instantiate the RequestQueue.
    RequestQueue queue;
    String url = "https://accounts.google.com/o/oauth2/auth?client_id=630371916595-669asffe699iek6nq4nq95k43b030q4q.apps.googleusercontent.com&redirect_uri=http%3A%2F%2Flocalhost%2Foauth2callback&scope=https://www.googleapis.com/auth/youtube&response_type=code&access_type=online";
    StringRequest stringRequest;
    private VideoList videoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
        findViewById(R.id.sign_in_button).setOnClickListener(this);
//        mStatusTextView = (TextView) findViewById(R.id.status);
//        mStatusTextView.setMovementMethod(new ScrollingMovementMethod());
        sharedPrefs = getSharedPreferences("qualityTest", MODE_PRIVATE);
        if(sharedPrefs.contains("AccessToken")) {
            accessToken = sharedPrefs.getString("AccessToken", "");
            Intent intent = new Intent(LoginActivity.this, TabWidget.class);
            intent.putExtra("AccessToken", accessToken);
            startActivity(intent);
            finish();
        } else {
            web_view = (WebView) findViewById(R.id.web_view);
            web_view.getSettings().setJavaScriptEnabled(true);
            web_view.setWebViewClient(new myWebViewClient());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                break;
        }
    }

    private void signIn() {
        web_view.setVisibility(View.VISIBLE);
        queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        stringRequest = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        //mStatusTextView.setText("Response is: "+ response.substring(0,500));
                        web_view.loadData(response, "text/html", null);
                        //web_view.loadUrl(url);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mStatusTextView.setText("That didn't work!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connecting to Google API Service failed");
    }

    @Override
    protected void onStop() {
        logSingleton.print();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    class myWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String urlHolder;
            String verifExtrctr;
            if(url.contains("usc")) {
                view.loadUrl(url);
                return true;
            }
            urlHolder = url.substring(0, url.indexOf('?'));
            Log.e(TAG, "Returned URL is " + url);
            if(urlHolder.equalsIgnoreCase(CALLBACK_URL))
            {
                //view.loadUrl("http://www.google.com");
                web_view.setVisibility(View.GONE);
                verifExtrctr = url.substring(url.indexOf('=') + 1);
                //mStatusTextView.append(verifExtrctr);
                params.put("code", verifExtrctr);
                params.put("client_id", "630371916595-669asffe699iek6nq4nq95k43b030q4q.apps.googleusercontent.com");
                params.put("client_secret", "885RGnNT9lDHdQZVoOFSvMBU");
                params.put("redirect_uri", "http://localhost/oauth2callback");
                params.put("grant_type", "authorization_code");
                client.post("https://accounts.google.com/o/oauth2/token", params, new TextHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String res) {
                                //mStatusTextView.append("\ngood!");
                                Gson gson = new GsonBuilder().create();
                                // Define Response class to correspond to the JSON response returned
                                com.example.daxing.qualitytest.Response resp = gson.fromJson(res, com.example.daxing.qualitytest.Response.class);
                                accessToken = resp.access_token;
                                Log.i(TAG, "AccessToken is " + accessToken);


                                // The SharedPreferences editor - must use commit() to submit changes
                                SharedPreferences.Editor editor = sharedPrefs.edit();
                                // Edit the saved preferences
                                editor.putString("AccessToken", accessToken);
                                editor.commit();

                                Intent intent = new Intent(LoginActivity.this, TabWidget.class);
                                intent.putExtra("AccessToken", accessToken);
                                startActivity(intent);
                                finish();
                            }
                            @Override
                            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                                mStatusTextView.append("\nbad!");
                                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                            }
                        }
                );
            }
            else
            {
                view.loadUrl(url);
            }
            return false;
        }
    }
}


