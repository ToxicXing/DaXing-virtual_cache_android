package com.example.daxing.qualitytest;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;

public class TabWidget extends TabActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_widget);
        String accessToken = "";

        String userID = "";
        Intent intent;
        Intent access_token_intent = getIntent();
        accessToken= access_token_intent.getStringExtra("AccessToken");
        userID = access_token_intent.getStringExtra("account");
        Log.i("TabWidget", "userID: " + userID);
        Log.i("TabWidget", "accessToken: " + accessToken);

        TabHost.TabSpec spec;
        Resources res = getResources();
        TabHost tabHost = getTabHost();

        intent = new Intent().setClass(this, TrendingTabActivity.class);
        intent.putExtra("account", userID);
        spec = tabHost.newTabSpec("use").setIndicator("Trends").setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, SearchingTabActivity.class);
        intent.putExtra("account", userID);
        spec = tabHost.newTabSpec("use1").setIndicator("Search").setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, SubscriptionTabActivity.class);
        intent.putExtra("AccessToken", accessToken);
        intent.putExtra("account", userID);
        spec = tabHost.newTabSpec("use2").setIndicator("Subscription").setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, DeveloperTabActivity.class);
        spec = tabHost.newTabSpec("use3").setIndicator("Settings").setContent(intent);
        tabHost.addTab(spec);

        Log.i("TabWidget", "Tab working");
    }

}
