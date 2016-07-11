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
        Intent intent;
        TabHost.TabSpec spec;
        Resources res = getResources();
        TabHost tabHost = getTabHost();

        intent = new Intent().setClass(this, TrendingTabActivity.class);
        spec = tabHost.newTabSpec("use").setIndicator("Trends").setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, SearchingTabActivity.class);
        spec = tabHost.newTabSpec("use").setIndicator("Search").setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, DeveloperTabActivity.class);
        spec = tabHost.newTabSpec("use").setIndicator("Developer").setContent(intent);
        tabHost.addTab(spec);

        Log.i("TabWidget", "Tab working");
    }

}
