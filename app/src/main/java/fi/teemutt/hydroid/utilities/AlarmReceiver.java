package fi.teemutt.hydroid.utilities;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.preference.PreferenceManager;
import android.util.Log;

import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.util.ArrayList;

import fi.teemutt.hydroid.R;
import fi.teemutt.hydroid.activities.MainActivity;
import fi.teemutt.hydroid.database.MyDataBaseHelper;
import fi.teemutt.hydroid.models.DrinkEvent;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Teemu on 12.11.2016.
 *
 */

public class AlarmReceiver extends BroadcastReceiver {

    private final static String TAG = AlarmReceiver.class.getSimpleName();

    private final static int THREE_HOUR_THRESHOLD = 350;
    private final static int NOTIFICATION_ICON = R.mipmap.ic_launcher;
    private final static String NOTIFICATION_TITLE = "Hydroid";
    private final static String TODAY_NO_DRINKS = "You haven't had a drink today!";
    private final static String THREE_HOURS_NO_DRINKS = "You haven't had a drink in the last three hours.";
    private final static String THREE_HOURS_NOT_ENOUGH = "You've only had %d ml of beverages in the last three hours.";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Reapply notifications on boot.
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            SharedPreferences prefs = context.getSharedPreferences("fi.teemutt.hydroid", MODE_PRIVATE);
            if (prefs.getBoolean("reminders", false)) {
                setUpReminders(context);
                return;
            }
        }

        // Only send notifications during daytime.
        LocalTime now = LocalTime.now();
        LocalTime firstNotification = LocalTime.of(11, 30);
        LocalTime lastNotification = LocalTime.of(21, 30);

        if (now.compareTo(firstNotification) > 0 && now.compareTo(lastNotification) < 0) {
            // What no notify about?
            //      Haven't drank today
            //      Have drank, but not in the last 3 hours
            //      Have drank, but only a little

            MyDataBaseHelper db = MyDataBaseHelper.getInstance(context);
            ArrayList<DrinkEvent> events = db.getEventsForDay(ZonedDateTime.now().with(LocalTime.of(0, 0)));
            if (events.size() == 0) {
                // Haven't had a drink today.
                sendNotification(context, TODAY_NO_DRINKS);
                return;
            }

            LocalTime currentTime = LocalTime.now(ZoneId.systemDefault());
            LocalTime threeHoursAgo = currentTime.minusHours(3);
            int threeHoursIntake = 0;
            for (DrinkEvent e : events) {
                if (e.getDate().toLocalTime().compareTo(threeHoursAgo) > 0) {
                    threeHoursIntake += e.getSize();
                }
            }

            // No drinks in three hours.
            if (threeHoursIntake == 0) {
                sendNotification(context, THREE_HOURS_NO_DRINKS);
                return;
            }

            // Haven't had enough in the last three hours.
            if (threeHoursIntake < THREE_HOUR_THRESHOLD) {
                sendNotification(context, String.format(THREE_HOURS_NOT_ENOUGH, threeHoursIntake));
            }
        }
    }

    private void sendNotification(Context context, String content) {
        // Intent to start the app when user clicks on the notification.
        Intent result = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, result, 0);

        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(NOTIFICATION_TITLE);
        builder.setContentText(content);
        builder.setSmallIcon(NOTIFICATION_ICON);
        builder.setContentIntent(resultPendingIntent);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        builder.setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    public static void setUpReminders(Context context) {

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int intervalHour = Integer.parseInt(prefs.getString("pref_notify_interval", "3"));

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime adjusted = now.plusHours(intervalHour);
        long triggerAt = adjusted.toInstant().toEpochMilli();
        Log.i(TAG, "now: " + System.currentTimeMillis() + ", trigger at: " + triggerAt);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAt, AlarmManager.INTERVAL_HOUR * intervalHour, alarmIntent);

        Log.i(TAG, "Reminders set!");
    }

    public static void cancelReminders(Context context) {
        // Construct the same intent used to create the alarm.
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        // Use it to cancel the alarm.
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(alarmIntent);

        Log.i(TAG, "Reminders canceled!");
    }
}
