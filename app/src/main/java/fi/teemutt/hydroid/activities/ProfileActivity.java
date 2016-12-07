package fi.teemutt.hydroid.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import fi.teemutt.hydroid.dialogs.NumberPickerDialog;
import fi.teemutt.hydroid.R;
import fi.teemutt.hydroid.dialogs.SeekBarDialog;

public class ProfileActivity extends AppCompatActivity implements SeekBarDialog.SeekBarDialogListener, NumberPickerDialog.NumberPickerListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ProfileFragment profileFragment = (ProfileFragment) getFragmentManager().findFragmentById(R.id.profileFragment);
        profileFragment.startAnimation();
    }

    @Override
    public void onPositiveClick(int progress) {
        ProfileFragment profileFragment = (ProfileFragment) getFragmentManager().findFragmentById(R.id.profileFragment);
        profileFragment.seekBarPositiveClick(progress);
    }

    @Override
    public void onPositiveClick(int type, int value) {
        ProfileFragment profileFragment = (ProfileFragment) getFragmentManager().findFragmentById(R.id.profileFragment);
        profileFragment.numberPickerPositiveClick(type, value);
    }
}
