package fi.teemutt.hydroid.models;

/**
 * Created by Teemu on 8.11.2016.
 *
 */

public class Portion {

    private long id;
    private int size;
    private int drawableId;

    public Portion(int size, int drawableId) {
        this.size = size;
        this.drawableId = drawableId;
    }

    public Portion(long id, int size, int drawable) {
        this.id = id;
        this.size = size;
        this.drawableId = drawable;
    }

    public int getSize() {
        return size;
    }

    public int getDrawableId() {
        return drawableId;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getId() {
        return id;
    }

    public void setDrawableId(int drawableId) {
        this.drawableId = drawableId;
    }
}
