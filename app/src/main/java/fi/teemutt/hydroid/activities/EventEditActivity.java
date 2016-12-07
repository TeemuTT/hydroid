package fi.teemutt.hydroid.activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import org.threeten.bp.LocalTime;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Locale;

import fi.teemutt.hydroid.R;
import fi.teemutt.hydroid.dialogs.TimePickerFragment;
import fi.teemutt.hydroid.database.MyDataBaseHelper;
import fi.teemutt.hydroid.models.DrinkEvent;

public class EventEditActivity extends AppCompatActivity implements TimePickerFragment.TimePickerFragmentListener {

    private MyDataBaseHelper db;

    private SeekBar sbSize;
    private long _id;
    private ZonedDateTime date;
    private EditText etTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        _id = intent.getLongExtra("event_id", -1);

        db = MyDataBaseHelper.getInstance(this);

        DrinkEvent event = db.getEvent(_id);
        date = event.getDate();

        etTime = (EditText) findViewById(R.id.etTime);
        etTime.setText(event.getDate().format(DateTimeFormatter.ofPattern("HH:mm")));
        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment frag = new TimePickerFragment();
                frag.show(getFragmentManager(), "timePicker");
            }
        });

        final TextView tvSize = (TextView) findViewById(R.id.tvAmount);
        tvSize.setText(String.format(Locale.getDefault(), "%d ml", event.getSize()));

        sbSize = (SeekBar) findViewById(R.id.sbAmount);
        sbSize.setProgress(event.getSize() / 10);
        sbSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvSize.setText(String.format(Locale.getDefault(), "%d ml", seekBar.getProgress() * 10));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void onSaveClick(View v) {
        // Save changes
        int size = sbSize.getProgress() * 10;
        if (size == 0) {
            Snackbar.make(v, "Size can't be zero!", Snackbar.LENGTH_SHORT).show();
        } else {
            db.saveEvent(_id, size, date.toInstant().toString());
            finish();
        }
    }

    public void timeSet(int hour, int minute) {
        ZonedDateTime d = ZonedDateTime.now();
        d = d.with(LocalTime.of(hour, minute));
        date = d;
        etTime.setText(String.format("%02d:%02d", hour, minute));
    }
}
