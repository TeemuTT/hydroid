package fi.teemutt.hydroid.utilities;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import fi.teemutt.hydroid.R;
import fi.teemutt.hydroid.models.Portion;

/**
 * Created by Teemu on 20.11.2016.
 *
 */

public class PortionAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<Portion> portions;

    public PortionAdapter(Context context, ArrayList<Portion> portions) {
        this.context = context;
        this.portions = portions;
    }

    @Override
    public int getCount() {
        return portions.size();
    }

    @Override
    public Object getItem(int i) {
        return portions.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.portion_list_item, parent, false);
        }

        ((TextView) v.findViewById(R.id.tvSize)).setText(String.format("%d ml", portions.get(position).getSize()));
        ((ImageView) v.findViewById(R.id.imageview)).setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), portions.get(position).getDrawableId(), null));

        return v;
    }
}
