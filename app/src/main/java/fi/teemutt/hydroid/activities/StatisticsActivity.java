package fi.teemutt.hydroid.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.android.gms.ads.AdView;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalField;
import org.threeten.bp.temporal.WeekFields;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fi.teemutt.hydroid.R;
import fi.teemutt.hydroid.database.MyDataBaseHelper;
import fi.teemutt.hydroid.models.DrinkEvent;

public class StatisticsActivity extends AppCompatActivity {

    private final static String TAG = StatisticsActivity.class.getSimpleName();

    // Number of weeks to show.
    private static final int NUM_PAGES = 3;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPager mPager = (ViewPager) findViewById(R.id.viewPager);
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(2);

//        mAdView = (AdView) findViewById(R.id.adView);
//        AdRequest request = new AdRequest.Builder()
//                .addTestDevice("67178A40781CF035F22E9BE63C680F6E")
//                .build();
//        mAdView.loadAd(request);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            Log.i(TAG, "adView paused");
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            Log.i(TAG, "adView destroyed");
            mAdView.destroy();
        }
        super.onDestroy();
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            WeekFragment frag = new WeekFragment();
            Bundle args = new Bundle();
            args.putInt("position", position);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    public static class WeekFragment extends Fragment {

        private MyDataBaseHelper db;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            db = MyDataBaseHelper.getInstance(getContext());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.content_statistics, container, false);

            // Position in the PagerAdapter. 1 = this week, 0 = previous week.
            int position = getArguments().getInt("position");

            TemporalField weekOfYear = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
            LocalDate date = LocalDate.now().minusWeeks(Math.abs(position - (NUM_PAGES - 1)));
            int weekNumber = date.get(weekOfYear);

//            String weekString = (position == 1) ? "This week" : "Previous week";
            String weekString;
            switch (position) {
                case NUM_PAGES - 1:
                    weekString = "This week";
                    break;
                case NUM_PAGES - 2:
                    weekString = "Previous week";
                    break;
                default:
                    weekString = "Week number";
            }
            ((TextView) rootView.findViewById(R.id.header)).setText(String.format(Locale.US, "%s (%d)", weekString, weekNumber));

            // Get the start of the week, i.e. Sunday/Monday 00:00:00
            TemporalField fieldLocal = WeekFields.of(Locale.getDefault()).dayOfWeek();
            ZonedDateTime weekStart = ZonedDateTime.now().minusWeeks(Math.abs(position - (NUM_PAGES - 1))).with(fieldLocal, 1);
            weekStart = weekStart.with(LocalTime.of(0, 0));

            // Set up BarChart. List<BarEntry> -> BarDataSet -> BarData -> barChart.setData(BarData).
            BarChart barChart = (BarChart) rootView.findViewById(R.id.barChart);
            barChart.setNoDataText("No data");
            barChart.setNoDataTextColor(Color.BLACK);
            barChart.setDescription(null);
            barChart.getLegend().setEnabled(false);
            barChart.setTouchEnabled(false);
            barChart.getAxisRight().setEnabled(false);
            barChart.setFitBars(true);

            // Get intakes for every day.
            List<BarEntry> barEntries = new ArrayList<>();
            int numberOfDaysWithData = 0; // Days with any data. Used for daily average.
            int weekTotal = 0;
            for (int i = 0; i < 7; i++) {
                int amount = calculateTotalIntake(db.getEventsForDay(weekStart.plusDays(i)));
                if (amount > 0) {
                    numberOfDaysWithData++;
                    weekTotal += amount;
                }
                barEntries.add(new BarEntry((float) i, (float) amount));
            }

            BarDataSet set = new BarDataSet(barEntries, "Water intake");
            set.setColors(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
            set.setValueTextSize(14f);
            BarData data = new BarData(set);

            // Format the X-axis values to days.
            IAxisValueFormatter formatter = new IAxisValueFormatter() {
                private final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return days[(int) value];
                }

                @Override
                public int getDecimalDigits() {
                    return 0;
                }
            };

            XAxis xAxis = barChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1f);
            xAxis.setValueFormatter(formatter);

            // Only set data if there is data for any day.
            // This way we the chart can show noDataText.
            if (weekTotal != 0) {
                barChart.setData(data);
            }

            barChart.animateY(1500, Easing.EasingOption.EaseOutSine);

            float dailyAvg = (numberOfDaysWithData > 0) ? (float) weekTotal / numberOfDaysWithData : 0;
            ((TextView) rootView.findViewById(R.id.tvWeekTotal)).setText(String.format(Locale.US, "%d ml", weekTotal));
            ((TextView) rootView.findViewById(R.id.tvDayAverage)).setText(String.format(Locale.US, "%.1f ml", dailyAvg));

            return rootView;
        }

        public int calculateTotalIntake(ArrayList<DrinkEvent> events) {
            int total = 0;
            for (DrinkEvent e : events) {
                total += e.getSize();
            }
            return total;
        }
    }

}
