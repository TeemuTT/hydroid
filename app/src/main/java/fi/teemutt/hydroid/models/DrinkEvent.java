package fi.teemutt.hydroid.models;

import org.threeten.bp.ZonedDateTime;

/**
 * Created by Teemu on 4.11.2016.
 *
 * A single event of drink. Consists of timestamp and the size drank.
 */

public class DrinkEvent {

    private final long id;
    private final ZonedDateTime date;
    private final int size;

    public DrinkEvent(long id, ZonedDateTime date, int size) {
        this.id = id;
        this.date = date;
        this.size = size;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public int getSize() {
        return size;
    }

    public long getId() {
        return id;
    }
}
