package fi.teemutt.hydroid.activities;

import android.animation.ValueAnimator;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Locale;

import fi.teemutt.hydroid.R;
import fi.teemutt.hydroid.dialogs.NumberPickerDialog;
import fi.teemutt.hydroid.dialogs.SeekBarDialog;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Teemu on 18.11.2016.
 *
 */

public class ProfileFragment extends Fragment {

    private final static String TAG = ProfileFragment.class.getSimpleName();

    private static final int DIALOG_WEIGHT = 1;
    private static final int DIALOG_AGE = 2;

    private int goal;
    private int weight;
    private int age;
    private boolean override;
    private EditText weightSpinner;
    private EditText ageSpinner;
    private TextView tvGoal;

    public ProfileFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getActivity().getSharedPreferences("fi.teemutt.hydroid", MODE_PRIVATE);
        goal = prefs.getInt("DAILY_GOAL", 2000);
        weight = prefs.getInt("WEIGHT", 60);
        age = prefs.getInt("AGE", 15);
        override = prefs.getBoolean("OVERRIDE_DAILY_GOAL", false);
        calculateDailyGoal();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.content_profile, container, false);

        weightSpinner = (EditText) view.findViewById(R.id.etWeight);
        weightSpinner.setText(String.format(Locale.getDefault(), "%d kg", weight));
        weightSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment fragment = NumberPickerDialog.newInstance(DIALOG_WEIGHT, weight);
                fragment.show(getActivity().getFragmentManager(), "WeightPicker");

            }
        });
        ageSpinner = (EditText) view.findViewById(R.id.etAge);
        ageSpinner.setText(String.valueOf(age));
        ageSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment fragment = NumberPickerDialog.newInstance(DIALOG_AGE, age);
                fragment.show(getActivity().getFragmentManager(), "AgePicker");
            }
        });

        ImageButton editButton = (ImageButton) view.findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment fragment = SeekBarDialog.newInstance((goal - 1000) / 10);
                fragment.show(getActivity().getFragmentManager(), "GoalPicker");
            }
        });

        tvGoal = (TextView) view.findViewById(R.id.tvGoal);

        return view;
    }

    public void startAnimation() {
        animateTextView(tvGoal, 0, goal, 2000);
    }

    public void seekBarPositiveClick(int progress) {
        int oldGoal = goal;
        goal = progress * 10 + 1000;
        override = true;
        animateTextView(tvGoal, oldGoal, goal, 1000);
    }

    public void numberPickerPositiveClick(int type, int value) {
        switch (type) {
            case DIALOG_WEIGHT:
                weight = value;
                weightSpinner.setText(String.format(Locale.US, "%d kg", weight));
                break;
            case DIALOG_AGE:
                age = value;
                ageSpinner.setText(String.valueOf(age));
                break;
        }
        override = false;
        int oldGoal = calculateDailyGoal();
        animateTextView(tvGoal, oldGoal, goal, 1000);
    }

    private int calculateDailyGoal() {
        if (override) {
            // User has set goal manually, do not calculate.
            return goal;
        }

        int oldGoal = goal;
        if (age < 31) {
            goal = weight * 40;
        } else if (age < 55) {
            goal = weight * 35;
        } else if (age < 66) {
            goal = weight * 30;
        } else {
            goal = weight * 25;
        }
        goal -= 1000;
        return oldGoal;
    }

    private void animateTextView(final TextView textView, int start, int end, int duration) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = Float.parseFloat(valueAnimator.getAnimatedValue().toString());
                if (value >= 1000) {
                    value /= 1000;
                    textView.setText(String.format(Locale.US, "%.2f l", value));
                } else {
                    textView.setText(String.format(Locale.US, "%.0f ml", value));
                }

            }
        });
        animator.start();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        // Save preference when exiting Activity.
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("fi.teemutt.hydroid", MODE_PRIVATE).edit();
        editor.putInt("DAILY_GOAL", goal);
        editor.putInt("WEIGHT", weight);
        editor.putInt("AGE", age);
        editor.putBoolean("OVERRIDE_DAILY_GOAL", override);
        editor.apply();
        super.onPause();
    }
}
