package fi.teemutt.hydroid.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.Locale;

import fi.teemutt.hydroid.R;
import fi.teemutt.hydroid.models.Portion;
import fi.teemutt.hydroid.utilities.WidgetService;


public class DrinkerWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Portion portion = DrinkerWidgetConfigureActivity.getPortion(context, appWidgetId);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.drinker_widget);
        views.setImageViewResource(R.id.imageview, portion.getDrawableId());
        views.setTextViewText(R.id.tvSize, String.format(Locale.US, "%d ml", portion.getSize()));

        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra("hello", "Hello there, service!");
        intent.putExtra("portion_size", portion.getSize());
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        views.setOnClickPendingIntent(R.id.imageview, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            DrinkerWidgetConfigureActivity.deletePreferences(context, appWidgetId);
        }
    }
}

