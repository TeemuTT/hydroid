package fi.teemutt.hydroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import fi.teemutt.hydroid.R;
import fi.teemutt.hydroid.database.MyDataBaseHelper;
import fi.teemutt.hydroid.dialogs.IconPickerDialog;
import fi.teemutt.hydroid.models.Portion;

public class PortionCreatorActivity extends AppCompatActivity implements IconPickerDialog.IconPickerListener {

    private ImageView imageView;
    private long _id = -1;
    private int size = 120;
    private int drawableId = R.drawable.ic_coffee;
    private MyDataBaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portion_creator);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setResult(RESULT_CANCELED);

        db = MyDataBaseHelper.getInstance(this);

        // Check if we are editing or creating a new one.
        _id = getIntent().getLongExtra("id", -1);
        if (_id != -1) {
            // Editing
            Portion portion = db.getPortion(_id);
            drawableId = portion.getDrawableId();
            size = portion.getSize();
            ((TextView) findViewById(R.id.tvHeader)).setText("Edit portion");
            ((Button) findViewById(R.id.btnCreate)).setText("SAVE");
        }

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageResource(drawableId);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IconPickerDialog iconPicker = IconPickerDialog.newInstance();
                iconPicker.show(getFragmentManager(), "IconPicker");
            }
        });

        final TextView tvSize = (TextView) findViewById(R.id.tvSize);
        tvSize.setText(String.format("%d ml", size));

        SeekBar sbSize = (SeekBar) findViewById(R.id.sbSize);
        sbSize.setProgress(size / 10);
        sbSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                size = seekBar.getProgress() * 10;
                tvSize.setText(String.format("%d ml", size));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void onCreateClick(View v) {
        if (size == 0) {
            Snackbar.make(v, "Size can't be zero!", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (_id != -1) {
            // Editing
            db.savePortion(_id, size, drawableId);
        } else {
            // New
            _id = db.createPortion(size, drawableId);
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("_id", _id);
        resultIntent.putExtra("size", size);
        resultIntent.putExtra("drawable", drawableId);
        resultIntent.putExtra("adapter_position", getIntent().getIntExtra("adapter_position", 0));
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void iconClicked(int drawable) {
        drawableId = drawable;
        imageView.setImageDrawable(AppCompatResources.getDrawable(this, drawable));
    }
}
