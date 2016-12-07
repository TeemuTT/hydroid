package fi.teemutt.hydroid.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;

import fi.teemutt.hydroid.R;

/**
 * Created by Teemu on 20.11.2016.
 *
 */

public class IconPickerDialog extends DialogFragment {

    private IconPickerListener mListener;
    private Context context;

    public interface IconPickerListener {
        void iconClicked(int drawable);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        try {
            mListener = (IconPickerListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException();
        }
    }

    public static IconPickerDialog newInstance() {
        return new IconPickerDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.icon_picker_dialog, null);

        // Hard-coded icon choices... Rework later if time / necessary.
        final int[] drawables = {R.drawable.ic_coffee, R.drawable.ic_glass, R.drawable.ic_mug, R.drawable.ic_bottle, R.drawable.ic_can, R.drawable.ic_wine};

        GridLayout root = (GridLayout) view.findViewById(R.id.gridLayout);
        for (int i = 0; i < root.getChildCount(); i++) {
            final int drawableId = i;
            ImageView imageView = (ImageView) root.getChildAt(i);
            imageView.setImageDrawable(AppCompatResources.getDrawable(context, drawables[i]));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.iconClicked(drawables[drawableId]);
                    dismiss();
                }
            });
        }
        builder.setView(view).setTitle("Pick an icon");
        return builder.create();
    }
}
