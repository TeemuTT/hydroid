package fi.teemutt.hydroid.utilities;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.ZonedDateTime;

import fi.teemutt.hydroid.database.MyDataBaseHelper;

/**
 * Created by Teemu on 23.11.2016.
 * <br>
 * Service started from widget to write to database even when application is closed.
 */

public class WidgetService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AndroidThreeTen.init(this);

        int size = intent.getIntExtra("portion_size", 0);
        MyDataBaseHelper myDatabase = MyDataBaseHelper.getInstance(this);
        myDatabase.createEvent(ZonedDateTime.now().toInstant().toString(), size);
        Toast.makeText(this, "Drank " + size + " ml!", Toast.LENGTH_SHORT).show();

        stopSelf();
        return START_NOT_STICKY;
    }
}
