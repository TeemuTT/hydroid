package fi.teemutt.hydroid.utilities;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by Teemu on 19.11.2016.
 *
 * Forces GridView to generate square cells.
 */

public class GridViewItem extends LinearLayout {

    public GridViewItem(Context context) {
        super(context);
    }

    public GridViewItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridViewItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // Send widthMeasureSpec to super to force square layout.
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //noinspection SuspiciousNameCombination
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
