package fi.teemutt.hydroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.threeten.bp.ZonedDateTime;

import java.util.ArrayList;
import java.util.Locale;

import fi.teemutt.hydroid.R;
import fi.teemutt.hydroid.database.MyDataBaseHelper;
import fi.teemutt.hydroid.models.Portion;
import fi.teemutt.hydroid.utilities.PortionAdapter;

public class DrinkActivity extends AppCompatActivity {

    private final static int REQUEST_NEW_PORTION = 1;
    private final static int REQUEST_EDIT_PORTION = 2;

    private MyDataBaseHelper db;
    private ArrayList<Portion> portions;
    private PortionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DrinkActivity.this, PortionCreatorActivity.class);
                startActivityForResult(intent, REQUEST_NEW_PORTION);
            }
        });

        db = MyDataBaseHelper.getInstance(this);
        portions = db.getPortions();

        final GridView gridView = (GridView) findViewById(R.id.gridView);
        adapter = new PortionAdapter(this, portions);
        gridView.setAdapter(adapter);
        registerForContextMenu(gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                gridView.setEnabled(false);
                drink(portions.get(position).getSize());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 500);
//                v.findViewById(R.id.imageview).animate().setDuration(750).rotation(360f).withEndAction(new Runnable() {
//                    @Override
//                    public void run() {
//                        finish();
//                    }
//                });
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.portion_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.menu_delete:
                long _id = portions.get(info.position).getId();
                db.deletePortion(_id);
                portions.remove(info.position);
                adapter.notifyDataSetChanged();
                return true;

            case R.id.menu_edit:
                long id = portions.get(info.position).getId();
                Intent intent = new Intent(DrinkActivity.this, PortionCreatorActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("adapter_position", info.position);
                startActivityForResult(intent, REQUEST_EDIT_PORTION);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    private void drink(int amount) {
        db.createEvent(ZonedDateTime.now().toInstant().toString(), amount);
        Snackbar.make(
                findViewById(R.id.content_drink),
                String.format(Locale.US, "drank %d ml of water!", amount),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            long _id = data.getLongExtra("_id", -1);
            int size = data.getIntExtra("size", -1);
            int drawable = data.getIntExtra("drawable", -1);

            switch (requestCode) {
                case REQUEST_NEW_PORTION:
                    portions.add(new Portion(_id, size, drawable));
                    break;

                case REQUEST_EDIT_PORTION:
                    portions.get(data.getIntExtra("adapter_position", 0)).setSize(size);
                    portions.get(data.getIntExtra("adapter_position", 0)).setDrawableId(drawable);
                    break;
            }
            adapter.notifyDataSetChanged();
        }
    }
}
