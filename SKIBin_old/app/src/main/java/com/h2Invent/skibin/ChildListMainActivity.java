package com.h2Invent.skibin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.material.tabs.TabLayout;

public class ChildListMainActivity extends AppCompatActivity implements FragmentChildListAfterList.OnItemSelectedListener, FragmentChildListCheckin.OnItemSelectedListener {
    private static final String LOG = ChildListMainActivity.class.getSimpleName();

    private TabLayout childList;
    private ViewPager mViewPager;
    private TabChildListAdapter mChildListAdapter;
    private LinearLayout childListContainer;
    private String checkinUrl;
    private String kinderlisteUrl;
    public static String userToken;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int PHONE_PERMISSION_CODE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_list_main);
        Fresco.initialize(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        childList = (TabLayout) findViewById(R.id.tabChildList);
        childListContainer = (LinearLayout) findViewById(R.id.childListContainer);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initApp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_get_data:
                initApp();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(ChildListMainActivity.this, TabSettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                Log.d(LOG, "Debug       - Press something.");
                return super.onOptionsItemSelected(item);
        }
    }

    public  void initApp() {

        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);

            childListContainer.setVisibility(View.VISIBLE);
            kinderlisteUrl = sharedPreferences.getString(SaveSettings.USER_KINDERLISTE_URL, "");
            checkinUrl = sharedPreferences.getString(SaveSettings.USER_CHECKIN_URL, "");
            userToken = sharedPreferences.getString(SaveSettings.USER_TOKEN,"");
            setViewPager();
    }
    private void setViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mChildListAdapter = new TabChildListAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mChildListAdapter);
        final FragmentChildListCheckin fragmentChildListCheckin = (FragmentChildListCheckin) mChildListAdapter.getItem(0);
        fragmentChildListCheckin.setUrl(checkinUrl);
        fragmentChildListCheckin.setUserToken(userToken);
        final FragmentChildListAfterList fragmentChildListAfterList= (FragmentChildListAfterList) mChildListAdapter.getItem(1);
        fragmentChildListAfterList.setUserToken(userToken);
        fragmentChildListAfterList.setUrl(kinderlisteUrl);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabChildList);
        tabLayout.setupWithViewPager(mViewPager);
    }
}