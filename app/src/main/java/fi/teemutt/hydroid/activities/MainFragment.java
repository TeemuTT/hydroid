package fi.teemutt.hydroid.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.threeten.bp.LocalTime;
import org.threeten.bp.ZonedDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fi.teemutt.hydroid.R;
import fi.teemutt.hydroid.database.MyDataBaseHelper;
import fi.teemutt.hydroid.models.DrinkEvent;

import static android.content.Context.MODE_PRIVATE;

public class MainFragment extends Fragment {

    private final static String TAG = MainFragment.class.getSimpleName();

    private MyDataBaseHelper db;
    private ArrayList<DrinkEvent> events;
    private PieChart pieChart;
    private TextView tvGoal;
    private TextView tvIntake;

    public MainFragment() {
    }

    private AnimatorSet setUpAnimation() {
        Animator anim1 = ObjectAnimator.ofFloat(pieChart, View.ROTATION, 0f, 45f).setDuration(150);
        Animator anim2 = ObjectAnimator.ofFloat(pieChart, View.ROTATION, 45f, -45f).setDuration(200);
        Animator anim3 = ObjectAnimator.ofFloat(pieChart, View.ROTATION, -45f, 0f).setDuration(150);
        anim3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                pieChart.setClickable(true);
                Intent intent = new Intent(getContext(), DrinkActivity.class);
                startActivity(intent);
            }
        });
        AnimatorSet animation = new AnimatorSet();
        animation.playSequentially(anim1, anim2, anim3);
        return animation;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");

        db = MyDataBaseHelper.getInstance(getContext());
        events = new ArrayList<>();

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);

        tvGoal = (TextView) rootView.findViewById(R.id.tvGoal);
        tvIntake = (TextView) rootView.findViewById(R.id.tvIntake);

        pieChart = (PieChart) rootView.findViewById(R.id.pieChart);
        pieChart.setCenterText("Tap to drink!");
        pieChart.setCenterTextSize(16);
        pieChart.setTouchEnabled(false);
        pieChart.setDescription(null);
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setHoleRadius(70f);
        pieChart.setTransparentCircleRadius(75f);

//        final AnimatorSet anim = setUpAnimation();
        pieChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent intent = new Intent(getContext(), DrinkActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        update(true);
    }

    public void update(boolean animate) {
        Log.i(TAG, "update");

        events = db.getEventsForDay(ZonedDateTime.now().with(LocalTime.of(0, 0)));
        SharedPreferences prefs = getActivity().getSharedPreferences("fi.teemutt.hydroid", MODE_PRIVATE);
        int goal = prefs.getInt("DAILY_GOAL", 2000);
        int totalIntake = calculateTotalIntake(events);
        float intakePercent = (float) totalIntake / (float) goal * 100;
        if (intakePercent > 100)
            intakePercent = 100;

        tvGoal.setText(String.format(Locale.US, "%d ml", goal));
        tvIntake.setText(String.format(Locale.US, "%d ml", totalIntake));

        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(intakePercent, "Intake"));
        pieEntries.add(new PieEntry(100 - intakePercent, "Remaining"));

        PieDataSet set = new PieDataSet(pieEntries, null);
        set.setColors(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null), ResourcesCompat.getColor(getResources(), R.color.colorGrey, null));
        set.setDrawValues(false);

        PieData data = new PieData(set);
        pieChart.setData(data);

        if (animate)
            pieChart.animateY(1500);
        else
            pieChart.invalidate();
    }

    private int calculateTotalIntake(ArrayList<DrinkEvent> events) {
        int total = 0;
        for (DrinkEvent e : events)
            total += e.getSize();
        return total;
    }
}
