package fi.teemutt.hydroid.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.ads.AdView;
import com.jakewharton.threetenabp.AndroidThreeTen;

import fi.teemutt.hydroid.R;
import fi.teemutt.hydroid.database.MyDataBaseHelper;
import fi.teemutt.hydroid.utilities.AlarmReceiver;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DetailsFragment.DetailsFragmentListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    // Number of pages in the PagerAdapter.
    // 1. page: MainFragment
    // 2. page: DetailsFragment
    private final static int NUM_PAGES = 2;

    // Activity request codes.
    private final static int REQUEST_SETTINGS = 2;
    private final static int REQUEST_PROFILE = 3;

    private ViewPager mPager;
    private AdView mAdView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        AndroidThreeTen.init(this);

        mPager = (ViewPager) findViewById(R.id.viewPager);
        mPager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager()));

//        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_unit_id));
//        mAdView = (AdView) findViewById(R.id.adView);
//        AdRequest request = new AdRequest.Builder()
//                .addTestDevice("67178A40781CF035F22E9BE63C680F6E")
//                .build();
//        mAdView.loadAd(request);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 1:
                    return new DetailsFragment();
                default:
                    return new MainFragment();
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null)
            mAdView.resume();
    }

    @Override
    public void onPause() {
        if (mAdView != null)
            mAdView.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mAdView != null)
            mAdView.destroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_statistics) {
            Intent intent = new Intent(this, StatisticsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, REQUEST_SETTINGS);
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivityForResult(intent, REQUEST_PROFILE);
        }

        // Developer options.
        else if (id == R.id.nav_launcher_activity) {
            Intent intent = new Intent(this, LauncherActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_reset_setup) {
            SharedPreferences.Editor editor = getSharedPreferences("fi.teemutt.hydroid", MODE_PRIVATE).edit();
            editor.putBoolean("setup_completed", false);
            editor.apply();
        } else if (id == R.id.nav_reset_database) {
            MyDataBaseHelper db = MyDataBaseHelper.getInstance(this);
            db.clearData();
        } else if (id == R.id.nav_create_data) {
            MyDataBaseHelper db = MyDataBaseHelper.getInstance(this);
            db.createTestData();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SETTINGS && resultCode == RESULT_OK) {
            // Settings have changed.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            // Cancel reminders and reapply if needed. Could maybe implement
            // logic to not cancel everytime.
            boolean reminders = prefs.getBoolean("pref_reminders", false);
            AlarmReceiver.cancelReminders(this);
            if (reminders)
                AlarmReceiver.setUpReminders(this);
        }
    }

    // DetailsFragment deleted a row, must update MainFragment.
    @Override
    public void onRowDeleted() {
        MainFragment mainFragment = (MainFragment) mPager.getAdapter().instantiateItem(mPager, 0);
        if (mainFragment != null)
            mainFragment.update(false);
    }

}
