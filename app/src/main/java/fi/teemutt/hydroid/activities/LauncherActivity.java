package fi.teemutt.hydroid.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import fi.teemutt.hydroid.dialogs.NumberPickerDialog;
import fi.teemutt.hydroid.R;
import fi.teemutt.hydroid.dialogs.SeekBarDialog;

public class LauncherActivity extends AppCompatActivity implements NumberPickerDialog.NumberPickerListener, SeekBarDialog.SeekBarDialogListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Check if the user has completed this setup. This activity should only be shown on the first start unless the user quits before completion.
        SharedPreferences prefs = getSharedPreferences("fi.teemutt.hydroid", MODE_PRIVATE);
        boolean setupCompleted = prefs.getBoolean("setup_completed", false);
        if (setupCompleted) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_launcher);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        TextView title = (TextView) findViewById(R.id.tvTitle);
        final TextView title2 = (TextView) findViewById(R.id.tvTitle2);
        final FrameLayout frame = (FrameLayout) findViewById(R.id.frameLayout);

        Animator anim1 = ObjectAnimator.ofFloat(title, View.ALPHA, 0f, 1f).setDuration(2000);
        Animator anim2 = ObjectAnimator.ofFloat(title2, View.ALPHA, 0f, 1f).setDuration(1500);

        Animator anim3 = ObjectAnimator.ofFloat(frame, View.ALPHA, 0f, 1f).setDuration(1500);
        Animator anim4 = ObjectAnimator.ofFloat(frame, View.TRANSLATION_Y, 350f, 180f).setDuration(1500);

        anim2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animationFinished();
            }
        });

        AnimatorSet s = new AnimatorSet();
        s.play(anim1).before(anim2);
        s.play(anim3).with(anim4);
        s.play(anim3).after(anim2);
        s.start();

    }

    private void animationFinished() {
        ProfileFragment profileFragment = (ProfileFragment) getFragmentManager().findFragmentById(R.id.profileFragment);
        profileFragment.startAnimation();
    }

    @Override
    public void onPositiveClick(int type, int value) {
        ProfileFragment profileFragment = (ProfileFragment) getFragmentManager().findFragmentById(R.id.profileFragment);
        profileFragment.numberPickerPositiveClick(type, value);
    }

    @Override
    public void onPositiveClick(int progress) {
        ProfileFragment profileFragment = (ProfileFragment) getFragmentManager().findFragmentById(R.id.profileFragment);
        profileFragment.seekBarPositiveClick(progress);
    }

    @SuppressWarnings("UnusedParameters")
    public void doneClicked(View view) {
        // Save and never come back.
        SharedPreferences.Editor editor = getSharedPreferences("fi.teemutt.hydroid", MODE_PRIVATE).edit();
        editor.putBoolean("setup_completed", true);
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressWarnings("UnusedParameters")
    public void quitClicked(View view) {
        finish();
    }
}
