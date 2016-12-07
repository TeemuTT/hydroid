package fi.teemutt.hydroid.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

import fi.teemutt.hydroid.R;
import fi.teemutt.hydroid.database.MyDataBaseHelper;
import fi.teemutt.hydroid.models.Portion;
import fi.teemutt.hydroid.utilities.PortionAdapter;


public class DrinkerWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "fi.teemutt.hydroid.widget.DrinkerWidget";
    private static final String PREF_SIZE_PREFIX = "appwidget_size_";
    private static final String PREF_DRAWABLE_PREFIX = "appwidget_drawable_";

    private ArrayList<Portion> portions;

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public DrinkerWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.drinker_widget_configure);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        MyDataBaseHelper db = MyDataBaseHelper.getInstance(this);

        portions = db.getPortions();

        final GridView gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(new PortionAdapter(this, portions));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {
                gridView.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        createWidget(position);
                    }
                }, 500);
//                v.findViewById(R.id.imageview).animate().setDuration(750).rotation(360f).withEndAction(new Runnable() {
//                    @Override
//                    public void run() {
//                        createWidget(position);
//                    }
//                });
            }
        });
    }

    private void createWidget(int position) {
        Portion portion = portions.get(position);
        Context context = DrinkerWidgetConfigureActivity.this;

        // When the button is clicked, save the portion locally.
        savePreferences(context, mAppWidgetId, portion.getSize(), portion.getDrawableId());

        // It is the responsibility of the configuration activity to update the app widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        DrinkerWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    private static void savePreferences(Context context, int appWidgetId, int portionSize, int drawableId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_SIZE_PREFIX + appWidgetId, portionSize);
        prefs.putInt(PREF_DRAWABLE_PREFIX + appWidgetId, drawableId);
        prefs.apply();
    }

    static Portion getPortion(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int portionSize = prefs.getInt(PREF_SIZE_PREFIX + appWidgetId, 0);
        int drawableId = prefs.getInt(PREF_DRAWABLE_PREFIX + appWidgetId, 0);
        return new Portion(portionSize, drawableId);
    }

    static void deletePreferences(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_SIZE_PREFIX + appWidgetId);
        prefs.remove(PREF_DRAWABLE_PREFIX + appWidgetId);
        prefs.apply();
    }

}

